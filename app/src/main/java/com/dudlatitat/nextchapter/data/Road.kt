package com.dudlatitat.nextchapter.data

/**
 * The three-year road: today → the milestones you've dated (A+ exam, cert dates, trips) → the
 * move. Each milestone sits at its share of the way. Pure, tested in RoadTest.
 */
object Road {
    enum class Kind { EXAM, CERT, TRIP, MOVE }
    data class Mark(val day: Long, val label: String, val kind: Kind) {
        fun at(today: Long, end: Long): Float = if (end <= today) 1f else ((day - today).toFloat() / (end - today)).coerceIn(0f, 1f)
    }

    private fun parse(iso: String?): Long? = iso?.let { runCatching { java.time.LocalDate.parse(it).toEpochDay() }.getOrNull() }

    /** Upcoming milestones up to the move, soonest first; the move is last (if it's set). */
    fun marks(s: AppState, day: Long = today()): List<Mark> {
        val move = parse(s.moveDate)
        val end = move ?: Long.MAX_VALUE
        val out = mutableListOf<Mark>()
        parse(s.aplusExam)?.let { out += Mark(it, "A+ exam", Kind.EXAM) }
        Career.certs(s).forEach { c -> parse(c.date)?.takeIf { c.status != "passed" }?.let { out += Mark(it, c.name.substringBefore(" ("), Kind.CERT) } }
        s.trips.forEach { t -> parse(t.depart)?.let { out += Mark(it, "Trip ✈", Kind.TRIP) } }
        val upcoming = out.filter { it.day in day..end }.distinctBy { it.day to it.kind }.sortedBy { it.day }
        return upcoming + listOfNotNull(move?.takeIf { it >= day }?.let { Mark(it, "成都", Kind.MOVE) })
    }

    /**
     * Pushes dot positions (0..1, sorted) apart so none sit closer than [gap]: close dates would
     * otherwise draw on top of each other. Keeps the order; the last dot (the move) stays at the end.
     */
    fun spread(at: List<Float>, gap: Float): List<Float> {
        if (at.isEmpty()) return at
        val g = gap.coerceAtMost(1f / at.size)
        val out = at.map { it.coerceIn(0f, 1f) }.toMutableList()
        for (i in 1 until out.size) out[i] = maxOf(out[i], out[i - 1] + g)
        // If that pushed past the end, pull back from the right.
        if (out.last() > 1f) { out[out.lastIndex] = 1f; for (i in out.lastIndex - 1 downTo 0) out[i] = minOf(out[i], out[i + 1] - g) }
        return out.map { it.coerceIn(0f, 1f) }
    }

    /** "3 years, 1 month" (or "5 months", "12 days") from [day] to [end]; calendar-true via java.time. */
    fun left(day: Long, end: Long): String {
        if (end <= day) return "here"
        val p = java.time.Period.between(java.time.LocalDate.ofEpochDay(day), java.time.LocalDate.ofEpochDay(end))
        fun n(v: Int, w: String) = "$v $w" + if (v == 1) "" else "s"
        return when {
            p.years > 0 -> n(p.years, "year") + if (p.months > 0) ", " + n(p.months, "month") else ""
            p.months > 0 -> n(p.months, "month")
            else -> n(p.days, "day")
        }
    }

    /** New Year's Days between [day] and [end], with their share of the way (for ticks on the line). */
    fun yearTicks(day: Long, end: Long): List<Pair<Int, Float>> {
        if (end <= day) return emptyList()
        val first = java.time.LocalDate.ofEpochDay(day).year + 1
        val last = java.time.LocalDate.ofEpochDay(end).year
        return (first..last).map { y ->
            val d = java.time.LocalDate.of(y, 1, 1).toEpochDay()
            y to ((d - day).toFloat() / (end - day)).coerceIn(0f, 1f)
        }.filter { it.second in 0.1f..0.88f }
    }

    /** "Fri · Jan 15" for a row's second line (adds the year when it isn't this year). */
    fun weekdayDate(day: Long, today: Long): String {
        val d = java.time.LocalDate.ofEpochDay(day)
        val pat = if (d.year == java.time.LocalDate.ofEpochDay(today).year) "EEE · MMM d" else "EEE · MMM d, yyyy"
        return d.format(java.time.format.DateTimeFormatter.ofPattern(pat, java.util.Locale.US))
    }

    /** "Mar 3" style, for labels. */
    fun short(day: Long): String = java.time.LocalDate.ofEpochDay(day).format(java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US))
}
