package com.dudlatitat.nextchapter.data

import com.dudlatitat.nextchapter.data.Dinner.Beat
import com.dudlatitat.nextchapter.data.Dinner.Reply

/**
 * Conversations: short scenes where someone talks and you answer out loud (or type, on the
 * computer). Her family's dinner scenes come from [Dinner]; the rest live here. Standard Mandarin,
 * what people actually say; pinyin follows Seed.kt (citation tones, 一/不 as spoken). Nothing
 * personal: speakers are roles ("Her mom", "Driver"), places are generic.
 */
object Talk {
    enum class Group(val title: String, val hanzi: String) {
        FAMILY("Her family", "家人"),
        DAILY("Daily life", "日常"),
        HER("Calls with her", "和她聊天"),
        GAMES("Gaming together", "一起玩"),
    }

    data class Scene(val id: String, val group: Group, val icon: String, val title: String, val hanzi: String, val beats: List<Beat>)

    private const val MOM = "Her mom 阿姨"
    private const val HER = "Her 她"
    private const val WAITER = "Waiter 服务员"
    private const val DRIVER = "Driver 师傅"
    private const val CLERK = "Pharmacist 店员"
    private const val VENDOR = "Stall owner 老板"
    private const val MATE = "Teammate 队友"

    private fun r(h: String, p: String, e: String) = Reply(h, p, e)
    private fun b(who: String, h: String, p: String, e: String, a: Reply, c: Reply, tip: String = "") = Beat(who, h, p, e, listOf(a, c), tip)

    private val icons = mapOf("arrive" to "🚪", "meal" to "🍲", "after" to "🍵")

