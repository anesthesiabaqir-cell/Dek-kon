package com.example.data.model

import org.json.JSONObject
import java.util.Locale

/**
 * Grammatical Type toggle: Nomen (Noun) or Verb
 */
enum class GrammarType(val displayName: String, val apiPromptType: String) {
    NOMEN("Nomen", "Nomen (Deklination)"),
    VERB("Verb", "Verb (Konjugation)");

    companion object {
        fun fromString(str: String?): GrammarType =
            if (str?.trim()?.equals("Verb", ignoreCase = true) == true) VERB else NOMEN
    }
}

/**
 * Represents a single declension case row (Nominativ, Akkusativ, Dativ, Genitiv).
 */
data class DeclensionCaseRow(
    val caseKey: String,          // nominativ, akkusativ, dativ, genitiv
    val caseNameDe: String,       // Nominativ, Akkusativ, Dativ, Genitiv
    val caseQuestionDe: String,   // Wer/Was?, Wen?, Wem?, Wessen?
    val definite: String,         // e.g. "der Tisch"
    val indefinite: String,       // e.g. "ein Tisch" / "keine Tische"
    val noArticle: String         // e.g. "Tisch"
)

/**
 * Group of declensions for a specific grammatical number (Singular or Plural).
 */
data class DeclensionTableGroup(
    val numberDe: String,         // "Singular", "Plural"
    val rows: List<DeclensionCaseRow>
)

/**
 * Full declension result for a German noun.
 */
data class WordDeclensionResult(
    val word: String,             // Base noun, capitalized
    val gender: String,           // Maskulin, Feminin, Neutrum, Plural
    val genderArticle: String,    // der, die, das
    val pluralNoun: String,       // e.g. "die Tische"
    val meaningEnglish: String,   // Meaning in English
    val singular: DeclensionTableGroup,
    val plural: DeclensionTableGroup,
    val rawJson: String = ""
) {
    companion object {
        fun fromJson(jsonStr: String): WordDeclensionResult {
            val root = JSONObject(jsonStr)
            val word = root.optString("word", "").trim()
            val gender = root.optString("gender", "Maskulin").trim()
            val genderArticle = root.optString("genderArticle", "der").trim()
            val pluralNoun = root.optString("pluralNoun", "").trim()
            val meaningEnglish = root.optString("meaningEnglish",
                root.optString("meaning", root.optString("meaningArabic", ""))
            ).trim()

            val singularObj = root.optJSONObject("singular") ?: JSONObject()
            val pluralObj = root.optJSONObject("plural") ?: JSONObject()

            val singularRows = listOf(
                createRow("nominativ", "Nominativ", "Wer oder was?", singularObj),
                createRow("akkusativ", "Akkusativ", "Wen oder was?", singularObj),
                createRow("dativ", "Dativ", "Wem?", singularObj),
                createRow("genitiv", "Genitiv", "Wessen?", singularObj)
            )

            val pluralRows = listOf(
                createRow("nominativ", "Nominativ", "Wer oder was?", pluralObj),
                createRow("akkusativ", "Akkusativ", "Wen oder was?", pluralObj),
                createRow("dativ", "Dativ", "Wem?", pluralObj),
                createRow("genitiv", "Genitiv", "Wessen?", pluralObj)
            )

            return WordDeclensionResult(
                word = word,
                gender = gender,
                genderArticle = genderArticle,
                pluralNoun = pluralNoun,
                meaningEnglish = meaningEnglish,
                singular = DeclensionTableGroup("Singular", singularRows),
                plural = DeclensionTableGroup("Plural", pluralRows),
                rawJson = jsonStr
            )
        }

        private fun createRow(
            key: String,
            nameDe: String,
            questionDe: String,
            container: JSONObject
        ): DeclensionCaseRow {
            val caseObj = container.optJSONObject(key) ?: JSONObject()
            val definite = caseObj.optString("definite", "-")
            val indefinite = caseObj.optString("indefinite", "-")
            val noArticle = caseObj.optString("noArticle", "-")

            return DeclensionCaseRow(
                caseKey = key,
                caseNameDe = nameDe,
                caseQuestionDe = questionDe,
                definite = definite,
                indefinite = indefinite,
                noArticle = noArticle
            )
        }
    }
}

