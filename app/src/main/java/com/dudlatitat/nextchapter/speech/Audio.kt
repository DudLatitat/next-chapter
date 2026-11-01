package com.dudlatitat.nextchapter.speech

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

/** Mono audio as floats (−1…1) plus its sample rate; [pcm] is the same as 16-bit samples. */
class Clip(val samples: FloatArray, val rate: Int) {
    val seconds get() = samples.size / rate.toFloat()
    /** Loudness as dBFS (0 is the loudest possible; quiet rooms are around −60). */
    fun rmsDb(from: Int = 0, to: Int = samples.size): Float {
        if (to <= from) return -100f
        var e = 0.0
        for (i in from until to) e += samples[i] * samples[i]
        return (10 * kotlin.math.log10((e / (to - from)).coerceAtLeast(1e-10))).toFloat()
    }

    /**
     * Ready for the recognizer: silence trimmed from both ends (keeping a little), and quiet
     * speech boosted so its peak is near full volume.
     */
    fun forRecognizer(): Clip {
        if (samples.isEmpty()) return this
        val win = rate / 50 // 20 ms
        val levels = (0 until samples.size / win).map { k -> rmsDb(k * win, (k + 1) * win) }
        if (levels.isEmpty()) return this
        val floor = levels.sorted()[levels.size / 10]
        val loud = levels.indices.filter { levels[it] > floor + 12 }
        val a = ((loud.firstOrNull() ?: 0) - 15).coerceAtLeast(0) * win
        val b = (((loud.lastOrNull() ?: levels.lastIndex) + 20) * win).coerceAtMost(samples.size)
        val cut = samples.copyOfRange(a, maxOf(a + 1, b))
        val peak = cut.maxOf { kotlin.math.abs(it) }.coerceAtLeast(1e-4f)
        val gain = (0.9f / peak).coerceIn(1f, 8f)
        return Clip(FloatArray(cut.size) { (cut[it] * gain).coerceIn(-1f, 1f) }, rate)
    }

    fun pcm16(): ByteArray {
        val out = ByteArray(samples.size * 2)
        samples.forEachIndexed { i, s ->
            val v = (s.coerceIn(-1f, 1f) * 32767).toInt()
            out[2 * i] = (v and 0xFF).toByte(); out[2 * i + 1] = (v shr 8 and 0xFF).toByte()
        }
        return out
    }
}

object Recorder {
    const val RATE = 16000

    /**
     * Records from the mic until you stop talking (0.9 s of quiet after speech), [maxSeconds]
     * pass, or [stop] returns true. [level] gets the loudness 0–1 for the mic animation.
     */
    @SuppressLint("MissingPermission")
    suspend fun record(maxSeconds: Float = 6f, stop: () -> Boolean = { false }, level: (Float) -> Unit = {}): Clip = withContext(Dispatchers.IO) {
        val minBuf = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val rec = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, RATE))
        // The phone's own noise suppression, where it has it, cleans up background noise.
        val ns = try { if (android.media.audiofx.NoiseSuppressor.isAvailable()) android.media.audiofx.NoiseSuppressor.create(rec.audioSessionId)?.apply { enabled = true } else null } catch (_: Exception) { null }
        val chunk = ShortArray(RATE / 20) // 50 ms
        val all = ArrayList<Float>(RATE * 4)
        var heard = false
        var quiet = 0
        var noise = 0.004f
        try {
            rec.startRecording()
            while (coroutineContext.isActive && !stop() && all.size < maxSeconds * RATE) {
                val n = rec.read(chunk, 0, chunk.size)
                if (n <= 0) continue
                var e = 0.0
                for (i in 0 until n) { val s = chunk[i] / 32768f; all += s; e += s * s }
                val rms = sqrt(e / n).toFloat()
                level((rms * 12f).coerceIn(0f, 1f))
                if (!heard && all.size < RATE / 2) noise = maxOf(noise, rms * 0.9f).coerceAtMost(0.03f)
                if (rms > noise * 3f + 0.01f) { heard = true; quiet = 0 } else if (heard) quiet++
                if (heard && quiet >= 18) break
            }
        } finally {
            try { rec.stop() } catch (_: Exception) {}
            try { ns?.release() } catch (_: Exception) {}
            rec.release()
        }
        Clip(all.toFloatArray(), RATE)
    }

    /** Decodes a recording from the app's assets (MP3) to mono floats. */
    suspend fun decodeAsset(context: Context, path: String): Clip? = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            val fd = context.assets.openFd(path)
            extractor.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            fd.close()
            val track = (0 until extractor.trackCount).firstOrNull { extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true } ?: return@withContext null
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec.configure(format, null, null, 0)
            codec.start()
            var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val out = ArrayList<Float>(rate * 3)
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            while (!outputDone) {
                if (!inputDone) {
                    val i = codec.dequeueInputBuffer(10_000)
                    if (i >= 0) {
                        val buf = codec.getInputBuffer(i)!!
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0) { codec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inputDone = true }
                        else { codec.queueInputBuffer(i, 0, size, extractor.sampleTime, 0); extractor.advance() }
                    }
                }
                val o = codec.dequeueOutputBuffer(info, 10_000)
                when {
                    o >= 0 -> {
                        val buf = codec.getOutputBuffer(o)!!.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                        val count = info.size / 2
                        var k = 0
                        while (k + channels - 1 < count) {
                            var s = 0f
                            for (ch in 0 until channels) s += buf.get(k + ch) / 32768f
                            out += s / channels
                            k += channels
                        }
                        codec.releaseOutputBuffer(o, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                    o == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        rate = codec.outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channels = codec.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                }
            }
            codec.stop(); codec.release()
            Clip(out.toFloatArray(), rate)
        } catch (_: Exception) {
            null
        } finally {
            extractor.release()
        }
    }
}
