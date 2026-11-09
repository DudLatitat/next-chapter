package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class LifeTest {
    private fun at(h: Int, m: Int) = LocalTime.of(h, m)

    @Test fun partsOfTheDayAndCallTime() {
        assertEquals(Life.Part.MORNING, Life.partOf(at(6, 30)))
        assertEquals(Life.Part.MORNING, Life.partOf(at(11, 59)))
        assertEquals(Life.Part.DAY, Life.partOf(at(12, 0)))
        assertEquals(Life.Part.DAY, Life.partOf(at(16, 59)))
        assertEquals(Life.Part.PLAN, Life.partOf(at(17, 0)))
        assertEquals(Life.Part.PLAN, Life.partOf(at(18, 59)))
        // 7–8 pm belongs to Yuling's call: nothing else is asked then.
        assertEquals(Life.Part.CALL, Life.partOf(at(19, 0)))
        assertEquals(Life.Part.CALL, Life.partOf(at(19, 59)))
        assertEquals(Life.Part.LATE, Life.partOf(at(20, 0)))
    }

    @Test fun herLabelAndMoveCityComeFromSettingsWithFallbacks() {
        assertEquals("Her time", Life.herLabel("", " "))
        assertEquals("A · B", Life.herLabel(" A ", "B"))
        assertEquals("A", Life.herLabel("A", ""))
        assertEquals("B", Life.herLabel("", "B"))
        assertEquals("Sichuan", Life.moveTo("  "))
        assertEquals("B", Life.moveTo("B"))
    }

    @Test fun notificationsStaySilentOnlyDuringTheCall() {
        assertFalse(Life.isCallTime(at(18, 59)))
        assertTrue(Life.isCallTime(at(19, 0)))
        assertTrue(Life.isCallTime(at(19, 59)))
        assertFalse(Life.isCallTime(at(20, 0)))
    }

    @Test fun earlyStartIsThreeToSeven() {
        assertTrue(Life.earlyStart(at(6, 59)))
        assertTrue(Life.earlyStart(at(3, 0)))
        assertFalse(Life.earlyStart(at(7, 0)))
        assertFalse(Life.earlyStart(at(2, 59))) // still last night
    }

    @Test fun oneGoalPerDayAndOldOnesDrop() {
        var goals = Life.withGoal(emptyList(), DayGoal(1000, "A+ study"))
        goals = Life.withGoal(goals, DayGoal(1000, "Chinese practice"))
        assertEquals(listOf(DayGoal(1000, "Chinese practice")), goals)
        goals = Life.withGoal(goals, DayGoal(1200, "Rest"))
        assertEquals(listOf(1200L), goals.map { it.day })
    }

    @Test fun weekCountsOnlyThatWeek() {
        val monday = LocalDate.of(2026, 11, 2)
        val d = monday.toEpochDay()
        val s = AppState(
            prayedDays = listOf(d - 1, d, d + 1, d + 6, d + 7),
            wakeDays = listOf(d + 2),
            goals = listOf(DayGoal(d, "a", true), DayGoal(d + 1, "b", false), DayGoal(d + 3, "c"), DayGoal(d + 7, "d", true)),
        )
        assertEquals(Life.Week(prayed = 3, earlyStarts = 1, goalsSet = 3, goalsDone = 1), Life.week(s, monday))
    }

    @Test fun addDayOnce() {
        assertEquals(listOf(5L), Life.addDay(Life.addDay(emptyList(), 5), 5))
    }

    @Test fun prayersTakeTurns() {
        val n = Prayers.daily.size
        assertEquals(Prayers.daily[0], Prayers.forDay(0))
        assertEquals(Prayers.forDay(3), Prayers.forDay(3L + n))
        assertEquals(Prayers.daily[n - 1], Prayers.forDay(-1)) // never crashes on odd input
    }

    @Test fun prayerLinesAddUpToTheWholePrayer() {
        (Prayers.daily + Prayers.signOfTheCross).forEach { p ->
            assertEquals(p.hanzi, p.lines.joinToString("") { it.first })
            assertTrue(p.lines.all { it.second.isNotBlank() } && p.english.isNotEmpty())
        }
    }

    @Test fun yulingsTimeFollowsDaylightSaving() {
        val la = java.time.ZoneId.of("America/Los_Angeles")
        // Winter (PST): 16 hours ahead. 8 pm Tuesday in LA is noon Wednesday in Sichuan.
        val winter = java.time.ZonedDateTime.of(2026, 12, 1, 20, 0, 0, 0, la)
        assertEquals(16, Life.hoursAhead(winter))
        assertEquals(java.time.LocalTime.NOON, Life.herTime(winter).toLocalTime())
        assertEquals("tomorrow", Life.herDayWord(winter))
        // Summer (PDT): 15 hours ahead. 7 am in LA is 10 pm the same day there.
        val summer = java.time.ZonedDateTime.of(2026, 7, 1, 7, 0, 0, 0, la)
        assertEquals(15, Life.hoursAhead(summer))
        assertEquals(22, Life.herTime(summer).hour)
        assertEquals("today", Life.herDayWord(summer))
    }

    @Test fun oldSavedDataStillLoads() {
        // A backup from before 1.32 has none of the new fields: they must default, not fail.
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val s = json.decodeFromString(AppState.serializer(), """{"cards":[],"newPerDay":8,"saved":1200}""")
        assertEquals(1200, s.saved)
        assertTrue(s.goals.isEmpty() && s.prayedDays.isEmpty() && s.weekNotes.isEmpty() && s.eveningCheck)
    }
}
