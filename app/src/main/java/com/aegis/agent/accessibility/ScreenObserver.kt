package com.aegis.agent.accessibility

/**
 * Unified interface for screen understanding.
 */
class ScreenObserver {

    fun isAvailable(): Boolean = AegisAccessibilityService.instance != null

    fun observe(): AegisAccessibilityService.ScreenSnapshot? {
        return AegisAccessibilityService.instance?.snapshot()
    }

    fun describeForAgent(): String {
        val snap = observe() ?: return "Accessibility service not enabled. Screen observation unavailable."
        return buildString {
            appendLine("Package: ${snap.packageName ?: "unknown"}")
            appendLine("Visible text (sample):")
            snap.visibleText.take(30).forEach { appendLine("  - $it") }
            appendLine("Clickable elements:")
            snap.clickable.take(20).forEach {
                appendLine("  - \"${it.text.ifBlank { it.contentDescription }}\" ${it.bounds}")
            }
        }
    }
}
