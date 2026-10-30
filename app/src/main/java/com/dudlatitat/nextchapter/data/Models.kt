package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

/** Today as a day number (days since 1970), the unit every schedule in the app counts in. */
fun today(): Long = LocalDate.now().toEpochDay()

/** One phrase to learn, and where it is in its review schedule. */
@Serializable
data class Card(
    val id: String,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val deck: String,
    val custom: Boolean = false,
    /** The day it was first studied; null while it's still new. */
    val firstSeen: Long? = null,
    /** The day it's next due. */
    val due: Long = 0,
    /** Days between reviews right now. */
    val interval: Int = 0,
    val ease: Double = 2.5,
    val reps: Int = 0,
    val lapses: Int = 0,
) {
    val isNew get() = firstSeen == null
    /** Remembered for a week or more. */
    val isKnown get() = interval >= 7
}

/** A group of phrases for one situation. */
data class Deck(val id: String, val title: String, val hanzi: String, val blurb: String)

val Decks = listOf(
    Deck("yuling", "With Yuling", "我们", "Sweet and everyday lines for you two"),
    Deck("family", "Her family", "家人", "Meeting and visiting her mom and relatives"),
    Deck("daily", "Everyday", "日常", "Getting by, asking for help"),
    Deck("food", "Eating out", "吃饭", "Ordering in Sichuan, spice levels"),
    Deck("shopping", "Shopping & paying", "购物", "Prices, bargaining, scan-to-pay"),
    Deck("transport", "Getting around", "出行", "Taxis, trains, driving"),
    Deck("home", "Home & renting", "住房", "Finding and running a place"),
    Deck("paperwork", "Paperwork", "手续", "Visa, bank, registration"),
    Deck("health", "Health", "看病", "When you're not feeling well"),
    Deck("sichuan", "Sichuanese", "四川话", "Local words you'll hear in Neijiang"),
    Deck("mine", "My phrases", "我的", "Things you added yourself"),
)

@Serializable
data class MoveTask(
    val id: String,
    val title: String,
    val group: String,
    val done: Boolean = false,
)

val TaskGroups = listOf("Documents", "Money", "School & certs", "Life in China")

@Serializable
data class AppState(
    val cards: List<Card> = emptyList(),
    val tasks: List<MoveTask> = emptyList(),
    /** Target move date and next Chengdu trip, as ISO dates (2028-06-01). */
    val moveDate: String? = null,
    val tripDate: String? = null,
    val savingsGoal: Int = 0,
    val saved: Int = 0,
    /** The round-trip LAX ⇄ CTU price you're waiting for. */
    val flightTarget: Int = 0,
    /** New phrases introduced per day. */
    val newPerDay: Int = 8,
    /** Days you finished a review session. */
    val studyDays: List<Long> = emptyList(),
    val seeded: Int = 0,
    /** Daily practice reminder, and when (minutes after midnight). */
    val reminderOn: Boolean = false,
    val reminderMinutes: Int = 7 * 60 + 30,
    /** Shown big in the phrasebook: your address there, and an allergy, both in Chinese. */
    val addressZh: String = "",
    val allergyZh: String = "",
)
