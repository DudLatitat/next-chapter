package com.dudlatitat.nextchapter.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Decks
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.moveReady
import com.dudlatitat.nextchapter.data.savingsProgress
import com.dudlatitat.nextchapter.data.taskProgress
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.ProgressRing
import com.dudlatitat.nextchapter.ui.components.ScreenTitle
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.components.Stat
import com.dudlatitat.nextchapter.ui.theme.Night

@Composable
fun ProgressScreen(state: AppState) {
    val known = state.cards.count { it.isKnown }
    val learning = state.cards.count { !it.isNew && !it.isKnown }
    val streak = Srs.streak(state.studyDays)
    val best = bestStreak(state.studyDays)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenTitle("Progress", "进步 · every day counts")
        WeeklyRecap(state)

        GlassCard(highlight = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("$streak", "day streak", Night.sun)
                Stat("$known", "known", Night.jade)
                Stat("$learning", "learning", Night.violet)
                Stat("$best", "best streak", Night.rose)
            }
        }

        SectionLabel("Last 12 weeks")
        GlassCard { Heatmap(state.studyDays) }

        SectionLabel("Move-ready")
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(state.moveReady(), 96.dp, Night.sun, stroke = 10.dp) {
                    Text("${(state.moveReady() * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                }
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Checklist", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Bar(state.taskProgress(), Night.sun)
                    Text("Savings", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Bar(state.savingsProgress(), Night.jade)
                }
            }
        }

        SectionLabel("Milestones")
        MilestoneGrid(state)

        SectionLabel("By deck")
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Decks.forEach { deck ->
                    val cards = state.cards.filter { it.deck == deck.id }
                    if (cards.isEmpty()) return@forEach
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(deck.title, style = MaterialTheme.typography.bodyMedium, color = Night.ink, modifier = Modifier.width(130.dp))
                        Bar(cards.count { it.isKnown }.toFloat() / cards.size, Night.jade, Modifier.weight(1f))
                        Text("  ${cards.count { it.isKnown }}/${cards.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** 12 columns of weeks, a square for each day: lit when you studied. */
@Composable
private fun Heatmap(days: List<Long>) {
    val set = days.toSet()
    val t = today()
    // Weeks start on Monday; the last column is this week.
    val dow = java.time.LocalDate.ofEpochDay(t).dayOfWeek.value - 1
    val firstDay = t - dow - 7 * 11
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(12) { w ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { d ->
                    val day = firstDay + w * 7 + d
                    val color = when {
                        day > t -> Color.Transparent
                        day in set -> Night.jade
                        else -> Color.White.copy(alpha = 0.07f)
                    }
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(3.dp)).background(color))
                }
            }
        }
    }
}

private fun bestStreak(days: List<Long>): Int {
    var best = 0
    var run = 0
    var prev: Long? = null
    days.distinct().sorted().forEach { d ->
        run = if (prev != null && d == prev!! + 1) run + 1 else 1
        if (run > best) best = run
        prev = d
    }
    return best
}

@Composable
private fun MilestoneGrid(state: AppState) {
    val all = com.dudlatitat.nextchapter.data.Milestones.all(state)
    GlassCard {
        Text("${all.count { it.done }} of ${all.size} reached", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        all.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { m ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(m.emoji, style = MaterialTheme.typography.titleLarge, color = if (m.done) Color.Unspecified else Color.White.copy(alpha = 0.25f), modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text(m.title, style = MaterialTheme.typography.labelLarge, color = if (m.done) Night.ink else Night.muted)
                            Text(m.detail, style = MaterialTheme.typography.labelSmall, color = Night.muted)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
