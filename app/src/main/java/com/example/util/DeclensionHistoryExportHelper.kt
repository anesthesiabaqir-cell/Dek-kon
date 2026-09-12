package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.WordDeclensionResult
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Generates horizontal, adjacent German noun declension tables for:
 * 1. Microsoft Excel (.xlsx - real native OpenXML workbook with formatted header fill, filters, auto-column widths).
 * 2. Standalone PDF document (Android native PdfDocument in true LANDSCAPE orientation with styled tables and pagination).
 * 3. System Print & PDF Spooler (Android PrintManager via WebView in Landscape orientation).
 * 4. UTF-8 BOM CSV / TSV fallback.
 *
 * Guaranteed data structure:
 * - Each searched word is recorded in a SINGLE continuous row.
 * - Deduplication is enforced (no repeated words).
 * - Exact German adjacent columns in order:
 *   1. Nomen (Singular)
 *   2. Plural
 *   3. English translation
 *   4. Nominativ (Singular / Plural)
 *   5. Akkusativ (Singular / Plural)
 *   6. Genitiv (Singular / Plural)
 *   7. Dativ (Singular / Plural)
 *   8. Ablativ (Singular / Plural)
 */
object DeclensionHistoryExportHelper {

    const val COLOR_HEX_MASKULIN = "#1565C0"
    const val COLOR_HEX_FEMININ = "#C2185B"
    const val COLOR_HEX_NEUTRUM = "#15803D"

    val HEADERS = listOf(
        "Nomen (Singular)",
        "Genus",
        "English translation",
        "Nominativ (Sg./Pl.)",
        "Akkusativ (Sg./Pl.)",
        "Genitiv (Sg./Pl.)",
        "Dativ (Sg./Pl.)"
    )

    val HEADERS_WITH_NR = listOf("Nr.") + HEADERS

    val VERB_HEADERS = listOf(
        "Infinitiv",
        "Hilfsverb",
        "Partizip I",
        "Partizip II",
        "English translation",
        "Präsens",
        "Präteritum",
        "Perfekt",
        "Plusquamperfekt",
        "Futur I",
        "Futur II",
        "Imperativ"
    )

    /**
     * Fixed 8 columns for the Unified Master Verb Table in PDF and Excel:
     * Verb (Infinitiv) | Zeit (Tense) | ich | du | er/sie/es | wir | ihr | sie/Sie
     */
    val VERB_MASTER_HEADERS = listOf(
        "Verb (Infinitiv)",
        "Zeit (Tense)",
        "ich",
        "du",
        "er/sie/es",
        "wir",
        "ihr",
        "sie/Sie"
    )

    val VERB_MASTER_HEADERS_WITH_NR = listOf("Nr.") + VERB_MASTER_HEADERS

    data class ExportTypographyOptions(
        val bodyFontSize: Float = 12f,
        val bodyFontFamily: String = "Arial",
        val bodyFontColorHex: String = "#000000",
        val headerFontSize: Float = 14f,
        val headerFontFamily: String = "Arial",
        val headerFontColorHex: String = "#000000"
    ) {
        companion object {
            val DEFAULT = ExportTypographyOptions()
        }
    }

    val AVAILABLE_FONTS = listOf(
        "Arial",
        "Times New Roman",
        "Helvetica",
        "Courier New",
        "Verdana",
        "Calibri",
        "Georgia",
        "Tahoma",
        "Trebuchet MS",
        "Palatino Linotype"
    )

    val PREDEFINED_FONT_SIZES = listOf(
        8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 22f, 24f, 26f, 28f, 36f, 48f, 72f
    )

    val PREDEFINED_COLORS = listOf(
        "Schwarz" to "#000000",
        "Weiß" to "#FFFFFF",
        "Rot" to "#FF0000",
        "Grün" to "#00FF00",
        "Blau" to "#0000FF",
        "Gelb" to "#FFFF00",
        "Violett" to "#800080",
        "Indigo" to "#4B0082",
        "Orange" to "#FFA500",
        "Grau" to "#808080",
        "Marineblau" to "#000080",
        "Braun" to "#A52A2A",
        "Rosa" to "#FFC0CB",
        "Türkis" to "#40E0D0",
        "Kupfer" to "#B87333",
        "Kastanienbraun" to "#800000",
        "Olivgrün" to "#808000",
        "Hellblau" to "#00FFFF",
        "Hellviolett" to "#8A2BE2",
        "Ziegelrot" to "#B22222"
    )

