package com.dudlatitat.nextchapter.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.desktop.DesktopServer
import com.dudlatitat.nextchapter.desktop.PageState
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Settings → Use on my computer → Phone remote. Each press is queued on the phone's server; the
 * computer page is always waiting for the next one (a "long-poll"), so it reacts at once.
 * It drives the page, not Windows itself: a web page can't move the computer's mouse.
 */
@Composable
fun RemoteScreen(server: DesktopServer?, vm: com.dudlatitat.nextchapter.MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val none = remember { MutableStateFlow<PageState?>(null) }
    val page by (server?.page ?: none).collectAsState()
    fun press(cmd: String, value: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        server?.send(cmd, value)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Phone remote", style = MaterialTheme.typography.headlineSmall, color = Night.ink, modifier = Modifier.weight(1f))
            if (server != null) Connection(server)
        }
        if (server == null) {
            GlassCard { Text("Turn on \"Available on my computer\" first.", color = Night.coral) }
        } else RemoteBody(server, vm, page) { c, v -> press(c, v) }
    }
}

/** A breathing jade dot while the computer page is listening (it checks in at least every 20 s). */
@Composable
private fun Connection(server: DesktopServer) {
    val seen by server.seen.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(3000) } }
    val live = seen > 0 && now - seen < 30_000
    val glow by rememberInfiniteTransition(label = "dot").animateFloat(
        0.35f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow",
    )
    val color = if (live) Night.jade else Night.muted
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).graphicsLayer { alpha = if (live) glow else 0.6f }.background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(if (live) "Connected" else "Waiting", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun ColumnScope.RemoteBody(server: DesktopServer, vm: com.dudlatitat.nextchapter.MainViewModel, page: PageState?, press: (String, Int) -> Unit) {
    val p = page
    val aplus = p?.view == "aplus"
    val review = p?.view == "review" || aplus
    // Session progress: the most cards seen waiting since Review opened.
    var most by remember { mutableIntStateOf(0) }
    LaunchedEffect(review, p?.left) { most = if (!review) 0 else maxOf(most, p?.left ?: 0) }
    // Good or Easy in a row, counted here on the phone.
    var combo by remember { mutableIntStateOf(0) }

    // The card on the computer, as a little flashcard.
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(p?.shown, p?.english) { tilt.snapTo(10f); tilt.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
    GlassCard(highlight = true, modifier = Modifier.graphicsLayer { rotationX = tilt.value; cameraDistance = 12 * density }) {
        if (review && most > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${p!!.left} to go", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                AnimatedVisibility(combo >= 3, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                    val bounce = remember { Animatable(1f) }
                    LaunchedEffect(combo) { bounce.snapTo(1.35f); bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                    Text(
                        "🔥 $combo in a row", style = MaterialTheme.typography.labelMedium, color = Night.sun,
                        modifier = Modifier.graphicsLayer { scaleX = bounce.value; scaleY = bounce.value }
                            .background(Night.sun.copy(alpha = .14f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            val done by animateFloatAsState(if (most == 0) 0f else 1f - p!!.left.toFloat() / most, tween(500), label = "done")
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = .08f))) {
                Box(Modifier.fillMaxWidth(done).height(5.dp).background(Brush.horizontalGradient(listOf(Night.sun, Night.rose))))
            }
            Spacer(Modifier.height(10.dp))
        }
        AnimatedContent(
            targetState = p, label = "screen",
            contentKey = { listOf(it?.view, it?.english, it?.shown) },
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
        ) { s ->
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    s == null -> {
                        Text("💻", fontSize = 34.sp)
                        Text("Open the page on your computer", color = Night.ink, textAlign = TextAlign.Center)
                        Text("It shows up here as soon as it's open.", style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center)
                    }
                    s.view != "review" && s.view != "aplus" -> {
                        Text("短语", fontSize = 30.sp, color = Night.sun)
                        Text("Browsing phrases", color = Night.ink)
                        Text("Swipe below to scroll, or start a review.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    s.english.isEmpty() -> {
                        Text("🌙", fontSize = 34.sp)
                        Text(if (s.view == "aplus") "A+ done for today ✓" else "All caught up · 都复习完了", color = Night.jade)
                        Text("Nice work. New phrases arrive tomorrow.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    else -> {
                        Text(s.english, style = MaterialTheme.typography.titleLarge, color = Night.ink, textAlign = TextAlign.Center)
                        if (s.shown && s.view == "aplus") {
                            Spacer(Modifier.height(6.dp))
                            Text(s.hanzi, style = MaterialTheme.typography.bodyLarge, color = Night.sun, textAlign = TextAlign.Center)
                        } else if (s.shown) {
                            Spacer(Modifier.height(4.dp))
                            Text(s.hanzi, fontSize = 40.sp, color = Night.sun, textAlign = TextAlign.Center)
                            PinyinText(s.pinyin, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        } else {
                            Text(if (s.view == "aplus") "Answer it in your head first" else "Say it out loud first", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                        }
                    }
                }
            }
        }
    }

    // Which page: a two-way switch.
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .05f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Segment("Phrases", !review) { press("tab", 0) }
        Segment("Review", review && !aplus) { press("tab", 1) }
        Segment("A+", aplus) { press("tab", 2) }
    }

    // Review buttons.
    if (review && p!!.english.isNotEmpty()) {
        if (!p.shown) {
            val breathe = rememberInfiniteTransition(label = "show").animateFloat(
                0.18f, 0.34f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pad("Show answer", "看答案", Night.sun, tall = true, fill = { breathe.value }) { press("show", 0) }
                if (!aplus) SayIt(server, vm, p) { combo++ }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pad("Again", "再来", Night.coral, tall = true) { combo = 0; press("grade", 1) }
                Pad("Hard", "有点难", Night.sun, tall = true) { combo = 0; press("grade", 2) }
                Pad("Good", "不错", Night.jade, tall = true) { combo++; press("grade", 3) }
                Pad("Easy", "简单", Night.violet, tall = true) { combo++; press("grade", 4) }
            }
            if (!aplus) Row { Pad("🔊  Play again", null, Night.ink, fill = { 0.06f }) { press("play", 0) } }
        }
    }

    TouchPad(server, Modifier.weight(1f)) { press("top", 0) }
}

/** Drag to scroll the page (finger up = page down, like a laptop touchpad); double-tap for the top. */
@Composable
private fun TouchPad(server: DesktopServer, modifier: Modifier, onTop: () -> Unit) {
    var touch by remember { mutableStateOf<Offset?>(null) }
    val glow by animateFloatAsState(if (touch != null) 1f else 0f, tween(250), label = "touch")
    var last by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier.fillMaxWidth().clip(shape)
            .background(Brush.verticalGradient(listOf(Night.cardTop.copy(alpha = .75f), Night.card.copy(alpha = .75f))))
            .border(1.dp, Night.outline, shape)
            .pointerInput(server) {
                var pending = 0f
                var lastSent = 0L
                detectVerticalDragGestures(
                    onDragStart = { touch = it; last = it },
                    onDragEnd = { touch = null; if (pending != 0f) { server.send("scroll", pending.toInt()); pending = 0f } },
                    onDragCancel = { touch = null },
                ) { change, dy ->
                    change.consume()
                    touch = change.position; last = change.position
                    pending -= dy * 2.5f
                    val now = System.currentTimeMillis()
                    if (now - lastSent > 90 && pending != 0f) { server.send("scroll", pending.toInt()); pending = 0f; lastSent = now }
                }
            }
            .pointerInput(server) { detectTapGestures(onDoubleTap = { last = it; onTop() }) },
        contentAlignment = Alignment.Center,
    ) {
        // A quiet dot grid, and a soft light under the finger.
        Canvas(Modifier.fillMaxSize()) {
            val step = 26.dp.toPx()
            var y = step / 2
            while (y < size.height) {
                var x = step / 2
                while (x < size.width) { drawCircle(Color.White.copy(alpha = .07f), 1.4.dp.toPx(), Offset(x, y)); x += step }
                y += step
            }
            if (glow > 0f) drawCircle(
                Brush.radialGradient(listOf(Night.sun.copy(alpha = .35f * glow), Color.Transparent), center = last, radius = 70.dp.toPx()),
                radius = 70.dp.toPx(), center = last,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = 1f - glow }) {
            Text("↕", fontSize = 26.sp, color = Night.muted)
            Text("Swipe to scroll", color = Night.muted, style = MaterialTheme.typography.bodyMedium)
            Text("Double-tap to jump to the top", color = Night.muted.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun RowScope.Segment(label: String, on: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (on) Night.sun else Color.Transparent, label = "seg")
    val fg by animateColorAsState(if (on) Color(0xFF1A1F3D) else Night.muted, label = "segText")
    Box(
        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(50)).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = fg, fontWeight = FontWeight.SemiBold) }
}

/** A big button that sinks a little when pressed. */
@Composable
private fun RowScope.Pad(label: String, zh: String?, color: Color, tall: Boolean = false, fill: () -> Float = { 0.2f }, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "press")
    Column(
        Modifier.weight(1f).height(if (tall) 76.dp else 52.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(shape)
            // Fill read while drawing, so a breathing button doesn't recompose the screen every frame.
            .drawBehind { drawRect(color.copy(alpha = if (pressed) fill() + 0.15f else fill())) }
            .border(1.dp, color.copy(alpha = .6f), shape)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = if (color == Night.ink) Night.ink else color, fontWeight = FontWeight.SemiBold)
        if (zh != null) Text(zh, color = color.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Couch speaking check: say the card into the phone; the offline check runs here and the result
 * goes to the computer, which colours each character. A clean answer counts as Good there.
 */
@Composable
private fun RowScope.SayIt(server: DesktopServer, vm: com.dudlatitat.nextchapter.MainViewModel, p: PageState, onGreat: () -> Unit) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var stopNow by remember { mutableStateOf(false) }
    val mic = com.dudlatitat.nextchapter.ui.learn.rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        val hanzi = p.hanzi; val pinyin = p.pinyin
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = com.dudlatitat.nextchapter.speech.Recorder.record(maxSeconds = 6f, stop = { stopNow })
            recording = false; busy = true
            val a = vm.coach.check(clip, hanzi, pinyin)
            busy = false
            val marks = a.chars
            val M = com.dudlatitat.nextchapter.speech.Mark
            val v = when {
                a.tooQuiet -> com.dudlatitat.nextchapter.data.Callouts.Verdict.MISS
                marks == null -> com.dudlatitat.nextchapter.data.Callouts.Verdict.CLOSE
                else -> com.dudlatitat.nextchapter.data.Callouts.verdict(marks.count { it.mark == M.RIGHT }, marks.count { it.mark == M.TONE }, marks.count { it.mark == M.MISSED })
            }
            // R = right, T = tone off, M = missed, one letter per character.
            val text = marks?.joinToString("") { when (it.mark) { M.RIGHT -> "R"; M.TONE -> "T"; M.MISSED -> "M" } } ?: ""
            server.send("said", when (v) { com.dudlatitat.nextchapter.data.Callouts.Verdict.GREAT -> 2; com.dudlatitat.nextchapter.data.Callouts.Verdict.CLOSE -> 1; else -> 0 }, if (a.tooQuiet) "quiet" else text)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (v == com.dudlatitat.nextchapter.data.Callouts.Verdict.GREAT) onGreat()
        }
    }
    Pad(
        when { recording -> "■  Stop"; busy -> "Checking…"; else -> "🎤  Say it" }, if (recording) "listening" else "说一说",
        if (recording) Night.coral else Night.jade, tall = true, fill = { if (recording) 0.32f else 0.2f },
    ) { if (!busy) mic() }
}
