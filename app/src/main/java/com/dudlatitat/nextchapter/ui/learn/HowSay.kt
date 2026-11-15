package com.dudlatitat.nextchapter.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Finder
import com.dudlatitat.nextchapter.lookup.Lookup
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.delay

/**
 * 🔍 How do I say…: type English (or pinyin), see whole phrases the app knows and dictionary words,
 * each with its pinyin, what to press on the pinyin keyboard, and buttons to use or save it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowSaySheet(state: AppState, vm: MainViewModel, onDismiss: () -> Unit, onUse: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    var q by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Lookup.Result?>(null) }
    var saved by remember { mutableStateOf(setOf<String>()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    // Search as you type, a moment after you stop.
    LaunchedEffect(q) { if (q.isBlank()) { result = null; return@LaunchedEffect }; delay(250); result = Lookup.search(context, state, q) }

    @Composable
    fun ResultRow(hanzi: String, pinyin: String, english: String) {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Night.cardHigh, RoundedCornerShape(14.dp)).padding(12.dp)) {
            Text(hanzi, fontSize = 24.sp, color = Night.ink)
            PinyinText(pinyin, style = MaterialTheme.typography.bodyMedium)
            Text("⌨️ type: ${Finder.typeKeys(pinyin)}", style = MaterialTheme.typography.labelMedium, color = Night.sun)
            Text(english, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 2)
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { vm.speaker.say(hanzi) }) { Text("🔊", color = Night.ink) }
                if (onUse != null) TextButton(onClick = { onUse(hanzi); onDismiss() }) { Text("Use it", color = Night.jade) }
                val have = hanzi in saved || state.cards.any { it.hanzi == hanzi }
                TextButton(onClick = { if (!have) { vm.addCard(hanzi, pinyin, english.substringBefore(";")); saved = saved + hanzi } }, enabled = !have) {
                    Text(if (have) "✓ in my phrases" else "＋ Save", color = if (have) Night.muted else Night.sun)
                }
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Night.ground1) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 32.dp)) {
            Text("🔍 How do I say…", style = MaterialTheme.typography.titleLarge, color = Night.ink)
            Text("Type it in English (or pinyin). Works offline.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(q, { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(focus), placeholder = { Text("e.g. are you home yet") })
            val r = result
            if (r != null) {
                if (r.phrases.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Phrases · checked sentences", style = MaterialTheme.typography.labelLarge, color = Night.muted)
                    r.phrases.forEach { ResultRow(it.hanzi, it.pinyin, it.english) }
                }
                if (r.words.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Words · dictionary", style = MaterialTheme.typography.labelLarge, color = Night.muted)
                    r.words.forEach { ResultRow(it.hanzi, it.pinyin, it.meanings.take(3).joinToString("; ")) }
                }
                if (r.phrases.isEmpty() && r.words.isEmpty()) Text(
                    "Nothing found. Try fewer words, or one key word (\"tired\", \"tomorrow\").",
                    style = MaterialTheme.typography.bodyMedium, color = Night.muted, modifier = Modifier.padding(top = 12.dp),
                )
                if (!r.hasDict) Text("(The word dictionary isn't in this build; phrases still work.)", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
            Spacer(Modifier.height(10.dp))
            Text("Words: CC-CEDICT (mdbg.net), CC BY-SA 4.0", style = MaterialTheme.typography.labelSmall, color = Night.muted.copy(alpha = 0.6f))
        }
    }
}
