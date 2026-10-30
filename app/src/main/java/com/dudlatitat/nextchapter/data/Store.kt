package com.dudlatitat.nextchapter.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Everything the app knows, kept in one JSON file on the phone. No account, no server:
 * it works offline and in China without a VPN.
 */
class Store private constructor(context: Context) {
    private val file = File(context.filesDir, "state.json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeLock = Mutex()

    private val _state = MutableStateFlow(seed(load()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    private fun load(): AppState = try {
        if (file.exists()) json.decodeFromString(AppState.serializer(), file.readText()) else AppState()
    } catch (_: Exception) {
        // A broken file: keep it aside rather than lose it, and start fresh.
        file.renameTo(File(file.parentFile, "state-broken-${System.currentTimeMillis()}.json"))
        AppState()
    }

    /** Adds starter phrases and tasks the saved state doesn't have yet. */
    private fun seed(s: AppState): AppState {
        if (s.seeded >= Seed.VERSION) return s
        val haveCards = s.cards.map { it.id }.toSet()
        val haveTasks = s.tasks.map { it.id }.toSet()
        return s.copy(
            cards = s.cards + Seed.cards.filter { it.id !in haveCards },
            tasks = s.tasks + Seed.tasks.filter { it.id !in haveTasks },
            seeded = Seed.VERSION,
        ).also { save(it) }
    }

    /** Called after every change (the widget listens). */
    var onChange: ((AppState) -> Unit)? = null

    fun edit(change: (AppState) -> AppState) {
        _state.update(change)
        save(_state.value)
        onChange?.invoke(_state.value)
    }

    /** Replaces everything with a backup; false if the text isn't a Next Chapter backup. */
    fun restore(text: String): Boolean {
        val s = try { json.decodeFromString(AppState.serializer(), text) } catch (_: Exception) { return false }
        if (s.cards.isEmpty() && s.tasks.isEmpty()) return false
        edit { seed(s.copy(seeded = 0)) }
        return true
    }

    private fun save(s: AppState) {
        scope.launch {
            writeLock.withLock {
                val tmp = File(file.parentFile, "state.json.tmp")
                tmp.writeText(json.encodeToString(AppState.serializer(), s))
                tmp.renameTo(file)
            }
        }
    }

    /** The whole state as text, for a manual backup. */
    fun export(): String = json.encodeToString(AppState.serializer(), _state.value)

    companion object {
        @Volatile private var instance: Store? = null

        /** One store for the whole app (the main screen, quick-add, widget and reminder share it). */
        fun get(context: Context): Store = instance ?: synchronized(this) {
            instance ?: Store(context.applicationContext).also { instance = it }
        }
    }
}