    fun parseHexColor(hex: String, fallback: Int = (0xFF shl 24) or (15 shl 16) or (23 shl 8) or 42): Int {
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> {
                    val r = clean.substring(0, 2).toInt(16)
                    val g = clean.substring(2, 4).toInt(16)
                    val b = clean.substring(4, 6).toInt(16)
                    (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
                8 -> clean.toLong(16).toInt()
                3 -> {
                    val r = clean.substring(0, 1).repeat(2).toInt(16)
                    val g = clean.substring(1, 2).repeat(2).toInt(16)
                    val b = clean.substring(2, 3).repeat(2).toInt(16)
                    (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    fun toExcelRgbHex(hex: String, defaultColor: String = "FF0F172A"): String {
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> "FF" + clean.uppercase(java.util.Locale.US)
                8 -> clean.uppercase(java.util.Locale.US)
                3 -> {
                    val r = clean[0]
                    val g = clean[1]
                    val b = clean[2]
                    "FF$r$r$g$g$b$b".uppercase(java.util.Locale.US)
                }
                else -> defaultColor
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    fun createSafeTypeface(fontFamily: String, style: Int): android.graphics.Typeface {
        return try {
            val clean = fontFamily.trim()
            val lower = clean.lowercase(java.util.Locale.US)
            val baseTypeface = when {
                lower.contains("times") || lower.contains("georgia") || lower.contains("palatino") || lower == "serif" ->
                    android.graphics.Typeface.create(android.graphics.Typeface.SERIF, style)
                lower.contains("courier") || lower == "monospace" ->
                    android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, style)
                lower.contains("tahoma") || lower.contains("condensed") ->
                    android.graphics.Typeface.create("sans-serif-condensed", style)
                lower.contains("calibri") || lower.contains("light") ->
                    android.graphics.Typeface.create("sans-serif-light", style)
                lower.contains("helvetica") || lower.contains("medium") ->
                    android.graphics.Typeface.create("sans-serif-medium", style)
                else ->
                    android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, style)
            }
            val tf = android.graphics.Typeface.create(clean, style)
            if (tf != null && tf != android.graphics.Typeface.DEFAULT && tf != android.graphics.Typeface.SANS_SERIF) {
                tf
            } else {
                baseTypeface
            }
        } catch (_: Exception) {
            android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, style)
        }
    }

    /**
     * Paper size options with standardized dimensions (in PostScript points for PDF)
     * and dynamic font sizes according to approved specifications:
     * - A3 / Letter Landscape / A4 Landscape: 10pt - 11pt
     * - A4 Portrait / Letter Portrait: 8pt - 9pt
     * - A5 (any orientation): 6pt - 7pt
     */
    enum class PaperSizeOption(
        val label: String,
        val widthMm: Int,
        val heightMm: Int,
        val isLandscape: Boolean,
        val defaultFontSizePt: Float,
        val headerFontSizePt: Float
    ) {
        A3_LANDSCAPE("A3 (Querformat)", 420, 297, true, 10.5f, 11.5f),
        A3_PORTRAIT("A3 (Hochformat)", 297, 420, false, 9.5f, 10.5f),
        A4_LANDSCAPE("A4 (Querformat)", 297, 210, true, 10.0f, 11.0f),
        A4_PORTRAIT("A4 (Hochformat)", 210, 297, false, 8.5f, 9.5f),
        A5_LANDSCAPE("A5 (Querformat)", 210, 148, true, 7.0f, 8.0f),
        A5_PORTRAIT("A5 (Hochformat)", 148, 210, false, 6.5f, 7.5f),
        LETTER_LANDSCAPE("Letter (Querformat)", 279, 216, true, 10.0f, 11.0f),
        LETTER_PORTRAIT("Letter (Hochformat)", 216, 279, false, 8.5f, 9.5f);

        val widthPt: Int get() = when (this) {
            A3_LANDSCAPE -> 1191
            A3_PORTRAIT -> 842
            A4_LANDSCAPE -> 842
            A4_PORTRAIT -> 595
            A5_LANDSCAPE -> 595
            A5_PORTRAIT -> 420
            LETTER_LANDSCAPE -> 792
            LETTER_PORTRAIT -> 612
        }

        val heightPt: Int get() = when (this) {
            A3_LANDSCAPE -> 842
            A3_PORTRAIT -> 1191
            A4_LANDSCAPE -> 595
            A4_PORTRAIT -> 842
            A5_LANDSCAPE -> 420
            A5_PORTRAIT -> 595
            LETTER_LANDSCAPE -> 612
            LETTER_PORTRAIT -> 792
        }

        companion object {
            val DEFAULT = A4_PORTRAIT
        }
    }

    enum class ExportScope(val displayName: String) {
        NOMEN("Nomen"),
        VERB("Verb"),
        ALLE("Alle");

        companion object {
            fun fromString(str: String?): ExportScope = when (str?.trim()?.lowercase()) {
                "verb", "verben" -> VERB
                "alle", "all" -> ALLE
                else -> NOMEN
            }
        }
    }

    data class WordExportRow(
        val nomenSingular: String,
        val genus: String,
        val englishTranslation: String,
        val nominativ: String,
        val akkusativ: String,
        val genitiv: String,
        val dativ: String
    ) {
        fun toList(): List<String> = listOf(
            nomenSingular,
            genus,
            englishTranslation,
            nominativ,
            akkusativ,
            genitiv,
            dativ
        )
    }

    data class VerbExportRow(
        val infinitiv: String,
        val hilfsverb: String,
        val partizip1: String,
        val partizip2: String,
        val englishTranslation: String,
        val praesens: String,
        val praeteritum: String,
        val perfekt: String,
        val plusquamperfekt: String,
        val futur1: String,
        val futur2: String,
        val imperativ: String
    ) {
        fun toList(): List<String> = listOf(
            infinitiv,
            hilfsverb,
            partizip1,
            partizip2,
            englishTranslation,
            praesens,
            praeteritum,
            perfekt,
            plusquamperfekt,
            futur1,
            futur2,
            imperativ
        )
    }

    data class VerbMasterRow(
        val infinitiv: String,
        val zeit: String,
        val ich: String,
        val du: String,
        val erSieEs: String,
        val wir: String,
        val ihr: String,
        val sieSie: String,
        val isLastRowOfVerb: Boolean = false
    ) {
        fun toList(): List<String> = listOf(
            infinitiv,
            zeit,
            ich,
            du,
            erSieEs,
            wir,
            ihr,
            sieSie
        )
    }

    /**
     * Builds the unified master verb table rows according to approved specifications:
     * - Deduplication: Duplicate searches of a verb appear only once.
     * - Order: Chronological by first search timestamp (oldest first).
     * - 7 rows per verb: Präsens, Präteritum, Perfekt, Plusquamperfekt, Futur I, Futur II, Imperativ.
     * - Pronouns: Clean conjugated forms matching: ich, du, er/sie/es, wir, ihr, sie/Sie.
     * - Imperativ: du form, ihr form, sie form ("... Sie"). If missing, shows em dash "—".
     */
    fun buildMasterVerbRows(verbs: List<com.example.data.model.VerbConjugationResult>): List<VerbMasterRow> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<VerbMasterRow>()

        for (v in verbs) {
            val key = v.word.trim().lowercase(java.util.Locale.GERMAN)
            if (key.isEmpty() || !seen.add(key)) continue

            val inf = v.infinitiv.ifBlank { v.word }

            fun cleanPronoun(raw: String, pronoun: String): String {
                val t = raw.trim()
                if (t.isBlank() || t == "-") return "–"
                if (t.startsWith("$pronoun ", ignoreCase = true)) {
                    val sub = t.substring(pronoun.length + 1).trim()
                    return if (sub.isBlank()) "–" else sub
                }
                return t
            }

            fun getTenseRow(tenseKey: String, tenseNameDe: String): VerbMasterRow {
                val t = v.tenses.firstOrNull { it.tenseKey.equals(tenseKey, ignoreCase = true) }
                return VerbMasterRow(
                    infinitiv = inf,
                    zeit = tenseNameDe,
                    ich = cleanPronoun(t?.ich.orEmpty(), "ich"),
                    du = cleanPronoun(t?.du.orEmpty(), "du"),
                    erSieEs = cleanPronoun(t?.erSieEs.orEmpty(), "er/sie/es").let {
                        if (it == "–" && t?.erSieEs?.isNotBlank() == true) t.erSieEs.trim() else it
                    },
                    wir = cleanPronoun(t?.wir.orEmpty(), "wir"),
                    ihr = cleanPronoun(t?.ihr.orEmpty(), "ihr"),
                    sieSie = cleanPronoun(t?.sieSie.orEmpty(), "sie/Sie").let {
                        if (it == "–" && t?.sieSie?.isNotBlank() == true) t.sieSie.trim() else it
                    }
                )
            }

            result.add(getTenseRow("praesens", "Präsens"))
            result.add(getTenseRow("praeteritum", "Präteritum"))
            result.add(getTenseRow("perfekt", "Perfekt"))
            result.add(getTenseRow("plusquamperfekt", "Plusquamperfekt"))
            result.add(getTenseRow("futur1", "Futur I"))
            result.add(getTenseRow("futur2", "Futur II"))

            val rawDu = v.imperativ.du.trim()
            val rawIhr = v.imperativ.ihr.trim()
            val rawSie = v.imperativ.sie.trim()

            val isMissing = (rawDu.isBlank() || rawDu == "-") &&
                            (rawIhr.isBlank() || rawIhr == "-") &&
                            (rawSie.isBlank() || rawSie == "-")

            val cleanDu = if (rawDu.isBlank() || rawDu == "-") "—" else rawDu
            val cleanIhr = if (rawIhr.isBlank() || rawIhr == "-") "—" else rawIhr
            val cleanSie = when {
                rawSie.isBlank() || rawSie == "-" -> "—"
                rawSie.contains("Sie", ignoreCase = true) -> rawSie
                else -> "$rawSie Sie"
            }

            result.add(
                VerbMasterRow(
                    infinitiv = inf,
                    zeit = "Imperativ",
                    ich = "—",
                    du = if (isMissing) "—" else cleanDu,
                    erSieEs = "—",
                    wir = "—",
                    ihr = if (isMissing) "—" else cleanIhr,
                    sieSie = if (isMissing) "—" else cleanSie,
                    isLastRowOfVerb = true
                )
            )
        }
        return result
    }

    fun buildDeduplicatedVerbRows(verbs: List<com.example.data.model.VerbConjugationResult>): List<VerbExportRow> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<VerbExportRow>()

        for (v in verbs) {
            val key = v.word.trim().lowercase(java.util.Locale.GERMAN)
            if (key.isEmpty() || !seen.add(key)) continue

            fun formatTense(tenseKey: String): String {
                val t = v.tenses.firstOrNull { it.tenseKey == tenseKey } ?: return "-"
                return "ich ${t.ich}, du ${t.du}, er ${t.erSieEs}, wir ${t.wir}, ihr ${t.ihr}, sie ${t.sieSie}"
            }

            val imperativ = "du ${v.imperativ.du} / ihr ${v.imperativ.ihr} / Sie ${v.imperativ.sie}".trim(' ', '/')

            result.add(
                VerbExportRow(
                    infinitiv = v.infinitiv.ifBlank { v.word },
                    hilfsverb = v.hilfsverb,
                    partizip1 = v.partizip1,
                    partizip2 = v.partizip2,
                    englishTranslation = cleanEnglishTranslation(v.meaningEnglish),
                    praesens = formatTense("praesens"),
                    praeteritum = formatTense("praeteritum"),
                    perfekt = formatTense("perfekt"),
                    plusquamperfekt = formatTense("plusquamperfekt"),
                    futur1 = formatTense("futur1"),
                    futur2 = formatTense("futur2"),
                    imperativ = imperativ.ifBlank { "-" }
                )
            )
        }
        return result
    }

    /**
     * Deduplicates the input words by base noun (case-insensitive) and maps each word
     * to a single continuous horizontal row conforming to the 8 required columns (no Ablativ).
     * Order preserved by first search time (oldest first).
     */
    fun buildDeduplicatedRows(words: List<WordDeclensionResult>): List<WordExportRow> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<WordExportRow>()

        for (w in words) {
            val normalizedKey = w.word.trim().lowercase(java.util.Locale.GERMAN)
            if (normalizedKey.isEmpty() || !seen.add(normalizedKey)) {
                continue // Skip duplicates to ensure each searched word appears exactly once
            }

            // Nomen (Singular) with article, e.g. "das Schloss", "der Aufzug"
            val nomenSingular = "${w.genderArticle} ${w.word}".trim()

            // Plural full form, e.g. "die Schlösser", "die Aufzüge"
            val pluralForm = if (w.pluralNoun.isNotBlank()) {
                w.pluralNoun
            } else {
                val nomPlural = w.plural.rows.firstOrNull { it.caseKey == "nominativ" }?.definite
                if (!nomPlural.isNullOrBlank()) nomPlural else "die ${w.word}"
            }

            // English translation: remove any Arabic characters to strictly guarantee German/English only
            val translation = cleanEnglishTranslation(w.meaningEnglish)

            // Helper to format: "Singular / Plural" (using definite forms or explicit values)
            fun formatCase(caseKey: String): String {
                val singRow = w.singular.rows.firstOrNull { it.caseKey == caseKey }
                val plurRow = w.plural.rows.firstOrNull { it.caseKey == caseKey }

                val singDef = singRow?.definite?.trim().orEmpty()
                val plurDef = plurRow?.definite?.trim().orEmpty()

                return when {
                    singDef.isNotEmpty() && plurDef.isNotEmpty() -> "$singDef / $plurDef"
                    singDef.isNotEmpty() -> singDef
                    plurDef.isNotEmpty() -> "- / $plurDef"
                    else -> "-"
                }
            }

            val nominativ = formatCase("nominativ")
            val akkusativ = formatCase("akkusativ")
            val genitiv = formatCase("genitiv")
            val dativ = formatCase("dativ")

            // Normalized German Genus (Maskulin, Feminin, Neutrum)
            val genus = when (w.gender.trim().lowercase(java.util.Locale.GERMAN)) {
                "maskulin", "m", "masculine", "der" -> "Maskulin"
                "feminin", "f", "feminine", "die" -> "Feminin"
                "neutrum", "n", "neuter", "das" -> "Neutrum"
                else -> when (w.genderArticle.trim().lowercase(java.util.Locale.GERMAN)) {
                    "der" -> "Maskulin"
                    "die" -> "Feminin"
                    "das" -> "Neutrum"
                    else -> w.gender.trim().ifBlank { "-" }
                }
            }

            result.add(
                WordExportRow(
                    nomenSingular = nomenSingular,
                    genus = genus,
                    englishTranslation = translation,
                    nominativ = nominativ,
                    akkusativ = akkusativ,
                    genitiv = genitiv,
                    dativ = dativ
                )
            )
        }

        return result
    }

    private fun cleanEnglishTranslation(raw: String): String {
        // Strip Arabic script characters to enforce German & English only
        val noArabic = raw.replace(Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF]"), "").trim()
        return noArabic.trim(' ', ',', '/', '-')
    }

    /**
     * Generates a genuine native Microsoft Excel (.xlsx) OpenXML zip archive.
     * Features:
     * - Header background #D9D9D9 with bold 11pt black font and left alignment.
     * - Alternating zebra data rows: Row 1 #FFFFFF, Row 2 #F2F2F2, font 10pt regular, left-aligned.
     * - Thin 0.5pt gray gridlines between all cells.
     * - No text wrapping: all content remains strictly single-line.
     * - 8 columns: Nomen (Singular), Plural, English translation, Nominativ, Akkusativ, Genitiv, Dativ, Genus.
     * - Auto-filter enabled across all 8 columns.
     */
    fun createXlsxFile(
        file: File,
        words: List<WordDeclensionResult>,
        documentTitle: String = "Suchverlauf_Deutsch",
        typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val rows = buildDeduplicatedRows(words)
        val cleanTitle = documentTitle.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }
        val numberedRows = rows.mapIndexed { idx, row ->
            listOf((idx + 1).toString()) + row.toList()
        }

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            // [Content_Types].xml
            addZipEntry(
                zos,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>"""
            )

            // _rels/.rels
            addZipEntry(
                zos,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
            )

            // docProps/core.xml
            addZipEntry(
                zos,
                "docProps/core.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>${escapeXml(cleanTitle)}</dc:title>
  <dc:creator>Deklination &amp; Konjugation App</dc:creator>
</cp:coreProperties>"""
            )

