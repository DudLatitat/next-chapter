package com.dudlatitat.nextchapter.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Places
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.notify.PlaceWatch
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.life.clockLabel
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Settings → Work & home: notice arriving at work (the day starts 5 min later and ends 8 h after)
 * and getting home (the evening opens). Spots are saved on this phone only.
 */
@Composable
fun WorkHomeCard(state: AppState, vm: MainViewModel) {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) } // re-check permissions after a dialog
    var picking by remember { mutableStateOf<String?>(null) }
    var explainBackground by remember { mutableStateOf(false) }
    // Coming back from Android's settings pages: check permissions and battery again.
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    val fg = remember(tick) { PlaceWatch.hasForeground(context) }
    val bg = remember(tick) { PlaceWatch.hasBackground(context) }

    val askBackground = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++; vm.setPlacesOn(true) }
    val askLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        tick++
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            vm.setPlacesOn(true)
            if (!PlaceWatch.hasBackground(context)) explainBackground = true
        }
    }

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Know when I'm at work and home", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text(
                    "Arriving at work starts your day and expects it to end 8 hours later. Getting home opens the evening: \"You're home\" and tonight's job.",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
            }
            Switch(
                checked = state.placesOn && fg,
                onCheckedChange = { on ->
                    if (!on) vm.setPlacesOn(false)
                    else if (!fg) askLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    else { vm.setPlacesOn(true); if (!bg) explainBackground = true }
                },
                colors = SwitchDefaults.colors(checkedTrackColor = Night.sun, checkedThumbColor = MaterialTheme.colorScheme.onPrimary),
            )
        }
        if (state.placesOn && fg) {
            Spacer(Modifier.height(10.dp))
            listOf("work" to state.workSpot, "home" to state.homeSpot).forEach { (kind, spot) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(if (kind == "work") "💼 Work" else "🏠 Home", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                        Text(
                            when {
                                spot == null -> "Not set yet"
                                spot.label.isNotBlank() -> "${spot.label} · ${spot.radius} m"
                                else -> "Pinned on the map ✓ · ${spot.radius} m"
                            },
                            style = MaterialTheme.typography.bodySmall, color = if (spot != null) Night.jade else Night.muted, maxLines = 1,
                        )
                    }
                    OutlinedButton(onClick = { picking = kind }) { Text(if (spot == null) "Set" else "Edit", color = Night.sun) }
                    if (spot != null) TextButton(onClick = { vm.clearSpot(kind) }) { Text("Clear", color = Night.muted) }
                }
            }
            val nowMin = java.time.LocalTime.now().let { it.hour * 60 + it.minute }
            Places.workStart(state.arrivals, today())?.let { start ->
                Spacer(Modifier.height(6.dp))
                val home = Places.homeArrival(state.arrivals, today())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Today: work from ${clockLabel(start)}, ends ~${clockLabel(Places.workEnd(state.arrivals, today()) ?: start)}" +
                            (home?.let { " · home at ${clockLabel(it)}" } ?: if (Places.atWork(state, today(), nowMin)) " · at work now" else ""),
                        style = MaterialTheme.typography.bodySmall, color = Night.ink, modifier = Modifier.weight(1f),
                    )
                    // Wrong day (a test, a false arrival)? Forget today's times; earlier days stay.
                    TextButton(onClick = { vm.resetToday() }) { Text("Reset today", color = Night.coral) }
                }
            }
            if (!bg) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "To notice arrivals while the app is closed, location needs \"Allow all the time\".",
                    style = MaterialTheme.typography.bodySmall, color = Night.coral,
                )
                TextButton(onClick = { explainBackground = true }) { Text("Allow all the time", color = Night.sun) }
            }
            // Battery tip, only until the app is set to Unrestricted (Android: "ignoring battery optimizations").
            val unrestricted = remember(tick) {
                context.getSystemService(android.os.PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
            if (!unrestricted) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    "Set battery to Unrestricted so arrivals aren't delayed: App info → Battery → Unrestricted.",
                    style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    context.startActivity(
                        android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + context.packageName)),
                    )
                    tick++
                }) { Text("App info", color = Night.sun) }
            }
        }
    }

    picking?.let { kind ->
        SpotPicker(
            title = if (kind == "work") "Where's work?" else "Where's home?",
            initial = if (kind == "work") state.workSpot ?: state.homeSpot?.copy(label = "") else state.homeSpot ?: state.workSpot?.copy(label = ""),
            onSave = { vm.saveSpot(kind, it) },
            onClose = { picking = null },
        )
    }

    if (explainBackground) AlertDialog(
        onDismissRequest = { explainBackground = false },
        title = { Text("One more step") },
        text = {
            Text(
                "On the next screen, choose \"Allow all the time\". That lets the phone notice you arriving at work or home with the app closed. Location is only checked against your two saved spots and never leaves the phone.",
            )
        },
        confirmButton = {
            TextButton(onClick = {
                explainBackground = false
                if (Build.VERSION.SDK_INT >= 29) askBackground.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }) { Text("Continue", color = Night.sun) }
        },
        dismissButton = { TextButton(onClick = { explainBackground = false }) { Text("Not now", color = Night.muted) } },
    )
}
