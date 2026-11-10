package com.dudlatitat.nextchapter.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.APlus
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Life
import com.dudlatitat.nextchapter.data.Prayers
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.friendlySpan
import com.dudlatitat.nextchapter.data.moveReady
import com.dudlatitat.nextchapter.data.nextTripDate
import com.dudlatitat.nextchapter.data.phraseOfTheDay
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.ProgressRing
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val clock = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

/**
 * Today answers "what now?": a warm header (your time and hers, the trip), four rings for today's
 * small things, then ONE card for this moment (prayer, practice, tonight's job, call time, wind
 * down), and what's coming up. Everything else lives in its own tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: AppState, vm: MainViewModel, onStudy: () -> Unit, onOpenMove: () -> Unit, onSettings: () -> Unit, onRedo: () -> Unit,
    onPhrasebook: () -> Unit = {}, onOpenLife: () -> Unit = {}, onAPlus: () -> Unit = {},
    onTool: (String) -> Unit = {}, onLifePage: (String) -> Unit = {},
) {
    // Opening the app by 30 minutes after your wake time counts toward the early-start streak.
    LaunchedEffect(Unit) { if (Life.earlyStart(LocalTime.now(), state.wakeMinutes)) vm.markEarlyStart() }
    val part = Life.partOf(LocalTime.now())
    var prayerOpen by remember { mutableStateOf(false) }
    var jobOpen by remember { mutableStateOf(false) }
    // Start opens the right place for tonight's job; anything else starts the study timer.
    val startJob: (com.dudlatitat.nextchapter.data.RoutineDay) -> Unit = { job ->
        jobOpen = false
        when (job.kind) {
            "aplus" -> onLifePage("test")
            "aplus-review" -> onAPlus()
            else -> {
                if (state.timerStart == null) vm.startTimer(if (job.kind == "chinese") "Chinese" else "Other")
                onLifePage("timer")
            }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Hero(state, part, onSettings, onOpenMove)
        if (state.tripModeOn()) TripModeCard(state, vm, onPhrasebook)
        TodayRings(state, part, onChinese = { if (Srs.queue(state).isEmpty()) onRedo() else onStudy() }, onAPlus = onAPlus, onPrayer = { prayerOpen = true }, onJob = { jobOpen = true }, onWeek = { onLifePage("routine") })

        NowCard(state, vm, part, startJob, onPray = { prayerOpen = true }, onChinese = { if (Srs.queue(state).isEmpty()) onRedo() else onStudy() }, onAPlus = onAPlus)
        val sundayAfternoon = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.SUNDAY && part != Life.Part.MORNING && part != Life.Part.CALL
        if (sundayAfternoon) SundayReview(state, vm, onOpenRoutine = { onLifePage("routine") })
        if (java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.MONDAY && state.studyDays.isNotEmpty()) {
            com.dudlatitat.nextchapter.ui.components.Collapsible(
                "today.lastweek", "Last week", summary = com.dudlatitat.nextchapter.ui.progress.recapSummary(state, lastWeek = true),
            ) { com.dudlatitat.nextchapter.ui.progress.WeeklyRecap(state, lastWeek = true, bare = true) }
        }
        com.dudlatitat.nextchapter.ui.life.comingUpSummary(state)?.let {
            GlassCard { com.dudlatitat.nextchapter.ui.life.ComingUpList(state, onOpenLife) }
        }

        Spacer(Modifier.height(8.dp))
    }

    if (prayerOpen) {
        ModalBottomSheet(
            onDismissRequest = { prayerOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Night.ground1,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 32.dp)) {
                Text("🙏 Prayer · 祈祷", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Spacer(Modifier.height(8.dp))
                PrayerBlock(vm, Prayers.forDay(today()))
                Spacer(Modifier.height(14.dp))
                AmenButton(state, vm)
            }
        }
    }
    if (jobOpen) {
        ModalBottomSheet(
            onDismissRequest = { jobOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Night.ground1,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 32.dp)) {
                TonightJob(state, vm, startJob, bare = true)
                TextButton(onClick = { jobOpen = false; onLifePage("routine") }) { Text("The whole week →", color = Night.sun) }
            }
        }
    }
}

/** The warm header: greeting, both clocks, the trip, the streaks, and a call-time note. */
@Composable
private fun Hero(state: AppState, part: Life.Part, onSettings: () -> Unit, onOpenMove: () -> Unit) {
    val now = ZonedDateTime.now()
    val her = Life.herTime(now)
    val hour = now.hour
    val (hello, helloZh) = when {
        hour < 5 -> "Up late" to "夜深了"
        hour < 12 -> "Good morning" to "早上好"
        hour < 18 -> "Good afternoon" to "下午好"
        else -> "Good evening" to "晚上好"
    }
    val herSky = if (her.hour in 6..18) "☀️" else "🌙"
    val streak = com.dudlatitat.nextchapter.data.Stats.streak(state)
    val early = Srs.streak(state.wakeDays)
    val trip = daysUntil(state.nextTripDate())
    val move = daysUntil(state.moveDate)
    GlassCard(highlight = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(helloZh, style = MaterialTheme.typography.titleMedium, color = Night.sun)
                Text("$hello, Rudy", style = MaterialTheme.typography.titleLarge, color = Night.ink, maxLines = 2)
            }
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = Night.muted) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column {
                Text("You", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                Text(now.format(clock), style = MaterialTheme.typography.titleMedium, color = Night.ink)
            }
            Column {
                Text("${Life.herLabel(state.herName, state.herCity)} $herSky", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                Text(her.format(clock) + " " + Life.herDayWord(now), style = MaterialTheme.typography.titleMedium, color = Night.rose)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.clickable(onClick = onOpenMove), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            trip?.takeIf { it >= 0 }?.let { Pill("✈️ 成都 in ${friendlySpan(it)}", Night.rose) }
            move?.takeIf { it >= 0 }?.let { Pill("搬家 ${friendlySpan(it)}", Night.sun) }
        }
        if (streak > 0 || early > 0) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (streak > 0) Pill("🔥 $streak-day streak", Night.sun)
                if (early > 0) Pill("☀️ $early early", Night.jade)
            }
        }
    }
}

