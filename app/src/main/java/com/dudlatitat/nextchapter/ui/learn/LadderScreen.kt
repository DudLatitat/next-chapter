package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Callouts.Verdict
import com.dudlatitat.nextchapter.data.Ladder
import com.dudlatitat.nextchapter.data.Prayer
import com.dudlatitat.nextchapter.data.Prayers
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.speech.verdict
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch

/** Practice → Prayer: learn a prayer by heart, rung by rung. The Our Father comes first. */
@Composable
fun LadderScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }
    val prayer = Prayers.daily.firstOrNull { it.title == open }
    if (prayer == null) Picker(state, onBack) { open = it.title }
    else Climb(state, vm, prayer, onBack = { open = null })
}

@Composable
private fun Picker(state: AppState, onBack: () -> Unit, onOpen: (Prayer) -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column {
                Text("Prayer ladder", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("祷告 · learn it by heart, one rung at a time", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        Text(
            "Each rung covers more of the words. Say every line out loud to climb. The words are the prayer exactly as written; nothing is changed, only hidden.",
            style = MaterialTheme.typography.bodyMedium, color = Night.muted,
        )
        Prayers.daily.forEachIndexed { i, p ->
            val rung = state.prayerSteps[p.title] ?: 0
            GlassCard(onClick = { onOpen(p) }, highlight = i == 0 && rung == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${p.title} · ${p.titleZh}", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        Text(
                            when {
                                Ladder.byHeart(state.prayerSteps, p.title) -> "By heart ✓"
                                rung == 0 -> if (i == 0) "Start here" else "Not started"
                                else -> "Rung $rung of ${Ladder.TOP + 1}"
                            },
                            style = MaterialTheme.typography.bodySmall, color = if (Ladder.byHeart(state.prayerSteps, p.title)) Night.jade else Night.muted,
                        )
                    }
                    Rungs(rung)
                }
            }
        }
    }
}

/** Five little lanterns, lit up to the rung you've reached. */
@Composable
private fun Rungs(reached: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(Ladder.TOP + 1) { i ->
            Box(Modifier.size(10.dp, 14.dp).background(if (i < reached) Night.sun else Night.ink.copy(alpha = 0.12f), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun Climb(state: AppState, vm: MainViewModel, prayer: Prayer, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val saved = state.prayerSteps[prayer.title] ?: 0
    var rung by rememberSaveable(prayer.title) { mutableIntStateOf(saved.coerceAtMost(Ladder.TOP)) }
    val passed = remember(rung) { mutableStateListOf<Int>() }
    var at by remember(rung) { mutableIntStateOf(0) }
    var peek by remember(rung, at) { mutableStateOf(false) }
    var pinyin by rememberSaveable { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var stopNow by remember { mutableStateOf(false) }
    var last by remember(rung, at) { mutableStateOf<Verdict?>(null) }
    val lines = prayer.lines
    val done = passed.size >= lines.size

    val mic = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        val (zh, py) = lines.getOrNull(at) ?: return@rememberMic
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = Recorder.record(maxSeconds = 10f, stop = { stopNow }, level = { level = it })
            recording = false; busy = true
            val v = vm.coach.check(clip, zh, py).verdict()
            busy = false
            last = v
            if (v != Verdict.MISS) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (at !in passed) passed += at
                if (passed.size >= lines.size) vm.climbPrayer(prayer.title, rung)
                else at = (0 until lines.size).firstOrNull { it !in passed } ?: at
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("${prayer.title} · ${prayer.titleZh}", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text("Rung ${rung + 1} of ${Ladder.TOP + 1} · ${(Ladder.hiddenShare(lines.map { it.first }, rung) * 100).toInt()}% hidden", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            }
            Rungs(maxOf(saved, rung))
        }
        GlassCard(highlight = true) {
            lines.forEachIndexed { i, (zh, py) ->
                val now = i == at && !done
                val ok = i in passed
                val bg by animateColorAsState(if (now) Night.sun.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent, label = "line")
                Column(
                    Modifier.fillMaxWidth().background(bg, RoundedCornerShape(12.dp)).clickable { at = i }.padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (ok || (now && peek)) zh else Ladder.masked(zh, rung),
                        fontSize = 22.sp, color = if (ok) Night.jade else Night.ink, lineHeight = 30.sp,
                    )
                    if (pinyin || (now && peek)) PinyinText(py, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (done) {
            val top = rung >= Ladder.TOP
            GlassCard(highlight = true) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (top) "🕊️" else "🏮", fontSize = 44.sp)
                    Text(if (top) "By heart. 阿门" else "Rung ${rung + 1} done", style = MaterialTheme.typography.titleLarge, color = Night.sun)
                    Text(
                        if (top) "You said the whole prayer with nothing on the screen." else "Next rung hides a little more.",
                        style = MaterialTheme.typography.bodyMedium, color = Night.muted, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onBack) { Text("Done", color = Night.muted) }
                        if (!top) Button(
                            onClick = { rung++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                        ) { Text("Next rung ↑") }
                        else Button(
                            onClick = { passed.clear(); at = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                        ) { Text("Say it again") }
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MicButton(recording, level, busy, onClick = mic)
                Text(
                    when {
                        recording -> "Listening… pause when you're done"
                        busy -> "Checking…"
                        last == Verdict.MISS -> "Not quite. Peek, listen, and try that line again."
                        else -> "Say line ${at + 1} of ${lines.size}"
                    },
                    style = MaterialTheme.typography.bodySmall, color = if (last == Verdict.MISS) Night.coral else Night.muted, textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { peek = !peek }) { Text(if (peek) "Hide" else "👀 Peek", color = Night.muted) }
                    TextButton(onClick = { vm.speaker.say(lines[at].first) }) { Text("🔊 Hear it", color = Night.muted) }
                    TextButton(onClick = { pinyin = !pinyin }) { Text(if (pinyin) "Pinyin off" else "Pinyin on", color = Night.muted) }
                }
            }
        }
        Spacer(Modifier.width(1.dp))
    }
}
