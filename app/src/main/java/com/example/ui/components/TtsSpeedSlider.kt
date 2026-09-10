package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnSurface
import com.example.ui.theme.GeoOnSurfaceVariant
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoSurfaceVariant
import java.util.Locale
import kotlin.math.round

/**
 * Speech speed control container:
 * Layout:
 * [Icon]  Sprech-Tempo    [=====●=====]   1.0x
 *
 * Requirements:
 * - Location: Above "Suchverlauf", below search field & search button, centered horizontally.
 * - Container: Surrounds entire slider (name + icon + slider bar).
 *   Adapts to app theme (matching general background with slight contrast: GeoSurfaceVariant, subtle GeoBorder).
 *   Rounded corners (12.dp to match other app elements).
 *   Internal padding (8-12dp).
 * - Name: "Sprech-Tempo" (German, two words).
 * - Icon: Speedometer gauge from attached image (ic_playback_speed).
 * - Slider: Horizontal, 0.5x to 2.0x, increments of 0.1, default 1.0x.
 * - Value: Displays current value next to slider (e.g. "1.0x").
 * - Applied across: Home screen (before searching), Noun search results, Verb search results.
 */
@Composable
fun SprechTempoControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(speed) { mutableFloatStateOf(speed) }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GeoBorder),
        colors = CardDefaults.cardColors(
            containerColor = GeoSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.testTag("sprech_tempo_container")
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Speedometer Icon from attached image (placed to the left of the name)
            Icon(
                painter = painterResource(id = R.drawable.ic_playback_speed),
                contentDescription = null,
                tint = GeoPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .testTag("sprech_tempo_icon")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Name in German: Sprech-Tempo
            Text(
                text = stringResource(id = R.string.speech_tempo_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoOnSurface,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.testTag("sprech_tempo_title")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Horizontal Slider: 0.5x to 2.0x, increments of 0.1
            Slider(
                value = sliderValue,
                onValueChange = { raw ->
                    val snapped = (round(raw * 10f) / 10f).coerceIn(0.5f, 2.0f)
                    sliderValue = snapped
                    onSpeedChange(snapped)
                },
                valueRange = 0.5f..2.0f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = GeoPrimary,
                    activeTrackColor = GeoPrimary,
                    inactiveTrackColor = GeoBackground,
                    activeTickColor = GeoPrimary.copy(alpha = 0.5f),
                    inactiveTickColor = GeoOnSurfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .widthIn(min = 90.dp, max = 130.dp)
                    .wrapContentHeight()
                    .testTag("sprech_tempo_slider")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Current value label: 1.0x
            Text(
                text = String.format(Locale.US, "%.1fx", sliderValue),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GeoOnSurface,
                maxLines = 1,
                modifier = Modifier.testTag("sprech_tempo_value")
            )
        }
    }
}

/**
 * Material 3 Playback Speed Slider for controlling German TTS speech rate.
 */
@Composable
fun TtsSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderWidth: androidx.compose.ui.unit.Dp = 200.dp
) {
    var sliderValue by remember(speed) { mutableFloatStateOf(speed) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentHeight()
            .testTag("tts_speed_control_container")
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { raw ->
                val snapped = (round(raw * 10f) / 10f).coerceIn(0.5f, 2.0f)
                sliderValue = snapped
                onSpeedChange(snapped)
            },
            valueRange = 0.5f..2.0f,
            steps = 14,
            colors = SliderDefaults.colors(
                thumbColor = GeoPrimary,
                activeTrackColor = GeoPrimary,
                inactiveTrackColor = GeoSurfaceVariant,
                activeTickColor = GeoPrimary.copy(alpha = 0.5f),
                inactiveTickColor = GeoOnSurfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .width(sliderWidth)
                .wrapContentHeight()
                .testTag("tts_speed_slider")
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = String.format(Locale.US, "%.1fx", sliderValue),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GeoOnSurface,
            modifier = Modifier.testTag("tts_speed_value_label")
        )
    }
}
