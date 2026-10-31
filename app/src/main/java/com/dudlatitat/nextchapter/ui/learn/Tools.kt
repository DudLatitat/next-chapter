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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Phrasebook
import com.dudlatitat.nextchapter.data.Pinyin
import com.dudlatitat.nextchapter.data.Survival
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.components.toneColor
import com.dudlatitat.nextchapter.ui.theme.Night

private val toneNames = listOf("1  ˉ  high", "2  ˊ  rising", "3  ˇ  dip", "4  ˋ  falling")

/** Hear a character, pick its tone. */
@Composable
fun ToneTrainer(vm: MainViewModel, onBack: () -> Unit, onCalibrate: () -> Unit, focus: Set<Int>? = null) {
    BackHandler(onBack = onBack)
    var sayMode by rememberSaveable { mutableStateOf(false) }
    if (sayMode) { SayTones(vm, onBack, onCalibrate, onHear = { sayMode = false }); return }
    var round by remember { mutableIntStateOf(0) }
    var right by remember { mutableIntStateOf(0) }
    var tried by remember { mutableIntStateOf(0) }
    val pool = remember(focus) { Pinyin.toneQuiz.filter { focus == null || Pinyin.tone(it.second) in focus } }
    val item = remember(round) { pool.random() }
    val tone = Pinyin.tone(item.second)
    var picked by remember(round) { mutableStateOf<Int?>(null) }

    LaunchedEffect(round) { vm.speaker.say(item.first, slow = true) }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text(if (focus != null) "Drill: tones ${focus.sorted().joinToString(" & ")}" else "Tone trainer · 声调", style = MaterialTheme.typography.headlineSmall, color = Night.ink, modifier = Modifier.weight(1f))
            if (tried > 0) Pill("$right/$tried", Night.jade)
        }
        if (focus == null) ModeRow(say = false, onHear = {}, onSay = { sayMode = true })
        GlassCard(Modifier.weight(1f), highlight = true) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { vm.speaker.say(item.first, slow = true) }, modifier = Modifier.height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Night.sun.copy(alpha = 0.2f), contentColor = Night.sun),
                ) { Icon(Icons.Rounded.PlayArrow, null); Text("  Hear it", style = MaterialTheme.typography.titleLarge) }
                Spacer(Modifier.height(24.dp))
                if (picked != null) {
                    Text(item.first, fontSize = 64.sp, color = Night.ink)
                    PinyinText(item.second, style = MaterialTheme.typography.headlineMedium)
                    Text(item.third, style = MaterialTheme.typography.titleMedium, color = Night.muted)
                } else {
                    Text("Which tone?", style = MaterialTheme.typography.titleMedium, color = Night.muted)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..4).forEach { t ->
                val color = when {
                    picked == null -> toneColor(t)
                    t == tone -> Night.jade
                    t == picked -> Night.coral
                    else -> Night.muted
                }
                Button(
                    onClick = {
                        if (picked == null) {
                            picked = t; tried++; if (t == tone) right++
                            vm.logTone(tone, t, spoken = false)
                        } else round++
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.16f), contentColor = color),
                ) { Text(toneNames[t - 1], style = MaterialTheme.typography.titleMedium) }
            }
            Text(
                if (picked == null) "Tip: the 3rd tone dips low, the 4th drops sharply." else "Tap any button for the next one.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/** Big-text lines to hold up for a driver, a shop, a pharmacy. */
@Composable
fun PhrasebookScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var showing by remember { mutableStateOf<Survival?>(null) }
    var editing by remember { mutableStateOf<String?>(null) }
    BackHandler { if (showing != null) showing = null else onBack() }

    val personal = buildList {
        if (state.addressZh.isNotBlank()) add(Survival(state.addressZh, Pinyin.fromHanzi(state.addressZh) ?: "", "My address"))
        if (state.allergyZh.isNotBlank()) add(Survival("我对${state.allergyZh}过敏", Pinyin.fromHanzi("我对${state.allergyZh}过敏") ?: "", "I'm allergic to this"))
    }

    showing?.let { line ->
        // Full screen, huge text, readable from across a counter.
        Column(
            Modifier.fillMaxSize().background(Night.ground2).clickable { showing = null }.padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(line.hanzi, fontSize = 56.sp, lineHeight = 68.sp, color = Night.ink, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            PinyinText(line.pinyin, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(line.english, style = MaterialTheme.typography.titleMedium, color = Night.muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { vm.speaker.say(line.hanzi) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Say it out loud", color = Night.sun) }
            Text("Tap anywhere to close", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column {
                    Text("Phrasebook · 救急", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text("Tap a line to show it full screen", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
        }
        item { SectionLabel("Mine") }
        item {
            GlassCard {
                PersonalRow("My address there", state.addressZh, "Add it in Chinese") { editing = "address" }
                Spacer(Modifier.height(8.dp))
                PersonalRow("Allergy", state.allergyZh, "e.g. 花生 (peanuts)") { editing = "allergy" }
            }
        }
        items(personal) { line -> LineCard(line) { showing = line } }
        item { SectionLabel("Essentials") }
        items(Phrasebook.lines) { line -> LineCard(line) { showing = line } }
    }

    editing?.let { field ->
        var text by remember(field) { mutableStateOf(if (field == "address") state.addressZh else state.allergyZh) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (field == "address") "Address in Chinese" else "Allergic to… (in Chinese)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (field == "address") Text("Ask Yuling to send it to you on WeChat and paste it here.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    OutlinedTextField(text, { text = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updatePlan { if (field == "address") it.copy(addressZh = text.trim()) else it.copy(allergyZh = text.trim()) }
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun PersonalRow(label: String, value: String, hint: String, onEdit: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onEdit), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Text(value.ifBlank { hint }, style = MaterialTheme.typography.bodyLarge, color = if (value.isBlank()) Night.muted else Night.ink)
        }
        Icon(Icons.Rounded.Edit, "Edit", tint = Night.muted)
    }
}

@Composable
private fun LineCard(line: Survival, onClick: () -> Unit) {
    GlassCard(onClick = onClick) {
        Box {
            Column {
                Text(line.hanzi, fontSize = 24.sp, color = Night.ink)
                if (line.pinyin.isNotBlank()) PinyinText(line.pinyin, style = MaterialTheme.typography.bodyMedium)
                Text(line.english, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            }
        }
    }
}

@Composable
private fun ModeRow(say: Boolean, onHear: () -> Unit, onSay: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Hear 听" to !say, "Say 说" to say).forEachIndexed { i, (label, selected) ->
            FilterChip(
                selected = selected, onClick = if (i == 0) onHear else onSay, label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Night.sun.copy(alpha = 0.18f), selectedLabelColor = Night.sun,
                    labelColor = Night.muted, containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
        }
    }
}

/** See a character, say it, and find out which tone you actually made. */
@Composable
private fun SayTones(vm: MainViewModel, onBack: () -> Unit, onCalibrate: () -> Unit, onHear: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val item = remember(round) { Pinyin.toneQuiz.random() }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Tone trainer · 声调", style = MaterialTheme.typography.headlineSmall, color = Night.ink, modifier = Modifier.weight(1f))
        }
        ModeRow(say = true, onHear = onHear, onSay = {})
        if (!vm.coach.calibrated) {
            GlassCard(onClick = onCalibrate) {
                Text("First: set up your voice (10 seconds)", style = MaterialTheme.typography.titleMedium, color = Night.violet)
                Text("Say 妈 麻 马 骂 once so the app learns your pitch range. Tone checks are much more accurate after.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        GlassCard(highlight = true) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.first, fontSize = 72.sp, color = Night.ink)
                PinyinText(item.second, style = MaterialTheme.typography.headlineMedium)
                Text(item.third, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                TextButton(onClick = { vm.speaker.say(item.first, slow = true) }) { Icon(Icons.Rounded.PlayArrow, null, tint = Night.sun); Text(" Hear it", color = Night.sun) }
                SpeakPanel(item.first, item.second, vm, onCalibrate)
            }
        }
        Button(
            onClick = { round++ }, modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Night.sun.copy(alpha = 0.18f), contentColor = Night.sun),
        ) { Text("Next character", style = MaterialTheme.typography.titleMedium) }
    }
}
