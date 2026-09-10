package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material 3 Earth Tone Themes for Deklination:
 * 40 distinctly selected earth tones across 5 color families:
 * 1. Rotbraun & Terracotta (Warm Earth)
 * 2. Sand & Beige (Neutral Warm)
 * 3. Grau & Schiefer (Cool Earth)
 * 4. Braun & Umbra (Deep Earth)
 * 5. Gedämpfte Töne (Lehm & Stein)
 *
 * Default theme: SCHIEFER (#6A7A7A)
 */
enum class AppThemePackage(
    val id: String,
    val germanName: String,
    val shortName: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val onSurfaceHex: Long,
    val primaryContainerHex: Long,
    val onPrimaryContainerHex: Long,
    val accentHex: Long,
    val borderHex: Long,
    val surfaceVariantHex: Long,
    val onSurfaceVariantHex: Long,
    val outlineHex: Long,
    val swatchHex: Long = primaryHex,
    val arabicName: String = ""
) {
    // =========================================================================
    // 1. Rotbraun & Terracotta (Warm Earth)
    // =========================================================================
    TERRACOTTA(
        id = "Terracotta",
        germanName = "Terracotta (Warmer Ton)",
        shortName = "Terracotta",
        primaryHex = 0xFFB85D38,
        secondaryHex = 0xFF9E5234,
        backgroundHex = 0xFFFFF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2D1B15,
        primaryContainerHex = 0xFFFFDBCF,
        onPrimaryContainerHex = 0xFF3B1205,
        accentHex = 0xFFD27D56,
        borderHex = 0xFFF5D4C7,
        surfaceVariantHex = 0xFFFDF0E9,
        onSurfaceVariantHex = 0xFF5B433B,
        outlineHex = 0xFFB87A64,
        swatchHex = 0xFFD27D56 // #D27D56
    ),
    ZIEGEL(
        id = "Ziegel",
        germanName = "Ziegel (Backsteinrot)",
        shortName = "Ziegel",
        primaryHex = 0xFFA0482B,
        secondaryHex = 0xFF8A3E24,
        backgroundHex = 0xFFFFF6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2A1610,
        primaryContainerHex = 0xFFFFDACF,
        onPrimaryContainerHex = 0xFF3B0F04,
        accentHex = 0xFFB85A3A,
        borderHex = 0xFFF3C7BC,
        surfaceVariantHex = 0xFFFCECE7,
        onSurfaceVariantHex = 0xFF553D37,
        outlineHex = 0xFFA56654,
        swatchHex = 0xFFB85A3A // #B85A3A
    ),
    ROST(
        id = "Rost",
        germanName = "Rost (Echtes Rostrot)",
        shortName = "Rost",
        primaryHex = 0xFF933B1E,
        secondaryHex = 0xFF7D3219,
        backgroundHex = 0xFFFFF5F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF28130B,
        primaryContainerHex = 0xFFFFD7C8,
        onPrimaryContainerHex = 0xFF380A00,
        accentHex = 0xFFA84A2A,
        borderHex = 0xFFEEC0AF,
        surfaceVariantHex = 0xFFF9E8E2,
        onSurfaceVariantHex = 0xFF543830,
        outlineHex = 0xFF9C5A48,
        swatchHex = 0xFFA84A2A // #A84A2A
    ),
    KUPFER(
        id = "Kupfer",
        germanName = "Kupfer (Warmes Metallbraun)",
        shortName = "Kupfer",
        primaryHex = 0xFFA16639,
        secondaryHex = 0xFF89552E,
        backgroundHex = 0xFFFFF8F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF291B11,
        primaryContainerHex = 0xFFFFDCC4,
        onPrimaryContainerHex = 0xFF341703,
        accentHex = 0xFFB87A4A,
        borderHex = 0xFFF3CFB6,
        surfaceVariantHex = 0xFFFDF0E5,
        onSurfaceVariantHex = 0xFF564336,
        outlineHex = 0xFFA3785A,
        swatchHex = 0xFFB87A4A // #B87A4A
    ),
    MAHAGONI(
        id = "Mahagoni",
        germanName = "Mahagoni (Edles Dunkelrotbraun)",
        shortName = "Mahagoni",
        primaryHex = 0xFF5E2F20,
        secondaryHex = 0xFF4F271A,
        backgroundHex = 0xFFFDF5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF22110C,
        primaryContainerHex = 0xFFE2BDB2,
        onPrimaryContainerHex = 0xFF2E1007,
        accentHex = 0xFF6A3A2A,
        borderHex = 0xFFD2AAA0,
        surfaceVariantHex = 0xFFF2E6E2,
        onSurfaceVariantHex = 0xFF4B3934,
        outlineHex = 0xFF7F554B,
        swatchHex = 0xFF6A3A2A // #6A3A2A
    ),
    ZIMT(
        id = "Zimt",
        germanName = "Zimt (Würziges Braun)",
        shortName = "Zimt",
        primaryHex = 0xFF8A4E30,
        secondaryHex = 0xFFA36545,
        backgroundHex = 0xFFFAF6F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261914,
        primaryContainerHex = 0xFFF7D9CC,
        onPrimaryContainerHex = 0xFF35170B,
        accentHex = 0xFFA86A4A,
        borderHex = 0xFFEACAB9,
        surfaceVariantHex = 0xFFF6ECE6,
        onSurfaceVariantHex = 0xFF53433C,
        outlineHex = 0xFF8D7166,
        swatchHex = 0xFFA86A4A // #A86A4A
    ),
    SIENA(
        id = "Siena",
        germanName = "Siena (Gebrannte Erde)",
        shortName = "Siena",
        primaryHex = 0xFF9E5224,
        secondaryHex = 0xFFB86A3A,
        backgroundHex = 0xFFFFF7F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2A160A,
        primaryContainerHex = 0xFFFFD7BF,
        onPrimaryContainerHex = 0xFF351200,
        accentHex = 0xFFB86A3A,
        borderHex = 0xFFF2C8B0,
        surfaceVariantHex = 0xFFFCECE2,
        onSurfaceVariantHex = 0xFF563D30,
        outlineHex = 0xFFA46E53,
        swatchHex = 0xFFB86A3A // #B86A3A
    ),
    TON(
        id = "Ton",
        germanName = "Ton (Gebrannter Lehmton)",
        shortName = "Ton",
        primaryHex = 0xFFA86244,
        secondaryHex = 0xFF905238,
        backgroundHex = 0xFFFFF7F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF2B1913,
        primaryContainerHex = 0xFFFFDCCF,
        onPrimaryContainerHex = 0xFF36150B,
        accentHex = 0xFFC47A5A,
        borderHex = 0xFFF4CEBF,
        surfaceVariantHex = 0xFFFDEEE7,
        onSurfaceVariantHex = 0xFF574139,
        outlineHex = 0xFFA87766,
        swatchHex = 0xFFC47A5A // #C47A5A
    ),

    // =========================================================================
    // 2. Sand & Beige (Neutral Warm)
    // =========================================================================
    SAND(
        id = "Sand",
        germanName = "Sand (Warmer Wüstensand)",
        shortName = "Sand",
        primaryHex = 0xFF8C714C,
        secondaryHex = 0xFFA58A65,
        backgroundHex = 0xFFFAF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF251F17,
        primaryContainerHex = 0xFFF3E7D7,
        onPrimaryContainerHex = 0xFF2A2012,
        accentHex = 0xFFC4A882,
        borderHex = 0xFFE5D8C7,
        surfaceVariantHex = 0xFFF5F0E8,
        onSurfaceVariantHex = 0xFF50463A,
        outlineHex = 0xFF867868,
        swatchHex = 0xFFC4A882 // #C4A882
    ),
    DUENE(
        id = "Düne",
        germanName = "Düne (Sanftes Dünenbeige)",
        shortName = "Düne",
        primaryHex = 0xFF877663,
        secondaryHex = 0xFF9E8D79,
        backgroundHex = 0xFFFBF9F7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF24201B,
        primaryContainerHex = 0xFFEFE9E1,
        onPrimaryContainerHex = 0xFF251E17,
        accentHex = 0xFFC4B09A,
        borderHex = 0xFFE2DAD1,
        surfaceVariantHex = 0xFFF5F1EC,
        onSurfaceVariantHex = 0xFF4C4640,
        outlineHex = 0xFF7F766E,
        swatchHex = 0xFFC4B09A // #C4B09A
    ),
    CREME(
        id = "Creme",
        germanName = "Creme (Helles Warmbeige)",
        shortName = "Creme",
        primaryHex = 0xFF948567,
        secondaryHex = 0xFF7D7054,
        backgroundHex = 0xFFFCFAF5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF24211A,
        primaryContainerHex = 0xFFF5EEDD,
        onPrimaryContainerHex = 0xFF2B2516,
        accentHex = 0xFFD4C4A4,
        borderHex = 0xFFE8E0CE,
        surfaceVariantHex = 0xFFF8F4EB,
        onSurfaceVariantHex = 0xFF4E4739,
        outlineHex = 0xFF867D6C,
        swatchHex = 0xFFD4C4A4 // #D4C4A4
    ),
    NATUR(
        id = "Natur",
        germanName = "Natur (Leinen & Rohfaser)",
        shortName = "Natur",
        primaryHex = 0xFF807156,
        secondaryHex = 0xFF6D6047,
        backgroundHex = 0xFFFAF8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221E17,
        primaryContainerHex = 0xFFEAE2D2,
        onPrimaryContainerHex = 0xFF262013,
        accentHex = 0xFFB8A88A,
        borderHex = 0xFFDDD4C2,
        surfaceVariantHex = 0xFFF4F0E8,
        onSurfaceVariantHex = 0xFF4A4437,
        outlineHex = 0xFF7B7262,
        swatchHex = 0xFFB8A88A // #B8A88A
    ),
    KIES(
        id = "Kies",
        germanName = "Kies (Warmgrauer Kieselton)",
        shortName = "Kies",
        primaryHex = 0xFF73654B,
        secondaryHex = 0xFF60543C,
        backgroundHex = 0xFFF9F7F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF211D15,
        primaryContainerHex = 0xFFE2D9C7,
        onPrimaryContainerHex = 0xFF221A0C,
        accentHex = 0xFFA8987A,
        borderHex = 0xFFD4CABA,
        surfaceVariantHex = 0xFFF2ECE0,
        onSurfaceVariantHex = 0xFF484234,
        outlineHex = 0xFF766E5E,
        swatchHex = 0xFFA8987A // #A8987A
    ),
    LEHM(
        id = "Lehm",
        germanName = "Lehm (Neutraler Ton)",
        shortName = "Lehm",
        primaryHex = 0xFF965C3E,
        secondaryHex = 0xFF7D4E35,
        backgroundHex = 0xFFFAF6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261912,
        primaryContainerHex = 0xFFF7DECE,
        onPrimaryContainerHex = 0xFF32190B,
        accentHex = 0xFFB87A5A,
        borderHex = 0xFFEAD2C3,
        surfaceVariantHex = 0xFFF5EDE7,
        onSurfaceVariantHex = 0xFF52443C,
        outlineHex = 0xFF8E7364,
        swatchHex = 0xFFB87A5A // #B87A5A
    ),
    OCKER(
        id = "Ocker",
        germanName = "Ocker (Warme Goldenerde)",
        shortName = "Ocker",
        primaryHex = 0xFFA37B3E,
        secondaryHex = 0xFF896732,
        backgroundHex = 0xFFFCF9F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF282012,
        primaryContainerHex = 0xFFF9E8CE,
        onPrimaryContainerHex = 0xFF322307,
        accentHex = 0xFFC49A5A,
        borderHex = 0xFFEEDBBF,
        surfaceVariantHex = 0xFFF9F2E7,
        onSurfaceVariantHex = 0xFF544733,
        outlineHex = 0xFF9B825B,
        swatchHex = 0xFFC49A5A // #C49A5A
    ),
    GELBBRAUN(
        id = "Gelbbraun",
        germanName = "Gelbbraun (Erdenbernstein)",
        shortName = "Gelbbraun",
        primaryHex = 0xFF966C33,
        secondaryHex = 0xFF7F5B29,
        backgroundHex = 0xFFFAF7F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF261D10,
        primaryContainerHex = 0xFFF3DFBE,
        onPrimaryContainerHex = 0xFF2F1F05,
        accentHex = 0xFFB88A4A,
        borderHex = 0xFFE6D1AF,
        surfaceVariantHex = 0xFFF6EFE4,
        onSurfaceVariantHex = 0xFF504432,
        outlineHex = 0xFF907751,
        swatchHex = 0xFFB88A4A // #B88A4A
    ),

    // =========================================================================
    // 3. Grau & Schiefer (Cool Earth)
    // =========================================================================
    SCHIEFER(
        id = "Schiefer",
        germanName = "Schiefer (Naturgrau)",
        shortName = "Schiefer",
        primaryHex = 0xFF4B5B5B,
        secondaryHex = 0xFF607272,
        backgroundHex = 0xFFF5F7F7,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF151D1D,
        primaryContainerHex = 0xFFD2E0E0,
        onPrimaryContainerHex = 0xFF101C1C,
        accentHex = 0xFF6A7A7A,
        borderHex = 0xFFC2D4D4,
        surfaceVariantHex = 0xFFEAF0F0,
        onSurfaceVariantHex = 0xFF3D4848,
        outlineHex = 0xFF667575,
        swatchHex = 0xFF6A7A7A // #6A7A7A (New Default)
    ),
    KIESEL(
        id = "Kiesel",
        germanName = "Kiesel (Flussstein)",
        shortName = "Kiesel",
        primaryHex = 0xFF716355,
        secondaryHex = 0xFF87796B,
        backgroundHex = 0xFFF8F7F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221E19,
        primaryContainerHex = 0xFFE6DDD4,
        onPrimaryContainerHex = 0xFF231D16,
        accentHex = 0xFF9A8A7A,
        borderHex = 0xFFD8CCC1,
        surfaceVariantHex = 0xFFF1EDE9,
        onSurfaceVariantHex = 0xFF4A433D,
        outlineHex = 0xFF7C7268,
        swatchHex = 0xFF9A8A7A // #9A8A7A
    ),
    GRANIT(
        id = "Granit",
        germanName = "Granit (Neutrales Steingrau)",
        shortName = "Granit",
        primaryHex = 0xFF585858,
        secondaryHex = 0xFF6D6D6D,
        backgroundHex = 0xFFF6F6F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1A1A1A,
        primaryContainerHex = 0xFFD7D7D7,
        onPrimaryContainerHex = 0xFF181818,
        accentHex = 0xFF7A7A7A,
        borderHex = 0xFFCCCCCC,
        surfaceVariantHex = 0xFFEDEDED,
        onSurfaceVariantHex = 0xFF424242,
        outlineHex = 0xFF707070,
        swatchHex = 0xFF7A7A7A // #7A7A7A
    ),
    STEIN(
        id = "Stein",
        germanName = "Stein (Kühler Naturstein)",
        shortName = "Stein",
        primaryHex = 0xFF656558,
        secondaryHex = 0xFF78786B,
        backgroundHex = 0xFFF7F7F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E1E19,
        primaryContainerHex = 0xFFDFDFD4,
        onPrimaryContainerHex = 0xFF1C1C15,
        accentHex = 0xFF8A8A7A,
        borderHex = 0xFFD2D2C4,
        surfaceVariantHex = 0xFFEFEFEA,
        onSurfaceVariantHex = 0xFF45453C,
        outlineHex = 0xFF757568,
        swatchHex = 0xFF8A8A7A // #8A8A7A
    ),
    ASCHE(
        id = "Asche",
        germanName = "Asche (Sanftes Aschgrau)",
        shortName = "Asche",
        primaryHex = 0xFF737365,
        secondaryHex = 0xFF878778,
        backgroundHex = 0xFFF8F8F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF21211C,
        primaryContainerHex = 0xFFE7E7DE,
        onPrimaryContainerHex = 0xFF21211A,
        accentHex = 0xFF9A9A8A,
        borderHex = 0xFFD8D8CC,
        surfaceVariantHex = 0xFFF2F2EC,
        onSurfaceVariantHex = 0xFF48483F,
        outlineHex = 0xFF7B7B6E,
        swatchHex = 0xFF9A9A8A // #9A9A8A
    ),
    MAUVE(
        id = "Mauve",
        germanName = "Mauve (Grauviolette Erde)",
        shortName = "Mauve",
        primaryHex = 0xFF675858,
        secondaryHex = 0xFF7B6B6B,
        backgroundHex = 0xFFF7F5F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1F1A1A,
        primaryContainerHex = 0xFFDFD5D5,
        onPrimaryContainerHex = 0xFF201818,
        accentHex = 0xFF8A7A7A,
        borderHex = 0xFFD4C7C7,
        surfaceVariantHex = 0xFFEFEAEA,
        onSurfaceVariantHex = 0xFF473F3F,
        outlineHex = 0xFF786A6A,
        swatchHex = 0xFF8A7A7A // #8A7A7A
    ),
    TAUPE(
        id = "Taupe",
        germanName = "Taupe (Warmes Graubraun)",
        shortName = "Taupe",
        primaryHex = 0xFF5D4E41,
        secondaryHex = 0xFF726153,
        backgroundHex = 0xFFF7F5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E1A16,
        primaryContainerHex = 0xFFD9CCC0,
        onPrimaryContainerHex = 0xFF20170F,
        accentHex = 0xFF7A6A5A,
        borderHex = 0xFFC9BBAE,
        surfaceVariantHex = 0xFFEFEBE7,
        onSurfaceVariantHex = 0xFF453D36,
        outlineHex = 0xFF706154,
        swatchHex = 0xFF7A6A5A // #7A6A5A
    ),
    GRAPHIT(
        id = "Graphit",
        germanName = "Graphit (Tiefes Schiefergrau)",
        shortName = "Graphit",
        primaryHex = 0xFF424242,
        secondaryHex = 0xFF545454,
        backgroundHex = 0xFFF5F5F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF141414,
        primaryContainerHex = 0xFFC7C7C7,
        onPrimaryContainerHex = 0xFF121212,
        accentHex = 0xFF5A5A5A,
        borderHex = 0xFFB8B8B8,
        surfaceVariantHex = 0xFFEBEBEB,
        onSurfaceVariantHex = 0xFF3D3D3D,
        outlineHex = 0xFF636363,
        swatchHex = 0xFF5A5A5A // #5A5A5A
    ),

    // =========================================================================
    // 4. Braun & Umbra (Deep Earth)
    // =========================================================================
    UMBRA(
        id = "Umbra",
        germanName = "Umbra (Erdiges Umbrabraun)",
        shortName = "Umbra",
        primaryHex = 0xFF524429,
        secondaryHex = 0xFF675638,
        backgroundHex = 0xFFF7F6F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D180F,
        primaryContainerHex = 0xFFD8CCB5,
        onPrimaryContainerHex = 0xFF1E1608,
        accentHex = 0xFF6A5A3A,
        borderHex = 0xFFC7B99E,
        surfaceVariantHex = 0xFFEEEAE2,
        onSurfaceVariantHex = 0xFF443D30,
        outlineHex = 0xFF6D6049,
        swatchHex = 0xFF6A5A3A // #6A5A3A
    ),
    TERRA(
        id = "Terra",
        germanName = "Terra (Warmer Erdboden)",
        shortName = "Terra",
        primaryHex = 0xFF705235,
        secondaryHex = 0xFF896746,
        backgroundHex = 0xFFF9F6F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF231A12,
        primaryContainerHex = 0xFFE7D4C2,
        onPrimaryContainerHex = 0xFF28190B,
        accentHex = 0xFF8A6A4A,
        borderHex = 0xFFD6C0AB,
        surfaceVariantHex = 0xFFF2ECE6,
        onSurfaceVariantHex = 0xFF4C4137,
        outlineHex = 0xFF7F6A54,
        swatchHex = 0xFF8A6A4A // #8A6A4A
    ),
    ZEDER(
        id = "Zeder",
        germanName = "Zeder (Dunkles Zedernholz)",
        shortName = "Zeder",
        primaryHex = 0xFF463728,
        secondaryHex = 0xFF5A4938,
        backgroundHex = 0xFFF7F5F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D1712,
        primaryContainerHex = 0xFFD9CCBF,
        onPrimaryContainerHex = 0xFF1D130B,
        accentHex = 0xFF5A4A3A,
        borderHex = 0xFFC9B9A9,
        surfaceVariantHex = 0xFFEFECE8,
        onSurfaceVariantHex = 0xFF443D36,
        outlineHex = 0xFF6C6156,
        swatchHex = 0xFF5A4A3A // #5A4A3A
    ),
    KAKAO(
        id = "Kakao",
        germanName = "Kakao (Sattes Kakaobraun)",
        shortName = "Kakao",
        primaryHex = 0xFF634629,
        secondaryHex = 0xFF7A5937,
        backgroundHex = 0xFFF8F5F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF21170F,
        primaryContainerHex = 0xFFDFCDBB,
        onPrimaryContainerHex = 0xFF241407,
        accentHex = 0xFF7A5A3A,
        borderHex = 0xFFD1BBA5,
        surfaceVariantHex = 0xFFF1EBE5,
        onSurfaceVariantHex = 0xFF483D33,
        outlineHex = 0xFF755E49,
        swatchHex = 0xFF7A5A3A // #7A5A3A
    ),
    MOKKA(
        id = "Mokka",
        germanName = "Mokka (Dunkle Kaffeebohne)",
        shortName = "Mokka",
        primaryHex = 0xFF54371C,
        secondaryHex = 0xFF694829,
        backgroundHex = 0xFFF7F4F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1E140B,
        primaryContainerHex = 0xFFD7C2B0,
        onPrimaryContainerHex = 0xFF231002,
        accentHex = 0xFF6A4A2A,
        borderHex = 0xFFC7AE99,
        surfaceVariantHex = 0xFFEEE7E2,
        onSurfaceVariantHex = 0xFF46392F,
        outlineHex = 0xFF72553B,
        swatchHex = 0xFF6A4A2A // #6A4A2A
    ),
    EICHE(
        id = "Eiche",
        germanName = "Eiche (Echtes Eichenholz)",
        shortName = "Eiche",
        primaryHex = 0xFF6E522B,
        secondaryHex = 0xFF856539,
        backgroundHex = 0xFFF8F5F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF221A10,
        primaryContainerHex = 0xFFE5D3BD,
        onPrimaryContainerHex = 0xFF271705,
        accentHex = 0xFF8A6A3A,
        borderHex = 0xFFD4C0A4,
        surfaceVariantHex = 0xFFF3ECE4,
        onSurfaceVariantHex = 0xFF4B4033,
        outlineHex = 0xFF7B6649,
        swatchHex = 0xFF8A6A3A // #8A6A3A
    ),
    WALNUSS(
        id = "Walnuss",
        germanName = "Walnuss (Dunkle Walnussschale)",
        shortName = "Walnuss",
        primaryHex = 0xFF46291B,
        secondaryHex = 0xFF5A3828,
        backgroundHex = 0xFFF7F4F2,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D100A,
        primaryContainerHex = 0xFFD2B9AC,
        onPrimaryContainerHex = 0xFF210902,
        accentHex = 0xFF5A3A2A,
        borderHex = 0xFFC0A294,
        surfaceVariantHex = 0xFFEEE6E3,
        onSurfaceVariantHex = 0xFF453630,
        outlineHex = 0xFF6C4C3E,
        swatchHex = 0xFF5A3A2A // #5A3A2A
    ),
    KASTANIE(
        id = "Kastanie",
        germanName = "Kastanie (Glänzendes Kastanienbraun)",
        shortName = "Kastanie",
        primaryHex = 0xFF572A11,
        secondaryHex = 0xFF6D381B,
        backgroundHex = 0xFFF8F3F1,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF200F06,
        primaryContainerHex = 0xFFDEBDB0,
        onPrimaryContainerHex = 0xFF270800,
        accentHex = 0xFF6A3A1A,
        borderHex = 0xFFCCA898,
        surfaceVariantHex = 0xFFEFE5E0,
        onSurfaceVariantHex = 0xFF47342C,
        outlineHex = 0xFF764A35,
        swatchHex = 0xFF6A3A1A // #6A3A1A
    ),

    // =========================================================================
    // 5. Gedämpfte Töne (Lehm & Stein)
    // =========================================================================
    FARN(
        id = "Farn",
        germanName = "Farn (Waldiger Farnton)",
        shortName = "Farn",
        primaryHex = 0xFF445E36,
        secondaryHex = 0xFF567247,
        backgroundHex = 0xFFF6F8F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF182014,
        primaryContainerHex = 0xFFCEE0C2,
        onPrimaryContainerHex = 0xFF101E0A,
        accentHex = 0xFF5A7A4A,
        borderHex = 0xFFC0D4B2,
        surfaceVariantHex = 0xFFEEF3E9,
        onSurfaceVariantHex = 0xFF404A3B,
        outlineHex = 0xFF637458,
        swatchHex = 0xFF5A7A4A // #5A7A4A
    ),
    BASILIKUM(
        id = "Basilikum",
        germanName = "Basilikum (Dunkles Kraut)",
        shortName = "Basilikum",
        primaryHex = 0xFF365129,
        secondaryHex = 0xFF476437,
        backgroundHex = 0xFFF5F7F3,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF141D10,
        primaryContainerHex = 0xFFC3DAB3,
        onPrimaryContainerHex = 0xFF0D1C06,
        accentHex = 0xFF4A6A3A,
        borderHex = 0xFFB3CFA1,
        surfaceVariantHex = 0xFFECF2E7,
        onSurfaceVariantHex = 0xFF3B4634,
        outlineHex = 0xFF586E4B,
        swatchHex = 0xFF4A6A3A // #4A6A3A
    ),
    ROSMARIN(
        id = "Rosmarin",
        germanName = "Rosmarin (Blaugrünes Nadelkraut)",
        shortName = "Rosmarin",
        primaryHex = 0xFF355244,
        secondaryHex = 0xFF456455,
        backgroundHex = 0xFFF4F8F6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF121E19,
        primaryContainerHex = 0xFFC3DDD1,
        onPrimaryContainerHex = 0xFF0A1B14,
        accentHex = 0xFF4A6A5A,
        borderHex = 0xFFB3D1C3,
        surfaceVariantHex = 0xFFEBF3EF,
        onSurfaceVariantHex = 0xFF3B4741,
        outlineHex = 0xFF587265,
        swatchHex = 0xFF4A6A5A // #4A6A5A
    ),
    THYMIAN(
        id = "Thymian",
        germanName = "Thymian (Graugrünes Kraut)",
        shortName = "Thymian",
        primaryHex = 0xFF435C43,
        secondaryHex = 0xFF556F55,
        backgroundHex = 0xFFF5F8F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF162016,
        primaryContainerHex = 0xFFD0E3D0,
        onPrimaryContainerHex = 0xFF0E1E0E,
        accentHex = 0xFF5A7A5A,
        borderHex = 0xFFBFD6BF,
        surfaceVariantHex = 0xFFECF3EC,
        onSurfaceVariantHex = 0xFF3F4B3F,
        outlineHex = 0xFF657865,
        swatchHex = 0xFF5A7A5A // #5A7A5A
    ),
    MINZE(
        id = "Minze",
        germanName = "Minze (Frisches Wiesenhellgrün)",
        shortName = "Minze",
        primaryHex = 0xFF547C54,
        secondaryHex = 0xFF689468,
        backgroundHex = 0xFFF6FAF6,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF162216,
        primaryContainerHex = 0xFFD2ECD2,
        onPrimaryContainerHex = 0xFF0F260F,
        accentHex = 0xFF7AAA7A,
        borderHex = 0xFFC2E4C2,
        surfaceVariantHex = 0xFFEAF5EA,
        onSurfaceVariantHex = 0xFF3D4F3D,
        outlineHex = 0xFF6B8C6B,
        swatchHex = 0xFF7AAA7A // #7AAA7A
    ),
    SALBEI(
        id = "Salbei",
        germanName = "Salbei (Sanftes Salbeigrün)",
        shortName = "Salbei",
        primaryHex = 0xFF596B4B,
        secondaryHex = 0xFF6F7E62,
        backgroundHex = 0xFFF7F9F5,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1D2319,
        primaryContainerHex = 0xFFDDE7D3,
        onPrimaryContainerHex = 0xFF182211,
        accentHex = 0xFF8A9A7A,
        borderHex = 0xFFD2DCB9,
        surfaceVariantHex = 0xFFF1F5ED,
        onSurfaceVariantHex = 0xFF464E41,
        outlineHex = 0xFF748169,
        swatchHex = 0xFF8A9A7A // #8A9A7A
    ),
    MOOS(
        id = "Moos",
        germanName = "Moos (Tiefes Moosgrün)",
        shortName = "Moos",
        primaryHex = 0xFF4E5E2F,
        secondaryHex = 0xFF627342,
        backgroundHex = 0xFFF7F8F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF1B2111,
        primaryContainerHex = 0xFFD6E3BE,
        onPrimaryContainerHex = 0xFF131D08,
        accentHex = 0xFF6A7A4A,
        borderHex = 0xFFCCD9B2,
        surfaceVariantHex = 0xFFF0F4E8,
        onSurfaceVariantHex = 0xFF434C37,
        outlineHex = 0xFF6C775D,
        swatchHex = 0xFF6A7A4A // #6A7A4A
    ),
    GRUENSPAN(
        id = "Grünspan",
        germanName = "Grünspan (Kräftiges Waldkraut)",
        shortName = "Grünspan",
        primaryHex = 0xFF2A5E2A,
        secondaryHex = 0xFF3C723C,
        backgroundHex = 0xFFF4F9F4,
        surfaceHex = 0xFFFFFFFF,
        onSurfaceHex = 0xFF102110,
        primaryContainerHex = 0xFFBDDEBD,
        onPrimaryContainerHex = 0xFF081F08,
        accentHex = 0xFF3A7A3A,
        borderHex = 0xFFAED3AE,
        surfaceVariantHex = 0xFFE9F3E9,
        onSurfaceVariantHex = 0xFF384938,
        outlineHex = 0xFF517E51,
        swatchHex = 0xFF3A7A3A // #3A7A3A
    );

    val primary: Color get() = Color(primaryHex)
    val secondary: Color get() = Color(secondaryHex)
    val background: Color get() = Color(backgroundHex)
    val surface: Color get() = Color(surfaceHex)
    val onSurface: Color get() = Color(onSurfaceHex)
    val primaryContainer: Color get() = Color(primaryContainerHex)
    val onPrimaryContainer: Color get() = Color(onPrimaryContainerHex)
    val accent: Color get() = Color(accentHex)
    val border: Color get() = Color(borderHex)
    val surfaceVariant: Color get() = Color(surfaceVariantHex)
    val onSurfaceVariant: Color get() = Color(onSurfaceVariantHex)
    val outline: Color get() = Color(outlineHex)
    val swatch: Color get() = Color(swatchHex)

    companion object {
        // 1. Rotbraun & Terracotta (Warm Earth)
        val WARM_EARTH_THEMES = listOf(
            TERRACOTTA, ZIEGEL, ROST, KUPFER,
            MAHAGONI, ZIMT, SIENA, TON
        )

        // 2. Sand & Beige (Neutral Warm)
        val NEUTRAL_WARM_THEMES = listOf(
            SAND, DUENE, CREME, NATUR,
            KIES, LEHM, OCKER, GELBBRAUN
        )

        // 3. Grau & Schiefer (Cool Earth)
        val COOL_EARTH_THEMES = listOf(
            SCHIEFER, KIESEL, GRANIT, STEIN,
            ASCHE, MAUVE, TAUPE, GRAPHIT
        )

        // 4. Braun & Umbra (Deep Earth)
        val DEEP_EARTH_THEMES = listOf(
            UMBRA, TERRA, ZEDER, KAKAO,
            MOKKA, EICHE, WALNUSS, KASTANIE
        )

        // 5. Gedämpfte Töne (Lehm & Stein)
        val MUTED_HERBAL_THEMES = listOf(
            FARN, BASILIKUM, ROSMARIN, THYMIAN,
            MINZE, SALBEI, MOOS, GRUENSPAN
        )

        // All 40 earth tone themes ordered by category
        val ALL_THEMES = WARM_EARTH_THEMES + NEUTRAL_WARM_THEMES + COOL_EARTH_THEMES + DEEP_EARTH_THEMES + MUTED_HERBAL_THEMES
        val ORDERED_THEMES = ALL_THEMES

        fun fromId(id: String?): AppThemePackage {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SCHIEFER
        }
    }
}

/**
 * CompositionLocal providing the current active theme package so any composable
 * can access theme colors dynamically without requiring activity restart.
 * Default theme: SCHIEFER
 */
val LocalAppThemePackage = staticCompositionLocalOf { AppThemePackage.SCHIEFER }

/**
 * Direct accessor for current active theme package colors.
 */
object AppThemeColors {
    val current: AppThemePackage
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current

    val primary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.primary

    val primaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.primaryContainer

    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.background

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.surface

    val onSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.onSurface

    val onSurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.onSurfaceVariant

    val border: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.border

    val outline: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.outline

    val accent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemePackage.current.accent
}

/**
 * SharedPreferences persistence manager for selected theme package.
 */
class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_THEME_MODE = "theme_mode"
    }

    fun getSelectedTheme(): AppThemePackage {
        val themeName = prefs.getString(KEY_SELECTED_THEME, AppThemePackage.SCHIEFER.id)
        return AppThemePackage.fromId(themeName)
    }

    fun saveSelectedTheme(theme: AppThemePackage) {
        prefs.edit().putString(KEY_SELECTED_THEME, theme.id).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "auto") ?: "auto"
    }

    fun saveThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }
}
