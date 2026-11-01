package com.dudlatitat.nextchapter.ui.move

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.FareAlert
import com.dudlatitat.nextchapter.data.FareFound
import com.dudlatitat.nextchapter.data.FareOption
import com.dudlatitat.nextchapter.data.money
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.notify.FareWatch
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

private fun md(iso: String) = parseDate(iso)?.format(DateTimeFormatter.ofPattern("EEE MMM d")) ?: iso
private fun stamp(ms: Long) = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ms))
private fun hm(min: Int) = if (min <= 0) "" else "${min / 60}h ${min % 60}m"

private fun openUrl(context: android.content.Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
}

/** Everything about one fare alert: fares found (with flights and links), what's next, and the log. */
@Composable
fun AlertDetail(state: AppState, vm: MainViewModel, alert: FareAlert, onEdit: () -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf("fares") }
    var sortByPrice by rememberSaveable { mutableStateOf(true) }
    val fares = state.fares.filter { it.alertId == alert.id }
    var selectedDay by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = if (selectedDay == null) fares else fares.filter { it.depart == selectedDay }
    val shown = if (sortByPrice) filtered.sortedBy { it.price } else filtered.sortedByDescending { it.checkedAt }
    val log = state.fareLog.filter { it.alertId == alert.id || it.alertId == null }.reversed()
    val nextRun by produceState<Long?>(null, state.fareLog.size) { value = withContext(Dispatchers.IO) { FareWatch.nextRun(context) } }
    val upcoming = remember(state.fares.size, alert) { FareWatch.nextPairs(context, alert, FareWatch.runBudget(context).coerceAtLeast(3)) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column(Modifier.weight(1f)) {
                    Text("${alert.from} → ${alert.to.replace(",", "/")}", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text("Leave ${md(alert.departStart)}–${md(alert.departEnd)} · stay ${alert.minDays}–${alert.maxDays} days · ≤ ${money(alert.maxPrice)}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                TextButton(onClick = onEdit) { Text("Edit", color = Night.sun) }
            }
        }

        // What happens next
        item {
            GlassCard {
                Text("Behind the scenes", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                Spacer(Modifier.height(4.dp))
                val pairs = FareWatch.pairs(alert).size
                Text(
                    "Next check: " + (nextRun?.let { stamp(it) } ?: if (alert.enabled) "waiting to be scheduled" else "paused"),
                    style = MaterialTheme.typography.bodyMedium, color = Night.ink,
                )
                Text(
                    "Each check uses about ${FareWatch.runBudget(context)} search${if (FareWatch.runBudget(context) == 1) "" else "es"} · " +
                        "sweep ${alert.cursor + 1} of $pairs date pairs · ${alert.passes} full pass${if (alert.passes == 1) "" else "es"} done",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
                Text(
                    if (alert.passes == 0) "First pass: every date pair gets one look before anything is re-checked."
                    else "After the first pass, every other search re-checks the cheapest pairs, where a drop is most likely.",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
                if (upcoming.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Up next: " + upcoming.joinToString(" · ") { (a, b) -> "${a.format(DateTimeFormatter.ofPattern("MMM d"))}–${b.format(DateTimeFormatter.ofPattern("MMM d"))}" }, style = MaterialTheme.typography.bodySmall, color = Night.sun)
                }
                Text(
                    "Android may delay background checks a little to save battery; they still run twice a day.",
                    style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        item { FareCalendar(alert, fares, selectedDay) { selectedDay = if (selectedDay == it) null else it } }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = tab == "fares", onClick = { tab = "fares" }, label = { Text("Fares found (${fares.size})") })
                FilterChip(selected = tab == "log", onClick = { tab = "log" }, label = { Text("Activity log") })
            }
        }

        if (tab == "fares") {
            if (fares.isEmpty()) item { Text("Nothing yet. Tap Check now, or wait for the next check.", color = Night.muted) }
            else item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { sortByPrice = true }) { Text("Cheapest first", color = if (sortByPrice) Night.sun else Night.muted) }
                    TextButton(onClick = { sortByPrice = false }) { Text("Newest first", color = if (!sortByPrice) Night.sun else Night.muted) }
                }
            }
            items(shown, key = { it.checkedAt.toString() + it.depart + it.returnDate }) { f -> FareCard(alert, f) }
        } else {
            if (log.isEmpty()) item { Text("No activity yet.", color = Night.muted) }
            items(log, key = { it.time.toString() + it.text.hashCode() }) { e ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        when (e.kind) { "fit" -> "✓"; "error" -> "!"; "skip" -> "–"; "run" -> "▶"; else -> "·" },
                        color = when (e.kind) { "fit" -> Night.jade; "error" -> Night.coral; "run" -> Night.sun; else -> Night.muted },
                        style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(22.dp),
                    )
                    Column {
                        Text(stamp(e.time), style = MaterialTheme.typography.labelSmall, color = Night.muted)
                        Text(e.text, style = MaterialTheme.typography.bodySmall, color = if (e.kind == "error") Night.coral else Night.ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun FareCard(alert: FareAlert, f: FareFound) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val best = f.options.firstOrNull { it.fits } ?: f.options.firstOrNull()
    GlassCard(onClick = { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${md(f.depart)} → ${md(f.returnDate)}", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                Text(
                    listOf(f.airline, if (f.stops == 0) "nonstop" else "${f.stops} stop${if (f.stops > 1) "s" else ""}", hm(f.minutes)).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
                Text("Found ${stamp(f.checkedAt)}" + (if (f.trigger.isNotBlank()) " · ${f.trigger}" else "") + if (f.notified) " · 🔔 notified" else "", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(money(f.price), style = MaterialTheme.typography.titleLarge, color = if (f.fits) Night.jade else Night.ink)
                Pill(if (f.fits) "Fits" else "Over limits", if (f.fits) Night.jade else Night.muted)
            }
        }
        if (best != null) Option(best)
        if (expanded && f.options.size > 1) {
            Spacer(Modifier.height(6.dp))
            SectionLabel("Other options from this search")
            f.options.filter { it != best }.forEach { Option(it, showPrice = true) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { openUrl(context, f.url.ifBlank { FareWatch.googleFlightsUrl(alert, f.depart, f.returnDate) }) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("Open in Google Flights", color = Night.sun, maxLines = 1)
            }
        }
        if (f.options.size > 1 && !expanded) Text("Tap for ${f.options.size - 1} more options", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        Text("Prices are round trip; Google Flights shows the return flights after you pick the outbound.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}

@Composable
private fun Option(o: FareOption, showPrice: Boolean = false) {
    Column(Modifier.padding(top = 6.dp)) {
        if (showPrice) Text(
            "${money(o.price)} · ${o.airline} · ${if (o.stops == 0) "nonstop" else "${o.stops} stop${if (o.stops > 1) "s" else ""}"} · ${hm(o.minutes)}" + if (o.fits) " ✓" else "",
            style = MaterialTheme.typography.bodySmall, color = if (o.fits) Night.jade else Night.ink,
        )
        o.legs.forEach { Text("✈ $it", style = MaterialTheme.typography.bodySmall, color = Night.ink) }
        o.layovers.forEach { Text("   ⏱ $it", style = MaterialTheme.typography.bodySmall, color = Night.muted) }
    }
}

/** Each departure day in the window, coloured by the cheapest fare found leaving that day. */
@Composable
private fun FareCalendar(alert: FareAlert, fares: List<FareFound>, selected: String?, onPick: (String) -> Unit) {
    val start = parseDate(alert.departStart) ?: return
    val end = parseDate(alert.departEnd) ?: return
    val days = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.take(62).toList()
    val cheapest = fares.groupBy { it.depart }.mapValues { (_, v) -> v.minOf { it.price } }
    GlassCard {
        Text("Fare calendar · by departure day", style = MaterialTheme.typography.titleSmall, color = Night.ink)
        Text("Green fits your price · yellow within 15% · tap a day to filter", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { d ->
                    val iso = d.toString()
                    val p = cheapest[iso]
                    val color = when {
                        p == null -> Night.muted
                        p <= alert.maxPrice -> Night.jade
                        p <= alert.maxPrice * 1.15 -> Night.sun
                        else -> Night.coral
                    }
                    Column(
                        Modifier.weight(1f).padding(vertical = 2.dp)
                            .then(if (iso == selected) Modifier.background(color.copy(alpha = 0.35f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) else Modifier.background(color.copy(alpha = 0.12f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp)))
                            .clickable { onPick(iso) }.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(d.format(DateTimeFormatter.ofPattern("EEE")).take(2), style = MaterialTheme.typography.labelSmall, color = Night.muted)
                        Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelLarge, color = Night.ink)
                        Text(p?.let { "$" + it } ?: "–", style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
