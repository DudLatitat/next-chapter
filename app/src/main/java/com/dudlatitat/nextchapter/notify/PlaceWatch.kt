package com.dudlatitat.nextchapter.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Places
import com.dudlatitat.nextchapter.data.Spot
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import java.time.LocalTime

/**
 * Work & home arrivals, with Android's own location system (LocationManager proximity alerts).
 * No Google Play Services, so it works on any phone and in China, offline (GPS needs no internet).
 * The two spots are saved in the app's data on this phone only. Android remembers the alerts
 * until the phone restarts, so they're set again at boot and whenever the app opens.
 */
object PlaceWatch {
    fun hasForeground(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Arrivals are noticed with the app closed only with location "Allow all the time". */
    fun hasBackground(context: Context) = Build.VERSION.SDK_INT < 29 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun pending(context: Context, kind: String) = PendingIntent.getBroadcast(
        context, if (kind == "work") 20 else 21,
        Intent(context, PlaceReceiver::class.java).putExtra("kind", kind),
        // Mutable: Android adds "entering or leaving" to the intent when it fires.
        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0),
    )

    /** Sets (or clears) the two watched circles to match Settings. */
    @SuppressLint("MissingPermission")
    fun register(context: Context, state: AppState) {
        val lm = context.getSystemService(LocationManager::class.java) ?: return
        for ((kind, spot) in listOf("work" to state.workSpot, "home" to state.homeSpot)) {
            val p = pending(context, kind)
            try { lm.removeProximityAlert(p) } catch (_: Exception) {}
            if (state.placesOn && spot != null && hasForeground(context)) {
                try { lm.addProximityAlert(spot.lat, spot.lon, spot.radius.toFloat(), -1, p) } catch (_: Exception) {}
            }
        }
    }

    /** One fresh location fix for "I'm here: save this spot" (null if none within about 30 s). */
    @SuppressLint("MissingPermission")
    fun currentSpot(context: Context, done: (Spot?) -> Unit) {
        val lm = context.getSystemService(LocationManager::class.java)
        if (lm == null || !hasForeground(context)) { done(null); return }
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> { done(null); return }
        }
        val main = Handler(Looper.getMainLooper())
        var finished = false
        fun finish(l: Location?) { if (!finished) { finished = true; done(l?.let { Spot(it.latitude, it.longitude) }) } }
        if (Build.VERSION.SDK_INT >= 30) {
            lm.getCurrentLocation(provider, null, context.mainExecutor) { finish(it ?: lm.getLastKnownLocation(provider)) }
        } else {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, { finish(it) }, Looper.getMainLooper())
        }
        main.postDelayed({ finish(lm.getLastKnownLocation(provider)) }, 35_000)
    }
}

/** Android tells us when you walk into (or out of) Work or Home; only arriving matters. */
class PlaceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)) return
        val kind = intent.getStringExtra("kind") ?: return
        val store = Store.get(context)
        if (!store.state.value.placesOn) return
        val now = LocalTime.now()
        var cameHome = false
        store.edit { s ->
            val r = Places.onEnter(s.arrivals, kind, today(), now.hour * 60 + now.minute)
            cameHome = r.home
            s.copy(arrivals = r.arrivals)
        }
        val state = store.state.value
        // Seen at work: the "You're home" fallback moves to the expected end of today's workday.
        Evening.schedule(context, state)
        if (cameHome) Evening.show(context, "home", fromPlace = true)
        com.dudlatitat.nextchapter.widget.WeekWidget.refresh(context, state)
    }
}
