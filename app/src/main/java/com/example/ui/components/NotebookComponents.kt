package com.example.ui.components

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.notebook.Notebook
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AppThemePackage
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoOnPrimary
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Top bar notebook switcher dropdown button.
 * Replaces the static title on the main screen.
 */
@Composable
fun NotebookSwitcher(
    activeNotebook: Notebook?,
    allNotebooks: List<Notebook>,
    onSelectNotebook: (String) -> Unit,
    onCreateNotebookClick: () -> Unit,
    onManageNotebooksClick: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: String = "de"
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        Box(modifier = modifier) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = GeoSurfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, GeoBorder),
                modifier = Modifier.testTag("notebook_switcher_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = strings.notebookLabelPrefix,
                        tint = GeoPrimary,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = activeNotebook?.name ?: "Allgemein",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = strings.myNotebooksTitle,
                        tint = GeoOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(GeoSurface)
                    .widthIn(min = 220.dp, max = 300.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
                    Text(
                        text = strings.myNotebooksTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = GeoBorder)

                    allNotebooks.forEach { notebook ->
                        val isActive = notebook.id == activeNotebook?.id
                        val themeColor = remember(notebook.settings.themeColorId) {
                            AppThemePackage.fromId(notebook.settings.themeColorId).primary
                        }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(themeColor)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notebook.name,
                                            fontSize = 14.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive) GeoPrimary else GeoOnSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = strings.wordsCount(notebook.wordCount),
                                            fontSize = 11.sp,
                                            color = GeoOnSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = strings.activeNotebookBadge,
                                            tint = GeoPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                expanded = false
                                onSelectNotebook(notebook.id)
                            }
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = GeoBorder)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.newNotebookButton,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onCreateNotebookClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = GeoOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.manageNotebooksButton,
                                    fontSize = 14.sp,
                                    color = GeoOnSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onManageNotebooksClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dialog to create a new notebook.
 */
