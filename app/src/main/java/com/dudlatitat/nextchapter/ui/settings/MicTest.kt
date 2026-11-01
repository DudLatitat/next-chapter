package com.dudlatitat.nextchapter.ui.settings

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.dudlatitat.nextchapter.speech.Heard
import com.dudlatitat.nextchapter.speech.Recognize
import com.dudlatitat.nextchapter.speech.Recorder
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
    else -> "Noisy. Recognition will struggle; try somewhere quieter or hold the phone closer" to Night.coral
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

/** Find out why the word check isn't hearing you: room noise, mic level, and both recognizer methods. */
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
    var liveResult by remember { mutableStateOf<Heard?>(null) }
    var clipResult by remember { mutableStateOf<Heard?>(null) }
    var mode by remember { mutableStateOf(vm.coach.mode) }
    val service = remember { Recognize.service(context) }

    val measureRoom = rememberMic {
        scope.launch {
            busy = "room"
            val c = Recorder.record(maxSeconds = 3f, stop = { false }, level = { level = it })
            room = c.rmsDb(); busy = null
        }
    }
    val recordVoice = rememberMic {
        scope.launch {
            busy = "voice"
            val c = Recorder.record(maxSeconds = 5f, level = { level = it })
            clip = c
            // The loudest half-second is your voice.
            val win = c.rate / 2
            voice = (0 until (c.samples.size / win).coerceAtLeast(1)).maxOf { k -> c.rmsDb(k * win, minOf(c.samples.size, (k + 1) * win)) }
            busy = "play"; playBack(c); busy = null
        }
    }
    val testLive = rememberMic { scope.launch { busy = "live"; liveResult = Recognize.live(context); busy = null } }
    val testClip = rememberMic {
        scope.launch {
            busy = "clip"
            val c = Recorder.record(maxSeconds = 6f, level = { level = it })
            busy = "clipcheck"
            clipResult = Recognize.fromClip(context, c); busy = null
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column {
                Text("Mic & recognition test", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("Find out why the word check isn't hearing you", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }

        GlassCard {
            Text("Your phone", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("Recognizer: ${Recognize.describe(service)}", style = MaterialTheme.typography.bodySmall, color = if (service?.contains("google") == true) Night.ink else Night.sun)
            Text("Available: ${if (Recognize.available(context)) "yes" else "no"} · Android ${Build.VERSION.RELEASE}" + if (Build.VERSION.SDK_INT < 33) " (one-take mode needs 13+)" else "", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            if (busy in setOf("room", "voice", "clip")) { Spacer(Modifier.height(6.dp)); Bar(level, Night.sun) }
        }

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
            Text("3 · Recognizer, live", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("Tap, then say something simple in Chinese (你好, 谢谢, 我想你).", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            ResultLine(liveResult)
            OutlinedButton(onClick = testLive, enabled = busy == null) { Text(if (busy == "live") "Listening… speak now" else "Test live", color = Night.sun) }
        }

        GlassCard {
            Text("4 · Recognizer, one take (recording)", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("The app records you, then hands the recording to the recognizer. It gives the pitch graph too, but not every phone accepts it.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            ResultLine(clipResult)
            OutlinedButton(onClick = testClip, enabled = busy == null) { Text(when (busy) { "clip" -> "Recording… pause to finish"; "clipcheck" -> "Recognizing…"; else -> "Test one take" }, color = Night.sun) }
        }

        GlassCard(highlight = true) {
            Text("Which to use", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text(
                when {
                    clipResult?.ok == true -> "One take works on your phone. Keep it: you get the pitch graph and the word check from a single try."
                    liveResult?.ok == true -> "Live works but one take doesn't. Switch to Live: the word check will work; the pitch graph needs one take."
                    liveResult != null || clipResult != null -> "Neither heard you yet. Check the room noise and playback above, make sure the recognizer is Google's, and download offline Chinese in Google's speech settings."
                    else -> "Run tests 3 and 4, then pick the one that heard you."
                },
                style = MaterialTheme.typography.bodySmall, color = Night.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                listOf("recorded" to "One take", "live" to "Live").forEach { (v, l) ->
                    androidx.compose.material3.FilterChip(selected = mode == v, onClick = { mode = v; vm.coach.mode = v }, label = { Text(l) })
                }
            }
        }
    }
}

@Composable
private fun ResultLine(r: Heard?) {
    if (r == null) return
    if (r.ok) {
        Pill("Heard ✓", Night.jade)
        r.texts.take(3).forEachIndexed { i, t -> Text("${i + 1}. $t", style = MaterialTheme.typography.bodyLarge, color = Night.ink) }
    } else {
        Text("✗ ${r.error}", style = MaterialTheme.typography.bodyMedium, color = Night.coral)
    }
}
