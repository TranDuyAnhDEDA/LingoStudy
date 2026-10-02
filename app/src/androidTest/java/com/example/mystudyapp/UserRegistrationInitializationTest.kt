package com.example.mystudyapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mystudyapp.data.model.UserProfile
import com.example.mystudyapp.data.model.UserProgress
import com.example.mystudyapp.data.repository.AuthRepositoryImpl
import com.example.mystudyapp.data.repository.ProgressRepositoryImpl
import com.example.mystudyapp.data.repository.UserRepositoryImpl
import com.example.mystudyapp.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRegistrationInitializationTest {

    @Test
    fun testDirectUserProfileAndProgressInitialization() = runBlocking {
        val testUid = "test_user_init_${System.currentTimeMillis()}"
        val testEmail = "test_init@example.com"
        val testDisplayName = "Direct Test User"

        val userRepository = UserRepositoryImpl()
        val progressRepository = ProgressRepositoryImpl()

        // 1. Create UserProfile
        val profile = UserProfile(
            uid = testUid,
            displayName = testDisplayName,
            email = testEmail,
            levelId = "level_3",
            levelName = "Explorer",
            xp = 0,
            streak = 0,
            longestStreak = 0,
            totalLessonsCompleted = 0
        )

        val profileResult = userRepository.createUserProfile(profile)
        assertTrue("createUserProfile failed: ${profileResult.exceptionOrNull()?.message}", profileResult.isSuccess)

        // 2. Create UserProgress
        val initialProgress = UserProgress(
            userId = testUid,
            vocabularyProgress = 0,
            grammarProgress = 0,
            listeningProgress = 0,
            pronunciationProgress = 0,
            currentLessonId = "lesson_3_1_1",
            completedLessons = 0,
            totalLessons = 3
        )

        val progressResult = progressRepository.createUserProgress(initialProgress)
        assertTrue("createUserProgress failed: ${progressResult.exceptionOrNull()?.message}", progressResult.isSuccess)

        val firestore = FirebaseFirestore.getInstance()

        // 3. Verify users/{testUid} document in Firestore
        val userDoc = firestore.collection("users").document(testUid).get().await()
        assertTrue("users/$testUid document should exist", userDoc.exists())
        assertEquals("displayName mismatch", testDisplayName, userDoc.getString("displayName"))
        assertEquals("email mismatch", testEmail, userDoc.getString("email"))
        assertEquals("levelId mismatch", "level_3", userDoc.getString("levelId"))
        assertEquals("levelName mismatch", "Explorer", userDoc.getString("levelName"))
        assertEquals("xp mismatch", 0L, userDoc.getLong("xp"))
        assertEquals("streak mismatch", 0L, userDoc.getLong("streak"))
        assertEquals("longestStreak mismatch", 0L, userDoc.getLong("longestStreak"))
        assertEquals("totalLessonsCompleted mismatch", 0L, userDoc.getLong("totalLessonsCompleted"))
        assertNotNull("createdAt timestamp should exist", userDoc.getTimestamp("createdAt"))
        assertNotNull("lastActiveAt timestamp should exist", userDoc.getTimestamp("lastActiveAt"))
        assertNull("Password MUST NOT be stored in Firestore", userDoc.getString("password"))

        // 4. Verify user_progress/{testUid} document in Firestore
        val progressDoc = firestore.collection("user_progress").document(testUid).get().await()
        assertTrue("user_progress/$testUid document should exist", progressDoc.exists())
        assertEquals("vocabularyProgress mismatch", 0L, progressDoc.getLong("vocabularyProgress"))
        assertEquals("grammarProgress mismatch", 0L, progressDoc.getLong("grammarProgress"))
        assertEquals("listeningProgress mismatch", 0L, progressDoc.getLong("listeningProgress"))
        assertEquals("pronunciationProgress mismatch", 0L, progressDoc.getLong("pronunciationProgress"))
        assertEquals("currentLessonId mismatch", "lesson_3_1_1", progressDoc.getString("currentLessonId"))
        assertEquals("completedLessons mismatch", 0L, progressDoc.getLong("completedLessons"))
        assertEquals("totalLessons mismatch", 3L, progressDoc.getLong("totalLessons"))
    }

    @Test
    fun testRegistrationFlowHandlesAuthErrorsGracefully() = runBlocking {
        val testEmail = "unenabled_auth@example.com"
        val testPassword = "password123"
        val testDisplayName = "Test User"

        val authRepository = AuthRepositoryImpl()
        val userRepository = UserRepositoryImpl()
        val progressRepository = ProgressRepositoryImpl()

        val viewModel = AuthViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            progressRepository = progressRepository
        )

        viewModel.register(testEmail, testPassword, testDisplayName)

        var attempts = 0
        while (!viewModel.uiState.value.isSuccess && viewModel.uiState.value.errorMessage == null && attempts < 100) {
            delay(100)
            attempts++
        }

        val state = viewModel.uiState.value
        if (!state.isSuccess) {
            assertNotNull("Error message should be set on failure", state.errorMessage)
            println("Successfully caught Auth error: ${state.errorMessage}")
        }
    }
}
