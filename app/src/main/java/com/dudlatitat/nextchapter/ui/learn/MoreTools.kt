package com.dudlatitat.nextchapter.ui.learn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Culture
import com.dudlatitat.nextchapter.data.Example
import com.dudlatitat.nextchapter.data.Grammar
import com.dudlatitat.nextchapter.data.GrammarPoint
import com.dudlatitat.nextchapter.data.Pinyin
import com.dudlatitat.nextchapter.data.Stories
import com.dudlatitat.nextchapter.data.Story
import com.dudlatitat.nextchapter.data.Vocab
import com.dudlatitat.nextchapter.drive.DriveService
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night

@Composable
private fun Header(title: String, sub: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
    }
}

@Composable
private fun ExampleRow(e: Example, vm: MainViewModel) {
    Row(Modifier.fillMaxWidth().clickable { vm.speaker.say(e.hanzi) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(e.hanzi, fontSize = 20.sp, color = Night.ink)
            PinyinText(e.pinyin, style = MaterialTheme.typography.bodyMedium)
            Text(e.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        Icon(Icons.Rounded.PlayArrow, "Listen", tint = Night.muted)
    }
}

// ---- Drive mode -----------------------------------------------------------------------

@Composable
fun DriveScreen(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val s by DriveService.state.collectAsState()
    var gap by rememberSaveable { mutableIntStateOf(4) }
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { DriveService.send(context, DriveService.ACTION_START, gap) }
    fun start() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
        else DriveService.send(context, DriveService.ACTION_START, gap)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Header("Drive mode · 开车", "Hands-free, works with the screen off", onBack)
        GlassCard(highlight = true) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!s.running) {
                    Text("🚗", fontSize = 56.sp)
                    Text("You hear the English, say it in Chinese, then hear the native answer twice.", style = MaterialTheme.typography.bodyMedium, color = Night.ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Uses today's reviews first, then recent phrases (up to 40, about 15 minutes). Music gets quieter while it talks. 10+ phrases count toward your streak.", style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center)
                } else {
                    Text("${s.index + 1} of ${s.total}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    val c = s.card
                    if (c != null) {
                        Text(c.english, style = MaterialTheme.typography.headlineSmall, color = Night.ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        if (s.phase == "chinese" || s.phase == "repeat") {
                            Text(c.hanzi, fontSize = 36.sp, color = Night.ink, textAlign = TextAlign.Center)
                            PinyinText(c.pinyin, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        } else {
                            Text(if (s.phase == "think") "Your turn: say it in Chinese…" else "Listen…", style = MaterialTheme.typography.titleMedium, color = Night.sun)
                        }
                    }
                    if (s.paused) Pill("Paused", Night.sun)
                }
            }
        }
        if (!s.running) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Time to answer:", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                listOf(3, 4, 6).forEach { n -> FilterChip(selected = gap == n, onClick = { gap = n }, label = { Text("${n}s") }) }
            }
            Button(onClick = { start() }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                Text("Start driving lesson", style = MaterialTheme.typography.titleMedium)
            }
            Text("Keep your eyes on the road; everything is controlled from the notification or your car's speakers.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { DriveService.send(context, DriveService.ACTION_PAUSE) }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (s.paused) "Resume" else "Pause", color = Night.sun) }
                OutlinedButton(onClick = { DriveService.send(context, DriveService.ACTION_NEXT) }, modifier = Modifier.weight(1f).height(64.dp)) { Text("Next", color = Night.sun) }
                OutlinedButton(onClick = { DriveService.send(context, DriveService.ACTION_STOP) }, modifier = Modifier.weight(1f).height(64.dp)) { Text("Stop", color = Night.coral) }
            }
        }
    }
}

// ---- Grammar --------------------------------------------------------------------------

