package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Builder
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 造句 Build: a mixed round of 8 from your own phrases. Build it from tiles, fill the missing word,
 * or listen and build what you heard; then hear it, see the pinyin, and say it if you like.
 */
@Composable
fun BuildScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit, onCalibrate: () -> Unit) {
    BackHandler(onBack = onBack)
    var seed by remember { mutableIntStateOf(Random.nextInt()) }
    val round = remember(seed) { Builder.round(state.cards, Random(seed)) }
    var index by remember(seed) { mutableIntStateOf(0) }
    var score by remember(seed) { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("造句 · Build sentences", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        if (round.isEmpty()) {
            GlassCard { Text("Start a few phrase decks first; sentences come from your phrases.", color = Night.muted) }
        } else {
            // Progress: one dot per sentence.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                round.indices.forEach { i ->
                    Box(Modifier.size(10.dp).clip(CircleShape).background(if (i < index) Night.jade else if (i == index) Night.sun else Night.muted.copy(alpha = 0.3f)))
                }
            }
            if (index >= round.size) {
                LaunchedEffect(seed) { vm.finishedSession() }
                GlassCard(highlight = true) {
                    Text(if (score == round.size) "Perfect round! 太棒了 🎉" else "$score of ${round.size} right", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text(
                        if (score >= round.size - 2) "Those sentences are yours now. Try one on her tonight." else "Every round makes the next one easier. 加油!",
                        style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { seed = Random.nextInt() }, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2)) { Text("Another round") }
                        OutlinedButton(onClick = onBack) { Text("Done", color = Night.ink) }
                    }
                }
            } else {
                val item = round[index]
                // Keyed to the item, so each exercise starts fresh.
                androidx.compose.runtime.key(seed, index) {
                    Exercise(item, vm, onCalibrate, onDone = { right -> if (right) score++; index++ })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Exercise(item: Builder.Item, vm: MainViewModel, onCalibrate: () -> Unit, onDone: (Boolean) -> Unit) {
    val scope = rememberCoroutineScope()
    val placed = remember { mutableStateListOf<Int>() } // indexes into item.tiles
    var tries by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Boolean?>(null) } // null = still answering
    var showEnglish by remember { mutableStateOf(item.kind != Builder.Kind.LISTEN) }
    var picked by remember { mutableStateOf<String?>(null) }
    var speaking by remember { mutableStateOf(false) }
    val wiggle = remember { Animatable(0f) }

    LaunchedEffect(Unit) { if (item.kind == Builder.Kind.LISTEN) vm.speaker.say(item.card.hanzi) }

    fun wrong() {
        tries++
        scope.launch { repeat(3) { wiggle.animateTo(10f, spring(stiffness = 3000f)); wiggle.animateTo(-10f, spring(stiffness = 3000f)) }; wiggle.animateTo(0f) }
        if (tries >= 2) { result = false; vm.speaker.say(item.card.hanzi) }
    }
    fun right() { result = true; vm.speaker.say(item.card.hanzi) }

    GlassCard(highlight = true, modifier = Modifier.animateContentSize()) {
        Text(
            when (item.kind) { Builder.Kind.BUILD -> "Build it"; Builder.Kind.FILL -> "Fill the missing word"; Builder.Kind.LISTEN -> "Listen, then build it" },
            style = MaterialTheme.typography.labelMedium, color = Night.muted,
        )
        Spacer(Modifier.height(6.dp))
        if (item.kind == Builder.Kind.LISTEN) Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { vm.speaker.say(item.card.hanzi) }) { Text("🔊 Play again", color = Night.sun) }
            if (!showEnglish) TextButton(onClick = { showEnglish = true }) { Text("English hint", color = Night.muted) }
        }
        if (showEnglish) Text(item.card.english, style = MaterialTheme.typography.titleLarge, color = Night.ink)
        Spacer(Modifier.height(12.dp))

        if (item.kind == Builder.Kind.FILL) {
            // The sentence with a gap, then four choices.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.answer.forEachIndexed { i, w ->
                    if (i == item.blank) {
                        val shown = if (result != null) w else picked ?: "＿＿"
                        Text(
                            shown, fontSize = 24.sp,
                            color = when (result) { true -> Night.jade; false -> Night.coral; null -> Night.sun },
                            modifier = Modifier.offset { IntOffset(wiggle.value.toInt(), 0) }.border(1.dp, Night.sun.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp),
                        )
                    } else Text(w, fontSize = 24.sp, color = Night.ink)
                }
            }
            Spacer(Modifier.height(14.dp))
            if (result == null) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.tiles.forEach { w ->
                    Tile(w, Night.violet) {
                        picked = w
                        if (w == item.answer[item.blank]) right() else wrong()
                    }
                }
            }
        } else {
            // Your sentence so far (tap a tile to send it back), then the tiles left.
            val marks = if (result != null) Builder.marks(item.answer, placed.map { item.tiles[it] }) else null
            FlowRow(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).offset { IntOffset(wiggle.value.toInt(), 0) }
                    .border(1.dp, Night.muted.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (placed.isEmpty()) Text("Tap the words in order…", color = Night.muted, modifier = Modifier.padding(6.dp))
                placed.forEachIndexed { pos, t ->
                    val color = when (marks?.getOrNull(pos)) { true -> Night.jade; false -> Night.coral; null -> Night.sun }
                    Tile(item.tiles[t], color) { if (result == null) placed.removeAt(pos) }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (result == null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.tiles.forEachIndexed { t, w ->
                        if (t !in placed) Tile(w, Night.violet) { placed.add(t) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { if (Builder.isRight(item.answer, placed.map { item.tiles[it] })) right() else wrong() },
                        enabled = placed.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2),
                    ) { Text("Check") }
                    if (tries == 1) Text("Not quite. One more try!", color = Night.coral, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // The answer: hear it, see the pinyin, say it if you like.
        AnimatedVisibility(result != null, enter = fadeIn()) {
            Column {
                Spacer(Modifier.height(14.dp))
                Text(
                    if (result == true) (if (tries == 0) "Right! 对了 ✓" else "Got it ✓") else "Here's how it goes:",
                    style = MaterialTheme.typography.titleMedium, color = if (result == true) Night.jade else Night.coral,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { vm.speaker.say(item.card.hanzi) }) {
                    Text(item.card.hanzi, fontSize = 26.sp, color = Night.ink)
                    Text("  🔊", color = Night.muted)
                }
                PinyinText(item.card.pinyin, style = MaterialTheme.typography.bodyLarge)
                if (!showEnglish) Text(item.card.english, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                Spacer(Modifier.height(10.dp))
                if (speaking) SpeakPanel(item.card.hanzi, item.card.pinyin, vm, onCalibrate)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!speaking) OutlinedButton(onClick = { speaking = true }) { Text("🎤 Say it", color = Night.sun) }
                    Button(onClick = { onDone(result == true) }, colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2)) { Text("Next →") }
                }
            }
        }
    }
}

@Composable
private fun Tile(word: String, color: Color, onClick: () -> Unit) {
    Text(
        word, fontSize = 22.sp, color = Night.ink,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.22f)).border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
