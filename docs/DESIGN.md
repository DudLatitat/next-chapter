# Next Chapter · 新篇章: redesign brief (design lead)

Written Nov 2026 from a full screenshot test of every screen (phone folded/unfolded and the PC page), app research and two product-test reports. Grounded in ~45 screenshots (cover: Today p1–4, body sheet, Chinese p1–4, review, Ready ladder, Life p1–3, routine, timer, Wins, Move p1–3, Money, Flights, Packing, Settings, Trip & computer, Search, Talk, Dialogues, Tones; inner: Today, Chinese, Life, Move, Tones; PC: Phrases, Review, A+, Sky, IT Lab, Talk, narrow views, ticket), the two product-test reports, `HANDOFF.md` (state at 1.110) and the current kit (`Kit.kt`, `Components.kt`, `Motion.kt`, `Theme.kt`).

What the screenshots say in one line: **the pieces are good, the home is a to-do list.** Today shows "今日三件 0/3", a pink Start, Done / Did the minimum / Skip today, and the Body row. That's four asks and almost no answers. Her clock (6:55 AM) is the hero next to "晚上好, Rudy". Zeros are everywhere (0/18, 0/14, 0 known, 0 new, ¥0). Each tab uses a different header.

---

## 1. Main goal

**Goal statement.** *Next Chapter helps Rudy live his best life over the next three years and arrive in Chengdu ready: able to talk with Yuling's family, employed in IT with the certs to show it, financially safe, healthy and rested, grounded in his faith, and closer to her than ever. It does this by showing him, each day, the one next small step and how far he's already come. It never shows a backlog.*

**Pillars.** Six pillars. Each has one number the app measures and one place it shows. The target is a 3-year milestone, not a daily quota.

| # | Pillar | Measure (already in data, or new) | 3-year target | Where it shows |
|---|---|---|---|---|
| 1 | **Talk with her family** | `Ready.family` rungs (8) + ★ said-for-real count | Rung 8 "the big dinner" before the move; ★ ≥ 300 | Today pillar strip "话", Chinese hero, Road milestones |
| 2 | **Career & certs** | Cert states (`Career`), A+ domain strength, AS classes | A+ → Net+ → Sec+ passed, AS done, IT job in hand | Life hero, Road, Today strip "业" |
| 3 | **Money runway** | `Money.runway` months + savings % of goal | Goal met, ≥12 months of runway at the Chengdu budget | Move hero, Today strip "钱" |
| 4 | **Health & routine** | Weekly routine nights (🌙 per week), wake-on-time days, optional weight trend | 5 of 7 nights a week on average; wake 6:30 | Today strip "身", Life week strip |
| 5 | **Faith** | Prayer days per week; prayer ladder rungs | Daily prayer; key prayers by heart in Chinese | Today 祷 row, Life (Chinese prayers in Chinese tab) |
| 6 | **Us** (the relationship) | Call-planner calls, trips taken, days until next trip, occasions remembered | Every trip planned ≥90 days out; no occasion missed | Today header (her time, next call), Move trip road, Occasions |

Rule: Today shows **at most one number per pillar**. It's always a forward or positive one ("3 of 5 nights", "11 mo runway", "rung 2"). It's never a deficit.

---

## 2. App rules (product)

