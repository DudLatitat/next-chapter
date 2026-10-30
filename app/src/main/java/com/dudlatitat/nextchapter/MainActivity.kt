package com.dudlatitat.nextchapter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.dudlatitat.nextchapter.ui.NextChapterApp
import com.dudlatitat.nextchapter.ui.theme.NextChapterTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    /** Set when opened from the reminder or widget with reviews waiting. */
    private val openStudy = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openStudy.value = intent.getBooleanExtra(EXTRA_STUDY, false)
        setContent { NextChapterTheme { NextChapterApp(vm, openStudy.value, onStudyOpened = { openStudy.value = false }) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_STUDY, false)) openStudy.value = true
    }

    companion object {
        const val EXTRA_STUDY = "study"
    }
}
