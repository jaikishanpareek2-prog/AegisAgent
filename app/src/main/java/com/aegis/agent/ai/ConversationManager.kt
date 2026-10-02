package com.aegis.agent.ai

import com.aegis.agent.tools.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationManager(private val providerRegistry:ProviderRegistry,private val toolRegistry:ToolRegistry){
 private val messages=mutableListOf<ChatMessage>()
 fun clear()=messages.clear()
 fun history():List<ChatMessage>=messages.toList()
 suspend fun chat(userText:String,systemPrompt:String?=null):String=withContext(Dispatchers.IO){
  if(systemPrompt!=null&&messages.none{it.role==ChatMessage.Role.SYSTEM})messages.add(0,ChatMessage(ChatMessage.Role.SYSTEM,systemPrompt))
  messages.add(ChatMessage(ChatMessage.Role.USER,userText))
  val p=providerRegistry.getActiveProvider()?:return@withContext "No AI provider configured."
  val r=p.chat(messages,providerRegistry.getActiveConfig(),toolRegistry.toToolDefinitions());messages.add(r.message);r.message.content
 }
}
