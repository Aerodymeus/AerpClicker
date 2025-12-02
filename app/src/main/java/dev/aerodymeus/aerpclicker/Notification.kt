package dev.aerodymeus.aerpclicker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class Notification : Application() {

    companion object {
        const val UPDATE_CHANNEL_ID = "update_notification_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Channel nur auf Android 8.0 (API 26) und höher erstellen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Updates" // Name, der in den App-Einstellungen angezeigt wird
            val descriptionText = "Notifications about new app versions and features"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(UPDATE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Registriere den Channel beim System
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}