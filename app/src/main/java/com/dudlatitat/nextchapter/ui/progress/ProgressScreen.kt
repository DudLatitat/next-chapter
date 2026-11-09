package com.dudlatitat.nextchapter.ui.progress

import androidx.compose.foundation.clickable
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

/**
 * Your Chinese progress, at the bottom of the Chinese tab: this week, streaks, the 12-week grid
 * and each deck. (It used to be its own Progress tab; now it sits next to what it measures.)
 */
@Composable
fun ChineseProgress(state: AppState, onOpenDeck: (String) -> Unit = {}) {
    val known = state.cards.count { it.isKnown }
    val learning = state.cards.count { !it.isNew && !it.isKnown }
    val streak = com.dudlatitat.nextchapter.data.Stats.streak(state)
    val best = bestStreak(com.dudlatitat.nextchapter.data.Stats.practiceDays(state))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // One card for the week and the totals (they used to be two cards saying similar things).
        GlassCard(highlight = true) {
            WeeklyRecap(state, bare = true)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("$streak", "day streak", Night.sun)
                Stat("$best", "best", Night.rose)
                Stat("$learning", "learning", Night.violet)
                Stat("$known", "known", Night.jade)
            }
        }

        val practiced = com.dudlatitat.nextchapter.data.Stats.practiceDays(state)
        com.dudlatitat.nextchapter.ui.components.Collapsible("progress.heatmap", "Last 12 weeks", summary = "${practiced.count { it > com.dudlatitat.nextchapter.data.today() - 84 }} days practiced", defaultOpen = true) {
            Heatmap(practiced)
        }

    }
}

/** Wins on the Life tab: milestones and the lines you wrote each Sunday. */
@Composable
fun LifeWins(state: AppState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val all = com.dudlatitat.nextchapter.data.Milestones.all(state)
        com.dudlatitat.nextchapter.ui.components.Collapsible("progress.milestones", "Milestones · ${all.count { it.done }} of ${all.size}", summary = all.firstOrNull { !it.done }?.let { "Next: ${it.emoji} ${it.title}" }) {
            MilestoneGrid(state)
        }

        if (state.weekNotes.isNotEmpty()) {
            com.dudlatitat.nextchapter.ui.components.Collapsible("progress.weeks", "Your weeks · ${state.weekNotes.size}", summary = state.weekNotes.entries.maxByOrNull { it.key }?.value) {
                com.dudlatitat.nextchapter.ui.today.WeekNotes(state, bare = true)
            }
        }

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
    Column {
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
