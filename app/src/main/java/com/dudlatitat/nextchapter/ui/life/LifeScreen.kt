package com.dudlatitat.nextchapter.ui.life

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import com.dudlatitat.nextchapter.ui.components.readable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.data.APlus
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.Career
import com.dudlatitat.nextchapter.data.Grade
import com.dudlatitat.nextchapter.data.JobPosting
import com.dudlatitat.nextchapter.data.People
import com.dudlatitat.nextchapter.data.Person
import com.dudlatitat.nextchapter.data.PersonDate
import com.dudlatitat.nextchapter.data.daysUntil
import com.dudlatitat.nextchapter.data.prettyDate
import com.dudlatitat.nextchapter.data.parseDate
import com.dudlatitat.nextchapter.data.CertStep
import com.dudlatitat.nextchapter.data.APlus2
import com.dudlatitat.nextchapter.data.Road
import com.dudlatitat.nextchapter.ui.components.pressClick
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import com.dudlatitat.nextchapter.data.friendlySpan
import com.dudlatitat.nextchapter.data.today
import com.dudlatitat.nextchapter.ui.components.Bar
import com.dudlatitat.nextchapter.ui.components.Collapsible
import com.dudlatitat.nextchapter.ui.components.GlassCard
import com.dudlatitat.nextchapter.ui.components.Pill
import com.dudlatitat.nextchapter.ui.components.ProgressRing
import com.dudlatitat.nextchapter.ui.components.ScreenTitle
import com.dudlatitat.nextchapter.ui.components.SectionLabel
import com.dudlatitat.nextchapter.ui.move.PickDate
import com.dudlatitat.nextchapter.ui.theme.Night
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFmt = DateTimeFormatter.ofPattern("MMM d", Locale.US)

/** The Life tab: your week, A+ study and the career path. Sub-pages slide in and out. */
@Composable
fun LifeScreen(state: AppState, vm: MainViewModel, startAPlus: Boolean = false, onStarted: () -> Unit = {}, startPage: String? = null, onPageStarted: () -> Unit = {}, onLeave: () -> Unit = {}) {
    val pageState = rememberSaveable { mutableStateOf<String?>(null) }
    var page by pageState
    var fromOutside by rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(startAPlus) { if (startAPlus) { page = "aplus"; fromOutside = true; onStarted() } }
    androidx.compose.runtime.LaunchedEffect(startPage) { if (startPage != null) { page = startPage.takeIf { it != "timer" }; onPageStarted() } }
    // Opened from a Today ring: closing it goes back to Today.
    var last by remember { mutableStateOf(page) }
    androidx.compose.runtime.LaunchedEffect(page) {
        if (page == null && last != null && fromOutside) { fromOutside = false; onLeave() }
        last = page
    }
    com.dudlatitat.nextchapter.ui.components.SubNav(page, isRoot = { it == null }, label = "life") { p ->
        when {
            p == "aplus" -> APlusReview(state, vm, onBack = { page = null })
            p == "test" -> PracticeExamScreen(state, vm, onBack = { page = null })
            p == "routine" -> RoutineScreen(state, vm, onBack = { page = null })
            p == "career" -> CareerPage(state, vm, onBack = { page = null })
            p?.startsWith("person:") == true -> {
                val person = state.people.firstOrNull { it.id == p!!.removePrefix("person:") }
                if (person != null) PersonScreen(state, person, vm, onBack = { page = null }) else androidx.compose.runtime.LaunchedEffect(Unit) { page = null }
            }
            else -> LifeHome(state, vm, pageState)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LifeHome(state: AppState, vm: MainViewModel, pageState: androidx.compose.runtime.MutableState<String?>) {
    var page by pageState
    var pickingExam by remember { mutableStateOf(false) }
    var passingExam by remember { mutableStateOf(false) }
    var bodyOpen by remember { mutableStateOf(false) }
    var winsOpen by remember { mutableStateOf(false) }
    val wide = com.dudlatitat.nextchapter.ui.components.isWide()
    // Start opens the right place for tonight's job; anything else starts the study timer.
    val startJob: (com.dudlatitat.nextchapter.data.RoutineDay) -> Unit = { job ->
        when (job.kind) {
            "aplus" -> page = "test"
            "aplus-review" -> page = "aplus"
            else -> if (state.timerStart == null) vm.startTimer(if (job.kind == "chinese") "Chinese" else "Other")
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            val facts = remember(state) { com.dudlatitat.nextchapter.data.Status.life(state) }
            com.dudlatitat.nextchapter.ui.components.TabHeader("Life", "生活", facts.map { f -> com.dudlatitat.nextchapter.ui.components.Fact(f, onClick = if (f.startsWith("🌙")) ({ page = "routine" }) else null) })
        }
        // Tonight first (the one job), then the week; A+, the timer, career and wins after.
        // Unfolded: tonight and the week on the left, the rest on the right.
        val left: @Composable () -> Unit = {
            com.dudlatitat.nextchapter.ui.components.HeroCard(accent = Night.violet) {
                com.dudlatitat.nextchapter.ui.today.TonightJob(state, vm, startJob, bare = true, showRun = false)
            }
            RoutineSummary(state, onOpen = { page = "routine" })
        }
        val right: @Composable () -> Unit = {
            SectionLabel("A+ exam")
            APlusCard(state, vm, onStart = { page = "aplus" }, onPickExam = { pickingExam = true }, onTest = { page = "test" }, onPassed = { passingExam = true })
            StudyTimer(state, vm)
            BodyGlance(state) { bodyOpen = true }
            SectionLabel("Your path")
            com.dudlatitat.nextchapter.ui.components.ListGroup {
                val next = com.dudlatitat.nextchapter.data.Career.next(state)?.let { it.copy(status = com.dudlatitat.nextchapter.data.Career.effective(state, it)) }
                com.dudlatitat.nextchapter.ui.components.ListRow(
                    "Career path", subtitle = next?.let { "Next: ${it.name.substringBefore(" (")} · ${it.status}" } ?: "Certs, classes and job postings",
                    leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("业", Night.violet) }, onClick = { page = "career" },
                )
                // Faith: prayer days and the prayer ladder (it lives on the Chinese tab).
                val planNav = com.dudlatitat.nextchapter.ui.components.LocalPlanNav.current
                val prayed = state.prayedDays.count { it in (com.dudlatitat.nextchapter.data.today() - 6)..com.dudlatitat.nextchapter.data.today() }
                com.dudlatitat.nextchapter.ui.components.ListRow(
                    "Faith", subtitle = (if (prayed > 0) "Prayed $prayed of the last 7 days · " else "") + "learn a prayer by heart",
                    leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("祷", Night.pearl) },
                    onClick = { planNav(com.dudlatitat.nextchapter.data.DayPlan.Item(com.dudlatitat.nextchapter.data.DayPlan.Kind.PLAY, "Prayer", "", false, tool = "prayer")) },
                )
                val all = com.dudlatitat.nextchapter.data.Milestones.all(state)
                val nextWin = all.firstOrNull { !it.done }
                com.dudlatitat.nextchapter.ui.components.ListRow(
                    "Wins", subtitle = "${all.count { it.done }} milestones" + (nextWin?.let { " · next: ${it.emoji} ${it.title}" } ?: ""), divider = false,
                    leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("赢", Night.sun) }, onClick = { winsOpen = true },
                )
            }
        }
        if (wide) item {
            com.dudlatitat.nextchapter.ui.components.TwoPanes(left, right)
        } else {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { left() } }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { right() } }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    if (pickingExam) PickDate(APlus.examDate(state), onDismiss = { pickingExam = false }) { vm.setAPlusExam(it); pickingExam = false }
    if (passingExam) PassedDialog(state, vm, if (APlus.core(state) == 2) "aplus2" else "aplus1") { passingExam = false }
    // Body lives here now (Today stays about the day, not the scale).
    if (bodyOpen) androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = { bodyOpen = false },
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Night.ground1,
    ) { com.dudlatitat.nextchapter.ui.today.BodySheet(state, vm) }
    // Wins in a sheet (it was a whole page holding one list).
    if (winsOpen) androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = { winsOpen = false },
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Night.ground1,
    ) {
        Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Wins · 成就", style = MaterialTheme.typography.titleLarge, color = Night.ink)
            Spacer(Modifier.height(10.dp))
            com.dudlatitat.nextchapter.ui.progress.LifeWins(state)
        }
    }
}

