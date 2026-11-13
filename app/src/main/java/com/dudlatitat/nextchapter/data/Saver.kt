package com.dudlatitat.nextchapter.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Writes the newest value, in order, on one coroutine. Values that arrive while a write is running
 * are merged: only the latest is written next, so ten quick grades cost two writes, not ten, and an
 * older state can never land on disk after a newer one.
 */
class Saver<T>(scope: CoroutineScope, private val write: (T) -> Unit) {
    private val pending = Channel<T>(Channel.CONFLATED)

    init {
        // A failed write (disk full, say) mustn't stop later saves.
        scope.launch { for (value in pending) try { write(value) } catch (_: Exception) {} }
    }

    fun save(value: T) { pending.trySend(value) }
}
