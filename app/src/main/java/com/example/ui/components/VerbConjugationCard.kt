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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VerbConjugationResult
import com.example.data.model.VerbImperativ
import com.example.data.model.VerbTenseConjugation
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GeoBannerGradient
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardRibbonColor
import com.example.ui.theme.GeoHeaderKasus
import com.example.ui.theme.GeoIsBoldTheme
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
fun VerbConjugationResultView(
    result: VerbConjugationResult,
    onSpeak: (String) -> Unit = {},
    ttsSpeed: Float = 1.0f,
    onTtsSpeedChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTenseTab by remember { mutableIntStateOf(0) }

    // Build tab labels from available tenses + Imperativ + Alle
    val tabTitles = remember(result.tenses) {
        val list = result.tenses.map { it.tenseNameDe }.toMutableList()
        list.add("Imperativ")
        list.add("Alle")
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("verb_conjugation_result_container")
    ) {
        // Verb Header Card (Flexible & Responsive)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                if (GeoIsBoldTheme) 1.5.dp else 1.dp,
                if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
            ),
            colors = CardDefaults.cardColors(containerColor = GeoSurface),
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
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Row 1: Verb (left) and Audio Icon (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        AutoSizingWordText(
                            text = result.infinitiv,
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
                            .clickable { onSpeak(result.infinitiv) }
                            .testTag("pronounce_verb_infinitive"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Verb aussprechen",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Row 2: Auxiliary Verb (own row, blue badge + normal verb text)
                val auxVerb = result.hilfsverb.trim().ifBlank { "haben" }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verb_auxiliary_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2196F3))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("verb_auxiliary_badge")
                    ) {
                        Text(
                            text = "Hilfsverb:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.3.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(
                        text = auxVerb,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeoOnSurface,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("verb_auxiliary_text")
                    )
                }

                // Row 3: Partizip I · Partizip II (own row, no audio icon, horizontally scrollable, no wrapping)
                val p1 = result.partizip1.trim().ifBlank {
                    if (result.infinitiv.isNotBlank()) {
                        if (result.infinitiv.endsWith("d", ignoreCase = true)) result.infinitiv else "${result.infinitiv}d"
                    } else ""
                }
                val p2 = result.partizip2.trim()

                if (p1.isNotBlank() || p2.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val partizipScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(partizipScrollState)
                            .testTag("verb_partizip_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                var hasPrev = false
                                if (p1.isNotBlank()) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GeoOnSurface)) {
                                        append("Partizip I: ")
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = GeoOnSurfaceVariant)) {
                                        append(p1)
                                    }
                                    hasPrev = true
                                }
                                if (p2.isNotBlank()) {
                                    if (hasPrev) {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = GeoOnSurfaceVariant)) {
                                            append(" · ")
                                        }
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GeoOnSurface)) {
                                        append("Partizip II: ")
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = GeoOnSurfaceVariant)) {
                                        append(p2)
                                    }
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Row 4: English Translation (own row, with translation prefix Eng.:)
                val englishMeaning = remember(result.meaningEnglish) {
                    val raw = result.meaningEnglish.trim()
                    when {
                        raw.startsWith("Eng.:", ignoreCase = true) -> raw.substringAfter("Eng.:").trim()
                        raw.startsWith("Eng:", ignoreCase = true) -> raw.substringAfter("Eng:").trim()
                        raw.startsWith("English:", ignoreCase = true) -> raw.substringAfter("English:").trim()
                        else -> raw
                    }
                }
                if (englishMeaning.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verb_translation_row"),
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
                            text = "Eng.: $englishMeaning",
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

        Spacer(modifier = Modifier.height(6.dp))

        // Tense Selector Tabs (Horizontal Scrollable Tabs)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GeoSurfaceVariant)
                .border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 3.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTenseTab.coerceIn(0, (tabTitles.size - 1).coerceAtLeast(0)),
                containerColor = Color.Transparent,
                edgePadding = 8.dp,
                divider = {},
                indicator = { tabPositions ->
                    val safeIdx = selectedTenseTab.coerceIn(0, (tabPositions.size - 1).coerceAtLeast(0))
                    if (tabPositions.isNotEmpty() && safeIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[safeIdx])
                                .clip(RoundedCornerShape(12.dp)),
                            color = GeoPrimary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, tenseName ->
                    val isSelected = selectedTenseTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTenseTab = index },
                        selectedContentColor = GeoOnPrimaryContainer,
                        unselectedContentColor = GeoOnSurfaceVariant,
                        text = {
                            Text(
                                text = tenseName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.testTag("tab_tense_${tenseName.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display selected tense or imperativ or all
        val numTenses = result.tenses.size
        val isImperativTab = selectedTenseTab == numTenses
        val isAlleTab = selectedTenseTab == numTenses + 1

        when {
            isAlleTab -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    result.tenses.forEach { tense ->
                        TenseConjugationCard(
                            title = tense.tenseNameDe,
                            conjugation = tense,
                            onSpeak = onSpeak
                        )
                    }
                    ImperativConjugationCard(
                        imperativ = result.imperativ,
                        onSpeak = onSpeak
                    )
                }
            }
            isImperativTab -> {
                ImperativConjugationCard(
                    imperativ = result.imperativ,
                    onSpeak = onSpeak
                )
            }
            selectedTenseTab in 0 until numTenses -> {
                val tense = result.tenses[selectedTenseTab]
                TenseConjugationCard(
                    title = tense.tenseNameDe,
                    conjugation = tense,
                    onSpeak = onSpeak
                )
            }
        }
    }
}

