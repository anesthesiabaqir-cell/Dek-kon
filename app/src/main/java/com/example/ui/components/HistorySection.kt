package com.example.ui.components

import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordHistoryEntity
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import org.json.JSONObject
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoFemininBg
import com.example.ui.theme.GeoFemininText
import com.example.ui.theme.GeoGlowColor
import com.example.ui.theme.GeoIsBoldTheme
import com.example.ui.theme.GeoMaskulinBg
import com.example.ui.theme.GeoMaskulinText
import com.example.ui.theme.GeoNeutrumBg
import com.example.ui.theme.GeoNeutrumText
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSecondaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPluralBg
import com.example.ui.theme.GeoPluralText
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSecondaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object HistoryFormatterCache {
    private val dateCache = LruCache<Long, String>(1024)
    private val shortDateCache = LruCache<Long, String>(1024)
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        val cached = dateCache.get(timestamp)
        if (cached != null) return cached
        val formatted = synchronized(dateFormat) {
            dateFormat.format(Date(timestamp))
        }
        dateCache.put(timestamp, formatted)
        return formatted
    }

    fun formatShortDate(timestamp: Long): String {
        val cached = shortDateCache.get(timestamp)
        if (cached != null) return cached
        val formatted = synchronized(shortDateFormat) {
            shortDateFormat.format(Date(timestamp))
        }
        shortDateCache.put(timestamp, formatted)
        return formatted
    }
}

internal data class HistoryBadgeInfo(
    val bg: Color,
    val text: Color,
    val label: String
)

@Composable
internal fun getBadgeInfo(isVerb: Boolean, gender: String): HistoryBadgeInfo {
    if (isVerb) return remember { HistoryBadgeInfo(Color(0xFFFEF3C7), Color(0xFFB45309), "VERB") }

    return remember(gender) {
        when (gender.lowercase(Locale.ROOT)) {
            "satz", "sentence", "–", "-" -> HistoryBadgeInfo(Color(0xFFE0F2F1), Color(0xFF00695C), "Satz")
            "maskulin", "masculine", "der" -> HistoryBadgeInfo(GeoMaskulinBg, GeoMaskulinText, "MASKULIN")
            "feminin", "feminine", "die" -> HistoryBadgeInfo(GeoFemininBg, GeoFemininText, "FEMININ")
            "neutrum", "neuter", "das" -> HistoryBadgeInfo(GeoNeutrumBg, GeoNeutrumText, "NEUTRUM")
            else -> HistoryBadgeInfo(GeoPluralBg, GeoPluralText, "PLURAL")
        }
    }
}

@Composable
fun HistoryHeaderRow(
    filteredCount: Int,
    hasItems: Boolean,
    onRequestClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = GeoPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Suchverlauf",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface,
                maxLines = 1,
                softWrap = false
            )
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GeoPrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$filteredCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnPrimaryContainer
                    )
                }
            }
        }

        if (hasItems) {
            TextButton(
                onClick = onRequestClear,
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Verlauf leeren",
                    modifier = Modifier.size(16.dp),
                    tint = GeoOnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Leeren",
                    style = MaterialTheme.typography.labelMedium,
                    color = GeoOnSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun HistorySearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GeoSurfaceVariant)
                .border(
                    1.dp,
                    if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.5f) else GeoBorder,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GeoPrimary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Verlauf durchsuchen",
                        fontSize = 16.sp,
                        color = GeoOnSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    maxLines = 1,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = GeoOnSurface
                    ),
                    cursorBrush = SolidColor(GeoPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input_field")
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
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
    }
}

@Composable
fun HistoryFilterPills(
    selectedFilterIndex: Int,
    onFilterChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf("Alle", "Nomen & Sätze", "Verben").forEachIndexed { index, title ->
            val isSelected = selectedFilterIndex == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) GeoPrimary else GeoSurfaceVariant)
                    .clickable { onFilterChanged(index) }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("history_filter_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else GeoOnSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun HistoryEmptyCard(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = GeoOutline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "Keine Treffer" else "Kein Suchverlauf",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Kein Eintrag im Verlauf entspricht \"$searchQuery\"."
                } else {
                    "Gesuchte Nomen und Verben erscheinen hier zur schnellen Wiederverwendung."
                },
                style = MaterialTheme.typography.bodySmall,
                color = GeoOnSurfaceVariant
            )
        }
    }
}

