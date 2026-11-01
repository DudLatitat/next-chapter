package com.dudlatitat.nextchapter.data

data class CultureSection(val id: String, val title: String, val hanzi: String, val tips: List<String>, val phrases: List<Example>)

/** Customs for meeting her family, eating, toasting and the holidays, with the words to use. */
object Culture {
    private fun p(h: String, py: String, e: String) = Example(h, py, e)

    val sections = listOf(
        CultureSection(
            "gifts", "Bringing gifts", "送礼",
            listOf(
                "Bring something for her mom on the first visit: good fruit, quality tea, health supplements, or something nice from California (nuts, chocolates, skincare).",
                "Hand it over with both hands. She may politely refuse once or twice. Offer again with a smile; that's part of the ritual.",
                "Don't expect it to be opened in front of you; that's normal.",
                "Avoid clocks (送钟 sounds like attending a funeral), umbrellas and pears (they sound like separating), anything in fours (四 sounds like 死, death), and green hats.",
                "Red or gold wrapping is lucky; avoid plain white or black.",
            ),
            listOf(
                p("一点小心意。", "yìdiǎn xiǎo xīnyì.", "Just a small token."),
                p("这是从美国带来的。", "zhè shì cóng Měiguó dàilái de.", "I brought this from America."),
                p("您别客气，收下吧。", "nín bié kèqi, shōuxià ba.", "Please, don't be polite, take it."),
            ),
        ),
        CultureSection(
            "table", "At the table", "吃饭",
            listOf(
                "Wait for the elders to start eating, and let the host lead.",
                "Her mom will probably keep putting food in your bowl (夹菜). Eat it and thank her; it's how she shows care.",
                "Try a bit of everything, and compliment the cooking often.",
                "Never stand chopsticks upright in your rice (it looks like funeral incense), and don't tap the bowl with them.",
                "Leaving a little food on your plate shows you're full; a clean plate can mean \"more please\".",
                "At restaurants, people politely fight over the bill. Offering to pay, at least once, is a good look.",
            ),
            listOf(
                p("太好吃了！", "tài hǎochī le!", "This is delicious!"),
                p("阿姨的手艺真好。", "āyí de shǒuyì zhēn hǎo.", "Auntie, your cooking is amazing."),
                p("我吃饱了，谢谢。", "wǒ chī bǎo le, xièxie.", "I'm full, thank you."),
                p("我来买单。", "wǒ lái mǎidān.", "Let me get the bill."),
            ),
        ),
        CultureSection(
            "toast", "Toasting", "敬酒",
            listOf(
                "干杯 literally means \"dry the glass\". With elders, a sip is usually fine unless everyone's finishing.",
                "When you clink with an elder, hold your glass a little lower than theirs. It's a sign of respect.",
                "Toast the elders first, especially her parents, and hold your glass with both hands.",
                "If you don't want to drink, toast with tea or juice and say 以茶代酒.",
            ),
            listOf(
                p("干杯！", "gānbēi!", "Cheers!"),
                p("我敬您一杯。", "wǒ jìng nín yì bēi.", "Let me toast you."),
                p("祝您身体健康！", "zhù nín shēntǐ jiànkāng!", "Wishing you good health!"),
                p("我以茶代酒。", "wǒ yǐ chá dài jiǔ.", "I'll toast with tea instead."),
            ),
        ),
        CultureSection(
            "names", "What to call people", "称呼",
            listOf(
                "Call her mom 阿姨 (auntie) and her dad 叔叔 (uncle). After you're married, it becomes 妈 and 爸.",
                "Her mom's parents are 外公 and 外婆; her dad's parents are 爷爷 and 奶奶. Follow how Yuling addresses each relative.",
                "Never call an elder by their given name. When unsure, ask Yuling quietly. Getting it right means a lot.",
                "Greet each elder by title when you arrive and when you leave.",
            ),
            listOf(
                p("阿姨好！", "āyí hǎo!", "Hello, auntie!"),
                p("叔叔好！", "shūshu hǎo!", "Hello, uncle!"),
                p("外婆好！", "wàipó hǎo!", "Hello, grandma!"),
                p("爷爷奶奶好！", "yéye nǎinai hǎo!", "Hello, grandpa and grandma!"),
            ),
        ),
        CultureSection(
            "visit", "Visiting a home", "做客",
            listOf(
                "Take your shoes off at the door; you'll usually be offered slippers.",
                "Accept the tea and fruit you're offered, even if you just take a little.",
                "Personal questions about age, salary, marriage or kids are normal and come from care, not nosiness.",
                "If someone compliments you, it's modest to brush it off: 哪里哪里.",
                "Offer to help clean up. They'll probably refuse, but offering matters.",
                "When you leave, they'll walk you out. Say 请留步 (\"please, no need to see me out\").",
            ),
            listOf(
                p("打扰了。", "dǎrǎo le.", "Sorry to bother you (on arriving or leaving)."),
                p("哪里哪里。", "nǎlǐ nǎlǐ.", "Oh, not at all."),
                p("请留步。", "qǐng liúbù.", "Please, no need to see me out."),
                p("下次再来！", "xià cì zài lái!", "I'll come again next time!"),
            ),
        ),
        CultureSection(
            "newyear", "Spring Festival", "春节",
            listOf(
                "Chinese New Year is the biggest holiday of the year; in 2027 it falls on February 6. Families gather from all over.",
                "New Year's Eve means the reunion dinner (年夜饭), usually a huge family meal.",
                "Red envelopes (红包) with money go to children and elders, usually from married adults. Amounts with 8 are lucky; avoid 4.",
                "New clothes, fireworks, and lots of visiting relatives. Don't sweep on New Year's Day; it's said to sweep away luck.",
            ),
            listOf(
                p("新年快乐！", "xīnnián kuàilè!", "Happy New Year!"),
                p("恭喜发财！", "gōngxǐ fācái!", "Wishing you wealth!"),
                p("万事如意！", "wànshì rúyì!", "May everything go your way!"),
                p("给您拜年了！", "gěi nín bàinián le!", "Happy New Year to you (to elders)!"),
            ),
        ),
        CultureSection(
            "sichuan", "Life in Sichuan", "四川",
            listOf(
                "Sichuan food is numbing and spicy (麻辣). Even 微辣 (mild) is spicy for most visitors, so order carefully at first.",
                "Tea houses and mahjong (打麻将) are a big part of social life; joining a game is a great way to bond.",
                "People are known for being relaxed and enjoying life. You'll hear 安逸 and 巴适 (comfy, great) all the time.",
            ),
            listOf(
                p("安逸！", "ānyì!", "So comfy / great! (Sichuanese)"),
                p("我们打麻将吧。", "wǒmen dǎ májiàng ba.", "Let's play mahjong."),
                p("我不太能吃辣。", "wǒ bú tài néng chī là.", "I can't handle much spice."),
            ),
        ),
    )
}
