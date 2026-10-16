package com.example.mystudyapp.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val trackerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

@Composable
fun ActiveStudyTimeTracker(
    lessonId: String,
    activityName: String? = null,
    authRepository: AuthRepository = AuthRepositoryImpl(),
    progressRepository: ProgressRepository = ProgressRepositoryImpl()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lessonId, activityName) {
        val uid = authRepository.getCurrentUser()?.uid ?: return@LaunchedEffect
        progressRepository.updateCurrentLearningPosition(uid, lessonId, activityName)
    }

    DisposableEffect(lifecycleOwner, lessonId, activityName) {
        var activeSeconds = 0L
        var isSessionActive = true

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        suspend fun flushStudyTime() {
            if (activeSeconds <= 0) return
            val uid = authRepository.getCurrentUser()?.uid ?: return
            val secondsToSave = activeSeconds
            activeSeconds = 0L
            val dateStr = sdf.format(Date())
            withContext(Dispatchers.IO) {
                progressRepository.recordStudyTime(uid, secondsToSave, dateStr)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isSessionActive = true
                }
                Lifecycle.Event.ON_PAUSE -> {
                    isSessionActive = false
                    trackerScope.launch {
                        flushStudyTime()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        val timerJob = trackerScope.launch {
            while (true) {
                delay(1000)
                if (isSessionActive) {
                    activeSeconds++
                    // Flush every 5 seconds for live real-time study time updates!
                    if (activeSeconds >= 5) {
                        flushStudyTime()
                    }
                }
            }
        }

        onDispose {
            isSessionActive = false
            timerJob.cancel()
            lifecycleOwner.lifecycle.removeObserver(observer)
            trackerScope.launch {
                flushStudyTime()
            }
        }
    }
}
