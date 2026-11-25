package com.dudlatitat.nextchapter.ui.life

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.CheckIn
import com.dudlatitat.nextchapter.data.Routine
import com.dudlatitat.nextchapter.data.RoutineDay
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun clockLabel(minutes: Int): String =
    LocalTime.of((minutes / 60) % 24, minutes % 60).format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

fun checkColor(c: CheckIn?): Color = when (c) {
    CheckIn.DONE -> Night.jade
    CheckIn.MINIMUM -> Night.sun
    CheckIn.SKIPPED -> Night.muted.copy(alpha = 0.45f)
    null -> Color.Transparent
}

/** Monday → Sunday as dots: green done, gold minimum, grey rest, outlined = not checked (today ringed). */
@Composable
fun WeekDots(state: AppState, big: Boolean = false) {
    val now = LocalDate.now()
    val week = Routine.week(state.routineChecks, now)
    val size = if (big) 30.dp else 18.dp
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        week.forEachIndexed { i, c ->
            val day = DayOfWeek.of(i + 1)
            val isToday = now.dayOfWeek == day
            val off = Routine.dayOff(state.daysOff, now.with(DayOfWeek.MONDAY).toEpochDay() + i) != null
            val fill by animateColorAsState(checkColor(c), label = "dot")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(size).background(fill, CircleShape)
                        .border(if (isToday) 2.dp else 1.dp, if (isToday) Night.rose else Night.muted.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (big && c == CheckIn.DONE) Text("✓", color = Night.ground2)
                    if (big && c == CheckIn.MINIMUM) Text("½", color = Night.ground2)
                }
                Text(
                    day.getDisplayName(if (big) TextStyle.SHORT else TextStyle.NARROW, Locale.US) + if (off) "🏖" else "",
                    style = MaterialTheme.typography.labelSmall, color = if (isToday) Night.ink else if (off) Night.jade else Night.muted,
                )
            }
        }
    }
}

/** On the Life tab: the one place the week lives. Nights in a row, early starts, the dots. */
@Composable
fun RoutineSummary(state: AppState, onOpen: () -> Unit) {
    val streak = Routine.week(state.routineChecks, LocalDate.now()).count { Routine.isWin(it) }
    val early = com.dudlatitat.nextchapter.data.Srs.streak(state.wakeDays)
    GlassCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("This week · 本周", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text("Tap to plan your evenings", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            com.dudlatitat.nextchapter.ui.components.StatTile("🌙$streak", "this week", Night.violet)
            Spacer(Modifier.width(14.dp))
            com.dudlatitat.nextchapter.ui.components.StatTile("☀️$early", "early", Night.jade)
        }
        Spacer(Modifier.height(12.dp))
        WeekDots(state)
    }
}

