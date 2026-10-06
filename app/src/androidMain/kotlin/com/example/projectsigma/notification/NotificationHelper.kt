package com.example.projectsigma.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "locapop_notifications_channel"
    private const val CHANNEL_NAME = "LocaPop Event & Friend Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for event starting reminders and friend requests on LocaPop"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Notification channel created.")
        }
    }

    fun sendDeviceNotification(context: Context, title: String, message: String, notificationId: Int = 101) {
        try {
            createNotificationChannel(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            Log.d("NotificationHelper", "Posted system notification: $title - $message")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Missing notification permission: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error posting notification: ${e.message}")
        }
    }
}