/** Career: certs, classes and job postings, on their own page. */
@Composable
private fun CareerPage(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    var addingClass by remember { mutableStateOf(false) }
    var editingJob by remember { mutableStateOf<JobPosting?>(null) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { com.dudlatitat.nextchapter.ui.components.SubHeader("Career path", onBack, zh = "事业 · certs, classes, jobs") }
        item { CertPath(state, vm) }
        item { Classes(state, vm, onAdd = { addingClass = true }) }
        item { Jobs(state, onEdit = { editingJob = it }, onAdd = { editingJob = JobPosting("job:${System.currentTimeMillis()}", "") }) }
    }
    if (addingClass) TwoFieldDialog("Add a class", "Class (e.g. CIS 101)", "Term (e.g. Spring 2027)", onDismiss = { addingClass = false }) { a, b ->
        vm.addClass(a, b); addingClass = false
    }
    editingJob?.let { j -> JobDialog(j, isNew = state.jobs.none { it.id == j.id }, vm, onDismiss = { editingJob = null }) }
}

// ---- A+ ----

/**
 * A+ as a short list: the cards due (ring + Study), the exam date, the practice test, and the
 * domains with cards-a-day folded in the last row.
 */
@Composable
private fun APlusCard(state: AppState, vm: MainViewModel, onStart: () -> Unit, onPickExam: () -> Unit, onTest: () -> Unit, onPassed: () -> Unit) {
    val queue = APlus.queue(state)
    val strength = APlus.strength(state)
    val weakest = APlus.weakest(state)
    val known = APlus.known(state)
    val days = daysUntil(APlus.examDate(state))
    val exam = APlus.examName(state)
    var details by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    com.dudlatitat.nextchapter.ui.components.ListGroup {
        com.dudlatitat.nextchapter.ui.components.ListRow(
            if (known > 0) "$exam · $known known" else exam,
            subtitle = if (queue.isEmpty()) "All done for today ✓" else "${queue.size} cards · about ${maxOf(1, queue.size / 4)} min" + (weakest?.let { " · focus ${APlus.domains(state).getValue(it).first}" } ?: ""),
            leading = {
                ProgressRing(known / APlus.deck(state).size.toFloat(), 44.dp, Night.violet, stroke = 4.dp) {
                    Text("考", style = MaterialTheme.typography.titleMedium, color = Night.violet)
                }
            },
            trailing = if (queue.isEmpty()) ({ Text("✓", style = MaterialTheme.typography.titleLarge, color = Night.jade) }) else ({ com.dudlatitat.nextchapter.ui.components.GoChip("Study", Night.violet, onStart) }),
            onClick = if (queue.isEmpty()) null else onStart,
        )
        val plan = remember(state) { com.dudlatitat.nextchapter.data.ExamPlan.of(state) }
        com.dudlatitat.nextchapter.ui.components.ListRow(
            when {
                days == null -> "Set your exam date"
                days < 0 -> "How did the exam go?"
                days == 0L -> "Exam day. You've got this 💪"
                else -> "Exam in ${friendlySpan(days)}"
            },
            subtitle = plan?.line ?: if (days == null) "A date turns this into a plan" else null,
            titleColor = if (days != null && days in 0..14) Night.coral else Night.ink,
            leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("日", Night.sun) }, onClick = onPickExam,
            // On exam day or after: the one place to say you passed (it also lives on Career path).
            trailing = if (days != null && days <= 0) ({ com.dudlatitat.nextchapter.ui.components.GoChip("I passed 🎉", Night.jade, onPassed) }) else null,
        )
        val last = state.tests.lastOrNull()
        com.dudlatitat.nextchapter.ui.components.ListRow(
            "Practice exam · Quick 20 or drill",
            subtitle = last?.let { t -> val pct = t.score * 100 / maxOf(1, t.total); "Last: ${t.score}/${t.total} ($pct%)" + if (pct >= 75) " · pass level ✓" else " · pass is ~75%" },
            leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("试", Night.violet) }, onClick = onTest,
        )
        com.dudlatitat.nextchapter.ui.components.ListRow(
            "Domains & cards a day", subtitle = "${state.aplusPerDay} new cards a day", divider = false,
            leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("域", Night.jade) },
            trailing = { Text(if (details) "▴" else "▾", style = MaterialTheme.typography.titleMedium, color = Night.muted) },
            onClick = { details = !details },
        )
        androidx.compose.animation.AnimatedVisibility(details) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                // The five domains in one row: short name, a mini bar, how much you know.
                val short = if (APlus.core(state) == 2) mapOf(1 to "Systems", 2 to "Security", 3 to "Fixes", 4 to "Procedures")
                    else mapOf(1 to "Mobile", 2 to "Network", 3 to "Hardware", 4 to "Cloud", 5 to "Fixes")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    APlus.domains(state).forEach { (d, _) ->
                        val s0 = strength[d] ?: 0f
                        val c = if (d == weakest) Night.sun else Night.jade
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(short.getValue(d), fontSize = 11.sp, color = if (d == weakest) Night.sun else Night.muted, maxLines = 1, softWrap = false)
                            Bar(s0, c, Modifier.padding(vertical = 3.dp))
                            Text("${(s0 * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Night.muted, maxLines = 1)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New cards a day: ${state.aplusPerDay}", style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.setAPlusPerDay(state.aplusPerDay - 2) }) { Text("−", color = Night.muted) }
                    TextButton(onClick = { vm.setAPlusPerDay(state.aplusPerDay + 2) }) { Text("+", color = Night.muted) }
                }
            }
        }
    }
}