    val scenes: List<Scene> = Dinner.scenes.map { Scene(it.id, Group.FAMILY, icons[it.id] ?: "🍽️", it.title, it.hanzi, it.beats) } + listOf(
        Scene(
            "momcall", Group.FAMILY, "📱", "Video call with her mom", "和阿姨视频",
            listOf(
                b(MOM, "能听到吗？", "néng tīng dào ma?", "Can you hear me?",
                    r("能听到，阿姨好", "néng tīng dào, āyí hǎo", "Yes, I can hear you. Hello, auntie"),
                    r("听得很清楚", "tīng de hěn qīngchu", "I can hear you clearly")),
                b(MOM, "你那边几点了？", "nǐ nàbiān jǐ diǎn le?", "What time is it over there?",
                    r("晚上八点", "wǎnshang bā diǎn", "Eight in the evening"),
                    r("我这边是晚上", "wǒ zhèbiān shì wǎnshang", "It's evening here"),
                    "Her morning is your evening: the gap is 15 or 16 hours, depending on daylight saving."),
                b(MOM, "吃饭了吗？", "chī fàn le ma?", "Have you eaten?",
                    r("吃了，您呢？", "chī le, nín ne?", "I have. And you?"),
                    r("还没，等一下吃", "hái méi, děng yíxià chī", "Not yet, I'll eat in a bit"),
                    "\"Have you eaten?\" is how many Chinese elders say \"how are you\": answer it and ask back."),
                b(MOM, "工作累不累？", "gōngzuò lèi bu lèi?", "Is work tiring?",
                    r("还好，不太累", "hái hǎo, bú tài lèi", "It's fine, not too tiring"),
                    r("有点累，但是很好", "yǒudiǎn lèi, dànshì hěn hǎo", "A little tiring, but good")),
                b(MOM, "天冷了，多穿点衣服", "tiān lěng le, duō chuān diǎn yīfu", "It's getting cold, wear warmer clothes",
                    r("好的，谢谢阿姨", "hǎo de, xièxie āyí", "Okay, thank you, auntie"),
                    r("您也注意身体", "nín yě zhùyì shēntǐ", "You take care of yourself too"),
                    "Reminders like this are how she shows she cares. A warm thank-you is perfect."),
                b(MOM, "什么时候过来？", "shénme shíhou guòlái?", "When are you coming over?",
                    r("下个月就过来", "xià ge yuè jiù guòlái", "I'm coming next month"),
                    r("很快，我在买机票", "hěn kuài, wǒ zài mǎi jīpiào", "Soon, I'm buying tickets")),
                b(MOM, "好，那你们聊吧", "hǎo, nà nǐmen liáo ba", "Okay, you two go ahead and chat",
                    r("阿姨再见", "āyí zàijiàn", "Bye, auntie"),
                    r("谢谢阿姨，再见", "xièxie āyí, zàijiàn", "Thank you, auntie, bye")),
            ),
        ),
        Scene(
            "restaurant", Group.DAILY, "🥢", "At a restaurant", "餐馆点菜",
            listOf(
                b(WAITER, "几位？", "jǐ wèi?", "How many of you?",
                    r("两位", "liǎng wèi", "Two"),
                    r("就我一个", "jiù wǒ yí ge", "Just me")),
                b(WAITER, "这是菜单，想吃点什么？", "zhè shì càidān, xiǎng chī diǎn shénme?", "Here's the menu. What would you like?",
                    r("有什么推荐的？", "yǒu shénme tuījiàn de?", "What do you recommend?"),
                    r("我要一个宫保鸡丁", "wǒ yào yí ge gōngbǎo jīdīng", "I'll have kung pao chicken")),
                b(WAITER, "我们的回锅肉很好吃", "wǒmen de huíguōròu hěn hǎochī", "Our twice-cooked pork is really good",
                    r("好，来一份", "hǎo, lái yí fèn", "Great, one of those"),
                    r("辣吗？", "là ma?", "Is it spicy?"),
                    "回锅肉 (twice-cooked pork) is a Sichuan classic."),
                b(WAITER, "要微辣、中辣还是特辣？", "yào wēilà, zhōnglà háishi tèlà?", "Mild, medium or extra spicy?",
                    r("微辣，谢谢", "wēilà, xièxie", "Mild, please"),
                    r("中辣吧", "zhōnglà ba", "Medium, I think"),
                    "In Sichuan, 微辣 (\"a little spicy\") is still properly spicy. Start there."),
                b(WAITER, "喝点什么？", "hē diǎn shénme?", "Anything to drink?",
                    r("一壶茶", "yì hú chá", "A pot of tea"),
                    r("来一瓶啤酒", "lái yì píng píjiǔ", "A bottle of beer")),
                b(WAITER, "还要别的吗？", "hái yào biéde ma?", "Anything else?",
                    r("不用了，就这些", "bú yòng le, jiù zhèxiē", "No, that's all"),
                    r("再来一碗米饭", "zài lái yì wǎn mǐfàn", "One more bowl of rice")),
                b(WAITER, "一共八十六块，扫码还是现金？", "yígòng bāshíliù kuài, sǎo mǎ háishi xiànjīn?", "86 yuan in total. Scan to pay, or cash?",
                    r("扫码", "sǎo mǎ", "I'll scan"),
                    r("我扫你吧", "wǒ sǎo nǐ ba", "I'll scan your code"),
                    "Almost everyone pays by scanning a QR code with WeChat Pay or Alipay."),
            ),
        ),
        Scene(
            "taxi", Group.DAILY, "🚕", "A DiDi ride", "打车",
            listOf(
                b(DRIVER, "手机尾号多少？", "shǒujī wěihào duōshao?", "What are the last four digits of your phone?",
                    r("幺二三四", "yāo èr sān sì", "One-two-three-four"),
                    r("尾号是幺二三四", "wěihào shì yāo èr sān sì", "It ends in 1234"),
                    "Ride-hailing drivers check the last 4 digits of your number. In phone numbers, 1 is said 幺 (yāo)."),
                b(DRIVER, "系好安全带", "jì hǎo ānquándài", "Buckle up, please",
                    r("好的", "hǎo de", "Okay"),
                    r("好，谢谢师傅", "hǎo, xièxie shīfu", "Okay, thanks"),
                    "Call drivers 师傅 (shīfu): it's the polite, everyday word for a skilled worker."),
                b(DRIVER, "去火车站是吧？", "qù huǒchēzhàn shì ba?", "The train station, right?",
                    r("对，去火车站", "duì, qù huǒchēzhàn", "Yes, the train station"),
                    r("不是，去这个地址", "bú shì, qù zhège dìzhǐ", "No, this address")),
                b(DRIVER, "你是哪国人？", "nǐ shì nǎ guó rén?", "Where are you from?",
                    r("我是美国人", "wǒ shì Měiguó rén", "I'm American"),
                    r("美国来的", "Měiguó lái de", "From America")),
                b(DRIVER, "在这儿停可以吗？", "zài zhèr tíng kěyǐ ma?", "Is here okay to stop?",
                    r("可以，谢谢", "kěyǐ, xièxie", "Yes, thanks"),
                    r("前面一点停吧", "qiánmiàn yìdiǎn tíng ba", "A little further ahead, please")),
                b(DRIVER, "到了，慢走", "dào le, màn zǒu", "We're here. Take care",
                    r("谢谢师傅，辛苦了", "xièxie shīfu, xīnkǔ le", "Thank you, appreciate it"),
                    r("谢谢，再见", "xièxie, zàijiàn", "Thanks, bye"),
                    "慢走 (\"walk slowly\") is a friendly goodbye, not advice."),
            ),
        ),
        Scene(
            "pharmacy", Group.DAILY, "💊", "At the pharmacy", "药店",
            listOf(
                b(CLERK, "你好，需要什么？", "nǐ hǎo, xūyào shénme?", "Hi, what do you need?",
                    r("我感冒了", "wǒ gǎnmào le", "I've caught a cold"),
                    r("我想买点感冒药", "wǒ xiǎng mǎi diǎn gǎnmàoyào", "I'd like some cold medicine")),
                b(CLERK, "发烧吗？", "fāshāo ma?", "Do you have a fever?",
                    r("有点发烧", "yǒudiǎn fāshāo", "A slight fever"),
                    r("不发烧，就是咳嗽", "bù fāshāo, jiùshì késou", "No fever, just a cough")),
                b(CLERK, "这个药一天三次，一次两片", "zhège yào yì tiān sān cì, yí cì liǎng piàn", "This one's three times a day, two tablets each time",
                    r("饭前吃还是饭后吃？", "fàn qián chī háishi fàn hòu chī?", "Before or after meals?"),
                    r("好的，一天三次", "hǎo de, yì tiān sān cì", "Okay, three times a day")),
                b(CLERK, "饭后吃", "fàn hòu chī", "After meals",
                    r("明白了，谢谢", "míngbai le, xièxie", "Got it, thanks"),
                    r("好，记住了", "hǎo, jìzhù le", "Okay, I'll remember")),
                b(CLERK, "多喝热水，好好休息", "duō hē rè shuǐ, hǎohǎo xiūxi", "Drink lots of hot water and rest well",
                    r("好的，谢谢", "hǎo de, xièxie", "Okay, thank you"),
                    r("多少钱？", "duōshao qián?", "How much is it?"),
                    "\"Drink hot water\" is the classic Chinese advice for almost any illness."),
            ),
        ),
        Scene(
            "market", Group.DAILY, "🥬", "At the market", "菜市场",
            listOf(
                b(VENDOR, "要点什么？", "yào diǎn shénme?", "What can I get you?",
                    r("这个怎么卖？", "zhège zěnme mài?", "How much is this?"),
                    r("西红柿多少钱一斤？", "xīhóngshì duōshao qián yì jīn?", "How much are tomatoes per jin?")),
                b(VENDOR, "五块一斤", "wǔ kuài yì jīn", "Five yuan a jin",
                    r("便宜一点吧", "piányi yìdiǎn ba", "Could you go a bit cheaper?"),
                    r("要两斤", "yào liǎng jīn", "I'll take two jin"),
                    "Markets price by the 斤 (jīn), which is 500 g, about a pound."),
                b(VENDOR, "四块五，不能再少了", "sì kuài wǔ, bù néng zài shǎo le", "4.5, can't go lower",
                    r("好，就要两斤", "hǎo, jiù yào liǎng jīn", "Okay, two jin then"),
                    r("行，谢谢老板", "xíng, xièxie lǎobǎn", "Deal, thanks"),
                    "Call stall owners 老板 (\"boss\"): friendly and normal."),
                b(VENDOR, "还要别的吗？", "hái yào biéde ma?", "Anything else?",
                    r("再来一把葱", "zài lái yì bǎ cōng", "And a bunch of green onions"),
                    r("不要了，谢谢", "bú yào le, xièxie", "No, thanks")),
                b(VENDOR, "一共九块", "yígòng jiǔ kuài", "Nine yuan in total",
                    r("可以用微信吗？", "kěyǐ yòng Wēixìn ma?", "Can I use WeChat?"),
                    r("我扫你吧", "wǒ sǎo nǐ ba", "I'll scan your code")),
            ),
        ),
        Scene(
            "yourday", Group.HER, "☀️", "How was your day?", "今天怎么样",
            listOf(
                b(HER, "我刚起床，好困", "wǒ gāng qǐchuáng, hǎo kùn", "I just woke up, so sleepy",
                    r("早上好，宝贝", "zǎoshang hǎo, bǎobèi", "Good morning, babe"),
                    r("再睡一会儿吧", "zài shuì yíhuìr ba", "Sleep a little more")),
                b(HER, "你今天怎么样？", "nǐ jīntiān zěnmeyàng?", "How was your day?",
                    r("还不错，就是有点累", "hái búcuò, jiùshì yǒudiǎn lèi", "Pretty good, just a little tired"),
                    r("挺好的，你呢？", "tǐng hǎo de, nǐ ne?", "Good! And you?")),
                b(HER, "今天工作忙吗？", "jīntiān gōngzuò máng ma?", "Was work busy today?",
                    r("有点忙，修了很多电脑", "yǒudiǎn máng, xiū le hěn duō diànnǎo", "A bit busy, I fixed lots of computers"),
                    r("不忙，挺轻松的", "bù máng, tǐng qīngsōng de", "Not busy, pretty relaxed")),
                b(HER, "你吃晚饭了吗？", "nǐ chī wǎnfàn le ma?", "Have you had dinner?",
                    r("吃了，我做了意大利面", "chī le, wǒ zuò le Yìdàlì miàn", "Yes, I made pasta"),
                    r("还没，等会儿吃", "hái méi, děng huìr chī", "Not yet, I'll eat in a bit")),
                b(HER, "你今天学中文了吗？", "nǐ jīntiān xué Zhōngwén le ma?", "Did you study Chinese today?",
                    r("学了，你听我说", "xué le, nǐ tīng wǒ shuō", "I did, listen to this"),
                    r("学了，我在学新的句子", "xué le, wǒ zài xué xīn de jùzi", "Yes, I'm learning new sentences")),
                b(HER, "我好想你", "wǒ hǎo xiǎng nǐ", "I miss you so much",
                    r("我也想你", "wǒ yě xiǎng nǐ", "I miss you too"),
                    r("我也是，很快就见面了", "wǒ yě shì, hěn kuài jiù jiànmiàn le", "Me too. We'll see each other soon")),
            ),
        ),
        Scene(
            "goodnight", Group.HER, "🌙", "Saying goodnight", "说晚安",
            listOf(
                b(HER, "你那边很晚了吧？", "nǐ nàbiān hěn wǎn le ba?", "It's late over there, isn't it?",
                    r("是啊，快十一点了", "shì a, kuài shíyī diǎn le", "Yeah, it's almost eleven"),
                    r("还好，不太晚", "hái hǎo, bú tài wǎn", "Not too late")),
                b(HER, "早点睡，别熬夜", "zǎo diǎn shuì, bié áoyè", "Go to bed early, don't stay up",
                    r("好，听你的", "hǎo, tīng nǐ de", "Okay, whatever you say"),
                    r("知道啦", "zhīdao la", "I know, I know")),
                b(HER, "明天几点起床？", "míngtiān jǐ diǎn qǐchuáng?", "What time are you getting up tomorrow?",
                    r("六点半", "liù diǎn bàn", "Six thirty"),
                    r("早上六点半", "zǎoshang liù diǎn bàn", "Six thirty in the morning")),
                b(HER, "晚安，好好休息", "wǎn'ān, hǎohǎo xiūxi", "Goodnight, get some good rest",
                    r("晚安，宝贝", "wǎn'ān, bǎobèi", "Goodnight, babe"),
                    r("晚安，明天聊", "wǎn'ān, míngtiān liáo", "Goodnight, talk tomorrow")),
            ),
        ),
        Scene(
            "gamenight", Group.GAMES, "🎮", "Gaming night", "开黑",
            listOf(
                b(HER, "上线了吗？", "shàngxiàn le ma?", "Are you online?",
                    r("上了，拉我", "shàng le, lā wǒ", "Yep, invite me"),
                    r("马上，等我一下", "mǎshàng, děng wǒ yíxià", "In a sec, wait for me")),
                b(HER, "今天玩什么？", "jīntiān wán shénme?", "What are we playing today?",
                    r("玩加页手记吧", "wán jiā yè shǒujì ba", "Let's play Cryptic Notes"),
                    r("随便，你决定", "suíbiàn, nǐ juédìng", "Anything, you choose")),
                b(HER, "我在二楼", "wǒ zài èr lóu", "I'm on the second floor",
                    r("我过去找你", "wǒ guòqù zhǎo nǐ", "I'm coming over to you"),
                    r("等我，马上到", "děng wǒ, mǎshàng dào", "Wait for me, almost there")),
                b(HER, "异象来了！快跑！", "yìxiàng lái le! kuài pǎo!", "The anomaly's coming! Run!",
                    r("跟着我！", "gēnzhe wǒ!", "Follow me!"),
                    r("往左走！", "wǎng zuǒ zǒu!", "Go left!")),
                b(HER, "我倒地了，救我！", "wǒ dǎo dì le, jiù wǒ!", "I'm down, save me!",
                    r("我来救你", "wǒ lái jiù nǐ", "I'm coming to save you"),
                    r("坚持住！", "jiānchí zhù!", "Hang in there!")),
                b(HER, "我们撤出来了！", "wǒmen chè chūlái le!", "We made it out!",
                    r("打得好！", "dǎ de hǎo!", "Nice play!"),
                    r("你太厉害了！", "nǐ tài lìhai le!", "You're amazing!")),
                b(HER, "再来一局？", "zài lái yì jú?", "One more round?",
                    r("好啊，最后一局", "hǎo a, zuìhòu yì jú", "Sure, last one"),
                    r("明天再玩吧，该睡觉了", "míngtiān zài wán ba, gāi shuìjiào le", "Let's play tomorrow, it's bedtime")),
            ),
        ),
        // Counter-Strike with a Chinese teammate. 下包/拆包/A点/回防/转点/补枪/经济局 checked against Chinese
        // CS sources (csgo.com.cn guides, Sina Esports' Dust2 callouts, Zhihu, Bilibili), Nov 2026.
        // 残血 ("low HP") is everyday gaming Chinese but wasn't found in a CS-specific source: flagged.
        Scene(
            "csmatch", Group.GAMES, "🔫", "A CS match", "打CS",
            listOf(
                b(MATE, "准备好了吗？", "zhǔnbèi hǎo le ma?", "Ready?",
                    r("好了，开始吧", "hǎo le, kāishǐ ba", "Ready, let's go"),
                    r("等我一下", "děng wǒ yíxià", "Give me a sec")),
                b(MATE, "这局买什么？", "zhè jú mǎi shénme?", "What are you buying this round?",
                    r("我买步枪", "wǒ mǎi bùqiāng", "I'm buying a rifle"),
                    r("没钱了，经济局", "méi qián le, jīngjì jú", "I'm broke, eco round"),
                    "经济局 = an eco round: you save money instead of buying."),
                b(MATE, "你去A还是B？", "nǐ qù A háishi B?", "Are you going A or B?",
                    r("我去B", "wǒ qù B", "I'll go B"),
                    r("我们一起去A", "wǒmen yìqǐ qù A", "Let's go A together")),
                b(MATE, "B点几个人？", "B diǎn jǐ ge rén?", "How many at B?",
                    r("两个", "liǎng ge", "Two"),
                    r("我看到一个", "wǒ kàn dào yí ge", "I see one")),
                b(MATE, "我残血了", "wǒ cán xuè le", "I'm low on HP",
                    r("退后，我来", "tuìhòu, wǒ lái", "Fall back, I've got it"),
                    r("我来补枪", "wǒ lái bǔ qiāng", "I'll trade for you"),
                    "补枪 = to trade a kill: shoot the enemy who just shot your teammate."),
                b(MATE, "包下了！", "bāo xià le!", "Bomb's planted!",
                    r("回防！", "huífáng!", "Retake!"),
                    r("快拆包！", "kuài chāi bāo!", "Defuse it, quick!")),
                b(MATE, "赢了！", "yíng le!", "We won!",
                    r("打得好！", "dǎ de hǎo!", "Nice play!"),
                    r("太好了！", "tài hǎo le!", "Awesome!")),
            ),
        ),
    )

