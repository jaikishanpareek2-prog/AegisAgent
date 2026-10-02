package com.aegis.agent.ai.providers

import com.aegis.agent.ai.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnthropicProvider(private val keyProvider:suspend()->String?):AIProvider{
 override val id="anthropic";override val displayName="Anthropic";override val supportsStreaming=false;override val supportsToolCalling=false
 override suspend fun listModels()=emptyList<ModelInfo>()
 override suspend fun chat(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):ChatResponse=throw IllegalStateException("Anthropic provider not implemented yet")
 override fun chatStream(messages:List<ChatMessage>,config:ModelConfiguration,tools:List<ToolDefinition>?):Flow<StreamChunk> = flow{emit(StreamChunk.Error("Anthropic provider not implemented yet"))}
}
