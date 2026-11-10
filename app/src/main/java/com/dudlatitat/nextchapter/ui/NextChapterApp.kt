package com.dudlatitat.nextchapter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
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
fun NextChapterApp(vm: MainViewModel, openStudy: Boolean = false, onStudyOpened: () -> Unit = {}, openToday: Boolean = false, onTodayOpened: () -> Unit = {}) {
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
    val fresh = com.dudlatitat.nextchapter.data.Milestones.fresh(state)
    if (fresh.isNotEmpty()) androidx.compose.material3.AlertDialog(
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

    LaunchedEffect(openStudy) {
        if (openStudy) { settings = false; startReview = true; tab = Tab.LEARN; onStudyOpened() }
    }
    LaunchedEffect(openToday) {
        if (openToday) { settings = false; tab = Tab.TODAY; onTodayOpened() }
    }

    if (settings) {
        Box(Modifier.fillMaxSize()) {
            StarryBackdrop()
            SettingsScreen(state, vm, onBack = { settings = false })
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        StarryBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Night.tabBar) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Icon(t.icon, contentDescription = null) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Night.sun,
                                selectedTextColor = Night.sun,
                                indicatorColor = Night.sun.copy(alpha = 0.16f),
                                unselectedIconColor = Night.muted,
                                unselectedTextColor = Night.muted,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            AnimatedContent(tab, Modifier.padding(padding), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
                when (t) {
                    Tab.TODAY -> TodayScreen(state, vm, onStudy = { startReview = true; tab = Tab.LEARN }, onOpenMove = { tab = Tab.MOVE }, onSettings = { settings = true }, onRedo = { startRedo = true; tab = Tab.LEARN }, onPhrasebook = { startTool = "phrasebook"; tab = Tab.LEARN }, onOpenLife = { tab = Tab.LIFE }, onAPlus = { startAPlus = true; tab = Tab.LIFE }, onTool = { startTool = it; tab = Tab.LEARN }, onLifePage = { startLife = it; tab = Tab.LIFE })
                    Tab.LEARN -> LearnScreen(state, vm, startReview = startReview, onReviewStarted = { startReview = false; startRedo = false; startTool = null }, startRedo = startRedo, startTool = startTool, startDeck = startDeck, onDeckOpened = { startDeck = null })
                    Tab.MOVE -> MoveScreen(state, vm)
                    Tab.LIFE -> com.dudlatitat.nextchapter.ui.life.LifeScreen(state, vm, startAPlus = startAPlus, onStarted = { startAPlus = false }, startPage = startLife, onPageStarted = { startLife = null })
                }
            }
        }
    }
}
