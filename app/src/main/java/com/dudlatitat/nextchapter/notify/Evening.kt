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
import com.dudlatitat.nextchapter.data.Life
import com.dudlatitat.nextchapter.data.Routine
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * The weekly routine's two evening notes (Settings → Routine reminders):
 * - "You're home": today's job and its first step (default 5:45 pm), unless you already checked in.
 * - "Wind down": phone down (default 11:30 pm), with a check-in nudge if today's job is unanswered.
 * Each is a one-shot alarm with a 10-minute window (no special permission needed) that books the
 * next day's when it fires. Tapping either opens Today. Silent if it lands in the 7–8 pm call.
 */
object Evening {
    private const val CHANNEL = "routine"
    private const val HOME = "home"
    private const val WIND = "wind"
    private const val WINDOW_MS = 10 * 60 * 1000L

    fun schedule(context: Context, state: AppState) {
        book(context, HOME, state.homeMinutes, state.eveningCheck)
        book(context, WIND, state.windDownMinutes, state.eveningCheck)
    }

    private fun pending(context: Context, which: String) = PendingIntent.getBroadcast(
        context, if (which == HOME) 2 else 7,
        Intent(context, EveningReceiver::class.java).putExtra("which", which),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun book(context: Context, which: String, minutes: Int, on: Boolean) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val p = pending(context, which)
        alarms.cancel(p)
        if (!on) return
        var next = LocalDate.now().atStartOfDay().plusMinutes(minutes.toLong())
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarms.setWindow(AlarmManager.RTC_WAKEUP, at, WINDOW_MS, p)
    }

    fun show(context: Context, which: String) {
        val state = Store.get(context).state.value
        schedule(context, state) // book tomorrow's
        if (!state.eveningCheck) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val job = Routine.jobFor(state.routine, LocalDate.now())
        val checked = Routine.checkFor(state.routineChecks, today())
        val task = Routine.text(job.task, state.herName)
        val (title, text) = if (which == HOME) {
            if (checked != null) return
            "You're home 🏠 Today's job: $task" to
                "First step: ${Routine.text(job.firstStep, state.herName)}. Bad day? ${Routine.text(job.minimum, state.herName)} still counts."
        } else {
            "Phone down, wind down 🌙" to
                if (checked == null) "How did \"$task\" go? Done, the minimum, or rest: tap to check in." else "Good night. 晚安."
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Evening routine", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 2, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_TODAY, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val note = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            // Nothing buzzes during the 7–8 pm call: it still shows, silently.
            .setSilent(Life.isCallTime(LocalTime.now()))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(if (which == HOME) 2 else 7, note)
    }
}

class EveningReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Evening.show(context, intent.getStringExtra("which") ?: "wind")
}
