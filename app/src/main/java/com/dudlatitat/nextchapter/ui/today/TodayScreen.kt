package com.dudlatitat.nextchapter.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.dudlatitat.nextchapter.ui.components.HanziText
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
import com.dudlatitat.nextchapter.ui.components.readable
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
    // The clock this screen runs on: ticks each minute and on coming back to the app, so the
    // greeting, the Now card, her time and "today" never go stale.
    val minute = rememberMinute()
    // Opening the app by 30 minutes after your wake time counts toward the early-start streak.
    LaunchedEffect(minute) { if (today() !in state.wakeDays && Life.earlyStart(LocalTime.now(), state.wakeMinutes)) vm.markEarlyStart() }
    val part = Life.partOf(LocalTime.now())
    var prayerOpen by remember { mutableStateOf(false) }
    var jobOpen by remember { mutableStateOf(false) }
    var bodyOpen by remember { mutableStateOf(false) }
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

    // Unfolded: the header and today's three on the left, this moment and the rest on the right.
    val wide = com.dudlatitat.nextchapter.ui.components.isWide()
    val top: @Composable () -> Unit = {
        Header(state, vm, minute, onSettings, onOpenMove, onTrip = onStudy)
        if (state.tripModeOn()) TripModeCard(state, vm, onPhrasebook)
        TodayThree(
            state, calm = part == Life.Part.CALL,
            onItem = { item ->
                when (item.kind) {
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.REVIEW -> if (Srs.queue(state).isEmpty()) onRedo() else onStudy()
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.PLAY -> item.tool?.let(onTool)
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.LIFE -> { if (item.tool == "aplus") onAPlus() else { jobOpen = true } }
                }
                Unit
            },
            onPrayer = { prayerOpen = true }, onWeek = { onLifePage("routine") },
        )
    }
    val rest: @Composable () -> Unit = {
        NowCard(state, vm, part, minute, startJob, onPray = { prayerOpen = true }, onChinese = { if (Srs.queue(state).isEmpty()) onRedo() else onStudy() }, onAPlus = onAPlus, onDrive = { onTool("drive") })
        // Tonight's sentence (starred in 造句 Build), 6–7 pm: right before her call, never during it.
        val nm = LocalTime.now().let { it.hour * 60 + it.minute }
        if (com.dudlatitat.nextchapter.data.TonightLine.showing(state.tonight, today(), nm)) state.tonight?.let { t ->
            GlassCard(highlight = true, onClick = { vm.speaker.say(t.hanzi) }) {
                Text("Say this to ${state.herName.trim().ifEmpty { "her" }} tonight 💛", style = MaterialTheme.typography.labelMedium, color = Night.rose)
                Spacer(Modifier.height(6.dp))
                HanziText(t.hanzi + "  🔊", 24f, align = null)
                PinyinText(t.pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(t.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        val sundayAfternoon = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.SUNDAY && part != Life.Part.MORNING && part != Life.Part.CALL
        if (sundayAfternoon) SundayReview(state, vm, onOpenRoutine = { onLifePage("routine") })
        // Everything else for today, folded: coming up, body, last week (Mondays).
        val monday = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.MONDAY && state.studyDays.isNotEmpty()
        val coming = com.dudlatitat.nextchapter.ui.life.comingUpSummary(state)
        com.dudlatitat.nextchapter.ui.components.Collapsible(
            "today.more", "More today",
            summary = listOfNotNull(coming, com.dudlatitat.nextchapter.data.Body.pill(state, today()), if (monday) "last week" else null).joinToString(" · "),
        ) {
            Pill(com.dudlatitat.nextchapter.data.Body.pill(state, today()), Night.violet, onClick = { bodyOpen = true })
            if (coming != null) { Spacer(Modifier.height(10.dp)); com.dudlatitat.nextchapter.ui.life.ComingUpList(state, onOpenLife) }
            if (monday) {
                Spacer(Modifier.height(12.dp))
                Text("Last week", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                com.dudlatitat.nextchapter.ui.progress.WeeklyRecap(state, lastWeek = true, bare = true)
            }
        }

    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable(if (wide) 1100.dp else 720.dp).padding(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (wide) com.dudlatitat.nextchapter.ui.components.TwoPanes(top, rest, leftWeight = 1.1f) else { top(); rest() }
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
                // Learn it by heart (Practice → Prayer ladder).
                androidx.compose.material3.TextButton(onClick = { prayerOpen = false; onTool("prayer") }) {
                    Text("🏮 Learn it by heart ›", color = Night.sun)
                }
            }
        }
    }
    if (bodyOpen) {
        ModalBottomSheet(
            onDismissRequest = { bodyOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Night.ground1,
        ) { BodySheet(state, vm) }
    }
    if (jobOpen) {
        ModalBottomSheet(
            onDismissRequest = { jobOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Night.ground1,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 32.dp)) {
                TonightJob(state, vm, startJob, bare = true)
                // Tomorrow's a day off? One tap plans it like Saturday (change it in the routine).
                val tomorrow = today() + 1
                val off = com.dudlatitat.nextchapter.data.Routine.dayOff(state.daysOff, tomorrow)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { if (off == null) vm.setDayOff(tomorrow, 6) else vm.removeDayOff(tomorrow) }) {
                        Text(if (off == null) "Tomorrow off 🏖" else "Tomorrow's off 🏖 · undo", color = Night.jade)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { jobOpen = false; onLifePage("routine") }) { Text("The whole week →", color = Night.sun) }
                }
            }
        }
    }
}

/**
 * The header: greeting, both clocks, and one line of countdowns. Its sky follows hers: the
 * background is the colour of the sky in Chengdu right now (dawn rose, pale day, dusk, deep
 * night), fading as her day turns (`HerSky`).
 */
@Composable
private fun Header(state: AppState, vm: MainViewModel, @Suppress("UNUSED_PARAMETER") minute: Long, onSettings: () -> Unit, onOpenMove: () -> Unit, onTrip: () -> Unit) {
    val now = ZonedDateTime.now()
    val her = Life.herTime(now)
    val hour = now.hour
    val (hello, helloZh) = when {
        hour < 5 -> "Up late" to "夜深了"
        hour < 12 -> "Good morning" to "早上好"
        hour < 18 -> "Good afternoon" to "下午好"
        else -> "Good evening" to "晚上好"
    }
    val herMin = her.hour * 60 + her.minute
    val sky = com.dudlatitat.nextchapter.data.HerSky
    val tint by androidx.compose.animation.animateColorAsState(Color(sky.color(herMin)), androidx.compose.animation.core.tween(1500), label = "herSky")
    val phase = sky.phase(herMin)
    val trip = daysUntil(state.nextTripDate())
    val move = daysUntil(state.moveDate)
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(tint, Night.cardBottom)))
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(helloZh, style = MaterialTheme.typography.titleMedium, color = Night.sun)
                Text("$hello, Rudy", style = MaterialTheme.typography.titleLarge, color = Night.ink, maxLines = 2)
            }
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = Night.ink.copy(alpha = 0.7f)) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                Text("You", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                Text(now.format(clock), style = MaterialTheme.typography.titleMedium, color = Night.ink)
            }
            Column {
                Text("${Life.herLabel(state.herName, state.herCity)} ${phase.emoji} ${phase.word}", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                Text(her.format(clock) + " " + Life.herDayWord(now), style = MaterialTheme.typography.titleMedium, color = Night.rose)
            }
        }
        // One line of countdowns (tap for Move): the trip with how ready you are, then the move.
        val parts = listOfNotNull(
            trip?.takeIf { it >= 0 }?.let { "✈️ 成都 ${friendlySpan(it)} · ready ${com.dudlatitat.nextchapter.data.Ready.family(state).percent}%" },
            move?.takeIf { it >= 0 }?.let { "搬家 ${friendlySpan(it)}" },
        )
        if (parts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                parts.joinToString("   ·   "), style = MaterialTheme.typography.bodyMedium, color = Night.ink,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onOpenMove).padding(vertical = 6.dp),
            )
        }
        // Only when it applies: trip phrases (60 days before a trip) and the work day.
        val T = com.dudlatitat.nextchapter.data.TripPrep
        val P = com.dudlatitat.nextchapter.data.Places
        val nowMin = now.hour * 60 + now.minute
        val atWork = P.atWork(state, today(), nowMin)
        if (T.open(state, today()) || atWork) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (T.open(state, today())) {
                    val said = T.saidToday(state, today())
                    Pill(
                        if (said >= T.GOAL) "✈️ trip phrases ✓" else "✈️ trip phrases $said/${T.GOAL}",
                        Night.rose, onClick = onTrip,
                    )
                }
                if (atWork) {
                    Pill("💼 till ~${com.dudlatitat.nextchapter.ui.life.clockLabel(P.workEnd(state.arrivals, today()) ?: nowMin)}", Night.violet)
                    if (nowMin >= (P.workStart(state.arrivals, today()) ?: nowMin) + P.MIN_AT_WORK) {
                        TextButton(onClick = { vm.arrivedHomeNow() }) { Text("I'm home 🏠", color = Night.sun) }
                    }
                }
            }
        }
    }
}

