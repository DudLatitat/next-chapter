package com.dudlatitat.nextchapter.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.Dialogue
import com.dudlatitat.nextchapter.data.Dialogues
import com.dudlatitat.nextchapter.data.Line
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night

@Composable
fun DialoguesScreen(vm: MainViewModel, onBack: () -> Unit, onCalibrate: () -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val scene = Dialogues.all.firstOrNull { it.id == open }
    if (scene != null) { Scene(scene, vm, onBack = { open = null }, onCalibrate = onCalibrate); return }
    BackHandler(onBack = onBack)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column {
                    Text("Dialogues · 对话", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text("Listen, then answer out loud", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
        }
        items(Dialogues.all, key = { it.id }) { d ->
            GlassCard(onClick = { open = d.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.hanzi, fontSize = 22.sp, color = Night.jade, modifier = Modifier.widthIn(min = 92.dp))
                    Column(Modifier.weight(1f)) {
                        Text(d.title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        Text(d.setting, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun Scene(d: Dialogue, vm: MainViewModel, onBack: () -> Unit, onCalibrate: () -> Unit) {
    BackHandler(onBack = onBack)
    var at by remember(d.id) { mutableIntStateOf(0) }
    var showText by remember { mutableStateOf(true) }
    val list = rememberLazyListState()
    val current = d.lines.getOrNull(at)

    LaunchedEffect(at) {
        current?.let { if (!it.you) vm.speaker.say(it.hanzi) }
        if (at > 0) list.animateScrollToItem(maxOf(0, at))
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text(d.title, style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text(d.setting, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            TextButton(onClick = { showText = !showText }) { Text(if (showText) "Hide text" else "Show text", color = Night.muted) }
        }
        LazyColumn(Modifier.weight(1f), state = list, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
            items(d.lines.take(at + 1).withIndex().toList()) { (i, line) ->
                if (line.you && i == at) YourTurn(line, vm, onCalibrate)
                else Bubble(line, showText || line.you, vm)
            }
            if (current == null) item {
                GlassCard(highlight = true) {
                    Text("Scene complete 🎉", style = MaterialTheme.typography.titleLarge, color = Night.sun)
                    Text("Run it again with the text hidden to test your ear.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                    TextButton(onClick = { at = 0; showText = false }) { Text("↻  Again, listening only", color = Night.sun) }
                }
            }
        }
        if (current != null) Button(
            onClick = { at++ }, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
        ) { Text(if (current.you) "Next" else "Your turn →".takeIf { d.lines.getOrNull(at + 1)?.you == true } ?: "Next", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun Bubble(line: Line, showText: Boolean, vm: MainViewModel) {
    val shape = RoundedCornerShape(18.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (line.you) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 320.dp).clip(shape)
                .background(if (line.you) Night.sun.copy(alpha = 0.16f) else Night.card)
                .clickable { vm.speaker.say(line.hanzi) }
                .padding(12.dp),
        ) {
            if (showText) {
                Text(line.hanzi, fontSize = 20.sp, color = Night.ink)
                PinyinText(line.pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(line.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Night.muted)
                    Text(" Tap to hear again", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                }
            }
        }
    }
}

@Composable
private fun YourTurn(line: Line, vm: MainViewModel, onCalibrate: () -> Unit) {
    var hint by remember(line) { mutableStateOf(false) }
    GlassCard(highlight = true) {
        Text("Your turn: say", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Text("“${line.english}”", style = MaterialTheme.typography.titleLarge, color = Night.ink)
        Spacer(Modifier.height(6.dp))
        if (hint) {
            Text(line.hanzi, fontSize = 22.sp, color = Night.ink)
            PinyinText(line.pinyin, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { vm.speaker.say(line.hanzi) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Hear it", color = Night.sun) }
        } else {
            TextButton(onClick = { hint = true }) { Text("Show me how", color = Night.sun) }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SpeakPanel(line.hanzi, line.pinyin, vm, onCalibrate)
        }
    }
}
