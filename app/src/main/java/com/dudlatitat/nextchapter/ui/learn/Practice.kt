package com.dudlatitat.nextchapter.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dudlatitat.nextchapter.data.AppState
import com.dudlatitat.nextchapter.data.DayPlan
import com.dudlatitat.nextchapter.ui.components.pressClick
import com.dudlatitat.nextchapter.ui.components.pressClickable
import com.dudlatitat.nextchapter.ui.components.pressScale
import com.dudlatitat.nextchapter.ui.theme.Night

/**
 * The Chinese tab's practice tools, grouped into four situations instead of a wall of 17 tiles.
 * A situation opens a sheet with its tools, each with one line saying what it's for.
 */
object PracticeTools {
    data class Tool(val id: String, val zh: String, val name: String, val blurb: String)
    data class Situation(val id: String, val zh: String, val name: String, val blurb: String, val color: Color, val tools: List<Tool>)

    private fun t(id: String, zh: String, name: String, blurb: String) = Tool(id, zh, name, blurb)

    val situations = listOf(
        Situation(
            "family", "家人", "Family & faith", "Dinner, her mom, manners, prayer", Night.rose, listOf(
                t("dinner", "对话", "Talk", "Act out scenes: dinner, a taxi, her mom's call"),
                t("family", "家谱", "Her family", "Who's who, and how to greet them"),
                t("culture", "礼节", "Etiquette", "Manners at her family's table"),
                t("dialogues", "听对话", "Dialogues", "Listen to everyday conversations"),
                t("prayer", "祷告", "Prayer", "Learn a prayer by heart, rung by rung"),
            ),
        ),
        Situation(
            "texting", "微信", "Texting", "Chats, typing, how do I say…", Night.jade, listOf(
                t("texts", "微信", "Texts", "Reply to WeChat chats"),
                t("sprint", "打字", "Typing sprint", "Type pinyin fast"),
                t("lookup", "查", "Look up", "How do I say…? English → Chinese"),
                t("build", "造句", "Build", "Make your own sentences"),
            ),
        ),
        Situation(
            "play", "玩", "Play & listen", "Callouts, prices, tones, the car", Night.sun, listOf(
                t("callouts", "喊话", "Callouts", "Game callouts, said out loud"),
                t("numbers", "听价格", "Number ears", "Hear prices and times"),
                t("tones", "声调", "Tones", "Hear and say the four tones"),
                t("drive", "开车", "Drive", "Hands-free practice for the car"),
            ),
        ),
        Situation(
            "read", "读写", "Read & write", "Stories, grammar, characters", Night.violet, listOf(
                t("stories", "故事", "Stories", "Short reads, tap any word"),
                t("grammar", "语法", "Grammar", "Patterns in quick quizzes"),
                t("writing", "写字", "Writing", "Write characters stroke by stroke"),
                t("phrasebook", "救急", "Phrasebook", "Emergency phrases to show or play"),
            ),
        ),
    )

    fun tool(id: String): Tool? = situations.flatMap { it.tools }.firstOrNull { it.id == id }
}

/** "Next up": today's game if you haven't played yet, else the next readiness piece. */
@Composable
fun SuggestedNext(state: AppState, onTool: (String) -> Unit, onDeck: (String) -> Unit) {
    val day = com.dudlatitat.nextchapter.data.today()
    val play = androidx.compose.runtime.remember(state, day) { DayPlan.today(state, day) }.items.first { it.kind == DayPlan.Kind.PLAY }
    val game: String? = if (play.done) null else play.tool
    val piece = androidx.compose.runtime.remember(state) { com.dudlatitat.nextchapter.data.Ready.family(state) }.next?.missing?.firstOrNull { it.tool != null }
    val pieceTool: String? = piece?.tool
    val (label, go) = when {
        game != null -> {
            val t = PracticeTools.tool(game)
            "Today's game · ${t?.zh ?: ""} ${t?.name ?: game}" to { onTool(game) }
        }
        piece != null && pieceTool != null -> {
            "Next for her family · ${piece.label}" to {
                when {
                    pieceTool.startsWith("deck:") -> onDeck(pieceTool.removePrefix("deck:"))
                    pieceTool.startsWith("talk:") -> onTool("dinner")
                    pieceTool.startsWith("texts:") -> onTool("texts")
                    else -> onTool(pieceTool)
                }
            }
        }
        else -> return
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).pressClick(onClick = go).clip(RoundedCornerShape(18.dp))
            .background(Night.rose.copy(alpha = 0.22f)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.dudlatitat.nextchapter.ui.components.IconBadge("玩", Night.rose, 36.dp, filled = true)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = Night.ink, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("›", style = MaterialTheme.typography.titleLarge, color = Night.rose)
    }
}

