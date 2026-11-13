package com.dudlatitat.nextchapter.data

/**
 * Prayer ladder: learn a prayer by heart one rung at a time. Each rung hides more characters
 * (the prayer text itself is never changed, only covered): 0 all shown, 1 every fourth hidden,
 * 2 every other one, 3 only each phrase's first character left, 4 nothing left. Pure, tested.
 */
object Ladder {
    const val TOP = 4
    const val GAP = '＿'

    private fun isHan(c: Char) = c in '一'..'鿿'

    /** The line as shown on [rung]: hidden characters become ＿, punctuation always stays. */
    fun masked(line: String, rung: Int): String {
        var k = 0 // position among the characters of the current phrase (resets after punctuation)
        val out = StringBuilder()
        for (c in line) {
            if (!isHan(c)) { out.append(c); k = 0; continue }
            val hide = when (rung.coerceIn(0, TOP)) {
                0 -> false
                1 -> k % 4 == 3
                2 -> k % 2 == 1
                3 -> k > 0
                else -> true
            }
            out.append(if (hide) GAP else c)
            k++
        }
        return out.toString()
    }

    /** Share of characters hidden on a rung, for the little meter. */
    fun hiddenShare(lines: List<String>, rung: Int): Float {
        val all = lines.sumOf { l -> l.count(::isHan) }
        if (all == 0) return 0f
        return lines.sumOf { l -> masked(l, rung).count { it == GAP } } / all.toFloat()
    }

    /** The saved rung after finishing [rung] (climbs one; never goes down). */
    fun climbed(saved: Map<String, Int>, prayer: String, rung: Int): Map<String, Int> =
        saved + (prayer to maxOf(saved[prayer] ?: 0, (rung + 1).coerceAtMost(TOP + 1)))

    /** By heart: the top rung (nothing shown) was finished. */
    fun byHeart(saved: Map<String, Int>, prayer: String) = (saved[prayer] ?: 0) > TOP
}
