package com.dudlatitat.nextchapter.ui.life

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.ArrowBack
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

/** The Life tab: A+ study, the career path, and notes about the people who matter. */
@Composable
fun LifeScreen(state: AppState, vm: MainViewModel, startAPlus: Boolean = false, onStarted: () -> Unit = {}, startPage: String? = null, onPageStarted: () -> Unit = {}) {
    var page by rememberSaveable { mutableStateOf<String?>(null) }
    androidx.compose.runtime.LaunchedEffect(startAPlus) { if (startAPlus) { page = "aplus"; onStarted() } }
    androidx.compose.runtime.LaunchedEffect(startPage) { if (startPage != null) { page = startPage.takeIf { it != "timer" }; onPageStarted() } }
    when {
        page == "aplus" -> { APlusReview(state, vm, onBack = { page = null }); return }
        page == "test" -> { PracticeTestScreen(vm, onBack = { page = null }); return }
        page == "routine" -> { RoutineScreen(state, vm, onBack = { page = null }); return }
        page?.startsWith("person:") == true -> {
            val p = state.people.firstOrNull { it.id == page!!.removePrefix("person:") }
            if (p != null) { PersonScreen(state, p, vm, onBack = { page = null }); return } else page = null
        }
    }
    var addingClass by remember { mutableStateOf(false) }
    var editingJob by remember { mutableStateOf<JobPosting?>(null) }
    var addingPerson by remember { mutableStateOf(false) }
    var pickingExam by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenTitle("Life", "生活 · study, career and people") }
        item { APlusCard(state, vm, onStart = { page = "aplus" }, onPickExam = { pickingExam = true }, onTest = { page = "test" }) }
        item { StudyTimer(state, vm) }
        item { RoutineSummary(state, onOpen = { page = "routine" }) }

        item { SectionLabel("Career path") }
        item { CertPath(state, vm) }
        item { Classes(state, vm, onAdd = { addingClass = true }) }
        item { Jobs(state, onEdit = { editingJob = it }, onAdd = { editingJob = JobPosting("job:${System.currentTimeMillis()}", "") }) }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (pickingExam) PickDate(state.aplusExam, onDismiss = { pickingExam = false }) { vm.setAPlusExam(it); pickingExam = false }
    if (addingClass) TwoFieldDialog("Add a class", "Class (e.g. CIS 101)", "Term (e.g. Spring 2027)", onDismiss = { addingClass = false }) { a, b ->
        vm.addClass(a, b); addingClass = false
    }
    if (addingPerson) TwoFieldDialog("Add someone", "Name", null, onDismiss = { addingPerson = false }) { a, _ ->
        vm.addPerson(a); addingPerson = false
    }
    editingJob?.let { j -> JobDialog(j, isNew = state.jobs.none { it.id == j.id }, vm, onDismiss = { editingJob = null }) }
}

// ---- A+ ----

