package com.aegis.agent.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val goal: String,
    val status: String,
    val stepsJson: String,
    val currentStepIndex: Int = 0,
    val result: String? = null,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val triggerType: String,
    val triggerConfigJson: String,
    val actionPrompt: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
