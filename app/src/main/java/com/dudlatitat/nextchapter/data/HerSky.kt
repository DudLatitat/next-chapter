package com.dudlatitat.nextchapter.data

/**
 * Her sky: the colour of the sky where she is right now (Chengdu time), for Today's header.
 * Keyframes through the day, blended smoothly between them, so one glance says dawn, day,
 * dusk or night for her. Colours are ARGB longs so this stays pure (tested in HerSkyTest).
 */
object HerSky {
    enum class Phase(val emoji: String, val word: String) { NIGHT("🌙", "night"), DAWN("🌅", "dawn"), DAY("☀️", "day"), DUSK("🌇", "dusk") }

    private const val NIGHT = 0xFF141C44L
    /** (minute of her day, colour) — must start at 0 and end at 1440. */
    val keys: List<Pair<Int, Long>> = listOf(
        0 to NIGHT,
        5 * 60 to NIGHT,
        6 * 60 + 15 to 0xFF5B3B6BL, // dawn rose
        8 * 60 to 0xFF3D5A9CL,
        12 * 60 to 0xFF4A6FB0L, // pale day
        17 * 60 to 0xFF41629FL,
        18 * 60 + 30 to 0xFF6A3E5EL, // dusk
        19 * 60 + 45 to 0xFF2A2F66L,
        21 * 60 to NIGHT,
        24 * 60 to NIGHT,
    )

    fun phase(minute: Int): Phase = when (Math.floorMod(minute, 1440)) {
        in 5 * 60 until 7 * 60 + 30 -> Phase.DAWN
        in 7 * 60 + 30 until 18 * 60 -> Phase.DAY
        in 18 * 60 until 20 * 60 + 30 -> Phase.DUSK
        else -> Phase.NIGHT
    }

    /** Her sky colour at [minute] of her day (0–1439), as ARGB. */
    fun color(minute: Int): Long {
        val m = Math.floorMod(minute, 1440)
        val i = keys.indexOfLast { it.first <= m }.coerceAtMost(keys.size - 2)
        val (m0, c0) = keys[i]
        val (m1, c1) = keys[i + 1]
        val t = (m - m0).toFloat() / (m1 - m0)
        return blend(c0, c1, t)
    }

    fun blend(a: Long, b: Long, t: Float): Long {
        fun ch(c: Long, shift: Int) = ((c shr shift) and 0xFF).toInt()
        var out = 0xFF000000L
        for (shift in intArrayOf(16, 8, 0)) {
            val v = Math.round(ch(a, shift) + (ch(b, shift) - ch(a, shift)) * t.coerceIn(0f, 1f))
            out = out or (v.toLong() shl shift)
        }
        return out
    }
}
