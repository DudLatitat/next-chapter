package com.dudlatitat.nextchapter.data

/** The starter phrases and move checklist. Bump [VERSION] when adding more: new ones get merged in. */
object Seed {
    const val VERSION = 8

    /**
     * Starter cards that were replaced (the 1v4 Identity V callouts, swapped for 加页手记 ones in v5).
     * They're removed from saved data only if never studied, so no progress is lost.
     */
    val retired = setOf(
        "idv:我来救你", "idv:监管者来了", "idv:屠夫来了", "idv:我来溜鬼", "idv:我被追了", "idv:我在修机", "idv:压机",
        "idv:开门", "idv:地窖", "idv:我倒地了", "idv:别救了，先修机", "idv:小心！", "idv:快走！", "idv:我们赢了！",
    )

    /** Saved cards plus any new starter cards, minus retired ones you never studied. */
    fun merge(saved: List<Card>): List<Card> {
        val kept = saved.filterNot { it.id in retired && it.isNew }
        val have = kept.map { it.id }.toSet()
        return kept + (cards + Vocab.cards).filter { it.id !in have }
    }

    private fun c(deck: String, hanzi: String, pinyin: String, english: String) =
        Card(id = "$deck:$hanzi", hanzi = hanzi, pinyin = pinyin, english = english, deck = deck)

