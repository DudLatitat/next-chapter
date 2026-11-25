package com.dudlatitat.nextchapter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.DayPlan
import com.dudlatitat.nextchapter.data.Payoff
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

/**
 * Where "Next" goes: set once at the top of the app, so any results screen can chain straight
 * into the next of today's three (a review, a game, A+, tonight's job).
 */
val LocalPlanNav = staticCompositionLocalOf<(DayPlan.Item) -> Unit> { {} }

@Composable
fun ProvidePlanNav(go: (DayPlan.Item) -> Unit, content: @Composable () -> Unit) = CompositionLocalProvider(LocalPlanNav provides go, content = content)

private fun colorOf(kind: DayPlan.Kind) = when (kind) { DayPlan.Kind.REVIEW -> Night.sun; DayPlan.Kind.PLAY -> Night.rose; DayPlan.Kind.LIFE -> Night.violet }

/** A number that counts up from 0 when it appears (scores feel earned when they tick up). */
@Composable
fun CountUp(value: Int, style: androidx.compose.ui.text.TextStyle, color: Color, prefix: String = "", suffix: String = "") {
    val n = remember { Animatable(0f) }
    LaunchedEffect(value) { n.animateTo(value.toFloat(), tween(800, easing = FastOutSlowInEasing)) }
    Text(prefix + n.value.toInt() + suffix, style = style, color = color)
}

/**
 * The payoff after a session or game, the same everywhere: today's three as small rings that
 * fill from where they were to where they are now (a ring that closes pops with sparks and a
 * buzz), any readiness piece you just earned, and "Next" straight into what's left.
 * [before] is the state when the screen opened.
 */
@Composable
fun PayoffPanel(before: AppState, after: AppState, modifier: Modifier = Modifier) {
    val result = remember(before, after) { Payoff.of(before, after) }
    val go = LocalPlanNav.current
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(result.closedAny) { if (result.closedAny) { kotlinx.coroutines.delay(1100); haptic.performHapticFeedback(HapticFeedbackType.LongPress) } }
    GlassCard(modifier) {
        Text(
            when { result.allDone -> "今天完成了 · All three done ✓"; result.closedAny -> "A ring closed ✓"; else -> "Today's three" },
            style = MaterialTheme.typography.titleSmall, color = if (result.closedAny) Night.jade else Night.ink,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            result.rings.forEachIndexed { i, r -> MiniRing(r, delayMs = 300 + i * 120) }
        }
        result.climbed?.let { rung ->
            Spacer(Modifier.height(12.dp))
            Text("✨ New rung: ${rung.hanzi} ${rung.title}", style = MaterialTheme.typography.titleSmall, color = Night.sun)
        }
        if (result.ticked.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            result.ticked.take(3).forEach { Text("✓ $it", style = MaterialTheme.typography.bodySmall, color = Night.jade) }
        }
        result.next?.let { item ->
            Spacer(Modifier.height(12.dp))
            val c = colorOf(item.kind)
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).pressClick { go(item) }
                    .clip(RoundedCornerShape(14.dp)).background(c.copy(alpha = 0.16f)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Next", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = Night.ink, maxLines = 2)
                }
                Text("→", style = MaterialTheme.typography.titleLarge, color = c)
            }
        }
    }
}

/** One small ring: fills from its old value, then (if it closed) pops and throws sparks. */
@Composable
private fun MiniRing(r: Payoff.Ring, delayMs: Int) {
    val color = colorOf(r.kind)
    val fill = remember { Animatable(r.from) }
    val pop = remember { Animatable(1f) }
    val spark = remember { Animatable(0f) }
    LaunchedEffect(r) {
        kotlinx.coroutines.delay(delayMs.toLong())
        fill.animateTo(r.to, tween(900, easing = FastOutSlowInEasing))
        if (r.closed) {
            kotlinx.coroutines.coroutineScope {
                launch { pop.animateTo(1.12f, spring(dampingRatio = 0.4f, stiffness = 400f)); pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f)) }
                launch { spark.animateTo(1f, tween(600, easing = LinearEasing)) }
            }
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            if (r.closed && spark.value in 0.01f..0.99f) Canvas(Modifier.size(64.dp)) {
                val t = spark.value
                repeat(8) { k ->
                    val a = (k / 8f) * 2f * PI.toFloat()
                    val dist = size.minDimension / 2f * (0.55f + 0.5f * t)
                    drawCircle(
                        (if (k % 2 == 0) Night.sun else color).copy(alpha = 1f - t), radius = 3.dp.toPx() * (1f - t * 0.5f),
                        center = Offset(center.x + cos(a) * dist, center.y + sin(a) * dist),
                    )
                }
            }
            ProgressRing(
                fill.value, 48.dp, if (fill.value >= 0.999f) Night.jade else color, stroke = 5.dp,
                modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
            ) {
                Text(if (fill.value >= 0.999f) "✓" else "${(fill.value * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Night.ink)
            }
        }
        Text(r.label, style = MaterialTheme.typography.labelMedium, color = if (r.closed) Night.jade else Night.muted)
    }
}

