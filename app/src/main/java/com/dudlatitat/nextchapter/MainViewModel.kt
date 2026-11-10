package com.dudlatitat.nextchapter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.BudgetItem
import com.dudlatitat.nextchapter.data.DayLog
import com.dudlatitat.nextchapter.data.PackItem
import com.dudlatitat.nextchapter.data.PriceSeen
import com.dudlatitat.nextchapter.data.ToneEvent
import com.dudlatitat.nextchapter.data.Trip
import com.dudlatitat.nextchapter.data.Card
import com.dudlatitat.nextchapter.data.Grade
import com.dudlatitat.nextchapter.data.MoveTask
import com.dudlatitat.nextchapter.data.Srs
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.data.today
import kotlinx.coroutines.flow.StateFlow
import com.dudlatitat.nextchapter.notify.Reminder
import com.dudlatitat.nextchapter.widget.PhraseWidget

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val store = Store.get(app)
    val state: StateFlow<AppState> = store.state
    val speaker = Speaker(app)
    val coach = com.dudlatitat.nextchapter.speech.Coach(app)

    init {
        store.onChange = { PhraseWidget.refresh(app, it); com.dudlatitat.nextchapter.widget.WeekWidget.refresh(app, it) }
        PhraseWidget.refresh(app, store.state.value)
        com.dudlatitat.nextchapter.widget.WeekWidget.refresh(app, store.state.value)
        Reminder.schedule(app, store.state.value)
        com.dudlatitat.nextchapter.notify.Evening.schedule(app, store.state.value)
        com.dudlatitat.nextchapter.notify.Deadlines.schedule(app, store.state.value)
        com.dudlatitat.nextchapter.notify.PlaceWatch.register(app, store.state.value)
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(app)
    }

    // Progress in the learning tools (for milestones)
    private fun addOnce(list: List<String>, id: String) = if (id in list) list else list + id
    fun markDialogue(id: String) = store.edit { it.copy(dialoguesDone = addOnce(it.dialoguesDone, id)) }
    fun passGrammar(id: String) = store.edit { it.copy(grammarPassed = addOnce(it.grammarPassed, id)) }
    fun readStory(id: String) = store.edit { it.copy(storiesRead = addOnce(it.storiesRead, id)) }
    fun wroteChar() = store.edit { it.copy(charsWritten = it.charsWritten + 1) }
    fun seeMilestones(ids: List<String>) = store.edit { s -> s.copy(milestonesSeen = (s.milestonesSeen + ids).distinct()) }
    fun setTripMode(mode: String) = store.edit { it.copy(tripMode = mode) }
    fun setHer(name: String, city: String) = store.edit { it.copy(herName = name.trim().take(30), herCity = city.trim().take(40)) }

    // Fare alerts
    fun saveAlert(a: com.dudlatitat.nextchapter.data.FareAlert) {
        store.edit { s -> s.copy(alerts = if (s.alerts.any { it.id == a.id }) s.alerts.map { if (it.id == a.id) a else it } else s.alerts + a) }
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(getApplication())
    }
    fun deleteAlert(a: com.dudlatitat.nextchapter.data.FareAlert) {
        store.edit { s -> s.copy(alerts = s.alerts.filterNot { it.id == a.id }, fares = s.fares.filterNot { it.alertId == a.id }) }
        com.dudlatitat.nextchapter.notify.FareWatch.schedule(getApplication())
    }

    fun setReminder(on: Boolean, minutes: Int) {
        store.edit { it.copy(reminderOn = on, reminderMinutes = minutes) }
        Reminder.schedule(getApplication(), store.state.value)
    }

    fun restore(text: String): Boolean = store.restore(text).also {
        if (it) Reminder.schedule(getApplication(), store.state.value)
    }

    fun grade(card: Card, grade: Grade) = store.edit { s ->
        s.copy(cards = s.cards.map { if (it.id == card.id) Srs.grade(it, grade) else it }).logDay { it.copy(reviews = it.reviews + 1) }
    }

    /** Adds practice time to today (called when a session ends or closes). */
    fun logSeconds(seconds: Int) {
        if (seconds <= 0) return
        store.edit { s -> s.logDay { it.copy(seconds = it.seconds + seconds.coerceAtMost(3600)) } }
    }

    private fun AppState.logDay(change: (DayLog) -> DayLog): AppState {
        val d = today()
        val kept = days.filterKeys { it > d - 400 }
        return copy(days = kept + (d to change(kept[d] ?: DayLog())))
    }

    /** Remembers one tone attempt, for weak-tone drills and the weekly recap. */
    fun logTone(target: Int, got: Int, spoken: Boolean) {
        if (target !in 1..4 || got !in 1..4) return
        store.edit { s -> s.copy(toneEvents = (s.toneEvents + ToneEvent(today(), target, got, spoken)).takeLast(800)) }
    }

    // Flights
    fun saveTrip(trip: Trip) = store.edit { s ->
        s.copy(trips = (s.trips.filterNot { it.id == trip.id } + trip).sortedBy { it.depart })
    }
    fun deleteTrip(trip: Trip) = store.edit { s -> s.copy(trips = s.trips.filterNot { it.id == trip.id }) }
    fun addPrice(price: Int, source: String) = store.edit { s ->
        s.copy(prices = (s.prices + PriceSeen("price:${System.currentTimeMillis()}", java.time.LocalDate.now().toString(), price, source.trim())).takeLast(300))
    }
    fun deletePrice(p: PriceSeen) = store.edit { s -> s.copy(prices = s.prices.filterNot { it.id == p.id }) }

    // Packing
    fun togglePack(item: PackItem) = store.edit { s -> s.copy(packing = s.packing.map { if (it.id == item.id) it.copy(done = !it.done) else it }) }
    fun addPack(title: String) = store.edit { s -> s.copy(packing = s.packing + PackItem("pack:${System.currentTimeMillis()}", title.trim())) }
    fun deletePack(item: PackItem) = store.edit { s -> s.copy(packing = s.packing.filterNot { it.id == item.id }) }
    fun resetPacking() = store.edit { s -> s.copy(packing = s.packing.map { it.copy(done = false) }) }

    // Money
    fun saveBudget(item: BudgetItem) = store.edit { s ->
        s.copy(budget = if (s.budget.any { it.id == item.id }) s.budget.map { if (it.id == item.id) item else it } else s.budget + item)
    }
    fun deleteBudget(item: BudgetItem) = store.edit { s -> s.copy(budget = s.budget.filterNot { it.id == item.id }) }
    fun setRate(rate: Double, date: String?) = store.edit { it.copy(rate = rate, rateDate = date) }

    /** Fetches today's USD → CNY rate; false without internet. */
    suspend fun refreshRate(): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val c = java.net.URL("https://open.er-api.com/v6/latest/USD").openConnection() as java.net.HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 8000
            val text = c.inputStream.bufferedReader().use { it.readText() }
            // A missing or unbelievable rate (e.g. 0) is ignored, so conversions never divide by zero.
            val rate = com.dudlatitat.nextchapter.data.Money.parseCny(text) ?: return@withContext false
            setRate(rate, java.time.LocalDate.now().toString())
            true
        } catch (_: Exception) { false }
    }

    /** Marks today as a study day (after finishing a session). */
    fun finishedSession() = store.edit { s ->
        val d = today()
        if (d in s.studyDays) s else s.copy(studyDays = (s.studyDays + d).takeLast(800))
    }

    fun addCard(hanzi: String, pinyin: String, english: String) = store.edit { s ->
        val card = Card(id = "mine:${System.currentTimeMillis()}", hanzi = hanzi.trim(), pinyin = pinyin.trim(), english = english.trim(), deck = "mine", custom = true)
        s.copy(cards = s.cards + card)
    }

    fun deleteCard(card: Card) = store.edit { s -> s.copy(cards = s.cards.filterNot { it.id == card.id }) }

    fun toggleTask(task: MoveTask) = store.edit { s ->
        // Steps linked to the Life tab (certs, the degree) are updated there and mirrored here.
        com.dudlatitat.nextchapter.data.Career.tickFromMove(s, task.id, !task.done)
            ?: s.copy(tasks = s.tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it })
    }

    fun addTask(title: String, group: String) = store.edit { s ->
        s.copy(tasks = s.tasks + MoveTask(id = "own:${System.currentTimeMillis()}", title = title.trim(), group = group))
    }

    fun deleteTask(task: MoveTask) = store.edit { s -> s.copy(tasks = s.tasks.filterNot { it.id == task.id }) }

    fun updatePlan(change: (AppState) -> AppState) = store.edit(change)

    // ---- Daily rhythm (Today's morning card, evening plan, Sunday review) ----
    fun markEarlyStart() = store.edit { s -> if (today() in s.wakeDays) s else s.copy(wakeDays = com.dudlatitat.nextchapter.data.Life.addDay(s.wakeDays, today())) }
    fun prayed() = store.edit { s -> s.copy(prayedDays = com.dudlatitat.nextchapter.data.Life.addDay(s.prayedDays, today())) }
    fun setGoal(text: String, day: Long = today()) = store.edit { s ->
        s.copy(goals = com.dudlatitat.nextchapter.data.Life.withGoal(s.goals, com.dudlatitat.nextchapter.data.DayGoal(day, text.trim())))
    }
    fun answerGoal(done: Boolean) = store.edit { s ->
        val g = com.dudlatitat.nextchapter.data.Life.goalFor(s) ?: return@edit s
        s.copy(goals = com.dudlatitat.nextchapter.data.Life.withGoal(s.goals, g.copy(done = done)))
    }
    fun setWeekNote(weekStart: String, note: String) = store.edit { s ->
        s.copy(weekNotes = if (note.isBlank()) s.weekNotes - weekStart else s.weekNotes + (weekStart to note.trim()))
    }
    // ---- Life tab: A+, career, people ----
    fun gradeAPlus(id: String, g: Grade) = store.edit { s ->
        val r = com.dudlatitat.nextchapter.data.APlus.grade(com.dudlatitat.nextchapter.data.APlus.review(s, id), g)
        s.copy(aplus = com.dudlatitat.nextchapter.data.APlus.withReview(s.aplus, r))
    }
    fun setAPlusExam(iso: String?) = store.edit { it.copy(aplusExam = iso) }
    fun setAPlusPerDay(n: Int) = store.edit { it.copy(aplusPerDay = n.coerceIn(3, 30)) }
    fun cycleCert(id: String) = store.edit { s ->
        val certs = com.dudlatitat.nextchapter.data.Career.certs(s)
        com.dudlatitat.nextchapter.data.Career.sync(
            s.copy(certs = certs.map { if (it.id == id) it.copy(status = com.dudlatitat.nextchapter.data.Career.nextStatus(it.status)) else it }),
        )
    }
    fun addClass(name: String, term: String) = store.edit { s ->
        com.dudlatitat.nextchapter.data.Career.sync(s.copy(classes = s.classes + com.dudlatitat.nextchapter.data.SchoolClass("class:${System.currentTimeMillis()}", name.trim(), term.trim())))
    }
    fun toggleClass(id: String) = store.edit { s -> com.dudlatitat.nextchapter.data.Career.sync(s.copy(classes = s.classes.map { if (it.id == id) it.copy(done = !it.done) else it })) }
    fun deleteClass(id: String) = store.edit { s -> com.dudlatitat.nextchapter.data.Career.sync(s.copy(classes = s.classes.filterNot { it.id == id })) }
    fun saveJob(job: com.dudlatitat.nextchapter.data.JobPosting) {
        store.edit { s -> s.copy(jobs = if (s.jobs.any { it.id == job.id }) s.jobs.map { if (it.id == job.id) job else it } else s.jobs + job) }
        com.dudlatitat.nextchapter.notify.Deadlines.schedule(getApplication(), store.state.value)
    }
    fun deleteJob(id: String) = store.edit { s -> s.copy(jobs = s.jobs.filterNot { it.id == id }) }
    fun setJobReminders(on: Boolean) {
        store.edit { it.copy(jobReminders = on) }
        com.dudlatitat.nextchapter.notify.Deadlines.schedule(getApplication(), store.state.value)
    }
    fun addPerson(name: String) = store.edit { s ->
        s.copy(people = s.people + com.dudlatitat.nextchapter.data.Person("person:${System.currentTimeMillis()}", name.trim()))
    }
    fun deletePerson(id: String) = store.edit { s -> s.copy(people = s.people.filterNot { it.id == id }, notes = s.notes.filterNot { it.personId == id }) }
    fun addPersonDate(personId: String, d: com.dudlatitat.nextchapter.data.PersonDate) = store.edit { s ->
        s.copy(people = s.people.map { if (it.id == personId) it.copy(dates = it.dates + d) else it })
    }
    fun removePersonDate(personId: String, d: com.dudlatitat.nextchapter.data.PersonDate) = store.edit { s ->
        s.copy(people = s.people.map { if (it.id == personId) it.copy(dates = it.dates - d) else it })
    }
    fun addNote(personId: String, text: String, kind: String) = store.edit { s ->
        s.copy(notes = s.notes + com.dudlatitat.nextchapter.data.PersonNote("note:${System.currentTimeMillis()}", personId, text.trim(), kind, System.currentTimeMillis()))
    }
    fun deleteNote(id: String) = store.edit { s -> s.copy(notes = s.notes.filterNot { it.id == id }) }

    // ---- Study timer and practice tests ----
    fun startTimer(subject: String) = store.edit { it.copy(timerStart = System.currentTimeMillis(), timerSubject = subject) }
    fun stopTimer() = store.edit { s ->
        val start = s.timerStart ?: return@edit s
        val mins = com.dudlatitat.nextchapter.data.Study.minutes(start, System.currentTimeMillis())
        s.copy(
            study = com.dudlatitat.nextchapter.data.Study.add(s.study, com.dudlatitat.nextchapter.data.StudySession(today(), s.timerSubject ?: "Other", mins)),
            timerStart = null, timerSubject = null,
        )
    }
    fun addStudy(subject: String, minutes: Int) = store.edit { s ->
        s.copy(study = com.dudlatitat.nextchapter.data.Study.add(s.study, com.dudlatitat.nextchapter.data.StudySession(today(), subject, minutes)))
    }
    /** Saves a practice test; missed cards come back in your A+ reviews today. */
    fun saveTest(score: Int, total: Int, seconds: Int, missed: List<String>) = store.edit { s ->
        var aplus = s.aplus
        missed.forEach { id ->
            aplus = com.dudlatitat.nextchapter.data.APlus.withReview(aplus, com.dudlatitat.nextchapter.data.APlus.grade(com.dudlatitat.nextchapter.data.APlus.review(s.copy(aplus = aplus), id), Grade.AGAIN))
        }
        val R = com.dudlatitat.nextchapter.data.Routine
        val job = R.jobFor(s.routine, java.time.LocalDate.now())
        val checks = if (job.kind == "aplus" && R.checkFor(s.routineChecks, today()) == null)
            R.withCheck(s.routineChecks, com.dudlatitat.nextchapter.data.RoutineCheck(today(), com.dudlatitat.nextchapter.data.CheckIn.DONE))
        else s.routineChecks
        s.copy(aplus = aplus, routineChecks = checks, tests = (s.tests + com.dudlatitat.nextchapter.data.TestResult(today(), score, total, seconds, missed)).takeLast(100))
    }

    // ---- Work & home ----
    private fun placesChanged() {
        com.dudlatitat.nextchapter.notify.PlaceWatch.register(getApplication(), store.state.value)
        com.dudlatitat.nextchapter.notify.Evening.schedule(getApplication(), store.state.value)
    }
    fun setPlacesOn(on: Boolean) { store.edit { it.copy(placesOn = on) }; placesChanged() }
    fun saveSpot(kind: String, spot: com.dudlatitat.nextchapter.data.Spot) {
        // A new spot starts today fresh: arrivals noticed with the old one don't count.
        store.edit { s -> (if (kind == "work") s.copy(workSpot = spot) else s.copy(homeSpot = spot)).copy(arrivals = com.dudlatitat.nextchapter.data.Places.resetDay(s.arrivals, today())) }
        placesChanged()
    }
    fun clearSpot(kind: String) {
        store.edit { s -> (if (kind == "work") s.copy(workSpot = null) else s.copy(homeSpot = null)).copy(arrivals = com.dudlatitat.nextchapter.data.Places.resetDay(s.arrivals, today())) }
        placesChanged()
    }
    fun resetToday() {
        store.edit { it.copy(arrivals = com.dudlatitat.nextchapter.data.Places.resetDay(it.arrivals, today())) }
        placesChanged()
    }
    /** "I'm home" by hand, when the phone didn't notice. */
    fun arrivedHomeNow() {
        val t = java.time.LocalTime.now()
        store.edit { it.copy(arrivals = com.dudlatitat.nextchapter.data.Places.manualHome(it.arrivals, today(), t.hour * 60 + t.minute)) }
        placesChanged()
    }

    // ---- Weekly routine ----
    fun checkIn(status: com.dudlatitat.nextchapter.data.CheckIn, day: Long = today()) = store.edit { s ->
        s.copy(routineChecks = com.dudlatitat.nextchapter.data.Routine.withCheck(s.routineChecks, com.dudlatitat.nextchapter.data.RoutineCheck(day, status)))
    }
    fun saveRoutineDay(day: com.dudlatitat.nextchapter.data.RoutineDay) = store.edit { s ->
        s.copy(routine = com.dudlatitat.nextchapter.data.Routine.withDay(s.routine, day))
    }
    fun setRoutineReview(weekStart: String, review: com.dudlatitat.nextchapter.data.RoutineReview) = store.edit { s ->
        s.copy(routineReviews = (s.routineReviews + (weekStart to review)).entries.sortedBy { it.key }.takeLast(104).associate { it.toPair() })
    }
    fun setRoutineTimes(wake: Int, home: Int, windDown: Int) {
        store.edit { it.copy(wakeMinutes = wake, homeMinutes = home, windDownMinutes = windDown) }
        com.dudlatitat.nextchapter.notify.Evening.schedule(getApplication(), store.state.value)
    }

    fun setEveningCheck(on: Boolean) {
        store.edit { it.copy(eveningCheck = on) }
        com.dudlatitat.nextchapter.notify.Evening.schedule(getApplication(), store.state.value)
    }

    override fun onCleared() = speaker.close()
}
