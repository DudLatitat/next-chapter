package com.dudlatitat.nextchapter.data

/**
 * What a finished session or game earned, for the payoff sequence: which of today's three rings
 * moved (and which closed), what readiness pieces just ticked, whether all three are now done,
 * and what to do next. Worked out by comparing the state when the screen opened with now.
 * Pure, tested in PayoffTest.
 */
object Payoff {
    data class Ring(val kind: DayPlan.Kind, val label: String, val from: Float, val to: Float, val closed: Boolean)
    data class Result(
        val rings: List<Ring>,
        val ticked: List<String>,
        val climbed: Ready.Rung?,
        val allDone: Boolean,
        val next: DayPlan.Item?,
    ) {
        val closedAny: Boolean get() = rings.any { it.closed }
    }

    fun of(before: AppState, after: AppState, day: Long = today()): Result {
        val a = DayPlan.today(before, day)
        val b = DayPlan.today(after, day)
        val rings = a.items.zip(b.items).map { (x, y) ->
            Ring(y.kind, y.ring, x.progress, y.progress, closed = !x.done && y.done)
        }
        val ra = Ready.family(before)
        val rb = Ready.family(after)
        val doneBefore = ra.rungs.flatMap { r -> r.pieces.filter { it.done }.map { r.id + "/" + it.label.substringBefore(" ") } }.toSet()
        val ticked = rb.rungs.flatMap { r -> r.pieces.filter { it.done && (r.id + "/" + it.label.substringBefore(" ")) !in doneBefore }.map { it.label } }
        val climbed = if (rb.climbed > ra.climbed) rb.rungs[rb.climbed - 1] else null
        return Result(rings, ticked, climbed, allDone = !a.finished && b.finished, next = b.items.firstOrNull { !it.done })
    }

    /** The once-a-day "done for today" moment: all three done and not yet claimed today. */
    fun claimDue(s: AppState, day: Long = today()): Boolean = s.celebrated != day && DayPlan.today(s, day).finished
}
