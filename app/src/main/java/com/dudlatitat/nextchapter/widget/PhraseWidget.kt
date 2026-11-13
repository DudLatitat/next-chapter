package com.dudlatitat.nextchapter.widget

import com.dudlatitat.nextchapter.data.nextTripDate
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.phraseOfTheDay

/** Home-screen widget: move countdown, today's phrase, reviews due and the streak. */
class PhraseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val state = Store.get(context).state.value
        ids.forEach { manager.updateAppWidget(it, views(context, state)) }
    }

    companion object {
        /** Redraws every placed widget (called whenever the app's data changes). */
        fun refresh(context: Context, state: AppState) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PhraseWidget::class.java))
            if (ids.isEmpty()) return
            val v = views(context, state)
            ids.forEach { manager.updateAppWidget(it, v) }
        }

        private fun views(context: Context, state: AppState): RemoteViews {
            val v = RemoteViews(context.packageName, R.layout.widget_phrase)
            val move = daysUntil(state.moveDate)
            val trip = daysUntil(state.nextTripDate())
            v.setTextViewText(
                R.id.countdown,
                when {
                    trip != null && trip >= 0 && (move == null || trip < move) -> "成都 · $trip days to the trip"
                    move != null && move >= 0 -> "搬家 · $move days to the move"
                    else -> "新篇章 · Next Chapter"
                },
            )
            val card = state.phraseOfTheDay()
            v.setTextViewText(R.id.hanzi, card?.hanzi ?: "你好")
            v.setTextViewText(R.id.pinyin, card?.pinyin ?: "nǐ hǎo")
            v.setTextViewText(R.id.english, card?.english ?: "Hello")
            val due = Srs.queue(state).size
            val streak = com.dudlatitat.nextchapter.data.Stats.streak(state)
            v.setTextViewText(R.id.status, buildString {
                append(if (due > 0) "$due to review" else "All caught up ✓")
                if (streak > 0) append("  ·  🔥 $streak")
            })
            v.setViewVisibility(R.id.status, View.VISIBLE)
            val open = PendingIntent.getActivity(
                // Its own request code: PendingIntents that differ only in extras overwrite each other.
                context, 12, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_STUDY, due > 0),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            v.setOnClickPendingIntent(R.id.root, open)
            return v
        }
    }
}
