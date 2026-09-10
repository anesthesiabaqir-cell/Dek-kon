package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WordDeclensionResult
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.util.DeclensionHistoryExportHelper
import com.example.util.ExportSharingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Modern, compact export dialog:
 * - Header: [<-] Exportieren [X]
 * - Scope selector: [ Nomen ] [ Verb ] [ Alle ]
 * - Format selector: [ Excel (.xlsx) ] [ PDF ]
 * - Paper size & orientation selector (when PDF selected): [A4 v] [ Hochformat / Querformat ]
 * - Advanced Print Customization:
 *   - Body Text: Font type (10 fonts), Font size (8-72pt + custom), Color (20 colors + hex)
 *   - Headers: Font type (10 fonts), Font size (8-72pt + custom), Color (20 colors + hex)
 * - Single-line file name input field pre-filled with "Suchverlauf_Deutsch"
 * - Two equal-weight action buttons side-by-side: [Speichern] & [Teilen]
 * - Strictly German/English only (no Arabic text, no text wrapping)
 */
@Composable
fun PrintExportDialog(
    currentWord: WordDeclensionResult? = null,
    historyWords: List<WordDeclensionResult> = emptyList(),
    currentGrammarResult: com.example.data.model.GrammarResult? = null,
    nounHistory: List<WordDeclensionResult> = emptyList(),
    verbHistory: List<com.example.data.model.VerbConjugationResult> = emptyList(),
    initialScope: DeclensionHistoryExportHelper.ExportScope = DeclensionHistoryExportHelper.ExportScope.ALLE,
    notebookName: String = "",
    registerName: String = notebookName,
    onDismiss: () -> Unit
) {
    val effectiveNotebookName = notebookName.ifBlank { registerName }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedScope by remember { mutableStateOf(initialScope) }
    var selectedFormat by remember { mutableStateOf(ExportSharingManager.ExportFormat.XLSX) }
    val defaultBaseName = remember(effectiveNotebookName) {
        val cleanNb = ExportSharingManager.sanitizeFileName(effectiveNotebookName, defaultName = "")
        if (cleanNb.isNotBlank()) "Suchverlauf_$cleanNb" else "Suchverlauf_Deutsch"
    }
    var fileNameInput by remember(defaultBaseName) { mutableStateOf(defaultBaseName) }

    // Paper size selection for PDF (A3, A4, A5, Letter) and orientation (Landscape/Portrait)
    var selectedBaseSize by remember { mutableStateOf("A4") }
    var isLandscape by remember { mutableStateOf(false) }

    val selectedPaperSize = remember(selectedBaseSize, isLandscape) {
        when (selectedBaseSize) {
            "A3" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A3_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A3_PORTRAIT
            "A5" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A5_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A5_PORTRAIT
            "Letter" -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.LETTER_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.LETTER_PORTRAIT
            else -> if (isLandscape) DeclensionHistoryExportHelper.PaperSizeOption.A4_LANDSCAPE else DeclensionHistoryExportHelper.PaperSizeOption.A4_PORTRAIT
        }
    }

    // Typography customization: Body Text
    var bodyFontFamily by remember { mutableStateOf("Arial") }
    var bodyFontSize by remember { mutableStateOf(12f) }
    var bodyFontColorHex by remember { mutableStateOf("#000000") }

    // Typography customization: Headers
    var headerFontFamily by remember { mutableStateOf("Arial") }
    var headerFontSize by remember { mutableStateOf(14f) }
    var headerFontColorHex by remember { mutableStateOf("#000000") }

    // Dialogs for color picker & custom size
    var activeColorTarget by remember { mutableStateOf<String?>(null) } // "body" or "header"
    var activeCustomSizeTarget by remember { mutableStateOf<String?>(null) } // "body" or "header"

    val currentTypography = remember(
        bodyFontSize, bodyFontFamily, bodyFontColorHex,
        headerFontSize, headerFontFamily, headerFontColorHex
    ) {
        DeclensionHistoryExportHelper.ExportTypographyOptions(
            bodyFontSize = bodyFontSize,
            bodyFontFamily = bodyFontFamily,
            bodyFontColorHex = bodyFontColorHex,
            headerFontSize = headerFontSize,
            headerFontFamily = headerFontFamily,
            headerFontColorHex = headerFontColorHex
        )
    }

    // Resolve Nouns to export
    val effectiveNouns = remember(nounHistory, historyWords, currentWord, currentGrammarResult) {
        if (nounHistory.isNotEmpty()) {
            nounHistory
        } else if (historyWords.isNotEmpty()) {
            historyWords
        } else if (currentGrammarResult is com.example.data.model.GrammarResult.Noun) {
            listOf(currentGrammarResult.declension)
        } else if (currentWord != null) {
            listOf(currentWord)
        } else {
            emptyList()
        }
    }

    // Resolve Verbs to export
    val effectiveVerbs = remember(verbHistory, currentGrammarResult) {
        if (verbHistory.isNotEmpty()) {
            verbHistory
        } else if (currentGrammarResult is com.example.data.model.GrammarResult.Verb) {
            listOf(currentGrammarResult.conjugation)
        } else {
            emptyList()
        }
    }

    // Launcher for creating document at user's chosen folder location
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            ExportSharingManager.writeUnifiedToStream(
                                context = context,
                                nouns = effectiveNouns,
                                verbs = effectiveVerbs,
                                scope = selectedScope,
                                format = selectedFormat,
                                outputStream = os,
                                customBaseName = ExportSharingManager.sanitizeFileName(fileNameInput),
                                paperSize = selectedPaperSize,
                                typography = currentTypography,
                                notebookName = effectiveNotebookName
                            )
                        }
                    }
                    Toast.makeText(context, "Datei erfolgreich gespeichert!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .heightIn(max = 700.dp)
                .testTag("export_dialog_surface"),
            shape = RoundedCornerShape(16.dp),
            color = GeoSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Back button + Title + [X] Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("export_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zurück",
                                tint = GeoOnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Exportieren",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("dismiss_export_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = GeoOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scope Selector: [ Nomen ] [ Verb ] [ Alle ]
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Verlauf:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSurfaceVariant)
                            .padding(2.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            DeclensionHistoryExportHelper.ExportScope.NOMEN to "Nomen",
                            DeclensionHistoryExportHelper.ExportScope.VERB to "Verb",
                            DeclensionHistoryExportHelper.ExportScope.ALLE to "Alle"
                        ).forEach { (scopeItem, label) ->
                            val isSelected = selectedScope == scopeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) GeoSurface else Color.Transparent)
                                    .clickable { selectedScope = scopeItem }
                                    .testTag("scope_tab_${scopeItem.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) GeoOnSurface else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Format Selector (Excel vs PDF)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Format:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSurfaceVariant)
                            .padding(2.5.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Excel Option
                        val isExcel = selectedFormat == ExportSharingManager.ExportFormat.XLSX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isExcel) GeoSurface else Color.Transparent)
                                .clickable { selectedFormat = ExportSharingManager.ExportFormat.XLSX }
                                .padding(horizontal = 6.dp)
                                .testTag("format_excel_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = if (isExcel) Color(0xFF107C41) else GeoOnSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Excel (.xlsx)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isExcel) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isExcel) GeoOnSurface else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // PDF Option
                        val isPdf = selectedFormat == ExportSharingManager.ExportFormat.PDF
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPdf) GeoSurface else Color.Transparent)
                                .clickable { selectedFormat = ExportSharingManager.ExportFormat.PDF }
                                .padding(horizontal = 6.dp)
                                .testTag("format_pdf_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isPdf) Color(0xFFB3261E) else GeoOnSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "PDF",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isPdf) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isPdf) GeoOnSurface else GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Paper Size & Orientation Selector (Visible for PDF format)
                if (selectedFormat == ExportSharingManager.ExportFormat.PDF) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Papiergröße & Ausrichtung:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Paper size dropdown selector
                            var paperDropdownExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(0.9f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeoSurfaceVariant)
                                        .clickable { paperDropdownExpanded = true }
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedBaseSize,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = GeoOnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = paperDropdownExpanded,
                                    onDismissRequest = { paperDropdownExpanded = false }
                                ) {
                                    listOf("A4", "A3", "A5", "Letter").forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text(size, fontSize = 12.sp) },
                                            onClick = {
                                                selectedBaseSize = size
                                                paperDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Orientation Segmented Buttons
                            Row(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeoSurfaceVariant)
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(false to "Hochformat", true to "Querformat").forEach { (landscape, label) ->
                                    val isSelected = isLandscape == landscape
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (isSelected) GeoSurface else Color.Transparent)
                                            .clickable { isLandscape = landscape }
                                            .testTag(if (landscape) "paper_landscape" else "paper_portrait"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) GeoOnSurface else GeoOnSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Customization: Body Text
                TypographyConfigCard(
                    title = "📝 Schriftart & -größe (Text)",
                    currentFont = bodyFontFamily,
                    onFontSelected = { bodyFontFamily = it },
                    currentSize = bodyFontSize,
                    onSizeSelected = { bodyFontSize = it },
                    onCustomSizeRequested = { activeCustomSizeTarget = "body" },
                    currentColorHex = bodyFontColorHex,
                    onColorPickerRequested = { activeColorTarget = "body" },
                    testTagPrefix = "body"
                )

                // Customization: Headers
                TypographyConfigCard(
                    title = "📝 Schriftart & -größe (Überschriften)",
                    currentFont = headerFontFamily,
                    onFontSelected = { headerFontFamily = it },
                    currentSize = headerFontSize,
                    onSizeSelected = { headerFontSize = it },
                    onCustomSizeRequested = { activeCustomSizeTarget = "header" },
                    currentColorHex = headerFontColorHex,
                    onColorPickerRequested = { activeColorTarget = "header" },
                    testTagPrefix = "header"
                )

                // File Name Input Section
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Dateiname:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { input -> fileNameInput = input.trimStart('.') },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("file_name_input"),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Suchverlauf_Deutsch",
                                fontSize = 13.sp,
                                color = GeoOnSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = ".${selectedFormat.extension}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoOnSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedContainerColor = GeoSurface,
                            unfocusedContainerColor = GeoSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Buttons Side by Side (Speichern & Teilen) with equal weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            val sanitizedBase = ExportSharingManager.sanitizeFileName(fileNameInput)
                            val targetFileName = ExportSharingManager.buildFileName(sanitizedBase, selectedFormat, effectiveNotebookName)
                            try {
                                saveFileLauncher.launch(targetFileName)
                            } catch (e: Exception) {
                                // Fallback directly to Downloads folder if picker unavailable
                                ExportSharingManager.saveUnifiedToDownloads(
                                    context = context,
                                    nouns = effectiveNouns,
                                    verbs = effectiveVerbs,
                                    scope = selectedScope,
                                    format = selectedFormat,
                                    customBaseName = sanitizedBase,
                                    paperSize = selectedPaperSize,
                                    typography = currentTypography,
                                    notebookName = effectiveNotebookName
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("export_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speichern",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Share Button
                    Button(
                        onClick = {
                            val sanitizedBase = ExportSharingManager.sanitizeFileName(fileNameInput)
                            ExportSharingManager.shareUnifiedSearchHistory(
                                context = context,
                                nouns = effectiveNouns,
                                verbs = effectiveVerbs,
                                scope = selectedScope,
                                format = selectedFormat,
                                customBaseName = sanitizedBase,
                                paperSize = selectedPaperSize,
                                typography = currentTypography,
                                notebookName = effectiveNotebookName
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("export_share_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Teilen",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Color Picker Modal Dialog
    if (activeColorTarget != null) {
        val initialColor = if (activeColorTarget == "body") bodyFontColorHex else headerFontColorHex
        ColorPickerDialog(
            initialColorHex = initialColor,
            onColorChosen = { chosenHex ->
                if (activeColorTarget == "body") {
                    bodyFontColorHex = chosenHex
                } else {
                    headerFontColorHex = chosenHex
                }
                activeColorTarget = null
            },
            onDismiss = { activeColorTarget = null }
        )
    }

    // Custom Font Size Dialog
    if (activeCustomSizeTarget != null) {
        val currentSz = if (activeCustomSizeTarget == "body") bodyFontSize else headerFontSize
        CustomSizeDialog(
            initialSize = currentSz,
            onSizeConfirmed = { newSz ->
                if (activeCustomSizeTarget == "body") {
                    bodyFontSize = newSz
                } else {
                    headerFontSize = newSz
                }
                activeCustomSizeTarget = null
            },
            onDismiss = { activeCustomSizeTarget = null }
        )
    }
}

/**
 * Compact card container displaying font family, font size, and color controls.
 */
@Composable
private fun TypographyConfigCard(
    title: String,
    currentFont: String,
    onFontSelected: (String) -> Unit,
    currentSize: Float,
    onSizeSelected: (Float) -> Unit,
    onCustomSizeRequested: () -> Unit,
    currentColorHex: String,
    onColorPickerRequested: () -> Unit,
    testTagPrefix: String
) {
    var fontDropdownExpanded by remember { mutableStateOf(false) }
    var sizeDropdownExpanded by remember { mutableStateOf(false) }

    val colorInt = remember(currentColorHex) {
        DeclensionHistoryExportHelper.parseHexColor(currentColorHex)
    }
    val colorLabel = remember(currentColorHex) {
        val found = DeclensionHistoryExportHelper.PREDEFINED_COLORS.find {
            it.second.equals(currentColorHex, ignoreCase = true)
        }
        if (found != null) "${found.first} ($currentColorHex)" else currentColorHex
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GeoSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = GeoOnSurface,
            maxLines = 1,
            softWrap = false
        )

        // Row 1: Font Type & Font Size side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font Type Dropdown
            Box(modifier = Modifier.weight(1.4f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeoSurface)
                        .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                        .clickable { fontDropdownExpanded = true }
                        .padding(horizontal = 8.dp)
                        .testTag("${testTagPrefix}_font_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentFont,
                        fontSize = 11.5.sp,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = GeoOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = fontDropdownExpanded,
                    onDismissRequest = { fontDropdownExpanded = false }
                ) {
                    DeclensionHistoryExportHelper.AVAILABLE_FONTS.forEach { font ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = font,
                                    fontSize = 12.sp,
                                    fontWeight = if (font == currentFont) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onFontSelected(font)
                                fontDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Font Size Dropdown
            Box(modifier = Modifier.weight(1f)) {
                val sizeDisplay = if (currentSize % 1.0f == 0.0f) "${currentSize.toInt()} pt" else "$currentSize pt"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeoSurface)
                        .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                        .clickable { sizeDropdownExpanded = true }
                        .padding(horizontal = 8.dp)
                        .testTag("${testTagPrefix}_size_selector"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sizeDisplay,
                        fontSize = 11.5.sp,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = GeoOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = sizeDropdownExpanded,
                    onDismissRequest = { sizeDropdownExpanded = false }
                ) {
                    DeclensionHistoryExportHelper.PREDEFINED_FONT_SIZES.forEach { sz ->
                        val szLabel = if (sz % 1.0f == 0.0f) "${sz.toInt()} pt" else "$sz pt"
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = szLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (sz == currentSize) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSizeSelected(sz)
                                sizeDropdownExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Benutzerdefiniert...", fontSize = 12.sp, color = GeoPrimary)
                            }
                        },
                        onClick = {
                            sizeDropdownExpanded = false
                            onCustomSizeRequested()
                        }
                    )
                }
            }
        }

        // Row 2: Color Picker Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(GeoSurface)
                .border(0.5.dp, GeoBorder, RoundedCornerShape(6.dp))
                .clickable { onColorPickerRequested() }
                .padding(horizontal = 8.dp)
                .testTag("${testTagPrefix}_color_selector"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(colorInt))
                        .border(1.dp, GeoBorder, RoundedCornerShape(4.dp))
                )
                Text(
                    text = colorLabel,
                    fontSize = 11.5.sp,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GeoOnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Color picker dialog offering 20 predefined colors and manual hex input.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerDialog(
    initialColorHex: String,
    onColorChosen: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColorHex.trim()) }
    val parsedColorInt = remember(hexInput) {
        DeclensionHistoryExportHelper.parseHexColor(hexInput)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = GeoSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Farbe auswählen",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnSurface,
                    maxLines = 1
                )

                // 20 predefined color swatches (FlowRow of 5 items per row)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    DeclensionHistoryExportHelper.PREDEFINED_COLORS.forEach { (name, hex) ->
                        val isSelected = hex.equals(hexInput.trim(), ignoreCase = true)
                        val colorVal = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) GeoPrimary else GeoBorder,
                                    shape = CircleShape
                                )
                                .clickable { hexInput = hex }
                                .testTag("color_swatch_$name"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                val checkTint = if (hex == "#FFFFFF" || hex == "#FFFF00" || hex == "#00FFFF") Color.Black else Color.White
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = checkTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Manual Hex Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview Swatch
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(parsedColorInt))
                            .border(1.dp, GeoBorder, RoundedCornerShape(6.dp))
                    )

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { hexInput = it.take(9) },
                        label = { Text("Hex-Code (#RRGGBB)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontSize = 12.sp, color = GeoOnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            val clean = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
                            onColorChosen(clean)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                    ) {
                        Text("Übernehmen", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Dialog for entering custom font size in points (1 to 1638, increments of 0.5).
 */
@Composable
private fun CustomSizeDialog(
    initialSize: Float,
    onSizeConfirmed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sizeInput by remember { mutableStateOf(if (initialSize % 1f == 0f) initialSize.toInt().toString() else initialSize.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = GeoSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Schriftgröße anpassen",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnSurface
                )
                Text(
                    text = "Wert zwischen 1 und 1638 pt (Schritte: 0.5)",
                    fontSize = 11.5.sp,
                    color = GeoOnSurfaceVariant
                )

                OutlinedTextField(
                    value = sizeInput,
                    onValueChange = {
                        sizeInput = it
                        errorText = null
                    },
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it, fontSize = 10.5.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeoPrimary,
                        unfocusedBorderColor = GeoBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontSize = 12.sp, color = GeoOnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            val parsed = sizeInput.replace(',', '.').toFloatOrNull()
                            if (parsed == null || parsed < 1f || parsed > 1638f) {
                                errorText = "Ungültig (1 - 1638 pt)"
                            } else {
                                val rounded = (Math.round(parsed * 2.0) / 2.0).toFloat()
                                onSizeConfirmed(rounded)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                    ) {
                        Text("OK", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

