package com.dudlatitat.nextchapter.ui.life

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.APlus
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.ExamBank
import com.dudlatitat.nextchapter.data.PracticeExam
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.HeroCard
import com.dudlatitat.nextchapter.ui.components.IconBadge
import com.dudlatitat.nextchapter.ui.components.PrimaryButton
import com.dudlatitat.nextchapter.ui.components.SecondaryButton
import com.dudlatitat.nextchapter.ui.components.SubHeader
import com.dudlatitat.nextchapter.ui.components.pressClick
import com.dudlatitat.nextchapter.ui.components.readable
import com.dudlatitat.nextchapter.ui.components.screenGutter
import com.dudlatitat.nextchapter.ui.theme.MonoStyle
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * 模拟考 · A+ practice exams. A start page with two modes, then the test:
 * - Quick 20: like the real exam. Answer, flag, jump around; answers only at the end.
 * - Weak-spot drill: 10 questions, each checked right away with why.
 * Then results: score against the pass line, each domain, and every miss explained.
 */
@Composable
fun PracticeExamScreen(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    val core = APlus.core(state)
    var mode by remember { mutableStateOf<PracticeExam.Mode?>(null) }
    var run by remember { mutableIntStateOf(0) }
    val m = mode
    if (m == null) ExamStart(state, core, onBack) { mode = it; run++ }
    else androidx.compose.runtime.key(run) {
        ExamRun(state, vm, core, m, onExit = { mode = null }, onDrill = { mode = PracticeExam.Mode.DRILL; run++ })
    }
}

// ---------------- Start ----------------

