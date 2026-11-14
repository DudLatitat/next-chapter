package com.dudlatitat.nextchapter.data

/**
 * Texting practice: WeChat-style chats you answer with your real pinyin keyboard, and typing
 * sprints. Replies pass on key words, so you can say it your own way; the examples show natural
 * answers. Standard Mandarin, generic contacts (roles, not people). Pure, tested in TextingTest.
 */
object Texting {
    /** One message from them; [voice] shows it as a voice bubble you play instead of read. */
    data class Msg(val hanzi: String, val pinyin: String, val english: String, val voice: Boolean = false)
    data class Example(val hanzi: String, val pinyin: String, val english: String)

    /**
     * They send [them]; your reply passes if it contains every word of any group in [accept]
     * (e.g. [["下班了"], ["还没"]] = "下班了" or "还没"). [words] is the hint word bank.
     */
    data class Turn(val them: List<Msg>, val accept: List<List<String>>, val examples: List<Example>, val words: List<String>, val tip: String = "")
    data class Thread(val id: String, val contact: String, val avatar: String, val title: String, val turns: List<Turn>)

    private fun m(h: String, p: String, e: String, voice: Boolean = false) = Msg(h, p, e, voice)
    private fun x(h: String, p: String, e: String) = Example(h, p, e)
    private fun any(vararg w: String) = w.map { listOf(it) }