    /**
     * CS callouts for Game mode: the English cue is spoken, you call it out in Chinese.
     * Sources as for the "csmatch" scene above; 残血 flagged there.
     */
    val csCallouts: List<Reply> = listOf(
        r("A点有人！", "A diǎn yǒu rén!", "Enemy at A!"),
        r("B点一个", "B diǎn yí ge", "One at B"),
        r("中路两个", "zhōnglù liǎng ge", "Two in mid"),
        r("A点下包了", "A diǎn xià bāo le", "Bomb's planted at A"),
        r("快拆包！", "kuài chāi bāo!", "Defuse, quick!"),
        r("转B点！", "zhuǎn B diǎn!", "Rotate to B!"),
        r("回防A点", "huífáng A diǎn", "Retake A"),
        r("我残血", "wǒ cán xuè", "I'm low HP"),
        r("补枪！", "bǔ qiāng!", "Trade me!"),
        r("扔个闪光", "rēng ge shǎnguāng", "Throw a flash"),
        r("扔个烟", "rēng ge yān", "Throw a smoke"),
        r("小心火！", "xiǎoxīn huǒ!", "Watch the molotov!"),
        r("这局经济局", "zhè jú jīngjì jú", "Eco this round"),
        r("这局买枪", "zhè jú mǎi qiāng", "Buy guns this round"),
        r("最后一个", "zuìhòu yí ge", "Last one"),
        r("我在换弹", "wǒ zài huàn dàn", "I'm reloading"),
        r("一起冲！", "yìqǐ chōng!", "Push together!"),
        r("守住这里", "shǒu zhù zhèlǐ", "Hold here"),
    )

