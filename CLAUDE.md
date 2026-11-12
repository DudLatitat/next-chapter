# Working on Next Chapter · 新篇章

> Current state, architecture and open work: see `HANDOFF.md`. Keep it current: update it in the same push as any change worth knowing next session.

## Who I am

I'm Rudy (Dudster). This is my private app for the road to China: learning Chinese for real life there, tracking the move, and the rest of my life on the way. I'm a beginner in Chinese and new to Android development, and I want to learn enough to change the code myself, so explain non-obvious changes in a sentence. I'm studying for CompTIA A+, Network+ and Security+, so when networking, storage or security comes up (fare alerts, the key, backups), explain it briefly.

## The end goal

This app exists to make my life better in every way, not only my Chinese. Learning the language, the move to China, my relationship and her family, my career and studies, my money, my faith and my daily routine all count. When you suggest ideas, think about the whole of my life, as long as it fits the rules below.

## The app

- **Tabs** (each answers one question): Today (what now: hero, breathing rings, this week's routine, one Now card), Chinese (practice: review, practice tools, decks, tones, how it's going), Life (getting better: weekly routine, A+ exam, career, wins) and Move (the plan: dates, flights, money, packing, savings, checklist). Extras: two widgets (phrase, weekly routine), reminders, Work & home, Use on my computer (the phone serves a desktop page on the home Wi-Fi while that screen is open), WeChat quick-add, backup.
- Mainly for me. Yuling doesn't get access yet, so no sharing or sync features without asking.
- **Hard constraint: everything works offline and in mainland China without a VPN.** No account, no server, no Google services, nothing loaded from Google, and no library that phones home to a blocked domain.
- **Allowed exceptions** (already in the app, fine to keep and improve; each is optional and the rest of the app works without it):
  - Fare alerts call SerpApi's free plan with a key stored only on my phone.
  - The money screen can fetch today's exchange rate (open.er-api.com).
  - Flight cards open Google Flights / Skyscanner links in the browser (links only, nothing loaded in the app).
  - Voice recordings are made in CI with edge-tts.
  - The speaking check is fully offline (Vosk on the phone).
  - Work & home (Settings) can search an address with OpenStreetMap's Nominatim and show OpenStreetMap map tiles, only while the spot picker is open. Noticing arrivals uses the phone's own location (no Google) and stays offline.
  - Ask me before adding any *new* online dependency.
- Built on the Two Skies stack (Kotlin, Jetpack Compose, night-sea theme) and should feel just as polished. Reuse Two Skies patterns rather than inventing new ones, but never touch the two-skies repo from here.

## Bring ideas

I want this app to be something I actually love opening, not just a flashcard tool. Be creative. The rest of this file is about not *breaking* things, not about thinking small.

**What it should feel like:**
- **Real conversations.** The goal is talking with Yuling, her family and people in Sichuan, not passing a test. Favor speaking, listening and real situations (dinner with her family, ordering food, a taxi, WeChat messages, gaming together) over memorizing lists.
- **Fun and game-like.** Streaks, rings, challenges, rewards, surprises, things that pull me back every day. Make practice feel like playing, not homework.
- **Calm, not cluttered.** I found the app "clunky and overwhelming" once. New things start folded or small; one glance at Today should tell me what matters now.
- **Practical.** It should make the move, the money, the A+ exam and the 15–16 hour gap easier day to day.

A good test: *would this help me say something real to Yuling's family next trip, and would I want to open it tomorrow?*

**How to bring ideas:**
- At the end of every task, suggest 1–3 ideas: new features, game mechanics, or polish. Be bold and specific (what it looks like, how it feels, roughly how big it is). I pick what gets built; don't build ideas I haven't chosen.
- When building something I asked for, go past the bare minimum: smooth animations, satisfying feedback when I get something right, nice empty states. That polish is part of the task, not an extra.
- If you see a better way to do what I asked, tell me before building it my way.
- The "simplest tool" rule is about libraries and code, not about ideas. Ideas can be big.

**Avoid suggesting:**
- Anything that costs money: paid APIs, paid services, servers.
- Anything social or public. The app is private.
- Anything that needs Yuling to do work. She can be part of the content (phrases for talking to her), but the app shouldn't depend on her.
- Reminders or nags during her call window, 7–8 pm (`Life.kt`: the "call" part of the day never asks anything).

If an idea breaks one of these but you think it's really worth it, you can still pitch it. Just say which rule it breaks and why it's worth it.

**Hard limits** (never cross, even in ideas): it works offline and in China without a VPN, never lose my saved progress, and the Safety section. Everything else in this file is a default you can argue with.

## Knowing me

Tips from working with me so far, so you don't have to learn them the hard way:

