package com.example.mystudyapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mystudyapp.data.repository.AuthRepository
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepository
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import com.example.mystudyapp.data.repository.UserRepository
import com.example.mystudyapp.data.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // Khi thiết bị khởi động lại, đặt lại lịch nhắc định kỳ 6 tiếng
            StudyReminderScheduler.schedulePeriodicReminder(context)
            return
        }

        if (StudyReminderScheduler.ACTION_STUDY_REMINDER == action) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    checkAndSendStudyReminder(context)
                } catch (e: Exception) {
                    // Tránh crash receiver
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun checkAndSendStudyReminder(
        context: Context,
        authRepository: AuthRepository = AuthRepositoryImpl(),
        userRepository: UserRepository = UserRepositoryImpl(),
        progressRepository: ProgressRepository = ProgressRepositoryImpl()
    ) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val uid = currentUser.uid

        // Lấy mục tiêu học hàng ngày (phút) của người dùng từ UserProfile (mặc định 15 hoặc 30 phút)
        val profile = withTimeoutOrNull(5000) {
            userRepository.getUserProfile(uid).firstOrNull()?.getOrNull()
        }
        val dailyGoalMinutes = profile?.dailyGoalMinutes?.takeIf { it > 0 } ?: 15

        // Lấy thời gian đã học thực tế trong ngày hôm nay
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val dailyStudy = withTimeoutOrNull(5000) {
            progressRepository.getDailyStudyTime(uid, todayStr).firstOrNull()?.getOrNull()
        }
        val todaySeconds = dailyStudy?.totalSeconds ?: 0L
        val todayMinutes = (todaySeconds / 60).toInt()

        // Chỉ gửi thông báo nếu người dùng chưa đạt chỉ tiêu học đã đề ra
        if (todayMinutes < dailyGoalMinutes) {
            NotificationHelper.showStudyGoalReminder(
                context = context,
                todayMinutes = todayMinutes,
                dailyGoalMinutes = dailyGoalMinutes
            )
        }
    }
}
