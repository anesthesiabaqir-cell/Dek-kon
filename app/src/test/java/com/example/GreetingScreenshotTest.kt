package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.VerbConjugationResult
import com.example.data.model.VerbImperativ
import com.example.data.model.VerbTenseConjugation
import com.example.data.model.WordDeclensionResult
import com.example.ui.components.DeclensionResultView
import com.example.ui.components.VerbConjugationResultView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { Greeting("Robolectric") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun test_render_declension_view() {
    val sampleRow = DeclensionCaseRow("dativ", "Dativ", "Wem?", "dem Tisch", "einem Tisch", "Tisch")
    val group = DeclensionTableGroup("Singular", listOf(sampleRow))
    val sampleResult = WordDeclensionResult(
      word = "Tisch",
      gender = "Maskulin",
      genderArticle = "der",
      pluralNoun = "die Tische",
      meaningEnglish = "Table",
      singular = group,
      plural = group
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        DeclensionResultView(
          result = sampleResult,
          selectedTab = 0,
          onTabSelected = {}
        )
      }
    }
  }

  @Test
  fun test_render_verb_view() {
    val sampleTense = VerbTenseConjugation(
      tenseKey = "praesens",
      tenseNameDe = "Präsens",
      ich = "arbeite",
      du = "arbeitest",
      erSieEs = "arbeitet",
      wir = "arbeiten",
      ihr = "arbeitet",
      sieSie = "arbeiten"
    )
    val sampleVerb = VerbConjugationResult(
      word = "arbeiten",
      infinitiv = "arbeiten",
      hilfsverb = "haben",
      partizip1 = "arbeitend",
      partizip2 = "gearbeitet",
      meaningEnglish = "to work",
      imperativ = VerbImperativ("arbeite!", "arbeitet!", "arbeiten Sie!"),
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
  }
}
