# Working on Next Chapter · 新篇章

## Who I am

I'm Rudy (Dudster). This is my private app for the road to China: learning Chinese for real life there and tracking the move. I'm a beginner in Chinese and new to Android development, and I want to learn enough to change the code myself, so explain non-obvious changes in a sentence.

## The app

- Mainly for me. Yuling doesn't get access yet, so no sharing or sync features without asking.
- Everything stays on the phone: no account, no server. It must work in China without a VPN, so no Google services and no library that phones home to a blocked domain. Keep it that way unless I say otherwise.
- Built on the Two Skies stack (Kotlin, Jetpack Compose, night-sea theme) and should feel just as polished. Reuse Two Skies patterns rather than inventing new ones.

## Chinese content

- Simplified characters, pinyin with tone marks.
- Phrases should be what people actually say in daily life (and Sichuan, where it matters), not textbook Chinese.
- Never guess a translation or a tone. Flag anything you're unsure of so I can check it with Yuling.
- Stroke data comes from hanzi-writer-data / Make Me a Hanzi (Arphic Public License); keep the credit.
- `tools/` builds generated assets (`make_strokes.py`, `make_voice.py`). Their outputs are gitignored: change the script, not the output.

## Builds

- One branch, `main`. Each push builds an APK on GitHub Actions and publishes it as the **latest** release, which I install on my phone and test myself.
- There's no Android SDK in the workspace, so app code is only compiled by CI. Read failures with `gh api repos/DudLatitat/next-chapter/check-runs/<job id>/annotations`.
- Batch small fixes into one push so they share one build.
- Shrinking (R8) can break things only a launch shows, so mention anything risky when sending a build.
- Test the layout on both of my Fold's screens: the narrow cover screen and the wide inner screen.

## How to work

Size the work before starting and say it in one line: **small** (copy, colors, a one-file tweak), **medium** (a bug fix or a change inside one screen), or **large** (a new feature, a data model change, anything that could lose my saved progress). For large, give me the plan in a few lines first. When torn, pick the smaller size.

- Bug fixes ship with a test that would have caught the bug, where the code can be tested.
- Never break my saved data. Any change to stored data needs a migration that keeps existing progress, streaks and phrases, and backup/restore must keep working.
- Before calling something done, be able to explain why it works and where it would break.
- If there are two reasonable ways to do something big, or a request contradicts how the app already works, stop and give me 2–3 options with real trade-offs.

## Calculations

Dates, countdowns, spaced-repetition scheduling, streaks, budget math and dollar ⇄ yuan conversion are pure calculation: write code and a test, never do the math in your head. Use `java.time` for dates.

## Tech choices

- Simplest standard tool wins: AndroidX and the Kotlin standard library first.
- Before adding a library, check that it's maintained and works offline and in China. Tell me what you picked and why in a line or two.

## Safety

- Never commit secrets or `local.properties`. Check `.gitignore` before committing anything new.
- Never touch or replace `signing/next-chapter.jks.enc` or its key. Losing it means the app can't be updated on my phone.
- Never run destructive commands (`rm -rf`, `git reset --hard`, `git push --force`) without asking.
- Never use `--no-verify`. Don't commit APKs or build outputs.

## When a task is finished

Report **DONE**, **DONE_WITH_CONCERNS** (and what worries you), **BLOCKED** (what's stopping it), or **NEEDS_CONTEXT** (exactly what you need). Then say where in the app to look when testing.

## How to talk to me

- Direct and short. No preamble.
- Name the actual file and function.
- If something is broken, say so plainly.
- End with what I should do next.
