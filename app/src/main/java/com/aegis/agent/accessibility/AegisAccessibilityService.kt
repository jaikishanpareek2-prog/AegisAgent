package com.aegis.agent.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.aegis.agent.core.AppContainer

/**
 * Observes screen content via AccessibilityService for agent observation loop.
 * Does not perform UI automation unless explicitly approved by RiskEngine.
 */
class AegisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AppContainer.screenObserver?.onServiceConnected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                AppContainer.screenObserver?.onEvent(event, rootInActiveWindow)
            }
        }
    }

    override fun onInterrupt() {
        // no-op
    }

    override fun onDestroy() {
        instance = null
        AppContainer.screenObserver?.onServiceDisconnected()
        super.onDestroy()
    }

    companion object {
        @Volatile
        var instance: AegisAccessibilityService? = null
            private set

        fun isEnabled(): Boolean = instance != null

        fun getRoot(): AccessibilityNodeInfo? = instance?.rootInActiveWindow
    }
}
