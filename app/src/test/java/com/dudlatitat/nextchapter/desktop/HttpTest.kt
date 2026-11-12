package com.dudlatitat.nextchapter.desktop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class HttpTest {
    @Test fun readsTheRequestLineAndHeaders() {
        val (m, t, h) = Http.parseHead("POST /api/grade HTTP/1.1\r\nHost: 192.168.1.5:8090\r\nX-Token: abc\r\nContent-Length: 12")!!
        assertEquals("POST", m); assertEquals("/api/grade", t)
        assertEquals("abc", h["x-token"]); assertEquals("12", h["content-length"])
        assertNull(Http.parseHead("hello"))
    }

    @Test fun decodesTheQuery() {
        val (p, q) = Http.splitTarget("/api/cards?deck=games&q=%E7%8E%A9")
        assertEquals("/api/cards", p); assertEquals("games", q["deck"]); assertEquals("玩", q["q"])
        assertEquals("/" to emptyMap<String, String>(), Http.splitTarget("/"))
    }

    @Test fun onlyTheHomeNetworkMayConnect() {
        assertTrue(Http.isLocal(InetAddress.getByName("192.168.1.20")))
        assertTrue(Http.isLocal(InetAddress.getByName("10.0.0.7")))
        assertTrue(Http.isLocal(InetAddress.getByName("172.16.4.2")))
        assertTrue(Http.isLocal(InetAddress.getByName("127.0.0.1")))
        assertFalse(Http.isLocal(InetAddress.getByName("8.8.8.8")))
        assertFalse(Http.isLocal(InetAddress.getByName("172.32.0.1")))
    }

    @Test fun secretsAndCodes() {
        val code = Http.pairingCode()
        assertEquals(6, code.length); assertTrue(code.all { it.isDigit() })
        assertEquals(48, Http.token().length)
        assertTrue(Http.same("abc", "abc")); assertFalse(Http.same("abc", "abd")); assertFalse(Http.same(null, "abc"))
    }

    @Test fun voiceFilesOnlyByTheirName() {
        assertEquals("voice/0123456789abcdef0123456789abcdef.mp3", Http.voiceAsset("/voice/0123456789abcdef0123456789abcdef.mp3"))
        assertNull(Http.voiceAsset("/voice/../vosk-cn/uuid"))
        assertNull(Http.voiceAsset("/voice/xyz.mp3"))
    }

    @Test fun responsesSayHowLongTheyAre() {
        val r = String(Http.response(200, "text/plain", "hi".toByteArray()))
        assertTrue(r.startsWith("HTTP/1.1 200 OK\r\n")); assertTrue("Content-Length: 2" in r); assertTrue(r.endsWith("\r\n\r\nhi"))
    }
}
