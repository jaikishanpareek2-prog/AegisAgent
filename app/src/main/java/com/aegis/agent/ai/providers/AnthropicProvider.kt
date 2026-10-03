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

class AnthropicProvider(private val keyProvider:suspend()->String?):AIProvider {
 override val id="anthropic"; override val displayName="Anthropic"; override val supportsStreaming=false; override val supportsToolCalling=false
 private val client=OkHttpClient()
 override suspend fun listModels()=emptyList<ModelInfo>()
 override suspend fun chat(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):ChatResponse=withContext(Dispatchers.IO){
  val key=keyProvider()?:throw IllegalStateException("Anthropic API key not configured")
  val system=messages.firstOrNull{it.role==ChatMessage.Role.SYSTEM}?.content
  val arr=JSONArray();messages.filter{it.role!=ChatMessage.Role.SYSTEM}.forEach{m->arr.put(JSONObject().put("role",if(m.role==ChatMessage.Role.ASSISTANT)"assistant" else "user").put("content",m.content))}
  val body=JSONObject().put("model",config.modelId.ifBlank{"claude-3-5-sonnet-latest"}).put("max_tokens",config.maxTokens).put("temperature",config.temperature.toDouble()).put("messages",arr)
  system?.let{body.put("system",it)}
  val req=Request.Builder().url("https://api.anthropic.com/v1/messages").header("x-api-key",key).header("anthropic-version","2023-06-01").post(body.toString().toRequestBody("application/json".toMediaType())).build()
  client.newCall(req).execute().use{r->val raw=r.body?.string().orEmpty();if(!r.isSuccessful)throw IllegalStateException("HTTP ${r.code}: $raw");val text=JSONObject(raw).getJSONArray("content").getJSONObject(0).optString("text");ChatResponse(ChatMessage(ChatMessage.Role.ASSISTANT,text))}
 }
 override fun chatStream(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):Flow<StreamChunk> = flow{try{val r=chat(messages,config,tools);emit(StreamChunk.TextDelta(r.message.content));emit(StreamChunk.Finished(r))}catch(e:Exception){emit(StreamChunk.Error(e.message?: "Anthropic request failed",e))}}.flowOn(Dispatchers.IO)
}
