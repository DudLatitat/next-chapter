package com.dudlatitat.nextchapter.ui.today

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.friendlySpan
import com.dudlatitat.nextchapter.data.moveReady
import com.dudlatitat.nextchapter.data.phraseOfTheDay
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.ProgressRing
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalTime

@Composable
fun TodayScreen(state: AppState, vm: MainViewModel, onStudy: () -> Unit, onOpenMove: () -> Unit, onSettings: () -> Unit) {
    val queue = Srs.queue(state)
    val streak = Srs.streak(state.studyDays)
    val studiedToday = com.dudlatitat.nextchapter.data.today() in state.studyDays
    val hour = LocalTime.now().hour
    val (hello, helloZh) = when {
        hour < 5 -> "Up late" to "夜深了"
        hour < 12 -> "Good morning" to "早上好"
        hour < 18 -> "Good afternoon" to "下午好"
        else -> "Good evening" to "晚上好"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(helloZh, style = MaterialTheme.typography.titleMedium, color = Night.sun)
                Text("$hello, Rudy", style = MaterialTheme.typography.headlineLarge, color = Night.ink)
            }
            if (streak > 0) Pill("🔥 $streak", Night.sun)
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = Night.muted) }
        }

        // Countdowns
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Countdown("Next trip", "成都", daysUntil(state.tripDate), Night.rose, Modifier.weight(1f), onOpenMove)
            Countdown("The move", "搬家", daysUntil(state.moveDate), Night.sun, Modifier.weight(1f), onOpenMove)
        }

        // Today's practice
        GlassCard(highlight = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Today's practice", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            queue.isNotEmpty() -> "${queue.size} phrases · about ${maxOf(1, queue.size / 3)} min"
                            studiedToday -> "All done for today. 太棒了!"
                            else -> "Nothing due. Add your own phrases in Learn."
                        },
                        style = MaterialTheme.typography.bodyMedium, color = Night.muted,
                    )
                }
                ProgressRing(if (queue.isEmpty()) 1f else 0f, 56.dp, Night.jade, stroke = 6.dp) {
                    Text(if (queue.isEmpty()) "✓" else "${queue.size}", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                }
            }
            if (queue.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onStudy,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
                ) { Text("Start", style = MaterialTheme.typography.titleMedium) }
            }
        }

        // Phrase of the day
        state.phraseOfTheDay()?.let { card ->
            SectionLabel("Phrase of the day")
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(card.hanzi, fontSize = 30.sp, color = Night.ink, fontWeight = FontWeight.Medium)
                        PinyinText(card.pinyin, style = MaterialTheme.typography.titleMedium)
                        Text(card.english, style = MaterialTheme.typography.bodyLarge, color = Night.muted)
                    }
                    IconButton(onClick = { vm.speaker.say(card.hanzi) }) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Listen", tint = Night.ink)
                    }
                }
            }
        }

        // Next move step
        SectionLabel("Toward the move")
        GlassCard(onClick = onOpenMove) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(state.moveReady(), 64.dp, Night.sun, stroke = 7.dp) {
                    Text("${(state.moveReady() * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    val next = state.tasks.firstOrNull { !it.done }
                    Text("Move-ready", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Text(next?.title ?: "Every step checked off!", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    if (next != null) Text("Next step · ${next.group}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Countdown(label: String, zh: String, days: Long?, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        Text("$label · $zh", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Spacer(Modifier.height(6.dp))
        if (days == null) {
            Text("Set date", style = MaterialTheme.typography.headlineSmall, color = color)
        } else if (days in 0..99) {
            Text("$days", style = MaterialTheme.typography.displaySmall, color = color)
            Text(if (days == 1L) "day" else "days", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        } else {
            Text(friendlySpan(days), style = MaterialTheme.typography.headlineSmall, color = color)
            Text("$days days", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        }
    }
}
