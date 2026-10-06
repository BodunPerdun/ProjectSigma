package com.example.projectsigma.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebaseMessaging", "Refreshed FCM token: $token")
        NotificationHelper.createNotificationChannel(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MyFirebaseMessaging", "FCM Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "LocaPop Notification"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "You have a new update in LocaPop"

        NotificationHelper.sendDeviceNotification(
            context = applicationContext,
            title = title,
            message = body
        )
    }
}
