package com.dudlatitat.nextchapter.data

import kotlin.math.max
import kotlin.math.roundToInt

/** How well you remembered a card. */
enum class Grade(val label: String) { AGAIN("Again"), HARD("Hard"), GOOD("Good"), EASY("Easy") }

/**
 * A small SM-2 style scheduler: each good answer stretches the gap before the card comes back,
 * a miss brings it back today and makes it come back sooner from then on.
 */
object Srs {
    fun grade(card: Card, grade: Grade, day: Long = today()): Card = gradeInner(card, grade, day).copy(last = day)

    private fun gradeInner(card: Card, grade: Grade, day: Long): Card {
        val first = card.firstSeen ?: day
        return when (grade) {
            Grade.AGAIN -> card.copy(
                firstSeen = first, interval = 0, due = day,
                ease = max(1.3, card.ease - 0.2), lapses = card.lapses + 1,
            )
            Grade.HARD -> {
                val next = max(1, (card.interval * 1.2).roundToInt())
                card.copy(firstSeen = first, interval = next, due = day + next, ease = max(1.3, card.ease - 0.15), reps = card.reps + 1)
            }
            Grade.GOOD -> {
                val next = goodInterval(card)
                card.copy(firstSeen = first, interval = next, due = day + next, reps = card.reps + 1)
            }
            Grade.EASY -> {
                val next = (goodInterval(card) * 1.3).roundToInt() + 1
                card.copy(firstSeen = first, interval = next, due = day + next, ease = card.ease + 0.15, reps = card.reps + 1)
            }
        }
    }

    private fun goodInterval(card: Card) = when {
        card.reps == 0 || card.interval == 0 -> 1
        card.reps == 1 -> 3
        else -> max(card.interval + 1, (card.interval * card.ease).roundToInt())
    }

    /** What to show today: reviews that are due, then today's share of new cards. */
    fun queue(state: AppState, day: Long = today()): List<Card> {
        val due = state.cards.filter { !it.isNew && it.due <= day }.sortedBy { it.due }
        val introducedToday = state.cards.count { it.firstSeen == day }
        val room = max(0, state.newPerDay - introducedToday)
        val newCards = state.cards.filter { it.isNew }
        val mine = newCards.filter { it.custom }
        val words = newCards.filter { !it.custom && it.deck == "core" }
        val phrases = newCards.filter { !it.custom && it.deck != "core" }.sortedBy { deckOrder(it.deck) }
        // Your own phrases first, then phrases and core words taking turns.
        val mixed = buildList {
            addAll(mine)
            val n = maxOf(words.size, phrases.size)
            for (i in 0 until n) { phrases.getOrNull(i)?.let(::add); words.getOrNull(i)?.let(::add) }
        }
        val fresh = mixed.take(room)
        return due + fresh
    }

    private fun deckOrder(deck: String) = Decks.indexOfFirst { it.id == deck }.let { if (it < 0) 99 else it }

    /** Days in a row (ending today or yesterday) you studied. */
    fun streak(days: List<Long>, day: Long = today()): Int {
        val set = days.toSet()
        var d = if (day in set) day else day - 1
        var n = 0
        while (d in set) { n++; d-- }
        return n
    }
}
