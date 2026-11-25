package com.dudlatitat.nextchapter.data

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Today as one line from wake to wind-down: work, the drive, home, the call, wind-down, with her
 * sleep and work hours shown underneath in your time (so the gap is something you can see; it
 * moves by an hour twice a year with daylight saving). Pure, tested in DayLineTest.
 */
object DayLine {
    enum class Kind { WORK, HOME, CALL, WIND_DOWN, HER_SLEEP, HER_WORK }
    data class Block(val from: Int, val to: Int, val kind: Kind)

    /** Your blocks (minutes after your midnight). Work defaults to 9–5 on weekdays unless days off. */
    fun mine(s: AppState, day: Long): List<Block> {
        val date = LocalDate.ofEpochDay(day)
        val weekday = date.dayOfWeek.value <= 5 && Routine.dayOff(s.daysOff, day) == null
        val start = Places.workStart(s.arrivals, day) ?: 9 * 60
        val end = Places.workEnd(s.arrivals, day) ?: 17 * 60
        val home = Places.homeTime(s, day)
        return buildList {
            if (weekday) add(Block(start, end, Kind.WORK))
            add(Block(if (weekday) maxOf(home, end) else s.wakeMinutes + 60, NowPlan.CALL_START, Kind.HOME))
            add(Block(NowPlan.CALL_START, NowPlan.CALL_END, Kind.CALL))
            add(Block(NowPlan.CALL_END, s.windDownMinutes, Kind.HOME))
            add(Block(s.windDownMinutes, minOf(24 * 60, s.windDownMinutes + 60), Kind.WIND_DOWN))
        }.filter { it.to > it.from }
    }

    /**
     * Her day in your clock: asleep 23:00–07:00 and at work 9:00–18:00 her time (typical, not
     * asked of her), converted to minutes on your [day] in [mine] zone. Blocks that cross your
     * midnight are split.
     */
    fun hers(day: Long, mine: ZoneId, her: ZoneId = Life.HER_ZONE): List<Block> {
        fun herSpan(fromH: Int, toH: Int, kind: Kind): List<Block> {
            val out = mutableListOf<Block>()
            // Her blocks on her dates around yours, mapped into your day.
            for (d in -1L..1L) {
                val herDate = LocalDate.ofEpochDay(day + d)
                val a = ZonedDateTime.of(herDate.atTime(fromH % 24, 0), her)
                val b = ZonedDateTime.of(herDate.atTime(toH % 24, 0), her).let { if (toH <= fromH) it.plusDays(1) else it }
                val dayStart = LocalDate.ofEpochDay(day).atStartOfDay(mine)
                val from = java.time.Duration.between(dayStart, a.withZoneSameInstant(mine)).toMinutes().toInt()
                val to = java.time.Duration.between(dayStart, b.withZoneSameInstant(mine)).toMinutes().toInt()
                val f = from.coerceIn(0, 1440); val t = to.coerceIn(0, 1440)
                if (t > f) out += Block(f, t, kind)
            }
            return out
        }
        return (herSpan(23, 7, Kind.HER_SLEEP) + herSpan(9, 18, Kind.HER_WORK)).sortedBy { it.from }
    }
}