            // docProps/app.xml
            addZipEntry(
                zos,
                "docProps/app.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>Deklination &amp; Konjugation Android</Application>
</Properties>"""
            )

            // xl/_rels/workbook.xml.rels
            addZipEntry(
                zos,
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            )

            // xl/workbook.xml
            addZipEntry(
                zos,
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="${escapeXml(safeSheetName(cleanTitle))}" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
            )

            // xl/styles.xml
            addZipEntry(zos, "xl/styles.xml", getStandardStylesXml(typography))

            // xl/worksheets/sheet1.xml
            val sheetXml = buildWorksheetXml(HEADERS_WITH_NR, numberedRows, cleanTitle, typography, notebookName)
            addZipEntry(zos, "xl/worksheets/sheet1.xml", sheetXml)
        }
    }

    /**
     * Sanitizes a string for use as an Excel worksheet name (max 31 chars, no illegal characters).
     */
    fun safeSheetName(rawName: String, fallback: String = "Suchverlauf"): String {
        val cleaned = rawName
            .replace(Regex("""[\\/?*\[\]:]"""), "_")
            .trim(' ', '_', '\t', '\n', '\r', '.')
        val truncated = if (cleaned.length > 31) cleaned.substring(0, 31).trimEnd('_', '.') else cleaned
        return if (truncated.isBlank()) fallback else truncated
    }

    /**
     * Generates an Excel workbook containing Nouns, Verbs, or both (multi-sheet: "Nomen" & "Verben").
     */
    fun createUnifiedXlsxFile(
        file: File,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: ExportScope,
        documentTitle: String = "Suchverlauf_Deutsch",
        typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val cleanTitle = documentTitle.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }

        if (scope == ExportScope.NOMEN) {
            createXlsxFile(file, nouns, cleanTitle, typography, notebookName)
            return
        }

        val nounRows = if (scope != ExportScope.VERB) buildDeduplicatedRows(nouns) else emptyList()
        val verbRows = if (scope != ExportScope.NOMEN) buildMasterVerbRows(verbs) else emptyList()

        val numberedNounRows = nounRows.mapIndexed { idx, row ->
            listOf((idx + 1).toString()) + row.toList()
        }
        val numberedVerbRows = verbRows.mapIndexed { idx, row ->
            val nr = if (idx % 7 == 0) ((idx / 7) + 1).toString() else ""
            listOf(nr) + row.toList()
        }

        if (scope == ExportScope.VERB) {
            // Single sheet for Verbs (Master Table: 9 columns with Nr.)
            val rowCount = numberedVerbRows.size + 1
            val dimensionRef = "A1:I${maxOf(rowCount, 2)}"

            ZipOutputStream(FileOutputStream(file)).use { zos ->
                addZipEntry(zos, "[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>""")

                addZipEntry(zos, "_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>""")

                addZipEntry(zos, "docProps/core.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>${escapeXml(cleanTitle)}</dc:title>
  <dc:creator>Deklination &amp; Konjugation App</dc:creator>
</cp:coreProperties>""")

                addZipEntry(zos, "docProps/app.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>Deklination &amp; Konjugation Android</Application>
</Properties>""")

                addZipEntry(zos, "xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")

                addZipEntry(zos, "xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="${escapeXml(safeSheetName(cleanTitle))}" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""")

                addZipEntry(zos, "xl/styles.xml", getStandardStylesXml(typography))

