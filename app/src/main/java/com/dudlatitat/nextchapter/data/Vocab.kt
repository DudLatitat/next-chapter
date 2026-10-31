package com.dudlatitat.nextchapter.data

/** The ~300 most useful single words: the building blocks for making your own sentences. */
object Vocab {
    private fun v(hanzi: String, pinyin: String, english: String) =
        Card(id = "core:$hanzi", hanzi = hanzi, pinyin = pinyin, english = english, deck = "core")

    val cards = listOf(
        // Everyday expressions
        v("你好", "nǐ hǎo", "Hello"), v("谢谢", "xièxie", "Thank you"), v("不客气", "bú kèqi", "You're welcome"),
        v("对不起", "duìbuqǐ", "Sorry"), v("再见", "zàijiàn", "Goodbye"), v("好的", "hǎo de", "Okay"),
        v("是的", "shì de", "Yes, that's right"), v("不是", "bú shì", "No, it isn't"), v("可能", "kěnéng", "Maybe"),
        v("当然", "dāngrán", "Of course"), v("加油", "jiāyóu", "Go for it! / You can do it"), v("小心", "xiǎoxīn", "Be careful"),
        v("慢走", "mànzǒu", "Take care (to a leaving guest)"), v("欢迎", "huānyíng", "Welcome"), v("恭喜", "gōngxǐ", "Congratulations"),
        v("干杯", "gānbēi", "Cheers!"), v("真的吗？", "zhēn de ma?", "Really?"), v("好久不见", "hǎojiǔ bú jiàn", "Long time no see"),

        // People and question words
        v("我", "wǒ", "I, me"), v("他", "tā", "he, him"), v("她", "tā", "she, her"), v("我们", "wǒmen", "we, us"),
        v("你们", "nǐmen", "you (plural)"), v("他们", "tāmen", "they, them"), v("这", "zhè", "this"), v("那", "nà", "that"),
        v("哪", "nǎ", "which"), v("谁", "shéi", "who"), v("什么", "shénme", "what"), v("怎么", "zěnme", "how"),
        v("为什么", "wèishénme", "why"), v("哪里", "nǎlǐ", "where"), v("几", "jǐ", "how many (small number)"), v("多少", "duōshao", "how many, how much"),

        // Family
        v("妈妈", "māma", "Mom"), v("爸爸", "bàba", "Dad"), v("哥哥", "gēge", "older brother"), v("姐姐", "jiějie", "older sister"),
        v("弟弟", "dìdi", "younger brother"), v("妹妹", "mèimei", "younger sister"), v("女朋友", "nǚpéngyou", "girlfriend"),
        v("男朋友", "nánpéngyou", "boyfriend"), v("老婆", "lǎopo", "wife (casual)"), v("老公", "lǎogōng", "husband (casual)"),
        v("孩子", "háizi", "child"), v("朋友", "péngyou", "friend"), v("家", "jiā", "home, family"),
        v("奶奶", "nǎinai", "grandma (dad's mom)"), v("爷爷", "yéye", "grandpa (dad's dad)"), v("外婆", "wàipó", "grandma (mom's mom)"),

        // Numbers
        v("一", "yī", "one"), v("二", "èr", "two"), v("三", "sān", "three"), v("四", "sì", "four"), v("五", "wǔ", "five"),
        v("六", "liù", "six"), v("七", "qī", "seven"), v("八", "bā", "eight"), v("九", "jiǔ", "nine"), v("十", "shí", "ten"),
        v("百", "bǎi", "hundred"), v("千", "qiān", "thousand"), v("万", "wàn", "ten thousand"), v("两", "liǎng", "two (of something)"),
        v("零", "líng", "zero"), v("半", "bàn", "half"),

        // Measure words
        v("个", "gè", "(general measure word)"), v("块", "kuài", "yuan (spoken); a piece"), v("杯", "bēi", "cup of"),
        v("本", "běn", "(for books)"), v("张", "zhāng", "(for flat things: tickets, photos)"), v("只", "zhī", "(for animals)"),
        v("次", "cì", "times (once, twice…)"), v("件", "jiàn", "(for clothes, matters)"), v("位", "wèi", "(polite, for people)"),
        v("碗", "wǎn", "bowl of"),

        // Time
        v("今天", "jīntiān", "today"), v("明天", "míngtiān", "tomorrow"), v("昨天", "zuótiān", "yesterday"), v("现在", "xiànzài", "now"),
        v("早上", "zǎoshang", "morning"), v("中午", "zhōngwǔ", "noon"), v("下午", "xiàwǔ", "afternoon"), v("晚上", "wǎnshang", "evening"),
        v("点", "diǎn", "o'clock"), v("分钟", "fēnzhōng", "minute"), v("小时", "xiǎoshí", "hour"), v("星期", "xīngqī", "week"),
        v("周末", "zhōumò", "weekend"), v("月", "yuè", "month"), v("年", "nián", "year"), v("时候", "shíhou", "time, when"),
        v("以后", "yǐhòu", "after, later"), v("以前", "yǐqián", "before"), v("马上", "mǎshàng", "right away"), v("已经", "yǐjīng", "already"),
        v("刚才", "gāngcái", "just now"), v("每天", "měitiān", "every day"), v("生日", "shēngrì", "birthday"), v("春节", "Chūnjié", "Spring Festival"),

        // Verbs
        v("是", "shì", "to be"), v("有", "yǒu", "to have; there is"), v("没有", "méiyǒu", "to not have"), v("要", "yào", "to want; will"),
        v("想", "xiǎng", "to want to; to miss; to think"), v("喜欢", "xǐhuan", "to like"), v("爱", "ài", "to love"), v("去", "qù", "to go"),
        v("来", "lái", "to come"), v("回", "huí", "to return"), v("到", "dào", "to arrive"), v("走", "zǒu", "to walk; to leave"),
        v("坐", "zuò", "to sit; to take (a bus, plane)"), v("开车", "kāichē", "to drive"), v("吃", "chī", "to eat"), v("喝", "hē", "to drink"),
        v("睡觉", "shuìjiào", "to sleep"), v("起床", "qǐchuáng", "to get up"), v("工作", "gōngzuò", "to work; job"), v("学习", "xuéxí", "to study"),
        v("说", "shuō", "to speak, say"), v("听", "tīng", "to listen"), v("看", "kàn", "to look, watch"), v("读", "dú", "to read aloud"),
        v("写", "xiě", "to write"), v("买", "mǎi", "to buy"), v("卖", "mài", "to sell"), v("给", "gěi", "to give; for"),
        v("打电话", "dǎ diànhuà", "to make a phone call"), v("等", "děng", "to wait"), v("找", "zhǎo", "to look for"), v("知道", "zhīdào", "to know (a fact)"),
        v("认识", "rènshi", "to know (a person)"), v("觉得", "juéde", "to feel, think"), v("会", "huì", "can (learned skill); will"), v("能", "néng", "can (be able)"),
        v("可以", "kěyǐ", "may, can (allowed)"), v("帮", "bāng", "to help"), v("用", "yòng", "to use"), v("做饭", "zuòfàn", "to cook"),
        v("洗澡", "xǐzǎo", "to shower, bathe"), v("玩", "wán", "to play, have fun"), v("住", "zhù", "to live (somewhere)"), v("结婚", "jiéhūn", "to get married"),
        v("见面", "jiànmiàn", "to meet up"), v("开始", "kāishǐ", "to start"), v("记得", "jìde", "to remember"), v("忘了", "wàng le", "forgot"),
        v("懂", "dǒng", "to understand"), v("告诉", "gàosu", "to tell"), v("问", "wèn", "to ask"), v("送", "sòng", "to give; to see off; to deliver"),
        v("接", "jiē", "to pick up (someone)"), v("准备", "zhǔnbèi", "to prepare"), v("休息", "xiūxi", "to rest"), v("生气", "shēngqì", "to be angry"),

        // Describing
        v("好", "hǎo", "good"), v("大", "dà", "big"), v("小", "xiǎo", "small"), v("多", "duō", "many, much"), v("少", "shǎo", "few, little"),
        v("长", "cháng", "long"), v("高", "gāo", "tall, high"), v("快", "kuài", "fast"), v("慢", "màn", "slow"), v("贵", "guì", "expensive"),
        v("便宜", "piányi", "cheap"), v("漂亮", "piàoliang", "pretty"), v("好看", "hǎokàn", "good-looking"), v("好吃", "hǎochī", "tasty"),
        v("累", "lèi", "tired"), v("忙", "máng", "busy"), v("饿", "è", "hungry"), v("渴", "kě", "thirsty"), v("开心", "kāixīn", "happy"),
        v("难", "nán", "difficult"), v("容易", "róngyì", "easy"), v("远", "yuǎn", "far"), v("近", "jìn", "near"), v("新", "xīn", "new"),
        v("旧", "jiù", "old (things)"), v("对", "duì", "correct"), v("错", "cuò", "wrong"), v("干净", "gānjìng", "clean"),
        v("舒服", "shūfu", "comfortable"), v("辣", "là", "spicy"), v("甜", "tián", "sweet"), v("可爱", "kě'ài", "cute"),
        v("早", "zǎo", "early"), v("晚", "wǎn", "late"),

        // Little words that hold sentences together
        v("很", "hěn", "very"), v("太", "tài", "too"), v("真", "zhēn", "really"), v("非常", "fēicháng", "extremely"),
        v("有点儿", "yǒudiǎnr", "a bit (too)"), v("一点儿", "yìdiǎnr", "a little"), v("一起", "yìqǐ", "together"), v("都", "dōu", "all, both"),
        v("也", "yě", "also"), v("还", "hái", "still; also"), v("就", "jiù", "just; then"), v("才", "cái", "only then; just"),
        v("再", "zài", "again"), v("别", "bié", "don't"), v("不", "bù", "not"), v("没", "méi", "not (didn't)"),
        v("和", "hé", "and"), v("但是", "dànshì", "but"), v("因为", "yīnwèi", "because"), v("所以", "suǒyǐ", "so, therefore"),
        v("如果", "rúguǒ", "if"), v("还是", "háishi", "or (in questions)"), v("或者", "huòzhě", "or"), v("吗", "ma", "(makes a yes/no question)"),
        v("吧", "ba", "(suggestion: let's…, right?)"), v("呢", "ne", "(and you? / what about…)"), v("了", "le", "(done; change of state)"),
        v("的", "de", "(possessive: 's)"), v("在", "zài", "at, in; (doing) now"),

        // Places and getting around
        v("中国", "Zhōngguó", "China"), v("美国", "Měiguó", "USA"), v("成都", "Chéngdū", "Chengdu"), v("四川", "Sìchuān", "Sichuan"),
        v("内江", "Nèijiāng", "Neijiang"), v("资中", "Zīzhōng", "Zizhong"), v("洛杉矶", "Luòshānjī", "Los Angeles"), v("机场", "jīchǎng", "airport"),
        v("飞机", "fēijī", "airplane"), v("火车", "huǒchē", "train"), v("出租车", "chūzūchē", "taxi"), v("地铁", "dìtiě", "subway"),
        v("公交车", "gōngjiāochē", "bus"), v("车", "chē", "car, vehicle"), v("路", "lù", "road"), v("商店", "shāngdiàn", "shop"),
        v("超市", "chāoshì", "supermarket"), v("饭店", "fàndiàn", "restaurant"), v("银行", "yínháng", "bank"), v("医院", "yīyuàn", "hospital"),
        v("学校", "xuéxiào", "school"), v("公司", "gōngsī", "company"), v("厕所", "cèsuǒ", "restroom"), v("房间", "fángjiān", "room"),
        v("酒店", "jiǔdiàn", "hotel"), v("这里", "zhèlǐ", "here"), v("那里", "nàlǐ", "there"), v("外面", "wàimiàn", "outside"),
        v("里面", "lǐmiàn", "inside"), v("上", "shàng", "up, on, above"), v("下", "xià", "down, under, below"), v("前面", "qiánmiàn", "in front"),
        v("后面", "hòumiàn", "behind"), v("左边", "zuǒbian", "left side"), v("右边", "yòubian", "right side"), v("旁边", "pángbiān", "next to"),

        // Food and drink
        v("米饭", "mǐfàn", "rice"), v("面条", "miàntiáo", "noodles"), v("菜", "cài", "dish; vegetables"), v("鸡肉", "jīròu", "chicken"),
        v("牛肉", "niúròu", "beef"), v("猪肉", "zhūròu", "pork"), v("鸡蛋", "jīdàn", "egg"), v("水果", "shuǐguǒ", "fruit"),
        v("苹果", "píngguǒ", "apple"), v("咖啡", "kāfēi", "coffee"), v("啤酒", "píjiǔ", "beer"), v("奶茶", "nǎichá", "milk tea"),
        v("早饭", "zǎofàn", "breakfast"), v("午饭", "wǔfàn", "lunch"), v("晚饭", "wǎnfàn", "dinner"), v("筷子", "kuàizi", "chopsticks"),

        // Things and money
        v("手机", "shǒujī", "cell phone"), v("电脑", "diànnǎo", "computer"), v("衣服", "yīfu", "clothes"), v("东西", "dōngxi", "thing, stuff"),
        v("礼物", "lǐwù", "gift"), v("钥匙", "yàoshi", "key"), v("照片", "zhàopiàn", "photo"), v("微信", "Wēixìn", "WeChat"),
        v("支付宝", "Zhīfùbǎo", "Alipay"), v("人民币", "rénmínbì", "RMB (Chinese money)"), v("美元", "měiyuán", "US dollar"), v("工资", "gōngzī", "salary"),
        v("天气", "tiānqì", "weather"), v("下雨", "xià yǔ", "to rain"), v("狗", "gǒu", "dog"),

        // Body and health
        v("头", "tóu", "head"), v("肚子", "dùzi", "belly"), v("手", "shǒu", "hand"), v("眼睛", "yǎnjing", "eyes"),
        v("身体", "shēntǐ", "body; health"), v("病", "bìng", "illness"), v("感冒", "gǎnmào", "a cold; to catch a cold"),
    )
}
