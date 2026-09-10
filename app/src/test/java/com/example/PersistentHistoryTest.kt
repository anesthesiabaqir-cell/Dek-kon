package com.example

import android.app.Application
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiKeyManager
import com.example.data.local.AppDatabase
import com.example.data.local.ExternalHistoryStorage
import com.example.data.local.WordHistoryEntity
import com.example.data.model.GrammarType
import com.example.data.remote.GeminiDeclensionService
import com.example.data.repository.WordRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PersistentHistoryTest {

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var externalStorage: ExternalHistoryStorage
    private lateinit var repository: WordRepository

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(application)
        externalStorage = ExternalHistoryStorage(application)
        val apiKeyManager = ApiKeyManager(application)
        val geminiService = GeminiDeclensionService(application)
        repository = WordRepository(
            historyDao = database.wordHistoryDao(),
            geminiService = geminiService,
            apiKeyManager = apiKeyManager,
            searchQueryDao = database.searchQueryDao(),
            externalHistoryStorage = externalStorage
        )
    }

    @After
    fun tearDown() {
        externalStorage.clearHistory()
        runBlocking {
            database.wordHistoryDao().clearAll()
        }
    }

    @Test
    fun `test external storage file path is in Documents Deklination`() {
        val historyFile = externalStorage.getHistoryFile()
        assertTrue(historyFile.path.contains("Documents"))
        assertTrue(historyFile.path.contains("Deklination"))
        assertEquals("history.json", historyFile.name)
    }

    @Test
    fun `test external storage write and load history`() {
        val testNoun = WordHistoryEntity(
            id = 1L,
            word = "Schloss",
            type = "Nomen",
            data = "{}",
            timestamp = System.currentTimeMillis(),
            gender = "Neutrum",
            genderArticle = "das",
            meaningEnglish = "castle",
            rawJsonResult = "{}"
        )
        val testVerb = WordHistoryEntity(
            id = 2L,
            word = "gehen",
            type = "Verb",
            data = "{}",
            timestamp = System.currentTimeMillis(),
            gender = "ist",
            genderArticle = "gegangen",
            meaningEnglish = "to go",
            rawJsonResult = "{}"
        )

        val saved = externalStorage.saveHistory(listOf(testNoun, testVerb))
        assertTrue(saved)

        val loaded = externalStorage.loadHistory()
        assertEquals(2, loaded.size)
        assertEquals("Schloss", loaded[0].word)
        assertEquals("gehen", loaded[1].word)
    }

    @Test
    fun `test append and update deduplicates by word and type`() {
        val item1 = WordHistoryEntity(
            id = 10L,
            word = "Haus",
            type = "Nomen",
            data = "{}",
            timestamp = 1000L,
            gender = "Neutrum",
            genderArticle = "das",
            meaningEnglish = "house",
            rawJsonResult = "{}"
        )
        externalStorage.appendOrUpdateItem(item1)

        val item2 = WordHistoryEntity(
            id = 11L,
            word = "Haus",
            type = "Nomen",
            data = "{}",
            timestamp = 2000L,
            gender = "Neutrum",
            genderArticle = "das",
            meaningEnglish = "house (updated)",
            rawJsonResult = "{}"
        )
        externalStorage.appendOrUpdateItem(item2)

        val items = externalStorage.loadHistory()
        assertEquals(1, items.size)
        assertEquals("Haus", items[0].word)
        assertEquals("house (updated)", items[0].meaningEnglish)
    }

    @Test
    fun `test clearHistory clears content`() {
        val item = WordHistoryEntity(
            id = 20L,
            word = "Tisch",
            type = "Nomen",
            data = "{}",
            timestamp = System.currentTimeMillis(),
            gender = "Maskulin",
            genderArticle = "der",
            meaningEnglish = "table",
            rawJsonResult = "{}"
        )
        externalStorage.appendOrUpdateItem(item)
        assertEquals(1, externalStorage.loadHistory().size)

        val cleared = externalStorage.clearHistory()
        assertTrue(cleared)
        assertEquals(0, externalStorage.loadHistory().size)
    }

    @Test
    fun `test history survives Clear App Data action`() = runBlocking {
        // 1. App runs normally and user searches words: saved to external storage & Room
        val nounEntity = WordHistoryEntity(
            id = 100L,
            word = "Schön",
            type = "Nomen",
            data = "{}",
            timestamp = System.currentTimeMillis(),
            gender = "Neutrum",
            genderArticle = "das",
            meaningEnglish = "beauty",
            rawJsonResult = "{}"
        )
        val verbEntity = WordHistoryEntity(
            id = 101L,
            word = "lernen",
            type = "Verb",
            data = "{}",
            timestamp = System.currentTimeMillis(),
            gender = "hat",
            genderArticle = "gelernt",
            meaningEnglish = "to learn",
            rawJsonResult = "{}"
        )

        database.wordHistoryDao().insert(nounEntity)
        database.wordHistoryDao().insert(verbEntity)
        externalStorage.saveHistory(listOf(nounEntity, verbEntity))

        // Verify Room and External Storage both have 2 items
        assertEquals(2, database.wordHistoryDao().getAllHistoryList().size)
        assertEquals(2, externalStorage.loadHistory().size)

        // 2. SIMULATE "CLEAR APP DATA" (Settings -> Apps -> Clear Data)
        // Internal Room database is wiped completely!
        database.wordHistoryDao().clearAll()
        assertEquals(0, database.wordHistoryDao().getAllHistoryList().size)

        // But external file in public Documents/Deklination/history.json remains INTACT!
        val survivingExternalHistory = externalStorage.loadHistory()
        assertEquals(2, survivingExternalHistory.size)

        // 3. User relaunches app: WordRepository syncHistoryOnStartup() runs
        repository.syncHistoryOnStartup()

        // 4. History is fully restored from external file into Room!
        val restoredRoomItems = database.wordHistoryDao().getAllHistoryList()
        assertEquals(2, restoredRoomItems.size)
        assertTrue(restoredRoomItems.any { it.word == "Schön" })
        assertTrue(restoredRoomItems.any { it.word == "lernen" })
    }

    @Test
    fun `test verb quick details extraction and string formatting`() {
        val verbJson = """
            {
              "type": "Verb",
              "word": "bilden",
              "infinitiv": "bilden",
              "hilfsverb": "haben",
              "auxiliary": "hat",
              "praeteritum": "bildete",
              "partizip1": "bildend",
              "partizip2": "gebildet",
              "verb_type": "schwach",
              "separable": "untrennbar",
              "case_object": "Akk.",
              "meaningEnglish": "to form, to educate, to build",
              "english": "to form, to educate, to build"
            }
        """.trimIndent()

        val details = com.example.data.model.extractVerbQuickDetails(
            word = "bilden",
            rawJson = verbJson
        )

        assertEquals("bilden", details.verb)
        assertEquals("(hat)", details.auxiliary)
        assertEquals("bildete", details.praeteritum)
        assertEquals("bildend", details.partizip1)
        assertEquals("gebildet", details.partizip2)
        assertEquals("schwach", details.verbType)
        assertEquals("untrennbar", details.separable)
        assertEquals("Akk.", details.caseObject)
        assertEquals("to form, to educate, to build", details.englishMeaning)

        val coreDetailsString = details.formatCoreDetailsString()
        assertEquals("Details: (hat) · bildete · Part I: bildend · Part II: gebildet · schwach · untrennbar · Akk.", coreDetailsString)

        val quickDetailsString = details.formatQuickDetailsString()
        assertEquals("(hat)  bildete  gebildet  ·  schwach  ·  untrennbar  ·  Akk.", quickDetailsString)
    }

    @Test
    fun `test strong verb with custom forms`() {
        val details = com.example.data.model.VerbQuickDetails(
            verb = "nehmen",
            auxiliary = "(hat)",
            praeteritum = "nahm",
            partizip1 = "nehmend",
            partizip2 = "genommen",
            verbType = "stark",
            separable = "",
            caseObject = "Akk.",
            englishMeaning = "to take"
        )
        assertEquals("(hat)  nahm  genommen  ·  stark  ·  Akk.", details.formatQuickDetailsString())
        assertEquals("Details: (hat) · nahm · Part I: nehmend · Part II: genommen · stark · Akk.", details.formatCoreDetailsString())
    }

    @Test
    fun `test external storage writes verb quick details fields to history json`() {
        val verbJson = """
            {
              "type": "Verb",
              "word": "anrufen",
              "infinitiv": "anrufen",
              "hilfsverb": "haben",
              "auxiliary": "hat",
              "praeteritum": "rief an",
              "partizip1": "anrufend",
              "partizip2": "angerufen",
              "verb_type": "stark",
              "separable": "trennbar",
              "case_object": "Akk.",
              "meaningEnglish": "to call",
              "english": "to call"
            }
        """.trimIndent()

        val verbEntity = WordHistoryEntity(
            id = 200L,
            word = "anrufen",
            type = "Verb",
            data = verbJson,
            timestamp = 1757300000000L,
            gender = "hat",
            genderArticle = "angerufen",
            meaningEnglish = "to call",
            rawJsonResult = verbJson
        )

        val saved = externalStorage.saveHistory(listOf(verbEntity))
        assertTrue(saved)

        val file = externalStorage.getHistoryFile()
        assertTrue(file.exists())
        val content = file.readText()

        val jsonArray = org.json.JSONArray(content)
        assertEquals(1, jsonArray.length())
        val obj = jsonArray.getJSONObject(0)

        assertEquals("anrufen", obj.optString("verb"))
        assertEquals("hat", obj.optString("auxiliary"))
        assertEquals("anrufend", obj.optString("partizip1"))
        assertEquals("angerufen", obj.optString("partizip2"))
        assertEquals("stark", obj.optString("verb_type"))
        assertEquals("trennbar", obj.optString("separable"))
        assertEquals("Akk.", obj.optString("case_object"))
        assertEquals("to call", obj.optString("english"))

        val loaded = externalStorage.loadHistory()
        assertEquals(1, loaded.size)
        assertEquals("anrufen", loaded[0].word)
        assertEquals("Verb", loaded[0].type)
    }

    @Test
    fun `test external storage saves noun quick details to json`() {
        val nounJson = """
            {
              "word": "Stift",
              "gender": "Maskulin",
              "genderArticle": "der",
              "meaningEnglish": "pen / pencil",
              "singular": {
                "nominativ": { "definite": "der Stift" },
                "genitiv": { "definite": "des Stiftes" }
              },
              "plural": {
                "nominativ": { "definite": "die Stifte" },
                "dativ": { "definite": "den Stiften" }
              }
            }
        """.trimIndent()

        val nounEntity = WordHistoryEntity(
            id = 50L,
            word = "Stift",
            type = "Nomen",
            data = nounJson,
            timestamp = 1757300000000L,
            gender = "Maskulin",
            genderArticle = "der",
            meaningEnglish = "pen / pencil",
            rawJsonResult = nounJson
        )

        val saved = externalStorage.saveHistory(listOf(nounEntity))
        assertTrue(saved)

        val file = externalStorage.getHistoryFile()
        assertTrue(file.exists())
        val content = file.readText()

        val jsonArray = org.json.JSONArray(content)
        assertEquals(1, jsonArray.length())
        val obj = jsonArray.getJSONObject(0)

        assertEquals("der Stift", obj.optString("singular"))
        assertEquals("die Stifte", obj.optString("plural"))
        assertEquals("des Stiftes", obj.optString("genitiveSingular"))
        assertEquals("den Stiften", obj.optString("dativePlural"))
        assertEquals("pen / pencil", obj.optString("english"))

        val details = com.example.data.model.extractNounQuickDetails(nounEntity)
        assertEquals("der Stift", details.singular)
        assertEquals("die Stifte", details.plural)
        assertEquals("Maskulin", details.gender)
        assertEquals("des Stiftes", details.genitiveSingular)
        assertEquals("den Stiften", details.dativePlural)
        assertEquals("pen / pencil", details.englishMeaning)
    }
}
