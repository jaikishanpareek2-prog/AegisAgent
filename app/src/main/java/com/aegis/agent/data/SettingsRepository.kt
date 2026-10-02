package com.aegis.agent.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aegis_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        val ACTIVE_MODEL = stringPreferencesKey("active_model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val OLLAMA_URL = stringPreferencesKey("ollama_url")
        val CUSTOM_URL = stringPreferencesKey("custom_url")
        val MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    suspend fun getActiveProviderId(): String =
        context.dataStore.data.map { it[Keys.ACTIVE_PROVIDER] ?: "openai" }.first()

    suspend fun setActiveProviderId(id: String) {
        context.dataStore.edit { it[Keys.ACTIVE_PROVIDER] = id }
    }

    suspend fun getActiveModelId(): String =
        context.dataStore.data.map { it[Keys.ACTIVE_MODEL] ?: "" }.first()

    suspend fun setActiveModelId(id: String) {
        context.dataStore.edit { it[Keys.ACTIVE_MODEL] = id }
    }

    suspend fun getTemperature(): Float =
        context.dataStore.data.map { it[Keys.TEMPERATURE] ?: 0.7f }.first()

    suspend fun setTemperature(v: Float) {
        context.dataStore.edit { it[Keys.TEMPERATURE] = v }
    }

    suspend fun getMaxTokens(): Int =
        context.dataStore.data.map { it[Keys.MAX_TOKENS] ?: 2048 }.first()

    suspend fun setMaxTokens(v: Int) {
        context.dataStore.edit { it[Keys.MAX_TOKENS] = v }
    }

    suspend fun getSystemPrompt(): String =
        context.dataStore.data.map {
            it[Keys.SYSTEM_PROMPT] ?: DEFAULT_SYSTEM
        }.first()

    suspend fun setSystemPrompt(v: String) {
        context.dataStore.edit { it[Keys.SYSTEM_PROMPT] = v }
    }

    suspend fun getOllamaBaseUrl(): String =
        context.dataStore.data.map { it[Keys.OLLAMA_URL] ?: "http://127.0.0.1:11434" }.first()

    suspend fun setOllamaBaseUrl(v: String) {
        context.dataStore.edit { it[Keys.OLLAMA_URL] = v }
    }

    suspend fun getCustomBaseUrl(): String =
        context.dataStore.data.map { it[Keys.CUSTOM_URL] ?: "" }.first()

    suspend fun setCustomBaseUrl(v: String) {
        context.dataStore.edit { it[Keys.CUSTOM_URL] = v }
    }

    suspend fun isMemoryEnabled(): Boolean =
        context.dataStore.data.map { it[Keys.MEMORY_ENABLED] ?: true }.first()

    suspend fun setMemoryEnabled(v: Boolean) {
        context.dataStore.edit { it[Keys.MEMORY_ENABLED] = v }
    }

    companion object {
        const val DEFAULT_SYSTEM = """You are Aegis, a careful autonomous personal AI agent on Android.
You help the user by planning and using tools when needed.
Be concise. Prefer low-risk tools. Never invent results of tools you did not call.
When you need to use a tool, call it. After tool results, continue reasoning.
Do not reveal private chain-of-thought; summarize actions instead."""
    }
}