1. **Today is a slice, never a backlog.** Show at most today's portion: "Review 10" and never "57 waiting". The whole queue only appears after the slice is done, as "Keep going? 10 more".
2. **Never show missed days.** Week dots show done, minimum and rest. A missed past day looks exactly like an unplanned future day (hollow). No red, no "you missed".
3. **One streak only.** 🔥 = practice days with the rest-day rule. The routine's 🌙 becomes a weekly count ("4 nights this week"), not a run. ☀ early starts become a weekly count inside Life only.
4. **No zeros until started.** A counter at 0 is replaced by an invitation: "0/18 checklist" → "18 steps · start with Passport". "0 known" → "Start A+ Core 1". "¥0 ≈ $0" → the field shows a placeholder sample.
5. **One primary action per screen.** It's one filled button (sun, or the pillar color). Everything else is a row, chip or text button. Today's quest rows have one Go total, on the next item.
6. **Offers, not debts.** Copy offers things ("Got 5 minutes? 10 phrases"). It never owes ("10 due", "overdue", "Skip today"). Skipping is a quiet "Not tonight" in a sheet and is never a primary-row button.
7. **Quiet hours are sacred.** During the call (19:00–20:00, `Life.isCallTime`) the app asks nothing: no Now card, no rings, no claims, no milestone pop-ups, no notifications. Today turns into "Call time 💗". After the wind-down time, no Start buttons. Only "Tomorrow: …" and the phrase of the day.
8. **Empty state = one friendly line + one button.** No paragraphs. Setup explanations live behind a "How this works" text link that opens a sheet.
9. **Comebacks are celebrated, not audited.** After 2+ days away, show "Welcome back. Here's an easy 5." (the cap is 5, not 15). Never count the days away.
10. **Folded by default.** A new section ships folded, or as one row. Above the fold on Today: ≤7 items.
11. **Every number has a reason to exist and a tap target.** If tapping a number leads nowhere, remove the number.
12. **The app never depends on Yuling.** Routine seeds, minimums and prompts must be doable alone. Replace "20-min walk + Chinese with Yuling" and "Learn 1 sentence from Yuling" with solo versions. She can be content, never a dependency.
13. **Celebrate at the end, not during.** Payoff panels and the day claim appear after a session. The app never interrupts a session.
14. **Her time is context, his time is the clock.** The big clock is Rudy's local time. Hers sits beside it, smaller, with her sky phase.
15. **One home for each thing.** Each feature lives on exactly one tab. Other tabs may *link* to it with a row, never duplicate its card (fixes readiness ×3, Tonight's job ×2, today's game ×2).
16. **Private by construction.** No personal names, school names, places or money in code. Personal values live in AppState fields entered on the phone.

**Copy tone: do / don't**

| Don't | Do |
|---|---|
| Bad day? 10 questions still counts. | Short on energy? 10 questions is a win. |
| Skip today | Not tonight (inside the sheet) |
| Phone down by 11:30 PM | Wind down at 11:30 · tomorrow starts at 6:30 |
| 57 waiting · 49 to remember | 10 for today · about 5 min |
| Checklist 0/18 | 18 steps · first: passport check |
| Core words 0/293 | Start your first 10 core words |
| You missed 3 days | Welcome back 👋 |
| tap to set ×8 | Add rent first. It's the big one. |

Voice: second person, short, warm, concrete minutes ("about 5 min"). Chinese words get a light touch (加油, 好的). Use at most one emoji per line.

---

## 3. Design rules (every page)

### Look: decision

**Hybrid: frosted night-sea everywhere, plus a terminal *accent* in exactly two places.** The first place is the one-line status strip under each tab header. The second is the IT/A+ world (Life's A+ card and the PC IT Lab).
- Why not full terminal: it reads as work and stress, and Chinese plus pinyin look cramped in mono. Why not frost only: Rudy asked for "modern", and a mono status line gives a crisp "system dashboard" read of vital info without clutter. It also matches his IT identity.
- Alternative B (if Rudy prefers): **Terminal-forward**, with a mono font for all numbers and labels and square 8dp corners. Recommendation: hybrid (A).

### Frost: how it's drawn (cheap)

1. **No real blur on scroll content.** One `FrostCard` modifier draws four things:
   - fill `Color(0xFF1D2752).copy(alpha = 0.62f)` over the star backdrop
   - a top highlight: a 1dp line, `ink @ 14%`, drawn with `drawBehind` on the top edge only
   - border `1.dp`, `Brush.verticalGradient(ink@18% → ink@4%)`
   - a static noise texture: one 128×128 `ImageBitmap` built once at startup (a seeded random alpha of 3–4%) and tiled with `ShaderBrush(ImageShader(..., TileMode.Repeated))`
   
   The cost is one shader. There's no `RenderEffect`.
2. **Backdrop does the "blur".** `StarryBackdrop` gets a pre-blurred soft "nebula" layer: two or three big radial gradients in her sky color, drawn once into a cached `drawWithCache`. Cards over it look frosted because the stars behind them are hidden by the 62% fill.
3. **Real blur only for the tab bar / rail**, and only on Android 12+ (`Modifier.graphicsLayer { renderEffect = BlurEffect(24f, 24f) }` on a copy of the backdrop clipped to the bar). Fallback below 12, or with "Reduce effects" on: solid `Night.tabBar`. Don't use a library (Haze isn't worth a dependency for one bar).
4. **Contrast guard:** body text on frost must reach ≥4.5:1 at the brightest sky phase (dawn). `HerSky` caps backdrop brightness behind text areas at `groundTop` luminance. Add a unit test on the color math.
5. **Setting:** Settings → Look → "Glass effects" (on by default). Off = solid `Night.card`. Store it in prefs `ui`, not AppState.

