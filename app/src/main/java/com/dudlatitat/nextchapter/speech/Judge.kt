package com.dudlatitat.nextchapter.speech

/**
 * Pure helpers for the one-take speaking check (no Android code, so they're unit-tested):
 * which tones count as right in a phrase, and where each character sits in the recording.
 */
object Judge {
    /**
     * The tones that count as right for each character. [tones] are dictionary tones (1–4, 5 =
     * neutral). Natives change some tones in speech (tone sandhi), and saying it either way is fine:
     * - 3 before another 3 becomes 2 (你好 nǐ hǎo is said ní hǎo)
     * - 不 before a 4th tone becomes 2 (不是 bú shì)
     * - 一 becomes 2 before a 4th tone, 4 before tones 1–3 (一个 yí gè, 一起 yì qǐ)
     * Neutral tones (5) aren't judged: an empty set means "any".
     */
    /**
     * Words whose second syllable natives soften to a neutral tone in everyday speech, even where
     * a dictionary (or a card) marks a full tone (知道 zhīdao, 意思 yìsi). That syllable isn't judged.
     * Keep in step with SOFT in tools/tone_proto/phrase_collect.py.
     */
    val SOFT = setOf("知道", "意思", "时候", "告诉", "休息", "舒服", "地方", "清楚", "事情", "麻烦", "先生", "消息", "便宜", "关系", "名字", "学生", "早上", "晚上", "东西", "朋友", "喜欢", "认识", "客气", "漂亮", "衣服", "明白", "头发", "眼睛", "豆腐", "姑娘", "打算", "觉得", "怎么", "什么", "这么", "那么", "多么", "钥匙")

    fun acceptedTones(chars: String, tones: List<Int>): List<Set<Int>> = tones.indices.map { i ->
        val t = tones[i]
        val next = tones.getOrNull(i + 1)
        val prev = chars.getOrNull(i - 1)
        when {
            t == 5 -> emptySet()
            // Second half of a softened word (知道) or a doubled word (妈妈, 看看, 谢谢): said lightly.
            i > 0 && chars.substring(i - 1, minOf(i + 1, chars.length)) in SOFT -> emptySet()
            i > 0 && prev == chars.getOrNull(i) && prev != '一' && prev != '不' -> emptySet()
            chars.getOrNull(i) == '不' && next == 4 -> setOf(4, 2)
            chars.getOrNull(i) == '一' && next == 4 -> setOf(1, 2)
            chars.getOrNull(i) == '一' && next in setOf(1, 2, 3) -> setOf(1, 4)
            t == 3 && next == 3 -> setOf(3, 2)
            else -> setOf(t)
        }
    }

    /**
     * Where each character's time span falls along the pitch line, as 0–1 fractions. The line
     * only has voiced frames ([hz] > 0, one per 10 ms), so a span is placed by counting the voiced
     * frames before and inside it.
     */
    fun spansOnCurve(hz: FloatArray, spans: List<Pair<Float, Float>>, frameSeconds: Float = 0.01f): List<Pair<Float, Float>> {
        val total = hz.count { it > 0 }
        if (total == 0) return spans.map { 0f to 0f }
        // before[k] = voiced frames in hz[0 until k].
        val before = IntArray(hz.size + 1)
        for (k in hz.indices) before[k + 1] = before[k] + if (hz[k] > 0) 1 else 0
        fun at(seconds: Float) = before[(seconds / frameSeconds).toInt().coerceIn(0, hz.size)] / total.toFloat()
        return spans.map { (s, e) -> at(s) to at(e) }
    }

    /** The frames of [hz] between two times (seconds), for judging one character's tone. */
    fun slice(hz: FloatArray, start: Float, end: Float, frameSeconds: Float = 0.01f): FloatArray {
        val a = (start / frameSeconds).toInt().coerceIn(0, hz.size)
        val b = (end / frameSeconds).toInt().coerceIn(a, hz.size)
        return hz.copyOfRange(a, b)
    }

    /**
     * Lines up recognized characters (with times) against the target, in order: the k-th target
     * character takes the next matching recognized one. Returns, per target character, its time
     * span or null if it wasn't heard.
     */
    fun matchTimes(target: String, heard: List<Triple<Char, Float, Float>>): List<Pair<Float, Float>?> {
        var j = 0
        return target.map { c ->
            var k = j
            while (k < heard.size && heard[k].first != c) k++
            if (k < heard.size) { j = k + 1; heard[k].second to heard[k].third } else null
        }
    }