/**
 * Conjugation of a verb for a specific tense (ich, du, er/sie/es, wir, ihr, sie/Sie).
 */
data class VerbTenseConjugation(
    val tenseKey: String,         // praesens, praeteritum, perfekt, plusquamperfekt, futur1, futur2
    val tenseNameDe: String,      // Präsens, Präteritum, Perfekt, Plusquamperfekt, Futur I, Futur II
    val ich: String,
    val du: String,
    val erSieEs: String,
    val wir: String,
    val ihr: String,
    val sieSie: String
)

/**
 * Imperative forms of a verb.
 */
data class VerbImperativ(
    val du: String,               // e.g. "arbeite!"
    val ihr: String,              // e.g. "arbeitet!"
    val sie: String               // e.g. "arbeiten Sie!"
)

/**
 * Full conjugation result for a German verb.
 */
data class VerbConjugationResult(
    val word: String,             // Infinitiv
    val infinitiv: String,        // e.g. "arbeiten"
    val hilfsverb: String,        // "haben" or "sein"
    val auxiliary: String = "",   // "(hat)" or "(ist)"
    val praeteritum: String = "", // e.g. "nahm" / "bildete"
    val partizip1: String,        // e.g. "arbeitend"
    val partizip2: String,        // e.g. "gearbeitet"
    val verbType: String = "",    // "stark" / "schwach" / "gemischt"
    val separable: String = "",   // "trennbar" / "untrennbar"
    val caseObject: String = "",  // "Akk." / "Dat." / "Gen."
    val meaningEnglish: String,   // English translation
    val imperativ: VerbImperativ,
    val tenses: List<VerbTenseConjugation>,
    val rawJson: String = ""
) {
    companion object {
        fun fromJson(jsonStr: String): VerbConjugationResult {
            val root = JSONObject(jsonStr)
            val word = root.optString("word", root.optString("infinitiv", "")).trim()
            val infinitiv = root.optString("infinitiv", word).trim()
            val hilfsverb = root.optString("hilfsverb", "haben").trim()
            val auxiliary = root.optString("auxiliary", "").trim().ifBlank {
                if (hilfsverb.contains("sein", true) || hilfsverb.equals("ist", true)) "(ist)" else "(hat)"
            }
            val partizip1 = root.optString("partizip1", "").trim()
            val partizip2 = root.optString("partizip2", "").trim()
            val verbType = root.optString("verbType", root.optString("verb_type", "")).trim()
            val separable = root.optString("separable", "").trim()
            var caseObject = root.optString("caseObject", root.optString("case_object", "")).trim()
            if (caseObject.isBlank() && word.isNotBlank()) {
                caseObject = getFallbackVerbCaseObject(word)
            }
            val meaningEnglish = root.optString("meaningEnglish",
                root.optString("english", root.optString("meaning", ""))
            ).trim()

            val impObj = root.optJSONObject("imperativ") ?: JSONObject()
            val imperativ = VerbImperativ(
                du = impObj.optString("du", "-").trim(),
                ihr = impObj.optString("ihr", "-").trim(),
                sie = impObj.optString("sie", "-").trim()
            )

            val tensesObj = root.optJSONObject("tenses") ?: JSONObject()

            val tensesList = listOf(
                createTenseRow("praesens", "Präsens", tensesObj),
                createTenseRow("praeteritum", "Präteritum", tensesObj),
                createTenseRow("perfekt", "Perfekt", tensesObj),
                createTenseRow("plusquamperfekt", "Plusquamperfekt", tensesObj),
                createTenseRow("futur1", "Futur I", tensesObj),
                createTenseRow("futur2", "Futur II", tensesObj)
            )

            var praeteritum = root.optString("praeteritum", "").trim()
            if (praeteritum.isBlank() || praeteritum.startsWith("{")) {
                val praetObj = tensesObj.optJSONObject("praeteritum")
                praeteritum = praetObj?.optString("erSieEs", praetObj.optString("ich", "")).orEmpty().trim()
            }
            if (praeteritum.contains(" ")) {
                val tokens = praeteritum.split("\\s+".toRegex())
                if (tokens.size >= 2 && (tokens[0].equals("er", true) || tokens[0].contains("/"))) {
                    praeteritum = tokens.last()
                }
            }

            return VerbConjugationResult(
                word = word,
                infinitiv = infinitiv,
                hilfsverb = hilfsverb,
                auxiliary = auxiliary,
                praeteritum = praeteritum,
                partizip1 = partizip1,
                partizip2 = partizip2,
                verbType = verbType,
                separable = separable,
                caseObject = caseObject,
                meaningEnglish = meaningEnglish,
                imperativ = imperativ,
                tenses = tensesList,
                rawJson = jsonStr
            )
        }

        private fun createTenseRow(
            key: String,
            nameDe: String,
            container: JSONObject
        ): VerbTenseConjugation {
            val tenseObj = container.optJSONObject(key) ?: JSONObject()
            return VerbTenseConjugation(
                tenseKey = key,
                tenseNameDe = nameDe,
                ich = tenseObj.optString("ich", "-").trim(),
                du = tenseObj.optString("du", "-").trim(),
                erSieEs = tenseObj.optString("erSieEs", tenseObj.optString("er_sie_es", "-")).trim(),
                wir = tenseObj.optString("wir", "-").trim(),
                ihr = tenseObj.optString("ihr", "-").trim(),
                sieSie = tenseObj.optString("sieSie", tenseObj.optString("sie_Sie", "-")).trim()
            )
        }
    }
}

