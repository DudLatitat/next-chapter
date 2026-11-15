package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinderTest {
    private val corpus = Finder.corpus(AppState(cards = Seed.cards))

    @Test fun englishFindsTheSentence() {
        assertEquals("到家了吗？", Finder.phrases("are you home yet", corpus).first().hanzi)
        assertTrue(Finder.phrases("miss you", corpus).any { "想你" in it.hanzi })
        assertTrue("stems: missing → miss", Finder.phrases("missing you", corpus).any { "想你" in it.hanzi })
        assertTrue("nothing for gibberish", Finder.phrases("xqzt", corpus).isEmpty())
    }

    @Test fun charactersAndPinyinWorkToo() {
        assertTrue(Finder.phrases("到家", corpus).all { "到家" in it.hanzi })
        assertTrue(Finder.phrases("daojia", corpus).any { "到家" in it.hanzi })
    }

    @Test fun howToTypeIt() {
        assertEquals("dao jia le ma", Finder.typeKeys("dào jiā le ma?"))
        assertEquals("nv er", Finder.typeKeys("nǚ ér"))
    }

    @Test fun dictionaryRanksTheEverydayWordFirst() {
        val d = Dict(listOf(
            "#\tCC-CEDICT",
            "累\tlěi\tto accumulate; continuous",
            "累\tlèi\ttired; weary; to work hard",
            "疲倦\tpí juàn\tto tire; tired",
            "倦怠\tjuàn dài\t(literary) tired; weary",
            "明天\tmíng tiān\ttomorrow",
            "明日\tmíng rì\t(literary) tomorrow",
            "那\tnuó\t(archaic) many; beautiful",
            "美\tměi\tbeautiful",
        ))
        assertEquals(8, d.size)
        assertEquals("a sense marked only on the entry's first meaning still counts as archaic", "美", d.search("beautiful", common = setOf("那", "美")).first().hanzi)
        assertEquals("the everyday word wins when the app knows it", "lèi", d.search("tired", common = setOf("累")).first().pinyin)
        assertEquals("明天", d.search("tomorrow").first().hanzi)
        assertEquals("literary sinks", "倦怠", d.search("tired", common = setOf("累", "倦怠")).last().hanzi)
        assertEquals("common words rise", "疲倦", d.search("tired", common = setOf("疲倦")).first().hanzi)
        assertEquals("明天", d.search("明").first().hanzi)
        assertTrue(d.search("").isEmpty())
    }
}
