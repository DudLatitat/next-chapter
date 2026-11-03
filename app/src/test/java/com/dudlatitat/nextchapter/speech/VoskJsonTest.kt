package com.dudlatitat.nextchapter.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoskJsonTest {
    @Test fun joinsChineseWordsAndKeepsTiming() {
        val r = VoskJson.parse(
            """{"result":[{"conf":0.98,"end":0.84,"start":0.42,"word":"你好"},{"conf":0.71,"end":1.5,"start":0.9,"word":"妈妈"}],"text":"你好 妈妈"}""",
        )
        assertTrue(r.ok)
        assertEquals("你好妈妈", r.text)
        assertEquals(2, r.words.size)
        assertEquals("妈妈", r.words[1].text)
        assertEquals(0.9f, r.words[1].start, 1e-4f)
        assertEquals(1.5f, r.words[1].end, 1e-4f)
    }

    @Test fun nothingHeard() {
        val r = VoskJson.parse("""{"text":""}""")
        assertTrue(r.ok)
        assertEquals("", r.text)
        assertTrue(r.words.isEmpty())
    }

    @Test fun unknownWordIsDropped() {
        val r = VoskJson.parse("""{"result":[{"conf":1.0,"end":0.5,"start":0.1,"word":"[unk]"}],"text":"[unk]"}""")
        assertEquals("", r.text)
        assertTrue(r.words.isEmpty())
    }

    @Test fun brokenAnswerIsAnError() {
        assertTrue(!VoskJson.parse("not json").ok)
    }

    @Test fun grammarListsWordsPlusUnknown() {
        assertEquals("""["汤","糖","躺","烫","[unk]"]""", VoskJson.grammar(listOf("汤", "糖", "躺", "烫", "汤")))
    }
}