/**
 * The one card for right now, for what the rings can't say: call time → tonight's job (Start and
 * check-in) → wind down → otherwise the phrase of the day. (Phrases, A+ and prayer are the rings.)
 */
@Composable
private fun NowCard(
    state: AppState, vm: MainViewModel, part: Life.Part, startJob: (com.dudlatitat.nextchapter.data.RoutineDay) -> Unit,
    onPray: () -> Unit, onChinese: () -> Unit, onAPlus: () -> Unit,
) {
    val R = com.dudlatitat.nextchapter.data.Routine
    val job = R.jobFor(state.routine, java.time.LocalDate.now())
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val unanswered = R.checkFor(state.routineChecks, today()) == null
    val her = state.herName.trim()
    when {
        part == Life.Part.CALL -> GlassCard(highlight = true) {
            Text("Call time 💛", style = MaterialTheme.typography.headlineSmall, color = Night.rose)
            Text(
                (if (her.isEmpty()) "She's" else "$her is") + " starting her day. Everything else can wait until 8.",
                style = MaterialTheme.typography.bodyMedium, color = Night.ink,
            )
            if (unanswered) Text("After: ${R.text(job.task, her)}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        unanswered && nowMin >= minOf(state.homeMinutes, job.minutes) - 15 -> TonightJob(state, vm, startJob)
        part == Life.Part.LATE -> GlassCard {
            Text("Winding down 🌙", style = MaterialTheme.typography.titleLarge, color = Night.ink)
            Text(
                "You did enough today. Phone down by ${com.dudlatitat.nextchapter.ui.life.clockLabel(state.windDownMinutes)}.",
                style = MaterialTheme.typography.bodyMedium, color = Night.muted,
            )
            state.phraseOfTheDay()?.let { Spacer(Modifier.height(10.dp)); PhraseRow(it, vm) }
        }
        // The rings already say what's left to do; the rest of the day, a line to carry with you.
        else -> state.phraseOfTheDay()?.let { GlassCard { PhraseRow(it, vm) } }
    }
}

/** A Now card with one sentence and one button. */
@Composable
private fun NowLine(title: String, line: String, button: String, color: Color, onClick: () -> Unit) {
    GlassCard(highlight = true, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text(line, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            }
            Spacer(Modifier.width(10.dp))
            androidx.compose.material3.Button(
                onClick = onClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color, contentColor = Night.ground2),
            ) { Text(button) }
        }
    }
}