/**
 * Fast glanceable quick details for a German verb:
 * 1. Auxiliary Verb: (hat) or (ist)
 * 2. Präteritum: simple past form (e.g. "nahm" or "bildete")
 * 3. Partizip II: past participle (e.g. "genommen" or "gebildet")
 * 4. Verb Type: "stark", "schwach", "gemischt"
 * 5. Separability: "trennbar" or "untrennbar" (if applicable, otherwise omit)
 * 6. Case object: "Akk.", "Dat.", "Gen." (if applicable, otherwise omit)
 * 7. English Translation: (e.g. "to form, to educate, to build")
 */
data class VerbQuickDetails(
    val verb: String = "",
    val auxiliary: String = "",       // "(hat)" or "(ist)"
    val praeteritum: String = "",     // e.g. "nahm" or "bildete"
    val partizip1: String = "",       // e.g. "bildend"
    val partizip2: String = "",       // e.g. "genommen" or "gebildet"
    val verbType: String = "",        // e.g. "stark" / "schwach" / "gemischt"
    val separable: String = "",       // e.g. "trennbar" / "untrennbar"
    val caseObject: String = "",      // e.g. "Akk." / "Dat." / "Gen."
    val englishMeaning: String = ""   // e.g. "to form, to educate, to build"
) {
    /**
     * Formats the 5 core details + auxiliary + case object in exact order:
     * Details: (hat) · bildete · Part I: bildend · Part II: gebildet · schwach · trennbar · Akk.
     */
    fun formatCoreDetailsString(): String {
        val items = mutableListOf<String>()
        if (auxiliary.isNotBlank()) items.add(auxiliary)
        if (praeteritum.isNotBlank()) items.add(praeteritum)
        if (partizip1.isNotBlank()) {
            val p1Formatted = if (partizip1.startsWith("Part I", ignoreCase = true)) partizip1 else "Part I: $partizip1"
            items.add(p1Formatted)
        }
        if (partizip2.isNotBlank()) {
            val p2Formatted = if (partizip2.startsWith("Part II", ignoreCase = true)) partizip2 else "Part II: $partizip2"
            items.add(p2Formatted)
        }
        if (verbType.isNotBlank()) items.add(verbType)
        if (separable.isNotBlank()) items.add(separable)
        if (caseObject.isNotBlank()) items.add(caseObject)

        val content = items.joinToString(" · ")
        return if (content.isNotBlank()) "Details: $content" else ""
    }

    /**
     * Formats the top-row right side string:
     * e.g. "(hat)  nahm  genommen  ·  stark  ·  Akk."
     * or "(hat)  bildete  gebildet  ·  schwach  ·  Akk."
     */
    fun formatQuickDetailsString(): String {
        val formsPart = listOfNotNull(
            auxiliary.takeIf { it.isNotBlank() },
            praeteritum.takeIf { it.isNotBlank() },
            partizip2.takeIf { it.isNotBlank() }
        ).joinToString("  ")

        val tags = mutableListOf<String>()
        if (formsPart.isNotBlank()) tags.add(formsPart)
        if (verbType.isNotBlank()) tags.add(verbType)
        if (separable.isNotBlank()) tags.add(separable)
        if (caseObject.isNotBlank()) tags.add(caseObject)

        return tags.joinToString("  ·  ")
    }
}

