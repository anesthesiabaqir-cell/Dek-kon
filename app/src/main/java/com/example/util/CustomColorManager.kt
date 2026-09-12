package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONException
import java.util.Locale

/**
 * Manages user-created custom colors with SharedPreferences persistence.
 */
object CustomColorManager {
    private const val PREFS_NAME = "custom_colors_preferences"
    private const val KEY_CUSTOM_COLORS = "saved_custom_colors_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the list of user-saved custom color hex strings (e.g. ["#FF5733", "#00B4D8"]).
     */
    fun getCustomColors(context: Context): List<String> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_CUSTOM_COLORS, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val hex = jsonArray.optString(i)
                if (isValidHex(hex)) {
                    val normalized = normalizeHex(hex)
                    if (!list.contains(normalized)) {
                        list.add(normalized)
                    }
                }
            }
        } catch (_: JSONException) {
            // fallback
        }
        return list
    }

    /**
     * Adds a new custom color hex string.
     * Returns true if successfully added, false if invalid or already exists.
     */
    fun addCustomColor(context: Context, rawHex: String): Boolean {
        if (!isValidHex(rawHex)) return false
        val normalized = normalizeHex(rawHex)
        val current = getCustomColors(context).toMutableList()
        if (current.any { it.equals(normalized, ignoreCase = true) }) {
            return true // Already present
        }
        current.add(0, normalized) // Add most recent at the beginning
        saveList(context, current)
        return true
    }

    /**
     * Removes a custom color from the saved list.
     */
    fun removeCustomColor(context: Context, rawHex: String): Boolean {
        val normalized = normalizeHex(rawHex)
        val current = getCustomColors(context).toMutableList()
        val removed = current.removeAll { it.equals(normalized, ignoreCase = true) }
        if (removed) {
            saveList(context, current)
        }
        return removed
    }

    private fun saveList(context: Context, list: List<String>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        getPrefs(context).edit().putString(KEY_CUSTOM_COLORS, jsonArray.toString()).apply()
    }

    /**
     * Validates if a string is a valid 3-character or 6-character hex color code.
     */
    fun isValidHex(hex: String): Boolean {
        val clean = hex.trim().removePrefix("#")
        return when (clean.length) {
            3, 6 -> clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            8 -> clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            else -> false
        }
    }

    /**
     * Normalizes a hex string into uppercase "#RRGGBB".
     */
    fun normalizeHex(hex: String): String {
        val clean = hex.trim().removePrefix("#").uppercase(Locale.ROOT)
        return when (clean.length) {
            3 -> {
                val r = clean[0].toString().repeat(2)
                val g = clean[1].toString().repeat(2)
                val b = clean[2].toString().repeat(2)
                "#$r$g$b"
            }
            6 -> "#$clean"
            8 -> "#" + clean.substring(2) // remove alpha if given
            else -> "#000000"
        }
    }

    /**
     * Converts HSV values (H: 0..360, S: 0..1, V: 0..1) to a "#RRGGBB" hex string.
     */
    fun hsvToHex(hue: Float, saturation: Float, value: Float): String {
        val hsv = floatArrayOf(
            hue.coerceIn(0f, 360f),
            saturation.coerceIn(0f, 1f),
            value.coerceIn(0f, 1f)
        )
        val colorInt = Color.HSVToColor(hsv)
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b)
    }

    /**
     * Converts a hex color string to HSV float array [Hue (0..360), Saturation (0..1), Value (0..1)].
     */
    fun hexToHsv(hex: String): FloatArray {
        val hsv = FloatArray(3)
        return try {
            val colorInt = DeclensionHistoryExportHelper.parseHexColor(hex)
            Color.colorToHSV(colorInt, hsv)
            hsv
        } catch (_: Exception) {
            floatArrayOf(0f, 1f, 1f)
        }
    }
}
