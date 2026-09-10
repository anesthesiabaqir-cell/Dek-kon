<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Deklination – German Declension & Grammar Assistant

An Android application built with Kotlin and Jetpack Compose for looking up German noun declensions, grammar tables (Nominativ, Akkusativ, Dativ, Genitiv), article rules, pronunciation, search history, and exporting to PDF and Excel.

## Features
- **Accurate German Declensions**: Instant linguistic analysis with articles (der, die, das) and plural forms across all 4 German grammatical cases.
- **Interactive Grammar Tables**: Visual breakdown of definitive and indefinite articles, adjectives, and noun endings.
- **Search History & Persistence**: Saved searches stored locally with Room database.
- **Audio Pronunciation**: Integrated German Text-To-Speech (TTS) playback.
- **Export & Share**: Generate and share declension sheets via PDF and Excel (.xlsx).
- **Flexible AI Models**: Configurable Gemini and OpenRouter models with API key management.

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio and select **Open** on the project root.
2. Provide your Gemini API key in the Secrets panel or create a `.env` file with `GEMINI_API_KEY=your_key`.
3. Build and run on an Android emulator or device (API 24+).
