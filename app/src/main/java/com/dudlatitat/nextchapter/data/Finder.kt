package com.dudlatitat.nextchapter.data

/**
 * "How do I say…": type English (or pinyin, or characters) and get Chinese you can type.
 * 1. [phrases]: whole sentences the app already has (checked, natural), ranked by shared words.
 * 2. [Dict]: single words from the offline CC-CEDICT dictionary, when a sentence isn't there.
 * Pure, tested in FinderTest.
 */
object Finder {
    data class Hit(val hanzi: String, val pinyin: String, val english: String, val from: String) {
        /** How to type it on a pinyin keyboard: plain letters, words spaced ("dao jia le ma"). */
        val keys: String get() = typeKeys(pinyin)
    }

    private val stop = setOf("a", "an", "the", "to", "of", "is", "are", "am", "be", "do", "does", "did", "it", "that", "this", "please", "just", "so", "very", "really")

    /** Lowercase words, light stemming ("missing" → "miss", "tomatoes" → "tomato"). */
    fun words(s: String): List<String> = s.lowercase().replace("'", "").split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() && it !in stop }.map(::stem)

    private fun stem(w: String): String = when {
        w.length > 5 && w.endsWith("ing") -> w.dropLast(3)
        w.length > 4 && w.endsWith("ed") -> w.dropLast(2)
        w.length > 4 && w.endsWith("es") -> w.dropLast(2)
        w.length > 3 && w.endsWith("s") && !w.endsWith("ss") -> w.dropLast(1)
        else -> w
    }

    private fun han(s: String) = s.filter { it in '一'..'鿿' }

    /** Every sentence the app knows: your cards (including your own), conversations and chats. */
    fun corpus(s: AppState): List<Hit> {
        val out = LinkedHashMap<String, Hit>()
        fun add(h: String, p: String, e: String, from: String) { if (han(h).isNotEmpty() && h !in out) out[h] = Hit(h, p, e, from) }
        s.cards.forEach { add(it.hanzi, it.pinyin, it.english, "phrase") }
        Talk.scenes.forEach { sc -> sc.beats.forEach { b -> add(b.hanzi, b.pinyin, b.english, sc.title); b.replies.forEach { r -> add(r.hanzi, r.pinyin, r.english, sc.title) } } }
        Texting.threads.forEach { th -> th.turns.forEach { t -> t.them.forEach { add(it.hanzi, it.pinyin, it.english, th.contact) }; t.examples.forEach { add(it.hanzi, it.pinyin, it.english, th.contact) } } }
        return out.values.toList()
    }

    /**
     * Sentences that mean what you typed, best first. English: share of your words found in the
     * meaning (all of them ranks first, shorter sentences break ties). Characters or pinyin: contains.
     */
    fun phrases(query: String, corpus: List<Hit>, limit: Int = 5): List<Hit> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        if (han(q).isNotEmpty()) return corpus.filter { han(q) in han(it.hanzi) }.sortedBy { it.hanzi.length }.take(limit)
        val qw = words(q)
        if (qw.isEmpty()) return emptyList()
        val plainQ = Talk.plain(q)
        return corpus.mapNotNull { h ->
            val ew = words(h.english).toSet()
            val shared = qw.count { it in ew }
            val share = shared.toDouble() / qw.size
            val pinyinHit = plainQ.length >= 4 && Talk.plain(h.pinyin).contains(plainQ)
            val score = if (pinyinHit) 1.5 else share
            if (score >= 0.5) h to (score - ew.size * 0.01) else null
        }.sortedByDescending { it.second }.take(limit).map { it.first }
    }

    /** Every 1–4 character piece of the app's phrases: the everyday words the dictionary should prefer. */
    fun common(corpus: List<Hit>): Set<String> {
        val out = HashSet<String>()
        corpus.forEach { h -> val t = han(h.hanzi); for (n in 1..4) for (i in 0..t.length - n) out += t.substring(i, i + n) }
        return out
    }

    /** "dào jiā le ma?" → "dao jia le ma" (what you press on a pinyin keyboard). */
    fun typeKeys(pinyin: String): String =
        pinyin.split(Regex("\\s+")).map { Talk.plain(it) }.filter { it.isNotEmpty() }.joinToString(" ")
}

/**
 * The offline dictionary (assets/cedict.tsv, built by tools/make_cedict.py from CC-CEDICT, CC BY-SA 4.0).
 * Lines: hanzi TAB pinyin TAB meaning; meaning… Searching English ranks exact meanings first, then
 * meanings that start with the word, then ones that contain it; old/literary/regional senses sink,
 * and words the app already teaches rise (they're the common ones).
 */
class Dict(private val lines: List<String>) {
    data class Entry(val hanzi: String, val pinyin: String, val meanings: List<String>)

    val size: Int get() = lines.count { !it.startsWith("#") }

    private fun parse(l: String): Entry? {
        val p = l.split('\t'); if (p.size < 3 || l.startsWith("#")) return null
        return Entry(p[0], p[1], p[2].split("; "))
    }

    private val rare = listOf("(literary)", "(archaic)", "(old)", "(dialect)", "(Cantonese)", "(Taiwan)", "(Tw)", "(slang)", "(vulgar)", "(classical)", "(Buddhism)", "(math.)", "(chemistry)", "(medicine)", "(physics)", "(botany)", "(zoology)", "(bound form)")

    /** A meaning without notes in brackets and without the "to " of verbs. */
    private fun core(m: String) = m.replace(Regex("\\([^)]*\\)"), "").trim().removePrefix("to ").trim().lowercase()

    fun search(query: String, common: Set<String> = emptySet(), limit: Int = 6): List<Entry> {
        val q = query.trim().lowercase().removePrefix("to ").trim()
        if (q.isEmpty()) return emptyList()
        val hanQ = q.filter { it in '一'..'鿿' }
        if (hanQ.isNotEmpty()) return lines.asSequence().filter { it.startsWith(hanQ) }.mapNotNull(::parse)
            .sortedBy { it.hanzi.length }.take(limit).toList()
        val word = Regex("(^|[^a-z])" + Regex.escape(q) + "([^a-z]|$)")
        return lines.asSequence()
            .filter { !it.startsWith("#") && it.contains(q, ignoreCase = true) }
            .mapNotNull(::parse)
            .mapNotNull { e ->
                var best = 0
                // CC-CEDICT often marks only the first sense "(archaic)"; then the whole entry is.
                val oldEntry = rare.any { e.meanings.first().startsWith(it) }
                for (m in e.meanings) {
                    val c = core(m)
                    val s = when {
                        c == q -> 100
                        c.startsWith("$q ") || c.startsWith("$q,") -> 55
                        word.containsMatchIn(c) -> 25
                        else -> 0
                    } - (if (oldEntry || rare.any { m.contains(it, ignoreCase = true) }) 90 else 0) // never rescued by being common
                    if (s > best) best = s
                }
                val known = e.hanzi in common
                // Words the app already teaches are the everyday ones; a lone uncommon character rarely is.
                val bonus = (if (known) 60 else 0) - (if (!known && e.hanzi.length == 1) 30 else 0)
                if (best <= 0) null else e to (best + bonus - e.hanzi.length - e.meanings.indexOfFirst { core(it) == q }.let { if (it > 0) it * 3 else 0 })
            }
            .sortedByDescending { it.second }.take(limit).map { it.first }.toList()
    }
}