/**
 * The one card for right now, for what the rings can't say: call time → heading home (Drive mode
 * for the ride) → tonight's job (Start and
 * check-in) → wind down → otherwise the phrase of the day. (Phrases, A+ and prayer are the rings.)
 */
@Composable
private fun NowCard(
    state: AppState, vm: MainViewModel, part: Life.Part, @Suppress("UNUSED_PARAMETER") minute: Long, startJob: (com.dudlatitat.nextchapter.data.RoutineDay) -> Unit,
    onPray: () -> Unit, onChinese: () -> Unit, onAPlus: () -> Unit, onDrive: () -> Unit,
) {
    val R = com.dudlatitat.nextchapter.data.Routine
    val job = R.jobOn(state, java.time.LocalDate.now())
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
        com.dudlatitat.nextchapter.data.Places.headingHome(state, today(), nowMin) -> NowLine(
            "Heading home? 🚗", "Drive mode: phrases out loud, hands-free, for the ride.", "Drive", Night.jade, onDrive,
        )
        unanswered && nowMin >= minOf(com.dudlatitat.nextchapter.data.Places.homeTime(state, today()), job.minutes) - 15 -> TonightJob(state, vm, startJob)
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

/** Phrase of the day as one slim row, tap ▶ to hear it. */
@Composable
private fun PhraseRow(card: com.dudlatitat.nextchapter.data.Card, vm: MainViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Phrase of the day", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                // Pinyin under the characters, so long phrases don't squeeze it into a column.
                HanziText(card.hanzi, 22f, align = null)
                PinyinText(card.pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(card.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            IconButton(onClick = { vm.speaker.say(card.hanzi) }) { Icon(Icons.Rounded.PlayArrow, "Listen", tint = Night.ink) }
        }
    }
}

/** The current minute (epoch minutes): changes every minute and whenever the app comes back. */
@Composable
fun rememberMinute(): Long {
    var m by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis() / 60_000) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000 - System.currentTimeMillis() % 60_000 + 50)
            m = System.currentTimeMillis() / 60_000
        }
    }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { m = System.currentTimeMillis() / 60_000; onPauseOrDispose { } }
    return m
}
