package com.dudlatitat.nextchapter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.BudgetItem
import com.dudlatitat.nextchapter.data.DayLog
import com.dudlatitat.nextchapter.data.PackItem
import com.dudlatitat.nextchapter.data.PriceSeen
import com.dudlatitat.nextchapter.data.ToneEvent
import com.dudlatitat.nextchapter.data.Trip
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.Grade
import com.dudlatitat.nextchapter.data.MoveTask
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import kotlinx.coroutines.flow.StateFlow
import com.dudlatitat.nextchapter.notify.Reminder
import com.dudlatitat.nextchapter.widget.PhraseWidget

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val store = Store.get(app)
    val state: StateFlow<AppState> = store.state
    val speaker = Speaker(app)
    val coach = com.dudlatitat.nextchapter.speech.Coach(app)

    init {
        store.onChange = { PhraseWidget.refresh(app, it) }
        PhraseWidget.refresh(app, store.state.value)
        Reminder.schedule(app, store.state.value)
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(app)
    }

    // Progress in the learning tools (for milestones)
    private fun addOnce(list: List<String>, id: String) = if (id in list) list else list + id
    fun markDialogue(id: String) = store.edit { it.copy(dialoguesDone = addOnce(it.dialoguesDone, id)) }
    fun passGrammar(id: String) = store.edit { it.copy(grammarPassed = addOnce(it.grammarPassed, id)) }
    fun readStory(id: String) = store.edit { it.copy(storiesRead = addOnce(it.storiesRead, id)) }
    fun wroteChar() = store.edit { it.copy(charsWritten = it.charsWritten + 1) }
    fun seeMilestones(ids: List<String>) = store.edit { s -> s.copy(milestonesSeen = (s.milestonesSeen + ids).distinct()) }
    fun setTripMode(mode: String) = store.edit { it.copy(tripMode = mode) }

    // Fare alerts
    fun saveAlert(a: com.dudlatitat.nextchapter.data.FareAlert) {
        store.edit { s -> s.copy(alerts = if (s.alerts.any { it.id == a.id }) s.alerts.map { if (it.id == a.id) a else it } else s.alerts + a) }
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(getApplication())
    }
    fun deleteAlert(a: com.dudlatitat.nextchapter.data.FareAlert) {
        store.edit { s -> s.copy(alerts = s.alerts.filterNot { it.id == a.id }, fares = s.fares.filterNot { it.alertId == a.id }) }
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(getApplication())
    }

    fun setReminder(on: Boolean, minutes: Int) {
        store.edit { it.copy(reminderOn = on, reminderMinutes = minutes) }
        Reminder.schedule(getApplication(), store.state.value)
    }

    fun restore(text: String): Boolean = store.restore(text).also {
        if (it) Reminder.schedule(getApplication(), store.state.value)
    }

    fun grade(card: Card, grade: Grade) = store.edit { s ->
        s.copy(cards = s.cards.map { if (it.id == card.id) Srs.grade(it, grade) else it }).logDay { it.copy(reviews = it.reviews + 1) }
    }

    /** Adds practice time to today (called when a session ends or closes). */
    fun logSeconds(seconds: Int) {
        if (seconds <= 0) return
        store.edit { s -> s.logDay { it.copy(seconds = it.seconds + seconds.coerceAtMost(3600)) } }
    }

    private fun AppState.logDay(change: (DayLog) -> DayLog): AppState {
        val d = today()
        val kept = days.filterKeys { it > d - 400 }
        return copy(days = kept + (d to change(kept[d] ?: DayLog())))
    }

    /** Remembers one tone attempt, for weak-tone drills and the weekly recap. */
    fun logTone(target: Int, got: Int, spoken: Boolean) {
        if (target !in 1..4 || got !in 1..4) return
        store.edit { s -> s.copy(toneEvents = (s.toneEvents + ToneEvent(today(), target, got, spoken)).takeLast(800)) }
    }

    // Flights
    fun saveTrip(trip: Trip) = store.edit { s ->
        s.copy(trips = (s.trips.filterNot { it.id == trip.id } + trip).sortedBy { it.depart })
    }
    fun deleteTrip(trip: Trip) = store.edit { s -> s.copy(trips = s.trips.filterNot { it.id == trip.id }) }
    fun addPrice(price: Int, source: String) = store.edit { s ->
        s.copy(prices = (s.prices + PriceSeen("price:${System.currentTimeMillis()}", java.time.LocalDate.now().toString(), price, source.trim())).takeLast(300))
    }
    fun deletePrice(p: PriceSeen) = store.edit { s -> s.copy(prices = s.prices.filterNot { it.id == p.id }) }

    // Packing
    fun togglePack(item: PackItem) = store.edit { s -> s.copy(packing = s.packing.map { if (it.id == item.id) it.copy(done = !it.done) else it }) }
    fun addPack(title: String) = store.edit { s -> s.copy(packing = s.packing + PackItem("pack:${System.currentTimeMillis()}", title.trim())) }
    fun deletePack(item: PackItem) = store.edit { s -> s.copy(packing = s.packing.filterNot { it.id == item.id }) }
    fun resetPacking() = store.edit { s -> s.copy(packing = s.packing.map { it.copy(done = false) }) }

    // Money
    fun saveBudget(item: BudgetItem) = store.edit { s ->
        s.copy(budget = if (s.budget.any { it.id == item.id }) s.budget.map { if (it.id == item.id) item else it } else s.budget + item)
    }
    fun deleteBudget(item: BudgetItem) = store.edit { s -> s.copy(budget = s.budget.filterNot { it.id == item.id }) }
    fun setRate(rate: Double, date: String?) = store.edit { it.copy(rate = rate, rateDate = date) }

    /** Fetches today's USD → CNY rate; false without internet. */
    suspend fun refreshRate(): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val c = java.net.URL("https://open.er-api.com/v6/latest/USD").openConnection() as java.net.HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 8000
            val text = c.inputStream.bufferedReader().use { it.readText() }
            val rate = Regex("\"CNY\"\\s*:\\s*([0-9.]+)").find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: return@withContext false
            setRate(rate, java.time.LocalDate.now().toString())
            true
        } catch (_: Exception) { false }
    }

    /** Marks today as a study day (after finishing a session). */
    fun finishedSession() = store.edit { s ->
        val d = today()
        if (d in s.studyDays) s else s.copy(studyDays = (s.studyDays + d).takeLast(800))
    }

    fun addCard(hanzi: String, pinyin: String, english: String) = store.edit { s ->
        val card = Card(id = "mine:${System.currentTimeMillis()}", hanzi = hanzi.trim(), pinyin = pinyin.trim(), english = english.trim(), deck = "mine", custom = true)
        s.copy(cards = s.cards + card)
    }

    fun deleteCard(card: Card) = store.edit { s -> s.copy(cards = s.cards.filterNot { it.id == card.id }) }

    fun toggleTask(task: MoveTask) = store.edit { s ->
        s.copy(tasks = s.tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it })
    }

    fun addTask(title: String, group: String) = store.edit { s ->
        s.copy(tasks = s.tasks + MoveTask(id = "own:${System.currentTimeMillis()}", title = title.trim(), group = group))
    }

    fun deleteTask(task: MoveTask) = store.edit { s -> s.copy(tasks = s.tasks.filterNot { it.id == task.id }) }

    fun updatePlan(change: (AppState) -> AppState) = store.edit(change)

    override fun onCleared() = speaker.close()
}