    val cards = listOf(
        // With Yuling
        c("yuling", "我想你", "wǒ xiǎng nǐ", "I miss you"),
        c("yuling", "宝贝", "bǎobèi", "Baby / sweetheart"),
        c("yuling", "你在干嘛？", "nǐ zài gàn má?", "What are you doing?"),
        c("yuling", "你睡了吗？", "nǐ shuì le ma?", "Are you asleep?"),
        c("yuling", "早点睡", "zǎo diǎn shuì", "Go to bed early"),
        c("yuling", "想见你", "xiǎng jiàn nǐ", "I want to see you"),
        c("yuling", "我马上回来", "wǒ mǎshàng huílái", "I'll be right back"),
        c("yuling", "别生气了", "bié shēngqì le", "Don't be mad anymore"),
        c("yuling", "我很快就来找你", "wǒ hěn kuài jiù lái zhǎo nǐ", "I'm coming to you soon"),
        c("yuling", "你是我的一切", "nǐ shì wǒ de yíqiè", "You are my everything"),

        // Identity V (第五人格), for 加页手记 (Cryptic Notes), the PvE loot-and-extract mode Rudy and Yuling
        // play. Mode terms (辞章 pages, 异象 anomalies, 撤离 extract, 搜刮 loot, 墨迹 ink, 智者/主角 classes,
        // 金柜 gold cabinet, 急救针 first-aid shot) checked against Chinese player guides (Sina, 233乐园, 九游), Nov 2026.
        c("idv", "我们玩加页手记吧", "wǒmen wán jiā yè shǒujì ba", "Let's play Cryptic Notes"),
        c("idv", "你选智者，我选主角", "nǐ xuǎn zhìzhě, wǒ xuǎn zhǔjué", "You pick the Sage, I'll pick the Protagonist"),
        c("idv", "先买装备", "xiān mǎi zhuāngbèi", "Buy gear first"),
        c("idv", "墨迹够吗？", "mòjì gòu ma?", "Do we have enough ink (to buy gear)?"),
        c("idv", "跟着我", "gēnzhe wǒ", "Follow me"),
        c("idv", "等等我！", "děngděng wǒ!", "Wait for me!"),
        c("idv", "我去搜刮", "wǒ qù sōuguā", "I'll go loot"),
        c("idv", "这里有辞章", "zhèlǐ yǒu cízhāng", "There's a page (辞章) here"),
        c("idv", "我开金柜", "wǒ kāi jīnguì", "I'll open the gold cabinet"),
        c("idv", "小心异象！", "xiǎoxīn yìxiàng!", "Watch out for the anomaly!"),
        c("idv", "异象来了！", "yìxiàng lái le!", "The anomaly is coming!"),
        c("idv", "快跑！", "kuài pǎo!", "Run!"),
        c("idv", "救我！", "jiù wǒ!", "Save me!"),
        c("idv", "我没血了", "wǒ méi xiě le", "I'm out of health"),
        c("idv", "给我急救针", "gěi wǒ jíjiùzhēn", "Give me a first-aid shot"),
        c("idv", "快撤离！", "kuài chèlí!", "Extract now!"),
        c("idv", "我们撤出来了！", "wǒmen chè chūlái le!", "We made it out!"),
        c("idv", "打得好！", "dǎ de hǎo!", "Nice play!"),
        c("idv", "你好厉害", "nǐ hǎo lìhai", "You're so good"),
        c("idv", "再来一局？", "zài lái yì jú?", "One more run?"),

        // Gaming together (一起玩): everyday talk for any game with Yuling. Common gaming words: 开黑
        // (team up on voice), 拉我 (invite me), 卡了 (lagging), 掉线 (disconnected), 带飞 (carry), 菜 (bad at it).
        c("games", "一起玩游戏吗？", "yìqǐ wán yóuxì ma?", "Want to play a game together?"),
        c("games", "来开黑吗？", "lái kāihēi ma?", "Want to team up (on voice chat)?"),
        c("games", "拉我一下", "lā wǒ yíxià", "Invite me in (to your team)"),
        c("games", "我准备好了", "wǒ zhǔnbèi hǎo le", "I'm ready"),
        c("games", "等我一下", "děng wǒ yíxià", "Give me a second"),
        c("games", "你听得到我吗？", "nǐ tīng de dào wǒ ma?", "Can you hear me?"),
        c("games", "我卡了", "wǒ kǎ le", "I'm lagging"),
        c("games", "我掉线了", "wǒ diàoxiàn le", "I got disconnected"),
        c("games", "过来这里", "guòlái zhèlǐ", "Come over here"),
        c("games", "往左走", "wǎng zuǒ zǒu", "Go left"),
        c("games", "往右走", "wǎng yòu zǒu", "Go right"),
        c("games", "在你后面！", "zài nǐ hòumiàn!", "Behind you!"),
        c("games", "我保护你", "wǒ bǎohù nǐ", "I'll protect you"),
        c("games", "带我飞！", "dài wǒ fēi!", "Carry me!"),
        c("games", "我太菜了", "wǒ tài cài le", "I'm so bad at this"),
        c("games", "你太强了！", "nǐ tài qiáng le!", "You're amazing!"),
        c("games", "差一点！", "chà yìdiǎn!", "So close!"),
        c("games", "没关系，下次赢", "méi guānxi, xià cì yíng", "It's okay, we'll win next time"),
        c("games", "跟你玩输了也开心", "gēn nǐ wán shū le yě kāixīn", "Even losing is fun with you"),
        c("games", "你赢了，我请你吃饭", "nǐ yíng le, wǒ qǐng nǐ chīfàn", "You won, dinner's on me"),
        c("games", "最后一局", "zuìhòu yì jú", "Last round"),
        c("games", "我先下了", "wǒ xiān xià le", "I'm logging off now"),
        c("games", "该睡觉了", "gāi shuìjiào le", "Time for bed"),
        c("games", "明天再玩", "míngtiān zài wán", "Let's play again tomorrow"),
        // More for playing with her while your Chinese catches up: asking her to slow down, being sweet,
        // teamwork, checking in on her, and good night.
        c("games", "我想跟你一起玩", "wǒ xiǎng gēn nǐ yìqǐ wán", "I want to play with you"),
        c("games", "你教我好不好？", "nǐ jiāo wǒ hǎo bu hǎo?", "Will you teach me?"),
        c("games", "我听不懂，再说一遍", "wǒ tīng bu dǒng, zài shuō yí biàn", "I didn't get that, say it again"),
        c("games", "我的中文不好，别笑我", "wǒ de Zhōngwén bù hǎo, bié xiào wǒ", "My Chinese isn't good, don't laugh at me"),
        c("games", "你笑什么？", "nǐ xiào shénme?", "What are you laughing at?"),
        c("games", "我在学中文，为了你", "wǒ zài xué Zhōngwén, wèile nǐ", "I'm learning Chinese for you"),
        c("games", "你好可爱", "nǐ hǎo kě'ài", "You're so cute"),
        c("games", "你的声音很好听", "nǐ de shēngyīn hěn hǎotīng", "Your voice sounds so nice"),
        c("games", "有你在，我不怕", "yǒu nǐ zài, wǒ bú pà", "With you here, I'm not scared"),
        c("games", "别丢下我！", "bié diūxià wǒ!", "Don't leave me behind!"),
        c("games", "我来了，等我！", "wǒ lái le, děng wǒ!", "I'm coming, wait for me!"),
        c("games", "你先走，我掩护你", "nǐ xiān zǒu, wǒ yǎnhù nǐ", "You go first, I'll cover you"),
        c("games", "谢谢你救我", "xièxie nǐ jiù wǒ", "Thanks for saving me"),
        c("games", "我们是最好的队友", "wǒmen shì zuì hǎo de duìyǒu", "We're the best team"),
        c("games", "你又赢了！", "nǐ yòu yíng le!", "You won again!"),
        c("games", "对不起，是我的错", "duìbuqǐ, shì wǒ de cuò", "Sorry, that was my fault"),
        c("games", "你生气了吗？", "nǐ shēngqì le ma?", "Are you mad at me?"),
        c("games", "你累不累？", "nǐ lèi bu lèi?", "Are you tired?"),
        c("games", "休息一下吧", "xiūxi yíxià ba", "Let's take a little break"),
        c("games", "你那边几点了？", "nǐ nàbiān jǐ diǎn le?", "What time is it over there?"),
        c("games", "真希望你在我身边", "zhēn xīwàng nǐ zài wǒ shēnbiān", "I wish you were right here next to me"),
        c("games", "下次见面一起玩", "xià cì jiànmiàn yìqǐ wán", "Next time we see each other, let's play together"),
        c("games", "再陪我玩一局嘛", "zài péi wǒ wán yì jú ma", "Play one more with me, pleeease"),
        c("games", "晚安，做个好梦", "wǎn'ān, zuò ge hǎo mèng", "Good night, sweet dreams"),

        // Her family
        c("family", "阿姨，您好", "āyí, nín hǎo", "Hello, auntie (to her mom)"),
        c("family", "叔叔好", "shūshu hǎo", "Hello, uncle"),
        c("family", "很高兴认识您", "hěn gāoxìng rènshi nín", "Very nice to meet you (polite)"),
        c("family", "我是玉玲的男朋友", "wǒ shì Yùlíng de nánpéngyou", "I'm Yuling's boyfriend"),
        c("family", "太好吃了", "tài hǎochī le", "This is delicious"),
        c("family", "我吃饱了", "wǒ chī bǎo le", "I'm full"),
        c("family", "您辛苦了", "nín xīnkǔ le", "Thank you for all your hard work"),
        c("family", "谢谢您的招待", "xièxie nín de zhāodài", "Thank you for your hospitality"),
        c("family", "我来帮忙", "wǒ lái bāngmáng", "Let me help"),
        c("family", "我会好好照顾她", "wǒ huì hǎohǎo zhàogù tā", "I'll take good care of her"),
        c("family", "祝您身体健康", "zhù nín shēntǐ jiànkāng", "Wishing you good health"),

        // Everyday
        c("daily", "早上好", "zǎoshang hǎo", "Good morning"),
        c("daily", "你吃饭了吗？", "nǐ chīfàn le ma?", "Have you eaten? (a greeting)"),
        c("daily", "我听不懂", "wǒ tīng bu dǒng", "I don't understand (what I hear)"),
        c("daily", "请说慢一点", "qǐng shuō màn yìdiǎn", "Please speak a bit slower"),
        c("daily", "再说一遍", "zài shuō yí biàn", "Say it one more time"),
        c("daily", "这个用中文怎么说？", "zhège yòng Zhōngwén zěnme shuō?", "How do you say this in Chinese?"),
        c("daily", "没关系", "méi guānxi", "It's okay / no worries"),
        c("daily", "没问题", "méi wèntí", "No problem"),
        c("daily", "等一下", "děng yíxià", "Wait a moment"),
        c("daily", "我明白了", "wǒ míngbai le", "I get it now"),
        c("daily", "晚安", "wǎn'ān", "Good night"),
        // Your day, out loud: what you'd tell her on a call or in a message, plus small getting-by lines.
        c("daily", "你今天怎么样？", "nǐ jīntiān zěnmeyàng?", "How was your day?"),
        c("daily", "你呢？", "nǐ ne?", "And you?"),
        c("daily", "我也是", "wǒ yě shì", "Me too"),
        c("daily", "我起床了", "wǒ qǐchuáng le", "I'm up"),
        c("daily", "我去上班了", "wǒ qù shàngbān le", "I'm heading to work"),
        c("daily", "我在开车", "wǒ zài kāichē", "I'm driving"),
        c("daily", "我下班了", "wǒ xiàbān le", "I'm off work"),
        c("daily", "我到家了", "wǒ dào jiā le", "I'm home"),
        c("daily", "我有点忙", "wǒ yǒudiǎn máng", "I'm a little busy"),
        c("daily", "今天好累", "jīntiān hǎo lèi", "Today was so tiring"),
        c("daily", "我饿了", "wǒ è le", "I'm hungry"),
        c("daily", "我渴了", "wǒ kě le", "I'm thirsty"),
        c("daily", "我要洗澡了", "wǒ yào xǐzǎo le", "I'm going to shower"),
        c("daily", "好热啊", "hǎo rè a", "It's so hot"),
        c("daily", "好冷啊", "hǎo lěng a", "It's so cold"),
        c("daily", "你在哪儿？", "nǐ zài nǎr?", "Where are you?"),
        c("daily", "现在几点了？", "xiànzài jǐ diǎn le?", "What time is it now?"),
        c("daily", "一会儿见", "yíhuìr jiàn", "See you in a bit"),
        c("daily", "我知道了", "wǒ zhīdào le", "Got it"),
        c("daily", "我忘了", "wǒ wàng le", "I forgot"),
        c("daily", "慢慢来", "mànman lái", "Take your time"),
        c("daily", "不好意思", "bù hǎoyìsi", "Excuse me / sorry"),
        c("daily", "麻烦你了", "máfan nǐ le", "Sorry for the trouble (thanks for helping)"),
        c("daily", "不用了，谢谢", "bú yòng le, xièxie", "No need, thanks"),

        // Eating out
        c("food", "服务员", "fúwùyuán", "Waiter / server"),
        c("food", "我要这个", "wǒ yào zhège", "I want this one"),
        c("food", "微辣", "wēi là", "Mildly spicy"),
        c("food", "不要太辣", "bú yào tài là", "Not too spicy"),
        c("food", "少放点辣椒", "shǎo fàng diǎn làjiāo", "Go easy on the chili"),
        c("food", "麻辣", "málà", "Numbing and spicy"),
        c("food", "火锅", "huǒguō", "Hot pot"),
        c("food", "好香啊", "hǎo xiāng a", "Smells so good"),
        c("food", "打包", "dǎbāo", "To go / pack it up"),
        c("food", "买单", "mǎidān", "The check, please"),

        // Shopping & paying
        c("shopping", "多少钱？", "duōshao qián?", "How much is it?"),
        c("shopping", "太贵了", "tài guì le", "Too expensive"),
        c("shopping", "便宜一点吧", "piányi yìdiǎn ba", "Make it a little cheaper?"),
        c("shopping", "可以扫码吗？", "kěyǐ sǎo mǎ ma?", "Can I scan to pay?"),
        c("shopping", "我用微信支付", "wǒ yòng Wēixìn zhīfù", "I'll pay with WeChat Pay"),
        c("shopping", "有没有大一点的？", "yǒu méiyǒu dà yìdiǎn de?", "Do you have a bigger one?"),
        c("shopping", "我只是看看", "wǒ zhǐshì kànkan", "I'm just looking"),

        // Getting around
        c("transport", "去高铁站", "qù gāotiě zhàn", "To the high-speed rail station"),
        c("transport", "师傅，到这里停", "shīfu, dào zhèlǐ tíng", "Driver, stop here"),
        c("transport", "离这儿远吗？", "lí zhèr yuǎn ma?", "Is it far from here?"),
        c("transport", "一直走", "yìzhí zǒu", "Go straight"),
        c("transport", "往左拐", "wǎng zuǒ guǎi", "Turn left"),
        c("transport", "往右拐", "wǎng yòu guǎi", "Turn right"),
        c("transport", "堵车", "dǔchē", "Traffic jam"),
        c("transport", "加油站", "jiāyóuzhàn", "Gas station"),
        c("transport", "停车场", "tíngchēchǎng", "Parking lot"),
        c("transport", "驾照", "jiàzhào", "Driver's license"),

        // Home & renting
        c("home", "我想租房子", "wǒ xiǎng zū fángzi", "I want to rent a place"),
        c("home", "房租", "fángzū", "Rent"),
        c("home", "押金", "yājīn", "Deposit"),
        c("home", "水电费", "shuǐdiànfèi", "Water and electric bill"),
        c("home", "空调坏了", "kōngtiáo huài le", "The AC is broken"),
        c("home", "网速太慢了", "wǎngsù tài màn le", "The internet is too slow"),

        // Paperwork
        c("paperwork", "护照", "hùzhào", "Passport"),
        c("paperwork", "签证", "qiānzhèng", "Visa"),
        c("paperwork", "结婚证", "jiéhūnzhèng", "Marriage certificate"),
        c("paperwork", "派出所", "pàichūsuǒ", "Local police station (where you register)"),
        c("paperwork", "我需要办什么手续？", "wǒ xūyào bàn shénme shǒuxù?", "What paperwork do I need to do?"),
        c("paperwork", "复印件", "fùyìnjiàn", "Photocopy"),
        c("paperwork", "银行卡", "yínhángkǎ", "Bank card"),
        c("paperwork", "手机号", "shǒujīhào", "Phone number"),

        // Health
        c("health", "我不舒服", "wǒ bù shūfu", "I don't feel well"),
        c("health", "我头疼", "wǒ tóu téng", "I have a headache"),
        c("health", "我发烧了", "wǒ fāshāo le", "I have a fever"),
        c("health", "医院在哪里？", "yīyuàn zài nǎlǐ?", "Where is the hospital?"),
        c("health", "药店", "yàodiàn", "Pharmacy"),

        // Sichuanese
        c("sichuan", "巴适", "bāshì", "Great, comfy (Sichuanese)"),
        c("sichuan", "要得", "yào de", "Okay, sure (Sichuanese)"),
        c("sichuan", "摆龙门阵", "bǎi lóngménzhèn", "To chit-chat (Sichuanese)"),
    )

