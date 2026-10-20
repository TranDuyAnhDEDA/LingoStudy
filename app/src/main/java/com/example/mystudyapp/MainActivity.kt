package com.example.mystudyapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mystudyapp.data.seed.FirestoreSeeder
import com.example.mystudyapp.notification.NotificationHelper
import com.example.mystudyapp.notification.StudyReminderScheduler
import com.example.mystudyapp.ui.navigation.AppNavigation
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Kết quả xin quyền thông báo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo kênh thông báo và đặt lịch nhắc nhở mỗi 6 tiếng nếu chưa đạt chỉ tiêu
        NotificationHelper.createNotificationChannel(this)
        StudyReminderScheduler.schedulePeriodicReminder(this)

        // Xin quyền gửi thông báo trên Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FirestoreSeeder.seedSampleData()
            } catch (e: Exception) {
                // Ignore background seed exception if offline
            }
        }

        setContent {
            MyStudyAppTheme {
                AppNavigation()
            }
        }
    }
}

