package com.dudlatitat.nextchapter.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.R
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// The same night-sea look as Two Skies: deep blue ground, glassy cards, Nunito for words and
// Fraunces for titles and big numbers. Chinese falls back to the phone's own font.

object Night {
    val groundTop = Color(0xFF2A3E78)
    val ground1 = Color(0xFF17214A)
    val ground2 = Color(0xFF0E1532)
    val groundBottom = Color(0xFF0B1129)
    val card = Color(0xFF18214A)
    val cardHigh = Color(0xFF1D2752)
    val cardTop = Color(0xFF243063)
    val cardBottom = Color(0xFF1A2350)
    val outline = Color(0xFF2E3866)
    val muted = Color(0xFFA9B1D6)
    val ink = Color(0xFFEEF1FF)
    val tabBar = Color(0xEB090D20)

    val sun = Color(0xFFFFCF7A)
    val rose = Color(0xFFFF8FB1)
    val jade = Color(0xFF4FB3A9)
    val violet = Color(0xFF8E94F2)
    val coral = Color(0xFFFF6B6B)

    val ground = Brush.verticalGradient(0f to groundTop, 0.25f to ground1, 0.6f to ground2, 1f to groundBottom)
    val cardBrush = Brush.verticalGradient(listOf(cardTop, cardBottom))
}

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_semibold, FontWeight.Medium),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

val Fraunces = FontFamily(
    Font(R.font.fraunces_light, FontWeight.Light),
    Font(R.font.fraunces_light, FontWeight.Normal),
    Font(R.font.fraunces_medium, FontWeight.Medium),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
    Font(R.font.fraunces_semibold, FontWeight.Bold),
    Font(R.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
)

private val AppTypography: Typography = Typography().let { b ->
    fun TextStyle.serif() = copy(fontFamily = Fraunces)
    fun TextStyle.round() = copy(fontFamily = Nunito)
    Typography(
        displayLarge = b.displayLarge.serif(),
        displayMedium = b.displayMedium.serif(),
        displaySmall = b.displaySmall.serif(),
        headlineLarge = b.headlineLarge.serif().copy(fontWeight = FontWeight.Medium, fontSize = 34.sp),
        headlineMedium = b.headlineMedium.serif().copy(fontWeight = FontWeight.Medium),
        headlineSmall = b.headlineSmall.serif().copy(fontWeight = FontWeight.Medium),
        titleLarge = b.titleLarge.round().copy(fontWeight = FontWeight.Bold),
        titleMedium = b.titleMedium.round().copy(fontWeight = FontWeight.Bold),
        titleSmall = b.titleSmall.round().copy(fontWeight = FontWeight.Bold),
        bodyLarge = b.bodyLarge.round(),
        bodyMedium = b.bodyMedium.round(),
        bodySmall = b.bodySmall.round(),
        labelLarge = b.labelLarge.round().copy(fontWeight = FontWeight.Bold),
        labelMedium = b.labelMedium.round().copy(fontWeight = FontWeight.Bold),
        labelSmall = b.labelSmall.round().copy(fontWeight = FontWeight.Bold),
    )
}

private val Colors = darkColorScheme(
    primary = Night.sun,
    onPrimary = Color(0xFF2A1E05),
    secondary = Night.rose,
    onSecondary = Color(0xFF3A0F1E),
    tertiary = Night.jade,
    background = Night.ground2,
    onBackground = Night.ink,
    surface = Color(0xFF131B40),
    onSurface = Night.ink,
    surfaceVariant = Color(0xFF263160),
    onSurfaceVariant = Night.muted,
    surfaceContainerLowest = Color(0xFF0A0F26),
    surfaceContainerLow = Color(0xFF151D44),
    surfaceContainer = Night.card,
    surfaceContainerHigh = Night.cardHigh,
    surfaceContainerHighest = Color(0xFF263160),
    outline = Color(0xFF6E77A3),
    outlineVariant = Night.outline,
    error = Night.coral,
)

@Composable
fun NextChapterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
}

/** The night ground with slowly twinkling stars, behind every tab. */
@Composable
fun StarryBackdrop(modifier: Modifier = Modifier.fillMaxSize()) {
    val stars = remember {
        val r = Random(13)
        List(36) { floatArrayOf(r.nextFloat(), r.nextFloat() * 0.7f, 0.6f + r.nextFloat() * 1.2f, r.nextFloat() * 2f * PI.toFloat()) }
    }
    // A slow twinkle doesn't need 120 frames a second: step it 10 times a second, and draw it in
    // its own layer so the stars redraw without touching anything on top.
    var t by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val start = System.nanoTime()
        while (true) {
            kotlinx.coroutines.delay(100)
            t = (((System.nanoTime() - start) / 1e9 / 9.0) % 1.0 * 2 * PI).toFloat()
        }
    }
    Canvas(modifier.graphicsLayer()) {
        drawRect(Night.ground)
        stars.forEach { (x, y, size, phase) ->
            val a = 0.35f + 0.45f * ((sin(t + phase) + 1f) / 2f)
            drawCircle(Color.White.copy(alpha = a), radius = size * density, center = Offset(x * this.size.width, y * this.size.height))
        }
    }
}
