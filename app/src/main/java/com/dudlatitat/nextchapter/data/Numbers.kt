package com.dudlatitat.nextchapter.data

import kotlin.random.Random

/**
 * Number ears 听价格: prices and clock times as people say them (standard Mandarin), heard at
 * full speed and typed as numbers. The lines are recorded in CI like any phrase (make_voice.py
 * reads this file). [read] understands them independently, and NumbersTest checks every line
 * against it, so a mistyped answer can't ship.
 */
object Numbers {
    enum class Kind { PRICE, TIME }
    data class Item(val kind: Kind, val hanzi: String, val answer: String)

    private fun p(h: String, a: String) = Item(Kind.PRICE, h, a)
    private fun t(h: String, a: String) = Item(Kind.TIME, h, a)

    val prices = listOf(
        p("两毛", "0.2"),
        p("五毛", "0.5"),
        p("八毛", "0.8"),
        p("一块五", "1.5"),
        p("两块", "2"),
        p("两块五", "2.5"),
        p("三块五", "3.5"),
        p("四块二", "4.2"),
        p("五块", "5"),
        p("六块六", "6.6"),
        p("七块五", "7.5"),
        p("八块", "8"),
        p("九块九", "9.9"),
        p("十块", "10"),
        p("十二块", "12"),
        p("十二块八", "12.8"),
        p("十五块", "15"),
        p("十五块五", "15.5"),
        p("十八块五", "18.5"),
        p("二十块", "20"),
        p("二十二块", "22"),
        p("二十五块", "25"),
        p("二十六块八", "26.8"),
        p("二十八块", "28"),
        p("三十五块", "35"),
        p("三十五块五", "35.5"),
        p("三十八块", "38"),
        p("四十五块", "45"),
        p("四十八块八", "48.8"),
        p("五十八块", "58"),
        p("六十六块", "66"),
        p("八十八块", "88"),
        p("九十九块", "99"),
        p("九十九块九", "99.9"),
        p("一百块", "100"),
        p("一百零五块", "105"),
        p("一百一十块", "110"),
        p("一百二十八块", "128"),
        p("一百六十八块", "168"),
        p("两百块", "200"),
        p("两百八十八块", "288"),
        p("两百九十九块", "299"),
        p("三百六十五块", "365"),
        p("五百二十块", "520"),
        p("八百八十八块", "888"),
    )

    val times = listOf(
        t("一点", "1:00"),
        t("一点零五分", "1:05"),
        t("两点", "2:00"),
        t("两点十分", "2:10"),
        t("两点半", "2:30"),
        t("三点", "3:00"),
        t("三点一刻", "3:15"),
        t("四点二十分", "4:20"),
        t("五点四十分", "5:40"),
        t("六点", "6:00"),
        t("六点三刻", "6:45"),
        t("差五分七点", "6:55"),
        t("七点", "7:00"),
        t("七点二十五分", "7:25"),
        t("七点半", "7:30"),
        t("八点", "8:00"),
        t("八点一刻", "8:15"),
        t("差五分九点", "8:55"),
        t("九点", "9:00"),
        t("九点零五分", "9:05"),
        t("九点半", "9:30"),
        t("差十分十点", "9:50"),
        t("十点", "10:00"),
        t("十点三刻", "10:45"),
        t("十一点", "11:00"),
        t("十一点五十分", "11:50"),
        t("十二点", "12:00"),
        t("十二点半", "12:30"),
    )

    const val ROUND = 10

    /** A round: mostly the kind you picked, at random (null = mixed). */
    fun round(kind: Kind? = null, random: Random = Random.Default): List<Item> =
        (if (kind == null) prices + times else if (kind == Kind.PRICE) prices else times).shuffled(random).take(ROUND)

    /** Speed for the next item: starts at normal, a little faster with each right answer in a row. */
    fun speed(streak: Int): Float = (1f + 0.08f * streak).coerceAtMost(1.5f)

    /** Does what you typed match? Prices compare as numbers (35.50 = 35.5); times accept 7:55, 7.55 or 755. */
    fun matches(item: Item, typed: String): Boolean {
        val t = typed.trim().replace('：', ':').replace('。', '.')
        return when (item.kind) {
            Kind.PRICE -> t.toDoubleOrNull()?.let { kotlin.math.abs(it - item.answer.toDouble()) < 0.001 } == true
            Kind.TIME -> {
                val digits = t.filter { it.isDigit() }
                val (h, m) = item.answer.split(":").map { it.toInt() }
                digits == "$h${"%02d".format(m)}" || (m == 0 && digits == "$h") ||
                    t.split(':', '.').mapNotNull { it.toIntOrNull() }.let { it.size == 2 && it[0] == h && it[1] == m }
            }
        }
    }

    private val digit = mapOf('零' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4, '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9)

    /** 1–999 written in Chinese ("一百零五", "两百", "十二") → the number; null if it isn't one. */
    fun whole(s: String): Int? {
        if (s.isEmpty()) return null
        var total = 0; var cur = 0
        for (c in s) when {
            c in digit -> cur = digit.getValue(c)
            c == '十' -> { total += (if (cur == 0) 1 else cur) * 10; cur = 0 }
            c == '百' -> { total += cur * 100; cur = 0 }
            else -> return null
        }
        return total + cur
    }

    /** What a line means, read straight from the Chinese: "35.5", "8:55"…; null if it can't tell. */
    fun read(h: String): String? {
        if (h.endsWith("毛")) return whole(h.dropLast(1))?.let { "0.$it" }
        if ("块" in h) {
            val (a, b) = h.split("块", limit = 2)
            val yuan = whole(a) ?: return null
            if (b.isEmpty()) return "$yuan"
            val jiao = whole(b) ?: return null
            return "$yuan.$jiao"
        }
        if (h.startsWith("差")) {
            val m = whole(h.substringAfter("差").substringBefore("分")) ?: return null
            val hour = whole(h.substringAfter("分").substringBefore("点")) ?: return null
            return "${hour - 1}:${"%02d".format(60 - m)}"
        }
        if ("点" in h) {
            val hour = whole(h.substringBefore("点")) ?: return null
            val rest = h.substringAfter("点")
            val m = when {
                rest.isEmpty() -> 0
                rest == "半" -> 30
                rest == "一刻" -> 15
                rest == "三刻" -> 45
                rest.endsWith("分") -> whole(rest.dropLast(1).removePrefix("零")) ?: return null
                else -> return null
            }
            return "$hour:${"%02d".format(m)}"
        }
        return null
    }
}
