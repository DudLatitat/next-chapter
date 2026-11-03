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
import com.dudlatitat.nextchapter.data.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * One gentle check-in at 8:30 pm, after her call: "did you get to tonight's goal?" Only when a
 * goal was set and not answered yet. Never during the 7–8 pm call.
 */
object Evening {
    private const val CHANNEL = "evening"
    private const val AT_MINUTES = 20 * 60 + 30

    fun schedule(context: Context, state: AppState) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = PendingIntent.getBroadcast(
            context, 2, Intent(context, EveningReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarms.cancel(pending)
        if (!state.eveningCheck) return
        var next = LocalDate.now().atStartOfDay().plusMinutes(AT_MINUTES.toLong())
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
        val at = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, at, AlarmManager.INTERVAL_DAY, pending)
    }

    fun show(context: Context) {
        val state = Store.get(context).state.value
        if (!state.eveningCheck) return
        val goal = Life.goalFor(state) ?: return
        if (goal.done != null) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Evening check-in", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(
            context, 2, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val note = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Tonight: ${goal.text}")
            .setContentText("Did you get to it? No pressure either way.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(2, note)
    }
}

class EveningReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Evening.show(context)
}
