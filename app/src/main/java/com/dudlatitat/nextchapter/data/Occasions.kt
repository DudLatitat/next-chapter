package com.dudlatitat.nextchapter.data

import java.time.LocalDate
import java.time.MonthDay

/**
 * Days worth remembering, with the greeting to say: Chinese festivals (lunar dates, from the
 * checked [Table]) and a few on the Western calendar. Shown on Today in the three weeks before.
 * Pure, tested in OccasionsTest.
 */
object Occasions {
    data class Occasion(val id: String, val name: String, val zh: String, val greeting: String, val pinyin: String, val english: String)
    data class Upcoming(val occasion: Occasion, val date: LocalDate, val daysAway: Long)

    /** Lunar month/day → the Gregorian date in a given Chinese year (the year it starts in). */
    fun interface Lunar { fun toDate(gregorianYear: Int, lunarMonth: Int, lunarDay: Int): LocalDate? }

    private class L(val o: Occasion, val month: Int, val day: Int)
    private class G(val o: Occasion, val md: MonthDay)

    private val lunar = listOf(
        L(Occasion("spring", "Spring Festival", "春节", "新年快乐", "xīnnián kuàilè", "Happy New Year"), 1, 1),
        L(Occasion("lantern", "Lantern Festival", "元宵节", "元宵节快乐", "yuánxiāo jié kuàilè", "Happy Lantern Festival"), 1, 15),
        L(Occasion("dragon", "Dragon Boat Festival", "端午节", "端午安康", "duānwǔ ānkāng", "Wishing you a healthy Dragon Boat Festival"), 5, 5),
        L(Occasion("qixi", "Qixi (Chinese Valentine's)", "七夕", "七夕快乐", "qīxī kuàilè", "Happy Qixi"), 7, 7),
        L(Occasion("midautumn", "Mid-Autumn Festival", "中秋节", "中秋快乐", "zhōngqiū kuàilè", "Happy Mid-Autumn Festival"), 8, 15),
    )
    private val gregorian = listOf(
        G(Occasion("valentine", "Valentine's Day", "情人节", "情人节快乐", "qíngrénjié kuàilè", "Happy Valentine's Day"), MonthDay.of(2, 14)),
        G(Occasion("520", "520 (\"I love you\" day)", "五二零", "我爱你", "wǒ ài nǐ", "I love you"), MonthDay.of(5, 20)),
        G(Occasion("christmas", "Christmas", "圣诞节", "圣诞快乐", "shèngdàn kuàilè", "Merry Christmas"), MonthDay.of(12, 25)),
    )

    /** Occasions in the next [within] days, soonest first. */
    fun upcoming(today: LocalDate, lunarCal: Lunar?, within: Long = 21): List<Upcoming> {
        val out = mutableListOf<Upcoming>()
        for (y in today.year..today.year + 1) {
            gregorian.forEach { g -> out += Upcoming(g.o, g.md.atYear(y), 0) }
            if (lunarCal != null) lunar.forEach { l -> lunarCal.toDate(y, l.month, l.day)?.let { out += Upcoming(l.o, it, 0) } }
        }
        return out.map { it.copy(daysAway = it.date.toEpochDay() - today.toEpochDay()) }
            .filter { it.daysAway in 0..within }
            .distinctBy { it.occasion.id }
            .sortedBy { it.daysAway }
    }

    /**
     * Festival dates, checked against published timetables (travelchinaguide.com's festival
     * calendar, calendardate.com; Dragon Boat 2028 is May 28 per the sources that agree).
     * The phone's ICU calendar puts Spring Festival 2027 a day late (the new moon falls minutes
     * before midnight), so the dates are listed rather than computed. Add years as they come.
     */
    object Table : Lunar {
        private val dates: Map<Triple<Int, Int, Int>, LocalDate> = buildMap {
            fun y(year: Int, spring: String, lantern: String, dragon: String, qixi: String, mid: String) {
                put(Triple(year, 1, 1), LocalDate.parse("$year-$spring")); put(Triple(year, 1, 15), LocalDate.parse("$year-$lantern"))
                put(Triple(year, 5, 5), LocalDate.parse("$year-$dragon")); put(Triple(year, 7, 7), LocalDate.parse("$year-$qixi"))
                put(Triple(year, 8, 15), LocalDate.parse("$year-$mid"))
            }
            y(2026, "02-17", "03-03", "06-19", "08-19", "09-25")
            y(2027, "02-06", "02-20", "06-09", "08-08", "09-15")
            y(2028, "01-26", "02-09", "05-28", "08-26", "10-03")
            y(2029, "02-13", "02-27", "06-16", "08-16", "09-22")
        }
        override fun toDate(gregorianYear: Int, lunarMonth: Int, lunarDay: Int): LocalDate? = dates[Triple(gregorianYear, lunarMonth, lunarDay)]
    }
}
