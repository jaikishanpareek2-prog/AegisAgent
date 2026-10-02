package com.aegis.agent.security

import com.aegis.agent.tools.SafetyLevel

class RiskEngine {

    enum class RiskLevel { LOW, MEDIUM, HIGH }

    private val highRiskTools = setOf(
        "send_message", "delete_file", "file_delete", "purchase",
        "change_password", "change_security", "install_app",
        "share_private", "grant_permission", "accessibility_action"
    )

    private val mediumRiskTools = setOf(
        "web_fetch", "app_launch", "memory_store", "file_write",
        "file_move", "notification_reply", "open_url"
    )

    fun classifyTool(name: String): RiskLevel = when {
        name in highRiskTools -> RiskLevel.HIGH
        name in mediumRiskTools -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }

    fun fromSafety(level: SafetyLevel): RiskLevel = when (level) {
        SafetyLevel.LOW -> RiskLevel.LOW
        SafetyLevel.MEDIUM -> RiskLevel.MEDIUM
        SafetyLevel.HIGH -> RiskLevel.HIGH
    }

    fun requiresConfirmation(level: RiskLevel): Boolean =
        level == RiskLevel.HIGH
}
