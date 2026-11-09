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
 * Today is the hub: a warm header (your time and Yuling's, the trip), four rings for today's
 * small things (Chinese, A+, prayer, tonight's goal), and tiles into the rest of the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: AppState, vm: MainViewModel, onStudy: () -> Unit, onOpenMove: () -> Unit, onSettings: () -> Unit, onRedo: () -> Unit,
    onPhrasebook: () -> Unit = {}, onOpenLife: () -> Unit = {}, onAPlus: () -> Unit = {},
    onTool: (String) -> Unit = {}, onOpenProgress: () -> Unit = {},
) {
    // Opening the app before 7 am counts toward the early-start streak.
    LaunchedEffect(Unit) { if (Life.earlyStart(LocalTime.now())) vm.markEarlyStart() }
    val part = Life.partOf(LocalTime.now())
    var prayerOpen by remember { mutableStateOf(false) }
    var goalOpen by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Hero(state, part, onSettings, onOpenMove)
        if (state.tripModeOn()) TripModeCard(state, vm, onPhrasebook)
        TodayRings(state, part, onChinese = { if (Srs.queue(state).isEmpty()) onRedo() else onStudy() }, onAPlus = onAPlus, onPrayer = { prayerOpen = true }, onGoal = { goalOpen = true })

        // After 8 pm, a goal that's waiting for an answer gets its gentle check-in.
        val goal = Life.goalFor(state)
        if (part == Life.Part.LATE && goal != null && goal.done == null) EveningCard(state, vm, part)
        if (java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.SUNDAY) SundayReview(state, vm)
        if (java.time.LocalDate.now().dayOfWeek == java.time.DayOfWeek.MONDAY && state.studyDays.isNotEmpty()) {
            com.dudlatitat.nextchapter.ui.components.Collapsible(
                "today.lastweek", "Last week", summary = com.dudlatitat.nextchapter.ui.progress.recapSummary(state, lastWeek = true),
            ) { com.dudlatitat.nextchapter.ui.progress.WeeklyRecap(state, lastWeek = true, bare = true) }
        }
        com.dudlatitat.nextchapter.ui.life.comingUpSummary(state)?.let {
            GlassCard { com.dudlatitat.nextchapter.ui.life.ComingUpList(state, onOpenLife) }
        }

        Tiles(state, onTool, onPhrasebook, onOpenMove, onOpenLife, onOpenProgress)
        state.phraseOfTheDay()?.let { PhraseLine(it, vm) }
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
    if (goalOpen) {
        // In the evening after the call, with tonight answered (or nothing set), plan tomorrow.
        val tomorrow = part == Life.Part.LATE && Life.goalFor(state)?.done != null
        AlertDialog(
            onDismissRequest = { goalOpen = false },
            title = { Text(if (tomorrow) "Tomorrow's one goal" else "Tonight's one goal") },
            text = { Column { GoalChooser(state, vm, day = today() + if (tomorrow) 1 else 0, onPicked = { goalOpen = false }) } },
            confirmButton = { TextButton(onClick = { goalOpen = false }) { Text("Done", color = Night.sun) } },
        )
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
        if (part == Life.Part.CALL) {
            Spacer(Modifier.height(10.dp))
            Text("💛 Call time" + state.herName.trim().let { if (it.isEmpty()) "" else " with $it" } + ". Everything else can wait until 8.", style = MaterialTheme.typography.bodyMedium, color = Night.rose)
        }
    }
}

/** Four rings for today's small things. Tap one to do it. */
@Composable
private fun TodayRings(state: AppState, part: Life.Part, onChinese: () -> Unit, onAPlus: () -> Unit, onPrayer: () -> Unit, onGoal: () -> Unit) {
    val d = today()
    val queue = Srs.queue(state)
    val zhDone = state.cards.count { it.last == d && it.due > d }
    val zhTotal = zhDone + queue.size
    val aDone = state.aplus.count { it.last == d }
    val aTotal = aDone + APlus.queue(state).size
    val prayed = d in state.prayedDays
    val goal = Life.goalFor(state)
    GlassCard {
        Text("Today · 今天", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Ring("Chinese", if (zhTotal == 0) 1f else zhDone / zhTotal.toFloat(), Night.jade, if (queue.isEmpty()) "✓" else "$zhDone/$zhTotal", onChinese)
            Ring("A+", if (aTotal == 0) 1f else aDone / aTotal.toFloat(), Night.violet, if (aTotal == 0 || aDone == aTotal) "✓" else "$aDone/$aTotal", onAPlus)
            Ring("Prayer", if (prayed) 1f else 0f, Night.sun, if (prayed) "✓" else "🙏", onPrayer)
            Ring(
                "Goal", when (goal?.done) { true -> 1f; null -> if (goal != null) 0.5f else 0f; false -> 0f }, Night.rose,
                when { goal?.done == true -> "✓"; goal != null -> "🎯"; else -> "+" }, onGoal,
                sub = goal?.text,
            )
        }
    }
}

@Composable
private fun Ring(label: String, progress: Float, color: Color, center: String, onClick: () -> Unit, sub: String? = null) {
    Column(Modifier.width(72.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(progress, 56.dp, color, stroke = 5.dp) {
            Text(center, style = MaterialTheme.typography.labelLarge, color = Night.ink)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Night.ink)
        if (sub != null) Text(sub, fontSize = 10.sp, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

/** Tiles into the rest of the app, each with a word on how it's going. */
@Composable
private fun Tiles(state: AppState, onTool: (String) -> Unit, onPhrasebook: () -> Unit, onOpenMove: () -> Unit, onOpenLife: () -> Unit, onOpenProgress: () -> Unit) {
    val tiles = listOf(
        // Rudy's layout (Layout Studio, Nov 2026): one row of three.
        Tile("📈", "Progress", com.dudlatitat.nextchapter.data.Stats.streak(state).let { if (it > 0) "🔥 $it-day streak" else "start a streak" }) { onOpenProgress() },
        Tile("📕", "Phrasebook", "big text") { onPhrasebook() },
        Tile("🚗", "Drive", "hands-free") { onTool("drive") },
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Night.card.copy(alpha = 0.85f))
                            .clickable(onClick = t.onClick).padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(t.emoji, fontSize = 22.sp)
                        Text(t.title, style = MaterialTheme.typography.labelLarge, color = Night.ink, maxLines = 1)
                        Text(t.sub, fontSize = 10.sp, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private class Tile(val emoji: String, val title: String, val sub: String, val onClick: () -> Unit)

/** Phrase of the day as one slim line, tap ▶ to hear it. */
@Composable
private fun PhraseLine(card: com.dudlatitat.nextchapter.data.Card, vm: MainViewModel) {
    GlassCard {
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
