package com.example.mystudyapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mystudyapp.MainActivity
import com.example.mystudyapp.R

object NotificationHelper {

    const val CHANNEL_ID = "lingo_study_reminder_channel"
    private const val CHANNEL_NAME = "Nhắc nhở học tập"
    private const val CHANNEL_DESC = "Thông báo nhắc nhở người dùng hoàn thành chỉ tiêu học tiếng Anh mỗi ngày"
    private const val NOTIFICATION_ID = 1001

    /**
     * Khởi tạo Notification Channel cho Android 8.0 (API 26) trở lên.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Hiển thị thông báo nhắc nhở người dùng học tập nếu chưa đạt chỉ tiêu.
     */
    fun showStudyGoalReminder(context: Context, todayMinutes: Int, dailyGoalMinutes: Int) {
        // Đảm bảo notification channel đã được khởi tạo
        createNotificationChannel(context)

        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                return
            }
        }

        // Tạo intent mở app khi người dùng nhấn vào thông báo
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "LingoStudy: Đã đến giờ học rồi! 🦜"
        val remainingMinutes = (dailyGoalMinutes - todayMinutes).coerceAtLeast(1)
        val contentText = if (todayMinutes <= 0) {
            "Hôm nay bạn chưa học tiếng Anh. Hãy dành $dailyGoalMinutes phút cùng Vẹt hoàn thành mục tiêu nhé!"
        } else {
            "Hôm nay bạn đã học $todayMinutes/$dailyGoalMinutes phút. Còn $remainingMinutes phút nữa là đạt chỉ tiêu rồi!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Trường hợp người dùng thu hồi quyền thông báo
        } catch (e: Exception) {
            // Bỏ qua lỗi hiển thị thông báo
        }
    }
}
