package com.dudlatitat.nextchapter.desktop

import java.net.Inet4Address
import java.net.InetAddress
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * The tiny bits of HTTP the desktop page needs, by hand (no server library): one request per
 * connection, small bodies. Pure, tested in HttpTest.
 */
object Http {
    const val MAX_BODY = 64 * 1024

    data class Request(val method: String, val path: String, val query: Map<String, String>, val headers: Map<String, String>, val body: String)

    /** Parses the request line and headers ("GET /api/queue?x=1 HTTP/1.1" …); null if it isn't HTTP. */
    fun parseHead(head: String): Triple<String, String, Map<String, String>>? {
        val lines = head.split("\r\n")
        val first = lines.firstOrNull()?.split(" ") ?: return null
        if (first.size < 3 || !first[2].startsWith("HTTP/")) return null
        val headers = lines.drop(1).mapNotNull { l -> l.indexOf(':').takeIf { it > 0 }?.let { l.substring(0, it).trim().lowercase() to l.substring(it + 1).trim() } }.toMap()
        return Triple(first[0].uppercase(), first[1], headers)
    }

    /** "/api/cards?deck=games&q=玩" → path and decoded query. */
    fun splitTarget(target: String): Pair<String, Map<String, String>> {
        val q = target.indexOf('?')
        if (q < 0) return target to emptyMap()
        val query = target.substring(q + 1).split('&').filter { it.isNotEmpty() }.associate { p ->
            val e = p.indexOf('=')
            if (e < 0) dec(p) to "" else dec(p.substring(0, e)) to dec(p.substring(e + 1))
        }
        return target.substring(0, q) to query
    }

    private fun dec(s: String) = try { URLDecoder.decode(s, "UTF-8") } catch (_: Exception) { s }

    /** Only devices on the same home network may connect (private IPv4 ranges, or the phone itself). */
    fun isLocal(a: InetAddress): Boolean = a.isLoopbackAddress || (a is Inet4Address && a.isSiteLocalAddress)

    fun response(status: Int, type: String, body: ByteArray): ByteArray {
        val reason = when (status) { 200 -> "OK"; 400 -> "Bad Request"; 401 -> "Unauthorized"; 403 -> "Forbidden"; 404 -> "Not Found"; 413 -> "Too Large"; else -> "Error" }
        val head = "HTTP/1.1 $status $reason\r\nContent-Type: $type\r\nContent-Length: ${body.size}\r\n" +
            "Cache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nConnection: close\r\n\r\n"
        return head.toByteArray(Charsets.UTF_8) + body
    }

    /** A fresh 6-digit pairing code, shown on the phone. */
    fun pairingCode(random: SecureRandom = SecureRandom()): String = (random.nextInt(900_000) + 100_000).toString()

    /** A long random token the page keeps after pairing (sent as X-Token). */
    fun token(random: SecureRandom = SecureRandom()): String = ByteArray(24).also(random::nextBytes).joinToString("") { "%02x".format(it) }

    /** Compares secrets in constant time (so timing doesn't leak how much matched). */
    fun same(a: String?, b: String?): Boolean =
        a != null && b != null && MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    /** Asset path for a voice recording request: only "<32 hex>.mp3" is allowed (no ../ tricks). */
    fun voiceAsset(path: String): String? =
        Regex("^/voice/([0-9a-f]{32})\\.mp3$").find(path)?.let { "voice/${it.groupValues[1]}.mp3" }
}
