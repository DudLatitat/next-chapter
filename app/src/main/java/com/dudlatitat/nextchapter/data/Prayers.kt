package com.dudlatitat.nextchapter.data

/** A Catholic prayer in English, with the Chinese used by Catholics in mainland China and its pinyin. */
data class Prayer(val title: String, val titleZh: String, val english: String, val hanzi: String, val pinyin: String)

/**
 * The morning card's daily prayer, one per day in turn, always opened with the Sign of the Cross.
 * Chinese texts follow the mainland Catholic prayer book (天主经, 圣母经…); check them against a
 * Chinese Catholic prayer book before relying on them.
 */
object Prayers {
    val signOfTheCross = Prayer(
        "Sign of the Cross", "圣号经",
        "In the name of the Father, and of the Son, and of the Holy Spirit. Amen.",
        "因父及子及圣神之名。阿们。",
        "Yīn Fù jí Zǐ jí Shèngshén zhī míng. Āmen.",
    )

    val daily = listOf(
        Prayer(
            "Our Father", "天主经",
            "Our Father, who art in heaven, hallowed be thy name; thy kingdom come, thy will be done on earth as it is in heaven. " +
                "Give us this day our daily bread, and forgive us our trespasses, as we forgive those who trespass against us; " +
                "and lead us not into temptation, but deliver us from evil. Amen.",
            "我们的天父，愿你的名受显扬；愿你的国来临；愿你的旨意奉行在人间，如同在天上。求你今天赏给我们日用的食粮；求你宽恕我们的罪过，如同我们宽恕别人一样；不要让我们陷于诱惑；但救我们免于凶恶。阿们。",
            "Wǒmen de Tiānfù, yuàn nǐ de míng shòu xiǎnyáng; yuàn nǐ de guó láilín; yuàn nǐ de zhǐyì fèngxíng zài rénjiān, rútóng zài tiānshàng. " +
                "Qiú nǐ jīntiān shǎng gěi wǒmen rìyòng de shíliáng; qiú nǐ kuānshù wǒmen de zuìguò, rútóng wǒmen kuānshù biérén yíyàng; " +
                "búyào ràng wǒmen xiànyú yòuhuò; dàn jiù wǒmen miǎnyú xiōng'è. Āmen.",
        ),
        Prayer(
            "Hail Mary", "圣母经",
            "Hail Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is the fruit of thy womb, Jesus. " +
                "Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death. Amen.",
            "万福玛利亚，你充满圣宠，主与你同在，你在妇女中受赞颂，你的亲子耶稣同受赞颂。天主圣母玛利亚，求你现在和我们临终时，为我们罪人祈求天主。阿们。",
            "Wànfú Mǎlìyà, nǐ chōngmǎn shèngchǒng, Zhǔ yǔ nǐ tóngzài, nǐ zài fùnǚ zhōng shòu zànsòng, nǐ de qīnzǐ Yēsū tóng shòu zànsòng. " +
                "Tiānzhǔ Shèngmǔ Mǎlìyà, qiú nǐ xiànzài hé wǒmen línzhōng shí, wèi wǒmen zuìrén qíqiú Tiānzhǔ. Āmen.",
        ),
        Prayer(
            "Glory Be", "圣三光荣经",
            "Glory be to the Father, and to the Son, and to the Holy Spirit, as it was in the beginning, is now, and ever shall be, world without end. Amen.",
            "愿光荣归于父、及子、及圣神，起初如何，今日亦然，直到永远。阿们。",
            "Yuàn guāngróng guīyú Fù, jí Zǐ, jí Shèngshén, qǐchū rúhé, jīnrì yì rán, zhídào yǒngyuǎn. Āmen.",
        ),
        Prayer(
            "Jesus, I trust in You", "耶稣，我信赖你",
            "Jesus, I trust in You.",
            "耶稣，我信赖你！",
            "Yēsū, wǒ xìnlài nǐ!",
        ),
        Prayer(
            "Lord, have mercy", "垂怜经",
            "Lord, have mercy. Christ, have mercy. Lord, have mercy.",
            "上主，求你垂怜。基督，求你垂怜。上主，求你垂怜。",
            "Shàngzhǔ, qiú nǐ chuílián. Jīdū, qiú nǐ chuílián. Shàngzhǔ, qiú nǐ chuílián.",
        ),
    )

    /** Today's prayer: they take turns, one per day. */
    fun forDay(day: Long): Prayer = daily[Math.floorMod(day, daily.size.toLong()).toInt()]
}
