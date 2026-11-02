package com.dudlatitat.nextchapter.speech

import android.content.Context
import com.dudlatitat.nextchapter.Speaker
import com.dudlatitat.nextchapter.data.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/** How one character came out. */
enum class Mark { RIGHT, TONE, WRONG, MISSED }

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

        if (mode == "offline" && Offline.bundled(context) && chars.isNotEmpty()) {
            offline(clip, hz, p, chars, pinyin, tone)?.let { (results, spans, heardText) ->
                return Attempt(results, heardText, melody, userCurve, ref, tone, tooQuiet = false, spans = Judge.spansOnCurve(hz, spans))
            }
        }
        // No offline model (or it failed): the phone's recognizer, from the same recording.
        val heard = Recognize.fromClip(context, clip)
        val results = if (heard.ok) judge(hanzi, pinyin, heard.texts) else null
        return Attempt(results?.second, results?.first, melody, userCurve, ref, tone, tooQuiet = false, recognizerError = heard.error)
    }

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

    private data class PitchPart(val melody: Int?, val user: FloatArray, val ref: FloatArray, val tone: Pitch.ToneGuess?)

    /** The pitch side of a check (graph, melody score, single-syllable tone), from a recording. */
    private suspend fun pitchOf(clip: Clip, hanzi: String): PitchPart? {
        val hz = withContext(Dispatchers.Default) { Pitch.track(clip.samples, clip.rate) }
        if (hz.count { it > 0 } < 6) return null
        val p = profileFor(hz)
        val userCurve = p.normalize(Pitch.voiced(hz))
        val ref = reference(hanzi)
        val chars = hanzi.filter { it.code in 0x4E00..0x9FFF }
        return PitchPart(if (ref.isNotEmpty()) Pitch.phraseScore(userCurve, ref) else null, userCurve, ref, if (chars.length == 1) Pitch.classify(hz, p) else null)
    }

    /** Whether this phone lets the app record while the live recognizer listens (learned on first try). */
    // Off: on Android the app on screen wins the mic, so recording alongside left the recognizer
    // hearing silence. Kept as a switch in case a phone handles it differently.
    private var parallel: Boolean
        get() = prefs.getBoolean("parallel2", false)
        set(v) { prefs.edit().putBoolean("parallel2", v).apply() }

    /**
     * Word check by listening live. While the recognizer listens, the app also tries to record
     * you for the pitch graph and tones; if the phone won't share the mic, it stops trying.
     */
    suspend fun checkLive(hanzi: String, pinyin: String, control: LiveControl? = null): Attempt = coroutineScope {
        var heard: Heard
        var clip: Clip? = null
        if (parallel) {
            var stop = false
            val rec = async { delay(250); Recorder.record(maxSeconds = 15f, stop = { stop }) }
            heard = Recognize.live(context, control)
            stop = true
            clip = try { rec.await() } catch (_: Exception) { null }
            if (heard.code == android.speech.SpeechRecognizer.ERROR_AUDIO || heard.code == android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                // The two fought over the mic: give it to the recognizer from now on.
                parallel = false; clip = null
                heard = Recognize.live(context, control)
            }
        } else heard = Recognize.live(context, control)
        val pitch = clip?.let { pitchOf(it, hanzi) }
        val results = if (heard.ok) judge(hanzi, pinyin, heard.texts) else null
        Attempt(
            results?.second, results?.first, pitch?.melody, pitch?.user ?: FloatArray(0), pitch?.ref ?: FloatArray(0), pitch?.tone,
            tooQuiet = false, recognizerError = heard.error,
        )
    }

    /**
     * "offline" (one take on the phone: words, tones and pitch graph) or "live" (Google listens
     * through the mic: words only). A new setting key, so everyone starts on offline once.
     */
    var mode: String
        get() = prefs.getString("mode2", "offline") ?: "offline"
        set(v) { prefs.edit().putString("mode2", v).apply() }

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

        /**
         * Lines up what was heard with the target, character by character. A different
         * character with the same syllable but another tone counts as a tone mistake; same
         * sound and tone (a homophone, like 他/她) counts as right.
         */
        fun judge(hanzi: String, pinyin: String, heardOptions: List<String>): Pair<String, List<CharResult>> {
            val target = hanzi.filter { it.code in 0x4E00..0x9FFF }
            val sylls = Pinyin.syllablesIn(pinyin).map { (r, _) -> pinyin.substring(r.first, r.last + 1) }
            fun targetSyll(i: Int): String = if (sylls.size == target.length) sylls[i] else Pinyin.fromHanzi(target[i].toString()) ?: ""

            var best: Pair<String, List<CharResult>>? = null
            var bestScore = -1
            for (option in heardOptions.ifEmpty { listOf("") }) {
                val heard = option.filter { it.code in 0x4E00..0x9FFF }
                val res = align(target, heard).mapIndexed { i, h ->
                    val t = targetSyll(i)
                    when {
                        h == null -> CharResult(target[i], t, Mark.MISSED)
                        h == target[i] -> CharResult(target[i], t, Mark.RIGHT)
                        else -> {
                            val hp = Pinyin.fromHanzi(h.toString()) ?: ""
                            when {
                                hp.equals(t, ignoreCase = true) -> CharResult(target[i], t, Mark.RIGHT)
                                Pinyin.bare(hp) == Pinyin.bare(t) -> CharResult(target[i], t, Mark.TONE, hp)
                                else -> CharResult(target[i], t, Mark.WRONG, hp.ifBlank { h.toString() })
                            }
                        }
                    }
                }
                val score = res.sumOf { when (it.mark) { Mark.RIGHT -> 3; Mark.TONE -> 1; else -> 0 }.toInt() }
                if (score > bestScore) { bestScore = score; best = option to res }
            }
            return best!!
        }

        /** For each target character, the heard character it lines up with (edit-distance alignment). */
        private fun align(t: String, h: String): List<Char?> {
            val n = t.length; val m = h.length
            val d = Array(n + 1) { IntArray(m + 1) }
            for (i in 0..n) d[i][0] = i
            for (j in 0..m) d[0][j] = j
            for (i in 1..n) for (j in 1..m) {
                d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + if (t[i - 1] == h[j - 1]) 0 else 1)
            }
            val out = arrayOfNulls<Char>(n)
            var i = n; var j = m
            while (i > 0) {
                when {
                    j > 0 && d[i][j] == d[i - 1][j - 1] + (if (t[i - 1] == h[j - 1]) 0 else 1) -> { out[i - 1] = h[j - 1]; i--; j-- }
                    d[i][j] == d[i - 1][j] + 1 -> { out[i - 1] = null; i-- }
                    else -> j--
                }
            }
            return out.toList()
        }
    }
}
