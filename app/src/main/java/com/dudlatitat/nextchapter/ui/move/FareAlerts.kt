package com.dudlatitat.nextchapter.ui.move

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.FareAlert
import com.dudlatitat.nextchapter.data.money
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.data.prettyDate
import com.dudlatitat.nextchapter.notify.FareWatch
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

private fun shortDate(iso: String) = parseDate(iso)?.format(DateTimeFormatter.ofPattern("MMM d")) ?: iso

/** Fare alerts: set the trip you want, and the app checks prices for you once or twice a day. */
@Composable
fun FareAlertsCard(state: AppState, vm: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasKey by remember { mutableStateOf(FareWatch.key(context) != null) }
    var editingKey by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FareAlert?>(null) }
    var checking by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var perDay by remember { mutableIntStateOf(FareWatch.perDay(context)) }
    var used by remember { mutableIntStateOf(FareWatch.usedThisMonth(context)) }
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun newAlert() = FareAlert(
        id = "alert:${System.currentTimeMillis()}",
        departStart = (parseDate(state.tripDate) ?: LocalDate.now().plusMonths(2)).minusDays(4).toString(),
        departEnd = (parseDate(state.tripDate) ?: LocalDate.now().plusMonths(2)).plusDays(4).toString(),
        maxPrice = if (state.flightTarget > 0) state.flightTarget else 750,
    )

    GlassCard(highlight = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Fare alerts 🔔", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text("Checks Google Flights prices ${if (perDay == 2) "twice" else "once"} a day and notifies you when one fits", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        if (!hasKey) {
            Spacer(Modifier.height(8.dp))
            Text(
                "One-time setup: make a free SerpApi account, copy your API key from its dashboard, and paste it here. It stays on this phone.",
                style = MaterialTheme.typography.bodyMedium, color = Night.ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedButton(onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://serpapi.com/users/sign_up")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
                }, modifier = Modifier.weight(1f)) { Text("Sign up", color = Night.sun, maxLines = 1) }
                OutlinedButton(onClick = { editingKey = true }, modifier = Modifier.weight(1f)) { Text("Paste key", color = Night.sun, maxLines = 1) }
            }
            return@GlassCard
        }

        state.alerts.forEach { a ->
            Spacer(Modifier.height(10.dp))
            AlertRow(state, a, onEdit = { editing = a }, onToggle = { vm.saveAlert(a.copy(enabled = !a.enabled)) })
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
                editing = newAlert()
            }, modifier = Modifier.weight(1f)) { Text("+ New alert", color = Night.sun, maxLines = 1) }
            if (state.alerts.any { it.enabled }) OutlinedButton(
                onClick = { scope.launch { checking = true; status = FareWatch.checkAll(context); used = FareWatch.usedThisMonth(context); checking = false } },
                enabled = !checking, modifier = Modifier.weight(1f),
            ) { Text(if (checking) "Checking…" else "Check now", color = Night.sun, maxLines = 1) }
        }
        status?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Night.ink, modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Checks a day:", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            listOf(1, 2).forEach { n -> FilterChip(selected = perDay == n, onClick = { perDay = n; FareWatch.setPerDay(context, n) }, label = { Text("$n") }) }
        }
        val monthly = perDay * 30 * state.alerts.count { it.enabled }
        Text(
            "$used of 250 free searches used this month · about $monthly a month at this pace" + if (monthly > 250) ". That's over the free plan; use 1 check a day or fewer alerts." else "",
            style = MaterialTheme.typography.bodySmall, color = if (monthly > 250 || used > 220) Night.sun else Night.muted,
        )
        TextButton(onClick = { editingKey = true }) { Text("Change API key", color = Night.muted) }
    }

    if (editingKey) {
        var key by remember { mutableStateOf(FareWatch.key(context) ?: "") }
        AlertDialog(
            onDismissRequest = { editingKey = false },
            title = { Text("SerpApi key") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Find it on serpapi.com → Dashboard → Your Private API Key.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    OutlinedTextField(key, { key = it.trim() }, singleLine = true, label = { Text("API key") })
                }
            },
            confirmButton = { TextButton(onClick = { FareWatch.setKey(context, key); hasKey = key.isNotBlank(); editingKey = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editingKey = false }) { Text("Cancel") } },
        )
    }
    editing?.let { a ->
        AlertDialog(a, isNew = state.alerts.none { it.id == a.id }, onDismiss = { editing = null },
            onSave = { vm.saveAlert(it); editing = null }, onDelete = { vm.deleteAlert(a); editing = null })
    }
}