    fun byId(id: String) = scenes.firstOrNull { it.id == id }

    /**
     * Does a typed answer match the reply? Characters match ignoring punctuation and spaces;
     * pinyin matches with or without tone marks (and ü typed as u or v).
     */
    fun typedMatches(typed: String, reply: Reply): Boolean {
        val t = typed.trim()
        if (t.isEmpty()) return false
        fun han(s: String) = s.filter { it in '一'..'鿿' }
        if (han(t).isNotEmpty()) return han(t) == han(reply.hanzi)
        return plain(t) == plain(reply.pinyin)
    }

    private val marks = mapOf(
        'ā' to 'a', 'á' to 'a', 'ǎ' to 'a', 'à' to 'a', 'ē' to 'e', 'é' to 'e', 'ě' to 'e', 'è' to 'e',
        'ī' to 'i', 'í' to 'i', 'ǐ' to 'i', 'ì' to 'i', 'ō' to 'o', 'ó' to 'o', 'ǒ' to 'o', 'ò' to 'o',
        'ū' to 'u', 'ú' to 'u', 'ǔ' to 'u', 'ù' to 'u', 'ǖ' to 'v', 'ǘ' to 'v', 'ǚ' to 'v', 'ǜ' to 'v', 'ü' to 'v',
    )

    /** "Nǐ hǎo, bǎobèi!" → "nihaobaobei" (letters only, tones dropped, ü as v). */
    fun plain(pinyin: String): String =
        pinyin.lowercase().map { marks[it] ?: it }.filter { it in 'a'..'z' }.joinToString("")
}
