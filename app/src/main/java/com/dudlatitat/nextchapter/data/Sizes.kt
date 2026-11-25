package com.dudlatitat.nextchapter.data

/**
 * How big to draw Chinese so long phrases still fit: short words get the full size, long sentences
 * step down (they'd otherwise wrap into 3–4 crowded lines). Pure, tested in SizesTest.
 */
object Sizes {
    fun hanzi(text: String, max: Float): Float {
        val n = text.count { it in '一'..'鿿' }
        val scale = when {
            n <= 6 -> 1f
            n <= 10 -> 0.82f
            n <= 16 -> 0.68f
            else -> 0.6f
        }
        return maxOf(max * scale, minOf(max, 20f))
    }

    /** Line spacing for big characters: 1.3× the size, so wrapped lines never touch. */
    fun lineHeight(size: Float): Float = size * 1.3f
}
