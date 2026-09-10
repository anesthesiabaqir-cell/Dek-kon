package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Supported AI Model Providers (Gemini & OpenRouter only)
 */
enum class ModelProvider(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    val defaultDailyQuota: Int,
    val isOpenAiCompatible: Boolean,
    val arabicName: String = ""
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        baseUrl = "https://generativelanguage.googleapis.com",
        defaultModel = "",
        defaultDailyQuota = 1500, // Free tier daily limit (1500 requests/day)
        isOpenAiCompatible = false,
        arabicName = ""
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "openrouter/free",
        defaultDailyQuota = 50,
        isOpenAiCompatible = true,
        arabicName = ""
    );

    companion object {
        fun fromId(id: String?): ModelProvider =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
}

/**
 * Detailed Information on AI Models
 */
data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: ModelProvider,
    val tier: String = "",
    val description: String = "",
    val functionalDescription: String = "",
    val isPro: Boolean = false,
    val isFreeTier: Boolean = false,
    val dailyQuota: Int = 0
)

typealias GeminiModelInfo = AiModelInfo

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("api_key_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API_KEY = "user_gemini_api_key"
        private const val KEY_OPENROUTER_API_KEY = "user_openrouter_api_key"
        private const val KEY_OTHER_API_KEY = "user_other_api_key" // Legacy compatibility
        private const val KEY_SELECTED_PROVIDER = "selected_model_provider"
        private const val KEY_SELECTED_MODEL_PREFIX = "selected_model_for_"

        // Cache for dynamically fetched Gemini models (24 hours)
        private const val KEY_CACHED_GEMINI_MODELS_JSON = "cached_gemini_models_json"
        private const val KEY_CACHED_GEMINI_TIMESTAMP = "cached_gemini_models_timestamp"

        // Cache for dynamically fetched OpenRouter models
        private const val KEY_CACHED_OPENROUTER_MODELS_JSON = "cached_openrouter_models_json"
        private const val KEY_CACHED_OPENROUTER_TIMESTAMP = "cached_openrouter_models_timestamp"
        private const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 24 hours

        // Daily usage tracking keys
        private const val KEY_USAGE_DATE = "daily_usage_date"
        private const val KEY_USAGE_PREFIX = "daily_usage_count_"

        const val DEFAULT_MODEL = ""

        // Initial Curated OpenRouter Free Models (Used before first online fetch or offline fallback)
        val DEFAULT_OPENROUTER_MODELS = listOf(
            AiModelInfo(
                id = "openrouter/free",
                name = "OpenRouter Free (Auto Router)",
                provider = ModelProvider.OPENROUTER,
                tier = "Free Router",
                description = "Automatisches Routing zum besten kostenlosen Modell",
                functionalDescription = "Kostenloses Modell",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "google/gemini-2.0-flash-exp:free",
                name = "Google: Gemini 2.0 Flash Exp (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "1M Context",
                description = "Schnelles Google-Modell über OpenRouter",
                functionalDescription = "Multimodales Modell",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "meta-llama/llama-3.3-70b-instruct:free",
                name = "Meta: Llama 3.3 70B Instruct (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "70B Model",
                description = "Leistungsstarkes Meta Open-Source-Modell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "inclusionai/ling-3.0-flash-fin:free",
                name = "Ling 3.0 Flash Fin (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "262K Context",
                description = "Spezialisiertes schnelles Modell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "dots-studio/dots-3-note-preview:free",
                name = "Dots3-Note Preview (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "512K Context",
                description = "Kompaktes intelligentes Modell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "liquid/lfm-2.5-2.6b:free",
                name = "LiquidAI: LFM 2.5 2.6B (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "66K Context",
                description = "Ultraschnelles Reaktionsmodell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "nvidia/nemotron-3.5-lightning:free",
                name = "NVIDIA Nemotron 3.5 Lightning (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "1M Context",
                description = "Extrem schnelle NVIDIA-Verarbeitung",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "poolside/laguna-s-2.1:free",
                name = "Laguna S 2.1 (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "262K Context",
                description = "Fortgeschrittenes Analysemodell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            ),
            AiModelInfo(
                id = "z-ai/glm-5.2:free",
                name = "GLM 5.2 (Free)",
                provider = ModelProvider.OPENROUTER,
                tier = "256K Context",
                description = "Mehrsprachiges KI-Modell",
                functionalDescription = "Text-Konversation",
                isFreeTier = true,
                dailyQuota = 50
            )
        )

        fun getModelsForProvider(provider: ModelProvider): List<AiModelInfo> {
            return when (provider) {
                ModelProvider.GEMINI -> emptyList()
                ModelProvider.OPENROUTER -> DEFAULT_OPENROUTER_MODELS
            }
        }
    }

    // -------------------------------------------------------------
    // Provider Selection
    // -------------------------------------------------------------
    fun getSelectedProvider(): ModelProvider {
        val id = prefs.getString(KEY_SELECTED_PROVIDER, ModelProvider.GEMINI.id)
        return ModelProvider.fromId(id)
    }

    fun saveSelectedProvider(provider: ModelProvider) {
        prefs.edit().putString(KEY_SELECTED_PROVIDER, provider.id).apply()
    }

    // -------------------------------------------------------------
    // Dynamic Gemini Models Caching (24-Hour Cache)
    // -------------------------------------------------------------
    fun getCachedGeminiModels(): List<AiModelInfo> {
        val json = prefs.getString(KEY_CACHED_GEMINI_MODELS_JSON, null)
        if (json.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            parseGeminiModelsJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCachedGeminiModels(jsonString: String) {
        prefs.edit()
            .putString(KEY_CACHED_GEMINI_MODELS_JSON, jsonString)
            .putLong(KEY_CACHED_GEMINI_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun clearCachedGeminiModels() {
        prefs.edit()
            .remove(KEY_CACHED_GEMINI_MODELS_JSON)
            .remove(KEY_CACHED_GEMINI_TIMESTAMP)
            .apply()
    }

    fun isGeminiCacheValid(): Boolean {
        val timestamp = prefs.getLong(KEY_CACHED_GEMINI_TIMESTAMP, 0L)
        val json = prefs.getString(KEY_CACHED_GEMINI_MODELS_JSON, null)
        return !json.isNullOrBlank() && (System.currentTimeMillis() - timestamp) < CACHE_EXPIRATION_MS
    }

    fun parseGeminiModelsJson(jsonString: String): List<AiModelInfo> {
        val result = mutableListOf<AiModelInfo>()
        val root = JSONObject(jsonString)
        val modelsArray = root.optJSONArray("models") ?: return emptyList()

        for (i in 0 until modelsArray.length()) {
            val item = modelsArray.optJSONObject(i) ?: continue
            val rawName = item.optString("name", "")
            if (rawName.isBlank()) continue

            // Filter: Only display models that support generateContent
            val methods = item.optJSONArray("supportedGenerationMethods")
            var supportsGenerateContent = false
            if (methods != null) {
                for (m in 0 until methods.length()) {
                    if (methods.optString(m).equals("generateContent", ignoreCase = true)) {
                        supportsGenerateContent = true
                        break
                    }
                }
            }
            if (!supportsGenerateContent) continue

            // Remove "models/" prefix for standard model ID
            val modelId = rawName.removePrefix("models/")
            val displayName = item.optString("displayName", modelId).ifBlank { modelId }
            val description = item.optString("description", "")

            val isFlash = modelId.contains("flash", ignoreCase = true)
            val isLite = modelId.contains("lite", ignoreCase = true)
            val isPro = modelId.contains("pro", ignoreCase = true)
            val tier = when {
                isLite -> "Lite"
                isFlash -> "Flash"
                isPro -> "Pro"
                else -> "Gemini"
            }

            result.add(
                AiModelInfo(
                    id = modelId,
                    name = displayName,
                    provider = ModelProvider.GEMINI,
                    tier = tier,
                    description = if (description.isNotBlank()) description else "Google Gemini Modell",
                    isPro = isPro,
                    isFreeTier = true,
                    dailyQuota = 1500
                )
            )
        }

        // Sort: Flash models first, non-experimental first, then alphabetical
        result.sortWith(
            compareByDescending<AiModelInfo> { it.id.contains("flash", ignoreCase = true) }
                .thenBy { it.id.contains("experimental", ignoreCase = true) || it.id.contains("exp", ignoreCase = true) }
                .thenBy { it.name }
        )

        return result
    }

    // -------------------------------------------------------------
    // Dynamic OpenRouter Models Caching (24-Hour Cache)
    // -------------------------------------------------------------
    fun getCachedOpenRouterModels(): List<AiModelInfo> {
        val json = prefs.getString(KEY_CACHED_OPENROUTER_MODELS_JSON, null)
        if (json.isNullOrBlank()) {
            return DEFAULT_OPENROUTER_MODELS
        }
        return try {
            parseOpenRouterModelsJson(json)
        } catch (e: Exception) {
            DEFAULT_OPENROUTER_MODELS
        }
    }

    fun saveCachedOpenRouterModels(jsonString: String) {
        prefs.edit()
            .putString(KEY_CACHED_OPENROUTER_MODELS_JSON, jsonString)
            .putLong(KEY_CACHED_OPENROUTER_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun isOpenRouterCacheValid(): Boolean {
        val timestamp = prefs.getLong(KEY_CACHED_OPENROUTER_TIMESTAMP, 0L)
        val json = prefs.getString(KEY_CACHED_OPENROUTER_MODELS_JSON, null)
        return !json.isNullOrBlank() && (System.currentTimeMillis() - timestamp) < CACHE_EXPIRATION_MS
    }

    fun parseOpenRouterModelsJson(jsonString: String): List<AiModelInfo> {
        val result = mutableListOf<AiModelInfo>()
        val root = JSONObject(jsonString)
        val dataArray = root.optJSONArray("data") ?: return DEFAULT_OPENROUTER_MODELS

        for (i in 0 until dataArray.length()) {
            val item = dataArray.optJSONObject(i) ?: continue
            val id = item.optString("id", "")
            if (id.isBlank()) continue

            val name = item.optString("name", id)
            val description = item.optString("description", "")
            val pricing = item.optJSONObject("pricing")
            val promptPrice = pricing?.optString("prompt", "0") ?: "0"
            val completionPrice = pricing?.optString("completion", "0") ?: "0"

            val isFree = (promptPrice == "0" || promptPrice == "0.0") &&
                    (completionPrice == "0" || completionPrice == "0.0") ||
                    id.endsWith(":free", ignoreCase = true)

            val tier = if (isFree) "Free" else "Standard"
            val functionalDesc = deriveOpenRouterFunctionalDescription(item, id, isFree)

            result.add(
                AiModelInfo(
                    id = id,
                    name = name,
                    provider = ModelProvider.OPENROUTER,
                    tier = tier,
                    description = if (description.isNotBlank()) description else "OpenRouter Modell",
                    functionalDescription = functionalDesc,
                    isPro = !isFree,
                    isFreeTier = isFree,
                    dailyQuota = if (isFree) 50 else 1000
                )
            )
        }

        // Sort so free models appear first, and "openrouter/free" is at top if present
        result.sortWith(compareByDescending<AiModelInfo> { it.id == "openrouter/free" }
            .thenByDescending { it.isFreeTier }
            .thenBy { it.name })

        return if (result.isNotEmpty()) result else DEFAULT_OPENROUTER_MODELS
    }

    /**
     * Derives a German functional description dynamically from OpenRouter model API fields:
     * architecture.modalities / architecture.input_modalities / architecture.output_modalities,
     * pricing, and model ID keywords.
     */
    fun deriveOpenRouterFunctionalDescription(item: JSONObject, id: String, isFree: Boolean): String {
        val architecture = item.optJSONObject("architecture")
        
        // Extract input and output modalities
        val inputModalities = mutableListOf<String>()
        val outputModalities = mutableListOf<String>()

        fun extractStringList(array: JSONArray?): List<String> {
            val list = mutableListOf<String>()
            if (array != null) {
                for (j in 0 until array.length()) {
                    val s = array.optString(j, "").lowercase(Locale.ROOT).trim()
                    if (s.isNotBlank()) list.add(s)
                }
            }
            return list
        }

        if (architecture != null) {
            inputModalities.addAll(extractStringList(architecture.optJSONArray("input_modalities")))
            outputModalities.addAll(extractStringList(architecture.optJSONArray("output_modalities")))

            // Fallback: general 'modalities' array
            val generalModalities = extractStringList(architecture.optJSONArray("modalities"))
            if (inputModalities.isEmpty() && generalModalities.isNotEmpty()) {
                inputModalities.addAll(generalModalities)
            }
        }

        val hasImage = inputModalities.contains("image")
        val hasAudio = inputModalities.contains("audio")
        val hasVideo = inputModalities.contains("video")
        val hasTextIn = inputModalities.contains("text")

        val isTextOnlyOut = outputModalities.isNotEmpty() && outputModalities.all { it == "text" }
        val isEmbeddingOut = outputModalities.contains("embedding") || outputModalities.contains("embeddings")

        val lowerId = id.lowercase(Locale.ROOT)
        val hasCodeKeyword = lowerId.contains("code") || lowerId.contains("coder")
        val hasReasoningKeyword = lowerId.contains("reasoning") || lowerId.contains("think") || lowerId.contains("deepseek-r1") || lowerId.contains("qwq")
        val hasEmbeddingKeyword = lowerId.contains("embed")
        val hasRerankKeyword = lowerId.contains("rerank")

        // 1. If input modalities has more than one type -> Multimodales Modell (with details if available)
        if (inputModalities.size > 1) {
            val parts = mutableListOf<String>()
            if (hasTextIn) parts.add("Text")
            if (hasImage) parts.add("Bild")
            if (hasAudio) parts.add("Audio")
            if (hasVideo) parts.add("Video")
            return if (parts.size > 1) {
                "Multimodales Modell (${parts.joinToString(", ")})"
            } else {
                "Multimodales Modell"
            }
        }

        // Specific single-modality checks
        if (hasImage) {
            return "Bildverarbeitung & Text"
        }
        if (hasAudio) {
            return "Audioverarbeitung & Text"
        }
        if (hasVideo) {
            return "Videoverarbeitung & Text"
        }

        // 2. Otherwise if output_modalities contains only text -> Text-Konversation
        if (isTextOnlyOut && !hasCodeKeyword && !hasReasoningKeyword && !hasEmbeddingKeyword && !hasRerankKeyword) {
            return "Text-Konversation"
        }

        // 3. Otherwise if ID contains code or coder -> Code-Generierung
        if (hasCodeKeyword) {
            return "Code-Generierung"
        }

        // 4. Otherwise if ID contains reasoning / think -> Reasoning & Logik
        if (hasReasoningKeyword) {
            return "Reasoning & Logik"
        }

        // 5. Otherwise if output is embedding or ID contains embed -> Texteinbettung
        if (isEmbeddingOut || hasEmbeddingKeyword) {
            return "Texteinbettung"
        }

        // 6. Otherwise if ID contains rerank -> Ergebnis-Neuordnung
        if (hasRerankKeyword) {
            return "Ergebnis-Neuordnung"
        }

        // 7. Otherwise if free model -> Kostenloses Modell
        if (isFree) {
            return "Kostenloses Modell"
        }

        // 8. General fallback -> KI-Modell
        return "KI-Modell"
    }

    fun getModelsForActiveProvider(provider: ModelProvider = getSelectedProvider()): List<AiModelInfo> {
        return when (provider) {
            ModelProvider.GEMINI -> getCachedGeminiModels()
            ModelProvider.OPENROUTER -> getCachedOpenRouterModels()
        }
    }

    // -------------------------------------------------------------
    // Model Selection (per provider)
    // -------------------------------------------------------------
    fun getSelectedModel(provider: ModelProvider = getSelectedProvider()): String {
        val key = KEY_SELECTED_MODEL_PREFIX + provider.id
        val stored = prefs.getString(key, null)
        val providerModels = getModelsForActiveProvider(provider)
        return if (stored != null && providerModels.any { it.id == stored }) {
            stored
        } else if (providerModels.isNotEmpty()) {
            providerModels.first().id
        } else {
            stored ?: provider.defaultModel
        }
    }

    fun saveSelectedModel(modelId: String, provider: ModelProvider = getSelectedProvider()) {
        val key = KEY_SELECTED_MODEL_PREFIX + provider.id
        prefs.edit().putString(key, modelId).apply()
    }

    // -------------------------------------------------------------
    // API Keys (1. Gemini Key & 2. OpenRouter Key)
    // -------------------------------------------------------------
    fun getGeminiApiKey(): String {
        val userKey = prefs.getString(KEY_GEMINI_API_KEY, "")?.trim().orEmpty()
        if (userKey.isNotEmpty()) {
            return userKey
        }
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
        return if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }
    }

    fun getUserEnteredGeminiKey(): String {
        return prefs.getString(KEY_GEMINI_API_KEY, "").orEmpty()
    }

    fun saveGeminiApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
    }

    fun clearGeminiApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun getOpenRouterApiKey(): String {
        val key = prefs.getString(KEY_OPENROUTER_API_KEY, "")?.trim().orEmpty()
        if (key.isNotEmpty()) return key

        // Migrate legacy other key if it was OpenRouter
        val legacyKey = prefs.getString(KEY_OTHER_API_KEY, "")?.trim().orEmpty()
        if (legacyKey.isNotEmpty()) {
            saveOpenRouterApiKey(legacyKey)
            return legacyKey
        }
        return ""
    }

    fun saveOpenRouterApiKey(apiKey: String) {
        prefs.edit().putString(KEY_OPENROUTER_API_KEY, apiKey.trim()).apply()
    }

    fun clearOpenRouterApiKey() {
        prefs.edit().remove(KEY_OPENROUTER_API_KEY).apply()
        prefs.edit().remove(KEY_OTHER_API_KEY).apply()
    }

    // Legacy aliases
    fun getOtherApiKey(): String = getOpenRouterApiKey()
    fun saveOtherApiKey(apiKey: String) = saveOpenRouterApiKey(apiKey)
    fun clearOtherApiKey() = clearOpenRouterApiKey()

    fun getApiKey(): String = getActiveApiKey()
    fun getUserEnteredKey(): String = getUserEnteredGeminiKey()
    fun saveApiKey(apiKey: String) = saveGeminiApiKey(apiKey)
    fun clearApiKey() {
        clearGeminiApiKey()
        clearOpenRouterApiKey()
    }

    /**
     * Retrieves the active API key corresponding to the currently selected provider.
     */
    fun getActiveApiKey(): String {
        return when (getSelectedProvider()) {
            ModelProvider.GEMINI -> getGeminiApiKey()
            ModelProvider.OPENROUTER -> getOpenRouterApiKey()
        }
    }

    /**
     * Checks if the active provider has a valid key configured.
     */
    fun isKeyConfigured(): Boolean {
        val key = getActiveApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    // -------------------------------------------------------------
    // Daily Usage Quota Tracking
    // -------------------------------------------------------------
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun checkAndResetDailyUsage() {
        val today = getTodayDateString()
        val storedDate = prefs.getString(KEY_USAGE_DATE, "")
        if (storedDate != today) {
            val editor = prefs.edit()
            editor.putString(KEY_USAGE_DATE, today)
            ModelProvider.entries.forEach { provider ->
                editor.putInt(KEY_USAGE_PREFIX + provider.id, 0)
            }
            editor.apply()
        }
    }

    fun incrementDailyUsage(provider: ModelProvider = getSelectedProvider()) {
        checkAndResetDailyUsage()
        val key = KEY_USAGE_PREFIX + provider.id
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getDailyUsage(
        provider: ModelProvider = getSelectedProvider(),
        modelId: String = getSelectedModel(provider)
    ): Pair<Int, Int> {
        checkAndResetDailyUsage()
        val used = prefs.getInt(KEY_USAGE_PREFIX + provider.id, 0)
        val models = getModelsForActiveProvider(provider)
        val modelInfo = models.find { it.id == modelId }
        val quota = if (modelInfo != null && modelInfo.dailyQuota > 0) {
            modelInfo.dailyQuota
        } else {
            provider.defaultDailyQuota
        }
        val remaining = maxOf(0, quota - used)
        return Pair(remaining, quota)
    }
}

