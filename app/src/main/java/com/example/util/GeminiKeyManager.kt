package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class ApiKeyStatus(val label: String) {
    ACTIVE_CUSTOM("Custom Key Active"),
    ACTIVE_DEFAULT("Default Key Active"),
    MISSING("API Key Missing"),
    QUOTA_EXCEEDED("Quota Limit Exceeded")
}

class GeminiKeyManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fitlit_gemini_prefs", Context.MODE_PRIVATE)

    private val _keyStatus = MutableStateFlow(determineInitialStatus())
    val keyStatus: StateFlow<ApiKeyStatus> = _keyStatus.asStateFlow()

    private val _customKey = MutableStateFlow(prefs.getString(KEY_CUSTOM_API_KEY, "") ?: "")
    val customKey: StateFlow<String> = _customKey.asStateFlow()

    companion object {
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

        @Volatile
        private var instance: GeminiKeyManager? = null

        fun getInstance(context: Context): GeminiKeyManager {
            return instance ?: synchronized(this) {
                instance ?: GeminiKeyManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun determineInitialStatus(): ApiKeyStatus {
        val saved = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (saved.isNotBlank()) {
            return ApiKeyStatus.ACTIVE_CUSTOM
        }
        val defaultKey = BuildConfig.GEMINI_API_KEY.trim()
        return if (defaultKey.isNotBlank() && defaultKey != "MY_GEMINI_API_KEY") {
            ApiKeyStatus.ACTIVE_DEFAULT
        } else {
            ApiKeyStatus.MISSING
        }
    }

    fun getEffectiveApiKey(): String {
        val custom = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (custom.isNotBlank()) return custom

        val defaultKey = BuildConfig.GEMINI_API_KEY.trim()
        if (defaultKey.isNotBlank() && defaultKey != "MY_GEMINI_API_KEY") {
            return defaultKey
        }
        return ""
    }

    fun hasValidKey(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    fun saveCustomApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString(KEY_CUSTOM_API_KEY, trimmed).apply()
        _customKey.value = trimmed
        _keyStatus.value = if (trimmed.isNotBlank()) ApiKeyStatus.ACTIVE_CUSTOM else determineInitialStatus()
    }

    fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
        _customKey.value = ""
        _keyStatus.value = determineInitialStatus()
    }

    fun markQuotaExceeded() {
        _keyStatus.value = ApiKeyStatus.QUOTA_EXCEEDED
    }

    fun markActive() {
        _keyStatus.value = determineInitialStatus()
    }

    suspend fun testApiKey(apiKeyToTest: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKeyToTest.trim()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        try {
            val service: GeminiApiService = GeminiClient.service
            val testRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Respond with one word: 'ACTIVE'")),
                        role = "user"
                    )
                )
            )

            val response = service.generateContent(
                url = "v1beta/models/gemini-1.5-flash:generateContent",
                apiKey = key,
                request = testRequest
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success("Key successfully verified & active! ✨")
            } else {
                Result.failure(Exception("Gemini returned empty response. Please verify key permissions."))
            }
        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            if (code == 429) {
                Result.failure(Exception("HTTP 429: Quota limit reached on this key. Try another key."))
            } else if (code == 400 || code == 403) {
                Result.failure(Exception("HTTP $code: Invalid API Key or Gemini API not enabled."))
            } else {
                Result.failure(Exception("HTTP Error $code: ${e.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }
}
