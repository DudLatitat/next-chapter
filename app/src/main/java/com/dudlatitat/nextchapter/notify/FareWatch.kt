package com.dudlatitat.nextchapter.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.data.FareAlert
import com.dudlatitat.nextchapter.data.FareFound
import com.dudlatitat.nextchapter.data.PriceSeen
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.parseDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Fare alerts: checks Google Flights prices through SerpApi (free plan: a limited number of
 * searches a month), one date pair per alert per check, and notifies when a fare fits.
 * The API key lives only on this phone.
 */
object FareWatch {
    private const val CHANNEL = "fares"
    private const val WORK = "fare-watch"

    private fun prefs(context: Context) = context.getSharedPreferences("fares", Context.MODE_PRIVATE)

    fun key(context: Context): String? = prefs(context).getString("serpapi_key", null)?.takeIf { it.isNotBlank() }
    fun setKey(context: Context, key: String) { prefs(context).edit().putString("serpapi_key", key.trim()).apply(); schedule(context) }

    const val MONTHLY_FREE = 250
    /** Kept back each month for tapping "Check now". */
    private const val RESERVE = 30

    /** Searches per day, spread over two runs (morning and evening). */
    fun perDay(context: Context): Int = prefs(context).getInt("searches_per_day", 6)
    fun setPerDay(context: Context, n: Int) { prefs(context).edit().putInt("searches_per_day", n.coerceIn(2, 8)).apply(); schedule(context, replace = true) }

    /**
     * Searches to spend on this scheduled run: half the daily amount, slowed down if the month's
     * free searches would otherwise run out before the month ends.
     */
    fun runBudget(context: Context): Int {
        val now = YearMonth.now()
        val daysLeft = now.lengthOfMonth() - LocalDate.now().dayOfMonth + 1
        val left = MONTHLY_FREE - RESERVE - usedThisMonth(context)
        if (left <= 0) return 0
        val affordablePerDay = left / daysLeft.coerceAtLeast(1)
        val perRun = (minOf(perDay(context), affordablePerDay) + 1) / 2
        return perRun.coerceAtLeast(if (left >= 2) 1 else 0)
    }

    /** Searches used this month (the free plan has a monthly cap). */
    fun usedThisMonth(context: Context): Int {
        val p = prefs(context)
        return if (p.getString("month", "") == YearMonth.now().toString()) p.getInt("used", 0) else 0
    }
    private fun countSearch(context: Context) {
        val p = prefs(context)
        val month = YearMonth.now().toString()
        val used = if (p.getString("month", "") == month) p.getInt("used", 0) else 0
        p.edit().putString("month", month).putInt("used", used + 1).apply()
    }

    fun schedule(context: Context, replace: Boolean = false) {
        val wm = WorkManager.getInstance(context)
        val active = key(context) != null && Store.get(context).state.value.alerts.any { it.enabled }
        if (!active) { wm.cancelUniqueWork(WORK); return }
        val req = PeriodicWorkRequestBuilder<FareWorker>(12L, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        wm.enqueueUniquePeriodicWork(WORK, if (replace) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP, req)
    }

    /** Every (leave, return) pair the alert covers, soonest first. */
    fun pairs(a: FareAlert): List<Pair<LocalDate, LocalDate>> {
        val start = parseDate(a.departStart) ?: return emptyList()
        val end = parseDate(a.departEnd) ?: return emptyList()
        val first = maxOf(start, LocalDate.now().plusDays(1))
        val out = mutableListOf<Pair<LocalDate, LocalDate>>()
        var d = first
        while (!d.isAfter(end) && out.size < 400) {
            // Stay lengths: the shortest, the longest and every 3 days between, to spread searches.
            val lengths = (a.minDays..maxOf(a.minDays, a.maxDays) step 3).toMutableSet().apply { add(maxOf(a.minDays, a.maxDays)) }
            lengths.sorted().forEach { out += d to d.plusDays(it.toLong()) }
            d = d.plusDays(1)
        }
        return out
    }

    /** How many days it takes to check every pair once, at the current pace (searches shared between alerts). */
    fun daysToCover(context: Context, a: FareAlert): Int {
        val n = pairs(a).size
        val alerts = Store.get(context).state.value.alerts.count { it.enabled }.coerceAtLeast(1)
        val perAlertDay = (perDay(context).toFloat() / alerts).coerceAtLeast(0.5f)
        return if (n == 0) 0 else kotlin.math.ceil(n / perAlertDay).toInt()
    }

    /**
     * Which date pairs to check next. First every pair gets one look (in order); after that,
     * every other search goes back to the cheapest pairs seen so far, since that's where a
     * price drop under your limit is most likely, and the rest keep sweeping the window.
     */
    fun nextPairs(context: Context, a: FareAlert, count: Int): List<Pair<LocalDate, LocalDate>> {
        val all = pairs(a)
        if (all.isEmpty()) return emptyList()
        val seen = Store.get(context).state.value.fares.filter { it.alertId == a.id }
        val hot = seen.groupBy { it.depart to it.returnDate }
            .mapNotNull { (k, v) -> parseDate(k.first)?.let { d -> parseDate(k.second)?.let { r -> Triple(d to r, v.minOf { it.price }, v.maxOf { it.checkedAt }) } } }
            .filter { it.first in all }
            .sortedBy { it.second }.take(3)
            .sortedBy { it.third } // least recently checked of the cheapest three
            .map { it.first }
        val out = mutableListOf<Pair<LocalDate, LocalDate>>()
        var c = a.cursor
        for (i in 0 until count) {
            val useHot = a.passes >= 1 && i % 2 == 1 && hot.isNotEmpty()
            val p = if (useHot) hot[(i / 2) % hot.size] else all[c++ % all.size]
            if (p !in out) out += p
        }
        return out
    }

    private val lock = kotlinx.coroutines.sync.Mutex()

    /** When the next scheduled check runs (null if none is scheduled). */
    fun nextRun(context: Context): Long? = try {
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK).get()
            .firstOrNull { !it.state.isFinished }?.nextScheduleTimeMillis?.takeIf { it > 0 && it < Long.MAX_VALUE }
    } catch (_: Exception) { null }