/** Four big situation tiles, 2 × 2 (unfolded they sit in the left pane, so still 2 across). */
@Composable
fun SituationTiles(onOpen: (PracticeTools.Situation) -> Unit) {
    val cols = 2
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PracticeTools.situations.chunked(cols).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { s ->
                    val shape = RoundedCornerShape(20.dp)
                    val press = com.dudlatitat.nextchapter.ui.components.rememberPress()
                    Column(
                        Modifier.weight(1f).then(Modifier.pressScale(press)).clip(shape)
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(s.color.copy(alpha = 0.22f), Night.card.copy(alpha = 0.9f))))
                            .border(1.dp, s.color.copy(alpha = 0.25f), shape)
                            .pressClickable(press) { onOpen(s) }
                            .padding(14.dp),
                    ) {
                        Text(s.zh, style = MaterialTheme.typography.headlineSmall, color = s.color, maxLines = 1)
                        Text(s.name, style = MaterialTheme.typography.titleSmall, color = Night.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(s.blurb, style = MaterialTheme.typography.bodySmall, color = Night.muted, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A situation's tools, one big row each. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SituationSheet(s: PracticeTools.Situation, onDismiss: () -> Unit, onTool: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Night.ground1) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${s.zh} · ${s.name}", style = MaterialTheme.typography.titleLarge, color = s.color)
            Spacer(Modifier.height(4.dp))
            s.tools.forEach { t ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Night.card.copy(alpha = 0.85f))
                        .pressClickable(com.dudlatitat.nextchapter.ui.components.rememberPress()) { onDismiss(); onTool(t.id) }.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(t.zh, style = MaterialTheme.typography.titleLarge, color = s.color, modifier = Modifier.width(76.dp), maxLines = 1)
                    Column(Modifier.weight(1f)) {
                        Text(t.name, style = MaterialTheme.typography.titleSmall, color = Night.ink)
                        Text(t.blurb, style = MaterialTheme.typography.bodySmall, color = Night.muted)
                    }
                    Text("›", style = MaterialTheme.typography.titleLarge, color = s.color)
                }
            }
        }
    }
}


/** What a tool's row says under its name: your best, or that you played it today. */
fun toolStatus(state: AppState, id: String, day: Long): String? {
    val today = if (state.played[id] == day) "played today ✓" else null
    val best = when (id) {
        "callouts" -> state.calloutBest.takeIf { it > 0 }?.let { "best $it" }
        "numbers" -> state.numbersBest.takeIf { it > 0 }?.let { "best $it" }
        "sprint" -> state.sprintBest.takeIf { it > 0 }?.let { "best $it/min" }
        "dinner" -> state.dinnerBest.size.takeIf { it > 0 }?.let { "$it scenes played" }
        "texts" -> state.textBest.size.takeIf { it > 0 }?.let { "$it chats done" }
        "prayer" -> state.prayerSteps.values.maxOrNull()?.takeIf { it > 0 }?.let { "rung $it" }
        "family" -> state.familyNames.size.takeIf { it > 0 }?.let { "$it names saved" }
        else -> null
    }
    return listOfNotNull(today, best).joinToString(" · ").ifEmpty { null }
}

/**
 * Practice, by situation: a row of chips (家人 · 微信 · 玩 · 读写) and that situation's tools
 * listed right under it. One tap to switch, no sheet; it remembers which one you had open.
 */
@Composable
fun SituationPanel(state: AppState, onTool: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { com.dudlatitat.nextchapter.ui.components.uiPrefs(context) }
    var pick by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(prefs.getInt("learn.situation", 0).coerceIn(0, PracticeTools.situations.lastIndex)) }
    val day = com.dudlatitat.nextchapter.data.today()
    Column {
        com.dudlatitat.nextchapter.ui.components.SegmentedChips(
            PracticeTools.situations.map { "${it.zh} ${it.name.substringBefore(" &")}" to it.color }, pick,
            onSelect = { pick = it; prefs.edit().putInt("learn.situation", it).apply() },
        )
        Spacer(Modifier.height(10.dp))
        val sit = PracticeTools.situations[pick]
        androidx.compose.animation.AnimatedContent(sit, transitionSpec = {
            androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220, delayMillis = 60)) togetherWith androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120))
        }, label = "situation") { s ->
            com.dudlatitat.nextchapter.ui.components.ListGroup {
                s.tools.forEachIndexed { i, t ->
                    val status = toolStatus(state, t.id, day)
                    com.dudlatitat.nextchapter.ui.components.ListRow(
                        t.name, subtitle = status ?: t.blurb, divider = i < s.tools.lastIndex,
                        leading = { com.dudlatitat.nextchapter.ui.components.IconBadge(t.zh, s.color, 44.dp) },
                        onClick = { onTool(t.id) },
                    )
                }
            }
        }
    }
}