                val sheetContent = buildWorksheetXml(VERB_MASTER_HEADERS_WITH_NR, numberedVerbRows, cleanTitle, typography, notebookName)
                addZipEntry(zos, "xl/worksheets/sheet1.xml", sheetContent)
            }
            return
        }

        // Scope == ALLE: Multi-sheet (Sheet 1: Nomen, Sheet 2: Verben)
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            addZipEntry(zos, "[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>""")

            addZipEntry(zos, "_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>""")

            addZipEntry(zos, "docProps/core.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>${escapeXml(cleanTitle)}</dc:title>
  <dc:creator>Deklination &amp; Konjugation App</dc:creator>
</cp:coreProperties>""")

            addZipEntry(zos, "docProps/app.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>Deklination &amp; Konjugation Android</Application>
</Properties>""")

            addZipEntry(zos, "xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")

            val sheet1Name = safeSheetName("${cleanTitle}_Nomen", "Nomen")
            val sheet2Name = safeSheetName("${cleanTitle}_Verben", "Verben")

            addZipEntry(zos, "xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="${escapeXml(sheet1Name)}" sheetId="1" r:id="rId1"/>
    <sheet name="${escapeXml(sheet2Name)}" sheetId="2" r:id="rId2"/>
  </sheets>
</workbook>""")

            addZipEntry(zos, "xl/styles.xml", getStandardStylesXml(typography))

            // Sheet 1: Nomen
            val sheet1Xml = buildWorksheetXml(HEADERS_WITH_NR, numberedNounRows, cleanTitle, typography, notebookName)
            addZipEntry(zos, "xl/worksheets/sheet1.xml", sheet1Xml)

            // Sheet 2: Verben
            val sheet2Xml = buildWorksheetXml(VERB_MASTER_HEADERS_WITH_NR, numberedVerbRows, cleanTitle, typography, notebookName)
            addZipEntry(zos, "xl/worksheets/sheet2.xml", sheet2Xml)
        }
    }

    private fun getColLetter(index: Int): String {
        var temp = index
        val sb = StringBuilder()
        while (temp >= 0) {
            sb.append(('A'.code + (temp % 26)).toChar())
            temp = temp / 26 - 1
        }
        return sb.reverse().toString()
    }

    private fun getStandardStylesXml(typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT): String {
        val bodySz = if (typography.bodyFontSize % 1.0f == 0f) "${typography.bodyFontSize.toInt()}" else "${typography.bodyFontSize}"
        val headerSz = if (typography.headerFontSize % 1.0f == 0f) "${typography.headerFontSize.toInt()}" else "${typography.headerFontSize}"
        val titleSz = if ((typography.headerFontSize + 2f) % 1.0f == 0f) "${(typography.headerFontSize + 2f).toInt()}" else "${typography.headerFontSize + 2f}"
        val registerSz = if (typography.headerFontSize % 1.0f == 0f) "${typography.headerFontSize.toInt()}" else "${typography.headerFontSize}"
        val bodyColorRgb = toExcelRgbHex(typography.bodyFontColorHex, "FF0F172A")
        val headerColorRgb = toExcelRgbHex(typography.headerFontColorHex, "FF0F172A")
        val bodyFontName = escapeXml(typography.bodyFontFamily)
        val headerFontName = escapeXml(typography.headerFontFamily)

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="4">
    <font>
      <sz val="$bodySz"/>
      <color rgb="$bodyColorRgb"/>
      <name val="$bodyFontName"/>
    </font>
    <font>
      <b/>
      <sz val="$headerSz"/>
      <color rgb="$headerColorRgb"/>
      <name val="$headerFontName"/>
    </font>
    <font>
      <b/>
      <sz val="$titleSz"/>
      <color rgb="$headerColorRgb"/>
      <name val="$headerFontName"/>
    </font>
    <font>
      <b/>
      <sz val="$registerSz"/>
      <color rgb="$headerColorRgb"/>
      <name val="$headerFontName"/>
    </font>
    <!-- 4: Maskulin Font (Blue #1565C0) -->
    <font>
      <b/>
      <sz val="$bodySz"/>
      <color rgb="FF1565C0"/>
      <name val="$bodyFontName"/>
    </font>
    <!-- 5: Feminin Font (Pink/Magenta #C2185B) -->
    <font>
      <b/>
      <sz val="$bodySz"/>
      <color rgb="FFC2185B"/>
      <name val="$bodyFontName"/>
    </font>
    <!-- 6: Neutrum Font (Green #15803D) -->
    <font>
      <b/>
      <sz val="$bodySz"/>
      <color rgb="FF15803D"/>
      <name val="$bodyFontName"/>
    </font>
  </fonts>
  <fills count="4">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill>
      <patternFill patternType="solid">
        <fgColor rgb="FFD9D9D9"/>
      </patternFill>
    </fill>
    <fill>
      <patternFill patternType="solid">
        <fgColor rgb="FFF2F2F2"/>
      </patternFill>
    </fill>
  </fills>
  <borders count="3">
    <border><left/><right/><top/><bottom/><diagonal/></border>
    <border>
      <left style="thin"><color rgb="FFD0D0D0"/></left>
      <right style="thin"><color rgb="FFD0D0D0"/></right>
      <top style="thin"><color rgb="FFD0D0D0"/></top>
      <bottom style="thin"><color rgb="FFD0D0D0"/></bottom>
    </border>
    <border>
      <left style="thin"><color rgb="FFB0B0B0"/></left>
      <right style="thin"><color rgb="FFB0B0B0"/></right>
      <top style="thin"><color rgb="FFB0B0B0"/></top>
      <bottom style="medium"><color rgb="FF505050"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="12">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 4: Title Left (s=4) -->
    <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 5: Register Name alongside Title (s=5) -->
    <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 6: Maskulin White (s=6) -->
    <xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 7: Maskulin Zebra (s=7) -->
    <xf numFmtId="0" fontId="4" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 8: Feminin White (s=8) -->
    <xf numFmtId="0" fontId="5" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 9: Feminin Zebra (s=9) -->
    <xf numFmtId="0" fontId="5" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 10: Neutrum White (s=10) -->
    <xf numFmtId="0" fontId="6" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
    <!-- 11: Neutrum Zebra (s=11) -->
    <xf numFmtId="0" fontId="6" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="0"/>
    </xf>
  </cellXfs>
</styleSheet>"""
    }

    private fun buildWorksheetXml(
        headers: List<String>,
        rows: List<List<String>>,
        documentTitle: String,
        typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ): String {
        val colCount = headers.size
        val rowCount = rows.size + 2 // Row 1 = Title, Row 2 = Header, Row 3..N = Data
        val lastColLetter = getColLetter(colCount - 1)
        val dimensionRef = "A1:${lastColLetter}${maxOf(rowCount, 3)}"

        val cleanNotebook = notebookName.trim()
        val displayNotebook = if (cleanNotebook.isBlank()) {
            ""
        } else if (cleanNotebook.startsWith("Notizbuch", ignoreCase = true)) {
            cleanNotebook
        } else {
            "Notizbuch: $cleanNotebook"
        }

        val headerCharFactor = (typography.headerFontSize / 11.0).coerceAtLeast(0.7) * 1.25
        val bodyCharFactor = (typography.bodyFontSize / 11.0).coerceAtLeast(0.7) * 1.15
        val colWidths = DoubleArray(colCount) { 16.0 }
        headers.forEachIndexed { i, h ->
            colWidths[i] = maxOf(colWidths[i], h.length * headerCharFactor + 3.0)
        }
        for (r in rows) {
            for (i in 0 until minOf(colCount, r.size)) {
                colWidths[i] = maxOf(colWidths[i], r[i].length * bodyCharFactor + 2.0)
            }
        }
        for (i in 0 until colCount) {
            val minW = if (i == 0 && headers.getOrNull(0) == "Nr.") maxOf(6.0, typography.bodyFontSize * 0.55) else maxOf(14.0, typography.bodyFontSize * 1.25)
            colWidths[i] = colWidths[i].coerceIn(minW, 80.0)
        }
        if (colCount > 0) {
            colWidths[0] = maxOf(colWidths[0], (documentTitle.length * headerCharFactor * 0.95) + 3.0)
        }
        if (displayNotebook.isNotBlank() && colCount > 1) {
            colWidths[1] = maxOf(colWidths[1], (displayNotebook.length * headerCharFactor * 0.95) + 3.0)
        }

        val titleRowHt = maxOf(28, (typography.headerFontSize * 2.2).toInt())
        val headerRowHt = maxOf(26, (typography.headerFontSize * 2.0).toInt())
        val dataRowHt = maxOf(22, (typography.bodyFontSize * 1.9).toInt())

        val sheetBuilder = StringBuilder()
        sheetBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <dimension ref="$dimensionRef"/>
  <sheetViews>
    <sheetView tabSelected="1" workbookViewId="0">
      <pane ySplit="2" topLeftCell="A3" activePane="bottomLeft" state="frozen"/>
    </sheetView>
  </sheetViews>
  <sheetFormatPr defaultRowHeight="20" customHeight="1"/>
  <cols>
""")
        for (i in 0 until colCount) {
            val colIndex = i + 1
            val widthFormatted = String.format(java.util.Locale.US, "%.1f", colWidths[i])
            sheetBuilder.append("""    <col min="$colIndex" max="$colIndex" width="$widthFormatted" customWidth="1"/>
""")
        }
        sheetBuilder.append("""  </cols>
  <sheetData>
""")

        // Row 1: Document Title in top-left cell A1, and Notizbuch Name alongside it in B1
        sheetBuilder.append("""    <row r="1" ht="$titleRowHt" customHeight="1">
      <c r="A1" s="4" t="inlineStr"><is><t>${escapeXml(documentTitle)}</t></is></c>
""")
        if (displayNotebook.isNotBlank()) {
            sheetBuilder.append("""      <c r="B1" s="5" t="inlineStr"><is><t>${escapeXml(displayNotebook)}</t></is></c>
""")
        }
        sheetBuilder.append("""    </row>
""")

        // Row 2: Header Row
        sheetBuilder.append("""    <row r="2" ht="$headerRowHt" customHeight="1">
""")
        headers.forEachIndexed { i, h ->
            val cellRef = "${getColLetter(i)}2"
            sheetBuilder.append("""      <c r="$cellRef" s="1" t="inlineStr"><is><t>${escapeXml(h)}</t></is></c>
""")
        }
        sheetBuilder.append("""    </row>
""")

        val genusColIndex = headers.indexOf("Genus")

        // Rows 3+: Data Rows (applying styleId to all cells across all columns)
        rows.forEachIndexed { rowIndex, rowData ->
            val rNum = rowIndex + 3
            val isZebra = rowIndex % 2 == 1
            val defaultStyleId = if (isZebra) 3 else 2

            sheetBuilder.append("""    <row r="$rNum" ht="$dataRowHt" customHeight="1">
""")
            for (cIndex in 0 until colCount) {
                val cellRef = "${getColLetter(cIndex)}$rNum"
                val rawVal = rowData.getOrElse(cIndex) { "" }
                val textVal = escapeXml(rawVal)

                val styleId = if (cIndex == genusColIndex && genusColIndex >= 0) {
                    val g = rawVal.trim().lowercase(java.util.Locale.ROOT)
                    when {
                        g.startsWith("maskulin") || g == "der" || g == "m" -> if (isZebra) 7 else 6
                        g.startsWith("feminin") || g == "die" || g == "f" -> if (isZebra) 9 else 8
                        g.startsWith("neutrum") || g == "das" || g == "n" -> if (isZebra) 11 else 10
                        else -> defaultStyleId
                    }
                } else {
                    defaultStyleId
                }

                sheetBuilder.append("""      <c r="$cellRef" s="$styleId" t="inlineStr"><is><t>$textVal</t></is></c>
""")
            }
            sheetBuilder.append("""    </row>
""")
        }

        sheetBuilder.append("""  </sheetData>
  <autoFilter ref="A2:${lastColLetter}${maxOf(rows.size + 2, 2)}"/>
</worksheet>""")

        return sheetBuilder.toString()
    }

    /**
     * Generates landscape PDF for Nouns conforming to PaperSizeOption.
     */
    fun createPdfFile(
        file: File,
        words: List<WordDeclensionResult>,
        documentTitle: String = "Suchverlauf_Deutsch",
        paperSize: PaperSizeOption = PaperSizeOption.DEFAULT,
        typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        createUnifiedPdfFile(
            file = file,
            nouns = words,
            verbs = emptyList(),
            scope = ExportScope.NOMEN,
            documentTitle = documentTitle,
            paperSize = paperSize,
            typography = typography,
            notebookName = notebookName
        )
    }

    data class DynamicRow(
        val cells: List<List<String>>, // lines for each cell in this row
        val rowHeight: Float,
        val isLastRowOfGroup: Boolean = false
    )

    data class DynamicTableLayout(
        val columnWidths: FloatArray,
        val effectiveBodyFontSize: Float,
        val effectiveHeaderFontSize: Float,
        val headerRow: DynamicRow,
        val rows: List<DynamicRow>
    )

    /**
     * Splits cell text into multiple lines breaking strictly at word boundaries (whitespace),
     * or at hyphen/slash delimiters for compound tokens, ensuring lines fit within availableWidth.
     * Uses ellipsis ("…") only as an absolute last resort if an unbreakable single word exceeds the column width.
     */
    fun wrapTextToLines(
        text: String,
        paint: Paint,
        availableWidth: Float
    ): List<String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return listOf("")
        if (availableWidth <= 0f) return listOf(trimmed)

        // If the entire text fits on one line, preserve it as a single line
        if (paint.measureText(trimmed) <= availableWidth) {
            return listOf(trimmed)
        }

        // Split by whitespace into individual words
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return listOf(trimmed)

        val lines = mutableListOf<String>()
        val currentLine = StringBuilder()

        fun flushCurrentLine() {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine.setLength(0)
            }
        }

        for (word in words) {
            if (currentLine.isEmpty()) {
                val wordW = paint.measureText(word)
                if (wordW <= availableWidth) {
                    currentLine.append(word)
                } else {
                    // Single word exceeds available width. Check if it has internal slashes or hyphens to split
                    if (word.contains('/') || word.contains('-')) {
                        val subParts = word.split(Regex("(?<=[/-])|(?=[/-])")).filter { it.isNotEmpty() }
                        var subBuffer = StringBuilder()
                        for (part in subParts) {
                            val candidate = if (subBuffer.isEmpty()) part else "$subBuffer$part"
                            if (paint.measureText(candidate) <= availableWidth) {
                                subBuffer.append(part)
                            } else {
                                if (subBuffer.isNotEmpty()) {
                                    lines.add(subBuffer.toString())
                                    subBuffer = StringBuilder()
                                }
                                if (paint.measureText(part) <= availableWidth) {
                                    subBuffer.append(part)
                                } else {
                                    // Truncate only as absolute last resort
                                    var truncated = part
                                    while (truncated.isNotEmpty() && paint.measureText("$truncated…") > availableWidth) {
                                        truncated = truncated.dropLast(1)
                                    }
                                    lines.add(if (truncated.isNotEmpty()) "$truncated…" else part)
                                }
                            }
                        }
                        if (subBuffer.isNotEmpty()) {
                            currentLine.append(subBuffer.toString())
                        }
                    } else {
                        // Single unbreakable word: truncate with ellipsis only as last resort
                        var truncated = word
                        while (truncated.isNotEmpty() && paint.measureText("$truncated…") > availableWidth) {
                            truncated = truncated.dropLast(1)
                        }
                        lines.add(if (truncated.isNotEmpty()) "$truncated…" else word)
                    }
                }
            } else {
                val test = "$currentLine $word"
                if (paint.measureText(test) <= availableWidth) {
                    currentLine.append(" ").append(word)
                } else {
                    flushCurrentLine()
                    val wordW = paint.measureText(word)
                    if (wordW <= availableWidth) {
                        currentLine.append(word)
                    } else {
                        if (word.contains('/') || word.contains('-')) {
                            val subParts = word.split(Regex("(?<=[/-])|(?=[/-])")).filter { it.isNotEmpty() }
                            var subBuffer = StringBuilder()
                            for (part in subParts) {
                                val candidate = if (subBuffer.isEmpty()) part else "$subBuffer$part"
                                if (paint.measureText(candidate) <= availableWidth) {
                                    subBuffer.append(part)
                                } else {
                                    if (subBuffer.isNotEmpty()) {
                                        lines.add(subBuffer.toString())
                                        subBuffer = StringBuilder()
                                    }
                                    if (paint.measureText(part) <= availableWidth) {
                                        subBuffer.append(part)
                                    } else {
                                        var truncated = part
                                        while (truncated.isNotEmpty() && paint.measureText("$truncated…") > availableWidth) {
                                            truncated = truncated.dropLast(1)
                                        }
                                        lines.add(if (truncated.isNotEmpty()) "$truncated…" else part)
                                    }
                                }
                            }
                            if (subBuffer.isNotEmpty()) {
                                currentLine.append(subBuffer.toString())
                            }
                        } else {
                            var truncated = word
                            while (truncated.isNotEmpty() && paint.measureText("$truncated…") > availableWidth) {
                                truncated = truncated.dropLast(1)
                            }
                            lines.add(if (truncated.isNotEmpty()) "$truncated…" else word)
                        }
                    }
                }
            }
        }
        flushCurrentLine()
        return if (lines.isEmpty()) listOf(trimmed) else lines
    }

    /**
     * Proportionally distributes available content width across columns based on natural text widths.
     * Enforces minimum width (at least the longest single word + cell padding) and maximum width (<= 40% of page width).
     */
    fun distributeProportionalWidths(
        contentWidth: Float,
        naturalWidths: FloatArray,
        minWidths: FloatArray,
        maxWidths: FloatArray
    ): FloatArray {
        val n = naturalWidths.size
        if (n == 0) return FloatArray(0)
        val widths = FloatArray(n)
        val pinned = BooleanArray(n) { false }

        val totalNatural = naturalWidths.sum()
        if (totalNatural <= 0f) {
            val eq = contentWidth / n
            return FloatArray(n) { eq }
        }

        // Proportional allocation based on natural content weights
        for (i in 0 until n) {
            widths[i] = (naturalWidths[i] / totalNatural) * contentWidth
        }

        // Iterative constraint enforcement
        for (iter in 0 until 6) {
            var newlyPinned = false
            for (i in 0 until n) {
                if (!pinned[i]) {
                    if (widths[i] < minWidths[i]) {
                        widths[i] = minWidths[i]
                        pinned[i] = true
                        newlyPinned = true
                    } else if (widths[i] > maxWidths[i]) {
                        widths[i] = maxWidths[i]
                        pinned[i] = true
                        newlyPinned = true
                    }
                }
            }

            val pinnedSum = widths.indices.filter { pinned[it] }.sumOf { widths[it].toDouble() }.toFloat()
            val unpinnedIndices = widths.indices.filter { !pinned[it] }
            val remainingWidth = contentWidth - pinnedSum

            if (unpinnedIndices.isEmpty() || !newlyPinned) {
                break
            }

            val unpinnedNaturalSum = unpinnedIndices.sumOf { naturalWidths[it].toDouble() }.toFloat()
            if (unpinnedNaturalSum > 0f) {
                for (idx in unpinnedIndices) {
                    widths[idx] = remainingWidth * (naturalWidths[idx] / unpinnedNaturalSum)
                }
            } else {
                val equalRemaining = remainingWidth / unpinnedIndices.size
                for (idx in unpinnedIndices) {
                    widths[idx] = equalRemaining
                }
            }
        }

        // Final rounding adjustment to guarantee sum matches contentWidth exactly
        val currentSum = widths.sum()
        val diff = contentWidth - currentSum
        if (Math.abs(diff) > 0.01f) {
            val unpinned = widths.indices.filter { !pinned[it] }
            val targetIdx = if (unpinned.isNotEmpty()) {
                unpinned.maxByOrNull { widths[it] } ?: unpinned.first()
            } else {
                widths.indices.maxByOrNull { widths[it] } ?: (n - 1)
            }
            widths[targetIdx] = maxOf(minWidths[targetIdx], widths[targetIdx] + diff)
        }

        return widths
    }

    /**
     * Calculates the sum of minimum required column widths for a table at a given font size.
     */
    fun calculateMinColumnsWidth(
        headers: List<String>,
        rawRows: List<List<String>>,
        boldColumns: Set<Int>,
        bodyFontSize: Float,
        headerFontSize: Float,
        headerTypeface: android.graphics.Typeface,
        bodyTypeface: android.graphics.Typeface,
        bodyBoldTypeface: android.graphics.Typeface
    ): Float {
        val headerPaint = Paint().apply {
            textSize = headerFontSize
            typeface = headerTypeface
            isAntiAlias = true
        }
        val cellPaint = Paint().apply {
            textSize = bodyFontSize
            typeface = bodyTypeface
            isAntiAlias = true
        }
        val boldCellPaint = Paint().apply {
            textSize = bodyFontSize
            typeface = bodyBoldTypeface
            isAntiAlias = true
        }

        var sum = 0f
        for (c in headers.indices) {
            val p = if (c in boldColumns) boldCellPaint else cellPaint
            val h = headers.getOrElse(c) { "" }
            var maxWord = h.split(Regex("\\s+")).maxOfOrNull { headerPaint.measureText(it) } ?: 0f

            for (r in rawRows) {
                val text = r.getOrElse(c) { "" }
                val w = text.split(Regex("\\s+")).maxOfOrNull { p.measureText(it) } ?: 0f
                if (w > maxWord) maxWord = w
            }
            val minFloor = if (c == 0) 18f else 22f
            sum += maxOf(maxWord + 8f, minFloor)
        }
        return sum
    }

    /**
     * Measures actual cell contents and builds a DynamicTableLayout with:
     * - Proportional column widths based on content lengths.
     * - Controlled word-wrapped lines inside each cell.
     * - Dynamic row heights calculated per row based on maximum line counts.
     * - Font-size scale fallback if minimum column widths exceed available width.
     */
    fun computeDynamicTableLayout(
        headers: List<String>,
        rawRows: List<List<String>>,
        rowIsLastOfGroup: List<Boolean>,
        boldColumns: Set<Int>,
        contentWidth: Float,
        initialBodyFontSize: Float,
        initialHeaderFontSize: Float,
        bodyTypeface: android.graphics.Typeface,
        bodyBoldTypeface: android.graphics.Typeface,
        headerTypeface: android.graphics.Typeface,
        bodyColor: Int,
        headerColor: Int
    ): DynamicTableLayout {
        val colCount = headers.size
        var bodyFontSize = initialBodyFontSize
        var headerFontSize = initialHeaderFontSize
        val minFontSize = 6.0f

        while (true) {
            val headerPaint = Paint().apply {
                color = headerColor
                textSize = headerFontSize
                typeface = headerTypeface
                isAntiAlias = true
            }
            val cellPaint = Paint().apply {
                color = bodyColor
                textSize = bodyFontSize
                typeface = bodyTypeface
                isAntiAlias = true
            }
            val boldCellPaint = Paint().apply {
                color = bodyColor
                textSize = bodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }

            val naturalWidths = FloatArray(colCount)
            val minWidths = FloatArray(colCount)
            val maxWidths = FloatArray(colCount) { maxOf(contentWidth * 0.40f, 60f) }

            for (c in 0 until colCount) {
                val p = if (c in boldColumns) boldCellPaint else cellPaint
                val hText = headers.getOrElse(c) { "" }
                var maxTextW = headerPaint.measureText(hText)
                var maxWordW = hText.split(Regex("\\s+")).maxOfOrNull { headerPaint.measureText(it) } ?: 0f

                for (r in rawRows) {
                    val cellText = r.getOrElse(c) { "" }
                    val textW = p.measureText(cellText)
                    if (textW > maxTextW) maxTextW = textW
                    val wordW = cellText.split(Regex("\\s+")).maxOfOrNull { p.measureText(it) } ?: 0f
                    if (wordW > maxWordW) maxWordW = wordW
                }

                val minFloor = if (c == 0) 18f else 22f
                minWidths[c] = maxOf(maxWordW + 8f, minFloor)
                naturalWidths[c] = maxOf(maxTextW + 8f, minWidths[c])
                if (minWidths[c] > maxWidths[c]) {
                    maxWidths[c] = minWidths[c]
                }
            }

            val totalMin = minWidths.sum()
            if (totalMin > contentWidth && bodyFontSize > minFontSize) {
                // Fallback Step 5.1: Automatically reduce font size progressively until table fits
                bodyFontSize = (bodyFontSize - 0.5f).coerceAtLeast(minFontSize)
                headerFontSize = (headerFontSize - 0.5f).coerceAtLeast(minFontSize + 1f)
                continue
            }

            // Proportionally distribute width across columns
            val colWidths = distributeProportionalWidths(contentWidth, naturalWidths, minWidths, maxWidths)

            // Dynamic header wrapping and row height
            val headerFm = headerPaint.fontMetrics
            val rawHeaderLineHeight = headerFm.descent - headerFm.ascent
            val headerLineHeight = if (rawHeaderLineHeight > 1f) rawHeaderLineHeight * 1.15f else headerFontSize * 1.35f
            val headerCellLines = headers.mapIndexed { c, h ->
                wrapTextToLines(h, headerPaint, colWidths[c] - 8f)
            }
            val maxHeaderLines = headerCellLines.maxOfOrNull { it.size } ?: 1
            val headerHeight = maxOf(22f, maxHeaderLines * headerLineHeight + 8f)
            val headerRow = DynamicRow(headerCellLines, headerHeight)

            // Dynamic data rows wrapping and row heights
            val cellFm = cellPaint.fontMetrics
            val rawCellLineHeight = cellFm.descent - cellFm.ascent
            val cellLineHeight = if (rawCellLineHeight > 1f) rawCellLineHeight * 1.15f else bodyFontSize * 1.35f
            val rows = rawRows.mapIndexed { rIdx, rawRow ->
                val cells = rawRow.mapIndexed { c, cellText ->
                    val p = if (c in boldColumns) boldCellPaint else cellPaint
                    wrapTextToLines(cellText, p, colWidths[c] - 8f)
                }
                val maxLines = cells.maxOfOrNull { it.size } ?: 1
                val rHeight = maxOf(18f, maxLines * cellLineHeight + 8f)
                DynamicRow(cells, rHeight, rowIsLastOfGroup.getOrElse(rIdx) { false })
            }

            return DynamicTableLayout(
                columnWidths = colWidths,
                effectiveBodyFontSize = bodyFontSize,
                effectiveHeaderFontSize = headerFontSize,
                headerRow = headerRow,
                rows = rows
            )
        }
    }

    /**
     * Draws a dynamically laid out data row on the PDF canvas with vertically centered wrapped lines and grid borders.
     */
    private fun drawDynamicPdfRow(
        canvas: android.graphics.Canvas,
        row: DynamicRow,
        colWidths: FloatArray,
        margin: Float,
        currentY: Float,
        contentWidth: Float,
        boldColumns: Set<Int>,
        cellPaint: Paint,
        boldCellPaint: Paint,
        zebraPaint: Paint?,
        gridPaint: Paint,
        dividerPaint: Paint? = null,
        genderColumnIndex: Int = -1,
        maskulinPaint: Paint? = null,
        femininPaint: Paint? = null,
        neutrumPaint: Paint? = null
    ) {
        if (zebraPaint != null) {
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + row.rowHeight, zebraPaint)
        }

        var colX = margin
        for (c in colWidths.indices) {
            val colW = colWidths[c]
            val lines = row.cells.getOrElse(c) { emptyList() }
            val p = if (c == genderColumnIndex && maskulinPaint != null && femininPaint != null && neutrumPaint != null) {
                val cellText = lines.joinToString(" ").trim().lowercase(java.util.Locale.ROOT)
                when {
                    cellText.startsWith("maskulin") || cellText == "der" || cellText == "m" -> maskulinPaint
                    cellText.startsWith("feminin") || cellText == "die" || cellText == "f" -> femininPaint
                    cellText.startsWith("neutrum") || cellText == "das" || cellText == "n" -> neutrumPaint
                    c in boldColumns -> boldCellPaint
                    else -> cellPaint
                }
            } else if (c in boldColumns) {
                boldCellPaint
            } else {
                cellPaint
            }
            val fm = p.fontMetrics
            val lineHeight = (fm.descent - fm.ascent) * 1.15f
            val totalBlockHeight = lines.size * lineHeight
            val startBaseline = currentY + (row.rowHeight - totalBlockHeight) / 2f - fm.ascent

            for ((lineIdx, lineText) in lines.withIndex()) {
                val textY = startBaseline + lineIdx * lineHeight
                canvas.drawText(lineText, colX + 4f, textY, p)
            }

            if (c > 0) {
                canvas.drawLine(colX, currentY, colX, currentY + row.rowHeight, gridPaint)
            }
            colX += colW
        }

        // Outer borders
        canvas.drawLine(margin, currentY, margin, currentY + row.rowHeight, gridPaint)
        canvas.drawLine(margin + contentWidth, currentY, margin + contentWidth, currentY + row.rowHeight, gridPaint)

        // Bottom border
        val borderPaint = if (row.isLastRowOfGroup && dividerPaint != null) dividerPaint else gridPaint
        canvas.drawLine(margin, currentY + row.rowHeight, margin + contentWidth, currentY + row.rowHeight, borderPaint)
    }

    /**
     * Draws the table header on the PDF canvas with vertically centered wrapped lines and bottom border.
     */
    private fun drawDynamicPdfHeader(
        canvas: android.graphics.Canvas,
        headerRow: DynamicRow,
        colWidths: FloatArray,
        margin: Float,
        currentY: Float,
        contentWidth: Float,
        headerBgPaint: Paint,
        headerTextPaint: Paint,
        headerBottomBorderPaint: Paint,
        gridPaint: Paint
    ) {
        canvas.drawRect(margin, currentY, margin + contentWidth, currentY + headerRow.rowHeight, headerBgPaint)

        var colX = margin
        val fm = headerTextPaint.fontMetrics
        val lineHeight = (fm.descent - fm.ascent) * 1.15f

        for (c in colWidths.indices) {
            val colW = colWidths[c]
            val lines = headerRow.cells.getOrElse(c) { emptyList() }
            val totalBlockHeight = lines.size * lineHeight
            val startBaseline = currentY + (headerRow.rowHeight - totalBlockHeight) / 2f - fm.ascent

            for ((lineIdx, lineText) in lines.withIndex()) {
                val textY = startBaseline + lineIdx * lineHeight
                canvas.drawText(lineText, colX + 4f, textY, headerTextPaint)
            }

            if (c > 0) {
                canvas.drawLine(colX, currentY, colX, currentY + headerRow.rowHeight, gridPaint)
            }
            colX += colW
        }

        // Outer borders & bottom header line
        canvas.drawLine(margin, currentY, margin, currentY + headerRow.rowHeight, headerBottomBorderPaint)
        canvas.drawLine(margin + contentWidth, currentY, margin + contentWidth, currentY + headerRow.rowHeight, headerBottomBorderPaint)
        canvas.drawLine(margin, currentY + headerRow.rowHeight, margin + contentWidth, currentY + headerRow.rowHeight, headerBottomBorderPaint)
    }

    /**
     * Draws the section title on the left and the register name on the right side of the page header.
     * Handles clipping/truncating the title if space is constrained to prevent overlapping.
     */
    private fun drawHeaderWithNotebook(
        canvas: Canvas,
        title: String,
        notebookText: String,
        leftMargin: Float,
        rightMargin: Float,
        y: Float,
        titlePaint: Paint,
        notebookPaint: Paint
    ) {
        if (notebookText.isNotBlank()) {
            canvas.drawText(notebookText, rightMargin, y, notebookPaint)
            val nbWidth = notebookPaint.measureText(notebookText)
            val availableTitleWidth = (rightMargin - nbWidth - 14f) - leftMargin
            val effectiveTitle = if (titlePaint.measureText(title) > availableTitleWidth && availableTitleWidth > 30f) {
                var truncated = title
                while (truncated.isNotEmpty() && titlePaint.measureText("$truncated...") > availableTitleWidth) {
                    truncated = truncated.dropLast(1)
                }
                "$truncated..."
            } else {
                title
            }
            canvas.drawText(effectiveTitle, leftMargin, y, titlePaint)
        } else {
            canvas.drawText(title, leftMargin, y, titlePaint)
        }
    }

    /**
     * Generates unified PDF with Nouns, Verbs, or both, formatted according to PaperSizeOption.
     * Implements dynamic column widths, proportional space distribution, cell word-wrapping,
     * dynamic row heights, and fallback overflow mechanisms.
     */
    fun createUnifiedPdfFile(
        file: File,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: ExportScope,
        documentTitle: String = "Suchverlauf_Deutsch",
        paperSize: PaperSizeOption = PaperSizeOption.DEFAULT,
        typography: ExportTypographyOptions = ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val cleanTitle = documentTitle.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }
        val doc = PdfDocument()

        val bodyTypeface = createSafeTypeface(typography.bodyFontFamily, android.graphics.Typeface.NORMAL)
        val bodyBoldTypeface = createSafeTypeface(typography.bodyFontFamily, android.graphics.Typeface.BOLD)
        val headerTypeface = createSafeTypeface(typography.headerFontFamily, android.graphics.Typeface.BOLD)
        val bodyColor = parseHexColor(typography.bodyFontColorHex, Color.rgb(15, 23, 42))
        val headerColor = parseHexColor(typography.headerFontColorHex, Color.rgb(15, 23, 42))

        val cleanNotebook = notebookName.trim()
        val displayNotebookName = if (cleanNotebook.isBlank()) {
            ""
        } else if (cleanNotebook.startsWith("Notizbuch", ignoreCase = true)) {
            cleanNotebook
        } else {
            "Notizbuch: $cleanNotebook"
        }

        val notebookPaint = Paint().apply {
            color = headerColor
            textSize = maxOf(9.5f, typography.headerFontSize - 1.5f)
            typeface = headerTypeface
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val hasNouns = (scope == ExportScope.ALLE || scope == ExportScope.NOMEN) && (nouns.isNotEmpty() || scope == ExportScope.NOMEN)
        val hasVerbs = (scope == ExportScope.ALLE || scope == ExportScope.VERB) && (verbs.isNotEmpty() || scope == ExportScope.VERB)

        val nounRows = if (hasNouns) buildDeduplicatedRows(nouns) else emptyList()
        val verbRows = if (hasVerbs) buildMasterVerbRows(verbs) else emptyList()

        val rawNounRows = nounRows.mapIndexed { idx, r -> listOf((idx + 1).toString()) + r.toList() }
        val rawVerbRows = verbRows.mapIndexed { idx, r ->
            val nr = if (idx % 7 == 0) ((idx / 7) + 1).toString() else ""
            listOf(nr) + r.toList()
        }
        val verbLastRowFlags = verbRows.map { it.isLastRowOfVerb }

        // Fallback Step 5.2: If portrait orientation cannot fit the minimum column widths comfortably,
        // switch orientation to Landscape to provide additional horizontal space.
        var pageWidth = paperSize.widthPt
        var pageHeight = paperSize.heightPt
        if (!paperSize.isLandscape && pageWidth < pageHeight) {
            val portraitContentWidth = pageWidth - (if (pageWidth <= 500) 36f else 48f)
            var needsLandscape = false
            if (hasNouns) {
                val minW = calculateMinColumnsWidth(
                    HEADERS_WITH_NR, rawNounRows, setOf(1, 2),
                    typography.bodyFontSize, typography.headerFontSize,
                    headerTypeface, bodyTypeface, bodyBoldTypeface
                )
                if (minW > portraitContentWidth) needsLandscape = true
            }
            if (!needsLandscape && hasVerbs) {
                val minW = calculateMinColumnsWidth(
                    VERB_MASTER_HEADERS_WITH_NR, rawVerbRows, setOf(1),
                    typography.bodyFontSize, typography.headerFontSize,
                    headerTypeface, bodyTypeface, bodyBoldTypeface
                )
                if (minW > portraitContentWidth) needsLandscape = true
            }
            if (needsLandscape) {
                pageWidth = maxOf(paperSize.widthPt, paperSize.heightPt)
                pageHeight = minOf(paperSize.widthPt, paperSize.heightPt)
            }
        }

        val margin = if (pageWidth <= 500) 18f else 24f
        val contentWidth = pageWidth - (margin * 2)

        val headerPaint = Paint().apply {
            color = Color.rgb(217, 217, 217) // #D9D9D9
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val headerBottomBorderPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val gridBorderPaint = Paint().apply {
            color = Color.rgb(215, 220, 228)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val verbGroupDividerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val zebraLightPaint = Paint().apply {
            color = Color.rgb(248, 249, 251)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = headerColor
            textSize = maxOf(13f, typography.headerFontSize)
            typeface = headerTypeface
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = bodyTypeface
            isAntiAlias = true
        }

        var pageNumber = 1

        // 1. Render Nouns
        if (hasNouns) {
            val nounTableLayout = computeDynamicTableLayout(
                headers = HEADERS_WITH_NR,
                rawRows = rawNounRows,
                rowIsLastOfGroup = List(rawNounRows.size) { false },
                boldColumns = setOf(1, 2),
                contentWidth = contentWidth,
                initialBodyFontSize = typography.bodyFontSize,
                initialHeaderFontSize = typography.headerFontSize,
                bodyTypeface = bodyTypeface,
                bodyBoldTypeface = bodyBoldTypeface,
                headerTypeface = headerTypeface,
                bodyColor = bodyColor,
                headerColor = headerColor
            )

            val nounCellPaint = Paint().apply {
                color = bodyColor
                textSize = nounTableLayout.effectiveBodyFontSize
                typeface = bodyTypeface
                isAntiAlias = true
            }
            val nounBoldCellPaint = Paint().apply {
                color = bodyColor
                textSize = nounTableLayout.effectiveBodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }
            val nounHeaderTextPaint = Paint().apply {
                color = headerColor
                textSize = nounTableLayout.effectiveHeaderFontSize
                typeface = headerTypeface
                isAntiAlias = true
            }

            val maskulinPaint = Paint().apply {
                color = Color.rgb(0x15, 0x65, 0xC0) // #1565C0
                textSize = nounTableLayout.effectiveBodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }
            val femininPaint = Paint().apply {
                color = Color.rgb(0xC2, 0x18, 0x5B) // #C2185B
                textSize = nounTableLayout.effectiveBodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }
            val neutrumPaint = Paint().apply {
                color = Color.rgb(0x15, 0x80, 0x3D) // #15803D
                textSize = nounTableLayout.effectiveBodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }

            var currentNounIdx = 0
            var nounPageCount = 0

            while (currentNounIdx < nounTableLayout.rows.size || (nounTableLayout.rows.isEmpty() && nounPageCount == 0)) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                var currentY = margin + 12f
                val nounSectionTitle = when {
                    scope == ExportScope.ALLE && nounPageCount == 0 -> "$cleanTitle — Nomen"
                    scope == ExportScope.ALLE -> "$cleanTitle — Nomen (Fortsetzung)"
                    nounPageCount == 0 -> cleanTitle
                    else -> "$cleanTitle (Fortsetzung)"
                }
                drawHeaderWithNotebook(
                    canvas = canvas,
                    title = nounSectionTitle,
                    notebookText = displayNotebookName,
                    leftMargin = margin,
                    rightMargin = pageWidth - margin,
                    y = currentY,
                    titlePaint = titlePaint,
                    notebookPaint = notebookPaint
                )
                currentY += 14f

                // Render repeating table header
                drawDynamicPdfHeader(
                    canvas = canvas,
                    headerRow = nounTableLayout.headerRow,
                    colWidths = nounTableLayout.columnWidths,
                    margin = margin,
                    currentY = currentY,
                    contentWidth = contentWidth,
                    headerBgPaint = headerPaint,
                    headerTextPaint = nounHeaderTextPaint,
                    headerBottomBorderPaint = headerBottomBorderPaint,
                    gridPaint = gridBorderPaint
                )
                currentY += nounTableLayout.headerRow.rowHeight

                if (nounTableLayout.rows.isEmpty()) {
                    canvas.drawText("Keine Nomen im Suchverlauf vorhanden.", margin + 6f, currentY + 14f, subtitlePaint)
                } else {
                    while (currentNounIdx < nounTableLayout.rows.size) {
                        val row = nounTableLayout.rows[currentNounIdx]
                        if (currentY + row.rowHeight > pageHeight - margin - 18f) {
                            break
                        }
                        val zebraPaint = if (currentNounIdx % 2 == 1) zebraLightPaint else null
                        drawDynamicPdfRow(
                            canvas = canvas,
                            row = row,
                            colWidths = nounTableLayout.columnWidths,
                            margin = margin,
                            currentY = currentY,
                            contentWidth = contentWidth,
                            boldColumns = setOf(1, 2),
                            cellPaint = nounCellPaint,
                            boldCellPaint = nounBoldCellPaint,
                            zebraPaint = zebraPaint,
                            gridPaint = gridBorderPaint,
                            genderColumnIndex = HEADERS_WITH_NR.indexOf("Genus"),
                            maskulinPaint = maskulinPaint,
                            femininPaint = femininPaint,
                            neutrumPaint = neutrumPaint
                        )
                        currentY += row.rowHeight
                        currentNounIdx++
                    }
                }

                val footerText = if (scope == ExportScope.ALLE) "Seite $pageNumber — Nomen" else "Seite $pageNumber"
                canvas.drawText(footerText, pageWidth - margin - subtitlePaint.measureText(footerText), pageHeight - 10f, subtitlePaint)

                doc.finishPage(page)
                pageNumber++
                nounPageCount++
                if (currentNounIdx >= nounTableLayout.rows.size) break
            }
        }

        // 2. Render Verbs (Master Table: ONE unified table with 9 columns)
        if (hasVerbs) {
            val verbTableLayout = computeDynamicTableLayout(
                headers = VERB_MASTER_HEADERS_WITH_NR,
                rawRows = rawVerbRows,
                rowIsLastOfGroup = verbLastRowFlags,
                boldColumns = setOf(1),
                contentWidth = contentWidth,
                initialBodyFontSize = typography.bodyFontSize,
                initialHeaderFontSize = typography.headerFontSize,
                bodyTypeface = bodyTypeface,
                bodyBoldTypeface = bodyBoldTypeface,
                headerTypeface = headerTypeface,
                bodyColor = bodyColor,
                headerColor = headerColor
            )

            val verbCellPaint = Paint().apply {
                color = bodyColor
                textSize = verbTableLayout.effectiveBodyFontSize
                typeface = bodyTypeface
                isAntiAlias = true
            }
            val verbBoldCellPaint = Paint().apply {
                color = bodyColor
                textSize = verbTableLayout.effectiveBodyFontSize
                typeface = bodyBoldTypeface
                isAntiAlias = true
            }
            val verbHeaderTextPaint = Paint().apply {
                color = headerColor
                textSize = verbTableLayout.effectiveHeaderFontSize
                typeface = headerTypeface
                isAntiAlias = true
            }

            var currentVerbIdx = 0
            var verbPageCount = 0

            while (currentVerbIdx < verbTableLayout.rows.size || (verbTableLayout.rows.isEmpty() && verbPageCount == 0)) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                var currentY = margin + 12f
                val sectionTitle = when {
                    scope == ExportScope.ALLE && verbPageCount == 0 -> "$cleanTitle — Verben"
                    scope == ExportScope.ALLE -> "$cleanTitle — Verben (Fortsetzung)"
                    verbPageCount == 0 -> cleanTitle
                    else -> "$cleanTitle (Fortsetzung)"
                }
                drawHeaderWithNotebook(
                    canvas = canvas,
                    title = sectionTitle,
                    notebookText = displayNotebookName,
                    leftMargin = margin,
                    rightMargin = pageWidth - margin,
                    y = currentY,
                    titlePaint = titlePaint,
                    notebookPaint = notebookPaint
                )
                currentY += 14f

                // Fixed table header (repeating at top of every page)
                drawDynamicPdfHeader(
                    canvas = canvas,
                    headerRow = verbTableLayout.headerRow,
                    colWidths = verbTableLayout.columnWidths,
                    margin = margin,
                    currentY = currentY,
                    contentWidth = contentWidth,
                    headerBgPaint = headerPaint,
                    headerTextPaint = verbHeaderTextPaint,
                    headerBottomBorderPaint = headerBottomBorderPaint,
                    gridPaint = gridBorderPaint
                )
                currentY += verbTableLayout.headerRow.rowHeight

                if (verbTableLayout.rows.isEmpty()) {
                    canvas.drawText("Keine Verben im Suchverlauf vorhanden.", margin + 6f, currentY + 14f, subtitlePaint)
                } else {
                    while (currentVerbIdx < verbTableLayout.rows.size) {
                        val row = verbTableLayout.rows[currentVerbIdx]
                        if (currentY + row.rowHeight > pageHeight - margin - 18f) {
                            break
                        }
                        val zebraPaint = if (currentVerbIdx % 2 == 1) zebraLightPaint else null
                        drawDynamicPdfRow(
                            canvas = canvas,
                            row = row,
                            colWidths = verbTableLayout.columnWidths,
                            margin = margin,
                            currentY = currentY,
                            contentWidth = contentWidth,
                            boldColumns = setOf(1),
                            cellPaint = verbCellPaint,
                            boldCellPaint = verbBoldCellPaint,
                            zebraPaint = zebraPaint,
                            gridPaint = gridBorderPaint,
                            dividerPaint = verbGroupDividerPaint
                        )
                        currentY += row.rowHeight
                        currentVerbIdx++
                    }
                }

                val footerText = if (scope == ExportScope.ALLE) "Seite $pageNumber — Verben" else "Seite $pageNumber"
                canvas.drawText(footerText, pageWidth - margin - subtitlePaint.measureText(footerText), pageHeight - 10f, subtitlePaint)

                doc.finishPage(page)
                pageNumber++
                verbPageCount++
                if (currentVerbIdx >= verbTableLayout.rows.size) break
            }
        }

        FileOutputStream(file).use { fos ->
            doc.writeTo(fos)
        }
        doc.close()
    }

    /**
     * Renders responsive HTML in Landscape orientation for direct printing or saving via Android PrintManager.
     */
    fun generateLandscapePrintableHtml(
        words: List<WordDeclensionResult>,
        documentTitle: String = "Suchverlauf_Deutsch",
        notebookName: String = ""
    ): String {
        val rows = buildDeduplicatedRows(words)
        val cleanTitle = documentTitle.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }
        val cleanNotebook = notebookName.trim()
        val displayNotebook = if (cleanNotebook.isBlank()) "" else if (cleanNotebook.startsWith("Notizbuch", ignoreCase = true)) cleanNotebook else "Notizbuch: $cleanNotebook"
        val sb = StringBuilder()

        sb.append("""
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>${escapeXml(cleanTitle)}</title>
    <style>
        @page {
            size: A4 landscape;
            margin: 8mm 8mm 8mm 8mm;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 10px;
            color: #0F172A;
            background: #FFFFFF;
            font-size: 10px;
            line-height: 1.2;
        }
        .header-container {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
            margin-bottom: 4px;
        }
        h1 {
            font-size: 15px;
            color: #0F172A;
            margin: 0;
            font-weight: bold;
        }
        .register-tag {
            font-size: 11px;
            color: #0F172A;
            font-weight: bold;
        }
        p.subtitle {
            font-size: 10px;
            color: #64748B;
            margin: 0 0 12px 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 16px;
        }
        tr {
            page-break-inside: avoid;
        }
        thead {
            display: table-header-group;
        }
        th {
            background-color: #D9D9D9 !important;
            color: #0F172A !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
            text-align: left;
            padding: 6px 5px;
            font-size: 11px;
            font-weight: bold;
            border: 0.5px solid #D0D0D0;
            border-bottom: 1.5px solid #505050;
            white-space: nowrap;
        }
        td {
            padding: 5px 5px;
            border: 0.5px solid #D0D0D0;
            font-size: 10px;
            text-align: left;
            vertical-align: middle;
            white-space: nowrap;
        }
        tr.data-row:nth-child(even) {
            background-color: #F2F2F2 !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        .bold-noun {
            font-weight: bold;
            color: #0F172A;
        }
        .gender-maskulin {
            color: #1565C0 !important;
            font-weight: bold;
        }
        .gender-feminin {
            color: #C2185B !important;
            font-weight: bold;
        }
        .gender-neutrum {
            color: #15803D !important;
            font-weight: bold;
        }
        .translation-col {
            color: #1E293B;
        }
    </style>
</head>
<body>
    <div class="header-container">
        <h1>${escapeXml(cleanTitle)}</h1>
        ${if (displayNotebook.isNotBlank()) """<span class="register-tag">${escapeXml(displayNotebook)}</span>""" else ""}
    </div>

    <table>
        <thead>
            <tr>
                <th>Nomen (Singular)</th>
                <th>Genus</th>
                <th>English translation</th>
                <th>Nominativ (Sg./Pl.)</th>
                <th>Akkusativ (Sg./Pl.)</th>
                <th>Genitiv (Sg./Pl.)</th>
                <th>Dativ (Sg./Pl.)</th>
            </tr>
        </thead>
        <tbody>
        """.trimIndent())

        for (row in rows) {
            val g = row.genus.trim().lowercase(java.util.Locale.ROOT)
            val genderClass = when {
                g.startsWith("maskulin") || g == "der" || g == "m" -> "gender-maskulin"
                g.startsWith("feminin") || g == "die" || g == "f" -> "gender-feminin"
                g.startsWith("neutrum") || g == "das" || g == "n" -> "gender-neutrum"
                else -> ""
            }
            sb.append("""
            <tr class="data-row">
                <td class="bold-noun">${escapeXml(row.nomenSingular)}</td>
                <td class="bold-noun $genderClass">${escapeXml(row.genus)}</td>
                <td class="translation-col">${escapeXml(row.englishTranslation)}</td>
                <td>${escapeXml(row.nominativ)}</td>
                <td>${escapeXml(row.akkusativ)}</td>
                <td>${escapeXml(row.genitiv)}</td>
                <td>${escapeXml(row.dativ)}</td>
            </tr>
            """.trimIndent())
        }

        sb.append("""
        </tbody>
    </table>
</body>
</html>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * Prints or saves as PDF via Android PrintManager with standard ISO_A4 in LANDSCAPE orientation.
     */
    fun printLandscapeTable(context: Context, words: List<WordDeclensionResult>, jobTitle: String = "Deklination_Suchverlauf") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val html = generateLandscapePrintableHtml(words, jobTitle)

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobTitle)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                    .build()
                printManager.print(jobTitle, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    /**
     * Generates unified responsive HTML in Landscape orientation for direct printing or saving via Android PrintManager.
     */
    fun generateUnifiedLandscapePrintableHtml(
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: ExportScope,
        documentTitle: String = "Suchverlauf_Deutsch",
        notebookName: String = ""
    ): String {
        val cleanTitle = documentTitle.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }
        val cleanNotebook = notebookName.trim()
        val displayNotebook = if (cleanNotebook.isBlank()) "" else if (cleanNotebook.startsWith("Notizbuch", ignoreCase = true)) cleanNotebook else "Notizbuch: $cleanNotebook"
        val sb = StringBuilder()
        sb.append("""
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>${escapeXml(cleanTitle)}</title>
    <style>
        @page {
            size: A4 landscape;
            margin: 8mm 8mm 8mm 8mm;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            margin: 10px;
            color: #0F172A;
            background: #FFFFFF;
            font-size: 10px;
            line-height: 1.2;
        }
        .header-container {
            display: flex;
            justify-content: space-between;
            align-items: baseline;
            margin-bottom: 8px;
        }
        h1 {
            font-size: 15px;
            color: #0F172A;
            margin: 0;
            font-weight: bold;
        }
        .register-tag {
            font-size: 11px;
            color: #0F172A;
            font-weight: bold;
        }
        h2 {
            font-size: 13px;
            color: #0F172A;
            margin: 14px 0 6px 0;
            font-weight: bold;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 16px;
        }
        tr {
            page-break-inside: avoid;
        }
        thead {
            display: table-header-group;
        }
        th {
            background-color: #D9D9D9 !important;
            color: #0F172A !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
            text-align: left;
            padding: 5px 4px;
            font-size: 10px;
            font-weight: bold;
            border: 0.5px solid #D0D0D0;
            border-bottom: 1.5px solid #505050;
            white-space: nowrap;
        }
        td {
            padding: 4px 4px;
            border: 0.5px solid #D0D0D0;
            font-size: 9.5px;
            text-align: left;
            vertical-align: middle;
            white-space: nowrap;
        }
        tr.data-row:nth-child(even) {
            background-color: #F8F9FB !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        tr.verb-divider td {
            border-bottom: 1.5px solid #64748B;
        }
        .bold-cell {
            font-weight: bold;
            color: #0F172A;
        }
        .gender-maskulin {
            color: #1565C0 !important;
            font-weight: bold;
        }
        .gender-feminin {
            color: #C2185B !important;
            font-weight: bold;
        }
        .gender-neutrum {
            color: #15803D !important;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="header-container">
        <h1>${escapeXml(cleanTitle)}</h1>
        ${if (displayNotebook.isNotBlank()) """<span class="register-tag">${escapeXml(displayNotebook)}</span>""" else ""}
    </div>
""".trimIndent())

        if (scope == ExportScope.ALLE || scope == ExportScope.NOMEN) {
            val nounRows = buildDeduplicatedRows(nouns)
            if (scope == ExportScope.ALLE) sb.append("<h2>Nomen</h2>")
            sb.append("""
    <table>
        <thead>
            <tr>
                <th>Nomen (Singular)</th>
                <th>Genus</th>
                <th>English translation</th>
                <th>Nominativ (Sg./Pl.)</th>
                <th>Akkusativ (Sg./Pl.)</th>
                <th>Genitiv (Sg./Pl.)</th>
                <th>Dativ (Sg./Pl.)</th>
            </tr>
        </thead>
        <tbody>
""".trimIndent())
            for (r in nounRows) {
                val g = r.genus.trim().lowercase(java.util.Locale.ROOT)
                val genderClass = when {
                    g.startsWith("maskulin") || g == "der" || g == "m" -> "gender-maskulin"
                    g.startsWith("feminin") || g == "die" || g == "f" -> "gender-feminin"
                    g.startsWith("neutrum") || g == "das" || g == "n" -> "gender-neutrum"
                    else -> ""
                }
                sb.append("""
            <tr class="data-row">
                <td class="bold-cell">${escapeXml(r.nomenSingular)}</td>
                <td class="bold-cell $genderClass">${escapeXml(r.genus)}</td>
                <td>${escapeXml(r.englishTranslation)}</td>
                <td>${escapeXml(r.nominativ)}</td>
                <td>${escapeXml(r.akkusativ)}</td>
                <td>${escapeXml(r.genitiv)}</td>
                <td>${escapeXml(r.dativ)}</td>
            </tr>
""".trimIndent())
            }
            sb.append("</tbody></table>")
        }

        if (scope == ExportScope.ALLE || scope == ExportScope.VERB) {
            val verbRows = buildMasterVerbRows(verbs)
            if (scope == ExportScope.ALLE) sb.append("<h2>Verben</h2>")
            sb.append("""
    <table>
        <thead>
            <tr>
                <th>Verb (Infinitiv)</th>
                <th>Zeit (Tense)</th>
                <th>ich</th>
                <th>du</th>
                <th>er/sie/es</th>
                <th>wir</th>
                <th>ihr</th>
                <th>sie/Sie</th>
            </tr>
        </thead>
        <tbody>
""".trimIndent())
            for (vr in verbRows) {
                val rowClass = if (vr.isLastRowOfVerb) "data-row verb-divider" else "data-row"
                sb.append("""
            <tr class="$rowClass">
                <td class="bold-cell">${escapeXml(vr.infinitiv)}</td>
                <td>${escapeXml(vr.zeit)}</td>
                <td>${escapeXml(vr.ich)}</td>
                <td>${escapeXml(vr.du)}</td>
                <td>${escapeXml(vr.erSieEs)}</td>
                <td>${escapeXml(vr.wir)}</td>
                <td>${escapeXml(vr.ihr)}</td>
                <td>${escapeXml(vr.sieSie)}</td>
            </tr>
""".trimIndent())
            }
            sb.append("</tbody></table>")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    /**
     * Unified print via Android PrintManager.
     */
    fun printUnifiedLandscapeTable(
        context: Context,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: ExportScope,
        jobTitle: String = "Suchverlauf_Deutsch"
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val html = generateUnifiedLandscapePrintableHtml(nouns, verbs, scope, jobTitle)

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobTitle)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                    .build()
                printManager.print(jobTitle, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    /**
     * CSV fallback with UTF-8 BOM, following the exact 8 adjacent columns.
     */
    fun generateCsv(words: List<WordDeclensionResult>): String {
        val rows = buildDeduplicatedRows(words)
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        sb.append(HEADERS.joinToString(",") { escapeCsv(it) }).append("\r\n")

        for (r in rows) {
            sb.append(r.toList().joinToString(",") { escapeCsv(it) }).append("\r\n")
        }

        return sb.toString()
    }

    private fun addZipEntry(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun escapeXml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun escapeCsv(v: String): String {
        val containsSpecial = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")
        return if (containsSpecial) {
            "\"" + v.replace("\"", "\"\"") + "\""
        } else {
            "\"$v\""
        }
    }
}
