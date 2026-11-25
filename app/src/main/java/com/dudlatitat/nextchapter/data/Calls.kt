package com.dudlatitat.nextchapter.data

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Good times to talk this week: when you're free (weekday evenings after work, weekend days)
 * overlaps with when she's usually awake (8 am–11 pm her time). Worked out with real time zones,
 * so the daylight-saving change moves the windows on its own. Nothing asked of her. Pure, tested in CallsTest.
 */
object Calls {
    data class Window(val day: Long, val fromMin: Int, val toMin: Int, val herFrom: ZonedDateTime) {
        val minutes: Int get() = toMin - fromMin
    }

    /** Your free time per day (minutes after your midnight). */
    fun myFree(date: LocalDate): IntRange = if (date.dayOfWeek.value <= 5) (19 * 60)..(22 * 60) else (9 * 60)..(22 * 60)

    /** Her usual waking hours (her clock). */
    val HER_AWAKE = (8 * 60)..(23 * 60)

    fun week(start: Long, mine: ZoneId, her: ZoneId = Life.HER_ZONE, days: Int = 7): List<Window> = (0 until days).mapNotNull { i ->
        val date = LocalDate.ofEpochDay(start + i)
        val free = myFree(date)
        // Walk your free time in 15-minute steps and keep the longest stretch she's awake for.
        var best: Pair<Int, Int>? = null
        var runStart: Int? = null
        var m = free.first
        while (m <= free.last) {
            val herT = date.atStartOfDay(mine).plusMinutes(m.toLong()).withZoneSameInstant(her)
            val awake = (herT.hour * 60 + herT.minute) in HER_AWAKE
            if (awake && runStart == null) runStart = m
            if ((!awake || m == free.last) && runStart != null) {
                val end = if (awake) m else m
                if (best == null || end - runStart > best.second - best.first) best = runStart to end
                runStart = null
            }
            m += 15
        }
        best?.takeIf { it.second - it.first >= 30 }?.let { (f, t) ->
            Window(start + i, f, t, date.atStartOfDay(mine).plusMinutes(f.toLong()).withZoneSameInstant(her))
        }
    }

    private val hm = java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
    private val dayName = java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.US)

    /** "Sat 9:00 AM–10:00 PM" (yours) and "her Sun 1:00 AM…" isn't useful: show her start time and weekday. */
    fun describe(w: Window, mine: ZoneId): Pair<String, String> {
        val date = LocalDate.ofEpochDay(w.day)
        val a = date.atStartOfDay(mine).plusMinutes(w.fromMin.toLong())
        val b = date.atStartOfDay(mine).plusMinutes(w.toMin.toLong())
        return "${a.format(dayName)} ${a.format(hm)}–${b.format(hm)}" to "her ${w.herFrom.format(dayName)} ${w.herFrom.format(hm)}"
    }
}
