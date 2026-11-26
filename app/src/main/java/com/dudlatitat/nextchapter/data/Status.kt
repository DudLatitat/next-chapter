package com.dudlatitat.nextchapter.data

/**
 * The mono status line under each tab's title: up to three short facts, always forward-looking
 * (never a deficit, never a zero that hasn't started). Pure, tested in StatusTest.
 */
object Status {
    fun chinese(s: AppState, day: Long = today()): List<String> = listOfNotNull(
        Stats.streak(s, day).takeIf { it > 0 }?.let { "🔥$it" },
        Ready.family(s).let { l -> if (l.next == null) "family ladder ✓" else "family ladder ${l.climbed + 1}/${l.rungs.size}" },
        s.realUses.map { it.id }.toSet().size.takeIf { it > 0 }?.let { "★$it for real" },
    )

    fun life(s: AppState, day: Long = today()): List<String> {
        val date = java.time.LocalDate.ofEpochDay(day)
        val nights = Routine.week(s.routineChecks, date).count { Routine.isWin(it) }
        val exam = daysUntil(APlus.examDate(s))?.takeIf { it >= 0 }
        val prayed = s.prayedDays.count { it in (day - 6)..day }
        return listOfNotNull(
            if (nights > 0) "🌙 $nights this week" else null,
            exam?.let { if (APlus.core(s) == 2) "Core 2 exam ${it}d" else "A+ exam ${it}d" },
            if (prayed > 0) "祷 $prayed/7" else null,
        )
    }

    fun move(s: AppState, day: Long = today()): List<String> {
        val trip = daysUntil(s.nextTripDate())?.takeIf { it >= 0 }
        val move = daysUntil(s.moveDate)?.takeIf { it >= 0 }
        val monthly = s.budget.sumOf { it.rmb }
        val runway = if (monthly > 0 && s.saved > 0) Money.monthsCovered(s.saved, s.rate, monthly) else null
        return listOfNotNull(
            trip?.let { "✈ ${it}d" },
            move?.let { "成都 ${friendlySpan(it)}" },
            runway?.let { "${"%.0f".format(java.util.Locale.US, it)}mo runway" },
        )
    }
}
