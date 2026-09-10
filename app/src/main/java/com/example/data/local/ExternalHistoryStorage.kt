package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject
import com.example.data.model.extractNounQuickDetails
import com.example.data.model.extractVerbQuickDetails
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Manages persistent storage of search history in a public external storage directory:
 * Documents/Deklination/history.json or a user-selected SAF folder.
 *
 * This allows search history (nouns and verbs) to survive a "Clear App Data" action,
 * since the external folder is not wiped by app data deletion.
 */
class ExternalHistoryStorage(private val context: Context) {

    companion object {
        private const val TAG = "ExternalHistoryStorage"
        const val SUBDIR_NAME = "Deklination"
        const val FILE_NAME = "history.json"
        const val PREFS_NAME = "history_folder_prefs"
        const val PREF_FOLDER_URI = "folder_tree_uri"
        const val PREF_FOLDER_NAME = "folder_display_name"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasFolderUri(): Boolean {
        val uriString = prefs.getString(PREF_FOLDER_URI, null)
        return !uriString.isNullOrBlank()
    }

    fun getFolderUri(): Uri? {
        val uriString = prefs.getString(PREF_FOLDER_URI, null) ?: return null
        return try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }
    }

    fun getFolderName(): String {
        return prefs.getString(PREF_FOLDER_NAME, null) ?: "Kein Ordner ausgewählt"
    }

    fun saveFolderUri(uri: Uri, preferredName: String? = null): String {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w(TAG, "takePersistableUriPermission note: ${e.message}")
        }

        val docName = try {
            DocumentFile.fromTreeUri(context, uri)?.name
        } catch (e: Exception) {
            null
        }

        val displayName = preferredName?.takeIf { it.isNotBlank() }
            ?: docName?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast(':')?.takeIf { it.isNotBlank() }
            ?: "Deklination"

        prefs.edit()
            .putString(PREF_FOLDER_URI, uri.toString())
            .putString(PREF_FOLDER_NAME, displayName)
            .apply()

