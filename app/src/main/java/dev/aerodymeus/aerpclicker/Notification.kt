package dev.aerodymeus.aerpclicker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class Notification : Application() {
    companion object {
        const val CHANNEL_ID = "app_update_channel"
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Benachrichtigungen nach einem App-Update"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}


