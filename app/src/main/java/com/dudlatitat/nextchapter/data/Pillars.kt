package com.dudlatitat.nextchapter.data

/**
 * The six pillars of the next three years, each with one forward-looking number for Today
 * (never a deficit, never a zero that hasn't started). Pure, tested in PillarsTest.
 */
object Pillars {
    enum class Id { TALK, CAREER, MONEY, HEALTH, FAITH, US }
    data class Pillar(val id: Id, val glyph: String, val name: String, val value: String)

    fun all(s: AppState, day: Long = today()): List<Pillar> {
        val ladder = Ready.family(s)
        val known = s.aplus.count { it.interval >= 7 }
        val aplusPct = if (APlus.core1.isEmpty()) 0 else known * 100 / APlus.core1.size
        val monthly = s.budget.sumOf { it.rmb }
        val runway = if (monthly > 0 && s.saved > 0) Money.monthsCovered(s.saved, s.rate, monthly) else null
        val nights = Routine.week(s.routineChecks, java.time.LocalDate.ofEpochDay(day)).count { Routine.isWin(it) }
        val prayed = s.prayedDays.count { it in (day - 6)..day }
        val trip = daysUntil(s.nextTripDate())?.takeIf { it >= 0 }
        // Short values (they sit in a three-across grid); the name line says what they mean.
        return listOf(
            if (ladder.next == null) Pillar(Id.TALK, "话", "Family ✓", "ready") else Pillar(Id.TALK, "话", "family rung", "${ladder.climbed + 1}/${ladder.rungs.size}"),
            if (known > 0) Pillar(Id.CAREER, "业", "A+ ready", "$aplusPct%") else Pillar(Id.CAREER, "业", "A+", "start"),
            when {
                runway != null -> Pillar(Id.MONEY, "钱", "runway", "${"%.0f".format(java.util.Locale.US, runway)} mo")
                s.savingsGoal > 0 && s.saved > 0 -> Pillar(Id.MONEY, "钱", "saved", "${(s.savingsProgress() * 100).toInt()}%")
                else -> Pillar(Id.MONEY, "钱", "savings", "set goal")
            },
            if (nights > 0) Pillar(Id.HEALTH, "身", "nights this wk", "$nights") else Pillar(Id.HEALTH, "身", "this week", "fresh"),
            if (prayed > 0) Pillar(Id.FAITH, "祷", "prayer days", "$prayed/7") else Pillar(Id.FAITH, "祷", "prayer", "today?"),
            Pillar(Id.US, "心", "next trip", trip?.let { "${it}d" } ?: "plan"),
        )
    }
}