    private fun p(title: String) = PackItem(id = "pack:$title", title = title)

    val packing = listOf(
        p("Passport"), p("Visa / entry documents"), p("Flight confirmations"), p("Phone + charger"),
        p("Power bank"), p("Plug adapter"), p("Gifts for her family"), p("Some cash in yuan"),
        p("Card that works with Alipay / WeChat Pay"), p("VPN set up and tested"), p("Medicine"),
        p("Clothes for Sichuan weather"), p("Toiletries"), p("Her address in Chinese (phrasebook)"),
    )

    private fun b(name: String, zh: String) = BudgetItem(id = "budget:$name", name = name, zh = zh)

    val budget = listOf(
        b("Rent", "房租"), b("Food", "吃饭"), b("Car & gas", "车和油费"), b("Phone & internet", "话费网费"),
        b("Utilities", "水电费"), b("Insurance", "保险"), b("Fun & going out", "娱乐"), b("Other", "其他"),
    )

    private fun t(group: String, title: String) = MoveTask(id = "$group:$title", title = title, group = group)

    val tasks = listOf(
        t("Documents", "Passport valid well past the move date"),
        t("Documents", "Research the visa for living with a Chinese spouse"),
        t("Documents", "Plan the marriage paperwork with Yuling"),
        t("Documents", "Certified birth certificate and degree copies"),
        t("Documents", "Get documents authenticated for use in China"),
        t("Money", "Hit the savings goal"),
        t("Money", "Emergency fund set aside"),
        t("Money", "Card that works with Alipay / WeChat Pay"),
        t("Money", "Plan for US bank accounts after moving"),
        t("School & certs", "Finish the AS in CIS at SAC"),
        t("School & certs", "Pass CompTIA A+"),
        t("School & certs", "Pass CompTIA Network+"),
        t("School & certs", "Pass CompTIA Security+"),
        t("Life in China", "Plan for a Chinese driver's license"),
        t("Life in China", "Phone plan / SIM for China"),
        t("Life in China", "Health insurance for China"),
        t("Life in China", "Decide what to ship, sell, or keep"),
        t("Life in China", "Job or remote work lined up"),
    )
}
