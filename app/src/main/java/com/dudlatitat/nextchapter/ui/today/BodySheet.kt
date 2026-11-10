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
        Text("Kept on this phone only.", style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 8.dp))
    }
}
