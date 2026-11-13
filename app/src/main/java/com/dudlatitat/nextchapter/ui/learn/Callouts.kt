package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Callouts
import com.dudlatitat.nextchapter.data.Callouts.Verdict
import com.dudlatitat.nextchapter.data.Seed
import com.dudlatitat.nextchapter.speech.Mark
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Practice → Callouts: ten Identity V callouts, shouted against the clock. */
@Composable
fun CalloutScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var round by remember { mutableStateOf<List<Callouts.Prompt>?>(null) }
    var i by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var greats by remember { mutableIntStateOf(0) }
    var left by remember { mutableIntStateOf(Callouts.SECONDS) }
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var stopNow by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Verdict, Int>?>(null) } // verdict, points
    var bestBefore by remember { mutableIntStateOf(state.calloutBest) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }

    val r = round
    val done = r != null && i >= r.size
    val prompt = if (r != null && !done) r[i] else null
    fun card(hanzi: String) = state.cards.firstOrNull { it.id == "idv:$hanzi" } ?: Seed.cards.first { it.id == "idv:$hanzi" }

    fun finish(v: Verdict) {
        val pts = Callouts.points(v, left)
        result = v to pts
        score += pts
        combo = if (v == Verdict.GREAT) combo + 1 else 0
        if (v == Verdict.GREAT) greats++
        haptic.performHapticFeedback(if (v == Verdict.MISS) HapticFeedbackType.TextHandleMove else HapticFeedbackType.LongPress)
        prompt?.let { vm.speaker.say(it.hanzi) }
    }
    fun next() {
        result = null; left = Callouts.SECONDS; i++
        if (r != null && i >= r.size) vm.saveCalloutScore(score)
    }
    fun start() { bestBefore = state.calloutBest; round = Callouts.round(); i = 0; score = 0; combo = 0; greats = 0; result = null; left = Callouts.SECONDS }

    // The clock: runs until you start talking; at zero the card shows itself.
    LaunchedEffect(i, round, result == null) {
        if (round == null || result != null) return@LaunchedEffect
        left = Callouts.SECONDS
        while (left > 0) { delay(1000); if (recording || busy) return@LaunchedEffect; left-- }
        finish(Verdict.MISS)
    }

    val mic = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        val p = prompt ?: return@rememberMic
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = Recorder.record(maxSeconds = 4f, stop = { stopNow }, level = { level = it })
            recording = false; busy = true
            val c = card(p.hanzi)
            val a = vm.coach.check(clip, c.hanzi, c.pinyin)
            busy = false
            val marks = a.chars
            finish(
                when {
                    a.tooQuiet -> Verdict.MISS
                    marks == null -> Verdict.CLOSE // the word check isn't available: count the effort
                    else -> Callouts.verdict(marks.count { it.mark == Mark.RIGHT }, marks.count { it.mark == Mark.TONE }, marks.count { it.mark == Mark.MISSED })
                },
            )
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Callouts", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("喊话 · Identity V, against the clock", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            if (r != null && !done) {
                val shown by animateIntAsState(score, tween(500), label = "score")
                Text("$shown", style = MaterialTheme.typography.titleLarge, color = Night.sun)
            }
        }

        when {
            r == null -> Intro(state.calloutBest, onStart = ::start)
            done -> Results(score, greats, bestBefore, onAgain = ::start, onBack = onBack)
            else -> {
                // Progress dots and the combo.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1} / ${r.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                    if (combo >= 2) Text("🔥 ×$combo", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                }
                // The clock bar drains jade → gold → coral.
                val frac by animateFloatAsState(left / Callouts.SECONDS.toFloat(), tween(950), label = "clock")
                val barColor = when { left > 5 -> Night.jade; left > 2 -> Night.sun; else -> Night.coral }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Night.ink.copy(alpha = .08f))) {
                    Box(Modifier.fillMaxWidth(if (result == null) frac else 0f).height(8.dp).background(barColor))
                }
                GlassCard(highlight = true) {
                    AnimatedContent(i, transitionSpec = { (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith fadeOut(tween(90)) }, label = "prompt") { idx ->
                        val p = r.getOrNull(idx) ?: return@AnimatedContent
                        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(p.icon, fontSize = 72.sp)
                            Text(p.hint, style = MaterialTheme.typography.titleMedium, color = Night.muted, textAlign = TextAlign.Center)
                        }
                    }
                    result?.let { (v, pts) ->
                        val c = card(prompt!!.hanzi)
                        val pop = remember(i) { Animatable(0.6f) }
                        LaunchedEffect(i) { pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                        Column(Modifier.fillMaxWidth().graphicsLayer { scaleX = pop.value; scaleY = pop.value }, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                when (v) { Verdict.GREAT -> "完美！ Perfect  +$pts"; Verdict.CLOSE -> "差不多！ Close  +$pts"; Verdict.MISS -> "Here it is" },
                                style = MaterialTheme.typography.titleMedium,
                                color = when (v) { Verdict.GREAT -> Night.jade; Verdict.CLOSE -> Night.sun; Verdict.MISS -> Night.coral },
                            )
                            Text(c.hanzi, fontSize = 34.sp, color = Night.ink, textAlign = TextAlign.Center)
                            PinyinText(c.pinyin, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            Text(c.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                        }
                    }
                }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (result == null) {
                        MicButton(recording, level, busy, onClick = mic)
                        Text(
                            when { recording -> "Listening… pause when you're done"; busy -> "Checking…"; else -> "Tap and shout it! (${left}s)" },
                            style = MaterialTheme.typography.bodySmall, color = Night.muted,
                        )
                        TextButton(onClick = { finish(Verdict.MISS) }, enabled = !recording && !busy) { Text("Show me", color = Night.muted) }
                    } else {
                        Button(
                            onClick = ::next,
                            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                        ) { Text(if (i + 1 >= r.size) "See my score" else "Next  →") }
                        TextButton(onClick = { vm.speaker.say(prompt!!.hanzi) }) { Text("🔊 Hear it again", color = Night.muted) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Intro(best: Int, onStart: () -> Unit) {
    GlassCard(highlight = true) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎮", fontSize = 56.sp)
            Text("Ten callouts. Eight seconds each.", style = MaterialTheme.typography.titleMedium, color = Night.ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                "A picture flashes: say the callout in Chinese before the clock runs out. Faster is worth more, and perfect ones in a row build a combo.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            if (best > 0) Text("Best: $best / ${Callouts.maxScore}", style = MaterialTheme.typography.labelLarge, color = Night.sun)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Start · 开始") }
        }
    }
}

@Composable
private fun Results(score: Int, greats: Int, bestBefore: Int, onAgain: () -> Unit, onBack: () -> Unit) {
    val newBest = score > bestBefore
    val shown by animateIntAsState(score, tween(1200), label = "final")
    GlassCard(highlight = true) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                when { greats >= 9 -> "🏆"; greats >= 6 -> "🌟"; greats >= 3 -> "👍"; else -> "🌱" },
                fontSize = 56.sp,
            )
            Text("$shown", fontSize = 44.sp, color = Night.sun)
            Text("out of ${Callouts.maxScore} · $greats perfect", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    newBest && bestBefore > 0 -> "New best! 新纪录！"
                    greats >= 8 -> "She'd be impressed. 太厉害了！"
                    greats >= 5 -> "Solid. One more round and it sticks."
                    else -> "Every round makes the next match easier."
                },
                style = MaterialTheme.typography.titleSmall, color = if (newBest) Night.jade else Night.ink, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onBack) { Text("Done", color = Night.muted) }
                Button(onClick = onAgain, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Again · 再来") }
            }
        }
    }
}