@Composable
fun GrammarScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    Grammar.points.firstOrNull { it.id == open }?.let { GrammarDetail(it, state, vm, onBack = { open = null }); return }
    BackHandler(onBack = onBack)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Grammar · 语法", "${state.grammarPassed.size} of ${Grammar.points.size} quizzes passed", onBack) }
        items(Grammar.points, key = { it.id }) { g ->
            GlassCard(onClick = { open = g.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(g.title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        Text(g.pattern, style = MaterialTheme.typography.bodyMedium, color = Night.sun)
                    }
                    if (g.id in state.grammarPassed) Pill("✓", Night.jade)
                }
            }
        }
    }
}

@Composable
private fun GrammarDetail(g: GrammarPoint, state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var q by remember(g.id) { mutableIntStateOf(0) }
    var right by remember(g.id) { mutableIntStateOf(0) }
    var picked by remember(g.id, q) { mutableStateOf<String?>(null) }
    val blank = g.quiz.getOrNull(q)
    val options = remember(g.id, q) { blank?.options?.shuffled() ?: emptyList() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Header(g.title, g.pattern, onBack)
        GlassCard { Text(g.explain, style = MaterialTheme.typography.bodyLarge, color = Night.ink) }
        SectionLabel("Examples · tap to hear")
        GlassCard { g.examples.forEach { ExampleRow(it, vm) } }
        SectionLabel("Quick quiz")
        GlassCard(highlight = true) {
            if (blank == null) {
                val passed = right == g.quiz.size
                Text(if (passed) "Passed! ${right}/${g.quiz.size} 🎉" else "${right}/${g.quiz.size}: try again?", style = MaterialTheme.typography.titleLarge, color = if (passed) Night.jade else Night.sun)
                TextButton(onClick = { q = 0; right = 0 }) { Text("↻ Again", color = Night.sun) }
            } else {
                Text("${q + 1} of ${g.quiz.size} · fill the blank", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Text(blank.sentence.replace("___", if (picked == null) "＿＿" else blank.options[0]), fontSize = 26.sp, color = Night.ink)
                Text(blank.english, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { o ->
                        val color = when { picked == null -> Night.ink; o == blank.options[0] -> Night.jade; o == picked -> Night.coral; else -> Night.muted }
                        Button(
                            onClick = {
                                if (picked == null) {
                                    picked = o
                                    if (o == blank.options[0]) right++
                                    vm.speaker.say(blank.sentence.replace("___", blank.options[0]))
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.16f), contentColor = color),
                        ) { Text(o, fontSize = 22.sp) }
                    }
                }
                if (picked != null) TextButton(onClick = {
                    if (q + 1 >= g.quiz.size && right == g.quiz.size) vm.passGrammar(g.id)
                    q++
                }) { Text(if (q + 1 >= g.quiz.size) "See result →" else "Next →", color = Night.sun) }
            }
        }
    }
}

// ---- Stories --------------------------------------------------------------------------

/** Words the app knows, for tap-to-define: core words, every card, and each story's own list. */
private class Dictionary(state: AppState) {
    val words: Map<String, Example> = buildMap {
        Stories.all.flatMap { it.glossary }.forEach { put(it.hanzi, it) }
        Vocab.cards.forEach { put(it.hanzi.trimEnd('？', '?', '。'), Example(it.hanzi, it.pinyin, it.english)) }
        state.cards.filter { it.hanzi.length <= 4 }.forEach { putIfAbsent(it.hanzi.trimEnd('？', '?', '。'), Example(it.hanzi, it.pinyin, it.english)) }
    }

    /** Splits a sentence into known words (longest match first) and other characters. */
    fun split(text: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            var len = minOf(4, text.length - i)
            while (len > 1 && text.substring(i, i + len) !in words) len--
            out += text.substring(i, i + len)
            i += len
        }
        return out
    }
}

