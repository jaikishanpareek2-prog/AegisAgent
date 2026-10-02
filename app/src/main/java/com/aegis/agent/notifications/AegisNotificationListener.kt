package com.aegis.agent.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AegisNotificationListener : NotificationListenerService() {

    data class NotifItem(
        val key: String,
        val packageName: String,
        val title: String,
        val text: String,
        val postTime: Long
    )

    override fun onListenerConnected() {
        instance = this
        _connected.value = true
        refresh()
    }

    override fun onListenerDisconnected() {
        instance = null
        _connected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refresh()
    }

    private fun refresh() {
        try {
            val list = activeNotifications?.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                NotifItem(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = extras.getCharSequence("android.title")?.toString() ?: "",
                    text = extras.getCharSequence("android.text")?.toString() ?: "",
                    postTime = sbn.postTime
                )
            } ?: emptyList()
            _notifications.value = list
        } catch (_: Exception) {
        }
    }

    fun summarize(): String {
        val items = _notifications.value
        if (items.isEmpty()) return "No active notifications."
        return items.take(15).joinToString("\n") {
            "[${it.packageName}] ${it.title}: ${it.text}"
        }
    }

    companion object {
        @Volatile var instance: AegisNotificationListener? = null
            private set

        private val _connected = MutableStateFlow(false)
        val connected: StateFlow<Boolean> = _connected.asStateFlow()

        private val _notifications = MutableStateFlow<List<NotifItem>>(emptyList())
        val notifications: StateFlow<List<NotifItem>> = _notifications.asStateFlow()
    }
}
