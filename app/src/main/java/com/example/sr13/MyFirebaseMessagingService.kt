package com.example.sr13

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sr13.patient.PatientMainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * This service handles incoming Firebase Cloud Messaging (FCM) messages.
 * It processes received messages and displays notifications to the user.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when an FCM message is received.
     * Logs the received data, extracts the sender's name and message content,
     * and triggers a notification display.
     *
     * @param remoteMessage The message received from FCM, containing data and notification payloads.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Otrzymano wiadomość: ${remoteMessage.data}")

        val senderName = remoteMessage.data["senderName"] ?: "Nieznany użytkownik"
        val message = remoteMessage.data["message"] ?: "Nowa wiadomość"

        Log.d("FCM", "Nadawca: $senderName, Wiadomość: $message")

        showNotification(senderName, "$senderName wysłał(a) ci wiadomość")
    }



    /**
     * Displays a notification to the user.
     * Constructs and sends a notification with the given title and body.
     * Checks for notification permissions before displaying the notification.
     *
     * @param title The title of the notification.
     * @param body The content text of the notification.
     */
    private fun showNotification(title: String, body: String) {
        Log.d("FCM", "Wyświetlanie powiadomienia: Tytuł=$title, Treść=$body")
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "messages_channel"

        val intent = Intent(this, PatientMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d("FCM", "Powiadomienie wysłane")
        } else {
            Log.e("FCM", "Brak uprawnień do wyświetlenia powiadomienia")
        }
    }
}
