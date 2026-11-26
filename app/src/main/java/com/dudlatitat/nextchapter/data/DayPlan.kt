package com.dudlatitat.nextchapter.data

/**
 * 今日三件 · Today's three: one small, finishable plan instead of a wall of tools.
 *  1. Reviews (about 10, fewer coming back after a break)
 *  2. One game, rotating to the one you haven't played longest (keeps it fresh)
 *  3. One life thing: A+ when cards are due, otherwise tonight's routine job
 * Plus the comeback rules: a rest day a week doesn't break the streak, and after missed days the
 * review session is capped (most overdue first; the rest return over the next days).
 * Pure, tested in DayPlanTest.
 */
object DayPlan {
    enum class Kind { REVIEW, PLAY, LIFE }
    /** [ring] is the short label under its ring on Today; [progress] fills it (0–1). */
    data class Item(val kind: Kind, val title: String, val detail: String, val done: Boolean, val tool: String? = null, val ring: String = "", val progress: Float = if (done) 1f else 0f)
    data class Plan(val items: List<Item>, val welcomeBack: Int) {
        val doneCount: Int get() = items.count { it.done }
        val finished: Boolean get() = items.all { it.done }
    }

    /** The games that rotate, with a friendly name (tool id = Practice tool / shortcut name). */
    val games = linkedMapOf(
        "dinner" to "Talk · 对话 (a short scene)",
        "callouts" to "Callouts · 喊话",
        "numbers" to "Number ears · 听价格",
        "texts" to "Texts · 微信 (one chat)",
        "sprint" to "Typing sprint · 打字",
        "family" to "Her family · 家谱",
        "prayer" to "Prayer ladder · 祷告",
    )

    const val REVIEWS = 10

    /** Your daily review number (Settings), kept sensible. */
    fun daily(s: AppState): Int = s.dailyReviews.coerceIn(5, 100)
    /** After a break: an easy few, not a pile (the rest come back over the next days). */
    const val COMEBACK_CAP = 5

    /** Days since you last practised (0 = today, 1 = yesterday); null if never. */
    fun daysAway(s: AppState, day: Long = today()): Long? = Stats.practiceDays(s).lastOrNull()?.let { day - it }

    /** "Welcome back" mode: 2+ days without practice. Returns how many days missed, or 0. */
    fun missed(s: AppState, day: Long = today()): Int = daysAway(s, day)?.let { if (it >= 3) (it - 1).toInt() else 0 } ?: 0

    /** How many cards a review session holds: everything normally, capped after a break. */
    fun sessionLimit(s: AppState, day: Long = today()): Int = sessionSize(s, day)

    /** Today's slice of reviews: 10 (or an easy 5 after a break), never more than is due. */
    fun slice(s: AppState, day: Long = today()): Int {
        val reviewedToday = s.days[day]?.reviews ?: 0
        val due = Srs.queue(s, day).size
        return minOf(if (missed(s, day) > 0) minOf(COMEBACK_CAP, daily(s)) else daily(s), reviewedToday + due).coerceAtLeast(0)
    }

    /** How many cards a session holds: what's left of today's slice, or "keep going: 10 more" after it. */
    fun sessionSize(s: AppState, day: Long = today()): Int {
        val left = slice(s, day) - (s.days[day]?.reviews ?: 0)
        return if (left > 0) left else daily(s)
    }

    /** The game for today: the one played longest ago (never-played first); ties broken by the day. */
    fun pickGame(s: AppState, day: Long = today()): String {
        val ids = games.keys.toList()
        return ids.withIndex().minWith(compareBy({ s.played[it.value] ?: -1L }, { Math.floorMod(it.index - day, ids.size.toLong()) })).value
    }

    fun today(s: AppState, day: Long = today()): Plan {
        val reviewedToday = s.days[day]?.reviews ?: 0
        val due = Srs.queue(s, day).size
        val miss = missed(s, day)
        val goal = minOf(if (miss > 0) minOf(COMEBACK_CAP, daily(s)) else daily(s), reviewedToday + due).coerceAtLeast(1)
        val caughtUp = due == 0
        val review = Item(
            Kind.REVIEW,
            when { caughtUp && reviewedToday == 0 -> "Reviews: all caught up"; miss > 0 -> "Welcome back: an easy $goal"; else -> "Review $goal phrases" },
            if (reviewedToday >= goal || caughtUp) "Done ✓" else "${minOf(reviewedToday, goal)}/$goal · about ${maxOf(1, goal / 2)} min",
            done = reviewedToday >= goal || caughtUp,
            ring = "Phrases",
            progress = if (reviewedToday >= goal || caughtUp) 1f else reviewedToday.toFloat() / goal,
        )
        val game = pickGame(s, day)
        val play = Item(Kind.PLAY, "Play: ${games.getValue(game)}", "One round, about 3 min", done = s.played.values.any { it == day }, tool = game, ring = "Play")
        val aplusDue = APlus.queue(s, day).size
        val aplusDone = s.aplus.count { it.last == day && it.due > day }
        val job = Routine.jobOn(s, java.time.LocalDate.ofEpochDay(day))
        val life = if (aplusDue > 0 || aplusDone > 0) {
            val g = minOf(5, aplusDone + aplusDue)
            Item(Kind.LIFE, "A+: $g cards", if (aplusDone >= g) "Done ✓" else "$aplusDone/$g", done = aplusDone >= g, tool = "aplus", ring = "A+", progress = (aplusDone.toFloat() / g).coerceAtMost(1f))
        } else {
            val checked = Routine.checkFor(s.routineChecks, day)
            Item(Kind.LIFE, "Tonight: ${Routine.text(job.task, s.herName)}", job.minimum, done = checked == CheckIn.DONE || checked == CheckIn.MINIMUM, tool = "job", ring = "Tonight",
                progress = when (checked) { CheckIn.DONE -> 1f; CheckIn.MINIMUM -> 0.6f; else -> 0f })
        }
        return Plan(listOf(review, play, life), miss)
    }

    /** Remembers that a game was played today (for the rotation and the plan). */
    fun played(s: AppState, tool: String, day: Long = today()): AppState = s.copy(played = s.played + (tool to day))
}
