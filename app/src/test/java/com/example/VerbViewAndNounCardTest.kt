package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.local.WordHistoryEntity
import com.example.data.model.VerbConjugationResult
import com.example.data.model.VerbImperativ
import com.example.data.model.VerbTenseConjugation
import com.example.ui.components.HistoryItemCard
import com.example.ui.components.VerbConjugationResultView
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VerbViewAndNounCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_verb_view_hilfsverb_badge_and_partizip_row() {
        val sampleTense = VerbTenseConjugation(
            tenseKey = "praesens",
            tenseNameDe = "Präsens",
            ich = "schließe",
            du = "schließt",
            erSieEs = "schließt",
            wir = "schließen",
            ihr = "schließt",
            sieSie = "schließen"
        )
        val sampleVerb = VerbConjugationResult(
            word = "schließen",
            infinitiv = "schließen",
            hilfsverb = "haben",
            partizip1 = "schließend",
            partizip2 = "geschlossen",
            meaningEnglish = "to close",
            imperativ = VerbImperativ("schließe!", "schließt!", "schließen Sie!"),
            tenses = listOf(sampleTense)
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                VerbConjugationResultView(
                    result = sampleVerb,
                    onSpeak = {}
                )
            }
        }

        // 1. Auxiliary verb row has Hilfsverb badge and normal text for auxiliary
        composeTestRule.onNodeWithTag("verb_auxiliary_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verb_auxiliary_badge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hilfsverb:").assertIsDisplayed()
        composeTestRule.onNodeWithText("haben").assertIsDisplayed()

        // 2. Partizip row has Partizip I and Partizip II
        composeTestRule.onNodeWithTag("verb_partizip_row").assertIsDisplayed()
        composeTestRule.onNodeWithText("Partizip I: schließend · Partizip II: geschlossen").assertIsDisplayed()

        // 3. Translation row has Eng.: prefix
        composeTestRule.onNodeWithTag("verb_translation_row").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eng.: to close").assertIsDisplayed()
    }

    @Test
    fun test_noun_history_card_gender_badge_and_no_nomen_label() {
        val nounItem = WordHistoryEntity(
            id = 10L,
            word = "Tisch",
            type = "Nomen",
            data = """{"gender":"Maskulin","genderArticle":"der","meaningEnglish":"table"}""",
            timestamp = 1718000000000L,
            gender = "Maskulin",
            genderArticle = "der",
            meaningEnglish = "table",
            rawJsonResult = "{}"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                HistoryItemCard(
                    item = nounItem,
                    onClick = {},
                    onDelete = {},
                    onSpeak = {}
                )
            }
        }

        // Gender badge MASKULIN is displayed
        composeTestRule.onNodeWithText("MASKULIN").assertIsDisplayed()

        // The "nomen" label must NOT exist
        composeTestRule.onNodeWithText("nomen").assertDoesNotExist()
        composeTestRule.onNodeWithText("Nomen").assertDoesNotExist()
    }

    @Test
    fun test_sentence_result_view_layout_and_no_tables_or_tabs() {
        val sentenceResult = com.example.data.model.WordDeclensionResult(
            word = "Auf Deutsch sagt man gern auch nur kuli",
            gender = "–",
            genderArticle = "",
            pluralNoun = "",
            meaningEnglish = "In German, people also often just say 'Kuli' (ballpoint pen).",
            singular = com.example.data.model.DeclensionTableGroup("Singular", emptyList()),
            plural = com.example.data.model.DeclensionTableGroup("Plural", emptyList())
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.components.SentenceResultView(
                    result = sentenceResult,
                    onSpeak = {}
                )
            }
        }

        // 1. German sentence is displayed in clear font with testTag
        composeTestRule.onNodeWithTag("sentence_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auf Deutsch sagt man gern auch nur kuli").assertIsDisplayed()

        // 2. Pronounce sentence button is present
        composeTestRule.onNodeWithTag("pronounce_sentence_button").assertIsDisplayed()

        // 3. English translation is displayed
        composeTestRule.onNodeWithTag("sentence_translation").assertIsDisplayed()
        composeTestRule.onNodeWithText("English: In German, people also often just say 'Kuli' (ballpoint pen).").assertIsDisplayed()

        // 4. Satz badge at bottom-right
        composeTestRule.onNodeWithTag("sentence_type_badge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Satz").assertIsDisplayed()

        // 5. No declension tables or tabs
        composeTestRule.onNodeWithText("SINGULAR").assertDoesNotExist()
        composeTestRule.onNodeWithText("PLURAL").assertDoesNotExist()
        composeTestRule.onNodeWithText("Kasus").assertDoesNotExist()
        composeTestRule.onNodeWithText("Singular").assertDoesNotExist()
        composeTestRule.onNodeWithText("Beiden").assertDoesNotExist()
    }
}
