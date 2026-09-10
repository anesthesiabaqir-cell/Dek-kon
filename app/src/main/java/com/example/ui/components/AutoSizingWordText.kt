package com.example.ui.components

import android.util.LruCache
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoOnSurface

/**
 * DynamicTextView:
 * Production-ready auto-sizing single-line text view that conforms to the app-wide
 * dynamic container standard to completely prevent truncation or cut-off text (e.g., "Wem?").
 *
 * App-Wide Rules:
 * 1. Dynamic Width & Height: Uses flexible layout with safe margins and full intrinsic size support.
 * 2. Automatic Text Sizing: Uniform dynamic scaling from [maxTextSize] down to [minTextSize] (8sp..24sp).
 * 3. Single-line: maxLines = 1, softWrap = false.
 * 4. Padding: paddingStart (4dp) and paddingEnd (8dp) to guarantee ample space for trailing characters/punctuation like '?'.
 * 5. Intrinsic Safety: Fully implements intrinsic measurement policies to never crash inside IntrinsicSize.Min/Max rows.
 */
@Composable
fun DynamicTextView(
    text: String,
    modifier: Modifier = Modifier,
    minTextSize: Float = 8f,
    maxTextSize: Float = 24f,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 8.dp,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    textStyle: TextStyle = LocalTextStyle.current
) {
    DynamicTextView(
        annotatedText = AnnotatedString(text),
        modifier = modifier,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        paddingStart = paddingStart,
        paddingEnd = paddingEnd,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        color = color,
        textAlign = textAlign,
        textStyle = textStyle
    )
}

@Composable
fun DynamicTextView(
    annotatedText: AnnotatedString,
    modifier: Modifier = Modifier,
    minTextSize: Float = 8f,
    maxTextSize: Float = 24f,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 8.dp,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    textStyle: TextStyle = LocalTextStyle.current
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val effectiveColor = if (color != Color.Unspecified) color else textStyle.color
    val effectiveFontWeight = fontWeight ?: textStyle.fontWeight
    val effectiveFontStyle = fontStyle ?: textStyle.fontStyle

    val paddingStartPx = with(density) { paddingStart.roundToPx() }
    val paddingEndPx = with(density) { paddingEnd.roundToPx() }
    val totalPaddingPx = paddingStartPx + paddingEndPx
    val extraGlyphMarginPx = with(density) { 4.dp.roundToPx() }

    Layout(
        modifier = modifier
            .semantics { this.text = annotatedText }
            .graphicsLayer { clip = false }
            .drawWithCache {
                val availableWidth = (size.width - totalPaddingPx - extraGlyphMarginPx).coerceAtLeast(10f)
                val bestFontSize = calculateBestFontSize(
                    textMeasurer = textMeasurer,
                    annotatedText = annotatedText,
                    textStyle = textStyle,
                    fontWeight = effectiveFontWeight,
                    fontStyle = effectiveFontStyle,
                    availableWidthPx = availableWidth,
                    minTextSize = minTextSize,
                    maxTextSize = maxTextSize
                )

                val textLayoutResult = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = bestFontSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle,
                        color = effectiveColor,
                        textAlign = textAlign ?: TextAlign.Start
                    ),
                    maxLines = 1,
                    softWrap = false
                )

                val xOffset = when (textAlign) {
                    TextAlign.Center -> paddingStartPx + ((size.width - totalPaddingPx - textLayoutResult.size.width) / 2f).coerceAtLeast(0f)
                    TextAlign.End, TextAlign.Right -> (size.width - paddingEndPx - textLayoutResult.size.width).coerceAtLeast(paddingStartPx.toFloat())
                    else -> paddingStartPx.toFloat()
                }
                val yOffset = ((size.height - textLayoutResult.size.height) / 2f).coerceAtLeast(0f)

                onDrawWithContent {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(xOffset, yOffset)
                    )
                }
            },
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                val availableWidth = if (constraints.hasBoundedWidth) {
                    (constraints.maxWidth.toFloat() - totalPaddingPx - extraGlyphMarginPx).coerceAtLeast(10f)
                } else {
                    Float.MAX_VALUE
                }

                val bestFontSize = if (availableWidth == Float.MAX_VALUE) {
                    maxTextSize
                } else {
                    calculateBestFontSize(
                        textMeasurer = textMeasurer,
                        annotatedText = annotatedText,
                        textStyle = textStyle,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle,
                        availableWidthPx = availableWidth,
                        minTextSize = minTextSize,
                        maxTextSize = maxTextSize
                    )
                }

                val measured = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = bestFontSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle
                    ),
                    maxLines = 1,
                    softWrap = false
                )

                val desiredWidth = measured.size.width + totalPaddingPx + extraGlyphMarginPx
                val width = desiredWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
                val height = measured.size.height.coerceIn(constraints.minHeight, constraints.maxHeight)

                return layout(width, height) {}
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int {
                val measured = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = minTextSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                return measured.size.width + totalPaddingPx + extraGlyphMarginPx
            }

            override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int {
                val measured = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = maxTextSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                return measured.size.width + totalPaddingPx + extraGlyphMarginPx
            }

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                val measured = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = minTextSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                return measured.size.height
            }

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                val measured = textMeasurer.measure(
                    text = annotatedText,
                    style = textStyle.copy(
                        fontSize = maxTextSize.sp,
                        fontWeight = effectiveFontWeight,
                        fontStyle = effectiveFontStyle
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                return measured.size.height
            }
        }
    )
}

