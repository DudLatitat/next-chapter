package com.dudlatitat.nextchapter.speech

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Pitch tracking and tone judging. Prototyped and tuned in Python against the app's own
 * recordings (tools/tone_proto/): 40/40 single-character tones in leave-one-out, 39/40 with the
 * voice shifted to a male range.
 */
object Pitch {
    /** One pitch value (Hz) every 10 ms; 0 where there's no voiced sound. YIN algorithm. */
    fun track(x: FloatArray, sr: Int, fmin: Int = 65, fmax: Int = 500): FloatArray {
        val n = (0.04 * sr).toInt()
        val hop = (0.01 * sr).toInt()
        val tmin = sr / fmax
        val tmax = sr / fmin
        val frames = max(0, (x.size - n - tmax) / hop)
        val f = FloatArray(frames)
        val ap = FloatArray(frames)
        val rms = FloatArray(frames)
        val d = DoubleArray(tmax + 2)
        val cm = DoubleArray(tmax + 2)
        for (k in 0 until frames) {
            val s = k * hop
            var e = 0.0
            for (i in 0 until n) e += x[s + i] * x[s + i]
            rms[k] = sqrt(e / n).toFloat()
            for (t in 0..tmax) {
                var sum = 0.0
                for (i in 0 until n) { val diff = x[s + i] - x[s + i + t]; sum += diff * diff }
                d[t] = sum
            }
            cm[0] = 1.0
            var cs = 0.0
            for (t in 1..tmax) { cs += d[t]; cm[t] = if (cs > 1e-12) d[t] * t / cs else 1.0 }
            var tau = -1
            var t = tmin
            while (t < tmax) {
                if (cm[t] < 0.15) {
                    while (t + 1 < tmax && cm[t + 1] < cm[t]) t++
                    tau = t; break
                }
                t++
            }
            if (tau < 0) { tau = tmin; for (u in tmin until tmax) if (cm[u] < cm[tau]) tau = u }
            val a = cm[max(tau - 1, 0)]; val b = cm[tau]; val c = cm[min(tau + 1, tmax)]
            val den = a - 2 * b + c
            val tt = tau + if (den != 0.0) 0.5 * (a - c) / den else 0.0
            f[k] = (sr / tt).toFloat()
            ap[k] = b.toFloat()
        }
        val gate = 0.1f * percentile(rms, 95f)
        for (k in 0 until frames) if (rms[k] < gate || ap[k] > 0.35f) f[k] = 0f
        return f
    }

    /** Octave errors folded back, smoothed, voiced frames only (for whole phrases). */
    fun voiced(f: FloatArray): FloatArray {
        val v = f.filter { it > 0 }.toFloatArray()
        if (v.size < 5) return FloatArray(0)
        val med = median(v)
        for (i in v.indices) { while (v[i] > med * 1.7f) v[i] /= 2; while (v[i] < med / 1.7f) v[i] *= 2 }
        return FloatArray(v.size) { i -> median(v.copyOfRange(max(0, i - 2), min(v.size, i + 3))) }
    }

    /** The main voiced stretch of a single syllable, gaps filled, edges trimmed, as 20 points (Hz). */
    fun syllable(f: FloatArray, points: Int = 20): FloatArray? {
        val g = f.copyOf()
        val vIdx = g.indices.filter { g[it] > 0 }
        if (vIdx.size < 6) return null
        val med = median(vIdx.map { g[it] }.toFloatArray())
        for (i in vIdx) { while (g[i] > med * 1.7f) g[i] /= 2; while (g[i] < med / 1.7f) g[i] *= 2 }
        val sm = g.copyOf()
        for (i in vIdx) sm[i] = median((max(0, i - 2) until min(g.size, i + 3)).map { g[it] }.filter { it > 0 }.toFloatArray())
        // Voiced runs, joined across gaps of up to 50 ms; keep the longest.
        val runs = mutableListOf<IntArray>()
        var i = 0
        while (i < sm.size) {
            if (sm[i] > 0) {
                var j = i
                while (j < sm.size && sm[j] > 0) j++
                if (runs.isNotEmpty() && i - runs.last()[1] <= 5) runs.last()[1] = j else runs += intArrayOf(i, j)
                i = j
            } else i++
        }
        val best = runs.maxByOrNull { it[1] - it[0] } ?: return null
        val seg = sm.copyOfRange(best[0], best[1])
        // Fill unvoiced gaps inside by linear interpolation.
        val known = seg.indices.filter { seg[it] > 0 }
        for (k in seg.indices) if (seg[k] <= 0) {
            val lo = known.lastOrNull { it < k } ?: known.first()
            val hi = known.firstOrNull { it > k } ?: known.last()
            seg[k] = if (hi == lo) seg[lo] else seg[lo] + (seg[hi] - seg[lo]) * (k - lo) / (hi - lo).toFloat()
        }
        if (seg.size < 6) return null
        val a = max(1, seg.size / 7)
        val b = seg.size - max(1, seg.size / 14)
        return resample(seg.copyOfRange(a, b), points)
    }

