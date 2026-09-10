package com.example

import com.example.util.ExportSharingManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNameSanitizationTest {

    @Test
    fun `test default file name produces Suchverlauf_Deutsch without leading dot`() {
        val base = ExportSharingManager.sanitizeFileName("")
        assertEquals("Suchverlauf_Deutsch", base)

        val pdfName = ExportSharingManager.buildFileName(base, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
        assertFalse("Filename must not start with a dot", pdfName.startsWith("."))
        assertTrue("Filename must start with a letter", pdfName.first().isLetter())

        val xlsxName = ExportSharingManager.buildFileName(base, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Suchverlauf_Deutsch.xlsx", xlsxName)
        assertFalse("Filename must not start with a dot", xlsxName.startsWith("."))
        assertTrue("Filename must start with a letter", xlsxName.first().isLetter())
    }

    @Test
    fun `test leading dot is removed from filename`() {
        val inputWithDot = ".Suchverlauf_Deutsch"
        val sanitized = ExportSharingManager.sanitizeFileName(inputWithDot)
        assertEquals("Suchverlauf_Deutsch", sanitized)

        val pdfName = ExportSharingManager.buildFileName(inputWithDot, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
        assertFalse("Filename must not start with a dot", pdfName.startsWith("."))

        val xlsxName = ExportSharingManager.buildFileName(inputWithDot, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Suchverlauf_Deutsch.xlsx", xlsxName)
        assertFalse("Filename must not start with a dot", xlsxName.startsWith("."))
    }

    @Test
    fun `test multiple leading dots and extensions are sanitized properly`() {
        val input = "...Suchverlauf_Deutsch.pdf"
        val sanitized = ExportSharingManager.sanitizeFileName(input)
        assertEquals("Suchverlauf_Deutsch", sanitized)

        val pdfName = ExportSharingManager.buildFileName(input, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", pdfName)
    }

    @Test
    fun `test dot only or extension only inputs fallback to default name without leading dot`() {
        val dotInput = "."
        val sanitizedDot = ExportSharingManager.sanitizeFileName(dotInput)
        assertEquals("Suchverlauf_Deutsch", sanitizedDot)

        val pdfOnly = ".pdf"
        val sanitizedPdf = ExportSharingManager.sanitizeFileName(pdfOnly)
        assertEquals("Suchverlauf_Deutsch", sanitizedPdf)

        val xlsxOnly = ".xlsx"
        val sanitizedXlsx = ExportSharingManager.sanitizeFileName(xlsxOnly)
        assertEquals("Suchverlauf_Deutsch", sanitizedXlsx)

        val finalPdf = ExportSharingManager.buildFileName(pdfOnly, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Suchverlauf_Deutsch.pdf", finalPdf)
        assertFalse(finalPdf.startsWith("."))
    }

    @Test
    fun `test custom user name is preserved without leading dot`() {
        val customName = "Meine_Wortliste"
        val sanitized = ExportSharingManager.sanitizeFileName(customName)
        assertEquals("Meine_Wortliste", sanitized)

        val pdfName = ExportSharingManager.buildFileName(customName, ExportSharingManager.ExportFormat.PDF)
        assertEquals("Meine_Wortliste.pdf", pdfName)

        val xlsxName = ExportSharingManager.buildFileName(customName, ExportSharingManager.ExportFormat.XLSX)
        assertEquals("Meine_Wortliste.xlsx", xlsxName)
    }

    @Test
    fun `test notebook name is included in file name`() {
        val xlsxWithNotebook = ExportSharingManager.buildFileName(
            baseName = "Suchverlauf_Deutsch",
            format = ExportSharingManager.ExportFormat.XLSX,
            notebookName = "B2_Prüfung"
        )
        assertEquals("Suchverlauf_Deutsch_B2_Prüfung.xlsx", xlsxWithNotebook)

        // If base name already has notebook name, avoid duplicating
        val alreadyHasIt = ExportSharingManager.buildFileName(
            baseName = "Suchverlauf_B2_Prüfung",
            format = ExportSharingManager.ExportFormat.XLSX,
            notebookName = "B2_Prüfung"
        )
        assertEquals("Suchverlauf_B2_Prüfung.xlsx", alreadyHasIt)
    }
}
