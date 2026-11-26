package com.dudlatitat.nextchapter.data

class RoadSpreadTest {
    @org.junit.Test fun closeDotsArePushedApart() {
        val out = Road.spread(listOf(0.10f, 0.11f, 0.12f, 1f), 0.05f)
        for (i in 1 until out.size) org.junit.Assert.assertTrue("$out", out[i] - out[i - 1] >= 0.0499f)
        org.junit.Assert.assertEquals(1f, out.last(), 0.0001f)
        org.junit.Assert.assertEquals(0.10f, out.first(), 0.0001f)
    }
    @org.junit.Test fun crowdedEndPullsBack() {
        val out = Road.spread(listOf(0.97f, 0.98f, 1f), 0.05f)
        org.junit.Assert.assertEquals(1f, out.last(), 0.0001f)
        for (i in 1 until out.size) org.junit.Assert.assertTrue("$out", out[i] - out[i - 1] >= 0.0499f)
    }
}
