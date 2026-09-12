package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiModelInfo
import com.example.data.ModelProvider
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnPrimary
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDialog(
    selectedProvider: ModelProvider,
    geminiKey: String,
    openRouterKey: String,
    selectedModel: String,
    availableModels: List<AiModelInfo>,
    selectedTheme: AppThemePackage,
    isTestingKey: Boolean,
    testKeyResult: Pair<Boolean, String>?,
    isRefreshingModels: Boolean = false,
    refreshModelsStatus: String? = null,
    remainingDailyRequests: Int,
    totalDailyQuota: Int,
    savedHistoryCount: Int = 0,
    selectedHistoryFolderName: String = "Kein Ordner ausgewählt",
    replaceHistoryOnImport: Boolean = false,
    importStatusMessage: String? = null,
    onDismiss: () -> Unit,
    onSelectProvider: (ModelProvider) -> Unit,
    onGeminiKeyChange: (String) -> Unit,
    onOpenRouterKeyChange: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onRefreshOpenRouterModels: () -> Unit = {},
    onRefreshGeminiModels: () -> Unit = {},
    onSelectTheme: (AppThemePackage) -> Unit,
    onTestKey: () -> Unit,
    onSaveAll: () -> Unit,
    onClearKey: () -> Unit,
    onOpenExport: () -> Unit = {},
    onClearHistory: () -> Unit = {},
    onSelectHistoryFolder: (Uri) -> Unit = {},
    onImportHistoryFile: (Uri) -> Unit = {},
    onReplaceHistoryOnImportChange: (Boolean) -> Unit = {},
    onDismissImportStatus: () -> Unit = {},
    activeNotebookName: String = "Allgemein",
    onRenameNotebook: ((String) -> Unit)? = null,
    themeMode: String = "auto",
    onThemeModeChange: ((String) -> Unit)? = null,
    onOpenNotebookManager: (() -> Unit)? = null,
    appLanguage: String = "de",
    onAppLanguageSelected: ((String) -> Unit)? = null,
    onLanguageChange: ((String) -> Unit)? = null
) {
    var isKeyVisible by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var modelSearchQuery by remember { mutableStateOf("") }
    var showFreeOnly by remember { mutableStateOf(true) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectHistoryFolder(uri)
        }
    }

    val importFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportHistoryFile(uri)
        }
    }

    // Direction based on selected language
    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 660.dp)
                .testTag("api_key_dialog"),
            shape = RoundedCornerShape(16.dp),
            containerColor = GeoSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.settingsBackDesc,
                            tint = GeoOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = strings.settingsTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ==============================================================
                    // 0. Notizbuch & Design-Modus & Sprache (Workbook & Theme Mode & Language)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Active Notebook row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = GeoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${strings.notebookLabelPrefix} $activeNotebookName",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoOnSurface
                                    )
                                }

                                if (onOpenNotebookManager != null) {
                                    TextButton(
                                        onClick = onOpenNotebookManager,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = strings.manageAllNotebooks,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GeoPrimary
                                        )
                                    }
                                }
                            }

                            // Theme Mode (Auto / Light / Dark)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = strings.themeModeTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoOnSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        Pair("auto", strings.themeModeAuto),
                                        Pair("light", strings.themeModeLight),
                                        Pair("dark", strings.themeModeDark)
                                    ).forEach { (modeKey, modeTitle) ->
                                        val isSelected = when (modeKey) {
                                            "light" -> themeMode.equals("light", ignoreCase = true) || themeMode.equals("hell", ignoreCase = true)
                                            "dark" -> themeMode.equals("dark", ignoreCase = true) || themeMode.equals("dunkel", ignoreCase = true)
                                            else -> themeMode.equals("auto", ignoreCase = true) || themeMode.equals("system", ignoreCase = true) || themeMode.isBlank()
                                        }
                                        OutlinedButton(
                                            onClick = { onThemeModeChange?.invoke(modeKey) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GeoPrimary else GeoBorder
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                                contentColor = if (isSelected) GeoPrimary else GeoOnSurface
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = modeTitle,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // Language Selector (Deutsch / English / العربية)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = strings.languageSectionTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoOnSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppLanguage.entries.forEach { lang ->
                                        val isSelected = lang.code.equals(appLanguage, ignoreCase = true)
                                        OutlinedButton(
                                            onClick = { (onAppLanguageSelected ?: onLanguageChange)?.invoke(lang.code) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .testTag("language_${lang.code}_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GeoPrimary else GeoBorder
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                                contentColor = if (isSelected) GeoPrimary else GeoOnSurface
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = lang.nativeName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 1. KI-Anbieter (Provider Selection: Gemini vs OpenRouter)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.aiProviderTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModelProvider.entries.forEach { provider ->
                                    val isSelected = provider == selectedProvider
                                    OutlinedButton(
                                        onClick = { onSelectProvider(provider) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag(if (provider == selectedProvider) "provider_dropdown_button" else "provider_${provider.id}_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) GeoPrimary else GeoBorder
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) GeoPrimaryContainer.copy(alpha = 0.5f) else GeoSurface,
                                            contentColor = if (isSelected) GeoPrimary else GeoOnSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (provider == ModelProvider.GEMINI) Icons.Default.AutoAwesome else Icons.Default.Hub,
                                                contentDescription = null,
                                                tint = if (isSelected) GeoPrimary else GeoOnSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = provider.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 2. API-Key Input Container (Reduced Size)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Label + Hint
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (selectedProvider == ModelProvider.OPENROUTER) strings.openRouterKeyLabel else strings.geminiKeyLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = if (selectedProvider == ModelProvider.OPENROUTER) strings.openRouterKeyHint else strings.geminiKeyHint,
                                    fontSize = 10.sp,
                                    color = GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val currentKey = if (selectedProvider == ModelProvider.GEMINI) geminiKey else openRouterKey
                            val onKeyChange = if (selectedProvider == ModelProvider.GEMINI) onGeminiKeyChange else onOpenRouterKeyChange
                            val inputTestTag = if (selectedProvider == ModelProvider.GEMINI) "gemini_key_input" else "openrouter_key_input"

                            // Single line compact input field
                            OutlinedTextField(
                                value = currentKey,
                                onValueChange = onKeyChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(inputTestTag),
                                placeholder = {
                                    Text(
                                        text = if (selectedProvider == ModelProvider.GEMINI) "AIzaSy..." else "sk-or-...",
                                        fontSize = 12.sp,
                                        color = GeoOnSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { onSaveAll() }),
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { isKeyVisible = !isKeyVisible },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isKeyVisible) "Hide" else "Show",
                                                tint = GeoOnSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (currentKey.isNotEmpty()) {
                                            IconButton(
                                                onClick = onClearKey,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = GeoOnSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeoPrimary,
                                    unfocusedBorderColor = GeoBorder,
                                    focusedContainerColor = GeoSurface,
                                    unfocusedContainerColor = GeoSurface
                                )
                            )

                            // Test Key Button
                            val testButtonTag = if (selectedProvider == ModelProvider.GEMINI) "test_gemini_key_button" else "test_openrouter_key_button"
                            OutlinedButton(
                                onClick = onTestKey,
                                enabled = !isTestingKey && currentKey.trim().isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag(testButtonTag),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GeoPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = GeoSurface,
                                    contentColor = GeoPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                if (isTestingKey) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = GeoPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = strings.testingKeyStatus, fontSize = 11.sp)
                                } else {
                                    Text(text = strings.testKeyButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Test Key Result Banner
                            AnimatedVisibility(visible = testKeyResult != null) {
                                testKeyResult?.let { (isSuccess, message) ->
                                    val bgColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    val textColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = message,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 3. Aktives Modell (Plain text ID beneath label, no heavy frame)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val activeModelInfo = availableModels.find { it.id == selectedModel }
                            val effectiveId = if (selectedModel.isNotBlank()) {
                                selectedModel
                            } else if (availableModels.isNotEmpty()) {
                                availableModels.first().id
                            } else {
                                if (selectedProvider == ModelProvider.OPENROUTER) "openrouter/free" else strings.noModelsFound
                            }
                            val isFreeRouter = selectedProvider == ModelProvider.OPENROUTER &&
                                    (effectiveId == "openrouter/free" || activeModelInfo?.tier?.contains("Free", ignoreCase = true) == true)

                            // Header row with Label, Provider hint, and action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = strings.activeModelTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoOnSurface
                                    )
                                    Text(
                                        text = if (isFreeRouter) "(Free Router)" else "(${selectedProvider.displayName})",
                                        fontSize = 10.sp,
                                        color = GeoOnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val onRefresh = if (selectedProvider == ModelProvider.GEMINI) onRefreshGeminiModels else onRefreshOpenRouterModels
                                    IconButton(
                                        onClick = onRefresh,
                                        enabled = !isRefreshingModels,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("refresh_models_button")
                                    ) {
                                        if (isRefreshingModels) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 2.dp,
                                                color = GeoPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = strings.refreshModelsButton,
                                                tint = GeoPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Button to open model picker
                                    OutlinedButton(
                                        onClick = { modelDropdownExpanded = true },
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("model_dropdown_button"),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, GeoBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = GeoSurface,
                                            contentColor = GeoPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text(text = "${strings.selectModelButton} ▼", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Active Model ID displayed strictly as plain text beneath label (no frame)
                            Text(
                                text = effectiveId,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (activeModelInfo != null && activeModelInfo.functionalDescription.isNotBlank()) {
                                Text(
                                    text = activeModelInfo.functionalDescription,
                                    fontSize = 11.sp,
                                    color = GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Status message if models refreshed
                            refreshModelsStatus?.let { status ->
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // DropdownMenu for model selection
                            DropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = {
                                    modelDropdownExpanded = false
                                    modelSearchQuery = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.90f)
                                    .heightIn(max = 360.dp)
                                    .background(GeoSurface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    OutlinedTextField(
                                        value = modelSearchQuery,
                                        onValueChange = { modelSearchQuery = it },
                                        placeholder = { Text(strings.searchPlaceholder, fontSize = 11.sp) },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = GeoSurfaceVariant,
                                            unfocusedContainerColor = GeoSurfaceVariant
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = showFreeOnly,
                                            onClick = { showFreeOnly = !showFreeOnly },
                                            label = {
                                                Text(
                                                    if (selectedProvider == ModelProvider.GEMINI) "Nur Flash" else strings.filterFreeOnly,
                                                    fontSize = 10.sp
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(12.dp))
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GeoPrimaryContainer,
                                                selectedLabelColor = GeoPrimary
                                            )
                                        )
                                        Text(
                                            text = "${availableModels.size} ${strings.words}",
                                            fontSize = 10.sp,
                                            color = GeoOnSurfaceVariant
                                        )
                                    }
                                }

                                val filteredModels = availableModels.filter { model ->
                                    val matchesSearch = modelSearchQuery.isBlank() ||
                                            model.name.contains(modelSearchQuery, ignoreCase = true) ||
                                            model.id.contains(modelSearchQuery, ignoreCase = true)
                                    val matchesFree = when {
                                        selectedProvider == ModelProvider.OPENROUTER && showFreeOnly -> model.isFreeTier
                                        selectedProvider == ModelProvider.GEMINI && showFreeOnly -> model.id.contains("flash", ignoreCase = true)
                                        else -> true
                                    }
                                    matchesSearch && matchesFree
                                }

                                if (availableModels.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (selectedProvider == ModelProvider.GEMINI)
                                                    strings.loadingModelsStatus
                                                else
                                                    strings.noModelsFound,
                                                fontSize = 11.sp,
                                                color = GeoOnSurfaceVariant
                                            )
                                        },
                                        onClick = {}
                                    )
                                } else if (filteredModels.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text(strings.noModelsFound, fontSize = 11.sp, color = GeoOnSurfaceVariant) },
                                        onClick = {}
                                    )
                                } else {
                                    filteredModels.forEach { model ->
                                        val isSelected = model.id == selectedModel
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = model.name,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                fontSize = 12.sp,
                                                                color = GeoOnSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (model.isFreeTier) {
                                                                FreeBadge()
                                                            }
                                                        }
                                                        Text(
                                                            text = model.id,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = GeoOnSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (model.functionalDescription.isNotBlank()) {
                                                            Text(
                                                                text = model.functionalDescription,
                                                                fontSize = 11.sp,
                                                                color = GeoOnSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.padding(top = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = GeoPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                onSelectModel(model.id)
                                                modelDropdownExpanded = false
                                                modelSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }

                            // --------------------------------------------------
                            // Usage Display: Single line without extra explanation
                            // --------------------------------------------------
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.remainingDailyRequests(remainingDailyRequests, totalDailyQuota),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoOnSurfaceVariant
                                )
                            }
                        }
                    }

                    // ==============================================================
                    // 4. Farbschema (Theme Selection)
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.colorSchemeTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSurface
                            )

                            // 0. Material 3 Haupt-Farben (6 Separate Themes)
                            Text(
                                text = strings.themeMainColors,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.M3_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 0B. 10 Neue Kräftige & Expressive Farben (Nicht Material 3)
                            Text(
                                text = strings.themeBoldColors,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.BOLD_EXPRESSIVE_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 1. Rotbraun & Terracotta (Warm Earth)
                            Text(
                                text = strings.themeWarmEarth,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.WARM_EARTH_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 2. Sand & Beige (Neutral Warm)
                            Text(
                                text = strings.themeSlateSand,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.NEUTRAL_WARM_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 3. Grau & Schiefer (Cool Earth)
                            Text(
                                text = strings.themeMossSage,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.COOL_EARTH_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 4. Braun & Umbra (Deep Earth)
                            Text(
                                text = strings.themeOceanPetrol,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.DEEP_EARTH_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = GeoBorder.copy(alpha = 0.6f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            // 5. Gedämpfte Töne (Lehm & Stein)
                            Text(
                                text = strings.themePlumLavender,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppThemePackage.MUTED_HERBAL_THEMES.chunked(3).forEach { rowThemes ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowThemes.size) {
                                                val theme = rowThemes[i]
                                                val isSelected = theme == selectedTheme
                                                ThemeColorItem(
                                                    theme = theme,
                                                    isSelected = isSelected,
                                                    onSelect = { onSelectTheme(theme) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 5. Speicherort für Verlauf & Import
                    // ==============================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.historyStorageTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface
                                )
                            }

                            // Current Folder display & change button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeoSurface)
                                    .border(1.dp, GeoBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.currentFolderLabel,
                                        fontSize = 10.sp,
                                        color = GeoOnSurfaceVariant
                                    )
                                    Text(
                                        text = if (selectedHistoryFolderName.isNotBlank()) selectedHistoryFolderName else strings.noFolderSelected,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { folderPickerLauncher.launch(null) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("change_history_folder_button")
                                ) {
                                    Text(
                                        text = if (selectedHistoryFolderName.isBlank() || selectedHistoryFolderName == "Kein Ordner ausgewählt") strings.changeFolderButton else strings.changeFolderButton,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Import: Single button
                            Button(
                                onClick = {
                                    importFilePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeoPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("import_history_button")
                            ) {
                                Text(
                                    text = "📂 ${strings.importHistoryButton}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (!importStatusMessage.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeoPrimaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = importStatusMessage,
                                        fontSize = 11.sp,
                                        color = GeoPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onDismissImportStatus,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = strings.close,
                                            tint = GeoPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            // Fixed bottom buttons: Speichern | Schließen
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("close_api_key_dialog"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GeoBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnSurface)
                    ) {
                        Text(
                            text = strings.close,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onSaveAll,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                            .testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = GeoOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = strings.save,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun FreeBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF2E7D32))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "Free",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ThemeColorItem(
    theme: AppThemePackage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GeoPrimaryContainer.copy(alpha = 0.45f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) GeoPrimary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(theme.swatch)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) GeoPrimary else GeoOutline.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                val iconTint = if (theme == AppThemePackage.WEISS || theme == AppThemePackage.HELLGRAU || theme == AppThemePackage.GELB || theme == AppThemePackage.CREME || theme == AppThemePackage.NEON_GELB || theme == AppThemePackage.GIFTGRUEN) {
                    Color(0xFF1E1A16)
                } else {
                    Color.White
                }
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = theme.shortName,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) GeoPrimary else GeoOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

