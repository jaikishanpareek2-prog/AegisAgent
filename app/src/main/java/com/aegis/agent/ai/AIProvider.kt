package com.aegis.agent.ai

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over remote/local AI model providers.
 * Implementations: Gemini, OpenAI-compatible, Anthropic-compatible, OpenRouter, Ollama.
 */
interface AIProvider {
    val id: String
    val displayName: String
    val supportsStreaming: Boolean
    val supportsToolCalling: Boolean

    suspend fun listModels(): List<ModelInfo>

    suspend fun chat(
        messages: List<ChatMessage>,
        config: ModelConfiguration,
        tools: List<ToolDefinition>? = null
    ): ChatResponse

    fun chatStream(
        messages: List<ChatMessage>,
        config: ModelConfiguration,
        tools: List<ToolDefinition>? = null
    ): Flow<StreamChunk>
}

data class ModelInfo(
    val id: String,
    val name: String,
    val contextWindow: Int = 8192,
    val supportsTools: Boolean = true
)

data class ModelConfiguration(
    val modelId: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val topP: Float = 1.0f,
    val systemPrompt: String? = null
)

data class ChatMessage(
    val role: Role,
    val content: String,
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val name: String? = null
) {
    enum class Role { SYSTEM, USER, ASSISTANT, TOOL }
}

data class ToolDefinition(
    val name: String,
    val description: String,
    val parametersJsonSchema: String
)

data class ToolCall(
    val id: String,
    val name: String,
    val argumentsJson: String
)

data class ChatResponse(
    val message: ChatMessage,
    val usage: TokenUsage? = null,
    val finishReason: String? = null
)

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

sealed class StreamChunk {
    data class TextDelta(val text: String) : StreamChunk()
    data class ToolCallDelta(val toolCall: ToolCall) : StreamChunk()
    data class Finished(val response: ChatResponse) : StreamChunk()
    data class Error(val message: String, val cause: Throwable? = null) : StreamChunk()
}
