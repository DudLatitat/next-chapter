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

    /** Checks per day: 1 or 2. */
    fun perDay(context: Context): Int = prefs(context).getInt("per_day", 2)
    fun setPerDay(context: Context, n: Int) { prefs(context).edit().putInt("per_day", n.coerceIn(1, 2)).apply(); schedule(context, replace = true) }

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
        val hours = if (perDay(context) >= 2) 12L else 24L
        val req = PeriodicWorkRequestBuilder<FareWorker>(hours, TimeUnit.HOURS)
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

    /** How many days it takes to check every pair once, at the current pace. */
    fun daysToCover(context: Context, a: FareAlert): Int {
        val n = pairs(a).size
        return if (n == 0) 0 else (n + perDay(context) - 1) / perDay(context)
    }

    /**
     * Runs one check for each enabled alert (one search each). Returns a short status for
     * "Check now", or null if there was nothing to do.
     */
    suspend fun checkAll(context: Context): String? = withContext(Dispatchers.IO) {
        val key = key(context) ?: return@withContext "Add your SerpApi key first"
        val store = Store.get(context)
        val alerts = store.state.value.alerts.filter { it.enabled }
        if (alerts.isEmpty()) return@withContext null
        val notes = mutableListOf<String>()
        for (a in alerts) {
            val pairs = pairs(a)
            if (pairs.isEmpty()) { notes += "Window has passed"; continue }
            val (out, back) = pairs[a.cursor % pairs.size]
            val result = try { search(key, a, out, back) } catch (e: Exception) { notes += "Couldn't reach SerpApi (${e.message?.take(60)})"; null }
            countSearch(context)
            val found = result?.let { bestFit(a, out, back, it) }
            store.edit { s ->
                val updated = s.alerts.map {
                    if (it.id != a.id) it
                    else it.copy(
                        cursor = (a.cursor + 1) % pairs.size,
                        lastChecked = System.currentTimeMillis(),
                        bestPrice = if (found != null && found.fits && (a.bestPrice == 0 || found.price < a.bestPrice)) found.price else a.bestPrice,
                    )
                }
                val fares = if (found != null) (s.fares + found).takeLast(200) else s.fares
                // Every fare seen also goes into the price tracker's history.
                val prices = if (found != null) (s.prices + PriceSeen("price:${System.currentTimeMillis()}", LocalDate.now().toString(), found.price, "auto · ${short(out)}–${short(back)}")).takeLast(300) else s.prices
                s.copy(alerts = updated, fares = fares, prices = prices)
            }
            when {
                found == null -> if (result != null) notes += "No flights found for ${short(out)}–${short(back)}"
                found.fits -> {
                    notes += "✓ ${money(found.price)} for ${short(out)}–${short(back)}"
                    if (a.bestPrice == 0 || found.price < a.bestPrice) notify(context, a, found)
                }
                else -> notes += "${money(found.price)} for ${short(out)}–${short(back)} (doesn't fit yet)"
            }
        }
        notes.joinToString("\n")
    }

    private fun short(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("MMM d"))
    private fun money(n: Int) = "$" + "%,d".format(n)

    private data class Option(val price: Int, val stops: Int, val minutes: Int, val airline: String)

    private fun search(key: String, a: FareAlert, out: LocalDate, back: LocalDate): List<Option> {
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
        return listOf("best_flights", "other_flights").flatMap { k ->
            json[k]?.jsonArray.orEmpty().mapNotNull { el ->
                val o = el.jsonObject
                val price = o["price"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                val legs = o["flights"]?.jsonArray.orEmpty()
                Option(
                    price = price,
                    stops = (legs.size - 1).coerceAtLeast(0),
                    minutes = o["total_duration"]?.jsonPrimitive?.intOrNull ?: 0,
                    airline = legs.mapNotNull { (it as? JsonObject)?.get("airline")?.jsonPrimitive?.content }.distinct().joinToString(" / "),
                )
            }
        }
    }

    /** The cheapest option that fits the alert, or the cheapest overall (marked as not fitting). */
    private fun bestFit(a: FareAlert, out: LocalDate, back: LocalDate, options: List<Option>): FareFound? {
        if (options.isEmpty()) return null
        fun fits(o: Option) = o.price <= a.maxPrice &&
            (a.maxStops < 0 || o.stops <= a.maxStops) &&
            (a.maxHours <= 0 || o.minutes == 0 || o.minutes <= a.maxHours * 60)
        val fit = options.filter(::fits).minByOrNull { it.price }
        val pick = fit ?: options.minBy { it.price }
        return FareFound(a.id, System.currentTimeMillis(), out.toString(), back.toString(), pick.price, pick.stops, pick.minutes, pick.airline, fits = fit != null)
    }

    private fun notify(context: Context, a: FareAlert, f: FareFound) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Fare alerts", NotificationManager.IMPORTANCE_HIGH))
        val out = LocalDate.parse(f.depart); val back = LocalDate.parse(f.returnDate)
        val q = "Flights from ${a.from.substringBefore(',')} to ${a.to.replace(",", " or ")} on ${f.depart} through ${f.returnDate}"
        val search = PendingIntent.getActivity(
            context, 10, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/travel/flights?q=" + URLEncoder.encode(q, "UTF-8"))),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val open = PendingIntent.getActivity(context, 11, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val hours = if (f.minutes > 0) " · ${f.minutes / 60}h${(f.minutes % 60).toString().padStart(2, '0')}" else ""
        val stops = if (f.stops == 0) "nonstop" else "${f.stops} stop${if (f.stops > 1) "s" else ""}"
        val note = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("✈ ${money(f.price)} to Chengdu · ${short(out)}–${short(back)}")
            .setContentText("$stops$hours · ${f.airline}".trim())
            .setContentIntent(open)
            .addAction(0, "Search & book", search)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(("fare" + a.id).hashCode(), note)
    }
}

class FareWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        FareWatch.checkAll(applicationContext)
        return Result.success()
    }
}

