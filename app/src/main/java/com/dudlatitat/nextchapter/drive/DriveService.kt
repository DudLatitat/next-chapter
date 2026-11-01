package com.dudlatitat.nextchapter.drive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dudlatitat.nextchapter.MainActivity
import com.dudlatitat.nextchapter.R
import com.dudlatitat.nextchapter.Speaker
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.DayLog
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/** What Drive mode is doing, for the screen. */
data class DriveState(
    val running: Boolean = false,
    val paused: Boolean = false,
    val index: Int = 0,
    val total: Int = 0,
    val card: Card? = null,
    /** "english", "think", "chinese" or "repeat". */
    val phase: String = "",
)

/**
 * Hands-free practice for the car: says the English, leaves a pause for you to answer in
 * Chinese, then plays the native Chinese twice. Runs with the screen off; music is lowered
 * while it speaks. Uses today's review phrases first, then recent ones.
 */
class DriveService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loop: Job? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private var startedAt = 0L
    private var played = 0
    private var cards: List<Card> = emptyList()
    private val recorded by lazy { try { assets.list("voice")?.toSet() ?: emptySet() } catch (_: Exception) { emptySet() } }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start(intent.getIntExtra(EXTRA_GAP, 4))
            ACTION_PAUSE -> { _state.value = _state.value.copy(paused = !_state.value.paused); if (_state.value.paused) { tts?.stop(); player?.pause() }; notifyUpdate() }
            ACTION_NEXT -> { skip = true; tts?.stop(); try { player?.stop() } catch (_: Exception) {} }
            ACTION_STOP -> stopAll()
        }
        return START_NOT_STICKY
    }

    @Volatile private var skip = false
    private var gapSeconds = 4

    private fun start(gap: Int) {
        gapSeconds = gap.coerceIn(2, 8)
        if (loop?.isActive == true) return
        val state = Store.get(this).state.value
        val queue = Srs.queue(state)
        val recent = state.cards.filter { !it.isNew && it !in queue }.sortedByDescending { it.last ?: 0 }.take(40).shuffled()
        cards = (queue + recent).take(40)
        if (cards.isEmpty()) cards = state.cards.take(20)
        startedAt = System.currentTimeMillis(); played = 0
        _state.value = DriveState(running = true, total = cards.size)
        startForegroundCompat()
        tts = TextToSpeech(this) { ttsReady = it == TextToSpeech.SUCCESS }
        val am = getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .build().also { am.requestAudioFocus(it) }
        loop = scope.launch { run() }
    }

    private suspend fun run() {
        var waited = 0
        while (!ttsReady && waited < 50) { delay(100); waited++ }
        for ((i, card) in cards.withIndex()) {
            if (!scope.isActive) return
            skip = false
            _state.value = _state.value.copy(index = i, card = card, phase = "english"); notifyUpdate()
            step { speak(card.english, Locale.US, 1.0f) }
            _state.value = _state.value.copy(phase = "think"); notifyUpdate()
            step { delay(gapSeconds * 1000L) }
            _state.value = _state.value.copy(phase = "chinese"); notifyUpdate()
            step { chinese(card, 0.9f) }
            step { delay(900) }
            _state.value = _state.value.copy(phase = "repeat"); notifyUpdate()
            step { chinese(card, 0.75f) }
            step { delay(1500) }
            played++
        }
        stopAll()
    }

    /** Runs one piece unless skipped; waits while paused. */
    private suspend fun step(block: suspend () -> Unit) {
        while (_state.value.paused && scope.isActive) delay(200)
        if (!skip) block()
        while (_state.value.paused && scope.isActive) delay(200)
    }

    private suspend fun chinese(card: Card, rate: Float) {
        val file = "${Speaker.md5(card.hanzi.trim())}.mp3"
        if (file in recorded) playAsset("voice/$file", rate) else speak(card.hanzi, Locale.SIMPLIFIED_CHINESE, rate)
    }

    private suspend fun speak(text: String, locale: Locale, rate: Float) = suspendCancellableCoroutine { cont ->
        val t = tts
        if (t == null || !ttsReady) { cont.resume(Unit); return@suspendCancellableCoroutine }
        t.language = locale
        t.setSpeechRate(rate)
        val id = "u${System.nanoTime()}"
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { if (utteranceId == id && cont.isActive) cont.resume(Unit) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { if (cont.isActive) cont.resume(Unit) }
            override fun onStop(utteranceId: String?, interrupted: Boolean) { if (cont.isActive) cont.resume(Unit) }
        })
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        cont.invokeOnCancellation { t.stop() }
    }

    private suspend fun playAsset(path: String, rate: Float) = suspendCancellableCoroutine { cont ->
        try {
            player?.release()
            val fd = assets.openFd(path)
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                fd.close()
                prepare()
                playbackParams = playbackParams.setSpeed(rate)
                setOnCompletionListener { if (cont.isActive) cont.resume(Unit) }
                setOnErrorListener { _, _, _ -> if (cont.isActive) cont.resume(Unit); true }
                start()
            }
            // A skip stops the player without a completion callback; check for it.
            scope.launch { while (cont.isActive) { delay(150); if (skip && cont.isActive) cont.resume(Unit) } }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    private fun stopAll() {
        loop?.cancel()
        val minutes = ((System.currentTimeMillis() - startedAt) / 60000).toInt()
        if (startedAt > 0) {
            val seconds = ((System.currentTimeMillis() - startedAt) / 1000).toInt().coerceAtMost(4 * 3600)
            Store.get(this).edit { s ->
                val d = today()
                val log = s.days[d] ?: DayLog()
                s.copy(
                    driveMinutes = s.driveMinutes + minutes,
                    days = s.days + (d to log.copy(seconds = log.seconds + seconds, reviews = log.reviews + played)),
                    // Ten phrases or more counts as today's practice for the streak.
                    studyDays = if (played >= 10 && d !in s.studyDays) s.studyDays + d else s.studyDays,
                )
            }
        }
        startedAt = 0
        tts?.shutdown(); tts = null
        player?.release(); player = null
        focus?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        _state.value = DriveState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (startedAt > 0) stopAll()
        scope.cancel()
        super.onDestroy()
    }

    private fun startForegroundCompat() {
        val n = notification()
        if (Build.VERSION.SDK_INT >= 29) startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(ID, n)
    }

    private fun notifyUpdate() {
        getSystemService(NotificationManager::class.java).notify(ID, notification())
    }

    private fun action(act: String, code: Int) = PendingIntent.getService(
        this, code, Intent(this, DriveService::class.java).setAction(act), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Drive mode", NotificationManager.IMPORTANCE_LOW))
        val s = _state.value
        val open = PendingIntent.getActivity(this, 20, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("🚗 Drive mode · ${s.index + 1} of ${s.total}")
            .setContentText(s.card?.let { if (s.phase == "english" || s.phase == "think") it.english else "${it.hanzi}  ${it.pinyin}" } ?: "Starting…")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(open)
            .addAction(0, if (s.paused) "Resume" else "Pause", action(ACTION_PAUSE, 21))
            .addAction(0, "Next", action(ACTION_NEXT, 22))
            .addAction(0, "Stop", action(ACTION_STOP, 23))
            .build()
    }

    companion object {
        private const val ID = 77
        private const val CHANNEL = "drive"
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_NEXT = "next"
        const val ACTION_STOP = "stop"
        const val EXTRA_GAP = "gap"

        private val _state = MutableStateFlow(DriveState())
        val state: StateFlow<DriveState> = _state

        fun send(context: Context, action: String, gap: Int = 4) {
            val i = Intent(context, DriveService::class.java).setAction(action).putExtra(EXTRA_GAP, gap)
            if (action == ACTION_START) ContextCompat.startForegroundService(context, i) else context.startService(i)
        }
    }
}
