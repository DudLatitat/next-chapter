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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
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
        modifier.fillMaxWidth().frost(shape, strong = true)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.12f), Color.Transparent), radius = 900f))
            .border(1.dp, accent.copy(alpha = 0.18f), shape)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        content = content,
    )
}

/** Rows that belong together, in one card with thin lines between them. */
@Composable
fun ListGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.fillMaxWidth().frost(shape),
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

// ---------------------------------------------------------------------------------------------
// Frost: the app's glass. Drawn without real blur (cheap on any phone): a translucent fill over
// the night sea, a fine grain, a soft border and a light top edge. "Glass effects" off = solid.
// ---------------------------------------------------------------------------------------------

/** On by default; Settings can turn it off for plain solid cards. */
val LocalGlass = androidx.compose.runtime.staticCompositionLocalOf { true }

/** A tiny tile of random light specks, made once and repeated: the "grain" of frosted glass. */
private val grain: androidx.compose.ui.graphics.ImageBitmap by lazy {
    val n = 96
    val rnd = java.util.Random(7)
    val px = IntArray(n * n) { if (rnd.nextFloat() < 0.5f) ((rnd.nextInt(10) + 4) shl 24) or 0xFFFFFF else 0 }
    android.graphics.Bitmap.createBitmap(px, n, n, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
}
private val grainBrush by lazy {
    androidx.compose.ui.graphics.ShaderBrush(androidx.compose.ui.graphics.ImageShader(grain, androidx.compose.ui.graphics.TileMode.Repeated, androidx.compose.ui.graphics.TileMode.Repeated))
}

/** Frosted glass for a card of this [shape]; [strong] for hero cards (a little brighter). */
@Composable
fun Modifier.frost(shape: androidx.compose.ui.graphics.Shape, strong: Boolean = false): Modifier {
    if (!LocalGlass.current) return clip(shape).background(if (strong) Night.cardHigh else Night.card).border(1.dp, Color.White.copy(alpha = 0.06f), shape)
    val top = if (strong) 0.78f else 0.66f
    return clip(shape)
        .background(Brush.verticalGradient(listOf(Night.cardTop.copy(alpha = top), Night.cardBottom.copy(alpha = top - 0.12f))))
        .background(grainBrush)
        .border(1.dp, Brush.verticalGradient(listOf(Night.ink.copy(alpha = if (strong) 0.22f else 0.16f), Night.ink.copy(alpha = 0.04f))), shape)
        .drawWithContent {
            drawContent()
            // The light catching the top edge of the glass.
            val y = 1.dp.toPx()
            drawLine(
                Brush.horizontalGradient(listOf(Color.Transparent, Night.ink.copy(alpha = 0.22f), Color.Transparent)),
                androidx.compose.ui.geometry.Offset(size.width * 0.12f, y), androidx.compose.ui.geometry.Offset(size.width * 0.88f, y), strokeWidth = 1.dp.toPx(),
            )
        }
}

// ---------------------------------------------------------------------------------------------
// Page anatomy: every tab page starts with TabHeader (title, 🔍, ⚙) and a mono status line.
// ---------------------------------------------------------------------------------------------

/** What a tab header's 🔍 and ⚙ open (provided once at the top of the app). */
class AppNav(val openSearch: () -> Unit, val openSettings: () -> Unit)
val LocalAppNav = androidx.compose.runtime.staticCompositionLocalOf { AppNav({}, {}) }

/** One fact in a status line; tap to go where it's about. */
class Fact(val text: String, val color: Color = Night.ink, val onClick: (() -> Unit)? = null)

/** The terminal accent: one mono line of ≤3 facts, e.g. "▸ 41d to 成都 · rung 2/8 · 11mo runway". */
@Composable
fun StatusStrip(facts: List<Fact>, modifier: Modifier = Modifier) {
    if (facts.isEmpty()) return
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        Text("▸ ", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.sun)
        facts.take(3).forEachIndexed { i, f ->
            if (i > 0) Text("  ·  ", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.muted)
            Text(
                f.text, style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = f.color, maxLines = 1,
                modifier = if (f.onClick != null) Modifier.heightIn(min = 32.dp).pressClick(haptic = false, onClick = f.onClick).padding(vertical = 6.dp) else Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

/** A tab page's header: title (with its Chinese), 🔍 and ⚙, then the status line. */
@Composable
fun TabHeader(title: String, zh: String, facts: List<Fact> = emptyList(), modifier: Modifier = Modifier) {
    val nav = LocalAppNav.current
    Column(modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(title, style = MaterialTheme.typography.headlineLarge, color = Night.ink, maxLines = 1)
                Spacer(Modifier.width(10.dp))
                Text(zh, style = MaterialTheme.typography.titleMedium, color = Night.muted, maxLines = 1, modifier = Modifier.padding(bottom = 4.dp))
            }
            androidx.compose.material3.IconButton(onClick = nav.openSearch) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Search, "Search", tint = Night.ink.copy(alpha = 0.8f))
            }
            androidx.compose.material3.IconButton(onClick = nav.openSettings) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Settings, "Settings", tint = Night.ink.copy(alpha = 0.8f))
            }
        }
        StatusStrip(facts)
    }
}

// ---------------------------------------------------------------------------------------------
// Buttons, empty states, action rows.
// ---------------------------------------------------------------------------------------------

/** The one filled button on a screen: full width on the cover screen, at most 320dp unfolded. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Night.sun, enabled: Boolean = true) {
    androidx.compose.material3.Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().widthIn(max = 320.dp).heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color, contentColor = Night.ground2),
    ) { Text(text, style = MaterialTheme.typography.titleMedium, maxLines = 1) }
}

/** A quieter outlined button for the second choice. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Night.ink) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1) }
}

/** Nothing here yet: a badge, one friendly line, and one button. */
@Composable
fun EmptyState(badge: String, color: Color, line: String, button: String? = null, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        IconBadge(badge, color, 52.dp)
        Spacer(Modifier.height(12.dp))
        Text(line, style = MaterialTheme.typography.bodyLarge, color = Night.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (button != null) { Spacer(Modifier.height(14.dp)); SecondaryButton(button, onClick, color = color) }
    }
}

/** A row that does something (rather than opening a page): a labelled chip on the right. */
@Composable
fun ActionRow(title: String, action: String, color: Color, onAction: () -> Unit, modifier: Modifier = Modifier, subtitle: String? = null, badge: String? = null, divider: Boolean = true) {
    ListRow(
        title, modifier, subtitle = subtitle, divider = divider,
        leading = badge?.let { b -> { IconBadge(b, color) } },
        trailing = { GoChip(action, color, onAction) },
    )
}
