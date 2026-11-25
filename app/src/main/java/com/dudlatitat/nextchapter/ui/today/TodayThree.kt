package com.dudlatitat.nextchapter.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
 * 今日三件 · Today's three: reviews, one game, one life thing, and one bar that fills.
 * After a break it says "Welcome back" instead of showing a pile of overdue cards.
 */
@Composable
fun TodayThree(state: AppState, onItem: (DayPlan.Item) -> Unit) {
    val plan = DayPlan.today(state)
    val fill by animateFloatAsState(plan.doneCount / plan.items.size.toFloat(), tween(700), label = "three")
    val barColor by animateColorAsState(if (plan.finished) Night.jade else Night.sun, label = "threeColor")
    GlassCard(highlight = !plan.finished) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (plan.finished) "今天完成了 · Done for today ✓" else "今日三件 · Today's three", style = MaterialTheme.typography.titleMedium, color = if (plan.finished) Night.jade else Night.ink)
                if (plan.welcomeBack > 0 && !plan.finished) {
                    Text(
                        "Welcome back 💛 No catching up: the ${DayPlan.COMEBACK_CAP} that matter most today, the rest come back over the next few days.",
                        style = MaterialTheme.typography.bodySmall, color = Night.rose,
                    )
                } else if (plan.finished) {
                    Text("Anything more tonight is a bonus.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
            Text("${plan.doneCount}/${plan.items.size}", style = MaterialTheme.typography.titleMedium, color = barColor)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(Night.outline)) {
            Box(Modifier.fillMaxWidth(fill).height(8.dp).clip(RoundedCornerShape(50)).background(barColor))
        }
        Spacer(Modifier.height(6.dp))
        plan.items.forEachIndexed { i, item -> PlanRow(i + 1, item) { onItem(item) } }
    }
}

@Composable
private fun PlanRow(n: Int, item: DayPlan.Item, onClick: () -> Unit) {
    val tint = when (item.kind) { DayPlan.Kind.REVIEW -> Night.sun; DayPlan.Kind.PLAY -> Night.rose; DayPlan.Kind.LIFE -> Night.violet }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(if (item.done) Night.jade else tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) { Text(if (item.done) "✓" else "$n", style = MaterialTheme.typography.labelLarge, color = if (item.done) Night.ground2 else tint) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, color = if (item.done) Night.muted else Night.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!item.done) Text("›", style = MaterialTheme.typography.titleLarge, color = tint)
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
                val mod = if (!p.done && p.tool != null) Modifier.clip(RoundedCornerShape(50)).clickable { onPiece(p.tool) } else Modifier
                Pill((if (p.done) "✓ " else "") + p.label, if (p.done) Night.jade else Night.rose, mod)
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
