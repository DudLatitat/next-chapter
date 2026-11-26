package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    // The app's colours (ui/theme/Theme.kt): ink, muted, and the frost's darkest/lightest stop.
    private val ink = 0xFFEEF1FFL
    private val muted = 0xFFA9B1D6L
    private val cardTop = 0xFF243063L
    private val cardBottom = 0xFF1A2350L

    @Test fun textOnFrostStaysReadableOverEveryHerSkyColour() {
        // The frost is cardTop at 66% (strong: 78%) over whatever is behind it; the brightest thing
        // that can be behind a card is her sky's colour at any minute of the day.
        val brightest = (0 until 1440 step 5).map { HerSky.color(it) }.maxBy { Contrast.luminance(it) }
        for (alpha in listOf(0.66f, 0.54f, 0.78f)) {
            val bg = Contrast.over(cardTop, alpha, brightest)
            assertTrue("ink on frost at $alpha: ${Contrast.ratio(ink, bg)}", Contrast.ratio(ink, bg) >= 4.5)
            assertTrue("muted on frost at $alpha: ${Contrast.ratio(muted, bg)}", Contrast.ratio(muted, bg) >= 3.0) // secondary text: large-text level
        }
        assertTrue(Contrast.ratio(ink, cardBottom) >= 4.5)
    }

    @Test fun mathSanity() {
        assertEquals(21.0, Contrast.ratio(0xFFFFFFFFL, 0xFF000000L), 0.01)
        assertEquals(1.0, Contrast.ratio(0xFF777777L, 0xFF777777L), 0.0001)
    }
}
