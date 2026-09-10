package com.example.data.repository

import com.example.data.AiModelInfo
import com.example.data.ApiKeyManager
import com.example.data.ModelProvider
import com.example.data.local.ExternalHistoryStorage
import com.example.data.local.SearchQueryDao
import com.example.data.local.SearchQueryEntity
import com.example.data.local.WordHistoryDao
import com.example.data.local.WordHistoryEntity
import com.example.data.model.WordDeclensionResult
import com.example.data.remote.GeminiDeclensionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class WordRepository(
    private val historyDao: WordHistoryDao,
    private val geminiService: GeminiDeclensionService,
    private val apiKeyManager: ApiKeyManager,
    private val searchQueryDao: SearchQueryDao? = null,
    val externalHistoryStorage: ExternalHistoryStorage? = null,
    val notebookRepository: com.example.data.notebook.NotebookRepository? = null
) {
    val searchHistory: Flow<List<WordHistoryEntity>> = historyDao.getAllHistory()

    val recentSearchQueries: Flow<List<SearchQueryEntity>> =
        searchQueryDao?.getAllRecentQueries() ?: flowOf(emptyList())

    fun getRecentQueriesByType(type: com.example.data.model.GrammarType): Flow<List<SearchQueryEntity>> =
        searchQueryDao?.getRecentQueriesByType(type.displayName) ?: flowOf(emptyList())

    suspend fun recordSearchQuery(query: String, type: com.example.data.model.GrammarType) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || searchQueryDao == null) return
        val existing = searchQueryDao.findQuery(trimmed, type.displayName)
        if (existing != null) {
            searchQueryDao.insertOrUpdate(
                existing.copy(
                    timestamp = System.currentTimeMillis(),
                    lookupCount = existing.lookupCount + 1
                )
            )
        } else {
            searchQueryDao.insertOrUpdate(
                SearchQueryEntity(
                    query = trimmed,
                    grammarType = type.displayName,
                    timestamp = System.currentTimeMillis(),
                    lookupCount = 1
                )
            )
        }
    }

    suspend fun deleteSearchQuery(id: Long) {
        searchQueryDao?.deleteById(id)
    }

    suspend fun clearAllSearchQueries() {
        searchQueryDao?.clearAllQueries()
    }

    fun getHistoryByType(type: com.example.data.model.GrammarType): Flow<List<WordHistoryEntity>> =
        historyDao.getHistoryByType(type.displayName)

    fun isApiKeyConfigured(): Boolean = apiKeyManager.isKeyConfigured()

    fun getSelectedProvider(): ModelProvider = apiKeyManager.getSelectedProvider()

    fun saveSelectedProvider(provider: ModelProvider) = apiKeyManager.saveSelectedProvider(provider)

    fun getGeminiApiKey(): String = apiKeyManager.getGeminiApiKey()

    fun getUserEnteredGeminiKey(): String = apiKeyManager.getUserEnteredGeminiKey()

    fun saveGeminiApiKey(key: String) = apiKeyManager.saveGeminiApiKey(key)

    fun clearGeminiApiKey() = apiKeyManager.clearGeminiApiKey()

    fun getOpenRouterApiKey(): String = apiKeyManager.getOpenRouterApiKey()

    fun saveOpenRouterApiKey(key: String) = apiKeyManager.saveOpenRouterApiKey(key)

    fun clearOpenRouterApiKey() = apiKeyManager.clearOpenRouterApiKey()

    fun getOtherApiKey(): String = apiKeyManager.getOpenRouterApiKey()

    fun saveOtherApiKey(key: String) = apiKeyManager.saveOpenRouterApiKey(key)

    fun clearOtherApiKey() = apiKeyManager.clearOpenRouterApiKey()

    fun getActiveApiKey(): String = apiKeyManager.getActiveApiKey()

    fun getApiKey(): String = getActiveApiKey()

    fun getUserEnteredKey(): String = getUserEnteredGeminiKey()

    fun saveApiKey(key: String) = saveGeminiApiKey(key)

    fun clearApiKey() = apiKeyManager.clearApiKey()

    fun getSelectedModel(provider: ModelProvider = getSelectedProvider()): String =
        apiKeyManager.getSelectedModel(provider)

    fun saveSelectedModel(modelId: String, provider: ModelProvider = getSelectedProvider()) =
        apiKeyManager.saveSelectedModel(modelId, provider)

    fun getAvailableModelsForProvider(provider: ModelProvider = getSelectedProvider()): List<AiModelInfo> =
        apiKeyManager.getModelsForActiveProvider(provider)

    suspend fun refreshGeminiModels(apiKey: String = getGeminiApiKey()): Result<List<AiModelInfo>> {
        val result = geminiService.fetchGeminiModelsFromApi(apiKey)
        return if (result.isSuccess) {
            val json = result.getOrNull().orEmpty()
            apiKeyManager.saveCachedGeminiModels(json)
            val parsedModels = apiKeyManager.parseGeminiModelsJson(json)
            Result.success(parsedModels)
        } else {
            // Fallback to cached models if available
            val cached = apiKeyManager.getCachedGeminiModels()
            val exception = result.exceptionOrNull() ?: Exception("Invalid Gemini API Key. Please check your key at ai.google.dev.")
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(exception)
            }
        }
    }

    suspend fun refreshOpenRouterModels(apiKey: String = getOpenRouterApiKey()): Result<List<AiModelInfo>> {
        val result = geminiService.fetchOpenRouterModelsFromApi(apiKey)
        return if (result.isSuccess) {
            val json = result.getOrNull().orEmpty()
            apiKeyManager.saveCachedOpenRouterModels(json)
            val parsedModels = apiKeyManager.parseOpenRouterModelsJson(json)
            Result.success(parsedModels)
        } else {
            // Fallback to cached models if available
            val cached = apiKeyManager.getCachedOpenRouterModels()
            val exception = result.exceptionOrNull() ?: Exception("Modelle konnten nicht online geladen werden")
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(exception)
            }
        }
    }

    fun getDailyUsage(provider: ModelProvider = getSelectedProvider(), modelId: String = getSelectedModel(provider)): Pair<Int, Int> =
        apiKeyManager.getDailyUsage(provider, modelId)

    suspend fun testApiKey(provider: ModelProvider, key: String, modelId: String): Result<String> {
        return geminiService.testApiKey(provider, key, modelId)
    }

    suspend fun searchWord(word: String): WordDeclensionResult {
        val grammarResult = searchGrammar(word, com.example.data.model.GrammarType.NOMEN)
        return when (grammarResult) {
            is com.example.data.model.GrammarResult.Noun -> grammarResult.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Erwartetes Nomen, aber Verb erhalten.")
        }
    }

    suspend fun searchVerb(word: String): com.example.data.model.VerbConjugationResult {
        val grammarResult = searchGrammar(word, com.example.data.model.GrammarType.VERB)
        return when (grammarResult) {
            is com.example.data.model.GrammarResult.Verb -> grammarResult.conjugation
            is com.example.data.model.GrammarResult.Noun -> throw IllegalStateException("Erwartetes Verb, aber Nomen erhalten.")
        }
    }

    suspend fun searchGrammar(word: String, type: com.example.data.model.GrammarType): com.example.data.model.GrammarResult {
        val provider = apiKeyManager.getSelectedProvider()
        val apiKey = apiKeyManager.getActiveApiKey()
        val modelId = apiKeyManager.getSelectedModel(provider)

        val result = geminiService.fetchGrammar(
            word = word,
            type = type,
            apiKey = apiKey,
            provider = provider,
            selectedModelId = modelId
        )

        // Increment daily usage on successful lookup
        apiKeyManager.incrementDailyUsage(provider)

        // Save or update in search history
        val entity = when (result) {
            is com.example.data.model.GrammarResult.Noun -> {
                val declension = result.declension
                WordHistoryEntity(
                    word = declension.word,
                    type = com.example.data.model.GrammarType.NOMEN.displayName,
                    data = declension.rawJson,
                    gender = declension.gender,
                    genderArticle = declension.genderArticle,
                    meaningEnglish = declension.meaningEnglish,
                    rawJsonResult = declension.rawJson,
                    timestamp = System.currentTimeMillis()
                )
            }
            is com.example.data.model.GrammarResult.Verb -> {
                val conjugation = result.conjugation
                WordHistoryEntity(
                    word = conjugation.word,
                    type = com.example.data.model.GrammarType.VERB.displayName,
                    data = conjugation.rawJson,
                    gender = conjugation.hilfsverb,          // Store hilfsverb as gender auxiliary
                    genderArticle = conjugation.partizip2,   // Store partizip2 as genderArticle auxiliary
                    meaningEnglish = conjugation.meaningEnglish,
                    rawJsonResult = conjugation.rawJson,
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        historyDao.deleteByWordAndType(result.word, type.displayName)
        val insertedId = historyDao.insert(entity)
        val savedEntity = entity.copy(id = insertedId)
        externalHistoryStorage?.appendOrUpdateItem(savedEntity)
        notebookRepository?.appendOrUpdateItemInActiveHistory(savedEntity)

        // Track user search query in local Room storage
        recordSearchQuery(result.word, type)

        return result
    }

    /**
     * Synchronizes persistent notebook history / external history with Room database.
     */
    suspend fun syncHistoryOnStartup() = withContext(Dispatchers.IO) {
        if (notebookRepository != null) {
            val notebookItems = notebookRepository.loadActiveHistory()
            val roomItems = historyDao.getAllHistoryList()
            if (notebookItems.isNotEmpty()) {
                historyDao.clearAll()
                historyDao.insertAll(notebookItems)
                return@withContext
            } else if (roomItems.isNotEmpty()) {
                notebookRepository.saveActiveHistory(roomItems)
                return@withContext
            }
        }

        val storage = externalHistoryStorage ?: return@withContext
        val externalItems = storage.loadHistory()
        val roomItems = historyDao.getAllHistoryList()

        if (externalItems.isNotEmpty() && roomItems.isEmpty()) {
            historyDao.insertAll(externalItems)
        } else if (roomItems.isNotEmpty() && externalItems.isEmpty()) {
            storage.saveHistory(roomItems)
        } else if (externalItems.isNotEmpty()) {
            val roomKeys = roomItems.map { "${it.word.lowercase()}_${it.type.lowercase()}" }.toSet()
            val toInsert = externalItems.filter {
                val key = "${it.word.lowercase()}_${it.type.lowercase()}"
                !roomKeys.contains(key)
            }
            if (toInsert.isNotEmpty()) {
                historyDao.insertAll(toInsert)
            }
        }
    }

    suspend fun reloadRoomWithItems(items: List<WordHistoryEntity>) = withContext(Dispatchers.IO) {
        historyDao.clearAll()
        if (items.isNotEmpty()) {
            historyDao.insertAll(items)
        }
    }

    suspend fun getGrammarFromHistory(entity: WordHistoryEntity): com.example.data.model.GrammarResult = withContext(Dispatchers.Default) {
        val json = if (entity.data.isNotBlank()) entity.data else entity.rawJsonResult
        val type = com.example.data.model.GrammarType.fromString(entity.type)
        try {
            com.example.data.model.GrammarResult.fromJson(json, fallbackType = type)
        } catch (e: Exception) {
            searchGrammar(entity.word, type)
        }
    }

    suspend fun getWordFromHistory(entity: WordHistoryEntity): WordDeclensionResult {
        val result = getGrammarFromHistory(entity)
        return when (result) {
            is com.example.data.model.GrammarResult.Noun -> result.declension
            is com.example.data.model.GrammarResult.Verb -> throw IllegalStateException("Eintrag ist ein Verb")
        }
    }

    suspend fun deleteHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteById(id)
        externalHistoryStorage?.deleteItem(id)
        notebookRepository?.deleteItemInActiveHistory(id)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
        externalHistoryStorage?.clearHistory()
        notebookRepository?.clearActiveHistory()
    }

    suspend fun clearHistoryByType(type: com.example.data.model.GrammarType) = withContext(Dispatchers.IO) {
        historyDao.clearByType(type.displayName)
        val remaining = historyDao.getAllHistoryList()
        externalHistoryStorage?.saveHistory(remaining)
        notebookRepository?.saveActiveHistory(remaining)
    }

    fun hasHistoryFolder(): Boolean {
        return externalHistoryStorage?.hasFolderUri() == true
    }

    fun getHistoryFolderName(): String {
        return externalHistoryStorage?.getFolderName() ?: "Kein Ordner ausgewählt"
    }

    suspend fun onFolderSelected(uri: android.net.Uri): Int {
        val storage = externalHistoryStorage ?: return 0
        storage.saveFolderUri(uri)
        // Automatically import history from history.json if it exists in the selected folder
        syncHistoryOnStartup()
        return historyDao.getAllHistoryList().size
    }

    suspend fun importHistoryFromFile(fileUri: android.net.Uri, replace: Boolean): Int {
        val storage = externalHistoryStorage ?: return 0
        val importedItems = storage.readHistoryFromUri(fileUri)
        if (importedItems.isEmpty()) return 0

        if (replace) {
            historyDao.clearAll()
            historyDao.insertAll(importedItems)
        } else {
            // Merge (default): insert items without wiping current items
            val existing = historyDao.getAllHistoryList()
            val existingKeys = existing.map { "${it.word.lowercase()}_${it.type.lowercase()}" }.toSet()
            val toInsert = importedItems.filter {
                val key = "${it.word.lowercase()}_${it.type.lowercase()}"
                !existingKeys.contains(key)
            }
            if (toInsert.isNotEmpty()) {
                historyDao.insertAll(toInsert)
            }
        }

        // Immediately update external storage with the new full history
        val fullList = historyDao.getAllHistoryList()
        storage.saveHistory(fullList)
        return importedItems.size
    }
}
