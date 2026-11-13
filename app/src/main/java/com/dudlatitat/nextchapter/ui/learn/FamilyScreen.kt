package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Callouts.Verdict
import com.dudlatitat.nextchapter.data.Family
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.speech.verdict
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch

/** Practice → Family 家谱: what to call everyone on her side, and a "Who's this?" game. */
@Composable
fun FamilyScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    var quiz by rememberSaveable { mutableStateOf(false) }
    var dadSide by rememberSaveable { mutableStateOf(false) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }

    if (quiz) { Quiz(vm, onBack = { quiz = false }); return }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable(720.dp).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Her family", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("家谱 · what to call everyone", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        GlassCard(highlight = true, onClick = { quiz = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎯", fontSize = 30.sp)
                Spacer(Modifier.padding(start = 10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Who's this? · 这是谁？", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("A relative is described; say how you'd greet them.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                Text("Play ›", color = Night.sun)
            }
        }
        Text(
            "You use the same word she does, except her parents: 叔叔 and 阿姨 until you're married. If she uses a different word for someone, hers wins.",
            style = MaterialTheme.typography.bodySmall, color = Night.muted,
        )
        Tree("Her parents", Family.side(Family.Side.PARENTS), state, open) { open = if (open == it) null else it }
        Tree("Her mom's side · 外婆家", Family.side(Family.Side.MOM), state, open) { open = if (open == it) null else it }
        TextButton(onClick = { dadSide = !dadSide }) { Text(if (dadSide) "Hide her dad's side" else "Her dad's side · 奶奶家 ›", color = Night.sun) }
        AnimatedVisibility(dadSide) { Tree("Her dad's side · 奶奶家", Family.side(Family.Side.DAD), state, open) { open = if (open == it) null else it } }
        open?.let { id -> Family.all.firstOrNull { it.id == id } }?.let { RelativeCard(it, state, vm, onClose = { open = null }) }
        Spacer(Modifier.height(24.dp))
    }
}

/** One side of the family, a row per generation, each person a tappable chip. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Tree(title: String, people: List<Family.Relative>, state: AppState, open: String?, onTap: (String) -> Unit) {
    GlassCard {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Night.muted)
        listOf(0 to "Grandparents", 1 to "Parents' generation", 2 to "Her generation").forEach { (row, label) ->
            val here = people.filter { it.row == row }
            if (here.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted.copy(alpha = 0.7f))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    here.forEach { p ->
                        val on = open == p.id
                        Column(
                            Modifier.widthIn(min = 84.dp)
                                .background(if (on) Night.sun.copy(alpha = 0.16f) else Night.cardHigh, RoundedCornerShape(16.dp))
                                .border(1.dp, if (on) Night.sun else Night.outline, RoundedCornerShape(16.dp))
                                .clickable { onTap(p.id) }.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(p.hanzi, fontSize = 22.sp, color = Night.ink)
                            PinyinText(p.pinyin, style = MaterialTheme.typography.labelMedium)
                            state.familyNames[p.id]?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Night.sun) }
                        }
                    }
                }
            }
        }
    }
}

/** The tapped relative: who they are, hear it, say the greeting, and (optionally) their name. */
@Composable
private fun RelativeCard(p: Family.Relative, state: AppState, vm: MainViewModel, onClose: () -> Unit) {
    var name by remember(p.id) { mutableStateOf(state.familyNames[p.id] ?: "") }
    LaunchedEffect(p.id) { vm.speaker.say(p.greet) }
    GlassCard(highlight = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(p.who, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text("Greet them: ${p.greet}", fontSize = 24.sp, color = Night.sun)
                PinyinText(p.greetPinyin, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { vm.speaker.say(p.greet) }) { Text("🔊", fontSize = 22.sp) }
        }
        SayCheck(p.greet, p.greetPinyin, vm)
        OutlinedTextField(
            name, { name = it.take(30) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            label = { Text("Their name (optional, stays on this phone)") },
        )
        Row {
            TextButton(onClick = { vm.setFamilyName(p.id, name) }) { Text("Save name", color = Night.sun) }
            TextButton(onClick = onClose) { Text("Close", color = Night.muted) }
        }
    }
}

/** A small mic: say the line, see right / close / again. */
@Composable
private fun SayCheck(hanzi: String, pinyin: String, vm: MainViewModel, onResult: (Verdict) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var stopNow by remember { mutableStateOf(false) }
    var last by remember(hanzi) { mutableStateOf<Verdict?>(null) }
    val mic = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = Recorder.record(maxSeconds = 4f, stop = { stopNow }, level = { level = it })
            recording = false; busy = true
            val v = vm.coach.check(clip, hanzi, pinyin).verdict()
            busy = false; last = v
            if (v != Verdict.MISS) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onResult(v)
        }
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MicButton(recording, level, busy, onClick = mic)
        Text(
            when {
                recording -> "Listening…"; busy -> "Checking…"
                last == Verdict.GREAT -> "Perfect · 很好！"; last == Verdict.CLOSE -> "Close! Once more?"; last == Verdict.MISS -> "Listen and try again"
                else -> "Tap and say it"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when (last) { Verdict.GREAT -> Night.jade; Verdict.CLOSE -> Night.sun; Verdict.MISS -> Night.coral; null -> Night.muted },
        )
    }
}

/** Who's this? A relative is described; pick (or say) the right greeting. */
@Composable
private fun Quiz(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var n by remember { mutableIntStateOf(0) }
    var right by remember { mutableIntStateOf(0) }
    val q = remember(n) { Family.question() }
    val (answer, choices) = q
    var picked by remember(n) { mutableStateOf<String?>(null) }
    val pop = remember(n) { Animatable(0.85f) }
    LaunchedEffect(n) { pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Who's this? · 这是谁？", style = MaterialTheme.typography.titleLarge, color = Night.ink, modifier = Modifier.weight(1f))
            val asked = n + if (picked != null) 1 else 0
            if (asked > 0) Text("$right / $asked", style = MaterialTheme.typography.titleMedium, color = Night.sun)
        }
        GlassCard(highlight = true, modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
            Text("They are…", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Text(answer.who, style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Text("How do you greet them?", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
        }
        choices.forEach { c ->
            val color = when {
                picked == null -> Night.ink
                c.id == answer.id -> Night.jade
                c.id == picked -> Night.coral
                else -> Night.muted
            }
            Column(
                Modifier.fillMaxWidth().background(color.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable(enabled = picked == null) {
                        picked = c.id
                        if (c.id == answer.id) right++
                        vm.speaker.say(answer.greet)
                    }.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(c.greet, fontSize = 22.sp, color = color)
                if (picked != null) PinyinText(c.greetPinyin, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (picked != null) {
            Text(if (picked == answer.id) "Right! Now say it out loud:" else "It's ${answer.greet}. Say it out loud:", style = MaterialTheme.typography.titleSmall, color = Night.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            SayCheck(answer.greet, answer.greetPinyin, vm)
            Button(
                onClick = { n++ }, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
            ) { Text("Next relative →") }
        }
    }
}
