package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RoadTest {
    private val d = LocalDate.of(2026, 11, 25).toEpochDay()

    @Test fun milestonesInOrderEndingAtTheMove() {
        val s = AppState(
            moveDate = "2029-06-01", aplusExam = "2027-03-10",
            trips = listOf(Trip("t", "2027-01-05")),
            certs = Career.defaultCerts.map { if (it.id == "netplus") it.copy(date = "2027-09-01") else it },
        )
        val m = Road.marks(s, d)
        assertEquals(listOf(Road.Kind.TRIP, Road.Kind.EXAM, Road.Kind.CERT, Road.Kind.MOVE), m.map { it.kind })
        val end = m.last().day
        assertTrue(m.zipWithNext().all { (a, b) -> a.at(d, end) <= b.at(d, end) })
        assertEquals(1f, m.last().at(d, end))
    }

    @Test fun pastAndPassedAreLeftOut() {
        val s = AppState(moveDate = "2029-06-01", aplusExam = "2026-01-01", certs = Career.defaultCerts.map { it.copy(status = "passed", date = "2027-01-01") })
        assertEquals(listOf(Road.Kind.MOVE), Road.marks(s, d).map { it.kind })
        assertTrue(Road.marks(AppState(), d).isEmpty())
    }
}

class PaperworkTest {
    private val passport = MoveTask("Documents:Passport valid well past the move date", "Passport", "Documents")

    @Test fun startDatesWorkBackFromTheMove() {
        assertEquals(LocalDate.of(2028, 6, 1).toEpochDay(), Paperwork.startBy(passport, "2029-06-01"))
        assertEquals("start by Jun 2028", Paperwork.label(passport, "2029-06-01", LocalDate.of(2026, 11, 25).toEpochDay()))
        assertEquals("start now", Paperwork.label(passport, "2027-06-01", LocalDate.of(2026, 11, 25).toEpochDay()))
        assertNull("done steps don't nag", Paperwork.label(passport.copy(done = true), "2029-06-01"))
        assertNull("no move date", Paperwork.label(passport, null))
    }

    @Test fun everyLeadTimeMatchesASeededStep() {
        val ids = Seed.tasks.map { it.id }.toSet()
        assertTrue(Paperwork.leadMonths.keys.all { it in ids })
    }

    @Test fun starterBudgetFillsOnlyEmptyLines() {
        val b = listOf(BudgetItem("1", "Rent", "房租", 4200), BudgetItem("2", "Food", "吃饭", 0), BudgetItem("3", "Custom", "", 0))
        val out = Seed.withStarterBudget(b)
        assertEquals(4200, out[0].rmb)
        assertEquals(2500, out[1].rmb)
        assertEquals(0, out[2].rmb)
    }
}

class CallsTest {
    private val la = ZoneId.of("America/Los_Angeles")

    @Test fun weekdayEveningsAndDaylightSaving() {
        // Winter (PST, China 16 h ahead): a weekday 7–10 PM is her 11 AM–2 PM next day: all awake.
        val wed = LocalDate.of(2026, 12, 2).toEpochDay()
        val w = Calls.week(wed, la, days = 1).single()
        assertEquals(19 * 60, w.fromMin); assertEquals(22 * 60, w.toMin)
        assertEquals(11, w.herFrom.hour)
        // Summer (PDT, 15 h ahead): her start moves an hour earlier, to 10 AM.
        val july = LocalDate.of(2026, 7, 1).toEpochDay()
        assertEquals(10, Calls.week(july, la, days = 1).single().herFrom.hour)
    }

    @Test fun weekendWindowSkipsHerNight() {
        // Saturday 9 AM–10 PM LA (winter) = her Sat 1 AM–Sun 2 PM; she's awake from her 8 AM = your 4 PM.
        val sat = LocalDate.of(2026, 12, 5).toEpochDay()
        val w = Calls.week(sat, la, days = 1).single()
        assertEquals(16 * 60, w.fromMin)
        assertEquals(22 * 60, w.toMin)
        assertEquals(7, Calls.week(sat, la).size)
    }
}