/** The full routine: the week, the rules, the daily anchors and each evening's job (tap to edit). */
@Composable
fun RoutineScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var editing by remember { mutableStateOf<RoutineDay?>(null) }
    val streak = Routine.week(state.routineChecks, LocalDate.now()).count { Routine.isWin(it) }
    val wins = Routine.week(state.routineChecks, LocalDate.now()).count { Routine.isWin(it) }
    val trial = Routine.inTrial(state.routineChecks, today())
    val sunday = LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Weekly routine · 每周", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        GlassCard(highlight = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This week", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                Pill(if (streak > 0) "🌙 $streak this week" else "Fresh week ✨", Night.violet)
            }
            Spacer(Modifier.height(12.dp))
            WeekDots(state, big = true)
            Spacer(Modifier.height(10.dp))
            Text(
                when {
                    wins == 0 -> "One job a night. That's all."
                    wins < 4 -> "$wins ${if (wins == 1) "evening" else "evenings"} done this week. Keep it simple."
                    else -> "$wins evenings this week. 太棒了!"
                },
                style = MaterialTheme.typography.bodyMedium, color = Night.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                Legend("Done", Night.jade); Legend("Minimum", Night.sun); Legend("Rest", Night.muted.copy(alpha = 0.45f))
            }
        }

        GlassCard {
            Routine.rules.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Night.ink, modifier = Modifier.padding(vertical = 2.dp)) }
        }

        SectionLabel("Each evening")
        if (trial && !sunday) Text(
            "Still in your first two weeks: try it as it is before changing things. (You can still edit if you need to.)",
            style = MaterialTheme.typography.bodySmall, color = Night.muted,
        )
        val todayDow = LocalDate.now().dayOfWeek.value
        state.routine.forEach { d ->
            GlassCard(onClick = { editing = d }, highlight = d.dow == todayDow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        DayOfWeek.of(d.dow).getDisplayName(TextStyle.SHORT, Locale.US) + (if (d.dow == todayDow) " · today" else ""),
                        style = MaterialTheme.typography.labelMedium, color = if (d.dow == todayDow) Night.rose else Night.muted, modifier = Modifier.weight(1f),
                    )
                    Text(clockLabel(d.minutes), style = MaterialTheme.typography.labelMedium, color = Night.muted)
                }
                Text(Routine.text(d.task, state.herName), style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text("Bad day: ${Routine.text(d.minimum, state.herName)}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }

        DaysOff(state, vm)

        SectionLabel("Every day")
        GlassCard {
            Routine.anchors(state).forEach { (m, text) ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text(clockLabel(m), style = MaterialTheme.typography.labelMedium, color = Night.sun, modifier = Modifier.width(76.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                }
            }
            Text("Wake, home and wind-down times are in Settings.", style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 6.dp))
        }
        Spacer(Modifier.height(8.dp))
    }

    editing?.let { d -> EditDaySheet(state, d.dow, vm, onClose = { editing = null }) }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape).border(1.dp, Night.muted.copy(alpha = 0.5f), CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

/**
 * Edit the evenings, as a sheet: day chips across the top (hop between days; changes are saved
 * as you hop), the job, its first step and bad-day minimum, when it starts and what Start opens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDaySheet(state: AppState, startDow: Int, vm: MainViewModel, onClose: () -> Unit) {
    var dow by remember { androidx.compose.runtime.mutableIntStateOf(startDow) }
    val d = state.routine.firstOrNull { it.dow == dow } ?: return
    var task by remember(dow) { mutableStateOf(d.task) }
    var step by remember(dow) { mutableStateOf(d.firstStep) }
    var min by remember(dow) { mutableStateOf(d.minimum) }
    var kind by remember(dow) { mutableStateOf(d.kind) }
    var minutes by remember(dow) { androidx.compose.runtime.mutableIntStateOf(d.minutes) }
    var picking by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    val valid = task.isNotBlank() && min.isNotBlank()
    fun edited() = d.copy(task = task.trim(), firstStep = step.trim(), minimum = min.trim(), kind = kind, minutes = minutes)
    fun saveIfChanged(): Boolean {
        val e = edited()
        if (valid && e != d) { vm.saveRoutineDay(e); return true }
        return false
    }
    val dayName = { n: Int -> DayOfWeek.of(n).getDisplayName(TextStyle.SHORT, Locale.US) }
    val todayDow = LocalDate.now().dayOfWeek.value
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = { saveIfChanged(); onClose() },
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Night.ground1,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Each evening · 每晚", style = MaterialTheme.typography.titleLarge, color = Night.ink)
            com.dudlatitat.nextchapter.ui.components.SegmentedChips(
                state.routine.map { (dayName(it.dow) + if (it.dow == todayDow) " ·" else "") to (if (it.dow == todayDow) Night.rose else Night.violet) },
                state.routine.indexOfFirst { it.dow == dow },
                onSelect = { i -> if (saveIfChanged()) note = "Saved ${dayName(dow)} ✓"; dow = state.routine[i].dow },
            )
            note?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = Night.jade) }
            OutlinedTextField(task, { task = it.take(70) }, label = { Text("The job") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(step, { step = it.take(60) }, label = { Text("Tiny first step") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(min, { min = it.take(60) }, label = { Text("Bad-day minimum") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            com.dudlatitat.nextchapter.ui.components.ListGroup {
                com.dudlatitat.nextchapter.ui.components.ListRow(
                    "Starts at", subtitle = clockLabel(minutes), divider = false,
                    leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("时", Night.sun) }, onClick = { picking = true },
                )
            }
            Text("Start opens", style = MaterialTheme.typography.labelLarge, color = Night.muted)
            val kinds = listOf("aplus" to "A+ test", "aplus-review" to "Missed A+", "chinese" to "Chinese", "" to "Timer")
            com.dudlatitat.nextchapter.ui.components.SegmentedChips(
                kinds.map { it.second to Night.sun }, kinds.indexOfFirst { it.first == kind }.coerceAtLeast(0), onSelect = { kind = kinds[it].first },
            )
            Text("Tip: write {her} and it shows her name from Settings.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { if (valid) { vm.saveRoutineDay(edited()); onClose() } }, enabled = valid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2),
            ) { Text("Save ${dayName(dow)}", style = MaterialTheme.typography.titleMedium) }
            Routine.seed.firstOrNull { it.dow == dow }?.let { seed ->
                TextButton(onClick = { task = seed.task; step = seed.firstStep; min = seed.minimum; kind = seed.kind; minutes = seed.minutes }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Reset ${dayName(dow)} to the default", color = Night.muted)
                }
            }
        }
    }
    if (picking) {
        val picker = rememberTimePickerState(minutes / 60, minutes % 60, is24Hour = false)
        AlertDialog(
            onDismissRequest = { picking = false },
            text = { TimePicker(picker) },
            confirmButton = { TextButton(onClick = { minutes = picker.hour * 60 + picker.minute; picking = false }) { Text("Set") } },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
        )
    }
}

/** Days off (a paid day off, a holiday): each one is planned like Saturday or Sunday. */
@Composable
private fun DaysOff(state: AppState, vm: MainViewModel) {
    var adding by remember { mutableStateOf(false) }
    val upcoming = state.daysOff.filter { it.day >= today() }.sortedBy { it.day }
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
    SectionLabel("Days off 🏖")
    GlassCard {
        if (upcoming.isEmpty()) Text(
            "A paid day off or a holiday? Add it and that day follows your weekend plan, with no workday.",
            style = MaterialTheme.typography.bodySmall, color = Night.muted,
        )
        upcoming.forEach { off ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(LocalDate.ofEpochDay(off.day).format(fmt), style = MaterialTheme.typography.bodyMedium, color = Night.ink, modifier = Modifier.weight(1f))
                listOf(6 to "like Sat", 7 to "like Sun").forEach { (dow, label) ->
                    FilterChip(selected = off.like == dow, onClick = { vm.setDayOff(off.day, dow) }, label = { Text(label) }, modifier = Modifier.padding(start = 4.dp))
                }
                TextButton(onClick = { vm.removeDayOff(off.day) }) { Text("✕", color = Night.muted) }
            }
        }
        TextButton(onClick = { adding = true }) { Text("+ Add a day off", color = Night.sun) }
    }
    if (adding) com.dudlatitat.nextchapter.ui.move.PickDate(
        initial = LocalDate.now().plusDays(1).toString(),
        onDismiss = { adding = false },
        onPick = { iso -> iso?.let { LocalDate.parse(it).toEpochDay() }?.let { vm.setDayOff(it, 6) }; adding = false },
    )
}
