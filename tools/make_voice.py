"""Records every built-in Chinese phrase with a neural voice, into app/src/main/assets/voice/.

Run by CI before building. Files are named by the MD5 of the text (what Speaker.kt looks up),
and existing files are kept, so only new phrases are recorded. If the voice service can't be
reached the build carries on and the app uses the phone's own voice.
"""
import asyncio, hashlib, pathlib, re, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/main/java/com/dudlatitat/nextchapter/data"
OUT = ROOT / "app/src/main/assets/voice"
VOICE = "zh-CN-XiaoxiaoNeural"
# Extra voices for training the tone model only (never shipped):
#   make_voice.py --voice zh-CN-YunxiNeural --out ~/tone-voices/zh-CN-YunxiNeural --short
if "--voice" in sys.argv: VOICE = sys.argv[sys.argv.index("--voice") + 1]
if "--out" in sys.argv: OUT = pathlib.Path(sys.argv[sys.argv.index("--out") + 1]).expanduser()
SHORT = "--short"  # only phrases of 2–10 characters (what the tone model trains on)
CJK = re.compile(r"[一-鿿]")

def phrases():
    found = []
    for name in ("Seed.kt", "Vocab.kt", "Dialogues.kt", "Phrasebook.kt", "Pinyin.kt", "Grammar.kt", "Stories.kt", "Culture.kt", "Prayers.kt", "Dinner.kt"):
        for s in re.findall(r'"((?:[^"\\]|\\.)*)"', (SRC / name).read_text(encoding="utf-8")):
            # Chinese only: skip quiz blanks and English sentences that quote a character.
            if CJK.search(s) and "$" not in s and "___" not in s and len(re.findall(r"[A-Za-z]", s)) < 3 and s not in found:
                found.append(s.strip())
    return found

async def main():
    OUT.mkdir(parents=True, exist_ok=True)
    texts = phrases()
    if SHORT in sys.argv: texts = [t for t in texts if 2 <= len(CJK.findall(t)) <= 10]
    if "--list" in sys.argv:
        print("\n".join(texts)); return
    import edge_tts
    made = failed = 0
    for text in texts:
        path = OUT / (hashlib.md5(text.encode("utf-8")).hexdigest() + ".mp3")
        if path.exists() and path.stat().st_size > 0:
            continue
        try:
            await edge_tts.Communicate(text, VOICE, rate="-5%").save(str(path))
            made += 1
        except Exception as e:
            failed += 1
            path.unlink(missing_ok=True)
            print(f"skip {text}: {e}")
            if failed >= 5 and made == 0:
                print("Voice service unreachable; building with the phone's voice only."); return
    print(f"{len(texts)} phrases, {made} newly recorded, {failed} failed")

asyncio.run(main())
