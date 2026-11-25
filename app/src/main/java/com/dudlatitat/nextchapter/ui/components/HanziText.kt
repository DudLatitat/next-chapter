package com.dudlatitat.nextchapter.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.data.Sizes
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * Big Chinese text that never overlaps: its own line height (the theme's 24sp line height is far
 * too tight for 44sp characters, which made wrapped lines draw over each other) and a size that
 * steps down for long phrases (Sizes.hanzi).
 */
@Composable
fun HanziText(
    text: String, max: Float, modifier: Modifier = Modifier, color: Color = Night.ink,
    align: TextAlign? = TextAlign.Center, weight: FontWeight? = null,
) {
    val size = Sizes.hanzi(text, max)
    Text(
        text, modifier, color = color, fontSize = size.sp, lineHeight = Sizes.lineHeight(size).sp, textAlign = align, fontWeight = weight,
        style = LocalTextStyle.current.copy(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)),
    )
}
