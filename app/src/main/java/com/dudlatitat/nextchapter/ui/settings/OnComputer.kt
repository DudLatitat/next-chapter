package com.dudlatitat.nextchapter.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.desktop.Desktop
import com.dudlatitat.nextchapter.desktop.DesktopServer
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Settings → Use on my computer: one switch. While it's on, the phone keeps the computer page
 * available in the background (DesktopService, with its notification); a computer pairs once.
 */
@Composable
fun OnComputerScreen(vm: com.dudlatitat.nextchapter.MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var on by remember { mutableStateOf(Desktop.enabled(context)) }
    var tick by remember { mutableIntStateOf(0) }
    val server by Desktop.server.collectAsState()
    val idle = remember { MutableStateFlow("") }
    val code by (server?.code ?: idle).collectAsState()
    val pairedLive by (server?.paired ?: MutableStateFlow(-1)).collectAsState()
    val paired = if (pairedLive >= 0) pairedLive else remember(tick) { Desktop.pairedCount(context) }
    val error by (server?.error ?: MutableStateFlow<String?>(null)).collectAsState()
    val addresses = remember(server) { DesktopServer.localAddresses() }
    var remote by remember { mutableStateOf(false) }
    // A computer can pair only while this screen is open (so nobody else on the Wi-Fi can try codes).
    androidx.compose.runtime.DisposableEffect(server, remote) {
        if (!remote) server?.openPairing()
        onDispose { server?.closePairing() }
    }
    if (remote) { RemoteScreen(server, vm) { remote = false }; return }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Use on my computer", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        GlassCard(highlight = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Available on my computer", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("Keeps working while you use other apps or the screen is off. A small notification shows while it's on.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                Switch(
                    checked = on,
                    onCheckedChange = { v -> on = v; if (v) Desktop.start(context) else Desktop.stop(context) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Night.sun, checkedThumbColor = MaterialTheme.colorScheme.onPrimary),
                )
            }
            if (on) {
                Spacer(Modifier.height(12.dp))
                if (addresses.isEmpty()) {
                    Text("Connect the phone to your home Wi-Fi.", style = MaterialTheme.typography.titleMedium, color = Night.coral)
                } else {
                    Text("On your computer, open (and bookmark):", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                    addresses.forEach { Text("http://$it:8090", fontSize = 24.sp, color = Night.sun) }
                    Spacer(Modifier.height(10.dp))
                    if (code.isNotEmpty()) {
                        Text(if (paired > 0) "To pair another computer, type:" else "First time? Type this code:", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                        Text(code.chunked(3).joinToString(" "), fontSize = 34.sp, color = Night.ink, letterSpacing = 4.sp)
                    }
                }
                error?.let { Text("Couldn't start: $it", style = MaterialTheme.typography.bodySmall, color = Night.coral) }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (paired) { 0 -> "No computers paired yet"; 1 -> "1 computer paired ✓"; else -> "$paired computers paired ✓" },
                    style = MaterialTheme.typography.bodyMedium, color = if (paired > 0) Night.jade else Night.muted, modifier = Modifier.weight(1f),
                )
                if (paired > 0) TextButton(onClick = { Desktop.forgetAll(context); tick++ }) { Text("Forget my computers", color = Night.coral) }
            }
        }
        if (on && server != null) GlassCard(onClick = { remote = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🎮 Phone remote", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("Review from the couch: show, grade and play from your phone, and swipe to scroll the page.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                Text("Open ›", color = Night.sun)
            }
        }
        GlassCard {
            Text("Keep the same address", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text(
                "Your router can hand the phone a new address now and then, which breaks the bookmark. " +
                    "Fix it once with a DHCP reservation: in your router's settings, find this phone (by its name or the address above) and reserve that address for it.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
        GlassCard {
            Text("How it works", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text(
                "Your phone is the only copy of your progress; the computer just shows it, so there's nothing to sync. " +
                    "It answers only your home network and only paired computers, and nothing goes through the internet. " +
                    "It uses a little battery while it's on: switch it off (or tap Stop on the notification) when you're done.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
    }
}
