package com.dudlatitat.nextchapter.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.dudlatitat.nextchapter.data.Pinyin
import com.dudlatitat.nextchapter.ui.theme.Night

/** The colour for each tone: 1 high, 2 rising, 3 dipping, 4 falling, 5 neutral. */
fun toneColor(tone: Int): Color = when (tone) {
    1 -> Night.coral
    2 -> Night.sun
    3 -> Night.jade
    4 -> Night.violet
    else -> Night.muted
}

/** Pinyin with each syllable coloured by its tone. */
@Composable
fun PinyinText(pinyin: String, style: TextStyle = LocalTextStyle.current, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    val text = remember(pinyin) {
        val parts = Pinyin.syllablesIn(pinyin)
        buildAnnotatedString {
            var at = 0
            parts.forEach { (range, tone) ->
                if (range.first > at) withStyle(SpanStyle(color = Night.muted)) { append(pinyin.substring(at, range.first)) }
                withStyle(SpanStyle(color = toneColor(tone))) { append(pinyin.substring(range.first, range.last + 1)) }
                at = range.last + 1
            }
            if (at < pinyin.length) withStyle(SpanStyle(color = Night.muted)) { append(pinyin.substring(at)) }
        }
    }
    Text(text, modifier = modifier, style = style, textAlign = textAlign)
}
