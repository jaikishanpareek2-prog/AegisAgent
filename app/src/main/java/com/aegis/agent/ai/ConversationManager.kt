package com.aegis.agent.ai

import com.aegis.agent.agent.AgentStepResult
import com.aegis.agent.agent.NextAction
import com.aegis.agent.tools.ToolSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates multi-turn conversations and tool-calling loops with the active AI provider.
 */
class ConversationManager(
    private val providerRegistry: ProviderRegistry
) {

    private val messages = mutableListOf<ChatMessage>()

    fun clear() {
        messages.clear()
    }

    fun history(): List<ChatMessage> = messages.toList()

    suspend fun chat(userText: String, systemPrompt: String? = null): String = withContext(Dispatchers.IO) {
        if (systemPrompt != null && messages.none { it.role == "system" }) {
            messages.add(0, ChatMessage(role = "system", content = systemPrompt))
        }
        messages.add(ChatMessage(role = "user", content = userText))

        val provider = providerRegistry.activeProvider()
            ?: return@withContext "No AI provider configured. Open Settings to add an API key or Ollama endpoint."

        val response = provider.chat(messages, tools = emptyList())
        val assistantText = response.content.orEmpty()
        messages.add(ChatMessage(role = "assistant", content = assistantText))
        assistantText
    }

    suspend fun planNextAction(
        goal: String,
        history: List<AgentStepResult>,
        observations: String,
        availableTools: List<ToolSpec>
    ): NextAction = withContext(Dispatchers.IO) {
        val provider = providerRegistry.activeProvider()
            ?: return@withContext NextAction.Error("No AI provider configured")

        val system = buildString {
            appendLine("You are Aegis Agent planner. Respond ONLY with a single JSON object.")
            appendLine("Schema options:")
            appendLine("{\"type\":\"tool\",\"tool\":\"name\",\"arguments\":{...}}")
            appendLine("{\"type\":\"finish\",\"summary\":\"...\"}")
            appendLine("Available tools:")
            availableTools.forEach { t ->
                appendLine("- ${t.name}: ${t.description}")
            }
        }

        val user = buildString {
            appendLine("Goal: $goal")
            appendLine("Observations: $observations")
            if (history.isNotEmpty()) {
                appendLine("Previous steps:")
                history.takeLast(6).forEach { s ->
                    appendLine("${s.step}. ${s.action} -> ${s.output.take(300)}")
                }
            }
            appendLine("Decide the next single action.")
        }

        val msgs = listOf(
            ChatMessage(role = "system", content = system),
            ChatMessage(role = "user", content = user)
        )

        val response = provider.chat(msgs, tools = availableTools.map { it.toProviderTool() })
        parseNextAction(response.content.orEmpty(), response.toolCalls)
    }

    private fun parseNextAction(content: String, toolCalls: List<ToolCall>?): NextAction {
        if (!toolCalls.isNullOrEmpty()) {
            val tc = toolCalls.first()
            return NextAction.ToolCall(tc.name, tc.arguments)
        }
        val trimmed = content.trim()
        return try {
            // Minimal JSON parse without extra deps
            when {
                trimmed.contains("\"type\"\s*:\s*\"finish\"".toRegex()) -> {
                    val summary = Regex("\"summary\"\s*:\s*\"([^\"]*)\"").find(trimmed)?.groupValues?.getOrNull(1)
                        ?: "Done"
                    NextAction.Finish(summary)
                }
                trimmed.contains("\"type\"\s*:\s*\"tool\"".toRegex()) -> {
                    val tool = Regex("\"tool\"\s*:\s*\"([^\"]*)\"").find(trimmed)?.groupValues?.getOrNull(1)
                        ?: return NextAction.Error("Missing tool name")
                    val argsRaw = Regex("\"arguments\"\s*:\s*(\{[^}]*\})").find(trimmed)?.groupValues?.getOrNull(1)
                        ?: "{}"
                    NextAction.ToolCall(tool, parseArgs(argsRaw))
                }
                else -> NextAction.Finish(trimmed.take(500))
            }
        } catch (e: Exception) {
            NextAction.Error("Failed to parse planner response: ${e.message}")
        }
    }

    private fun parseArgs(raw: String): Map<String, Any?> {
        // Very small fallback parser for flat string/number values
        val map = mutableMapOf<String, Any?>()
        Regex("\"([^\"]+)\"\s*:\s*\"([^\"]*)\"").findAll(raw).forEach { m ->
            map[m.groupValues[1]] = m.groupValues[2]
        }
        Regex("\"([^\"]+)\"\s*:\s*([0-9.]+)").findAll(raw).forEach { m ->
            map[m.groupValues[1]] = m.groupValues[2].toDoubleOrNull() ?: m.groupValues[2]
        }
        return map
    }
}
