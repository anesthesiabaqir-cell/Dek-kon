package com.example

import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.WordDeclensionResult
import com.example.util.DeclensionExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclensionExportHelperTest {

    private fun createSampleWord(): WordDeclensionResult {
        val singularRows = listOf(
            DeclensionCaseRow("nominativ", "Nominativ", "Wer oder was?", "der Tisch", "ein Tisch", "Tisch"),
            DeclensionCaseRow("akkusativ", "Akkusativ", "Wen oder was?", "den Tisch", "einen Tisch", "Tisch"),
            DeclensionCaseRow("dativ", "Dativ", "Wem?", "dem Tisch", "einem Tisch", "Tisch"),
            DeclensionCaseRow("genitiv", "Genitiv", "Wessen?", "des Tisches", "eines Tisches", "Tisches")
        )
        val pluralRows = listOf(
            DeclensionCaseRow("nominativ", "Nominativ", "Wer oder was?", "die Tische", "keine Tische", "Tische"),
            DeclensionCaseRow("akkusativ", "Akkusativ", "Wen oder was?", "die Tische", "keine Tische", "Tische"),
            DeclensionCaseRow("dativ", "Dativ", "Wem?", "den Tischen", "keinen Tischen", "Tischen"),
            DeclensionCaseRow("genitiv", "Genitiv", "Wessen?", "der Tische", "keiner Tische", "Tische")
        )
        return WordDeclensionResult(
            word = "Tisch",
            gender = "Maskulin",
            genderArticle = "der",
            pluralNoun = "die Tische",
            meaningEnglish = "table",
            singular = DeclensionTableGroup("Singular", singularRows),
            plural = DeclensionTableGroup("Plural", pluralRows)
        )
    }

    @Test
    fun testGenerateExcelCsv_containsRequiredColumnsAndBOM() {
        val word = createSampleWord()
        val csv = DeclensionExportHelper.generateExcelCsv(listOf(word))

        // Must start with UTF-8 BOM for Microsoft Excel compatibility
        assertTrue(csv.startsWith("\uFEFF"))

        // Header check
        assertTrue(csv.contains("Word (Wort)"))
        assertTrue(csv.contains("Number (Numerus)"))
        assertTrue(csv.contains("Case (Kasus)"))
        assertTrue(csv.contains("Definite Form (Bestimmter Artikel)"))
        assertTrue(csv.contains("Indefinite Form (Unbestimmter Artikel)"))
        assertTrue(csv.contains("Without Article (Ohne Artikel)"))

        // Rows check (Singular and Plural)
        assertTrue(csv.contains("Singular"))
        assertTrue(csv.contains("Plural"))
        assertTrue(csv.contains("Nominativ"))
        assertTrue(csv.contains("Akkusativ"))
        assertTrue(csv.contains("Dativ"))
        assertTrue(csv.contains("Genitiv"))
        assertTrue(csv.contains("der Tisch"))
        assertTrue(csv.contains("ein Tisch"))
        assertTrue(csv.contains("die Tische"))
        assertTrue(csv.contains("table"))
    }

    @Test
    fun testGenerateExcelTsv_containsTabDelimitedColumns() {
        val word = createSampleWord()
        val tsv = DeclensionExportHelper.generateExcelTsv(listOf(word))

        assertTrue(tsv.contains("Word\tNumber\tCase\tDefinite Form\tIndefinite Form\tWithout Article\tQuestion\tMeaning"))
        assertTrue(tsv.contains("der Tisch\tSingular\tNominativ\tder Tisch\tein Tisch\tTisch\tWer oder was?\ttable"))
        assertTrue(tsv.contains("der Tisch\tPlural\tDativ\tden Tischen\tkeinen Tischen\tTischen\tWem?\ttable"))
    }

    @Test
    fun testGeneratePrintableHtml_containsCompleteTableStructure() {
        val word = createSampleWord()
        val html = DeclensionExportHelper.generatePrintableHtml(listOf(word))

        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Deutsche Nomen — Deklinationstabelle"))
        assertTrue(html.contains("der Tisch"))
        assertTrue(html.contains("Singular"))
        assertTrue(html.contains("Plural"))
        assertTrue(html.contains("Nominativ"))
        assertTrue(html.contains("Akkusativ"))
        assertTrue(html.contains("Dativ"))
        assertTrue(html.contains("Genitiv"))
        assertTrue(html.contains("table"))
    }
}
