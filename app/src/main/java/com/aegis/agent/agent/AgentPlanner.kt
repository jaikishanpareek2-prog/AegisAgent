package com.aegis.agent.agent

import com.aegis.agent.ai.ChatMessage
import com.aegis.agent.ai.ModelConfiguration
import com.aegis.agent.ai.ProviderRegistry
import com.aegis.agent.tools.ToolRegistry
import org.json.JSONArray
import org.json.JSONObject

class AgentPlanner(
    private val providerRegistry: ProviderRegistry,
    private val toolRegistry: ToolRegistry
) {
    suspend fun plan(goal: String): List<AgentStep> {
        val provider = providerRegistry.getActiveProvider()
            ?: return listOf(AgentStep(description = "No AI provider available"))
        val config = providerRegistry.getActiveConfig().copy(
            systemPrompt = """You are a planning module. Given a user goal, output ONLY a JSON array of step objects.
Each object: {"description":"..."}. Keep steps concrete and ordered. Max 10 steps. No markdown."""
        )
        val toolsDesc = toolRegistry.all().joinToString(", ") { it.name }
        val messages = listOf(
            ChatMessage(
                ChatMessage.Role.USER,
                "Available tools: $toolsDesc\n\nGoal: $goal\n\nProduce the step plan as a JSON array."
            )
        )
        return try {
            val resp = provider.chat(messages, config, tools = null)
            parseSteps(resp.message.content)
        } catch (_: Exception) {
            listOf(
                AgentStep(description = "Analyze goal: $goal"),
                AgentStep(description = "Execute with available tools"),
                AgentStep(description = "Verify and report result")
            )
        }
    }

    private fun parseSteps(content: String): List<AgentStep> {
        val start = content.indexOf('[')
        val end = content.lastIndexOf(']')
        if (start < 0 || end <= start) {
            return listOf(AgentStep(description = content.take(200)))
        }
        return try {
            val arr = JSONArray(content.substring(start, end + 1))
            (0 until arr.length()).map { i ->
                val obj = arr.optJSONObject(i)
                AgentStep(description = obj?.optString("description") ?: "Step ${i + 1}")
            }
        } catch (_: Exception) {
            listOf(AgentStep(description = content.take(200)))
        }
    }
}
