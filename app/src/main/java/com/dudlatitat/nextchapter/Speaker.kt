package com.dudlatitat.nextchapter

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.util.Locale

/** A Mandarin voice the phone can use. */
data class VoiceOption(val name: String, val label: String, val online: Boolean)

/** A text-to-speech app on the phone (Google, Samsung…). */
data class EngineOption(val pkg: String, val label: String)

/**
 * Reads Chinese aloud. Built-in phrases play natural recordings (made with a neural voice when
 * the app is built, stored in assets/voice/); anything else uses the phone's voice, with the
 * engine and voice picked in Settings.
 */
class Speaker(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("voice", Context.MODE_PRIVATE)
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private val recorded: Set<String> = try { app.assets.list("voice")?.toSet() ?: emptySet() } catch (_: Exception) { emptySet() }

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready
    private val _voices = MutableStateFlow<List<VoiceOption>>(emptyList())
    val voices: StateFlow<List<VoiceOption>> = _voices
    private val _engines = MutableStateFlow<List<EngineOption>>(emptyList())
    val engines: StateFlow<List<EngineOption>> = _engines

    val hasRecordings get() = recorded.isNotEmpty()
    var useRecordings: Boolean
        get() = prefs.getBoolean("recorded", true)
        set(v) { prefs.edit().putBoolean("recorded", v).apply() }
    val engine: String? get() = prefs.getString("engine", null)
    val voice: String? get() = prefs.getString("voice", null)

    /** False when the phone has no Chinese voice installed (recordings still play). */
    val available get() = _ready.value

    init { start() }

    private fun start() {
        _ready.value = false
        tts?.shutdown()
        lateinit var t: TextToSpeech
        val listener = TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.SUCCESS) return@OnInitListener
            val result = t.setLanguage(Locale.SIMPLIFIED_CHINESE)
            _ready.value = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            _engines.value = t.engines.map { EngineOption(it.name, it.label) }
            val zh = try {
                t.voices.orEmpty().filter { it.locale.language == "zh" && (it.locale.country.isEmpty() || it.locale.country == "CN") }
                    .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))
            } catch (_: Exception) { emptyList() }
            _voices.value = zh.mapIndexed { i, v ->
                VoiceOption(v.name, "Voice ${i + 1}" + if (v.quality >= 400) " · high quality" else "", v.isNetworkConnectionRequired)
            }
            voice?.let { name -> zh.firstOrNull { it.name == name }?.let { t.voice = it } }
        }
        t = engine?.let { TextToSpeech(app, listener, it) } ?: TextToSpeech(app, listener)
        tts = t
    }

    fun setEngine(pkg: String) {
        prefs.edit().putString("engine", pkg).remove("voice").apply()
        start()
    }

    /**
     * Uses one of the phone's voices for everything (turns the recordings off). Returns false if
     * the engine wouldn't switch to it (some, like Samsung's, ignore the choice).
     */
    fun setVoice(name: String): Boolean {
        prefs.edit().putString("voice", name).putBoolean("recorded", false).apply()
        val ok = applyVoice()
        sayWithPhone("你好，我们一起学中文吧", slow = false)
        return ok
    }

    /** Back to the recorded natural voice for built-in phrases. */
    fun useNatural() {
        prefs.edit().putBoolean("recorded", true).apply()
        say("我想你")
    }

    /** Sets the picked voice on the engine again (engines can drop it between phrases). */
    private fun applyVoice(): Boolean {
        val t = tts ?: return false
        val name = voice ?: return true
        if (t.voice?.name == name) return true
        val v = try { t.voices?.firstOrNull { it.name == name } } catch (_: Exception) { null } ?: return false
        t.voice = v
        return t.voice?.name == name
    }

    fun say(text: String, slow: Boolean = false) {
        val file = "${md5(text.trim())}.mp3"
        if (useRecordings && file in recorded && play("voice/$file", slow)) return
        sayWithPhone(text, slow)
    }

    private fun sayWithPhone(text: String, slow: Boolean) {
        val t = tts ?: return
        if (!_ready.value) return
        player?.stop()
        applyVoice()
        t.setSpeechRate(if (slow) 0.55f else 0.85f)
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    private fun play(asset: String, slow: Boolean): Boolean = try {
        tts?.stop()
        player?.release()
        val fd = app.assets.openFd(asset)
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            fd.close()
            prepare()
            if (slow) playbackParams = PlaybackParams().setSpeed(0.7f).setPitch(1f)
            start()
        }
        true
    } catch (_: Exception) { false }

    fun stop() {
        tts?.stop()
        try { player?.stop() } catch (_: Exception) {}
    }

    fun close() {
        tts?.shutdown()
        player?.release()
    }

    companion object {
        fun md5(text: String): String =
            MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