    fun semitones(hz: FloatArray, base: Float): FloatArray = FloatArray(hz.size) { 12f * log2(hz[it] / base) }

    fun resample(v: FloatArray, n: Int): FloatArray = FloatArray(n) { i ->
        if (v.size == 1) v[0] else {
            val p = i * (v.size - 1) / (n - 1).toFloat()
            val lo = p.toInt().coerceAtMost(v.size - 2)
            v[lo] + (v[lo + 1] - v[lo]) * (p - lo)
        }
    }

    fun median(v: FloatArray): Float {
        if (v.isEmpty()) return 0f
        val s = v.sorted()
        return if (s.size % 2 == 1) s[s.size / 2] else (s[s.size / 2 - 1] + s[s.size / 2]) / 2
    }

    private fun percentile(v: FloatArray, p: Float): Float {
        if (v.isEmpty()) return 0f
        val s = v.sorted()
        return s[((s.size - 1) * p / 100f).toInt()]
    }

    // ---- Tones -------------------------------------------------------------------------

    /**
     * Each tone's shape in semitones above/below the speaker's normal pitch, averaged from the
     * app's native recordings: 1 high and level, 2 low then rising, 3 low and dipping, 4 high and falling.
     */
    val templates: Map<Int, FloatArray> = mapOf(
        1 to floatArrayOf(3.50f, 3.63f, 3.71f, 3.76f, 3.80f, 3.84f, 3.87f, 3.89f, 3.92f, 3.95f, 3.99f, 4.02f, 4.06f, 4.10f, 4.12f, 4.15f, 4.19f, 4.21f, 4.21f, 4.23f),
        2 to floatArrayOf(-2.68f, -2.80f, -2.91f, -2.99f, -3.02f, -3.03f, -3.01f, -2.94f, -2.83f, -2.63f, -2.36f, -2.03f, -1.63f, -1.03f, -0.32f, 0.38f, 1.03f, 1.66f, 2.14f, 2.37f),
        3 to floatArrayOf(-2.18f, -2.31f, -2.48f, -2.65f, -2.81f, -2.96f, -3.08f, -3.16f, -3.20f, -3.20f, -3.17f, -3.10f, -2.97f, -2.80f, -2.58f, -2.29f, -2.02f, -1.82f, -1.61f, -1.51f),
        4 to floatArrayOf(4.56f, 4.61f, 4.58f, 4.53f, 4.45f, 4.33f, 4.15f, 3.91f, 3.63f, 3.26f, 2.79f, 2.27f, 1.67f, 0.94f, 0.03f, -0.92f, -1.58f, -2.08f, -2.33f, -2.26f),
    )

    /** The recordings' speaker's normal pitch (Hz), for putting reference phrases on the same scale. */
    const val REFERENCE_BASE = 248f

    private fun toneDistance(c: FloatArray, tp: FloatArray): Float {
        var best = Float.MAX_VALUE
        for (ai in 0..10) {
            val a = 0.6f + ai * 0.1f
            for (bi in 0..8) {
                val b = -2f + bi * 0.5f
                var sq = 0f
                var dsq = 0f
                for (i in c.indices) {
                    val m = a * tp[i] + b
                    sq += (c[i] - m) * (c[i] - m)
                    if (i > 0) {
                        val dc = c[i] - c[i - 1]; val dm = a * (tp[i] - tp[i - 1])
                        dsq += (dc - dm) * (dc - dm)
                    }
                }
                val dist = sq / c.size + 2f * dsq / (c.size - 1) + 0.3f * b * b + 1.5f * (a - 1) * (a - 1)
                if (dist < best) best = dist
            }
        }
        return best
    }

