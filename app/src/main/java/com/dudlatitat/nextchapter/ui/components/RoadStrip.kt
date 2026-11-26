package com.dudlatitat.nextchapter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Road
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.theme.MonoStyle
import com.dudlatitat.nextchapter.ui.theme.Night

private fun colorOf(k: Road.Kind) = when (k) { Road.Kind.TRIP -> Night.rose; Road.Kind.EXAM, Road.Kind.CERT -> Night.violet; Road.Kind.MOVE -> Night.sun }
private fun glyphOf(k: Road.Kind) = when (k) { Road.Kind.TRIP -> "飞"; Road.Kind.EXAM, Road.Kind.CERT -> "考"; Road.Kind.MOVE -> "蓉" }

/**
 * 成都之路 · The road to Chengdu, as Today's card: the days left as the one big number, how long
 * that really is ("3 years, 1 month"), the road drawn as a glowing line with a badge for each
 * milestone and the years marked underneath, then the next few milestones as rows.
 */
@Composable
fun RoadCard(state: AppState, modifier: Modifier = Modifier) {
    val day = today()
    val marks = remember(state, day) { Road.marks(state, day) }
    if (marks.isEmpty()) return
    val end = marks.last()
    val moving = end.kind == Road.Kind.MOVE
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("The road to 成都", style = MaterialTheme.typography.labelLarge, color = Night.muted, modifier = Modifier.weight(1f))
            // 一步一步 (yī bù yī bù): step by step.
            Text("一步一步", style = MaterialTheme.typography.labelMedium, color = Night.sun.copy(alpha = 0.7f))
        }
        if (moving) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                CountUp((end.day - day).toInt(), MaterialTheme.typography.displaySmall, Night.sun)
                Spacer(Modifier.width(8.dp))
                Text("days to go", style = MaterialTheme.typography.titleSmall, color = Night.ink, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text("${Road.left(day, end.day)} · ${Road.weekdayDate(end.day, day)}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        Spacer(Modifier.height(14.dp))
        RoadStrip(state)
    }
}

/** The road line and its rows (also the strip inside Move's hero). */
@Composable
fun RoadStrip(state: AppState, modifier: Modifier = Modifier, rows: Boolean = true) {
    val day = today()
    val marks = remember(state, day) { Road.marks(state, day) }
    if (marks.isEmpty()) return
    val end = marks.last().day
    Column(modifier.fillMaxWidth()) {
        RoadLine(marks, day, end)
        if (!rows) return@Column
        Spacer(Modifier.height(10.dp))
        val shown = (marks.dropLast(1).take(3) + marks.last()).distinct()
        shown.forEachIndexed { i, m ->
            val c = colorOf(m.kind)
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBadge(glyphOf(m.kind), c, 36.dp, filled = i == 0 && m.kind != Road.Kind.MOVE)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(m.label, style = MaterialTheme.typography.titleSmall, color = Night.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(Road.weekdayDate(m.day, day), style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1)
                }
                val left = m.day - day
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (left == 0L) "today" else "${left}d", style = MonoStyle, color = c, maxLines = 1)
                    if (left in 14..364) Text("${left / 7} wk", style = MonoStyle.copy(fontSize = 11.sp), color = Night.muted, maxLines = 1)
                }
            }
        }
    }
}

/**
 * Today on the left, 成都 on the right. The travelled-to part glows from ink to gold; each
 * milestone is a small badge on the line (pushed apart when dates are close); New Year ticks
 * mark the years underneath.
 */
@Composable
private fun RoadLine(marks: List<Road.Mark>, day: Long, end: Long) {
    val ticks = remember(day, end) { Road.yearTicks(day, end) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(58.dp)) {
        val w = maxWidth
        val pad = 14.dp
        val span = w - pad * 2
        val lineY = 22.dp
        // "now" (0) goes in first so the first badge keeps clear of it.
        val at = Road.spread(listOf(0f) + marks.map { it.at(day, end) }, (26f / span.value)).drop(1)
        Canvas(Modifier.fillMaxWidth().height(58.dp)) {
            val y = lineY.toPx()
            val l = pad.toPx(); val r = size.width - pad.toPx()
            // The road: dashed for the way ahead, a soft gold glow under it.
            drawLine(
                Brush.horizontalGradient(listOf(Night.ink.copy(alpha = 0.5f), Night.violet.copy(alpha = 0.5f), Night.sun), startX = l, endX = r),
                Offset(l, y), Offset(r, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx())),
            )
            // Year ticks.
            ticks.forEach { (_, a) ->
                val x = l + a * (r - l)
                drawLine(Night.muted.copy(alpha = 0.5f), Offset(x, y + 6.dp.toPx()), Offset(x, y + 12.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            }
            // 成都: a warm halo.
            drawCircle(Brush.radialGradient(listOf(Night.sun.copy(alpha = 0.45f), Color.Transparent), center = Offset(r, y), radius = 18.dp.toPx()), 18.dp.toPx(), Offset(r, y))
            // You are here.
            drawCircle(Night.ink, 6.dp.toPx(), Offset(l, y))
            drawCircle(Night.ink.copy(alpha = 0.25f), 10.dp.toPx(), Offset(l, y))
        }
        // Milestone badges sit on the line.
        marks.forEachIndexed { i, m ->
            val x = pad + span * at[i]
            val s = if (m.kind == Road.Kind.MOVE) 24.dp else 20.dp
            Box(
                Modifier.offset(x = x - s / 2, y = lineY - s / 2).size(s).clip(CircleShape).background(colorOf(m.kind)),
                contentAlignment = Alignment.Center,
            ) { Text(glyphOf(m.kind), color = Night.ground2, fontSize = (s.value * 0.5f).sp, maxLines = 1) }
        }
        // Labels under the line: now, the years, 成都.
        Text("now", style = MonoStyle.copy(fontSize = 11.sp), color = Night.muted, modifier = Modifier.offset(y = 38.dp))
        ticks.forEach { (y, a) ->
            Text(
                "$y", style = MonoStyle.copy(fontSize = 11.sp), color = Night.muted, textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp).offset(x = pad + span * a - 20.dp, y = 38.dp),
            )
        }
        if (marks.last().kind == Road.Kind.MOVE) Text(
            "成都", style = MaterialTheme.typography.labelMedium, color = Night.sun, textAlign = TextAlign.End,
            modifier = Modifier.align(Alignment.TopEnd).offset(y = 36.dp),
        )
    }
}
