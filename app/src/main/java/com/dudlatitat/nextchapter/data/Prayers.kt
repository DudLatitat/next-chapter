package com.dudlatitat.nextchapter.data

/**
 * A Catholic prayer: English in short reading lines, the full Chinese (for "play all"), and
 * the Chinese line by line with each line's pinyin, as the source lays it out.
 */
data class Prayer(
    val title: String,
    val titleZh: String,
    val english: List<String>,
    val hanzi: String,
    val lines: List<Pair<String, String>>,
)

/**
 * The morning card's daily prayer, one per day in turn, always opened with the Sign of the Cross.
 * Chinese and pinyin are copied exactly from dominicweb.eu/en/prayers/standard-chinese (Roman
 * Catholic, Simplified characters, its line breaks kept; the pinyin is the site's approximate
 * transliteration). English is the Roman Missal wording. Don't edit the Chinese by hand: re-copy
 * from the site.
 */
object Prayers {
    val signOfTheCross = Prayer(
        "Sign of the Cross", "圣号经",
        english = listOf(
            "In the name of the Father, and of the Son, and of the Holy Spirit.",
            "Amen.",
        ),
        hanzi = "因父、及子、及圣神之名。阿门",
        lines = listOf(
            "因父、及子、及圣神之名。阿门" to "Yīn fù, jí zǐ, jí shèngshén zhī míng. Āmen",
        ),
    )

