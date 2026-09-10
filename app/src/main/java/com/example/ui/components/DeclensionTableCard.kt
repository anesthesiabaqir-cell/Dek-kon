package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.WordDeclensionResult
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoHeaderKasus
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoOnSecondaryContainer
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoSecondaryContainer
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant

@Composable
fun DeclensionResultView(
    result: WordDeclensionResult,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSpeak: (String) -> Unit = {},
    onExport: () -> Unit = {},
    ttsSpeed: Float = 1.0f,
    onTtsSpeedChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val genusDisplay = when (result.gender.lowercase()) {
        "maskulin", "masculine", "der", "mas" -> "Maskulin"
        "feminin", "feminine", "die", "fem" -> "Feminin"
        "neutrum", "neuter", "das", "neu" -> "Neutrum"
        else -> result.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    val detailsText = remember(genusDisplay, result.pluralNoun) {
        buildString {
            if (genusDisplay.isNotBlank()) {
                append(genusDisplay)
            }
            if (result.pluralNoun.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append("Plural: ")
                append(result.pluralNoun)
            }
        }
    }

    val fullPhraseString = remember(result.genderArticle, result.word) {
        if (result.genderArticle.isNotBlank()) {
            "${result.genderArticle} ${result.word}"
        } else {
            result.word
        }
    }

    val primaryColor = GeoPrimary
    val onSurfaceColor = GeoOnSurface

    val fullWordAnnotated = remember(result.genderArticle, result.word, primaryColor, onSurfaceColor) {
        buildAnnotatedString {
            if (result.genderArticle.isNotBlank()) {
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append(result.genderArticle)
                    append(" ")
                }
            }
            withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
                append(result.word)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("declension_result_container")
    ) {
        // Redesigned Top Word Display Card (Flexible & Responsive)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GeoBorder),
            colors = CardDefaults.cardColors(containerColor = GeoSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Row 1: Main Word + Article (left) and Audio Icon (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AutoSizingWordText(
                            text = fullWordAnnotated,
                            rawLength = fullPhraseString.length,
                            modifier = Modifier.fillMaxWidth(),
                            minTextSize = 14f,
                            maxTextSize = 24f
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Audio Pronunciation Button (consistent 32x32 grey circular style, fixed right)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                            .clickable { onSpeak(fullPhraseString) }
                            .testTag("pronounce_word_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Aussprache anhören",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Row 2: Grammatical Details - Genus & Plural (14sp, On Surface, left-aligned)
                if (detailsText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detailsText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = GeoOnSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (result.pluralNoun.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                                    .clickable { onSpeak(result.pluralNoun) }
                                    .testTag("pronounce_plural_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Plural anhören",
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Row 3: English Translation
                if (result.meaningEnglish.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(GeoPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = GeoOnPrimaryContainer
                            )
                        }
                        Text(
                            text = "English: ${result.meaningEnglish}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoOnSurface,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Geometric Segmented Tabs: Singular / Plural / Both (single-line no-wrap)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GeoSurfaceVariant)
                .border(1.dp, GeoBorder, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .clip(RoundedCornerShape(16.dp)),
                        color = GeoPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Singular",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_singular")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Plural",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_plural")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    selectedContentColor = GeoOnPrimaryContainer,
                    unselectedContentColor = GeoOnSurfaceVariant,
                    text = {
                        Text(
                            text = "Beiden",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tab_both")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tables display with Geometric Grid
        when (selectedTab) {
            0 -> DeclensionTableCard(group = result.singular, isPlural = false, onSpeak = onSpeak)
            1 -> DeclensionTableCard(group = result.plural, isPlural = true, onSpeak = onSpeak)
            2 -> {
                DeclensionTableCard(group = result.singular, isPlural = false, onSpeak = onSpeak)
                Spacer(modifier = Modifier.height(14.dp))
                DeclensionTableCard(group = result.plural, isPlural = true, onSpeak = onSpeak)
            }
        }
    }
}

@Composable
fun DeclensionTableCard(
    group: DeclensionTableGroup,
    isPlural: Boolean = false,
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val headerBg = if (isPlural) GeoSecondaryContainer else GeoPrimaryContainer
    val headerTextColor = if (isPlural) GeoOnSecondaryContainer else GeoOnPrimaryContainer

    // Outer Container with Geometric Balance: white card, crisp border, rounded-2xl
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Header Banner: uppercase, tracking-wider, e.g. "SINGULAR" or "PLURAL"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = group.numberDe.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = headerTextColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Declension Geometric Grid with horizontal scroll for long German nouns
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = maxWidth
                val maxKasusTextLen = group.rows.maxOfOrNull { maxOf(it.caseNameDe.length, it.caseQuestionDe.length) } ?: 0
                val kasusWidth = maxOf(120.dp, (maxKasusTextLen * 10f + 36f).dp)
                val maxDefiniteLen = group.rows.maxOfOrNull { it.definite.length } ?: 0
                val maxIndefiniteLen = group.rows.maxOfOrNull { it.indefinite.length } ?: 0
                val maxNoArticleLen = group.rows.maxOfOrNull { it.noArticle.length } ?: 0

                val remainingWidth = (cardWidth - kasusWidth - 3.dp).coerceAtLeast(300.dp)
                val baseColWidth = remainingWidth / 3f

                val colDefWidth = maxOf(baseColWidth, (maxDefiniteLen * 10f + 56f).dp, 120.dp)
                val colIndefWidth = maxOf(baseColWidth, (maxIndefiniteLen * 10f + 56f).dp, 120.dp)
                val colNoArtWidth = maxOf(baseColWidth, (maxNoArticleLen * 10f + 56f).dp, 108.dp)
                val totalDeclensionTableWidth = kasusWidth + 3.dp + colDefWidth + colIndefWidth + colNoArtWidth

                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.width(totalDeclensionTableWidth)) {
                        // Table Column Titles Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(GeoHeaderKasus)
                                .border(BorderStroke(1.dp, GeoBorder)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCellHeader(
                                title = "Kasus\n(Fall)",
                                modifier = Modifier.width(kasusWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Bestimmt\n(mit Artikel)",
                                modifier = Modifier.width(colDefWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Unbestimmt\n(ein / kein)",
                                modifier = Modifier.width(colIndefWidth)
                            )
                            TableGridDivider()
                            TableCellHeader(
                                title = "Ohne\n(Nullartikel)",
                                modifier = Modifier.width(colNoArtWidth)
                            )
                        }

                        // Table Rows with grid dividers
                        group.rows.forEachIndexed { index, row ->
                            val isLast = index == group.rows.size - 1
                            DeclensionGeometricRow(
                                row = row,
                                kasusWidth = kasusWidth,
                                colDefWidth = colDefWidth,
                                colIndefWidth = colIndefWidth,
                                colNoArtWidth = colNoArtWidth,
                                showBottomBorder = !isLast,
                                onSpeak = onSpeak
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCellHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GeoPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun DeclensionGeometricRow(
    row: DeclensionCaseRow,
    kasusWidth: Dp = 104.dp,
    colDefWidth: Dp = 95.dp,
    colIndefWidth: Dp = 95.dp,
    colNoArtWidth: Dp = 85.dp,
    showBottomBorder: Boolean = true,
    onSpeak: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(GeoSurface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Case Name (Kasus cell) - Dynamic & Safe: wrap_content, paddingEnd 8dp, min 10sp, max 16sp
            Box(
                modifier = Modifier
                    .width(kasusWidth)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DynamicTextView(
                        text = row.caseNameDe,
                        minTextSize = 10f,
                        maxTextSize = 14f,
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnSurface,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    DynamicTextView(
                        text = row.caseQuestionDe,
                        minTextSize = 10f, // Exact user requirement for question mark case (e.g. Wem?)
                        maxTextSize = 16f, // Exact user requirement: 10sp to 16sp
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp, // Ensures the '?' has dedicated space and is never cut off
                        color = GeoOutline,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.labelSmall
                    )
                }
            }

            TableGridDivider()

            // Definite (with Audio Icon)
            DeclensionCell(
                text = row.definite,
                width = colDefWidth,
                onSpeak = onSpeak,
                isBold = true,
                textColor = GeoOnSurface
            )

            TableGridDivider()

            // Indefinite (with Audio Icon)
            DeclensionCell(
                text = row.indefinite,
                width = colIndefWidth,
                onSpeak = onSpeak,
                isBold = false,
                textColor = GeoOnSurface
            )

            TableGridDivider()

            // Without Article (with Audio Icon)
            DeclensionCell(
                text = row.noArticle,
                width = colNoArtWidth,
                onSpeak = onSpeak,
                isBold = false,
                textColor = GeoOnSurfaceVariant
            )
        }

        if (showBottomBorder) {
            HorizontalDivider(thickness = 1.dp, color = GeoBorder)
        }
    }
}

@Composable
private fun DeclensionCell(
    text: String,
    width: Dp,
    onSpeak: (String) -> Unit,
    isBold: Boolean = false,
    textColor: Color = GeoOnSurface
) {
    val hasContent = text.isNotBlank() && text != "-"

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable(enabled = hasContent) { onSpeak(text) }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasContent) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.weight(1f, fill = false),
                    contentAlignment = Alignment.Center
                ) {
                    DynamicTextView(
                        text = text,
                        minTextSize = 8f,
                        maxTextSize = 15f,
                        paddingStart = 4.dp,
                        paddingEnd = 8.dp,
                        fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak $text",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        } else {
            DynamicTextView(
                text = text,
                minTextSize = 8f,
                maxTextSize = 15f,
                paddingStart = 4.dp,
                paddingEnd = 8.dp,
                fontWeight = FontWeight.Normal,
                color = GeoOnSurfaceVariant,
                textAlign = TextAlign.Center,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TableGridDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(GeoBorder)
    )
}