@Composable
private fun AlertRow(state: AppState, a: FareAlert, onEdit: () -> Unit, onToggle: () -> Unit) {
    val context = LocalContext.current
    val found = state.fares.filter { it.alertId == a.id }
    val bestFit = found.filter { it.fits }.minByOrNull { it.price }
    val cheapest = found.minByOrNull { it.price }
    GlassCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${a.from} → ${a.to.replace(",", "/")}", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                Text(
                    "Leave ${shortDate(a.departStart)}–${shortDate(a.departEnd)} · stay ${a.minDays}–${a.maxDays} days",
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
                Text(
                    listOfNotNull(
                        "≤ ${money(a.maxPrice)}",
                        when (a.maxStops) { -1 -> null; 0 -> "nonstop"; else -> "≤ ${a.maxStops} stop${if (a.maxStops > 1) "s" else ""}" },
                        if (a.maxHours > 0) "≤ ${a.maxHours}h each way" else null,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Night.muted,
                )
            }
            Switch(a.enabled, { onToggle() }, colors = SwitchDefaults.colors(checkedTrackColor = Night.sun))
        }
        Spacer(Modifier.height(6.dp))
        when {
            bestFit != null -> Pill("Best match ${money(bestFit.price)} · ${shortDate(bestFit.depart)}–${shortDate(bestFit.returnDate)}", Night.jade)
            cheapest != null -> Pill("Cheapest seen ${money(cheapest.price)} · none fit yet", Night.sun)
            else -> Pill("Not checked yet", Night.muted)
        }
        val pairs = FareWatch.pairs(a).size
        Text(
            (if (a.lastChecked > 0) "Last checked ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(a.lastChecked))} · " else "") +
                "checks all $pairs date pairs every ${FareWatch.daysToCover(context, a)} days",
            style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun AlertDialog(alert: FareAlert, isNew: Boolean, onDismiss: () -> Unit, onSave: (FareAlert) -> Unit, onDelete: () -> Unit) {
    var a by remember { mutableStateOf(alert) }
    var picking by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf(alert.maxPrice.toString()) }
    var minD by remember { mutableStateOf(alert.minDays.toString()) }
    var maxD by remember { mutableStateOf(alert.maxDays.toString()) }
    var hours by remember { mutableStateOf(if (alert.maxHours > 0) alert.maxHours.toString() else "") }
    val digits = KeyboardOptions(keyboardType = KeyboardType.Number)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New fare alert" else "Fare alert") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(a.from, { a = a.copy(from = it.uppercase().filter { c -> c.isLetter() || c == ',' }) }, label = { Text("From") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(a.to, { a = a.copy(to = it.uppercase().filter { c -> c.isLetter() || c == ',' }) }, label = { Text("To") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("CTU and TFU are both Chengdu airports.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                Text("Leave between", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picking = "start" }, modifier = Modifier.weight(1f)) { Text(prettyDate(a.departStart) ?: "pick", color = Night.sun, maxLines = 1) }
                    OutlinedButton(onClick = { picking = "end" }, modifier = Modifier.weight(1f)) { Text(prettyDate(a.departEnd) ?: "pick", color = Night.sun, maxLines = 1) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(minD, { minD = it.filter(Char::isDigit).take(2) }, label = { Text("Stay min (days)") }, singleLine = true, keyboardOptions = digits, modifier = Modifier.weight(1f))
                    OutlinedTextField(maxD, { maxD = it.filter(Char::isDigit).take(2) }, label = { Text("Stay max") }, singleLine = true, keyboardOptions = digits, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(price, { price = it.filter(Char::isDigit).take(5) }, label = { Text("Max round-trip price ($)") }, singleLine = true, keyboardOptions = digits)
                Text("Stops each way", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Nonstop", 1 to "≤ 1", 2 to "≤ 2", -1 to "Any").forEach { (v, l) ->
                        FilterChip(selected = a.maxStops == v, onClick = { a = a.copy(maxStops = v) }, label = { Text(l) })
                    }
                }
                OutlinedTextField(hours, { hours = it.filter(Char::isDigit).take(2) }, label = { Text("Max travel time each way (hours, optional)") }, singleLine = true, keyboardOptions = digits)
                if (!isNew) TextButton(onClick = onDelete) { Text("Delete alert", color = Night.coral) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val lo = minD.toIntOrNull()?.coerceIn(1, 90) ?: 12
                val hi = (maxD.toIntOrNull() ?: lo).coerceIn(lo, 90)
                val start = parseDate(a.departStart); val end = parseDate(a.departEnd)
                val fixed = if (start != null && end != null && end.isBefore(start)) a.copy(departEnd = a.departStart) else a
                onSave(fixed.copy(minDays = lo, maxDays = hi, maxPrice = price.toIntOrNull() ?: 750, maxHours = hours.toIntOrNull() ?: 0, cursor = 0))
            }, enabled = a.from.isNotBlank() && a.to.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
    picking?.let { which ->
        PickDate(
            initial = if (which == "start") a.departStart else a.departEnd,
            onDismiss = { picking = null },
            onPick = { iso -> if (iso != null) a = if (which == "start") a.copy(departStart = iso) else a.copy(departEnd = iso); picking = null },
        )
    }
}
