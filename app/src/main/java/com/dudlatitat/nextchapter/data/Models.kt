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
    /** The day it was last answered. */
    val last: Long? = null,
) {
    val isNew get() = firstSeen == null
    /** Remembered for a week or more. */
    val isKnown get() = interval >= 7
}

/** A group of phrases for one situation. */
data class Deck(val id: String, val title: String, val hanzi: String, val blurb: String)

val Decks = listOf(
    Deck("yuling", "With Yuling", "我们", "Sweet and everyday lines for you two"),
    Deck("core", "Core words", "基础", "The 300 most useful single words"),
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

/** A trip to China: dates, flights, what it cost. */
@Serializable
data class Trip(
    val id: String,
    val depart: String,
    val returnDate: String? = null,
    val airline: String = "",
    val flights: String = "",
    val price: Int = 0,
    val booked: Boolean = false,
    val confirmation: String = "",
    val notes: String = "",
)

/** Watch fares for a date window and get a notification when one fits. */
@Serializable
data class FareAlert(
    val id: String,
    val from: String = "LAX",
    val to: String = "CTU,TFU",
    /** Earliest and latest day to leave (ISO). */
    val departStart: String,
    val departEnd: String,
    /** How long to stay, in days. */
    val minDays: Int = 12,
    val maxDays: Int = 18,
    val maxPrice: Int = 750,
    /** Most stops allowed each way (0 = nonstop only); −1 for any. */
    val maxStops: Int = 1,
    /** Longest acceptable travel time one way, in hours; 0 for any. */
    val maxHours: Int = 0,
    val enabled: Boolean = true,
    /** Which date pair the next check looks at (they're checked in turn). */
    val cursor: Int = 0,
    val lastChecked: Long = 0,
    /** Cheapest fitting fare found so far, for not repeating the same alert. */
    val bestPrice: Int = 0,
)

/** A fare a check found. */
@Serializable
data class FareFound(
    val alertId: String,
    val checkedAt: Long,
    val depart: String,
    val returnDate: String,
    val price: Int,
    val stops: Int,
    val minutes: Int,
    val airline: String,
    val fits: Boolean,
)

/** A fare you saw, for tracking prices before booking. */
@Serializable
data class PriceSeen(val id: String, val day: String, val price: Int, val source: String = "")

@Serializable
data class PackItem(val id: String, val title: String, val done: Boolean = false)

/** One line of the monthly budget for life there, in yuan. */
@Serializable
data class BudgetItem(val id: String, val name: String, val zh: String = "", val rmb: Int = 0)

/** One tone attempt: the tone it should have been, and what you heard or said. */
@Serializable
data class ToneEvent(val day: Long, val target: Int, val got: Int, val spoken: Boolean = false)

/** Practice done on one day. */
@Serializable
data class DayLog(val reviews: Int = 0, val seconds: Int = 0)

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
    val trips: List<Trip> = emptyList(),
    val prices: List<PriceSeen> = emptyList(),
    val packing: List<PackItem> = emptyList(),
    val budget: List<BudgetItem> = emptyList(),
    /** Yuan per US dollar, and when it was last updated (ISO date). */
    val rate: Double = 6.75,
    val rateDate: String? = null,
    val toneEvents: List<ToneEvent> = emptyList(),
    /** Practice per day, keyed by day number. */
    val days: Map<Long, DayLog> = emptyMap(),
    val alerts: List<FareAlert> = emptyList(),
    val fares: List<FareFound> = emptyList(),
)
