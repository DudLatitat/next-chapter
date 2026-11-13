package com.dudlatitat.nextchapter.ui.learn

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.Deck
import com.dudlatitat.nextchapter.data.Decks
import com.dudlatitat.nextchapter.data.Grade
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.components.Collapsible
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.ScreenTitle
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night

@Composable
fun LearnScreen(
    state: AppState, vm: MainViewModel, startReview: Boolean, onReviewStarted: () -> Unit, startRedo: Boolean = false, startTool: String? = null,
    startDeck: String? = null, onDeckOpened: () -> Unit = {},
) {
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var redoing by rememberSaveable { mutableStateOf(false) }
    var drill by rememberSaveable { mutableStateOf<String?>(null) }
    var openDeck by rememberSaveable { mutableStateOf<String?>(null) }
    var tool by rememberSaveable { mutableStateOf<String?>(null) }
    var calibrating by rememberSaveable { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }

    LaunchedEffect(startTool) {
        if (startTool == "add") { adding = true; onReviewStarted() } // shortcut: straight to "add a phrase"
        else if (startTool != null) { tool = startTool; onReviewStarted() }
    }
    // Opening a deck from elsewhere (Progress → By deck).
    LaunchedEffect(startDeck) { if (startDeck != null) { openDeck = startDeck; onDeckOpened() } }
    LaunchedEffect(startReview, startRedo) {
        if (startReview) { reviewing = true; onReviewStarted() }
        if (startRedo) { redoing = true; onReviewStarted() }
    }

    when {
        calibrating -> CalibrateScreen(vm, onDone = { calibrating = false })
        reviewing -> Review(state, vm, onClose = { reviewing = false }, onCalibrate = { calibrating = true })
        redoing -> Review(state, vm, onClose = { redoing = false }, onCalibrate = { calibrating = true }, practice = true)
        drill != null -> ToneTrainer(vm, onBack = { drill = null }, onCalibrate = { calibrating = true }, focus = drill!!.split(",").map { it.toInt() }.toSet())
        tool == "dialogues" -> DialoguesScreen(vm, onBack = { tool = null }, onCalibrate = { calibrating = true })
        tool == "writing" -> WritingScreen(state, vm, onBack = { tool = null })
        tool == "drive" -> DriveScreen(vm, onBack = { tool = null })
        tool == "grammar" -> GrammarScreen(state, vm, onBack = { tool = null })
        tool == "stories" -> StoriesScreen(state, vm, onBack = { tool = null })
        tool == "build" -> BuildScreen(state, vm, onBack = { tool = null }, onCalibrate = { calibrating = true })
        tool == "culture" -> CultureScreen(vm, onBack = { tool = null })
        tool == "tones" -> ToneTrainer(vm, onBack = { tool = null }, onCalibrate = { calibrating = true })
        tool == "phrasebook" -> PhrasebookScreen(state, vm, onBack = { tool = null })
        tool == "callouts" -> CalloutScreen(state, vm, onBack = { tool = null })
        tool == "dinner" -> DinnerScreen(state, vm, onBack = { tool = null })
        openDeck != null -> DeckDetail(Decks.first { it.id == openDeck }, state, vm, onBack = { openDeck = null }, onAdd = { adding = true })
        else -> Overview(
            state, onReview = { reviewing = true }, onDeck = { openDeck = it }, onAdd = { adding = true }, onTool = { tool = it },
            onRedo = { redoing = true }, onDrill = { a, b -> drill = "$a,$b" },
        )
    }

    if (adding) AddPhraseDialog(onDismiss = { adding = false }) { h, p, e -> vm.addCard(h, p, e); adding = false }
}

