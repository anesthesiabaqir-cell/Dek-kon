package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.WordDeclensionResult
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object DeclensionExportHelper {

    /**
     * Generates a CSV file formatted for Microsoft Excel with UTF-8 BOM,
     * so that special characters and umlauts (ä, ö, ü, ß) open cleanly in Excel without encoding issues.
     */
    fun generateExcelCsv(words: List<WordDeclensionResult>): String {
        val sb = StringBuilder()
        // Prepend UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")

        // Header columns as requested:
        // Word, Number (Singular/Plural), Case (Grammatical cases), Definite form, Indefinite form, Without Article, Meaning
        sb.append(escapeCsv("Word (Wort)"))
            .append(",")
            .append(escapeCsv("Number (Numerus)"))
            .append(",")
            .append(escapeCsv("Case (Kasus)"))
            .append(",")
            .append(escapeCsv("Definite Form (Bestimmter Artikel)"))
            .append(",")
            .append(escapeCsv("Indefinite Form (Unbestimmter Artikel)"))
            .append(",")
            .append(escapeCsv("Without Article (Ohne Artikel)"))
            .append(",")
            .append(escapeCsv("Case Question (Frage)"))
            .append(",")
            .append(escapeCsv("Meaning (Bedeutung)"))
            .append("\r\n")

        for (item in words) {
            val baseWord = "${item.genderArticle} ${item.word}"

            // Singular Rows
            for (row in item.singular.rows) {
                sb.append(escapeCsv(baseWord)).append(",")
                    .append(escapeCsv("Singular")).append(",")
                    .append(escapeCsv(row.caseNameDe)).append(",")
                    .append(escapeCsv(row.definite)).append(",")
                    .append(escapeCsv(row.indefinite)).append(",")
                    .append(escapeCsv(row.noArticle)).append(",")
                    .append(escapeCsv(row.caseQuestionDe)).append(",")
                    .append(escapeCsv(item.meaningEnglish))
                    .append("\r\n")
            }

            // Plural Rows
            for (row in item.plural.rows) {
                sb.append(escapeCsv(baseWord)).append(",")
                    .append(escapeCsv("Plural")).append(",")
                    .append(escapeCsv(row.caseNameDe)).append(",")
                    .append(escapeCsv(row.definite)).append(",")
                    .append(escapeCsv(row.indefinite)).append(",")
                    .append(escapeCsv(row.noArticle)).append(",")
                    .append(escapeCsv(row.caseQuestionDe)).append(",")
                    .append(escapeCsv(item.meaningEnglish))
                    .append("\r\n")
            }
        }

        return sb.toString()
    }

    /**
     * Generates Tab-Separated Values (TSV) for direct clipboard pasting into Excel or Google Sheets.
     */
    fun generateExcelTsv(words: List<WordDeclensionResult>): String {
        val sb = StringBuilder()
        sb.append("Word\tNumber\tCase\tDefinite Form\tIndefinite Form\tWithout Article\tQuestion\tMeaning\n")

        for (item in words) {
            val baseWord = "${item.genderArticle} ${item.word}"
            for (row in item.singular.rows) {
                sb.append("$baseWord\tSingular\t${row.caseNameDe}\t${row.definite}\t${row.indefinite}\t${row.noArticle}\t${row.caseQuestionDe}\t${item.meaningEnglish}\n")
            }
            for (row in item.plural.rows) {
                sb.append("$baseWord\tPlural\t${row.caseNameDe}\t${row.definite}\t${row.indefinite}\t${row.noArticle}\t${row.caseQuestionDe}\t${item.meaningEnglish}\n")
            }
        }
        return sb.toString()
    }

    /**
     * Copies the table data to the clipboard in Excel-ready format.
     */
    fun copyToClipboard(context: Context, words: List<WordDeclensionResult>) {
        if (words.isEmpty()) {
            Toast.makeText(context, "No words available to copy", Toast.LENGTH_SHORT).show()
            return
        }
        val tsvData = generateExcelTsv(words)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("German Declension Table", tsvData)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Table copied to clipboard (ready to paste into Excel)!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Exports the Excel CSV file and opens the Android share sheet to save or open in Excel / Sheets.
     */
    fun exportAndShareExcelCsv(
        context: Context,
        words: List<WordDeclensionResult>,
        fileNamePrefix: String = "deklination_tabelle"
    ) {
        if (words.isEmpty()) {
            Toast.makeText(context, "No words available to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val csvContent = generateExcelCsv(words)
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val sanitizedPrefix = fileNamePrefix.trimStart('.', '_', ' ')
                .replace(Regex("[^a-zA-Z0-9_]"), "_")
                .trimStart('_', '.')
                .ifBlank { "deklination_tabelle" }
            val file = File(exportDir, "${sanitizedPrefix}_${System.currentTimeMillis()}.csv")

            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                    writer.flush()
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "German Noun Declension Table (Excel)")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Open or Share Excel Table")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Renders an HTML document specifically styled for printing or saving as PDF.
     */
    fun generatePrintableHtml(words: List<WordDeclensionResult>): String {
        val sb = StringBuilder()
        sb.append("""
            <!DOCTYPE html>
            <html lang="de">
            <head>
                <meta charset="UTF-8">
                <title>Deutsche Nomen Deklinationstabelle</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        margin: 20px;
                        color: #0F172A;
                        background: #FFFFFF;
                        font-size: 12px;
                    }
                    h1 {
                        font-size: 20px;
                        color: #1E3A8A;
                        margin-bottom: 4px;
                    }
                    p.subtitle {
                        font-size: 12px;
                        color: #64748B;
                        margin-top: 0;
                        margin-bottom: 16px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 24px;
                        page-break-inside: avoid;
                    }
                    th {
                        background-color: #1E3A8A;
                        color: #FFFFFF;
                        text-align: left;
                        padding: 8px 10px;
                        font-size: 11px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        border: 1px solid #1E3A8A;
                    }
                    td {
                        padding: 6px 10px;
                        border: 1px solid #CBD5E1;
                        font-size: 11px;
                    }
                    tr.singular-row {
                        background-color: #FFFFFF;
                    }
                    tr.singular-row:nth-child(even) {
                        background-color: #F8FAFC;
                    }
                    tr.plural-row {
                        background-color: #F1F5F9;
                    }
                    tr.plural-row:nth-child(even) {
                        background-color: #E2E8F0;
                    }
                    .badge-singular {
                        display: inline-block;
                        padding: 2px 6px;
                        background: #DBEAFE;
                        color: #1E40AF;
                        border-radius: 4px;
                        font-weight: 600;
                        font-size: 10px;
                    }
                    .badge-plural {
                        display: inline-block;
                        padding: 2px 6px;
                        background: #FEF3C7;
                        color: #92400E;
                        border-radius: 4px;
                        font-weight: 600;
                        font-size: 10px;
                    }
                    .case-name {
                        font-weight: bold;
                        color: #1E293B;
                    }
                    .question {
                        font-size: 10px;
                        color: #64748B;
                        font-style: italic;
                    }
                    .word-title {
                        font-size: 15px;
                        font-weight: bold;
                        color: #1E3A8A;
                        margin-top: 18px;
                        margin-bottom: 6px;
                    }
                    @media print {
                        body {
                            margin: 10mm;
                            font-size: 10px;
                        }
                        th {
                            background-color: #1E3A8A !important;
                            color: #FFFFFF !important;
                            -webkit-print-color-adjust: exact;
                            print-color-adjust: exact;
                        }
                        tr.plural-row {
                            background-color: #F1F5F9 !important;
                            -webkit-print-color-adjust: exact;
                            print-color-adjust: exact;
                        }
                    }
                </style>
            </head>
            <body>
                <h1>Deutsche Nomen — Deklinationstabelle</h1>
                <p class="subtitle">Übersicht der Fälle: Nominativ, Akkusativ, Dativ, Genitiv (Singular &amp; Plural)</p>
        """.trimIndent())

        for (item in words) {
            val baseWord = "${item.genderArticle} ${item.word}"
            sb.append("""
                <div class="word-title">${escapeHtml(baseWord)} (${escapeHtml(item.gender)}) — <em>${escapeHtml(item.meaningEnglish)}</em></div>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 14%;">Word</th>
                            <th style="width: 11%;">Number</th>
                            <th style="width: 13%;">Case</th>
                            <th style="width: 22%;">Definite Form</th>
                            <th style="width: 22%;">Indefinite Form</th>
                            <th style="width: 18%;">Without Article</th>
                        </tr>
                    </thead>
                    <tbody>
            """.trimIndent())

            // Singular rows
            for (row in item.singular.rows) {
                sb.append("""
                    <tr class="singular-row">
                        <td><strong>${escapeHtml(baseWord)}</strong></td>
                        <td><span class="badge-singular">Singular</span></td>
                        <td><span class="case-name">${escapeHtml(row.caseNameDe)}</span><br><span class="question">${escapeHtml(row.caseQuestionDe)}</span></td>
                        <td><strong>${escapeHtml(row.definite)}</strong></td>
                        <td>${escapeHtml(row.indefinite)}</td>
                        <td>${escapeHtml(row.noArticle)}</td>
                    </tr>
                """.trimIndent())
            }

            // Plural rows
            for (row in item.plural.rows) {
                sb.append("""
                    <tr class="plural-row">
                        <td><strong>${escapeHtml(item.pluralNoun.ifEmpty { baseWord })}</strong></td>
                        <td><span class="badge-plural">Plural</span></td>
                        <td><span class="case-name">${escapeHtml(row.caseNameDe)}</span><br><span class="question">${escapeHtml(row.caseQuestionDe)}</span></td>
                        <td><strong>${escapeHtml(row.definite)}</strong></td>
                        <td>${escapeHtml(row.indefinite)}</td>
                        <td>${escapeHtml(row.noArticle)}</td>
                    </tr>
                """.trimIndent())
            }

            sb.append("</tbody></table>")
        }

        sb.append("""
                <footer style="margin-top: 30px; font-size: 10px; color: #94A3B8; text-align: center; border-top: 1px solid #E2E8F0; padding-top: 8px;">
                    Erstellt mit Deklination — Deutsche Nomen-Deklinationstabellen
                </footer>
            </body>
            </html>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * Invokes the Android native PrintManager to print the table directly to physical printer or save as PDF.
     */
    fun printDeclensionTable(
        context: Context,
        words: List<WordDeclensionResult>,
        jobTitle: String = "Deklination_Tabelle"
    ) {
        if (words.isEmpty()) {
            Toast.makeText(context, "No words available to print", Toast.LENGTH_SHORT).show()
            return
        }

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Printing service unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val htmlContent = generatePrintableHtml(words)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobTitle)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
                printManager.print(jobTitle, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun escapeCsv(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (containsSpecial) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            "\"$value\""
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
