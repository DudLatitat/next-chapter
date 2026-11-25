package com.dudlatitat.nextchapter.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.DayPlan
import com.dudlatitat.nextchapter.data.Ready
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * 今日三件 · Today's three, as three rings: phrases, one game, one life thing. One streak, the
 * prayer dot and your week of evenings sit under them. After a break it says "Welcome back".
 * This replaced both the old Today's three list and the four rings (they said the same thing).
 */
@Composable
fun TodayThree(
    state: AppState, calm: Boolean, onItem: (DayPlan.Item) -> Unit, onPrayer: () -> Unit, onWeek: () -> Unit,
) {
    val plan = DayPlan.today(state)
    val streak = com.dudlatitat.nextchapter.data.Stats.streak(state)
    val early = com.dudlatitat.nextchapter.data.Srs.streak(state.wakeDays)
    val titleColor by animateColorAsState(if (plan.finished) Night.jade else Night.ink, label = "threeTitle")
    GlassCard(highlight = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (plan.finished) "今天完成了 · Done for today ✓" else "今日三件 · Today's three", style = MaterialTheme.typography.titleMedium, color = titleColor)
                val sub = when {
                    plan.finished -> "Anything more tonight is a bonus."
                    plan.welcomeBack > 0 -> "Welcome back 💛 No catching up: the ${DayPlan.COMEBACK_CAP} that matter most, the rest return over the next few days."
                    else -> "${plan.doneCount} of ${plan.items.size} done"
                }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = if (plan.welcomeBack > 0 && !plan.finished) Night.rose else Night.muted)
            }
            // The one streak (practice days; one rest day a week allowed).
            if (streak > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥 $streak", style = MaterialTheme.typography.headlineSmall, color = Night.sun)
                Text(if (streak == 1) "day" else "days", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Only the first unfinished ring breathes: one clear "next", not three blinking things.
            val next = plan.items.indexOfFirst { !it.done }
            plan.items.forEachIndexed { i, item ->
                PlanRing(item, breathe = !calm && i == next, modifier = Modifier.weight(1f)) { onItem(item) }
            }
        }
        Spacer(Modifier.height(12.dp))
        // Small things under the rings: prayer, early starts, and the week of evenings.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val prayed = com.dudlatitat.nextchapter.data.today() in state.prayedDays
            Pill(if (prayed) "🙏 Prayed ✓" else "🙏 Pray", if (prayed) Night.jade else Night.sun, onClick = onPrayer)
            if (early > 0) Pill("☀️ $early early", Night.jade)
        }
        Spacer(Modifier.height(10.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onWeek).padding(vertical = 4.dp)) {
            Text("This week ›", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Spacer(Modifier.height(6.dp))
            com.dudlatitat.nextchapter.ui.life.WeekDots(state)
        }
    }
}

/** One of the three: a ring that fills, its label, and what it is (e.g. "Talk"). */
@Composable
private fun PlanRing(item: DayPlan.Item, breathe: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = when (item.kind) { DayPlan.Kind.REVIEW -> Night.sun; DayPlan.Kind.PLAY -> Night.rose; DayPlan.Kind.LIFE -> Night.violet }
    val ringColor by animateColorAsState(if (item.done) Night.jade else color, label = "ringColor")
    // A soft glow swells behind the next ring, slow enough to feel calm. Read only while drawing.
    val breath: androidx.compose.runtime.State<Float> = if (breathe) {
        androidx.compose.animation.core.rememberInfiniteTransition(label = "breathe").animateFloat(
            0f, 1f,
            androidx.compose.animation.core.infiniteRepeatable(tween(1600, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "breath",
        )
    } else remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            if (breathe) androidx.compose.foundation.Canvas(Modifier.size(72.dp)) {
                drawCircle(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(color.copy(alpha = 0.15f + 0.35f * breath.value), color.copy(alpha = 0f)),
                        center = center, radius = size.minDimension / 2,
                    ),
                    radius = size.minDimension / 2 * (0.8f + 0.2f * breath.value),
                )
            }
            com.dudlatitat.nextchapter.ui.components.ProgressRing(item.progress, 60.dp, ringColor, stroke = 6.dp) {
                Text(
                    if (item.done) "✓" else when (item.kind) { DayPlan.Kind.REVIEW -> "字"; DayPlan.Kind.PLAY -> "🎲"; DayPlan.Kind.LIFE -> if (item.tool == "aplus") "A+" else "🌙" },
                    style = MaterialTheme.typography.titleMedium, color = if (item.done) Night.jade else Night.ink,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(item.ring, style = MaterialTheme.typography.labelLarge, color = Night.ink)
        val sub = when (item.kind) {
            DayPlan.Kind.PLAY -> DayPlan.games[item.tool]?.substringBefore(" ·") ?: item.detail
            DayPlan.Kind.LIFE -> if (item.tool == "aplus") item.detail else item.title.removePrefix("Tonight: ")
            DayPlan.Kind.REVIEW -> item.detail.substringBefore(" ·")
        }
        Text(sub, style = MaterialTheme.typography.labelSmall, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

/**
 * 准备好了吗 · Ready for her family: a ladder of real situations, climbed from what you've
 * already done. Shows the next rung and exactly what's missing; tap a piece to go do it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadyLadder(state: AppState, onPiece: (String) -> Unit) {
    val ladder = Ready.family(state)
    val next = ladder.next
    com.dudlatitat.nextchapter.ui.components.Collapsible(
        "learn.ready", "准备好了吗 · Ready for her family",
        summary = if (next == null) "Every rung climbed ✓" else "${ladder.climbed}/${ladder.rungs.size} · next: ${next.title}",
    ) {
        // The ladder: one segment per rung, filled as you climb.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ladder.rungs.forEach { r ->
                val c: Color = when { r.done -> Night.jade; r === next -> Night.sun; else -> Night.outline }
                Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)).background(c))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (next == null) {
            Text("You can get through a whole visit in Chinese. 了不起！", style = MaterialTheme.typography.bodyMedium, color = Night.jade)
            return@Collapsible
        }
        Text("Rung ${ladder.rungs.indexOf(next) + 1} · ${next.hanzi} ${next.title}", style = MaterialTheme.typography.titleSmall, color = Night.sun)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            next.pieces.forEach { p ->
                val tool = p.tool
                Pill((if (p.done) "✓ " else "") + p.label, if (p.done) Night.jade else Night.rose, onClick = if (!p.done && tool != null) ({ onPiece(tool) }) else null)
            }
        }
        if (ladder.climbed > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Climbed: " + ladder.rungs.take(ladder.climbed).joinToString(" · ") { it.title },
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("Ready for the trip: ${ladder.percent}%", style = MaterialTheme.typography.labelMedium, color = Night.muted)
    }
}
