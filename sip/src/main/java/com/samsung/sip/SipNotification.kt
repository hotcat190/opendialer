package com.samsung.sip

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object SipNotification {
    const val CHANNEL_ID = "sip_service"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "SIP Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps SIP calling available"
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(context: Context): Notification {
        return NotificationCompat.Builder(context,CHANNEL_ID)
            .setContentTitle("SIP Service")
            .setContentText("Waiting for incoming calls")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}