@Composable
private fun ExamStart(state: AppState, core: Int, onBack: () -> Unit, onPick: (PracticeExam.Mode) -> Unit) {
    val missed = remember(state) { PracticeExam.stillMissed(state, core).size }
    val focus = remember(state) { PracticeExam.focus(state, core) }
    val domains = APlus.domainsOf(core)
    val history = remember(state) { PracticeExam.domainHistory(state, core) }
    val past = state.tests.filter { it.core == core && it.mode.isNotEmpty() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = screenGutter())) {
        SubHeader("Practice exam", onBack, zh = "模拟考 · A+ Core $core")
        Spacer(Modifier.height(8.dp))
        HeroCard(accent = Night.violet) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge("试", Night.violet, 48.dp, filled = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quick 20", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                    Text("20 questions · 25 min · like the real exam", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Scenarios, \"choose two\" and put-in-order questions, weighted like 220-120$core. Flag any to come back to. Answers and why at the end.",
                style = MaterialTheme.typography.bodyMedium, color = Night.ink.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Start the test", { onPick(PracticeExam.Mode.QUICK) }, Modifier.fillMaxWidth(), color = Night.violet)
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(onClick = { onPick(PracticeExam.Mode.DRILL) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge("练", Night.sun, 44.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Weak-spot drill", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text(
                        (if (missed > 0) "$missed you missed, then " else "") + domains.getValue(focus).first + " · 10 questions, answers right away",
                        style = MaterialTheme.typography.bodySmall, color = Night.muted,
                    )
                }
                Text("›", style = MaterialTheme.typography.titleLarge, color = Night.muted)
            }
        }
        if (history.values.any { it != null }) {
            Spacer(Modifier.height(24.dp))
            Text("YOUR DOMAINS", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            GlassCard {
                domains.forEach { (d, v) ->
                    val acc = history[d]
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(v.first, style = MaterialTheme.typography.bodyMedium, color = if (d == focus) Night.sun else Night.ink, modifier = Modifier.weight(1f), maxLines = 1)
                        Box(Modifier.width(90.dp)) { Bar(acc ?: 0f, if ((acc ?: 0f) >= 0.75f) Night.jade else Night.sun) }
                        Text(acc?.let { "${(it * 100).toInt()}%" } ?: "–", style = MonoStyle, color = Night.muted, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                    }
                }
            }
        }
        if (past.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("RECENT", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            com.dudlatitat.nextchapter.ui.components.ListGroup {
                past.takeLast(4).asReversed().forEachIndexed { i, t ->
                    val pct = t.score * 100 / maxOf(1, t.total)
                    com.dudlatitat.nextchapter.ui.components.ListRow(
                        (if (t.mode == "drill") "Drill" else "Quick 20") + " · ${t.score}/${t.total}",
                        subtitle = com.dudlatitat.nextchapter.data.Road.weekdayDate(t.day, com.dudlatitat.nextchapter.data.today()) + " · ${t.seconds / 60} min",
                        trailing = { Text("$pct%", style = MonoStyle, color = if (pct >= 75) Night.jade else Night.sun) },
                        divider = i < minOf(4, past.size) - 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ---------------- The test ----------------

@Composable
private fun ExamRun(state: AppState, vm: MainViewModel, core: Int, mode: PracticeExam.Mode, onExit: () -> Unit, onDrill: () -> Unit) {
    val items = remember { if (mode == PracticeExam.Mode.QUICK) PracticeExam.quick(core) else PracticeExam.drill(state, core) }
    val answers = remember { mutableStateMapOf<String, List<Int>>() }
    val flagged = remember { mutableStateMapOf<String, Boolean>() }
    val checked = remember { mutableStateMapOf<String, Boolean>() } // drill: answer shown
    var index by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<PracticeExam.Result?>(null) }
    val startedAt = remember { System.currentTimeMillis() }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(result) { while (result == null) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val elapsed = ((now - startedAt) / 1000).toInt()
    var confirmLeave by remember { mutableStateOf(false) }
    var confirmFinish by remember { mutableStateOf(false) }

    fun finish() {
        val r = PracticeExam.score(items, answers)
        result = r
        vm.saveExam(r, items.map { it.q.id }, mode.key, core, elapsed)
    }

    val r = result
    if (r != null) { ExamResults(items, answers, r, mode, elapsed, onDone = onExit, onDrill = onDrill); return }

    val leave = { if (answers.isEmpty()) onExit() else confirmLeave = true }
    BackHandler(onBack = leave)
    if (confirmLeave) AlertDialog(
        onDismissRequest = { confirmLeave = false }, containerColor = Night.ground1,
        title = { Text("Leave the test?") },
        text = { Text("Your answers so far won't be saved.") },
        confirmButton = { TextButton(onClick = { confirmLeave = false }) { Text("Keep going", color = Night.sun) } },
        dismissButton = { TextButton(onClick = { confirmLeave = false; onExit() }) { Text("Leave", color = Night.muted) } },
    )
    val unanswered = items.count { answers[it.q.id].isNullOrEmpty() }
    val flaggedCount = flagged.count { it.value }
    if (confirmFinish) AlertDialog(
        onDismissRequest = { confirmFinish = false }, containerColor = Night.ground1,
        title = { Text("Finish the test?") },
        text = {
            Text(
                listOfNotNull(
                    if (unanswered > 0) "$unanswered unanswered (they count as wrong)" else null,
                    if (flaggedCount > 0) "$flaggedCount flagged to check" else null,
                ).joinToString(" · ").ifEmpty { "Every question answered." },
            )
        },
        confirmButton = { TextButton(onClick = { confirmFinish = false; finish() }) { Text("See my score", color = Night.violet) } },
        dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text("Review", color = Night.muted) } },
    )

    val item = items[index]
    val q = item.q
    val chosen = answers[q.id].orEmpty()
    val isDrill = mode == PracticeExam.Mode.DRILL
    val shown = isDrill && checked[q.id] == true

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = screenGutter())) {
        SubHeader(if (isDrill) "Weak-spot drill" else "Quick 20", leave, zh = "Question ${index + 1} of ${items.size}", handleBack = false) {
            if (!isDrill) {
                val left = mode.minutes * 60 - elapsed
                Text(
                    if (left >= 0) "%d:%02d".format(left / 60, left % 60) else "+%d:%02d".format(-left / 60, -left % 60),
                    style = MonoStyle.copy(fontSize = 16.sp), color = if (left < 180) Night.sun else Night.ink.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
        // Question map: tap a number to jump (Quick); progress dots (Drill).
        val mapScroll = rememberScrollState()
        val density = androidx.compose.ui.platform.LocalDensity.current
        // Keep the current number in view (each dot is 30dp + 6dp gap).
        LaunchedEffect(index) { mapScroll.animateScrollTo(with(density) { (maxOf(0, index - 3) * 36).dp.roundToPx() }) }
        Row(Modifier.fillMaxWidth().horizontalScroll(mapScroll).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEachIndexed { i, it ->
                val id = it.q.id
                val answered = !answers[id].isNullOrEmpty()
                val fill = when {
                    isDrill && checked[id] == true -> if (PracticeExam.isRight(it.q, answers[id].orEmpty())) Night.jade else Night.rose
                    answered -> Night.violet.copy(alpha = 0.55f)
                    else -> Color.Transparent
                }
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(fill)
                        .border(if (i == index) 2.dp else 1.dp, if (flagged[id] == true) Night.sun else if (i == index) Night.ink else Night.outline, CircleShape)
                        .then(if (!isDrill) Modifier.pressClick(haptic = false) { index = i } else Modifier),
                    contentAlignment = Alignment.Center,
                ) { Text("${i + 1}", style = MonoStyle.copy(fontSize = 11.sp), color = Night.ink) }
            }
        }
        Spacer(Modifier.height(8.dp))
        GlassCard(highlight = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(APlus.domainsOf(core)[q.domain]?.first.orEmpty(), style = MaterialTheme.typography.labelMedium, color = Night.violet, modifier = Modifier.weight(1f))
                when (q.kind) {
                    ExamBank.Kind.MULTI -> KindChip("Choose ${if (q.right.size == 2) "two" else "three"}")
                    ExamBank.Kind.ORDER -> KindChip("Tap in order")
                    else -> {}
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(q.stem, style = MaterialTheme.typography.titleMedium.copy(lineHeight = 24.sp), color = Night.ink)
        }
        Spacer(Modifier.height(12.dp))
        item.order.forEachIndexed { pos, opt ->
            val sel = opt in chosen
            val orderNo = if (q.kind == ExamBank.Kind.ORDER && sel) chosen.indexOf(opt) + 1 else null
            val right = opt in q.right.indices && q.kind != ExamBank.Kind.ORDER
            val tone = when {
                shown && q.kind != ExamBank.Kind.ORDER && right -> Night.jade
                shown && q.kind != ExamBank.Kind.ORDER && sel && !right -> Night.rose
                sel -> Night.violet
                else -> Night.outline
            }
            OptionRow(
                letter = orderNo?.toString() ?: ('A' + pos).toString(),
                text = q.options[opt], tone = tone, selected = sel || (shown && right),
                onClick = if (shown) null else ({
                    answers[q.id] = when (q.kind) {
                        ExamBank.Kind.SINGLE -> listOf(opt)
                        ExamBank.Kind.MULTI -> if (sel) chosen - opt else (chosen + opt).takeLast(q.right.size)
                        ExamBank.Kind.ORDER -> if (sel) chosen - opt else chosen + opt
                    }
                }),
            )
        }
        if (q.kind == ExamBank.Kind.ORDER && chosen.isNotEmpty() && !shown) {
            TextButton(onClick = { answers.remove(q.id) }) { Text("Clear order", color = Night.muted) }
        }
        // Drill: right away, the answer and why.
        AnimatedVisibility(shown) {
            val ok = PracticeExam.isRight(q, chosen)
            Column(Modifier.padding(top = 8.dp)) { Explain(q, ok) }
        }
        Spacer(Modifier.height(16.dp))
        val complete = chosen.size == PracticeExam.picks(q)
        val last = index == items.lastIndex
        if (isDrill) {
            when {
                !shown -> PrimaryButton("Check", { checked[q.id] = true }, Modifier.fillMaxWidth(), color = Night.violet, enabled = complete)
                last -> PrimaryButton("See my score", { finish() }, Modifier.fillMaxWidth(), color = Night.violet)
                else -> PrimaryButton("Next", { index++ }, Modifier.fillMaxWidth(), color = Night.violet)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val f = flagged[q.id] == true
                SecondaryButton(if (f) "⚑ Flagged" else "⚐ Flag", { flagged[q.id] = !f }, Modifier.weight(1f), color = if (f) Night.sun else Night.ink)
                if (last) PrimaryButton("Finish", { confirmFinish = true }, Modifier.weight(1.4f), color = Night.violet)
                else PrimaryButton("Next", { index++ }, Modifier.weight(1.4f), color = Night.violet)
            }
            if (index > 0) TextButton(onClick = { index-- }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("‹ Previous", color = Night.muted) }
            if (!last) TextButton(onClick = { confirmFinish = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Finish early", color = Night.muted) }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun KindChip(text: String) = Text(
    text, style = MaterialTheme.typography.labelSmall, color = Night.sun,
    modifier = Modifier.clip(RoundedCornerShape(50)).background(Night.sun.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp),
)

/** One answer: a letter (or order number) badge and the text. */
@Composable
private fun OptionRow(letter: String, text: String, tone: Color, selected: Boolean, onClick: (() -> Unit)?) {
    val bg by animateColorAsState(if (selected) tone.copy(alpha = 0.16f) else Night.card.copy(alpha = 0.55f), label = "optBg")
    val edge by animateColorAsState(if (selected) tone else Night.outline.copy(alpha = 0.6f), label = "optEdge")
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).heightIn(min = 56.dp).clip(RoundedCornerShape(16.dp)).background(bg)
            .border(1.dp, edge, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.pressClick(onClick = onClick) else Modifier).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(if (selected) tone else Color.Transparent).border(1.dp, if (selected) tone else Night.muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(letter, style = MonoStyle, color = if (selected) Night.ground2 else Night.muted) }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Night.ink, modifier = Modifier.weight(1f))
    }
}

/** Right/wrong, the correct answer(s), and why. */
@Composable
private fun Explain(q: ExamBank.Q, ok: Boolean) {
    GlassCard {
        Text(if (ok) "✓ Right" else "Not quite", style = MaterialTheme.typography.titleSmall, color = if (ok) Night.jade else Night.rose)
        if (!ok) {
            Spacer(Modifier.height(4.dp))
            if (q.kind == ExamBank.Kind.ORDER) q.right.forEachIndexed { i, step -> Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = Night.jade) }
            else q.right.forEach { Text("✓ $it", style = MaterialTheme.typography.bodyMedium, color = Night.jade) }
        }
        Spacer(Modifier.height(6.dp))
        Text(q.why, style = MaterialTheme.typography.bodyMedium, color = Night.ink.copy(alpha = 0.85f))
    }
}

// ---------------- Results ----------------

@Composable
private fun ExamResults(
    items: List<PracticeExam.Item>, answers: Map<String, List<Int>>, r: PracticeExam.Result, mode: PracticeExam.Mode, seconds: Int,
    onDone: () -> Unit, onDrill: () -> Unit,
) {
    BackHandler(onBack = onDone)
    val core = items.first().q.core
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).readable().padding(horizontal = screenGutter())) {
        SubHeader(if (mode == PracticeExam.Mode.DRILL) "Drill done" else "Your score", onDone, zh = "A+ Core $core · ${seconds / 60} min ${seconds % 60} s", handleBack = false)
        Spacer(Modifier.height(8.dp))
        HeroCard(accent = if (r.passed) Night.jade else Night.violet) {
            Row(verticalAlignment = Alignment.Bottom) {
                com.dudlatitat.nextchapter.ui.components.CountUp(r.pct, MaterialTheme.typography.displayMedium, if (r.passed) Night.jade else Night.sun, suffix = "%")
                Spacer(Modifier.width(10.dp))
                Text("${r.right} of ${r.total}", style = MaterialTheme.typography.titleMedium, color = Night.ink, modifier = Modifier.padding(bottom = 10.dp))
            }
            // The pass line on a bar.
            Box(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Bar(r.pct / 100f, if (r.passed) Night.jade else Night.sun)
            }
            Text(
                when {
                    r.passed && mode == PracticeExam.Mode.QUICK -> "Pass level (about 75%). 太棒了!"
                    r.passed -> "Strong. These are sticking."
                    else -> "Pass is about 75%. Every miss below is a point for next time."
                },
                style = MaterialTheme.typography.bodyMedium, color = Night.muted, modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        GlassCard {
            Text("By domain", style = MaterialTheme.typography.labelLarge, color = Night.muted)
            Spacer(Modifier.height(6.dp))
            r.byDomain.forEach { (d, rt) ->
                val acc = rt.first / rt.second.toFloat()
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(APlus.domainsOf(core)[d]?.first.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = Night.ink, modifier = Modifier.weight(1f), maxLines = 1)
                    Box(Modifier.width(80.dp)) { Bar(acc, if (acc >= 0.75f) Night.jade else Night.sun) }
                    Text("${rt.first}/${rt.second}", style = MonoStyle, color = Night.muted, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                }
            }
        }
        if (r.missed.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("TO LEARN FROM (${r.missed.size})", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            items.filter { it.q.id in r.missed }.forEach { it ->
                val q = it.q
                val mine = answers[q.id].orEmpty()
                Spacer(Modifier.height(8.dp))
                GlassCard {
                    Text(q.stem, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
                    Spacer(Modifier.height(6.dp))
                    if (mine.isEmpty()) Text("Not answered", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    else if (q.kind != ExamBank.Kind.ORDER) mine.filter { o -> o !in q.right.indices }.forEach { o -> Text("✗ ${q.options[o]}", style = MaterialTheme.typography.bodyMedium, color = Night.rose) }
                    else Text("Your order: " + mine.joinToString(" → ") { o -> q.options[o] }, style = MaterialTheme.typography.bodySmall, color = Night.rose)
                    if (q.kind == ExamBank.Kind.ORDER) q.right.forEachIndexed { i, step -> Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = Night.jade) }
                    else q.right.forEach { a -> Text("✓ $a", style = MaterialTheme.typography.bodyMedium, color = Night.jade) }
                    Spacer(Modifier.height(6.dp))
                    Text(q.why, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (r.missed.isNotEmpty()) {
            PrimaryButton("Drill what I missed", onDrill, Modifier.fillMaxWidth(), color = Night.violet)
            TextButton(onClick = onDone, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Done", color = Night.muted) }
        } else PrimaryButton("Done", onDone, Modifier.fillMaxWidth(), color = Night.jade)
        Spacer(Modifier.height(32.dp))
    }
}
