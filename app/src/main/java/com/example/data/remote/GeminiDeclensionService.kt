package com.example.data.remote

import android.content.Context
import com.example.data.ModelProvider
import com.example.data.model.WordDeclensionResult
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiDeclensionService(private val context: Context? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchDeclension(
        word: String,
        apiKey: String,
        provider: ModelProvider = ModelProvider.GEMINI,
        selectedModelId: String = "gemini-3.8-flash"
    ): WordDeclensionResult = withContext(Dispatchers.IO) {
        val result = fetchGrammar(word, com.example.data.model.GrammarType.NOMEN, apiKey, provider, selectedModelId)
        when (result) {
            is com.example.data.model.GrammarResult.Noun -> result.declension
            is com.example.data.model.GrammarResult.Sentence -> result.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Erwartetes Nomen, aber Verb erhalten.")
        }
    }

    suspend fun fetchGrammar(
        word: String,
        type: com.example.data.model.GrammarType,
        apiKey: String,
        provider: ModelProvider = ModelProvider.GEMINI,
        selectedModelId: String = "gemini-3.8-flash"
    ): com.example.data.model.GrammarResult = withContext(Dispatchers.IO) {
        val trimmedWord = word.trim()
        if (trimmedWord.isEmpty()) {
            val typeLabel = if (type == com.example.data.model.GrammarType.NOMEN) "deutsches Nomen" else "deutsches Verb"
            throw IllegalArgumentException("Bitte geben Sie ein $typeLabel ein.")
        }

        if (apiKey.isBlank()) {
            throw IllegalStateException("Bitte konfigurieren Sie Ihren API-Schlüssel für ${provider.displayName} in den Einstellungen.")
        }

        // Offline / No Internet verification
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            throw IOException("Keine Internetverbindung. Die linguistische Analyse erfordert eine aktive Internetverbindung.")
        }

        val prompt = when (type) {
            com.example.data.model.GrammarType.NOMEN -> buildNounPrompt(trimmedWord)
            com.example.data.model.GrammarType.VERB -> buildVerbPrompt(trimmedWord)
            com.example.data.model.GrammarType.SENTENCE -> buildSentencePrompt(trimmedWord)
        }

        val jsonContent = if (provider == ModelProvider.GEMINI) {
            fetchRawFromGemini(prompt, apiKey, selectedModelId)
        } else {
            fetchRawFromOpenAiCompatible(prompt, apiKey, provider, selectedModelId)
        }

        return@withContext com.example.data.model.GrammarResult.fromJson(jsonContent, fallbackType = type)
    }

    /**
     * Executes query via Google Gemini Generative Language API
     */
    private fun fetchRawFromGemini(
        prompt: String,
        apiKey: String,
        selectedModelId: String
    ): String {
        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val targetModel = selectedModelId.trim().removePrefix("models/").ifBlank { "gemini-2.5-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = parseGeminiErrorMessage(response.code, responseBody)
                throw IOException(errorMsg)
            }

            return extractGeminiContentText(responseBody)
        }
    }

    /**
     * Executes query via OpenAI-compatible endpoints (Groq, Cerebras, OpenRouter, Mistral)
     */
    private fun fetchRawFromOpenAiCompatible(
        prompt: String,
        apiKey: String,
        provider: ModelProvider,
        selectedModelId: String
    ): String {
        val endpointUrl = "${provider.baseUrl}/chat/completions"

        val requestJson = JSONObject().apply {
            put("model", selectedModelId)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an authoritative German linguistic dictionary. If the user's input word contains spelling errors, typos, or incorrect casing, silently correct it to the intended valid German word without asking or commenting. Return strictly raw valid JSON matching the exact specified schema. No markdown backticks, no explanations.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.1)
        }

        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")

        if (provider == ModelProvider.OPENROUTER) {
            requestBuilder.addHeader("HTTP-Referer", "https://deklination.app")
            requestBuilder.addHeader("X-Title", "German Deklination App")
        }

        val request = requestBuilder
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = parseOpenAiErrorMessage(provider, response.code, responseBody)
                throw IOException(errorMsg)
            }

            return extractOpenAiContentText(responseBody)
        }
    }

    /**
     * Verifies API Key with a lightweight request
     */
    suspend fun testApiKey(
        provider: ModelProvider,
        apiKey: String,
        modelId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(Exception("Bitte API-Schlüssel eingeben (Key required)"))
        }

        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(Exception("Keine Internetverbindung (Offline)"))
        }

        try {
            if (provider == ModelProvider.GEMINI) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$trimmedKey"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful && body.isNotBlank()) {
                        Result.success("✓ Schlüssel gültig (Google Gemini)")
                    } else {
                        if (response.code in listOf(400, 401, 403)) {
                            Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
                        } else {
                            Result.failure(Exception(parseGeminiErrorMessage(response.code, body)))
                        }
                    }
                }
            } else {
                // OpenRouter: First check the official /auth/key endpoint for key details and usage
                val authKeyUrl = "https://openrouter.ai/api/v1/auth/key"
                val authRequest = Request.Builder()
                    .url(authKeyUrl)
                    .addHeader("Authorization", "Bearer $trimmedKey")
                    .addHeader("HTTP-Referer", "https://deklination.app")
                    .addHeader("X-Title", "German Deklination App")
                    .get()
                    .build()

                client.newCall(authRequest).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        val successMessage = try {
                            val root = JSONObject(body)
                            val data = root.optJSONObject("data")
                            val usage = data?.optDouble("usage", -1.0) ?: -1.0
                            val limit = if (data != null && !data.isNull("limit")) data.optDouble("limit", -1.0) else -1.0
                            val isFreeTier = data?.optBoolean("is_free_tier", false) ?: false

                            buildString {
                                append("✓ Schlüssel gültig (OpenRouter)")
                                if (usage >= 0.0) {
                                    append(" • $${String.format(java.util.Locale.US, "%.3f", usage)}")
                                }
                                if (limit > 0.0) {
                                    append(" / $${String.format(java.util.Locale.US, "%.2f", limit)}")
                                }
                                if (isFreeTier) {
                                    append(" (Free Tier)")
                                }
                            }
                        } catch (e: Exception) {
                            "✓ Schlüssel gültig (OpenRouter)"
                        }
                        Result.success(successMessage)
                    } else if (response.code == 401) {
                        Result.failure(Exception("Schlüssel ungültig (401 Unauthorized)"))
                    } else if (response.code == 403) {
                        Result.failure(Exception("Zugriff verweigert (403 Forbidden)"))
                    } else if (response.code == 429) {
                        Result.failure(Exception("Ratenlimit erreicht (429 Rate Limit)"))
                    } else {
                        // Fallback ping via chat/completions if auth/key is unavailable
                        testOpenRouterViaChat(trimmedKey, modelId)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Verbindungstest fehlgeschlagen."))
        }
    }

    private fun testOpenRouterViaChat(apiKey: String, modelId: String): Result<String> {
        return try {
            val url = "https://openrouter.ai/api/v1/chat/completions"
            val targetModel = if (modelId.isNotBlank()) modelId else "openrouter/free"
            val pingJson = JSONObject().apply {
                put("model", targetModel)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "ping")
                    })
                })
                put("max_tokens", 5)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://deklination.app")
                .addHeader("X-Title", "German Deklination App")
                .post(pingJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success("✓ Schlüssel gültig (OpenRouter)")
                } else {
                    Result.failure(Exception(parseOpenAiErrorMessage(ModelProvider.OPENROUTER, response.code, body)))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Verbindungstest fehlgeschlagen."))
        }
    }

    /**
     * Dynamically fetches the list of available models from Google Generative Language API
     */
    suspend fun fetchGeminiModelsFromApi(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
        }

        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(IOException("Keine Internetverbindung"))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$trimmedKey"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful && body.isNotBlank()) {
                    Result.success(body)
                } else {
                    if (response.code in listOf(400, 401, 403)) {
                        Result.failure(Exception("Invalid Gemini API Key. Please check your key at ai.google.dev."))
                    } else {
                        val errorMsg = parseGeminiErrorMessage(response.code, body)
                        Result.failure(Exception(errorMsg))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Dynamically fetches the list of available models from OpenRouter API
     */
    suspend fun fetchOpenRouterModelsFromApi(apiKey: String = ""): Result<String> = withContext(Dispatchers.IO) {
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(IOException("Keine Internetverbindung"))
        }

        try {
            val url = "https://openrouter.ai/api/v1/models"
            val requestBuilder = Request.Builder()
                .url(url)
                .addHeader("HTTP-Referer", "https://deklination.app")
                .addHeader("X-Title", "German Deklination App")
                .get()

            val trimmedKey = apiKey.trim()
            if (trimmedKey.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $trimmedKey")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful && body.isNotBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Modelle konnten nicht geladen werden (Code: ${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSentencePrompt(sentence: String): String {
        return """
            You are an authoritative German linguistic dictionary and translator.
            Analyze and translate the German sentence or expression: "$sentence".

            IMPORTANT SPELLING & TYPO RULE: If the input sentence contains typos, misspellings, accent/umlaut errors, punctuation issues, or incorrect casing (e.g. "gutn morgn" instead of "Guten Morgen", "wie get es dir" instead of "Wie geht es dir"), SILENTLY correct it to the correct intended German sentence and return the translation for the corrected sentence. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the result as if the user had typed the sentence correctly from the beginning, without showing any indication that a correction occurred.

            Include:
            1. Corrected German sentence in "word" (capitalized correctly, natural German punctuation).
            2. Type: "Satz".
            3. Natural, accurate English translation in "meaningEnglish".
            4. For gender and pluralNoun, provide "–".
            5. For all grammatical cases (nominativ, akkusativ, dativ, genitiv, ablativ), provide "–".

            Return strictly valid JSON only matching this schema:
            {
              "type": "Satz",
              "word": "Guten Morgen",
              "gender": "–",
              "genderArticle": "",
              "pluralNoun": "–",
              "meaningEnglish": "Good morning",
              "singular": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "ablativ": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              },
              "plural": {
                "nominativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "akkusativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "dativ": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "genitiv": { "definite": "–", "indefinite": "–", "noArticle": "–" },
                "ablativ": { "definite": "–", "indefinite": "–", "noArticle": "–" }
              }
            }
        """.trimIndent()
    }

    private fun buildNounPrompt(word: String): String {
        return """
            You are an authoritative German linguistic dictionary.
            Analyze the German noun: "$word".
            IMPORTANT SPELLING & TYPO RULE: If the input word contains typos, misspellings, accent errors, or incorrect casing (e.g. "tifcht" instead of "Tisch", "abfel" instead of "Apfel", "frau" instead of "Frau"), SILENTLY correct it to the correct intended German noun and return the full declension table for the corrected word. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the JSON directly for the corrected word.
            Provide the complete declension table for SINGULAR and PLURAL across all 4 German grammatical cases: Nominativ, Akkusativ, Dativ, and Genitiv.
            Include:
            1. Base noun (capitalized correctly in German).
            2. Gender ("Maskulin", "Feminin", "Neutrum", or "Pluralwort").
            3. Base definite article ("der", "die", or "das").
            4. Plural noun form with article (e.g. "die Tische", "die Bücher", "die Frauen").
            5. Clear English translation / meaning (e.g. "table / desk", "book", "car", "woman").
            6. For Singular and Plural, provide the 4 cases (nominativ, akkusativ, dativ, genitiv) with:
               - definite: with definite article (der/die/das/des/dem/den etc. and inflected noun, e.g. "des Mannes", "dem Mann", "den Männern")
               - indefinite: with indefinite article (ein/eine/einen/einem/eines for singular; keine/keinen/keiner for plural)
               - noArticle: with zero article (e.g. "Mann", "Mannes", "Männer", "Männern")
            Ensure grammatical precision (weak masculine n-deklination if applicable like "des Studenten", genitive -(e)s, dativ plural -(e)n).

            Return strictly valid JSON only matching this schema:
            {
              "type": "Nomen",
              "word": "Tisch",
              "gender": "Maskulin",
              "genderArticle": "der",
              "pluralNoun": "die Tische",
              "meaningEnglish": "table / desk",
              "singular": {
                "nominativ": { "definite": "der Tisch", "indefinite": "ein Tisch", "noArticle": "Tisch" },
                "akkusativ": { "definite": "den Tisch", "indefinite": "einen Tisch", "noArticle": "Tisch" },
                "dativ": { "definite": "dem Tisch", "indefinite": "einem Tisch", "noArticle": "Tisch" },
                "genitiv": { "definite": "des Tisches", "indefinite": "eines Tisches", "noArticle": "Tisches" }
              },
              "plural": {
                "nominativ": { "definite": "die Tische", "indefinite": "keine Tische", "noArticle": "Tische" },
                "akkusativ": { "definite": "die Tische", "indefinite": "keine Tische", "noArticle": "Tische" },
                "dativ": { "definite": "den Tischen", "indefinite": "keinen Tischen", "noArticle": "Tischen" },
                "genitiv": { "definite": "der Tische", "indefinite": "keiner Tische", "noArticle": "Tische" }
              }
            }
        """.trimIndent()
    }

    private fun buildVerbPrompt(word: String): String {
        return """
            You are an authoritative German linguistic dictionary.
            Analyze the German verb: "$word".
            IMPORTANT SPELLING & TYPO RULE: If the input word contains typos, misspellings, accent errors, or incorrect casing (e.g. "arbeiteb" instead of "arbeiten", "gehn" instead of "gehen", "schlafn" instead of "schlafen"), SILENTLY correct it to the correct intended German verb and return the full conjugation table for the corrected verb. Do not ask questions, do not add comments, and do not say "Did you mean?". Return the JSON directly for the corrected word.
            Provide the complete conjugation across all 6 German tenses (Präsens, Präteritum, Perfekt, Plusquamperfekt, Futur I, Futur II) for the active indicative (Indikativ Aktiv), the Imperativ (du, ihr, Sie), and the fundamental forms (Infinitiv, Hilfsverb haben/sein, Partizip I, Partizip II).
            Include:
            1. Infinitiv (base verb in lowercase/correct German spelling, e.g. "arbeiten", "gehen", "sehen").
            2. Hilfsverb ("haben" or "sein").
            3. Auxiliary ("hat" or "ist").
            4. Präteritum 3rd person singular form (e.g. "arbeitete", "nahm", "ging").
            5. Partizip I (e.g. "arbeitend", "gehend").
            6. Partizip II (e.g. "gearbeitet", "gegangen").
            7. Verb Type ("stark", "schwach", or "gemischt").
            8. Separability ("trennbar" or "untrennbar", or empty string "" if not a compound prefix verb).
            9. Case object ("Akk.", "Dat.", "Gen.", "Akk. / Dat." or empty string "" if intransitive).
            10. Clear English translation / meaning (e.g. "to work", "to go", "to form, to educate, to build").
            11. Imperativ:
               - du (e.g. "arbeite", "geh")
               - ihr (e.g. "arbeitet", "geht")
               - sie (e.g. "arbeiten Sie", "gehen Sie")
            12. All 6 tenses with all 6 grammatical persons:
               - ich
               - du
               - erSieEs (er/sie/es)
               - wir
               - ihr
               - sieSie (sie/Sie)
            Tenses required:
            - praesens (Präsens)
            - praeteritum (Präteritum)
            - perfekt (Perfekt)
            - plusquamperfekt (Plusquamperfekt)
            - futur1 (Futur I)
            - futur2 (Futur II)

            Return strictly valid JSON only matching this exact schema:
            {
              "type": "Verb",
              "word": "arbeiten",
              "infinitiv": "arbeiten",
              "hilfsverb": "haben",
              "auxiliary": "hat",
              "praeteritum": "arbeitete",
              "partizip1": "arbeitend",
              "partizip2": "gearbeitet",
              "verb_type": "schwach",
              "separable": "untrennbar",
              "case_object": "Akk.",
              "meaningEnglish": "to work",
              "english": "to work",
              "imperativ": {
                "du": "arbeite",
                "ihr": "arbeitet",
                "sie": "arbeiten Sie"
              },
              "tenses": {
                "praesens": { "ich": "arbeite", "du": "arbeitest", "erSieEs": "arbeitet", "wir": "arbeiten", "ihr": "arbeitet", "sieSie": "arbeiten" },
                "praeteritum": { "ich": "arbeitete", "du": "arbeitetest", "erSieEs": "arbeitete", "wir": "arbeiteten", "ihr": "arbeitetet", "sieSie": "arbeiteten" },
                "perfekt": { "ich": "habe gearbeitet", "du": "hast gearbeitet", "erSieEs": "hat gearbeitet", "wir": "haben gearbeitet", "ihr": "habt gearbeitet", "sieSie": "haben gearbeitet" },
                "plusquamperfekt": { "ich": "hatte gearbeitet", "du": "hattest gearbeitet", "erSieEs": "hatte gearbeitet", "wir": "hatten gearbeitet", "ihr": "hattet gearbeitet", "sieSie": "hatten gearbeitet" },
                "futur1": { "ich": "werde arbeiten", "du": "wirst arbeiten", "erSieEs": "wird arbeiten", "wir": "werden arbeiten", "ihr": "werdet arbeiten", "sieSie": "werden arbeiten" },
                "futur2": { "ich": "werde gearbeitet haben", "du": "wirst gearbeitet haben", "erSieEs": "wird gearbeitet haben", "wir": "werden gearbeitet haben", "ihr": "werdet gearbeitet haben", "sieSie": "werden gearbeitet haben" }
              }
            }
        """.trimIndent()
    }

    private fun extractGeminiContentText(responseJson: String): String {
        val root = JSONObject(responseJson)
        val candidates = root.optJSONArray("candidates")
            ?: throw IOException("Keine Antwort von Gemini API erhalten.")
        if (candidates.length() == 0) {
            throw IOException("Keine Antwort von Gemini API erhalten.")
        }
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
            ?: throw IOException("Antwortinhalt fehlt.")
        val parts = content.optJSONArray("parts")
            ?: throw IOException("Antwortteile fehlen.")
        val firstPart = parts.getJSONObject(0)
        return cleanJsonText(firstPart.optString("text", ""))
    }

    private fun extractOpenAiContentText(responseJson: String): String {
        val root = JSONObject(responseJson)
        val choices = root.optJSONArray("choices")
            ?: throw IOException("Keine Antwortauswahl von ${root.optString("model")} erhalten.")
        if (choices.length() == 0) {
            throw IOException("Leere Antwort vom Modellanbieter erhalten.")
        }
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.optJSONObject("message")
            ?: throw IOException("Nachrichtenobjekt fehlt in der Antwort.")
        return cleanJsonText(message.optString("content", ""))
    }

    private fun cleanJsonText(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        }
        if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1)
        }
        return text
    }

    private fun parseGeminiErrorMessage(code: Int, responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message", "")
            when {
                code in listOf(400, 401, 403) && (message?.contains("API key", ignoreCase = true) == true || message?.contains("key", ignoreCase = true) == true) ->
                    "Invalid Gemini API Key. Please check your key at ai.google.dev."
                code == 400 ->
                    "Invalid Gemini API Key. Please check your key at ai.google.dev."
                code == 403 ->
                    "Invalid Gemini API Key. Please check your key at ai.google.dev."
                code == 429 ->
                    "Gemini API Kontingent erreicht (429). Bitte warten Sie einen Moment."
                !message.isNullOrBlank() ->
                    "Gemini Fehler: $message"
                else ->
                    "Verbindungsfehler zu Google Gemini (Code $code)"
            }
        } catch (e: Exception) {
            "Verbindungsfehler zu Google Gemini (Code $code)"
        }
    }

    private fun parseOpenAiErrorMessage(provider: ModelProvider, code: Int, responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message", "")
            when {
                code == 401 ->
                    "Ungültiger API-Schlüssel für ${provider.displayName}. Bitte überprüfen Sie Ihren Schlüssel."
                code == 403 ->
                    "Zugriff verweigert für ${provider.displayName} (403). Überprüfen Sie Berechtigungen oder Kontoguthaben."
                code == 429 ->
                    "Ratenlimit für ${provider.displayName} erreicht (429). Bitte warten Sie kurz."
                !message.isNullOrBlank() ->
                    "${provider.displayName} Fehler: $message"
                else ->
                    "Fehler bei ${provider.displayName} (Code $code)"
            }
        } catch (e: Exception) {
            "Fehler bei ${provider.displayName} (Code $code)"
        }
    }
}
