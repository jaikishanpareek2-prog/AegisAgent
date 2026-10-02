package com.aegis.agent.tools

import com.aegis.agent.ai.ToolDefinition
import org.json.JSONObject

data class ToolResult(
    val success: Boolean,
    val output: String,
    val data: Map<String, Any?> = emptyMap()
)

enum class SafetyLevel { LOW, MEDIUM, HIGH }

data class Skill(
    val name: String,
    val description: String,
    val requiredPermissions: List<String> = emptyList(),
    val inputSchema: String,
    val safetyLevel: SafetyLevel = SafetyLevel.LOW,
    val execute: suspend (args: JSONObject) -> ToolResult
)

class ToolRegistry {
    private val skills = linkedMapOf<String, Skill>()

    fun register(skill: Skill) {
        skills[skill.name] = skill
    }

    fun get(name: String): Skill? = skills[name]

    fun all(): List<Skill> = skills.values.toList()

    fun toToolDefinitions(): List<ToolDefinition> =
        skills.values.map {
            ToolDefinition(
                name = it.name,
                description = it.description,
                parametersJsonSchema = it.inputSchema
            )
        }

    suspend fun execute(name: String, argumentsJson: String): ToolResult {
        val skill = skills[name] ?: return ToolResult(false, "Unknown tool: $name")
        val args = try {
            JSONObject(argumentsJson.ifBlank { "{}" })
        } catch (e: Exception) {
            return ToolResult(false, "Invalid JSON arguments: ${e.message}")
        }
        return try {
            skill.execute(args)
        } catch (e: Exception) {
            ToolResult(false, "Execution error: ${e.message}")
        }
    }
}
