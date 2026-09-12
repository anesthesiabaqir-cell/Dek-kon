package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiModelInfo
import com.example.data.ApiKeyManager
import com.example.data.GeminiModelInfo
import com.example.data.ModelProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ExternalHistoryStorage
import com.example.data.local.SearchQueryEntity
import com.example.data.local.WordHistoryEntity
import com.example.data.model.GrammarResult
import com.example.data.model.GrammarType
import com.example.data.model.VerbConjugationResult
import com.example.data.model.WordDeclensionResult
import com.example.data.notebook.Notebook
import com.example.data.notebook.NotebookRepository
import com.example.data.notebook.NotebookSettings
import com.example.data.notebook.ImportedJsonResult
import com.example.data.notebook.ImportedNotebookPackage
import com.example.data.remote.GeminiDeclensionService
import com.example.data.repository.WordRepository
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.ThemePreferences
import com.example.util.ExportSharingManager
import com.example.util.GermanTtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class JsonExportTarget(
    val fileName: String,
    val jsonContent: String,
    val title: String
)

data class ImportSingleChoice(
    val detectedName: String,
    val words: List<WordHistoryEntity>,
    val settings: NotebookSettings? = null
)

data class ImportMultiChoice(
    val packages: List<ImportedNotebookPackage>,
    val fallbackName: String
) {
    val notebooksMap: Map<String, List<WordHistoryEntity>>
        get() = packages.associate { it.name to it.words }
}

data class PostImportApiKeyPrompt(
    val notebookName: String,
    val provider: String = "GEMINI",
    val modelId: String = "gemini-2.5-flash-latest"
)

