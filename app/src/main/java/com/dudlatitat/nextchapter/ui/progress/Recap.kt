package com.dudlatitat.nextchapter.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Stats
import com.dudlatitat.nextchapter.data.moveReady
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * This week next to last week: days practiced, reviews, minutes, new phrases, tone accuracy.
 * [lastWeek] shows the week that just ended (for the Monday recap on Today).
 */
@Composable
fun WeeklyRecap(state: AppState, lastWeek: Boolean = false, modifier: Modifier = Modifier) {
    val thisStart = Stats.weekStart()
    val start = if (lastWeek) thisStart.minusWeeks(1) else thisStart
    val now = Stats.week(state, start)
    val before = Stats.week(state, start.minusWeeks(1))
    val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    GlassCard(modifier, highlight = true) {
        Text(
            (if (lastWeek) "Last week" else "This week") + " · ${start.format(fmt)}–${start.plusDays(6).format(fmt)}",
            style = MaterialTheme.typography.labelMedium, color = Night.muted,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RecapStat("${now.daysPracticed}/7", "days", now.daysPracticed - before.daysPracticed, Night.sun)
            RecapStat("${now.reviews}", "reviews", now.reviews - before.reviews, Night.jade)
            RecapStat("${now.minutes}", "minutes", now.minutes - before.minutes, Night.violet)
            RecapStat("${now.newPhrases}", "new", now.newPhrases - before.newPhrases, Night.rose)
        }
        Spacer(Modifier.height(10.dp))
        val tone = now.toneAccuracy
        val prevTone = before.toneAccuracy
        Text(
            when {
                tone == null -> "Use the tone trainer or 🎤 Say it to track your tones."
                prevTone == null -> "Tones right: ${(tone * 100).toInt()}%"
                else -> "Tones right: ${(tone * 100).toInt()}% (" + signed(((tone - prevTone) * 100).toInt()) + " pts vs the week before)"
            },
            style = MaterialTheme.typography.bodyMedium, color = Night.ink,
        )
        Text("Move-ready: ${(state.moveReady() * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        if (lastWeek && LocalDate.now().dayOfWeek.value <= 1) Text(message(now.daysPracticed), style = MaterialTheme.typography.bodySmall, color = Night.sun)
    }
}

private fun signed(n: Int) = if (n > 0) "+$n" else "$n"

private fun message(days: Int) = when {
    days >= 6 -> "Amazing week. 太厉害了!"
    days >= 4 -> "Solid week. Keep it rolling."
    days >= 1 -> "A start. Try for a few more days this week."
    else -> "New week, fresh start. 加油!"
}

@Composable
private fun RecapStat(value: String, label: String, change: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
        if (change != 0) Text(signed(change), style = MaterialTheme.typography.labelSmall, color = if (change > 0) Night.jade else Night.coral)
    }
}
