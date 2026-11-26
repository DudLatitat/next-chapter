package com.dudlatitat.nextchapter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Road
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.theme.MonoStyle
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * The three-year road as one line: today on the left, the move on the right, and each dated
 * milestone (trips, the A+ exam, cert dates) as a dot at its share of the way (pushed apart when
 * dates are close, so they never overlap). Underneath, the next few as even rows: dot, name, date,
 * days to go. The move is always the last row.
 */
@Composable
fun RoadStrip(state: AppState, modifier: Modifier = Modifier) {
    val day = today()
    val marks = remember(state, day) { Road.marks(state, day) }
    if (marks.isEmpty()) return
    val end = marks.last().day
    fun color(k: Road.Kind) = when (k) { Road.Kind.TRIP -> Night.rose; Road.Kind.EXAM, Road.Kind.CERT -> Night.violet; Road.Kind.MOVE -> Night.sun }
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(18.dp)) {
            val y = size.height / 2
            val r = 5.dp.toPx()
            val span = size.width - 2 * r
            drawLine(Night.outline, Offset(r, y), Offset(size.width - r, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(Night.ink, r, Offset(r, y))
            // At least ~3 dot-widths between centres.
            val at = Road.spread(marks.map { it.at(day, end) }, (r * 3f) / span)
            marks.forEachIndexed { i, m ->
                drawCircle(color(m.kind), if (m.kind == Road.Kind.MOVE) r * 1.3f else r * 0.9f, Offset(r + at[i] * span, y))
            }
        }
        Spacer(Modifier.height(8.dp))
        val shown = (marks.dropLast(1).take(3) + marks.last()).distinct()
        shown.forEach { m ->
            Row(Modifier.fillMaxWidth().heightIn(min = 30.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color(m.kind)))
                Spacer(Modifier.width(10.dp))
                Text(m.label, style = MaterialTheme.typography.bodyMedium, color = Night.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(Road.short(m.day), style = MonoStyle, color = Night.muted, maxLines = 1, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
                Text(
                    (m.day - day).let { if (it == 0L) "today" else "${it}d" }, style = MonoStyle, color = color(m.kind), maxLines = 1,
                    modifier = Modifier.width(56.dp), textAlign = TextAlign.End,
                )
            }
        }
    }
}
