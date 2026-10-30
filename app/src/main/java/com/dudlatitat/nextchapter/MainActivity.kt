package com.dudlatitat.nextchapter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dudlatitat.nextchapter.ui.NextChapterApp
import com.dudlatitat.nextchapter.ui.theme.NextChapterTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { NextChapterTheme { NextChapterApp(vm) } }
    }
}
