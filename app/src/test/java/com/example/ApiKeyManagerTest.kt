package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiKeyManager
import com.example.data.ModelProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiKeyManagerTest {

    private lateinit var context: Context
    private lateinit var apiKeyManager: ApiKeyManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        apiKeyManager = ApiKeyManager(context)
        apiKeyManager.clearGeminiApiKey()
        apiKeyManager.clearOpenRouterApiKey()
    }

    @Test
    fun testOnlyTwoProvidersSupported() {
        val providers = ModelProvider.entries
        assertEquals(2, providers.size)
        assertTrue(providers.contains(ModelProvider.GEMINI))
        assertTrue(providers.contains(ModelProvider.OPENROUTER))
    }

    @Test
    fun testSeparateKeyStorage() {
        apiKeyManager.saveGeminiApiKey("AIzaSy-gemini-test-key")
        apiKeyManager.saveOpenRouterApiKey("sk-or-v1-openrouter-test-key")

        assertEquals("AIzaSy-gemini-test-key", apiKeyManager.getGeminiApiKey())
        assertEquals("sk-or-v1-openrouter-test-key", apiKeyManager.getOpenRouterApiKey())

        // Active key depends on provider
        apiKeyManager.saveSelectedProvider(ModelProvider.GEMINI)
        assertEquals("AIzaSy-gemini-test-key", apiKeyManager.getActiveApiKey())

        apiKeyManager.saveSelectedProvider(ModelProvider.OPENROUTER)
        assertEquals("sk-or-v1-openrouter-test-key", apiKeyManager.getActiveApiKey())
    }

    @Test
    fun testGeminiJsonParsingAndFiltering() {
        val sampleJson = """
            {
              "models": [
                {
                  "name": "models/gemini-2.5-flash",
                  "displayName": "Gemini 2.5 Flash",
                  "description": "Fast and versatile model",
                  "supportedGenerationMethods": ["generateContent", "countTokens"]
                },
                {
                  "name": "models/gemini-2.5-pro",
                  "displayName": "Gemini 2.5 Pro",
                  "description": "Complex reasoning model",
                  "supportedGenerationMethods": ["generateContent"]
                },
                {
                  "name": "models/text-embedding-004",
                  "displayName": "Text Embedding 004",
                  "description": "Embedding model",
                  "supportedGenerationMethods": ["embedContent"]
                }
              ]
            }
        """.trimIndent()

        val parsed = apiKeyManager.parseGeminiModelsJson(sampleJson)
        // Only generateContent models are included (embedding is filtered out)
        assertEquals(2, parsed.size)
        assertEquals("gemini-2.5-flash", parsed[0].id)
        assertEquals("Gemini 2.5 Flash", parsed[0].name)
        assertEquals("gemini-2.5-pro", parsed[1].id)
        assertEquals("Gemini 2.5 Pro", parsed[1].name)
    }

    @Test
    fun testGeminiCaching() {
        val sampleJson = """
            {
              "models": [
                {
                  "name": "models/gemini-2.5-flash",
                  "displayName": "Gemini 2.5 Flash",
                  "supportedGenerationMethods": ["generateContent"]
                }
              ]
            }
        """.trimIndent()

        apiKeyManager.saveCachedGeminiModels(sampleJson)
        val cached = apiKeyManager.getCachedGeminiModels()
        assertNotNull(cached)
        assertTrue(cached.isNotEmpty())
        assertEquals("gemini-2.5-flash", cached[0].id)
    }

    @Test
    fun testOpenRouterJsonParsingAndSorting() {
        val sampleJson = """
            {
              "data": [
                {
                  "id": "anthropic/claude-3.5-sonnet",
                  "name": "Anthropic: Claude 3.5 Sonnet",
                  "description": "Smart model",
                  "pricing": {"prompt": "0.000003", "completion": "0.000015"}
                },
                {
                  "id": "meta-llama/llama-3.3-70b-instruct:free",
                  "name": "Meta: Llama 3.3 70B Instruct (free)",
                  "description": "Powerful open weights",
                  "pricing": {"prompt": "0", "completion": "0"}
                },
                {
                  "id": "google/gemini-2.0-flash-exp:free",
                  "name": "Google: Gemini 2.0 Flash Experimental (free)",
                  "description": "Fast multimodal free",
                  "pricing": {"prompt": "0", "completion": "0"}
                }
              ]
            }
        """.trimIndent()

        val parsed = apiKeyManager.parseOpenRouterModelsJson(sampleJson)
        assertEquals(3, parsed.size)
        // Free models must come first and sorted alphabetically by name (Google before Meta)
        assertTrue(parsed[0].isFreeTier)
        assertTrue(parsed[1].isFreeTier)
        assertFalse(parsed[2].isFreeTier)
        assertEquals("google/gemini-2.0-flash-exp:free", parsed[0].id)
        assertEquals("meta-llama/llama-3.3-70b-instruct:free", parsed[1].id)
        assertEquals("anthropic/claude-3.5-sonnet", parsed[2].id)
    }

    @Test
    fun testOpenRouterFunctionalDescriptionExtraction() {
        val sampleJson = """
            {
              "data": [
                {
                  "id": "openai/gpt-4o",
                  "name": "GPT-4o",
                  "architecture": {
                    "input_modalities": ["text", "image"],
                    "output_modalities": ["text"]
                  },
                  "pricing": {"prompt": "0.000005", "completion": "0.000015"}
                },
                {
                  "id": "deepseek/deepseek-coder",
                  "name": "DeepSeek Coder",
                  "architecture": {
                    "input_modalities": ["text"],
                    "output_modalities": ["text"]
                  },
                  "pricing": {"prompt": "0.000001", "completion": "0.000002"}
                },
                {
                  "id": "deepseek/deepseek-r1:free",
                  "name": "DeepSeek R1 Free",
                  "architecture": {
                    "input_modalities": ["text"],
                    "output_modalities": ["text"]
                  },
                  "pricing": {"prompt": "0", "completion": "0"}
                },
                {
                  "id": "meta-llama/llama-3.1-8b-instruct",
                  "name": "Llama 3.1 8B",
                  "architecture": {
                    "input_modalities": ["text"],
                    "output_modalities": ["text"]
                  },
                  "pricing": {"prompt": "0.000001", "completion": "0.000001"}
                },
                {
                  "id": "baai/bge-m3",
                  "name": "BGE M3",
                  "architecture": {
                    "input_modalities": ["text"],
                    "output_modalities": ["embedding"]
                  },
                  "pricing": {"prompt": "0.000001", "completion": "0"}
                },
                {
                  "id": "cohere/rerank-v3",
                  "name": "Cohere Rerank",
                  "architecture": {
                    "input_modalities": ["text"],
                    "output_modalities": ["text"]
                  },
                  "pricing": {"prompt": "0.000001", "completion": "0"}
                }
              ]
            }
        """.trimIndent()

        val parsed = apiKeyManager.parseOpenRouterModelsJson(sampleJson)
        val gpt4o = parsed.first { it.id == "openai/gpt-4o" }
        assertEquals("Multimodales Modell (Text, Bild)", gpt4o.functionalDescription)

        val coder = parsed.first { it.id == "deepseek/deepseek-coder" }
        assertEquals("Code-Generierung", coder.functionalDescription)

        val r1 = parsed.first { it.id == "deepseek/deepseek-r1:free" }
        assertEquals("Reasoning & Logik", r1.functionalDescription)

        val llama = parsed.first { it.id == "meta-llama/llama-3.1-8b-instruct" }
        assertEquals("Text-Konversation", llama.functionalDescription)

        val embed = parsed.first { it.id == "baai/bge-m3" }
        assertEquals("Texteinbettung", embed.functionalDescription)

        val rerank = parsed.first { it.id == "cohere/rerank-v3" }
        assertEquals("Ergebnis-Neuordnung", rerank.functionalDescription)
    }

    @Test
    fun testOpenRouterCaching() {
        val sampleJson = """
            {
              "data": [
                {
                  "id": "openrouter/free",
                  "name": "OpenRouter Free Router",
                  "pricing": {"prompt": "0", "completion": "0"}
                }
              ]
            }
        """.trimIndent()

        apiKeyManager.saveCachedOpenRouterModels(sampleJson)
        val cached = apiKeyManager.getCachedOpenRouterModels()
        assertNotNull(cached)
        assertTrue(cached.isNotEmpty())
        assertEquals("openrouter/free", cached[0].id)
    }
}
