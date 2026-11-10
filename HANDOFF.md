# Handoff · state of Next Chapter (Nov 9, 2026)

Read `CLAUDE.md` first: it has Rudy's rules. This file is the current state: what's built, how it fits together, what's broken or open, and what was being decided when the last session ended.

Latest build: **1.48** (1.43–1.48: Identity V deck for Cryptic Notes, Gaming together 48 cards, Everyday +24, new Sweet talk deck; earlier: Life tab, homey Today, study timer, practice tests) (GitHub release `latest`). Builds 1.32+ are on the session branch, not yet merged to `main`. All of it was compiled by CI only; Rudy tests every APK on his Galaxy Z Fold 8 Ultra (Android 17).

## Where things are

```
app/src/main/java/com/dudlatitat/nextchapter/
  MainActivity, MainViewModel     one ViewModel, all actions go through Store.edit
  QuickAddActivity                "Add to Next Chapter" from WeChat's text menu / Share
  Speaker                         plays assets/voice/<md5(hanzi)>.mp3, else the phone's TTS
  data/
    Models.kt                     AppState (everything saved) + Card, Trip, FareAlert, ...
    Store.kt                      one JSON file (filesDir/state.json), singleton Store.get(context)
    Seed.kt / Vocab.kt            starter phrases (VERSION bump merges new ones in), 293 core words
    Dialogues, Grammar, Stories, Culture, Phrasebook   content (Chinese + pinyin + English)
    Srs.kt                        SM-2 style scheduler, queue(), streak()
    Plan.kt / Stats.kt / Milestones.kt   countdowns, money, weekly recap, tone stats, milestones
    Pinyin.kt                     syllable splitting, tones, Han→pinyin (ICU), tone-quiz chars
  speech/
    Audio.kt                      Recorder (AudioRecord 16 kHz + NoiseSuppressor), MP3 asset decode, Clip
    Pitch.kt                      YIN pitch tracking, tone templates/classifier, calibration, DTW melody score
    Offline.kt                    Vosk on-phone recognizer (model in assets/vosk-cn, unpacked to filesDir once)
    Judge.kt, PhraseToneModel.kt  tone sandhi, per-character timing, phrase tone model (generated)
    Coach.kt                      one speaking check: Vosk sounds + timing, pitch tones, melody
  drive/DriveService.kt           Drive mode foreground audio service
  notify/Reminder.kt              daily reminder (AlarmManager) + boot receiver
  notify/FareWatch.kt             fare alerts: SerpApi Google Flights, WorkManager every 12 h
  widget/PhraseWidget.kt          home-screen widget (RemoteViews): countdown + phrase
  widget/WeekWidget.kt            home-screen widget: the weekly routine's dots + tonight's job
  ui/                             Compose screens: today/, learn/ (the Chinese tab), life/, move/, progress/ (sections used by Chinese and Life), settings/, theme/
tools/
  make_voice.py                   records every built-in Chinese line with edge-tts (CI)
  make_strokes.py                 picks stroke data for used characters from hanzi-writer-data (CI)
  tone_proto/                     the Python prototype the tone classifier was tuned with
```

## How a build works (.github/workflows/build.yml)

1. Restores the voice cache, runs `make_voice.py` (edge-tts, voice zh-CN-XiaoxiaoNeural; only new lines get recorded; failures fall back to the phone voice).
2. `npm pack hanzi-writer-data@2.0.1` → `make_strokes.py` → `assets/strokes.json`.
3. Decrypts `signing/next-chapter.jks.enc` with the repo secret `NC_KEY_PASSWORD` (Rudy has the password; never print or change it).
4. `assembleRelease`, then publishes `next-chapter-1.<run>.apk` as the `latest` release.
5. On failure, "Summarise build errors" puts compiler errors in an annotation: read with `gh api .../check-runs/<job id>/annotations`.

`assets/voice/` and `assets/strokes.json` are gitignored build outputs.

## Daily rhythm (1.32) and what's next