@Composable
private fun APlusCard(state: AppState, vm: MainViewModel, onStart: () -> Unit, onPickExam: () -> Unit, onTest: () -> Unit) {
    val queue = APlus.queue(state)
    val strength = APlus.strength(state)
    val weakest = APlus.weakest(state)
    val known = state.aplus.count { it.interval >= 7 }
    val days = daysUntil(state.aplusExam)
    GlassCard(highlight = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("A+ · Core 1", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text("220-1201 · ${APlus.core1.size} cards", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            }
            ProgressRing(known / APlus.core1.size.toFloat(), 56.dp, Night.jade, stroke = 6.dp) {
                Text("$known", style = MaterialTheme.typography.titleMedium, color = Night.ink)
            }
        }
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onPickExam, contentPadding = PaddingValues(0.dp)) {
            Text(
                when {
                    days == null -> "📅 Set your exam date"
                    days < 0 -> "📅 Exam date passed · set the next one"
                    else -> "📅 Exam in ${friendlySpan(days)}" + (if (days < 100) "" else " ($days days)")
                },
                color = if (days != null && days in 0..14) Night.coral else Night.sun,
            )
        }
        Spacer(Modifier.height(6.dp))
        // The five domains in one row: short name, a mini bar, how much you know.
        val short = mapOf(1 to "Mobile", 2 to "Network", 3 to "Hardware", 4 to "Cloud", 5 to "Fixes")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            APlus.domains.forEach { (d, info) ->
                val s0 = strength[d] ?: 0f
                val c = if (d == weakest) Night.sun else Night.jade
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(short.getValue(d), fontSize = 10.sp, color = if (d == weakest) Night.sun else Night.muted, maxLines = 1, softWrap = false)
                    Bar(s0, c, Modifier.padding(vertical = 3.dp))
                    Text("${(s0 * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Night.muted, maxLines = 1)
                }
            }
        }
        weakest?.let { Text("Focus: ${APlus.domains.getValue(it).first} (${APlus.domains.getValue(it).second}% of the exam)", style = MaterialTheme.typography.labelSmall, color = Night.sun, modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onStart, enabled = queue.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
        ) { Text(if (queue.isEmpty()) "All done for today ✓" else "Study ${queue.size} cards · about ${maxOf(1, queue.size / 4)} min") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onTest, modifier = Modifier.fillMaxWidth()) {
            Text("📝 Practice test · 20 questions", color = Night.violet, maxLines = 1)
        }
        state.tests.lastOrNull()?.let { t ->
            val pct = t.score * 100 / maxOf(1, t.total)
            Text(
                "Last test: ${t.score}/${t.total} ($pct%)" + if (pct >= 75) " · pass level ✓" else " · pass is ~75%",
                style = MaterialTheme.typography.labelSmall, color = if (pct >= 75) Night.jade else Night.muted,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("New cards a day: ${state.aplusPerDay}", style = MaterialTheme.typography.bodySmall, color = Night.muted, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.setAPlusPerDay(state.aplusPerDay - 2) }) { Text("−", color = Night.muted) }
            TextButton(onClick = { vm.setAPlusPerDay(state.aplusPerDay + 2) }) { Text("+", color = Night.muted) }
        }
    }
}

/** One A+ session: question, tap to reveal, grade. Misses come back at the end. */
@Composable
private fun APlusReview(state: AppState, vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val queue = remember { mutableStateListOf<String>().apply { addAll(APlus.queue(state).map { it.id }) } }
    val total = remember { queue.size }
    var done by remember { mutableIntStateOf(0) }
    var shown by remember { mutableStateOf(false) }
    val card = queue.firstOrNull()?.let { id -> APlus.core1.firstOrNull { it.id == id } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
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
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Life", color = Night.sun) }
            return@Column
        }
        Spacer(Modifier.height(10.dp))
        GlassCard(highlight = true, onClick = { shown = true }, modifier = Modifier.animateContentSize()) {
            Text(APlus.domains.getValue(card.domain).first, style = MaterialTheme.typography.labelMedium, color = Night.violet)
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

// ---- Career ----

private fun certColor(status: String) = when (status) { "studying" -> Night.violet; "booked" -> Night.sun; "passed" -> Night.jade; else -> Night.muted }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CertPath(state: AppState, vm: MainViewModel) {
    val certs = Career.certs(state)
    GlassCard {
        Text("Certifications · ${certs.count { it.status == "passed" }}/${certs.size} passed", style = MaterialTheme.typography.titleMedium, color = Night.ink)
        Text("Tap one to move it along: planned → studying → booked → passed", style = MaterialTheme.typography.bodySmall, color = Night.muted)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            certs.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { c ->
                        val color = certColor(c.status)
                        Column(
                            Modifier.weight(1f).clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                .background(color.copy(alpha = 0.12f)).clickable { vm.cycleCert(c.id) }.padding(10.dp),
                        ) {
                            Text(c.name.substringBefore(" ("), style = MaterialTheme.typography.labelLarge, color = Night.ink, maxLines = 1)
                            Text((if (c.status == "passed") "✓ " else "") + c.status, style = MaterialTheme.typography.labelSmall, color = color)
                        }
                    }
                }
            }
        }
    }
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
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
    Text("A+ card · ${APlus.domains.getValue(card.domain).first}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { if (running) vm.stopTimer() else vm.startTimer(subject) },
                colors = ButtonDefaults.buttonColors(containerColor = if (running) Night.coral else Night.jade, contentColor = Night.ground2),
            ) { Text(if (running) "■ Stop and save" else "▶ Start") }
            if (!running) TextButton(onClick = { vm.addStudy(subject, 15) }) { Text("+15 min I forgot", color = Night.muted) }
        }
        if (week.isNotEmpty()) Text(
            "This week: " + week.entries.sortedByDescending { it.value }.joinToString(" · ") { "${it.key} ${S.label(it.value)}" },
            style = MaterialTheme.typography.labelSmall, color = Night.muted, modifier = Modifier.padding(top = 6.dp),
        )
    }
}

