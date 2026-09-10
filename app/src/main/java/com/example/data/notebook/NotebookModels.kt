package com.example.data.notebook

import org.json.JSONObject

/**
 * Settings specific to an individual learning notebook (Notizbuch).
 * Persisted in Documents/Deklination/Notizbücher/<NotebookFolder>/settings.json
 */
data class NotebookSettings(
    val notebookName: String = "Allgemein",
    val themeColorId: String = "Schiefer",
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
    val storageFolderUri: String = ""
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("notebookName", notebookName)

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

    companion object {
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
                    storageFolderUri = storageFolderUri
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