### Color roles (keep the `Night` palette, enforce the meaning)

| Role | Color | Used for only |
|---|---|---|
| Primary action / Chinese | `sun` FFCF7A | the one filled button, Chinese pillar |
| Her / us / trips | `rose` FF8FB1 | her time, call, trips, occasions |
| Career / A+ / IT | `violet` 8E94F2 | A+, certs, career, terminal accent |
| Done / money healthy | `jade` 4FB3A9 | completed, savings, runway |
| Urgent (rare) | `coral` FF6B6B | hard deadlines ≤3 days only, never for "missed" |
| Faith | new `pearl` E8E3D3 | prayer, Sunday |
| Health | new `mint` 9BE3C0 | body, routine nights |
| Text | `ink` / `muted` | body / secondary |

- The pink Start button on Tonight's job breaks the role table (rose = her). Tonight's job uses the pillar color of its task (violet for A+).
- A pillar color appears as a badge tint, a ring or the button. Never more than two accent colors in one card.

### Type scale (Nunito body, Fraunces display, JetBrains Mono numbers/status)

| Token | Font | Size/line | Use |
|---|---|---|---|
| display | Fraunces 600 | 32/38 | tab header title, the big clock |
| title | Fraunces 600 | 22/28 | sub-page title, hero headline |
| headline | Nunito 700 | 17/22 | card titles |
| body | Nunito 400 | 15/21 | text |
| label | Nunito 600 | 13/18 | row details, chips |
| caption | Nunito 400 | 12/16 | footnotes (min 11) |
| mono | JetBrains Mono 500 | 12/16 | status strip, timers, counts, codes |
| hanzi | HanziText (`Sizes.hanzi`) | 1.3× line | all Chinese ≥ 20sp |

