package com.dudlatitat.nextchapter.lookup

import android.content.Context
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Dict
import com.dudlatitat.nextchapter.data.Finder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * "How do I say…" on the phone and the computer: the app's own phrases first, then words from the
 * offline dictionary (assets/cedict.tsv, built in CI; may be missing in a build if the download
 * failed, then only phrases are searched). Loaded once, in the background (~6 MB of text).
 */
object Lookup {
    data class Result(val phrases: List<Finder.Hit>, val words: List<Dict.Entry>, val hasDict: Boolean)

    private val lock = Mutex()
    @Volatile private var dict: Dict? = null
    @Volatile private var missing = false

    suspend fun dict(context: Context): Dict? = lock.withLock {
        dict ?: if (missing) null else withContext(Dispatchers.IO) {
            try {
                Dict(context.assets.open("cedict.tsv").bufferedReader().readLines()).also { dict = it }
            } catch (_: Exception) { missing = true; null }
        }
    }

    suspend fun search(context: Context, state: AppState, query: String): Result = withContext(Dispatchers.Default) {
        val corpus = Finder.corpus(state)
        val phrases = Finder.phrases(query, corpus)
        val d = dict(context)
        val words = d?.search(query, Finder.common(corpus)).orEmpty()
        Result(phrases, words, d != null)
    }

    /** The global search: many more results, pinyin with or without tones (see [Finder.search]). */
    suspend fun searchAll(context: Context, state: AppState, query: String): Result = withContext(Dispatchers.Default) {
        val corpus = Finder.corpus(state)
        val phrases = Finder.search(query, corpus, limit = 40)
        val d = dict(context)
        val have = phrases.map { it.hanzi }.toSet()
        val words = d?.search(query, Finder.common(corpus), limit = 12).orEmpty().filter { it.hanzi !in have }
        Result(phrases, words, d != null)
    }
}
