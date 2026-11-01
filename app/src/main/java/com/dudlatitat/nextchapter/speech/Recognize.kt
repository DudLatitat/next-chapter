package com.dudlatitat.nextchapter.speech

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.concurrent.thread
import kotlin.coroutines.resume

/** What the recognizer heard (best guess first), or why it couldn't. */
data class Heard(val texts: List<String>, val error: String? = null, val code: Int = 0) {
    val ok get() = error == null
}

/** The phone's own Chinese speech recognizer: what a native ear would write down. */
object Recognize {
    fun available(context: Context) = SpeechRecognizer.isRecognitionAvailable(context)

    /** The recognizer app the phone uses ("com.google.android.tts", Samsung's…), if Android says. */
    fun service(context: Context): String? = try {
        Settings.Secure.getString(context.contentResolver, "voice_recognition_service")?.let { ComponentName.unflattenFromString(it)?.packageName ?: it }
    } catch (_: Exception) { null }

    fun describe(pkg: String?): String = when {
        pkg == null -> "Unknown"
        pkg.contains("google") -> "Google ($pkg)"
        pkg.contains("samsung") || pkg.contains("bixby") -> "Samsung ($pkg). Google's usually does much better with Chinese"
        else -> pkg
    }

    private fun intent(offline: Boolean) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, offline)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        // Give learners time: don't cut off after a short pause.
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
    }

    /** Recognizes audio you already recorded (Android 13+), so one take gives pitch and words. */
    suspend fun fromClip(context: Context, clip: Clip): Heard {
        if (Build.VERSION.SDK_INT < 33) return Heard(emptyList(), "Needs Android 13 or newer; use Live mode")
        if (!available(context)) return Heard(emptyList(), "No speech recognizer on this phone")
        val clean = clip.forRecognizer()
        var r = runClip(context, clean, offline = true)
        // Offline Chinese not downloaded (or not supported this way): try again online.
        if (!r.ok && r.code != SpeechRecognizer.ERROR_NO_MATCH) r = runClip(context, clean, offline = false).let { if (it.ok) it else r }
        return r
    }

    /**
     * Asks Google to download Chinese for on-device recognition (Android 13+), which is what
     * one-take mode needs. Android shows its own download prompt or notification.
     */
    fun downloadChinese(context: Context, status: (String) -> Unit = {}): Boolean {
        if (Build.VERSION.SDK_INT < 33) return false
        if (Build.VERSION.SDK_INT >= 34) return try {
            val sr = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else SpeechRecognizer.createSpeechRecognizer(context)
            status("Asking Google to download Chinese…")
            sr.triggerModelDownload(intent(offline = true), context.mainExecutor, object : android.speech.ModelDownloadListener {
                override fun onProgress(completedPercent: Int) = status("Downloading Chinese… $completedPercent%")
                override fun onSuccess() { status("Chinese downloaded ✓ Test one take again."); sr.destroy() }
                override fun onScheduled() { status("Download scheduled; Google will fetch it on Wi-Fi soon. Test again in a few minutes."); sr.destroy() }
                override fun onError(error: Int) {
                    status(
                        "Google wouldn't download it from here (error $error). Use \"Open voice settings\" and add Chinese under offline / on-device speech recognition.",
                    )
                    sr.destroy()
                }
            })
            true
        } catch (e: Exception) { status("Couldn't start the download (${e.message}). Use \"Open voice settings\"."); false }
        return try {
            val sr = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else SpeechRecognizer.createSpeechRecognizer(context)
            sr.triggerModelDownload(intent(offline = true))
            // Give the request a moment before releasing the recognizer.
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ sr.destroy() }, 3000)
            true
        } catch (_: Exception) { false }
    }

    /** Whether Chinese is installed for on-device recognition, if Android can say (13+). */
    suspend fun chineseInstalled(context: Context): Boolean? {
        if (Build.VERSION.SDK_INT < 33) return null
        return withContext(Dispatchers.Main) {
            withTimeoutOrNull(5000) {
                suspendCancellableCoroutine { cont ->
                    try {
                        val sr = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else SpeechRecognizer.createSpeechRecognizer(context)
                        sr.checkRecognitionSupport(intent(offline = true), context.mainExecutor, object : android.speech.RecognitionSupportCallback {
                            override fun onSupportResult(support: android.speech.RecognitionSupport) {
                                val ok = support.installedOnDeviceLanguages.any { it.startsWith("zh") || it.startsWith("cmn") }
                                sr.destroy(); if (cont.isActive) cont.resume(ok)
                            }
                            override fun onError(error: Int) { sr.destroy(); if (cont.isActive) cont.resume(null) }
                        })
                    } catch (_: Exception) { if (cont.isActive) cont.resume(null) }
                }
            }
        }
    }

    private suspend fun runClip(context: Context, clip: Clip, offline: Boolean): Heard {
        val pipe = ParcelFileDescriptor.createPipe()
        val i = intent(offline).apply {
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
                    out.write(ByteArray(clip.rate * 2)) // a second of silence to end the utterance
                }
            } catch (_: Exception) {}
        }
        return run(context, i, onDevice = offline).also { try { pipe[0].close() } catch (_: Exception) {} }
    }

    /** Listens live through the mic. */
    suspend fun live(context: Context): Heard {
        if (!available(context)) return Heard(emptyList(), "No speech recognizer on this phone")
        // Online first: offline Chinese often isn't available to apps (error 12). A second listen
        // only happens when there's no internet, and you'd be asked to speak again anyway.
        val r = run(context, intent(offline = false))
        val noNet = r.code == SpeechRecognizer.ERROR_NETWORK || r.code == SpeechRecognizer.ERROR_NETWORK_TIMEOUT || r.code == SpeechRecognizer.ERROR_SERVER
        return if (noNet) run(context, intent(offline = true)).let { if (it.ok) it else r } else r
    }

    private fun explain(code: Int) = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Heard you, but couldn't match any Chinese words"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Didn't hear any speech"
        SpeechRecognizer.ERROR_AUDIO -> "Couldn't use the audio (the recognizer may not accept recordings; try Live mode)"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Needs internet, or download offline Chinese in Google's speech settings"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is off"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "The recognizer is busy; try again"
        SpeechRecognizer.ERROR_CLIENT -> "The recognizer stopped early"
        SpeechRecognizer.ERROR_SERVER -> "The recognition server had a problem"
        12 -> "This phone's recognizer doesn't do Chinese from recordings; use Live (it works the same)"
        13 -> "Chinese isn't downloaded for offline use"
        else -> "Recognizer error $code"
    }

    private suspend fun run(context: Context, i: Intent, onDevice: Boolean = false): Heard = withContext(Dispatchers.Main) {
        withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { cont ->
                // On-device recognition handles recorded audio best, once Chinese is downloaded.
                val sr = if (onDevice && Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                    else SpeechRecognizer.createSpeechRecognizer(context)
                var done = false
                fun finish(r: Heard) {
                    if (done) return
                    done = true
                    sr.destroy()
                    if (cont.isActive) cont.resume(r)
                }
                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty().filter { it.isNotBlank() }
                        finish(if (list.isEmpty()) Heard(emptyList(), explain(SpeechRecognizer.ERROR_NO_MATCH), SpeechRecognizer.ERROR_NO_MATCH) else Heard(list))
                    }
                    override fun onError(error: Int) = finish(Heard(emptyList(), explain(error), error))
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
        } ?: Heard(emptyList(), "The recognizer didn't answer in time")
    }
}
