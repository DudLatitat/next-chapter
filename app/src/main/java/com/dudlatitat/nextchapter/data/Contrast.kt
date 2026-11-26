package com.dudlatitat.nextchapter.data

/**
 * WCAG contrast math for the frosted cards: text must stay readable (≥ 4.5:1) even over the
 * brightest sky behind the glass. Pure, tested in ContrastTest.
 */
object Contrast {
    private fun lin(c: Int): Double { val s = c / 255.0; return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4) }
    fun luminance(argb: Long): Double = 0.2126 * lin(((argb shr 16) and 0xFF).toInt()) + 0.7152 * lin(((argb shr 8) and 0xFF).toInt()) + 0.0722 * lin((argb and 0xFF).toInt())
    fun ratio(a: Long, b: Long): Double { val x = luminance(a); val y = luminance(b); return (maxOf(x, y) + 0.05) / (minOf(x, y) + 0.05) }
    /** [top] at [alpha] laid over [under] (both opaque ARGB). */
    fun over(top: Long, alpha: Float, under: Long): Long {
        fun ch(c: Long, sh: Int) = ((c shr sh) and 0xFF).toInt()
        var out = 0xFF000000L
        for (sh in intArrayOf(16, 8, 0)) out = out or ((ch(top, sh) * alpha + ch(under, sh) * (1 - alpha)).toInt().toLong() shl sh)
        return out
    }
}
