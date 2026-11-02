package com.dudlatitat.nextchapter.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var micTest by remember { mutableStateOf(false) }
    if (micTest) { MicTestScreen(vm, onBack = { micTest = false }); return }
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var pickingTime by remember { mutableStateOf(false) }

    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.setReminder(granted, state.reminderMinutes)
        if (!granted) Toast.makeText(context, "Notifications are off for Next Chapter", Toast.LENGTH_SHORT).show()
    }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(vm.store.export().toByteArray()) } != null
        } catch (_: Exception) { false }
        Toast.makeText(context, if (ok) "Backup saved" else "Couldn't save the backup", Toast.LENGTH_SHORT).show()
    }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        pendingRestore = try { context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } } catch (_: Exception) { null }
        if (pendingRestore == null) Toast.makeText(context, "Couldn't read that file", Toast.LENGTH_SHORT).show()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Night.ink)
        }

        SectionLabel("Daily reminder")
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Remind me to practice", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("Only when phrases are waiting and you haven't studied yet", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                Switch(
                    checked = state.reminderOn,
                    onCheckedChange = { on ->
                        val needsAsk = on && Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        if (needsAsk) askNotify.launch(Manifest.permission.POST_NOTIFICATIONS) else vm.setReminder(on, state.reminderMinutes)
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = Night.sun, checkedThumbColor = MaterialTheme.colorScheme.onPrimary),
                )
            }
            if (state.reminderOn) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { pickingTime = true }) { Text("At ${timeLabel(state.reminderMinutes)}", color = Night.sun) }
            }
        }

        SectionLabel("Learning")
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("New phrases per day", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("More means faster progress but more reviews later", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                TextButton(onClick = { vm.updatePlan { it.copy(newPerDay = (it.newPerDay - 1).coerceAtLeast(1)) } }) { Text("−", style = MaterialTheme.typography.titleLarge) }
                Text("${state.newPerDay}", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                TextButton(onClick = { vm.updatePlan { it.copy(newPerDay = (it.newPerDay + 1).coerceAtMost(30)) } }) { Text("+", style = MaterialTheme.typography.titleLarge) }
            }
        }

        SectionLabel("Trip mode")
        GlassCard {
            Text("Survival screen on Today", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text("Her address, quick yuan and the lines you'll need most. Auto turns it on during a trip in your flight planner.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                listOf("auto" to "Auto", "on" to "On", "off" to "Off").forEach { (v, l) ->
                    androidx.compose.material3.FilterChip(selected = state.tripMode == v, onClick = { vm.setTripMode(v) }, label = { Text(l) })
                }
            }
        }

        SectionLabel("Voice")
        VoiceCard(vm)

        SectionLabel("Speaking check")
        GlassCard(onClick = { micTest = true }) {
            Text("Test microphone & recognition →", style = MaterialTheme.typography.titleMedium, color = Night.sun)
            Text("Room noise, hear your own recording, and test both ways the word check can listen", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Text("Using: ${if (vm.coach.mode == "live") "Google live (words only)" else "Offline on the phone (words, tones and pitch)"}", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        }

        SectionLabel("Backup")
        GlassCard {
            Text("Save everything to a file", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text("Your phrases, progress, dates and checklist. Keep it in Drive or Files, and restore it on a new phone.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { exportFile.launch("next-chapter-${LocalDate.now()}.json") }, modifier = Modifier.weight(1f)) { Text("Back up", color = Night.jade) }
                OutlinedButton(onClick = { importFile.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, modifier = Modifier.weight(1f)) { Text("Restore", color = Night.sun) }
            }
        }
        Spacer(Modifier.width(1.dp).height(24.dp))
    }

    if (pickingTime) {
        val picker = rememberTimePickerState(state.reminderMinutes / 60, state.reminderMinutes % 60, is24Hour = false)
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            title = { Text("Reminder time") },
            text = { TimePicker(picker) },
            confirmButton = { TextButton(onClick = { vm.setReminder(true, picker.hour * 60 + picker.minute); pickingTime = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { pickingTime = false }) { Text("Cancel") } },
        )
    }
    pendingRestore?.let { text ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore this backup?") },
            text = { Text("Everything in the app now gets replaced by the backup.") },
            confirmButton = {
                TextButton(onClick = {
                    val ok = vm.restore(text)
                    Toast.makeText(context, if (ok) "Restored" else "That isn't a Next Chapter backup", Toast.LENGTH_SHORT).show()
                    pendingRestore = null
                }) { Text("Restore", color = Night.coral) }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancel") } },
        )
    }
}

private fun timeLabel(minutes: Int): String =
    LocalTime.of(minutes / 60, minutes % 60).format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

@Composable
private fun VoiceCard(vm: MainViewModel) {
    val context = LocalContext.current
    val speaker = vm.speaker
    val ready by speaker.ready.collectAsState()
    val voices by speaker.voices.collectAsState()
    val engines by speaker.engines.collectAsState()
    var recorded by remember { mutableStateOf(speaker.useRecordings && speaker.hasRecordings) }
    var engine by remember { mutableStateOf(speaker.engine) }
    var voice by remember { mutableStateOf(speaker.voice) }

    var refused by remember { mutableStateOf(false) }

    GlassCard {
        Text("Voice for phrases", style = MaterialTheme.typography.titleMedium, color = Night.ink)
        Text("Tap one to hear it and use it", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        Spacer(Modifier.height(6.dp))
        if (speaker.hasRecordings) {
            VoiceRow("Natural · Xiaoxiao", "Recorded, most natural (built-in phrases; yours use the phone voice)", recorded) {
                recorded = true; refused = false; speaker.useNatural()
            }
        }
        voices.forEach { v ->
            VoiceRow(v.label, if (v.online) "Phone voice · needs internet" else "Phone voice", !recorded && voice == v.name) {
                recorded = false; voice = v.name; refused = !speaker.setVoice(v.name)
            }
        }
        if (refused) Text(
            "This engine ignored that voice. Switch the engine to Google below.",
            style = MaterialTheme.typography.bodySmall, color = Night.coral,
        )
        if (!ready) Text(
            "No Chinese phone voice found. Pick another engine, or install Chinese (Mandarin) in text-to-speech settings.",
            style = MaterialTheme.typography.bodySmall, color = Night.coral,
        )
        if (engines.size > 1) {
            Spacer(Modifier.height(10.dp))
            Text("Engine", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            engines.forEach { e ->
                val selected = engine == e.pkg || (engine == null && e == engines.first())
                Row(Modifier.fillMaxWidth().clickable { engine = e.pkg; voice = null; refused = false; speaker.setEngine(e.pkg) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = { engine = e.pkg; voice = null; refused = false; speaker.setEngine(e.pkg) })
                    Text(e.label, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
                }
            }
        }
        TextButton(onClick = {
            try { context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
        }) { Text("Open text-to-speech settings", color = Night.sun) }
        Text("Tip: Google's \"Speech Recognition & Synthesis\" app from the Play Store has the most natural Mandarin voices.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }
}

@Composable
private fun VoiceRow(title: String, sub: String, selected: Boolean, onPick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onPick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onPick)
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
    }
}
