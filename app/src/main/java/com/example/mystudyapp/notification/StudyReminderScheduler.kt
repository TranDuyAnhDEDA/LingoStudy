package com.example.mystudyapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

object StudyReminderScheduler {

    const val ACTION_STUDY_REMINDER = "com.example.mystudyapp.ACTION_STUDY_REMINDER"
    private const val REQUEST_CODE = 2001

    // Chu kỳ lặp lại: 6 tiếng (6 hours = 21,600,000 milliseconds)
    const val INTERVAL_SIX_HOURS_MILLIS: Long = 6L * 60L * 60L * 1000L

    /**
     * Lên lịch báo thức định kỳ mỗi 6 tiếng.
     * Sử dụng AlarmManager.setInexactRepeating để tiết kiệm pin và đảm bảo kích hoạt ổn định.
     */
    fun schedulePeriodicReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_STUDY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = SystemClock.elapsedRealtime() + INTERVAL_SIX_HOURS_MILLIS

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                INTERVAL_SIX_HOURS_MILLIS,
                pendingIntent
            )
        } catch (e: Exception) {
            // Trường hợp bảo mật hoặc thiết bị đặc thù
        }
    }

    /**
     * Hủy lịch báo thức nhắc nhở định kỳ.
     */
    fun cancelPeriodicReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_STUDY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
