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
import com.dudlatitat.nextchapter.speech.LiveControl
import com.dudlatitat.nextchapter.speech.Offline
import com.dudlatitat.nextchapter.speech.VoskResult
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
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    var installed by remember { mutableStateOf<Boolean?>(null) }
    var downloadAsked by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf<String?>(null) }
    androidx.compose.runtime.LaunchedEffect(Unit) { installed = Recognize.chineseInstalled(context) }

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
    var liveControl by remember { mutableStateOf<LiveControl?>(null) }
    val testLive = rememberMic {
        scope.launch {
            val c = LiveControl(level = { level = it })
            liveControl = c; busy = "live"
            liveResult = try { Recognize.live(context, c) } finally { liveControl = null; busy = null }
        }
    }
    // Offline recognizer (Vosk) test: free listening, or a pick between same-sound words.
    val toneSets = remember { listOf(listOf("妈", "麻", "马", "骂"), listOf("汤", "糖", "躺", "烫")) }
    var voskSet by remember { mutableStateOf<List<String>?>(null) }
    var voskStatus by remember { mutableStateOf<String?>(null) }
    var voskResult by remember { mutableStateOf<VoskResult?>(null) }
    var voskTone by remember { mutableStateOf<Int?>(null) }
    var voskMs by remember { mutableStateOf<Long?>(null) }
    val bundled = remember { Offline.bundled(context) }
    val testVosk = rememberMic {
        scope.launch {
            busy = "vosk"; voskResult = null; voskTone = null; voskStatus = null
            val c = Recorder.record(maxSeconds = 6f, level = { level = it })
            busy = "voskcheck"
            val t0 = System.currentTimeMillis()
            val set = voskSet
            voskResult = Offline.recognize(context, c, grammar = set) { voskStatus = it }
            voskMs = System.currentTimeMillis() - t0
            // The app's own pitch guess for the same take, to compare with what Vosk picked.
            if (set != null) voskTone = withContext(Dispatchers.Default) {
                com.dudlatitat.nextchapter.speech.Pitch.classify(com.dudlatitat.nextchapter.speech.Pitch.track(c.samples, c.rate), vm.coach.profile)?.tone
            }
            voskStatus = null; busy = null
        }
    }
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
            if (busy in setOf("room", "voice", "clip", "live", "vosk")) { Spacer(Modifier.height(6.dp)); Bar(level, Night.sun) }
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
            OutlinedButton(onClick = { if (busy == "live") liveControl?.stop() else testLive() }, enabled = busy == null || busy == "live") {
                Text(if (busy == "live") "■ Stop (listening…)" else "Test live", color = Night.sun)
            }
        }

        GlassCard {
            Text("4 · Recognizer, one take (recording)", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text("The app records you, then hands the recording to the recognizer. It gives the pitch graph too, but not every phone accepts it.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            ResultLine(clipResult)
            installed?.let { Text(if (it) "Offline Chinese: installed ✓" else "Offline Chinese: not downloaded (one take needs it)", style = MaterialTheme.typography.bodySmall, color = if (it) Night.jade else Night.sun) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = testClip, enabled = busy == null) { Text(when (busy) { "clip" -> "Recording… pause to finish"; "clipcheck" -> "Recognizing…"; else -> "Test one take" }, color = Night.sun) }
            }
            if (installed != true) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (Build.VERSION.SDK_INT >= 33) OutlinedButton(onClick = {
                        downloadAsked = true
                        downloadStatus = "Asking Google…"
                        Recognize.downloadChinese(context) { downloadStatus = it }
                        scope.launch { repeat(10) { kotlinx.coroutines.delay(6000); installed = Recognize.chineseInstalled(context); if (installed == true) return@launch } }
                    }) { Text("Download Chinese", color = Night.jade, maxLines = 1) }
                    OutlinedButton(onClick = {
                        val tries = listOf(
                            android.content.Intent("android.settings.VOICE_INPUT_SETTINGS"),
                            android.content.Intent(android.provider.Settings.ACTION_SETTINGS),
                        )
                        for (i in tries) { try { context.startActivity(i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)); break } catch (_: Exception) {} }
                    }) { Text("Open voice settings", color = Night.sun, maxLines = 1) }
                }
                downloadStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.ink) }
                Text(
                    "By hand: in voice settings, tap the ⚙️ next to Speech Recognition & Synthesis from Google → Offline (or On-device) speech recognition → add Chinese (Mandarin, Simplified). " +
                        "Or: Google app → your picture → Settings → Voice → Offline speech recognition.",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
            }
        }

        GlassCard(highlight = true) {
            Text("5 · Offline recognizer (new, on the phone)", style = MaterialTheme.typography.titleSmall, color = Night.ink)
            Text(
                "Vosk runs on your phone with no internet and no Google, from the same recording the pitch graph uses. " +
                    "\"Anything\": say a phrase and see what it heard. A word set: say one of the four, and see if it tells the tones apart.",
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
            if (!bundled) Text("This build doesn't include the model.", style = MaterialTheme.typography.bodySmall, color = Night.coral)
            // Wraps onto two lines on the narrow cover screen.
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilterChip(selected = voskSet == null, onClick = { voskSet = null }, label = { Text("Anything") })
                toneSets.forEach { set ->
                    androidx.compose.material3.FilterChip(selected = voskSet == set, onClick = { voskSet = set }, label = { Text(set.joinToString(" ")) })
                }
            }
            voskStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.sun) }
            voskResult?.let { r ->
                if (!r.ok) Text("✗ ${r.error}", style = MaterialTheme.typography.bodyMedium, color = Night.coral)
                else if (r.text.isEmpty()) Text("Heard nothing it knows. Try again, a bit slower and closer.", style = MaterialTheme.typography.bodyMedium, color = Night.sun)
                else {
                    Pill("Heard ✓", Night.jade)
                    Text(r.text, style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text(
                        r.words.joinToString("  ") { w -> "${w.text} ${"%.2f".format(w.start)}–${"%.2f".format(w.end)}s ${(w.conf * 100).toInt()}%" },
                        style = MaterialTheme.typography.labelSmall, color = Night.muted,
                    )
                }
                voskSet?.let { set ->
                    voskTone?.let { t -> Text("Your pitch sounded like tone $t (${set.getOrNull(t - 1) ?: "?"})", style = MaterialTheme.typography.bodySmall, color = Night.ink) }
                }
                voskMs?.let { Text("Took ${"%.1f".format(it / 1000f)} s" + if (it > 4000) " (the first time includes unpacking the model)" else "", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
            }
            OutlinedButton(onClick = testVosk, enabled = busy == null && bundled) {
                Text(when (busy) { "vosk" -> "Recording… pause to finish"; "voskcheck" -> "Recognizing…"; else -> "Test offline" }, color = Night.sun)
            }
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
