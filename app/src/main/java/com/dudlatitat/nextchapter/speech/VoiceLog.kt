package com.dudlatitat.nextchapter.speech

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * One Say-it attempt, as numbers only (no audio): what you said, the tones expected, each
 * character's pitch shape as the tone model sees it (20 points, semitones vs your normal pitch),
 * and what the model decided. Exported from Settings to measure tone models on your own voice.
 */
@Serializable
data class LoggedAttempt(
    val time: Long,
    val hanzi: String,
    val pinyin: String,
    /** The tone each character should have (5 = neutral). */
    val expected: List<Int>,
    /** Tones accepted per character (sandhi, softened words); empty = not judged. */
    val accepted: List<List<Int>>,
    /** Per character: the pitch shape, or null if that character wasn't heard or had no pitch. */
    val contours: List<List<Float>?>,
    /** The model's guess per character (tone, confidence), or null. */
    val guessTone: List<Int?>,
    val guessConfidence: List<Float?>,
    /** Your normal pitch (Hz) and whether it came from voice setup. */
    val baseHz: Float,
    val calibrated: Boolean,
    /** Which tone model judged it (how many phrases it was trained on, and on which voices). */
    val model: String = "",
)

/** Keeps the last [MAX] attempts in their own file (not in AppState, so backups stay small). */
object VoiceLog {
    const val MAX = 100
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val serializer = ListSerializer(LoggedAttempt.serializer())

    /** Adds one, keeping only the newest [max]. Pure, tested. */
    fun withAttempt(log: List<LoggedAttempt>, a: LoggedAttempt, max: Int = MAX): List<LoggedAttempt> = (log + a).takeLast(max)

    fun encode(log: List<LoggedAttempt>): String = json.encodeToString(serializer, log)
    fun decode(text: String): List<LoggedAttempt> = try { json.decodeFromString(serializer, text) } catch (_: Exception) { emptyList() }

    private fun file(context: Context) = File(context.filesDir, "voice_log.json")
    private fun prefs(context: Context) = context.getSharedPreferences("voice_profile", Context.MODE_PRIVATE)

    fun enabled(context: Context): Boolean = prefs(context).getBoolean("voice_log", true)
    fun setEnabled(context: Context, on: Boolean) = prefs(context).edit().putBoolean("voice_log", on).apply()

    fun read(context: Context): List<LoggedAttempt> = file(context).takeIf { it.exists() }?.let { decode(it.readText()) } ?: emptyList()

    @Synchronized
    fun add(context: Context, a: LoggedAttempt) {
        if (!enabled(context)) return
        try { file(context).writeText(encode(withAttempt(read(context), a))) } catch (_: Exception) {}
    }

    fun clear(context: Context) { file(context).delete() }

    /** The whole log as text, for Export. */
    fun export(context: Context): String = encode(read(context))
}
