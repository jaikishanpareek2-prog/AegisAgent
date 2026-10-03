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

class OpenAICompatibleProvider(
    private val providerId: String,
    private val name: String,
    private val baseUrl: String,
    private val apiKeyProvider: suspend () -> String?,
    private val dynamicBaseUrl: (suspend () -> String)? = null
) : AIProvider {
    override val id = providerId
    override val displayName = name
    override val supportsStreaming = false
    override val supportsToolCalling = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private suspend fun endpoint(): String =
        (dynamicBaseUrl?.invoke()?.ifBlank { baseUrl } ?: baseUrl).trimEnd('/')

    override suspend fun listModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider() ?: return@withContext emptyList()
        try {
            val req = Request.Builder().url("${endpoint()}/models")
                .header("Authorization", "Bearer $key").get().build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val data = JSONObject(resp.body?.string().orEmpty()).optJSONArray("data") ?: return@withContext emptyList()
                (0 until data.length()).mapNotNull { i ->
                    data.optJSONObject(i)?.optString("id")?.takeIf { it.isNotBlank() }?.let { ModelInfo(it, it) }
                }
            }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        config: ModelConfiguration,
        tools: List<ToolDefinition>?
    ): ChatResponse = withContext(Dispatchers.IO) {
        val key = apiKeyProvider() ?: throw IllegalStateException("API key not configured for $providerId")
        val jsonMessages = JSONArray()
        messages.forEach { m ->
            val o = JSONObject().put("role", m.role.name.lowercase()).put("content", m.content)
            if (m.name != null) o.put("name", m.name)
            if (m.toolCallId != null) o.put("tool_call_id", m.toolCallId)
            if (!m.toolCalls.isNullOrEmpty()) {
                val calls = JSONArray()
                m.toolCalls.forEach { call ->
                    calls.put(JSONObject().put("id", call.id).put("type", "function")
                        .put("function", JSONObject().put("name", call.name).put("arguments", call.argumentsJson)))
                }
                o.put("tool_calls", calls)
            }
            jsonMessages.put(o)
        }
        val body = JSONObject()
            .put("model", config.modelId)
            .put("messages", jsonMessages)
            .put("temperature", config.temperature.toDouble())
            .put("max_tokens", config.maxTokens)
            .put("top_p", config.topP)
        if (!tools.isNullOrEmpty()) {
            val definitions = JSONArray()
            tools.forEach { t ->
                definitions.put(JSONObject().put("type", "function").put("function",
                    JSONObject().put("name", t.name).put("description", t.description)
                        .put("parameters", JSONObject(t.parametersJsonSchema))))
            }
            body.put("tools", definitions)
        }
        val request = Request.Builder().url("${endpoint()}/chat/completions")
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}: $raw")
            val choice = JSONObject(raw).getJSONArray("choices").getJSONObject(0)
            val msg = choice.getJSONObject("message")
            val calls = msg.optJSONArray("tool_calls")
            val parsedCalls = if (calls == null) null else (0 until calls.length()).mapNotNull { i ->
                calls.optJSONObject(i)?.let { c ->
                    val fn = c.optJSONObject("function") ?: return@let null
                    ToolCall(c.optString("id"), fn.optString("name"), fn.optString("arguments", "{}"))
                }
            }
            ChatResponse(
                ChatMessage(ChatMessage.Role.ASSISTANT, msg.optString("content", ""), parsedCalls),
                finishReason = choice.optString("finish_reason")
            )
        }
    }

    override fun chatStream(messages: List<ChatMessage>, config: ModelConfiguration, tools: List<ToolDefinition>?): Flow<StreamChunk> =
        flow {
            try { val r = chat(messages, config, tools); if (r.message.content.isNotEmpty()) emit(StreamChunk.TextDelta(r.message.content)); emit(StreamChunk.Finished(r)) }
            catch (e: Exception) { emit(StreamChunk.Error(e.message ?: "Request failed", e)) }
        }.flowOn(Dispatchers.IO)
}
