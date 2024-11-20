package com.example.sr13

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Próba wyświetlenia powiadomienia") // Dodanie logowania

        // Sprawdzenie uprawnienia do wysyłania powiadomień
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationReceiver", "Brak uprawnienia POST_NOTIFICATIONS")
            return // Wyjście z metody, jeśli brak uprawnienia
        }

        // Tworzenie i wysyłanie powiadomienia
        val builder = NotificationCompat.Builder(context, "reminderChannel")
            .setSmallIcon(R.drawable.ic_notification) // Ustaw ikonę powiadomienia
            .setContentTitle("Przypomnienie o raporcie")
            .setContentText("Dodaj raport z badania.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(200, builder.build()) // Wyślij powiadomienie
        Log.d("NotificationReceiver", "Powiadomienie zostało wyświetlone") // Potwierdzenie wysyłki powiadomienia
    }
}
