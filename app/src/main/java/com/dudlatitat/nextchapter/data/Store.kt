package com.dudlatitat.nextchapter.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    // One writer, newest state wins (see Saver): declared before _state, which saves while seeding.
    private val tmp = File(context.filesDir, "state.json.tmp")
    private val saver = Saver<AppState>(scope) { s ->
        // Write a temp file, force it onto the disk (sync), then swap it in: a crash or a dead
        // battery mid-save leaves either the old file or the new one, never half of one.
        java.io.FileOutputStream(tmp).use { out ->
            out.write(json.encodeToString(AppState.serializer(), s).toByteArray())
            out.fd.sync()
        }
        if (!tmp.renameTo(file)) { tmp.copyTo(file, overwrite = true); tmp.delete() }
    }
    // Widgets and other listeners hear about changes off the main thread, newest state only.
    private val notifier = Saver<AppState>(scope) { s -> onChange?.invoke(s) }

    private val _state = MutableStateFlow(seed(load()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    private fun read(f: File): AppState? = try {
        if (f.exists() && f.length() > 0) json.decodeFromString(AppState.serializer(), f.readText()) else null
    } catch (_: Exception) { null }

    private fun load(): AppState {
        read(file)?.let { return it }
        // A broken or empty file: keep it aside rather than lose it, then try the last temp copy.
        if (file.exists()) file.renameTo(File(file.parentFile, "state-broken-${System.currentTimeMillis()}.json"))
        return read(tmp) ?: AppState()
    }

    /** Adds starter phrases and tasks the saved state doesn't have yet. */
    private fun seed(s: AppState): AppState {
        if (s.seeded >= Seed.VERSION) return s
        val haveTasks = s.tasks.map { it.id }.toSet()
        return s.copy(
            cards = Seed.merge(s.cards),
            tasks = s.tasks + Seed.tasks.filter { it.id !in haveTasks },
            packing = s.packing.ifEmpty { Seed.packing },
            budget = s.budget.ifEmpty { Seed.budget },
            seeded = Seed.VERSION,
        ).also { save(it) }
    }

    /** Called after every change (the widget listens). */
    var onChange: ((AppState) -> Unit)? = null

    fun edit(change: (AppState) -> AppState) {
        _state.update(change)
        save(_state.value)
        notifier.save(_state.value)
    }

    /** Replaces everything with a backup; false if the text isn't a Next Chapter backup. */
    fun restore(text: String): Boolean {
        val s = try { json.decodeFromString(AppState.serializer(), text) } catch (_: Exception) { return false }
        if (s.cards.isEmpty() && s.tasks.isEmpty()) return false
        edit { seed(s.copy(seeded = 0)) }
        return true
    }

    private fun save(s: AppState) = saver.save(s)

    /** The whole state as text, for a manual backup. */
    fun export(): String = json.encodeToString(AppState.serializer(), _state.value)

    companion object {
        @Volatile private var instance: Store? = null

        /** One store for the whole app (the main screen, quick-add, widget and reminder share it). */
        fun get(context: Context): Store = instance ?: synchronized(this) {
            instance ?: Store(context.applicationContext).also { st ->
                // Widgets follow every change, whoever made it (app, computer page, Drive, arrivals).
                val app = context.applicationContext
                st.onChange = { s ->
                    com.dudlatitat.nextchapter.widget.PhraseWidget.refresh(app, s)
                    com.dudlatitat.nextchapter.widget.WeekWidget.refresh(app, s)
                }
                instance = st
            }
        }
    }
}