@Composable
fun CreateNotebookDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, themeColorId: String, themeMode: String) -> Unit,
    appLanguage: String = "de"
) {
    var notebookName by remember { mutableStateOf("") }
    var selectedThemeId by remember { mutableStateOf("Dunkelblau") }
    var selectedThemeMode by remember { mutableStateOf("auto") }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GeoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = strings.createNotebookTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.createNotebookPrompt,
                        fontSize = 13.sp,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = notebookName,
                        onValueChange = { notebookName = it },
                        label = {
                            Text(
                                text = strings.notebookNameFieldLabel,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_notebook_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedLabelColor = GeoPrimary,
                            cursorColor = GeoPrimary
                        )
                    )

                    // Theme Color Options (The 6 M3 Colors: Gelb, Dunkelblau, Hellgrau, Weiß, Dunkelgrau, Rot)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = strings.notebookColorLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppThemePackage.M3_THEMES.forEach { theme ->
                                val isSelected = theme.id.equals(selectedThemeId, ignoreCase = true)
                                val checkTint = if (theme == AppThemePackage.WEISS || theme == AppThemePackage.HELLGRAU || theme == AppThemePackage.GELB) {
                                    Color(0xFF1E1A16)
                                } else {
                                    Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.swatch)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) GeoPrimary else Color(0x33000000),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedThemeId = theme.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = checkTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Theme Mode Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = strings.themeModeTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("auto", strings.themeModeAuto, "System"),
                                Triple("light", strings.themeModeLight, "Hell"),
                                Triple("dark", strings.themeModeDark, "Dunkel")
                            ).forEach { (modeKey, modeTitle, _) ->
                                val isSelected = when (modeKey) {
                                    "light" -> selectedThemeMode.equals("light", ignoreCase = true) || selectedThemeMode.equals("hell", ignoreCase = true)
                                    "dark" -> selectedThemeMode.equals("dark", ignoreCase = true) || selectedThemeMode.equals("dunkel", ignoreCase = true)
                                    else -> selectedThemeMode.equals("auto", ignoreCase = true) || selectedThemeMode.equals("system", ignoreCase = true) || selectedThemeMode.isBlank()
                                }
                                Surface(
                                    onClick = { selectedThemeMode = modeKey },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) GeoPrimaryContainer else GeoSurfaceVariant,
                                    border = BorderStroke(1.dp, if (isSelected) GeoPrimary else GeoBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = modeTitle,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GeoPrimary else GeoOnSurface,
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = notebookName.trim().ifBlank { "Neues Notizbuch" }
                        onCreate(name, selectedThemeId, selectedThemeMode)
                    },
                    enabled = notebookName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text(
                        text = strings.createButton,
                        color = GeoOnPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(
                        text = strings.cancel,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            containerColor = GeoSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Single-line text that automatically decreases its font size down to [minFontSize]
 * if the text would otherwise overflow its container width, preventing line wrapping or abrupt truncation.
 */
@Composable
fun AutoSizingText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 12.sp,
    minFontSize: TextUnit = 10.sp,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    step: Float = 0.5f
) {
    var scaledFontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text, maxFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        fontSize = scaledFontSize,
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth && scaledFontSize.value > minFontSize.value) {
                val nextVal = (scaledFontSize.value - step).coerceAtLeast(minFontSize.value)
                if (nextVal != scaledFontSize.value) {
                    scaledFontSize = nextVal.sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

/**
 * Notebook Manager Dialog for listing, renaming, deleting, JSON exporting, and JSON importing notebooks.
 * Fully redesigned to be compact, elegant, and adaptive to Material 3 dynamic color roles.
 */
@Composable
fun NotebookManagerDialog(
    notebooks: List<Notebook>,
    activeNotebookId: String,
    onDismiss: () -> Unit,
    onSelectNotebook: (String) -> Unit,
    onCreateNotebook: () -> Unit,
    onRenameNotebook: (id: String, newName: String) -> Unit,
    onDeleteNotebook: (id: String) -> Unit,
    onExportJson: (notebookId: String) -> Unit,
    onImportJson: (uri: Uri) -> Unit,
    onExportAllCombined: () -> Unit,
    onExportAllSeparate: () -> Unit,
    onExportSelectedClick: () -> Unit,
    appLanguage: String = "de"
) {
    var notebookToRename by remember { mutableStateOf<Notebook?>(null) }
    var notebookToDelete by remember { mutableStateOf<Notebook?>(null) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    val jsonPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportJson(uri)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        val configuration = LocalConfiguration.current
        val maxDialogHeight = remember(configuration.screenHeightDp) { (configuration.screenHeightDp * 0.88f).dp }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .heightIn(max = maxDialogHeight)
                        .testTag("notebook_manager_dialog"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(
                        if (GeoIsBoldTheme) 1.5.dp else 1.dp,
                        if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    if (GeoIsBoldTheme) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Brush.horizontalGradient(GeoBannerGradient))
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.notebookManagerTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.languageSectionSubtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.close,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Actions: 2 Full-width Rows (Neues Notizbuch & Aus JSON laden)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Neues Notizbuch
                            Surface(
                                onClick = onCreateNotebook,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AutoSizingText(
                                        text = strings.newNotebookButton,
                                        modifier = Modifier.weight(1f),
                                        maxFontSize = 16.sp,
                                        minFontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = strings.newNotebookButton,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Row 2: Aus JSON laden
                            Surface(
                                onClick = { jsonPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AutoSizingText(
                                        text = strings.loadFromJsonButton,
                                        modifier = Modifier.weight(1f),
                                        maxFontSize = 16.sp,
                                        minFontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = strings.loadFromJsonButton,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 🔒 Export-Optionen Section (Ohne API-Schlüssel)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Section Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = strings.exportOptionsSectionTitle,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "(${strings.exportSecurityNote})",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                )

                                // Option 1: Alle exportieren (eine Datei) - Clean without subtitle
                                Surface(
                                    onClick = onExportAllCombined,
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = strings.exportAllCombinedTitle,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )

                                // Option 2: Alle exportieren (einzelne Dateien) - Clean without subtitle
                                Surface(
                                    onClick = onExportAllSeparate,
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = strings.exportAllSeparateTitle,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )

                                // Option 3: Ausgewählte exportieren - Clean without subtitle
                                Surface(
                                    onClick = onExportSelectedClick,
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Checklist,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = strings.exportSelectedTitle,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Notebooks list - Unified scroll with no fixed height or crammed area
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            notebooks.forEach { notebook ->
                                val isActive = notebook.id == activeNotebookId
                                val theme = remember(notebook.settings.themeColorId) {
                                    AppThemePackage.fromId(notebook.settings.themeColorId)
                                }
                                val formattedDate = remember(notebook.lastModified) {
                                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date(notebook.lastModified))
                                }
                                var showMenu by remember { mutableStateOf(false) }
                                val boldGradColors = remember(theme) {
                                    listOf(Color(theme.bannerGradientStartLightHex), Color(theme.bannerGradientEndLightHex))
                                }
                                val ribbonCol = remember(theme) {
                                    Color(theme.cardAccentRibbonLightHex)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 72.dp)
                                        .then(
                                            if (!isActive) Modifier.clickable { onSelectNotebook(notebook.id) }
                                            else Modifier
                                        ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    border = BorderStroke(
                                        if (isActive) (if (theme.isBoldTheme) 2.dp else 1.5.dp) else 1.dp,
                                        if (isActive) {
                                            if (theme.isBoldTheme) ribbonCol else MaterialTheme.colorScheme.primary
                                        } else {
                                            if (theme.isBoldTheme) ribbonCol.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        }
                                    )
                                ) {
                                    if (theme.isBoldTheme) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.5.dp)
                                                .background(Brush.horizontalGradient(boldGradColors))
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Top Row: [Color Dot] [Notebook Name]  [Attractive Aktiv/Öffnen Pill Badge]
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(if (theme.isBoldTheme) 11.dp else 9.dp)
                                                        .clip(CircleShape)
                                                        .then(
                                                            if (theme.isBoldTheme) Modifier.background(Brush.horizontalGradient(boldGradColors))
                                                            else Modifier.background(theme.primary)
                                                        )
                                                )

                                                Text(
                                                    text = notebook.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Mutually exclusive: Pill badge for Aktiv vs. Öffnen
                                            if (isActive) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (theme.isBoldTheme) ribbonCol.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    border = BorderStroke(1.dp, if (theme.isBoldTheme) ribbonCol else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "✅",
                                                            fontSize = 11.sp
                                                        )
                                                        Text(
                                                            text = strings.activeNotebookBadge,
                                                            color = if (theme.isBoldTheme) ribbonCol else MaterialTheme.colorScheme.primary,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                            } else {
                                                Surface(
                                                    onClick = { onSelectNotebook(notebook.id) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = strings.activateNotebookAction,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = strings.activateNotebookAction,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Bottom Row: [📚 Word Count  ·  📅 Date]  [Options 3-dot menu]
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📚 ${strings.wordsCount(notebook.wordCount)}  ·  📅 $formattedDate",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Box {
                                                IconButton(
                                                    onClick = { showMenu = true },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(strings.renameNotebookAction, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface) },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            notebookToRename = notebook
                                                        }
                                                    )

                                                    DropdownMenuItem(
                                                        text = { Text(strings.exportNotebookJsonAction, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface) },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.UploadFile,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            onExportJson(notebook.id)
                                                        }
                                                    )

                                                    if (notebooks.size > 1) {
                                                        DropdownMenuItem(
                                                            text = { Text(strings.delete, fontSize = 13.sp, color = MaterialTheme.colorScheme.error) },
                                                            leadingIcon = {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            },
                                                            onClick = {
                                                                showMenu = false
                                                                notebookToDelete = notebook
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (notebookToRename != null) {
        val target = notebookToRename!!
        var newTitle by remember { mutableStateOf(target.name) }

        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { notebookToRename = null },
                title = {
                    Text(
                        text = strings.renameDialogTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = {
                            Text(
                                text = strings.renameDialogFieldLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = newTitle.trim()
                            if (name.isNotBlank()) {
                                onRenameNotebook(target.id, name)
                            }
                            notebookToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                    ) {
                        Text(
                            text = strings.save,
                            color = GeoOnPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { notebookToRename = null }) {
                        Text(
                            text = strings.cancel,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (notebookToDelete != null) {
        val target = notebookToDelete!!
        CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
            AlertDialog(
                onDismissRequest = { notebookToDelete = null },
                title = {
                    Text(
                        text = strings.deleteNotebookDialogTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                text = {
                    Text(
                        text = strings.deleteNotebookDialogMessage(target.name),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteNotebook(target.id)
                            notebookToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                    ) {
                        Text(
                            text = strings.delete,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { notebookToDelete = null }) {
                        Text(
                            text = strings.cancel,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            )
        }
    }
}

/**
 * Dialog offering Save or Share choice for exported JSON.
 */
/**
 * Dialog offering Save or Share choice for exported JSON.
 * Redesigned for high visual elegance, balanced layout, and full trilingual localization.
 */
@Composable
fun JsonExportActionDialog(
    fileName: String,
    title: String,
    onSaveToFile: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    // Dynamic clean localized title
    val displayTitle = when {
        title.contains("Alle", ignoreCase = true) || title.contains("All", ignoreCase = true) || title.contains("جميع", ignoreCase = true) ->
            strings.exportAllCombinedTitle
        title.contains("Ausgewählte", ignoreCase = true) || title.contains("Selected", ignoreCase = true) || title.contains("المحددة", ignoreCase = true) ->
            strings.exportSelectedTitle
        title.contains("Notizbuch exportieren", ignoreCase = true) || title.contains("Export Notebook", ignoreCase = true) || title.contains("تصدير دفتر", ignoreCase = true) -> {
            val nbName = title.substringAfter(":", "").trim().ifBlank { fileName.removeSuffix(".json") }
            "${strings.exportNotebookJsonAction}: $nbName"
        }
        else -> title.ifBlank { strings.jsonExportDialogTitle }
    }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("json_export_action_dialog"),
            shape = RoundedCornerShape(20.dp),
            containerColor = GeoSurface,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GeoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = GeoPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = displayTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // File summary card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GeoSurfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, GeoBorder.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GeoPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "JSON • UTF-8",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoPrimary
                                )
                            }
                        }
                    }

                    // Guidance text
                    Text(
                        text = strings.exportActionDialogMessage,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = GeoOnSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Save to file (Primary)
                        Button(
                            onClick = onSaveToFile,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeoPrimary,
                                contentColor = GeoOnPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.saveToFile,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share (Secondary)
                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GeoPrimary.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GeoPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.share,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Cancel
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Text(
                            text = strings.cancel,
                            color = GeoOnSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            dismissButton = null
        )
    }
}

/**
 * Dialog for selecting specific notebooks to export (Option 3).
 */
@Composable
fun SelectNotebooksExportDialog(
    notebooks: List<Notebook>,
    onDismiss: () -> Unit,
    onExportCombined: (selectedIds: List<String>) -> Unit,
    onExportSeparate: (selectedIds: List<String>) -> Unit,
    appLanguage: String = "de"
) {
    var selectedIds by remember { mutableStateOf(notebooks.map { it.id }.toSet()) }
    var exportMode by remember { mutableStateOf("combined") } // "combined" or "separate"
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = GeoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = strings.selectedNotebooksDialogTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick toggle: Keep only "All" button (removed "None" per user request)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.selectedCountOfTotal(selectedIds.size, notebooks.size),
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        )
                        val areAllSelected = selectedIds.size == notebooks.size && notebooks.isNotEmpty()
                        TextButton(
                            onClick = {
                                selectedIds = if (areAllSelected) emptySet() else notebooks.map { it.id }.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.selectAll,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoPrimary
                            )
                        }
                    }

                    // List of notebooks with checkboxes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .background(GeoSurfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(notebooks, key = { it.id }) { nb ->
                            val isChecked = selectedIds.contains(nb.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (isChecked) {
                                            selectedIds - nb.id
                                        } else {
                                            selectedIds + nb.id
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) {
                                            selectedIds + nb.id
                                        } else {
                                            selectedIds - nb.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = GeoPrimary),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nb.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GeoOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.wordsCount(nb.wordCount),
                                        fontSize = 10.5.sp,
                                        color = GeoOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Export mode selection
                    Text(
                        text = strings.exportFormatLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Surface(
                        onClick = { exportMode = "combined" },
                        shape = RoundedCornerShape(6.dp),
                        color = if (exportMode == "combined") GeoPrimaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (exportMode == "combined") GeoPrimary else GeoBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportMode == "combined",
                                onClick = { exportMode = "combined" },
                                colors = RadioButtonDefaults.colors(selectedColor = GeoPrimary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.exportFormatCombined,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = strings.exportFormatCombinedSubtitle,
                                    fontSize = 10.sp,
                                    color = GeoOnSurfaceVariant
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { exportMode = "separate" },
                        shape = RoundedCornerShape(6.dp),
                        color = if (exportMode == "separate") GeoPrimaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (exportMode == "separate") GeoPrimary else GeoBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = exportMode == "separate",
                                onClick = { exportMode = "separate" },
                                colors = RadioButtonDefaults.colors(selectedColor = GeoPrimary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.exportFormatSeparate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoOnSurface
                                )
                                Text(
                                    text = strings.exportFormatSeparateSubtitle,
                                    fontSize = 10.sp,
                                    color = GeoOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val list = selectedIds.toList()
                        if (exportMode == "combined") {
                            onExportCombined(list)
                        } else {
                            onExportSeparate(list)
                        }
                    },
                    enabled = selectedIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text(
                        text = strings.exportCountAction(selectedIds.size),
                        fontSize = 12.5.sp,
                        color = GeoOnPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = strings.cancel, fontSize = 12.5.sp, color = GeoOnSurfaceVariant)
                }
            },
            containerColor = GeoSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog when importing a single notebook from JSON.
 */
@Composable
fun ImportSingleChoiceDialog(
    detectedName: String,
    wordCount: Int,
    onMergeIntoActive: () -> Unit,
    onCreateAsNew: (name: String) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    var notebookName by remember { mutableStateOf(detectedName) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("import_single_choice_dialog"),
            icon = {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.importSingleTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.importSingleMessage(wordCount),
                        fontSize = 14.sp,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = notebookName,
                        onValueChange = { notebookName = it },
                        label = {
                            Text(
                                text = strings.nameForNewNotebookLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = GeoOnSurface,
                            unfocusedTextColor = GeoOnSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Neues Notizbuch (Height 44dp, padding 16dp, corner 10dp)
                    Button(
                        onClick = { onCreateAsNew(notebookName.trim().ifBlank { detectedName }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = GeoOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAsNewNotebookButton,
                            maxFontSize = 15.sp,
                            minFontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnPrimary
                        )
                    }

                    // Option 2: Zusammenführen (Height 44dp, padding 16dp, corner 10dp)
                    OutlinedButton(
                        onClick = onMergeIntoActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GeoPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        AutoSizingText(
                            text = strings.mergeIntoActiveNotebookButton,
                            maxFontSize = 15.sp,
                            minFontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoPrimary
                        )
                    }

                    // Cancel: Abbrechen (Height 36dp, text only)
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = strings.cancel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            },
            containerColor = GeoSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog when importing multiple notebooks from a combined JSON.
 */
@Composable
fun ImportMultiChoiceDialog(
    notebookNames: List<String>,
    totalWordCount: Int,
    fallbackName: String,
    onImportAllSeparate: () -> Unit,
    onImportAsSingle: (name: String) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: String = "de"
) {
    var singleNotebookName by remember { mutableStateOf(fallbackName) }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("import_multi_choice_dialog"),
            icon = {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.importMultiTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AutoSizingText(
                        text = strings.importMultiMessage(notebookNames.size, totalWordCount, notebookNames.take(4)),
                        maxFontSize = 13.sp,
                        minFontSize = 10.5.sp,
                        color = GeoOnSurfaceVariant
                    )

                    OutlinedTextField(
                        value = singleNotebookName,
                        onValueChange = { singleNotebookName = it },
                        label = {
                            Text(
                                text = strings.nameIfSingleCombinedLabel,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = GeoOnSurface,
                            unfocusedTextColor = GeoOnSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onImportAllSeparate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = GeoOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAllSeparateButton(notebookNames.size),
                            maxFontSize = 14.sp,
                            minFontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { onImportAsSingle(singleNotebookName.trim().ifBlank { fallbackName }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GeoPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GeoPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        AutoSizingText(
                            text = strings.importAsOneCombinedButton,
                            maxFontSize = 14.sp,
                            minFontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoPrimary
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = strings.cancel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            },
            containerColor = GeoSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Dialog presented after importing a notebook with settings, prompting the user
 * to configure their API key for the AI service or skip.
 */
@Composable
fun PostImportApiKeyDialog(
    notebookName: String,
    provider: String,
    modelId: String,
    onSaveApiKey: (apiKey: String) -> Unit,
    onOpenFullSettings: () -> Unit,
    onSkip: () -> Unit,
    appLanguage: String = "de"
) {
    var keyInput by remember { mutableStateOf("") }
    val strings = remember(appLanguage) { AppStrings.get(appLanguage) }

    CompositionLocalProvider(LocalLayoutDirection provides strings.language.layoutDirection) {
        AlertDialog(
            onDismissRequest = onSkip,
            icon = {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = GeoPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.postImportKeyTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.postImportKeyPrompt(notebookName),
                        fontSize = 12.5.sp,
                        color = GeoOnSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GeoSurfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, GeoBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$provider • $modelId",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoOnSurface
                            )
                        }
                    }

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text(strings.apiKeyPlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder
                        )
                    )

                    Text(
                        text = strings.postImportSecurityNote,
                        fontSize = 10.5.sp,
                        color = GeoOnSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (keyInput.isNotBlank()) {
                        Button(
                            onClick = { onSaveApiKey(keyInput.trim()) },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.postImportKeySave,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoOnPrimary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenFullSettings,
                        border = BorderStroke(1.dp, GeoPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.postImportKeyOpenSettings,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoPrimary
                        )
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.postImportKeySkip,
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        )
                    }
                }
            },
            containerColor = GeoSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

