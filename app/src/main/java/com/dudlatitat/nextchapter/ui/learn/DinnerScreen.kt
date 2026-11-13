package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Callouts
import com.dudlatitat.nextchapter.data.Callouts.Verdict
import com.dudlatitat.nextchapter.data.Dinner
import com.dudlatitat.nextchapter.speech.Mark
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch

/** One line said at the table: who, what they said, and how your answer went. */
private data class Said(val beat: Dinner.Beat, val reply: Dinner.Reply?, val verdict: Verdict?)

/** Practice → Dinner: a family dinner as a chat. They speak; you answer out loud. */
@Composable
fun DinnerScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var scene by remember { mutableStateOf<Dinner.Scene?>(null) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }
    val sc = scene
    if (sc == null) ScenePicker(state, onBack, onPick = { scene = it })
    else Table(sc, vm, onBack = { scene = null })
}

@Composable
private fun ScenePicker(state: AppState, onBack: () -> Unit, onPick: (Dinner.Scene) -> Unit) {
    BackHandler(onBack = onBack)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column {
                    Text("Family dinner", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text("家宴 · a run-through before the real one", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
        }
        item {
            Text(
                "Her family talks; you answer out loud. Pick either reply: each clean answer earns 🥢🥢. Tips on what's polite come along the way.",
                style = MaterialTheme.typography.bodyMedium, color = Night.muted,
            )
        }
        val icons = mapOf("arrive" to "🚪", "meal" to "🍲", "after" to "🍵")
        Dinner.scenes.forEachIndexed { n, s ->
            item(key = s.id) {
                val best = state.dinnerBest[s.id] ?: 0
                GlassCard(onClick = { onPick(s) }, highlight = best == 0 && Dinner.scenes.take(n).all { (state.dinnerBest[it.id] ?: 0) > 0 }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(icons[s.id] ?: "🍽️", fontSize = 34.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${n + 1}. ${s.title} · ${s.hanzi}", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                            Text("${s.beats.size} lines", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                        }
                        if (best > 0) Text("🥢 $best/${Dinner.most(s)}", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                        else Text("Start ›", color = Night.sun)
                    }
                }
            }
        }
    }
}

@Composable
private fun Table(scene: Dinner.Scene, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val said = remember(scene) { mutableStateListOf<Said>() }
    var at by remember(scene) { mutableIntStateOf(0) }
    var picked by remember(scene) { mutableStateOf<Dinner.Reply?>(null) }
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var stopNow by remember { mutableStateOf(false) }
    var got by remember(scene) { mutableIntStateOf(0) }
    var english by remember { mutableStateOf(false) }
    val list = rememberLazyListState()
    val done = at >= scene.beats.size
    val beat = scene.beats.getOrNull(at)

    // Each new line is spoken as it arrives, and the chat scrolls to it.
    LaunchedEffect(at) {
        beat?.let { vm.speaker.say(it.hanzi) }
        list.animateScrollToItem(said.size)
    }
    LaunchedEffect(done) { if (done) vm.saveDinner(scene.id, got) }

    fun answer(r: Dinner.Reply?, v: Verdict?) {
        val b = beat ?: return
        said += Said(b, r, v)
        got += v?.let(Dinner::chopsticks) ?: 0
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        picked = null
        at++
    }

    val mic = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        val r = picked ?: return@rememberMic
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = Recorder.record(maxSeconds = 6f, stop = { stopNow }, level = { level = it })
            recording = false; busy = true
            val a = vm.coach.check(clip, r.hanzi, r.pinyin)
            busy = false
            val marks = a.chars
            answer(
                r,
                when {
                    a.tooQuiet -> Verdict.MISS
                    marks == null -> Verdict.CLOSE
                    else -> Callouts.verdict(marks.count { it.mark == Mark.RIGHT }, marks.count { it.mark == Mark.TONE }, marks.count { it.mark == Mark.MISSED })
                },
            )
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("${scene.title} · ${scene.hanzi}", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text("${minOf(at + 1, scene.beats.size)} / ${scene.beats.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            }
            val shown by animateIntAsState(got, tween(400), label = "chopsticks")
            Text("🥢 $shown", style = MaterialTheme.typography.titleMedium, color = Night.sun)
        }
        TextButton(onClick = { english = !english }) { Text(if (english) "Hide English" else "Show English", color = Night.muted) }

        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = list, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(said, key = { i, _ -> "said$i" }) { _, s ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TheirBubble(s.beat, english = true, onPlay = { vm.speaker.say(s.beat.hanzi) })
                    MyBubble(s.reply, s.verdict, onPlay = { s.reply?.let { vm.speaker.say(it.hanzi) } })
                }
            }
            if (beat != null) item(key = "now$at") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TheirBubble(beat, english = english, onPlay = { vm.speaker.say(beat.hanzi) }, fresh = true)
                    if (beat.tip.isNotBlank()) Text("💡 ${beat.tip}", style = MaterialTheme.typography.bodySmall, color = Night.violet, modifier = Modifier.padding(start = 6.dp))
                    Text("Your answer: pick one, then say it", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.padding(top = 4.dp))
                    beat.replies.forEach { r -> ReplyChoice(r, r == picked, english) { picked = r; vm.speaker.say(r.hanzi) } }
                }
            }
            if (done) item(key = "end") {
                val (icon, line) = Dinner.ending(got, Dinner.most(scene))
                GlassCard(highlight = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(icon, fontSize = 52.sp)
                        Text("🥢 $got / ${Dinner.most(scene)}", style = MaterialTheme.typography.headlineSmall, color = Night.sun)
                        Text(line, style = MaterialTheme.typography.bodyMedium, color = Night.ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(onClick = onBack) { Text("Other scenes", color = Night.muted) }
                            Button(
                                onClick = { said.clear(); at = 0; got = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                            ) { Text("Again · 再来") }
                        }
                    }
                }
            }
        }

        if (!done) Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            MicButton(recording, level, busy, onClick = { if (picked != null || recording) mic() })
            Text(
                when { recording -> "Listening… pause when you're done"; busy -> "Checking…"; picked == null -> "Pick a reply first"; else -> "Tap and say: ${picked!!.hanzi}" },
                style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center,
            )
            TextButton(onClick = { answer(picked, null) }, enabled = !recording && !busy) { Text("Skip this one", color = Night.muted) }
        }
    }
}

