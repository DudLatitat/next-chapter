package com.dudlatitat.nextchapter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.ui.theme.Night

/*
 * The small kit the redesigned screens are built from (so every tab looks like one app):
 * HeroCard (one per tab, the thing to look at first), ListGroup + ListRow (lists inside one card
 * instead of a card per item), SegmentedChips (pick one, never wraps), StatTile (a number with a
 * label), IconBadge (a round badge with one character, instead of emoji as icons).
 */

/** The one card a tab leads with: a little bigger, with a glow in its [accent] colour. */
@Composable
fun HeroCard(modifier: Modifier = Modifier, accent: Color = Night.sun, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier.fillMaxWidth().clip(shape)
            .background(Night.cardBrush)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.12f), Color.Transparent), radius = 900f))
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        content = content,
    )
}

/** Rows that belong together, in one card with thin lines between them. */
@Composable
fun ListGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(Night.card.copy(alpha = 0.85f)).border(1.dp, Color.White.copy(alpha = 0.06f), shape),
        content = content,
    )
}

/** One row of a [ListGroup]: optional badge, title and subtitle, something on the right. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    divider: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = { Text("›", style = MaterialTheme.typography.titleLarge, color = Night.muted) },
    titleColor: Color = Night.ink,
    onClick: (() -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .then(if (onClick != null) Modifier.pressClick(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) { leading(); Spacer(Modifier.width(14.dp)) }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (trailing != null) { Spacer(Modifier.width(10.dp)); trailing() }
        }
        if (divider) HorizontalDivider(Modifier.padding(start = if (leading != null) 70.dp else 16.dp), thickness = 1.dp, color = Night.outline.copy(alpha = 0.5f))
    }
}

/** A round badge with one or two characters (字, 玩, 考…) in an area's colour. */
@Composable
fun IconBadge(text: String, color: Color, size: Dp = 40.dp, filled: Boolean = false) {
    Box(
        Modifier.size(size).clip(CircleShape).background(if (filled) color else color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text, color = if (filled) Night.ground2 else color,
            fontSize = (size.value * (when (text.length) { 1 -> 0.45f; 2 -> 0.32f; else -> 0.24f })).sp,
            style = MaterialTheme.typography.titleMedium, maxLines = 1,
        )
    }
}

/** Pick one of a few, as a row of chips that scrolls sideways rather than wrapping. */
@Composable
fun SegmentedChips(options: List<Pair<String, Color>>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, (label, color) ->
            val on = i == selected
            val bg by animateColorAsState(if (on) color.copy(alpha = 0.24f) else Night.card.copy(alpha = 0.85f), label = "chip")
            val fg by animateColorAsState(if (on) color else Night.muted, label = "chipText")
            Text(
                label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1,
                modifier = Modifier.heightIn(min = 44.dp).pressClick { onSelect(i) }.clip(RoundedCornerShape(22.dp)).background(bg)
                    .border(1.dp, if (on) color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

/** A number with a label under it; [big] uses the display serif. */
@Composable
fun StatTile(value: String, label: String, color: Color, modifier: Modifier = Modifier, big: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = if (big) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall, color = color, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted, maxLines = 1)
    }
}

/** A small filled action button for rows ("Go", "Start"). */
@Composable
fun GoChip(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text, style = MaterialTheme.typography.labelLarge, color = Night.ground2,
        modifier = Modifier.heightIn(min = 40.dp).pressClick(onClick = onClick).clip(RoundedCornerShape(20.dp)).background(color)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

@Composable
fun Gap(h: Dp = 12.dp) = Spacer(Modifier.height(h))
