package com.dudlatitat.nextchapter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dudlatitat.nextchapter.data.Store
import com.dudlatitat.nextchapter.ui.learn.AddPhraseDialog
import com.dudlatitat.nextchapter.ui.theme.NextChapterTheme

/**
 * Opens over WeChat (or any app): select Chinese text → "Add to Next Chapter" in the text menu,
 * or Share → Next Chapter. Pinyin fills itself in; you add the meaning.
 */
class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = (intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT) ?: "")
            .toString().trim().take(200)
        setContent {
            NextChapterTheme {
                AddPhraseDialog(initialHanzi = text, onDismiss = { finish() }) { hanzi, pinyin, english ->
                    // The same add as everywhere else (the store refreshes the widgets).
                    Store.get(this).edit { s -> com.dudlatitat.nextchapter.data.CardEdits.add(s, hanzi, pinyin, english) }
                    Toast.makeText(this, "Added to My phrases", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
