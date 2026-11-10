package com.dudlatitat.nextchapter.data

import kotlin.random.Random

/**
 * Sentence building (Chinese → Practice → 造句): rounds made from your own deck phrases, split into
 * words by their pinyin spacing (我想跟你一起玩 = 我 / 想 / 跟 / 你 / 一起 / 玩). Pure, tested in BuilderTest.
 */
object Builder {
    enum class Kind { BUILD, FILL, LISTEN }

    /** One exercise. [answer] is the words in order; [tiles] what you can tap (BUILD/LISTEN: the words
     *  plus decoys, shuffled; FILL: the 4 choices for the blank at [blank]). */
    data class Item(val card: Card, val kind: Kind, val answer: List<String>, val tiles: List<String>, val blank: Int = -1)

    private const val VOWELS = "aeiouüāáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"
    private val HAN = Regex("""\p{IsHan}""")

    /** Syllables in one pinyin word: groups of vowels (an apostrophe also splits: kě'ài = 2). */
    fun syllables(token: String): Int =
        token.lowercase().split('\'', '’').sumOf { part ->
            var n = 0; var inVowel = false
            for (c in part) { val v = c in VOWELS; if (v && !inVowel) n++; inVowel = v }
            n
        }

    /**
     * The phrase as words, or null if the pinyin and characters don't line up (erhua like 哪儿 = nǎr,
     * or anything unusual), so a phrase is only used when the split is certain.
     */
    fun words(card: Card): List<String>? {
        val chars = HAN.findAll(card.hanzi).map { it.value }.toList()
        val tokens = card.pinyin.split(' ', ' ').map { t -> t.filter { it.isLetter() || it == '\'' || it == '’' } }.filter { it.isNotEmpty() }
        val sizes = tokens.map { syllables(it) }
        if (sizes.any { it == 0 } || sizes.sum() != chars.size || chars.isEmpty()) return null
        var i = 0
        return sizes.map { n -> chars.subList(i, i + n).joinToString("").also { i += n } }
    }

    /** Phrases good for building: 3–8 words, not single core words, and a certain split. */
    fun eligible(cards: List<Card>): List<Pair<Card, List<String>>> =
        cards.filter { it.deck != "core" }.mapNotNull { c -> words(c)?.takeIf { it.size in 3..8 }?.let { c to it } }

    /** A round of up to [size]: phrases you've started first, then new ones; kinds take turns. */
    fun round(cards: List<Card>, random: Random, size: Int = 8): List<Item> {
        val pool = eligible(cards)
        if (pool.size < 2) return emptyList()
        val started = pool.filter { !it.first.isNew }.shuffled(random)
        val fresh = pool.filter { it.first.isNew }.shuffled(random)
        val picked = (started + fresh).take(size)
        val allWords = pool.flatMap { it.second }.distinct()
        val kinds = listOf(Kind.BUILD, Kind.FILL, Kind.LISTEN)
        return picked.mapIndexed { i, (card, ws) ->
            val kind = kinds[i % 3]
            val others = (allWords - ws.toSet()).shuffled(random)
            when (kind) {
                Kind.FILL -> {
                    val blank = ws.indices.maxByOrNull { j -> ws[j].length * 10 + random.nextInt(10) }!!
                    val choices = (listOf(ws[blank]) + others.filter { it.length == ws[blank].length }.take(3).ifEmpty { others.take(3) })
                        .let { if (it.size < 4) it + others.filterNot { o -> o in it }.take(4 - it.size) else it }
                    Item(card, kind, ws, choices.distinct().shuffled(random), blank)
                }
                else -> Item(card, kind, ws, (ws + others.take(if (ws.size >= 6) 1 else 2)).shuffled(random))
            }
        }
    }

    /** Which placed words are in the right spot (for green/red tiles). */
    fun marks(answer: List<String>, placed: List<String>): List<Boolean> = placed.mapIndexed { i, w -> answer.getOrNull(i) == w }

    /** Right if it reads the same (two words can swap places when they're identical). */
    fun isRight(answer: List<String>, placed: List<String>): Boolean = placed.joinToString("") == answer.joinToString("")
}
