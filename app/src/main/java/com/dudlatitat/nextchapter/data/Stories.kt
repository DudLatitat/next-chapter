package com.dudlatitat.nextchapter.data

data class Story(val id: String, val title: String, val hanzi: String, val level: Int, val sentences: List<Example>, val glossary: List<Example>)

/** Short stories set in your own life, using words you're learning. Easiest first. */
object Stories {
    private fun s(h: String, p: String, e: String) = Example(h, p, e)

    val all = listOf(
        Story(
            "morning", "Rudy's morning", "早上", 1,
            listOf(
                s("早上七点，我起床了。", "zǎoshang qī diǎn, wǒ qǐchuáng le.", "At seven in the morning, I got up."),
                s("我很累，但是我很开心。", "wǒ hěn lèi, dànshì wǒ hěn kāixīn.", "I'm tired, but I'm happy."),
                s("我给女朋友打电话。", "wǒ gěi nǚpéngyou dǎ diànhuà.", "I call my girlfriend."),
                s("中国现在是晚上。", "Zhōngguó xiànzài shì wǎnshang.", "It's evening in China now."),
                s("她说：“你吃饭了吗？”", "tā shuō: \"nǐ chīfàn le ma?\"", "She says, \"Have you eaten?\""),
                s("我说：“还没有，我要去工作了。”", "wǒ shuō: \"hái méiyǒu, wǒ yào qù gōngzuò le.\"", "I say, \"Not yet, I'm about to go to work.\""),
                s("她说：“早点吃饭，别太累！”", "tā shuō: \"zǎo diǎn chīfàn, bié tài lèi!\"", "She says, \"Eat soon, don't wear yourself out!\""),
                s("我说：“好的，宝贝。我想你。”", "wǒ shuō: \"hǎo de, bǎobèi. wǒ xiǎng nǐ.\"", "I say, \"Okay, baby. I miss you.\""),
            ),
            listOf(s("女朋友", "nǚpéngyou", "girlfriend"), s("早点", "zǎo diǎn", "a bit earlier, soon")),
        ),
        Story(
            "airport", "Flying to Chengdu", "去成都", 2,
            listOf(
                s("今天我坐飞机去成都。", "jīntiān wǒ zuò fēijī qù Chéngdū.", "Today I'm flying to Chengdu."),
                s("飞机很大，人很多。", "fēijī hěn dà, rén hěn duō.", "The plane is big, and there are lots of people."),
                s("我坐了十四个小时。", "wǒ zuò le shísì gè xiǎoshí.", "I sat for fourteen hours."),
                s("到了成都，我有点儿饿。", "dào le Chéngdū, wǒ yǒudiǎnr è.", "When I got to Chengdu, I was a bit hungry."),
                s("女朋友在机场等我。", "nǚpéngyou zài jīchǎng děng wǒ.", "My girlfriend was waiting for me at the airport."),
                s("她看到我，很高兴地说：“你终于来了！”", "tā kàndào wǒ, hěn gāoxìng de shuō: \"nǐ zhōngyú lái le!\"", "She saw me and happily said, \"You're finally here!\""),
                s("我们一起去吃火锅。", "wǒmen yìqǐ qù chī huǒguō.", "We went to eat hot pot together."),
                s("火锅很辣，但是很好吃。", "huǒguō hěn là, dànshì hěn hǎochī.", "The hot pot was spicy, but delicious."),
            ),
            listOf(s("看到", "kàndào", "to see"), s("高兴", "gāoxìng", "glad, happy"), s("地", "de", "(turns a word into \"-ly\")"), s("终于", "zhōngyú", "finally"), s("人", "rén", "person, people")),
        ),
        Story(
            "dinner", "Dinner at her mom's", "在阿姨家", 3,
            listOf(
                s("星期六，我们坐火车去她妈妈家。", "xīngqīliù, wǒmen zuò huǒchē qù tā māma jiā.", "On Saturday, we took the train to her mom's place."),
                s("阿姨做了很多菜。", "āyí zuò le hěn duō cài.", "Auntie made lots of dishes."),
                s("我说：“阿姨，您辛苦了！”", "wǒ shuō: \"āyí, nín xīnkǔ le!\"", "I said, \"Auntie, thank you for all your work!\""),
                s("阿姨笑着说：“快吃吧，别客气。”", "āyí xiào zhe shuō: \"kuài chī ba, bié kèqi.\"", "Auntie smiled and said, \"Eat up, don't be shy.\""),
                s("她一直给我夹菜。", "tā yìzhí gěi wǒ jiā cài.", "She kept putting food in my bowl."),
                s("我吃了三碗米饭。", "wǒ chī le sān wǎn mǐfàn.", "I ate three bowls of rice."),
                s("吃完饭，我帮阿姨洗碗。", "chī wán fàn, wǒ bāng āyí xǐ wǎn.", "After dinner, I helped Auntie wash the dishes."),
                s("阿姨对她说：“这个男朋友不错！”", "āyí duì tā shuō: \"zhège nánpéngyou búcuò!\"", "Auntie said to her, \"This boyfriend is pretty good!\""),
            ),
            listOf(
                s("星期六", "xīngqīliù", "Saturday"), s("阿姨", "āyí", "auntie (her mom)"), s("您", "nín", "you (polite)"), s("辛苦", "xīnkǔ", "hard work"),
                s("笑", "xiào", "to smile, laugh"), s("着", "zhe", "(while doing…)"), s("客气", "kèqi", "polite, formal"), s("一直", "yìzhí", "the whole time"),
                s("夹菜", "jiā cài", "to put food in someone's bowl"), s("完", "wán", "to finish"), s("洗碗", "xǐ wǎn", "to wash the dishes"), s("不错", "búcuò", "pretty good"),
            ),
        ),
        Story(
            "house", "House hunting in Sichuan", "看房子", 4,
            listOf(
                s("以后我们想住在四川。", "yǐhòu wǒmen xiǎng zhù zài Sìchuān.", "Someday we want to live in Sichuan."),
                s("今天我们去看房子。", "jīntiān wǒmen qù kàn fángzi.", "Today we went to look at apartments."),
                s("第一个房子很大，但是离她妈妈家太远了。", "dì yī gè fángzi hěn dà, dànshì lí tā māma jiā tài yuǎn le.", "The first one was big, but too far from her mom's place."),
                s("第二个房子比第一个小，可是很干净，也很便宜。", "dì èr gè fángzi bǐ dì yī gè xiǎo, kěshì hěn gānjìng, yě hěn piányi.", "The second was smaller than the first, but very clean and cheap too."),
                s("房东问我：“你是哪国人？”", "fángdōng wèn wǒ: \"nǐ shì nǎ guó rén?\"", "The landlord asked me, \"What country are you from?\""),
                s("我说：“我是美国人，可是我很喜欢四川。”", "wǒ shuō: \"wǒ shì Měiguó rén, kěshì wǒ hěn xǐhuan Sìchuān.\"", "I said, \"I'm American, but I really like Sichuan.\""),
                s("他说：“你的中文说得真好！”", "tā shuō: \"nǐ de Zhōngwén shuō de zhēn hǎo!\"", "He said, \"Your Chinese is really good!\""),
                s("我笑着说：“哪里哪里，我还在学习。”", "wǒ xiào zhe shuō: \"nǎlǐ nǎlǐ, wǒ hái zài xuéxí.\"", "I smiled and said, \"Oh, not really, I'm still learning.\""),
            ),
            listOf(
                s("房子", "fángzi", "house, apartment"), s("第一", "dì yī", "first"), s("第二", "dì èr", "second"), s("离", "lí", "away from"),
                s("可是", "kěshì", "but"), s("房东", "fángdōng", "landlord"), s("国", "guó", "country"), s("得", "de", "(how well something is done)"),
                s("哪里哪里", "nǎlǐ nǎlǐ", "oh, not at all (modest reply to a compliment)"), s("中文", "Zhōngwén", "Chinese language"), s("笑", "xiào", "to smile, laugh"), s("着", "zhe", "(while doing…)"),
            ),
        ),
    )
}
