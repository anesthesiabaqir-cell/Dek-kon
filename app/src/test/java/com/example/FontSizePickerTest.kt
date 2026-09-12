package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.PrintExportDialog
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FontSizePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFontSizeDialog_BodyText_SelectionAndAdjustment() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PrintExportDialog(
                    onDismiss = {}
                )
            }
        }

        // 1. Initial size for body text should be "12 pt"
        composeTestRule.onNodeWithTag("body_size_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("body_size_selector").assertTextContains("12 pt")

        // 2. Click body size selector to open the centered dialog
        composeTestRule.onNodeWithTag("body_size_selector").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify Centered Dialog is displayed with Header, Standard Sizes, and Eigene Größe
        composeTestRule.onNodeWithTag("font_size_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Schriftgröße wählen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Standardgrößen:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eigene Größe:").assertIsDisplayed()

        // 4. Verify 12 preset sizes exist
        val presets = listOf(9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 48)
        for (sz in presets) {
            composeTestRule.onNodeWithTag("preset_size_$sz").assertIsDisplayed()
        }

        // 5. Select preset 18 pt
        composeTestRule.onNodeWithTag("preset_size_18").performClick()
        composeTestRule.waitForIdle()

        // Input field should now show 18
        composeTestRule.onNodeWithTag("font_size_custom_input").assertTextContains("18")

        // 6. Use [+] button to increment by 1pt -> 19 pt
        composeTestRule.onNodeWithTag("font_size_plus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("font_size_custom_input").assertTextContains("19")

        // 7. Click "Übernehmen" to confirm
        composeTestRule.onNodeWithTag("font_size_confirm_button").performClick()
        composeTestRule.waitForIdle()

        // 8. Dialog dismissed, body size selector now displays "19 pt"
        composeTestRule.onNodeWithTag("body_size_selector").assertTextContains("19 pt")
    }

    @Test
    fun testFontSizeDialog_Header_CancelLeavesUnchanged() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PrintExportDialog(
                    onDismiss = {}
                )
            }
        }

        // Initial header size is "14 pt"
        composeTestRule.onNodeWithTag("header_size_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_size_selector").assertTextContains("14 pt")

        // Click header size selector
        composeTestRule.onNodeWithTag("header_size_selector").performClick()
        composeTestRule.waitForIdle()

        // Pick preset 24 pt
        composeTestRule.onNodeWithTag("preset_size_24").performClick()
        composeTestRule.waitForIdle()

        // Click "Abbrechen" to cancel
        composeTestRule.onNodeWithTag("font_size_cancel_button").performClick()
        composeTestRule.waitForIdle()

        // Header size remains unchanged at "14 pt"
        composeTestRule.onNodeWithTag("header_size_selector").assertTextContains("14 pt")
    }
}
