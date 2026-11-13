package com.dudlatitat.nextchapter.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections

class SaverTest {
    private fun waitFor(check: () -> Boolean) {
        val until = System.currentTimeMillis() + 5000
        while (!check() && System.currentTimeMillis() < until) Thread.sleep(5)
    }

    @Test fun quickSavesLandInOrderAndTheNewestWins() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val written = Collections.synchronizedList(mutableListOf<Int>())
        val saver = Saver<Int>(scope) { Thread.sleep(20); written += it }
        (1..100).forEach { saver.save(it) }
        waitFor { written.lastOrNull() == 100 }
        assertEquals(100, written.last())
        assertEquals("never an older state after a newer one", written.sorted(), written)
        assertTrue("merged, not 100 separate writes", written.size < 20)
        scope.cancel()
    }

    @Test fun aFailedWriteDoesNotStopLaterSaves() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val written = Collections.synchronizedList(mutableListOf<Int>())
        val saver = Saver<Int>(scope) { if (it == 1) error("disk full") else written += it }
        saver.save(1)
        Thread.sleep(50)
        saver.save(2)
        waitFor { written.isNotEmpty() }
        assertEquals(listOf(2), written.toList())
        scope.cancel()
    }
}
