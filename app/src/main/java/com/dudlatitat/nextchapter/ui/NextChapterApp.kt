package com.dudlatitat.nextchapter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dudlatitat.nextchapter.MainViewModel
import com.dudlatitat.nextchapter.ui.learn.LearnScreen
import com.dudlatitat.nextchapter.ui.move.MoveScreen
import com.dudlatitat.nextchapter.ui.theme.Night
import com.dudlatitat.nextchapter.ui.theme.StarryBackdrop
import com.dudlatitat.nextchapter.ui.today.TodayScreen
import com.dudlatitat.nextchapter.ui.settings.SettingsScreen

enum class Tab(val label: String, val icon: ImageVector) {
    // Four tabs, each one question: what now, practice, getting better, the plan.
    TODAY("Today", Icons.Rounded.Home),
    LEARN("Chinese", Icons.Rounded.Star),
    LIFE("Life", Icons.Rounded.Person),
    MOVE("Move", Icons.Rounded.Place),
}

@Composable
fun NextChapterApp(vm: MainViewModel, openStudy: Boolean = false, onStudyOpened: () -> Unit = {}, openToday: Boolean = false, onTodayOpened: () -> Unit = {}, openGo: String? = null, onGoOpened: () -> Unit = {}) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    // Opening a review straight from Today.
    var startReview by rememberSaveable { mutableStateOf(false) }
    var startRedo by rememberSaveable { mutableStateOf(false) }
    var startTool by rememberSaveable { mutableStateOf<String?>(null) }
    var startAPlus by rememberSaveable { mutableStateOf(false) }
    // A Life page to open straight away: "test", "routine".
    var startLife by rememberSaveable { mutableStateOf<String?>(null) }
    var startDeck by rememberSaveable { mutableStateOf<String?>(null) }
    // New milestones get a little celebration, once.
    // ...but never mid-practice (the Chinese tab) or during her call.
    val fresh = androidx.compose.runtime.remember(state) { com.dudlatitat.nextchapter.data.Milestones.fresh(state) }
    val quiet = tab == Tab.LEARN || com.dudlatitat.nextchapter.data.Life.isCallTime(java.time.LocalTime.now())
    if (fresh.isNotEmpty() && !quiet) androidx.compose.material3.AlertDialog(
        onDismissRequest = { vm.seeMilestones(fresh.map { it.id }) },
        title = { Text(if (fresh.size == 1) "${fresh[0].emoji} ${fresh[0].title}!" else "🏅 ${fresh.size} new milestones!") },
        text = {
            androidx.compose.foundation.layout.Column {
                fresh.forEach { Text("${it.emoji}  ${it.title}: ${it.detail}") }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = { vm.seeMilestones(fresh.map { it.id }) }) { Text("太好了!") } },
    )
    var settings by rememberSaveable { mutableStateOf(false) }
    var searching by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openStudy) {
        if (openStudy) { settings = false; startReview = true; tab = Tab.LEARN; onStudyOpened() }
    }
    // Launcher shortcuts (long-press the app icon).
    LaunchedEffect(openGo) {
        when (openGo) {
            null -> return@LaunchedEffect
            "review" -> { startReview = true; tab = Tab.LEARN }
            "add", "drive", "callouts", "dinner" -> { startTool = openGo; tab = Tab.LEARN }
            "aplus" -> { startAPlus = true; tab = Tab.LIFE }
        }
        settings = false
        onGoOpened()
    }
    LaunchedEffect(openToday) {
        if (openToday) { settings = false; tab = Tab.TODAY; onTodayOpened() }
    }

    // Each tab keeps its place (an open review, a deck, a test) while you look at another tab.
    val tabStates = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
    // Tapping the tab you're already on goes back to its top page.
    val resets = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateMapOf<Tab, Int>() }
    // Back on a tab's top page goes to Today first, rather than closing the app.
    androidx.activity.compose.BackHandler(enabled = tab != Tab.TODAY) { tab = Tab.TODAY }

    // Opened from a Today ring: when that review or game closes, come back to Today to see the
    // ring fill (instead of being left on the Chinese or Life tab).
    var returnToday by rememberSaveable { mutableStateOf(false) }
    val backToToday: () -> Unit = { if (returnToday) { returnToday = false; tab = Tab.TODAY } }
    val wide = com.dudlatitat.nextchapter.ui.components.isWide()
    val pick: (Tab) -> Unit = { t ->
        returnToday = false
        if (tab == t) { tabStates.removeState("${t.name}-${resets[t] ?: 0}"); resets[t] = (resets[t] ?: 0) + 1 } else tab = t
    }
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Night.sun, selectedTextColor = Night.sun, indicatorColor = Night.sun.copy(alpha = 0.16f),
        unselectedIconColor = Night.muted, unselectedTextColor = Night.muted,
    )

    // "Next" from any payoff screen: the same routes as Today's rings.
    val planNav: (com.dudlatitat.nextchapter.data.DayPlan.Item) -> Unit = { item ->
        when (item.kind) {
            com.dudlatitat.nextchapter.data.DayPlan.Kind.REVIEW -> { startReview = true; tab = Tab.LEARN }
            com.dudlatitat.nextchapter.data.DayPlan.Kind.PLAY -> { startTool = item.tool; tab = Tab.LEARN }
            com.dudlatitat.nextchapter.data.DayPlan.Kind.LIFE -> if (item.tool == "aplus") { startAPlus = true; tab = Tab.LIFE } else { returnToday = false; tab = Tab.TODAY }
        }
    }
    val day = com.dudlatitat.nextchapter.data.today()
    val claim = androidx.compose.runtime.remember(state, day) { com.dudlatitat.nextchapter.data.Payoff.claimDue(state, day) }
    val callTime = com.dudlatitat.nextchapter.data.Life.isCallTime(java.time.LocalTime.now())

    val appNav = androidx.compose.runtime.remember { com.dudlatitat.nextchapter.ui.components.AppNav(openSearch = { searching = true }, openSettings = { settings = true }) }
    androidx.compose.runtime.CompositionLocalProvider(com.dudlatitat.nextchapter.ui.components.LocalAppNav provides appNav) {
    com.dudlatitat.nextchapter.ui.components.ProvidePlanNav(planNav) {
    Box(Modifier.fillMaxSize()) {
        StarryBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Unfolded, the tabs live in a rail on the left instead (a bar 840dp wide is a long reach).
                if (!wide) NavigationBar(containerColor = Night.tabBar) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t, onClick = { pick(t) },
                            icon = { Icon(t.icon, contentDescription = null) }, label = { Text(t.label) }, colors = navColors,
                        )
                    }
                }
            },
        ) { padding ->
            androidx.compose.foundation.layout.Row(Modifier.padding(padding)) {
                if (wide) androidx.compose.material3.NavigationRail(
                    containerColor = Night.tabBar,
                    header = { Spacer(Modifier.height(12.dp)) },
                ) {
                    Spacer(Modifier.weight(1f))
                    Tab.entries.forEach { t ->
                        androidx.compose.material3.NavigationRailItem(
                            selected = tab == t, onClick = { pick(t) },
                            icon = { Icon(t.icon, contentDescription = null) }, label = { Text(t.label) },
                            colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                                selectedIconColor = Night.sun, selectedTextColor = Night.sun, indicatorColor = Night.sun.copy(alpha = 0.16f),
                                unselectedIconColor = Night.muted, unselectedTextColor = Night.muted,
                            ),
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            AnimatedContent(tab, Modifier.weight(1f), transitionSpec = { com.dudlatitat.nextchapter.ui.components.Motion.fadeThrough() }, label = "tab") { t ->
                tabStates.SaveableStateProvider("${t.name}-${resets[t] ?: 0}") {
                when (t) {
                    Tab.TODAY -> TodayScreen(state, vm, onStudy = { startReview = true; returnToday = true; tab = Tab.LEARN }, onOpenMove = { tab = Tab.MOVE }, onSettings = { settings = true }, onRedo = { startRedo = true; returnToday = true; tab = Tab.LEARN }, onPhrasebook = { startTool = "phrasebook"; tab = Tab.LEARN }, onOpenLife = { tab = Tab.LIFE }, onAPlus = { startAPlus = true; returnToday = true; tab = Tab.LIFE }, onTool = { startTool = it; returnToday = true; tab = Tab.LEARN }, onLifePage = { startLife = it; tab = Tab.LIFE }, onOpenChinese = { tab = Tab.LEARN })
                    Tab.LEARN -> LearnScreen(state, vm, startReview = startReview, onReviewStarted = { startReview = false; startRedo = false; startTool = null }, startRedo = startRedo, startTool = startTool, startDeck = startDeck, onDeckOpened = { startDeck = null }, onLeave = backToToday)
                    Tab.MOVE -> MoveScreen(state, vm)
                    Tab.LIFE -> com.dudlatitat.nextchapter.ui.life.LifeScreen(state, vm, startAPlus = startAPlus, onStarted = { startAPlus = false }, startPage = startLife, onPageStarted = { startLife = null }, onLeave = backToToday)
                }
                }
            }
            }
        }
        // Settings slides up over the app instead of replacing it, so whatever you were in the
        // middle of (a review, a deck) is still there when you come back.
        androidx.compose.animation.AnimatedVisibility(
            settings,
            enter = androidx.compose.animation.slideInVertically(tween(320, easing = com.dudlatitat.nextchapter.ui.components.Motion.Emph)) { it / 8 } + androidx.compose.animation.fadeIn(tween(220)),
            exit = androidx.compose.animation.slideOutVertically(tween(220, easing = com.dudlatitat.nextchapter.ui.components.Motion.EmphAcc)) { it / 8 } + androidx.compose.animation.fadeOut(tween(160)),
        ) {
            Box(
                Modifier.fillMaxSize().clickable(
                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                ) { },
            ) {
                StarryBackdrop()
                com.dudlatitat.nextchapter.ui.components.ReadableWidth { SettingsScreen(state, vm, onBack = { settings = false }) }
            }
        }
        // Search opens over whatever tab you're on (that tab stays highlighted underneath).
        androidx.compose.animation.AnimatedVisibility(
            searching,
            enter = androidx.compose.animation.fadeIn(tween(200)) + androidx.compose.animation.slideInVertically(tween(280, easing = com.dudlatitat.nextchapter.ui.components.Motion.Emph)) { it / 12 },
            exit = androidx.compose.animation.fadeOut(tween(160)),
        ) {
            Box(Modifier.fillMaxSize().clickable(interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { }) {
                StarryBackdrop()
                com.dudlatitat.nextchapter.ui.components.ReadableWidth { com.dudlatitat.nextchapter.ui.learn.SearchScreen(state, vm, onBack = { searching = false }, onDeck = { searching = false; startDeck = it; tab = Tab.LEARN }) }
            }
        }
        // All three done: the once-a-day moment, on Today (where the rings are), never during her call.
        androidx.compose.animation.AnimatedVisibility(
            claim && tab == Tab.TODAY && !settings && !searching && !callTime,
            enter = androidx.compose.animation.fadeIn(tween(500)),
            exit = androidx.compose.animation.fadeOut(tween(400)) + androidx.compose.animation.scaleOut(tween(400), targetScale = 1.05f),
        ) {
            com.dudlatitat.nextchapter.ui.components.DayClaim(state, androidx.compose.runtime.remember(state, day) { com.dudlatitat.nextchapter.data.Stats.streak(state, day) }) { vm.celebrate(day) }
        }
    }
    }
}
}
