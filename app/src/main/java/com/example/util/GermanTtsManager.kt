package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.round

class GermanTtsManager(context: Context) : TextToSpeech.OnInitListener {
    private val prefs = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        const val PREF_KEY_TTS_SPEED = "tts_speech_rate"
        const val DEFAULT_TTS_SPEED = 1.0f
    }

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpokenText = MutableStateFlow<String?>(null)
    val currentlySpokenText: StateFlow<String?> = _currentlySpokenText.asStateFlow()

    private val _speechRate = MutableStateFlow(
        prefs.getFloat(PREF_KEY_TTS_SPEED, DEFAULT_TTS_SPEED)
    )
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.w("GermanTtsManager", "TextToSpeech init failed", e)
        }
    }

    /**
     * Updates the playback speed rate and persists it to SharedPreferences.
     * Note: Strictly does NOT trigger any audio playback or alter currently playing speech.
     * The new speed rate is only applied upon the next explicit playback trigger.
     */
    fun setSpeedRate(rate: Float) {
        val snapped = (round(rate * 10f) / 10f).coerceIn(0.5f, 2.0f)
        _speechRate.value = snapped
        try {
            prefs.edit().putFloat(PREF_KEY_TTS_SPEED, snapped).apply()
        } catch (e: Exception) {
            Log.w("GermanTtsManager", "Failed to persist TTS speed rate", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.GERMAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("GermanTtsManager", "German language data missing or not supported on this device")
            } else {
                isReady = true
                tts?.setSpeechRate(_speechRate.value)
                tts?.setPitch(1.0f)
                setupProgressListener()
            }
        } else {
            Log.e("GermanTtsManager", "TTS initialization failed with status: $status")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpokenText.value = null
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpokenText.value = null
            }
        })
    }

    /**
     * Speaks the given German phrase or word.
     */
    fun speak(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return

        if (!isReady) {
            Log.w("GermanTtsManager", "TTS is not ready yet")
            return
        }

        try {
            tts?.setSpeechRate(_speechRate.value)
            _currentlySpokenText.value = clean
            val utteranceId = "GermanPronounce_${System.currentTimeMillis()}"
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("GermanTtsManager", "Failed to speak text: $clean", e)
            _isSpeaking.value = false
            _currentlySpokenText.value = null
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _currentlySpokenText.value = null
        } catch (e: Exception) {
            Log.e("GermanTtsManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isReady = false
            _isSpeaking.value = false
            _currentlySpokenText.value = null
        } catch (e: Exception) {
            Log.e("GermanTtsManager", "Error shutting down TTS", e)
        }
    }
}
