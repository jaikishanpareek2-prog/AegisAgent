package com.aegis.agent.agent

import java.util.UUID

enum class TaskStatus {
    PENDING, PLANNING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

enum class StepStatus {
    PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
}

data class AgentStep(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    var status: StepStatus = StepStatus.PENDING,
    var result: String? = null,
    var error: String? = null
)

data class AgentTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val goal: String,
    var status: TaskStatus = TaskStatus.PENDING,
    val steps: MutableList<AgentStep> = mutableListOf(),
    var currentStepIndex: Int = 0,
    var result: String? = null,
    var error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
