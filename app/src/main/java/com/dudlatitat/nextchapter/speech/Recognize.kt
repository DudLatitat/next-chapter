package com.dudlatitat.nextchapter.speech

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.concurrent.thread
import kotlin.coroutines.resume

/** The phone's own Chinese speech recognizer: what a native ear would write down. */
object Recognize {
    fun available(context: Context) = SpeechRecognizer.isRecognitionAvailable(context)

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    /**
     * Recognizes audio you already recorded (Android 13+), so one take gives both the pitch
     * check and the word check. Null if the recognizer can't take recorded audio.
     */
    suspend fun fromClip(context: Context, clip: Clip): List<String>? {
        if (Build.VERSION.SDK_INT < 33 || !available(context)) return null
        val pipe = ParcelFileDescriptor.createPipe()
        val i = intent().apply {
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, pipe[0])
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, clip.rate)
        }
        val bytes = clip.pcm16()
        thread(name = "feed-recognizer") {
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { out ->
                    out.write(bytes)
                    out.write(ByteArray(clip.rate)) // half a second of silence to end the utterance
                }
            } catch (_: Exception) {}
        }
        return run(context, i).also { try { pipe[0].close() } catch (_: Exception) {} }
    }

    /** Listens live through the mic (the fallback when recorded audio isn't accepted). */
    suspend fun live(context: Context): List<String>? = if (available(context)) run(context, intent()) else null

    private suspend fun run(context: Context, i: Intent): List<String>? = withContext(Dispatchers.Main) {
        withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine { cont ->
                val sr = SpeechRecognizer.createSpeechRecognizer(context)
                var done = false
                fun finish(r: List<String>?) {
                    if (done) return
                    done = true
                    sr.destroy()
                    if (cont.isActive) cont.resume(r)
                }
                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) = finish(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
                    override fun onError(error: Int) = finish(if (error == SpeechRecognizer.ERROR_NO_MATCH) emptyList() else null)
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                cont.invokeOnCancellation { if (!done) { done = true; sr.destroy() } }
                sr.startListening(i)
            }
        }
    }
}