// ---- Practice test ----

/** 20 mixed questions, about a minute each like the real exam; misses come back in your reviews. */
@Composable
private fun PracticeTestScreen(vm: MainViewModel, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val questions = remember { com.dudlatitat.nextchapter.data.PracticeTest.build(20) }
    var index by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    val missed = remember { mutableStateListOf<String>() }
    val startedAt = remember { System.currentTimeMillis() }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var finished by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(finished) { while (!finished) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val elapsed = ((now - startedAt) / 1000).toInt()
    val limit = questions.size * 60

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back", tint = Night.ink) }
            Column(Modifier.weight(1f)) {
                Text("A+ practice test", style = MaterialTheme.typography.titleLarge, color = Night.ink)
                Text(if (finished) "Finished" else "Question ${index + 1} of ${questions.size}", style = MaterialTheme.typography.labelMedium, color = Night.muted)
            }
            val left = limit - elapsed
            Text(
                if (left >= 0) "%d:%02d".format(left / 60, left % 60) else "+%d:%02d".format(-left / 60, -left % 60),
                style = MaterialTheme.typography.titleMedium, color = if (left < 120) Night.coral else Night.sun,
            )
        }
        Bar((if (finished) questions.size else index) / questions.size.toFloat(), Night.violet, Modifier.padding(vertical = 8.dp))

        if (finished) {
            val score = questions.size - missed.size
            val pct = score * 100 / questions.size
            val passed = pct >= (com.dudlatitat.nextchapter.data.PracticeTest.PASS * 100).toInt()
            Spacer(Modifier.height(20.dp))
            Text("$score / ${questions.size}", style = MaterialTheme.typography.displaySmall, color = if (passed) Night.jade else Night.sun, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(
                if (passed) "$pct% · that's pass level. 太棒了!" else "$pct% · pass is about 75%. You're getting there.",
                style = MaterialTheme.typography.bodyLarge, color = Night.ink, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            )
            Text("Time ${elapsed / 60} min ${elapsed % 60} s", style = MaterialTheme.typography.labelMedium, color = Night.muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            if (missed.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Missed (they're back in today's A+ cards)", style = MaterialTheme.typography.labelMedium, color = Night.muted)
                missed.forEach { id ->
                    val q = questions.first { it.card.id == id }.card
                    GlassCard(Modifier.padding(top = 8.dp)) {
                        Text(q.q, style = MaterialTheme.typography.bodyLarge, color = Night.ink)
                        Text(q.a, style = MaterialTheme.typography.bodyMedium, color = Night.sun)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Life", color = Night.sun) }
            return@Column
        }

        val q = questions[index]
        Spacer(Modifier.height(8.dp))
        GlassCard(highlight = true) {
            Text(APlus.domains.getValue(q.card.domain).first, style = MaterialTheme.typography.labelMedium, color = Night.violet)
            Spacer(Modifier.height(8.dp))
            Text(q.card.q, style = MaterialTheme.typography.titleLarge, color = Night.ink)
        }
        Spacer(Modifier.height(12.dp))
        q.options.forEachIndexed { i, option ->
            val color = when {
                picked == null -> Night.ink
                i == q.correct -> Night.jade
                i == picked -> Night.coral
                else -> Night.muted
            }
            OutlinedButton(
                onClick = {
                    if (picked != null) return@OutlinedButton
                    picked = i
                    if (i != q.correct) missed.add(q.card.id)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) { Text(option, color = color, modifier = Modifier.fillMaxWidth()) }
        }
        if (picked != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    picked = null
                    if (index + 1 < questions.size) index++ else {
                        finished = true
                        vm.saveTest(questions.size - missed.size, questions.size, elapsed, missed.toList())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Night.sun, contentColor = MaterialTheme.colorScheme.onPrimary),
            ) { Text(if (index + 1 < questions.size) "Next" else "See my score") }
        }
    }
}
