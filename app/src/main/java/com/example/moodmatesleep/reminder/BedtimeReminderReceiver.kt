package com.example.moodmatesleep.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moodmatesleep.R

class BedtimeReminderReceiver :
    BroadcastReceiver() {

    companion object {

        private const val CHANNEL_ID =
            "bedtime_reminder_channel"

        private const val NOTIFICATION_ID =
            1002
    }

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        createNotificationChannel(
            context
        )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.mipmap.ic_launcher
                )
                .setContentTitle(
                    "Time to wind down"
                )
                .setContentText(
                    "Your bedtime is getting close. Start your relaxing sleep routine."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .build()

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            NotificationManagerCompat
                .from(context)
                .notify(
                    NOTIFICATION_ID,
                    notification
                )
        }

        scheduleNextReminder(
            context
        )
    }

    private fun createNotificationChannel(
        context: Context
    ) {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Bedtime Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    "Reminders for your planned bedtime."
            }

        manager.createNotificationChannel(
            channel
        )
    }

    private fun scheduleNextReminder(
        context: Context
    ) {

        val preferences =
            context.getSharedPreferences(
                "moodmate_preferences",
                Context.MODE_PRIVATE
            )

        val enabled =
            preferences.getBoolean(
                "bedtime_reminder_enabled",
                false
            )

        if (!enabled) {
            return
        }

        val hour =
            preferences.getInt(
                "bedtime_reminder_hour",
                22
            )

        val minute =
            preferences.getInt(
                "bedtime_reminder_minute",
                0
            )

        BedtimeReminderScheduler.schedule(
            context,
            hour,
            minute
        )
    }
}