"""
Collects training data for the phrase tone model: the app's native recordings of every
built-in phrase, each character timed by Vosk (forced to the phrase's own characters), with its
pitch track and the tone the app expects (from the card's pinyin; tone sandhi allowed).

  pip install vosk numpy pypinyin   (vosk needs a stub "srt" module on some setups)
  python3 phrase_collect.py . <stub dir> <vosk-cn model dir> <voice mp3 dir> app/.../data out.pkl

The model and voice folders are the APK's assets/vosk-cn and assets/voice (unzip the APK).
"""
import sys, re, glob, json, hashlib, subprocess, os, pickle, unicodedata
sys.path.insert(0, sys.argv[1]); sys.path.insert(0, sys.argv[2])
import numpy as np
from pitch import track
from vosk import Model, KaldiRecognizer, SetLogLevel
from pypinyin import pinyin, Style
from pypinyin.constants import PINYIN_DICT
SetLogLevel(-1)
MODEL, VOICE, SRC, OUT = sys.argv[3:7]
model = Model(MODEL)
han = re.compile(r'[一-鿿]')
MARK = {}
for base, toned in {"a":"āáǎà","e":"ēéěè","i":"īíǐì","o":"ōóǒò","u":"ūúǔù","ü":"ǖǘǚǜ"}.items():
    for i, ch in enumerate(toned): MARK[ch] = (base, i+1)
def bare(s): return "".join(MARK.get(c,(c,0))[0] for c in s.lower()).replace("v","ü")
def tone(s): return next((MARK[c][1] for c in s if c in MARK), 5)
VALID = set()
for v in PINYIN_DICT.values():
    for r in v.split(","): VALID.add(bare(r))
def syllables(p):
    out = []
    for word in re.findall(r"[a-zA-Zāáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜüv]+", p):
        def go(at):
            if at == len(word): return []
            for L in range(min(6, len(word)-at), 0, -1):
                piece = word[at:at+L]
                if bare(piece) in VALID:
                    rest = go(at+L)
                    if rest is not None: return [piece] + rest
            return None
        r = go(0)
        if r is None: return None
        out += r
    return out
def sandhi(chars, t):
    out = []
    for i, x in enumerate(t):
        nx = t[i+1] if i+1 < len(t) else None
        s = {x}
        if x == 5: s = set()
        elif chars[i] == "不" and nx == 4: s = {4, 2}
        elif chars[i] == "一" and nx == 4: s = {1, 2}
        elif chars[i] == "一" and nx in (1,2,3): s = {1, 4}
        elif x == 3 and nx == 3: s = {3, 2}
        out.append(s)
    return out
pairs = {}
for f in glob.glob(SRC + "/*.kt"):
    for h, p in re.findall(r'"([^"\n]*[一-鿿][^"\n]*)"\s*,\s*"([^"\n]+)"', open(f).read()):
        chars = "".join(han.findall(h))
        sy = syllables(p)
        if 2 <= len(chars) <= 10 and sy and len(sy) == len(chars) and os.path.exists(f"{VOICE}/{hashlib.md5(h.strip().encode()).hexdigest()}.mp3"):
            pairs[h] = (chars, [tone(s) for s in sy])
print("phrases", len(pairs), flush=True)
def audio(text, shift=0):
    f = f"{VOICE}/{hashlib.md5(text.strip().encode()).hexdigest()}.mp3"
    af = f"asetrate=24000*{2**(shift/12)},aresample=16000" if shift else "aresample=16000"
    return subprocess.run(["ffmpeg","-v","quiet","-i",f,"-af",af,"-ar","16000","-ac","1","-f","s16le","-"],capture_output=True).stdout
out = []
for shift in (0, -5):
    for h, (chars, t) in pairs.items():
        raw = audio(h, shift); x = np.frombuffer(raw, dtype=np.int16)/32768.0
        r = KaldiRecognizer(model, 16000, json.dumps([" ".join(chars), *sorted(set(chars)), "[unk]"], ensure_ascii=False))
        r.SetWords(True); r.AcceptWaveform(raw)
        seq = [(w["word"], w["start"], w["end"]) for w in json.loads(r.FinalResult()).get("result", []) if w["word"] != "[unk]"]
        out.append(dict(h=h, chars=chars, shift=shift, hz=track(x, 16000), seq=seq, tones=t, ok=sandhi(chars, t)))
pickle.dump(out, open(OUT, "wb")); print("done", len(out))
