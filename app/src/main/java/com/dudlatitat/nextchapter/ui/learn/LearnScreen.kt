package com.dudlatitat.nextchapter.ui.learn

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
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.ScreenTitle
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night

@Composable
fun LearnScreen(state: AppState, vm: MainViewModel, startReview: Boolean, onReviewStarted: () -> Unit, startRedo: Boolean = false, startTool: String? = null) {
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var redoing by rememberSaveable { mutableStateOf(false) }
    var drill by rememberSaveable { mutableStateOf<String?>(null) }
    var openDeck by rememberSaveable { mutableStateOf<String?>(null) }
    var tool by rememberSaveable { mutableStateOf<String?>(null) }
    var calibrating by rememberSaveable { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }

    LaunchedEffect(startTool) { if (startTool != null) { tool = startTool; onReviewStarted() } }
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
        tool == "culture" -> CultureScreen(vm, onBack = { tool = null })
        tool == "tones" -> ToneTrainer(vm, onBack = { tool = null }, onCalibrate = { calibrating = true })
        tool == "phrasebook" -> PhrasebookScreen(state, vm, onBack = { tool = null })
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
    val wide = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Learn", "学中文 · phrases for your life there") }
        item {
            GlassCard(highlight = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (queue.isEmpty()) "You're caught up" else "${queue.size} to review", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                        val fresh = queue.count { it.isNew }
                        Text(
                            if (queue.isEmpty()) "New phrases unlock tomorrow" else "${queue.size - fresh} reviews · $fresh new",
                            style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                        )
                    }
                    Button(
                        onClick = onReview, enabled = queue.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                    ) { Text("Review") }
                }
                if (doneToday > 0) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = onRedo) { Text("↻  Redo today ($doneToday)", color = Night.sun) }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolCard("声调", "Tone trainer", "Hear it or say it", Night.violet, Modifier.weight(1f)) { onTool("tones") }
                    ToolCard("对话", "Dialogues", "Real scenes, answer out loud", Night.jade, Modifier.weight(1f)) { onTool("dialogues") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolCard("写字", "Writing", "Trace with stroke order", Night.sun, Modifier.weight(1f)) { onTool("writing") }
                    ToolCard("救急", "Phrasebook", "Big text to show people", Night.rose, Modifier.weight(1f)) { onTool("phrasebook") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolCard("语法", "Grammar", "Patterns that build sentences", Night.violet, Modifier.weight(1f)) { onTool("grammar") }
                    ToolCard("故事", "Stories", "Read, tap any word", Night.rose, Modifier.weight(1f)) { onTool("stories") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolCard("开车", "Drive mode", "Hands-free on your commute", Night.jade, Modifier.weight(1f)) { onTool("drive") }
                    ToolCard("礼节", "Etiquette", "Meeting her family, holidays", Night.sun, Modifier.weight(1f)) { onTool("culture") }
                }
            }
        }
        item { WeakTones(state, onDrill) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Decks", Modifier.weight(1f))
                TextButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, null, tint = Night.sun)
                    Spacer(Modifier.width(4.dp))
                    Text("Add phrase", color = Night.sun)
                }
            }
        }
        val visible = Decks.filter { d -> d.id != "mine" || state.cards.any { it.deck == "mine" } }
        items(visible.chunked(if (wide) 2 else 1), key = { row -> row.first().id }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { deck -> DeckCard(deck, state, Modifier.weight(1f)) { onDeck(deck.id) } }
                if (row.size == 1 && wide) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DeckCard(deck: Deck, state: AppState, modifier: Modifier, onClick: () -> Unit) {
    val cards = state.cards.filter { it.deck == deck.id }
    val known = cards.count { it.isKnown }
    val started = cards.count { !it.isNew }
    GlassCard(modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(deck.hanzi, fontSize = if (deck.hanzi.length > 3) 16.sp else 22.sp, maxLines = 1, color = Night.sun, modifier = Modifier.width(76.dp))
            Column(Modifier.weight(1f)) {
                Text(deck.title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text(deck.blurb, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                Spacer(Modifier.height(8.dp))
                Bar(if (cards.isEmpty()) 0f else started.toFloat() / cards.size, Night.jade)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (started == 0) "Not started" else "${started - known} learning · $known known",
                    style = MaterialTheme.typography.labelSmall, color = Night.muted,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text("$started/${cards.size}", style = MaterialTheme.typography.labelLarge, color = Night.muted)
        }
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
    val queue = remember {
        mutableStateListOf<String>().apply {
            addAll(if (practice) today().let { d -> state.cards.filter { it.last == d }.shuffled().map { it.id } } else Srs.queue(state).map { it.id })
        }
    }
    val startedAt = remember { System.currentTimeMillis() }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { vm.logSeconds(((System.currentTimeMillis() - startedAt) / 1000).toInt()) }
    }
    fun gradeCard(card: Card, g: Grade) { if (!practice) vm.grade(card, g) }
    val total = remember { queue.size.coerceAtLeast(1) }
    var done by remember { mutableIntStateOf(0) }
    var shown by remember { mutableStateOf(false) }
    var direction by rememberSaveable { mutableStateOf(Direction.READ) }
    var speaking by remember { mutableStateOf(false) }
    // Phrases missed at least once this session, for the summary at the end.
    val missed = remember { mutableStateListOf<String>() }

    val card = queue.firstOrNull()?.let { id -> state.cards.firstOrNull { it.id == id } }

    LaunchedEffect(card?.id) {
        if (card == null) { if (!practice) vm.finishedSession() }
        else if (direction != Direction.SAY) vm.speaker.say(card.hanzi)
    }

    if (speaking && card != null) SpeakSheet(card, vm, onCalibrate, onDismiss = { speaking = false })

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
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
                            else {
                                val g = if (picked == card.english) Grade.GOOD else Grade.AGAIN
                                gradeCard(card, g)
                                queue.removeAt(0)
                                if (g == Grade.AGAIN) { queue.add(card.id); if (card.id !in missed) missed.add(card.id) } else done++
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.14f), contentColor = color),
                    ) { Text(option, style = MaterialTheme.typography.titleSmall) }
                }
                if (picked != null) Text(
                    if (picked == card.english) "Right! Tap any answer to continue." else "Not quite. Tap any answer to continue.",
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
                            gradeCard(card, g)
                            queue.removeAt(0)
                            if (g == Grade.AGAIN) { queue.add(card.id); if (card.id !in missed) missed.add(card.id) } else done++
                            shown = false
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
    }
}

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
private fun ToolCard(zh: String, title: String, blurb: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        Text(zh, fontSize = 22.sp, color = color)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
        Text(blurb, style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }
}

/** Accuracy for each tone lately, and a drill for the pair you mix up most. */
@Composable
private fun WeakTones(state: AppState, onDrill: (Int, Int) -> Unit) {
    val acc = com.dudlatitat.nextchapter.data.Stats.toneAccuracy(state)
    if (acc.values.all { it == null }) return
    val pair = com.dudlatitat.nextchapter.data.Stats.weakestPair(state)
    GlassCard {
        Text("Your tones · last 30 days", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..4).forEach { t ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    val a = acc[t]
                    Text(if (a == null) "–" else "${(a * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = com.dudlatitat.nextchapter.ui.components.toneColor(t))
                    Text("tone $t", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                }
            }
        }
        if (pair != null) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onDrill(pair.first, pair.second) }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Night.violet.copy(alpha = 0.2f), contentColor = Night.violet),
            ) { Text("Drill tones ${pair.first} & ${pair.second}: you mix these up most") }
        }
    }
}
