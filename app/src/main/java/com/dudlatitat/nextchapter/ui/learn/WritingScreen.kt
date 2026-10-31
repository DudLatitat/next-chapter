package com.dudlatitat.nextchapter.ui.learn

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Pinyin
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.hypot


/** One character's strokes (SVG outlines) and their centre lines, in a 1024 box with y up. */
class CharStrokes(val outlines: List<Path>, val medians: List<List<Offset>>)

/** Stroke-order data for the app's characters (assets/strokes.json, from Make Me a Hanzi). */
object Strokes {
    private var data: JsonObject? = null
    private val cache = HashMap<Char, CharStrokes?>()

    suspend fun get(context: Context, c: Char): CharStrokes? = withContext(Dispatchers.Default) {
        cache[c]?.let { return@withContext it }
        val all = data ?: try {
            Json.parseToJsonElement(context.assets.open("strokes.json").bufferedReader().use { it.readText() }).jsonObject.also { data = it }
        } catch (_: Exception) { return@withContext null }
        val entry = all[c.toString()]?.jsonObject ?: return@withContext null
        val outlines = entry["s"]!!.jsonArray.map { PathParser().parsePathString(it.jsonPrimitive.content).toPath() }
        val medians = entry["m"]!!.jsonArray.map { stroke -> stroke.jsonArray.map { p -> val xy = p.jsonArray; Offset(xy[0].jsonPrimitive.float, xy[1].jsonPrimitive.float) } }
        CharStrokes(outlines, medians).also { cache[c] = it }
    }

    fun available(context: Context) = try { context.assets.open("strokes.json").close(); true } catch (_: Exception) { false }
}

/** Does a traced line match the stroke's centre line? Both in the 1024 data box. */
private fun matches(user: List<Offset>, median: List<Offset>): Boolean {
    if (user.size < 2) return median.lengthOf() < 120f && user.isNotEmpty() && dist(user.first(), median.first()) < 220f
    val u = resample(user, 16)
    val m = resample(median, 32)
    val avg = u.map { p -> m.minOf { dist(it, p) } }.average().toFloat()
    val start = dist(u.first(), median.first())
    val end = dist(u.last(), median.last())
    val lu = u.lengthOf() // smoothed, so finger jitter doesn't inflate it
    val lm = median.lengthOf()
    val ratio = lu / lm.coerceAtLeast(1f)
    val vu = u.last() - u.first(); val vm = median.last() - median.first()
    val cos = (vu.x * vm.x + vu.y * vm.y) / ((vu.getDistance() * vm.getDistance()).coerceAtLeast(1f))
    val directionOk = lm < 100f || cos > 0.3f
    val ratioOk = if (lm < 150f) ratio in 0.2f..4f else ratio in 0.4f..3f
    return avg < 150f && start < 260f && end < 260f && ratioOk && directionOk
}

private fun dist(a: Offset, b: Offset) = hypot(a.x - b.x, a.y - b.y)
private fun List<Offset>.lengthOf() = zipWithNext().sumOf { (a, b) -> dist(a, b).toDouble() }.toFloat()

private fun resample(pts: List<Offset>, n: Int): List<Offset> {
    val total = pts.lengthOf()
    if (total == 0f) return List(n) { pts.first() }
    val step = total / (n - 1)
    val out = mutableListOf(pts.first())
    var acc = 0f
    var i = 1
    var prev = pts.first()
    while (i < pts.size && out.size < n) {
        val d = dist(prev, pts[i])
        if (acc + d >= step && d > 0f) {
            val t = (step - acc) / d
            val q = Offset(prev.x + (pts[i].x - prev.x) * t, prev.y + (pts[i].y - prev.y) * t)
            out += q; prev = q; acc = 0f
        } else { acc += d; prev = pts[i]; i++ }
    }
    while (out.size < n) out += pts.last()
    return out
}

/** Practise writing the characters you're learning: watch the stroke order, then trace. */
@Composable
fun WritingScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    // Characters from phrases you've started, then the core words, one of each.
    val chars = remember {
        val started = state.cards.filter { !it.isNew }.sortedByDescending { it.last ?: 0 }
        val rest = state.cards.filter { it.deck == "core" }
        (started + rest).flatMap { card ->
            val sylls = Pinyin.syllablesIn(card.pinyin).map { (r, _) -> card.pinyin.substring(r.first, r.last + 1) }
            val hz = card.hanzi.filter { it.code in 0x4E00..0x9FFF }
            hz.mapIndexed { i, c -> Triple(c, if (sylls.size == hz.length) sylls[i] else (Pinyin.fromHanzi(c.toString()) ?: ""), card.english.takeIf { hz.length == 1 } ?: "in ${card.hanzi}") }
        }.distinctBy { it.first }
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val item = chars.getOrNull(index)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Writing · 写字", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("${index + 1} of ${chars.size} · from your phrases first", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        val hasData = remember { Strokes.available(context) }
        if (item == null || !hasData) {
            Text("Stroke data isn't in this build. Update to the next version.", color = Night.coral)
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PinyinText(item.second, style = MaterialTheme.typography.headlineSmall)
                Text(item.third, style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            }
            TextButton(onClick = { vm.speaker.say(item.first.toString(), slow = true) }) { Text("▶ Hear", color = Night.sun) }
        }
        TraceBox(item.first, onDone = {})
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { if (index > 0) index-- }, modifier = Modifier.weight(1f), enabled = index > 0) { Text("← Previous", color = Night.muted) }
            OutlinedButton(onClick = { if (index < chars.size - 1) index++ }, modifier = Modifier.weight(1f)) { Text("Next →", color = Night.sun) }
        }
    }
}