    val daily = listOf(
        Prayer(
            "Our Father", "天主经",
            english = listOf(
                "Our Father, who art in heaven, hallowed be thy name;",
                "thy kingdom come, thy will be done on earth as it is in heaven.",
                "Give us this day our daily bread, and forgive us our trespasses, as we forgive those who trespass against us;",
                "and lead us not into temptation, but deliver us from evil.",
                "Amen.",
            ),
            hanzi = "我们的天父，愿你的名受显扬；愿你的国来临；愿你的旨意奉行在人间，如同在天上。求你今天赏给我们日用的食粮；求你宽恕我们的罪过，如同我们宽恕别人一样；不要让我们陷于诱惑；但救我们免于凶恶。阿门",
            lines = listOf(
                "我们的天父，愿你的名受显扬；" to "Wǒmen de tiān fù, yuàn nǐ de míng shòu xiǎnyáng;",
                "愿你的国来临；" to "yuàn nǐ de guó láilín;",
                "愿你的旨意奉行在人间，如同在天上。" to "yuàn nǐ de zhǐyì fèngxíng zài rénjiān, rútóng zài tiānshàng.",
                "求你今天赏给我们日用的食粮；" to "Qiú nǐ jīntiān shǎng gěi wǒmen rìyòng de shíliáng;",
                "求你宽恕我们的罪过，如同我们宽恕别人一样；" to "qiú nǐ kuānshù wǒmen de zuìguò, rútóng wǒmen kuānshù biérén yīyàng;",
                "不要让我们陷于诱惑；" to "bùyào ràng wǒmen xiànyú yòuhuò;",
                "但救我们免于凶恶。" to "dàn jiù wǒmen miǎn yú xiōng'è.",
                "阿门" to "Āmen",
            ),
        ),
        Prayer(
            "Hail Mary", "圣母经",
            english = listOf(
                "Hail Mary, full of grace, the Lord is with thee.",
                "Blessed art thou among women, and blessed is the fruit of thy womb, Jesus.",
                "Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death.",
                "Amen.",
            ),
            hanzi = "万福玛利亚，你充满圣宠，主与你同在，你在妇女中受赞颂，你的亲子耶稣，同受赞颂。天主圣母玛利亚，求你现在和我们临终时，为我们罪人祈求天主。阿门",
            lines = listOf(
                "万福玛利亚，你充满圣宠，" to "Wànfú Mǎlìyà, nǐ chōngmǎn shèng chǒng,",
                "主与你同在，你在妇女中受赞颂，" to "zhǔ yǔ nǐ tóng zài, nǐ zài fùnǚ zhōng shòu zànsòng,",
                "你的亲子耶稣，同受赞颂。" to "nǐ de qīnzǐ Yēsū, tóng shòu zànsòng.",
                "天主圣母玛利亚，" to "Tiānzhǔ shèngmǔ Mǎlìyà,",
                "求你现在和我们临终时，" to "qiú nǐ xiànzài hé wǒmen línzhōng shí,",
                "为我们罪人祈求天主。" to "wèi wǒmen zuìrén qíqiú tiānzhǔ.",
                "阿门" to "Āmen",
            ),
        ),
        Prayer(
            "Glory Be", "圣三光荣经",
            english = listOf(
                "Glory be to the Father, and to the Son, and to the Holy Spirit, as it was in the beginning, is now, and ever shall be, world without end.",
                "Amen.",
            ),
            hanzi = "愿光荣归于父，及子，及圣神。起初如何，今日亦然，直到永远。阿门",
            lines = listOf(
                "愿光荣归于父，及子，及圣神。" to "Yuàn guāngróng guīyú fù, jí zǐ, jí shèngshén.",
                "起初如何，今日亦然，直到永远。" to "Qǐchū rúhé, jīnrì yìrán, zhídào yǒngyuǎn.",
                "阿门" to "Āmen",
            ),
        ),
        Prayer(
            "Apostles' Creed", "宗徒信经",
            english = listOf(
                "I believe in God, the Father almighty, Creator of heaven and earth, and in Jesus Christ, his only Son, our Lord, who was conceived by the Holy Spirit, born of the Virgin Mary, suffered under Pontius Pilate, was crucified, died and was buried;",
                "he descended into hell;",
                "on the third day he rose again from the dead;",
                "he ascended into heaven, and is seated at the right hand of God the Father almighty;",
                "from there he will come to judge the living and the dead.",
                "I believe in the Holy Spirit, the holy catholic Church, the communion of saints, the forgiveness of sins, the resurrection of the body, and life everlasting.",
                "Amen.",
            ),
            hanzi = "我信全能的天主父，天地万物的创造者。我信父的唯一子，我们的主耶稣基督。我信他因圣神降孕，由童贞玛利亚诞生。我信他在比拉多执政时蒙难，被钉在十字架上，死而安葬。我信他下降阴府，第三日从死者中复活。我信他升了天，坐在全能天主父的右边。我信他要从天降来，审判生者死者。我信圣神，我信圣而公教会，诸圣的相通。我信罪过的赦免，我信肉身的复活，我信永恒的生命。阿门",
            lines = listOf(
                "我信全能的天主父，" to "Wǒ xìn quánnéng de tiānzhǔ fù,",
                "天地万物的创造者。" to "tiāndì wànwù de chuàngzào zhě.",
                "我信父的唯一子，" to "Wǒ xìn fù de wéiyī zǐ,",
                "我们的主耶稣基督。" to "wǒmen de zhǔ Yēsū jīdū.",
                "我信他因圣神降孕，" to "Wǒ xìn tā yīn shèngshén jiàng yùn,",
                "由童贞玛利亚诞生。" to "yóu tóngzhēn mǎlìyà dànshēng.",
                "我信他在比拉多执政时蒙难，" to "Wǒ xìn tā zài Bǐlāduō zhízhèng shí méngnàn,",
                "被钉在十字架上，" to "bèi dīng zài shízìjià shàng,",
                "死而安葬。" to "sǐ ér ānzàng.",
                "我信他下降阴府，" to "Wǒ xìn tā xiàjiàng yīn fǔ,",
                "第三日从死者中复活。" to "dì sān rì cóng sǐzhě zhōng fùhuó.",
                "我信他升了天，" to "Wǒ xìn tā shēng le tiān,",
                "坐在全能天主父的右边。" to "zuò zài quánnéng tiānzhǔ fù de yòubiān.",
                "我信他要从天降来，" to "Wǒ xìn tā yào cóng tiān jiàng lái,",
                "审判生者死者。" to "shěnpàn shēng zhě sǐ zhě.",
                "我信圣神，" to "Wǒ xìn shèngshén,",
                "我信圣而公教会，" to "wǒ xìn shèng ér gōng jiàohuì,",
                "诸圣的相通。" to "zhū shèng de xiāngtōng.",
                "我信罪过的赦免，" to "Wǒ xìn zuìguò de shèmiǎn,",
                "我信肉身的复活，" to "wǒ xìn ròushēn de fùhuó,",
                "我信永恒的生命。" to "wǒ xìn yǒnghéng de shēngmìng.",
                "阿门" to "Āmen",
            ),
        ),
        Prayer(
            "Nicene Creed", "尼西亚信经",
            english = listOf(
                "I believe in one God, the Father almighty, maker of heaven and earth, of all things visible and invisible.",
                "I believe in one Lord Jesus Christ, the Only Begotten Son of God, born of the Father before all ages.",
                "God from God, Light from Light, true God from true God, begotten, not made, consubstantial with the Father;",
                "through him all things were made.",
                "For us men and for our salvation he came down from heaven, and by the Holy Spirit was incarnate of the Virgin Mary, and became man.",
                "For our sake he was crucified under Pontius Pilate, he suffered death and was buried, and rose again on the third day in accordance with the Scriptures.",
                "He ascended into heaven and is seated at the right hand of the Father.",
                "He will come again in glory to judge the living and the dead and his kingdom will have no end.",
                "I believe in the Holy Spirit, the Lord, the giver of life, who proceeds from the Father and the Son, who with the Father and the Son is adored and glorified, who has spoken through the prophets.",
                "I believe in one, holy, catholic and apostolic Church.",
                "I confess one Baptism for the forgiveness of sins and I look forward to the resurrection of the dead and the life of the world to come.",
                "Amen.",
            ),
            hanzi = "我信唯一的天主，全能的圣父，天地万物，无论有形无形，都是他所创造的。我信唯一的主、耶稣基督、天主的独生子。他在万世之前，由圣父所生。他是出自天主的天主，出自光明的光明，出自真天主的真天主。他是圣父所生，而非圣父所造，与圣父同性同体，万物是借着他而造成的。他为了我们人类，并为了我们的得救，从天降下。他因圣神，由童真玛利亚取得肉躯，而成为人。他在般雀比拉多执政时，为我们被钉在十字架上，受难而被埋葬。他正如圣经所载，第三日复活了。他升了天，坐在圣父的右边。他还要光荣地降来，审判生者死者， 他的神国万世无疆。我信圣神， 他是主及赋予生命者，由圣父圣子所共发。他和圣父圣子，同受钦崇，同享光荣，他曾借先知们发言。我信唯一、至圣、至公、从宗徒传下来的教会。我承认赦罪的圣洗，只有一个。我期待死人的复活，及来世的生命。阿门",
            lines = listOf(
                "我信唯一的天主，全能的圣父，" to "Wǒ xìn wéiyī de tiānzhǔ, quánnéng de shèng fù,",
                "天地万物，无论有形无形，都是他所创造的。" to "tiāndì wànwù, wúlùn yǒuxíng wúxíng, dōu shì tā suǒ chuàngzào de.",
                "我信唯一的主、耶稣基督、天主的独生子。" to "Wǒ xìn wéiyī de zhǔ, Yēsū jīdū, tiānzhǔ de dúshēngzǐ.",
                "他在万世之前，由圣父所生。" to "Tā zài wànshì zhīqián, yóu shèng fù suǒ shēng.",
                "他是出自天主的天主，出自光明的光明，出自真天主的真天主。" to "Tā shì chūzì tiānzhǔ de tiānzhǔ, chūzì guāngmíng de guāngmíng, chūzì zhēn tiānzhǔ de zhēn tiānzhǔ.",
                "他是圣父所生，而非圣父所造，与圣父同性同体，万物是借着他而造成的。" to "Tā shì shèng fù suǒ shēng, ér fēi shèng fù suǒ zào, yǔ shèng fù tóngxìng tóngtǐ, wànwù shì jiè zhe tā ér zàochéng de.",
                "他为了我们人类，并为了我们的得救，从天降下。" to "Tā wèile wǒmen rénlèi, bìng wèile wǒmen de déjiù, cóng tiān jiàngxià.",
                "他因圣神，由童真玛利亚取得肉躯，而成为人。" to "Tā yīn shèng shén, yóu tóngzhēn Mǎlìyà qǔdé ròuqū, ér chéngwéi rén.",
                "他在般雀比拉多执政时，为我们被钉在十字架上，受难而被埋葬。" to "Tā zài Bānquè Bǐlāduō zhízhèng shí, wèi wǒmen bèi dīng zài shízìjià shàng, shòunàn ér bèi máizàng.",
                "他正如圣经所载，第三日复活了。" to "Tā zhèngrú shèngjīng suǒ zài, dì sān rì fùhuó le.",
                "他升了天，坐在圣父的右边。" to "Tā shēng le tiān, zuò zài shèng fù de yòubiān.",
                "他还要光荣地降来，审判生者死者， 他的神国万世无疆。" to "Tā hái yào guāngróng de jiànglái, shěnpàn shēng zhě sǐ zhě, tā de shén guó wànshì wújiāng.",
                "我信圣神， 他是主及赋予生命者，由圣父圣子所共发。" to "Wǒ xìn shèngshén, tā shì zhǔ jí fùyǔ shēngmìng zhě, yóu shèng fù shèng zǐ suǒ gòng fā.",
                "他和圣父圣子，同受钦崇，同享光荣，他曾借先知们发言。" to "Tā hé shèng fù shèng zǐ, tóng shòu qīnchóng, tóng xiǎng guāngróng, tā céng jiè xiānzhī men fāyán.",
                "我信唯一、至圣、至公、从宗徒传下来的教会。" to "Wǒ xìn wéiyī, zhì shèng, zhì gōng, cóng zōngtú chuán xiàlái de jiàohuì.",
                "我承认赦罪的圣洗，只有一个。" to "Wǒ chéngrèn shèzuì de shèng xǐ, zhǐyǒu yī gè.",
                "我期待死人的复活，及来世的生命。" to "Wǒ qīdài sǐrén de fùhuó, jí láishì de shēngmìng.",
                "阿门" to "Āmen",
            ),
        ),
    )

    /** Today's prayer: they take turns, one per day. */
    fun forDay(day: Long): Prayer = daily[Math.floorMod(day, daily.size.toLong()).toInt()]
}
