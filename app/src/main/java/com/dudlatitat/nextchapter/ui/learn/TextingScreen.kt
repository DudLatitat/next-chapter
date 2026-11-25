package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.Texting
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.delay

/** WeChat's own green for your bubbles, so it feels like the real thing. */
private val WeGreen = Color(0xFF95EC69)

/** One line in the chat: theirs (maybe a voice bubble) or yours. */
private data class Line(val mine: Boolean, val text: String, val pinyin: String = "", val english: String = "", val voice: Boolean = false, val hinted: Boolean = false)

/** Practice → 微信 Texts: chats you answer with your real Chinese keyboard. */
@Composable
fun TextingScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }
    val th = Texting.threads.firstOrNull { it.id == open }
    if (th == null) ChatList(state, onBack) { open = it }
    else Chat(th, state, vm, onBack = { open = null })
}

@Composable
private fun ChatList(state: AppState, onBack: () -> Unit, onOpen: (String) -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column {
                Text("Texts", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("微信 · reply with your Chinese keyboard", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        GlassCard {
            Text("⌨️ Set up a Chinese keyboard once", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text(
                "Samsung Keyboard: Settings → General management → Samsung Keyboard settings → Languages and types → Manage input languages → turn on 中文(简体) Pinyin. Then swipe the space bar (or tap 🌐) to switch while you type.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
        Texting.threads.forEach { th ->
            val best = state.textBest[th.id] ?: 0
            GlassCard(onClick = { onOpen(th.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(Night.cardHigh, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(th.avatar, fontSize = 24.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(th.contact, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        Text(th.turns.first().them.first().let { if (it.voice) "[语音]" else it.hanzi }, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1)
                    }
                    Text(if (best > 0) "★ $best/${th.turns.size * 2}" else th.title, style = MaterialTheme.typography.labelMedium, color = if (best > 0) Night.sun else Night.muted)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chat(th: Texting.Thread, state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    val payBefore = com.dudlatitat.nextchapter.ui.components.rememberBefore(vm)
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val lines = remember(th.id) { mutableStateListOf<Line>() }
    var turn by remember(th.id) { mutableIntStateOf(0) }
    var typing by remember(th.id) { mutableStateOf(false) }
    var input by remember(th.id) { mutableStateOf("") }
    var hint by remember(th.id, turn) { mutableIntStateOf(0) } // 0 none, 1 words, 2 pinyin, 3 example
    var nudge by remember(th.id, turn) { mutableStateOf("") }
    var score by remember(th.id) { mutableIntStateOf(0) }
    // Pinyin and English under their messages: on by default; switch off for a challenge.
    var help by rememberSaveable { mutableStateOf(true) }
    var lookup by remember { mutableStateOf(false) }
    val list = rememberLazyListState()
    val focus = remember { FocusRequester() }
    val t = th.turns.getOrNull(turn)
    val done = t == null

    // Their messages arrive one by one, with "typing…" in between, like the real thing.
    LaunchedEffect(th.id, turn) {
        val now = th.turns.getOrNull(turn) ?: run { vm.saveTextThread(th.id, score); return@LaunchedEffect }
        now.them.forEach { msg ->
            typing = true; delay(if (lines.isEmpty()) 400 else 900); typing = false
            lines += Line(false, msg.hanzi, msg.pinyin, msg.english, msg.voice)
            if (msg.voice) vm.speaker.say(msg.hanzi)
        }
    }
    LaunchedEffect(lines.size, typing, turn) { if (lines.isNotEmpty()) list.animateScrollToItem(lines.size + if (typing || turn >= th.turns.size) 1 else 0) }

    fun send() {
        val now = t ?: return
        if (input.isBlank()) return
        if (Texting.accepts(now, input)) {
            lines += Line(true, input.trim(), hinted = hint > 0)
            score += if (hint == 0) 2 else 1
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            input = ""; turn++
        } else {
            nudge = if (input.none { it in '一'..'鿿' }) "Switch your keyboard to Chinese (中) and pick the characters." else "Close! Try using: ${now.words.first()}"
        }
    }

    if (lookup) HowSaySheet(state, vm, onDismiss = { lookup = false }, onUse = { input += it; nudge = "" })

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().background(Night.cardHigh).padding(top = 28.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text(th.contact, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text(if (typing) "对方正在输入… (typing)" else th.title, style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
            TextButton(onClick = { help = !help }) { Text(if (help) "拼音 on" else "拼音 off", color = if (help) Night.sun else Night.muted) }
            val shown by animateIntAsState(score, tween(400), label = "score")
            Text("★ $shown", color = Night.sun, modifier = Modifier.padding(end = 16.dp))
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), state = list, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(8.dp)) }
            items(lines) { l -> Bubble(l, th.avatar, help, onPlay = { vm.speaker.say(l.text) }) }
            if (typing) item { Text("   …", color = Night.muted, fontSize = 22.sp) }
            if (done) item {
                GlassCard(highlight = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (score >= th.turns.size * 2) "💬✨" else "💬", fontSize = 40.sp)
                        Text("★ $score / ${th.turns.size * 2}", style = MaterialTheme.typography.headlineSmall, color = Night.sun)
                        Text(if (score >= th.turns.size * 2) "No hints needed. Real texting." else "Run it again without hints for full stars.", style = MaterialTheme.typography.bodyMedium, color = Night.muted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(onClick = onBack) { Text("Other chats", color = Night.muted) }
                            Button(onClick = { lines.clear(); score = 0; turn = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Again · 再来") }
                        }
                        Spacer(Modifier.height(12.dp))
                        com.dudlatitat.nextchapter.ui.components.SessionPayoff(vm, payBefore)
                    }
                }
            }
        }
        if (t != null) {
            // Hints, step by step: the words, then the pinyin of an answer, then the answer.
            AnimatedVisibility(hint > 0 || nudge.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                    if (nudge.isNotEmpty()) Text(nudge, style = MaterialTheme.typography.bodySmall, color = Night.coral)
                    if (hint >= 1) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        t.words.forEach { w ->
                            Text(w, fontSize = 18.sp, color = Night.ink, modifier = Modifier.background(Night.cardHigh, RoundedCornerShape(10.dp)).clickable { input += w }.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    if (hint >= 2) PinyinText(t.examples.first().pinyin, style = MaterialTheme.typography.bodyMedium)
                    if (hint >= 3) Text("${t.examples.first().hanzi}  ·  ${t.examples.first().english}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    if (t.tip.isNotBlank() && hint >= 1) Text("💡 ${t.tip}", style = MaterialTheme.typography.bodySmall, color = Night.violet)
                }
            }
            Row(Modifier.fillMaxWidth().background(Night.cardHigh).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { lookup = true }) { Text("🔍", fontSize = 20.sp) }
                TextButton(onClick = { if (hint < 3) hint++ }) { Text(if (hint < 3) "💡" else "✓", fontSize = 20.sp) }
                OutlinedTextField(
                    input, { input = it; nudge = "" }, modifier = Modifier.weight(1f).focusRequester(focus),
                    placeholder = { Text("用中文回复… (Chinese keyboard)") }, maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send), keyboardActions = KeyboardActions(onSend = { send() }),
                )
                Spacer(Modifier.width(6.dp))
                Button(onClick = ::send, colors = ButtonDefaults.buttonColors(containerColor = WeGreen, contentColor = Color(0xFF111111))) { Text("发送") }
            }
        }
    }
}

@Composable
private fun Bubble(l: Line, avatar: String, help: Boolean, onPlay: () -> Unit) {
    var showMeaning by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(!l.voice) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (l.mine) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Top) {
        if (!l.mine) { Text(avatar, fontSize = 26.sp); Spacer(Modifier.width(6.dp)) }
        AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 3 }) {
            Column(
                Modifier.widthIn(max = 280.dp)
                    .background(if (l.mine) WeGreen else Night.cardHigh, RoundedCornerShape(8.dp))
                    .clickable { if (l.voice && !revealed) { onPlay(); revealed = true } else if (!l.mine) showMeaning = !showMeaning }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (l.voice && !revealed) Text("🔊 )))   0:0${(l.text.length / 3).coerceIn(1, 9)}″", color = Night.ink)
                else {
                    Text(l.text, fontSize = 18.sp, color = if (l.mine) Color(0xFF111111) else Night.ink)
                    if (!l.mine && (help != showMeaning || l.voice)) { // tapping a bubble flips it either way
                        PinyinText(l.pinyin, style = MaterialTheme.typography.bodySmall)
                        Text(l.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    if (l.mine && l.hinted) Text("with a hint", fontSize = 11.sp, color = Color(0xFF335522))
                }
            }
        }
    }
}

/** Practice → 打字 Sprint: an English line, you type it in Chinese, 60 seconds. */
@Composable
fun SprintScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    val payBefore = com.dudlatitat.nextchapter.ui.components.rememberBefore(vm)
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var pool by remember { mutableStateOf<List<Card>>(emptyList()) }
    var i by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var chars by remember { mutableIntStateOf(0) }
    var lines by remember { mutableIntStateOf(0) }
    var startAt by remember { mutableLongStateOf(0L) }
    var left by remember { mutableIntStateOf(60) }
    var flash by remember { mutableStateOf(false) }
    var showPy by remember { mutableStateOf(false) }
    var bestBefore by remember { mutableIntStateOf(state.sprintBest) }
    val focus = remember { FocusRequester() }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }

    fun start() {
        bestBefore = state.sprintBest
        pool = Texting.sprintPool(state).shuffled(); i = 0; input = ""; chars = 0; lines = 0; left = 60
        startAt = System.currentTimeMillis(); finished = false; running = true
    }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        runCatching { focus.requestFocus() }
        while (left > 0) { delay(1000); left-- }
        running = false; finished = true
        vm.saveSprint(Texting.cpm(chars, 60_000))
    }
    val card = pool.getOrNull(i % maxOf(pool.size, 1))
    LaunchedEffect(input, card) {
        val c = card ?: return@LaunchedEffect
        if (running && Texting.typedRight(input, c)) {
            chars += c.hanzi.count { it in '一'..'鿿' }; lines++; input = ""; i++
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            flash = true; delay(250); flash = false
        }
    }

    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Typing sprint", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("打字 · 60 seconds, as many lines as you can", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            if (running) Text("${left}s", style = MaterialTheme.typography.titleLarge, color = if (left <= 10) Night.coral else Night.sun)
        }
        when {
            running && card != null -> {
                GlassCard(highlight = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.english, style = MaterialTheme.typography.headlineSmall, color = if (flash) Night.jade else Night.ink, textAlign = TextAlign.Center)
                        Text("$lines lines · $chars characters", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    }
                }
                OutlinedTextField(
                    input, { input = it }, modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                    placeholder = { Text("Type it in Chinese (中)") }, textStyle = MaterialTheme.typography.headlineSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { input = ""; i++ }) { Text("Skip ›", color = Night.muted) }
                    TextButton(onClick = { vm.speaker.say(card.hanzi) }) { Text("🔊 Hear it", color = Night.muted) }
                    TextButton(onClick = { showPy = !showPy }) { Text(if (showPy) "Hide pinyin" else "Pinyin", color = Night.muted) }
                }
                if (showPy) PinyinText(card.pinyin, style = MaterialTheme.typography.bodyMedium)
            }
            else -> GlassCard(highlight = true) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (finished) {
                        val cpm = Texting.cpm(chars, 60_000)
                        Text(if (cpm > bestBefore && bestBefore > 0) "🏆" else "⌨️", fontSize = 48.sp)
                        Text("$cpm characters a minute", style = MaterialTheme.typography.headlineSmall, color = Night.sun)
                        Text("$lines lines typed right${if (cpm > bestBefore && bestBefore > 0) " · new best!" else ""}", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                    } else {
                        Text("⌨️", fontSize = 48.sp)
                        Text("English shows up; type it in Chinese with your pinyin keyboard. It moves on as soon as it's right.", style = MaterialTheme.typography.bodyMedium, color = Night.ink, textAlign = TextAlign.Center)
                        if (state.sprintBest > 0) Text("Best: ${state.sprintBest} characters a minute", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = ::start, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text(if (finished) "Again · 再来" else "Start · 开始") }
                    if (finished) { Spacer(Modifier.height(12.dp)); com.dudlatitat.nextchapter.ui.components.SessionPayoff(vm, payBefore) }
                }
            }
        }
    }
}
