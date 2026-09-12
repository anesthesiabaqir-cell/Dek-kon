package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GrammarResult
import com.example.data.model.GrammarType
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.ClearHistoryConfirmDialog
import com.example.ui.components.CreateNotebookDialog
import com.example.ui.components.DeclensionResultView
import com.example.ui.components.DeleteHistoryItemConfirmDialog
import com.example.ui.components.FolderSelectionOverlay
import com.example.ui.components.HistorySection
import com.example.ui.components.ImportMultiChoiceDialog
import com.example.ui.components.ImportSingleChoiceDialog
import com.example.ui.components.JsonExportActionDialog
import com.example.ui.components.NotebookManagerDialog
import com.example.ui.components.NotebookSwitcher
import com.example.ui.components.PostImportApiKeyDialog
import com.example.ui.components.PrintExportDialog
import com.example.ui.components.SelectNotebooksExportDialog
import com.example.ui.components.SentenceResultView
import com.example.ui.components.SprechTempoControl
import com.example.ui.components.VerbConjugationResultView
import com.example.ui.components.historySectionItems
import com.example.util.ExportSharingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoGlowColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val ttsSpeed by viewModel.ttsSpeedRate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val activeModelInfo = remember(uiState.selectedModel, uiState.availableModels) {
        uiState.availableModels.find { it.id == uiState.selectedModel }
            ?: uiState.availableModels.firstOrNull()
    }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var historyItemToDeleteId by remember { mutableStateOf<Long?>(null) }
    var historySearchQuery by rememberSaveable { mutableStateOf("") }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.executeSaveJsonToUri(uri, context)
        }
    }

    val treeFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val pendingIds = uiState.pendingTreeExportNotebookIds ?: emptyList()
            viewModel.executeTreeExport(uri, context, pendingIds)
        }
    }

    val filteredHistoryList by produceState(
        initialValue = historyList,
        key1 = historyList,
        key2 = uiState.selectedHistoryFilter,
        key3 = historySearchQuery
    ) {
        withContext(Dispatchers.Default) {
            val typeFiltered = when (uiState.selectedHistoryFilter) {
                1 -> historyList.filter { !it.type.equals("Verb", ignoreCase = true) }
                2 -> historyList.filter { it.type.equals("Verb", ignoreCase = true) }
                else -> historyList
            }
            value = if (historySearchQuery.isBlank()) {
                typeFiltered
            } else {
                val query = historySearchQuery.trim().lowercase(Locale.ROOT)
                typeFiltered.filter { item ->
                    item.word.lowercase(Locale.ROOT).contains(query) ||
                    item.genderArticle.lowercase(Locale.ROOT).contains(query) ||
                    item.meaningEnglish.lowercase(Locale.ROOT).contains(query)
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.onDismissError()
        }
    }

    // Text direction is strictly English / Latin LTR (No Arabic RTL)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BackHandler(enabled = true) {
            if (showClearHistoryDialog) {
                showClearHistoryDialog = false
            } else if (historyItemToDeleteId != null) {
                historyItemToDeleteId = null
            } else {
                val handled = viewModel.handleBackPress()
                if (!handled) {
                    (context as? Activity)?.moveTaskToBack(true)
                }
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = GeoBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        NotebookSwitcher(
                            activeNotebook = uiState.activeNotebook,
                            allNotebooks = uiState.allNotebooks,
                            onSelectNotebook = { viewModel.selectNotebook(it) },
                            onCreateNotebookClick = { viewModel.openCreateNotebookDialog() },
                            onManageNotebooksClick = { viewModel.openManageNotebooksDialog() },
                            appLanguage = uiState.appLanguage
                        )
                    },
                    navigationIcon = {
                        val showBack = uiState.currentGrammarResult != null ||
                                       uiState.currentResult != null ||
                                       uiState.selectedHistoryFilter != 0
                        if (showBack) {
                            IconButton(
                                onClick = { viewModel.handleBackPress() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("toolbar_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Zurück",
                                    tint = GeoOnSurface
                                )
                            }
                        }
                    },
                    actions = {
                        // Model Provider Icon (Clean icon without box, border or background)
                        val providerIcon = when (uiState.selectedProvider) {
                            com.example.data.ModelProvider.GEMINI -> Icons.Default.AutoAwesome
                            com.example.data.ModelProvider.OPENROUTER -> Icons.Default.Hub
                        }
                        IconButton(
                            onClick = { viewModel.onOpenApiKeyDialog() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("active_model_pill")
                        ) {
                            Icon(
                                imageVector = providerIcon,
                                contentDescription = "${uiState.selectedProvider.displayName} - ${activeModelInfo?.name ?: uiState.selectedModel}",
                                modifier = Modifier.size(24.dp),
                                tint = GeoPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Export Button (Icon: 📤 / File Upload, compact ~44-48dp, opens modern export dialog)
                        IconButton(
                            onClick = { viewModel.onOpenExportDialog() },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Exportieren",
                                tint = GeoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeoBackground
                    )
                )
            }
        ) { innerPadding ->
            val lazyListState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = lazyListState
            ) {
                item(key = "top_spacer") {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Warning Banner if API Key is not configured (in English for settings)
                if (!uiState.hasApiKey) {
                    item(key = "api_key_warning_banner") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GeoBorder),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Gemini API Key Required",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "Configure your API key in Settings to analyze German noun declensions.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Button(
                                    onClick = { viewModel.onOpenApiKeyDialog() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("setup_api_key_banner_button")
                                ) {
                                    Text("Settings", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Nomen / Verb Toggle
                item(key = "grammar_type_toggle") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val searchTypes = listOf(
                            GrammarType.NOMEN to "Nomen & Sätze",
                            GrammarType.VERB to "Verb"
                        )
                        searchTypes.forEach { (type, label) ->
                            val isSelected = uiState.selectedGrammarType == type ||
                                (type == GrammarType.NOMEN && uiState.selectedGrammarType == GrammarType.SENTENCE)
                            val textColor = if (isSelected) Color.White else GeoOnSurfaceVariant
                            val icon = if (type == GrammarType.NOMEN) Icons.Default.Description else Icons.Default.Bolt

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (isSelected) {
                                            if (GeoIsBoldTheme) Modifier.background(Brush.horizontalGradient(GeoBannerGradient))
                                            else Modifier.background(GeoPrimary)
                                        } else {
                                            Modifier.background(GeoSurfaceVariant)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) {
                                            if (GeoIsBoldTheme) GeoCardRibbonColor else GeoPrimary
                                        } else GeoBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.onGrammarTypeSelected(type) }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = textColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar Container: match_parent with 16dp side margins
                item(key = "search_bar_container") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Input Field Container (match_parent remaining width, inner left/right padding)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GeoSurfaceVariant)
                                .border(
                                    if (GeoIsBoldTheme) 1.5.dp else 1.dp,
                                    if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(start = 8.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading search icon
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            // Text Field + Placeholder Area
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = if (uiState.selectedGrammarType == GrammarType.VERB) {
                                            "Suche nach Verben"
                                        } else {
                                            "Suche nach Nomen & Sätze"
                                        },
                                        fontSize = 12.sp,
                                        color = GeoOnSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                }

                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onQueryChange(it) },
                                    singleLine = true,
                                    maxLines = 1,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = GeoOnSurface
                                    ),
                                    cursorBrush = SolidColor(GeoPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { viewModel.onSearch() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_input_field")
                                )
                            }

                            // Clear Button if search query is not empty
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onQueryChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Sucheingabe löschen",
                                        tint = GeoOutline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Margin between elements: 8dp between the search field and the search button
                        Spacer(modifier = Modifier.width(8.dp))

                        // Search Submit Button (Changes shape and triggers cancellation when a search is running)
                        Button(
                            onClick = { viewModel.onSearch() },
                            enabled = true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isSearching) MaterialTheme.colorScheme.error else (if (GeoIsBoldTheme) GeoCardRibbonColor else GeoPrimary),
                                contentColor = Color.White
                            ),
                            shape = if (uiState.isSearching) RoundedCornerShape(24.dp) else RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("search_submit_button"),
                            contentPadding = PaddingValues(horizontal = if (uiState.isSearching) 12.dp else 14.dp)
                        ) {
                            if (uiState.isSearching) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Suche abbrechen",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Stoppen",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Suchen",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Sprech-Tempo Speech Rate Control Slider Container
                // Placed below search field & search button, centered horizontally, and directly above Suchverlauf & results
                item(key = "speech_speed_control_container") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SprechTempoControl(
                            speed = ttsSpeed,
                            onSpeedChange = { viewModel.setTtsSpeed(it) }
                        )
                    }
                }

                // Searching Indicator
                if (uiState.isSearching) {
                    item(key = "searching_indicator") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GeoBorder),
                            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = GeoPrimary
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = if (uiState.searchQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size >= 2) {
                                        "Satz wird übersetzt..."
                                    } else if (uiState.selectedGrammarType == GrammarType.VERB) {
                                        "Konjugationstabelle wird analysiert..."
                                    } else {
                                        "Deklinationstabelle wird analysiert..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoOnSurface
                                )
                            }
                        }
                    }
                }

                // Grammar Result View: Noun, Verb, or Sentence
                val hasActiveResult = (uiState.currentGrammarResult != null || uiState.currentResult != null) && !uiState.isSearching
                if (hasActiveResult) {
                    item(key = "active_grammar_result") {
                        when (val grammarRes = uiState.currentGrammarResult) {
                            is GrammarResult.Verb -> {
                                VerbConjugationResultView(
                                    result = grammarRes.conjugation,
                                    onSpeak = { viewModel.speakGerman(it) },
                                    ttsSpeed = ttsSpeed,
                                    onTtsSpeedChange = { viewModel.setTtsSpeed(it) }
                                )
                            }
                            is GrammarResult.Noun -> {
                                DeclensionResultView(
                                    result = grammarRes.declension,
                                    selectedTab = uiState.selectedNumberTab,
                                    onTabSelected = { viewModel.onSelectTab(it) },
                                    onSpeak = { viewModel.speakGerman(it) },
                                    onExport = { viewModel.onOpenExportDialog() },
                                    ttsSpeed = ttsSpeed,
                                    onTtsSpeedChange = { viewModel.setTtsSpeed(it) }
                                )
                            }
                            is GrammarResult.Sentence -> {
                                SentenceResultView(
                                    result = grammarRes.declension,
                                    onSpeak = { viewModel.speakGerman(it) }
                                )
                            }
                            null -> {
                                uiState.currentResult?.let { nounRes ->
                                    val isSentence = nounRes.gender.equals("Satz", ignoreCase = true) ||
                                        nounRes.gender == "–" ||
                                        nounRes.word.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size >= 2
                                    if (isSentence) {
                                        SentenceResultView(
                                            result = nounRes,
                                            onSpeak = { viewModel.speakGerman(it) }
                                        )
                                    } else {
                                        DeclensionResultView(
                                            result = nounRes,
                                            selectedTab = uiState.selectedNumberTab,
                                            onTabSelected = { viewModel.onSelectTab(it) },
                                            onSpeak = { viewModel.speakGerman(it) },
                                            onExport = { viewModel.onOpenExportDialog() },
                                            ttsSpeed = ttsSpeed,
                                            onTtsSpeedChange = { viewModel.setTtsSpeed(it) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "spacer_after_result") {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                if (!uiState.isSearching) {
                    historySectionItems(
                        historyList = historyList,
                        filteredList = filteredHistoryList,
                        selectedFilterIndex = uiState.selectedHistoryFilter,
                        historySearchQuery = historySearchQuery,
                        onSearchQueryChange = { historySearchQuery = it },
                        onFilterChanged = { viewModel.onSelectHistoryFilter(it) },
                        onSelectWord = { viewModel.onSelectHistoryItem(it) },
                        onDeleteItem = { historyItemToDeleteId = it },
                        onRequestClearAll = { showClearHistoryDialog = true },
                        onSpeak = { viewModel.speakGerman(it) }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                viewModel.onClearHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    historyItemToDeleteId?.let { id ->
        DeleteHistoryItemConfirmDialog(
            onConfirm = {
                viewModel.onDeleteHistoryItem(id)
                historyItemToDeleteId = null
            },
            onDismiss = { historyItemToDeleteId = null }
        )
    }

    // Settings & AI Models Dialog
    if (uiState.isApiKeyDialogOpen) {
        ApiKeyDialog(
            selectedProvider = uiState.selectedProvider,
            geminiKey = uiState.geminiApiKeyInput,
            openRouterKey = uiState.openRouterApiKeyInput,
            selectedModel = uiState.selectedModel,
            availableModels = uiState.availableModels,
            selectedTheme = uiState.selectedTheme,
            isTestingKey = uiState.isTestingKey,
            testKeyResult = uiState.testKeyResult,
            isRefreshingModels = uiState.isRefreshingModels,
            refreshModelsStatus = uiState.refreshModelsStatus,
            remainingDailyRequests = uiState.remainingDailyRequests,
            totalDailyQuota = uiState.totalDailyQuota,
            savedHistoryCount = historyList.size,
            selectedHistoryFolderName = uiState.selectedHistoryFolderName,
            replaceHistoryOnImport = uiState.replaceHistoryOnImport,
            importStatusMessage = uiState.importStatusMessage,
            onDismiss = { viewModel.onCloseApiKeyDialog() },
            onSelectProvider = { provider -> viewModel.onSelectProvider(provider) },
            onGeminiKeyChange = { key -> viewModel.onGeminiApiKeyInputChange(key) },
            onOpenRouterKeyChange = { key -> viewModel.onOpenRouterApiKeyInputChange(key) },
            onSelectModel = { modelId -> viewModel.onSelectModel(modelId) },
            onRefreshOpenRouterModels = { viewModel.onRefreshOpenRouterModels() },
            onRefreshGeminiModels = { viewModel.onRefreshGeminiModels() },
            onSelectTheme = { theme -> viewModel.onThemeSelected(theme) },
            onTestKey = { viewModel.onTestApiKey() },
            onSaveAll = { viewModel.onSaveApiKey() },
            onClearKey = { viewModel.onClearApiKey() },
            onOpenExport = { viewModel.onOpenExportDialog(fromSettings = true) },
            onClearHistory = { viewModel.onClearHistory() },
            onSelectHistoryFolder = { uri -> viewModel.onSelectHistoryFolder(uri) },
            onImportHistoryFile = { uri -> viewModel.onImportHistoryFile(uri) },
            onReplaceHistoryOnImportChange = { replace -> viewModel.setReplaceHistoryOnImport(replace) },
            onDismissImportStatus = { viewModel.clearImportStatusMessage() },
            activeNotebookName = uiState.activeNotebook?.name ?: "Allgemein",
            onRenameNotebook = { newName ->
                uiState.activeNotebook?.let { viewModel.renameNotebook(it.id, newName) }
            },
            themeMode = uiState.themeMode,
            onThemeModeChange = { mode -> viewModel.onThemeModeSelected(mode) },
            onOpenNotebookManager = { viewModel.onOpenNotebookManagerFromSettings() },
            appLanguage = uiState.appLanguage,
            onAppLanguageSelected = { lang -> viewModel.onAppLanguageSelected(lang) }
        )
    }

    // Create Notebook Dialog
    if (uiState.isCreateNotebookDialogOpen) {
        CreateNotebookDialog(
            onDismiss = { viewModel.closeCreateNotebookDialog() },
            onCreate = { name, themeColorId, themeMode ->
                viewModel.createNotebook(name, themeColorId, themeMode)
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Manage Notebooks Dialog
    if (uiState.isManageNotebooksDialogOpen) {
        NotebookManagerDialog(
            notebooks = uiState.allNotebooks,
            activeNotebookId = uiState.activeNotebook?.id ?: "",
            onDismiss = { viewModel.closeManageNotebooksDialog() },
            onSelectNotebook = { id ->
                viewModel.selectNotebook(id)
                viewModel.closeManageNotebooksDialog()
            },
            onCreateNotebook = {
                viewModel.closeManageNotebooksDialog()
                viewModel.openCreateNotebookDialog()
            },
            onRenameNotebook = { id, newName ->
                viewModel.renameNotebook(id, newName)
            },
            onDeleteNotebook = { id ->
                viewModel.deleteNotebook(id)
            },
            onExportJson = { id ->
                viewModel.prepareSingleNotebookExport(id)
            },
            onImportJson = { uri ->
                viewModel.handleImportJsonUri(uri, context)
            },
            onExportAllCombined = {
                viewModel.prepareAllNotebooksCombinedExport()
            },
            onExportAllSeparate = {
                viewModel.setPendingTreeExport(null)
                treeFolderLauncher.launch(null)
            },
            onExportSelectedClick = {
                viewModel.openSelectNotebooksExportDialog()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // JSON Action Dialog (Speichern unter / Teilen)
    uiState.activeJsonExportTarget?.let { target ->
        JsonExportActionDialog(
            fileName = target.fileName,
            title = target.title,
            onSaveToFile = {
                saveJsonLauncher.launch(target.fileName)
            },
            onShare = {
                viewModel.shareActiveJsonExport(context)
            },
            onDismiss = {
                viewModel.dismissJsonExportDialog()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Select Notebooks Export Dialog (Option 3)
    if (uiState.isSelectNotebooksExportDialogOpen) {
        SelectNotebooksExportDialog(
            notebooks = uiState.allNotebooks,
            onDismiss = { viewModel.closeSelectNotebooksExportDialog() },
            onExportCombined = { selectedIds ->
                viewModel.closeSelectNotebooksExportDialog()
                viewModel.prepareSelectedNotebooksCombinedExport(selectedIds)
            },
            onExportSeparate = { selectedIds ->
                viewModel.closeSelectNotebooksExportDialog()
                viewModel.setPendingTreeExport(selectedIds)
                treeFolderLauncher.launch(null)
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Single Notebook Import Choice Dialog
    uiState.importSingleChoice?.let { singleChoice ->
        ImportSingleChoiceDialog(
            detectedName = singleChoice.detectedName,
            wordCount = singleChoice.words.size,
            onMergeIntoActive = {
                viewModel.executeImportSingleMerge()
            },
            onCreateAsNew = { newName ->
                viewModel.executeImportSingleAsNew(newName)
            },
            onDismiss = {
                viewModel.dismissImportSingleChoice()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Multi-Notebook Import Choice Dialog
    uiState.importMultiChoice?.let { multiChoice ->
        ImportMultiChoiceDialog(
            notebookNames = multiChoice.notebooksMap.keys.toList(),
            totalWordCount = multiChoice.notebooksMap.values.sumOf { it.size },
            fallbackName = multiChoice.fallbackName,
            onImportAllSeparate = {
                viewModel.executeImportMultiSeparate()
            },
            onImportAsSingle = { name ->
                viewModel.executeImportMultiAsSingle(name)
            },
            onDismiss = {
                viewModel.dismissImportMultiChoice()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Post-Import API Key Configuration Dialog
    uiState.postImportApiKeyPrompt?.let { prompt ->
        PostImportApiKeyDialog(
            notebookName = prompt.notebookName,
            provider = prompt.provider,
            modelId = prompt.modelId,
            onSaveApiKey = { key ->
                viewModel.savePostImportApiKey(key)
            },
            onOpenFullSettings = {
                viewModel.openSettingsFromPostImport()
            },
            onSkip = {
                viewModel.dismissPostImportApiKeyPrompt()
            },
            appLanguage = uiState.appLanguage
        )
    }

    // Print & Export Dialog (Unified PDF / Excel)
    if (uiState.isExportDialogOpen) {
        val (nouns, verbs) = viewModel.getUnifiedHistoryForExport()
        val initialScope = when (uiState.selectedGrammarType) {
            GrammarType.NOMEN -> com.example.util.DeclensionHistoryExportHelper.ExportScope.NOMEN
            GrammarType.VERB -> com.example.util.DeclensionHistoryExportHelper.ExportScope.VERB
            GrammarType.SENTENCE -> com.example.util.DeclensionHistoryExportHelper.ExportScope.NOMEN
        }
        PrintExportDialog(
            currentGrammarResult = uiState.currentGrammarResult,
            nounHistory = nouns,
            verbHistory = verbs,
            initialScope = initialScope,
            notebookName = uiState.activeNotebook?.name ?: "",
            appLanguage = uiState.appLanguage,
            onDismiss = { viewModel.onCloseExportDialog() }
        )
    }

    // Mandatory Folder Selection Overlay (on first install or after clear app data)
    if (uiState.isFolderSelectionRequired) {
        FolderSelectionOverlay(
            onFolderSelected = { uri ->
                viewModel.onSelectHistoryFolder(uri)
            }
        )
    }
}
