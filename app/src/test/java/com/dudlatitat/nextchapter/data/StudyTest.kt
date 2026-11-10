package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class StudyTest {
    @Test fun timerMinutesAreCapped() {
        assertEquals(25, Study.minutes(0, 25 * 60_000L + 30_000))
        assertEquals(240, Study.minutes(0, 10 * 3_600_000L)) // left running overnight
        assertEquals(0, Study.minutes(1000, 0))
    }

    @Test fun sessionsMergeByDayAndSubject() {
        var log = Study.add(emptyList(), StudySession(10, "A+", 25))
        log = Study.add(log, StudySession(10, "A+", 15))
        log = Study.add(log, StudySession(10, "School", 30))
        log = Study.add(log, StudySession(10, "School", 0)) // nothing to add
        assertEquals(listOf(StudySession(10, "A+", 40), StudySession(10, "School", 30)), log)
    }

    @Test fun weekTotalsOnlyCountThatWeek() {
        val monday = LocalDate.of(2026, 11, 2); val d = monday.toEpochDay()
        val s = AppState(study = listOf(StudySession(d - 1, "A+", 99), StudySession(d, "A+", 30), StudySession(d + 6, "A+", 20), StudySession(d + 3, "School", 45)))
        assertEquals(mapOf("A+" to 50, "School" to 45), Study.week(s, monday))
        assertEquals("1 h 35 min", Study.label(95)); assertEquals("45 min", Study.label(45)); assertEquals("2 h", Study.label(120))
    }

    @Test fun testIsWeightedLikeTheExam() {
        val split = PracticeTest.perDomain(20)
        assertEquals(20, split.values.sum())
        assertEquals(mapOf(1 to 3, 2 to 5, 3 to 5, 4 to 2, 5 to 5), split)
        assertEquals(90, PracticeTest.perDomain(90).values.sum())
    }

    @Test fun eachQuestionHasFourDifferentOptionsAndOneRightAnswer() {
        val qs = PracticeTest.build(20, Random(7))
        assertEquals(20, qs.size)
        assertEquals(20, qs.map { it.card.id }.toSet().size) // no repeats
        qs.forEach { q ->
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.toSet().size)
            assertEquals(q.card.a, q.options[q.correct])
            assertTrue(q.options.all { o -> APlus.core1.any { it.domain == q.card.domain && it.a == o } }) // distractors from the same domain
        }
    }
}