private data class FontSizeCacheKey(
    val text: String,
    val widthBucket: Int,
    val minTextSize: Float,
    val maxTextSize: Float,
    val fontWeightWeight: Int?,
    val fontStyleIndex: Int?
)

private val fontSizeLruCache = LruCache<FontSizeCacheKey, Float>(1024)

private fun calculateBestFontSize(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    annotatedText: AnnotatedString,
    textStyle: TextStyle,
    fontWeight: FontWeight?,
    fontStyle: FontStyle?,
    availableWidthPx: Float,
    minTextSize: Float,
    maxTextSize: Float
): Float {
    if (annotatedText.isEmpty() || availableWidthPx <= 0f || availableWidthPx == Float.MAX_VALUE) {
        return maxTextSize
    }

    // Cache lookup using rounded width bucket for high cache hit rate during layout
    val widthBucket = availableWidthPx.toInt()
    val cacheKey = FontSizeCacheKey(
        text = annotatedText.text,
        widthBucket = widthBucket,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        fontWeightWeight = fontWeight?.weight,
        fontStyleIndex = fontStyle?.value
    )

    val cached = fontSizeLruCache.get(cacheKey)
    if (cached != null) {
        return cached
    }

    // Fast-path 1: Check if maxTextSize already fits (avoids 100% of binary search loops for short text)
    val maxMeasured = textMeasurer.measure(
        text = annotatedText,
        style = textStyle.copy(
            fontSize = maxTextSize.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        ),
        maxLines = 1,
        softWrap = false
    )
    if (maxMeasured.size.width <= availableWidthPx) {
        fontSizeLruCache.put(cacheKey, maxTextSize)
        return maxTextSize
    }

    // Fast-path 2: Check if even minTextSize does not fit
    val minMeasured = textMeasurer.measure(
        text = annotatedText,
        style = textStyle.copy(
            fontSize = minTextSize.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        ),
        maxLines = 1,
        softWrap = false
    )
    if (minMeasured.size.width > availableWidthPx) {
        fontSizeLruCache.put(cacheKey, minTextSize)
        return minTextSize
    }

    // Narrowed binary search (step 0.5f) between min and max
    var low = minTextSize
    var high = maxTextSize
    var best = minTextSize

    while (high - low >= 0.5f) {
        val mid = (low + high) / 2f
        val measured = textMeasurer.measure(
            text = annotatedText,
            style = textStyle.copy(
                fontSize = mid.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle
            ),
            maxLines = 1,
            softWrap = false
        )
        if (measured.size.width <= availableWidthPx) {
            best = mid
            low = mid + 0.5f
        } else {
            high = mid - 0.5f
        }
    }
    fontSizeLruCache.put(cacheKey, best)
    return best
}

/**
 * Flexible auto-sizing single-line word view that scales from maxTextSize (24sp)
 * down to minTextSize (8sp) to ensure long German nouns/verbs fit horizontally
 * on a single line without wrapping or ellipsis.
 */
@Composable
fun AutoSizingWordText(
    text: AnnotatedString,
    rawLength: Int = text.length,
    modifier: Modifier = Modifier,
    minTextSize: Float = 8f,
    maxTextSize: Float = 24f,
    defaultColor: Color = GeoOnSurface,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 8.dp
) {
    DynamicTextView(
        annotatedText = text,
        modifier = modifier,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        paddingStart = paddingStart,
        paddingEnd = paddingEnd,
        fontWeight = FontWeight.Bold,
        color = defaultColor
    )
}

@Composable
fun AutoSizingWordText(
    text: String,
    modifier: Modifier = Modifier,
    minTextSize: Float = 8f,
    maxTextSize: Float = 24f,
    color: Color = GeoOnSurface,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 8.dp
) {
    AutoSizingWordText(
        text = AnnotatedString(text),
        rawLength = text.length,
        modifier = modifier,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        defaultColor = color,
        paddingStart = paddingStart,
        paddingEnd = paddingEnd
    )
}
