package com.aegis.agent.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores API keys using Android Keystore-backed EncryptedSharedPreferences.
 */
class CredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "aegis_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun getApiKey(providerId: String): String? {
        return prefs.getString("key_$providerId", null)?.takeIf { it.isNotBlank() }
    }

    fun setApiKey(providerId: String, key: String) {
        prefs.edit().putString("key_$providerId", key.trim()).apply()
    }

    fun clearApiKey(providerId: String) {
        prefs.edit().remove("key_$providerId").apply()
    }

    fun hasKey(providerId: String): Boolean =
        !prefs.getString("key_$providerId", null).isNullOrBlank()
}
