package com.aegis.agent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.aegis.agent.core.AppContainer
import com.aegis.agent.data.AppDatabase

class AegisApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        container = AppContainer(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_AGENT,
                    getString(R.string.notification_channel_agent),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_AUTOMATION,
                    getString(R.string.notification_channel_automation),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    companion object {
        const val CHANNEL_AGENT = "aegis_agent"
        const val CHANNEL_AUTOMATION = "aegis_automation"

        @Volatile
        private var instance: AegisApplication? = null

        fun get(): AegisApplication = instance!!
    }
}
