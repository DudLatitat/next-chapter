package com.dudlatitat.nextchapter.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.CheckIn
import com.dudlatitat.nextchapter.data.Routine
import com.dudlatitat.nextchapter.data.RoutineDay
import com.dudlatitat.nextchapter.data.Life
import com.dudlatitat.nextchapter.data.Prayers
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Stats
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Tonight's one job from the weekly routine: the task, the tiny first step, the bad-day minimum,
 * a big Start, then Done / Did the minimum / Skip. Nothing to choose: that's the point.
 */
@Composable
fun TonightJob(state: AppState, vm: MainViewModel, onStart: (RoutineDay) -> Unit, bare: Boolean = false) {
    val job = Routine.jobFor(state.routine, java.time.LocalDate.now())
    val checked = Routine.checkFor(state.routineChecks, today())
    val streak = Routine.streak(state.routineChecks, today())
    val her = state.herName
    var changing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val body: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tonight's job · 今晚", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
            if (streak > 0) Pill("🔥 $streak", Night.sun)
        }
        Spacer(Modifier.height(6.dp))
        Text(Routine.text(job.task, her), style = MaterialTheme.typography.titleLarge, color = Night.ink)
        if (checked == null || changing) {
            Spacer(Modifier.height(6.dp))
            Text("👣 First step: ${Routine.text(job.firstStep, her)}", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            Text("Bad day? ${Routine.text(job.minimum, her)} still counts.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Spacer(Modifier.height(12.dp))
            if (checked == null) Button(
                onClick = { onStart(job) }, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Night.rose, contentColor = Night.ground2),
            ) { Text("Start ▶", style = MaterialTheme.typography.titleMedium) }
            if (job.kind == "chinese") TextButton(onClick = { saving = true }) { Text("✍ Save a sentence ${Routine.text("{her}", her)} taught you", color = Night.sun) }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { vm.checkIn(CheckIn.DONE); changing = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2),
                ) { Text("Done ✓") }
                OutlinedButton(onClick = { vm.checkIn(CheckIn.MINIMUM); changing = false }) { Text("Did the minimum", color = Night.sun) }
            }
            TextButton(onClick = { vm.checkIn(CheckIn.SKIPPED); changing = false }) { Text("Skip today", color = Night.muted) }
        } else {
            Spacer(Modifier.height(8.dp))
            // A little bounce when you check in.
            var shown by remember(checked) { mutableStateOf(false) }
            androidx.compose.runtime.LaunchedEffect(checked) { shown = true }
            val pop by animateFloatAsState(if (shown) 1f else 0.7f, androidx.compose.animation.core.spring(dampingRatio = 0.4f), label = "pop")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Routine.cheer(checked), style = MaterialTheme.typography.bodyMedium, color = com.dudlatitat.nextchapter.ui.life.checkColor(checked).takeIf { checked != CheckIn.SKIPPED } ?: Night.muted, modifier = Modifier.weight(1f).scale(pop))
                TextButton(onClick = { changing = true }) { Text("Change", color = Night.muted) }
            }
        }
    }
    if (bare) Column(Modifier.animateContentSize(), content = body) else GlassCard(highlight = checked == null, modifier = Modifier.animateContentSize(), content = body)

    if (saving) SaveSentence(vm, her, onClose = { saving = false })
}

