package com.dudlatitat.nextchapter.speech

import android.content.Context
import com.dudlatitat.nextchapter.Speaker
import com.dudlatitat.nextchapter.data.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** How one character came out. */
enum class Mark { RIGHT, TONE, MISSED }

data class CharResult(
    val hanzi: Char,
    val target: String,
    val mark: Mark,
    /** What the recognizer heard there, as pinyin (when not right). */
    val heard: String? = null,
    /** False when the tone couldn't be judged with confidence (it's then given the benefit of the doubt). */
    val sure: Boolean = true,
)

data class Attempt(
    /** Per-character check by the recognizer; null if it wasn't available. */
    val chars: List<CharResult>?,
    val heardText: String?,
    /** 0–100 pitch-melody match to the native recording; null without a recording or voice. */
    val melody: Int?,
    /** Your pitch and the native pitch, both in semitones vs each speaker's normal pitch. */
    val userCurve: FloatArray,
    val refCurve: FloatArray,
    /** For a single character: the tone you produced. */
    val tone: Pitch.ToneGuess?,
    val tooQuiet: Boolean,
    /** Why the word check didn't work, if it didn't. */
    val recognizerError: String? = null,
    /** Where each character sits along [userCurve] (0–1 start and end), for labelling the graph. */
    val spans: List<Pair<Float, Float>> = emptyList(),
)

/** Records you saying a phrase and checks the words, each character's tone and the pitch melody. */
class Coach(private val context: Context) {
    private val prefs = context.getSharedPreferences("voice_profile", Context.MODE_PRIVATE)
    private val refCache = HashMap<String, FloatArray>()

    // Settings from the old Google recognizer modes, no longer used.
    init { prefs.edit().remove("mode").remove("mode2").remove("parallel2").apply() }

    var profile: Pitch.VoiceProfile
        get() = Pitch.VoiceProfile(prefs.getFloat("base", 0f), prefs.getFloat("scale", 1f), prefs.getBoolean("calibrated", false))
        set(v) { prefs.edit().putFloat("base", v.baseHz).putFloat("scale", v.scale).putBoolean("calibrated", v.calibrated).apply() }

    val calibrated get() = profile.calibrated

    /** Profile to use now: calibrated, else a running estimate from your recordings. */
    private fun profileFor(hz: FloatArray): Pitch.VoiceProfile {
        val p = profile
        if (p.calibrated) return p
        val med = Pitch.median(hz.filter { it > 0 }.toFloatArray())
        if (med <= 0f) return p
        // Phrases sit around your normal pitch; nudge the estimate toward each new one.
        val base = Pitch.blend(p.baseHz, med, 0.3f)
        profile = Pitch.VoiceProfile(base, 1f, false)
        return Pitch.VoiceProfile(base, 1f, false)
    }

    private suspend fun reference(text: String): FloatArray {
        refCache[text]?.let { return it }
        val clip = Recorder.decodeAsset(context, "voice/${Speaker.md5(text.trim())}.mp3") ?: return FloatArray(0)
        val curve = withContext(Dispatchers.Default) {
            val v = Pitch.voiced(Pitch.track(clip.samples, clip.rate))
            if (v.isEmpty()) v else Pitch.semitones(v, Pitch.REFERENCE_BASE)
        }
        refCache[text] = curve
        return curve
    }

    /** Checks a recording of you saying [hanzi] (with its [pinyin]): words, tones and melody from one take. */
    suspend fun check(clip: Clip, hanzi: String, pinyin: String): Attempt {
        val hz = withContext(Dispatchers.Default) { Pitch.track(clip.samples, clip.rate) }
        val voicedCount = hz.count { it > 0 }
        if (voicedCount < 6) return Attempt(null, null, null, FloatArray(0), FloatArray(0), null, tooQuiet = true)

        val chars = hanzi.filter { it.code in 0x4E00..0x9FFF }
        val p = profileFor(hz)
        val userCurve = p.normalize(Pitch.voiced(hz))
        val ref = reference(hanzi)
        val melody = if (ref.isNotEmpty()) Pitch.phraseScore(userCurve, ref) else null
        val tone = if (chars.length == 1) Pitch.classify(hz, p) else null

        if (chars.isEmpty()) return Attempt(null, null, melody, userCurve, ref, tone, tooQuiet = false)
        val r = offline(clip, hz, p, chars, pinyin, tone)
            // The recognizer couldn't run: the pitch graph and melody still work.
            ?: return Attempt(null, null, melody, userCurve, ref, tone, tooQuiet = false, recognizerError = lastError ?: "the offline recognizer didn't start")
        val (results, spans, heardText) = r
        return Attempt(results, heardText, melody, userCurve, ref, tone, tooQuiet = false, spans = Judge.spansOnCurve(hz, spans))
    }

    private var lastError: String? = null

    /**
     * The offline check. Vosk listens only for the target characters, which tells whether each
     * one's sound (consonant + vowel) came through and when it was said. Vosk barely hears
     * tones, so tones come from your pitch instead: the single-syllable classifier for one
     * character, the phrase tone model for several. Returns null if Vosk failed.
     */
    private suspend fun offline(
        clip: Clip, hz: FloatArray, p: Pitch.VoiceProfile, chars: String, pinyin: String, single: Pitch.ToneGuess?,
    ): Triple<List<CharResult>, List<Pair<Float, Float>>, String>? {
        val sylls = Pinyin.syllablesIn(pinyin).map { (r, _) -> pinyin.substring(r.first, r.last + 1) }
        fun target(i: Int): String = if (sylls.size == chars.length) sylls[i] else Pinyin.fromHanzi(chars[i].toString()) ?: ""
        val targets = chars.indices.map(::target)
        val accepted = Judge.acceptedTones(chars, targets.map { Pinyin.tone(it) })

        val heard = Offline.recognize(context, clip, Judge.grammarFor(chars))
        lastError = heard.error
        if (!heard.ok) return null
        val times = Judge.matchTimes(chars, Judge.charsOf(heard.words))

        val guesses: List<Pair<Int, Float>?> = if (chars.length == 1) listOf(single?.let { it.tone to 1f }) else withContext(Dispatchers.Default) {
            Judge.phraseTones(times.map { t -> t?.let { (s, e) -> Pitch.syllable(Judge.slice(hz, s, e))?.let(p::normalize) } })
        }
        val results = chars.indices.map { i ->
            val t = targets[i]
            val g = guesses[i]
            when {
                times[i] == null -> CharResult(chars[i], t, Mark.MISSED)
                accepted[i].isEmpty() -> CharResult(chars[i], t, Mark.RIGHT) // neutral tone: not judged
                g == null || g.second < SURE -> CharResult(chars[i], t, Mark.RIGHT, sure = false)
                g.first in accepted[i] -> CharResult(chars[i], t, Mark.RIGHT)
                else -> CharResult(chars[i], t, Mark.TONE, Judge.retone(t, g.first))
            }
        }
        return Triple(results, times.map { it ?: (0f to 0f) }, heard.text)
    }

    /** Learns your voice from four recordings of 妈 麻 马 骂. */
    suspend fun calibrate(clips: List<Clip>): Boolean {
        val curves = withContext(Dispatchers.Default) { clips.map { Pitch.syllable(Pitch.track(it.samples, it.rate)) } }
        if (curves.any { it == null }) return false
        val p = Pitch.calibrate(curves.filterNotNull()) ?: return false
        profile = p
        return true
    }

    companion object {
        /** Below this, the phrase tone model isn't sure enough to call a tone wrong (~91% right above it on native phrases). */
        const val SURE = 0.7f
    }
}
