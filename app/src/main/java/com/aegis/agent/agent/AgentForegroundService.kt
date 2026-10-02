package com.aegis.agent.agent

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aegis.agent.AegisApplication
import com.aegis.agent.MainActivity
import com.aegis.agent.R

/**
 * Lightweight foreground service to keep long-running agent work alive when needed.
 * Started only when a multi-step task is actively executing.
 */
class AgentForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Aegis is working…")
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, AegisApplication.CHANNEL_AGENT)
            .setContentTitle("Aegis Agent")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
