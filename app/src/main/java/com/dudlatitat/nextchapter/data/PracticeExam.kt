package com.dudlatitat.nextchapter.data

import kotlin.random.Random

/**
 * Practice exams from [ExamBank]: Quick 20 (weighted like the real exam, answers at the end)
 * and the Weak-spot drill (what you've missed first, then your weakest domain; instant answers).
 * Pure, tested in PracticeExamTest.
 */
object PracticeExam {
    enum class Mode(val key: String, val size: Int, val minutes: Int) { QUICK("quick", 20, 25), DRILL("drill", 10, 0) }

    /** A question as shown: [order] lists indices into q.options in display order. */
    data class Item(val q: ExamBank.Q, val order: List<Int>)

    fun item(q: ExamBank.Q, random: Random) = Item(q, q.options.indices.shuffled(random))

    /** Quick 20: questions per domain in proportion to the exam (largest remainder). */
    fun quick(core: Int, random: Random = Random.Default): List<Item> {
        val bank = ExamBank.of(core)
        return PracticeTest.perDomain(Mode.QUICK.size, core).flatMap { (d, n) ->
            bank.filter { it.domain == d }.shuffled(random).take(n)
        }.shuffled(random).map { item(it, random) }
    }

    /** Ids whose most recent answer was wrong (an old miss clears once you get it right). */
    fun stillMissed(s: AppState, core: Int): List<String> {
        val last = linkedMapOf<String, Boolean>()
        s.tests.forEach { t ->
            t.asked.forEach { id -> last.remove(id); last[id] = id !in t.missed }
        }
        return last.filter { !it.value && it.key.startsWith("x$core:") }.keys.toList().asReversed()
    }

    /** Right share per domain from past exam-bank answers (null where nothing's been asked). */
    fun domainHistory(s: AppState, core: Int): Map<Int, Float?> {
        val asked = mutableMapOf<Int, Int>(); val right = mutableMapOf<Int, Int>()
        s.tests.forEach { t ->
            t.asked.forEach { id ->
                val q = ExamBank.find(id)?.takeIf { it.core == core } ?: return@forEach
                asked[q.domain] = (asked[q.domain] ?: 0) + 1
                if (id !in t.missed) right[q.domain] = (right[q.domain] ?: 0) + 1
            }
        }
        return APlus.domainsOf(core).keys.associateWith { d -> asked[d]?.let { (right[d] ?: 0) / it.toFloat() } }
    }

    /** The domain to drill: lowest test accuracy (weighted by exam share); before any tests, the card strength's weakest. */
    fun focus(s: AppState, core: Int): Int {
        val h = domainHistory(s, core)
        val w = APlus.domainsOf(core)
        return h.filterValues { it != null }.entries.maxByOrNull { (d, acc) -> (1f - acc!!) * w.getValue(d).second }?.key
            ?: APlus.weakest(s)?.takeIf { core == APlus.core(s) }
            ?: w.maxByOrNull { it.value.second }!!.key
    }

    /** Weak-spot drill: up to 10, your misses first, then the focus domain, then anything. */
    fun drill(s: AppState, core: Int, random: Random = Random.Default): List<Item> {
        val n = Mode.DRILL.size
        val missed = stillMissed(s, core).mapNotNull { ExamBank.find(it) }.take(n)
        val f = focus(s, core)
        val bank = ExamBank.of(core)
        val more = (bank.filter { it.domain == f }.shuffled(random) + bank.shuffled(random)).filter { it !in missed }.distinct()
        return (missed + more).take(n).map { item(it, random) }
    }

    /**
     * Whether [answer] is right. Answers are indices into q.options: for SINGLE/MULTI the chosen
     * ones (any order); for ORDER the options in the order tapped.
     */
    fun isRight(q: ExamBank.Q, answer: List<Int>): Boolean = when (q.kind) {
        ExamBank.Kind.ORDER -> answer == q.right.indices.toList()
        else -> answer.toSet() == q.right.indices.toSet()
    }

    /** How many choices a question wants ("Choose two"). */
    fun picks(q: ExamBank.Q): Int = if (q.kind == ExamBank.Kind.ORDER) q.right.size else q.right.size

    data class Result(val right: Int, val total: Int, val missed: List<String>, val byDomain: Map<Int, Pair<Int, Int>>) {
        val pct: Int get() = if (total == 0) 0 else right * 100 / total
        val passed: Boolean get() = pct >= (PracticeTest.PASS * 100).toInt()
    }

    fun score(items: List<Item>, answers: Map<String, List<Int>>): Result {
        val missed = items.filterNot { isRight(it.q, answers[it.q.id].orEmpty()) }.map { it.q.id }
        val byDomain = items.groupBy { it.q.domain }.mapValues { (_, l) -> l.count { it.q.id !in missed } to l.size }
        return Result(items.size - missed.size, items.size, missed, byDomain.toSortedMap())
    }
}
