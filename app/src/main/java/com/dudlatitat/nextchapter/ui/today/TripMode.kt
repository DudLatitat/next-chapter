package com.dudlatitat.nextchapter.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Phrasebook
import com.dudlatitat.nextchapter.data.Trip
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.move.yuan
import com.dudlatitat.nextchapter.ui.theme.Night

/** The trip happening now, if any. */
fun AppState.currentTrip(): Trip? {
    val d = today()
    return trips.firstOrNull { t ->
        val a = parseDate(t.depart)?.toEpochDay() ?: return@firstOrNull false
        val b = parseDate(t.returnDate)?.toEpochDay() ?: a
        d in a..b
    }
}

fun AppState.tripModeOn(): Boolean = tripMode == "on" || (tripMode == "auto" && currentTrip() != null)

/** On a trip: day count, flights home, her address, quick yuan and the lines you'll need most. */
@Composable
fun TripModeCard(state: AppState, vm: MainViewModel, onPhrasebook: () -> Unit) {
    val trip = state.currentTrip()
    GlassCard(highlight = true) {
        Text("🇨🇳 Trip mode", style = MaterialTheme.typography.labelMedium, color = Night.rose)
        if (trip != null) {
            val a = parseDate(trip.depart)!!.toEpochDay()
            val b = parseDate(trip.returnDate)?.toEpochDay()
            val day = (today() - a + 1).toInt()
            Text(if (b != null) "Day $day of ${b - a + 1}" else "Day $day", style = MaterialTheme.typography.headlineMedium, color = Night.ink)
            if (b != null) Text(if (b == today()) "Flying home today" else "${b - today()} days until you fly home", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            if (trip.flights.isNotBlank() || trip.confirmation.isNotBlank()) Text(
                listOf(trip.airline, trip.flights, trip.confirmation.takeIf { it.isNotBlank() }?.let { "conf. $it" }).filter { !it.isNullOrBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall, color = Night.muted,
            )
        } else Text("Survival kit", style = MaterialTheme.typography.headlineMedium, color = Night.ink)

        if (state.addressZh.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("Her address · tap to say it", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Text(state.addressZh, fontSize = 22.sp, color = Night.ink, modifier = Modifier.clickable { vm.speaker.say(state.addressZh) })
        }
        Spacer(Modifier.height(10.dp))
        Text("Money", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(10, 50, 100).forEach { usd -> Text("$$usd ≈ ${yuan((usd * state.rate).toLong())}", style = MaterialTheme.typography.bodyMedium, color = Night.ink) }
        }
        Spacer(Modifier.height(10.dp))
        Text("Tap to say out loud", style = MaterialTheme.typography.labelMedium, color = Night.muted)
        Phrasebook.lines.take(6).forEach { line ->
            Row(Modifier.fillMaxWidth().clickable { vm.speaker.say(line.hanzi) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(line.hanzi, fontSize = 18.sp, color = Night.ink)
                    PinyinText(line.pinyin, style = MaterialTheme.typography.bodySmall)
                }
                Text(line.english, style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.weight(1f))
            }
        }
        OutlinedButton(onClick = onPhrasebook, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Full phrasebook (big text)", color = Night.sun) }
    }
}
