package com.dudlatitat.nextchapter.data

data class Example(val hanzi: String, val pinyin: String, val english: String)

/** A fill-in-the-blank question; the first option is the right one (the screen shuffles them). */
data class Blank(val sentence: String, val options: List<String>, val english: String)

data class GrammarPoint(
    val id: String,
    val title: String,
    val pattern: String,
    val explain: String,
    val examples: List<Example>,
    val quiz: List<Blank>,
)

/** The patterns behind most everyday sentences, simplest first. */
object Grammar {
    private fun ex(h: String, p: String, e: String) = Example(h, p, e)

    val points = listOf(
        GrammarPoint(
            "shi", "是: A is B", "A 是 B",
            "Links two nouns: \"I am American\", \"this is my phone\". Not used with adjectives. For \"I'm tired\" see 很.",
            listOf(
                ex("我是美国人。", "wǒ shì Měiguó rén.", "I'm American."),
                ex("她是我女朋友。", "tā shì wǒ nǚpéngyou.", "She's my girlfriend."),
                ex("这是我的手机。", "zhè shì wǒ de shǒujī.", "This is my phone."),
            ),
            listOf(Blank("我___美国人。", listOf("是", "很", "在"), "I'm American."), Blank("我___累。", listOf("很", "是", "的"), "I'm tired.")),
        ),
        GrammarPoint(
            "hen", "很 + adjective", "Subject 很 Adjective",
            "Adjectives work like verbs, so no 是. 很 (\"very\") usually goes in front even when you just mean \"is\".",
            listOf(
                ex("我很累。", "wǒ hěn lèi.", "I'm tired."),
                ex("火锅很辣。", "huǒguō hěn là.", "Hot pot is spicy."),
                ex("今天很忙。", "jīntiān hěn máng.", "Today is busy."),
            ),
            listOf(Blank("今天___忙。", listOf("很", "是", "了"), "Today is busy."), Blank("这个菜___好吃。", listOf("很", "是", "吗"), "This dish is tasty.")),
        ),
        GrammarPoint(
            "ma", "吗: yes/no questions", "Statement + 吗？",
            "Add 吗 to the end of any statement to make it a yes/no question. The word order doesn't change.",
            listOf(
                ex("你饿吗？", "nǐ è ma?", "Are you hungry?"),
                ex("你是她的朋友吗？", "nǐ shì tā de péngyou ma?", "Are you her friend?"),
                ex("可以扫码吗？", "kěyǐ sǎo mǎ ma?", "Can I scan to pay?"),
            ),
            listOf(Blank("你累___？", listOf("吗", "呢", "的"), "Are you tired?"), Blank("你喜欢火锅___？", listOf("吗", "了", "的"), "Do you like hot pot?")),
        ),
        GrammarPoint(
            "bu-mei", "不 vs 没", "不 + verb · 没(有) + verb",
            "不 says no for now, the future, or habits. 没 says something didn't happen, or you don't have something (没有).",
            listOf(
                ex("我不喝酒。", "wǒ bù hē jiǔ.", "I don't drink alcohol."),
                ex("我没吃饭。", "wǒ méi chīfàn.", "I haven't eaten."),
                ex("我没有钱。", "wǒ méiyǒu qián.", "I don't have money."),
            ),
            listOf(Blank("我昨天___去。", listOf("没", "不", "别"), "I didn't go yesterday."), Blank("我明天___去。", listOf("不", "没", "了"), "I'm not going tomorrow.")),
        ),
        GrammarPoint(
            "le", "了: done, or changed", "Verb + 了 · Sentence + 了",
            "After a verb, 了 means it's done. At the end of a sentence, it often means something is new or has changed.",
            listOf(
                ex("我吃了。", "wǒ chī le.", "I ate."),
                ex("下雨了。", "xià yǔ le.", "It's started raining."),
                ex("我到了！", "wǒ dào le!", "I've arrived!"),
            ),
            listOf(Blank("我到___！", listOf("了", "吗", "过"), "I've arrived!"), Blank("我饿___。", listOf("了", "过", "的"), "I'm hungry now.")),
        ),
        GrammarPoint(
            "guo", "过: have you ever…", "Verb + 过",
            "过 after a verb means you've done it at some point in your life. Negative: 没 + verb + 过.",
            listOf(
                ex("我去过成都。", "wǒ qù guo Chéngdū.", "I've been to Chengdu."),
                ex("你吃过火锅吗？", "nǐ chī guo huǒguō ma?", "Have you ever had hot pot?"),
                ex("我没去过北京。", "wǒ méi qù guo Běijīng.", "I've never been to Beijing."),
            ),
            listOf(Blank("你去___中国吗？", listOf("过", "在", "很"), "Have you ever been to China?"), Blank("我没吃___这个。", listOf("过", "了", "吗"), "I've never eaten this.")),
        ),
        GrammarPoint(
            "zai", "在: doing it right now", "在 + verb",
            "在 before a verb means it's happening right now, like \"-ing\".",
            listOf(
                ex("我在开车。", "wǒ zài kāichē.", "I'm driving."),
                ex("她在睡觉。", "tā zài shuìjiào.", "She's sleeping."),
                ex("你在干嘛？", "nǐ zài gàn má?", "What are you doing?"),
            ),
            listOf(Blank("我___吃饭。", listOf("在", "过", "很"), "I'm eating."), Blank("你___干嘛？", listOf("在", "了", "是"), "What are you doing?")),
        ),
        GrammarPoint(
            "yao-xiang", "要 and 想: want", "要 + thing/verb · 想 + verb",
            "要 is a firm \"want\" or \"going to\". 想 is softer, \"would like to\", and also means \"miss\" (我想你).",
            listOf(
                ex("我要这个。", "wǒ yào zhège.", "I want this one."),
                ex("我想去成都。", "wǒ xiǎng qù Chéngdū.", "I'd like to go to Chengdu."),
                ex("我想你。", "wǒ xiǎng nǐ.", "I miss you."),
            ),
            listOf(Blank("我___一杯咖啡。", listOf("要", "是", "很"), "I want a cup of coffee."), Blank("宝贝，我___你。", listOf("想", "要", "在"), "Baby, I miss you.")),
        ),
        GrammarPoint(
            "hui-neng-keyi", "会 · 能 · 可以: can", "会 / 能 / 可以 + verb",
            "会 is a learned skill (I can speak Chinese). 能 is being able to right now. 可以 is being allowed (may I…?).",
            listOf(
                ex("我会说一点儿中文。", "wǒ huì shuō yìdiǎnr Zhōngwén.", "I can speak a little Chinese."),
                ex("我今天不能来。", "wǒ jīntiān bù néng lái.", "I can't come today."),
                ex("我可以坐这儿吗？", "wǒ kěyǐ zuò zhèr ma?", "May I sit here?"),
            ),
            listOf(Blank("我___说一点儿中文。", listOf("会", "可以", "在"), "I can speak a little Chinese."), Blank("这里___扫码吗？", listOf("可以", "会", "过"), "Can I scan to pay here?")),
        ),
        GrammarPoint(
            "bi", "比: comparing", "A 比 B + adjective",
            "A 比 B 冷 = A is colder than B. Don't add 很. Use 更 for \"even more\".",
            listOf(
                ex("成都比洛杉矶冷。", "Chéngdū bǐ Luòshānjī lěng.", "Chengdu is colder than LA."),
                ex("她比我小。", "tā bǐ wǒ xiǎo.", "She's younger than me."),
                ex("这个比那个便宜。", "zhège bǐ nàge piányi.", "This one is cheaper than that one."),
            ),
            listOf(Blank("火锅___面条辣。", listOf("比", "很", "和"), "Hot pot is spicier than noodles."), Blank("今天___昨天热。", listOf("比", "是", "在"), "Today is hotter than yesterday.")),
        ),
        GrammarPoint(
            "de", "的: 's and describing", "A 的 B · Adjective 的 noun",
            "的 joins an owner to a thing (女朋友的妈妈 = girlfriend's mom) or a description to a noun. With close family it's often dropped (我妈妈).",
            listOf(
                ex("这是我女朋友的妈妈。", "zhè shì wǒ nǚpéngyou de māma.", "This is my girlfriend's mom."),
                ex("我喜欢辣的。", "wǒ xǐhuan là de.", "I like spicy ones."),
                ex("这是我的钥匙。", "zhè shì wǒ de yàoshi.", "These are my keys."),
            ),
            listOf(Blank("这是我___手机。", listOf("的", "了", "吗"), "This is my phone."), Blank("我喜欢辣___。", listOf("的", "过", "在"), "I like spicy ones.")),
        ),
        GrammarPoint(
            "shi-de", "是…的: when, where, how", "是 + detail + verb + 的",
            "For something that already happened, 是…的 puts the focus on when, where or how it happened.",
            listOf(
                ex("我是昨天到的。", "wǒ shì zuótiān dào de.", "I arrived yesterday."),
                ex("你是怎么来的？", "nǐ shì zěnme lái de?", "How did you get here?"),
                ex("我是坐飞机来的。", "wǒ shì zuò fēijī lái de.", "I came by plane."),
            ),
            listOf(Blank("我是坐飞机来___。", listOf("的", "了", "吗"), "I came by plane."), Blank("你___什么时候到的？", listOf("是", "在", "很"), "When did you arrive?")),
        ),
        GrammarPoint(
            "ba", "把: doing something to it", "把 + thing + verb + result",
            "把 moves the thing before the verb, to say what you do to it: close the door, forget the phone, give this to me.",
            listOf(
                ex("请把门关上。", "qǐng bǎ mén guānshang.", "Please close the door."),
                ex("我把手机忘了。", "wǒ bǎ shǒujī wàng le.", "I forgot my phone."),
                ex("把这个给我。", "bǎ zhège gěi wǒ.", "Give this to me."),
            ),
            listOf(Blank("请___门关上。", listOf("把", "比", "在"), "Please close the door."), Blank("我___手机忘了。", listOf("把", "是", "过"), "I forgot my phone.")),
        ),
        GrammarPoint(
            "yao-le", "要…了: about to", "要 + verb + 了",
            "要…了 around a verb means it's about to happen. Handy for \"I'm almost there\".",
            listOf(
                ex("我要到了。", "wǒ yào dào le.", "I'm almost there."),
                ex("要下雨了。", "yào xià yǔ le.", "It's about to rain."),
                ex("飞机要起飞了。", "fēijī yào qǐfēi le.", "The plane is about to take off."),
            ),
            listOf(Blank("我___到了！", listOf("要", "在", "过"), "I'm almost there!"), Blank("要下雨___。", listOf("了", "吗", "的"), "It's about to rain.")),
        ),
    )
}
