package com.dudlatitat.nextchapter

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Reads Chinese aloud with the phone's own voice (works offline once the voice is installed). */
class Speaker(context: Context) {
    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            tts.setSpeechRate(0.85f)
        }
    }

    /** False when the phone has no Chinese voice installed. */
    val available get() = ready

    fun say(text: String, slow: Boolean = false) {
        if (!ready) return
        tts.setSpeechRate(if (slow) 0.55f else 0.85f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    fun close() = tts.shutdown()
}
