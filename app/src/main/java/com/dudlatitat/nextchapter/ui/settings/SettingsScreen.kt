package com.dudlatitat.nextchapter.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
            Spacer(Modifier.height(12.dp))
            Text("Chinese voice", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text(
                if (vm.speaker.available) "Ready ✓" else "Not found. Install Chinese (Mandarin) in your phone's text-to-speech settings.",
                style = MaterialTheme.typography.bodySmall, color = if (vm.speaker.available) Night.jade else Night.coral,
            )
            TextButton(onClick = {
                try { context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
            }) { Text("Open text-to-speech settings", color = Night.sun) }
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