data class MainUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedGrammarType: GrammarType = GrammarType.NOMEN,
    val currentResult: WordDeclensionResult? = null,
    val currentGrammarResult: GrammarResult? = null,
    val errorMessage: String? = null,
    val hasApiKey: Boolean = false,
    val isApiKeyDialogOpen: Boolean = false,
    val selectedProvider: ModelProvider = ModelProvider.GEMINI,
    val apiKeyInput: String = "", // Gemini key
    val geminiApiKeyInput: String = "",
    val openRouterApiKeyInput: String = "",
    val otherApiKeyInput: String = "",
    val selectedModel: String = ApiKeyManager.DEFAULT_MODEL,
    val availableModels: List<AiModelInfo> = emptyList(),
    val isRefreshingModels: Boolean = false,
    val refreshModelsStatus: String? = null,
    val selectedNumberTab: Int = 0, // 0: Singular, 1: Plural, 2: Both
    val selectedHistoryFilter: Int = 0, // 0: Alle, 1: Nomen, 2: Verben
    val isExportDialogOpen: Boolean = false,
    val exportOpenedFromSettings: Boolean = false,
    val selectedTheme: AppThemePackage = AppThemePackage.SCHIEFER,
    val isTestingKey: Boolean = false,
    val testKeyResult: Pair<Boolean, String>? = null, // Pair(isSuccess, message)
    val remainingDailyRequests: Int = 20,
    val totalDailyQuota: Int = 20,
    val isFolderSelectionRequired: Boolean = false,
    val selectedHistoryFolderName: String = "Kein Ordner ausgewählt",
    val replaceHistoryOnImport: Boolean = false,
    val importStatusMessage: String? = null,
    val allNotebooks: List<Notebook> = emptyList(),
    val activeNotebook: Notebook? = null,
    val themeMode: String = "auto", // "auto", "light", "dark"
    val appLanguage: String = "de", // "de", "en", "ar"
    val isCreateNotebookDialogOpen: Boolean = false,
    val isManageNotebooksDialogOpen: Boolean = false,
    val activeJsonExportTarget: JsonExportTarget? = null,
    val isSelectNotebooksExportDialogOpen: Boolean = false,
    val pendingTreeExportNotebookIds: List<String>? = null,
    val importSingleChoice: ImportSingleChoice? = null,
    val importMultiChoice: ImportMultiChoice? = null,
    val postImportApiKeyPrompt: PostImportApiKeyPrompt? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val notebookRepository: NotebookRepository = NotebookRepository(application)
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private val apiKeyManager: ApiKeyManager = ApiKeyManager(application)
    private val geminiService: GeminiDeclensionService = GeminiDeclensionService(application)
    private val externalStorage: ExternalHistoryStorage = ExternalHistoryStorage(application)
    private val repository: WordRepository = WordRepository(
        historyDao = database.wordHistoryDao(),
        geminiService = geminiService,
        apiKeyManager = apiKeyManager,
        searchQueryDao = database.searchQueryDao(),
        externalHistoryStorage = externalStorage,
        notebookRepository = notebookRepository
    )
    private val themePreferences: ThemePreferences = ThemePreferences(application)
    private val ttsManager: GermanTtsManager = GermanTtsManager(application)

    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val currentlySpokenText: StateFlow<String?> = ttsManager.currentlySpokenText
    val ttsSpeedRate: StateFlow<Float> = ttsManager.speechRate

    fun setTtsSpeed(speed: Float) {
        ttsManager.setSpeedRate(speed)
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(currentSettings.copy(ttsSpeed = speed))
        }
    }

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedTheme = themePreferences.getSelectedTheme(),
            themeMode = themePreferences.getThemeMode(),
            appLanguage = themePreferences.getAppLanguage()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<WordHistoryEntity>> = repository.searchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val recentQueries: StateFlow<List<SearchQueryEntity>> = repository.recentSearchQueries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            try {
                repository.syncHistoryOnStartup()
            } catch (e: Throwable) {
                Log.e("MainViewModel", "Failed to sync history on startup", e)
            }
        }

        try {
            val allNotebooks = notebookRepository.listAllNotebooks()
            val activeNotebook = notebookRepository.getActiveNotebook()
            val notebookSettings = activeNotebook.settings

            // Apply notebook settings
            val activeTheme = AppThemePackage.fromId(notebookSettings.themeColorId)
            val activeThemeMode = notebookSettings.themeMode
            ttsManager.setSpeedRate(notebookSettings.ttsSpeed)

            val isConfigured = if (notebookSettings.customApiKey.isNotBlank() || notebookSettings.openRouterApiKey.isNotBlank()) {
                true
            } else {
                repository.isApiKeyConfigured()
            }
            val provider = ModelProvider.fromId(notebookSettings.selectedProvider)
            val currentModel = notebookSettings.selectedModel.ifBlank { repository.getSelectedModel(provider) }
            val geminiKey = notebookSettings.customApiKey.ifBlank { repository.getUserEnteredGeminiKey() }
            val openRouterKey = notebookSettings.openRouterApiKey.ifBlank { repository.getOpenRouterApiKey() }
            val (remaining, total) = repository.getDailyUsage(provider, currentModel)
            val folderName = if (notebookSettings.storageFolderUri.isNotBlank()) {
                notebookSettings.storageFolderUri
            } else {
                "Standard (${activeNotebook.name})"
            }

            val currentAppLanguage = themePreferences.getAppLanguage()
            val activeLanguage = if (currentAppLanguage.isNotBlank()) currentAppLanguage else notebookSettings.appLanguage.ifBlank { "de" }
            themePreferences.saveAppLanguage(activeLanguage)

            _uiState.value = _uiState.value.copy(
                allNotebooks = allNotebooks,
                activeNotebook = activeNotebook,
                themeMode = activeThemeMode,
                appLanguage = activeLanguage,
                hasApiKey = isConfigured,
                selectedProvider = provider,
                apiKeyInput = geminiKey,
                geminiApiKeyInput = geminiKey,
                openRouterApiKeyInput = openRouterKey,
                otherApiKeyInput = openRouterKey,
                selectedModel = currentModel,
                availableModels = repository.getAvailableModelsForProvider(provider),
                selectedTheme = activeTheme,
                remainingDailyRequests = remaining,
                totalDailyQuota = total,
                isFolderSelectionRequired = false,
                selectedHistoryFolderName = folderName
            )
        } catch (e: Throwable) {
            Log.e("MainViewModel", "Failed to load initial notebook settings", e)
        }
    }

    private fun refreshUsageQuota() {
        val provider = _uiState.value.selectedProvider
        val model = _uiState.value.selectedModel
        val (remaining, total) = repository.getDailyUsage(provider, model)
        _uiState.value = _uiState.value.copy(
            remainingDailyRequests = remaining,
            totalDailyQuota = total
        )
    }

    fun onThemeSelected(theme: AppThemePackage) {
        themePreferences.saveSelectedTheme(theme)
        _uiState.value = _uiState.value.copy(selectedTheme = theme)
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(currentSettings.copy(themeColorId = theme.id))
        }
    }

    fun onGrammarTypeSelected(type: GrammarType) {
        if (_uiState.value.selectedGrammarType != type) {
            _uiState.value = _uiState.value.copy(
                selectedGrammarType = type,
                searchQuery = "",
                errorMessage = null
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(searchQuery = newQuery)
    }

    fun onSearch(specificQuery: String? = null) {
        val targetQuery = specificQuery ?: _uiState.value.searchQuery
        val trimmed = targetQuery.trim()
        if (trimmed.isEmpty()) {
            val emptyMsg = if (_uiState.value.selectedGrammarType == GrammarType.VERB) {
                "Bitte geben Sie ein deutsches Verb ein (z. B. 'arbeiten')."
            } else {
                "Bitte geben Sie ein deutsches Nomen ein (z. B. 'Tisch')."
            }
            _uiState.value = _uiState.value.copy(errorMessage = emptyMsg)
            return
        }

        val hasKey = repository.isApiKeyConfigured()
        if (!hasKey) {
            val providerName = _uiState.value.selectedProvider.displayName
            _uiState.value = _uiState.value.copy(
                isApiKeyDialogOpen = true,
                errorMessage = "Bitte konfigurieren Sie Ihren API-Schlüssel für $providerName in den Einstellungen."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                errorMessage = null,
                searchQuery = trimmed
            )

            try {
                if (_uiState.value.selectedGrammarType == GrammarType.VERB) {
                    val verbResult = repository.searchVerb(trimmed)
                    refreshUsageQuota()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        currentGrammarResult = GrammarResult.Verb(verbResult),
                        currentResult = null,
                        errorMessage = null
                    )
                } else {
                    val declensionResult = repository.searchWord(trimmed)
                    refreshUsageQuota()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        currentResult = declensionResult,
                        currentGrammarResult = GrammarResult.Noun(declensionResult),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = e.message ?: "Fehler beim Abrufen der Grammatikdaten."
                )
            }
        }
    }

    fun onSelectHistoryItem(entity: WordHistoryEntity) {
        viewModelScope.launch {
            try {
                val isVerb = entity.type.equals("Verb", ignoreCase = true)
                if (isVerb) {
                    val cachedVerb = withContext(Dispatchers.Default) {
                        VerbConjugationResult.fromJson(entity.rawJsonResult)
                    }
                    _uiState.value = _uiState.value.copy(
                        selectedGrammarType = GrammarType.VERB,
                        currentGrammarResult = GrammarResult.Verb(cachedVerb),
                        currentResult = null,
                        searchQuery = entity.word,
                        errorMessage = null
                    )
                } else {
                    val cachedResult = withContext(Dispatchers.Default) {
                        WordDeclensionResult.fromJson(entity.rawJsonResult)
                    }
                    _uiState.value = _uiState.value.copy(
                        selectedGrammarType = GrammarType.NOMEN,
                        currentResult = cachedResult,
                        currentGrammarResult = GrammarResult.Noun(cachedResult),
                        searchQuery = entity.word,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                onSearch(entity.word)
            }
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    suspend fun syncExternalHistory() {
        repository.syncHistoryOnStartup()
    }

    fun getWordRepository(): WordRepository = repository

    fun onOpenApiKeyDialog() {
        val provider = repository.getSelectedProvider()
        val currentModel = repository.getSelectedModel(provider)
        val geminiKey = repository.getUserEnteredGeminiKey()
        val openRouterKey = repository.getOpenRouterApiKey()
        val (remaining, total) = repository.getDailyUsage(provider, currentModel)

        val models = repository.getAvailableModelsForProvider(provider)
        val currentLanguage = themePreferences.getAppLanguage()

        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = true,
            appLanguage = currentLanguage,
            selectedProvider = provider,
            apiKeyInput = geminiKey,
            geminiApiKeyInput = geminiKey,
            openRouterApiKeyInput = openRouterKey,
            otherApiKeyInput = openRouterKey,
            selectedModel = currentModel,
            availableModels = models,
            testKeyResult = null,
            isTestingKey = false,
            refreshModelsStatus = null,
            remainingDailyRequests = remaining,
            totalDailyQuota = total
        )

        if (provider == ModelProvider.GEMINI && models.isEmpty() && repository.getGeminiApiKey().isNotBlank()) {
            onRefreshGeminiModels()
        }
    }

    fun onCloseApiKeyDialog() {
        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = false,
            exportOpenedFromSettings = false,
            testKeyResult = null,
            isTestingKey = false,
            refreshModelsStatus = null
        )
    }

    fun onSelectProvider(provider: ModelProvider) {
        repository.saveSelectedProvider(provider)
        val modelForProvider = repository.getSelectedModel(provider)
        val models = repository.getAvailableModelsForProvider(provider)
        val (remaining, total) = repository.getDailyUsage(provider, modelForProvider)

        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            selectedModel = modelForProvider,
            availableModels = models,
            testKeyResult = null,
            refreshModelsStatus = null,
            remainingDailyRequests = remaining,
            totalDailyQuota = total,
            hasApiKey = repository.isApiKeyConfigured()
        )

        // If switching to OpenRouter and models list is empty or minimal, initiate a background fetch
        if (provider == ModelProvider.OPENROUTER && models.size <= 2) {
            onRefreshOpenRouterModels()
        } else if (provider == ModelProvider.GEMINI && models.isEmpty() && repository.getGeminiApiKey().isNotBlank()) {
            onRefreshGeminiModels()
        }
    }

    fun onSelectModel(modelId: String) {
        val provider = _uiState.value.selectedProvider
        repository.saveSelectedModel(modelId, provider)
        val (remaining, total) = repository.getDailyUsage(provider, modelId)
        _uiState.value = _uiState.value.copy(
            selectedModel = modelId,
            remainingDailyRequests = remaining,
            totalDailyQuota = total
        )
    }

    fun onGeminiApiKeyInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            apiKeyInput = input,
            geminiApiKeyInput = input,
            testKeyResult = null
        )
    }

    fun onOpenRouterApiKeyInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            openRouterApiKeyInput = input,
            otherApiKeyInput = input,
            testKeyResult = null
        )
    }

    fun onOtherApiKeyInputChange(input: String) {
        onOpenRouterApiKeyInputChange(input)
    }

    fun onApiKeyInputChange(input: String) {
        onGeminiApiKeyInputChange(input)
    }

    /**
     * Dynamically fetches OpenRouter models and updates the available list
     */
    fun onRefreshOpenRouterModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshingModels = true,
                refreshModelsStatus = null
            )
            val key = _uiState.value.openRouterApiKeyInput.ifBlank { repository.getOpenRouterApiKey() }
            val result = repository.refreshOpenRouterModels(key)
            if (result.isSuccess) {
                val models = result.getOrNull().orEmpty()
                val currentModel = _uiState.value.selectedModel
                val updatedModel = if (models.any { it.id == currentModel }) currentModel else models.firstOrNull()?.id ?: "openrouter/free"
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = models,
                    selectedModel = updatedModel,
                    refreshModelsStatus = "✓ ${models.size} Modelle geladen"
                )
            } else {
                val cached = repository.getAvailableModelsForProvider(ModelProvider.OPENROUTER)
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = cached,
                    refreshModelsStatus = "Offline: Lokale Modelle geladen"
                )
            }
        }
    }

    /**
     * Dynamically fetches Gemini models and updates the available list
     */
    fun onRefreshGeminiModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshingModels = true,
                refreshModelsStatus = null
            )
            val key = _uiState.value.geminiApiKeyInput.ifBlank { repository.getGeminiApiKey() }
            val result = repository.refreshGeminiModels(key)
            if (result.isSuccess) {
                val models = result.getOrNull().orEmpty()
                val currentModel = _uiState.value.selectedModel
                val updatedModel = if (models.any { it.id == currentModel }) {
                    currentModel
                } else {
                    models.firstOrNull()?.id ?: ""
                }
                if (updatedModel.isNotBlank() && updatedModel != currentModel) {
                    repository.saveSelectedModel(updatedModel, ModelProvider.GEMINI)
                }
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = models,
                    selectedModel = updatedModel,
                    refreshModelsStatus = "✓ ${models.size} Modelle geladen"
                )
            } else {
                val cached = repository.getAvailableModelsForProvider(ModelProvider.GEMINI)
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    availableModels = cached,
                    refreshModelsStatus = if (cached.isNotEmpty()) "Offline: Lokale Modelle geladen" else (result.exceptionOrNull()?.message ?: "Fehler beim Laden der Modelle")
                )
            }
        }
    }

    /**
     * Tests the API key of the selected provider with an actual network ping
     */
    fun onTestApiKey() {
        val provider = _uiState.value.selectedProvider
        val keyToTest = if (provider == ModelProvider.GEMINI) {
            _uiState.value.geminiApiKeyInput.trim().ifEmpty { repository.getGeminiApiKey() }
        } else {
            _uiState.value.openRouterApiKeyInput.trim().ifEmpty { repository.getOpenRouterApiKey() }
        }

        if (keyToTest.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                testKeyResult = Pair(false, "Bitte ${provider.displayName}-Key eingeben")
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingKey = true, testKeyResult = null)
            val result = repository.testApiKey(provider, keyToTest, _uiState.value.selectedModel)
            val isSuccess = result.isSuccess
            val message = if (isSuccess) {
                result.getOrNull() ?: "✓ Schlüssel gültig"
            } else {
                result.exceptionOrNull()?.message ?: "Verbindung fehlgeschlagen"
            }
            _uiState.value = _uiState.value.copy(
                isTestingKey = false,
                testKeyResult = Pair(isSuccess, message)
            )
            if (isSuccess) {
                if (provider == ModelProvider.GEMINI) {
                    onRefreshGeminiModels()
                } else if (provider == ModelProvider.OPENROUTER) {
                    onRefreshOpenRouterModels()
                }
            }
        }
    }

    fun onSaveApiKey() {
        val geminiKey = _uiState.value.geminiApiKeyInput.trim()
        val openRouterKey = _uiState.value.openRouterApiKeyInput.trim()

        if (geminiKey.isNotEmpty()) {
            repository.saveGeminiApiKey(geminiKey)
        } else {
            repository.clearGeminiApiKey()
        }

        if (openRouterKey.isNotEmpty()) {
            repository.saveOpenRouterApiKey(openRouterKey)
        } else {
            repository.clearOpenRouterApiKey()
        }

        val isConfigured = repository.isApiKeyConfigured()
        _uiState.value = _uiState.value.copy(
            hasApiKey = isConfigured,
            isApiKeyDialogOpen = false,
            errorMessage = if (isConfigured) null else _uiState.value.errorMessage
        )

        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            saveActiveNotebookSettings(
                currentSettings.copy(
                    customApiKey = geminiKey,
                    openRouterApiKey = openRouterKey,
                    selectedProvider = _uiState.value.selectedProvider.name,
                    selectedModel = _uiState.value.selectedModel
                )
            )
        }

        if (geminiKey.isNotEmpty() && repository.getSelectedProvider() == ModelProvider.GEMINI) {
            onRefreshGeminiModels()
        }
    }

    fun onClearApiKey() {
        val provider = _uiState.value.selectedProvider
        if (provider == ModelProvider.GEMINI) {
            repository.clearGeminiApiKey()
            _uiState.value = _uiState.value.copy(
                geminiApiKeyInput = "",
                apiKeyInput = ""
            )
        } else {
            repository.clearOpenRouterApiKey()
            _uiState.value = _uiState.value.copy(
                openRouterApiKeyInput = "",
                otherApiKeyInput = ""
            )
        }
        _uiState.value = _uiState.value.copy(
            hasApiKey = repository.isApiKeyConfigured(),
            testKeyResult = null
        )
    }

    fun onSelectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedNumberTab = tabIndex)
    }

    fun onSelectHistoryFilter(filterIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedHistoryFilter = filterIndex)
    }

    fun onOpenExportDialog(fromSettings: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            isExportDialogOpen = true,
            exportOpenedFromSettings = fromSettings
        )
    }

    fun onCloseExportDialog() {
        val fromSettings = _uiState.value.exportOpenedFromSettings
        _uiState.value = _uiState.value.copy(
            isExportDialogOpen = false,
            exportOpenedFromSettings = false,
            isApiKeyDialogOpen = fromSettings || _uiState.value.isApiKeyDialogOpen
        )
    }

    fun onDismissExportDialog() {
        onCloseExportDialog()
    }

    /**
     * Handles the Back action in accordance with the application's strict back-stack rules:
     * 1. If Print/Export dialog is open:
     *    - Closes Export dialog. If opened from Settings, returns to Settings. Otherwise returns to previous view.
     * 2. If Settings (ApiKeyDialog) is open:
     *    - Closes Settings dialog and returns to the previous screen (Word Detail View or Main Screen).
     * 3. If Word Detail View is active (currentGrammarResult != null || currentResult != null):
     *    - Clears the active word result and returns to the Main Screen.
     * 4. If a History sub-tab is active (selectedHistoryFilter != 0, e.g. Nomen or Verben filter):
     *    - Returns to the base "Alle" (all history) tab on the Main Screen.
     * 5. If on the Main Screen (root of the app, no sub-screens or dialogs active):
     *    - Returns false so the caller can move the task to the back (exit immediately to home screen).
     *
     * Returns true if the back event was consumed/handled within the app.
     */
    fun handleBackPress(): Boolean {
        val state = _uiState.value
        return when {
            state.isCreateNotebookDialogOpen -> {
                closeCreateNotebookDialog()
                true
            }
            state.isManageNotebooksDialogOpen -> {
                closeManageNotebooksDialog()
                true
            }
            state.isExportDialogOpen -> {
                onCloseExportDialog()
                true
            }
            state.isApiKeyDialogOpen -> {
                onCloseApiKeyDialog()
                true
            }
            state.currentGrammarResult != null || state.currentResult != null -> {
                onClearResult()
                true
            }
            state.selectedHistoryFilter != 0 -> {
                onSelectHistoryFilter(0)
                true
            }
            else -> {
                false
            }
        }
    }

    /**
     * Retrieves all history items deduplicated by base word, ordered chronologically
     * by the earliest search timestamp (oldest first).
     */
    fun getHistoryForExport(): List<WordDeclensionResult> {
        val history = historyList.value
        if (history.isEmpty()) {
            return _uiState.value.currentResult?.let { listOf(it) } ?: emptyList()
        }

        val grouped = history.groupBy { it.word.trim().lowercase(java.util.Locale.GERMAN) }
        val earliestEntries = grouped.values.mapNotNull { list ->
            list.minByOrNull { it.timestamp }
        }.sortedBy { it.timestamp }

        val results = earliestEntries.mapNotNull { entity ->
            try {
                WordDeclensionResult.fromJson(entity.rawJsonResult)
            } catch (e: Exception) {
                null
            }
        }

        val current = _uiState.value.currentResult
        if (current != null && results.none { it.word.equals(current.word, ignoreCase = true) }) {
            return results + current
        }

        return results
    }

    fun getHistoryAsDeclensionResults(): List<WordDeclensionResult> {
        return getHistoryForExport()
    }

    /**
     * Retrieves deduplicated lists of both Nouns and Verbs for unified export (PDF / Excel).
     */
    fun getUnifiedHistoryForExport(): Pair<List<WordDeclensionResult>, List<VerbConjugationResult>> {
        val history = historyList.value.sortedBy { it.timestamp }
        val nouns = mutableListOf<WordDeclensionResult>()
        val verbs = mutableListOf<VerbConjugationResult>()

        val seenNouns = mutableSetOf<String>()
        val seenVerbs = mutableSetOf<String>()

        history.forEach { entity ->
            val isVerb = entity.type.equals("Verb", ignoreCase = true)
            val key = entity.word.trim().lowercase(java.util.Locale.GERMAN)
            if (isVerb) {
                if (seenVerbs.add(key)) {
                    try {
                        verbs.add(VerbConjugationResult.fromJson(entity.rawJsonResult))
                    } catch (_: Exception) {}
                }
            } else {
                if (seenNouns.add(key)) {
                    try {
                        nouns.add(WordDeclensionResult.fromJson(entity.rawJsonResult))
                    } catch (_: Exception) {}
                }
            }
        }

        // Also include currently displayed result if not in list
        when (val curr = _uiState.value.currentGrammarResult) {
            is GrammarResult.Noun -> {
                val k = curr.declension.word.trim().lowercase(java.util.Locale.GERMAN)
                if (seenNouns.add(k)) {
                    nouns.add(curr.declension)
                }
            }
            is GrammarResult.Verb -> {
                val k = curr.conjugation.infinitiv.trim().lowercase(java.util.Locale.GERMAN)
                if (seenVerbs.add(k)) {
                    verbs.add(curr.conjugation)
                }
            }
            null -> {
                _uiState.value.currentResult?.let { res ->
                    val k = res.word.trim().lowercase(java.util.Locale.GERMAN)
                    if (seenNouns.add(k)) {
                        nouns.add(res)
                    }
                }
            }
        }

        return Pair(nouns, verbs)
    }

    fun onClearResult() {
        _uiState.value = _uiState.value.copy(
            currentResult = null,
            currentGrammarResult = null,
            searchQuery = "",
            errorMessage = null
        )
    }

    fun setGrammarResultForTest(result: GrammarResult?) {
        _uiState.value = _uiState.value.copy(
            currentGrammarResult = result,
            currentResult = (result as? GrammarResult.Noun)?.declension
        )
    }

    fun onDismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Pronounces the given German word or phrase using TextToSpeech.
     */
    fun speakGerman(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun onSelectHistoryFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            repository.onFolderSelected(uri)
            val newName = repository.getHistoryFolderName()
            _uiState.value = _uiState.value.copy(
                isFolderSelectionRequired = false,
                selectedHistoryFolderName = newName,
                importStatusMessage = "Speicherort festgelegt: $newName"
            )
        }
    }

    fun setReplaceHistoryOnImport(replace: Boolean) {
        _uiState.value = _uiState.value.copy(replaceHistoryOnImport = replace)
    }

    fun onImportHistoryFile(fileUri: android.net.Uri) {
        viewModelScope.launch {
            val replace = _uiState.value.replaceHistoryOnImport
            val count = repository.importHistoryFromFile(fileUri, replace)
            val modeText = if (replace) "Ersetzt" else "Zusammengeführt"
            val message = if (count > 0) {
                "$count Einträge erfolgreich importiert ($modeText)"
            } else {
                "Keine gültigen Einträge in der Datei gefunden"
            }
            _uiState.value = _uiState.value.copy(importStatusMessage = message)
        }
    }

    fun clearImportStatusMessage() {
        _uiState.value = _uiState.value.copy(importStatusMessage = null)
    }

    // ==============================================================
    // Notebooks (Notizbücher) Management
    // ==============================================================

    fun selectNotebook(notebookId: String) {
        viewModelScope.launch {
            notebookRepository.setActiveNotebookId(notebookId)
            val updatedAll = notebookRepository.listAllNotebooks()
            val newActive = notebookRepository.getActiveNotebook()
            val settings = newActive.settings

            // Hydrate Room with the selected notebook's history
            val historyItems = notebookRepository.loadNotebookHistory(notebookId)
            repository.reloadRoomWithItems(historyItems)

            // Update TTS speed rate
            ttsManager.setSpeedRate(settings.ttsSpeed)

            // Update theme
            val theme = AppThemePackage.fromId(settings.themeColorId)
            themePreferences.saveSelectedTheme(theme)
            themePreferences.saveThemeMode(settings.themeMode)
            val globalLanguage = themePreferences.getAppLanguage()
            val effectiveLanguage = if (globalLanguage.isNotBlank()) globalLanguage else settings.appLanguage.ifBlank { "de" }
            themePreferences.saveAppLanguage(effectiveLanguage)

            // Sync API key to repository
            if (settings.customApiKey.isNotBlank()) {
                repository.saveGeminiApiKey(settings.customApiKey)
            }
            if (settings.openRouterApiKey.isNotBlank()) {
                repository.saveOpenRouterApiKey(settings.openRouterApiKey)
            }

            _uiState.value = _uiState.value.copy(
                allNotebooks = updatedAll,
                activeNotebook = newActive,
                selectedTheme = theme,
                themeMode = settings.themeMode,
                appLanguage = effectiveLanguage,
                selectedProvider = ModelProvider.fromId(settings.selectedProvider),
                selectedModel = settings.selectedModel,
                apiKeyInput = settings.customApiKey,
                geminiApiKeyInput = settings.customApiKey,
                openRouterApiKeyInput = settings.openRouterApiKey,
                otherApiKeyInput = settings.openRouterApiKey,
                currentResult = null,
                currentGrammarResult = null,
                searchQuery = "",
                selectedHistoryFolderName = if (settings.storageFolderUri.isNotBlank()) settings.storageFolderUri else "Standard (${newActive.name})"
            )
        }
    }

    fun createNotebook(name: String, themeColorId: String = "Schiefer", themeMode: String = "auto") {
        viewModelScope.launch {
            val notebook = notebookRepository.createNotebook(name, themeColorId, themeMode)
            selectNotebook(notebook.id)
            _uiState.value = _uiState.value.copy(isCreateNotebookDialogOpen = false)
        }
    }

    fun renameNotebook(notebookId: String, newName: String) {
        viewModelScope.launch {
            notebookRepository.renameNotebook(notebookId, newName)
            val updatedAll = notebookRepository.listAllNotebooks()
            val active = notebookRepository.getActiveNotebook()
            _uiState.value = _uiState.value.copy(
                allNotebooks = updatedAll,
                activeNotebook = active
            )
        }
    }

    fun deleteNotebook(notebookId: String) {
        viewModelScope.launch {
            val success = notebookRepository.deleteNotebook(notebookId)
            if (success) {
                val active = notebookRepository.getActiveNotebook()
                selectNotebook(active.id)
            }
        }
    }

    // =========================================================================
    // Direct JSON Export & Import Logic
    // =========================================================================

    fun prepareSingleNotebookExport(notebookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val notebook = notebookRepository.listAllNotebooks().find { it.id == notebookId } ?: return@launch
            val json = notebookRepository.exportNotebookToJsonString(notebookId)
            val fileName = "${notebook.name}.json"
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = fileName,
                        jsonContent = json,
                        title = "Notizbuch exportieren: ${notebook.name}"
                    )
                )
            }
        }
    }

    fun prepareAllNotebooksCombinedExport() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = notebookRepository.exportNotebooksCombinedJsonString()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = "Alle Notizbücher.json",
                        jsonContent = json,
                        title = "Alle Notizbücher exportieren"
                    )
                )
            }
        }
    }

    fun prepareSelectedNotebooksCombinedExport(selectedIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = notebookRepository.exportNotebooksCombinedJsonString(selectedIds)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    isSelectNotebooksExportDialogOpen = false,
                    activeJsonExportTarget = JsonExportTarget(
                        fileName = "Ausgewählte Notizbücher.json",
                        jsonContent = json,
                        title = "Ausgewählte Notizbücher exportieren"
                    )
                )
            }
        }
    }

    fun openSelectNotebooksExportDialog() {
        _uiState.value = _uiState.value.copy(isSelectNotebooksExportDialogOpen = true)
    }

    fun closeSelectNotebooksExportDialog() {
        _uiState.value = _uiState.value.copy(isSelectNotebooksExportDialogOpen = false)
    }

    fun dismissJsonExportDialog() {
        _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
    }

    fun executeSaveJsonToUri(uri: Uri, context: Context) {
        val target = _uiState.value.activeJsonExportTarget ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val success = ExportSharingManager.saveJsonToUri(context, uri, target.jsonContent)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        importStatusMessage = "Datei '${target.fileName}' erfolgreich gespeichert!"
                    )
                }
            }
        }
    }

    fun shareActiveJsonExport(context: Context) {
        val target = _uiState.value.activeJsonExportTarget ?: return
        ExportSharingManager.shareJsonContent(
            context = context,
            fileName = target.fileName,
            jsonContent = target.jsonContent,
            title = target.title
        )
        _uiState.value = _uiState.value.copy(activeJsonExportTarget = null)
    }

    fun setPendingTreeExport(notebookIds: List<String>?) {
        _uiState.value = _uiState.value.copy(pendingTreeExportNotebookIds = notebookIds)
    }

    fun executeTreeExport(folderUri: Uri, context: Context, notebookIds: List<String> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = notebookRepository.exportNotebooksToTreeUri(context, folderUri, notebookIds)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    pendingTreeExportNotebookIds = null,
                    isSelectNotebooksExportDialogOpen = false,
                    importStatusMessage = "$count Notizbücher als separate JSON-Dateien exportiert!"
                )
            }
        }
    }

    fun handleImportJsonUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = getFileNameFromUri(uri, context) ?: "Importiertes_Notizbuch.json"
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (content.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Die ausgewählte Datei ist leer oder ungültig."
                        )
                    }
                    return@launch
                }

                when (val result = notebookRepository.parseImportedJson(content, fileName)) {
                    is ImportedJsonResult.SingleNotebook -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                importSingleChoice = ImportSingleChoice(
                                    detectedName = result.detectedName,
                                    words = result.words,
                                    settings = result.settings
                                )
                            )
                        }
                    }
                    is ImportedJsonResult.MultipleNotebooks -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                importMultiChoice = ImportMultiChoice(
                                    packages = result.packages,
                                    fallbackName = fileName.removeSuffix(".json").removeSuffix(".JSON")
                                )
                            )
                        }
                    }
                    is ImportedJsonResult.EmptyOrInvalid -> {
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Keine gültigen Wörter in der JSON-Datei gefunden."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error reading imported JSON", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Fehler beim Lesen der Datei: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissImportSingleChoice() {
        _uiState.value = _uiState.value.copy(importSingleChoice = null)
    }

    fun dismissImportMultiChoice() {
        _uiState.value = _uiState.value.copy(importMultiChoice = null)
    }

    fun dismissPostImportApiKeyPrompt() {
        _uiState.value = _uiState.value.copy(postImportApiKeyPrompt = null)
    }

    fun savePostImportApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        if (trimmed.isNotBlank()) {
            apiKeyManager.saveApiKey(trimmed)
            val currentSettings = _uiState.value.activeNotebook?.settings
            if (currentSettings != null) {
                val updated = currentSettings.copy(customApiKey = trimmed)
                saveActiveNotebookSettings(updated)
            }
        }
        _uiState.value = _uiState.value.copy(
            postImportApiKeyPrompt = null,
            hasApiKey = apiKeyManager.isKeyConfigured()
        )
    }

    fun openSettingsFromPostImport() {
        _uiState.value = _uiState.value.copy(
            postImportApiKeyPrompt = null,
            isApiKeyDialogOpen = true
        )
    }

    fun executeImportSingleMerge() {
        val choice = _uiState.value.importSingleChoice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val merged = notebookRepository.importSingleNotebookMergeActive(choice.words)
            repository.reloadRoomWithItems(merged)
            val updatedAll = notebookRepository.listAllNotebooks()
            val active = notebookRepository.getActiveNotebook()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    importSingleChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = active,
                    importStatusMessage = "${choice.words.size} Wörter in '${active.name}' zusammengeführt!"
                )
            }
        }
    }

    fun executeImportSingleAsNew(notebookName: String) {
        val choice = _uiState.value.importSingleChoice ?: return
        val nameToUse = notebookName.trim().ifBlank { choice.detectedName }
        viewModelScope.launch(Dispatchers.IO) {
            val newNotebook = notebookRepository.importSingleNotebookAsNew(
                name = nameToUse,
                words = choice.words,
                importedSettings = choice.settings
            )
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()

            // Apply theme/language from imported settings if present
            if (choice.settings != null) {
                themePreferences.saveSelectedTheme(AppThemePackage.fromId(choice.settings.themeColorId))
                themePreferences.saveThemeMode(choice.settings.themeMode)
                themePreferences.saveAppLanguage(choice.settings.appLanguage)
            }

            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || choice.settings != null) {
                    PostImportApiKeyPrompt(
                        notebookName = newNotebook.name,
                        provider = newNotebook.settings.selectedProvider,
                        modelId = newNotebook.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importSingleChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = newNotebook,
                    selectedTheme = AppThemePackage.fromId(newNotebook.settings.themeColorId),
                    themeMode = newNotebook.settings.themeMode,
                    appLanguage = newNotebook.settings.appLanguage,
                    importStatusMessage = "Notizbuch '${newNotebook.name}' mit ${choice.words.size} Wörtern erstellt!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    fun executeImportMultiSeparate() {
        val choice = _uiState.value.importMultiChoice ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val importedList = notebookRepository.importMultipleNotebooksSeparate(choice.packages)
            val active = notebookRepository.getActiveNotebook()
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()
            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || importedList.any { it.settings.selectedProvider.isNotBlank() }) {
                    PostImportApiKeyPrompt(
                        notebookName = active.name,
                        provider = active.settings.selectedProvider,
                        modelId = active.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importMultiChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = active,
                    selectedTheme = AppThemePackage.fromId(active.settings.themeColorId),
                    themeMode = active.settings.themeMode,
                    appLanguage = active.settings.appLanguage,
                    importStatusMessage = "${importedList.size} Notizbücher erfolgreich importiert!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    fun executeImportMultiAsSingle(notebookName: String) {
        val choice = _uiState.value.importMultiChoice ?: return
        val nameToUse = notebookName.trim().ifBlank { choice.fallbackName }
        viewModelScope.launch(Dispatchers.IO) {
            val allWords = choice.packages.flatMap { it.words }
            val firstSettings = choice.packages.firstOrNull { it.settings != null }?.settings
            val newNotebook = notebookRepository.importMultipleNotebooksAsSingle(
                name = nameToUse,
                allWords = allWords,
                firstSettings = firstSettings
            )
            val activeHistory = notebookRepository.loadActiveHistory()
            repository.reloadRoomWithItems(activeHistory)
            val updatedAll = notebookRepository.listAllNotebooks()

            if (firstSettings != null) {
                themePreferences.saveSelectedTheme(AppThemePackage.fromId(firstSettings.themeColorId))
                themePreferences.saveThemeMode(firstSettings.themeMode)
                themePreferences.saveAppLanguage(firstSettings.appLanguage)
            }

            withContext(Dispatchers.Main) {
                val promptKey = if (!apiKeyManager.isKeyConfigured() || firstSettings != null) {
                    PostImportApiKeyPrompt(
                        notebookName = newNotebook.name,
                        provider = newNotebook.settings.selectedProvider,
                        modelId = newNotebook.settings.selectedModel
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    importMultiChoice = null,
                    allNotebooks = updatedAll,
                    activeNotebook = newNotebook,
                    selectedTheme = AppThemePackage.fromId(newNotebook.settings.themeColorId),
                    themeMode = newNotebook.settings.themeMode,
                    appLanguage = newNotebook.settings.appLanguage,
                    importStatusMessage = "Notizbuch '${newNotebook.name}' mit ${allWords.size} Wörtern erstellt!",
                    postImportApiKeyPrompt = promptKey
                )
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        return name ?: uri.lastPathSegment
    }

    fun openCreateNotebookDialog() {
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.value = _uiState.value.copy(
            appLanguage = currentLanguage,
            isCreateNotebookDialogOpen = true
        )
    }

    fun closeCreateNotebookDialog() {
        _uiState.value = _uiState.value.copy(isCreateNotebookDialogOpen = false)
    }

    fun openManageNotebooksDialog() {
        val all = notebookRepository.listAllNotebooks()
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.value = _uiState.value.copy(
            allNotebooks = all,
            appLanguage = currentLanguage,
            isManageNotebooksDialogOpen = true
        )
    }

    fun closeManageNotebooksDialog() {
        _uiState.value = _uiState.value.copy(isManageNotebooksDialogOpen = false)
    }

    fun onOpenNotebookManagerFromSettings() {
        val currentLanguage = themePreferences.getAppLanguage()
        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = false,
            appLanguage = currentLanguage,
            isManageNotebooksDialogOpen = true
        )
    }

    fun onThemeModeSelected(mode: String) {
        themePreferences.saveThemeMode(mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            val updated = currentSettings.copy(themeMode = mode)
            saveActiveNotebookSettings(updated)
        }
    }

    fun onAppLanguageSelected(languageCode: String) {
        val cleanCode = when (languageCode.lowercase().trim()) {
            "en" -> "en"
            "ar" -> "ar"
            else -> "de"
        }
        themePreferences.saveAppLanguage(cleanCode)
        _uiState.value = _uiState.value.copy(appLanguage = cleanCode)
        val currentSettings = _uiState.value.activeNotebook?.settings
        if (currentSettings != null) {
            val updated = currentSettings.copy(appLanguage = cleanCode)
            saveActiveNotebookSettings(updated)
        }
    }

    fun saveActiveNotebookSettings(settings: NotebookSettings) {
        notebookRepository.saveActiveSettings(settings)
        val updatedActive = _uiState.value.activeNotebook?.copy(settings = settings)
        if (updatedActive != null) {
            _uiState.value = _uiState.value.copy(activeNotebook = updatedActive)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