        return displayName
    }

    fun clearFolderPreference() {
        prefs.edit()
            .remove(PREF_FOLDER_URI)
            .remove(PREF_FOLDER_NAME)
            .apply()
    }

    /**
     * Resolves the primary public external file: Documents/Deklination/history.json
     */
    fun getHistoryFile(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val deklinationDir = File(publicDocs, SUBDIR_NAME)
        return File(deklinationDir, FILE_NAME)
    }

    /**
     * Fallback file in external storage root if DIRECTORY_DOCUMENTS is inaccessible.
     */
    private fun getFallbackFile(): File {
        val extDir = Environment.getExternalStorageDirectory()
        return File(File(extDir, SUBDIR_NAME), FILE_NAME)
    }

    /**
     * Loads the search history from the user-selected folder or Documents/Deklination/history.json.
     * If the file does not exist or is empty, returns an empty list.
     */
    @Synchronized
    fun loadHistory(): List<WordHistoryEntity> {
        // 1. Try reading from the user-selected SAF folder if available
        val folderUri = getFolderUri()
        if (folderUri != null) {
            try {
                val docDir = DocumentFile.fromTreeUri(context, folderUri)
                if (docDir != null && docDir.exists()) {
                    val historyDoc = docDir.findFile(FILE_NAME)
                    if (historyDoc != null && historyDoc.exists() && historyDoc.length() > 0L) {
                        val content = context.contentResolver.openInputStream(historyDoc.uri)?.bufferedReader()?.use { it.readText() }
                        if (!content.isNullOrBlank()) {
                            val items = parseJsonHistory(content)
                            if (items.isNotEmpty()) return items
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading history from SAF folder: ${e.message}", e)
            }
        }

        // 2. Fallback to direct file on public external storage
        val file = getHistoryFile()
        val targetFile = if (file.exists() && file.length() > 0L) {
            file
        } else {
            val fallback = getFallbackFile()
            if (fallback.exists() && fallback.length() > 0L) fallback else file
        }

        if (!targetFile.exists() || targetFile.length() == 0L) {
            val mediaStoreContent = readFromMediaStore()
            if (!mediaStoreContent.isNullOrBlank()) {
                return parseJsonHistory(mediaStoreContent)
            }
            return emptyList()
        }

        return try {
            val jsonString = FileInputStream(targetFile).bufferedReader().use { it.readText() }
            parseJsonHistory(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read external history file: ${targetFile.absolutePath}", e)
            val mediaStoreContent = readFromMediaStore()
            if (!mediaStoreContent.isNullOrBlank()) {
                parseJsonHistory(mediaStoreContent)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Reads history items from any JSON file selected via ACTION_OPEN_DOCUMENT.
     */
    fun readHistoryFromUri(uri: Uri): List<WordHistoryEntity> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!content.isNullOrBlank()) {
                parseJsonHistory(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading history from URI: $uri", e)
            emptyList()
        }
    }

    /**
     * Saves the entire list of WordHistoryEntity to the user-selected folder
     * and to Documents/Deklination/history.json.
     */
    @Synchronized
    fun saveHistory(items: List<WordHistoryEntity>): Boolean {
        val jsonArray = serializeHistoryToJson(items)
        val jsonContent = jsonArray.toString(2)

        // 1. Write to user-selected SAF folder if configured
        var writtenToSaf = false
        val folderUri = getFolderUri()
        if (folderUri != null) {
            try {
                val docDir = DocumentFile.fromTreeUri(context, folderUri)
                if (docDir != null && docDir.exists() && docDir.canWrite()) {
                    var historyDoc = docDir.findFile(FILE_NAME)
                    if (historyDoc == null) {
                        historyDoc = docDir.createFile("application/json", FILE_NAME)
                    }
                    historyDoc?.let { target ->
                        context.contentResolver.openOutputStream(target.uri, "wt")?.bufferedWriter()?.use { writer ->
                            writer.write(jsonContent)
                            writer.flush()
                        }
                        writtenToSaf = true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Writing to SAF folder failed: ${e.message}")
            }
        }

        // 2. Also write to direct file in Documents/Deklination for local persistence & tests
        var writtenDirectly = false
        try {
            val file = getHistoryFile()
            file.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            FileOutputStream(file, false).bufferedWriter().use { writer ->
                writer.write(jsonContent)
                writer.flush()
            }
            writtenDirectly = true
        } catch (e: Exception) {
            Log.w(TAG, "Direct write to Documents/Deklination/history.json failed, trying fallback: ${e.message}")
            try {
                val fallback = getFallbackFile()
                fallback.parentFile?.let { if (!it.exists()) it.mkdirs() }
                FileOutputStream(fallback, false).bufferedWriter().use { writer ->
                    writer.write(jsonContent)
                    writer.flush()
                }
                writtenDirectly = true
            } catch (ex2: Exception) {
                Log.w(TAG, "Fallback direct write failed: ${ex2.message}")
            }
        }

        // On Android 10+ (Q+), also sync to MediaStore if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                writeToMediaStore(jsonContent)
            } catch (e: Exception) {
                Log.d(TAG, "MediaStore write note: ${e.message}")
            }
        }

        return writtenToSaf || writtenDirectly
    }

    /**
     * Appends or updates an item in the persistent history file.
     * Deduplicates by word and type (case-insensitive), prepending the newest entry.
     */
    @Synchronized
    fun appendOrUpdateItem(item: WordHistoryEntity): Boolean {
        val currentItems = loadHistory().toMutableList()
        currentItems.removeAll {
            it.word.equals(item.word, ignoreCase = true) &&
            it.type.equals(item.type, ignoreCase = true)
        }
        currentItems.add(0, item)
        return saveHistory(currentItems)
    }

    /**
     * Deletes an item from the persistent history file by its ID or by word & type.
     */
    @Synchronized
    fun deleteItem(id: Long, word: String? = null, type: String? = null): Boolean {
        val currentItems = loadHistory().toMutableList()
        val removed = currentItems.removeAll {
            it.id == id || (word != null && type != null &&
                it.word.equals(word, ignoreCase = true) &&
                it.type.equals(type, ignoreCase = true))
        }
        return if (removed) {
            saveHistory(currentItems)
        } else {
            true
        }
    }

    /**
     * Clears the persistent history file content when user taps "Leeren".
     */
    @Synchronized
    fun clearHistory(): Boolean {
        return saveHistory(emptyList())
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

    private fun readFromMediaStore(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val resolver = context.contentResolver
            val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(FILE_NAME, "Documents/$SUBDIR_NAME%")

            resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val fileUri = Uri.withAppendedPath(uri, id.toString())
                    resolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() }
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun writeToMediaStore(content: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val resolver = context.contentResolver
            val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(FILE_NAME, "Documents/$SUBDIR_NAME%")

            var targetUri: Uri? = null
            resolver.query(collectionUri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    targetUri = Uri.withAppendedPath(collectionUri, id.toString())
                }
            }

            if (targetUri == null) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/$SUBDIR_NAME/")
                }
                targetUri = resolver.insert(collectionUri, contentValues)
            }

            targetUri?.let { dest ->
                resolver.openOutputStream(dest, "wt")?.bufferedWriter()?.use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "writeToMediaStore error: ${e.message}")
        }
    }
}