/** Four rings for today's small things. Tap one to do it. */
@Composable
private fun TodayRings(state: AppState, part: Life.Part, onChinese: () -> Unit, onAPlus: () -> Unit, onPrayer: () -> Unit, onJob: () -> Unit, onWeek: () -> Unit) {
    val d = today()
    val queue = Srs.queue(state)
    val zhDone = state.cards.count { it.last == d && it.due > d }
    val zhTotal = zhDone + queue.size
    val aDone = state.aplus.count { it.last == d }
    val aTotal = aDone + APlus.queue(state).size
    val prayed = d in state.prayedDays
    val R = com.dudlatitat.nextchapter.data.Routine
    val job = R.jobFor(state.routine, java.time.LocalDate.now())
    val checked = R.checkFor(state.routineChecks, d)
    val calm = part == Life.Part.CALL
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val jobTime = checked == null && nowMin >= minOf(state.homeMinutes, job.minutes) - 15
    GlassCard {
        Text("Today · 今天", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Ring("Chinese", if (zhTotal == 0) 1f else zhDone / zhTotal.toFloat(), Night.jade, if (queue.isEmpty()) "✓" else "$zhDone/$zhTotal", onChinese, breathe = !calm && queue.isNotEmpty())
            Ring("A+", if (aTotal == 0) 1f else aDone / aTotal.toFloat(), Night.violet, if (aTotal == 0 || aDone == aTotal) "✓" else "$aDone/$aTotal", onAPlus, breathe = !calm && aTotal > 0 && aDone < aTotal)
            Ring("Prayer", if (prayed) 1f else 0f, Night.sun, if (prayed) "✓" else "🙏", onPrayer, breathe = !calm && !prayed)
            Ring(
                "Tonight", when (checked) { com.dudlatitat.nextchapter.data.CheckIn.DONE -> 1f; com.dudlatitat.nextchapter.data.CheckIn.MINIMUM -> 0.6f; else -> 0f }, Night.rose,
                when (checked) { com.dudlatitat.nextchapter.data.CheckIn.DONE -> "✓"; com.dudlatitat.nextchapter.data.CheckIn.MINIMUM -> "½"; com.dudlatitat.nextchapter.data.CheckIn.SKIPPED -> "🌙"; null -> "→" }, onJob,
                sub = R.text(job.task, state.herName), breathe = !calm && jobTime,
            )
        }
        // Your week of evenings, at a glance (tap for the whole routine).
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onWeek).padding(vertical = 4.dp)) {
            val streak = R.streak(state.routineChecks, d)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This week", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                if (streak > 0) Text("🔥 $streak", style = MaterialTheme.typography.labelMedium, color = Night.sun)
            }
            Spacer(Modifier.height(6.dp))
            com.dudlatitat.nextchapter.ui.life.WeekDots(state)
        }
    }
}

@Composable
private fun Ring(label: String, progress: Float, color: Color, center: String, onClick: () -> Unit, sub: String? = null, breathe: Boolean = false) {
    // Unfinished rings breathe in their own color: a soft glow swells behind the ring and fades,
    // slow enough to feel calm, bright enough to notice. Done rings sit still.
    val pulse = androidx.compose.animation.core.rememberInfiniteTransition(label = "breathe")
    val breath by pulse.animateFloat(
        0f, 1f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(1600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "breath",
    )
    val b = if (breathe) breath else 0f
    Column(Modifier.width(72.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.foundation.layout.Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            if (breathe) androidx.compose.foundation.Canvas(Modifier.size(64.dp)) {
                drawCircle(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(color.copy(alpha = 0.15f + 0.40f * b), color.copy(alpha = 0f)),
                        center = this.center, radius = this.size.minDimension / 2,
                    ),
                    radius = this.size.minDimension / 2 * (0.8f + 0.2f * b),
                )
            }
            ProgressRing(
                progress, 56.dp, color, stroke = 5.dp,
                modifier = Modifier.graphicsLayer { val sc = 1f + 0.08f * b; scaleX = sc; scaleY = sc },
            ) {
            Text(center, style = MaterialTheme.typography.labelLarge, color = Night.ink)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Night.ink)
        if (sub != null) Text(sub, fontSize = 10.sp, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

/** Phrase of the day as one slim row, tap ▶ to hear it. */
@Composable
private fun PhraseRow(card: com.dudlatitat.nextchapter.data.Card, vm: MainViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Phrase of the day", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(card.hanzi, fontSize = 22.sp, color = Night.ink)
                    Spacer(Modifier.width(10.dp))
                    PinyinText(card.pinyin, style = MaterialTheme.typography.bodyMedium)
                }
                Text(card.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            IconButton(onClick = { vm.speaker.say(card.hanzi) }) { Icon(Icons.Rounded.PlayArrow, "Listen", tint = Night.ink) }
        }
    }
}
