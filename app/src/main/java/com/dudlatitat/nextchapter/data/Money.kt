package com.dudlatitat.nextchapter.data

import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Dollars ⇄ yuan and the savings pace: pure math, tested in MoneyTest. */
object Money {
    /** Used when the saved rate is missing or nonsense, so a bad download can't divide by zero. */
    const val FALLBACK_RATE = 7.0

    /** A believable USD → CNY rate (it has stayed between about 6 and 8 for decades). */
    fun sane(rate: Double): Boolean = rate in 4.0..12.0

    private fun use(rate: Double) = if (sane(rate)) rate else FALLBACK_RATE

    fun toYuan(usd: Double, rate: Double): Long = (usd * use(rate)).roundToLong()
    fun toDollars(rmb: Double, rate: Double): Double = rmb / use(rate)
    fun toWholeDollars(rmb: Double, rate: Double): Int = toDollars(rmb, rate).roundToInt()

    /** How many months of a monthly budget (in yuan) the savings (in dollars) would cover. */
    fun monthsCovered(savedUsd: Int, rate: Double, monthlyRmb: Int): Double =
        if (monthlyRmb <= 0) 0.0 else savedUsd * use(rate) / monthlyRmb

    /**
     * Dollars to put aside each month to reach the goal in [daysLeft] days, rounded *up* so
     * following it actually gets you there; null when there's no goal, it's reached, or the date passed.
     */
    fun savePerMonth(goal: Int, saved: Int, daysLeft: Long): Int? {
        val left = goal - saved
        if (goal <= 0 || left <= 0 || daysLeft <= 0) return null
        val months = (daysLeft / 30.4375).coerceAtLeast(1.0)
        return ceil(left / months).toInt()
    }

    /** The CNY rate from open.er-api.com's reply, or null if it's missing or not believable. */
    fun parseCny(json: String): Double? =
        Regex("\"CNY\"\\s*:\\s*([0-9.]+)").find(json)?.groupValues?.get(1)?.toDoubleOrNull()?.takeIf(::sane)
}
