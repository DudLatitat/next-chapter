package com.dudlatitat.nextchapter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dudlatitat.nextchapter.data.Card
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
                    Store.get(this).edit { s ->
                        s.copy(cards = s.cards + Card(
                            id = "mine:${System.currentTimeMillis()}", hanzi = hanzi.trim(), pinyin = pinyin.trim(),
                            english = english.trim(), deck = "mine", custom = true,
                        ))
                    }
                    com.dudlatitat.nextchapter.widget.PhraseWidget.refresh(this, Store.get(this).state.value)
                    Toast.makeText(this, "Added to My phrases", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
