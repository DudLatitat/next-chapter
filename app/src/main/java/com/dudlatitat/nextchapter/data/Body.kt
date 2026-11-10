package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/** One weigh-in (pounds), at most one per day. */
@Serializable
data class WeightEntry(val day: Long, val lb: Double)

/** Calories added on a day (each tap is one entry, so the last can be undone). */
@Serializable
data class CalorieAdd(val day: Long, val cal: Int)

/** Weight and calories, logged by hand on Today. Kept on this phone only. Pure, tested in BodyTest. */
object Body {
    fun latest(weights: List<WeightEntry>): WeightEntry? = weights.maxByOrNull { it.day }

    /** Sets (or replaces) a day's weight, keeping about the last two years. */
    fun withWeight(weights: List<WeightEntry>, day: Long, lb: Double): List<WeightEntry> =
        (weights.filterNot { it.day == day } + WeightEntry(day, lb)).filter { it.day > day - 730 }.sortedBy { it.day }

    /** Change since the weigh-in closest to a week before the latest one (null if there's none that old). */
    fun weekChange(weights: List<WeightEntry>): Double? {
        val now = latest(weights) ?: return null
        val before = weights.filter { it.day <= now.day - 7 }.maxByOrNull { it.day } ?: return null
        return now.lb - before.lb
    }

    fun caloriesOn(adds: List<CalorieAdd>, day: Long): Int = adds.filter { it.day == day }.sumOf { it.cal }

    fun addCalories(adds: List<CalorieAdd>, day: Long, cal: Int): List<CalorieAdd> =
        if (cal <= 0) adds else (adds + CalorieAdd(day, cal.coerceAtMost(5000))).filter { it.day > day - 120 }

    /** Takes back today's last add (a mistaken tap). */
    fun undoLast(adds: List<CalorieAdd>, day: Long): List<CalorieAdd> {
        val i = adds.indexOfLast { it.day == day }
        return if (i < 0) adds else adds.filterIndexed { j, _ -> j != i }
    }

    /** "182.4", "170" (one decimal, no ".0"). */
    fun lbs(v: Double): String {
        val r = (v * 10).roundToInt() / 10.0
        return if (r % 1.0 == 0.0) r.toInt().toString() else r.toString()
    }

    fun cal(n: Int): String = NumberFormat.getIntegerInstance(Locale.US).format(n)

    /** Today's pill: "⚖️ 182.4 → 170 lb · 🍽 1,450 / 2,000 cal". */
    fun pill(state: AppState, day: Long): String {
        val w = latest(state.weights)?.lb
        val weight = when {
            w == null -> "⚖️ Add weight"
            state.goalWeight != null -> "⚖️ ${lbs(w)} → ${lbs(state.goalWeight)} lb"
            else -> "⚖️ ${lbs(w)} lb"
        }
        val eaten = caloriesOn(state.calorieAdds, day)
        val food = if (state.calorieTarget > 0) "🍽 ${cal(eaten)} / ${cal(state.calorieTarget)} cal" else "🍽 ${cal(eaten)} cal"
        return "$weight · $food"
    }
}
