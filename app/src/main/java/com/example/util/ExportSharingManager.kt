package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.WordDeclensionResult
import java.io.File
import java.io.OutputStream

/**
 * Handles exporting and sharing PDF and Excel (.xlsx) files
 * with customized file names, saving via Storage Access Framework or Downloads,
 * and sharing via Android share sheet.
 */
object ExportSharingManager {

    enum class ExportFormat(val extension: String, val mimeType: String) {
        PDF("pdf", "application/pdf"),
        XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    /**
     * Sanitizes a base file name by removing invalid file system characters,
     * stripping any file extensions (.pdf, .xlsx), and removing any leading dots (.)
     * so that the file is never created as a hidden file on Android (e.g. Xiaomi / MIUI).
     * The resulting name will always start with a letter.
     */
    fun sanitizeFileName(rawName: String, defaultName: String = "Suchverlauf_Deutsch"): String {
        val cleanDefault = defaultName.trim().trimStart('.', '_', ' ').ifBlank { "Suchverlauf_Deutsch" }
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) return cleanDefault

        // Remove common extensions case-insensitively
        val withoutExt = trimmed
            .replace(Regex("""\.(pdf|xlsx)$""", RegexOption.IGNORE_CASE), "")
            .trim()

        // Replace illegal filesystem characters
        val sanitized = withoutExt.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()

        // STRICT FIX: Strip ALL leading dots (.) and spaces. In Android/Linux, files starting with '.' are hidden files.
        var noLeadingDots = sanitized.trimStart('.', ' ')
        // Also strip any leading underscores so the file begins with a visible character
        noLeadingDots = noLeadingDots.trimStart('_')

        return if (noLeadingDots.isBlank()) {
            cleanDefault
        } else {
            noLeadingDots
        }
    }

    /**
     * Builds a clean, fully-qualified file name with extension.
     * Guarantees that the name never starts with a dot (.) and begins with a valid letter.
     * If a notebookName is provided, ensures it is incorporated into the file name (e.g. for XLSX).
     */
    fun buildFileName(
        baseName: String,
        format: ExportFormat,
        notebookName: String = ""
    ): String {
        var safeBase = sanitizeFileName(baseName)
        val cleanNb = notebookName.trim().trimStart('.', '_', ' ')
        if (cleanNb.isNotBlank()) {
            val sanitizedNb = sanitizeFileName(cleanNb)
            if (sanitizedNb.isNotBlank() && !safeBase.contains(sanitizedNb, ignoreCase = true)) {
                safeBase = "${safeBase}_$sanitizedNb"
            }
        }
        val ext = format.extension.trimStart('.').lowercase()
        val fullName = "$safeBase.$ext"
        return fullName.trimStart('.')
    }

    /**
     * Writes either PDF or XLSX into an output stream directly.
     */
    fun writeToStream(
        context: Context,
        words: List<WordDeclensionResult>,
        format: ExportFormat,
        outputStream: OutputStream,
        customBaseName: String = "Suchverlauf_Deutsch",
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        writeUnifiedToStream(
            context = context,
            nouns = words,
            verbs = emptyList(),
            scope = DeclensionHistoryExportHelper.ExportScope.NOMEN,
            format = format,
            outputStream = outputStream,
            customBaseName = customBaseName,
            typography = typography,
            notebookName = notebookName
        )
    }

