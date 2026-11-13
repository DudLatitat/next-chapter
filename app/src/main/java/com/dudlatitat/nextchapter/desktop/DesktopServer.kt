package com.dudlatitat.nextchapter.desktop

import android.content.Context
import com.dudlatitat.nextchapter.data.CardEdits
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.Decks
import com.dudlatitat.nextchapter.data.Grade
import com.dudlatitat.nextchapter.data.Pinyin
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Stats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

@Serializable data class DCard(val id: String, val hanzi: String, val pinyin: String, val english: String, val deck: String, val isNew: Boolean, val custom: Boolean, val known: Boolean, val voice: String)
@Serializable data class DDeck(val id: String, val title: String, val hanzi: String, val count: Int, val started: Int)
@Serializable data class DSummary(val due: Int, val fresh: Int, val streak: Int, val known: Int, val decks: List<DDeck>)
@Serializable data class PairIn(val code: String)
@Serializable data class PairOut(val token: String)
@Serializable data class GradeIn(val id: String, val grade: Int)
@Serializable data class CardIn(val id: String? = null, val hanzi: String, val pinyin: String = "", val english: String = "")
@Serializable data class IdIn(val id: String)
@Serializable data class PinyinOut(val pinyin: String)
@Serializable data class Msg(val ok: Boolean, val message: String = "")

/**
 * "Use on my computer": a small web server on the phone, for the desktop page (assets/desktop).
 * Run by DesktopService. Only answers the home network; a computer pairs once with the 6-digit
 * code and then keeps its token (saved here too, so it survives restarts until "Forget"). Every
 * change goes through Store with the same CardEdits the phone uses, so the phone's data is the
 * only copy and nothing needs syncing.
 */