@Composable
fun ClearHistoryConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GeoSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Suchverlauf leeren",
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface
            )
        },
        text = {
            Text(
                "Möchten Sie wirklich alle Einträge aus dem Verlauf entfernen?",
                color = GeoOnSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_clear_history")
            ) {
                Text("Löschen", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = GeoPrimary)
            }
        }
    )
}

@Composable
fun DeleteHistoryItemConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GeoSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Eintrag löschen?",
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface
            )
        },
        text = {
            Text(
                "Möchten Sie diesen Eintrag aus dem Verlauf entfernen?",
                color = GeoOnSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_history_item")
            ) {
                Text("Löschen", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = GeoPrimary)
            }
        }
    )
}

fun LazyListScope.historySectionItems(
    historyList: List<WordHistoryEntity>,
    filteredList: List<WordHistoryEntity>,
    selectedFilterIndex: Int,
    historySearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChanged: (Int) -> Unit,
    onSelectWord: (WordHistoryEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onRequestClearAll: () -> Unit,
    onSpeak: (String) -> Unit
) {
    item(key = "history_header_row") {
        HistoryHeaderRow(
            filteredCount = filteredList.size,
            hasItems = historyList.isNotEmpty(),
            onRequestClear = onRequestClearAll
        )
    }

    item(key = "history_search_box") {
        HistorySearchBar(
            searchQuery = historySearchQuery,
            onQueryChange = onSearchQueryChange
        )
    }

    if (historyList.isNotEmpty()) {
        item(key = "history_filter_pills") {
            HistoryFilterPills(
                selectedFilterIndex = selectedFilterIndex,
                onFilterChanged = onFilterChanged
            )
        }
    }

    if (filteredList.isEmpty()) {
        item(key = "history_empty_card") {
            HistoryEmptyCard(searchQuery = historySearchQuery)
        }
    } else {
        items(
            items = filteredList,
            key = { it.id },
            contentType = { "history_item_card" }
        ) { item ->
            HistoryItemCard(
                item = item,
                onClick = { onSelectWord(item) },
                onDelete = { onDeleteItem(item.id) },
                onSpeak = onSpeak,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun HistorySection(
    historyList: List<WordHistoryEntity>,
    onSelectWord: (WordHistoryEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onSpeak: (String) -> Unit = {},
    onExportAll: () -> Unit = {},
    selectedFilterIndex: Int = 0,
    onFilterChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableStateOf<Long?>(null) }
    var historySearchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = showClearConfirmDialog || itemToDeleteId != null) {
        showClearConfirmDialog = false
        itemToDeleteId = null
    }

    val filteredList by produceState(
        initialValue = historyList,
        key1 = historyList,
        key2 = selectedFilterIndex,
        key3 = historySearchQuery
    ) {
        withContext(Dispatchers.Default) {
            val typeFiltered = when (selectedFilterIndex) {
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        HistoryHeaderRow(
            filteredCount = filteredList.size,
            hasItems = historyList.isNotEmpty(),
            onRequestClear = { showClearConfirmDialog = true }
        )

        HistorySearchBar(
            searchQuery = historySearchQuery,
            onQueryChange = { historySearchQuery = it }
        )

        if (historyList.isNotEmpty()) {
            HistoryFilterPills(
                selectedFilterIndex = selectedFilterIndex,
                onFilterChanged = onFilterChanged
            )
        }

        if (filteredList.isEmpty()) {
            HistoryEmptyCard(searchQuery = historySearchQuery)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filteredList.forEach { item ->
                    key(item.id) {
                        HistoryItemCard(
                            item = item,
                            onClick = { onSelectWord(item) },
                            onDelete = { itemToDeleteId = item.id },
                            onSpeak = onSpeak
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                onClearAll()
                showClearConfirmDialog = false
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    itemToDeleteId?.let { id ->
        DeleteHistoryItemConfirmDialog(
            onConfirm = {
                onDeleteItem(id)
                itemToDeleteId = null
            },
            onDismiss = { itemToDeleteId = null }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: WordHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isVerb = remember(item.type) { item.type.equals("Verb", ignoreCase = true) }
    val isSentence = remember(item.type, item.gender, item.word) {
        item.type.equals("Satz", ignoreCase = true) ||
        item.type.equals("Sentence", ignoreCase = true) ||
        item.gender.equals("Satz", ignoreCase = true) ||
        item.gender == "–" ||
        item.word.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size >= 2
    }
    val badge = getBadgeInfo(isVerb, item.gender)
    val formattedDate = remember(item.timestamp) { HistoryFormatterCache.formatDate(item.timestamp) }

    val verbDetails = remember(item, isVerb) {
        if (isVerb) extractVerbQuickDetails(item) else null
    }

    val nounDetails = remember(item, isVerb, isSentence) {
        if (!isVerb && !isSentence) extractNounQuickDetails(item) else null
    }

    val pluralText = remember(item.rawJsonResult, isVerb, isSentence) {
        if (isVerb || isSentence) "" else extractPluralForm(item.rawJsonResult)
    }

    val singularDisplay = remember(isVerb, isSentence, item.genderArticle, item.word) {
        if (!isVerb && !isSentence && item.genderArticle.isNotBlank()) {
            "${item.genderArticle} ${item.word}"
        } else {
            item.word
        }
    }

    if (isSentence) {
        val sentenceScrollState = rememberScrollState()
        val translationScrollState = rememberScrollState()
        val shortDate = remember(item.timestamp) {
            HistoryFormatterCache.formatShortDate(item.timestamp)
        }
        val contentTextColor = GeoOnSurface

        val translationText = remember(item.meaningEnglish, item.rawJsonResult) {
            var raw = item.meaningEnglish.trim()
            if (raw.isBlank() && item.rawJsonResult.isNotBlank()) {
                try {
                    val obj = JSONObject(item.rawJsonResult)
                    raw = obj.optString("meaningEnglish", obj.optString("meaning", "")).trim()
                } catch (_: Exception) {}
            }
            val clean = if (raw.startsWith("Eng.:", ignoreCase = true)) {
                raw.substringAfter("Eng.:").trim()
            } else if (raw.startsWith("Eng:", ignoreCase = true)) {
                raw.substringAfter("Eng:").trim()
            } else {
                raw
            }
            clean.replace(Regex("[\\u0600-\\u06FF]"), "").replace(Regex("\\s+"), " ").trim()
        }

        val translationAnnotated = remember(translationText, contentTextColor) {
            val normalColor = contentTextColor
            val greenColor = Color(0xFF2E7D32)
            buildAnnotatedString {
                withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                    append("Eng.:")
                }
                withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                    append(if (translationText.isNotBlank()) " $translationText" else " —")
                }
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
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
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp)
            ) {
                // Header: Original sentence with horizontal scrolling and action icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(sentenceScrollState),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onSpeak(item.word) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Satz anhören",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Divider
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Row 2: English Translation - horizontally scrollable, strictly no wrapping
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .horizontalScroll(translationScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Divider
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Row 3: Metadata - Date with 📅 and "Satz" badge in teal container
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 $shortDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (LocalIsDarkTheme.current) Color(0xFF004D40) else Color(0xFFE0F2F1))
                            .border(1.dp, Color(0xFF00897B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Satz",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = if (LocalIsDarkTheme.current) Color(0xFF80CBC4) else Color(0xFF00695C),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    } else if (isVerb) {
        // 🎯 Unified Card Design for Verbs (Dynamic Theming)
        // Fixed/flexible min 160dp height, unified dynamic GeoSurfaceVariant container, 12dp start/end padding, 8dp top padding, 14dp bottom padding
        // 4 clearly separated rows with 1dp dynamic dividers (GeoBorder)
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
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
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp)
            ) {
                // Element 1: Header (word) - 32dp height, bold font, distinctive color
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Quick action icons (speak and delete) on top right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onSpeak(item.word) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Wort anhören",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 2: Conjugations row (German) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val conjugationsScrollState = rememberScrollState()

                val aux = remember(verbDetails?.auxiliary) {
                    val raw = verbDetails?.auxiliary.orEmpty().trim()
                        .removePrefix("Hilfsv.:").removePrefix("Hilfsv:").removePrefix("HV.:").removePrefix("HV:")
                        .removePrefix("Hilfsverb:").removePrefix("Hilfsverb")
                        .trim()
                        .removeSurrounding("(", ")")
                        .trim()
                    when {
                        raw.equals("haben", ignoreCase = true) -> "hat"
                        raw.equals("sein", ignoreCase = true) -> "ist"
                        else -> raw
                    }
                }

                val p1 = remember(verbDetails?.partizip1, item.word) {
                    val raw = verbDetails?.partizip1.orEmpty().trim()
                        .removePrefix("Part I:").removePrefix("Part I").removePrefix("Partizip I:").removePrefix("Partizip I")
                        .trim()
                    if (raw.isBlank() && item.word.isNotBlank()) {
                        if (item.word.endsWith("d", ignoreCase = true)) item.word else "${item.word}d"
                    } else {
                        raw
                    }
                }

                val p2 = remember(verbDetails?.partizip2) {
                    verbDetails?.partizip2.orEmpty().trim()
                        .removePrefix("Part II:").removePrefix("Part II").removePrefix("Partizip II:").removePrefix("Partizip II")
                        .trim()
                }

                val contentTextColor = GeoOnSurface

                val conjugationsAnnotated = remember(aux, p1, p2, contentTextColor) {
                    val normalColor = contentTextColor
                    val blueColor = Color(0xFF1565C0)
                    val orangeColor = Color(0xFFE65100)

                    val parts = mutableListOf<Triple<String, String, Color>>()
                    if (aux.isNotBlank()) {
                        parts.add(Triple("Hilfsv.:", aux, blueColor))
                    }
                    if (p1.isNotBlank()) {
                        parts.add(Triple("Part I:", p1, orangeColor))
                    }
                    if (p2.isNotBlank()) {
                        parts.add(Triple("Part II:", p2, orangeColor))
                    }

                    if (parts.isEmpty()) return@remember null

                    buildAnnotatedString {
                        parts.forEachIndexed { index, (indicator, value, indColor) ->
                            if (index > 0) {
                                withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            withStyle(SpanStyle(color = indColor, fontWeight = FontWeight.Bold)) {
                                append(indicator)
                            }
                            withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                append(" $value")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(conjugationsScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conjugationsAnnotated != null) {
                        Text(
                            text = conjugationsAnnotated,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = "—",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentTextColor
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeoBorder.copy(alpha = 0.5f))
                )

                // Element 3: Translation row (English) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val translationScrollState = rememberScrollState()
                val translationText = remember(verbDetails?.englishMeaning) {
                    val raw = verbDetails?.englishMeaning.orEmpty().trim()
                    val clean = if (raw.startsWith("Eng.:", ignoreCase = true)) {
                        raw.substringAfter("Eng.:").trim()
                    } else if (raw.startsWith("Eng:", ignoreCase = true)) {
                        raw.substringAfter("Eng:").trim()
                    } else {
                        raw
                    }
                    if (clean.contains(",")) {
                        clean.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · ")
                    } else {
                        clean
                    }
                }

                val translationAnnotated = remember(translationText, contentTextColor) {
                    val normalColor = contentTextColor
                    val greenColor = Color(0xFF2E7D32)
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                            append("Eng.:")
                        }
                        withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                            append(if (translationText.isNotBlank()) " $translationText" else " —")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(translationScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 4: Metadata row - 28dp height
                Spacer(modifier = Modifier.height(4.dp))
                val shortDate = remember(item.timestamp) {
                    HistoryFormatterCache.formatShortDate(item.timestamp)
                }
                val praetValue = remember(verbDetails) {
                    if (verbDetails == null) return@remember ""
                    val form = verbDetails.praeteritum.trim().ifBlank { verbDetails.partizip2.trim() }
                    form.removePrefix("Prät.:").removePrefix("Prät:").removePrefix("Prät.").trim()
                }

                val caseObjValue = remember(verbDetails, item.word) {
                    val rawCase = verbDetails?.caseObject.orEmpty().trim()
                    val clean = when {
                        rawCase.contains("akk", ignoreCase = true) && rawCase.contains("dat", ignoreCase = true) -> "Akkusativ / Dativ"
                        rawCase.contains("akk", ignoreCase = true) -> "Akkusativ"
                        rawCase.contains("dat", ignoreCase = true) -> "Dativ"
                        rawCase.contains("gen", ignoreCase = true) -> "Genitiv"
                        rawCase == "-" || rawCase.equals("none", ignoreCase = true) || rawCase.equals("kein", ignoreCase = true) -> ""
                        else -> {
                            val trimmed = rawCase.removeSuffix(":").trim()
                            when {
                                trimmed.equals("Akk.", ignoreCase = true) || trimmed.equals("Akk", ignoreCase = true) -> "Akkusativ"
                                trimmed.equals("Dat.", ignoreCase = true) || trimmed.equals("Dat", ignoreCase = true) -> "Dativ"
                                trimmed.equals("Gen.", ignoreCase = true) || trimmed.equals("Gen", ignoreCase = true) -> "Genitiv"
                                else -> trimmed
                            }
                        }
                    }
                    val candidate = if (clean.isBlank()) {
                        com.example.data.model.getFallbackVerbCaseObject(item.word)
                    } else {
                        clean
                    }
                    when {
                        candidate.contains("akk", ignoreCase = true) && candidate.contains("dat", ignoreCase = true) -> "Akkusativ / Dativ"
                        candidate.contains("akk", ignoreCase = true) -> "Akkusativ"
                        candidate.contains("dat", ignoreCase = true) -> "Dativ"
                        candidate.contains("gen", ignoreCase = true) -> "Genitiv"
                        else -> candidate.removeSuffix(":").trim()
                    }
                }

                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val isDark = LocalIsDarkTheme.current
                val normalTextColor = GeoOnSurface

                val pastAndCaseAnnotated = remember(praetValue, caseObjValue, tertiaryColor, isDark, normalTextColor) {
                    if (praetValue.isBlank() && caseObjValue.isBlank()) return@remember null
                    val redColor = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828)

                    buildAnnotatedString {
                        var hasPreceding = false

                        // 1. Präteritum form: Prät.: bildete
                        if (praetValue.isNotBlank()) {
                            withStyle(SpanStyle(color = redColor, fontWeight = FontWeight.Bold)) {
                                append("Prät.:")
                            }
                            withStyle(SpanStyle(color = normalTextColor, fontWeight = FontWeight.Normal)) {
                                append(" $praetValue")
                            }
                            hasPreceding = true
                        }

                        // 2. Case Object: · Akkusativ / Dativ / Genitiv (no abbreviation, no colon, no extra text)
                        if (caseObjValue.isNotBlank()) {
                            if (hasPreceding) {
                                withStyle(SpanStyle(color = normalTextColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            val caseLabel = caseObjValue.removeSuffix(":")
                            withStyle(SpanStyle(color = tertiaryColor, fontWeight = FontWeight.Bold)) {
                                append(caseLabel)
                            }
                        }
                    }
                }

                val metaScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .horizontalScroll(metaScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Date with emoji: 📅 08.09.2026
                    Text(
                        text = "📅 $shortDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 2. verb indicator: in yellow (#F9A825) and bold
                    Text(
                        text = "verb",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9A825)
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 3. Past tense label and Case object with Verb: Prät.: bildete · Akk.: bilden
                    if (pastAndCaseAnnotated != null) {
                        Text(
                            text = pastAndCaseAnnotated,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    } else {
        // Noun Unified Card Layout matching Verb Card 4-row structure
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .graphicsLayer { clip = false }
                .clickable(onClick = onClick)
                .testTag("history_item_${item.id}"),
            shape = RoundedCornerShape(12.dp),
            border = if (GeoIsBoldTheme) {
                BorderStroke(1.5.dp, GeoCardRibbonColor.copy(alpha = 0.55f))
            } else {
                BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
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
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp)
            ) {
                val titleWord = nounDetails?.singular?.ifBlank { singularDisplay } ?: singularDisplay

                // Element 1: Header (word) - 32dp height, bold font, distinctive color
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = titleWord,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (GeoIsBoldTheme) GeoPrimary else if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Quick action icons (speak and delete) on top right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onSpeak(titleWord) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pronounce_history_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Wort anhören",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("delete_history_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eintrag löschen",
                                tint = GeoOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeoBorder.copy(alpha = 0.5f))
                )

                // Element 2: Declensions row (German) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val declensionsScrollState = rememberScrollState()

                val pluralVal = remember(nounDetails, pluralText) {
                    val p = nounDetails?.plural?.ifBlank { pluralText } ?: pluralText
                    p.trim()
                }
                val genVal = remember(nounDetails) {
                    nounDetails?.genitiveSingular.orEmpty().trim()
                }
                val datVal = remember(nounDetails) {
                    nounDetails?.dativePlural.orEmpty().trim()
                }

                val isDark = LocalIsDarkTheme.current
                val contentTextColor = GeoOnSurface

                val declensionsAnnotated = remember(pluralVal, genVal, datVal, contentTextColor, isDark) {
                    val normalColor = contentTextColor
                    val purpleColor = if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2)
                    val blueColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
                    val orangeColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)

                    val parts = mutableListOf<Triple<String, String, Color>>()
                    if (pluralVal.isNotBlank() && pluralVal != "—") {
                        parts.add(Triple("Pl.:", pluralVal, purpleColor))
                    }
                    if (genVal.isNotBlank() && genVal != "—") {
                        parts.add(Triple("Gen.:", genVal, blueColor))
                    }
                    if (datVal.isNotBlank() && datVal != "—") {
                        parts.add(Triple("Dat. Pl.:", datVal, orangeColor))
                    }

                    if (parts.isEmpty()) return@remember null

                    buildAnnotatedString {
                        parts.forEachIndexed { index, (indicator, value, indColor) ->
                            if (index > 0) {
                                withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                    append(" · ")
                                }
                            }
                            withStyle(SpanStyle(color = indColor, fontWeight = FontWeight.Bold)) {
                                append(indicator)
                            }
                            withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                                append(" $value")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(declensionsScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (declensionsAnnotated != null) {
                        Text(
                            text = declensionsAnnotated,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = "—",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = contentTextColor
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 3: Translation row (English) - 40dp height, scrollable horizontally
                Spacer(modifier = Modifier.height(4.dp))
                val translationScrollState = rememberScrollState()
                val translationText = remember(nounDetails?.englishMeaning, item.meaningEnglish) {
                    val raw = nounDetails?.englishMeaning?.takeIf { it.isNotBlank() }
                        ?: item.meaningEnglish.trim()
                    val clean = if (raw.startsWith("Eng.:", ignoreCase = true)) {
                        raw.substringAfter("Eng.:").trim()
                    } else if (raw.startsWith("Eng:", ignoreCase = true)) {
                        raw.substringAfter("Eng:").trim()
                    } else {
                        raw
                    }
                    val noArabic = clean.replace(Regex("[\\u0600-\\u06FF]"), "").replace(Regex("\\s+"), " ").trim()
                    if (noArabic.contains(",")) {
                        noArabic.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · ")
                    } else {
                        noArabic
                    }
                }

                val translationAnnotated = remember(translationText, contentTextColor) {
                    val normalColor = contentTextColor
                    val greenColor = Color(0xFF2E7D32)
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = greenColor, fontWeight = FontWeight.Bold)) {
                            append("Eng.:")
                        }
                        withStyle(SpanStyle(color = normalColor, fontWeight = FontWeight.Normal)) {
                            append(if (translationText.isNotBlank()) " $translationText" else " —")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .horizontalScroll(translationScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translationAnnotated,
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Thin horizontal divider: 2dp top margin, 1dp height
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (GeoIsBoldTheme) GeoGlowColor.copy(alpha = 0.35f) else GeoBorder.copy(alpha = 0.5f))
                )

                // Element 4: Metadata row - 28dp height
                Spacer(modifier = Modifier.height(4.dp))
                val shortDate = remember(item.timestamp) {
                    HistoryFormatterCache.formatShortDate(item.timestamp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Date with emoji: 📅 08.09.2026 (left)
                    Text(
                        text = "📅 $shortDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GeoOnSurfaceVariant
                        ),
                        maxLines = 1,
                        softWrap = false
                    )

                    // 2. Gender badge moved to far right (colored container)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badge.bg)
                            .border(1.dp, GeoBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = badge.text,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

private fun extractPluralForm(rawJson: String): String {
    if (rawJson.isBlank()) return ""
    return try {
        val json = JSONObject(rawJson)
        val pluralNoun = json.optString("pluralNoun").trim()
        if (pluralNoun.isNotBlank()) return pluralNoun
        val pluralObj = json.optJSONObject("plural")
        val nominativObj = pluralObj?.optJSONObject("nominativ")
        val definite = nominativObj?.optString("definite").orEmpty().trim()
        if (definite.isNotBlank()) return definite
        nominativObj?.optString("noArticle").orEmpty().trim()
    } catch (e: Exception) {
        ""
    }
}