    private fun log(context: Context, alertId: String?, kind: String, text: String) {
        Store.get(context).edit { s -> s.copy(fareLog = (s.fareLog + com.dudlatitat.nextchapter.data.FareLog(System.currentTimeMillis(), alertId, kind, text)).takeLast(300)) }
    }

    /**
     * Runs a check: [searches] spread over the enabled alerts (scheduled), or one per alert
     * (manual, searches = −1). Returns a short summary for "Check now".
     */
    suspend fun checkAll(context: Context, searches: Int = -1): String? = lock.withLock {
        withContext(Dispatchers.IO) { runChecks(context, searches) }
    }

    private fun runChecks(context: Context, searches: Int): String? {
        val trigger = if (searches < 0) "manual" else "scheduled"
        val key = key(context) ?: return "Add your SerpApi key first"
        val store = Store.get(context)
        val alerts = store.state.value.alerts.filter { it.enabled }
        if (alerts.isEmpty()) return null
        val budget = if (searches < 0) alerts.size else searches
        if (budget == 0) {
            log(context, null, "skip", "Skipped a $trigger check: this month's free searches are used up (${usedThisMonth(context)}/$MONTHLY_FREE, 30 kept for Check now)")
            return "This month's free searches are used up; checks resume next month"
        }
        log(context, null, "run", "${trigger.replaceFirstChar { it.uppercase() }} check started: $budget search${if (budget > 1) "es" else ""} for ${alerts.size} alert${if (alerts.size > 1) "s" else ""} · ${usedThisMonth(context)}/$MONTHLY_FREE used this month")
        val notes = mutableListOf<String>()
        val shares = alerts.indices.map { i -> budget / alerts.size + if (i < budget % alerts.size) 1 else 0 }
        alerts@ for ((ai, a0) in alerts.withIndex()) {
            val first = store.state.value.alerts.firstOrNull { it.id == a0.id } ?: continue
            val pairs = pairs(first)
            if (pairs.isEmpty()) { notes += "Window has passed"; log(context, a0.id, "skip", "Departure window has passed; nothing to check"); continue }
            for ((out, back) in nextPairs(context, first, shares[ai])) {
                val a = store.state.value.alerts.firstOrNull { it.id == a0.id } ?: break
                val fromSweep = pairs[a.cursor % pairs.size] == (out to back)
                val why = if (fromSweep) "sweep ${a.cursor % pairs.size + 1}/${pairs.size}" else "re-check of a cheap pair"
                val result = try { search(key, a, out, back) } catch (e: Exception) {
                    val msg = e.message?.take(120) ?: e.javaClass.simpleName
                    notes += "Couldn't reach SerpApi ($msg)"
                    log(context, a.id, "error", "${short(out)}–${short(back)}: $msg. Stopped this run (failed searches aren't charged).")
                    break@alerts
                }
                countSearch(context)
                val found = evaluate(a, out, back, result, trigger)
                val isNewBest = found != null && found.fits && (a.bestPrice == 0 || found.price < a.bestPrice)
                store.edit { s ->
                    val updated = s.alerts.map {
                        if (it.id != a.id) it
                        else it.copy(
                            cursor = if (fromSweep) (a.cursor + 1) % pairs.size else a.cursor,
                            passes = if (fromSweep && a.cursor + 1 >= pairs.size) a.passes + 1 else a.passes,
                            lastChecked = System.currentTimeMillis(),
                            bestPrice = if (isNewBest) found!!.price else a.bestPrice,
                        )
                    }
                    val fares = if (found != null) (s.fares + found.copy(notified = isNewBest)).takeLast(200) else s.fares
                    val prices = if (found != null) (s.prices + PriceSeen("price:${System.currentTimeMillis()}", LocalDate.now().toString(), found.price, "auto · ${short(out)}–${short(back)}")).takeLast(300) else s.prices
                    s.copy(alerts = updated, fares = fares, prices = prices)
                }
                val line = when {
                    found == null -> "${short(out)}–${short(back)} ($why): no flights returned"
                    found.fits -> "${short(out)}–${short(back)} ($why): ✓ ${money(found.price)} fits · ${result.options.size} options" + if (isNewBest) " · new best, notified you" else " · not cheaper than the best already found (${money(a.bestPrice)}), no notification"
                    else -> "${short(out)}–${short(back)} ($why): cheapest ${money(found.price)}, ${result.options.size} options, none fit (" + reasons(a, result.options) + ")"
                }
                log(context, a.id, if (found?.fits == true) "fit" else "search", line)
                notes += line.substringBefore(" (") + ": " + (found?.let { money(it.price) + if (it.fits) " ✓" else "" } ?: "none")
                if (isNewBest) notify(context, a, found!!)
            }
        }
        return notes.joinToString("\n")
    }

