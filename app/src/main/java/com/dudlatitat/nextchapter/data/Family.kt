package com.dudlatitat.nextchapter.data

import kotlin.random.Random

/**
 * Family map 家谱: what to call each relative on her side, in standard Mandarin. You use the same
 * word she does (a partner follows their partner), except her parents, who are 叔叔 / 阿姨 until
 * you're married. Her mom's side comes first. Real names are typed on the phone only
 * (AppState.familyNames); nothing personal lives here. Local words differ by family and region,
 * so when she uses another word, hers wins.
 */
object Family {
    enum class Side { PARENTS, MOM, DAD }

    data class Relative(
        val id: String,
        val hanzi: String,
        val pinyin: String,
        /** Who it is, from her point of view. */
        val who: String,
        val side: Side,
        /** 0 grandparents, 1 parents' generation, 2 her generation. */
        val row: Int,
        /** What you say when you greet them. */
        val greet: String,
        val greetPinyin: String,
    )

    private fun r(id: String, h: String, p: String, who: String, side: Side, row: Int, g: String, gp: String) = Relative(id, h, p, who, side, row, g, gp)

    val all = listOf(
        r("mom", "阿姨", "āyí", "Her mom", Side.PARENTS, 1, "阿姨好", "āyí hǎo"),
        r("dad", "叔叔", "shūshu", "Her dad", Side.PARENTS, 1, "叔叔好", "shūshu hǎo"),
        // Her mom's side
        r("waigong", "外公", "wàigōng", "Her mom's father", Side.MOM, 0, "外公好", "wàigōng hǎo"),
        r("waipo", "外婆", "wàipó", "Her mom's mother", Side.MOM, 0, "外婆好", "wàipó hǎo"),
        r("jiujiu", "舅舅", "jiùjiu", "Her mom's brother", Side.MOM, 1, "舅舅好", "jiùjiu hǎo"),
        r("jiuma", "舅妈", "jiùmā", "Her mom's brother's wife", Side.MOM, 1, "舅妈好", "jiùmā hǎo"),
        r("yima", "姨妈", "yímā", "Her mom's sister", Side.MOM, 1, "姨妈好", "yímā hǎo"),
        r("yifu", "姨父", "yífu", "Her mom's sister's husband", Side.MOM, 1, "姨父好", "yífu hǎo"),
        r("biaoge", "表哥", "biǎogē", "An older male cousin on her mom's side", Side.MOM, 2, "表哥好", "biǎogē hǎo"),
        r("biaojie", "表姐", "biǎojiě", "An older female cousin on her mom's side", Side.MOM, 2, "表姐好", "biǎojiě hǎo"),
        r("biaodi", "表弟", "biǎodì", "A younger male cousin on her mom's side", Side.MOM, 2, "表弟好", "biǎodì hǎo"),
        r("biaomei", "表妹", "biǎomèi", "A younger female cousin on her mom's side", Side.MOM, 2, "表妹好", "biǎomèi hǎo"),
        // Her dad's side
        r("yeye", "爷爷", "yéye", "Her dad's father", Side.DAD, 0, "爷爷好", "yéye hǎo"),
        r("nainai", "奶奶", "nǎinai", "Her dad's mother", Side.DAD, 0, "奶奶好", "nǎinai hǎo"),
        r("bobo", "伯伯", "bóbo", "Her dad's older brother", Side.DAD, 1, "伯伯好", "bóbo hǎo"),
        r("bomu", "伯母", "bómǔ", "Her dad's older brother's wife", Side.DAD, 1, "伯母好", "bómǔ hǎo"),
        r("shenshen", "婶婶", "shěnshen", "Her dad's younger brother's wife (he is 叔叔 too)", Side.DAD, 1, "婶婶好", "shěnshen hǎo"),
        r("gugu", "姑姑", "gūgu", "Her dad's sister", Side.DAD, 1, "姑姑好", "gūgu hǎo"),
        r("gufu", "姑父", "gūfu", "Her dad's sister's husband", Side.DAD, 1, "姑父好", "gūfu hǎo"),
        r("tangge", "堂哥", "tánggē", "An older male cousin, son of her dad's brother", Side.DAD, 2, "堂哥好", "tánggē hǎo"),
        r("tangjie", "堂姐", "tángjiě", "An older female cousin, daughter of her dad's brother", Side.DAD, 2, "堂姐好", "tángjiě hǎo"),
    )

    fun side(s: Side) = all.filter { it.side == s }

    /** A "Who's this?" question: the relative plus three other titles to choose from, mom's side first. */
    fun question(random: Random = Random.Default, momFirst: Boolean = true): Pair<Relative, List<Relative>> {
        val pool = if (momFirst && random.nextFloat() < 0.75f) all.filter { it.side != Side.DAD } else all
        val answer = pool.random(random)
        val others = all.filter { it.id != answer.id && it.hanzi != answer.hanzi }.shuffled(random).take(3)
        return answer to (others + answer).shuffled(random)
    }
}
