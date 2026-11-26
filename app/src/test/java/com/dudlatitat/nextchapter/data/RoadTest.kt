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

class RoadLeftTest {
    private fun d(s: String) = java.time.LocalDate.parse(s).toEpochDay()
    @org.junit.Test fun yearsAndMonths() {
        org.junit.Assert.assertEquals("3 years, 1 month", Road.left(d("2026-11-26"), d("2029-12-31")))
        org.junit.Assert.assertEquals("1 year", Road.left(d("2026-01-15"), d("2027-02-01")))
        org.junit.Assert.assertEquals("2 months", Road.left(d("2026-11-26"), d("2027-01-31")))
        org.junit.Assert.assertEquals("5 days", Road.left(d("2026-11-26"), d("2026-12-01")))
        org.junit.Assert.assertEquals("here", Road.left(d("2026-11-26"), d("2026-11-26")))
    }
    @org.junit.Test fun ticksAreNewYears() {
        val t = Road.yearTicks(d("2026-11-26"), d("2029-12-31"))
        org.junit.Assert.assertEquals(listOf(2028, 2029), t.map { it.first }) // 2027 is too close to "now" to label
        org.junit.Assert.assertTrue(t.zipWithNext().all { (a, b) -> a.second < b.second })
    }
    @org.junit.Test fun weekdayDate() {
        org.junit.Assert.assertEquals("Fri · Jan 15, 2027", Road.weekdayDate(d("2027-01-15"), d("2026-11-26")))
        org.junit.Assert.assertEquals("Thu · Dec 31", Road.weekdayDate(d("2026-12-31"), d("2026-11-26")))
    }
}
