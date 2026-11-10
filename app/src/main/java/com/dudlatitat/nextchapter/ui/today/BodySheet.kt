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
        val history = Body.history(state, day)
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("Every day · 每天", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            val recent = Body.recentWeights(state.weights, day)
            if (recent.size >= 2) {
                Spacer(Modifier.height(8.dp))
                WeightChart(recent, state.goalWeight, day)
            }
            Spacer(Modifier.height(6.dp))
            history.forEach { h -> HistoryRow(h, state.calorieTarget, day) }
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

/** The last 30 days of weigh-ins as a soft line, with the goal as a dashed line when it's in view. */
@Composable
private fun WeightChart(points: List<com.dudlatitat.nextchapter.data.WeightEntry>, goal: Double?, today: Long) {
    val lo0 = points.minOf { it.lb }; val hi0 = points.maxOf { it.lb }
    val pad = maxOf(1.0, (hi0 - lo0) * 0.15)
    val lo = lo0 - pad; val hi = hi0 + pad
    val first = today - 29
    Column {
        androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(90.dp)) {
            fun x(d: Long) = ((d - first).toFloat() / 29f) * size.width
            fun y(v: Double) = (size.height * (1 - (v - lo) / (hi - lo))).toFloat()
            goal?.takeIf { it in lo..hi }?.let { g ->
                drawLine(
                    Night.violet.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, y(g)), androidx.compose.ui.geometry.Offset(size.width, y(g)),
                    strokeWidth = 1.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                )
            }
            val path = androidx.compose.ui.graphics.Path()
            points.forEachIndexed { i, p -> if (i == 0) path.moveTo(x(p.day), y(p.lb)) else path.lineTo(x(p.day), y(p.lb)) }
            drawPath(path, Night.violet, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            points.forEach { p -> drawCircle(Night.violet, radius = 3.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x(p.day), y(p.lb))) }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("30 days ago", style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.weight(1f))
            Text("${Body.lbs(lo0)}–${Body.lbs(hi0)} lb", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        }
    }
}
