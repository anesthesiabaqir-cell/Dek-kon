package com.example.data.notebook

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.example.data.local.WordHistoryEntity
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Repository managing the "Notizbücher" (Autonomous Learning Workbooks) hierarchy.
 * Location: Documents/Deklination/Notizbücher/<NotebookName>/
 * Contains:
 *   - settings.json (independent settings: theme color, auto/light/dark mode, AI, TTS, export)
 *   - history.json (independent word declension/conjugation records)
 */
class NotebookRepository(private val context: Context) {

    companion object {
        private const val TAG = "NotebookRepository"
        const val ROOT_FOLDER_NAME = "Deklination/Notizbücher"
        const val DEFAULT_NOTEBOOK_ID = "Allgemein"
        const val SETTINGS_FILE_NAME = "settings.json"
        const val HISTORY_FILE_NAME = "history.json"
        private const val PREFS_NAME = "notebook_system_prefs"
        private const val PREF_ACTIVE_NOTEBOOK = "active_notebook_id"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        ensureInitialized()
    }

    /**
     * Resolves the root directory: Documents/Deklination/Notizbücher/
     */
    fun getRootDirectory(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val primaryDir = File(publicDocs, ROOT_FOLDER_NAME)
        if (primaryDir.exists() || primaryDir.mkdirs()) {
            return primaryDir
        }
        val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), ROOT_FOLDER_NAME)
        fallbackDir.mkdirs()
        return fallbackDir
    }

    /**
     * Ensures the root directory and the default "Allgemein" notebook exist.
     * Also migrates legacy history and preferences if present.
     */
    @Synchronized
    fun ensureInitialized() {
        val root = getRootDirectory()
        val notebookDirs = root.listFiles { file -> file.isDirectory }

        if (notebookDirs.isNullOrEmpty()) {
            val defaultFolder = File(root, DEFAULT_NOTEBOOK_ID)
            defaultFolder.mkdirs()

            // Initialize default settings.json
            val defaultSettings = NotebookSettings(
                notebookName = DEFAULT_NOTEBOOK_ID,
                themeColorId = "Schiefer",
                themeMode = "auto"
            )
            val settingsFile = File(defaultFolder, SETTINGS_FILE_NAME)
            try {
                settingsFile.writeText(defaultSettings.toJson())
            } catch (e: Exception) {
                Log.e(TAG, "Error creating initial settings.json", e)
            }

            // Check if legacy Documents/Deklination/history.json exists and migrate
            val legacyFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "Deklination/history.json"
            )
            val defaultHistoryFile = File(defaultFolder, HISTORY_FILE_NAME)
            if (legacyFile.exists() && legacyFile.length() > 0) {
                try {
                    legacyFile.copyTo(defaultHistoryFile, overwrite = true)
                } catch (e: Exception) {
                    defaultHistoryFile.writeText("[]")
                }
            } else {
                defaultHistoryFile.writeText("[]")
            }

            prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, DEFAULT_NOTEBOOK_ID).apply()
        }
    }

    fun getActiveNotebookId(): String {
        val saved = prefs.getString(PREF_ACTIVE_NOTEBOOK, null)
        if (!saved.isNullOrBlank()) {
            val folder = File(getRootDirectory(), saved)
            if (folder.exists() && folder.isDirectory) {
                return saved
            }
        }
        val firstAvailable = listAllNotebooks().firstOrNull()?.id ?: DEFAULT_NOTEBOOK_ID
        prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, firstAvailable).apply()
        return firstAvailable
    }

    fun setActiveNotebookId(id: String) {
        val folder = File(getRootDirectory(), id)
        if (folder.exists() && folder.isDirectory) {
            prefs.edit().putString(PREF_ACTIVE_NOTEBOOK, id).apply()
        }
    }

    /**
     * Lists all notebooks found in the root directory.
     */
    @Synchronized
    fun listAllNotebooks(): List<Notebook> {
        val root = getRootDirectory()
        val dirs = root.listFiles { file -> file.isDirectory } ?: emptyArray()

        val list = dirs.mapNotNull { dir ->
            try {
                val settingsFile = File(dir, SETTINGS_FILE_NAME)
                val settings = if (settingsFile.exists()) {
                    NotebookSettings.fromJson(settingsFile.readText(), fallbackName = dir.name)
                } else {
                    NotebookSettings(notebookName = dir.name)
                }

                val historyFile = File(dir, HISTORY_FILE_NAME)
                val wordCount = if (historyFile.exists()) {
                    val content = historyFile.readText().trim()
                    if (content.isNotEmpty() && content != "[]") {
                        try {
                            JSONArray(content).length()
                        } catch (e: Exception) {
                            0
                        }
                    } else 0
                } else 0

                Notebook(
                    id = dir.name,
                    name = settings.notebookName.ifBlank { dir.name },
                    lastModified = maxOf(dir.lastModified(), settingsFile.lastModified(), historyFile.lastModified()),
                    wordCount = wordCount,
                    settings = settings
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error reading notebook directory: ${dir.name}", e)
                null
            }
        }.sortedByDescending { it.lastModified }

        return if (list.isEmpty()) {
            ensureInitialized()
            listAllNotebooks()
        } else {
            list
        }
    }

    fun getActiveNotebook(): Notebook {
        val activeId = getActiveNotebookId()
        val all = listAllNotebooks()
        return all.find { it.id == activeId } ?: all.first()
    }

    /**
     * Creates a new notebook directory with settings.json and empty history.json.
     */
    @Synchronized
    fun createNotebook(name: String, initialThemeColorId: String = "Schiefer", themeMode: String = "auto"): Notebook {
        val cleanName = sanitizeFileName(name.trim().ifBlank { "Neues Notizbuch" })
        var folderName = cleanName
        var counter = 1
        val root = getRootDirectory()

        while (File(root, folderName).exists()) {
            folderName = "${cleanName}_$counter"
            counter++
        }

        val folder = File(root, folderName)
        folder.mkdirs()

        val settings = NotebookSettings(
            notebookName = name.trim().ifBlank { folderName },
            themeColorId = initialThemeColorId,
            themeMode = themeMode
        )

        File(folder, SETTINGS_FILE_NAME).writeText(settings.toJson())
        File(folder, HISTORY_FILE_NAME).writeText("[]")

        val notebook = Notebook(
            id = folderName,
            name = settings.notebookName,
            lastModified = System.currentTimeMillis(),
            wordCount = 0,
            settings = settings
        )

        setActiveNotebookId(folderName)
        return notebook
    }

    /**
     * Renames a notebook (both directory and display name in settings.json).
     */
    @Synchronized
    fun renameNotebook(notebookId: String, newName: String): Notebook {
        val root = getRootDirectory()
        val oldFolder = File(root, notebookId)
        val cleanName = sanitizeFileName(newName.trim().ifBlank { notebookId })

        val targetFolder = if (cleanName != notebookId && !File(root, cleanName).exists()) {
            val newFile = File(root, cleanName)
            if (oldFolder.renameTo(newFile)) {
                if (getActiveNotebookId() == notebookId) {
                    setActiveNotebookId(cleanName)
                }
                newFile
            } else {
                oldFolder
            }
        } else {
            oldFolder
        }

        val effectiveId = targetFolder.name
        val settingsFile = File(targetFolder, SETTINGS_FILE_NAME)
        val currentSettings = if (settingsFile.exists()) {
            NotebookSettings.fromJson(settingsFile.readText(), fallbackName = newName)
        } else {
            NotebookSettings(notebookName = newName)
        }

        val updatedSettings = currentSettings.copy(notebookName = newName.trim().ifBlank { effectiveId })
        settingsFile.writeText(updatedSettings.toJson())

        return getActiveNotebook()
    }

    /**
     * Deletes a notebook and its directory completely.
     * Protected: Cannot delete the last remaining notebook.
     */
    @Synchronized
    fun deleteNotebook(notebookId: String): Boolean {
        val all = listAllNotebooks()
        if (all.size <= 1) {
            return false // Cannot delete the only existing notebook
        }

        val folder = File(getRootDirectory(), notebookId)
        if (folder.exists()) {
            folder.deleteRecursively()
        }

        if (getActiveNotebookId() == notebookId) {
            val remaining = listAllNotebooks()
            setActiveNotebookId(remaining.firstOrNull()?.id ?: DEFAULT_NOTEBOOK_ID)
        }
        return true
    }

    /**
     * Saves settings for the currently active notebook.
     */
    @Synchronized
    fun saveActiveSettings(settings: NotebookSettings): Boolean {
        return saveNotebookSettings(getActiveNotebookId(), settings)
    }

    @Synchronized
    fun saveNotebookSettings(notebookId: String, settings: NotebookSettings): Boolean {
        return try {
            val folder = File(getRootDirectory(), notebookId)
            folder.mkdirs()
            val file = File(folder, SETTINGS_FILE_NAME)
            file.writeText(settings.toJson())
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notebook settings", e)
            false
        }
    }

    /**
     * Loads the search history items for the active notebook.
     */
    @Synchronized
    fun loadActiveHistory(): List<WordHistoryEntity> {
        return loadNotebookHistory(getActiveNotebookId())
    }

    @Synchronized
    fun loadNotebookHistory(notebookId: String): List<WordHistoryEntity> {
        val folder = File(getRootDirectory(), notebookId)
        val file = File(folder, HISTORY_FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val content = file.readText().trim()
            parseJsonHistory(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notebook history for $notebookId", e)
            emptyList()
        }
    }

    /**
     * Saves the search history items to the active notebook.
     */
    @Synchronized
    fun saveActiveHistory(items: List<WordHistoryEntity>): Boolean {
        val folder = File(getRootDirectory(), getActiveNotebookId())
        folder.mkdirs()
        val file = File(folder, HISTORY_FILE_NAME)
        return try {
            val jsonArray = serializeHistoryToJson(items)
            file.writeText(jsonArray.toString(2))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving active notebook history", e)
            false
        }
    }

    @Synchronized
    fun appendOrUpdateItemInActiveHistory(item: WordHistoryEntity): Boolean {
        val items = loadActiveHistory().toMutableList()
        items.removeAll {
            it.word.equals(item.word, ignoreCase = true) &&
            it.type.equals(item.type, ignoreCase = true)
        }
        items.add(0, item)
        return saveActiveHistory(items)
    }

    @Synchronized
    fun deleteItemInActiveHistory(id: Long, word: String? = null, type: String? = null): Boolean {
        val items = loadActiveHistory().toMutableList()
        val removed = items.removeAll {
            it.id == id || (word != null && type != null &&
                it.word.equals(word, ignoreCase = true) &&
                it.type.equals(type, ignoreCase = true))
        }
        return if (removed) {
            saveActiveHistory(items)
        } else {
            true
        }
    }

    @Synchronized
    fun clearActiveHistory(): Boolean {
        return saveActiveHistory(emptyList())
    }

    // =========================================================================
    // ZIP Export and Import
    // =========================================================================

    /**
     * Compresses a notebook folder (settings.json + history.json) into a .zip file.
     */
    fun exportNotebookToZip(notebookId: String, destZipFile: File): File {
        val folder = File(getRootDirectory(), notebookId)
        if (!folder.exists()) {
            throw IllegalArgumentException("Notebook folder does not exist: $notebookId")
        }

        destZipFile.parentFile?.mkdirs()
        ZipOutputStream(BufferedOutputStream(FileOutputStream(destZipFile))).use { zos ->
            val files = folder.listFiles() ?: emptyArray()
            val buffer = ByteArray(4096)

            for (file in files) {
                if (file.isFile) {
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis ->
                        var len: Int
                        while (fis.read(buffer).also { len = it } > 0) {
                            zos.write(buffer, 0, len)
                        }
                    }
                    zos.closeEntry()
                }
            }
        }
        return destZipFile
    }

    /**
     * Imports a notebook from a ZIP input stream.
     * Extracts settings.json and history.json into a new or updated notebook folder.
     */
    fun importNotebookFromZip(zipInputStream: InputStream, preferredName: String? = null): Notebook? {
        val root = getRootDirectory()
        var notebookName = preferredName ?: "Importiertes_Notizbuch"
        var tempSettingsContent: String? = null
        var tempHistoryContent: String? = null

        ZipInputStream(BufferedInputStream(zipInputStream)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            val buffer = ByteArray(4096)

            while (entry != null) {
                val fileName = File(entry.name).name
                val outBytes = java.io.ByteArrayOutputStream()
                var count: Int
                while (zis.read(buffer).also { count = it } != -1) {
                    outBytes.write(buffer, 0, count)
                }

                val content = outBytes.toString(Charsets.UTF_8.name())
                if (fileName.equals(SETTINGS_FILE_NAME, ignoreCase = true)) {
                    tempSettingsContent = content
                    try {
                        val parsed = NotebookSettings.fromJson(content)
                        if (parsed.notebookName.isNotBlank()) {
                            notebookName = parsed.notebookName
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error extracting name from imported settings: ${e.message}")
                    }
                } else if (fileName.equals(HISTORY_FILE_NAME, ignoreCase = true)) {
                    tempHistoryContent = content
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (tempSettingsContent == null && tempHistoryContent == null) {
            return null // Invalid zip file
        }

        val cleanName = sanitizeFileName(notebookName)
        var folderName = cleanName
        var counter = 1
        while (File(root, folderName).exists()) {
            folderName = "${cleanName}_$counter"
            counter++
        }

        val targetDir = File(root, folderName)
        targetDir.mkdirs()

        val settings = if (tempSettingsContent != null) {
            NotebookSettings.fromJson(tempSettingsContent!!, fallbackName = folderName).copy(
                notebookName = notebookName
            )
        } else {
            NotebookSettings(notebookName = notebookName)
        }

        File(targetDir, SETTINGS_FILE_NAME).writeText(settings.toJson())
        File(targetDir, HISTORY_FILE_NAME).writeText(tempHistoryContent ?: "[]")

        val notebook = Notebook(
            id = folderName,
            name = settings.notebookName,
            lastModified = System.currentTimeMillis(),
            wordCount = if (tempHistoryContent != null) {
                try { JSONArray(tempHistoryContent!!).length() } catch (e: Exception) { 0 }
            } else 0,
            settings = settings
        )

        setActiveNotebookId(folderName)
        return notebook
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    private fun serializeHistoryToJson(items: List<WordHistoryEntity>): JSONArray {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("word", item.word)
                put("type", item.type)
                put("data", item.data)
                put("timestamp", item.timestamp)
                put("gender", item.gender)
                put("genderArticle", item.genderArticle)
                put("meaningEnglish", item.meaningEnglish)
                put("rawJsonResult", item.rawJsonResult)
                if (item.type.equals("Verb", ignoreCase = true)) {
                    val details = extractVerbQuickDetails(item)
                    put("verb", details.verb.ifBlank { item.word })
                    put("auxiliary", details.auxiliary.removeSurrounding("(", ")"))
                    put("praeteritum", details.praeteritum)
                    put("partizip1", details.partizip1)
                    put("partizip2", details.partizip2)
                    put("verb_type", details.verbType)
                    put("separable", details.separable)
                    put("case_object", details.caseObject)
                    put("english", details.englishMeaning)
                } else {
                    val details = extractNounQuickDetails(item)
                    put("singular", details.singular)
                    put("plural", details.plural)
                    put("pluralNoun", details.plural)
                    put("genitive", details.genitiveSingular)
                    put("genitiveSingular", details.genitiveSingular)
                    put("dativePlural", details.dativePlural)
                    put("english", details.englishMeaning)
                }
            }
            jsonArray.put(obj)
        }
        return jsonArray
    }

    private fun parseJsonHistory(jsonString: String): List<WordHistoryEntity> {
        val result = mutableListOf<WordHistoryEntity>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return result

        try {
            val jsonArray = JSONArray(trimmed)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val word = obj.optString("word").ifBlank { obj.optString("verb", "") }
                val type = obj.optString("type", "Nomen")
                val meaningEnglish = obj.optString("meaningEnglish").ifBlank { obj.optString("english", "") }
                val gender = obj.optString("gender").ifBlank { obj.optString("auxiliary", "") }
                val genderArticle = obj.optString("genderArticle").ifBlank { obj.optString("partizip2", "") }
                val data = obj.optString("data", "").ifBlank { obj.toString() }
                val rawJsonResult = obj.optString("rawJsonResult", "").ifBlank { data }

                result.add(
                    WordHistoryEntity(
                        id = obj.optLong("id", 0L),
                        word = word,
                        type = type,
                        data = data,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        gender = gender,
                        genderArticle = genderArticle,
                        meaningEnglish = meaningEnglish,
                        rawJsonResult = rawJsonResult
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing history json", e)
        }
        return result
    }
}
