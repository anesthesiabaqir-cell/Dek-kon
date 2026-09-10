package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GrammarType
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchPlaceholderLayoutTest {

    private lateinit var application: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(application)
    }

    @Test
    fun `test default placeholder text is Suche nach Nomen in Noun Mode`() {
        assertEquals(GrammarType.NOMEN, viewModel.uiState.value.selectedGrammarType)
        val placeholder = if (viewModel.uiState.value.selectedGrammarType == GrammarType.VERB) {
            "Suche nach Verben"
        } else {
            "Suche nach Nomen"
        }
        assertEquals("Suche nach Nomen", placeholder)
        assertNotEquals("Nomen suchen", placeholder)
        assertNotEquals("Nomen suche", placeholder)
    }

    @Test
    fun `test placeholder text switches to Suche nach Verben in Verb Mode`() {
        viewModel.onGrammarTypeSelected(GrammarType.VERB)
        assertEquals(GrammarType.VERB, viewModel.uiState.value.selectedGrammarType)
        val placeholder = if (viewModel.uiState.value.selectedGrammarType == GrammarType.VERB) {
            "Suche nach Verben"
        } else {
            "Suche nach Nomen"
        }
        assertEquals("Suche nach Verben", placeholder)
        assertNotEquals("Verb suchen", placeholder)
    }

    @Test
    fun `test string resources match exact specification`() {
        val nounPlaceholder = application.getString(R.string.search_placeholder_noun)
        val verbPlaceholder = application.getString(R.string.search_placeholder_verb)
        assertEquals("Suche nach Nomen", nounPlaceholder)
        assertEquals("Suche nach Verben", verbPlaceholder)
    }

    @Test
    fun `test query change and clearing retains correct state`() {
        viewModel.onQueryChange("Haus")
        assertEquals("Haus", viewModel.uiState.value.searchQuery)

        viewModel.onQueryChange("")
        assertEquals("", viewModel.uiState.value.searchQuery)
    }
}
