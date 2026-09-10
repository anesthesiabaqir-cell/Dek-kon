package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.notebook.Notebook
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.GeoBorder
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
                    contentDescription = "Notizbuch",
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
                    contentDescription = "Notizbücher anzeigen",
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
            Text(
                text = "Meine Notizbücher",
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
                                    text = "${notebook.wordCount} Wörter",
                                    fontSize = 11.sp,
                                    color = GeoOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Aktiv",
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
                            text = "Neues Notizbuch",
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
                            text = "Notizbücher verwalten",
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

/**
 * Dialog to create a new notebook.
 */
@Composable
fun CreateNotebookDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, themeColorId: String, themeMode: String) -> Unit
) {
    var notebookName by remember { mutableStateOf("") }
    var selectedThemeId by remember { mutableStateOf("Schiefer") }
    var selectedThemeMode by remember { mutableStateOf("auto") }

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
                    text = "Neues Notizbuch erstellen",
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
                    text = "Geben Sie einen Namen für das neue Notizbuch ein (z. B. 'A1.1 - Grundlagen' oder 'A2 - Verben'):",
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
                            text = "Name des Notizbuchs",
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

                // Theme Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Design-Modus:",
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
                            Triple("auto", "System", "Folgt dem System"),
                            Triple("light", "Hell", "Light Mode"),
                            Triple("dark", "Dunkel", "Dark Mode")
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
                    text = "Erstellen",
                    color = GeoOnPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = "Abbrechen",
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

/**
 * Notebook Manager Dialog for listing, renaming, deleting, ZIP exporting, and ZIP importing workbooks.
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
    onExportZip: (notebookId: String) -> Unit,
    onImportZip: (uri: Uri) -> Unit
) {
    var notebookToRename by remember { mutableStateOf<Notebook?>(null) }
    var notebookToDelete by remember { mutableStateOf<Notebook?>(null) }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportZip(uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 640.dp)
                .testTag("notebook_manager_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GeoSurface),
            border = BorderStroke(1.dp, GeoBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
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
                                .background(GeoPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = GeoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notizbücher verwalten",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Lernhefte verwalten & wechseln",
                                fontSize = 11.sp,
                                color = GeoOnSurfaceVariant,
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
                            contentDescription = "Schließen",
                            tint = GeoOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCreateNotebook,
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = GeoOnPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Neues Notizbuch",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = { zipPickerLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, GeoBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = GeoPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aus ZIP laden",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeoOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.5.dp, color = GeoBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // Notebooks list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(notebooks, key = { it.id }) { notebook ->
                        val isActive = notebook.id == activeNotebookId
                        val theme = remember(notebook.settings.themeColorId) {
                            AppThemePackage.fromId(notebook.settings.themeColorId)
                        }
                        val formattedDate = remember(notebook.lastModified) {
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date(notebook.lastModified))
                        }
                        var showMenu by remember { mutableStateOf(false) }

                        // Dynamic text size for notebook name based on length to prevent truncation
                        val nameFontSize = when {
                            notebook.name.length > 28 -> 12.sp
                            notebook.name.length > 20 -> 13.sp
                            notebook.name.length > 14 -> 13.5.sp
                            else -> 14.5.sp
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!isActive) Modifier.clickable { onSelectNotebook(notebook.id) }
                                    else Modifier
                                ),
                            shape = RoundedCornerShape(9.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFF1B382B).copy(alpha = 0.28f) else GeoSurfaceVariant.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isActive) Color(0xFF4CAF50).copy(alpha = 0.65f) else GeoBorder.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(theme.primary)
                                        )

                                        Text(
                                            text = notebook.name,
                                            fontSize = nameFontSize,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GeoOnSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Mutually exclusive: Attractive, modern pill badge for Aktiv vs. Öffnen
                                    if (isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.16f),
                                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.6f)),
                                            shadowElevation = 0.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                                            ) {
                                                Text(
                                                    text = "Aktiv",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Aktiv",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            onClick = { onSelectNotebook(notebook.id) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF2196F3).copy(alpha = 0.16f),
                                            border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.6f)),
                                            shadowElevation = 0.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                                            ) {
                                                Text(
                                                    text = "Öffnen",
                                                    color = Color(0xFF2196F3),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Öffnen",
                                                    tint = Color(0xFF2196F3),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                // Bottom Row: [Metadata details: word count • date]  [Options 3-dot menu]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${notebook.wordCount} Wörter  •  $formattedDate",
                                        fontSize = 10.5.sp,
                                        color = GeoOnSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Box {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Optionen",
                                                tint = GeoOnSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(GeoSurface)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Umbenennen", fontSize = 13.sp, color = GeoOnSurface) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        tint = GeoPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    notebookToRename = notebook
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text("Als ZIP exportieren", fontSize = 13.sp, color = GeoOnSurface) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Archive,
                                                        contentDescription = null,
                                                        tint = GeoPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onExportZip(notebook.id)
                                                }
                                            )

                                            if (notebooks.size > 1) {
                                                DropdownMenuItem(
                                                    text = { Text("Löschen", fontSize = 13.sp, color = Color(0xFFBA1A1A)) },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = null,
                                                            tint = Color(0xFFBA1A1A),
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

    // Rename Dialog
    if (notebookToRename != null) {
        val target = notebookToRename!!
        var newTitle by remember { mutableStateOf(target.name) }

        AlertDialog(
            onDismissRequest = { notebookToRename = null },
            title = {
                Text(
                    text = "Notizbuch umbenennen",
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
                            text = "Neuer Name",
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
                        text = "Speichern",
                        color = GeoOnPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { notebookToRename = null }) {
                    Text(
                        text = "Abbrechen",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (notebookToDelete != null) {
        val target = notebookToDelete!!
        AlertDialog(
            onDismissRequest = { notebookToDelete = null },
            title = {
                Text(
                    text = "Notizbuch löschen?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBA1A1A),
                    maxLines = 1,
                    softWrap = false
                )
            },
            text = {
                Text(
                    text = "Möchten Sie das Notizbuch '${target.name}' mit allen gespeicherten Wörtern und Einstellungen wirklich unwiderruflich löschen?",
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
                        text = "Löschen",
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { notebookToDelete = null }) {
                    Text(
                        text = "Abbrechen",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        )
    }
}