    val threads = listOf(
        Thread(
            "morning", "Her 她", "🌸", "Good morning", listOf(
                Turn(listOf(m("早呀", "zǎo ya", "Morning!"), m("醒了吗？", "xǐng le ma?", "Are you up?")),
                    any("早", "醒了"),
                    listOf(x("早呀，醒了", "zǎo ya, xǐng le", "Morning! I'm up"), x("早安，宝贝", "zǎo'ān, bǎobèi", "Good morning, babe")),
                    listOf("早", "醒了", "宝贝")),
                Turn(listOf(m("今天好冷", "jīntiān hǎo lěng", "It's so cold today")),
                    any("多穿", "别着凉", "注意"),
                    listOf(x("多穿点衣服", "duō chuān diǎn yīfu", "Wear more layers"), x("别着凉了", "bié zháoliáng le", "Don't catch a cold")),
                    listOf("多穿", "衣服", "别着凉"),
                    "Caring through little reminders (wear more, eat something) is how people show love in Chinese."),
                Turn(listOf(m("我去上班啦，拜拜", "wǒ qù shàngbān la, bàibài", "Off to work, bye!", voice = true)),
                    any("拜拜", "加油", "小心"),
                    listOf(x("加油！拜拜", "jiāyóu! bàibài", "You've got this! Bye"), x("路上小心", "lùshang xiǎoxīn", "Be careful on the way")),
                    listOf("加油", "拜拜", "路上小心"),
                    "Voice messages (语音) are everywhere on WeChat: play it, then answer by text."),
            ),
        ),
        Thread(
            "evening", "Her 她", "🌸", "After your work", listOf(
                Turn(listOf(m("下班了吗？", "xiàbān le ma?", "Off work yet?")),
                    any("下班了", "还没"),
                    listOf(x("下班了，刚到家", "xiàbān le, gāng dào jiā", "Yep, just got home"), x("还没，还要一会儿", "hái méi, hái yào yíhuìr", "Not yet, a little longer")),
                    listOf("下班了", "刚到家", "还没")),
                Turn(listOf(m("辛苦啦", "xīnkǔ la", "You must be tired"), m("吃了没？", "chī le méi?", "Eaten yet?")),
                    any("吃了", "还没", "没吃"),
                    listOf(x("吃了，你呢？", "chī le, nǐ ne?", "I have, you?"), x("还没，等会儿吃", "hái méi, děng huìr chī", "Not yet, soon")),
                    listOf("吃了", "你呢", "还没")),
                Turn(listOf(m("哈哈哈，你今天说中文了吗？", "hāhāhā, nǐ jīntiān shuō Zhōngwén le ma?", "Haha, did you speak Chinese today?")),
                    any("说了", "学了", "没有"),
                    listOf(x("说了！我在学打字", "shuō le! wǒ zài xué dǎzì", "Yes! I'm learning to type"), x("学了，你考考我", "xué le, nǐ kǎokao wǒ", "Yep, quiz me")),
                    listOf("说了", "打字", "考考我")),
                Turn(listOf(m("想你了", "xiǎng nǐ le", "Missing you")),
                    any("想你", "抱抱"),
                    listOf(x("我也想你，抱抱", "wǒ yě xiǎng nǐ, bàobào", "Miss you too, hugs"), x("我也想你了", "wǒ yě xiǎng nǐ le", "I miss you too")),
                    listOf("我也", "想你", "抱抱"),
                    "A bare 嗯 can read as cold in a chat; 嗯嗯 or a few words feels warmer."),
            ),
        ),
        Thread(
            "mom", "Her mom 阿姨", "👩", "A message from her mom", listOf(
                Turn(listOf(m("小伙子，在吗？", "xiǎohuǒzi, zài ma?", "Young man, are you there?")),
                    any("在"),
                    listOf(x("在的，阿姨好", "zài de, āyí hǎo", "I'm here. Hello, auntie"), x("阿姨好，我在", "āyí hǎo, wǒ zài", "Hello auntie, I'm here")),
                    listOf("在的", "阿姨好"),
                    "小伙子 is a warm way older people address a young man."),
                Turn(listOf(m("收到你寄的茶叶了，谢谢你", "shōudào nǐ jì de cháyè le, xièxie nǐ", "Got the tea you sent, thank you")),
                    any("不客气", "喜欢", "不用谢"),
                    listOf(x("不客气，您喜欢就好", "bú kèqi, nín xǐhuan jiù hǎo", "You're welcome, I'm glad you like it"), x("不用谢，阿姨", "bú yòng xiè, āyí", "No need to thank me, auntie")),
                    listOf("不客气", "喜欢就好", "您"),
                    "Use 您 (the polite \"you\") with her parents, in texts too."),
                Turn(listOf(m("什么时候过来玩？", "shénme shíhou guòlái wán?", "When are you coming to visit?", voice = true)),
                    any("月", "很快", "快了"),
                    listOf(x("下个月就过来", "xià ge yuè jiù guòlái", "Next month"), x("很快就过来", "hěn kuài jiù guòlái", "Very soon")),
                    listOf("下个月", "很快", "过来")),
                Turn(listOf(m("好，路上注意安全", "hǎo, lùshang zhùyì ānquán", "Good. Stay safe on the way")),
                    any("谢谢", "好的", "收到"),
                    listOf(x("好的，谢谢阿姨", "hǎo de, xièxie āyí", "Okay, thank you, auntie"), x("收到，谢谢阿姨", "shōudào, xièxie āyí", "Got it, thank you, auntie")),
                    listOf("好的", "谢谢", "阿姨")),
            ),
        ),
        Thread(
            "didi", "Driver 师傅", "🚕", "Your DiDi driver", listOf(
                Turn(listOf(m("你好，我到了，白色的车", "nǐ hǎo, wǒ dào le, báisè de chē", "Hi, I'm here, white car")),
                    any("马上", "来了", "稍等"),
                    listOf(x("好的，马上到", "hǎo de, mǎshàng dào", "Okay, almost there"), x("稍等，我来了", "shāoděng, wǒ lái le", "One moment, coming")),
                    listOf("马上到", "稍等", "来了")),
                Turn(listOf(m("在哪个门？", "zài nǎge mén?", "Which entrance?")),
                    any("门", "定位"),
                    listOf(x("我在北门", "wǒ zài běi mén", "I'm at the north gate"), x("南门，我发个定位", "nán mén, wǒ fā ge dìngwèi", "South gate, I'll send my location")),
                    listOf("北门", "南门", "定位")),
                Turn(listOf(m("看到你了", "kàn dào nǐ le", "I see you")),
                    any("好", "谢谢"),
                    listOf(x("好的，谢谢师傅", "hǎo de, xièxie shīfu", "Okay, thanks"), x("好，马上上车", "hǎo, mǎshàng shàng chē", "Okay, getting in now")),
                    listOf("好的", "谢谢", "师傅")),
            ),
        ),
        Thread(
            "delivery", "Delivery 外卖", "🛵", "Your food arrived", listOf(
                Turn(listOf(m("您好，外卖放在门口了", "nín hǎo, wàimài fàng zài ménkǒu le", "Hi, your food is at the door")),
                    any("谢谢", "收到", "好的"),
                    listOf(x("收到，辛苦了", "shōudào, xīnkǔ le", "Got it, thanks for your trouble"), x("好的，谢谢", "hǎo de, xièxie", "Okay, thanks")),
                    listOf("收到", "辛苦了", "谢谢")),
            ),
        ),
        Thread(
            "game", "Her 她", "🎮", "Game night", listOf(
                Turn(listOf(m("今晚上号吗？", "jīnwǎn shàng hào ma?", "Getting on tonight?")),
                    any("上", "好", "来"),
                    listOf(x("上！等我十分钟", "shàng! děng wǒ shí fēnzhōng", "Yes! Give me ten minutes"), x("好啊，八点上号", "hǎo a, bā diǎn shàng hào", "Sure, on at eight")),
                    listOf("上号", "等我", "八点"),
                    "上号 = get on (your game account)."),
                Turn(listOf(m("拉我", "lā wǒ", "Invite me")),
                    any("拉", "好", "稍等"),
                    listOf(x("好，拉你了", "hǎo, lā nǐ le", "Okay, invited you"), x("稍等，马上拉你", "shāoděng, mǎshàng lā nǐ", "One sec, inviting you")),
                    listOf("拉你", "稍等", "马上")),
                Turn(listOf(m("你太强了哈哈哈", "nǐ tài qiáng le hāhāhā", "You're so good, hahaha")),
                    any("哈哈", "谢谢", "你也"),
                    listOf(x("哈哈哈，你也很强", "hāhāhā, nǐ yě hěn qiáng", "Haha, you're good too"), x("谢谢，你带我飞", "xièxie, nǐ dài wǒ fēi", "Thanks, you carried me")),
                    listOf("哈哈哈", "你也", "带我飞")),
                Turn(listOf(m("发红包了，抢！", "fā hóngbāo le, qiǎng!", "Sent a red envelope, grab it!")),
                    any("谢谢"),
                    listOf(x("谢谢老板！", "xièxie lǎobǎn!", "Thanks, boss!"), x("哈哈，谢谢老板", "hāha, xièxie lǎobǎn", "Haha, thanks boss")),
                    listOf("谢谢老板", "哈哈"),
                    "People race to \"grab\" (抢) red envelopes in chats; 谢谢老板 is the joking thank-you."),
            ),
        ),
    )

