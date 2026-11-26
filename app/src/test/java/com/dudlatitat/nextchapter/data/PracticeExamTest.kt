package com.dudlatitat.nextchapter.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamBankTest {
    @Test fun bankIsWellFormed() {
        assertEquals("unique ids", ExamBank.all.size, ExamBank.all.map { it.id }.toSet().size)
        for (q in ExamBank.all) {
            assertTrue(q.id, q.id.startsWith("x${q.core}:"))
            assertTrue(q.id, q.domain in APlus.domainsOf(q.core).keys)
            assertTrue(q.id, q.why.isNotBlank() && q.stem.isNotBlank())
            assertEquals(q.id + " has no duplicate options", q.options.size, q.options.toSet().size)
            when (q.kind) {
                ExamBank.Kind.SINGLE -> { assertEquals(q.id, 1, q.right.size); assertEquals(q.id, 3, q.wrong.size) }
                ExamBank.Kind.MULTI -> { assertTrue(q.id, q.right.size >= 2 && q.wrong.size >= 2); assertTrue(q.id, q.stem.contains("TWO") || q.stem.contains("THREE")) }
                ExamBank.Kind.ORDER -> { assertTrue(q.id, q.right.size >= 3 && q.wrong.isEmpty()) }
            }
        }
        // Enough in every domain for Quick 20's share.
        for (core in 1..2) for ((d, n) in PracticeTest.perDomain(20, core)) assertTrue("core $core domain $d", ExamBank.of(core).count { it.domain == d } >= n * 2)
    }

    @Test fun quickIsWeightedAndShuffled() {
        val t = PracticeExam.quick(2, Random(3))
        assertEquals(20, t.size)
        assertTrue(t.all { it.q.core == 2 })
        assertEquals(PracticeTest.perDomain(20, 2), t.groupBy { it.q.domain }.mapValues { it.value.size })
        assertTrue(t.all { it.order.sorted() == it.q.options.indices.toList() })
    }

    @Test fun grading() {
        val single = ExamBank.core1.first { it.kind == ExamBank.Kind.SINGLE }
        assertTrue(PracticeExam.isRight(single, listOf(0)))
        assertFalse(PracticeExam.isRight(single, listOf(1)))
        assertFalse(PracticeExam.isRight(single, emptyList()))
        val multi = ExamBank.all.first { it.kind == ExamBank.Kind.MULTI }
        assertTrue(PracticeExam.isRight(multi, listOf(1, 0)))
        assertFalse("half right is wrong", PracticeExam.isRight(multi, listOf(0)))
        assertFalse(PracticeExam.isRight(multi, listOf(0, 2)))
        val order = ExamBank.all.first { it.kind == ExamBank.Kind.ORDER }
        assertTrue(PracticeExam.isRight(order, order.right.indices.toList()))
        assertFalse(PracticeExam.isRight(order, order.right.indices.toList().reversed()))
    }

    @Test fun missesClearWhenRightAndDrillStartsWithThem() {
        val a = ExamBank.core1[0].id; val b = ExamBank.core1[1].id; val c = ExamBank.core1[2].id
        val s = AppState(tests = listOf(
            TestResult(1, 1, 3, 60, missed = listOf(a, b), asked = listOf(a, b, c), mode = "quick", core = 1),
            TestResult(2, 1, 1, 60, missed = emptyList(), asked = listOf(a), mode = "drill", core = 1),
        ))
        assertEquals(listOf(b), PracticeExam.stillMissed(s, 1))
        val d = PracticeExam.drill(s, 1, Random(1))
        assertEquals(10, d.size)
        assertEquals(b, d.first().q.id)
        assertEquals(d.size, d.map { it.q.id }.toSet().size)
        // Old tests (card ids, no 'asked') don't break anything.
        assertTrue(PracticeExam.stillMissed(AppState(tests = listOf(TestResult(1, 15, 20, 900, listOf("c1:port-ftp")))), 1).isEmpty())
    }

    @Test fun scoreByDomain() {
        val items = PracticeExam.quick(1, Random(5))
        val answers = items.associate { it.q.id to (if (it.q.kind == ExamBank.Kind.ORDER) it.q.right.indices.toList() else it.q.right.indices.toList()) }
        val r = PracticeExam.score(items, answers)
        assertEquals(20, r.right); assertEquals(100, r.pct); assertTrue(r.passed)
        val none = PracticeExam.score(items, emptyMap())
        assertEquals(0, none.right); assertEquals(20, none.missed.size)
        assertEquals(20, none.byDomain.values.sumOf { it.second })
    }
}
