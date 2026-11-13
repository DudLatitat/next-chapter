package com.dudlatitat.nextchapter.data

/**
 * Card changes shared by the phone screens (MainViewModel) and the computer page (DesktopService),
 * so both do exactly the same thing. Pure, tested in CardEditsTest.
 */
object CardEdits {
    /** Adds today's numbers to the daily log (keeps the last 400 days). */
    fun AppState.logged(day: Long = today(), change: (DayLog) -> DayLog): AppState {
        val kept = days.filterKeys { it > day - 400 }
        return copy(days = kept + (day to change(kept[day] ?: DayLog())))
    }

    fun grade(s: AppState, id: String, grade: Grade, day: Long = today()): AppState =
        s.copy(cards = s.cards.map { if (it.id == id) Srs.grade(it, grade, day) else it }).logged(day) { it.copy(reviews = it.reviews + 1) }

    fun add(s: AppState, hanzi: String, pinyin: String, english: String, now: Long = System.currentTimeMillis()): AppState {
        val card = Card(id = "mine:$now", hanzi = hanzi.trim(), pinyin = pinyin.trim(), english = english.trim(), deck = "mine", custom = true)
        return s.copy(cards = s.cards + card)
    }

    /** Starter cards keep their characters (their id and voice depend on them); your own can change. */
    fun update(s: AppState, id: String, hanzi: String, pinyin: String, english: String): AppState =
        s.copy(cards = s.cards.map { c ->
            if (c.id != id) c else c.copy(hanzi = if (c.custom && hanzi.isNotBlank()) hanzi.trim() else c.hanzi, pinyin = pinyin.trim(), english = english.trim())
        })

    /**
     * Takes back a grade: the card goes back to exactly how it was ([before]) and today's review
     * count drops by one. Only for the grade just made (Undo right after a mis-tap).
     */
    fun ungrade(s: AppState, before: Card, day: Long = today()): AppState =
        s.copy(cards = s.cards.map { if (it.id == before.id) before else it }).logged(day) { it.copy(reviews = (it.reviews - 1).coerceAtLeast(0)) }

    fun delete(s: AppState, id: String): AppState = s.copy(cards = s.cards.filterNot { it.id == id })
}
