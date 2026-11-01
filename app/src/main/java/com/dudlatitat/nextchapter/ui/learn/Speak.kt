package com.dudlatitat.nextchapter.ui.learn

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.speech.Attempt
import com.dudlatitat.nextchapter.speech.CharResult
import com.dudlatitat.nextchapter.speech.Clip
import com.dudlatitat.nextchapter.speech.Mark
import com.dudlatitat.nextchapter.speech.Recorder
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.toneColor
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch

private val toneWords = mapOf(1 to "high & level", 2 to "rising", 3 to "low dip", 4 to "falling")

/** Asks for the mic the first time; runs [onGranted] once it's allowed. */
@Composable
fun rememberMic(onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) onGranted() }
    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) onGranted()
        else ask.launch(Manifest.permission.RECORD_AUDIO)
    }
}

/** The round mic button: tap to talk, tap again to stop; pulses with your voice. */
@Composable
fun MicButton(recording: Boolean, level: Float, busy: Boolean, onClick: () -> Unit) {
    val pulse by animateFloatAsState(if (recording) 1f + level * 0.35f else 1f, label = "pulse")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
        if (recording) Box(Modifier.size(84.dp).scale(pulse).clip(CircleShape).background(Night.coral.copy(alpha = 0.25f)))
        Box(
            Modifier.size(72.dp).clip(CircleShape)
                .background(if (recording) Night.coral else Night.sun)
                .clickable(enabled = !busy, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (busy) CircularProgressIndicator(Modifier.size(28.dp), color = Night.ground2, strokeWidth = 3.dp)
            else Text(if (recording) "■" else "🎤", fontSize = 28.sp, color = Night.ground2)
        }
    }
}

/** Say a phrase and see how it came out: each character, its tone, and your pitch melody. */
@Composable
fun SpeakPanel(hanzi: String, pinyin: String, vm: MainViewModel, onCalibrate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var attempt by remember(hanzi) { mutableStateOf<Attempt?>(null) }
    var liveChars by remember(hanzi) { mutableStateOf<List<CharResult>?>(null) }
    var stopNow by remember { mutableStateOf(false) }

    var listeningLive by remember { mutableStateOf(false) }
    val start = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        if (listeningLive) return@rememberMic
        scope.launch {
            vm.speaker.stop()
            liveChars = null
            val a = if (vm.coach.mode == "live") {
                // The recognizer listens through the mic itself.
                listeningLive = true; busy = true
                vm.coach.checkLive(hanzi, pinyin).also { listeningLive = false }
            } else {
                recording = true; stopNow = false
                val clip: Clip = Recorder.record(stop = { stopNow }, level = { level = it })
                recording = false; busy = true
                vm.coach.check(clip, hanzi, pinyin)
            }
            attempt = a
            busy = false
            logTones(vm, a, pinyin)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        MicButton(recording, level, busy, onClick = start)
        Text(
            when { listeningLive -> "Listening… just speak"; recording -> "Listening… tap to stop"; busy -> "Checking…"; attempt == null -> "Tap and say it"; else -> "Tap to try again" },
            style = MaterialTheme.typography.bodySmall, color = Night.muted,
        )
        attempt?.let { a ->
            Spacer(Modifier.height(10.dp))
            if (a.tooQuiet) {
                Text("Didn't catch that. Speak up a little, closer to the phone.", style = MaterialTheme.typography.bodyMedium, color = Night.coral, textAlign = TextAlign.Center)
                return@let
            }
            val chars = liveChars ?: a.chars
            if (chars != null) {
                CharResults(chars)
                a.heardText?.let { Text("Heard: $it", style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center) }
            } else {
                Text("Word check: ${a.recognizerError ?: "not available"}", style = MaterialTheme.typography.bodySmall, color = Night.coral, textAlign = TextAlign.Center)
                if (vm.coach.mode != "live") Text("Tip: switch to Live in Settings → Speaking check.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
                if (vm.coach.mode != "live") TextButton(onClick = {
                    scope.launch {
                        busy = true; listeningLive = true
                        val live = vm.coach.checkLive(hanzi, pinyin)
                        listeningLive = false; busy = false
                        liveChars = live.chars ?: emptyList()
                        if (live.chars == null) attempt = a.copy(recognizerError = live.recognizerError)
                    }
                }) { Text("Try the live recognizer (say it once more)", color = Night.sun) }
            }
            a.tone?.let { t ->
                val aimed = com.dudlatitat.nextchapter.data.Pinyin.syllablesIn(pinyin).firstOrNull()?.second
                Spacer(Modifier.height(6.dp))
                Text(
                    if (aimed == null || t.tone == aimed) "Tone ${t.tone} (${toneWords[t.tone]}) ✓" else "Sounded like tone ${t.tone} (${toneWords[t.tone]}), aim for tone $aimed (${toneWords[aimed]})",
                    style = MaterialTheme.typography.titleSmall, color = if (aimed == null || t.tone == aimed) Night.jade else Night.coral, textAlign = TextAlign.Center,
                )
            }
            if (a.refCurve.isNotEmpty() && a.userCurve.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Melody", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Spacer(Modifier.width(8.dp))
                    a.melody?.let { m -> Pill("$m%", if (m >= 75) Night.jade else if (m >= 50) Night.sun else Night.coral) }
                }
                PitchGraph(a.userCurve, a.refCurve)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Legend("Native", Night.muted); Legend("You", Night.sun)
                }
            }
            if (!vm.coach.calibrated) TextButton(onClick = onCalibrate) { Text("Set up your voice for more accurate tones →", color = Night.violet) }
        }
    }
}

