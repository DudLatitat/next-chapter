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
import androidx.compose.material.icons.rounded.Search
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
    onTool: (String) -> Unit = {}, onLifePage: (String) -> Unit = {}, onOpenChinese: () -> Unit = {},
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

    // Unfolded: your clock, the Now card and today's three on the left; the day line and the
    // pillars on the right. During her call only the call card shows.
    val wide = com.dudlatitat.nextchapter.ui.components.isWide()
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val kind = remember(state, minute) { com.dudlatitat.nextchapter.data.NowPlan.of(state) }
    val calm = kind == com.dudlatitat.nextchapter.data.NowPlan.Kind.CALL
    val quiet = kind == com.dudlatitat.nextchapter.data.NowPlan.Kind.WIND_DOWN
    val top: @Composable () -> Unit = {
        Header(state, vm, minute, onSettings, onOpenMove, onTrip = onStudy, onSearch = com.dudlatitat.nextchapter.ui.components.LocalAppNav.current.openSearch)
        if (state.tripModeOn()) TripModeCard(state, vm, onPhrasebook)
        NowHero(
            kind, state, vm, startJob, onPray = { prayerOpen = true },
            onReview = { if (Srs.queue(state).isEmpty()) onRedo() else onStudy() }, onDrive = { onTool("drive") }, onAPlus = onAPlus,
        )
        if (!calm) TodayThree(
            state, calm = calm, quiet = quiet,
            onItem = { item ->
                when (item.kind) {
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.REVIEW -> if (Srs.queue(state).isEmpty()) onRedo() else onStudy()
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.PLAY -> item.tool?.let(onTool)
                    com.dudlatitat.nextchapter.data.DayPlan.Kind.LIFE -> { if (item.tool == "aplus") onAPlus() else { jobOpen = true } }
                }
                Unit
            },
            onPrayer = { prayerOpen = true },
        )
    }
    val rest: @Composable () -> Unit = {
        if (!calm) {
            DayTimeline(state, nowMin)
            PillarStrip(state) { id ->
                when (id) {
                    com.dudlatitat.nextchapter.data.Pillars.Id.TALK -> onOpenChinese()
                    com.dudlatitat.nextchapter.data.Pillars.Id.CAREER -> onOpenLife()
                    com.dudlatitat.nextchapter.data.Pillars.Id.MONEY, com.dudlatitat.nextchapter.data.Pillars.Id.US -> onOpenMove()
                    com.dudlatitat.nextchapter.data.Pillars.Id.HEALTH -> onLifePage("routine")
                    com.dudlatitat.nextchapter.data.Pillars.Id.FAITH -> prayerOpen = true
                }
            }
            // The whole plan as one line: trips, exams, the move.
            if (com.dudlatitat.nextchapter.data.Road.marks(state).isNotEmpty()) GlassCard(onClick = onOpenMove) {
                Text("The road to 成都", style = MaterialTheme.typography.labelLarge, color = Night.muted)
                Spacer(Modifier.height(8.dp))
                com.dudlatitat.nextchapter.ui.components.RoadStrip(state)
            }
        }
        // Tonight's sentence (starred in 造句 Build), 6–7 pm: right before her call, never during it.
        if (!calm && com.dudlatitat.nextchapter.data.TonightLine.showing(state.tonight, today(), nowMin)) state.tonight?.let { t ->
            GlassCard(highlight = true, onClick = { vm.speaker.say(t.hanzi) }) {
                Text("Say this to ${state.herName.trim().ifEmpty { "her" }} tonight 💛", style = MaterialTheme.typography.labelMedium, color = Night.rose)
                Spacer(Modifier.height(6.dp))
                HanziText(t.hanzi + "  🔊", 24f, align = null)
                PinyinText(t.pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(t.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        val sundayAfternoon = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.SUNDAY && part != Life.Part.MORNING && !calm
        if (sundayAfternoon) SundayReview(state, vm, onOpenRoutine = { onLifePage("routine") })
        // What's coming and (Mondays) last week, as one quiet list.
        val monday = java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.MONDAY && state.studyDays.isNotEmpty()
        val coming = com.dudlatitat.nextchapter.ui.life.comingUpSummary(state)
        if (!calm && (coming != null || monday)) com.dudlatitat.nextchapter.ui.components.ListGroup {
            if (coming != null) com.dudlatitat.nextchapter.ui.components.ListRow(
                "Coming up", subtitle = coming, divider = monday,
                leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("近", Night.sun) }, onClick = onOpenLife,
            )
            if (monday) com.dudlatitat.nextchapter.ui.components.ListRow(
                "Last week", subtitle = com.dudlatitat.nextchapter.ui.progress.recapSummary(state, lastWeek = true), divider = false,
                leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("周", Night.jade) }, onClick = { onLifePage("routine") },
            )
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
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Header(state: AppState, vm: MainViewModel, @Suppress("UNUSED_PARAMETER") minute: Long, onSettings: () -> Unit, onOpenMove: () -> Unit, onTrip: () -> Unit, onSearch: () -> Unit) {
    val now = ZonedDateTime.now()
    val her = Life.herTime(now)
    val hour = now.hour
    val helloZh = when { hour < 5 -> "夜深了"; hour < 12 -> "早上好"; hour < 18 -> "下午好"; else -> "晚上好" }
    val hello = when { hour < 5 -> "Up late"; hour < 12 -> "Good morning"; hour < 18 -> "Good afternoon"; else -> "Good evening" }
    val herMin = her.hour * 60 + her.minute
    val sky = com.dudlatitat.nextchapter.data.HerSky
    val tint by androidx.compose.animation.animateColorAsState(Color(sky.color(herMin)), androidx.compose.animation.core.tween(1500), label = "herSky")
    val phase = sky.phase(herMin)
    val trip = daysUntil(state.nextTripDate())
    val move = daysUntil(state.moveDate)
    val shape = RoundedCornerShape(28.dp)
    val nowMin = now.hour * 60 + now.minute
    // Her sky: the card is the colour of the sky where she is right now, fading into the night.
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(tint, tint.copy(alpha = 0.5f), Night.ground1.copy(alpha = 0.0f))))
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$helloZh · $hello, Rudy", style = MaterialTheme.typography.titleSmall, color = Night.sun, modifier = Modifier.weight(1f), maxLines = 1)
            IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, "Search", tint = Night.ink.copy(alpha = 0.8f)) }
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = Night.ink.copy(alpha = 0.8f)) }
        }
        // Your clock is the big one; hers sits right under it.
        Text(now.format(clock), style = MaterialTheme.typography.displaySmall, color = Night.ink)
        Text(
            "${state.herName.trim().ifEmpty { "Her time" }} ${her.format(clock)} · ${com.dudlatitat.nextchapter.data.NowPlan.herPart(now)} ${phase.emoji}" +
                Life.herDayWord(now).let { if (it.isBlank() || it == "today") "" else " · $it" },
            style = MaterialTheme.typography.bodyMedium, color = Night.rose, maxLines = 2,
        )
        Spacer(Modifier.height(4.dp))
        com.dudlatitat.nextchapter.ui.components.StatusStrip(listOfNotNull(
            com.dudlatitat.nextchapter.ui.components.Fact(com.dudlatitat.nextchapter.data.NowPlan.callLine(nowMin), Night.rose),
            trip?.takeIf { it >= 0 }?.let { com.dudlatitat.nextchapter.ui.components.Fact("✈ ${it}d", onClick = onOpenMove) },
        ))
        // Only when they apply: trip phrases (60 days before a trip) and the work day.
        val T = com.dudlatitat.nextchapter.data.TripPrep
        val P = com.dudlatitat.nextchapter.data.Places
        val atWork = P.atWork(state, today(), nowMin)
        if (T.open(state, today()) || atWork) androidx.compose.foundation.layout.FlowRow(
            Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (T.open(state, today())) {
                val said = T.saidToday(state, today())
                Pill(if (said >= T.GOAL) "✈️ trip phrases ✓" else "✈️ trip phrases $said/${T.GOAL}", Night.rose, onClick = onTrip)
            }
            if (atWork) {
                Pill("💼 till ~${com.dudlatitat.nextchapter.ui.life.clockLabel(P.workEnd(state.arrivals, today()) ?: nowMin)}", Night.violet)
                if (nowMin >= (P.workStart(state.arrivals, today()) ?: nowMin) + P.MIN_AT_WORK) {
                    Pill("I'm home 🏠", Night.sun, onClick = { vm.arrivedHomeNow() })
                }
            }
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
