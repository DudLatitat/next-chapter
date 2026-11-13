package com.dudlatitat.nextchapter.data

import android.os.Build

/** Pinyin helpers: splitting into syllables, reading the tone of each, and making pinyin from characters. */
object Pinyin {
    private val marks: Map<Char, Pair<Char, Int>> = buildMap {
        fun put(base: Char, toned: String) = toned.forEachIndexed { i, c -> put(c, base to i + 1) }
        put('a', "āáǎà"); put('e', "ēéěè"); put('i', "īíǐì"); put('o', "ōóǒò"); put('u', "ūúǔù"); put('ü', "ǖǘǚǜ")
        put('A', "ĀÁǍÀ"); put('E', "ĒÉĚÈ"); put('I', "ĪÍǏÌ"); put('O', "ŌÓǑÒ"); put('U', "ŪÚǓÙ")
    }

    private val initials = listOf("zh", "ch", "sh", "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "r", "z", "c", "s", "y", "w")
    private val finals = listOf(
        "a", "o", "e", "i", "u", "ü", "ai", "ei", "ao", "ou", "an", "en", "ang", "eng", "ong", "er",
        "ia", "ie", "iao", "iu", "ian", "in", "iang", "ing", "iong", "ua", "uo", "uai", "ui", "uan", "un", "uang",
        "üe", "üan", "ün", "ue", "van", "ve",
    )
    private val alone = setOf("a", "o", "e", "ai", "ei", "ao", "ou", "an", "en", "ang", "eng", "er")
    private val syllables: Set<String> = buildSet {
        addAll(alone)
        initials.forEach { i -> finals.forEach { f -> add(i + f) } }
    }

    /** A letter with its tone mark removed, lower-cased (same length, so positions line up). */
    private fun plain(c: Char): Char = (marks[c]?.first ?: c).lowercaseChar()

    /** A syllable without its tone mark, lower-case ("Mǎ" → "ma"). */
    fun bare(syllable: String): String = syllable.map(::plain).joinToString("").replace("v", "ü")

    fun isLetter(c: Char) = c in marks || c.isLetter() && c.code < 0x250

    /** Tone of one syllable: 1–4, or 5 for neutral. */
    fun tone(syllable: String): Int = syllable.firstNotNullOfOrNull { marks[it]?.second } ?: 5

    /**
     * Splits a run of pinyin letters ("nánpéngyou") into syllable ranges, or one range for the
     * whole run if it can't be split. Handles erhua ("yǒudiǎnr" → yǒu + diǎnr).
     */
    fun split(word: String): List<IntRange> {
        splitExact(word)?.let { return it }
        if (word.length > 2 && plain(word.last()) == 'r') {
            splitExact(word.dropLast(1))?.let { inner -> return inner.dropLast(1) + listOf(inner.last().first..word.lastIndex) }
        }
        return listOf(word.indices)
    }

    private fun splitExact(word: String): List<IntRange>? {
        val p = word.map(::plain).joinToString("")
        val memo = HashMap<Int, List<IntRange>?>()
        fun go(at: Int): List<IntRange>? {
            if (at == p.length) return emptyList()
            if (memo.containsKey(at)) return memo[at]
            memo[at] = null
            var result: List<IntRange>? = null
            for (len in minOf(6, p.length - at) downTo 1) {
                if (p.substring(at, at + len) in syllables) {
                    val rest = go(at + len)
                    if (rest != null) { result = listOf(at until at + len) + rest; break }
                }
            }
            memo[at] = result
            return result
        }
        return go(0)
    }

    /** Every syllable in a pinyin line, with where it sits in the line. */
    fun syllablesIn(line: String): List<Pair<IntRange, Int>> {
        val out = mutableListOf<Pair<IntRange, Int>>()
        var i = 0
        while (i < line.length) {
            if (!isLetter(line[i])) { i++; continue }
            val start = i
            while (i < line.length && isLetter(line[i])) i++
            val word = line.substring(start, i)
            split(word).forEach { r -> out += (r.first + start..r.last + start) to tone(word.substring(r.first, r.last + 1)) }
        }
        return out
    }

    // Building the ICU transliterator is slow: make it once (it isn't thread-safe, hence synchronized).
    @Volatile private var hanLatin: android.icu.text.Transliterator? = null

    /** Pinyin with tone marks for Chinese text, from the phone's own dictionary (Android 10+). */
    fun fromHanzi(hanzi: String): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hanzi.isBlank()) return null
        return try {
            val t = hanLatin ?: android.icu.text.Transliterator.getInstance("Han-Latin").also { hanLatin = it }
            synchronized(t) { t.transliterate(hanzi.trim()) }.replace(Regex("\\s+"), " ").trim()
        } catch (_: Exception) { null }
    }

    /** Characters with one clear reading, for the tone quiz. */
    val toneQuiz: List<Triple<String, String, String>> = listOf(
        Triple("妈", "mā", "mom"), Triple("麻", "má", "numbing"), Triple("马", "mǎ", "horse"), Triple("骂", "mà", "to scold"),
        Triple("汤", "tāng", "soup"), Triple("糖", "táng", "sugar"), Triple("躺", "tǎng", "to lie down"), Triple("烫", "tàng", "scalding"),
        Triple("猫", "māo", "cat"), Triple("毛", "máo", "fur"), Triple("帽", "mào", "hat"),
        Triple("八", "bā", "eight"), Triple("拔", "bá", "to pull"), Triple("爸", "bà", "dad"),
        Triple("衣", "yī", "clothes"), Triple("姨", "yí", "aunt"), Triple("椅", "yǐ", "chair"),
        Triple("天", "tiān", "sky"), Triple("书", "shū", "book"), Triple("吃", "chī", "to eat"), Triple("花", "huā", "flower"), Triple("他", "tā", "he"),
        Triple("茶", "chá", "tea"), Triple("人", "rén", "person"), Triple("鱼", "yú", "fish"), Triple("钱", "qián", "money"), Triple("门", "mén", "door"), Triple("来", "lái", "to come"),
        Triple("水", "shuǐ", "water"), Triple("你", "nǐ", "you"), Triple("米", "mǐ", "rice"), Triple("买", "mǎi", "to buy"), Triple("冷", "lěng", "cold"), Triple("雨", "yǔ", "rain"),
        Triple("饭", "fàn", "meal"), Triple("去", "qù", "to go"), Triple("是", "shì", "to be"), Triple("肉", "ròu", "meat"), Triple("卖", "mài", "to sell"), Triple("热", "rè", "hot"),
    )
}