/**
 * Extracts the 5 quick details + English meaning from a verb's raw JSON or fallback fields.
 */
fun extractVerbQuickDetails(
    word: String,
    rawJson: String,
    fallbackHilfsverb: String = "",
    fallbackPartizip2: String = "",
    fallbackMeaning: String = ""
): VerbQuickDetails {
    val jsonObj = if (rawJson.isNotBlank()) {
        try { JSONObject(rawJson) } catch (_: Exception) { null }
    } else null

    // 1. Auxiliary
    val rawHilfsverb = jsonObj?.optString("auxiliary")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("hilfsverb")?.takeIf { it.isNotBlank() }
        ?: fallbackHilfsverb.takeIf { it.isNotBlank() }
        ?: ""
    val auxiliary = when {
        rawHilfsverb.isBlank() -> ""
        rawHilfsverb.startsWith("(") && rawHilfsverb.endsWith(")") -> rawHilfsverb
        rawHilfsverb.contains("sein", ignoreCase = true) || rawHilfsverb.equals("ist", ignoreCase = true) -> "(ist)"
        rawHilfsverb.contains("haben", ignoreCase = true) || rawHilfsverb.equals("hat", ignoreCase = true) -> "(hat)"
        else -> "($rawHilfsverb)"
    }

    // 2. Präteritum
    var praeteritum = jsonObj?.optString("praeteritum")?.trim() ?: ""
    if (praeteritum.isBlank() || praeteritum.startsWith("{")) {
        val tensesObj = jsonObj?.optJSONObject("tenses")
        val praetObj = tensesObj?.optJSONObject("praeteritum")
        praeteritum = praetObj?.optString("erSieEs")?.takeIf { it.isNotBlank() && it != "-" }
            ?: praetObj?.optString("ich")?.takeIf { it.isNotBlank() && it != "-" }
            ?: ""
    }
    if (praeteritum.contains(" ")) {
        val tokens = praeteritum.split("\\s+".toRegex())
        if (tokens.size >= 2 && (tokens[0].equals("er", true) || tokens[0].contains("/"))) {
            praeteritum = tokens.last()
        }
    }

    // 3. Partizip I
    var partizip1 = jsonObj?.optString("partizip1")?.trim()?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("partizip_1")?.trim()?.takeIf { it.isNotBlank() }
        ?: ""
    if (partizip1.isBlank() && word.isNotBlank()) {
        val trimmedWord = word.trim()
        partizip1 = if (trimmedWord.endsWith("d", ignoreCase = true)) trimmedWord else "${trimmedWord}d"
    }

    // 4. Partizip II
    var partizip2 = jsonObj?.optString("partizip2")?.trim() ?: ""
    if (partizip2.isBlank()) {
        partizip2 = fallbackPartizip2.trim()
    }
    if (partizip2.isBlank()) {
        val tensesObj = jsonObj?.optJSONObject("tenses")
        val perfektObj = tensesObj?.optJSONObject("perfekt")
        val perfektEr = perfektObj?.optString("erSieEs")?.takeIf { it.isNotBlank() && it != "-" }
            ?: perfektObj?.optString("ich")?.takeIf { it.isNotBlank() && it != "-" }
            ?: ""
        if (perfektEr.contains(" ")) {
            partizip2 = perfektEr.substringAfterLast(" ").trim()
        }
    }

    // 5. Verb Type
    var verbType = jsonObj?.optString("verbType")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("verb_type")?.takeIf { it.isNotBlank() }
        ?: ""
    verbType = when {
        verbType.contains("stark", ignoreCase = true) -> "stark"
        verbType.contains("schwach", ignoreCase = true) -> "schwach"
        verbType.contains("gemischt", ignoreCase = true) -> "gemischt"
        verbType.contains("unregel", ignoreCase = true) -> "unregelmäßig"
        else -> verbType.trim()
    }

    // 6. Separability
    var separable = jsonObj?.optString("separable")?.trim() ?: ""
    if (separable.isBlank()) {
        val isSep = if (jsonObj?.has("separable") == true) jsonObj.optBoolean("separable") else null
            ?: if (jsonObj?.has("isSeparable") == true) jsonObj.optBoolean("isSeparable") else null
        if (isSep == true) {
            separable = "trennbar"
        }
    } else {
        separable = when {
            separable.equals("true", ignoreCase = true) || (separable.contains("trennbar", ignoreCase = true) && !separable.contains("untrennbar", ignoreCase = true)) -> "trennbar"
            separable.equals("false", ignoreCase = true) || separable.contains("untrennbar", ignoreCase = true) -> "untrennbar"
            separable == "nein" || separable == "no" || separable == "-" -> ""
            else -> separable
        }
    }

    // 7. Case object
    var caseObject = jsonObj?.optString("caseObject")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("case_object")?.takeIf { it.isNotBlank() }
        ?: ""
    caseObject = when {
        caseObject.contains("akk", ignoreCase = true) && caseObject.contains("dat", ignoreCase = true) -> "Akk. / Dat."
        caseObject.contains("akk", ignoreCase = true) -> "Akk."
        caseObject.contains("dat", ignoreCase = true) -> "Dat."
        caseObject.contains("gen", ignoreCase = true) -> "Gen."
        caseObject == "-" || caseObject.equals("none", ignoreCase = true) || caseObject.equals("kein", ignoreCase = true) -> ""
        else -> caseObject.trim()
    }
    if (caseObject.isBlank() && word.isNotBlank()) {
        caseObject = getFallbackVerbCaseObject(word)
    }

    // 8. English Meaning
    val englishMeaning = fallbackMeaning.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("meaningEnglish")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("english")?.takeIf { it.isNotBlank() }
        ?: jsonObj?.optString("meaning")?.takeIf { it.isNotBlank() }
        ?: ""

    return VerbQuickDetails(
        verb = word,
        auxiliary = auxiliary,
        praeteritum = praeteritum,
        partizip1 = partizip1,
        partizip2 = partizip2,
        verbType = verbType,
        separable = separable,
        caseObject = caseObject,
        englishMeaning = englishMeaning
    )
}

