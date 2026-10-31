package com.dudlatitat.nextchapter.ui.move

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.PriceSeen
import com.dudlatitat.nextchapter.data.Trip
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.friendlySpan
import com.dudlatitat.nextchapter.data.money
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.data.prettyDate
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Trips to China: plan them, track fares before booking, and keep the details in one place. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlightsScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Trip?>(null) }
    var loggingPrice by remember { mutableStateOf(false) }
    var deletingPrice by remember { mutableStateOf<PriceSeen?>(null) }
    val now = today()
    val upcoming = state.trips.filter { (parseDate(it.returnDate ?: it.depart)?.toEpochDay() ?: 0) >= now }
    val past = state.trips.filter { it !in upcoming }.reversed()
    // A date set by hand on the Move tab counts as the next trip until it has details.
    val manual = state.tripDate?.takeIf { (daysUntil(it) ?: -1) >= 0 }?.let { Trip(id = "trip:${System.currentTimeMillis()}", depart = it) }
    val next = upcoming.firstOrNull() ?: manual

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
                Column(Modifier.weight(1f)) {
                    Text("Flights · 机票", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                    Text("LAX ⇄ Chengdu", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
                IconButton(onClick = { editing = Trip(id = "trip:${System.currentTimeMillis()}", depart = LocalDate.now().plusMonths(2).toString()) }) {
                    Icon(Icons.Rounded.Add, "Add trip", tint = Night.sun)
                }
            }
        }

        // Next trip
        item {
            GlassCard(highlight = true, onClick = next?.let { { editing = it } }) {
                if (next == null) {
                    Text("No trip planned", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                    Text("Tap ＋ to add your next one. Its date becomes the trip countdown.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                } else {
                    val d = daysUntil(next.depart) ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Next trip", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                            Text(if (d > 0) friendlySpan(d) else "Happening now", style = MaterialTheme.typography.headlineMedium, color = Night.rose)
                            Text(tripDates(next), style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                            if (next.airline.isNotBlank() || next.flights.isNotBlank()) Text("${next.airline} ${next.flights}".trim(), style = MaterialTheme.typography.bodySmall, color = Night.muted)
                        }
                        Pill(if (next.booked) "Booked ✓" else "Not booked", if (next.booked) Night.jade else Night.sun)
                    }
                    if (next.confirmation.isNotBlank()) Text("Confirmation: ${next.confirmation}", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                    if (state.trips.none { it.depart == next.depart }) Text("Tap to add flights, price and return date", style = MaterialTheme.typography.bodySmall, color = Night.sun)
                }
            }
        }

        // Search
        if (next == null || !next.booked) item {
            GlassCard {
                Text("Find fares", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text(
                    if (next != null) "For ${tripDates(next)}" else "Add a trip to search with its dates",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { open(context, googleFlights(next)) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Google Flights", color = Night.sun, maxLines = 1, softWrap = false) }
                    OutlinedButton(onClick = { open(context, skyscanner(next)) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Skyscanner", color = Night.sun, maxLines = 1, softWrap = false) }
                }
            }
        }

        // Price tracker
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Price tracker", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                        val low = state.prices.minByOrNull { it.price }
                        Text(
                            buildString {
                                if (state.flightTarget > 0) append("Target ${money(state.flightTarget)}")
                                if (low != null) append(if (isEmpty()) "" else " · ").append("lowest seen ${money(low.price)}")
                                if (isEmpty()) append("Log fares as you see them to spot a good deal")
                            },
                            style = MaterialTheme.typography.bodySmall, color = Night.muted,
                        )
                    }
                    TextButton(onClick = { loggingPrice = true }) { Text("+ Log fare", color = Night.sun) }
                }
                val recent = state.prices.takeLast(30)
                if (recent.size >= 2) PriceChart(recent.map { it.price }, state.flightTarget)
                val latest = state.prices.lastOrNull()
                if (latest != null && state.flightTarget > 0) {
                    Text(
                        if (latest.price <= state.flightTarget) "✓ Latest fare is at or under your target. Good time to book!" else "Latest is ${money(latest.price - state.flightTarget)} over your target",
                        style = MaterialTheme.typography.bodyMedium, color = if (latest.price <= state.flightTarget) Night.jade else Night.muted,
                    )
                }
                state.prices.takeLast(6).reversed().forEach { p ->
                    Row(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { deletingPrice = p }).padding(vertical = 4.dp)) {
                        Text(prettyDate(p.day) ?: p.day, style = MaterialTheme.typography.bodyMedium, color = Night.muted, modifier = Modifier.weight(1f))
                        if (p.source.isNotBlank()) Text(p.source + "  ", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                        Text(money(p.price), style = MaterialTheme.typography.bodyMedium, color = Night.ink)
                    }
                }
            }
        }

        // This year
        item { YearStats(state) }

        if (upcoming.size > 1) {
            item { SectionLabel("Coming up") }
            items(upcoming.drop(1), key = { it.id }) { t -> TripRow(t) { editing = t } }
        }
        if (past.isNotEmpty()) {
            item { SectionLabel("Past trips") }
            items(past, key = { it.id }) { t -> TripRow(t) { editing = t } }
        }
    }

    editing?.let { t ->
        TripDialog(t, isNew = state.trips.none { it.id == t.id }, onDismiss = { editing = null },
            onSave = { vm.saveTrip(it); editing = null }, onDelete = { vm.deleteTrip(t); editing = null })
    }
    if (loggingPrice) PriceDialog(onDismiss = { loggingPrice = false }) { price, source -> vm.addPrice(price, source); loggingPrice = false }
    deletingPrice?.let { p ->
        AlertDialog(
            onDismissRequest = { deletingPrice = null },
            title = { Text("Remove this fare?") },
            text = { Text("${money(p.price)} on ${prettyDate(p.day)}") },
            confirmButton = { TextButton(onClick = { vm.deletePrice(p); deletingPrice = null }) { Text("Remove", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { deletingPrice = null }) { Text("Keep") } },
        )
    }
}

private fun tripDates(t: Trip): String {
    val f = DateTimeFormatter.ofPattern("MMM d")
    val a = parseDate(t.depart) ?: return t.depart
    val b = parseDate(t.returnDate)
    return if (b == null) a.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) else "${a.format(f)} – ${b.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))} · ${b.toEpochDay() - a.toEpochDay()} days"
}

@Composable
private fun TripRow(t: Trip, onClick: () -> Unit) {
    GlassCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tripDates(t), style = MaterialTheme.typography.titleSmall, color = Night.ink)
                Text(listOf(t.airline, t.flights).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "No flight details yet" }, style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            if (t.price > 0) Text(money(t.price), style = MaterialTheme.typography.titleSmall, color = Night.ink)
        }
    }
}

