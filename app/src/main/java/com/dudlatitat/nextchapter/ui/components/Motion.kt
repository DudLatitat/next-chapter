package com.dudlatitat.nextchapter.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * How things move in the app, in one place so every screen feels the same.
 * Emphasized easing (fast start, gentle landing) for things arriving; its accelerating twin for
 * things leaving. Durations are short: motion should guide the eye, never make you wait.
 */
object Motion {
    val Emph = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphAcc = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    const val ENTER = 280
    const val EXIT = 200

    /** Going deeper (into a sub-screen): the new page slides in a little from the right. */
    fun push(): ContentTransform =
        (slideInHorizontally(tween(ENTER, easing = Emph)) { it / 6 } + fadeIn(tween(220, delayMillis = 40))) togetherWith
            (slideOutHorizontally(tween(EXIT, easing = EmphAcc)) { -it / 12 } + fadeOut(tween(120)))

    /** Coming back: the page underneath slides back from the left. */
    fun pop(): ContentTransform =
        (slideInHorizontally(tween(ENTER, easing = Emph)) { -it / 12 } + fadeIn(tween(220, delayMillis = 40))) togetherWith
            (slideOutHorizontally(tween(EXIT, easing = EmphAcc)) { it / 6 } + fadeOut(tween(120)))

    /** Switching tabs: a quick fade-through, the new tab settling from 97% size. */
    fun fadeThrough(): ContentTransform =
        (fadeIn(tween(210, delayMillis = 90, easing = Emph)) + scaleIn(tween(210, delayMillis = 90, easing = Emph), initialScale = 0.97f)) togetherWith
            fadeOut(tween(90))
}

/**
 * A tab's own little navigator: [root] is its main page, anything else is a sub-screen.
 * Sub-screens slide in and out, and the main page keeps its scroll position and open sections
 * while you're away (its state is held, not thrown away).
 */
@Composable
fun <T> SubNav(target: T, isRoot: (T) -> Boolean, label: String, content: @Composable (T) -> Unit) {
    val holder = rememberSaveableStateHolder()
    AnimatedContent(
        target,
        transitionSpec = { if (isRoot(targetState)) Motion.pop() else Motion.push() },
        label = label,
    ) { t ->
        if (isRoot(t)) holder.SaveableStateProvider("root") { content(t) } else content(t)
    }
}

/** One press: its touch source and the animated scale (0.96 while held). */
class Press(val source: MutableInteractionSource, val scale: androidx.compose.runtime.State<Float>)

@Composable
fun rememberPress(): Press {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = animateFloatAsState(
        if (pressed) 0.96f else 1f,
        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow + 400f),
        label = "press",
    )
    return remember(source) { Press(source, scale) }
}

/** Shrinks with the press (put it before the background so the whole card sinks). */
fun Modifier.pressScale(p: Press): Modifier = graphicsLayer { scaleX = p.scale.value; scaleY = p.scale.value }

/** The click half: ripple, a light tick, then [onClick] (put it after the background). */
@Composable
fun Modifier.pressClickable(p: Press, enabled: Boolean = true, haptic: Boolean = true, onClick: () -> Unit): Modifier {
    val feel = LocalHapticFeedback.current
    return clickable(interactionSource = p.source, indication = androidx.compose.material3.ripple(), enabled = enabled) {
        if (haptic) feel.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
    }
}

/**
 * Press feedback for anything tappable: it sinks to 96% with a soft spring and a light tick,
 * so a tap feels like pressing something real. Replaces `.clickable { }` where there's no
 * background in the same chain (otherwise use [pressScale] + [pressClickable]).
 */
@Composable
fun Modifier.pressClick(enabled: Boolean = true, haptic: Boolean = true, onClick: () -> Unit): Modifier {
    val p = rememberPress()
    return pressScale(p).pressClickable(p, enabled, haptic, onClick)
}

/**
 * The one header every sub-screen uses: back arrow (also handles the system back), title with an
 * optional Chinese line, and room for actions on the right.
 */
@Composable
fun SubHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    zh: String? = null,
    handleBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (handleBack) BackHandler(onBack = onBack)
    Row(modifier.padding(top = 20.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Night.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (zh != null) Text(zh, style = MaterialTheme.typography.labelMedium, color = Night.muted, maxLines = 1)
        }
        actions()
    }
}
