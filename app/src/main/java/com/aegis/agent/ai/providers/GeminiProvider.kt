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

class GeminiProvider(private val keyProvider:suspend()->String?):AIProvider {
 override val id="gemini"; override val displayName="Google Gemini"; override val supportsStreaming=false; override val supportsToolCalling=false
 private val client=OkHttpClient()
 override suspend fun listModels()=emptyList<ModelInfo>()
 override suspend fun chat(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):ChatResponse=withContext(Dispatchers.IO){
  val key=keyProvider()?:throw IllegalStateException("Gemini API key not configured")
  val contents=JSONArray(); messages.filter{it.role!=ChatMessage.Role.SYSTEM}.forEach{m->contents.put(JSONObject().put("role",if(m.role==ChatMessage.Role.ASSISTANT)"model" else "user").put("parts",JSONArray().put(JSONObject().put("text",m.content))))}
  val body=JSONObject().put("contents",contents)
  config.systemPrompt?.let{body.put("systemInstruction",JSONObject().put("parts",JSONArray().put(JSONObject().put("text",it))))}
  val url="https://generativelanguage.googleapis.com/v1beta/models/${config.modelId.ifBlank{"gemini-2.0-flash"}}:generateContent?key=$key"
  val req=Request.Builder().url(url).post(body.toString().toRequestBody("application/json".toMediaType())).build()
  client.newCall(req).execute().use{r->val raw=r.body?.string().orEmpty();if(!r.isSuccessful)throw IllegalStateException("HTTP ${r.code}: $raw");val text=JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).optString("text");ChatResponse(ChatMessage(ChatMessage.Role.ASSISTANT,text))}
 }
 override fun chatStream(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):Flow<StreamChunk>=flow{try{val r=chat(messages,config,tools);emit(StreamChunk.TextDelta(r.message.content));emit(StreamChunk.Finished(r))}catch(e:Exception){emit(StreamChunk.Error(e.message?: "Gemini request failed",e))}}.flowOn(Dispatchers.IO)
}