@Composable
private fun Overview(
    state: AppState, onReview: () -> Unit, onDeck: (String) -> Unit, onAdd: () -> Unit, onTool: (String) -> Unit,
    onRedo: () -> Unit, onDrill: (Int, Int) -> Unit,
) {
    val doneToday = today().let { d -> state.cards.count { it.last == d } }
    val queue = Srs.queue(state)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Chinese", "学中文 · for real life there") }
        item {
            GlassCard(highlight = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (queue.isEmpty()) "You're all caught up ✓" else "${queue.size} waiting", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                        val fresh = queue.count { it.isNew }
                        Text(
                            if (queue.isEmpty()) "New phrases arrive tomorrow" else "${queue.size - fresh} to remember · $fresh new today",
                            style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                        )
                    }
                    Button(
                        onClick = onReview, enabled = queue.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                    ) { Text("Practice") }
                }
                if (doneToday > 0) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = onRedo) { Text("↻  Redo today ($doneToday)", color = Night.sun) }
                }
            }
        }
        // Practice tools right under the review card (always open), grouped by what you're doing.
        item {
            val groups = listOf(
                "Play" to listOf(Triple("家宴", "Dinner", "dinner") to Night.rose, Triple("喊话", "Callouts", "callouts") to Night.coral),
                "Speak & listen" to listOf(Triple("声调", "Tones", "tones") to Night.violet, Triple("对话", "Dialogues", "dialogues") to Night.jade, Triple("开车", "Drive", "drive") to Night.jade),
                "Build & read" to listOf(Triple("造句", "Build", "build") to Night.jade, Triple("故事", "Stories", "stories") to Night.rose, Triple("语法", "Grammar", "grammar") to Night.violet),
                "Write & go" to listOf(Triple("写字", "Writing", "writing") to Night.sun, Triple("救急", "Phrasebook", "phrasebook") to Night.rose, Triple("礼节", "Etiquette", "culture") to Night.sun),
            )
            GlassCard {
                Text("Practice", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.forEach { (label, row) ->
                        Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (t, color) -> ToolTile(t.first, t.second, color, Modifier.weight(1f)) { onTool(t.third) } }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        item {
            val visible = Decks.filter { d -> d.id != "mine" || state.cards.any { it.deck == "mine" } }
            val started = visible.count { d -> state.cards.any { it.deck == d.id && !it.isNew } }
            Collapsible("learn.decks", "Decks · ${visible.size}", summary = "$started started · tap to see them all", defaultOpen = true) {
                // A grid of small tiles (2 across on the cover screen, 3 unfolded): far less scrolling.
                val cols = if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) 3 else 2
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visible.chunked(cols).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { deck -> DeckTile(deck, state, Modifier.weight(1f)) { onDeck(deck.id) } }
                            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                TextButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, null, tint = Night.sun)
                    Spacer(Modifier.width(4.dp))
                    Text("Add phrase", color = Night.sun)
                }
            }
        }
        // Then your tones and how it's going.
        item { WeakTones(state, onDrill) }
        item { SectionLabel("How it's going") }
        item { com.dudlatitat.nextchapter.ui.progress.ChineseProgress(state, onOpenDeck = onDeck) }
    }
}