    private fun han(s: String) = s.filter { it in '一'..'鿿' }

    /** Does your reply pass? It must contain every word of at least one accepted group (characters only). */
    fun accepts(turn: Turn, typed: String): Boolean {
        val t = han(typed)
        if (t.isEmpty()) return false
        return turn.accept.any { group -> group.all { han(it) in t } }
    }

    // ---- Typing sprints ----
    /** Decks the sprint draws from: everyday things you'd actually text. */
    val sprintDecks = setOf("text", "daily", "yuling", "games", "sweet", "family")

    /** Lines for a sprint: studied ones first, short enough to type (2–12 characters). */
    fun sprintPool(s: AppState): List<Card> {
        val fits = s.cards.filter { it.deck in sprintDecks && han(it.hanzi).length in 2..12 }
        val seen = fits.filter { it.firstSeen != null }
        return if (seen.size >= 15) seen else fits
    }

    /** Typed the same characters (punctuation and spaces don't count)? */
    fun typedRight(typed: String, card: Card): Boolean = han(typed).isNotEmpty() && han(typed) == han(card.hanzi)

    /** Characters per minute, from characters typed right and the time taken. */
    fun cpm(chars: Int, millis: Long): Int = if (millis <= 0) 0 else (chars * 60_000L / millis).toInt()
}
