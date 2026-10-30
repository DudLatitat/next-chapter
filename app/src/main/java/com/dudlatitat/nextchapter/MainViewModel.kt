package com.dudlatitat.nextchapter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dudlatitat.nextchapter.data.AppState
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

    init {
        store.onChange = { PhraseWidget.refresh(app, it) }
        PhraseWidget.refresh(app, store.state.value)
        Reminder.schedule(app, store.state.value)
    }

    fun setReminder(on: Boolean, minutes: Int) {
        store.edit { it.copy(reminderOn = on, reminderMinutes = minutes) }
        Reminder.schedule(getApplication(), store.state.value)
    }

    fun restore(text: String): Boolean = store.restore(text).also {
        if (it) Reminder.schedule(getApplication(), store.state.value)
    }

    fun grade(card: Card, grade: Grade) = store.edit { s ->
        s.copy(cards = s.cards.map { if (it.id == card.id) Srs.grade(it, grade) else it })
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
