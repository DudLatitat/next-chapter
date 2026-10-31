package com.dudlatitat.nextchapter.data

/** The starter phrases and move checklist. Bump [VERSION] when adding more: new ones get merged in. */
object Seed {
    const val VERSION = 2

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
