package com.dudlatitat.nextchapter.speech

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import java.io.File

/** One word Vosk heard, with when it was said (seconds from the start of the clip). */
data class VoskWord(val text: String, val start: Float, val end: Float, val conf: Float)

/** What the offline recognizer heard: the text (no spaces) and each word with its timing. */
data class VoskResult(val text: String, val words: List<VoskWord>, val error: String? = null) {
    val ok get() = error == null
}

/** Reads Vosk's JSON answers. Kept apart from the recognizer so it can be tested on a computer. */
object VoskJson {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Vosk writes Chinese with spaces between words ("你 好"); the app wants it joined.
     * With word timing on, "result" lists each word as {word, start, end, conf}.
     */
    fun parse(text: String): VoskResult {
        val o = try { json.parseToJsonElement(text) as? JsonObject } catch (_: Exception) { null }
            ?: return VoskResult("", emptyList(), "Couldn't read the recognizer's answer")
        val words = (o["result"] as? JsonArray).orEmpty().mapNotNull { e ->
            val w = e as? JsonObject ?: return@mapNotNull null
            val word = (w["word"] as? JsonPrimitive)?.content ?: return@mapNotNull null
            VoskWord(
                word,
                (w["start"] as? JsonPrimitive)?.floatOrNull ?: 0f,
                (w["end"] as? JsonPrimitive)?.floatOrNull ?: 0f,
                (w["conf"] as? JsonPrimitive)?.floatOrNull ?: 0f,
            )
        }.filter { it.text != "[unk]" }
        val joined = ((o["text"] as? JsonPrimitive)?.content ?: words.joinToString(" ") { it.text })
            .split(' ').filter { it.isNotBlank() && it != "[unk]" }.joinToString("")
        return VoskResult(joined, words)
    }

    /** A grammar for Vosk: only these words (plus "[unk]" for anything else) can be heard. */
    fun grammar(words: List<String>): String =
        (words.distinct() + "[unk]").joinToString(",", "[", "]") { "\"" + it.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" }
}

/**
 * The on-phone Chinese recognizer (Vosk, vosk-model-small-cn-0.22, Apache 2.0). Works offline,
 * sends nothing anywhere, and takes our own recordings, so one take gives words and pitch.
 */
object Offline {
    private const val ASSET_DIR = "vosk-cn"
    private val lock = Mutex()
    @Volatile private var model: org.vosk.Model? = null

    /**
     * Loads the model, first copying it out of the APK into the app's own storage (Vosk needs
     * real files, and the APK's assets aren't files). The copy happens once per model version:
     * the "uuid" file CI writes into the model folder says which version it is.
     */
    suspend fun load(context: Context, status: (String) -> Unit = {}): org.vosk.Model = lock.withLock {
        model?.let { return it }
        withContext(Dispatchers.IO) {
            val target = File(context.filesDir, ASSET_DIR)
            val want = context.assets.open("$ASSET_DIR/uuid").bufferedReader().use { it.readLine()?.trim() }
            val have = File(target, "uuid").takeIf { it.exists() }?.readText()?.trim()
            if (want == null || want != have) {
                status("Unpacking the Chinese model (one time only)…")
                target.deleteRecursively()
                copyAssets(context, ASSET_DIR, target)
                // Written last, so a copy cut short (app closed mid-way) is redone next time.
                File(target, "uuid").writeText(want ?: "unknown")
            }
            status("Loading the Chinese model…")
            org.vosk.LibVosk.setLogLevel(org.vosk.LogLevel.WARNINGS)
            org.vosk.Model(target.absolutePath).also { model = it }
        }
    }

    private fun copyAssets(context: Context, path: String, to: File) {
        val children = context.assets.list(path).orEmpty()
        if (children.isEmpty()) {
            // A file (asset folders always list their children).
            to.parentFile?.mkdirs()
            context.assets.open(path).use { input -> to.outputStream().use { input.copyTo(it, 64 * 1024) } }
            return
        }
        to.mkdirs()
        for (c in children) if (c != "uuid") copyAssets(context, "$path/$c", File(to, c))
    }

    /**
     * Recognizes a recording. With [grammar], Vosk can only answer with those words (or
     * nothing), which is how a tone check asks "was it 汤, 糖, 躺 or 烫?".
     */
    suspend fun recognize(context: Context, clip: Clip, grammar: List<String>? = null, status: (String) -> Unit = {}): VoskResult = try {
        val m = load(context, status)
        status("Recognizing…")
        withContext(Dispatchers.Default) {
            val rec = if (grammar != null) org.vosk.Recognizer(m, clip.rate.toFloat(), VoskJson.grammar(grammar)) else org.vosk.Recognizer(m, clip.rate.toFloat())
            rec.use { r ->
                r.setWords(true)
                // Vosk wants 16-bit samples; feed a quarter second at a time.
                val step = clip.rate / 4
                val buf = ShortArray(step)
                var i = 0
                while (i < clip.samples.size) {
                    val n = minOf(step, clip.samples.size - i)
                    for (k in 0 until n) buf[k] = (clip.samples[i + k].coerceIn(-1f, 1f) * 32767).toInt().toShort()
                    r.acceptWaveForm(buf, n)
                    i += n
                }
                VoskJson.parse(r.finalResult)
            }
        }
    } catch (e: Throwable) {
        if (e is kotlinx.coroutines.CancellationException) throw e // leaving the screen stops the check
        // Includes a missing native library (UnsatisfiedLinkError): show it instead of crashing.
        VoskResult("", emptyList(), "Offline recognizer failed: ${e.javaClass.simpleName}: ${e.message}")
    }
}
