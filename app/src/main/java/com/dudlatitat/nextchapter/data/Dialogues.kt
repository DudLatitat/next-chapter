package com.dudlatitat.nextchapter.data

/** One line of a scene: said by the other person, or by you. */
data class Line(val you: Boolean, val hanzi: String, val pinyin: String, val english: String)

data class Dialogue(val id: String, val title: String, val hanzi: String, val setting: String, val lines: List<Line>)

/** Short real-life scenes: you hear them, then answer out loud. */
object Dialogues {
    private fun them(h: String, p: String, e: String) = Line(false, h, p, e)
    private fun you(h: String, p: String, e: String) = Line(true, h, p, e)

    val all = listOf(
        Dialogue(
            "mom", "Meeting her mom", "见阿姨", "You arrive at her mom's home.",
            listOf(
                them("你来了！快进来坐。", "nǐ lái le! kuài jìnlái zuò.", "You're here! Come in and sit."),
                you("阿姨好！这是给您的礼物。", "āyí hǎo! zhè shì gěi nín de lǐwù.", "Hello, auntie! This gift is for you."),
                them("哎呀，太客气了。路上累不累？", "āiyā, tài kèqi le. lùshang lèi bu lèi?", "Oh, you shouldn't have. Tired from the trip?"),
                you("不累，我很高兴见到您。", "bú lèi, wǒ hěn gāoxìng jiàndào nín.", "Not tired. I'm so happy to meet you."),
                them("饿了吧？饭马上就好。", "è le ba? fàn mǎshàng jiù hǎo.", "You must be hungry. Food's almost ready."),
                you("谢谢阿姨，辛苦您了。", "xièxie āyí, xīnkǔ nín le.", "Thank you, auntie. Thanks for all the trouble."),
            ),
        ),
        Dialogue(
            "hotpot", "Ordering hot pot", "吃火锅", "A hot pot place in Chengdu, just the two of you.",
            listOf(
                them("你好，几位？", "nǐ hǎo, jǐ wèi?", "Hi, how many?"),
                you("两位。", "liǎng wèi.", "Two."),
                them("要什么锅底？", "yào shénme guōdǐ?", "Which broth would you like?"),
                you("鸳鸯锅，辣的微辣。", "yuānyang guō, là de wēi là.", "The half-and-half pot, with the spicy side mild."),
                them("好的，还要别的吗？", "hǎo de, hái yào biéde ma?", "Okay, anything else?"),
                you("再来两瓶啤酒，谢谢。", "zài lái liǎng píng píjiǔ, xièxie.", "Two bottles of beer too, thanks."),
            ),
        ),
        Dialogue(
            "taxi", "Taxi to the station", "打车", "You need to catch a train from Chengdu East.",
            listOf(
                them("去哪儿？", "qù nǎr?", "Where to?"),
                you("师傅，去成都东站。", "shīfu, qù Chéngdū Dōngzhàn.", "Driver, to Chengdu East Station."),
                them("好。你赶几点的车？", "hǎo. nǐ gǎn jǐ diǎn de chē?", "Okay. What time is your train?"),
                you("三点的，来得及吗？", "sān diǎn de, lái de jí ma?", "Three o'clock. Will we make it?"),
                them("来得及，不堵车的话二十分钟。", "lái de jí, bù dǔchē dehuà èrshí fēnzhōng.", "We'll make it. Twenty minutes if there's no traffic."),
                you("太好了，谢谢师傅。", "tài hǎo le, xièxie shīfu.", "Great, thank you."),
            ),
        ),
        Dialogue(
            "call", "Evening video call", "视频聊天", "Yuling calls as you're heading home from work.",
            listOf(
                them("宝贝，你下班了吗？", "bǎobèi, nǐ xiàbān le ma?", "Baby, are you off work?"),
                you("刚下班，在开车回家。", "gāng xiàbān, zài kāichē huí jiā.", "Just got off. I'm driving home."),
                them("今天累不累？", "jīntiān lèi bu lèi?", "Are you tired today?"),
                you("有点儿累，但是看到你就不累了。", "yǒudiǎnr lèi, dànshì kàndào nǐ jiù bú lèi le.", "A little, but seeing you I'm not tired anymore."),
                them("嘴真甜！你吃饭了吗？", "zuǐ zhēn tián! nǐ chīfàn le ma?", "Such a sweet talker! Have you eaten?"),
                you("还没有，回家就做饭。", "hái méiyǒu, huí jiā jiù zuòfàn.", "Not yet. I'll cook when I get home."),
            ),
        ),
        Dialogue(
            "market", "At the market", "买水果", "Buying fruit at a street stall.",
            listOf(
                them("帅哥，要点什么？", "shuàigē, yào diǎn shénme?", "Hey handsome, what can I get you?"),
                you("这个苹果多少钱一斤？", "zhège píngguǒ duōshao qián yì jīn?", "How much are these apples per jin?"),
                them("八块一斤。", "bā kuài yì jīn.", "Eight kuai a jin."),
                you("有点儿贵，七块行吗？", "yǒudiǎnr guì, qī kuài xíng ma?", "A bit pricey. Would seven work?"),
                them("好吧，要几斤？", "hǎo ba, yào jǐ jīn?", "Fine. How many jin?"),
                you("两斤。可以扫码吗？", "liǎng jīn. kěyǐ sǎo mǎ ma?", "Two. Can I scan to pay?"),
            ),
        ),
        Dialogue(
            "airport", "She picks you up", "接机", "You just landed at Chengdu Tianfu airport.",
            listOf(
                them("你终于到了！路上顺利吗？", "nǐ zhōngyú dào le! lùshang shùnlì ma?", "You're finally here! Was the trip okay?"),
                you("很顺利，就是有点儿累。", "hěn shùnlì, jiùshì yǒudiǎnr lèi.", "It went smoothly, I'm just a little tired."),
                them("我好想你！", "wǒ hǎo xiǎng nǐ!", "I missed you so much!"),
                you("我也想你，终于见到你了。", "wǒ yě xiǎng nǐ, zhōngyú jiàndào nǐ le.", "I missed you too. Finally I get to see you."),
                them("走吧，我们先去吃饭。", "zǒu ba, wǒmen xiān qù chīfàn.", "Let's go, we'll get food first."),
                you("好啊，我想吃火锅！", "hǎo a, wǒ xiǎng chī huǒguō!", "Sure, I want hot pot!"),
            ),
        ),
    )
}
