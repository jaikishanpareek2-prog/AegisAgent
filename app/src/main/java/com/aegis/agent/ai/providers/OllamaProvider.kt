package com.aegis.agent.ai.providers

import com.aegis.agent.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OllamaProvider(
    private val baseUrlProvider: suspend () -> String
) : AIProvider {

    override val id = "ollama"
    override val displayName = "Ollama (Local)"
    override val supportsStreaming = true
    override val supportsToolCalling = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private suspend fun base() = baseUrlProvider().trimEnd('/').ifBlank { "http://127.0.0.1:11434" }

    override suspend fun listModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("${base()}/api/tags").get().build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val models = JSONObject(body).optJSONArray("models") ?: return@withContext emptyList()
                (0 until models.length()).mapNotNull { i ->
                    val m = models.optJSONObject(i) ?: return@mapNotNull null
                    val name = m.optString("name")
                    if (name.isBlank()) null else ModelInfo(name, name)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        config: ModelConfiguration,
        tools: List<ToolDefinition>?
    ): ChatResponse = withContext(Dispatchers.IO) {
        val url = "${base()}/api/chat"
        val body = JSONObject()
        body.put("model", config.modelId.ifBlank { "llama3.2" })
        body.put("stream", false)
        val msgs = JSONArray()
        config.systemPrompt?.let {
            msgs.put(JSONObject().put("role", "system").put("content", it))
        }
        for (m in messages) {
            msgs.put(
                JSONObject()
                    .put("role", m.role.name.lowercase())
                    .put("content", m.content)
            )
        }
        body.put("messages", msgs)
        body.put(
            "options",
            JSONObject()
                .put("temperature", config.temperature.toDouble())
                .put("num_predict", config.maxTokens)
        )
        val req = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string() ?: throw IllegalStateException("Empty response")
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}: $text")
            val root = JSONObject(text)
            val msg = root.getJSONObject("message")
            ChatResponse(
                message = ChatMessage(
                    role = ChatMessage.Role.ASSISTANT,
                    content = msg.optString("content", "")
                )
            )
        }
    }

    override fun chatStream(
        messages: List<ChatMessage>,
        config: ModelConfiguration,
        tools: List<ToolDefinition>?
    ): Flow<StreamChunk> = flow {
        try {
            val response = chat(messages, config, tools)
            emit(StreamChunk.TextDelta(response.message.content))
            emit(StreamChunk.Finished(response))
        } catch (e: Exception) {
            emit(StreamChunk.Error(e.message ?: "Ollama unreachable. Is the local server running?", e))
        }
    }.flowOn(Dispatchers.IO)
}
