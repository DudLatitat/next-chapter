package com.dudlatitat.nextchapter.data

/**
 * Family dinner run-through: three scenes (arriving, the meal, after dinner). Each beat is a line a
 * family member says; you answer with one of the replies out loud (the first is the best answer).
 * Standard Mandarin throughout (no Sichuanese), since family will usually speak Mandarin to a guest.
 * Pinyin follows Seed.kt: citation tones, 一/不 marked as spoken, neutral tones unmarked.
 */
object Dinner {
    data class Reply(val hanzi: String, val pinyin: String, val english: String)

    /** One beat: a family member says something; the learner answers with one of [replies] (first is the best). */
    data class Beat(
        val who: String,
        val hanzi: String,
        val pinyin: String,
        val english: String,
        val replies: List<Reply>,
        val tip: String = "",
    )

    data class Scene(val id: String, val title: String, val hanzi: String, val beats: List<Beat>)

    private const val MOM = "Her mom 阿姨"
    private const val DAD = "Her dad 叔叔"
    private const val GRANDMA = "Grandma 奶奶"

    private fun r(h: String, p: String, e: String) = Reply(h, p, e)

    /** 🥢 for one answer: two for a clean one, one for close. */
    fun chopsticks(v: Callouts.Verdict): Int = when (v) { Callouts.Verdict.GREAT -> 2; Callouts.Verdict.CLOSE -> 1; Callouts.Verdict.MISS -> 0 }

    fun most(scene: Scene): Int = scene.beats.size * 2

    /** How the evening went, from the 🥢 earned. */
    fun ending(got: Int, most: Int): Pair<String, String> {
        val f = if (most == 0) 0.0 else got.toDouble() / most
        return when {
            f >= 0.85 -> "🏮" to "They loved you. Auntie is already planning the next dinner."
            f >= 0.6 -> "🥟" to "A warm evening. A couple of lines to polish, and you're family."
            f >= 0.3 -> "🍵" to "Nervous but sweet. They noticed how hard you're trying."
            else -> "🌱" to "First dinners are hard for everyone. Run it again: it gets easier fast."
        }
    }