    /** Why nothing fit, e.g. "all over $800" or "cheaper ones had 2 stops". */
    private fun reasons(a: FareAlert, options: List<Opt>): String {
        val r = mutableListOf<String>()
        if (options.all { it.price > a.maxPrice }) r += "all over ${money(a.maxPrice)}"
        else {
            val cheap = options.filter { it.price <= a.maxPrice }
            if (a.maxStops >= 0 && cheap.all { it.stops > a.maxStops }) r += "the ones under ${money(a.maxPrice)} have too many stops"
            if (a.maxHours > 0 && cheap.all { it.minutes > a.maxHours * 60 }) r += "the ones under ${money(a.maxPrice)} take over ${a.maxHours}h"
        }
        return r.joinToString("; ").ifBlank { "outside your limits" }
    }

    private fun short(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("MMM d"))
    private fun money(n: Int) = "$" + "%,d".format(n)

    private data class Opt(val price: Int, val stops: Int, val minutes: Int, val airline: String, val legs: List<String>, val layovers: List<String>)
    private data class SearchResult(val options: List<Opt>, val url: String)

    private fun search(key: String, a: FareAlert, out: LocalDate, back: LocalDate): SearchResult {
        fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
        val params = buildMap {
            put("engine", "google_flights")
            put("departure_id", a.from)
            put("arrival_id", a.to)
            put("outbound_date", out.toString())
            put("return_date", back.toString())
            put("type", "1")
            put("currency", "USD")
            put("hl", "en")
            put("gl", "us")
            // 0 any, 1 nonstop, 2 one stop or fewer, 3 two or fewer.
            if (a.maxStops >= 0) put("stops", (a.maxStops + 1).coerceAtMost(3).toString())
            put("api_key", key)
        }
        val url = "https://serpapi.com/search.json?" + params.entries.joinToString("&") { "${it.key}=${enc(it.value)}" }
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 20_000; c.readTimeout = 60_000
        val code = c.responseCode
        val body = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
        val json = Json.parseToJsonElement(body).jsonObject
        json["error"]?.jsonPrimitive?.content?.let { if (!it.contains("hasn't returned any results", true)) throw IllegalStateException(it) }
        val page = (json["search_metadata"] as? JsonObject)?.get("google_flights_url")?.jsonPrimitive?.content ?: ""
        fun time(t: String?) = t?.substringAfter(' ', t) ?: ""
        val options = listOf("best_flights", "other_flights").flatMap { k ->
            json[k]?.jsonArray.orEmpty().mapNotNull { el ->
                val o = el.jsonObject
                val price = o["price"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                val legs = o["flights"]?.jsonArray.orEmpty().map { it.jsonObject }
                fun JsonObject.str(k: String) = this[k]?.jsonPrimitive?.content
                fun JsonObject.obj(k: String) = this[k] as? JsonObject
                Opt(
                    price = price,
                    stops = (legs.size - 1).coerceAtLeast(0),
                    minutes = o["total_duration"]?.jsonPrimitive?.intOrNull ?: 0,
                    airline = legs.mapNotNull { it.str("airline") }.distinct().joinToString(" / "),
                    legs = legs.map { l ->
                        val dep = l.obj("departure_airport"); val arr = l.obj("arrival_airport")
                        "${l.str("flight_number") ?: l.str("airline") ?: ""} · ${dep?.str("id") ?: ""} ${time(dep?.str("time"))} → ${arr?.str("id") ?: ""} ${time(arr?.str("time"))}"
                    },
                    layovers = o["layovers"]?.jsonArray.orEmpty().map { it.jsonObject }.map { l ->
                        val m = l["duration"]?.jsonPrimitive?.intOrNull ?: 0
                        "${m / 60}h ${m % 60}m in ${l.str("id") ?: l.str("name") ?: "?"}" + if (l["overnight"]?.jsonPrimitive?.content == "true") " (overnight)" else ""
                    },
                )
            }
        }.sortedBy { it.price }
        return SearchResult(options, page)
    }

    /** The cheapest option that fits the alert, or the cheapest overall (marked as not fitting). */
    private fun evaluate(a: FareAlert, out: LocalDate, back: LocalDate, r: SearchResult, trigger: String): FareFound? {
        if (r.options.isEmpty()) return null
        fun fits(o: Opt) = o.price <= a.maxPrice &&
            (a.maxStops < 0 || o.stops <= a.maxStops) &&
            (a.maxHours <= 0 || o.minutes == 0 || o.minutes <= a.maxHours * 60)
        val fit = r.options.filter(::fits).minByOrNull { it.price }
        val pick = fit ?: r.options.first()
        val kept = (listOfNotNull(fit) + r.options).distinct().take(6).map {
            com.dudlatitat.nextchapter.data.FareOption(it.price, it.stops, it.minutes, it.airline, it.legs, it.layovers, fits(it))
        }
        return FareFound(
            a.id, System.currentTimeMillis(), out.toString(), back.toString(), pick.price, pick.stops, pick.minutes, pick.airline,
            fits = fit != null, url = r.url.ifBlank { googleFlightsUrl(a, out.toString(), back.toString()) }, options = kept, trigger = trigger,
        )
    }

    fun googleFlightsUrl(a: FareAlert, depart: String, ret: String): String {
        val q = "Flights from ${a.from.substringBefore(',')} to ${a.to.replace(",", " or ")} on $depart through $ret"
        return "https://www.google.com/travel/flights?q=" + URLEncoder.encode(q, "UTF-8")
    }

    private fun notify(context: Context, a: FareAlert, f: FareFound) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Fare alerts", NotificationManager.IMPORTANCE_HIGH))
        val out = LocalDate.parse(f.depart); val back = LocalDate.parse(f.returnDate)
        val search = PendingIntent.getActivity(
            context, 10, Intent(Intent.ACTION_VIEW, Uri.parse(f.url.ifBlank { googleFlightsUrl(a, f.depart, f.returnDate) })),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val open = PendingIntent.getActivity(context, 11, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val hours = if (f.minutes > 0) " · ${f.minutes / 60}h${(f.minutes % 60).toString().padStart(2, '0')}" else ""
        val stops = if (f.stops == 0) "nonstop" else "${f.stops} stop${if (f.stops > 1) "s" else ""}"
        val legs = f.options.firstOrNull { it.fits }?.legs.orEmpty().joinToString("\n")
        val note = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("✈ ${money(f.price)} to Chengdu · ${short(out)}–${short(back)}")
            .setContentText("$stops$hours · ${f.airline}".trim())
            .setStyle(NotificationCompat.BigTextStyle().bigText("$stops$hours · ${f.airline}\n$legs".trim()))
            .setContentIntent(open)
            .addAction(0, "Open in Google Flights", search)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(("fare" + a.id).hashCode(), note)
    }
}

class FareWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        FareWatch.checkAll(applicationContext, FareWatch.runBudget(applicationContext))
        return Result.success()
    }
}
