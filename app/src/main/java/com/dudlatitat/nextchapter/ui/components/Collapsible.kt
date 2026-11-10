package com.dudlatitat.nextchapter.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Whether each collapsible section is open, remembered on the phone (a screen preference, so
 * it lives in its own small settings file, not in your saved progress or backups).
 */
private fun uiPrefs(context: Context) = context.getSharedPreferences("ui_sections", Context.MODE_PRIVATE)

/**
 * A card with a header you tap to open or close: title, a one-line summary (shown when closed),
 * and an arrow that turns. [key] names it so the app remembers how you left it.
 */
@Composable
fun Collapsible(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultOpen: Boolean = false,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { uiPrefs(context) }
    var open by remember(key) { mutableStateOf(prefs.getBoolean(key, defaultOpen)) }
    val turn by animateFloatAsState(if (open) 90f else 0f, label = "arrow")
    GlassCard(modifier, highlight = highlight) {
        Row(
            Modifier.fillMaxWidth().clickable {
                open = !open
                prefs.edit().putBoolean(key, open).apply()
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Night.ink)
                if (summary != null && !open) {
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("▸", style = MaterialTheme.typography.titleMedium, color = Night.muted, modifier = Modifier.rotate(turn).padding(4.dp))
        }
        AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}