- `data/Life.kt` (pure, tested in `LifeTest`): parts of the day (morning < 12, plan 17–19, **call 19–20: never ask anything**, late ≥ 20), early start = opened 3–7 am, one `DayGoal` per day, Sunday week counts. `data/Prayers.kt`: Catholic prayers EN + mainland Catholic Chinese + pinyin, one per day after the Sign of the Cross (Chinese recorded by make_voice; Chinese + pinyin copied exactly from dominicweb.eu, Standard Mandarin page, per Rudy).
- `ui/today/Rhythm.kt`: MorningCard, EveningCard, SundayReview, WeekNotes (on Progress). `notify/Evening.kt`: 8:30 pm check-in if a goal is set and unanswered (Settings switch, `eveningCheck`).
- Identity V deck (`idv`, Seed.VERSION 5): for 加页手记 (Cryptic Notes), the PvE loot-and-extract mode Rudy and Yuling play; terms from Chinese player guides (Sina, 233乐园, 九游). Old 1v4 callouts are in `Seed.retired` and removed by `Seed.merge` only if never studied. Rudy won't ask Yuling about slang: check terms yourself.
- **Life tab (1.34, `ui/life/LifeScreen.kt`):** A+ Core 1 deck (`data/APlus.kt`, ~140 original cards from the 220-1201 objectives; progress in `AppState.aplus` as `Review`s, scheduled by `Srs` via `APlus.grade`, never mixed into the Chinese queue), exam-date countdown, domain strength bars weighted by exam share (`APlus.weakest`). Career (`data/Career.kt`): cert path (tap cycles planned→studying→booked→passed), AS classes, job postings with deadline reminders (`notify/Deadlines.kt`, 12:15, 3 and 1 days before). People: dates (MonthDay, Feb 29 safe) and notes (gift idea / likes / size / moment); Today's "Coming up" card shows birthdays ≤14 days and deadlines ≤7 days. Morning card shows one A+ card. Tests: `LifeTabTest`.
- **1.35:** Move's School & certs steps are linked to Life (`Career.linkedTasks`, `sync`, `tickFromMove`: certs passed ↔ ticked, AS ticks when all listed classes are done). Progress grid uses `Stats.practiceDays` (any day with reviews). Single-character tones only count/judge at `Coach.SINGLE_SURE` 0.5 (measured on native recordings).
- **1.36 (less scrolling):** `ui/components/Collapsible.kt` (open/closed remembered in the `ui_sections` prefs, not AppState). Today = greeting, countdowns, one "Now" card by time of day (after 8 pm with no goal: plan tomorrow's goal), practice, then closed rows (🙏 Prayer any time, Coming up, Phrase of the day, Last week on Mondays, Toward the move). Learn: 4×2 tool tiles, tones and decks collapsible with one-line deck rows. Move checklist groups, Life career/people, Progress sections collapsible. Rudy found the app "clunky and overwhelming"; keep new things folded by default.
- **1.39 (homey hub):** Today = Hero (greeting, your time + Yuling's via `Life.herTime` Asia/Shanghai, trip/move pills, streaks, call-time note), 4 rings (Chinese, A+, Prayer → bottom sheet, Goal → dialog), late check-in, Sunday/Monday items, Coming up, 6 tiles (Tones, Phrasebook, Drive, Move, People, Progress), slim phrase line. Learn tones: bars + kind words + weekly trend (`Stats.toneAccuracyBetween/toneWord`); decks as a 2/3-column tile grid. Life: A+ domains in one row, certs 2×2. Move savings compact. Progress: one week+totals card, no move-ready repeat, By deck uses Learn's numbers and opens the deck.
- **1.40:** `data/Study.kt`: study timer/log (`AppState.study`, running timer saved in `timerStart`/`timerSubject`, capped at 4 h) and A+ practice tests (`PracticeTest.build`: 20 questions weighted like the exam, distractors = other answers in the same domain; results in `AppState.tests`; missed cards graded AGAIN so they return in reviews). UI in Life (StudyTimer card, PracticeTestScreen); Sunday review shows study time. Tests: `StudyTest`.
- **1.42 (Rudy's Layout Studio edits):** Today tiles = Progress, Phrasebook, Drive (one row); rings evenly spaced; Learn shows Decks before Your tones; People section removed from Life (Person/PersonNote data and `people`/`notes` fields kept, so nothing saved is lost; Coming up now only shows job deadlines). Layout Studio artifact: https://claude.ai/artifact/Ut6ht23RAZrdeLU3CL4qzP (db `screens/<tab>`).
- **1.43–1.48 (phrases):** `idv` rebuilt for Cryptic Notes (old callouts in `Seed.retired`); `games` deck (48); `daily` +24; new `sweet` deck (Sweet talk · 甜言蜜语, 24; Rudy wants it natural, not corny, so five cheesy lines were retired in v10). Seed.VERSION 10; `SeedTest` pins deck counts. 睛 alone is not in the Vosk vocabulary.
- **1.49 (rules audit):** money math moved to `data/Money.kt` (rounds instead of truncating, savings pace rounds up, a bad or zero downloaded rate is ignored instead of dividing by zero); reminders, fare alerts and deadline notes arrive silently during the 7–8 pm call (`Life.isCallTime`).
- **1.50 (privacy):** her name and city are `AppState.herName`/`herCity`, typed in Settings → Her (on the phone only); Today/Move use `Life.herLabel`/`Life.moveTo` (fallback "Sichuan"). Content uses 她/女朋友/四川 instead of her Chinese name and towns. `Seed.merge` now drops any never-studied, non-custom card that is no longer a starter (so old ids needn't be listed in `Seed.retired`). Keep personal names and places out of the code. Sweet talk: 你的眼睛很漂亮 back (眼睛 is in `Judge.modelWords`).
- **1.51 (weekly routine, replaces the pick-a-goal evening):** `data/Routine.kt` (pure, `RoutineTest`): 7 `RoutineDay`s (task, first step, minimum, start time, `kind` = what Start opens: aplus test / aplus-review / chinese / timer), seeded from Rudy's plan (personal bits generic: "side project", `{her}` = Settings name), `routineChecks` (DONE / MINIMUM / SKIPPED; streak counts DONE+MINIMUM, a skip today ends it), `routineReviews` (Sunday: worked / change), `wakeMinutes` 7:30 (early start = before wake + 30 min), `homeMinutes` 5:45 pm, `windDownMinutes` 11:30 pm. UI: Today ring "Tonight" + `TonightJob` card (from home time − 15 min until checked in, hidden during the call), `ui/life/Routine.kt` (RoutineSummary on Life, RoutineScreen: week dots, rules, anchors, per-day editor with a 2-week note), Sunday review has the routine questions. A finished A+ test on an A+ evening checks the day off. `notify/Evening.kt` now sends "You're home" and "Wind down" (one-shot `setWindow` alarms, 10-min window, rebooked daily; open Today via `MainActivity.EXTRA_TODAY`); the 8:30 goal check-in is gone. Old `goals` data is kept.
- **1.52 (reorganized, "each tab answers one question"):** 4 tabs: **Today** (what now) · **Chinese** (`Tab.LEARN`, practice) · **Life** (getting better) · **Move** (the plan). The Progress tab is gone: `ui/progress/ProgressScreen.kt` now holds `ChineseProgress` (bottom of Chinese: week + streaks, 12-week grid; "By deck" dropped, the deck tiles already show it) and `LifeWins` (bottom of Life: milestones, Sunday lines). Today = hero, rings, ONE `NowCard` chosen by time (call → tonight's job → morning prayer → phrases waiting → A+ cards → wind down → all caught up + phrase of the day), Sunday review in the afternoon, Monday last week, coming up; the tiles are gone (Phrasebook/Drive live in Chinese → Practice). Chinese: review card, decks, Practice grouped as Speak & listen / Read & write / Out in China, tones, "How it's going". Life: routine first, A+ section, career, wins. Settings: You & her, Reminders, Learning, Voice, Speaking check, Trip mode, Backup.
- **1.53:** week dots on Today (inside the rings card, tap → routine). New home-screen widget `widget/WeekWidget.kt` (`layout/widget_week.xml`, `xml/widget_week_info.xml`, `drawable/widget_dot_*`): this week's dots, streak, tonight's job and first step; refreshed from `Store.onChange` and every 30 min. Chinese tab: Practice above Decks, always open. Fixed: changing "Next trip" on Move did nothing when the flight planner had a trip (the card shows the planner trip but the picker saved `tripDate`); now `AppState.withNextTripDate` moves the planner trip (return keeps the trip length). Test in `PlanTest`.
- **1.58 (Work & home):** `data/Places.kt` (pure, `PlacesTest`): arriving at work starts the day +5 min (`GRACE`), expected end +8 h; a home arrival counts once ≥4 h after the start (lunch doesn't); `homeTime` = real arrival, else expected end + 45 min, else `homeMinutes`. `notify/PlaceWatch.kt`: platform `LocationManager.addProximityAlert` (150 m, no Play Services, works offline) for `AppState.workSpot`/`homeSpot` (saved on the phone via "I'm here" in Settings → Work & home), re-registered at app start and boot; `PlaceReceiver` records arrivals, rebooks `Evening`, and sends "You're home" on arrival (the clock alarm then stays quiet). Today: tonight's job window uses `Places.homeTime`; hero shows "💼 Work till ~…" and an "I'm home" button. Needs location "Allow all the time"; Samsung battery "Unrestricted" recommended. Not testable off-device: confirm on Rudy's Fold.
- **1.59 (set Work/Home by address or map):** `ui/settings/SpotPicker.kt`: full-screen picker with OpenStreetMap search (Nominatim, one request per Search tap, app User-Agent) or pasted coordinates, and a small map drawn by the app from OSM tiles (no map library; 2 downloads at a time, cached a week in `cacheDir/tiles`, attribution shown); drag the map under a fixed pin, ◎ = my location. `data/MapMath.kt` (pure, `MapMathTest`): Web Mercator tile math, coordinate parsing, search parsing and short names. `Spot` has a `label`. Settings rows: Set/Edit + Clear.
- **1.60:** each `Spot` has a `radius` (50–500 m, default 150; slider + gold circle in the picker, used by the proximity alert). Fixed: a work arrival outside 5 am–1 pm no longer starts a workday (saving Work at home at 5 pm made "work" run to midnight and hid tonight's job); saving or clearing a spot resets today's arrivals; "Reset today" button; the battery tip hides once the app is Unrestricted (`isIgnoringBatteryOptimizations`) and links to App info. Tests in `PlacesTest`, `MapMathTest`.
- **1.61 (weight & calories):** `data/Body.kt` (pure, `BodyTest`): `AppState.weights` (one `WeightEntry` per day, lb), `calorieAdds` (each tap, so the last can be undone), `goalWeight`, `calorieTarget`; `Body.pill` gives "⚖️ 182.4 → 170 lb · 🍽 1,450 / 2,000 cal". Today hero shows the pill (tap → `ui/today/BodySheet.kt`: today's weight, week change, +100/+250/+500 or any amount, undo, goals). Logged by hand only (Rudy's choice); Health Connect not used.
- **1.62:** body sheet keeps every day: `Body.history` (60 days back to the first log, blank days included, weight change vs the last weigh-in, calories) and a graph (1.63: `BodyGraph` in `BodySheet.kt`: calorie bars green/coral with a dashed target, weight line with a dashed goal, 7/30/90 days, tap a day for its numbers; `Body.series`); the day list is folded under "Each day". Calories now kept 2 years (were 120 days). Tests in `BodyTest`.
- **1.65:** `Places.GRACE` = 0 (arrivals were accurate). Days off: `AppState.daysOff` (`DayOff(day, like = 6|7)`), `Routine.jobOn(state, date)` (replaces `jobFor` everywhere) follows the Saturday/Sunday plan on a day off; no workday then (`Places.atWork`/`homeTime`, `PlaceReceiver` ignores work). UI: Life → Weekly routine → Days off (add with a date picker, like Sat/Sun, ✕); "Tomorrow off 🏖" in Today's Tonight sheet; 🏖 on the week dots. "Heading home? 🚗" Now card (`Places.headingHome`: 15 min before the expected end until home) opens Drive mode. Tests in `PlacesTest`, `RoutineTest`.
- **1.66 (造句 Build):** `data/Builder.kt` (pure, `BuilderTest`): splits deck phrases into words by pinyin spacing (syllables = vowel groups; skips phrases where it doesn't line up, e.g. erhua 哪儿; 151 of 198 starter phrases usable), mixed rounds of 8 (BUILD tiles + decoys / FILL a blank from 4 / LISTEN then build), started phrases first. `ui/learn/BuildScreen.kt`: tiles, Check (2 tries, wiggle), answer with voice + pinyin, optional `SpeakPanel`, score; counts as a practice day. Practice groups now: Speak & listen · Build & read · Write & go.
- **1.67 (swap, tonight, trip):** Rudy's Anki: Refold Mandarin 1k, first 112 words studied (as of Nov 2026); used only to pick swap words (not copied). `data/Swaps.kt` (pure, `SwapsTest`): 10 frames with a slot (我想跟你一起__ …), `Swaps.pick` → Build kind SWAP (2 per round of 8). `TonightLine` (`AppState.tonight`): ☆ on a Build answer; Today shows it 6–7 pm (after 7 → next evening). Trip prep deck `trip` (27 phrases, Seed v12): `TripPrep` opens it 60 days before the next trip (`Srs.queue` holds its new cards until then, then puts them first); Today hero pill "✈️ trip phrases n/3"; deck tile shows "🔒 opens in N days".
- **Ideas not built:** Core 2 (220-1202) cards; lunar birthdays (convert with ICU ChineseCalendar).

## Saved data: be careful

- Everything is in `AppState` (kotlinx.serialization, `ignoreUnknownKeys`). New fields need defaults so old files still load.
- `Seed.VERSION` bump = new starter cards/tasks get merged into existing state on launch.
- Backup/restore (Settings) exports/imports the whole AppState JSON.
- Preferences outside AppState: `voice` (TTS engine/voice/recordings), `voice_profile` (pitch calibration), `fares` (SerpApi key, searches/day, monthly counter), `app_font` not used here.

## Features (all shipped)

Settings shows the app version at the bottom. Today (countdowns, practice, phrase of the day, move step, Monday recap, trip mode) · Learn (decks + SRS review with Read/Say/Listen, tone trainer hear/say, dialogues, writing with stroke checking, grammar quizzes, stories with tap-to-define, Drive mode, etiquette, phrasebook, weak-tone drills, redo today) · Move (dates, savings, checklist, flights planner, fare alerts with calendar/activity log/Google Flights links, packing, money in yuan) · Progress (stats, heatmap, weekly recap, milestones) · widget, reminder, WeChat quick-add, backup, voice picker, microphone check.

## Open issue being worked on: the speaking check

**Status (Nov 3): solved; history below.** 1.31 removed the Google recognizer entirely (`Recognize.kt`, Live mode, the Mic test's Google cards); the Mic check is now room noise, playback and what Vosk hears.

**Earlier status:** the pitch/tone side works (records, tracks pitch, classifies tones, draws the graph). The *word check* (did a native ear understand you) is the problem on Rudy's phone.

What we learned on his phone (from the Settings → Mic & recognition test):
- Recognizer is Google (`com.google.android.tts`), mic level and room noise are fine.
- **Live** recognition (recognizer uses the mic itself) works online.
- Recognizing **a recording** (`EXTRA_AUDIO_SOURCE`, "one take") fails with **error 12** (language not supported): Google's on-device recognizer doesn't offer Mandarin to apps on his phone. `triggerModelDownload` also returned error 12. Not fixable in settings.
- Recording at the same time as the live recognizer doesn't work: the foreground app wins the mic and the recognizer hears silence (that bug was in 1.23, fixed in 1.24).

1.24: Live goes online first, no parallel recording, Live is the default mode. So today Live = word check only, one take = pitch only (on his phone). **Rudy hasn't confirmed 1.24 works yet; ask him.**

**Decision pending: add an on-device recognizer** so one recording gives both words and pitch, offline, in China. Researched options:
1. **Vosk** (recommended last session): `vosk-model-small-cn-0.22`, about 42 MB, Apache 2.0, Android library `com.alphacephei:vosk-android`, accepts our own 16 kHz PCM. Grammar-restricted recognition can be given the target plus tone-confusable alternatives (e.g. 糖/汤/躺/烫) to tell which one was said. Model could ship in the APK or download on first use (check that the download host works from China).
2. **sherpa-onnx Paraformer** (Alibaba/Xiaomi): more accurate for Chinese, even a Sichuanese model, but bigger and harder to integrate (AAR from GitHub releases).
3. **Azure Pronunciation Assessment** (zh-CN): best per-syllable scoring, 5 free hours/month, but online + needs a key and a paid-service account, which conflicts with the "nothing that costs money / works in China" rules. Only as an optional extra if Rudy wants.

**Decided (Nov 2):** Vosk, model bundled in the APK, two builds. Both shipped.

- **Build 29 (1.29):** Live can be stopped (`LiveControl` in `Recognize.kt`). Vosk added (`speech/Offline.kt`), test card 5 in Settings → Mic & recognition test. On Rudy's phone: Vosk works (你好 100%, 0.5 s), no R8 problems.
- **Finding:** Vosk small-cn barely hears tones. On the app's own native recordings, picking among 妈麻马骂 / 汤糖躺烫 it got 6/8 (4/8 with the voice 5 semitones lower); tone 3 is heard as tone 2. But forced to a phrase's own characters it times 99% of characters and rejects a different phrase ~88% of the time. So: **Vosk = sounds + timing, pitch = tones.**
- **Build 30 (1.30):** `Coach.check`: Vosk grammar = the phrase's characters (`Judge.grammarFor`, which keeps 啡/杉/睛/矶/础/逸/钥/鸯/鸳 inside their words since the model only knows them there; `tools/tone_proto/vosk_oov.py` finds such characters) → per-character times (`Judge.matchTimes`) → tone per character: one character = `Pitch.classify`; phrases = `Judge.phraseTones`, a small softmax model (`PhraseToneModel.kt`, generated by `tools/tone_proto/phrase_tones.py` from `phrase_collect.py` data). Held-out native phrases: 82–86% right, 91–92% when p ≥ 0.7 (`Coach.SURE`); below that the tone gets the benefit of the doubt (`CharResult.sure = false`, not logged to tone stats). On native speech overall: 94% green, 5% yellow (false alarms), 1% red. Tone sandhi accepted (`Judge.acceptedTones`). Graph shows each character's stretch with its colour. If Vosk fails, the pitch graph and melody still show (1.31; the Google fallback is gone). Model sha256 pinned in build.yml.
- **To do / watch:** learner voices are harder than native ones; ask Rudy how often yellow feels wrong. Retrain (`phrase_collect.py` → `phrase_tones.py`) after adding many phrases. Tests: `JudgeTest`, `VoskJsonTest` (CI runs `testReleaseUnitTest`).

## Things that bend CLAUDE.md's rules (already in the app, flag before expanding)

- **Fare alerts** call SerpApi over the internet (free plan, 250 searches/month; key stored on the phone only). The exchange-rate "Update" button calls open.er-api.com. Both are optional, internet-only features.
- Voice recordings are generated in CI with edge-tts (Microsoft's unofficial endpoint). If it stops working, existing recordings stay and new lines fall back to the phone's voice.

## Known rough edges / ideas not built yet

- Drive mode hasn't been confirmed on his car/Bluetooth yet.
- Writing-practice tolerance was tuned on simulated traces only (accepted 100% of natural traces, 0% backwards); ask how it feels.
- Tone classifier: 40/40 on native recordings, 39/40 male-shifted; real learner voices are harder. Voice setup (calibration) improves it a lot.
- Erhua words (一点儿, 哪儿) don't split one syllable per character; the judge falls back to per-character pinyin from ICU.
- Pitch prototype and tests live in `tools/tone_proto/` (Python, uses the APK's recordings).
- Unit tests (CI runs them before every build): `SrsTest`, `PlanTest`, `MoneyTest`, `LifeTest`, `LifeTabTest`, `StudyTest`, `SeedTest`, `JudgeTest`, `VoskJsonTest`. Not yet covered: `Stats`, `Pitch`, `Pinyin` (uses android.icu, so it needs Robolectric or a JVM stand-in).
- Ideas Rudy liked but hasn't picked yet: Sichuanese dialect deck/audio, conversation challenges, more stories.

## Other repos

- `DudLatitat/two-skies`: Rudy's other app (him + Yuling). Separate rules in its own CLAUDE.md. Don't touch it from here.
