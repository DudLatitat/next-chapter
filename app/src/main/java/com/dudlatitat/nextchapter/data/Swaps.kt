package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import kotlin.random.Random

/** A word that fits the slot in a [Swaps.Frame], with the whole sentence's English. */
data class SwapWord(val hanzi: String, val pinyin: String, val english: String)

/**
 * Swap-a-word: sentence frames with one open slot, so a sentence you know becomes new ones
 * (我想跟你一起玩 → 吃饭 / 看电影). Slot words lean on the first words of the Refold 1k list
 * Rudy already knows (吃 喝 看 电影 电视 书 水 医院 学校 飞机…). Pure, tested in SwapsTest.
 */
object Swaps {
    /** [words] as hanzi to pinyin, with the slot at [slot]; [end] is the punctuation (Chinese, pinyin). */
    data class Frame(val words: List<Pair<String, String>>, val slot: Int, val end: Pair<String, String>, val options: List<SwapWord>)

    private fun w(vararg p: String) = p.toList().chunked(2).map { it[0] to it[1] }
    private val dot = "。" to "."
    private val ask = "？" to "?"

    val frames = listOf(
        Frame(w("我", "wǒ", "想", "xiǎng", "跟", "gēn", "你", "nǐ", "一起", "yìqǐ", "", ""), 5, dot, listOf(
            SwapWord("玩", "wán", "I want to play with you."),
            SwapWord("吃饭", "chīfàn", "I want to have a meal with you."),
            SwapWord("看电影", "kàn diànyǐng", "I want to watch a movie with you."),
            SwapWord("看电视", "kàn diànshì", "I want to watch TV with you."),
            SwapWord("买东西", "mǎi dōngxi", "I want to go shopping with you."),
        )),
        Frame(w("你", "nǐ", "想", "xiǎng", "", "", "吗", "ma"), 2, ask, listOf(
            SwapWord("吃饭", "chīfàn", "Do you want to eat?"),
            SwapWord("喝水", "hē shuǐ", "Do you want some water?"),
            SwapWord("睡觉", "shuìjiào", "Do you want to sleep?"),
            SwapWord("出去", "chūqù", "Do you want to go out?"),
            SwapWord("回家", "huí jiā", "Do you want to go home?"),
        )),
        Frame(w("我", "wǒ", "在", "zài", "", ""), 2, dot, listOf(
            SwapWord("家", "jiā", "I'm at home."),
            SwapWord("公司", "gōngsī", "I'm at the office."),
            SwapWord("学校", "xuéxiào", "I'm at school."),
            SwapWord("医院", "yīyuàn", "I'm at the hospital."),
            SwapWord("机场", "jīchǎng", "I'm at the airport."),
        )),
        Frame(w("我", "wǒ", "在", "zài", "", "", "呢", "ne"), 2, dot, listOf(
            SwapWord("开车", "kāichē", "I'm driving right now."),
            SwapWord("吃饭", "chīfàn", "I'm eating right now."),
            SwapWord("工作", "gōngzuò", "I'm working right now."),
            SwapWord("看书", "kàn shū", "I'm reading right now."),
            SwapWord("打电话", "dǎ diànhuà", "I'm on the phone right now."),
        )),
        Frame(w("我", "wǒ", "喜欢", "xǐhuan", "", ""), 2, dot, listOf(
            SwapWord("你", "nǐ", "I like you."),
            SwapWord("狗", "gǒu", "I like dogs."),
            SwapWord("看书", "kàn shū", "I like reading."),
            SwapWord("喝茶", "hē chá", "I like drinking tea."),
            SwapWord("四川菜", "Sìchuān cài", "I like Sichuan food."),
        )),
        Frame(w("", "", "多少", "duōshao", "钱", "qián"), 0, ask, listOf(
            SwapWord("这个", "zhège", "How much is this?"),
            SwapWord("那个", "nàge", "How much is that?"),
            SwapWord("这件衣服", "zhè jiàn yīfu", "How much is this shirt?"),
            SwapWord("机票", "jīpiào", "How much is the plane ticket?"),
        )),
        Frame(w("我们", "wǒmen", "明天", "míngtiān", "去", "qù", "", "", "吧", "ba"), 3, dot, listOf(
            SwapWord("公园", "gōngyuán", "Let's go to the park tomorrow."),
            SwapWord("成都", "Chéngdū", "Let's go to Chengdu tomorrow."),
            SwapWord("你家", "nǐ jiā", "Let's go to your place tomorrow."),
            SwapWord("超市", "chāoshì", "Let's go to the supermarket tomorrow."),
        )),
        Frame(w("我", "wǒ", "", "", "你", "nǐ"), 1, dot, listOf(
            SwapWord("爱", "ài", "I love you."),
            SwapWord("想", "xiǎng", "I miss you."),
            SwapWord("等", "děng", "I'll wait for you."),
        )),
        Frame(w("你", "nǐ", "会", "huì", "说", "shuō", "", "", "吗", "ma"), 3, ask, listOf(
            SwapWord("中文", "Zhōngwén", "Can you speak Chinese?"),
            SwapWord("英文", "Yīngwén", "Can you speak English?"),
            SwapWord("四川话", "Sìchuānhuà", "Can you speak Sichuanese?"),
        )),
        Frame(w("我", "wǒ", "今天", "jīntiān", "很", "hěn", "", ""), 3, dot, listOf(
            SwapWord("累", "lèi", "I'm really tired today."),
            SwapWord("忙", "máng", "I'm really busy today."),
            SwapWord("高兴", "gāoxìng", "I'm really happy today."),
            SwapWord("饿", "è", "I'm really hungry today."),
        )),
    )

