package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DeclensionCaseRow
import com.example.data.model.DeclensionTableGroup
import com.example.data.model.GrammarResult
import com.example.data.model.WordDeclensionResult
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackNavigationTest {

    private lateinit var application: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(application)
    }

    private fun createSampleNounResult(word: String): WordDeclensionResult {
        val row = DeclensionCaseRow(
            caseKey = "nominativ",
            caseNameDe = "Nominativ",
            caseQuestionDe = "Wer oder was?",
            definite = "der $word",
            indefinite = "ein $word",
            noArticle = word
        )
        val group = DeclensionTableGroup(
            numberDe = "Singular",
            rows = listOf(row)
        )
        return WordDeclensionResult(
            word = word,
            gender = "Maskulin",
            genderArticle = "der",
            pluralNoun = "die ${word}e",
            meaningEnglish = "table",
            singular = group,
            plural = group
        )
    }

    @Test
    fun `test Scenario 1 - Main to Settings to Back returns to Main`() {
        // 1. Initially on Main screen
        assertFalse(viewModel.uiState.value.isApiKeyDialogOpen)

        // 2. Open Settings
        viewModel.onOpenApiKeyDialog()
        assertTrue(viewModel.uiState.value.isApiKeyDialogOpen)

        // 3. Press Back
        val handled = viewModel.handleBackPress()
        assertTrue(handled)

        // 4. Returns to Main screen
        assertFalse(viewModel.uiState.value.isApiKeyDialogOpen)
    }

    @Test
    fun `test Scenario 2 - Main to Settings to Export to Back returns to Settings, then to Main`() {
        // 1. Open Settings
        viewModel.onOpenApiKeyDialog()
        assertTrue(viewModel.uiState.value.isApiKeyDialogOpen)
        assertFalse(viewModel.uiState.value.isExportDialogOpen)

        // 2. Open Export from Settings
        viewModel.onOpenExportDialog(fromSettings = true)
        assertTrue(viewModel.uiState.value.isExportDialogOpen)
        assertTrue(viewModel.uiState.value.exportOpenedFromSettings)

        // 3. Press Back from Export -> Returns to Settings, NOT to Main
        val handledFirstBack = viewModel.handleBackPress()
        assertTrue(handledFirstBack)
        assertFalse(viewModel.uiState.value.isExportDialogOpen)
        assertTrue(viewModel.uiState.value.isApiKeyDialogOpen)

        // 4. Press Back from Settings -> Returns to Main
        val handledSecondBack = viewModel.handleBackPress()
        assertTrue(handledSecondBack)
        assertFalse(viewModel.uiState.value.isApiKeyDialogOpen)
        assertFalse(viewModel.uiState.value.isExportDialogOpen)

        // 5. Press Back on Main -> Returns false (immediate exit to background)
        val handledThirdBack = viewModel.handleBackPress()
        assertFalse(handledThirdBack)
    }

    @Test
    fun `test Scenario 3 - Main to Back exits cleanly to background`() {
        // 1. On Main screen
        assertFalse(viewModel.uiState.value.isApiKeyDialogOpen)
        assertFalse(viewModel.uiState.value.isExportDialogOpen)
        assertNull(viewModel.uiState.value.currentGrammarResult)

        // 2. Press Back on Main -> Not handled by VM, allowing activity to move task to back
        val handled = viewModel.handleBackPress()
        assertFalse(handled)
    }

    @Test
    fun `test Scenario 4 - Main to Word Detail View to Back returns to Main`() {
        // 1. Simulate word search result (Word Detail View)
        val sampleResult = createSampleNounResult("Tisch")
        viewModel.setGrammarResultForTest(GrammarResult.Noun(sampleResult))
        assertNotNull(viewModel.uiState.value.currentGrammarResult)

        // 2. Press Back from Word Detail View
        val handled = viewModel.handleBackPress()
        assertTrue(handled)

        // 3. Active word result is cleared, returning to Main
        assertNull(viewModel.uiState.value.currentGrammarResult)
        assertNull(viewModel.uiState.value.currentResult)
    }

    @Test
    fun `test Scenario 5 - Word Detail View to Export to Back returns to Word Detail View`() {
        // 1. On Word Detail View
        val sampleResult = createSampleNounResult("Stuhl")
        viewModel.setGrammarResultForTest(GrammarResult.Noun(sampleResult))
        assertNotNull(viewModel.uiState.value.currentGrammarResult)

        // 2. Open Export from Word Detail View (fromSettings = false)
        viewModel.onOpenExportDialog(fromSettings = false)
        assertTrue(viewModel.uiState.value.isExportDialogOpen)

        // 3. Press Back in Export -> Returns to Word Detail View
        val handledFirstBack = viewModel.handleBackPress()
        assertTrue(handledFirstBack)
        assertFalse(viewModel.uiState.value.isExportDialogOpen)
        assertNotNull(viewModel.uiState.value.currentGrammarResult)

        // 4. Press Back in Word Detail View -> Returns to Main
        val handledSecondBack = viewModel.handleBackPress()
        assertTrue(handledSecondBack)
        assertNull(viewModel.uiState.value.currentGrammarResult)
    }

    @Test
    fun `test Scenario 6 - History subtab filter to Back returns to All history`() {
        // 1. User selects History subtab (1 = Nomen)
        viewModel.onSelectHistoryFilter(1)
        assertEquals(1, viewModel.uiState.value.selectedHistoryFilter)

        // 2. Press Back
        val handled = viewModel.handleBackPress()
        assertTrue(handled)

        // 3. Filter resets to 0 (Alle)
        assertEquals(0, viewModel.uiState.value.selectedHistoryFilter)
    }

    @Test
    fun `test MainActivity back navigation integration`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
        assertFalse(activity.isFinishing)
    }
}
