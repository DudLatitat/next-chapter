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
import com.dudlatitat.nextchapter.data.Career
import com.dudlatitat.nextchapter.data.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Job deadlines: a lunchtime nudge (12:15) 3 days and 1 day before a saved posting closes. */
object Deadlines {
    private const val CHANNEL = "deadlines"
    private const val AT_MINUTES = 12 * 60 + 15

    fun schedule(context: Context, state: AppState) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = PendingIntent.getBroadcast(
            context, 3, Intent(context, DeadlinesReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarms.cancel(pending)
        if (!state.jobReminders || state.jobs.none { it.deadline != null }) return
        var next = LocalDate.now().atStartOfDay().plusMinutes(AT_MINUTES.toLong())
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, at, AlarmManager.INTERVAL_DAY, pending)
    }

    fun show(context: Context) {
        val state = Store.get(context).state.value
        if (!state.jobReminders) return
        val due = Career.remindToday(state, LocalDate.now())
        if (due.isEmpty()) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Job deadlines", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 3, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val first = due.first()
        val left = Career.daysLeft(first, LocalDate.now())
        val note = NotificationCompat.Builder(context, CHANNEL)
            // Nothing buzzes during the 7–8 pm call: it still shows, silently.
            .setSilent(com.dudlatitat.nextchapter.data.Life.isCallTime(java.time.LocalTime.now()))
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(if (due.size == 1) "Closes ${if (left == 1L) "tomorrow" else "in $left days"}: ${first.title}" else "${due.size} job postings close soon")
            .setContentText(due.joinToString(" · ") { it.title + if (it.org.isNotBlank()) " (${it.org})" else "" })
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(3, note)
    }
}

class DeadlinesReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Deadlines.show(context)
}
