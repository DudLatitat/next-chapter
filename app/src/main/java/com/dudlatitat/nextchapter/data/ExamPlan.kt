package com.dudlatitat.nextchapter.data

/**
 * The A+ exam plan, worked out from the exam date: how many new cards a day see every card at
 * least a week before the exam (the last week is for review and practice tests), whether your
 * cards-a-day setting already does that, and how ready you are now. Pure, tested in ExamPlanTest.
 */
object ExamPlan {
    data class Plan(val daysLeft: Long, val newPerDay: Int, val onTrack: Boolean, val readyPct: Int, val unseen: Int) {
        /** One calm line for the A+ card. */
        val line: String get() = when {
            unseen == 0 -> "Every card seen ✓ · reviews and Sunday tests from here"
            onTrack -> "On track: every card seen a week early · tests on Sundays"
            else -> "$newPerDay new a day sees every card a week early"
        }
    }

    /** Review buffer before the exam: the last week is for review and practice tests only. */
    const val BUFFER_DAYS = 7

    fun of(s: AppState, day: Long = today(), total: Int = APlus.core1.size): Plan? {
        val exam = s.aplusExam?.let { runCatching { java.time.LocalDate.parse(it).toEpochDay() }.getOrNull() } ?: return null
        val left = exam - day
        if (left < 0) return null
        val seen = s.aplus.count { it.firstSeen != null }
        val unseen = (total - seen).coerceAtLeast(0)
        val days = (left - BUFFER_DAYS).coerceAtLeast(1)
        val need = if (unseen == 0) 0 else ((unseen + days - 1) / days).toInt().coerceIn(1, 30)
        val known = s.aplus.count { it.interval >= 7 }
        return Plan(left, need, need <= s.aplusPerDay, if (total == 0) 0 else known * 100 / total, unseen)
    }
}
