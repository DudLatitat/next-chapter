package com.dudlatitat.nextchapter.data

import kotlin.random.Random

/**
 * Callout drill: a picture flashes, you shout the Identity V callout before the timer runs out.
 * Pure rules here (tested in CalloutsTest); the screen is ui/learn/Callouts.kt.
 */
object Callouts {
    /** One prompt: what flashes on screen, and the idv card it means. */
    data class Prompt(val icon: String, val hint: String, val hanzi: String)

    val prompts = listOf(
        Prompt("⬅️", "Turn left", "左转"),
        Prompt("➡️", "Turn right", "右转"),
        Prompt("⬆️", "Go straight", "直走"),
        Prompt("↩️", "Go back", "往回走"),
        Prompt("🔼", "You're upstairs", "我在楼上"),
        Prompt("🔽", "You're downstairs", "我在楼下"),
        Prompt("👈", "It's on her left", "在你左边"),
        Prompt("👉", "It's on her right", "在你右边"),
        Prompt("👻", "The anomaly is coming", "异象来了！"),
        Prompt("🏃", "Run!", "快跑！"),
        Prompt("🆘", "Save me!", "救我！"),
        Prompt("📍", "Where is she?", "你在哪里？"),
        Prompt("👀", "You can see her", "我看到你了"),
        Prompt("🙈", "You can't see her", "我看不到你"),
        Prompt("🚪", "Find the way out", "撤离点在哪里？"),
        Prompt("🤝", "Meet up", "我们会合吧"),
        Prompt("✋", "Stay put, you're coming", "站着别动，我来找你"),
        Prompt("👣", "Follow me", "跟着我"),
        Prompt("⏳", "Wait for me!", "等等我！"),
        Prompt("🧭", "You're lost", "我迷路了"),
        Prompt("🏃‍♂️💨", "On your way to her", "我过去找你"),
        Prompt("📦", "Loot first", "我去搜刮"),
    )

    const val ROUND = 10
    /** Seconds to start speaking before the card reveals itself. */
    const val SECONDS = 8

    fun round(random: Random = Random.Default): List<Prompt> = prompts.shuffled(random).take(ROUND)

    enum class Verdict { GREAT, CLOSE, MISS }

    /**
     * From the speaking check's per-character marks: every character right is GREAT; mostly right
     * (tones a bit off count as half) is CLOSE, so a nervous shout still feels like progress.
     */
    fun verdict(right: Int, tone: Int, missed: Int): Verdict {
        val total = right + tone + missed
        if (total == 0) return Verdict.MISS
        if (missed == 0 && tone == 0) return Verdict.GREAT
        return if ((right + tone * 0.5) / total >= 0.6) Verdict.CLOSE else Verdict.MISS
    }

    /** Points: 100 great, 50 close, plus 10 for each second left on the clock when you started talking. */
    fun points(v: Verdict, secondsLeft: Int): Int = when (v) {
        Verdict.GREAT -> 100 + 10 * secondsLeft.coerceIn(0, SECONDS)
        Verdict.CLOSE -> 50 + 5 * secondsLeft.coerceIn(0, SECONDS)
        Verdict.MISS -> 0
    }

    /** The most a round can score, for the "out of" line. */
    val maxScore = ROUND * points(Verdict.GREAT, SECONDS)
}
