package com.dudlatitat.nextchapter.ui.today

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Body
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.theme.Night

/** Tap the ⚖️ pill: log today's weight, add calories in a tap or two, set your goals. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodySheet(state: AppState, vm: MainViewModel) {
    val day = today()
    val latest = Body.latest(state.weights)
    val eaten = Body.caloriesOn(state.calorieAdds, day)
    var weight by remember { mutableStateOf("") }
    var custom by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf(false) }
    var goalText by remember { mutableStateOf(state.goalWeight?.let { Body.lbs(it) } ?: "") }
    var targetText by remember { mutableStateOf(if (state.calorieTarget > 0) state.calorieTarget.toString() else "") }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 32.dp).animateContentSize()) {
        Text("Body · 身体", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(10.dp))

        // Weight
        Row(verticalAlignment = Alignment.Bottom) {
            Text(latest?.let { "${Body.lbs(it.lb)} lb" } ?: "No weight yet", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            state.goalWeight?.let { Text("  → ${Body.lbs(it)} lb", style = MaterialTheme.typography.titleMedium, color = Night.violet) }
        }
        Body.weekChange(state.weights)?.let { c ->
            Text(
                when {
                    c < -0.05 -> "↓ ${Body.lbs(-c)} lb this week. Nice and steady 💪"
                    c > 0.05 -> "↑ ${Body.lbs(c)} lb this week. One week doesn't define you."
                    else -> "Holding steady this week."
                },
                style = MaterialTheme.typography.bodySmall, color = if (c < -0.05) Night.jade else Night.muted,
            )
        }
        latest?.let { l -> state.goalWeight?.let { g -> if (l.lb > g) Text("${Body.lbs(l.lb - g)} lb to go", style = MaterialTheme.typography.bodySmall, color = Night.muted) } }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                weight, { weight = it.filter { c -> c.isDigit() || c == '.' }.take(6) }, singleLine = true,
                label = { Text(if (latest?.day == day) "Update today's weight" else "Today's weight (lb)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { weight.toDoubleOrNull()?.takeIf { it in 50.0..700.0 }?.let { vm.logWeight(it); weight = "" } },
                enabled = weight.toDoubleOrNull()?.let { it in 50.0..700.0 } == true,
                colors = ButtonDefaults.buttonColors(containerColor = Night.violet, contentColor = Night.ground2),
            ) { Text("Save") }
        }

        Spacer(Modifier.height(18.dp))
        // Calories
        Text(
            "🍽 ${Body.cal(eaten)}" + if (state.calorieTarget > 0) " / ${Body.cal(state.calorieTarget)} cal today" else " cal today",
            style = MaterialTheme.typography.titleLarge, color = Night.ink,
        )
        if (state.calorieTarget > 0) {
            Spacer(Modifier.height(6.dp))
            Bar((eaten / state.calorieTarget.toFloat()).coerceIn(0f, 1f), if (eaten > state.calorieTarget) Night.coral else Night.jade, Modifier.fillMaxWidth())
            Text(
                if (eaten <= state.calorieTarget) "${Body.cal(state.calorieTarget - eaten)} left" else "${Body.cal(eaten - state.calorieTarget)} over. Tomorrow's a fresh page.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(100, 250, 500).forEach { n -> AssistChip(onClick = { vm.addCalories(n) }, label = { Text("+$n", color = Night.sun) }) }
            if (eaten > 0) AssistChip(onClick = { vm.undoCalories() }, label = { Text("Undo last", color = Night.muted) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                custom, { custom = it.filter(Char::isDigit).take(4) }, singleLine = true, label = { Text("Other amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { custom.toIntOrNull()?.let { vm.addCalories(it) }; custom = "" },
                enabled = (custom.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2),
            ) { Text("Add") }
        }

        Spacer(Modifier.height(12.dp))
        // Goals, tucked away until needed
        TextButton(onClick = { goals = !goals }) { Text(if (goals) "Hide goals" else "Goals: weight and daily calories ›", color = Night.muted) }
        if (goals) {
            OutlinedTextField(
                goalText, { goalText = it.filter { c -> c.isDigit() || c == '.' }.take(6) }, singleLine = true, label = { Text("Goal weight (lb)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                targetText, { targetText = it.filter(Char::isDigit).take(5) }, singleLine = true, label = { Text("Daily calories (blank = none)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { vm.setBodyGoals(goalText.toDoubleOrNull()?.takeIf { it in 50.0..700.0 }, targetText.toIntOrNull() ?: 0); goals = false },
                colors = ButtonDefaults.buttonColors(containerColor = Night.violet, contentColor = Night.ground2),
            ) { Text("Save goals") }
        }
        val history = remember(state.weights, state.calorieAdds, day) { Body.history(state, day) }
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("Every day · 每天", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            BodyGraph(state, day)
            // The same days as a list, for exact numbers.
            var list by remember { mutableStateOf(false) }
            TextButton(onClick = { list = !list }) { Text(if (list) "Hide each day" else "Each day ›", color = Night.muted) }
            if (list) history.forEach { h -> HistoryRow(h, state.calorieTarget, day) }
        }
        Text("Kept on this phone only.", style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 8.dp))
    }
}

/** One day: the date, the weight and how it moved, and calories against the target. Blank days show "—". */
@Composable
private fun HistoryRow(h: Body.Day, target: Int, today: Long) {
    val date = java.time.LocalDate.ofEpochDay(h.day)
    val label = when (h.day) {
        today -> "Today"
        today - 1 -> "Yesterday"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US))
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Night.ink, modifier = Modifier.weight(1.1f))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(h.lb?.let { Body.lbs(it) } ?: "—", style = MaterialTheme.typography.bodyMedium, color = if (h.lb != null) Night.ink else Night.muted)
            h.change?.let { c ->
                val (t, color) = when {
                    c < -0.05 -> "  ↓${Body.lbs(-c)}" to Night.jade
                    c > 0.05 -> "  ↑${Body.lbs(c)}" to Night.coral
                    else -> "  =" to Night.muted
                }
                Text(t, style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
        val cal = h.calories
        Text(
            when {
                cal == null -> "—"
                target <= 0 -> "${Body.cal(cal)} cal"
                cal <= target -> "${Body.cal(cal)} ✓"
                else -> "${Body.cal(cal)} +${Body.cal(cal - target)}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = when { cal == null -> Night.muted; target > 0 && cal > target -> Night.coral; target > 0 -> Night.jade; else -> Night.ink },
            modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

/**
 * The graph: calories as a bar for each day (green under target, coral over, dashed gold target
 * line) and weight as a violet line on top (dashed goal line). 7 / 30 / 90 days; tap a day for its numbers.
 */
@Composable
private fun BodyGraph(state: AppState, today: Long) {
    var days by remember { mutableStateOf(30) }
    var picked by remember(days) { mutableStateOf<Int?>(null) }
    val series = remember(state.weights, state.calorieAdds, today, days) { Body.series(state, today, days) }
    val target = state.calorieTarget
    val maxCal = maxOf((target * 1.3).toInt(), series.maxOf { it.calories ?: 0 }, 500)
    val weights = series.mapNotNull { it.lb } + listOfNotNull(Body.latest(state.weights)?.lb.takeIf { series.none { s -> s.lb != null } })
    val lo0 = (weights + listOfNotNull(state.goalWeight)).minOrNull()
    val hi0 = (weights + listOfNotNull(state.goalWeight)).maxOrNull()
    val pad = if (lo0 != null && hi0 != null) maxOf(1.0, (hi0 - lo0) * 0.15) else 1.0
    val lo = (lo0 ?: 0.0) - pad; val hi = (hi0 ?: 1.0) + pad
    val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US)

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(7, 30, 90).forEach { n ->
            androidx.compose.material3.FilterChip(selected = days == n, onClick = { days = n }, label = { Text("$n days") })
        }
    }
    // The picked day's numbers (or a hint).
    val sel = picked?.let { series.getOrNull(it) }
    Text(
        sel?.let { d ->
            val date = java.time.LocalDate.ofEpochDay(d.day).format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.US))
            val w = d.lb?.let { "${Body.lbs(it)} lb" + (d.change?.let { c -> if (c < -0.05) " ↓${Body.lbs(-c)}" else if (c > 0.05) " ↑${Body.lbs(c)}" else "" } ?: "") } ?: "no weigh-in"
            val c = d.calories?.let { "${Body.cal(it)} cal" + if (target > 0 && it > target) " +${Body.cal(it - target)}" else "" } ?: "no calories logged"
            "$date · $w · $c"
        } ?: "Tap a day to see its numbers.",
        style = MaterialTheme.typography.bodySmall, color = if (sel != null) Night.ink else Night.muted,
        modifier = Modifier.padding(vertical = 6.dp),
    )
    androidx.compose.foundation.Canvas(
        Modifier.fillMaxWidth().height(180.dp).pointerInput(days) {
            detectTapGestures { o -> picked = (o.x / (size.width / series.size.toFloat())).toInt().coerceIn(0, series.size - 1) }
        },
    ) {
        val slot = size.width / series.size
        val barW = (slot * 0.62f).coerceAtLeast(1.5f)
        val bottom = size.height
        fun yCal(c: Int) = bottom - (c / maxCal.toFloat()) * size.height * 0.95f
        fun yLb(v: Double) = (size.height * 0.05f + (size.height * 0.85f) * (1 - (v - lo) / (hi - lo))).toFloat()
        fun xMid(i: Int) = slot * i + slot / 2
        val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        // Calories
        series.forEachIndexed { i, d ->
            val c = d.calories ?: return@forEachIndexed
            val color = when { target <= 0 -> Night.sun; c > target -> Night.coral; else -> Night.jade }
            drawRoundRect(
                color.copy(alpha = if (picked == null || picked == i) 0.85f else 0.35f),
                topLeft = androidx.compose.ui.geometry.Offset(xMid(i) - barW / 2, yCal(c)),
                size = androidx.compose.ui.geometry.Size(barW, bottom - yCal(c)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 3, barW / 3),
            )
        }
        if (target > 0) drawLine(Night.sun.copy(alpha = 0.7f), androidx.compose.ui.geometry.Offset(0f, yCal(target)), androidx.compose.ui.geometry.Offset(size.width, yCal(target)), strokeWidth = 1.5.dp.toPx(), pathEffect = dash)
        // Weight
        state.goalWeight?.let { g -> drawLine(Night.violet.copy(alpha = 0.55f), androidx.compose.ui.geometry.Offset(0f, yLb(g)), androidx.compose.ui.geometry.Offset(size.width, yLb(g)), strokeWidth = 1.5.dp.toPx(), pathEffect = dash) }
        val pts = series.mapIndexedNotNull { i, d -> d.lb?.let { androidx.compose.ui.geometry.Offset(xMid(i), yLb(it)) } }
        if (pts.size >= 2) {
            val path = androidx.compose.ui.graphics.Path()
            pts.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
            drawPath(path, Night.violet, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        pts.forEach { p -> drawCircle(Night.violet, radius = (if (days > 30) 2.5 else 4.0).dp.toPx(), center = p) }
        picked?.let { i -> drawLine(Night.ink.copy(alpha = 0.35f), androidx.compose.ui.geometry.Offset(xMid(i), 0f), androidx.compose.ui.geometry.Offset(xMid(i), bottom), strokeWidth = 1.dp.toPx()) }
    }
    Row(Modifier.fillMaxWidth()) {
        Text(java.time.LocalDate.ofEpochDay(series.first().day).format(fmt), style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.weight(1f))
        Text("Today", style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 4.dp)) {
        Text("▮ calories", style = MaterialTheme.typography.labelSmall, color = Night.jade)
        Text("— weight", style = MaterialTheme.typography.labelSmall, color = Night.violet)
        if (lo0 != null && hi0 != null) Text("${Body.lbs(lo0)}–${Body.lbs(hi0)} lb", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        if (target > 0) Text("- - target", style = MaterialTheme.typography.labelSmall, color = Night.sun)
    }
}
