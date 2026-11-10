package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacesTest {
    private val d = 20_400L
    private fun t(h: Int, m: Int = 0) = h * 60 + m

    @Test fun arrivingAtWorkStartsTheDayFiveMinutesLaterAndItEndsEightHoursAfter() {
        val r = Places.onEnter(emptyList(), "work", d, t(8, 52))
        assertEquals(t(8, 57), Places.workStart(r.arrivals, d))
        assertEquals(t(16, 57), Places.workEnd(r.arrivals, d))
        assertFalse(r.home)
    }

    @Test fun onlyTheFirstWorkArrivalCounts() {
        var a = Places.onEnter(emptyList(), "work", d, t(8, 50)).arrivals
        a = Places.onEnter(a, "work", d, t(13, 10)).arrivals // back from lunch
        assertEquals(t(8, 55), Places.workStart(a, d))
    }

    @Test fun homeAfterWorkOpensTheEveningButLunchAtHomeDoesNot() {
        var a = Places.onEnter(emptyList(), "work", d, t(9)).arrivals
        val lunch = Places.onEnter(a, "home", d, t(12, 30))
        assertFalse("2.5 h after starting: just lunch", lunch.home)
        assertNull(Places.homeArrival(lunch.arrivals, d))
        val evening = Places.onEnter(a, "home", d, t(17, 40))
        assertTrue(evening.home)
        assertEquals(t(17, 40), Places.homeArrival(evening.arrivals, d))
        a = evening.arrivals
        assertFalse("only once a day", Places.onEnter(a, "home", d, t(21)).home)
    }

    @Test fun homeOnADayOffIsIgnored() {
        val r = Places.onEnter(emptyList(), "home", d, t(15))
        assertFalse(r.home); assertTrue(r.arrivals.isEmpty())
    }

    @Test fun theEveningStartsWhenYouGetHomeElseAfterTheExpectedEndElseTheUsualTime() {
        val base = AppState(placesOn = true, homeMinutes = t(17, 45))
        assertEquals("nothing noticed: usual time", t(17, 45), Places.homeTime(base, d))
        val atWork = base.copy(arrivals = Places.onEnter(emptyList(), "work", d, t(9, 30)).arrivals)
        assertEquals("seen at work 9:35 → ends 5:35, evening opens by 6:20", t(18, 20), Places.homeTime(atWork, d))
        val home = atWork.copy(arrivals = Places.onEnter(atWork.arrivals, "home", d, t(17, 58)).arrivals)
        assertEquals(t(17, 58), Places.homeTime(home, d))
        assertEquals("feature off: usual time", t(17, 45), Places.homeTime(home.copy(placesOn = false), d))
    }

    @Test fun atWorkOnlyBetweenArrivingAndTheEvening() {
        val s = AppState(placesOn = true, arrivals = Places.onEnter(emptyList(), "work", d, t(9)).arrivals)
        assertTrue(Places.atWork(s, d, t(9)))
        assertTrue(Places.atWork(s, d, t(16)))
        assertFalse("past the fallback", Places.atWork(s, d, t(18)))
        val home = s.copy(arrivals = Places.manualHome(s.arrivals, d, t(17)))
        assertFalse(Places.atWork(home, d, t(17, 1)))
    }

    @Test fun aWorkArrivalOnlyCountsInTheMorning() {
        // The 1.58 bug: saving Work while at home at 5:05 pm "started" a workday ending at midnight,
        // so tonight's job never opened.
        assertTrue(Places.onEnter(emptyList(), "work", d, t(17, 5)).arrivals.isEmpty())
        assertTrue(Places.onEnter(emptyList(), "work", d, t(4, 59)).arrivals.isEmpty())
        assertEquals(t(12, 59) + 5, Places.workStart(Places.onEnter(emptyList(), "work", d, t(12, 59)).arrivals, d))
    }

    @Test fun resetForgetsOnlyToday() {
        val a = listOf(Arrival(d - 1, "work", t(9)), Arrival(d, "work", t(9)), Arrival(d, "home", t(17)))
        assertEquals(listOf(Arrival(d - 1, "work", t(9))), Places.resetDay(a, d))
    }
}
