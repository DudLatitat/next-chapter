package com.dudlatitat.nextchapter.speech

import com.dudlatitat.nextchapter.data.Callouts

/** One word on how an attempt went (used by Callouts, Dinner, the remote, the ladder, the family map). */
fun Attempt.verdict(): Callouts.Verdict {
    val marks = chars
    return when {
        tooQuiet -> Callouts.Verdict.MISS
        marks == null -> Callouts.Verdict.CLOSE // the word check isn't available: count the effort
        else -> Callouts.verdict(marks.count { it.mark == Mark.RIGHT }, marks.count { it.mark == Mark.TONE }, marks.count { it.mark == Mark.MISSED })
    }
}