/**
 * 今天完成了: the once-a-day moment when all three are done. Full screen under her sky's
 * colour, lanterns drifting up, the streak, and "Claim 🏮". Never shown during her call.
 */
@Composable
fun DayClaim(state: AppState, streak: Int, onClaim: () -> Unit) {
    val herMin = Life_herMinute()
    val sky = Color(com.dudlatitat.nextchapter.data.HerSky.color(herMin))
    val haptic = LocalHapticFeedback.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true; kotlinx.coroutines.delay(400); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    val glow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { glow.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(sky, Night.ground2)))
            .pressClick(haptic = false) { },
        contentAlignment = Alignment.Center,
    ) {
        Lanterns(Modifier.fillMaxSize().graphicsLayer { alpha = glow.value })
        AnimatedVisibility(shown, enter = fadeIn(tween(500)) + slideInVertically(tween(600, easing = Motion.Emph)) { it / 6 }) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏮", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text("今天完成了", style = MaterialTheme.typography.headlineLarge, color = Night.sun)
                Text("Done for today", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Spacer(Modifier.height(16.dp))
                if (streak > 0) CountUp(streak, MaterialTheme.typography.displaySmall, Night.sun, prefix = "🔥 ")
                if (streak > 0) Text(if (streak == 1) "day" else "days in a row", style = MaterialTheme.typography.labelLarge, color = Night.muted)
                Spacer(Modifier.height(12.dp))
                val whose = state.herName.trim().let { if (it.isEmpty()) "her" else "$it's" }
                Text(
                    "Every one of these is a sentence closer to $whose family's table.",
                    style = MaterialTheme.typography.bodyMedium, color = Night.muted, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = onClaim, modifier = Modifier.height(52.dp).width(200.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = Night.ground2),
                ) { Text("Claim 🏮", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

private fun Life_herMinute(): Int = com.dudlatitat.nextchapter.data.Life.herTime(java.time.ZonedDateTime.now()).let { it.hour * 60 + it.minute }

/** Paper lanterns rising slowly from the bottom, each on its own sway. */
@Composable
private fun Lanterns(modifier: Modifier) {
    val seeds = remember { List(9) { k -> floatArrayOf((k * 0.113f + 0.07f) % 1f, (k * 0.37f) % 1f, 0.7f + (k % 3) * 0.2f) } }
    val t by rememberInfiniteTransition(label = "lanterns").animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart), label = "rise")
    Canvas(modifier) {
        seeds.forEach { (x, phase, scale) ->
            val p = (t + phase) % 1f
            val cx = size.width * x + sin((p * 6f + phase * 10f)) * 14.dp.toPx()
            val cy = size.height * (1.05f - p * 1.1f)
            val r = 9.dp.toPx() * scale
            val a = (if (p < 0.15f) p / 0.15f else if (p > 0.85f) (1f - p) / 0.15f else 1f) * 0.85f
            drawCircle(Brush.radialGradient(listOf(Night.sun.copy(alpha = 0.35f * a), Color.Transparent), center = Offset(cx, cy), radius = r * 3f), radius = r * 3f, center = Offset(cx, cy))
            drawRoundRect(
                Color(0xFFFF8A5B).copy(alpha = a), topLeft = Offset(cx - r * 0.8f, cy - r),
                size = androidx.compose.ui.geometry.Size(r * 1.6f, r * 2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.7f),
            )
        }
    }
}

/** The state as it was when this screen opened (for the payoff's "before"). */
@Composable
fun rememberBefore(vm: com.dudlatitat.nextchapter.MainViewModel): AppState = remember { vm.state.value }

/** [PayoffPanel] wired to the live state. */
@Composable
fun SessionPayoff(vm: com.dudlatitat.nextchapter.MainViewModel, before: AppState, modifier: Modifier = Modifier) {
    val now by vm.state.collectAsState()
    PayoffPanel(before, now, modifier)
}