/** Feeds what a speaking check found into the tone history (weak-tone drills, weekly recap). */
private fun logTones(vm: MainViewModel, a: Attempt, pinyin: String) {
    val P = com.dudlatitat.nextchapter.data.Pinyin
    a.tone?.let { t -> P.syllablesIn(pinyin).firstOrNull()?.second?.let { aimed -> vm.logTone(aimed, t.tone, spoken = true) } }
    if (a.tone == null) a.chars?.forEach { c ->
        val target = P.tone(c.target)
        when (c.mark) {
            Mark.RIGHT -> vm.logTone(target, target, spoken = true)
            Mark.TONE -> c.heard?.let { vm.logTone(target, P.tone(it), spoken = true) }
            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharResults(chars: List<CharResult>) {
    if (chars.isEmpty()) {
        Text("The recognizer couldn't make out the words. Try once more, a bit slower.", style = MaterialTheme.typography.bodySmall, color = Night.coral, textAlign = TextAlign.Center)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chars.forEach { c ->
            val color = when (c.mark) { Mark.RIGHT -> Night.jade; Mark.TONE -> Night.sun; Mark.WRONG -> Night.coral; Mark.MISSED -> Night.muted }
            Column(
                Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.14f)).border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(c.hanzi.toString(), fontSize = 24.sp, color = Night.ink)
                PinyinText(c.target, style = MaterialTheme.typography.labelMedium)
                if (c.heard != null) Text("heard ${c.heard}", style = MaterialTheme.typography.labelSmall, color = color)
                if (c.mark == Mark.MISSED) Text("missed", style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
    val right = chars.count { it.mark == Mark.RIGHT }
    val tones = chars.count { it.mark == Mark.TONE }
    Spacer(Modifier.height(6.dp))
    Text(
        when {
            right == chars.size -> "Every character understood. 很好!"
            tones > 0 -> "$right/${chars.size} clear · yellow ones were heard with another tone"
            else -> "$right/${chars.size} understood"
        },
        style = MaterialTheme.typography.bodySmall, color = Night.muted, textAlign = TextAlign.Center,
    )
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 14.dp, height = 3.dp).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

/** Your pitch line over the native one, both stretched to the same length. */
@Composable
fun PitchGraph(user: FloatArray, ref: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(110.dp).padding(vertical = 8.dp)) {
        val lo = minOf(user.minOrNull() ?: 0f, ref.minOrNull() ?: 0f, -6f) - 1f
        val hi = maxOf(user.maxOrNull() ?: 0f, ref.maxOrNull() ?: 0f, 6f) + 1f
        fun line(v: FloatArray, color: Color, width: Float) {
            if (v.size < 2) return
            val p = Path()
            v.forEachIndexed { i, s ->
                val x = i / (v.size - 1f) * size.width
                val y = size.height * (1f - (s - lo) / (hi - lo))
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            drawPath(p, color, style = Stroke(width, cap = StrokeCap.Round))
        }
        val mid = size.height * (1f - (0f - lo) / (hi - lo))
        drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, mid), Offset(size.width, mid), 1.dp.toPx())
        line(ref, Night.muted, 3.dp.toPx())
        line(user, Night.sun, 4.dp.toPx())
    }
}

/** Four quick recordings (妈 麻 马 骂) so tone checks fit your voice. */
@Composable
fun CalibrateScreen(vm: MainViewModel, onDone: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onDone)
    val scope = rememberCoroutineScope()
    val steps = listOf("妈" to "mā", "麻" to "má", "马" to "mǎ", "骂" to "mà")
    var step by remember { mutableIntStateOf(0) }
    val clips = remember { mutableStateListOf<Clip>() }
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var stopNow by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Boolean?>(null) }

    val record = rememberMic {
        if (recording) { stopNow = true; return@rememberMic }
        scope.launch {
            vm.speaker.stop()
            recording = true; stopNow = false
            val clip = Recorder.record(maxSeconds = 3f, stop = { stopNow }, level = { level = it })
            recording = false
            val ok = com.dudlatitat.nextchapter.speech.Pitch.syllable(
                com.dudlatitat.nextchapter.speech.Pitch.track(clip.samples, clip.rate),
            ) != null
            if (!ok) return@launch
            clips.add(clip)
            if (step < 3) { step++; vm.speaker.say(steps[step].first, slow = true) }
            else { busy = true; result = vm.coach.calibrate(clips.toList()); busy = false }
        }
    }

    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Set up your voice", style = MaterialTheme.typography.headlineMedium, color = Night.ink)
        Text(
            "Say each one clearly, copying the native voice. This teaches the app your normal pitch and range, so tone checks fit you.",
            style = MaterialTheme.typography.bodyMedium, color = Night.muted, textAlign = TextAlign.Center,
        )
        when (result) {
            true -> {
                Text("All set ✓", style = MaterialTheme.typography.headlineSmall, color = Night.jade)
                TextButton(onClick = onDone) { Text("Done", color = Night.sun) }
            }
            false -> {
                Text("Those didn't come through clearly. Try again somewhere quieter.", color = Night.coral, textAlign = TextAlign.Center)
                TextButton(onClick = { clips.clear(); step = 0; result = null }) { Text("Start over", color = Night.sun) }
            }
            null -> {
                GlassCard(highlight = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${step + 1} of 4", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                        Text(steps[step].first, fontSize = 72.sp, color = Night.ink)
                        PinyinText(steps[step].second, style = MaterialTheme.typography.headlineSmall)
                        Text("Tone ${step + 1} · ${toneWords[step + 1]}", style = MaterialTheme.typography.bodyMedium, color = toneColor(step + 1))
                        TextButton(onClick = { vm.speaker.say(steps[step].first, slow = true) }) { Text("▶ Hear it", color = Night.sun) }
                    }
                }
                MicButton(recording, level, busy, onClick = record)
                Text(if (recording) "Listening…" else "Tap and say it", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        if (result != true) TextButton(onClick = onDone) { Text("Cancel", color = Night.muted) }
    }
}