    val scenes: List<Scene> = listOf(
        Scene(
            "arrive", "Arriving", "进门",
            listOf(
                Beat(
                    MOM, "来啦！快进来！", "lái la! kuài jìnlái!", "You made it! Come on in!",
                    listOf(
                        r("叔叔阿姨好", "shūshu āyí hǎo", "Hello, uncle and auntie"), // Seed trip
                        r("阿姨，您好", "āyí, nín hǎo", "Hello, auntie"), // Seed family
                    ),
                    "Greet each elder by title (叔叔, 阿姨) as you walk in. It matters more than any other word tonight.",
                ),
                Beat(
                    MOM, "换双拖鞋吧", "huàn shuāng tuōxié ba", "Here, put on some slippers",
                    listOf(
                        r("好的，谢谢阿姨", "hǎo de, xièxie āyí", "Okay, thank you, auntie"),
                        r("我自己来", "wǒ zìjǐ lái", "I can get it myself"),
                    ),
                    "Shoes come off at the door; there's usually a pair of slippers waiting for you.",
                ),
                Beat(
                    DAD, "路上辛苦了吧？", "lùshang xīnkǔ le ba?", "That was a long trip, huh?",
                    listOf(
                        r("不辛苦，很顺利", "bù xīnkǔ, hěn shùnlì", "Not at all, it went smoothly"),
                        r("还好，不累", "hái hǎo, bú lèi", "It was fine, I'm not tired"),
                    ),
                ),
                Beat(
                    MOM, "来就来，还带什么东西！", "lái jiù lái, hái dài shénme dōngxi!", "Just coming is enough, you didn't need to bring anything!",
                    listOf(
                        r("一点小心意", "yìdiǎn xiǎo xīnyì", "Just a small token"), // Culture.kt
                        r("您别客气，收下吧", "nín bié kèqi, shōuxià ba", "Please, don't be polite, take it"), // Culture.kt
                    ),
                    "Hand the gift over with both hands. A polite refusal or two is part of the ritual; offer again with a smile.",
                ),
                Beat(
                    GRANDMA, "长得真精神！", "zhǎng de zhēn jīngshen!", "What a fine-looking young man!",
                    listOf(
                        r("谢谢奶奶", "xièxie nǎinai", "Thank you, grandma"),
                        r("奶奶好", "nǎinai hǎo", "Hello, grandma"),
                    ),
                    "Her mom's mother is 外婆, her dad's mother is 奶奶. Use whichever word she uses for each grandparent.",
                ),
                Beat(
                    MOM, "快坐，快坐！", "kuài zuò, kuài zuò!", "Sit down, sit down!",
                    listOf(
                        r("谢谢阿姨", "xièxie āyí", "Thank you, auntie"),
                        r("您也坐", "nín yě zuò", "You sit too"),
                    ),
                ),
                Beat(
                    MOM, "吃点水果", "chī diǎn shuǐguǒ", "Have some fruit",
                    listOf(
                        r("好，我尝尝", "hǎo, wǒ chángchang", "Okay, I'll try some"),
                        r("谢谢，我自己来", "xièxie, wǒ zìjǐ lái", "Thanks, I'll help myself"),
                    ),
                    "Take at least a little of the tea or fruit you're offered. Saying no to everything can feel cold.",
                ),
                Beat(
                    MOM, "饭马上就好，你们先聊", "fàn mǎshàng jiù hǎo, nǐmen xiān liáo", "Food's almost ready, you all chat for now",
                    listOf(
                        r("您辛苦了", "nín xīnkǔ le", "Thank you for all your hard work"), // Seed family
                        r("我来帮忙", "wǒ lái bāngmáng", "Let me help"), // Seed family
                    ),
                    "Offer to help in the kitchen. You'll probably be sent back to sit, but the offer is noticed.",
                ),
            ),
        ),
        Scene(
            "meal", "At the table", "吃饭",
            listOf(
                Beat(
                    DAD, "来，吃饭了！别客气", "lái, chīfàn le! bié kèqi", "Come on, food's ready! Make yourself at home",
                    listOf(
                        r("叔叔阿姨先吃", "shūshu āyí xiān chī", "You two start first, uncle and auntie"),
                        r("好，谢谢叔叔", "hǎo, xièxie shūshu", "Okay, thank you, uncle"),
                    ),
                    "Wait for the elders to pick up their chopsticks first, and let the host lead.",
                ),
                Beat(
                    MOM, "多吃点，尝尝这个", "duō chī diǎn, chángchang zhège", "Eat more, try this one",
                    listOf(
                        r("谢谢阿姨", "xièxie āyí", "Thank you, auntie"),
                        r("够了够了，我自己来", "gòu le gòu le, wǒ zìjǐ lái", "That's plenty, I'll help myself"),
                    ),
                    "She'll keep putting food in your bowl (夹菜). It's how she shows care: eat it and thank her.",
                ),
                Beat(
                    MOM, "好吃吗？", "hǎochī ma?", "Is it good?",
                    listOf(
                        r("太好吃了", "tài hǎochī le", "This is delicious"), // Seed family
                        r("阿姨的手艺真好", "āyí de shǒuyì zhēn hǎo", "Auntie, your cooking is amazing"), // Culture.kt
                    ),
                    "Praise the cooking more than once during the meal. Name a dish you like if you can.",
                ),
                Beat(
                    DAD, "能吃辣吗？", "néng chī là ma?", "Can you handle spicy food?",
                    listOf(
                        r("能，我喜欢吃辣", "néng, wǒ xǐhuan chī là", "Yes, I love spicy food"),
                        r("一点点就好", "yìdiǎndiǎn jiù hǎo", "Just a little is fine"),
                    ),
                    "Sichuan food is 麻辣 (numbing and spicy). Plain rice helps more than water, and it's fine to say it's spicy; they'll enjoy that.",
                ),
                Beat(
                    DAD, "来，喝一杯！", "lái, hē yì bēi!", "Come on, have a drink with me!",
                    listOf(
                        r("我敬您一杯", "wǒ jìng nín yì bēi", "Here's a toast to you"), // Seed trip
                        r("我以茶代酒", "wǒ yǐ chá dài jiǔ", "I'll toast with tea instead"), // Culture.kt
                    ),
                    "When you clink with an elder, hold your glass a little lower than theirs, with both hands.",
                ),
                Beat(
                    DAD, "干杯！", "gānbēi!", "Cheers!",
                    listOf(
                        r("祝您身体健康", "zhù nín shēntǐ jiànkāng", "Wishing you good health"), // Seed family
                        r("干杯！", "gānbēi!", "Cheers!"),
                    ),
                    "干杯 means \"dry the glass\", but with elders a sip is usually fine unless everyone's finishing.",
                ),
                Beat(
                    MOM, "再来一碗饭吧？", "zài lái yì wǎn fàn ba?", "Have another bowl of rice?",
                    listOf(
                        r("好，再来一点", "hǎo, zài lái yìdiǎn", "Okay, just a little more"),
                        r("不用了，谢谢阿姨", "bú yòng le, xièxie āyí", "No need, thank you, auntie"),
                    ),
                    "Expect to be asked again and again. A warm \"no\" on the second or third offer is normal.",
                ),
                Beat(
                    MOM, "吃饱了没有？", "chī bǎo le méiyǒu?", "Are you full?",
                    listOf(
                        r("我吃饱了，谢谢", "wǒ chī bǎo le, xièxie", "I'm full, thank you"), // Seed family + Culture.kt
                        r("吃得很饱", "chī de hěn bǎo", "I'm really full"),
                    ),
                    "They'll offer more even after you say you're full. Saying 吃饱了 again, with a smile, is completely fine.",
                ),
                Beat(
                    DAD, "慢慢吃，别着急", "mànman chī, bié zháojí", "Take your time eating, no rush",
                    listOf(
                        r("我吃好了，你们慢慢吃", "wǒ chī hǎo le, nǐmen mànman chī", "I'm done, take your time, everyone"),
                        r("好的，谢谢叔叔", "hǎo de, xièxie shūshu", "Okay, thank you, uncle"),
                    ),
                    "\"你们慢慢吃\" is the polite thing to say if you finish before the others.",
                ),
            ),
        ),
        Scene(
            "after", "After dinner", "饭后聊天",
            listOf(
                Beat(
                    MOM, "你坐着，我来收拾", "nǐ zuòzhe, wǒ lái shōushi", "You sit, I'll clear up",
                    listOf(
                        r("我来洗碗", "wǒ lái xǐ wǎn", "Let me do the dishes"), // Seed trip
                        r("我来帮忙吧", "wǒ lái bāngmáng ba", "Let me help"),
                    ),
                    "Offer at least once. If she insists, let her, and thank her.",
                ),
                Beat(
                    DAD, "你在美国做什么工作？", "nǐ zài Měiguó zuò shénme gōngzuò?", "What do you do for work in America?",
                    listOf(
                        r("我是做电脑的", "wǒ shì zuò diànnǎo de", "I work with computers"),
                        r("我在一家公司上班", "wǒ zài yì jiā gōngsī shàngbān", "I work at a company"),
                    ),
                    "Questions about work, pay, age or marriage plans are normal here and come from care, not nosiness.",
                ),
                Beat(
                    DAD, "工作忙不忙？", "gōngzuò máng bu máng?", "Is work busy?",
                    listOf(
                        r("有点忙，还好", "yǒudiǎn máng, hái hǎo", "A little busy, but it's fine"),
                        r("不太忙", "bú tài máng", "Not too busy"),
                    ),
                ),
                Beat(
                    GRANDMA, "你中文学了多久了？", "nǐ Zhōngwén xué le duō jiǔ le?", "How long have you been learning Chinese?",
                    listOf(
                        r("刚开始学", "gāng kāishǐ xué", "I've only just started"),
                        r("学了几个月了", "xué le jǐ ge yuè le", "For a few months now"),
                    ),
                ),
                Beat(
                    DAD, "你中文说得不错嘛！", "nǐ Zhōngwén shuō de búcuò ma!", "Your Chinese is pretty good!",
                    listOf(
                        r("哪里哪里，还在学", "nǎlǐ nǎlǐ, hái zài xué", "Oh, not really, I'm still learning"),
                        r("谢谢，我会继续努力", "xièxie, wǒ huì jìxù nǔlì", "Thank you, I'll keep working at it"),
                    ),
                    "Brushing off a compliment (哪里哪里) is the modest, expected answer.",
                ),
                Beat(
                    MOM, "喜欢这里吗？", "xǐhuan zhèlǐ ma?", "Do you like it here?",
                    listOf(
                        r("很喜欢", "hěn xǐhuan", "I really like it"),
                        r("喜欢，吃的太好了", "xǐhuan, chī de tài hǎo le", "I do, the food is so good"),
                    ),
                ),
                Beat(
                    MOM, "什么时候再来？", "shénme shíhou zài lái?", "When are you coming back?",
                    listOf(
                        r("下次再来看你们", "xià cì zài lái kàn nǐmen", "I'll come see you again next time"), // Seed trip
                        r("很快就回来", "hěn kuài jiù huílái", "I'll be back soon"),
                    ),
                ),
                Beat(
                    MOM, "路上小心，到了说一声", "lùshang xiǎoxīn, dào le shuō yì shēng", "Be careful on the way, let us know when you get there",
                    listOf(
                        r("好的，请留步", "hǎo de, qǐng liúbù", "Okay. Please, no need to see me out"),
                        r("谢谢您的招待", "xièxie nín de zhāodài", "Thank you for your hospitality"), // Seed family
                    ),
                    "They'll walk you to the door or further. 请留步 politely tells them they don't need to.",
                ),
                Beat(
                    DAD, "常来玩啊！", "cháng lái wán a!", "Come visit often!",
                    listOf(
                        r("谢谢叔叔阿姨，再见", "xièxie shūshu āyí, zàijiàn", "Thank you, uncle and auntie. Goodbye"),
                        r("今天真开心", "jīntiān zhēn kāixīn", "Today was really fun"), // Seed trip
                    ),
                    "Say goodbye to each elder by title, just like when you arrived.",
                ),
            ),
        ),
    )
}
