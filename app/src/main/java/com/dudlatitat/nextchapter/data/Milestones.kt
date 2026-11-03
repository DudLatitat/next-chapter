package com.dudlatitat.nextchapter.data

data class Milestone(val id: String, val emoji: String, val title: String, val detail: String, val done: Boolean)

/** Little celebrations, worked out from what's already saved. */
object Milestones {
    fun all(s: AppState): List<Milestone> {
        val started = s.cards.count { !it.isNew }
        val known = s.cards.count { it.isKnown }
        val best = bestStreak(Stats.practiceDays(s))
        val spoken = s.toneEvents.filter { it.spoken }
        val toneAcc = (1..4).map { t -> s.toneEvents.filter { it.target == t }.let { e -> if (e.size < 10) 0f else e.count { it.got == t }.toFloat() / e.size } }
        val bookedUnder = s.flightTarget > 0 && s.trips.any { it.booked && it.price in 1..s.flightTarget }
        fun m(id: String, emoji: String, title: String, detail: String, done: Boolean) = Milestone(id, emoji, title, detail, done)
        return listOf(
            m("first", "🌱", "First step", "Finish your first review", s.studyDays.isNotEmpty()),
            m("streak7", "🔥", "One week strong", "Practice 7 days in a row", best >= 7),
            m("streak30", "🌋", "Unstoppable", "Practice 30 days in a row", best >= 30),
            m("words50", "📗", "50 phrases started", "Start learning 50 phrases or words", started >= 50),
            m("words100", "📚", "100 phrases started", "Start learning 100", started >= 100),
            m("words250", "🏯", "250 phrases started", "Start learning 250", started >= 250),
            m("known25", "🧠", "25 you really know", "Remember 25 for a week or more", known >= 25),
            m("known100", "🎓", "100 you really know", "Remember 100 for a week or more", known >= 100),
            m("spoken", "🎤", "First words out loud", "Use a speaking check", spoken.isNotEmpty()),
            m("tones", "🎶", "Tone master", "Every tone right 80%+ (10 tries each)", toneAcc.all { it >= 0.8f }),
            m("dialogue", "💬", "First conversation", "Finish a dialogue", s.dialoguesDone.isNotEmpty()),
            m("dialogues", "🗣️", "Smooth talker", "Finish every dialogue", s.dialoguesDone.size >= Dialogues.all.size),
            m("grammar", "🧩", "Pattern spotter", "Pass a grammar quiz", s.grammarPassed.isNotEmpty()),
            m("grammarAll", "🏗️", "Sentence builder", "Pass every grammar quiz", s.grammarPassed.size >= Grammar.points.size),
            m("story", "📖", "First story", "Read a story to the end", s.storiesRead.isNotEmpty()),
            m("write10", "✍️", "Writer", "Write 10 characters", s.charsWritten >= 10),
            m("write50", "🖌️", "Calligrapher", "Write 50 characters", s.charsWritten >= 50),
            m("drive", "🚗", "Road scholar", "Practice 60 minutes in Drive mode", s.driveMinutes >= 60),
            m("flight", "✈️", "Deal hunter", "Book a flight at or under your target price", bookedUnder),
            m("packed", "🧳", "Ready to go", "Check off the whole packing list", s.packing.isNotEmpty() && s.packing.all { it.done }),
            m("save50", "💰", "Halfway there", "Reach half your savings goal", s.savingsProgress() >= 0.5f),
            m("move50", "🧭", "Move-ready 50%", "Half of the move checklist and savings done", s.moveReady() >= 0.5f),
        )
    }

    /** Milestones reached but not celebrated yet. */
    fun fresh(s: AppState) = all(s).filter { it.done && it.id !in s.milestonesSeen }

    private fun bestStreak(days: List<Long>): Int {
        var best = 0; var run = 0; var prev: Long? = null
        days.distinct().sorted().forEach { d -> run = if (prev != null && d == prev!! + 1) run + 1 else 1; best = maxOf(best, run); prev = d }
        return best
    }
}