/** A sentence she taught you goes straight into My phrases, pinyin filled in for you. */
@Composable
private fun SaveSentence(vm: MainViewModel, her: String, onClose: () -> Unit) {
    var zh by remember { mutableStateOf("") }
    var en by remember { mutableStateOf("") }
    val auto = remember(zh) { if (zh.isBlank()) "" else com.dudlatitat.nextchapter.data.Pinyin.fromHanzi(zh.trim()) ?: "" }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("What did ${Routine.text("{her}", her)} teach you?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(zh, { zh = it.take(40) }, label = { Text("Chinese") }, singleLine = true)
                if (auto.isNotEmpty()) PinyinText(auto, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(en, { en = it.take(60) }, label = { Text("What it means") }, singleLine = true)
                Text("It goes into My phrases, first in line for review.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
        },
        confirmButton = {
            TextButton(onClick = { vm.addCard(zh, auto, en); onClose() }, enabled = zh.isNotBlank()) { Text("Save 💛", color = Night.sun) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel", color = Night.muted) } },
    )
}

/** Today's prayer, opened with the Sign of the Cross; Chinese and pinyin on a tap. */
@Composable
fun PrayerBlock(vm: MainViewModel, prayer: com.dudlatitat.nextchapter.data.Prayer = Prayers.forDay(today())) {
    // Unfolded Fold: English and Chinese side by side. Cover screen: switch between them.
    val wide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    var chinese by remember { mutableStateOf(false) }
    Text("✝  ${prayer.title} · ${prayer.titleZh}", style = MaterialTheme.typography.titleMedium, color = Night.ink)
    Spacer(Modifier.height(8.dp))
    if (wide) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.weight(1f)) { EnglishLines(prayer) }
            Column(Modifier.weight(1f)) { ChineseLines(prayer, vm) }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !chinese, onClick = { chinese = false }, label = { Text("English") })
            FilterChip(selected = chinese, onClick = { chinese = true }, label = { Text("中文 + pinyin") })
        }
        Spacer(Modifier.height(8.dp))
        if (chinese) ChineseLines(prayer, vm) else EnglishLines(prayer)
    }
}

/** English in short lines with room between them, opened with the Sign of the Cross. */
@Composable
private fun EnglishLines(prayer: com.dudlatitat.nextchapter.data.Prayer) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(Prayers.signOfTheCross.english.joinToString(" "), style = MaterialTheme.typography.bodyMedium, color = Night.muted)
        prayer.english.forEach { Text(it, style = MaterialTheme.typography.bodyLarge, color = Night.ink) }
    }
}

