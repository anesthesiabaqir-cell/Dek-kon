package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CustomColorManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomColorManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before test
        val prefs = context.getSharedPreferences("custom_colors_preferences", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testIsValidHex() {
        assertTrue(CustomColorManager.isValidHex("#FF5733"))
        assertTrue(CustomColorManager.isValidHex("FF5733"))
        assertTrue(CustomColorManager.isValidHex("#FFF"))
        assertTrue(CustomColorManager.isValidHex("000000"))
        assertTrue(CustomColorManager.isValidHex("#A1B2C3"))

        assertFalse(CustomColorManager.isValidHex(""))
        assertFalse(CustomColorManager.isValidHex("#12345"))
        assertFalse(CustomColorManager.isValidHex("ZZZZZZ"))
        assertFalse(CustomColorManager.isValidHex("FF 57 33"))
    }

    @Test
    fun testNormalizeHex() {
        assertEquals("#FF5733", CustomColorManager.normalizeHex("ff5733"))
        assertEquals("#FF5733", CustomColorManager.normalizeHex("#ff5733"))
        assertEquals("#FFFFFF", CustomColorManager.normalizeHex("fff"))
        assertEquals("#AABBCC", CustomColorManager.normalizeHex("#abc"))
    }

    @Test
    fun testAddAndGetCustomColors() {
        val initialList = CustomColorManager.getCustomColors(context)
        assertTrue(initialList.isEmpty())

        assertTrue(CustomColorManager.addCustomColor(context, "#FF5733"))
        assertTrue(CustomColorManager.addCustomColor(context, "00B4D8"))

        val updatedList = CustomColorManager.getCustomColors(context)
        assertEquals(2, updatedList.size)
        assertEquals("#00B4D8", updatedList[0])
        assertEquals("#FF5733", updatedList[1])
    }

    @Test
    fun testRemoveCustomColor() {
        CustomColorManager.addCustomColor(context, "#FF5733")
        CustomColorManager.addCustomColor(context, "#00B4D8")

        assertTrue(CustomColorManager.removeCustomColor(context, "ff5733"))
        val remaining = CustomColorManager.getCustomColors(context)
        assertEquals(1, remaining.size)
        assertEquals("#00B4D8", remaining[0])
    }

    @Test
    fun testHsvToHexConversion() {
        val redHex = CustomColorManager.hsvToHex(0f, 1f, 1f)
        assertEquals("#FF0000", redHex)

        val greenHex = CustomColorManager.hsvToHex(120f, 1f, 1f)
        assertEquals("#00FF00", greenHex)

        val blueHex = CustomColorManager.hsvToHex(240f, 1f, 1f)
        assertEquals("#0000FF", blueHex)
    }
}
