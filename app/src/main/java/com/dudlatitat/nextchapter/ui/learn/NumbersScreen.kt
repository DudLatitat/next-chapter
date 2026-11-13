package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.dudlatitat.nextchapter.data.Money
import com.dudlatitat.nextchapter.data.Numbers
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.theme.Night

/** Practice → Number ears 听价格: hear a price or a time at real speed, type the number. */
@Composable
fun NumbersScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    var kind by remember { mutableStateOf<Numbers.Kind?>(null) }
    var round by remember { mutableStateOf<List<Numbers.Item>?>(null) }
    var i by remember { mutableIntStateOf(0) }
    var typed by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Boolean?>(null) }
    var streak by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var right by remember { mutableIntStateOf(0) }
    var bestBefore by remember { mutableIntStateOf(state.numbersBest) }
    val started = remember { System.currentTimeMillis() }
    DisposableEffect(Unit) { onDispose { vm.logSeconds(((System.currentTimeMillis() - started) / 1000).toInt()) } }

    val r = round
    val item = r?.getOrNull(i)
    val done = r != null && i >= r.size
    fun play() { item?.let { vm.speaker.say(it.hanzi, speed = Numbers.speed(streak)) } }
    LaunchedEffect(i, round) { if (result == null) play() }
    fun start(k: Numbers.Kind?) { kind = k; bestBefore = state.numbersBest; round = Numbers.round(k); i = 0; typed = ""; result = null; streak = 0; score = 0; right = 0 }
    fun check() {
        val it = item ?: return
        val ok = Numbers.matches(it, typed)
        result = ok
        if (ok) { streak++; right++; score += 10 + 5 * streak; haptic.performHapticFeedback(HapticFeedbackType.LongPress) } else streak = 0
    }
    fun next() {
        typed = ""; result = null; i++
        if (r != null && i >= r.size) vm.saveNumbersScore(score)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Number ears", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("听价格 · prices and times at real speed", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            if (r != null && !done) {
                val shown by animateIntAsState(score, tween(400), label = "score")
                Text("$shown", style = MaterialTheme.typography.titleLarge, color = Night.sun)
            }
        }

        when {
            r == null -> GlassCard(highlight = true) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👂", fontSize = 52.sp)
                    Text("At the market, in a taxi, on the phone: numbers come fast.", style = MaterialTheme.typography.titleMedium, color = Night.ink, textAlign = TextAlign.Center)
                    Text(
                        "Listen, then type the number. Every right answer in a row makes the next one a little faster.",
                        style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center,
                    )
                    if (state.numbersBest > 0) Text("Best: ${state.numbersBest}", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { start(Numbers.Kind.PRICE) }, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("¥ Prices") }
                        Button(onClick = { start(Numbers.Kind.TIME) }, colors = ButtonDefaults.buttonColors(containerColor = Night.violet, contentColor = Night.ground2)) { Text("🕒 Times") }
                        Button(onClick = { start(null) }, colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2)) { Text("Mix") }
                    }
                }
            }
            done -> GlassCard(highlight = true) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (right >= 9) "🏆" else if (right >= 6) "🌟" else "🌱", fontSize = 52.sp)
                    Text("$right / ${r.size} right · $score", style = MaterialTheme.typography.headlineSmall, color = Night.sun)
                    Text(
                        if (score > bestBefore && bestBefore > 0) "New best! 新纪录！" else if (right >= 8) "The market won't fool you. 厉害！" else "A few more rounds and numbers stop feeling fast.",
                        style = MaterialTheme.typography.bodyMedium, color = Night.ink, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onBack) { Text("Done", color = Night.muted) }
                        Button(onClick = { start(kind) }, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Again · 再来") }
                    }
                }
            }
            item != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1} / ${r.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.weight(1f))
                    if (streak >= 2) Text("🔥 ×$streak · ${"%.2f".format(java.util.Locale.US, Numbers.speed(streak))}×", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                }
                GlassCard(highlight = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (item.kind == Numbers.Kind.PRICE) "¥ How much?" else "🕒 What time?", style = MaterialTheme.typography.titleMedium, color = Night.muted)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = ::play, modifier = Modifier.height(64.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Night.sun.copy(alpha = 0.2f), contentColor = Night.sun),
                        ) { Text("🔊  Play again", style = MaterialTheme.typography.titleMedium) }
                        TextButton(onClick = { vm.speaker.say(item.hanzi, slow = true) }) { Text("Slow", color = Night.muted) }
                        // What you've typed, big, like a price tag.
                        val shake = remember(i) { Animatable(0f) }
                        LaunchedEffect(result) { if (result == false) { repeat(3) { shake.animateTo(10f, tween(40)); shake.animateTo(-10f, tween(40)) }; shake.animateTo(0f, tween(40)) } }
                        Text(
                            typed.ifEmpty { if (item.kind == Numbers.Kind.PRICE) "¥ —" else "— : —" },
                            fontSize = 40.sp,
                            color = when (result) { true -> Night.jade; false -> Night.coral; null -> Night.ink },
                            modifier = Modifier.graphicsLayer { translationX = shake.value },
                        )
                        result?.let { ok ->
                            Text(item.hanzi, fontSize = 26.sp, color = Night.ink)
                            PinyinText(com.dudlatitat.nextchapter.data.Pinyin.fromHanzi(item.hanzi) ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (item.kind == Numbers.Kind.PRICE) "${if (ok) "✓" else "It was"} ¥${item.answer} ≈ $${"%.2f".format(java.util.Locale.US, Money.toDollars(item.answer.toDouble(), state.rate))}"
                                else "${if (ok) "✓" else "It was"} ${item.answer}",
                                style = MaterialTheme.typography.titleSmall, color = if (ok) Night.jade else Night.coral,
                            )
                        }
                    }
                }
                if (result == null) Keypad(
                    time = item.kind == Numbers.Kind.TIME,
                    onKey = { k -> if (typed.length < 7) typed += k },
                    onDelete = { typed = typed.dropLast(1) },
                    onEnter = { if (typed.isNotEmpty()) check() },
                ) else Button(
                    onClick = ::next, modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                ) { Text(if (i + 1 >= r.size) "See my score" else "Next  →") }
            }
        }
    }
}

/** A big number pad (the phone keyboard is small and hides the card). */
@Composable
private fun Keypad(time: Boolean, onKey: (String) -> Unit, onDelete: () -> Unit, onEnter: () -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf(if (time) ":" else ".", "0", "⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    Box(
                        Modifier.weight(1f).height(54.dp).background(Night.cardHigh, RoundedCornerShape(16.dp))
                            .clickable { if (k == "⌫") onDelete() else onKey(k) },
                        contentAlignment = Alignment.Center,
                    ) { Text(k, fontSize = 22.sp, color = Night.ink) }
                }
            }
        }
        Button(
            onClick = onEnter, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2),
        ) { Text("Check ✓", style = MaterialTheme.typography.titleMedium) }
    }
}
