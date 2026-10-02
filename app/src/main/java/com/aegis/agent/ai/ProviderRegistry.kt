package com.aegis.agent.ai

import com.aegis.agent.ai.providers.AnthropicProvider
import com.aegis.agent.ai.providers.GeminiProvider
import com.aegis.agent.ai.providers.OllamaProvider
import com.aegis.agent.ai.providers.OpenAICompatibleProvider
import com.aegis.agent.data.SettingsRepository
import com.aegis.agent.security.CredentialStore

class ProviderRegistry(private val credentialStore:CredentialStore,private val settings:SettingsRepository){
 private val providers=linkedMapOf<String,AIProvider>()
 init{providers["openai"]=OpenAICompatibleProvider("openai","OpenAI","https://api.openai.com/v1",{credentialStore.getApiKey("openai")});providers["openrouter"]=OpenAICompatibleProvider("openrouter","OpenRouter","https://openrouter.ai/api/v1",{credentialStore.getApiKey("openrouter")});providers["custom"]=OpenAICompatibleProvider("custom","Custom OpenAI-compatible","",{credentialStore.getApiKey("custom")}){settings.getCustomBaseUrl()};providers["gemini"]=GeminiProvider({credentialStore.getApiKey("gemini")});providers["anthropic"]=AnthropicProvider({credentialStore.getApiKey("anthropic")});providers["ollama"]=OllamaProvider{settings.getOllamaBaseUrl()}}
 fun get(id:String)=providers[id]
 fun all()=providers.values.toList()
 suspend fun getActiveProvider()=providers[settings.getActiveProviderId()]
 suspend fun getActiveConfig()=ModelConfiguration(settings.getActiveModelId().ifBlank{"gpt-4o-mini"},settings.getTemperature(),settings.getMaxTokens(),systemPrompt=settings.getSystemPrompt())
}
