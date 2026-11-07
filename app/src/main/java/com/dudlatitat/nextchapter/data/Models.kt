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
    Deck("idv", "Identity V", "第五人格", "Cryptic Notes callouts for playing with Yuling"),
    Deck("games", "Gaming together", "一起玩", "Teaming up, cheering each other on, calling it a night"),
    Deck("sweet", "Sweet talk", "甜言蜜语", "Compliments and flirting, said like you mean it"),
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
    /** Days of the week you're willing to leave (1 = Monday … 7 = Sunday); empty = any day. */
    val departDays: List<Int> = emptyList(),
    /** Days of the week you're willing to fly home; empty = any day. */
    val returnDays: List<Int> = emptyList(),
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
    /** Full passes through every date pair so far. */
    val passes: Int = 0,
    val lastChecked: Long = 0,
    /** Cheapest fitting fare found so far, for not repeating the same alert. */
    val bestPrice: Int = 0,
)

/** One itinerary from a search: the outbound flights, with the round-trip price. */
@Serializable
data class FareOption(
    val price: Int,
    val stops: Int,
    val minutes: Int,
    val airline: String,
    /** One line per flight: "UA 2119 · LAX 11:40 → SFO 13:05". */
    val legs: List<String> = emptyList(),
    /** "2h 10m in SFO", one per layover. */
    val layovers: List<String> = emptyList(),
    val fits: Boolean = false,
)

/** Something the background checker did, for the activity log. */
@Serializable
data class FareLog(val time: Long, val alertId: String?, val kind: String, val text: String)

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
    /** The Google Flights results page for this exact search. */
    val url: String = "",
    /** Cheapest few itineraries the search returned. */
    val options: List<FareOption> = emptyList(),
    val trigger: String = "",
    val notified: Boolean = false,
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
    val fareLog: List<FareLog> = emptyList(),
    /** Trip mode: "auto" (on during a planned trip), "on" or "off". */
    val tripMode: String = "auto",
    val milestonesSeen: List<String> = emptyList(),
    val dialoguesDone: List<String> = emptyList(),
    val grammarPassed: List<String> = emptyList(),
    val storiesRead: List<String> = emptyList(),
    val charsWritten: Int = 0,
    val driveMinutes: Int = 0,
    /** Days the app was opened before 7 am (the early-start streak). */
    val wakeDays: List<Long> = emptyList(),
    /** Days the morning prayer was said ("Amen" tapped). */
    val prayedDays: List<Long> = emptyList(),
    /** One goal per evening (Life.withGoal keeps the last 120 days). */
    val goals: List<DayGoal> = emptyList(),
    /** The Sunday review's one line, by the week's Monday (ISO date). */
    val weekNotes: Map<String, String> = emptyMap(),
    /** The 8:30 pm "did you get to it?" notification. */
    val eveningCheck: Boolean = true,
    // ---- Life tab ----
    /** A+ review progress (card text lives in APlus.kt), new cards per day, exam date (ISO). */
    val aplus: List<Review> = emptyList(),
    val aplusPerDay: Int = 10,
    val aplusExam: String? = null,
    val certs: List<CertStep> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val jobs: List<JobPosting> = emptyList(),
    /** Job deadline reminders (3 days and 1 day before), at lunchtime. */
    val jobReminders: Boolean = true,
    val people: List<Person> = emptyList(),
    val notes: List<PersonNote> = emptyList(),
    // ---- Study timer and practice tests (1.40) ----
    val study: List<StudySession> = emptyList(),
    /** A running timer: when it started (epoch ms) and for what; null when stopped. */
    val timerStart: Long? = null,
    val timerSubject: String? = null,
    val tests: List<TestResult> = emptyList(),
)