fun extractVerbQuickDetails(entity: com.example.data.local.WordHistoryEntity): VerbQuickDetails =
    extractVerbQuickDetails(
        word = entity.word,
        rawJson = if (entity.rawJsonResult.isNotBlank()) entity.rawJsonResult else entity.data,
        fallbackHilfsverb = entity.gender,
        fallbackPartizip2 = entity.genderArticle,
        fallbackMeaning = entity.meaningEnglish
    )

/**
 * Fast glanceable quick details for a German noun:
 * 1. Singular with definite article (e.g. "der Stift")
 * 2. Plural with definite article (e.g. "die Stifte")
 * 3. Gender (e.g. "Maskulin", "Feminin", "Neutrum")
 * 4. Genitive Singular (e.g. "des Stiftes")
 * 5. Dative Plural (e.g. "den Stiften")
 * 6. English Translation (e.g. "pen / pencil")
 */
data class NounQuickDetails(
    val singular: String = "",
    val plural: String = "",
    val gender: String = "",
    val genitiveSingular: String = "",
    val dativePlural: String = "",
    val englishMeaning: String = ""
)

/**
 * Extracts glanceable noun details from AI response JSON or fallback history fields.
 */
fun extractNounQuickDetails(
    word: String,
    rawJson: String,
    fallbackGender: String = "",
    fallbackGenderArticle: String = "",
    fallbackMeaning: String = ""
): NounQuickDetails {
    val jsonObj = if (rawJson.isNotBlank()) {
        try { JSONObject(rawJson) } catch (_: Exception) { null }
    } else null

    // 1. Gender & Gender Article
    val rawGender = jsonObj?.optString("gender")?.takeIf { it.isNotBlank() }
        ?: fallbackGender.takeIf { it.isNotBlank() }
        ?: "Maskulin"

    val gender = when {
        rawGender.contains("mask", ignoreCase = true) -> "Maskulin"
        rawGender.contains("fem", ignoreCase = true) -> "Feminin"
        rawGender.contains("neut", ignoreCase = true) -> "Neutrum"
        else -> rawGender.trim()
    }

    val genderArticle = jsonObj?.optString("genderArticle")?.takeIf { it.isNotBlank() }
        ?: fallbackGenderArticle.takeIf { it.isNotBlank() }
        ?: when (gender.lowercase(Locale.ROOT)) {
            "maskulin" -> "der"
            "feminin" -> "die"
            "neutrum" -> "das"
            else -> ""
        }

    // 2. Singular (with definite article, e.g. "der Stift")
    val singularObj = jsonObj?.optJSONObject("singular")
    val nomSingularObj = singularObj?.optJSONObject("nominativ")
    var singularDefinite = nomSingularObj?.optString("definite")?.trim().orEmpty()
    if (singularDefinite.isBlank() || singularDefinite == "-") {
        val topSingular = jsonObj?.optString("singular")?.trim().orEmpty()
        if (topSingular.isNotBlank() && !topSingular.startsWith("{")) {
            singularDefinite = topSingular
        }
    }
    val cleanWord = jsonObj?.optString("word")?.trim()?.takeIf { it.isNotBlank() } ?: word.trim()
    val singular = when {
        singularDefinite.isNotBlank() && singularDefinite != "-" -> singularDefinite
        genderArticle.isNotBlank() && cleanWord.isNotBlank() -> "$genderArticle $cleanWord"
        else -> cleanWord
    }

    // 3. Plural (with definite article, e.g. "die Stifte")
    val pluralObj = jsonObj?.optJSONObject("plural")
    val nomPluralObj = pluralObj?.optJSONObject("nominativ")
    var pluralDefinite = jsonObj?.optString("pluralNoun")?.trim().orEmpty()
    if (pluralDefinite.isBlank() || pluralDefinite == "-") {
        pluralDefinite = nomPluralObj?.optString("definite")?.trim().orEmpty()
    }
    if (pluralDefinite.isBlank() || pluralDefinite == "-") {
        val topPlural = jsonObj?.optString("plural")?.trim().orEmpty()
        if (topPlural.isNotBlank() && !topPlural.startsWith("{")) {
            pluralDefinite = topPlural
        }
    }
    if (pluralDefinite.isBlank() || pluralDefinite == "-") {
        val noArt = nomPluralObj?.optString("noArticle")?.trim().orEmpty()
        if (noArt.isNotBlank() && noArt != "-") {
            pluralDefinite = if (noArt.startsWith("die ", ignoreCase = true)) noArt else "die $noArt"
        }
    }
    val plural = when {
        pluralDefinite.isBlank() || pluralDefinite == "-" -> ""
        pluralDefinite.startsWith("die ", ignoreCase = true) -> pluralDefinite
        else -> "die $pluralDefinite"
    }

    // 4. Genitive Singular (e.g. "des Stiftes")
    var genitiveSingular = jsonObj?.optString("genitiveSingular")?.trim().orEmpty()
    if (genitiveSingular.isBlank() || genitiveSingular == "-") {
        genitiveSingular = jsonObj?.optString("genitive")?.trim().orEmpty()
    }
    if (genitiveSingular.isBlank() || genitiveSingular == "-") {
        val genSingularObj = singularObj?.optJSONObject("genitiv")
        genitiveSingular = genSingularObj?.optString("definite")?.trim().orEmpty()
        if (genitiveSingular.isBlank() || genitiveSingular == "-") {
            val noArt = genSingularObj?.optString("noArticle")?.trim().orEmpty()
            if (noArt.isNotBlank() && noArt != "-") {
                val art = when (gender.lowercase(Locale.ROOT)) {
                    "feminin" -> "der"
                    else -> "des"
                }
                genitiveSingular = "$art $noArt"
            }
        }
    }
    genitiveSingular = genitiveSingular.removePrefix("Gen.:").removePrefix("Gen:").removePrefix("Gen.").trim()

    // 5. Dative Plural (e.g. "den Stiften")
    var dativePlural = jsonObj?.optString("dativePlural")?.trim().orEmpty()
    if (dativePlural.isBlank() || dativePlural == "-") {
        val datPluralObj = pluralObj?.optJSONObject("dativ")
        dativePlural = datPluralObj?.optString("definite")?.trim().orEmpty()
        if (dativePlural.isBlank() || dativePlural == "-") {
            val noArt = datPluralObj?.optString("noArticle")?.trim().orEmpty()
            if (noArt.isNotBlank() && noArt != "-") {
                dativePlural = if (noArt.startsWith("den ", ignoreCase = true)) noArt else "den $noArt"
            }
        }
    }
    dativePlural = dativePlural.removePrefix("Dat. Pl.:").removePrefix("Dat.Pl.:").removePrefix("Dat. Pl.").removePrefix("Dat:").trim()

    // 6. English Meaning (strictly no Arabic)
    var englishMeaning = jsonObj?.optString("meaningEnglish")?.trim().orEmpty()
    if (englishMeaning.isBlank()) {
        englishMeaning = jsonObj?.optString("english")?.trim().orEmpty()
    }
    if (englishMeaning.isBlank()) {
        englishMeaning = fallbackMeaning.trim()
    }
    if (englishMeaning.isBlank()) {
        englishMeaning = jsonObj?.optString("meaning")?.trim().orEmpty()
    }
    englishMeaning = englishMeaning.removePrefix("Eng.:").removePrefix("Eng:").removePrefix("Eng.").trim()
    // Strip any Arabic characters completely
    englishMeaning = englishMeaning.replace(Regex("[\\u0600-\\u06FF]"), "").replace(Regex("\\s+"), " ").trim()
    if (englishMeaning.startsWith("/") || englishMeaning.endsWith("/")) {
        englishMeaning = englishMeaning.trim('/', ' ')
    }

    return NounQuickDetails(
        singular = singular,
        plural = plural,
        gender = gender,
        genitiveSingular = genitiveSingular,
        dativePlural = dativePlural,
        englishMeaning = englishMeaning
    )
}

