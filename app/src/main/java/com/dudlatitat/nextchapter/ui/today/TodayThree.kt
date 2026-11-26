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
import androidx.compose.foundation.layout.heightIn
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
import com.dudlatitat.nextchapter.ui.components.pressClick
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * 今日三件 · Today's three, as a quest card: three rows (phrases, one game, A+ or tonight), each
 * with a ring around a one-character badge, what it is, and a Go button on the next one. Prayer
 * is a quiet fourth row. The one 🔥 streak sits in the corner; "Welcome back" after a break.
 */
@Composable
fun TodayThree(
    state: AppState, calm: Boolean, onItem: (DayPlan.Item) -> Unit, onPrayer: () -> Unit, quiet: Boolean = false,
    @Suppress("UNUSED_PARAMETER") onWeek: () -> Unit = {},
) {
    val day = com.dudlatitat.nextchapter.data.today()
    val plan = remember(state, day) { DayPlan.today(state, day) }
    val streak = remember(state, day) { com.dudlatitat.nextchapter.data.Stats.streak(state, day) }
    val titleColor by animateColorAsState(if (plan.finished) Night.jade else Night.ink, label = "threeTitle")
    com.dudlatitat.nextchapter.ui.components.HeroCard(accent = if (plan.finished) Night.jade else Night.sun) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(if (plan.finished) "今天完成了" else "今日三件", style = MaterialTheme.typography.titleLarge, color = titleColor)
                    Spacer(Modifier.width(8.dp))
                    Text("${plan.doneCount}/${plan.items.size}", style = MaterialTheme.typography.titleMedium, color = Night.muted)
                }
                val sub = when {
                    plan.finished -> "Done for today. Anything more is a bonus."
                    plan.welcomeBack > 0 -> "Welcome back 💛 Just the ${DayPlan.COMEBACK_CAP} that matter most today."
                    else -> "Today's three"
                }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = if (plan.welcomeBack > 0 && !plan.finished) Night.rose else Night.muted)
            }
            if (streak > 0) com.dudlatitat.nextchapter.ui.components.StatTile("🔥$streak", if (streak == 1) "day" else "days", Night.sun)
        }
        Spacer(Modifier.height(10.dp))
        val next = plan.items.indexOfFirst { !it.done }
        // After wind-down: no Go buttons, nothing breathing. Just where the day ended up.
        val still = com.dudlatitat.nextchapter.ui.components.LocalReduceMotion.current
        plan.items.forEachIndexed { i, item -> QuestRow(item, isNext = !quiet && i == next, breathe = !calm && !quiet && !still && i == next) { onItem(item) } }
        // Prayer: a quiet fourth row (it isn't one of the three, but it's part of the day).
        val prayed = day in state.prayedDays
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).pressClick(onClick = onPrayer).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            com.dudlatitat.nextchapter.ui.components.IconBadge("祷", if (prayed) Night.jade else Night.violet, 44.dp, filled = prayed)
            Spacer(Modifier.width(14.dp))
            Text(if (prayed) "Prayed today ✓" else "Prayer for today", style = MaterialTheme.typography.bodyMedium, color = if (prayed) Night.jade else Night.muted, modifier = Modifier.weight(1f))
            if (!prayed) Text("🙏", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun badgeOf(item: DayPlan.Item) = when (item.kind) {
    DayPlan.Kind.REVIEW -> "字"
    DayPlan.Kind.PLAY -> "玩"
    DayPlan.Kind.LIFE -> if (item.tool == "aplus") "考" else "晚"
}

/** One quest: a ring around its badge, what it is, and Go / ✓ / ›. */
@Composable
private fun QuestRow(item: DayPlan.Item, isNext: Boolean, breathe: Boolean, onClick: () -> Unit) {
    val color = when (item.kind) { DayPlan.Kind.REVIEW -> Night.sun; DayPlan.Kind.PLAY -> Night.rose; DayPlan.Kind.LIFE -> Night.violet }
    val ringColor by animateColorAsState(if (item.done) Night.jade else color, label = "ringColor")
    // A soft glow swells behind the next one, slow enough to feel calm. Read only while drawing.
    val breath: androidx.compose.runtime.State<Float> = if (breathe) {
        androidx.compose.animation.core.rememberInfiniteTransition(label = "breathe").animateFloat(
            0f, 1f,
            androidx.compose.animation.core.infiniteRepeatable(tween(1600, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "breath",
        )
    } else remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).pressClick(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            if (breathe) androidx.compose.foundation.Canvas(Modifier.size(52.dp)) {
                drawCircle(
                    androidx.compose.ui.graphics.Brush.radialGradient(listOf(color.copy(alpha = 0.15f + 0.35f * breath.value), color.copy(alpha = 0f)), center = center, radius = size.minDimension / 2),
                    radius = size.minDimension / 2,
                )
            }
            com.dudlatitat.nextchapter.ui.components.ProgressRing(item.progress, 48.dp, ringColor, stroke = 4.dp) {
                com.dudlatitat.nextchapter.ui.components.IconBadge(if (item.done) "✓" else badgeOf(item), if (item.done) Night.jade else color, 36.dp)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = if (item.done) Night.muted else Night.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        when {
            item.done -> Text("✓", style = MaterialTheme.typography.titleLarge, color = Night.jade)
            isNext -> com.dudlatitat.nextchapter.ui.components.GoChip("Go", color, onClick)
            else -> Text("›", style = MaterialTheme.typography.titleLarge, color = Night.muted)
        }
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
        "learn.ready2", "准备好了吗 · Ready for her family",
        summary = if (next == null) "Every rung climbed ✓" else "${ladder.climbed}/${ladder.rungs.size} · next: ${next.title}",
        defaultOpen = true,
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
