package com.dudlatitat.nextchapter.ui.move

import com.dudlatitat.nextchapter.data.nextTripDate
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
    var page by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    when (page) {
        "flights" -> { FlightsScreen(state, vm, onBack = { page = null }); return }
        "money" -> { MoneyScreen(state, vm, onBack = { page = null }); return }
        "packing" -> { PackingScreen(state, vm, onBack = { page = null }); return }
    }
    var picking by remember { mutableStateOf<DateField?>(null) }
    var editingMoney by remember { mutableStateOf(false) }
    var addingTask by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<MoveTask?>(null) }

    val wide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
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
                DateCard("Next trip", state.nextTripDate(), Night.rose, Modifier.weight(1f)) { picking = DateField.TRIP }
            }
        }

        // Flights, money, packing
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MoveTool("✈", "Flights", "机票", Modifier.weight(1f)) { page = "flights" }
                MoveTool("¥", "Money", "人民币", Modifier.weight(1f)) { page = "money" }
                MoveTool("🧳", "Packing", "${state.packing.count { it.done }}/${state.packing.size}", Modifier.weight(1f)) { page = "packing" }
            }
        }

        // Savings, compact: amount and goal on one line, the plan underneath, quick-add chips.
        item {
            GlassCard(highlight = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(state.savingsProgress(), 52.dp, Night.jade, stroke = 6.dp) {
                        Text("${(state.savingsProgress() * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Night.ink)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Savings · 存钱", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                        Text(
                            money(state.saved) + if (state.savingsGoal > 0) " of ${money(state.savingsGoal)}" else "",
                            style = MaterialTheme.typography.titleMedium, color = Night.ink, maxLines = 1,
                        )
                        if (state.savingsGoal > 0) perMonth(state)?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Night.jade) }
                        else Text("Tap ✏️ to set a goal", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                    }
                    IconButton(onClick = { editingMoney = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = Night.muted) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(50, 100, 500).forEach { n ->
                        androidx.compose.material3.AssistChip(onClick = { vm.updatePlan { it.copy(saved = it.saved + n) } }, label = { Text("+$$n", color = Night.jade) })
                    }
                }
                if (state.flightTarget > 0) Text("✈ Book LAX ⇄ CTU at ${money(state.flightTarget)} or less", style = MaterialTheme.typography.labelMedium, color = Night.rose)
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
        val groups = (TaskGroups + state.tasks.map { it.group }).distinct().filter { g -> state.tasks.any { it.group == g } }
        groups.chunked(if (wide) 2 else 1).forEach { row ->
            item(key = "g:${row.first()}") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { group -> TaskGroupCard(group, state.tasks.filter { it.group == group }, vm, Modifier.weight(1f)) { deleting = it } }
                    if (row.size == 1 && wide) Spacer(Modifier.weight(1f))
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
internal fun PickDate(initial: String?, onDismiss: () -> Unit, onPick: (String?) -> Unit) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskGroupCard(group: String, tasks: List<MoveTask>, vm: MainViewModel, modifier: Modifier, onDelete: (MoveTask) -> Unit) {
    val done = tasks.count { it.done }
    val next = tasks.firstOrNull { !it.done }
    // Closed: the group's count and its next step. Tap to see every step.
    com.dudlatitat.nextchapter.ui.components.Collapsible(
        "move.group.$group", "$group · $done/${tasks.size}", modifier,
        summary = next?.let { "Next: ${it.title}" } ?: "All done ✓",
    ) {
        Bar(done.toFloat() / tasks.size, Night.sun)
        Spacer(Modifier.height(6.dp))
        tasks.forEach { task ->
            Row(
                Modifier.fillMaxWidth().combinedClickable(onClick = { vm.toggleTask(task) }, onLongClick = { onDelete(task) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = task.done, onCheckedChange = { vm.toggleTask(task) },
                    colors = CheckboxDefaults.colors(checkedColor = Night.sun, uncheckedColor = Night.muted, checkmarkColor = MaterialTheme.colorScheme.onPrimary),
                )
                Column {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (task.done) Night.muted else Night.ink,
                        textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    )
                    if (task.id in com.dudlatitat.nextchapter.data.Career.linkedTasks) {
                        Text("Linked to the Life tab", style = MaterialTheme.typography.labelSmall, color = Night.violet)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveTool(icon: String, title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        Text(icon, style = MaterialTheme.typography.headlineSmall, color = Night.sun)
        Text(title, style = MaterialTheme.typography.titleSmall, color = Night.ink)
        Text(sub, style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }
}