fun extractNounQuickDetails(entity: com.example.data.local.WordHistoryEntity): NounQuickDetails =
    extractNounQuickDetails(
        word = entity.word,
        rawJson = if (entity.rawJsonResult.isNotBlank()) entity.rawJsonResult else entity.data,
        fallbackGender = entity.gender,
        fallbackGenderArticle = entity.genderArticle,
        fallbackMeaning = entity.meaningEnglish
    )

/**
 * Unified Grammar Result representing either a Noun Declension or a Verb Conjugation.
 */
sealed class GrammarResult {
    abstract val word: String
    abstract val meaningEnglish: String
    abstract val type: GrammarType
    abstract val rawJson: String

    data class Noun(val declension: WordDeclensionResult) : GrammarResult() {
        override val word: String get() = declension.word
        override val meaningEnglish: String get() = declension.meaningEnglish
        override val type: GrammarType get() = GrammarType.NOMEN
        override val rawJson: String get() = declension.rawJson
    }

    data class Verb(val conjugation: VerbConjugationResult) : GrammarResult() {
        override val word: String get() = conjugation.word
        override val meaningEnglish: String get() = conjugation.meaningEnglish
        override val type: GrammarType get() = GrammarType.VERB
        override val rawJson: String get() = conjugation.rawJson
    }

