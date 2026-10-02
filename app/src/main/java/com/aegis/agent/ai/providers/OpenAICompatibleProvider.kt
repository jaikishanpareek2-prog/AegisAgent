package com.aegis.agent.ai.providers

import com.aegis.agent.ai.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OpenAICompatibleProvider(private val providerId:String,private val name:String,private val baseUrl:String,private val apiKeyProvider:suspend()->String?,private val dynamicBaseUrl:(suspend()->String)?=null):AIProvider{
 override val id=providerId
 override val displayName=name
 override val supportsStreaming=false
 override val supportsToolCalling=true
 override suspend fun listModels()=emptyList<ModelInfo>()
 override suspend fun chat(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):ChatResponse=throw IllegalStateException("Provider network implementation pending")
 override fun chatStream(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):Flow<StreamChunk> = flow{emit(StreamChunk.Error("Provider network implementation pending"))}
}