class DesktopServer(private val context: Context, private val store: Store, val port: Int = 8090) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var scope: CoroutineScope? = null
    private var socket: ServerSocket? = null
    private val prefs = context.getSharedPreferences("desktop", Context.MODE_PRIVATE)
    private val tokens = (prefs.getStringSet("tokens", emptySet()) ?: emptySet()).toMutableSet()
    /** Wakes the phone briefly after each request, so a page in use stays quick with the screen off. */
    private val wake = (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
        .newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "NextChapter:desktop").apply { setReferenceCounted(false) }
    private var wrongCodes = 0

    private val _code = MutableStateFlow(Http.pairingCode())
    /** The code to type on the computer (a new one each start, and after 5 wrong tries). */
    val code: StateFlow<String> = _code
    private val _paired = MutableStateFlow(tokens.size)
    /** How many computers are paired (remembered until "Forget my computers"). */
    val paired: StateFlow<Int> = _paired

    /** Unpairs every computer; they'll need the code again. */
    fun forgetAll() {
        synchronized(tokens) { tokens.clear(); prefs.edit().putStringSet("tokens", emptySet()).apply() }
        _paired.value = 0
        _code.value = Http.pairingCode()
    }
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    companion object {
        /** This phone's addresses on the home network (what to type on the computer). */
        fun localAddresses(): List<String> = try {
            java.net.NetworkInterface.getNetworkInterfaces().toList().filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filter { it is java.net.Inet4Address && it.isSiteLocalAddress }
                .map { it.hostAddress ?: "" }.filter { it.isNotEmpty() }.distinct()
        } catch (_: Exception) { emptyList() }
    }

    fun start() {
        if (scope != null) return
        val s = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = s
        s.launch {
            try {
                val server = ServerSocket().apply { reuseAddress = true; bind(InetSocketAddress(port)) }
                socket = server
                while (isActive) {
                    val client = server.accept()
                    launch { handle(client) }
                }
            } catch (e: Exception) {
                if (scope != null) _error.value = e.message ?: "Couldn't start"
            }
        }
    }

    fun stop() {
        val s = scope; scope = null // first, so the closing socket isn't reported as an error
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        s?.cancel()
        if (wake.isHeld) wake.release()
    }

    private fun handle(client: Socket) {
        client.use { c ->
            try {
                c.soTimeout = 10_000
                if (!Http.isLocal(c.inetAddress)) { c.getOutputStream().write(Http.response(403, "text/plain", "Home network only".toByteArray())); return }
                val input = c.getInputStream()
                // Read the head (up to the blank line), then the body by Content-Length.
                val head = ByteArrayOutputStream()
                var last4 = 0
                while (true) {
                    val b = input.read(); if (b < 0) return
                    head.write(b); last4 = (last4 shl 8) or b
                    if (last4 == 0x0D0A0D0A) break
                    if (head.size() > 16 * 1024) return
                }
                val (method, target, headers) = Http.parseHead(head.toString(Charsets.UTF_8.name()).trimEnd()) ?: return
                val length = headers["content-length"]?.toIntOrNull() ?: 0
                if (length > Http.MAX_BODY) { c.getOutputStream().write(Http.response(413, "text/plain", ByteArray(0))); return }
                val body = ByteArray(length).also { var n = 0; while (n < length) { val r = input.read(it, n, length - n); if (r < 0) break; n += r } }
                val (path, query) = Http.splitTarget(target)
                wake.acquire(10 * 60 * 1000L)
                c.getOutputStream().write(route(method, path, query, headers, String(body, Charsets.UTF_8)))
            } catch (_: Exception) {}
        }
    }

    private fun ok(any: String) = Http.response(200, "application/json; charset=utf-8", any.toByteArray())
    private fun bad(status: Int, message: String) = Http.response(status, "application/json; charset=utf-8", json.encodeToString(Msg(false, message)).toByteArray())

    private fun DCardOf(c: Card) = DCard(c.id, c.hanzi, c.pinyin, c.english, c.deck, c.isNew, c.custom, c.isKnown, com.dudlatitat.nextchapter.Speaker.md5(c.hanzi.trim()))

    private fun route(method: String, path: String, query: Map<String, String>, headers: Map<String, String>, body: String): ByteArray {
        if (method == "GET" && (path == "/" || path == "/index.html")) {
            val page = context.assets.open("desktop/index.html").use { it.readBytes() }
            return Http.response(200, "text/html; charset=utf-8", page)
        }
        if (method == "POST" && path == "/api/pair") {
            val code = try { json.decodeFromString<PairIn>(body).code.trim() } catch (_: Exception) { "" }
            if (!Http.same(code, _code.value)) {
                // Too many wrong guesses: a new code, so the 6 digits can't be guessed by trying them all.
                if (++wrongCodes >= 5) { wrongCodes = 0; _code.value = Http.pairingCode() }
                return bad(401, "That code doesn't match. Check the phone.")
            }
            wrongCodes = 0
            val t = Http.token()
            synchronized(tokens) { tokens += t; prefs.edit().putStringSet("tokens", tokens.toSet()).apply() }
            _paired.value = tokens.size
            return ok(json.encodeToString(PairOut(t)))
        }
        val token = headers["x-token"] ?: query["t"]
        if (synchronized(tokens) { tokens.none { Http.same(it, token) } }) return bad(401, "Not paired")

        Http.voiceAsset(path)?.let { asset ->
            val bytes = try { context.assets.open(asset).use { it.readBytes() } } catch (_: Exception) { return bad(404, "No recording") }
            return Http.response(200, "audio/mpeg", bytes)
        }
        val s = store.state.value
        return when ("$method $path") {
            "GET /api/summary" -> {
                val decks = Decks.mapNotNull { d ->
                    val cs = s.cards.filter { it.deck == d.id }
                    if (cs.isEmpty()) null else DDeck(d.id, d.title, d.hanzi, cs.size, cs.count { !it.isNew })
                }
                val q = Srs.queue(s)
                ok(json.encodeToString(DSummary(q.count { !it.isNew }, q.count { it.isNew }, Stats.streak(s), s.cards.count { it.isKnown }, decks)))
            }
            "GET /api/queue" -> ok(json.encodeToString(Srs.queue(s).map { DCardOf(it) }))
            "GET /api/cards" -> {
                val deck = query["deck"].orEmpty(); val q = query["q"].orEmpty().trim().lowercase()
                val list = s.cards.filter { (deck.isEmpty() || it.deck == deck) && (q.isEmpty() || q in it.hanzi || q in it.pinyin.lowercase() || q in it.english.lowercase()) }
                ok(json.encodeToString(list.take(3000).map { DCardOf(it) }))
            }
            "POST /api/grade" -> {
                val g = try { json.decodeFromString<GradeIn>(body) } catch (_: Exception) { return bad(400, "Bad grade") }
                val card = s.cards.firstOrNull { it.id == g.id } ?: return bad(404, "No such card")
                val grade = Grade.entries.getOrNull(g.grade - 1) ?: return bad(400, "Grade is 1–4")
                store.edit { CardEdits.grade(it, card.id, grade) }
                ok(json.encodeToString(Msg(true)))
            }
            "POST /api/card" -> {
                val c = try { json.decodeFromString<CardIn>(body) } catch (_: Exception) { return bad(400, "Bad card") }
                if (c.hanzi.isBlank() && c.id == null) return bad(400, "Chinese is needed")
                val pinyin = c.pinyin.ifBlank { Pinyin.fromHanzi(c.hanzi) ?: "" }
                if (c.id == null) store.edit { CardEdits.add(it, c.hanzi.take(60), pinyin.take(120), c.english.take(120)) }
                else store.edit { CardEdits.update(it, c.id, c.hanzi.take(60), pinyin.take(120), c.english.take(120)) }
                ok(json.encodeToString(Msg(true)))
            }
            "POST /api/delete" -> {
                val id = try { json.decodeFromString<IdIn>(body).id } catch (_: Exception) { return bad(400, "Bad id") }
                val card = s.cards.firstOrNull { it.id == id && it.custom } ?: return bad(403, "Only your own phrases can be deleted")
                store.edit { CardEdits.delete(it, card.id) }
                ok(json.encodeToString(Msg(true)))
            }
            "GET /api/pinyin" -> ok(json.encodeToString(PinyinOut(Pinyin.fromHanzi(query["h"].orEmpty()) ?: "")))
            else -> bad(404, "Not found")
        }
    }
}
