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
import com.dudlatitat.nextchapter.data.Life
import com.dudlatitat.nextchapter.data.Prayers
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Stats
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night

/** Before noon: today's prayer, your early-start streak, and tonight's one goal. */
@Composable
fun MorningCard(state: AppState, vm: MainViewModel, onAPlus: () -> Unit = {}) {
    val day = today()
    val prayer = Prayers.forDay(day)
    val prayedToday = day in state.prayedDays
    val early = Srs.streak(state.wakeDays)
    var chinese by remember { mutableStateOf(false) }
    val amenScale by animateFloatAsState(if (prayedToday) 1f else 0.96f, label = "amen")

    GlassCard(highlight = true, modifier = Modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Morning · 早上", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
            if (early > 0) Pill("☀️ $early ${if (early == 1) "early start" else "early starts"}", Night.sun)
        }
        Spacer(Modifier.height(10.dp))

        // Today's prayer, opened with the Sign of the Cross.
        Text("✝  ${prayer.title} · ${prayer.titleZh}", style = MaterialTheme.typography.titleMedium, color = Night.ink)
        Spacer(Modifier.height(4.dp))
        Text(Prayers.signOfTheCross.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
        Spacer(Modifier.height(6.dp))
        Text(prayer.english, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
        TextButton(onClick = { chinese = !chinese }) { Text(if (chinese) "中文 ▴" else "中文 + pinyin ▾", color = Night.violet) }
        AnimatedVisibility(chinese) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    Prayers.signOfTheCross.hanzi, style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                    modifier = Modifier.clickable { vm.speaker.say(Prayers.signOfTheCross.hanzi) },
                )
                Text(
                    "${prayer.hanzi}  🔊", style = MaterialTheme.typography.titleMedium, color = Night.ink,
                    modifier = Modifier.clickable { vm.speaker.say(prayer.hanzi) },
                )
                PinyinText(prayer.pinyin, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(6.dp))
        if (prayedToday) Pill("🙏 Amen · prayed today", Night.jade)
        else Button(
            onClick = { vm.prayed() },
            modifier = Modifier.scale(amenScale),
            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
        ) { Text("Amen 🙏") }

        Spacer(Modifier.height(14.dp))
        com.dudlatitat.nextchapter.ui.life.MorningAPlus(state, vm, onMore = onAPlus)
        Spacer(Modifier.height(14.dp))
        GoalPicker(state, vm, title = "Tonight's goal")
    }
}

/**
 * The evening: pick tonight's goal (5–7 pm), call time with Yuling (7–8 pm, nothing else), and
 * a gentle "did you get to it?" after 8. Shows nothing in the afternoon.
 */
@Composable
fun EveningCard(state: AppState, vm: MainViewModel, part: Life.Part) {
    val goal = Life.goalFor(state)
    when (part) {
        Life.Part.PLAN -> GlassCard {
            Text("Tonight · 今晚", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            GoalPicker(state, vm, title = "One thing before her call")
            Spacer(Modifier.height(8.dp))
            Text("7–8 pm is call time 💛 No reminders then.", style = MaterialTheme.typography.bodySmall, color = Night.rose)
        }
        Life.Part.CALL -> GlassCard {
            Text("Call time · 💛", style = MaterialTheme.typography.titleLarge, color = Night.rose)
            Text("Everything else can wait until 8.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            goal?.takeIf { it.done == null }?.let { Text("After: ${it.text}", style = MaterialTheme.typography.bodySmall, color = Night.muted) }
        }
        Life.Part.LATE -> {
            if (goal == null) return
            GlassCard(modifier = Modifier.animateContentSize()) {
                Text("Tonight · 今晚", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Text(goal.text, style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Spacer(Modifier.height(8.dp))
                when (goal.done) {
                    null -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { vm.answerGoal(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2),
                        ) { Text("Done ✓") }
                        OutlinedButton(onClick = { vm.answerGoal(false) }) { Text("Not today", color = Night.muted) }
                    }
                    true -> Text("Done ✓ 很好! One step closer.", style = MaterialTheme.typography.bodyMedium, color = Night.jade)
                    false -> Text("That's okay. Tomorrow's another day. 明天见.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                }
            }
        }
        else -> {}
    }
}

/** Pick tonight's one goal: a quick choice, or your own words. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalPicker(state: AppState, vm: MainViewModel, title: String) {
    val goal = Life.goalFor(state)
    var changing by remember { mutableStateOf(false) }
    var writing by remember { mutableStateOf(false) }
    Text(title, style = MaterialTheme.typography.labelMedium, color = Night.muted)
    if (goal != null && !changing) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎯 ${goal.text}", style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.weight(1f))
            if (goal.done == null) TextButton(onClick = { changing = true }) { Text("Change", color = Night.muted) }
        }
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Life.goalIdeas.forEach { idea ->
            FilterChip(selected = goal?.text == idea, onClick = { vm.setGoal(idea); changing = false }, label = { Text(idea) })
        }
        FilterChip(selected = false, onClick = { writing = true }, label = { Text("Other…") })
    }
    if (writing) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { writing = false },
            title = { Text("Tonight's goal") },
            text = { OutlinedTextField(text, { text = it.take(60) }, singleLine = true, label = { Text("e.g. Finish the subnetting chapter") }) },
            confirmButton = {
                TextButton(onClick = { if (text.isNotBlank()) vm.setGoal(text); writing = false; changing = false }) { Text("Set", color = Night.sun) }
            },
            dismissButton = { TextButton(onClick = { writing = false }) { Text("Cancel", color = Night.muted) } },
        )
    }
}

/** Sundays: the week in numbers, and one line to remember it by. */
@Composable
fun SundayReview(state: AppState, vm: MainViewModel) {
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
            WeekStat(if (life.goalsSet == 0) "–" else "${life.goalsDone}/${life.goalsSet}", "goals done", Night.rose)
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

@Composable
private fun WeekStat(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

/** Past Sunday notes, newest first (on Progress). */
@Composable
fun WeekNotes(state: AppState) {
    if (state.weekNotes.isEmpty()) return
    val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US)
    GlassCard {
        state.weekNotes.entries.sortedByDescending { it.key }.take(12).forEach { (week, line) ->
            val start = try { java.time.LocalDate.parse(week) } catch (_: Exception) { null }
            Row(Modifier.fillMaxWidth()) {
                Text(start?.format(fmt) ?: week, style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.width(56.dp))
                Text(line, style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
