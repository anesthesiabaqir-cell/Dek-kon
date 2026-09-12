package com.example.data.notebook

import org.json.JSONObject

/**
 * Settings specific to an individual learning notebook (Notizbuch).
 * Persisted in Documents/Deklination/Notizbücher/<NotebookFolder>/settings.json
 */
data class NotebookSettings(
    val notebookName: String = "Allgemein",
    val themeColorId: String = "Dunkelblau",
    val themeMode: String = "auto", // "auto", "light", "dark"
    val selectedProvider: String = "GEMINI",
    val selectedModel: String = "gemini-2.5-flash-latest",
    val customApiKey: String = "",
    val openRouterApiKey: String = "",
    val dailyRequestsRemaining: Int = 20,
    val totalDailyQuota: Int = 20,
    val ttsSpeed: Float = 1.0f,
    val bodyFontSize: Float = 9.0f,
    val headerFontSize: Float = 10.5f,
    val bodyColorHex: String = "#1F2937",
    val headerColorHex: String = "#1E3A8A",
    val fontFamilyName: String = "Roboto",
    val storageFolderUri: String = "",
    val appLanguage: String = "de"
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("notebookName", notebookName)
        root.put("appLanguage", appLanguage)

        val themeObj = JSONObject()
        themeObj.put("selectedColor", themeColorId)
        themeObj.put("themeMode", themeMode)
        root.put("theme", themeObj)

        val aiObj = JSONObject()
        aiObj.put("provider", selectedProvider)
        aiObj.put("modelId", selectedModel)
        aiObj.put("apiKey", customApiKey)
        aiObj.put("openRouterApiKey", openRouterApiKey)
        aiObj.put("dailyRequestsRemaining", dailyRequestsRemaining)
        aiObj.put("totalDailyQuota", totalDailyQuota)
        root.put("ai", aiObj)

        val ttsObj = JSONObject()
        ttsObj.put("defaultSpeed", ttsSpeed.toDouble())
        root.put("tts", ttsObj)

        val exportObj = JSONObject()
        exportObj.put("bodyFontSize", bodyFontSize.toDouble())
        exportObj.put("headerFontSize", headerFontSize.toDouble())
        exportObj.put("bodyColorHex", bodyColorHex)
        exportObj.put("headerColorHex", headerColorHex)
        exportObj.put("fontFamily", fontFamilyName)
        root.put("export", exportObj)

        val storageObj = JSONObject()
        storageObj.put("folderUri", storageFolderUri)
        root.put("storage", storageObj)

        return root.toString(2)
    }

    /**
     * Serializes notebook settings for export files (Full Backup).
     * Strictly EXCLUDES API keys and storage folder path for security & portability.
     */
    fun toExportSettingsJson(): JSONObject {
        val obj = JSONObject()
        obj.put("themeColorId", themeColorId)
        obj.put("themeMode", themeMode)
        obj.put("appLanguage", appLanguage)
        obj.put("selectedProvider", selectedProvider)
        obj.put("selectedModel", selectedModel)
        obj.put("ttsSpeed", ttsSpeed.toDouble())
        obj.put("bodyFontSize", bodyFontSize.toDouble())
        obj.put("headerFontSize", headerFontSize.toDouble())
        obj.put("bodyColorHex", bodyColorHex)
        obj.put("headerColorHex", headerColorHex)
        obj.put("fontFamilyName", fontFamilyName)
        obj.put("dailyRequestsRemaining", dailyRequestsRemaining)
        obj.put("totalDailyQuota", totalDailyQuota)
        // API keys (customApiKey, openRouterApiKey) and storageFolderUri are intentionally excluded
        return obj
    }

    companion object {
        /**
         * Parses settings from an exported settings object.
         * Handles both flat export schema and nested settings.json schema.
         * Guarantees API keys and device storage URIs remain blank.
         */
        fun parseFromExportSettingsJson(obj: JSONObject, fallbackName: String = "Allgemein"): NotebookSettings {
            return try {
                if (obj.has("theme") || obj.has("ai")) {
                    // Nested format (from raw settings.json)
                    val base = fromJson(obj.toString(), fallbackName)
                    base.copy(
                        customApiKey = "",
                        openRouterApiKey = "",
                        storageFolderUri = ""
                    )
                } else {
                    // Flat export package format
                    NotebookSettings(
                        notebookName = fallbackName,
                        themeColorId = obj.optString("themeColorId", "Schiefer"),
                        themeMode = obj.optString("themeMode", "auto"),
                        selectedProvider = obj.optString("selectedProvider", "GEMINI"),
                        selectedModel = obj.optString("selectedModel", "gemini-2.5-flash-latest"),
                        customApiKey = "", // Excluded from export
                        openRouterApiKey = "", // Excluded from export
                        storageFolderUri = "", // Excluded from export
                        dailyRequestsRemaining = obj.optInt("dailyRequestsRemaining", 20),
                        totalDailyQuota = obj.optInt("totalDailyQuota", 20),
                        ttsSpeed = obj.optDouble("ttsSpeed", 1.0).toFloat(),
                        bodyFontSize = obj.optDouble("bodyFontSize", 9.0).toFloat(),
                        headerFontSize = obj.optDouble("headerFontSize", 10.5).toFloat(),
                        bodyColorHex = obj.optString("bodyColorHex", "#1F2937"),
                        headerColorHex = obj.optString("headerColorHex", "#1E3A8A"),
                        fontFamilyName = obj.optString("fontFamilyName", "Roboto"),
                        appLanguage = obj.optString("appLanguage", "de")
                    )
                }
            } catch (e: Exception) {
                NotebookSettings(notebookName = fallbackName)
            }
        }

        fun fromJson(jsonString: String, fallbackName: String = "Allgemein"): NotebookSettings {
            return try {
                val root = JSONObject(jsonString)
                val notebookName = root.optString("notebookName", fallbackName)

                val themeObj = root.optJSONObject("theme")
                val themeColorId = themeObj?.optString("selectedColor", "Schiefer")
                    ?: root.optString("themeColorId", "Schiefer")
                val themeMode = themeObj?.optString("themeMode", "auto")
                    ?: root.optString("themeMode", "auto")

                val aiObj = root.optJSONObject("ai")
                val selectedProvider = aiObj?.optString("provider", "GEMINI")
                    ?: root.optString("selectedProvider", "GEMINI")
                val selectedModel = aiObj?.optString("modelId", "gemini-2.5-flash-latest")
                    ?: root.optString("selectedModel", "gemini-2.5-flash-latest")
                val customApiKey = aiObj?.optString("apiKey", "")
                    ?: root.optString("customApiKey", "")
                val openRouterApiKey = aiObj?.optString("openRouterApiKey", "")
                    ?: root.optString("openRouterApiKey", "")
                val dailyRequestsRemaining = aiObj?.optInt("dailyRequestsRemaining", 20)
                    ?: root.optInt("dailyRequestsRemaining", 20)
                val totalDailyQuota = aiObj?.optInt("totalDailyQuota", 20)
                    ?: root.optInt("totalDailyQuota", 20)

                val ttsObj = root.optJSONObject("tts")
                val ttsSpeed = ttsObj?.optDouble("defaultSpeed", 1.0)?.toFloat()
                    ?: root.optDouble("ttsSpeed", 1.0).toFloat()

                val exportObj = root.optJSONObject("export")
                val bodyFontSize = exportObj?.optDouble("bodyFontSize", 9.0)?.toFloat() ?: 9.0f
                val headerFontSize = exportObj?.optDouble("headerFontSize", 10.5)?.toFloat() ?: 10.5f
                val bodyColorHex = exportObj?.optString("bodyColorHex", "#1F2937") ?: "#1F2937"
                val headerColorHex = exportObj?.optString("headerColorHex", "#1E3A8A") ?: "#1E3A8A"
                val fontFamilyName = exportObj?.optString("fontFamily", "Roboto") ?: "Roboto"

                val storageObj = root.optJSONObject("storage")
                val storageFolderUri = storageObj?.optString("folderUri", "")
                    ?: root.optString("storageFolderUri", "")

                val appLanguage = root.optString("appLanguage", "de").ifBlank { "de" }

                NotebookSettings(
                    notebookName = notebookName,
                    themeColorId = themeColorId,
                    themeMode = themeMode,
                    selectedProvider = selectedProvider,
                    selectedModel = selectedModel,
                    customApiKey = customApiKey,
                    openRouterApiKey = openRouterApiKey,
                    dailyRequestsRemaining = dailyRequestsRemaining,
                    totalDailyQuota = totalDailyQuota,
                    ttsSpeed = ttsSpeed,
                    bodyFontSize = bodyFontSize,
                    headerFontSize = headerFontSize,
                    bodyColorHex = bodyColorHex,
                    headerColorHex = headerColorHex,
                    fontFamilyName = fontFamilyName,
                    storageFolderUri = storageFolderUri,
                    appLanguage = appLanguage
                )
            } catch (e: Exception) {
                NotebookSettings(notebookName = fallbackName)
            }
        }
    }
}

/**
 * Representation of a Notebook (autonomous study workbook).
 */
data class Notebook(
    val id: String, // Directory name under Documents/Deklination/Notizbücher/
    val name: String, // Display name
    val lastModified: Long,
    val wordCount: Int = 0,
    val settings: NotebookSettings = NotebookSettings(notebookName = name)
)

/**
 * Encapsulates a parsed notebook package from an export file.
 */
data class ImportedNotebookPackage(
    val name: String,
    val words: List<com.example.data.local.WordHistoryEntity>,
    val settings: NotebookSettings? = null
)