- **My life right now:** I work a weekday 9–5 IT job with a short drive (15–30 min), so Drive mode and quick sessions fit my day. I'm trying to wake at 6:30 every day and use evenings for my goals. Yuling usually calls around 7–8 pm (her morning), and once she calls I don't get anything else done.
- **The big plan:** move to Sichuan in about 2–3 years, marry Yuling, and build a life there. I fly LAX ⇄ Chengdu 2–3 times a year to visit. I'm saving toward the move, so I care about free options and stretching money.
- **Career and school:** finishing an AS in Computer Information Systems; A+ first (Core 1, then Core 2), then Network+ and Security+. Growing in IT (eventually a public-sector job) matters for the move.
- **Chinese:** beginner. Yuling speaks little English. What I want most is to talk to her and her family myself. Phrases for real conversations beat everything else.
- **What I'm into:** gaming (Honkai: Star Rail, and Identity V's Cryptic Notes mode, which Yuling and I play together), hands-on tech (Raspberry Pi, Python, PowerShell, VPNs, home networking). Game-like design really works on me.
- **How I use the app:** on a Galaxy Z Fold 8 Ultra (Android 17), folded and unfolded. I test every build myself and usually reply with screenshots. Read them closely: they show bugs I might not mention.
- **How I work with you:**
  - I often answer "what else can we add?" with "all of it." Build it, but tell me what to test first.
  - I like options laid out with a clear recommendation and why, then I pick. When I say "optimize as you see fit," just do it.
  - Give me exact steps for settings on my phone or on websites, with direct links that open in the browser (the GitHub Android app can't do things like secrets).
  - Say plainly when something isn't possible (like a phone limitation) and offer the best workaround.
  - For layout changes I may point you at the Layout Studio artifact (link in `HANDOFF.md`): read my edits and notes there before building.

## Chinese content

- Simplified characters, pinyin with tone marks, English that says what it *means*, not word for word.
- Phrases should be what people actually say in daily life (and Sichuan, where it matters), not textbook Chinese. Natural and sincere beats clever: no cheesy pickup lines or corny romance.
- Never guess a translation or a tone. I won't ask Yuling about slang: check terms yourself (player guides, dictionaries, the source I give you) and flag anything you're still unsure of.
- Prayers come only from the source I named (dominicweb.eu, Standard Mandarin page), copied exactly.
- **Adding or changing starter phrases** (`data/Seed.kt`):
  - Card ids are `"deck:hanzi"`; check the new hanzi isn't already in another deck or in `Vocab.kt`.
  - Bump `Seed.VERSION` so `Seed.merge` adds them to my saved cards.
  - To remove or replace a phrase, put its id in `Seed.retired`: it disappears only if I never studied it.
  - A new deck needs a `Deck(...)` line in `Models.kt`; keep `SeedTest`'s deck counts in step.
  - Run the Vosk vocabulary check (`tools/tone_proto/vosk_oov.py`): a character the model doesn't know (like 睛 on its own) can't be checked when I speak, so swap the phrase or keep the character inside a word the model knows (`Judge.grammarFor`).
  - After adding many phrases, retrain the phrase tone model (`phrase_collect.py` → `phrase_tones.py`).
- Stroke data comes from hanzi-writer-data / Make Me a Hanzi (Arphic Public License); keep the credit.
- `tools/` builds generated assets (`make_strokes.py`, `make_voice.py`); their outputs (`assets/voice/`, `assets/strokes.json`, `assets/vosk-cn/`) are gitignored: change the script, not the output. Voice recordings are cached by a hash of `data/*.kt`, so new lines get recorded on the next build.

## Branches and builds

- **Work happens on `main`** (my call, Nov 2026, after merging builds 1.32–1.67): commit and push there; each push builds the APK I install. Don't push to any other branch without my OK.
- **Every push to any branch builds an APK and replaces the `latest` release** (`next-chapter-1.<run>.apk`), which I install. So only push what you want on my phone.
- The version is `1.<GitHub run number>`; Settings shows it at the bottom.
- CI runs the unit tests (`testReleaseUnitTest`) before building; a failing test stops the release.
- No Android SDK in the workspace: app code is only compiled by CI. Read failures with `gh api repos/DudLatitat/next-chapter/check-runs/<job id>/annotations` (the "Summarise build errors" step puts compiler errors there). Pure Kotlin in `data/` can be tested locally in a small JVM Gradle project before pushing.
- Each build uses ~4 of the 2,000 free GitHub Actions minutes a month. Batch small fixes into one push. A push that only changes docs (`.md`) should say `[skip ci]` in the commit message so it doesn't build; GitHub skips on those words *anywhere* in the message, so never write them in a message that should build.
- Shrinking (R8) can break things only a launch shows. `app/proguard-rules.pro` keeps kotlinx.serialization serializers and Vosk/JNA whole; a new library that uses reflection or JNI needs keep rules too. Mention anything risky when sending a build.
- Only `arm64-v8a` is built (keeps the APK small); don't add other ABIs.
- The Vosk model is downloaded in CI and its sha256 is pinned in `build.yml`; a changed model must fail the build, not ship.

## How the code fits

- **One saved state:** everything I enter or earn lives in `AppState` (`data/Models.kt`), saved as one JSON file by `Store`. All changes go through `MainViewModel` → `Store.edit`.
- **Settings outside AppState** (SharedPreferences): `voice`, `voice_profile`, `fares` (SerpApi key), `ui_sections` (which collapsible sections are open). These are per-phone settings, not progress, and are not in backups.
- **Voice log** (`speech/VoiceLog.kt`, file `voice_log.json`, switch in Settings → Speaking check): the last 100 Say-it attempts as numbers only (pitch shapes, expected tones, the model's guesses; no audio). Not in AppState or backups. Rudy exports it to measure tone models on his voice with `tools/tone_proto/eval_voicelog.py`.
- **Pure logic in `data/`** (`Srs`, `Life`, `Stats`, `Study`, `Career`, `Plan`, `Pinyin`, `Seed`): no Android imports, so it can be unit-tested. Put new calculations there, not in Compose screens.
- **UI text** is written directly in the Compose screens in English (no `strings.xml` resources); Chinese appears as content with pinyin. Don't start a translation layer unless I ask.
- **Layout:** test both Fold screens: the narrow cover screen and the wide inner screen. Use the wide screen for side-by-side reading (prayers, dialogues) and grids, not just stretched cards.
- **Speech:** Vosk gives the sounds and timing; pitch gives the tones. Don't make the tone check stricter without measuring on native recordings first (`tools/tone_proto/`): a check that's wrong feels discouraging.

## How to work

Size the work before starting and say it in one line: **small** (copy, colors, phrases, a one-file tweak), **medium** (a bug fix or a change inside one screen), or **large** (a new feature, a data model change, anything that could lose my saved progress). For large, give me the plan in a few lines first. When torn, pick the smaller size.

- Bug fixes ship with a test that would have caught the bug, where the code can be tested.
- **Never break my saved data.**
  - New `AppState` fields need defaults, so old files still load (`ignoreUnknownKeys` covers removed ones).
  - Never rename or retype a saved field without a migration that keeps existing progress, streaks and phrases.
  - When a feature is removed from the UI, keep its data (like `people`/`notes`).
  - Backup/restore must keep working: it exports and imports the whole AppState.
- Before calling something done, be able to explain why it works and where it would break.
- If there are two reasonable ways to do something big, or a request contradicts how the app already works, stop and give me 2–3 options with real trade-offs. Don't stop for small, obvious things.

## Time math and calculations

Dates, countdowns, the time gap, spaced-repetition scheduling, streaks, study time, budget math and dollar ⇄ yuan conversion are pure calculation: write code and a test, never do the math in your head.

- Always `java.time` with real zone IDs (`Asia/Shanghai` for Yuling's time, the phone's zone for mine), never hardcoded hour offsets. LA has daylight saving and China doesn't, so the gap changes twice a year.
- Birthdays and yearly dates use `MonthDay` and must handle Feb 29.

## Tech choices

- Simplest standard tool wins: AndroidX and the Kotlin standard library first.
- Before adding a library, check that it's maintained and works offline and in China without a VPN, and that it doesn't pull in Google Play Services. Tell me what you picked and why in a line or two.

## Safety

- Never commit secrets or `local.properties`. Check `.gitignore` before committing anything new.
- Never touch or replace `signing/next-chapter.jks.enc` or its key, and never print or change the `NC_KEY_PASSWORD` secret. Losing the key means the app can't be updated on my phone without losing my data.
- **This repo is public.** Never put my personal details (money, work matters, anything private about Yuling or her family, our chats) in code, phrases, commits or docs. Phrases stay general enough for anyone.
- Never run destructive commands (`rm -rf`, `git reset --hard`, `git push --force`) without asking.
- Never use `--no-verify`. Don't commit APKs, build outputs or Python caches.

## When a task is finished

Report **DONE**, **DONE_WITH_CONCERNS** (and what worries you), **BLOCKED** (what's stopping it), or **NEEDS_CONTEXT** (exactly what you need). Then say which build it's in and where in the app to look when testing.

## How to talk to me

- Direct and short. No preamble. Lists over long paragraphs: I read on my phone.
- Name the actual file and function, not "the time code."
- If something is broken, say so plainly.
- End with what I should do next.