/** The writing square: shows the stroke order, then checks each stroke you trace. */
@Composable
private fun TraceBox(c: Char, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strokes by produceState<CharStrokes?>(null, c) { value = Strokes.get(context, c) }
    var done by remember(c) { mutableIntStateOf(0) }
    var misses by remember(c) { mutableIntStateOf(0) }
    var totalMisses by remember(c) { mutableIntStateOf(0) }
    var showOutline by remember(c) { mutableStateOf(true) }
    val trace = remember(c) { mutableStateListOf<Offset>() }
    val watch = remember(c) { Animatable(-1f) }
    val flash by rememberInfiniteTransition(label = "hint").animateFloat(0.15f, 0.6f, infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse), label = "hint")

    // Show the order once when a new character appears.
    LaunchedEffect(strokes) { strokes?.let { watch.snapTo(0f); watch.animateTo(it.outlines.size.toFloat(), tween(it.outlines.size * 550, easing = LinearEasing)); watch.snapTo(-1f) } }

    val s = strokes
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Canvas(
            Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Night.card)
                .pointerInput(c, s, done) {
                    if (s == null) return@pointerInput
                    val scale = size.width / 1024f
                    fun toData(p: Offset) = Offset(p.x / scale, 900f - p.y / scale)
                    detectDragGestures(
                        onDragStart = { trace.clear(); trace += it },
                        onDrag = { change, _ -> trace += change.position },
                        onDragEnd = {
                            if (done < s.medians.size) {
                                if (matches(trace.map(::toData), s.medians[done])) { done++; misses = 0; if (done == s.medians.size) onDone() }
                                else { misses++; totalMisses++ }
                            }
                            trace.clear()
                        },
                    )
                },
        ) {
            val scale = size.width / 1024f
            grid()
            if (s == null) return@Canvas
            withTransform({ translate(top = 900f * scale); scale(scale, -scale, Offset.Zero) }) {
                s.outlines.forEachIndexed { i, path ->
                    val w = watch.value
                    val color = when {
                        w >= 0f -> if (i < w.toInt()) Night.ink else if (i == w.toInt()) Night.sun.copy(alpha = (w - i).coerceIn(0f, 1f)) else Color.White.copy(alpha = 0.08f)
                        i < done -> Night.ink
                        i == done && misses >= 3 -> Night.sun.copy(alpha = flash)
                        showOutline -> Color.White.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }
                    drawPath(path, color)
                }
                // Where the next stroke starts, once you've missed it a few times.
                if (watch.value < 0f && done < s.medians.size && misses >= 3) drawCircle(Night.coral, 26f, s.medians[done].first())
            }
            if (trace.size > 1) {
                val p = Path().apply { moveTo(trace[0].x, trace[0].y); trace.drop(1).forEach { lineTo(it.x, it.y) } }
                drawPath(p, Night.sun, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (s != null) Text(
            when {
                done == s.medians.size -> if (totalMisses == 0) "Perfect! 写得好！" else "Done · $totalMisses slip${if (totalMisses == 1) "" else "s"}"
                misses >= 3 -> "Follow the glowing stroke, starting at the red dot"
                else -> "Stroke ${done + 1} of ${s.medians.size}"
            },
            style = MaterialTheme.typography.titleSmall, color = if (s != null && done == s.medians.size) Night.jade else Night.muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { scope.launch { s?.let { watch.snapTo(0f); watch.animateTo(it.outlines.size.toFloat(), tween(it.outlines.size * 550, easing = LinearEasing)); watch.snapTo(-1f) } } }) { Text("▶ Watch order", color = Night.sun) }
            TextButton(onClick = { done = 0; misses = 0; totalMisses = 0 }) { Text("↻ Again", color = Night.muted) }
            TextButton(onClick = { showOutline = !showOutline }) { Text(if (showOutline) "Hide outline" else "Show outline", color = Night.muted) }
        }
    }
}

/** The 米 guide lines inside the writing square. */
private fun DrawScope.grid() {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
    val c = Color.White.copy(alpha = 0.08f)
    val w = 1.dp.toPx()
    drawLine(c, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), w, pathEffect = dash)
    drawLine(c, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), w, pathEffect = dash)
    drawLine(c, Offset(0f, 0f), Offset(size.width, size.height), w, pathEffect = dash)
    drawLine(c, Offset(size.width, 0f), Offset(0f, size.height), w, pathEffect = dash)
}

