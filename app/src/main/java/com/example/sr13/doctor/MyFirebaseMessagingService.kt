import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sr13.R
import com.example.sr13.patient.PatientAddReportActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service to handle Firebase Cloud Messaging (FCM) messages.
 * This service processes incoming messages and displays notifications.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new message is received from Firebase Cloud Messaging.
     *
     * @param remoteMessage The message received from FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload and display it.
        remoteMessage.notification?.let {
            sendNotification(it.body ?: "New notification")
        }
    }

    /**
     * Creates and displays a notification for the received FCM message.
     *
     * @param messageBody The content of the notification.
     */
    private fun sendNotification(messageBody: String) {
        // Create an intent to launch PatientAddReportActivity when the notification is tapped.
        val intent = Intent(this, PatientAddReportActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Define the notification channel ID.
        val channelId = "Default_Channel"

        // Set the default notification sound.
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build the notification.
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Icon to be displayed in the notification
            .setContentTitle("FCM Message") // Title of the notification
            .setContentText(messageBody) // Message content
            .setAutoCancel(true) // Dismiss the notification when tapped
            .setSound(defaultSoundUri) // Set the notification sound
            .setContentIntent(pendingIntent) // Set the action on notification tap

        // Get the NotificationManager system service.
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For devices running Android O and above, create a notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title", // User-visible name for the channel
                NotificationManager.IMPORTANCE_DEFAULT // Importance level for the channel
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification with an ID of 0.
        notificationManager.notify(0, notificationBuilder.build())
    }
}