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
import androidx.compose.material.icons.rounded.ArrowBack
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

/** On the Life tab: this week's dots, the streak and tonight's job. Tap for the full routine. */
@Composable
fun RoutineSummary(state: AppState, onOpen: () -> Unit) {
    val streak = Routine.streak(state.routineChecks, today())
    val job = Routine.jobOn(state, LocalDate.now())
    GlassCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Weekly routine · 每周", style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.weight(1f))
            if (streak > 0) Pill("🔥 $streak", Night.sun)
        }
        Text("Tonight: ${Routine.text(job.task, state.herName)}", style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1)
        Spacer(Modifier.height(10.dp))
        WeekDots(state)
    }
}

/** The full routine: the week, the rules, the daily anchors and each evening's job (tap to edit). */
@Composable
fun RoutineScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var editing by remember { mutableStateOf<RoutineDay?>(null) }
    val streak = Routine.streak(state.routineChecks, today())
    val wins = Routine.week(state.routineChecks, LocalDate.now()).count { Routine.isWin(it) }
    val trial = Routine.inTrial(state.routineChecks, today())
    val sunday = LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Weekly routine · 每周", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        GlassCard(highlight = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This week", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                Pill(if (streak > 0) "🔥 $streak-day streak" else "Fresh start ✨", Night.sun)
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

    editing?.let { d -> EditDay(d, vm, onClose = { editing = null }) }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape).border(1.dp, Night.muted.copy(alpha = 0.5f), CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditDay(d: RoutineDay, vm: MainViewModel, onClose: () -> Unit) {
    var task by remember { mutableStateOf(d.task) }
    var step by remember { mutableStateOf(d.firstStep) }
    var min by remember { mutableStateOf(d.minimum) }
    var kind by remember { mutableStateOf(d.kind) }
    var minutes by remember { mutableStateOf(d.minutes) }
    var picking by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(DayOfWeek.of(d.dow).getDisplayName(TextStyle.FULL, Locale.US)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(task, { task = it.take(70) }, label = { Text("The job") })
                OutlinedTextField(step, { step = it.take(60) }, label = { Text("Tiny first step") }, singleLine = true)
                OutlinedTextField(min, { min = it.take(60) }, label = { Text("Bad-day minimum") }, singleLine = true)
                OutlinedButton(onClick = { picking = true }) { Text("Starts at ${clockLabel(minutes)}", color = Night.sun) }
                Text("Start button opens", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("aplus" to "A+ test", "aplus-review" to "Missed A+", "chinese" to "Chinese", "" to "Timer").forEach { (k, l) ->
                        FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(l) })
                    }
                }
                Text("Write {her} to use her name from Settings.", style = MaterialTheme.typography.labelSmall, color = Night.muted, textAlign = TextAlign.Start)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.saveRoutineDay(d.copy(task = task.trim(), firstStep = step.trim(), minimum = min.trim(), kind = kind, minutes = minutes)); onClose() },
                enabled = task.isNotBlank() && min.isNotBlank(),
            ) { Text("Save", color = Night.sun) }
        },
        dismissButton = {
            Row {
                Routine.seed.firstOrNull { it.dow == d.dow }?.let { s ->
                    TextButton(onClick = { task = s.task; step = s.firstStep; min = s.minimum; kind = s.kind; minutes = s.minutes }) { Text("Reset", color = Night.muted) }
                }
                TextButton(onClick = onClose) { Text("Cancel", color = Night.muted) }
            }
        },
    )
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
