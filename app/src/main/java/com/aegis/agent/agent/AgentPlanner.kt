package com.aegis.agent.agent

import com.aegis.agent.ai.*
import com.aegis.agent.security.RiskEngine
import com.aegis.agent.tools.ToolRegistry
import org.json.JSONArray

class AgentPlanner(
    private val providerRegistry: ProviderRegistry,
    private val toolRegistry: ToolRegistry
) {
    suspend fun plan(goal: String): List<AgentStep> {
        val provider = providerRegistry.getActiveProvider()
            ?: return listOf(AgentStep(description = "No AI provider available"))
        val config = providerRegistry.getActiveConfig().copy(
            systemPrompt = """You are Aegis planning module. Output ONLY a JSON array of concrete ordered steps.
Each object must be {"description":"..."}. Maximum 10 steps. No markdown."""
        )
        val toolsDesc = toolRegistry.all().joinToString(", ") { it.name }
        val messages = listOf(ChatMessage(ChatMessage.Role.USER, "Available tools: $toolsDesc\nGoal: $goal\nProduce the step plan as JSON."))
        return try { parseSteps(provider.chat(messages, config).message.content) }
        catch (_: Exception) {
            listOf(
                AgentStep(description = "Analyze goal"),
                AgentStep(description = "Use available tools where appropriate"),
                AgentStep(description = "Verify and report the result")
            )
        }
    }

    suspend fun execute(goal: String): String {
        val provider = providerRegistry.getActiveProvider()
            ?: return "No AI provider configured."
        val config = providerRegistry.getActiveConfig().copy(
            systemPrompt = """You are Aegis, an autonomous Android agent. Work toward the user's goal using the available tools.
Use tools when useful. Never claim a tool ran unless it returned a result. Keep actions bounded and stop when the goal is satisfied.
High-risk actions require confirmation and are not available without explicit approval."""
        )
        val messages = mutableListOf(ChatMessage(ChatMessage.Role.USER, goal))
        repeat(8) {
            val response = provider.chat(messages, config, toolRegistry.toToolDefinitions())
            messages += response.message
            val calls = response.message.toolCalls.orEmpty()
            if (calls.isEmpty()) return response.message.content.ifBlank { "Task completed without a textual result." }
            for (call in calls) {
                val skill = toolRegistry.get(call.name)
                    ?: return "Agent requested unavailable tool: ${call.name}"
                if (RiskEngine().requiresConfirmation(RiskEngine().fromSafety(skill.safetyLevel))) {
                    return "Confirmation required before using high-risk tool: ${call.name}"
                }
                val result = toolRegistry.execute(call.name, call.argumentsJson)
                messages += ChatMessage(
                    role = ChatMessage.Role.TOOL,
                    content = result.output,
                    toolCallId = call.id,
                    name = call.name
                )
            }
        }
        return "Execution stopped after the maximum number of agent cycles."
    }

    private fun parseSteps(content: String): List<AgentStep> {
        val start = content.indexOf('[')
        val end = content.lastIndexOf(']')
        if (start < 0 || end <= start) return listOf(AgentStep(description = content.take(200)))
        return try {
            val arr = JSONArray(content.substring(start, end + 1))
            (0 until arr.length()).map { i ->
                AgentStep(description = arr.optJSONObject(i)?.optString("description") ?: "Step ${i + 1}")
            }
        } catch (_: Exception) { listOf(AgentStep(description = content.take(200))) }
    }
}
