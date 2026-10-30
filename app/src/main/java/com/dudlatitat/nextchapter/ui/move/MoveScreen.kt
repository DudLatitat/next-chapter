package com.dudlatitat.nextchapter.ui.move

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.MoveTask
import com.dudlatitat.nextchapter.data.TaskGroups
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.friendlySpan
import com.dudlatitat.nextchapter.data.money
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.data.prettyDate
import com.dudlatitat.nextchapter.data.savingsProgress
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.ProgressRing
import com.dudlatitat.nextchapter.ui.components.ScreenTitle
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class DateField { MOVE, TRIP }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoveScreen(state: AppState, vm: MainViewModel) {
    var picking by remember { mutableStateOf<DateField?>(null) }
    var editingMoney by remember { mutableStateOf(false) }
    var addingTask by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<MoveTask?>(null) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("The move", "搬到中国 · Neijiang, Sichuan") }

        // Dates
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateCard("Move date", state.moveDate, Night.sun, Modifier.weight(1f)) { picking = DateField.MOVE }
                DateCard("Next trip", state.tripDate, Night.rose, Modifier.weight(1f)) { picking = DateField.TRIP }
            }
        }

        // Savings
        item {
            GlassCard(highlight = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(state.savingsProgress(), 88.dp, Night.jade, stroke = 9.dp) {
                        Text("${(state.savingsProgress() * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Savings · 存钱", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                        Text(money(state.saved), style = MaterialTheme.typography.headlineMedium, color = Night.ink)
                        Text(
                            if (state.savingsGoal > 0) "of ${money(state.savingsGoal)} · ${money((state.savingsGoal - state.saved).coerceAtLeast(0))} to go" else "Set a goal",
                            style = MaterialTheme.typography.bodySmall, color = Night.muted,
                        )
                        perMonth(state)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.jade) }
                    }
                    IconButton(onClick = { editingMoney = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = Night.muted) }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 100, 500).forEach { n ->
                        OutlinedButton(onClick = { vm.updatePlan { it.copy(saved = it.saved + n) } }, modifier = Modifier.weight(1f)) {
                            Text("+$$n", color = Night.jade)
                        }
                    }
                }
                if (state.flightTarget > 0) {
                    Spacer(Modifier.height(10.dp))
                    Text("✈  Book LAX ⇄ CTU at ${money(state.flightTarget)} or less", style = MaterialTheme.typography.bodyMedium, color = Night.rose)
                }
            }
        }

        // Checklist
        val done = state.tasks.count { it.done }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Checklist · $done/${state.tasks.size}", Modifier.weight(1f))
                TextButton(onClick = { addingTask = true }) {
                    Icon(Icons.Rounded.Add, null, tint = Night.sun); Spacer(Modifier.width(4.dp)); Text("Add step", color = Night.sun)
                }
            }
        }
        val groups = (TaskGroups + state.tasks.map { it.group }).distinct()
        groups.forEach { group ->
            val tasks = state.tasks.filter { it.group == group }
            if (tasks.isEmpty()) return@forEach
            item(key = "g:$group") {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(group, style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.weight(1f))
                        Text("${tasks.count { it.done }}/${tasks.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    }
                    Spacer(Modifier.height(6.dp))
                    Bar(tasks.count { it.done }.toFloat() / tasks.size, Night.sun)
                    Spacer(Modifier.height(6.dp))
                    tasks.forEach { task ->
                        Row(
                            Modifier.fillMaxWidth().combinedClickable(onClick = { vm.toggleTask(task) }, onLongClick = { deleting = task }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = task.done, onCheckedChange = { vm.toggleTask(task) },
                                colors = CheckboxDefaults.colors(checkedColor = Night.sun, uncheckedColor = Night.muted, checkmarkColor = MaterialTheme.colorScheme.onPrimary),
                            )
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (task.done) Night.muted else Night.ink,
                                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                            )
                        }
                    }
                }
            }
        }
        item { Text("Long-press a step to remove it.", style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.padding(4.dp)) }
    }

    picking?.let { field ->
        PickDate(
            initial = if (field == DateField.MOVE) state.moveDate else state.tripDate,
            onDismiss = { picking = null },
            onPick = { iso ->
                vm.updatePlan { if (field == DateField.MOVE) it.copy(moveDate = iso) else it.copy(tripDate = iso) }
                picking = null
            },
        )
    }
    if (editingMoney) MoneyDialog(state, onDismiss = { editingMoney = false }) { goal, saved, flight ->
        vm.updatePlan { it.copy(savingsGoal = goal, saved = saved, flightTarget = flight) }
        editingMoney = false
    }
    if (addingTask) AddTaskDialog(onDismiss = { addingTask = false }) { title, group -> vm.addTask(title, group); addingTask = false }
    deleting?.let { task ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove step?") },
            text = { Text(task.title) },
            confirmButton = { TextButton(onClick = { vm.deleteTask(task); deleting = null }) { Text("Remove", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep") } },
        )
    }
}

/** "Save $420/month to make it" when there's a goal and a move date. */
private fun perMonth(state: AppState): String? {
    val days = daysUntil(state.moveDate) ?: return null
    val left = state.savingsGoal - state.saved
    if (state.savingsGoal <= 0 || left <= 0 || days <= 0) return null
    val months = (days / 30.4).coerceAtLeast(1.0)
    return "Save ${money((left / months).toInt())}/month to make it"
}

@Composable
private fun DateCard(label: String, iso: String?, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.DateRange, null, tint = Night.muted)
        }
        Spacer(Modifier.height(6.dp))
        val days = daysUntil(iso)
        Text(if (days == null) "Tap to set" else friendlySpan(days), style = MaterialTheme.typography.headlineSmall, color = color)
        prettyDate(iso)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.muted) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickDate(initial: String?, onDismiss: () -> Unit, onPick: (String?) -> Unit) {
    val start = (parseDate(initial) ?: LocalDate.now().plusMonths(1)).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val picker = rememberDatePickerState(initialSelectedDateMillis = start)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val ms = picker.selectedDateMillis
                onPick(ms?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() })
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (initial != null) TextButton(onClick = { onPick(null) }) { Text("Clear", color = Night.coral) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    ) { DatePicker(picker) }
}

@Composable
private fun MoneyDialog(state: AppState, onDismiss: () -> Unit, onSave: (Int, Int, Int) -> Unit) {
    var goal by remember { mutableStateOf(if (state.savingsGoal > 0) state.savingsGoal.toString() else "") }
    var saved by remember { mutableStateOf(if (state.saved > 0) state.saved.toString() else "") }
    var flight by remember { mutableStateOf(if (state.flightTarget > 0) state.flightTarget.toString() else "") }
    fun num(s: String) = s.filter { it.isDigit() }.take(9).toIntOrNull() ?: 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Money plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val digits = KeyboardOptions(keyboardType = KeyboardType.Number)
                OutlinedTextField(goal, { goal = it }, label = { Text("Savings goal ($)") }, singleLine = true, keyboardOptions = digits)
                OutlinedTextField(saved, { saved = it }, label = { Text("Saved so far ($)") }, singleLine = true, keyboardOptions = digits)
                OutlinedTextField(flight, { flight = it }, label = { Text("Flight price target ($)") }, singleLine = true, keyboardOptions = digits)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(num(goal), num(saved), num(flight)) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(TaskGroups.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("What needs doing?") })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskGroups.take(2).forEach { g -> FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) }) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskGroups.drop(2).forEach { g -> FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(title, group) }, enabled = title.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