/** One deck as a small tile: Chinese name, English name, progress. */
@Composable
private fun DeckTile(deck: Deck, state: AppState, modifier: Modifier, onClick: () -> Unit) {
    val cards = state.cards.filter { it.deck == deck.id }
    val started = cards.count { !it.isNew }
    Column(
        modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(Night.ground2.copy(alpha = 0.5f))
            .clickable(onClick = onClick).padding(10.dp),
    ) {
        Text(deck.hanzi, fontSize = 15.sp, color = Night.sun, maxLines = 1)
        Text(deck.title, style = MaterialTheme.typography.labelLarge, color = Night.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Bar(if (cards.isEmpty()) 0f else started.toFloat() / cards.size, Night.jade)
        val opensIn = if (deck.id == "trip") com.dudlatitat.nextchapter.data.TripPrep.opensIn(state, com.dudlatitat.nextchapter.data.today()) else null
        Text(
            if (opensIn != null) "🔒 opens in $opensIn ${if (opensIn == 1L) "day" else "days"}" else "$started/${cards.size}",
            style = MaterialTheme.typography.labelSmall, color = if (opensIn != null) Night.rose else Night.muted,
        )
    }
}

/** One deck as a single compact line: name, progress bar, count. */
@Composable
private fun DeckRow(deck: Deck, state: AppState, onClick: () -> Unit) {
    val cards = state.cards.filter { it.deck == deck.id }
    val started = cards.count { !it.isNew }
    val known = cards.count { it.isKnown }
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(deck.hanzi, fontSize = if (deck.hanzi.length > 3) 13.sp else 17.sp, maxLines = 1, color = Night.sun, modifier = Modifier.width(60.dp))
        Column(Modifier.weight(1f)) {
            Text(deck.title, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
            Bar(if (cards.isEmpty()) 0f else started.toFloat() / cards.size, Night.jade)
        }
        Spacer(Modifier.width(10.dp))
        Text(if (known > 0) "$known✓ $started/${cards.size}" else "$started/${cards.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeckDetail(deck: Deck, state: AppState, vm: MainViewModel, onBack: () -> Unit, onAdd: () -> Unit) {
    BackHandler(onBack = onBack)
    var confirmDelete by remember { mutableStateOf<Card?>(null) }
    val cards = state.cards.filter { it.deck == deck.id }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column(Modifier.weight(1f)) {
                    Text(deck.title, style = MaterialTheme.typography.headlineMedium, color = Night.ink)
                    Text("${deck.hanzi} · tap to listen", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                if (deck.id == "mine") IconButton(onClick = onAdd) { Icon(Icons.Rounded.Add, "Add", tint = Night.sun) }
            }
        }
        items(cards, key = { it.id }) { card ->
            Box(
                Modifier.combinedClickable(
                    onClick = { vm.speaker.say(card.hanzi) },
                    onLongClick = { if (card.custom) confirmDelete = card },
                ),
            ) {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(card.hanzi, fontSize = 22.sp, color = Night.ink)
                            PinyinText(card.pinyin, style = MaterialTheme.typography.bodyMedium)
                            Text(card.english, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                        }
                        when {
                            card.isKnown -> Pill("Known", Night.jade)
                            !card.isNew -> Pill("Learning", Night.violet)
                            else -> Pill("New", Night.muted)
                        }
                    }
                }
            }
        }
        if (deck.id == "mine") item {
            Text("Long-press a phrase to delete it.", style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.padding(4.dp))
        }
    }
    confirmDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete phrase?") },
            text = { Text("${card.hanzi} · ${card.english}") },
            confirmButton = { TextButton(onClick = { vm.deleteCard(card); confirmDelete = null }) { Text("Delete", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Keep") } },
        )
    }
}

/** Which side of the card you're quizzed on. */
private enum class Direction { READ, SAY, LISTEN }

@Composable
private fun Review(state: AppState, vm: MainViewModel, onClose: () -> Unit, onCalibrate: () -> Unit, practice: Boolean = false) {
    BackHandler(onBack = onClose)
    // The queue as it was when the session started; misses go back to the end. Practice mode
    // replays today's cards without touching their schedule.
    // Saveable, so glancing at another tab (or folding the phone) keeps your place in the session.
    val queue = rememberSaveable(saver = idListSaver) {
        mutableStateListOf<String>().apply {
            addAll(if (practice) today().let { d -> state.cards.filter { it.last == d }.shuffled().map { it.id } } else Srs.queue(state).map { it.id })
        }
    }
    val startedAt = remember { System.currentTimeMillis() }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { vm.logSeconds(((System.currentTimeMillis() - startedAt) / 1000).toInt()) }
    }
    fun gradeCard(card: Card, g: Grade) { if (!practice) vm.grade(card, g) }
    val total = rememberSaveable { queue.size.coerceAtLeast(1) }
    var done by rememberSaveable { mutableIntStateOf(0) }
    var shown by rememberSaveable { mutableStateOf(false) }
    var direction by rememberSaveable { mutableStateOf(Direction.READ) }
    var speaking by remember { mutableStateOf(false) }
    // Phrases missed at least once this session, for the summary at the end.
    val missed = rememberSaveable(saver = idListSaver) { mutableStateListOf<String>() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    // The grade just made, for a few seconds: Undo puts the card back exactly as it was.
    var undo by remember { mutableStateOf<Pair<Card, Grade>?>(null) }
    LaunchedEffect(undo) { if (undo != null) { kotlinx.coroutines.delay(4000); undo = null } }

    val card = queue.firstOrNull()?.let { id -> state.cards.firstOrNull { it.id == id } }

    fun advance(c: Card, g: Grade) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        undo = c to g
        gradeCard(c, g)
        queue.removeAt(0)
        if (g == Grade.AGAIN) { queue.add(c.id); if (c.id !in missed) missed.add(c.id) } else done++
        shown = false
    }
    fun takeBack() {
        val (before, g) = undo ?: return
        if (!practice) vm.ungrade(before)
        if (g == Grade.AGAIN) { val i = queue.lastIndexOf(before.id); if (i >= 0) queue.removeAt(i) } else done = (done - 1).coerceAtLeast(0)
        queue.add(0, before.id)
        shown = true
        undo = null
    }

    LaunchedEffect(card?.id) {
        if (card == null) { if (!practice) vm.finishedSession() }
        else if (direction != Direction.SAY) vm.speaker.say(card.hanzi)
    }

    if (speaking && card != null) SpeakSheet(card, vm, onCalibrate, onDismiss = { speaking = false })

    Column(Modifier.fillMaxSize().readable().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Night.ink) }
            Bar(done.toFloat() / total, Night.jade, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text("${queue.size} left", style = MaterialTheme.typography.labelLarge, color = Night.muted)
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DirChip("Read 读", direction == Direction.READ) { direction = Direction.READ; shown = false }
            DirChip("Say 说", direction == Direction.SAY) { direction = Direction.SAY; shown = false }
            DirChip("Listen 听", direction == Direction.LISTEN) { direction = Direction.LISTEN; shown = false }
        }

        if (card == null) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("做得好！", style = MaterialTheme.typography.headlineMedium, color = Night.sun)
                Text("Session done · $done phrases", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text(
                    if (missed.isEmpty()) "No misses. 完美!" else "${done - missed.size} right first try · ${missed.size} to keep an eye on",
                    style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                )
                if (missed.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    GlassCard {
                        Text("Tricky ones", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                        missed.mapNotNull { id -> state.cards.firstOrNull { it.id == id } }.forEach { c ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                                    Text(c.hanzi, fontSize = 20.sp, color = Night.ink)
                                    PinyinText(c.pinyin, style = MaterialTheme.typography.bodyMedium)
                                    Text(c.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                                }
                                IconButton(onClick = { vm.speaker.say(c.hanzi) }) { Icon(Icons.Rounded.PlayArrow, "Listen", tint = Night.ink) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Back") }
            }
            return@Column
        }

        if (direction == Direction.LISTEN) {
            val options = remember(card.id) {
                (state.cards.filter { it.id != card.id && it.english != card.english }.shuffled().take(3).map { it.english } + card.english).shuffled()
            }
            var picked by remember(card.id) { mutableStateOf<String?>(null) }
            // Right answers move on by themselves; a wrong one waits so you can look at it.
            LaunchedEffect(picked) { if (picked != null && picked == card.english) { kotlinx.coroutines.delay(900); advance(card, Grade.GOOD) } }
            Spacer(Modifier.height(16.dp))
            GlassCard(Modifier.weight(1f), highlight = true) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("What did you hear?", style = MaterialTheme.typography.titleMedium, color = Night.muted)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { vm.speaker.say(card.hanzi) }, modifier = Modifier.height(72.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Night.sun.copy(alpha = 0.2f), contentColor = Night.sun),
                    ) { Icon(Icons.Rounded.PlayArrow, null); Text("  Play again", style = MaterialTheme.typography.titleMedium) }
                    TextButton(onClick = { vm.speaker.say(card.hanzi, slow = true) }) { Text("Slow", color = Night.muted) }
                    if (picked != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(card.hanzi, fontSize = 36.sp, color = Night.ink)
                        PinyinText(card.pinyin, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val right = option == card.english
                    val color = when {
                        picked == null -> Night.ink
                        right -> Night.jade
                        option == picked -> Night.coral
                        else -> Night.muted
                    }
                    Button(
                        onClick = {
                            if (picked == null) picked = option
                            else advance(card, if (picked == card.english) Grade.GOOD else Grade.AGAIN)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.14f), contentColor = color),
                    ) { Text(option, style = MaterialTheme.typography.titleSmall) }
                }
                if (picked != null) Text(
                    if (picked == card.english) "Right! 对了" else "Not quite. Tap any answer to continue.",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            return@Column
        }

        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.weight(1f), highlight = true, onClick = { shown = true }) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                if (card.isNew) Pill("New phrase", Night.rose)
                Spacer(Modifier.height(12.dp))
                if (direction == Direction.READ) {
                    Text(card.hanzi, fontSize = 44.sp, color = Night.ink, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                } else {
                    Text(card.english, style = MaterialTheme.typography.headlineMedium, color = Night.ink, textAlign = TextAlign.Center)
                }
                val reveal by animateFloatAsState(if (shown) 1f else 0f, tween(300), label = "reveal")
                Spacer(Modifier.height(20.dp))
                Column(Modifier.alpha(reveal), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (direction == Direction.SAY) Text(card.hanzi, fontSize = 40.sp, color = Night.ink, textAlign = TextAlign.Center)
                    PinyinText(card.pinyin, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    if (direction == Direction.READ) Text(card.english, style = MaterialTheme.typography.titleMedium, color = Night.muted, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    TextButton(onClick = { vm.speaker.say(card.hanzi) }) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Night.ink); Text("Listen", color = Night.ink)
                    }
                    TextButton(onClick = { vm.speaker.say(card.hanzi, slow = true) }) { Text("Slow", color = Night.muted) }
                    if (shown) TextButton(onClick = { speaking = true }) { Text("🎤 Say it", color = Night.sun) }
                }
                if (!shown) Text("Tap to reveal", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        Spacer(Modifier.height(14.dp))
        AnimatedVisibility(shown, enter = fadeIn()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Grade.entries.forEach { g ->
                    val color = when (g) { Grade.AGAIN -> Night.coral; Grade.HARD -> Night.violet; Grade.GOOD -> Night.jade; Grade.EASY -> Night.sun }
                    val next = Srs.grade(card, g)
                    Button(
                        onClick = {
                            if (direction == Direction.SAY && g != Grade.AGAIN) vm.speaker.say(card.hanzi)
                            advance(card, g)
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        contentPadding = PaddingValues(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f), contentColor = color),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(g.label, style = MaterialTheme.typography.labelLarge)
                            Text(if (next.due <= today()) "now" else "${next.interval}d", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        if (!shown) Spacer(Modifier.height(64.dp))
        UndoChip(undo, onUndo = ::takeBack)
    }
}

/** "↶ Undo" for a few seconds after each grade, in case of a mis-tap. */
@Composable
private fun UndoChip(undo: Pair<Card, Grade>?, onUndo: () -> Unit) {
    AnimatedVisibility(undo != null, enter = fadeIn(), exit = androidx.compose.animation.fadeOut()) {
        val (c, g) = undo ?: return@AnimatedVisibility
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onUndo) { Text("↶  Undo ${g.label} · ${c.hanzi}", color = Night.sun) }
        }
    }
}

private val idListSaver = androidx.compose.runtime.saveable.listSaver<androidx.compose.runtime.snapshots.SnapshotStateList<String>, String>(
    save = { it.toList() }, restore = { it.toMutableStateList() },
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SpeakSheet(card: Card, vm: MainViewModel, onCalibrate: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Night.ground1) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(card.hanzi, fontSize = 34.sp, color = Night.ink, textAlign = TextAlign.Center)
            PinyinText(card.pinyin, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            TextButton(onClick = { vm.speaker.say(card.hanzi) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Hear it first", color = Night.sun) }
            SpeakPanel(card.hanzi, card.pinyin, vm, onCalibrate = { onDismiss(); onCalibrate() })
        }
    }
}

@Composable
private fun DirChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Night.sun.copy(alpha = 0.18f), selectedLabelColor = Night.sun,
            labelColor = Night.muted, containerColor = Color.Transparent,
        ),
    )
}

@Composable
fun AddPhraseDialog(initialHanzi: String = "", onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var hanzi by remember { mutableStateOf(initialHanzi) }
    var pinyin by remember { mutableStateOf(com.dudlatitat.nextchapter.data.Pinyin.fromHanzi(initialHanzi) ?: "") }
    var english by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a phrase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Paste a line from WeChat you want to learn.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                OutlinedTextField(hanzi, { hanzi = it }, label = { Text("Chinese 中文") }, singleLine = false)
                OutlinedTextField(
                    pinyin, { pinyin = it }, label = { Text("Pinyin") }, singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { com.dudlatitat.nextchapter.data.Pinyin.fromHanzi(hanzi)?.let { pinyin = it } }, enabled = hanzi.isNotBlank()) { Text("Auto") }
                    },
                )
                OutlinedTextField(english, { english = it }, label = { Text("Meaning") }, singleLine = false)
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(hanzi, pinyin, english) }, enabled = hanzi.isNotBlank() && english.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ToolTile(zh: String, title: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    // A smaller cousin of GlassCard: four fit across even the Fold's cover screen.
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    Column(
        modifier.clip(shape).background(Night.card.copy(alpha = 0.85f)).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(zh, fontSize = 18.sp, color = color, maxLines = 1)
        Text(title, fontSize = 12.sp, color = Night.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}


/** Accuracy for each tone lately, and a drill for the pair you mix up most. */
@Composable
private fun WeakTones(state: AppState, onDrill: (Int, Int) -> Unit) {
    val S = com.dudlatitat.nextchapter.data.Stats
    val acc = S.toneAccuracy(state)
    if (acc.values.all { it == null }) return
    val d = today()
    // This week vs the three weeks before, so you see the direction, not just a number.
    val now = S.toneAccuracyBetween(state, d - 6, d, min = 3)
    val before = S.toneAccuracyBetween(state, d - 30, d - 7, min = 3)
    val pair = S.weakestPair(state)
    GlassCard {
        Text("Your tones · last 30 days", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        (1..4).forEach { t ->
            val a = acc[t]
            val n = now[t]; val b = before[t]
            val trend = if (n != null && b != null) when { n > b + 0.05f -> " · ↑ better this week"; n < b - 0.05f -> " · ↓ dipped this week"; else -> "" } else ""
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Text("Tone $t", style = MaterialTheme.typography.labelMedium, color = com.dudlatitat.nextchapter.ui.components.toneColor(t), modifier = Modifier.width(56.dp))
                Column(Modifier.weight(1f)) {
                    Bar(a ?: 0f, com.dudlatitat.nextchapter.ui.components.toneColor(t).copy(alpha = 0.8f))
                    Text(S.toneWord(a) + trend, style = MaterialTheme.typography.labelSmall, color = Night.muted)
                }
            }
        }
        if (pair != null) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onDrill(pair.first, pair.second) }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Night.violet.copy(alpha = 0.2f), contentColor = Night.violet),
            ) { Text("Practice tones ${pair.first} & ${pair.second} · 2 min") }
        }
    }
}
