package com.dudlatitat.nextchapter.data

/**
 * Move checklist steps with a suggested "start by" date, worked back from the move date with
 * typical lead times (renewals, government offices and mail are slow). Suggestions, not rules:
 * shown as "start by …" next to the step. Pure, tested in PaperworkTest.
 */
object Paperwork {
    /** Months before the move to start each seeded step (matched by the step's id). */
    val leadMonths: Map<String, Long> = mapOf(
        "Documents:Passport valid well past the move date" to 12,
        "Documents:Research the visa for living with a Chinese spouse" to 12,
        "Documents:Plan the marriage paperwork with Yuling" to 10,
        "Documents:Certified birth certificate and degree copies" to 8,
        "Documents:Get documents authenticated for use in China" to 6,
        "Life in China:Job or remote work lined up" to 6,
        "Life in China:Decide what to ship, sell, or keep" to 4,
        "Money:Emergency fund set aside" to 3,
        "Money:Plan for US bank accounts after moving" to 3,
        "Life in China:Plan for a Chinese driver's license" to 3,
        "Money:Card that works with Alipay / WeChat Pay" to 2,
        "Life in China:Health insurance for China" to 2,
        "Life in China:Phone plan / SIM for China" to 1,
    )

    /** The day to start [task], or null (no move date, or a step without a typical lead time). */
    fun startBy(task: MoveTask, moveIso: String?): Long? {
        val months = leadMonths[task.id] ?: return null
        val move = moveIso?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() } ?: return null
        return move.minusMonths(months).toEpochDay()
    }

    /** "start by Mar 2028", "start now" once that date has come; null if it doesn't apply. */
    fun label(task: MoveTask, moveIso: String?, day: Long = today()): String? {
        if (task.done) return null
        val d = startBy(task, moveIso) ?: return null
        return if (d <= day) "start now" else "start by " + java.time.LocalDate.ofEpochDay(d).format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.US))
    }

    /** The next step to start (soonest start-by among open steps). */
    fun next(s: AppState, day: Long = today()): MoveTask? =
        s.tasks.filter { !it.done && startBy(it, s.moveDate) != null }.minByOrNull { startBy(it, s.moveDate)!! }
}