/** One A+ session: question, tap to reveal, grade. Misses come back at the end. */
@Composable
private fun APlusReview(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    val payBefore = com.dudlatitat.nextchapter.ui.components.rememberBefore(vm)
    BackHandler(onBack = onBack)
    val queue = remember { mutableStateListOf<String>().apply { addAll(APlus.queue(state).map { it.id }) } }
    val total = remember { queue.size }
    var done by remember { mutableIntStateOf(0) }
    var shown by remember { mutableStateOf(false) }
    val card = queue.firstOrNull()?.let { id -> APlus.find(id) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("A+ · Core 1", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text("$done of $total", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            }
        }
        Bar(if (total == 0) 1f else done / total.toFloat(), Night.jade, Modifier.padding(vertical = 8.dp))
        if (card == null) {
            Spacer(Modifier.height(40.dp))
            Text("Done for today ✓", style = MaterialTheme.typography.headlineMedium, color = Night.jade, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text("One step closer to passing. 加油!", style = MaterialTheme.typography.bodyLarge, color = Night.muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            com.dudlatitat.nextchapter.ui.components.SessionPayoff(vm, payBefore)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Life", color = Night.sun) }
            return@Column
        }
        Spacer(Modifier.height(10.dp))
        GlassCard(highlight = true, onClick = { shown = true }, modifier = Modifier.animateContentSize()) {
            Text(APlus.domainName(card), style = MaterialTheme.typography.labelMedium, color = Night.violet)
            Spacer(Modifier.height(10.dp))
            Text(card.q, style = MaterialTheme.typography.headlineSmall, color = Night.ink)
            Spacer(Modifier.height(16.dp))
            if (shown) Text(card.a, style = MaterialTheme.typography.titleMedium, color = Night.sun)
            else Text("Answer in your head, then tap to check", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        }
        Spacer(Modifier.height(14.dp))
        AnimatedVisibility(shown, enter = fadeIn()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Grade.entries.forEach { g ->
                    val color = when (g) { Grade.AGAIN -> Night.coral; Grade.HARD -> Night.violet; Grade.GOOD -> Night.jade; Grade.EASY -> Night.sun }
                    val next = APlus.grade(APlus.review(state, card.id), g)
                    Button(
                        onClick = {
                            vm.gradeAPlus(card.id, g)
                            queue.removeAt(0)
                            if (g == Grade.AGAIN) queue.add(card.id) else done++
                            shown = false
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        contentPadding = PaddingValues(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f), contentColor = color),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(g.label, style = MaterialTheme.typography.labelLarge)
                            Text(if (next.due <= today()) "now" else "${next.interval}d", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

// ---- Body ----

/** 身体 · Body at a glance: your weight (and goal), this week's change, today's calories as a ring. Tap to log. */
@Composable
private fun BodyGlance(state: AppState, onOpen: () -> Unit) {
    val day = com.dudlatitat.nextchapter.data.today()
    val B = com.dudlatitat.nextchapter.data.Body
    val w = B.latest(state.weights)
    val eaten = B.caloriesOn(state.calorieAdds, day)
    val target = state.calorieTarget
    GlassCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.dudlatitat.nextchapter.ui.components.IconBadge("身", Night.mint, 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                if (w == null) {
                    Text("Body", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                    Text("Weight and meals, a tap or two a day", style = MaterialTheme.typography.bodySmall, color = Night.muted)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${B.lbs(w.lb)} lb", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle.copy(fontSize = 20.sp), color = Night.ink)
                        state.goalWeight?.let { Text("  → ${B.lbs(it)}", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.violet, modifier = Modifier.padding(bottom = 2.dp)) }
                    }
                    val wk = B.weekChange(state.weights)
                    val trend = wk?.let { " · " + (if (it <= 0) "−" else "+") + B.lbs(kotlin.math.abs(it)) + "/wk" } ?: ""
                    Text(
                        (if (w.day == day) "Today ✓" else "Tap to log today") + trend,
                        style = MaterialTheme.typography.bodySmall, color = if (w.day == day) Night.mint else Night.muted, maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Today's food as a ring (no ring without a target: just the number).
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (target > 0) ProgressRing((eaten / target.toFloat()).coerceIn(0f, 1f), 52.dp, if (eaten > target) Night.sun else Night.mint, stroke = 4.dp) {
                    Text(B.cal(eaten), style = com.dudlatitat.nextchapter.ui.theme.MonoStyle.copy(fontSize = 11.sp), color = Night.ink, maxLines = 1)
                } else Text(B.cal(eaten), style = com.dudlatitat.nextchapter.ui.theme.MonoStyle, color = Night.ink)
                Text(if (target > 0) "of ${B.cal(target)}" else "cal today", style = MaterialTheme.typography.labelSmall, color = Night.muted, maxLines = 1)
            }
        }
    }
}

// ---- Career ----

private fun certColor(status: String) = when (status) { "studying" -> Night.violet; "booked" -> Night.sun; "passed" -> Night.jade; else -> Night.muted }

/**
 * Certifications: each tile shows where it really is (an exam date means booked; reviewing A+
 * cards means studying; passed only when you say so). Tap a tile for its date and "I passed".
 */
@Composable
private fun CertPath(state: AppState, vm: MainViewModel) {
    val certs = Career.certs(state)
    var open by remember { mutableStateOf<String?>(null) }
    val today = com.dudlatitat.nextchapter.data.today()
    GlassCard {
        val passed = certs.count { Career.effective(state, it) == "passed" }
        Text(if (passed == 0) "Certifications" else "Certifications · $passed/${certs.size} passed", style = MaterialTheme.typography.titleMedium, color = Night.ink)
        Text("They follow what you do. Tap one for its exam date.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            certs.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { c ->
                        val st = Career.effective(state, c)
                        val color = certColor(st)
                        val date = Career.dateOf(state, c)?.let { parseDate(it) }
                        // The A+ core you're on shows how ready you are.
                        val active = c.id == (if (APlus.core(state) == 2) "aplus2" else "aplus1") && st != "passed"
                        Column(
                            Modifier.weight(1f).heightIn(min = 76.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                .background(color.copy(alpha = 0.12f)).pressClick { open = c.id }.padding(10.dp),
                        ) {
                            Text(c.name.substringBefore(" ("), style = MaterialTheme.typography.labelLarge, color = Night.ink, maxLines = 1)
                            Text(
                                when {
                                    st == "passed" -> "✓ passed"
                                    st == "booked" && date != null -> "booked · " + Road.short(date.toEpochDay())
                                    else -> st
                                },
                                style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1,
                            )
                            if (active && APlus.started(state)) Text("${APlus.readyPct(state)}% ready", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle.copy(fontSize = 11.sp), color = Night.muted)
                            else if (st == "booked" && date != null && date.toEpochDay() >= today) Text("in ${date.toEpochDay() - today}d", style = com.dudlatitat.nextchapter.ui.theme.MonoStyle.copy(fontSize = 11.sp), color = Night.muted)
                        }
                    }
                }
            }
        }
    }
    open?.let { id -> certs.firstOrNull { it.id == id }?.let { c -> CertSheet(state, vm, c) { open = null } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertSheet(state: AppState, vm: MainViewModel, c: CertStep, onClose: () -> Unit) {
    var picking by remember { mutableStateOf(false) }
    var passing by remember { mutableStateOf(false) }
    val st = Career.effective(state, c)
    val name = c.name.substringBefore(" (")
    val days = daysUntil(Career.dateOf(state, c))
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Night.ground1,
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(name, style = MaterialTheme.typography.titleLarge, color = Night.ink)
            Text(c.name.substringAfter(" (", "").removeSuffix(")").ifEmpty { "CompTIA" } + " · " + st, style = MaterialTheme.typography.bodySmall, color = certColor(st))
            Spacer(Modifier.height(12.dp))
            com.dudlatitat.nextchapter.ui.components.ListGroup {
                com.dudlatitat.nextchapter.ui.components.ListRow(
                    when { days == null -> "Set the exam date"; days < 0 -> "Exam was ${prettyDate(Career.dateOf(state, c))}"; days == 0L -> "Exam today 💪"; else -> "Exam in ${friendlySpan(days)}" },
                    subtitle = if (days == null) "Booking it marks this \"booked\"" else prettyDate(Career.dateOf(state, c)),
                    leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("日", Night.sun) }, onClick = { picking = true },
                    divider = c.id.startsWith("aplus") || st != "passed",
                )
                if (c.id.startsWith("aplus")) {
                    val core = if (c.id == "aplus2") 2 else 1
                    val ids = APlus.cardsOf(core).map { it.id }.toSet()
                    val known = state.aplus.count { it.id in ids && it.interval >= 7 }
                    com.dudlatitat.nextchapter.ui.components.ListRow(
                        "Study cards · $known/${ids.size} known",
                        subtitle = when {
                            st == "passed" && core == 1 -> "A few come back each day to keep it fresh"
                            APlus.core(state) == core -> "Your A+ study on Life"
                            else -> "Starts when Core 1 is passed"
                        },
                        leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("考", Night.violet) }, divider = false,
                    )
                } else if (st != "passed") {
                    com.dudlatitat.nextchapter.ui.components.ListRow(
                        if (c.status == "studying") "Studying this now ✓" else "Studying this now?",
                        subtitle = if (c.status == "studying") "Tap to set back to planned" else "Tap when you start",
                        leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("读", Night.violet) }, divider = false,
                        onClick = { vm.setCertStudying(c.id, c.status != "studying") },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (st == "passed") {
                TextButton(onClick = { vm.setCertPassed(c.id, false) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Not passed after all? Undo", color = Night.muted) }
            } else {
                com.dudlatitat.nextchapter.ui.components.PrimaryButton("I passed 🎉", { passing = true }, Modifier.fillMaxWidth(), color = Night.jade)
            }
        }
    }
    if (picking) PickDate(Career.dateOf(state, c), onDismiss = { picking = false }) { vm.setCertDate(c.id, it); picking = false }
    if (passing) PassedDialog(state, vm, c.id) { passing = false }
}

/** "Did you pass?" → yes → a celebration that says what's next. */
@Composable
internal fun PassedDialog(state: AppState, vm: MainViewModel, id: String, onClose: () -> Unit) {
    val c = Career.certs(state).firstOrNull { it.id == id } ?: return
    val name = c.name.substringBefore(" (")
    var done by remember { mutableStateOf(false) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onClose,
        containerColor = Night.ground1,
        title = { Text(if (done) "🎉 $name passed!" else "Passed $name?", style = MaterialTheme.typography.titleLarge, color = if (done) Night.jade else Night.ink) },
        text = {
            Text(
                if (!done) "Nice. This marks it passed on Career path and ticks it on Move."
                else when (id) {
                    "aplus1" -> "了不起！ Core 2 starts now: ${APlus2.cards.size} new cards, weighted like the 220-1202 exam. Core 1 comes back a few cards a day so it stays fresh."
                    "aplus2" -> "You're A+ certified. 了不起！ That's a real step toward IT work in 成都."
                    else -> "了不起！ One more on the path."
                },
                style = MaterialTheme.typography.bodyMedium, color = Night.muted,
            )
        },
        confirmButton = {
            if (done) TextButton(onClick = onClose) { Text("Keep going", color = Night.jade) }
            else TextButton(onClick = { vm.setCertPassed(id, true); done = true }) { Text("Yes, I passed", color = Night.jade) }
        },
        dismissButton = { if (!done) TextButton(onClick = onClose) { Text("Not yet", color = Night.muted) } },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Classes(state: AppState, vm: MainViewModel, onAdd: () -> Unit) {
    var deleting by remember { mutableStateOf<String?>(null) }
    val left = state.classes.count { !it.done }
    Collapsible(
        "life.classes", "AS in CIS · classes",
        summary = if (state.classes.isEmpty()) "Add the classes you have left" else if (left == 0) "All done. Graduation! 🎓" else "$left left",
    ) {
        state.classes.forEach { c ->
            Row(
                Modifier.fillMaxWidth().combinedClickable(onClick = { vm.toggleClass(c.id) }, onLongClick = { deleting = c.id }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(c.done, { vm.toggleClass(c.id) }, colors = CheckboxDefaults.colors(checkedColor = Night.jade))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.bodyLarge, color = if (c.done) Night.muted else Night.ink)
                    if (c.term.isNotBlank()) Text(c.term, style = MaterialTheme.typography.labelSmall, color = Night.muted)
                }
            }
        }
        TextButton(onClick = onAdd) { Text("+ Add class", color = Night.sun) }
    }
    deleting?.let { id ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove this class?") },
            confirmButton = { TextButton(onClick = { vm.deleteClass(id); deleting = null }) { Text("Remove", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Jobs(state: AppState, onEdit: (JobPosting) -> Unit, onAdd: () -> Unit) {
    val today = LocalDate.now()
    val open = state.jobs.filter { it.status != "closed" }.sortedWith(compareBy({ it.deadline == null }, { it.deadline }))
    val soonest = open.firstOrNull { it.status == "saved" }?.let { j -> Career.daysLeft(j, today)?.takeIf { it >= 0 }?.let { "${j.title} closes in ${it}d" } }
    Collapsible(
        "life.jobs", "Job postings · ${open.size}",
        summary = soonest ?: if (open.isEmpty()) "Save postings you're watching" else "${open.count { it.status == "applied" }} applied",
    ) {
        Text("Public-sector IT and more. Reminders 3 days and 1 day before a deadline.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        open.forEach { j ->
            val left = Career.daysLeft(j, today)
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onEdit(j) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(j.title, style = MaterialTheme.typography.bodyLarge, color = Night.ink, modifier = Modifier.weight(1f))
                    Pill(j.status, when (j.status) { "applied" -> Night.violet; "interview" -> Night.sun; "offer" -> Night.jade; else -> Night.muted })
                }
                val sub = listOfNotNull(
                    j.org.takeIf { it.isNotBlank() },
                    left?.let { if (it < 0) "closed ${-it}d ago" else if (it == 0L) "closes today" else "closes in ${it}d" },
                ).joinToString(" · ")
                if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.labelSmall, color = if (left != null && left in 0..3 && j.status == "saved") Night.coral else Night.muted)
            }
        }
        val closed = state.jobs.count { it.status == "closed" }
        if (closed > 0) Text("$closed closed", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        TextButton(onClick = onAdd) { Text("+ Add posting", color = Night.sun) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobDialog(job: JobPosting, isNew: Boolean, vm: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var j by remember { mutableStateOf(job) }
    var picking by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add a posting" else "Posting") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(j.title, { j = j.copy(title = it) }, label = { Text("Job title") }, singleLine = true)
                OutlinedTextField(j.org, { j = j.copy(org = it) }, label = { Text("Employer (city, county, state…)") }, singleLine = true)
                OutlinedTextField(j.url, { j = j.copy(url = it.trim()) }, label = { Text("Link") }, singleLine = true)
                OutlinedButton(onClick = { picking = true }) {
                    Text(j.deadline?.let { "Deadline: ${LocalDate.parse(it).format(dayFmt)}" } ?: "Set deadline", color = Night.sun)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Career.jobStatuses.forEach { st -> FilterChip(selected = j.status == st, onClick = { j = j.copy(status = st) }, label = { Text(st) }) }
                }
                OutlinedTextField(j.notes, { j = j.copy(notes = it) }, label = { Text("Notes") })
                if (j.url.isNotBlank()) TextButton(onClick = {
                    try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(j.url))) } catch (_: Exception) {}
                }) { Text("Open the posting ↗", color = Night.violet) }
                if (!isNew) TextButton(onClick = { vm.deleteJob(j.id); onDismiss() }) { Text("Delete", color = Night.coral) }
            }
        },
        confirmButton = { TextButton(onClick = { if (j.title.isNotBlank()) vm.saveJob(j.copy(title = j.title.trim())); onDismiss() }) { Text("Save", color = Night.sun) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
    if (picking) PickDate(j.deadline, onDismiss = { picking = false }) { j = j.copy(deadline = it); picking = false }
}

// ---- People ----

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeopleList(state: AppState, vm: MainViewModel, onOpen: (Person) -> Unit, onAdd: () -> Unit) {
    val today = LocalDate.now()
    if (state.people.isEmpty()) {
        GlassCard {
            Text("Gift ideas, what they like, sizes, little moments", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            Text("Jot things down when they come up; they come back when a birthday gets close.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                People.starters.forEach { name -> FilterChip(selected = false, onClick = { vm.addPerson(name) }, label = { Text("+ $name") }) }
                FilterChip(selected = false, onClick = onAdd, label = { Text("+ Someone else") })
            }
        }
        return
    }
    val soon = People.upcoming(state, today, within = 30).firstOrNull()
    Collapsible(
        "life.people", "People · ${state.people.size}",
        summary = soon?.let { "${it.person.name}'s ${it.date.label.lowercase()} in ${it.days}d" } ?: "${state.notes.size} notes",
    ) {
        state.people.forEach { p ->
            val next = p.dates.mapNotNull { d -> runCatching { d to People.next(d, today) }.getOrNull() }.minByOrNull { it.second }
            val count = state.notes.count { it.personId == p.id }
            Column(Modifier.fillMaxWidth().clickable { onOpen(p) }.padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, style = MaterialTheme.typography.bodyLarge, color = Night.ink, modifier = Modifier.weight(1f))
                    if (count > 0) Pill("$count notes", Night.violet)
                    Text("  ›", color = Night.muted)
                }
                next?.let { (d, on) ->
                    val days = on.toEpochDay() - today.toEpochDay()
                    Text(
                        "${d.label} · ${on.format(dayFmt)} · " + if (days == 0L) "today! 🎉" else "in $days days",
                        style = MaterialTheme.typography.bodySmall, color = if (days <= 14) Night.rose else Night.muted,
                    )
                }
            }
        }
        TextButton(onClick = onAdd) { Text("+ Add someone", color = Night.sun) }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PersonScreen(state: AppState, person: Person, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var text by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("idea") }
    var addingDate by remember { mutableStateOf(false) }
    var deletingNote by remember { mutableStateOf<String?>(null) }
    var deletingPerson by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = com.dudlatitat.nextchapter.ui.components.screenGutter(), vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Text(person.name, style = MaterialTheme.typography.headlineSmall, color = Night.ink, modifier = Modifier.weight(1f))
        }
        GlassCard {
            Text("Dates", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            if (person.dates.isEmpty()) Text("Birthday, anniversary… Today reminds you 2 weeks ahead.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            person.dates.forEach { d ->
                val on = runCatching { People.next(d, today) }.getOrNull()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${d.label} · ${on?.format(dayFmt) ?: "${d.month}/${d.day}"}", style = MaterialTheme.typography.bodyLarge, color = Night.ink, modifier = Modifier.weight(1f))
                    on?.let { Text("in ${it.toEpochDay() - today.toEpochDay()}d", style = MaterialTheme.typography.labelSmall, color = Night.muted) }
                    TextButton(onClick = { vm.removePersonDate(person.id, d) }) { Text("✕", color = Night.muted) }
                }
            }
            TextButton(onClick = { addingDate = true }) { Text("+ Add date", color = Night.sun) }
        }
        GlassCard {
            Text("Add a note", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                People.kinds.forEach { (k, label) -> FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(label) }) }
            }
            OutlinedTextField(text, { text = it.take(200) }, modifier = Modifier.fillMaxWidth(), label = { Text("e.g. loves osmanthus tea") })
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { vm.addNote(person.id, text, kind); text = "" }, enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
            ) { Text("Save") }
        }
        val notes = People.notesFor(state, person.id)
        if (notes.isEmpty()) Text("Nothing yet. Little things you notice add up to great gifts.", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        notes.forEach { n ->
            val label = People.kinds.firstOrNull { it.first == n.kind }?.second ?: "📝"
            GlassCard(Modifier.combinedClickable(onClick = {}, onLongClick = { deletingNote = n.id })) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = Night.muted)
                Text(n.text, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
            }
        }
        if (notes.isNotEmpty()) Text("Long-press a note to delete it.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        TextButton(onClick = { deletingPerson = true }) { Text("Remove ${person.name}", color = Night.coral) }
    }

    if (addingDate) DateDialog(onDismiss = { addingDate = false }) { vm.addPersonDate(person.id, it); addingDate = false }
    deletingNote?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            title = { Text("Delete this note?") },
            confirmButton = { TextButton(onClick = { vm.deleteNote(id); deletingNote = null }) { Text("Delete", color = Night.coral) } },
            dismissButton = { TextButton(onClick = { deletingNote = null }) { Text("Cancel") } },
        )
    }
    if (deletingPerson) AlertDialog(
        onDismissRequest = { deletingPerson = false },
        title = { Text("Remove ${person.name}?") },
        text = { Text("Their dates and notes are deleted too.") },
        confirmButton = { TextButton(onClick = { vm.deletePerson(person.id); deletingPerson = false; onBack() }) { Text("Remove", color = Night.coral) } },
        dismissButton = { TextButton(onClick = { deletingPerson = false }) { Text("Cancel") } },
    )
}

/** A yearly date: label plus month and day (no year, so it repeats). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateDialog(onDismiss: () -> Unit, onSave: (PersonDate) -> Unit) {
    var label by remember { mutableStateOf("Birthday") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    val m = month.toIntOrNull(); val d = day.toIntOrNull()
    val valid = m != null && d != null && runCatching { java.time.MonthDay.of(m, d) }.isSuccess
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Birthday", "Anniversary").forEach { l -> FilterChip(selected = label == l, onClick = { label = l }, label = { Text(l) }) }
                }
                OutlinedTextField(label, { label = it.take(30) }, label = { Text("What") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(month, { month = it.filter(Char::isDigit).take(2) }, label = { Text("Month") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(day, { day = it.filter(Char::isDigit).take(2) }, label = { Text("Day") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("For a lunar birthday, use this year's date for now.", style = MaterialTheme.typography.labelSmall, color = Night.muted)
            }
        },
        confirmButton = { TextButton(onClick = { if (valid) onSave(PersonDate(label.trim().ifBlank { "Date" }, m!!, d!!)) }, enabled = valid) { Text("Save", color = Night.sun) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TwoFieldDialog(title: String, first: String, second: String?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(a, { a = it.take(60) }, label = { Text(first) }, singleLine = true)
                if (second != null) OutlinedTextField(b, { b = it.take(40) }, label = { Text(second) }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { if (a.isNotBlank()) onSave(a, b) }, enabled = a.isNotBlank()) { Text("Add", color = Night.sun) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** On Today: one line for the closed "Coming up" row, or null when nothing is coming up. */
fun comingUpSummary(state: AppState): String? {
    val today = LocalDate.now()
    val dates = emptyList<People.Upcoming>() // People was removed from the app (Nov 2026); its saved notes stay in the data
    val jobs = Career.deadlinesSoon(state, today, within = 7)
    if (dates.isEmpty() && jobs.isEmpty()) return null
    return listOfNotNull(
        dates.firstOrNull()?.let { "🎂 ${it.person.name} " + if (it.days == 0L) "today" else "in ${it.days}d" },
        jobs.firstOrNull()?.let { (j, d) -> "💼 ${j.title} " + if (d == 0L) "today" else "in ${d}d" },
    ).joinToString(" · ")
}

/** Birthdays in the next 2 weeks and job deadlines in the next week (inside Today's row). */
@Composable
fun ComingUpList(state: AppState, onOpen: () -> Unit) {
    val today = LocalDate.now()
    val dates = emptyList<People.Upcoming>()
    val jobs = Career.deadlinesSoon(state, today, within = 7)
    Column(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        dates.forEach { u ->
            Text(
                "🎂 ${u.person.name}'s ${u.date.label.lowercase()} · " + if (u.days == 0L) "today!" else "in ${u.days} days",
                style = MaterialTheme.typography.bodyLarge, color = Night.rose,
            )
            val ideas = state.notes.count { it.personId == u.person.id && it.kind == "idea" }
            if (ideas > 0) Text("   $ideas gift ${if (ideas == 1) "idea" else "ideas"} saved", style = MaterialTheme.typography.labelSmall, color = Night.muted)
        }
        jobs.forEach { (j, days) ->
            Text("💼 ${j.title} closes " + if (days == 0L) "today" else "in $days days", style = MaterialTheme.typography.bodyLarge, color = if (days <= 3) Night.coral else Night.ink)
        }
        Text("Open Life →", style = MaterialTheme.typography.labelMedium, color = Night.sun, modifier = Modifier.padding(top = 6.dp))
    }
}

/** On the morning card: one A+ question to wake the brain up. */
@Composable
fun MorningAPlus(state: AppState, vm: MainViewModel, onMore: () -> Unit) {
    // One card a morning: once any A+ card is answered today, just say so.
    if (state.aplus.any { it.last == today() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("A+ ✓ studied today", style = MaterialTheme.typography.bodyMedium, color = Night.jade, modifier = Modifier.weight(1f))
            val left = APlus.queue(state).size
            if (left > 0) TextButton(onClick = onMore) { Text("$left more →", color = Night.sun) }
        }
        return
    }
    val card = APlus.queue(state).firstOrNull() ?: return
    var shown by remember(card.id) { mutableStateOf(false) }
    Text("A+ card · ${APlus.domainName(card)}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
    Column(Modifier.fillMaxWidth().animateContentSize().clickable { shown = true }) {
        Text(card.q, style = MaterialTheme.typography.titleMedium, color = Night.ink)
        if (shown) Text(card.a, style = MaterialTheme.typography.bodyLarge, color = Night.sun)
        else Text("Tap to check", style = MaterialTheme.typography.bodySmall, color = Night.muted)
    }
    if (shown) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { vm.gradeAPlus(card.id, Grade.AGAIN) }) { Text("Missed it", color = Night.coral) }
        Button(
            onClick = { vm.gradeAPlus(card.id, Grade.GOOD) },
            colors = ButtonDefaults.buttonColors(containerColor = Night.jade, contentColor = Night.ground2),
        ) { Text("Got it ✓") }
    }
}



// ---- Study timer ----

/** Start/stop a timer for what you're studying; today's and this week's minutes underneath. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyTimer(state: AppState, vm: MainViewModel) {
    var subject by rememberSaveable { mutableStateOf(com.dudlatitat.nextchapter.data.Study.subjects.first()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val running = state.timerStart != null
    // Tick once a second while the timer runs (the start time is saved, so it survives closing the app).
    androidx.compose.runtime.LaunchedEffect(state.timerStart) {
        while (state.timerStart != null) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) }
    }
    val S = com.dudlatitat.nextchapter.data.Study
    val week = S.week(state, com.dudlatitat.nextchapter.data.Stats.weekStart())
    // Not running: one row with Start (tap the row to pick the subject or add time you forgot).
    var open by rememberSaveable { mutableStateOf(false) }
    if (!running && !open) {
        com.dudlatitat.nextchapter.ui.components.ListGroup {
            com.dudlatitat.nextchapter.ui.components.ListRow(
                "Study timer · $subject",
                subtitle = if (week.values.sum() == 0) "Time your A+, school or Chinese study" else "Today ${S.label(S.dayTotal(state))} · this week ${S.label(week.values.sum())}", divider = false,
                leading = { com.dudlatitat.nextchapter.ui.components.IconBadge("时", Night.jade) },
                trailing = { com.dudlatitat.nextchapter.ui.components.GoChip("▶ Start", Night.jade) { vm.startTimer(subject) } },
                onClick = { open = true },
            )
        }
        return
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Study timer ⏱", style = MaterialTheme.typography.titleMedium, color = Night.ink)
                Text("Today ${S.label(S.dayTotal(state))} · this week ${S.label(week.values.sum())}", style = MaterialTheme.typography.bodySmall, color = Night.muted)
            }
            if (running) {
                val secs = ((now - state.timerStart!!) / 1000).coerceAtLeast(0)
                Text("%d:%02d".format(secs / 60, secs % 60), style = MaterialTheme.typography.headlineSmall, color = Night.sun)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (!running) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            S.subjects.forEach { sub -> FilterChip(selected = subject == sub, onClick = { subject = sub }, label = { Text(sub) }) }
        } else Text("Studying ${state.timerSubject}", style = MaterialTheme.typography.bodyMedium, color = Night.ink)
        Spacer(Modifier.height(6.dp))
        // Wraps onto a second line on the narrow cover screen (three buttons used to squeeze "Close" into "Clos/e").
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = { if (running) vm.stopTimer() else vm.startTimer(subject) },
                colors = ButtonDefaults.buttonColors(containerColor = if (running) Night.coral else Night.jade, contentColor = Night.ground2),
            ) { Text(if (running) "■ Stop and save" else "▶ Start") }
            if (!running) TextButton(onClick = { vm.addStudy(subject, 15) }) { Text("+15 min I forgot", color = Night.muted) }
            if (!running) TextButton(onClick = { open = false }) { Text("Close", color = Night.muted) }
        }
        if (week.isNotEmpty()) Text(
            "This week: " + week.entries.sortedByDescending { it.value }.joinToString(" · ") { "${it.key} ${S.label(it.value)}" },
            style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 6.dp),
        )
    }
}

