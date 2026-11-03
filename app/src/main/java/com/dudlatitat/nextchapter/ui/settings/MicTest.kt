package com.dudlatitat.nextchapter.ui.settings

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.speech.Clip
import com.dudlatitat.nextchapter.speech.Offline
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.speech.VoskResult
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.learn.rememberMic
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun roomVerdict(db: Float) = when {
    db < -55 -> "Quiet ✓" to Night.jade
    db < -42 -> "Some background noise; should be okay" to Night.sun
    else -> "Noisy. The check will struggle; try somewhere quieter or hold the phone closer" to Night.coral
}

private fun voiceVerdict(db: Float) = when {
    db < -38 -> "Quiet; speak up or hold the phone closer" to Night.coral
    db > -8 -> "Very loud; may distort, hold it a bit further away" to Night.sun
    else -> "Good level ✓" to Night.jade
}

private suspend fun playBack(clip: Clip) = withContext(Dispatchers.IO) {
    val bytes = clip.pcm16()
    val track = AudioTrack.Builder()
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
        .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(clip.rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
        .setBufferSizeInBytes(bytes.size)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    track.write(bytes, 0, bytes.size)
    track.play()
    Thread.sleep((clip.seconds * 1000).toLong() + 300)
    track.release()
}

/** For when the speaking check isn't hearing you: room noise, your mic level, and what the recognizer hears. */
@Composable
fun MicTestScreen(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var level by remember { mutableFloatStateOf(0f) }
    var busy by remember { mutableStateOf<String?>(null) }
    var room by remember { mutableStateOf<Float?>(null) }
    var voice by remember { mutableStateOf<Float?>(null) }
    var clip by remember { mutableStateOf<Clip?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var heard by remember { mutableStateOf<VoskResult?>(null) }

    val measureRoom = rememberMic {
        scope.launch {
            busy = "room"
            val c = Recorder.record(maxSeconds = 3f, stop = { false }, level = { level = it })
            room = c.rmsDb(); busy = null
        }
    }
    val recordVoice = rememberMic {
        scope.launch {
            vm.speaker.stop()
            busy = "voice"
            val c = Recorder.record(maxSeconds = 5f, level = { level = it })
            clip = c
            // The loudest half-second is your voice.
            val win = c.rate / 2
            voice = (0 until (c.samples.size / win).coerceAtLeast(1)).maxOf { k -> c.rmsDb(k * win, minOf(c.samples.size, (k + 1) * win)) }
            busy = "play"; playBack(c); busy = null
        }
    }
    val listen = rememberMic {
        scope.launch {
            vm.speaker.stop()
            busy = "listen"; heard = null
            val c = Recorder.record(maxSeconds = 6f, level = { level = it })
            busy = "recognize"
            heard = Offline.recognize(context, c) { status = it }
            status = null; busy = null
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column {
                Text("Microphone check", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("If the speaking check isn't hearing you", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        if (busy in setOf("room", "voice", "listen")) Bar(level, Night.sun)

        GlassCard {
            Text("1 · Room noise", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("Stay quiet for 3 seconds while it listens.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            room?.let { val (t, c) = roomVerdict(it); Text("${it.toInt()} dB · $t", style = MaterialTheme.typography.bodyMedium, color = c) }
            OutlinedButton(onClick = measureRoom, enabled = busy == null) { Text(if (busy == "room") "Listening…" else "Measure", color = Night.sun) }
        }

        GlassCard {
            Text("2 · Hear what the app hears", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("Say a phrase like 你好，我叫Rudy. It plays back right after, so you can hear if it's muffled, quiet or noisy.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            voice?.let { val (t, c) = voiceVerdict(it); Text("Voice ${it.toInt()} dB · $t", style = MaterialTheme.typography.bodyMedium, color = c) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = recordVoice, enabled = busy == null) { Text(when (busy) { "voice" -> "Recording…"; "play" -> "Playing…"; else -> "Record & play back" }, color = Night.sun) }
                if (clip != null && busy == null) OutlinedButton(onClick = { scope.launch { busy = "play"; playBack(clip!!); busy = null } }) { Text("Replay", color = Night.muted) }
            }
        }

        GlassCard {
            Text("3 · What the recognizer hears", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text(
                "Say anything in Chinese. This is the on-phone recognizer the speaking check uses, listening freely (in practice it only listens for the card's own words, which is much easier).",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
            status?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.sun) }
            heard?.let { r ->
                when {
                    !r.ok -> Text("✗ ${r.error}", style = MaterialTheme.typography.bodyMedium, color = Night.coral)
                    r.text.isEmpty() -> Text("Heard nothing it knows. Try again, a bit slower and closer.", style = MaterialTheme.typography.bodyMedium, color = Night.sun)
                    else -> {
                        Pill("Heard ✓", Night.jade)
                        Spacer(Modifier.height(4.dp))
                        Text(r.text, style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    }
                }
            }
            OutlinedButton(onClick = listen, enabled = busy == null) {
                Text(when (busy) { "listen" -> "Listening… pause to finish"; "recognize" -> "Recognizing…"; else -> "Listen" }, color = Night.sun)
            }
        }
    }
}
