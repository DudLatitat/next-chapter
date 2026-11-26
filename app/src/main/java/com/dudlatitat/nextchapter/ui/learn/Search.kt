package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import com.dudlatitat.nextchapter.ui.components.frost
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Decks
import com.dudlatitat.nextchapter.data.Finder
import com.dudlatitat.nextchapter.lookup.Lookup
import com.dudlatitat.nextchapter.ui.components.HanziText
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.pressClick
import com.dudlatitat.nextchapter.ui.components.screenGutter
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.delay

/**
 * 🔍 Search everything: your phrases, the lines from Talk scenes and Texts chats, and the offline
 * dictionary. Type English, characters, or pinyin (tone marks and spaces optional). Tap a result
 * to open it: how to type it, save it, or jump to its deck.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit, onDeck: (String) -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var q by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<Lookup.Result?>(null) }
    var doneFor by remember { mutableStateOf("") }
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    LaunchedEffect(q) {
        if (q.isBlank()) { result = null; return@LaunchedEffect }
        delay(200)
        result = Lookup.searchAll(context, state, q)
        doneFor = q
        open = null
    }
    val byHanzi = remember(state.cards) { state.cards.associateBy { it.hanzi } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = screenGutter(), vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "bar") {
            Row(Modifier.padding(top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Row(
                    Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(26.dp)).background(Night.cardHigh)
                        .border(1.dp, Night.sun.copy(alpha = 0.35f), RoundedCornerShape(26.dp)).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Search, null, tint = Night.sun)
                    Spacer(Modifier.width(10.dp))
                    androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
                        if (q.isEmpty()) Text("English, 汉字 or pinyin", style = MaterialTheme.typography.bodyLarge, color = Night.muted)
                        BasicTextField(
                            q, { q = it }, singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Night.ink),
                            cursorBrush = SolidColor(Night.sun),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier.fillMaxWidth().focusRequester(focus),
                        )
                    }
                    if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Rounded.Close, "Clear", tint = Night.muted) }
                }
            }
        }
        val r = result
        if (q.isBlank()) {
            item(key = "hint") {
                Column(Modifier.padding(top = 24.dp, start = 8.dp, end = 8.dp)) {
                    Text("Search everything you're learning", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text(
                        "Your ${state.cards.size} phrases, every line from Talk and Texts, and an offline dictionary. Tone marks optional: \"nihao\" finds 你好.",
                        style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Try", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Spacer(Modifier.height(6.dp))
                    // An even 2-column grid of same-size tiles (a wrapping row looked ragged).
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("are you home", "tired", "how much", "nihao", "吃", "mom", "miss you", "thank you").chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { s ->
                                    androidx.compose.foundation.layout.Box(
                                        Modifier.weight(1f).heightIn(min = 48.dp).frost(RoundedCornerShape(14.dp)).pressClick { q = s },
                                        contentAlignment = Alignment.Center,
                                    ) { Text(s, style = MaterialTheme.typography.bodyMedium, color = Night.sun, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            return@LazyColumn
        }
        // Typing: a quiet "Searching…" instead of a blank flash.
        if (r == null || doneFor != q) {
            item(key = "busy") { Text("Searching…", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.muted, modifier = Modifier.padding(16.dp)) }
            return@LazyColumn
        }
        val mine = r.phrases.filter { it.hanzi in byHanzi }
        val lines = r.phrases.filter { it.hanzi !in byHanzi }
        fun section(key: String, title: String, n: Int) = item(key = "h:$key") {
            Text("$title · $n", style = MaterialTheme.typography.labelLarge, color = Night.muted, modifier = Modifier.padding(top = 10.dp, start = 4.dp).animateItem())
        }
        if (mine.isNotEmpty()) {
            section("mine", "Your phrases", mine.size)
            items(mine, key = { "m:" + it.hanzi }) { h ->
                val card = byHanzi.getValue(h.hanzi)
                val deck = Decks.firstOrNull { it.id == card.deck }
                val status = when { card.isKnown -> "Known" to Night.jade; !card.isNew -> "Learning" to Night.violet; else -> "New" to Night.rose }
                ResultRow(h.hanzi, h.pinyin, h.english, open == "m:" + h.hanzi, vm, Modifier.animateItem(),
                    tag = { Pill(status.first, status.second); deck?.let { Pill(it.hanzi + " " + it.title, Night.sun) } },
                    onToggle = { open = if (open == "m:" + h.hanzi) null else "m:" + h.hanzi },
                    actions = { deck?.let { d -> TextButton(onClick = { onDeck(d.id) }) { Text("Open deck ›", color = Night.sun) } } },
                )
            }
        }
        if (lines.isNotEmpty()) {
            section("lines", "In scenes & chats", lines.size)
            items(lines, key = { "l:" + it.hanzi }) { h ->
                ResultRow(h.hanzi, h.pinyin, h.english, open == "l:" + h.hanzi, vm, Modifier.animateItem(),
                    tag = { Pill(h.from, Night.violet) },
                    onToggle = { open = if (open == "l:" + h.hanzi) null else "l:" + h.hanzi },
                    actions = { SaveButton(state, vm, h.hanzi, h.pinyin, h.english) },
                )
            }
        }
        if (r.words.isNotEmpty()) {
            section("words", "Dictionary", r.words.size)
            items(r.words, key = { "w:" + it.hanzi + it.pinyin }) { w ->
                val english = w.meanings.take(3).joinToString("; ")
                ResultRow(w.hanzi, w.pinyin, english, open == "w:" + w.hanzi + w.pinyin, vm, Modifier.animateItem(),
                    tag = {},
                    onToggle = { open = if (open == "w:" + w.hanzi + w.pinyin) null else "w:" + w.hanzi + w.pinyin },
                    actions = { SaveButton(state, vm, w.hanzi, w.pinyin, w.meanings.first()) },
                )
            }
        }
        if (r.phrases.isEmpty() && r.words.isEmpty()) item(key = "none") {
            com.dudlatitat.nextchapter.ui.components.EmptyState(
                "查", Night.sun, "Nothing for \"$q\" yet. Try one key word, or pinyin without tones.",
                button = "Clear search", onClick = { q = "" },
            )
        }
        if (!r.hasDict) item(key = "nodict") { Text("(The word dictionary isn't in this build; phrases still work.)", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
        item(key = "credit") {
            Text("Words: CC-CEDICT (mdbg.net), CC BY-SA 4.0", style = MaterialTheme.typography.labelSmall, color = Night.muted.copy(alpha = 0.6f), modifier = Modifier.padding(top = 12.dp, start = 4.dp))
        }
    }
}

/** One result: characters, pinyin, meaning and a play button; tap to open how to type it and actions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultRow(
    hanzi: String, pinyin: String, english: String, expanded: Boolean, vm: MainViewModel, modifier: Modifier,
    tag: @Composable () -> Unit, onToggle: () -> Unit, actions: @Composable () -> Unit,
) {
    Column(
        modifier.fillMaxWidth().pressClick(haptic = false, onClick = onToggle).clip(RoundedCornerShape(18.dp))
            .background(if (expanded) Night.cardHigh else Night.card.copy(alpha = 0.85f)).padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                HanziText(hanzi, 24f, align = null)
                PinyinText(pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(english, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = if (expanded) 6 else 2)
            }
            IconButton(onClick = { vm.speaker.say(hanzi) }) { Icon(Icons.Rounded.PlayArrow, "Listen", tint = Night.ink) }
        }
        FlowRow(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { tag() }
        AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.padding(top = 8.dp)) {
                Text("⌨️ Type: ${Finder.typeKeys(pinyin)}", style = MaterialTheme.typography.labelLarge, color = Night.sun)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { vm.speaker.say(hanzi, slow = true) }) { Text("Slow 🐢", color = Night.muted) }
                    actions()
                }
            }
        }
    }
}

@Composable
private fun SaveButton(state: AppState, vm: MainViewModel, hanzi: String, pinyin: String, english: String) {
    var saved by remember { mutableStateOf(false) }
    val have = saved || state.cards.any { it.hanzi == hanzi }
    TextButton(onClick = { if (!have) { vm.addCard(hanzi, pinyin, english.substringBefore(";")); saved = true } }, enabled = !have) {
        Text(if (have) "✓ In my phrases" else "＋ Save to my phrases", color = if (have) Night.muted else Night.sun)
    }
}

/** The search pill at the top of the Chinese tab (and anywhere else that wants one). */
@Composable
fun SearchPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 48.dp).pressClick(onClick = onClick).clip(RoundedCornerShape(24.dp))
            .background(Night.card.copy(alpha = 0.9f)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = Night.sun)
        Spacer(Modifier.width(10.dp))
        Text("Search phrases, words, pinyin…", style = MaterialTheme.typography.bodyLarge, color = Night.muted)
    }
}