- **Bundle JetBrains Mono (OFL 1.1)** as `res/font/jbmono_medium.ttf` (~100 KB, subset Latin + digits). Credit it in Settings → About. Mono goes in the status strip, countdowns, timers, `HH:MM`, money figures and PC IT Lab. It never goes in sentences or Chinese.
- Tabular numbers everywhere (`fontFeatureSettings = "tnum"` on Nunito where mono isn't used).
- No more Nunito-overline-plus-Fraunces mixes. Overlines (SectionLabel, uppercase) are used only for groups inside a page.

### Spacing & shape
- Use `Space` (4-pt) only. Card padding 16. Gap between cards 12. Gap between groups 24. Gutters come from `screenGutter()` (16 cover / 24 inner).
- Corners: cards 20, rows inside ListGroup 0 (with 1dp dividers at `outline@50%`), chips/pills full, buttons 16.

### Page anatomy (one standard)
1. **Tab page header (`TabHeader`, new):** the title in display (e.g. "Today", "中文 Chinese", "Life", "Move") on the left and 🔍 ⚙ icons on the right. Below it the **status strip**, one mono line made of ≤3 facts that are each tappable (e.g. `▸ 41d to 成都 · rung 2/8 · 11mo runway`). Then the HeroCard. Every tab has one; Life and Chinese currently have none.
2. **Sub-page header (`SubHeader`, existing, standardized):** ← back, then the title (Fraunces title, e.g. "Money · 人民币"), and one optional trailing action icon *with a label* ("+ Add"). Don't repeat the title as an overline, and don't put a footer.
3. **Body order:** Hero (one) → primary list (ListGroup) → folded extras (Collapsible) → nothing else. There's no version footer except at the bottom of Settings.
4. **Sheets** for quick edits (≤1 field group). **Sub-pages** for anything with a list.

### Width (unfolded, `isWide()`)
- Reading content is capped at `readable(640)`. Today's content area is capped at 1040 and centered.
- **Two-pane rule:** use `TwoPanes` only when both panes have content *of similar height*. The left pane holds the primary flow and the right pane the glance/support. When the right pane would be <50% of the left's height, use one centered column at 720 instead.
- **Lists of items ≥ 6** (decks, scenes, tools, checklist groups) become an adaptive grid (`GridCells.Adaptive(260.dp)`). Never stretch a single row to 884dp.
- All ~15 stretched sub-screens get `readable()` or a grid in build 1 (list in §6).
- Content that's vertically short is centered in the available height (review card, Tones, Talk scene), with the actions pinned to the bottom third. This fixes the "void below".

### Components (use these, nothing ad hoc)
- **Existing (keep):** `HeroCard`, `ListGroup`/`ListRow`, `IconBadge`, `SegmentedChips`, `StatTile`, `GoChip`, `Pill`, `ProgressRing`, `Bar`, `Collapsible`, `SubHeader`, `TwoPanes`, `HanziText`, `PayoffPanel`, `CountUp`.
- **Retire:** `ScreenTitle` (use `TabHeader`/`SubHeader`), `GlassCard` (becomes `FrostCard`), and bare `TextButton`s for actions.
- **New:**
  - `FrostCard` (above)
  - `TabHeader` + `StatusStrip` (mono, tappable segments)
  - `PillarStrip` (six mini rings/glyphs, one row)
  - `DayTimeline` (horizontal now-line with work/home/call/wind-down blocks)
  - `EmptyState(line, button)`
  - `PrimaryButton` (filled, full width on cover, 280dp max on inner)
  - `SecondaryButton` (outlined)
  - `ActionRow` (a ListRow with a trailing labelled chip instead of a chevron when it *does* something)
  - `RoadStrip` (today → milestones → move)

### Buttons & taps
- Every tap target is ≥48dp. Buttons say what they do, with a verb and an object ("Start test", "Add $100"). Don't use bare "Go" except as GoChip inside a quest row.
- Chevron › = navigates. A labelled chip = does something. "+" icons always carry a word ("+ Flight").
- Never use long-press as the only way to do something. Delete goes through a row's ⋯ menu or a swipe with undo.
- Destructive actions take a confirm sheet or 4-second undo, never a dialog chain.

### Motion
- Use `Motion.kt` only: `push`/`pop` for sub-pages, `fadeThrough` for tabs, and `pressScale` on every tappable card.
- Rings fill with a 600ms Emph curve on appear. CountUp is used for numbers that changed since the last open.
- The breathing glow goes on the *one* hero element only. Stars stay at 10 fps.
- "Reduce motion" (system animator scale 0, or the Settings toggle) disables count-ups, breathing and sparks.

### Empty states
- Use the `EmptyState` composable: an IconBadge (the pillar char) + one line + one button. Examples:
  - Money: "What does a month in Chengdu cost? Start with rent." [Add rent]
  - Body graph: "Your first weigh-in starts the line." [Log weight]
  - Search no results: "Nothing for 'xyz' yet." [Look it up in the dictionary] / [Add as a phrase]
  - Talk scene with 0 lines: hide it.

### Numbers & counters
- At most **three numbers per card**. Numbers use mono, and big numbers use display only in the hero.
- Prefer progress *toward* ("rung 2 of 8") over totals ("594 phrases").
- Never show a denominator above 30 on Today or a hero. Big pools show as "% of deck" inside the deck page only.
- Money shows both currencies only where the screen is about money. Elsewhere show one, the one the user picked.

### Icons (character badges)
- `IconBadge` with one Chinese character (two max, at 0.8× size) in the pillar color. Keep one registry in `ui/components/Glyphs.kt` so that no two features share a character. Today 考/试 and 话/对话 are near-collisions.
- In lists, the badge sits in a fixed 40dp column with 12dp to the title. The title text never shares a Box with the badge (fixes the Dialogues/Stories overlap).
- Emoji only in copy and celebration, never as an icon.

### PC page alignment
- Same tokens as CSS variables: the `Night` colors, Nunito/Fraunces/JetBrains Mono bundled in `assets/desktop/fonts/`, never from Google. `FrostCard` = `background: rgba(29,39,82,.62); border:1px solid rgba(238,241,255,.12); box-shadow: inset 0 1px 0 rgba(238,241,255,.14)`. `backdrop-filter: blur(16px)` is allowed on the PC since the browser does it cheaply. There's a solid fallback via `@supports`.
- Same header pattern: the app mark, nav, and a mono status strip on the right.
- Terminal look is used in IT Lab only.

---

## 4. Information architecture

**Keep 4 tabs, renamed by question.** Rudy has learned them, and four fits the cover screen. The change is in *what lives where*.

| Tab | Question | Owns |
|---|---|---|
| **Today** 今天 | What matters now? | Header (both clocks, next call), Now card, Today's three, pillar strip, day timeline, Road strip, phrase of the day |
| **Chinese** 中文 | Practice | Review, situations & tools, decks, Ready ladder (sole home), tones, How it's going |
| **Life** 生活 | Getting better | Weekly routine (sole home of Tonight's job editor), A+ & exam plan, career, faith, body, wins row |
| **Move** 搬家 | The plan | Road (3-year), trips & call planner, money & runway, paperwork, packing, occasions |

**Removed**
- The Tonight's job *card* from Today's default view. It becomes the third quest row, or the Now card only in its window, so it's no longer shown twice.
- The Ready ladder card on Today (unfolded right pane). Its home is Chinese; Today shows rung N in the pillar strip.
- The Wins page. It becomes a row in Life ("4 of 22 milestones · next: Unstoppable") opening a sheet. The data is kept.
- "Last 12 weeks" grid as a default. It's folded inside How it's going.
- Developer tools (Voice log export/clear) move to Settings → About → "For developers" (folded). The data is kept.
- The calories quick-add from Today. Body moves to Life; Today keeps only the weight trend in the pillar strip, if Rudy logs.
- The Price tracker as a separate card. It merges into Fare alerts as "Fares you've seen".

**Merged**
- Dialogues (听对话) → Talk as "Listen mode" on each scene (same scenes, no answer step). Content data is kept.
- Grammar search → global Search. Situation chips go from 4 down to 3: *Family & faith*, *Talk & text*, *Play & drill*. Read & write folds into Play & drill.
- The flight price target moves out of the savings dialog into Flights.
- Decks: started decks are shown and the rest folded under "More decks (12)".

**Added** (picked for the goal)
1. **Call planner** (Us pillar, Move → Trips & calls, and Today's header line "Next call 7:00 pm · her 11:00 am"). A DST-aware window list for the week using `Life.herTime`. It's pure math with tests, with no input from her.
2. **Road: 3-year strip** (all pillars). Today → milestones (A+ exam, AS done, Net+, trips, savings 50%, visa start, move) on one line. Data: auto from existing dates + `AppState.milestonesRoad` (custom, default empty).
3. **Paperwork back-planning** (Move). It works back from `moveDate` with standard lead times (passport validity, visa type research, document legalization, medical check, police certificate). Dates are computed and shown as "start by". Seeded generic steps, editable. It goes into the existing checklist "Documents" group with `startBy` offsets.
4. **Exam plan** (Life → A+). From the exam date and the domain weakness, it works out cards per day and a practice test each Sunday, with a "ready" estimate. It reuses `APlus.weakest` and `PracticeTest`.
5. **Occasions** (Us). Spring Festival, Mid-Autumn, Qixi, her birthday, family birthdays (MonthDay, Feb 29 safe; lunar via ICU `ChineseCalendar`). It reminds 14/3 days before with a matching phrase card and gift-idea note. It reuses the kept `people`/`notes` data.

**Deferred:** trip-mode arrival essentials (Trip mode already covers most of it), the Chengdu monthly cost template (do it as a one-tap preset inside Money's empty state in build 4 if time allows), the weekly "best moment" journal (it borders on homework; revisit), the Chinese Mass responses deck (needs a named source like the prayers; ask Rudy), and the Ctrl+K palette on the phone.

---

## 5. Screen specs

### Today (cover, 373dp), top → bottom

```
┌ TabHeader ─────────────────────────────┐
│ 晚上好, Rudy                    🔍  ⚙  │  label, muted
│ 10:55 PM            (display, ink)     │  HIS time
│ Yuling 1:55 PM · afternoon ☀ · Thu     │  rose, her sky phase; tap → call planner
│ ▸ next call tmrw 7:00 pm               │  mono status strip (segments tappable)
├ NOW card (HeroCard, one) ──────────────┤  changes by time of day (below)
│ [badge] title                          │
│ one line of why/first step             │
│ [ PrimaryButton ]                      │
├ Today's three (FrostCard) ─────────────┤
│ 字 Phrases 4/10 ◔         [Go]         │  only the next item has Go
│ 玩 Her family · 3 min       ›          │
│ 考 A+ · 5 cards             ›          │
│ 祷 Prayer                   🙏         │  quiet row
│ 🔥 25 (top-right of card)              │
├ PillarStrip (one row, 6 glyphs) ───────┤
│ 话 rung2 · 业 A+ 40% · 钱 11mo ·        │
│ 身 4/7 · 祷 5/7 · 心 41d                │  each → its home
├ Road strip (RoadStrip) ────────────────┤
│ Now ●──A+ Mar──✈ Jan 5──AS Jun──…──成都 │  tap → Move › Road
├ Phrase of the day (one slim row, ▶) ───┤
└ ▸ More today (folded): Coming up,      │
    Monday recap, Sunday review          ┘
```

**Above the fold on the cover:** header, Now card, Today's three. That's ≤7 items.

**Now card by time** (`Life` parts, using Rudy's zone):

| Window | Now card |
|---|---|
| 3:00–wake+30 (early) | "Early start ☀. Morning prayer + 1 A+ card" [Pray] |
| Morning → work | Phrase of the day with ▶ and "Say it in the car" [Drive mode] |
| At work (Places) | Quiet: "At work till ~5:00. Lunch break? 5 phrases" [Review 5] |
| Heading home | "Heading home 🚗" [Drive mode] |
| Home → 19:00 | Tonight's job (pillar color) [Start …] + "Did the minimum" as a text link, with "Not tonight" in a ⋯ sheet |
| **19:00–20:00 call** | **Call card**: "Call time 💗 · her morning", tonight's line (`TonightLine`) if starred. No buttons except ▶ to hear the line. Today's three and the pillar strip are hidden. |
| 20:00–wind-down | Done → "Nice evening. Tomorrow: Wed A+ at 6:00" · not done → "Still time for 10 minutes" [Start short] |
| After wind-down | "Wind down 🌙 · up at 6:30" + phrase of the day. Quest Go buttons are hidden. |
| All three done | DayClaim once (outside call), then "All done ✓" with the next pillar suggestion |

**Unfolded (884dp):** content area 1040, centered. Two panes, 5:4:
- **Left:** TabHeader with status strip, Now card, Today's three.
- **Right:** a `DayTimeline` card first. It's a horizontal 24h bar from wake to wind-down: blocks for work (violet), home (mint), call (rose) and wind-down, with a now-line, her hours as a ghost row beneath (sleep/work), and a tap → call planner. Then the PillarStrip as a 3×2 grid of `StatTile`s with mini rings. Then the RoadStrip.
- Below both panes: phrase of the day and More today.
- The panes balance; there's no Ready ladder here.

### Chinese
- TabHeader "中文 Chinese", status `▸ 🔥25 · ★ 2 said for real · rung 2/8`, search pill below.
- Hero: "10 for today · about 5 min" [▶ Practice]. When done: "Keep going? 10 more" / today's game.
- Ready ladder (sole home): 8 rungs as a stepped bar. The current rung is expanded with its 2 pieces as chips.
- Situations: `SegmentedChips` (3) → a ListGroup of tools with status ("best 9/16", "played today").
- Decks: started ones as tiles (grid on inner) and "More decks (12)" folded.
- How it's going (folded): a week strip (days, reviews, minutes), then tones, then the 12-week grid. Numbers ≤3 per card.
- **Unfolded:** left = hero + situations; right = Ready + decks grid.

### Tool screen template (Talk, Tones, Build, Callouts, Number ears, Texts, Stories, Grammar, Writing, Drive)
1. SubHeader: ← title · 中文 and an optional "?" that opens a "How this works" sheet (all intro paragraphs move here).
2. Setup (if any): one row of `SegmentedChips` (mode/level).
3. Stage: the content centered vertically in the remaining height, `readable(640)`.
4. Action zone: anchored to the bottom (thumb zone): one PrimaryButton, with secondary actions as labelled chips (🔊 Hear it, Slow, Show English).
5. End: `SessionPayoff`, then "Next →" (LocalPlanNav).
6. Lists in a tool (scenes, stories) use a ListGroup with a 40dp badge column, and a grid when wide.

### Life
- TabHeader "Life", status `▸ 🌙 4 this week · A+ exam 56d · 祷 5/7`.
- Hero: Tonight's job (editable home) if it's within the window, otherwise **This week** (the dots strip without missed marks + "4 nights · keep it simple").
- **A+ & exam plan** (ListGroup, violet; mono numbers): "Ready ~62% · 56 days" with ProgressRing [Study 10], then Practice test Sunday, domains folded, Study timer as an ActionRow ("Timer · A+ ▶"). The open state shows the chips on one row and "Stop"/"+15 min" in a second row, which fixes the wrap.
- Career path row, Body row ("weight · trend this week", tap → BodySheet), Faith row (prayer ladder + Sunday), Wins row.
- Unfolded: left = hero + A+, right = week + career + body + faith.

### Move
- TabHeader "搬家 Move", status `▸ ✈ 41d · 成都 2y 2m · 11mo runway`.
- Hero `Countdown`: labelled **"Next trip · 下次见面"** over "41 days to 成都 ✈" (fixes the 搬到中国 mislabel). The RoadStrip in the hero has two ends: trip and move.
- Doors (3 → 4): 机 Trips & calls · 钱 Money · 证 Paperwork · 包 Packing.
- Savings card: ring % + "$X of $Y goal" + quick-add Pills. Runway lanterns show only if the budget is set; otherwise EmptyState "What does a month there cost?" [Use a Chengdu starter budget] [Enter my own].
- Checklist groups: no "0/5". Show "Next: passport check · 5 steps". The delete affordance is ⋯.
- Occasions (folded row): "Mid-Autumn in 12 days · 中秋快乐" → sheet.
- Unfolded: grid of groups (it already works).

### Settings
- The front page stays (five ListRows with current values). Add **Look** (glass effects, reduce motion) and **About** (version, credits incl. JetBrains Mono, "For developers": voice log).
- Fare alerts setup: one line + [Set up] → a stepper sheet (3 steps, with links that open in the browser).
- Footer appears only on the front page.

### Search
- Opened from any 🔍. The tab highlight stays on the tab it came from.
- Empty: "Find any phrase, word or line" + 4 sample chips (keep).
- Typing: a spinner row "Searching…" (no blank flash).
- No results: EmptyState "Nothing for 'xyz' in your phrases." [Search the dictionary] [Save as a new phrase].
- Results are grouped: Your phrases · Talk & texts · Dictionary, ≤5 each, with "More" per group.

### PC page
- **Nav:** `新篇章` mark · **Home** · Practice (Review + A+ with a shared sidebar) · Phrases · Talk · IT Lab · (Sky moves to the Home backdrop + a toggle). The mono status strip sits on the right (`🔥12 · 5 to review · A+ 56d`). Ctrl+K palette: jump to any tab/lab/scene, a `>_` easter egg.
- **Home view (new, default):**
  - Left 2/3: today's three as wide cards with keyboard hints (`R` review, `G` game, `A` A+); the Road strip; the week planner (routine week, editable with the keyboard).
  - Right 1/3: her time + next call, savings/runway, exam countdown + weakest domain.
  - The background is the Sky constellation, dimmed.
- Phrases: a table view toggle (hanzi · pinyin · English · deck · status) with filters. The current grid stays as "Cards".
- IT Lab: the order is fixed by a static `LAB_ORDER` array; cards render once all scripts load (fixes the random order).
- Talk: hide 0-line scenes. Game mode moves out of "Texting" into its own group "Play". Custom-styled controls replace the native checkboxes.
- Remote-off state: an EmptyState with steps ("On your phone: Settings › Trip & computer › Use on my computer"), not a red line.

---

## 6. Build plan

Each build ships alone. Sizes follow CLAUDE.md.

### Build 1: Foundations + fixes (large; ~1.111)
- **Files:** `theme/Theme.kt` (pearl, mint, Type tokens, mono font family), `res/font/jbmono_medium.ttf` + OFL notice, `components/Kit.kt` (FrostCard, TabHeader, StatusStrip, EmptyState, PrimaryButton, SecondaryButton, ActionRow), `components/Glyphs.kt`, `Components.kt` (GlassCard → FrostCard delegate; ScreenTitle deprecated).
- **Bugs:**
  1. Study timer row wrap: `ui/life/LifeScreen.kt` StudyTimer. Chips get their own row, Stop/+15 a second row (`FlowRow`).
  2. PC lab order: `assets/desktop/index.html` ~1270. Use a static order array + render after all scripts load.
  3. Wake default: `Models.kt:205` `wakeMinutes` 7:30 → 6:30 for *new* installs only. Existing saved values stay (the default only applies to missing keys). Test: a fresh `AppState().wakeMinutes == 390`.
  4. School name: remove the school name from `Seed.kt`, `Career.kt` → "your college"; school name becomes an optional AppState field `schoolName = ""`. The Seed merge must not rename saved tasks, so the display uses a `{school}` template.
  5. Move hero label → "Next trip · 下次见面".
  6. Search: add the no-results and searching states, and keep the origin tab highlighted.
  7. Badge collisions: `DialoguesScreen.kt:75`, `MoreTools.kt:267` get a fixed 40dp badge column.
  8. Routine seeds: `Routine.kt:43,45` get solo minimums (seed change only for unedited days).
  9. Routine editor: verify tap targets (day chips 48dp, Save full width) on the screenshot.
- **Width pass:** `readable()`/grid on Tones, Talk list, Dialogues, Stories, Grammar, Build, Callouts, Drive, A+ review, Settings, Flights, Money, Packing, Career, Wins.
- **Data:** `schoolName: String = ""`.
- **Risk:** low. The font adds ~100 KB. FrostCard's noise shader costs per card on low-end devices, but the Fold is fine and there's a solid fallback.

### Build 2: Today rebuilt (large)
- **Files:** `ui/today/TodayScreen.kt`, `Header`, `TodayThree.kt`, new `NowCard` logic in `data/NowPlan.kt` (pure, `NowPlanTest`: every window incl. DST change days and call hour), `PillarStrip`, `DayTimeline`, `data/Pillars.kt` (`PillarsTest`).
- Remove the Ready ladder and the duplicate Tonight's job from Today. Move Body to Life.
- **Data:** none (reads existing).
- **Risk:** medium. Time windows are easy to get wrong, so cover them with tests. Widget behavior is unchanged.

### Build 3: Chinese + Life cleanup (medium-large)
- TabHeaders and status strips. Situations 4 → 3. Dialogues become Talk's Listen mode (the screen stays reachable from the scene). Decks folded. How it's going folded.
- Life: Body row, Faith row, Wins as a row/sheet, exam plan (`data/ExamPlan.kt`, `ExamPlanTest`).
- Streak rules: 🌙 becomes a weekly count (display only; `routineChecks` untouched). Comeback cap 15 → 5 (`DayPlan.sessionLimit`, test updated).
- **Data:** none.
- **Risk:** removing visible counters may surprise Rudy, so call it out.

### Build 4: Move: Road, paperwork, calls, money (large)
- `data/Road.kt` (milestones from dates; `RoadTest`), `data/Paperwork.kt` (back-planning offsets; `PaperworkTest`), `data/Calls.kt` (DST-aware windows over the next 7 days with `America/Los_Angeles`/phone zone vs `Asia/Shanghai`; `CallsTest` incl. the March/November DST weeks).
- UI: RoadStrip on Today + Move, a Paperwork door, Trips & calls, a Money empty state + Chengdu starter budget preset (generic numbers, user-confirmed), Price tracker merged into Fare alerts.
- **Data:**
  - `roadMilestones: List<RoadMilestone> = emptyList()`
  - `ChecklistItem.startByDays: Int? = null`
  - `callPrefs: CallPrefs = CallPrefs()` (his free hours default 19–20, her default 9–23)
- **Risk:** the checklist item gets a new field (nullable default, safe). The budget preset must not overwrite user values.

### Build 5: Occasions + PC Home (medium-large)
- `data/Occasions.kt` (MonthDay + ICU ChineseCalendar for the lunar festivals; `OccasionsTest` incl. Feb 29 and lunar year boundaries). It reuses `people`. `notify/Occasions.kt` fires silently during call time.
- PC: Home view, Practice merge, nav, status strip, CSS tokens, bundled fonts in assets, table view, Ctrl+K, Talk fixes, remote-off empty state.
- **Data:** `occasions: List<Occasion> = emptyList()` (custom ones only; the festivals are computed).
- **Risk:** ICU lunar conversion on Android needs the API 24+ `android.icu`, which is fine. The PC page is untested by CI, so Rudy checks it in a browser.

### Build 6 (optional): Frost polish
- Blurred tab bar/rail on Android 12+, a sky-tinted nebula layer, reduce-motion settings, and a contrast unit test.
- **Risk:** RenderEffect during fold transitions. The toggle is the fallback.

---

## 7. Open questions for Rudy

1. **Look: hybrid (frost + mono status strip) or terminal-forward?** *Recommend hybrid.* It's calm for daily use and keeps the terminal vibe for IT.
2. **Big clock: your time or hers?** *Recommend yours big, hers on the line below in rose.* The screenshots show 6:55 AM next to "晚上好", which confuses.
3. **Body on Today: keep the weight pill, or move Body fully to Life?** *Recommend Life, with Today showing only the weekly trend in the pillar strip.* Calorie logging felt like homework on the home page.
4. **Routine 🌙: keep "N nights in a row" or switch to "N this week"?** *Recommend "this week".* There's one streak in the app (🔥), and weekly counts forgive.
5. **Dialogues: merge into Talk as Listen mode?** *Recommend yes.* There's no content loss, one fewer door, and no more name clash.
6. **Chinese Mass responses / occasions phrases: which source may I use for church texts?** *Recommend the same dominicweb.eu page for any prayer text. Festival greetings come from dictionaries, flagged if unsure.*