@Composable
private fun YearStats(state: AppState) {
    val year = LocalDate.now().year
    val trips = state.trips.filter { parseDate(it.depart)?.year == year }
    if (trips.isEmpty()) return
    val days = trips.sumOf { t ->
        val a = parseDate(t.depart) ?: return@sumOf 0L
        val b = parseDate(t.returnDate) ?: return@sumOf 0L
        (b.toEpochDay() - a.toEpochDay()).coerceAtLeast(0)
    }
    GlassCard {
        Text("$year so far", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${trips.size}", style = MaterialTheme.typography.headlineSmall, color = Night.rose); Text("trips", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("$days", style = MaterialTheme.typography.headlineSmall, color = Night.sun); Text("days together", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(money(trips.sumOf { it.price }), style = MaterialTheme.typography.headlineSmall, color = Night.jade); Text("on flights", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
        }
    }
}

@Composable
private fun PriceChart(prices: List<Int>, target: Int) {
    Canvas(Modifier.fillMaxWidth().height(90.dp).padding(vertical = 8.dp)) {
        val lo = (listOf(prices.min(), if (target > 0) target else prices.min()).min() * 0.95f)
        val hi = (listOf(prices.max(), target).max() * 1.05f)
        fun y(v: Number) = size.height * (1f - (v.toFloat() - lo) / (hi - lo).coerceAtLeast(1f))
        if (target > 0) drawLine(Night.jade.copy(alpha = 0.6f), Offset(0f, y(target)), Offset(size.width, y(target)), 1.5.dp.toPx())
        val p = Path()
        prices.forEachIndexed { i, v ->
            val x = i / (prices.size - 1f) * size.width
            if (i == 0) p.moveTo(x, y(v)) else p.lineTo(x, y(v))
        }
        drawPath(p, Night.sun, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        val min = prices.indexOf(prices.min())
        drawCircle(Night.jade, 5.dp.toPx(), Offset(min / (prices.size - 1f) * size.width, y(prices[min])))
        drawCircle(Color.White, 4.dp.toPx(), Offset(size.width, y(prices.last())))
    }
}

private fun open(context: android.content.Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
}

private fun googleFlights(t: Trip?): String {
    val q = buildString {
        append("Flights from LAX to CTU")
        if (t != null) { append(" on ${t.depart}"); t.returnDate?.let { append(" through $it") } }
    }
    return "https://www.google.com/travel/flights?q=" + Uri.encode(q)
}

private fun skyscanner(t: Trip?): String {
    val f = DateTimeFormatter.ofPattern("yyMMdd")
    val a = parseDate(t?.depart)?.format(f)
    val b = parseDate(t?.returnDate)?.format(f)
    return "https://www.skyscanner.com/transport/flights/lax/ctu/" + listOfNotNull(a, b).joinToString("/") + if (a != null) "/" else ""
}

@Composable
private fun TripDialog(trip: Trip, isNew: Boolean, onDismiss: () -> Unit, onSave: (Trip) -> Unit, onDelete: () -> Unit) {
    var t by remember { mutableStateOf(trip) }
    var price by remember { mutableStateOf(if (trip.price > 0) trip.price.toString() else "") }
    var picking by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New trip" else "Trip") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picking = "depart" }, modifier = Modifier.weight(1f)) { Text("Leave\n${prettyDate(t.depart) ?: "pick"}", color = Night.sun) }
                    OutlinedButton(onClick = { picking = "return" }, modifier = Modifier.weight(1f)) { Text("Return\n${prettyDate(t.returnDate) ?: "pick"}", color = Night.sun) }
                }
                OutlinedTextField(t.airline, { t = t.copy(airline = it) }, label = { Text("Airline") }, singleLine = true)
                OutlinedTextField(t.flights, { t = t.copy(flights = it) }, label = { Text("Flight numbers") }, singleLine = true)
                OutlinedTextField(price, { price = it.filter(Char::isDigit).take(6) }, label = { Text("Price paid ($)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(t.confirmation, { t = t.copy(confirmation = it) }, label = { Text("Confirmation code") }, singleLine = true)
                OutlinedTextField(t.notes, { t = t.copy(notes = it) }, label = { Text("Notes") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Booked", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(t.booked, { t = t.copy(booked = it) }, colors = SwitchDefaults.colors(checkedTrackColor = Night.jade))
                }
                if (!isNew) TextButton(onClick = onDelete) { Text("Delete trip", color = Night.coral) }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(t.copy(price = price.toIntOrNull() ?: 0)) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
    picking?.let { which ->
        PickDate(
            initial = if (which == "depart") t.depart else t.returnDate ?: t.depart,
            onDismiss = { picking = null },
            onPick = { iso -> t = if (which == "depart") t.copy(depart = iso ?: t.depart) else t.copy(returnDate = iso); picking = null },
        )
    }
}

@Composable
private fun PriceDialog(onDismiss: () -> Unit, onSave: (Int, String) -> Unit) {
    var price by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a fare you saw") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(price, { price = it.filter(Char::isDigit).take(6) }, label = { Text("Round-trip price ($)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(source, { source = it }, label = { Text("Where (airline / site)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { price.toIntOrNull()?.let { onSave(it, source) } }, enabled = price.toIntOrNull() != null) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
