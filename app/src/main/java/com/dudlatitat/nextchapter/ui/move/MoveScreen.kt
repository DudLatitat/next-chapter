package com.dudlatitat.nextchapter.ui.move

import com.dudlatitat.nextchapter.data.nextTripDate
import com.dudlatitat.nextchapter.data.withNextTripDate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Money
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
import com.dudlatitat.nextchapter.ui.components.pressClick
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
    val pageState = androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    var page by pageState
    com.dudlatitat.nextchapter.ui.components.SubNav(page, isRoot = { it == null }, label = "move") { p ->
        when (p) {
            "flights" -> FlightsScreen(state, vm, onBack = { page = null })
            "money" -> MoneyScreen(state, vm, onBack = { page = null })
            "packing" -> PackingScreen(state, vm, onBack = { page = null })
            else -> MoveHome(state, vm, pageState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoveHome(state: AppState, vm: MainViewModel, pageState: androidx.compose.runtime.MutableState<String?>) {
    var page by pageState
    var picking by remember { mutableStateOf<DateField?>(null) }
    var editingMoney by remember { mutableStateOf(false) }
    var addingTask by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<MoveTask?>(null) }

    val wide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // The countdown: the next trip big, and a line from today through the trip to the move.
        item {
            val facts = remember(state) { com.dudlatitat.nextchapter.data.Status.move(state) }
            com.dudlatitat.nextchapter.ui.components.TabHeader("Move", "搬家", facts.map { f -> com.dudlatitat.nextchapter.ui.components.Fact(f, onClick = if (f.endsWith("runway")) ({ page = "money" }) else null) })
        }
        item { Countdown(state, onTrip = { picking = DateField.TRIP }, onMove = { picking = DateField.MOVE }) }

        // Flights, money, packing: three small doors.
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MoveTool("机", "Flights", "机票", Night.rose, Modifier.weight(1f)) { page = "flights" }
                MoveTool("钱", "Money", "人民币", Night.jade, Modifier.weight(1f)) { page = "money" }
                MoveTool("包", "Packing", "${state.packing.count { it.done }}/${state.packing.size}", Night.sun, Modifier.weight(1f)) { page = "packing" }
            }
        }

        // Savings, with what they mean there (the lanterns) in the same card.
        item {
            GlassCard {
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
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(50, 100, 500).forEach { n -> com.dudlatitat.nextchapter.ui.components.Pill("+$$n", Night.jade, onClick = { vm.updatePlan { it.copy(saved = it.saved + n) } }) }
                }
                if (state.flightTarget > 0) Text("✈ Book LAX ⇄ CTU at ${money(state.flightTarget)} or less", style = MaterialTheme.typography.labelMedium, color = Night.rose, modifier = Modifier.padding(top = 6.dp))
                androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Night.outline.copy(alpha = 0.5f))
                Runway(state, onOpen = { page = "money" }, onStarter = { vm.updatePlan { it.copy(budget = com.dudlatitat.nextchapter.data.Seed.withStarterBudget(it.budget)) } })
            }
        }

        // Good times to talk this week (your free time × her waking hours, in real time zones).
        item {
            val zone = java.time.ZoneId.systemDefault()
            val day = com.dudlatitat.nextchapter.data.today()
            val windows = remember(day, zone) { com.dudlatitat.nextchapter.data.Calls.week(day, zone) }
            val first = windows.firstOrNull()?.let { com.dudlatitat.nextchapter.data.Calls.describe(it, zone) }
            com.dudlatitat.nextchapter.ui.components.Collapsible(
                "move.calls", "Good times to talk", summary = first?.let { "${it.first} · ${it.second}" } ?: "This week",
            ) {
                windows.forEach { w ->
                    val (mine, hers) = com.dudlatitat.nextchapter.data.Calls.describe(w, zone)
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(mine, style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.ink, modifier = Modifier.weight(1f))
                        Text(hers, style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.rose)
                    }
                }
                Text("Your evenings and weekends where she's usually awake (8 am–11 pm her time). Daylight saving is counted.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
        }

        // Paperwork: the next step to start, worked back from the move date.
        com.dudlatitat.nextchapter.data.Paperwork.next(state)?.let { t ->
            item {
                com.dudlatitat.nextchapter.ui.components.ListGroup {
                    com.dudlatitat.nextchapter.ui.components.ListRow(
                        t.title, subtitle = "Next step · " + (com.dudlatitat.nextchapter.data.Paperwork.label(t, state.moveDate) ?: ""), divider = false,
                        leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("证", Night.violet) },
                        trailing = { com.dudlatitat.nextchapter.ui.components.GoChip("Done", Night.jade) { vm.toggleTask(t) } },
                    )
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
            initial = if (field == DateField.MOVE) state.moveDate else state.nextTripDate(),
            onDismiss = { picking = null },
            onPick = { iso ->
                vm.updatePlan { if (field == DateField.MOVE) it.copy(moveDate = iso) else it.withNextTripDate(iso) }
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
    val each = Money.savePerMonth(state.savingsGoal, state.saved, days) ?: return null
    return "Save ${money(each)}/month to make it"
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
                    com.dudlatitat.nextchapter.data.Paperwork.label(task, vm.state.value.moveDate)?.let { l ->
                        Text(l, style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = if (l == "start now") Night.sun else Night.muted)
                    }
                    if (task.id in com.dudlatitat.nextchapter.data.Career.linkedTasks) {
                        Text("Linked to the Life tab", style = MaterialTheme.typography.labelSmall, color = Night.violet)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveTool(badge: String, title: String, sub: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        com.dudlatitat.nextchapter.ui.components.IconBadge(badge, color, 36.dp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = Night.ink, maxLines = 1)
        Text(sub, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1)
    }
}

/**
 * The next trip as a big number, and a line from today to the move with the trip marked on it,
 * so the two dates read as one road. Tap the trip or the move to change its date.
 */
@Composable
private fun Countdown(state: AppState, onTrip: () -> Unit, onMove: () -> Unit) {
    val trip = daysUntil(state.nextTripDate())?.takeIf { it >= 0 }
    val move = daysUntil(state.moveDate)?.takeIf { it >= 0 }
    com.dudlatitat.nextchapter.ui.components.HeroCard(accent = Night.rose) {
        // The big number is the next trip, so it's labelled as that (the move is the far end of the road below).
        Text("Next trip · 下次见面", style = MaterialTheme.typography.labelLarge, color = Night.muted)
        Spacer(Modifier.height(6.dp))
        Column(Modifier.pressClick(onClick = onTrip)) {
            if (trip != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(friendlySpan(trip), style = MaterialTheme.typography.displaySmall, color = Night.rose)
                    Spacer(Modifier.width(10.dp))
                    Text("to 成都 ✈", style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.padding(bottom = 6.dp))
                }
                prettyDate(state.nextTripDate())?.let { Text("Next trip · $it", style = MaterialTheme.typography.bodySmall, color = Night.muted) }
            } else {
                Text("Set your next trip ✈", style = MaterialTheme.typography.titleLarge, color = Night.rose)
            }
        }
        Spacer(Modifier.height(16.dp))
        // The road: today → trips, exams → the move.
        com.dudlatitat.nextchapter.ui.components.RoadStrip(state)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Today", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
            Text(
                if (move != null) "搬家 ${friendlySpan(move)}" else "Set the move date",
                style = MaterialTheme.typography.labelLarge, color = Night.sun,
                modifier = Modifier.pressClick(onClick = onMove).padding(vertical = 6.dp),
            )
        }
    }
}

/** Twelve little lanterns, lit by how many months of life there your savings would cover. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Runway(state: AppState, onOpen: () -> Unit, onStarter: () -> Unit = {}) {
    val monthly = state.budget.sumOf { it.rmb }
    Column(Modifier.fillMaxWidth().pressClick(haptic = false, onClick = onOpen)) {
        Text("Life there · 生活费", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        if (monthly <= 0) {
            // Empty: one line, a starter to try, and your own numbers.
            Text("What does a month there cost? Start from a rough Sichuan month and make it yours.", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                com.dudlatitat.nextchapter.ui.components.GoChip("Use a starter month", Night.jade, onStarter)
                com.dudlatitat.nextchapter.ui.components.SecondaryButton("Enter my own", onOpen)
            }
            return@Column
        }
        if (state.saved <= 0) {
            Text("Add what you've saved (✏️ above) to light the lanterns.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            return@Column
        }
        val months = Money.monthsCovered(state.saved, state.rate, monthly)
        Text(
            if (months >= 12) "Over a year of life there ✨" else "≈ ${"%.1f".format(java.util.Locale.US, months)} months of life there",
            style = MaterialTheme.typography.titleMedium, color = Night.ink,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            Money.runway(months).forEachIndexed { i, f ->
                val lit by androidx.compose.animation.core.animateFloatAsState(f, androidx.compose.animation.core.tween(500, delayMillis = i * 60), label = "lantern")
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(18.dp, 24.dp)
                            .background(Night.coral.copy(alpha = 0.12f + 0.75f * lit), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    )
                    Text("${i + 1}", fontSize = 11.sp, color = if (lit > 0.5f) Night.ink else Night.muted)
                }
            }
        }
        Text("${yuan(monthly)} a month · ${money(state.saved)} saved", style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}
