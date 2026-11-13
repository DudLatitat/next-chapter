package com.dudlatitat.nextchapter.ui.learn

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Said it for real ★: tap when you actually used this phrase with her or her family. Gold once
 * used (with how many times); a little burst when you tap. Counts as a Good answer (CardEdits.saidForReal).
 */
@Composable
fun RealStar(state: AppState, card: Card, vm: MainViewModel, onUse: (() -> Unit)? = null) {
    val uses = state.realUses.count { it.id == card.id }
    val today = state.realUses.any { it.id == card.id && it.day == today() }
    val haptic = LocalHapticFeedback.current
    var bursts by remember { mutableIntStateOf(0) }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(bursts) { if (bursts > 0) { pop.snapTo(1.7f); pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) } }
    IconButton(onClick = {
        if (!today) { if (onUse != null) onUse() else vm.saidForReal(card); haptic.performHapticFeedback(HapticFeedbackType.LongPress); bursts++ }
    }) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (uses > 0) "★" else "☆", fontSize = 22.sp, color = if (uses > 0) Night.sun else Night.muted,
                    modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                )
                if (uses > 1) Text("$uses", fontSize = 9.sp, color = Night.sun)
            }
        }
    }
}