    data class ToneGuess(val tone: Int, val confidence: Float, val curve: FloatArray)

    /** Which tone a single spoken syllable has. [curve] is 20 points in semitones vs the speaker's normal pitch. */
    fun classify(hz: FloatArray, voice: VoiceProfile): ToneGuess? {
        val c = syllable(hz) ?: return null
        val st = voice.normalize(c)
        val d = templates.mapValues { toneDistance(st, it.value) }
        val sorted = d.entries.sortedBy { it.value }
        val margin = sorted[1].value - sorted[0].value
        val conf = (margin / (margin + sorted[0].value + 0.5f)).coerceIn(0f, 1f)
        return ToneGuess(sorted[0].key, conf, st)
    }

    // ---- Whole phrases -----------------------------------------------------------------

    /** How closely your pitch melody follows the native one, 0–100 (DTW on pitch and slope). */
    fun phraseScore(user: FloatArray, ref: FloatArray): Int {
        if (user.size < 5 || ref.size < 5) return 0
        val d = dtw(user, ref)
        val lenPenalty = 1.5f * max(0f, abs(ln(user.size / ref.size.toFloat())) - 0.25f)
        val x = (d + lenPenalty) / 1.4f
        return (100f / (1f + x.pow(3))).toInt().coerceIn(0, 100)
    }

    private fun gradient(v: FloatArray) = FloatArray(v.size) { i ->
        when (i) {
            0 -> v[1] - v[0]
            v.size - 1 -> v[i] - v[i - 1]
            else -> (v[i + 1] - v[i - 1]) / 2
        } * 4f
    }

    private fun dtw(a: FloatArray, b: FloatArray, w: Float = 0.2f): Float {
        val da = gradient(a); val db = gradient(b)
        val n = a.size; val m = b.size
        val inf = Float.MAX_VALUE / 4
        var prev = FloatArray(m + 1) { inf }.also { it[0] = 0f }
        for (i in 1..n) {
            val cur = FloatArray(m + 1) { inf }
            val center = i * m / n
            val band = (w * m).toInt() + 2
            for (j in max(1, center - band)..min(m, center + band)) {
                val cost = abs(a[i - 1] - b[j - 1]) + abs(da[i - 1] - db[j - 1])
                cur[j] = cost + minOf(prev[j] + 0.5f, cur[j - 1] + 0.5f, prev[j - 1])
            }
            prev = cur
        }
        return prev[m] / (n + m)
    }

    /** Your normal pitch and range, for normalising. */
    data class VoiceProfile(val baseHz: Float, val scale: Float = 1f, val calibrated: Boolean = false) {
        fun normalize(hz: FloatArray): FloatArray {
            val st = semitones(hz, baseHz)
            return FloatArray(st.size) { st[it] / scale }
        }
    }

    /**
     * Learns your normal pitch and range from 妈 麻 马 骂 (tones 1–4): fits your curves to the
     * templates with one offset and one scale, by least squares.
     */
    fun calibrate(syllables: List<FloatArray>): VoiceProfile? {
        if (syllables.size != 4) return null
        val all = syllables.flatMap { it.toList() }.toFloatArray()
        val base0 = median(all)
        // c ≈ a·T + b over all points.
        var sx = 0.0; var sy = 0.0; var sxx = 0.0; var sxy = 0.0; var n = 0
        syllables.forEachIndexed { k, hz ->
            val c = semitones(hz, base0)
            val tp = templates.getValue(k + 1)
            for (i in c.indices) { sx += tp[i]; sy += c[i]; sxx += tp[i] * tp[i]; sxy += tp[i] * c[i]; n++ }
        }
        val a = ((n * sxy - sx * sy) / (n * sxx - sx * sx)).toFloat().coerceIn(0.35f, 1.8f)
        val b = ((sy - a * sx) / n).toFloat()
        val base = base0 * 2f.pow(b / 12f)
        return VoiceProfile(base, a, calibrated = true)
    }

    /** Simple smoothing exponential helper for the running baseline. */
    fun blend(old: Float, new: Float, weight: Float) = if (old <= 0f) new else exp((1 - weight) * ln(old) + weight * ln(new))
}
