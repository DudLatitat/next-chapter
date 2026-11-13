package com.dudlatitat.nextchapter.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.desktop.DesktopServer
import com.dudlatitat.nextchapter.desktop.PageState
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Settings → Use on my computer → Phone remote. Each press is queued on the phone's server; the
 * computer page is always waiting for the next one (a "long-poll"), so it reacts at once.
 * It drives the page, not Windows itself: a web page can't move the computer's mouse.
 */
@Composable
fun RemoteScreen(server: DesktopServer?, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val haptic = LocalHapticFeedback.current
    val none = remember { MutableStateFlow<PageState?>(null) }
    val page by (server?.page ?: none).collectAsState()
    fun press(cmd: String, value: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        server?.send(cmd, value)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Phone remote", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        if (server == null) {
            GlassCard { Text("Turn on \"Available on my computer\" first.", color = Night.coral) }
        } else RemoteBody(server, page) { c, v -> press(c, v) }
    }
}

@Composable
private fun ColumnScope.RemoteBody(server: DesktopServer, page: PageState?, press: (String, Int) -> Unit) {
    run {
        val p = page
        // What's on the computer right now.
        GlassCard(highlight = true) {
            AnimatedContent(targetState = p, label = "screen") { s ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        s == null -> Text("Open the page on your computer, then press anything here.", color = Night.muted, textAlign = TextAlign.Center)
                        s.view != "review" -> Text("Phrases on the computer", color = Night.muted)
                        s.english.isEmpty() -> Text("All caught up · 都复习完了 ✓", color = Night.jade)
                        else -> {
                            Text("${s.left} to go", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                            Text(s.english, style = MaterialTheme.typography.titleLarge, color = Night.ink, textAlign = TextAlign.Center)
                            if (s.shown) {
                                Text(s.hanzi, fontSize = 34.sp, color = Night.sun)
                                Text(s.pinyin, color = Night.ink)
                            }
                        }
                    }
                }
            }
        }
        // Which page.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pad("Phrases", p?.view != "review", Night.sun) { press("tab", 0) }
            Pad("Review", p?.view == "review", Night.sun) { press("tab", 1) }
        }
        // Review buttons.
        if (p?.view == "review" && p.english.isNotEmpty()) {
            if (!p.shown) {
                Row { Pad("Show answer", true, Night.sun, tall = true) { press("show", 0) } }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pad("Again", true, Night.coral, tall = true) { press("grade", 1) }
                    Pad("Hard", true, Night.sun, tall = true) { press("grade", 2) }
                    Pad("Good", true, Night.jade, tall = true) { press("grade", 3) }
                    Pad("Easy", true, Night.violet, tall = true) { press("grade", 4) }
                }
                Row { Pad("🔊  Play again", false, Night.ink) { press("play", 0) } }
            }
        }
        // Swipe pad: drag to scroll the page, double-tap to jump to the top.
        Box(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp))
                .background(Night.card.copy(alpha = .7f))
                .border(1.dp, Night.outline, RoundedCornerShape(24.dp))
                .pointerInput(server) {
                    var pending = 0f
                    var lastSent = 0L
                    detectVerticalDragGestures(
                        onDragEnd = { if (pending != 0f) { server.send("scroll", pending.toInt()); pending = 0f } },
                    ) { change, dy ->
                        change.consume()
                        pending -= dy * 2.5f // finger up = page down, like a laptop touchpad
                        val now = System.currentTimeMillis()
                        if (now - lastSent > 90 && pending != 0f) { server.send("scroll", pending.toInt()); pending = 0f; lastSent = now }
                    }
                }
                .pointerInput(server) { detectTapGestures(onDoubleTap = { press("top", 0) }) },
            contentAlignment = Alignment.Center,
        ) {
            Text("Swipe to scroll\nDouble-tap for the top", color = Night.muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RowScope.Pad(label: String, on: Boolean, color: Color, tall: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier.weight(1f).height(if (tall) 72.dp else 52.dp).clip(shape)
            .background(if (on) color.copy(alpha = .22f) else Night.card.copy(alpha = .7f))
            .border(1.dp, if (on) color.copy(alpha = .7f) else Night.outline, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (on) color else Night.ink, fontWeight = FontWeight.SemiBold) }
}
