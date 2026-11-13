package com.dudlatitat.nextchapter.desktop

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.data.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Keeps the computer page available while the app isn't on screen: a foreground service (Android
 * needs the ongoing notification for that) running [DesktopServer], holding a Wi-Fi lock so the
 * phone keeps answering. Switched with Settings → Use on my computer, or Stop on the notification.
 */
class DesktopService : Service() {
    private var server: DesktopServer? = null
    private var wifi: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Desktop.setEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(
            this, NOTE_ID, notification(),
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0,
        )
        if (server == null) {
            server = DesktopServer(applicationContext, Store.get(applicationContext)).also { it.start() }
            Desktop.running.value = server
            @Suppress("DEPRECATION")
            wifi = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "NextChapter:desktop").apply { setReferenceCounted(false); acquire() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        server?.stop(); server = null
        Desktop.running.value = null
        wifi?.let { if (it.isHeld) it.release() }; wifi = null
        super.onDestroy()
    }

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "On my computer", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 30, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 31, Intent(this, DesktopService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val where = DesktopServer.localAddresses().firstOrNull()?.let { "http://$it:8090" } ?: "your home Wi-Fi"
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Next Chapter is open to your computer")
            .setContentText(where)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .build()
    }

    companion object {
        private const val CHANNEL = "desktop"
        private const val NOTE_ID = 40
        const val ACTION_STOP = "com.dudlatitat.nextchapter.desktop.STOP"
    }
}

/** The on/off switch, remembered on the phone, and the running server (for the Settings screen). */
object Desktop {
    val running = MutableStateFlow<DesktopServer?>(null)
    val server: StateFlow<DesktopServer?> = running

    private fun prefs(context: Context) = context.getSharedPreferences("desktop", Context.MODE_PRIVATE)
    fun enabled(context: Context) = prefs(context).getBoolean("enabled", false)
    fun setEnabled(context: Context, on: Boolean) = prefs(context).edit().putBoolean("enabled", on).apply()

    /** Starts the service if the switch is on (app start, phone restart). */
    fun resume(context: Context) { if (enabled(context)) start(context) }

    fun start(context: Context) {
        setEnabled(context, true)
        try { ContextCompat.startForegroundService(context, Intent(context, DesktopService::class.java)) } catch (_: Exception) {}
    }

    fun stop(context: Context) {
        setEnabled(context, false)
        context.stopService(Intent(context, DesktopService::class.java))
    }

    /** Unpairs all computers (works whether or not it's running). */
    fun forgetAll(context: Context) {
        running.value?.forgetAll() ?: prefs(context).edit().putStringSet("tokens", emptySet()).apply()
    }

    fun pairedCount(context: Context): Int = prefs(context).getStringSet("tokens", emptySet())?.size ?: 0
}