    /** The frame's words with [word] in the slot. */
    fun fill(f: Frame, word: SwapWord): List<String> = f.words.mapIndexed { i, (h, _) -> if (i == f.slot) word.hanzi else h }

    /** The whole sentence as a card (for the voice, the pinyin and the speaking check). */
    fun card(f: Frame, word: SwapWord): Card {
        val hanzi = fill(f, word).joinToString("") + f.end.first
        val pinyin = f.words.mapIndexed { i, (_, p) -> if (i == f.slot) word.pinyin else p }.joinToString(" ") + f.end.second
        return Card("swap:$hanzi", hanzi, pinyin, word.english, "swap")
    }

    /** One swap: a sentence you know ([from]) and the new one to build ([to]), with decoy slot words. */
    data class Swap(val from: Card, val to: Card, val answer: List<String>, val tiles: List<String>)

    fun pick(random: Random): Swap {
        val f = frames[random.nextInt(frames.size)]
        val (a, b) = f.options.shuffled(random).take(2)
        val answer = fill(f, b)
        val decoys = (f.options - b).shuffled(random).take(2).map { it.hanzi }
        return Swap(card(f, a), card(f, b), answer, (answer + decoys).shuffled(random))
    }
}

/** Tonight's sentence: what to say to her, shown on Today from 6 to 7 pm on [day]. */
@Serializable
data class TonightLine(val day: Long, val hanzi: String, val pinyin: String, val english: String) {
    companion object {
        const val FROM = 18 * 60
        const val UNTIL = 19 * 60

        /** Starred before 7 pm: tonight. After: tomorrow evening. */
        fun dayFor(today: Long, nowMinutes: Int): Long = if (nowMinutes >= UNTIL) today + 1 else today

        fun showing(line: TonightLine?, today: Long, nowMinutes: Int): Boolean =
            line != null && line.day == today && nowMinutes in FROM until UNTIL
    }
}

/** Trip prep: the 出发 deck opens 60 days before a trip; then 3 trip phrases a day is the goal. */
object TripPrep {
    const val OPENS = 60
    const val GOAL = 3

    /** Days until the next trip (planner trips first, else the hand-set date), or null. */
    fun daysToTrip(state: AppState, today: Long): Long? {
        val planned = state.trips.mapNotNull { parseDate(it.depart)?.toEpochDay() }.filter { it >= today }.minOrNull()
        val d = planned ?: parseDate(state.tripDate)?.toEpochDay()?.takeIf { it >= today }
        return d?.let { it - today }
    }

    fun open(state: AppState, today: Long): Boolean = daysToTrip(state, today)?.let { it in 0..OPENS.toLong() } == true

    /** Days until the deck opens (null if it's open or there's no trip). */
    fun opensIn(state: AppState, today: Long): Long? = daysToTrip(state, today)?.let { it - OPENS }?.takeIf { it > 0 }

    fun saidToday(state: AppState, today: Long): Int = state.cards.count { it.deck == "trip" && it.last == today }
}
