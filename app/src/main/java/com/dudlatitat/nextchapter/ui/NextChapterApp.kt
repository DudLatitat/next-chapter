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
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.dudlatitat.nextchapter.ui.progress.ProgressScreen
import com.dudlatitat.nextchapter.ui.theme.Night
import com.dudlatitat.nextchapter.ui.theme.StarryBackdrop
import com.dudlatitat.nextchapter.ui.today.TodayScreen

enum class Tab(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Rounded.Home),
    LEARN("Learn", Icons.Rounded.Star),
    MOVE("Move", Icons.Rounded.Place),
    PROGRESS("Progress", Icons.Rounded.CheckCircle),
}

@Composable
fun NextChapterApp(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    // Opening a review straight from Today.
    var startReview by rememberSaveable { mutableStateOf(false) }

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
                    Tab.TODAY -> TodayScreen(state, vm, onStudy = { startReview = true; tab = Tab.LEARN }, onOpenMove = { tab = Tab.MOVE })
                    Tab.LEARN -> LearnScreen(state, vm, startReview = startReview, onReviewStarted = { startReview = false })
                    Tab.MOVE -> MoveScreen(state, vm)
                    Tab.PROGRESS -> ProgressScreen(state)
                }
            }
        }
    }
}
