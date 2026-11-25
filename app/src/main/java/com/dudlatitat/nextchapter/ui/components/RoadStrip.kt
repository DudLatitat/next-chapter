package com.dudlatitat.nextchapter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
 * milestone (trips, the A+ exam, cert dates) as a dot at its share of the way. The next three
 * are named underneath.
 */
@Composable
fun RoadStrip(state: AppState, modifier: Modifier = Modifier) {
    val day = today()
    val marks = remember(state, day) { Road.marks(state, day) }
    if (marks.isEmpty()) return
    val end = marks.last().day
    fun color(k: Road.Kind) = when (k) { Road.Kind.TRIP -> Night.rose; Road.Kind.EXAM, Road.Kind.CERT -> Night.violet; Road.Kind.MOVE -> Night.sun }
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(16.dp)) {
            val y = size.height / 2
            val r = 5.dp.toPx()
            drawLine(Night.outline, Offset(r, y), Offset(size.width - r, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(Night.ink, r, Offset(r, y))
            marks.forEach { m ->
                val x = r + m.at(day, end) * (size.width - 2 * r)
                drawCircle(color(m.kind), if (m.kind == Road.Kind.MOVE) r * 1.3f else r * 0.9f, Offset(x, y))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            marks.take(3).joinToString("  ·  ") { "${it.label} ${Road.short(it.day)}" } + if (marks.size > 3) "  ·  … 成都 ${Road.short(end)}" else "",
            style = MonoStyle, color = Night.muted, maxLines = 1,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
    }
}