@Composable
private fun TheirBubble(beat: Dinner.Beat, english: Boolean, onPlay: () -> Unit, fresh: Boolean = false) {
    var visible by remember { mutableStateOf(!fresh) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn() + slideInHorizontally { -it / 6 } + expandVertically()) {
        Column(Modifier.widthIn(max = 340.dp)) {
            Text(beat.who, style = MaterialTheme.typography.labelSmall, color = Night.rose, modifier = Modifier.padding(start = 10.dp, bottom = 2.dp))
            Column(
                Modifier.background(Night.cardHigh, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .clickable(onClick = onPlay).padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(beat.hanzi + "  🔊", fontSize = 22.sp, color = Night.ink)
                PinyinText(beat.pinyin, style = MaterialTheme.typography.bodyMedium)
                if (english) Text(beat.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
    }
}

@Composable
private fun MyBubble(reply: Dinner.Reply?, verdict: Verdict?, onPlay: () -> Unit) {
    val color = when (verdict) { Verdict.GREAT -> Night.jade; Verdict.CLOSE -> Night.sun; Verdict.MISS -> Night.coral; null -> Night.muted }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Column(
            Modifier.widthIn(max = 300.dp)
                .background(color.copy(alpha = .16f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                .border(1.dp, color.copy(alpha = .5f), RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                .clickable(enabled = reply != null, onClick = onPlay).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(reply?.hanzi ?: "(skipped)", fontSize = 18.sp, color = Night.ink)
            Text(
                when (verdict) { Verdict.GREAT -> "🥢🥢 clear"; Verdict.CLOSE -> "🥢 nearly"; Verdict.MISS -> "try again next time"; null -> "" },
                style = MaterialTheme.typography.labelSmall, color = color,
            )
        }
    }
}

@Composable
private fun ReplyChoice(r: Dinner.Reply, on: Boolean, english: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth()
            .background(if (on) Night.sun.copy(alpha = .16f) else Color.White.copy(alpha = .04f), shape)
            .border(1.dp, if (on) Night.sun else Night.outline, shape)
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(r.hanzi, fontSize = 20.sp, color = Night.ink)
        PinyinText(r.pinyin, style = MaterialTheme.typography.bodyMedium)
        if (english || on) Text(r.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }
}