    /**
     * Characters Vosk's model only knows inside a word (啡 only in 咖啡), found by
     * tools/tone_proto/vosk_oov.py over all the app's content. Run it again after adding phrases.
     */
    private val modelWords = listOf("洛杉矶", "咖啡", "眼睛", "钥匙", "基础", "安逸", "鸳鸯")

    /** The phrase as Vosk words: single characters, except those that only exist inside a word. */
    fun tokens(chars: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < chars.length) {
            val w = modelWords.firstOrNull { chars.startsWith(it, i) }
            if (w != null) { out += w; i += w.length } else { out += chars[i].toString(); i++ }
        }
        return out
    }

    /** Vosk grammar for checking a phrase: the whole phrase, or any of its words on their own. */
    fun grammarFor(chars: String): List<String> = tokens(chars).let { t -> listOf(t.joinToString(" ")) + t }

    /** Splits recognized words into characters, sharing each word's time evenly between its characters. */
    fun charsOf(words: List<VoskWord>): List<Triple<Char, Float, Float>> = words.flatMap { w ->
        val cs = w.text.filter { it.code in 0x4E00..0x9FFF }
        val step = if (cs.isEmpty()) 0f else (w.end - w.start) / cs.length
        cs.mapIndexed { i, c -> Triple(c, w.start + i * step, w.start + (i + 1) * step) }
    }

    private val toned = mapOf('a' to "āáǎà", 'e' to "ēéěè", 'i' to "īíǐì", 'o' to "ōóǒò", 'u' to "ūúǔù", 'ü' to "ǖǘǚǜ")
    private val untone: Map<Char, Char> = buildMap { toned.forEach { (b, t) -> t.forEach { put(it, b) } } }

    /**
     * The same syllable with another tone ("tāng", 2 → "táng"), for hints like "sounded like
     * táng". The mark goes where pinyin puts it: on a or e, on the o of "ou", else the last vowel.
     */
    fun retone(syllable: String, tone: Int): String {
        val bare = syllable.map { untone[it] ?: it }.joinToString("").lowercase().replace('v', 'ü')
        if (tone !in 1..4) return bare
        val at = when {
            'a' in bare -> bare.indexOf('a')
            'e' in bare -> bare.indexOf('e')
            "ou" in bare -> bare.indexOf("ou")
            else -> bare.indexOfLast { it in toned.keys }
        }
        if (at < 0) return bare
        return bare.substring(0, at) + toned.getValue(bare[at])[tone - 1] + bare.substring(at + 1)
    }

    /**
     * What the phrase tone model looks at for one character: its pitch shape (20 points, in
     * semitones vs your normal pitch) plus how it joins its neighbours. Must match
     * tools/tone_proto/phrase_tones.py exactly, since the weights were learned there.
     */
    fun phraseFeatures(contours: List<FloatArray?>, i: Int): FloatArray {
        val c = contours[i]!!
        val p = contours.getOrNull(i - 1)
        val n = contours.getOrNull(i + 1)
        var argmin = 0
        for (k in c.indices) if (c[k] < c[argmin]) argmin = k
        return floatArrayOf(
            c[0], c[4], c[8], c[12], c[16],
            c[19] - c[0], c[10] - c[0], c[19] - c[10], c.min(), c.max(), argmin / 19f,
            if (p != null) c[0] - p[19] else 0f, if (p != null) 1f else 0f,
            if (n != null) n[0] - c[19] else 0f, if (n != null) 1f else 0f,
            if (i == contours.lastIndex) 1f else 0f, if (i == 0) 1f else 0f,
        )
    }

    /**
     * The tone of each character in a phrase, with how sure the model is (0–1), or null where
     * there was no clear pitch. A small linear model (softmax) trained on the app's own native
     * recordings, because tones in connected speech don't look like tones said alone.
     */
    fun phraseTones(contours: List<FloatArray?>): List<Pair<Int, Float>?> = contours.indices.map { i ->
        if (contours[i] == null) return@map null
        val f = phraseFeatures(contours, i)
        val m = PhraseToneModel
        val z = FloatArray(4) { t ->
            var sum = m.B[t]
            for (k in f.indices) sum += m.W[t][k] * (f[k] - m.MU[k]) / m.SD[k]
            sum
        }
        val top = z.max()
        val e = FloatArray(4) { kotlin.math.exp(z[it] - top) }
        val total = e.sum()
        val best = (0 until 4).maxBy { e[it] }
        (best + 1) to e[best] / total
    }
}
