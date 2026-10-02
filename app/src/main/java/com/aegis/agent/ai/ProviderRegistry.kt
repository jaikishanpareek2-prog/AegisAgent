package com.aegis.agent.ai

import com.aegis.agent.ai.providers.AnthropicProvider
import com.aegis.agent.ai.providers.GeminiProvider
import com.aegis.agent.ai.providers.OllamaProvider
import com.aegis.agent.ai.providers.OpenAICompatibleProvider
import com.aegis.agent.data.SettingsRepository
import com.aegis.agent.security.CredentialStore
import kotlinx.coroutines.flow.first

class ProviderRegistry(
    private val credentialStore: CredentialStore,
    private val settings: SettingsRepository
) {
    private val providers = mutableMapOf<String, AIProvider>()

    init {
        registerDefaults()
    }

    private fun registerDefaults() {
        providers["openai"] = OpenAICompatibleProvider(
            id = "openai",
            displayName = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            apiKeyProvider = { credentialStore.getApiKey("openai") }
        )
        providers["openrouter"] = OpenAICompatibleProvider(
            id = "openrouter",
            displayName = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            apiKeyProvider = { credentialStore.getApiKey("openrouter") }
        )
        providers["gemini"] = GeminiProvider(
            apiKeyProvider = { credentialStore.getApiKey("gemini") }
        )
        providers["anthropic"] = AnthropicProvider(
            apiKeyProvider = { credentialStore.getApiKey("anthropic") }
        )
        providers["ollama"] = OllamaProvider(
            baseUrlProvider = { settings.getOllamaBaseUrl() }
        )
        providers["custom"] = OpenAICompatibleProvider(
            id = "custom",
            displayName = "Custom OpenAI-compatible",
            baseUrl = "",
            apiKeyProvider = { credentialStore.getApiKey("custom") },
            dynamicBaseUrl = { settings.getCustomBaseUrl() }
        )
    }

    fun get(id: String): AIProvider? = providers[id]

    fun all(): List<AIProvider> = providers.values.toList()

    suspend fun getActiveProvider(): AIProvider? {
        val id = settings.getActiveProviderId()
        return providers[id]
    }

    suspend fun getActiveConfig(): ModelConfiguration {
        val model = settings.getActiveModelId()
        return ModelConfiguration(
            modelId = model.ifBlank { "gpt-4o-mini" },
            temperature = settings.getTemperature(),
            maxTokens = settings.getMaxTokens(),
            systemPrompt = settings.getSystemPrompt()
        )
    }
}
