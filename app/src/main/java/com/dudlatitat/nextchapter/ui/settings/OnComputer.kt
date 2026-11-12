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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.desktop.DesktopServer
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Settings → Use on my computer. While this screen is open, the phone serves the desktop page on
 * the home Wi-Fi; leaving it (or the app) stops the server. The screen stays on meanwhile.
 */
@Composable
fun OnComputerScreen(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val server = remember { DesktopServer(context.applicationContext, vm) }
    val view = LocalView.current
    DisposableEffect(Unit) {
        server.start()
        view.keepScreenOn = true
        onDispose { server.stop(); view.keepScreenOn = false }
    }
    val code by server.code.collectAsState()
    val paired by server.paired.collectAsState()
    val error by server.error.collectAsState()
    val addresses = remember { DesktopServer.localAddresses() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Use on my computer", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        GlassCard(highlight = true) {
            if (addresses.isEmpty()) {
                Text("Connect the phone to your home Wi-Fi first.", style = MaterialTheme.typography.titleMedium, color = Night.coral)
            } else {
                Text("1. On your computer (same Wi-Fi), open a browser and go to:", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                addresses.forEach { Text("http://$it:${server.port}", fontSize = 26.sp, color = Night.sun) }
                Spacer(Modifier.height(14.dp))
                Text("2. Type this code:", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
                Text(code.chunked(3).joinToString(" "), fontSize = 40.sp, color = Night.ink, letterSpacing = 4.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    if (paired > 0) "✓ Connected. Keep this screen open while you study on the computer." else "Waiting for your computer…",
                    style = MaterialTheme.typography.bodyMedium, color = if (paired > 0) Night.jade else Night.muted,
                )
            }
            error?.let { Text("Couldn't start: $it", style = MaterialTheme.typography.bodySmall, color = Night.coral) }
        }
        GlassCard {
            Text("How it works", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text(
                "Your phone is the only copy of your progress; the computer just shows it, so there's nothing to sync. " +
                    "It works only on your home network, only after the code, and only while this screen is open. " +
                    "Nothing goes through the internet. On the computer: Space shows the answer, 1–4 grades it, P plays it.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        }
    }
}