@Composable
fun StoriesScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    Stories.all.firstOrNull { it.id == open }?.let { StoryReader(it, state, vm, onBack = { open = null }); return }
    BackHandler(onBack = onBack)
    val dict = remember { Dictionary(state) }
    val knownWords = remember(state.cards) { state.cards.filter { !it.isNew }.map { it.hanzi }.toSet() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Stories · 故事", "Tap any word to hear it and see what it means", onBack) }
        items(Stories.all, key = { it.id }) { st ->
            val words = st.sentences.flatMap { dict.split(it.hanzi) }.filter { w -> w.any { it.code in 0x4E00..0x9FFF } }
            val pct = if (words.isEmpty()) 0 else words.count { it in knownWords }.times(100) / words.size
            GlassCard(onClick = { open = st.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(st.hanzi, fontSize = 22.sp, color = Night.rose, modifier = Modifier.widthIn(min = 92.dp))
                    Column(Modifier.weight(1f)) {
                        Text(st.title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        Text("Level ${st.level} · you've studied $pct% of its words", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    if (st.id in state.storiesRead) Pill("Read ✓", Night.jade)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StoryReader(st: Story, state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val dict = remember { Dictionary(state) }
    var showPinyin by rememberSaveable { mutableStateOf(false) }
    var showEnglish by rememberSaveable { mutableStateOf(false) }
    var word by remember { mutableStateOf<Pair<String, Example?>?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Header("${st.title} · ${st.hanzi}", "Level ${st.level}", onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = showPinyin, onClick = { showPinyin = !showPinyin }, label = { Text("Pinyin") })
            FilterChip(selected = showEnglish, onClick = { showEnglish = !showEnglish }, label = { Text("English") })
        }
        st.sentences.forEach { sent ->
            GlassCard {
                FlowRow(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    dict.split(sent.hanzi).forEach { w ->
                        val isWord = w.any { it.code in 0x4E00..0x9FFF }
                        Text(
                            w, fontSize = 24.sp, color = Night.ink,
                            modifier = if (isWord) Modifier.clip(RoundedCornerShape(6.dp)).clickable { word = w to dict.words[w]; vm.speaker.say(w) }.padding(horizontal = 1.dp) else Modifier,
                        )
                    }
                }
                if (showPinyin) PinyinText(sent.pinyin, style = MaterialTheme.typography.bodyMedium)
                if (showEnglish) Text(sent.english, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                TextButton(onClick = { vm.speaker.say(sent.hanzi) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Hear sentence", color = Night.sun) }
            }
        }
        Button(
            onClick = { vm.readStory(st.id); onBack() }, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
        ) { Text("I finished it ✓", style = MaterialTheme.typography.titleMedium) }
    }
    word?.let { (w, e) ->
        ModalBottomSheet(onDismissRequest = { word = null }, containerColor = Night.ground1) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w, fontSize = 48.sp, color = Night.ink)
                PinyinText(e?.pinyin ?: Pinyin.fromHanzi(w) ?: "", style = MaterialTheme.typography.headlineSmall)
                Text(e?.english ?: "Not in your decks yet", style = MaterialTheme.typography.titleMedium, color = Night.muted, textAlign = TextAlign.Center)
                TextButton(onClick = { vm.speaker.say(w, slow = true) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Hear it slowly", color = Night.sun) }
                if (e != null && state.cards.none { it.hanzi.trimEnd('？', '?', '。') == w }) TextButton(onClick = { vm.addCard(w, e.pinyin, e.english); word = null }) { Text("+ Add to My phrases", color = Night.jade) }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ---- Etiquette ------------------------------------------------------------------------

@Composable
fun CultureScreen(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var open by rememberSaveable { mutableStateOf<String?>(Culture.sections.first().id) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Etiquette · 礼节", "Meeting her family, eating, toasting, holidays", onBack) }
        items(Culture.sections, key = { it.id }) { sec ->
            val expanded = open == sec.id
            GlassCard(onClick = { open = if (expanded) null else sec.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sec.hanzi, fontSize = 22.sp, color = Night.sun, modifier = Modifier.widthIn(min = 64.dp))
                    Text(sec.title, style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.weight(1f))
                    Text(if (expanded) "–" else "+", style = MaterialTheme.typography.titleLarge, color = Night.muted)
                }
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    sec.tips.forEach { tip ->
                        Row(Modifier.padding(vertical = 3.dp)) {
                            Text("•  ", color = Night.sun)
                            Text(tip, style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    SectionLabel("Say it")
                    sec.phrases.forEach { ExampleRow(it, vm) }
                }
            }
        }
    }
}

