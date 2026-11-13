package com.dudlatitat.nextchapter.desktop

/**
 * Who may try a pairing code, and when. Pairing is only open while the phone's
 * "Use on my computer" screen is showing, and wrong guesses lock it for 30 s, then 1 min,
 * 2 min… (up to an hour), so the 6 digits can't be brute-forced from a shared Wi-Fi.
 * Pure (the clock is passed in), tested in PairGateTest.
 */
class PairGate {
    private var openSince: Boolean = false
    private var fails = 0
    private var lockouts = 0
    private var lockedUntil = 0L

    @Synchronized fun open() { openSince = true }
    @Synchronized fun close() { openSince = false }

    sealed interface Check
    data object Ok : Check
    data object Closed : Check
    data class Locked(val seconds: Long) : Check

    @Synchronized fun check(now: Long): Check = when {
        !openSince -> Closed
        now < lockedUntil -> Locked((lockedUntil - now + 999) / 1000)
        else -> Ok
    }

    /** A wrong code. True when this started a lockout (and the phone shows a new code). */
    @Synchronized fun wrong(now: Long): Boolean {
        if (++fails < 5) return false
        fails = 0
        lockedUntil = now + minOf(30_000L shl minOf(lockouts, 7), 3_600_000L)
        lockouts++
        return true
    }

    @Synchronized fun paired() { fails = 0; lockouts = 0; lockedUntil = 0 }
}
