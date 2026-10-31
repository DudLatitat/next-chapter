package com.dudlatitat.nextchapter.ui.move

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.BudgetItem
import com.dudlatitat.nextchapter.data.PackItem
import com.dudlatitat.nextchapter.data.money
import com.dudlatitat.nextchapter.data.prettyDate
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.theme.Night
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

fun yuan(n: Number): String = "¥" + NumberFormat.getIntegerInstance(Locale.US).format(n.toLong())

/** Dollars ⇄ yuan, and a monthly budget for life in Neijiang. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoneyScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val scope = rememberCoroutineScope()
    var usd by remember { mutableStateOf("100") }
    var rmb by remember { mutableStateOf(((100 * state.rate).toLong()).toString()) }
    var updating by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BudgetItem?>(null) }
    val total = state.budget.sumOf { it.rmb }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text("Money · 人民币", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
        }
        GlassCard(highlight = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(usd, { v ->
                    usd = v.filter { it.isDigit() || it == '.' }.take(10)
                    usd.toDoubleOrNull()?.let { rmb = (it * state.rate).toLong().toString() }
                }, label = { Text("US $") }, singleLine = true, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Text("⇄", color = Night.muted)
                OutlinedTextField(rmb, { v ->
                    rmb = v.filter { it.isDigit() || it == '.' }.take(10)
                    rmb.toDoubleOrNull()?.let { usd = "%.2f".format(Locale.US, it / state.rate) }
                }, label = { Text("¥ yuan") }, singleLine = true, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("$1 = ¥${"%.4f".format(Locale.US, state.rate)}", style = MaterialTheme.typography.titleSmall, color = Night.ink)
                    Text(
                        when { failed -> "Couldn't update (no internet?)"; state.rateDate != null -> "Updated ${prettyDate(state.rateDate)}"; else -> "Approximate. Tap update for today's rate" },
                        style = MaterialTheme.typography.bodySmall, color = if (failed) Night.coral else Night.muted,
                    )
                }
                TextButton(onClick = { scope.launch { updating = true; failed = !vm.refreshRate(); updating = false } }, enabled = !updating) {
                    Text(if (updating) "Updating…" else "Update", color = Night.sun)
                }
            }
        }

        SectionLabel("Monthly budget there")
        GlassCard {
            state.budget.forEach { b ->
                Row(
                    Modifier.fillMaxWidth().combinedClickable(onClick = { editing = b }, onLongClick = { editing = b }).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(b.name, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
                        if (b.zh.isNotBlank()) Text(b.zh, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (b.rmb > 0) yuan(b.rmb) else "tap to set", style = MaterialTheme.typography.bodyLarge, color = if (b.rmb > 0) Night.ink else Night.muted)
                        if (b.rmb > 0) Text("≈ ${money((b.rmb / state.rate).toInt())}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                }
            }
            TextButton(onClick = { editing = BudgetItem(id = "budget:${System.currentTimeMillis()}", name = "") }) { Text("+ Add a line", color = Night.sun) }
        }
        GlassCard(highlight = true) {
            Text("Each month", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Text("${yuan(total)}  ≈ ${money((total / state.rate).toInt())}", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            if (total > 0 && state.saved > 0) {
                val months = state.saved * state.rate / total
                Text(
                    "Your savings (${money(state.saved)} ≈ ${yuan((state.saved * state.rate).toLong())}) would cover about ${"%.1f".format(Locale.US, months)} months there",
                    style = MaterialTheme.typography.bodyMedium, color = Night.jade,
                )
            }
        }
    }

    editing?.let { b ->
        var name by remember(b.id) { mutableStateOf(b.name) }
        var amount by remember(b.id) { mutableStateOf(if (b.rmb > 0) b.rmb.toString() else "") }
        val isNew = state.budget.none { it.id == b.id }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (isNew) "New budget line" else b.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("What") }, singleLine = true)
                    OutlinedTextField(amount, { amount = it.filter(Char::isDigit).take(7) }, label = { Text("Per month (¥)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    amount.toIntOrNull()?.let { Text("≈ ${money((it / state.rate).toInt())} a month", style = MaterialTheme.typography.bodySmall, color = Night.muted) }
                    if (!isNew) TextButton(onClick = { vm.deleteBudget(b); editing = null }) { Text("Remove line", color = Night.coral) }
                }
            },
            confirmButton = { TextButton(onClick = { vm.saveBudget(b.copy(name = name.trim(), rmb = amount.toIntOrNull() ?: 0)); editing = null }, enabled = name.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }
}

/** A packing list that resets for every trip. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackingScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<PackItem?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val done = state.packing.count { it.done }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("Packing · 行李", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
                Text("$done of ${state.packing.size} packed", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
        }
        Bar(if (state.packing.isEmpty()) 0f else done.toFloat() / state.packing.size, Night.rose)
        GlassCard {
            state.packing.forEach { item ->
                Row(Modifier.fillMaxWidth().combinedClickable(onClick = { vm.togglePack(item) }, onLongClick = { deleting = item }), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(item.done, { vm.togglePack(item) }, colors = CheckboxDefaults.colors(checkedColor = Night.rose, uncheckedColor = Night.muted))
                    Text(item.title, style = MaterialTheme.typography.bodyLarge, color = if (item.done) Night.muted else Night.ink, textDecoration = if (item.done) TextDecoration.LineThrough else null)
                }
            }
            TextButton(onClick = { adding = true }) { Text("+ Add item", color = Night.sun) }
        }
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) { Text("↻  Unpack all for the next trip", color = Night.muted) }
        Text("Long-press an item to remove it.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }

    if (adding) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("Add to the list") },
            text = { OutlinedTextField(text, { text = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { vm.addPack(text); adding = false }, enabled = text.isNotBlank()) { Text("Add") } },
            dismissButton = { TextButton(onClick = { adding = false }) { Text("Cancel") } },
        )
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove “${item.title}”?") },
            confirmButton = { TextButton(onClick = { vm.deletePack(item); deleting = null }) { Text("Remove", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep") } },
        )
    }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("Start fresh?") },
        text = { Text("Unchecks everything so the list is ready for your next trip.") },
        confirmButton = { TextButton(onClick = { vm.resetPacking(); confirmReset = false }) { Text("Unpack all") } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
    )
}
