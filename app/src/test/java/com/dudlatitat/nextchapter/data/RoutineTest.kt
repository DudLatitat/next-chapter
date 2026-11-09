package com.dudlatitat.nextchapter.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RoutineTest {
    // Monday 9 Nov 2026.
    private val monday = LocalDate.of(2026, 11, 9)
    private val d = monday.toEpochDay()

    @Test fun seedHasOneJobPerDayWithAMinimum() {
        assertEquals((1..7).toList(), Routine.seed.map { it.dow })
        assertTrue(Routine.seed.all { it.task.isNotBlank() && it.firstStep.isNotBlank() && it.minimum.isNotBlank() })
    }

    @Test fun todaysJobFollowsTheLocalDate() {
        assertEquals("aplus", Routine.jobFor(Routine.seed, monday).kind)
        assertEquals("chinese", Routine.jobFor(Routine.seed, monday.plusDays(1)).kind)
        assertEquals("aplus-review", Routine.jobFor(Routine.seed, monday.plusDays(4)).kind)
        assertEquals(7, Routine.jobFor(Routine.seed, monday.plusDays(6)).dow)
        // A schedule missing a day still has a job for it.
        assertEquals(3, Routine.jobFor(Routine.seed.filterNot { it.dow == 3 }, monday.plusDays(2)).dow)
    }

    @Test fun herNameFillsIn() {
        assertEquals("Chinese with Mei", Routine.text("Chinese with {her}", " Mei "))
        assertEquals("Chinese with her", Routine.text("Chinese with {her}", ""))
    }

    @Test fun checkInsReplaceAndStreakCountsDoneAndMinimum() {
        var c = listOf<RoutineCheck>()
        c = Routine.withCheck(c, RoutineCheck(d, CheckIn.DONE))
        c = Routine.withCheck(c, RoutineCheck(d + 1, CheckIn.MINIMUM))
        c = Routine.withCheck(c, RoutineCheck(d + 2, CheckIn.SKIPPED))
        c = Routine.withCheck(c, RoutineCheck(d + 2, CheckIn.MINIMUM)) // changed her mind: one entry per day
        assertEquals(3, c.size)
        assertEquals(CheckIn.MINIMUM, Routine.checkFor(c, d + 2))
        assertEquals(3, Routine.streak(c, d + 2))
        assertEquals("today not checked yet: the streak is still alive", 3, Routine.streak(c, d + 3))
        c = Routine.withCheck(c, RoutineCheck(d + 3, CheckIn.SKIPPED))
        assertEquals(0, Routine.streak(c, d + 3))
    }

    @Test fun weekDotsRunMondayToSunday() {
        val c = listOf(RoutineCheck(d, CheckIn.DONE), RoutineCheck(d + 6, CheckIn.SKIPPED), RoutineCheck(d + 7, CheckIn.DONE))
        val week = Routine.week(c, monday.plusDays(3))
        assertEquals(7, week.size)
        assertEquals(CheckIn.DONE, week[0]); assertNull(week[1]); assertEquals(CheckIn.SKIPPED, week[6])
    }

    @Test fun firstTwoWeeksAreTheTrial() {
        assertTrue(Routine.inTrial(emptyList(), d))
        val c = listOf(RoutineCheck(d, CheckIn.DONE))
        assertTrue(Routine.inTrial(c, d + 13))
        assertFalse(Routine.inTrial(c, d + 14))
    }

    @Test fun editingADayKeepsTheWeekInOrder() {
        val edited = Routine.withDay(Routine.seed, Routine.seed[2].copy(task = "Subnetting", minutes = 18 * 60 + 15))
        assertEquals((1..7).toList(), edited.map { it.dow })
        assertEquals("Subnetting", edited[2].task)
    }

    @Test fun weekReviewCountsJobs() {
        val s = AppState(routineChecks = listOf(RoutineCheck(d, CheckIn.DONE), RoutineCheck(d + 1, CheckIn.SKIPPED), RoutineCheck(d + 2, CheckIn.MINIMUM)))
        val w = Life.week(s, monday)
        assertEquals(3, w.jobsChecked); assertEquals(2, w.jobsWon)
    }

    @Test fun earlyStartFollowsWakeTime() {
        assertTrue(Life.earlyStart(LocalTime.of(7, 59), wakeMinutes = 7 * 60 + 30))
        assertFalse(Life.earlyStart(LocalTime.of(8, 0), wakeMinutes = 7 * 60 + 30))
        assertFalse(Life.earlyStart(LocalTime.of(2, 0), wakeMinutes = 7 * 60 + 30))
    }

    @Test fun oldSavedDataLoadsWithTheSeededRoutine() {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val old = json.decodeFromString<AppState>("""{"newPerDay":5}""")
        assertEquals(Routine.seed, old.routine)
        assertEquals(7 * 60 + 30, old.wakeMinutes)
        // And a check-in survives a save/load round trip.
        val saved = old.copy(routineChecks = listOf(RoutineCheck(d, CheckIn.MINIMUM)))
        assertEquals(saved.routineChecks, json.decodeFromString<AppState>(json.encodeToString(saved)).routineChecks)
    }
}
