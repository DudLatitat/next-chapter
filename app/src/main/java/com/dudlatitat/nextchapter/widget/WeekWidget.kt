package com.dudlatitat.nextchapter.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.CheckIn
import com.dudlatitat.nextchapter.data.Routine
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import java.time.LocalDate

/**
 * Home-screen widget for the weekly routine: Monday → Sunday as dots (green done, gold minimum,
 * grey rest, today ringed) and tonight's job. Tap to open Today. Redrawn whenever the app's data
 * changes, and every 30 minutes so "today" moves on after midnight.
 */
class WeekWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val state = Store.get(context).state.value
        ids.forEach { manager.updateAppWidget(it, views(context, state)) }
    }

    companion object {
        private val dots = intArrayOf(R.id.dot0, R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4, R.id.dot5, R.id.dot6)
        private val days = intArrayOf(R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6)

        fun refresh(context: Context, state: AppState) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WeekWidget::class.java))
            if (ids.isEmpty()) return
            val v = views(context, state)
            ids.forEach { manager.updateAppWidget(it, v) }
        }

        private fun views(context: Context, state: AppState): RemoteViews {
            val v = RemoteViews(context.packageName, R.layout.widget_week)
            val now = LocalDate.now()
            val todayIndex = now.dayOfWeek.value - 1
            Routine.week(state.routineChecks, now).forEachIndexed { i, c ->
                val bg = when (c) {
                    CheckIn.DONE -> R.drawable.widget_dot_done
                    CheckIn.MINIMUM -> R.drawable.widget_dot_min
                    CheckIn.SKIPPED -> R.drawable.widget_dot_skip
                    null -> if (i == todayIndex) R.drawable.widget_dot_today else R.drawable.widget_dot_empty
                }
                v.setInt(dots[i], "setBackgroundResource", bg)
                v.setTextViewText(dots[i], when (c) { CheckIn.DONE -> "✓"; CheckIn.MINIMUM -> "½"; else -> "" })
                v.setTextColor(days[i], if (i == todayIndex) 0xFFFF8FB1.toInt() else 0xFFA9B1D6.toInt())
            }
            val streak = Routine.streak(state.routineChecks, today())
            v.setTextViewText(R.id.streak, if (streak > 0) "🔥 $streak" else "")
            val job = Routine.jobOn(state, now)
            v.setTextViewText(R.id.tonight, "Tonight: " + Routine.text(job.task, state.herName))
            v.setTextViewText(
                R.id.status,
                when (Routine.checkFor(state.routineChecks, today())) {
                    CheckIn.DONE -> "Done ✓ 很好!"
                    CheckIn.MINIMUM -> "The minimum counts. 加油!"
                    CheckIn.SKIPPED -> "Rest is okay. 明天见."
                    null -> "First step: " + Routine.text(job.firstStep, state.herName)
                },
            )
            val open = PendingIntent.getActivity(
                context, 8, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_TODAY, true),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            v.setOnClickPendingIntent(R.id.root, open)
            return v
        }
    }
}