    companion object {
        fun fromJson(jsonStr: String, fallbackType: GrammarType = GrammarType.NOMEN): GrammarResult {
            val root = JSONObject(jsonStr)
            val typeStr = root.optString("type", "").trim()
            val isVerb = typeStr.equals("Verb", ignoreCase = true) ||
                    root.has("tenses") || root.has("infinitiv") ||
                    fallbackType == GrammarType.VERB

            return if (isVerb && (root.has("tenses") || fallbackType == GrammarType.VERB)) {
                Verb(VerbConjugationResult.fromJson(jsonStr))
            } else {
                Noun(WordDeclensionResult.fromJson(jsonStr))
            }
        }
    }
}

/**
 * Fallback mapping for the case governed by common German verbs (Akkusativ / Dativ / Genitiv).
 */
fun getFallbackVerbCaseObject(verb: String): String {
    val v = verb.trim().lowercase()
    return when (v) {
        "bilden", "sehen", "finden", "nehmen", "geben", "haben", "essen", "trinken", "lesen", "schreiben",
        "kaufen", "fragen", "hören", "kochen", "lernen", "machen", "öffnen", "suchen", "tragen",
        "verstehen", "zahlen", "bezahlen", "lieben", "brauchen", "kennen", "treffen", "schließen", "bringen",
        "verlieren", "gewinnen", "vergessen", "erinnern", "bitten", "erwarten", "erklären", "erzählen",
        "besuchen", "anrufen", "einladen", "beginnen", "anfangen", "beenden", "halten", "lassen", "wählen",
        "zeigen", "drücken", "ziehen", "bauen", "benutzen", "nutzen", "erreichen", "empfehlen", "bekommen",
        "erhalten", "schaffen", "versuchen", "prüfen", "wechseln", "verkaufen", "holen", "tauschen" -> "Akk."

        "helfen", "danken", "folgen", "gehören", "gratulieren", "schmecken", "vertrauen", "antworten",
        "begegnen", "gefallen", "glauben", "passen", "schaden", "verzeihen", "wehtun", "zuhören",
        "zustimmen", "widersprechen", "ähneln", "dienen", "fehlen", "gelingen", "nützen", "gehorchen",
        "genügen", "passieren", "zusehen" -> "Dat."

        "gedenken", "bedürfen", "anklagen", "beschuldigen", "überführen", "entbehren",
        "rühmen", "schämen", "belehren", "verdächtigen", "harren", "brüsten" -> "Gen."

        else -> ""
    }
}

