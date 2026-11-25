package com.dudlatitat.nextchapter.data

/**
 * 准备好了吗 · Readiness: "can I actually do this at her family's place yet?"
 * A ladder of real situations, each worked out from what you've already done (Talk scenes,
 * Texts, Number ears, decks, ★ said for real). Nothing to tick by hand. The first rung that
 * isn't finished is "next", with its missing pieces listed. Pure, tested in ReadyTest.
 */
object Ready {
    data class Piece(val label: String, val done: Boolean, val tool: String? = null)
    data class Rung(val id: String, val title: String, val hanzi: String, val pieces: List<Piece>) {
        val done: Boolean get() = pieces.all { it.done }
        val missing: List<Piece> get() = pieces.filterNot { it.done }
    }
    data class Ladder(val rungs: List<Rung>) {
        val climbed: Int get() = rungs.takeWhile { it.done }.size
        /** The first rung with something missing (null when the whole ladder is done). */
        val next: Rung? get() = rungs.firstOrNull { !it.done }
        /** 0–100: every piece done anywhere counts, so work on later rungs isn't wasted. */
        val percent: Int get() = rungs.flatMap { it.pieces }.let { p -> if (p.isEmpty()) 0 else p.count { it.done } * 100 / p.size }
    }

    /** A scene counts once you've got 60% of its chopsticks (most = 2 per beat). */
    const val SCENE_PASS = 0.6
    /** Number ears: a round of 10 with about 6 right in a row scores 150+. */
    const val NUMBERS_PASS = 150

    /** A card you've answered right at least twice. */
    fun learned(c: Card): Boolean = !c.isNew && c.reps >= 2

    private fun deck(s: AppState, id: String, n: Int, name: String): Piece {
        val got = s.cards.count { it.deck == id && learned(it) }
        // No zeros before you start: "Learn 5 family phrases", then "Family phrases 3/5".
        return Piece(if (got == 0) "Learn $n ${name.lowercase()} phrases" else "$name phrases ${minOf(got, n)}/$n", got >= n, "deck:$id")
    }

    private fun scene(s: AppState, id: String): Piece {
        val sc = Talk.scenes.first { it.id == id }
        val most = sc.beats.size * 2
        val best = s.dinnerBest[id] ?: 0
        return Piece("Talk: ${sc.title}", best >= kotlin.math.ceil(most * SCENE_PASS).toInt(), "talk:$id")
    }

    private fun text(s: AppState, id: String): Piece {
        val th = Texting.threads.first { it.id == id }
        val best = s.textBest[id] ?: 0
        return Piece("Texts: ${th.title}", best >= kotlin.math.ceil(th.turns.size * 2 * SCENE_PASS).toInt(), "texts:$id")
    }

    private fun card(s: AppState, id: String, label: String) = Piece(label, s.cards.any { it.id == id && learned(it) }, "deck:${id.substringBefore(':')}")

    fun family(s: AppState): Ladder = Ladder(listOf(
        Rung("hello", "Say hello", "问好", listOf(deck(s, "family", 5, "Family"), deck(s, "daily", 5, "Everyday"))),
        Rung("call", "Video call with her mom", "视频", listOf(scene(s, "momcall"), deck(s, "family", 12, "Family"))),
        Rung("arrive", "Arrive at the house", "进门", listOf(scene(s, "arrive"))),
        Rung("table", "Get through dinner", "吃饭", listOf(scene(s, "meal"), deck(s, "food", 8, "Eating out"))),
        Rung("out", "Out and about", "出门", listOf(scene(s, "taxi"), scene(s, "restaurant"), Piece("Number ears: $NUMBERS_PASS+", s.numbersBest >= NUMBERS_PASS, "numbers"))),
        Rung("errands", "Run errands", "跑腿", listOf(scene(s, "market"), scene(s, "pharmacy"))),
        Rung("wechat", "Text her family", "微信", listOf(text(s, "mom"), deck(s, "text", 8, "Texting"))),
        Rung("feast", "The big dinner", "大餐", listOf(
            scene(s, "after"),
            card(s, "trip:我敬您一杯", "The toast · 我敬您一杯"),
            Piece(if (s.realUses.isEmpty()) "★ Say 3 phrases for real" else "★ Said for real ${minOf(s.realUses.size, 3)}/3", s.realUses.size >= 3),
        )),
    ))
}
