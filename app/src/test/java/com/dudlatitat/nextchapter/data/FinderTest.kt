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

class GlobalSearchTest {
    private val corpus = listOf(
        Finder.Hit("你好", "nǐ hǎo", "hello", "phrase"),
        Finder.Hit("你到家了吗？", "nǐ dào jiā le ma?", "are you home yet?", "phrase"),
        Finder.Hit("妈妈", "māma", "mom", "phrase"),
        Finder.Hit("我很累", "wǒ hěn lèi", "I'm really tired", "Texts"),
    )

    @Test fun pinyinWithOrWithoutTonesAndSpaces() {
        for (q in listOf("nihao", "ni hao", "nǐ hǎo", "NIHAO")) assertEquals(q, "你好", Finder.search(q, corpus).firstOrNull()?.hanzi)
        assertEquals("dao jia finds it mid-phrase", "你到家了吗？", Finder.search("dao jia", corpus).firstOrNull()?.hanzi)
        // Only from the start of a syllable: "ao" must not match inside "hao" or "dao".
        assertTrue(Finder.search("ao", corpus).none { it.hanzi == "你好" })
    }

    @Test fun charactersAndEnglish() {
        assertEquals(listOf("你好", "你到家了吗？"), Finder.search("你", corpus).map { it.hanzi })
        assertEquals("我很累", Finder.search("tired", corpus).first().hanzi)
        assertTrue(Finder.search("   ", corpus).isEmpty())
    }
}
