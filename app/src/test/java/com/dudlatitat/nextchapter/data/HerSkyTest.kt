package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Test

class HerSkyTest {
    @Test fun keyframesAreExactAndBlendsAreBetween() {
        assertEquals(0xFF141C44L, HerSky.color(0))
        assertEquals(0xFF141C44L, HerSky.color(3 * 60))
        assertEquals(0xFF4A6FB0L, HerSky.color(12 * 60))
        assertEquals("wraps past midnight", HerSky.color(0), HerSky.color(1440))
        // Halfway between two keys is halfway in every channel.
        assertEquals(0xFF808080L, HerSky.blend(0xFF000000L, 0xFFFFFFFFL, 0.5f))
        val mid = HerSky.color(7 * 60) // between dawn (6:15) and morning (8:00)
        val r = (mid shr 16) and 0xFF
        assert(r in 0x3D..0x5B) { "red $r should sit between the two keys" }
        assertEquals(0xFF000000L, HerSky.color(17 * 60 + 45) and 0xFF000000L) // always opaque
    }

    @Test fun phases() {
        assertEquals(HerSky.Phase.NIGHT, HerSky.phase(2 * 60))
        assertEquals(HerSky.Phase.DAWN, HerSky.phase(6 * 60))
        assertEquals(HerSky.Phase.DAY, HerSky.phase(12 * 60))
        assertEquals(HerSky.Phase.DUSK, HerSky.phase(19 * 60))
        assertEquals(HerSky.Phase.NIGHT, HerSky.phase(22 * 60))
    }
}