    /**
     * Unified stream writer handling Nouns, Verbs, or both (Scope).
     */
    fun writeUnifiedToStream(
        context: Context,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: DeclensionHistoryExportHelper.ExportScope,
        format: ExportFormat,
        outputStream: OutputStream,
        customBaseName: String = "Suchverlauf_Deutsch",
        paperSize: DeclensionHistoryExportHelper.PaperSizeOption = DeclensionHistoryExportHelper.PaperSizeOption.DEFAULT,
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val tempFile = File.createTempFile("export_", ".tmp", context.cacheDir)
        val safeBaseName = sanitizeFileName(customBaseName)
        try {
            when (format) {
                ExportFormat.PDF -> DeclensionHistoryExportHelper.createUnifiedPdfFile(tempFile, nouns, verbs, scope, safeBaseName, paperSize, typography, notebookName)
                ExportFormat.XLSX -> DeclensionHistoryExportHelper.createUnifiedXlsxFile(tempFile, nouns, verbs, scope, safeBaseName, typography, notebookName)
            }
            tempFile.inputStream().use { input ->
                input.copyTo(outputStream)
            }
            outputStream.flush()
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Generates the export file in the app cache and shares it via the Android share sheet.
     */
    fun shareSearchHistory(
        context: Context,
        words: List<WordDeclensionResult>,
        format: ExportFormat,
        customBaseName: String = "Suchverlauf_Deutsch",
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        shareUnifiedSearchHistory(
            context = context,
            nouns = words,
            verbs = emptyList(),
            scope = DeclensionHistoryExportHelper.ExportScope.NOMEN,
            format = format,
            customBaseName = customBaseName,
            typography = typography,
            notebookName = notebookName
        )
    }

    /**
     * Unified share handling Nouns, Verbs, or both (Scope) via Android share sheet.
     */
    fun shareUnifiedSearchHistory(
        context: Context,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: DeclensionHistoryExportHelper.ExportScope,
        format: ExportFormat,
        customBaseName: String = "Suchverlauf_Deutsch",
        paperSize: DeclensionHistoryExportHelper.PaperSizeOption = DeclensionHistoryExportHelper.PaperSizeOption.DEFAULT,
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val hasContent = when (scope) {
            DeclensionHistoryExportHelper.ExportScope.NOMEN -> nouns.isNotEmpty()
            DeclensionHistoryExportHelper.ExportScope.VERB -> verbs.isNotEmpty()
            DeclensionHistoryExportHelper.ExportScope.ALLE -> nouns.isNotEmpty() || verbs.isNotEmpty()
        }

        if (!hasContent) {
            Toast.makeText(context, "Keine Einträge zum Teilen vorhanden", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val safeBaseName = sanitizeFileName(customBaseName)
            val fileName = buildFileName(safeBaseName, format, notebookName)
            val file = File(exportDir, fileName)

            when (format) {
                ExportFormat.PDF -> DeclensionHistoryExportHelper.createUnifiedPdfFile(file, nouns, verbs, scope, safeBaseName, paperSize, typography, notebookName)
                ExportFormat.XLSX -> DeclensionHistoryExportHelper.createUnifiedXlsxFile(file, nouns, verbs, scope, safeBaseName, typography, notebookName)
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_SUBJECT, safeBaseName)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Teilen: $fileName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Fehler beim Teilen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Fallback save: saves to the Downloads directory and opens a view/open chooser.
     */
    fun saveToDownloads(
        context: Context,
        words: List<WordDeclensionResult>,
        format: ExportFormat,
        customBaseName: String = "Suchverlauf_Deutsch",
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        saveUnifiedToDownloads(
            context = context,
            nouns = words,
            verbs = emptyList(),
            scope = DeclensionHistoryExportHelper.ExportScope.NOMEN,
            format = format,
            customBaseName = customBaseName,
            typography = typography,
            notebookName = notebookName
        )
    }

    /**
     * Unified fallback save for Downloads directory.
     */
    fun saveUnifiedToDownloads(
        context: Context,
        nouns: List<WordDeclensionResult>,
        verbs: List<com.example.data.model.VerbConjugationResult>,
        scope: DeclensionHistoryExportHelper.ExportScope,
        format: ExportFormat,
        customBaseName: String = "Suchverlauf_Deutsch",
        paperSize: DeclensionHistoryExportHelper.PaperSizeOption = DeclensionHistoryExportHelper.PaperSizeOption.DEFAULT,
        typography: DeclensionHistoryExportHelper.ExportTypographyOptions = DeclensionHistoryExportHelper.ExportTypographyOptions.DEFAULT,
        notebookName: String = ""
    ) {
        val hasContent = when (scope) {
            DeclensionHistoryExportHelper.ExportScope.NOMEN -> nouns.isNotEmpty()
            DeclensionHistoryExportHelper.ExportScope.VERB -> verbs.isNotEmpty()
            DeclensionHistoryExportHelper.ExportScope.ALLE -> nouns.isNotEmpty() || verbs.isNotEmpty()
        }

        if (!hasContent) {
            Toast.makeText(context, "Keine Einträge zum Speichern vorhanden", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val safeBaseName = sanitizeFileName(customBaseName)
            val fileName = buildFileName(safeBaseName, format, notebookName)
            val file = File(exportDir, fileName)

            when (format) {
                ExportFormat.PDF -> DeclensionHistoryExportHelper.createUnifiedPdfFile(file, nouns, verbs, scope, safeBaseName, paperSize, typography, notebookName)
                ExportFormat.XLSX -> DeclensionHistoryExportHelper.createUnifiedXlsxFile(file, nouns, verbs, scope, safeBaseName, typography, notebookName)
            }

            var savedToDownloads = false
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && downloadsDir.exists()) {
                    val destFile = File(downloadsDir, fileName)
                    file.copyTo(destFile, overwrite = true)
                    savedToDownloads = true

                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    downloadManager?.addCompletedDownload(
                        fileName,
                        safeBaseName,
                        true,
                        format.mimeType,
                        destFile.absolutePath,
                        destFile.length(),
                        true
                    )
                }
            } catch (_: Exception) {
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, format.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(viewIntent, "Datei öffnen: $fileName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val msg = if (savedToDownloads) {
                "In Downloads gespeichert: $fileName"
            } else {
                "Gespeichert: $fileName"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Fehler beim Speichern: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares an arbitrary file (e.g. ZIP archive, PDF, XLSX) via Android share sheet.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Fehler beim Teilen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Backward-compatible alias for existing calls.
     */
    fun downloadSearchHistory(
        context: Context,
        words: List<WordDeclensionResult>,
        format: ExportFormat,
        customBaseName: String = "Suchverlauf_Deutsch",
        notebookName: String = ""
    ) {
        saveToDownloads(context, words, format, customBaseName, notebookName = notebookName)
    }
}
