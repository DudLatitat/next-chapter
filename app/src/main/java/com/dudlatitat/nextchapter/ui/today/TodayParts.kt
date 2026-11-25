package com.dudlatitat.nextchapter.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.DayLine
import com.dudlatitat.nextchapter.data.NowPlan
import com.dudlatitat.nextchapter.data.Pillars
import com.dudlatitat.nextchapter.data.Routine
import com.dudlatitat.nextchapter.data.phraseOfTheDay
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.HanziText
import com.dudlatitat.nextchapter.ui.components.HeroCard
import com.dudlatitat.nextchapter.ui.components.IconBadge
import com.dudlatitat.nextchapter.ui.components.PinyinText
import com.dudlatitat.nextchapter.ui.components.PrimaryButton
import com.dudlatitat.nextchapter.ui.components.SecondaryButton
import com.dudlatitat.nextchapter.ui.components.pressClick
import com.dudlatitat.nextchapter.ui.theme.MonoStyle
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * The one card for right now (`NowPlan`): what fits this moment of your day, with at most one
 * main button. The call hour asks nothing; after wind-down there's nothing to start.
 */
@Composable
fun NowHero(
    kind: NowPlan.Kind, state: AppState, vm: MainViewModel, startJob: (com.dudlatitat.nextchapter.data.RoutineDay) -> Unit,
    onPray: () -> Unit, onReview: () -> Unit, onDrive: () -> Unit, onAPlus: () -> Unit,
) {
    val her = state.herName.trim().ifEmpty { "her" }
    val phrase = state.phraseOfTheDay()
    when (kind) {
        NowPlan.Kind.CALL -> HeroCard(accent = Night.rose) {
            Text("Call time 💗", style = MaterialTheme.typography.headlineSmall, color = Night.rose)
            Text("It's $her's morning. Everything else can wait until 8.", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            state.tonight?.let { t ->
                Spacer(Modifier.height(12.dp))
                Text("Your line for tonight", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.pressClick { vm.speaker.say(t.hanzi) }) {
                    Column(Modifier.weight(1f)) { HanziText(t.hanzi, 24f, align = null); PinyinText(t.pinyin, style = MaterialTheme.typography.bodyMedium) }
                    Text("🔊", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        NowPlan.Kind.EARLY -> HeroCard(accent = Night.pearl) {
            Text("Early start ☀", style = MaterialTheme.typography.headlineSmall, color = Night.pearl)
            Text("A quiet minute to pray, then one A+ card while the coffee's on.", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Pray", onPray, color = Night.pearl)
            TextButton(onClick = onAPlus) { Text("One A+ card ›", color = Night.violet) }
        }
        NowPlan.Kind.WORK -> HeroCard(accent = Night.violet) {
            Text("At work", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Text("Lunch break? Five phrases take about three minutes.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Review 5", onReview)
        }
        NowPlan.Kind.HEADING_HOME -> HeroCard(accent = Night.jade) {
            Text("Heading home 🚗", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Text("Phrases out loud, hands-free, for the ride.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Drive mode", onDrive, color = Night.jade)
        }
        NowPlan.Kind.EVENING, NowPlan.Kind.AFTER_OPEN -> TonightJob(state, vm, startJob)
        NowPlan.Kind.AFTER_DONE -> HeroCard(accent = Night.jade) {
            val tomorrow = Routine.jobOn(state, java.time.LocalDate.now().plusDays(1))
            Text("Nice evening ✓", style = MaterialTheme.typography.headlineSmall, color = Night.jade)
            Text(
                "Tomorrow: ${Routine.text(tomorrow.task, state.herName)} at ${com.dudlatitat.nextchapter.ui.life.clockLabel(tomorrow.minutes)}.",
                style = MaterialTheme.typography.bodyMedium, color = Night.ink,
            )
            phrase?.let { Spacer(Modifier.height(10.dp)); PhraseOfDay(it, vm, "For the morning") }
        }
        NowPlan.Kind.WIND_DOWN -> HeroCard(accent = Night.violet) {
            Text("Wind down 🌙", style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Text("Up at ${com.dudlatitat.nextchapter.ui.life.clockLabel(state.wakeMinutes)}. You did enough today.", style = MaterialTheme.typography.bodyMedium, color = Night.muted)
            phrase?.let { Spacer(Modifier.height(10.dp)); PhraseOfDay(it, vm, "One to fall asleep on") }
        }
        NowPlan.Kind.MORNING -> HeroCard {
            phrase?.let { PhraseOfDay(it, vm, "Phrase of the day · say it in the car") }
            Spacer(Modifier.height(10.dp))
            SecondaryButton("Drive mode", onDrive, color = Night.jade)
        }
        NowPlan.Kind.DAY -> HeroCard {
            phrase?.let { PhraseOfDay(it, vm, "Phrase of the day") }
        }
    }
}

/** A phrase you can hear: characters, pinyin, meaning, and a tap to play. */
@Composable
fun PhraseOfDay(card: com.dudlatitat.nextchapter.data.Card, vm: MainViewModel, label: String) {
    Row(Modifier.fillMaxWidth().pressClick { vm.speaker.say(card.hanzi) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted)
            HanziText(card.hanzi, 24f, align = null)
            PinyinText(card.pinyin, style = MaterialTheme.typography.bodyMedium)
            Text(card.english, style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        Text("🔊", style = MaterialTheme.typography.titleLarge)
    }
}

/** The six pillars of the three years, one forward-looking number each. Tap to go to its home. */
@Composable
fun PillarStrip(state: AppState, onPillar: (Pillars.Id) -> Unit) {
    val day = today()
    val pillars = remember(state, day) { Pillars.all(state, day) }
    val colors = mapOf(
        Pillars.Id.TALK to Night.sun, Pillars.Id.CAREER to Night.violet, Pillars.Id.MONEY to Night.jade,
        Pillars.Id.HEALTH to Night.mint, Pillars.Id.FAITH to Night.pearl, Pillars.Id.US to Night.rose,
    )
    GlassCard {
        Text("Your three years", style = MaterialTheme.typography.labelLarge, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        pillars.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { p ->
                    val c = colors.getValue(p.id)
                    Row(
                        Modifier.weight(1f).pressClick { onPillar(p.id) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconBadge(p.glyph, c, 34.dp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(p.value, style = MaterialTheme.typography.labelLarge.merge(MonoStyle), color = Night.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(p.name, style = MaterialTheme.typography.labelSmall, color = Night.muted, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Your day as one line (work, home, the call, wind-down) with her sleep and work underneath in
 * your time, and a "now" marker. So you can see when she's free without doing time math.
 */
@Composable
fun DayTimeline(state: AppState, nowMinute: Int, onOpen: () -> Unit = {}) {
    val day = today()
    val zone = java.time.ZoneId.systemDefault()
    val mine = remember(state, day) { DayLine.mine(state, day) }
    val hers = remember(day, zone) { DayLine.hers(day, zone) }
    val from = 5 * 60
    val to = 24 * 60
    fun color(k: DayLine.Kind): Color = when (k) {
        DayLine.Kind.WORK -> Night.violet
        DayLine.Kind.HOME -> Night.mint.copy(alpha = 0.55f)
        DayLine.Kind.CALL -> Night.rose
        DayLine.Kind.WIND_DOWN -> Night.muted.copy(alpha = 0.5f)
        DayLine.Kind.HER_SLEEP -> Night.pearl.copy(alpha = 0.25f)
        DayLine.Kind.HER_WORK -> Night.rose.copy(alpha = 0.3f)
    }
    GlassCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Today, both days", style = MaterialTheme.typography.labelLarge, color = Night.muted, modifier = Modifier.weight(1f))
            Text(NowPlan.callLine(nowMinute), style = MonoStyle, color = Night.rose)
        }
        Spacer(Modifier.height(10.dp))
        Text("You", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        Canvas(Modifier.fillMaxWidth().height(14.dp)) {
            val w = size.width
            fun x(m: Int) = ((m - from).coerceIn(0, to - from)) / (to - from).toFloat() * w
            drawRoundRect(Night.outline.copy(alpha = 0.5f), size = size, cornerRadius = CornerRadius(7.dp.toPx()))
            mine.forEach { b -> if (b.to > from) drawRoundRect(color(b.kind), topLeft = Offset(x(b.from), 0f), size = Size(x(b.to) - x(b.from), size.height), cornerRadius = CornerRadius(7.dp.toPx())) }
            val n = x(nowMinute)
            drawLine(Night.ink, Offset(n, -4f), Offset(n, size.height + 4f), strokeWidth = 2.dp.toPx())
        }
        Spacer(Modifier.height(6.dp))
        Text(state.herName.trim().ifEmpty { "Her" }, style = MaterialTheme.typography.labelSmall, color = Night.muted)
        Canvas(Modifier.fillMaxWidth().height(10.dp)) {
            val w = size.width
            fun x(m: Int) = ((m - from).coerceIn(0, to - from)) / (to - from).toFloat() * w
            drawRoundRect(Night.outline.copy(alpha = 0.3f), size = size, cornerRadius = CornerRadius(5.dp.toPx()))
            hers.forEach { b -> if (b.to > from) drawRoundRect(color(b.kind), topLeft = Offset(x(b.from), 0f), size = Size(x(b.to) - x(b.from), size.height), cornerRadius = CornerRadius(5.dp.toPx())) }
            val n = x(nowMinute)
            drawLine(Night.ink.copy(alpha = 0.6f), Offset(n, -3f), Offset(n, size.height + 3f), strokeWidth = 1.5.dp.toPx())
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("5a", "11a", "6p", "12a").forEach { Text(it, style = MonoStyle, color = Night.muted) }
        }
        Text("Rose = her workday, pale = she's asleep (in your time).", style = MaterialTheme.typography.labelSmall, color = Night.muted)
    }
}
