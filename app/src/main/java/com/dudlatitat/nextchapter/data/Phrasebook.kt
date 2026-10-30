package com.dudlatitat.nextchapter.data

/** Lines to show someone in a hurry: big text, no quiz. */
data class Survival(val hanzi: String, val pinyin: String, val english: String)

object Phrasebook {
    val lines = listOf(
        Survival("请带我去这个地址", "qǐng dài wǒ qù zhège dìzhǐ", "Please take me to this address"),
        Survival("我不太会说中文", "wǒ bú tài huì shuō Zhōngwén", "I don't speak much Chinese"),
        Survival("请写下来", "qǐng xiě xiàlái", "Please write it down"),
        Survival("请帮帮我", "qǐng bāngbang wǒ", "Please help me"),
        Survival("我迷路了", "wǒ mílù le", "I'm lost"),
        Survival("厕所在哪里？", "cèsuǒ zài nǎlǐ?", "Where's the bathroom?"),
        Survival("我是美国人", "wǒ shì Měiguó rén", "I'm American"),
        Survival("我的手机没电了", "wǒ de shǒujī méi diàn le", "My phone is dead"),
        Survival("可以刷卡吗？", "kěyǐ shuā kǎ ma?", "Can I pay by card?"),
        Survival("这个辣不辣？", "zhège là bu là?", "Is this spicy?"),
        Survival("我在等我女朋友", "wǒ zài děng wǒ nǚpéngyou", "I'm waiting for my girlfriend"),
        Survival("我需要看医生", "wǒ xūyào kàn yīshēng", "I need to see a doctor"),
        Survival("请叫救护车", "qǐng jiào jiùhùchē", "Please call an ambulance"),
    )
}
