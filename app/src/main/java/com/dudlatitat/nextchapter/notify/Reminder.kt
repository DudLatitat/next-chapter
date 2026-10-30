package com.dudlatitat.nextchapter.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** The once-a-day nudge to practice, at the time picked in Settings. */
object Reminder {
    private const val CHANNEL = "practice"

    fun schedule(context: Context, state: AppState) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = PendingIntent.getBroadcast(
            context, 1, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarms.cancel(pending)
        if (!state.reminderOn) return
        var next = LocalDate.now().atStartOfDay().plusMinutes(state.reminderMinutes.toLong())
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // Inexact is fine for a practice nudge, and needs no special alarm permission.
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, at, AlarmManager.INTERVAL_DAY, pending)
    }

    fun show(context: Context) {
        val state = Store.get(context).state.value
        if (!state.reminderOn) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val due = Srs.queue(state).size
        val streak = Srs.streak(state.studyDays)
        if (due == 0 || today() in state.studyDays) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Daily practice", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_STUDY, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = if (streak > 0) "Keep your 🔥 $streak-day streak going" else "Time for a little Chinese"
        val note = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText("$due phrases waiting · about ${maxOf(1, due / 3)} min")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, note)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminder.show(context)
}

/** Alarms are forgotten when the phone restarts: set it again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Reminder.schedule(context, Store.get(context).state.value)
        }
    }
}