/** Chinese line by line, each with its pinyin right under it. Tap a line to hear it. */
@Composable
private fun ChineseLines(prayer: com.dudlatitat.nextchapter.data.Prayer, vm: MainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        (Prayers.signOfTheCross.lines + prayer.lines).forEachIndexed { i, (zh, py) ->
            Column(Modifier.fillMaxWidth().clickable { vm.speaker.say(zh) }) {
                Text(zh, style = MaterialTheme.typography.titleMedium, color = if (i == 0) Night.muted else Night.ink)
                PinyinText(py, style = MaterialTheme.typography.bodySmall)
            }
        }
        TextButton(onClick = { vm.speaker.say(prayer.hanzi) }) { Text("🔊 Hear it all", color = Night.sun) }
        Text("Tap any line to hear just that line.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

/** The Amen button (or "prayed today"), shared by the morning card and the prayer row. */
@Composable
fun AmenButton(state: AppState, vm: MainViewModel) {
    if (today() in state.prayedDays) Pill("🙏 Amen · prayed today", Night.jade)
    else Button(
        onClick = { vm.prayed() },
        colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
    ) { Text("Amen 🙏") }
}

/** Sundays: the week in numbers, and one line to remember it by. */
@Composable
fun SundayReview(state: AppState, vm: MainViewModel, onOpenRoutine: () -> Unit = {}) {
    val start = Stats.weekStart()
    val key = start.toString()
    val chinese = Stats.week(state, start)
    val life = Life.week(state, start)
    var editing by remember { mutableStateOf(false) }
    var note by remember(key) { mutableStateOf(state.weekNotes[key] ?: "") }
    val saved = state.weekNotes[key]

    GlassCard(highlight = true, modifier = Modifier.animateContentSize()) {
        Text("Your week · 这周", style = MaterialTheme.typography.titleLarge, color = Night.ink)
        Text("Sunday review", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            WeekStat("${chinese.daysPracticed}/7", "Chinese days", Night.jade)
            WeekStat("${life.prayed}/7", "prayed", Night.violet)
            WeekStat("${life.earlyStarts}/7", "early starts", Night.sun)
            WeekStat(if (life.jobsChecked == 0) "–" else "${life.jobsWon}/7", "evening jobs", Night.rose)
        }
        Spacer(Modifier.height(12.dp))
        com.dudlatitat.nextchapter.ui.life.WeekDots(state)
        RoutineQuestions(state, vm, key, onOpenRoutine)
        val studied = com.dudlatitat.nextchapter.data.Study.week(state, start)
        if (studied.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "⏱ Studied ${com.dudlatitat.nextchapter.data.Study.label(studied.values.sum())}: " +
                    studied.entries.sortedByDescending { it.value }.joinToString(" · ") { "${it.key} ${com.dudlatitat.nextchapter.data.Study.label(it.value)}" },
                style = MaterialTheme.typography.bodySmall, color = Night.ink,
            )
        }
        Spacer(Modifier.height(12.dp))
        if (saved != null && !editing) {
            Text("“$saved”", style = MaterialTheme.typography.bodyLarge, color = Night.ink)
            TextButton(onClick = { editing = true }) { Text("Edit", color = Night.muted) }
        } else {
            OutlinedTextField(
                note, { note = it.take(140) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("One line about this week") },
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { vm.setWeekNote(key, note); editing = false },
                enabled = note.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
            ) { Text("Save") }
        }
    }
}

/** The routine's Sunday questions: what worked, what to change. Then the schedule is one tap away. */
@Composable
private fun RoutineQuestions(state: AppState, vm: MainViewModel, key: String, onOpenRoutine: () -> Unit) {
    val saved = state.routineReviews[key]
    var worked by remember(key) { mutableStateOf(saved?.worked ?: "") }
    var change by remember(key) { mutableStateOf(saved?.change ?: "") }
    var editing by remember(key) { mutableStateOf(saved == null) }
    Spacer(Modifier.height(12.dp))
    if (!editing && saved != null) {
        if (saved.worked.isNotBlank()) Text("✓ Worked: ${saved.worked}", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
        if (saved.change.isNotBlank()) Text("↻ Change: ${saved.change}", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
        Row {
            TextButton(onClick = { editing = true }) { Text("Edit", color = Night.muted) }
            TextButton(onClick = onOpenRoutine) { Text("Edit the schedule →", color = Night.sun) }
        }
    } else {
        OutlinedTextField(worked, { worked = it.take(120) }, modifier = Modifier.fillMaxWidth(), label = { Text("What worked this week?") })
        OutlinedTextField(change, { change = it.take(120) }, modifier = Modifier.fillMaxWidth(), label = { Text("What would you change?") })
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { vm.setRoutineReview(key, com.dudlatitat.nextchapter.data.RoutineReview(worked.trim(), change.trim())); editing = false },
                enabled = worked.isNotBlank() || change.isNotBlank(),
            ) { Text("Save", color = Night.sun) }
            TextButton(onClick = onOpenRoutine) { Text("Edit the schedule →", color = Night.sun) }
        }
    }
}

@Composable
private fun WeekStat(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

/** Past Sunday notes, newest first (on Progress). */
@Composable
fun WeekNotes(state: AppState, bare: Boolean = false) {
    if (state.weekNotes.isEmpty()) return
    val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US)
    val body: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
        state.weekNotes.entries.sortedByDescending { it.key }.take(12).forEach { (week, line) ->
            val start = try { java.time.LocalDate.parse(week) } catch (_: Exception) { null }
            Row(Modifier.fillMaxWidth()) {
                Text(start?.format(fmt) ?: week, style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.width(56.dp))
                Text(line, style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            }
            Spacer(Modifier.height(6.dp))
        }
    }
    if (bare) Column(content = body) else GlassCard(content = body)
}