@Composable
fun TenseConjugationCard(
    title: String,
    conjugation: VerbTenseConjugation,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val persons = listOf(
        "ich" to conjugation.ich,
        "du" to conjugation.du,
        "er / sie / es" to conjugation.erSieEs,
        "wir" to conjugation.wir,
        "ihr" to conjugation.ihr,
        "sie / Sie" to conjugation.sieSie
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tense Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (GeoIsBoldTheme) Modifier.background(Brush.horizontalGradient(GeoBannerGradient))
                        else Modifier.background(GeoPrimaryContainer)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = if (GeoIsBoldTheme) Color.White else GeoOnPrimaryContainer,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Column Headers & Rows with horizontal scroll for long verb forms
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = maxWidth
                val personColWidth = 116.dp
                val maxFormLength = persons.maxOfOrNull { it.second.length } ?: 0
                val calculatedFormWidth = (maxFormLength * 11f + 68f).dp
                val availableFormWidth = (cardWidth - personColWidth - 1.dp).coerceAtLeast(180.dp)
                val formColWidth = maxOf(availableFormWidth, calculatedFormWidth)
                val totalTableWidth = personColWidth + 1.dp + formColWidth

                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.width(totalTableWidth)) {
                        // Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(GeoHeaderKasus)
                                .border(BorderStroke(1.dp, GeoBorder)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(personColWidth)
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Person",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(GeoBorder)
                            )
                            Box(
                                modifier = Modifier
                                    .width(formColWidth)
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Konjugierte Form",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Person Rows
                        persons.forEachIndexed { index, (person, form) ->
                            val isLast = index == persons.size - 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .background(GeoSurface)
                                    .clickable { onSpeak("$person $form") }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DynamicTextView(
                                    text = person,
                                    modifier = Modifier.width(personColWidth - 10.dp),
                                    minTextSize = 10f,
                                    maxTextSize = 15f,
                                    paddingStart = 4.dp,
                                    paddingEnd = 8.dp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(GeoBorder)
                                )
                                Row(
                                    modifier = Modifier
                                        .width(formColWidth)
                                        .padding(start = 10.dp, end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f, fill = false)) {
                                        DynamicTextView(
                                            text = form,
                                            minTextSize = 8f,
                                            maxTextSize = 16f,
                                            paddingStart = 4.dp,
                                            paddingEnd = 8.dp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GeoPrimary,
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEEEEEE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak $form",
                                            tint = Color(0xFF9E9E9E),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            if (!isLast) {
                                HorizontalDivider(thickness = 1.dp, color = GeoBorder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImperativConjugationCard(
    imperativ: VerbImperativ,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        "(du)" to imperativ.du,
        "(ihr)" to imperativ.ihr,
        "(Sie)" to imperativ.sie
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (GeoIsBoldTheme) 1.5.dp else 1.dp,
            if (GeoIsBoldTheme) GeoCardRibbonColor.copy(alpha = 0.55f) else GeoBorder
        ),
        colors = CardDefaults.cardColors(containerColor = GeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (GeoIsBoldTheme) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (GeoIsBoldTheme) Modifier.background(Brush.horizontalGradient(GeoBannerGradient))
                        else Modifier.background(GeoSecondaryContainer)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "IMPERATIV (BEFEHLSFORM)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = if (GeoIsBoldTheme) Color.White else GeoOnSecondaryContainer,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Headers & Imperative rows with horizontal scroll for long forms
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = maxWidth
                val formColHeaderWidth = 116.dp
                val maxFormLength = items.maxOfOrNull { it.second.length } ?: 0
                val calculatedFormWidth = (maxFormLength * 11f + 68f).dp
                val availableFormWidth = (cardWidth - formColHeaderWidth - 1.dp).coerceAtLeast(180.dp)
                val formColWidth = maxOf(availableFormWidth, calculatedFormWidth)
                val totalTableWidth = formColHeaderWidth + 1.dp + formColWidth

                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.width(totalTableWidth)) {
                        // Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(GeoHeaderKasus)
                                .border(BorderStroke(1.dp, GeoBorder)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(formColHeaderWidth)
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Form",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(GeoBorder)
                            )
                            Box(
                                modifier = Modifier
                                    .width(formColWidth)
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Imperativform",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Imperative items
                        items.forEachIndexed { index, (person, form) ->
                            val isLast = index == items.size - 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .background(GeoSurface)
                                    .clickable { onSpeak(form) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DynamicTextView(
                                    text = person,
                                    modifier = Modifier.width(formColHeaderWidth - 10.dp),
                                    minTextSize = 10f,
                                    maxTextSize = 15f,
                                    paddingStart = 4.dp,
                                    paddingEnd = 8.dp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnSurface,
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(GeoBorder)
                                )
                                Row(
                                    modifier = Modifier
                                        .width(formColWidth)
                                        .padding(start = 10.dp, end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f, fill = false)) {
                                        DynamicTextView(
                                            text = form,
                                            minTextSize = 8f,
                                            maxTextSize = 16f,
                                            paddingStart = 4.dp,
                                            paddingEnd = 8.dp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GeoPrimary,
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEEEEEE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak $form",
                                            tint = Color(0xFF9E9E9E),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            if (!isLast) {
                                HorizontalDivider(thickness = 1.dp, color = GeoBorder)
                            }
                        }
                    }
                }
            }
        }
    }
}
