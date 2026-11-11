"""
Trains the phrase tone model (Judge.phraseTones in the app) from phrase_collect.py's data and
writes app/.../speech/PhraseToneModel.kt.

Why: tones inside phrases don't look like tones said alone (a 4th tone before another syllable
only half-falls, a 3rd tone stays low without rising), so the single-syllable templates only
got ~60% right on native phrases. This small linear model looks at each character's pitch shape
and how it joins its neighbours.

  pip install numpy scikit-learn
  python3 phrase_tones.py data.pkl ../../app/src/main/java/com/dudlatitat/nextchapter/speech/PhraseToneModel.kt [voices]
"""
import sys, pickle, numpy as np
from pitch_kt import syllable
from sklearn.linear_model import LogisticRegression

BASE = {}  # (voice, shift) -> that speaker's normal pitch in Hz, set below

def contours(d):
    """Per character: 20-point pitch shape in semitones vs the speaker's normal pitch.

    Each voice is measured against its own normal pitch (like the app does for you after voice
    setup). 1.69 measured every voice against the app voice's 248 Hz, so the male voices looked
    an octave "low" everywhere and the model got worse for everyone."""
    base = BASE[(d.get("voice", "app"), d["shift"])]
    hz = d["hz"]
    if "".join(w for w, _, _ in d["seq"]) != d["chars"]: return None
    out = []
    for (_, s, e) in d["seq"]:
        c = syllable(hz[int(s / 0.01):int(e / 0.01)])  # same as Judge.slice
        out.append(None if c is None else 12 * np.log2(c / base))
    return out

def features(cs, i):
    """Must match Judge.phraseFeatures exactly."""
    c = cs[i]; p = cs[i - 1] if i > 0 else None; n = cs[i + 1] if i + 1 < len(cs) else None
    return [c[0], c[4], c[8], c[12], c[16],
            c[19] - c[0], c[10] - c[0], c[19] - c[10], c.min(), c.max(), int(np.argmin(c)) / 19,
            (c[0] - p[19]) if p is not None else 0, 1 if p is not None else 0,
            (n[0] - c[19]) if n is not None else 0, 1 if n is not None else 0,
            1 if i == len(cs) - 1 else 0, 1 if i == 0 else 0]

def rows(data, keep):
    X, y, ok = [], [], []
    for d in data:
        if not keep(d): continue
        cs = contours(d)
        if cs is None: continue
        for i in range(len(cs)):
            if cs[i] is None or not d["ok"][i]: continue
            # The tone natives actually say: the sandhi one when there is one (你好 → ní).
            said = d["ok"][i] - {d["tones"][i]} or {d["tones"][i]}
            X.append(features(cs, i)); y.append(min(said)); ok.append(d["ok"][i])
    return np.array(X, float), np.array(y), ok

def fit(X, y):
    mu, sd = X.mean(0), X.std(0) + 1e-6
    return mu, sd, LogisticRegression(C=0.5, max_iter=5000).fit((X - mu) / sd, y)

APP_SCORES = []

def report(name, m, X, ok, sure=0.7):
    mu, sd, lr = m
    P = lr.predict_proba((X - mu) / sd); g = lr.classes_[P.argmax(1)]
    right = np.array([gg in o for gg, o in zip(g, ok)]); hi = P.max(1) >= sure
    if name.endswith("app voice"): APP_SCORES.append(right.mean())
    print(f"{name}: {right.mean():.0%} right; when sure (p≥{sure}): {right[hi].mean():.0%} on {hi.mean():.0%} of characters")

data = pickle.load(open(sys.argv[1], "rb"))
# Each speaker's normal pitch: the app voice keeps REFERENCE_BASE (248 Hz, what the app assumes before
# voice setup); every other voice is scaled by how its median pitch compares with the app voice's.
def median_hz(ds):
    v = np.concatenate([np.asarray(d["hz"])[np.asarray(d["hz"]) > 0] for d in ds] or [np.array([248.0])])
    return float(np.median(v)) if len(v) else 248.0
app_median = median_hz([d for d in data if d.get("voice", "app") == "app" and d["shift"] == 0])
for key in {(d.get("voice", "app"), d["shift"]) for d in data}:
    m = median_hz([d for d in data if (d.get("voice", "app"), d["shift"]) == key])
    BASE[key] = 248.0 * m / app_median
print("speaker bases (Hz):", {f"{k[0]} {k[1]:+d}": round(v) for k, v in sorted(BASE.items())})
phrases = sorted(set(d["h"] for d in data))
for k in range(2):  # 2-fold by phrase, so a phrase is never tested on itself
    train = set(phrases[k::2])
    m = fit(*rows(data, lambda d: d["h"] in train)[:2])
    groups = [("app voice", lambda d: d.get("voice", "app") == "app" and d["shift"] == 0),
              ("app voice -5 st", lambda d: d.get("voice", "app") == "app" and d["shift"] == -5),
              ("male voices", lambda d: "Yun" in d.get("voice", "")),
              ("other female", lambda d: "Xiaoyi" in d.get("voice", ""))]
    for name, g in groups:
        X, _, ok = rows(data, lambda d, g=g: d["h"] not in train and g(d))
        if len(X): report(f"fold {k}, {name}", m, X, ok)
X, y, _ = rows(data, lambda d: True)
mu, sd, lr = fit(X, y)
assert list(lr.classes_) == [1, 2, 3, 4]
f = lambda v: ", ".join(f"{x:.5f}f" for x in v)
# A worked example the Kotlin unit test repeats (same inputs must give the same answer).
cs = [np.linspace(3, 4, 20), np.linspace(4, -2, 20), np.linspace(-2, -3.5, 20)]
P = lr.predict_proba((np.array([features(cs, i) for i in range(3)]) - mu) / sd)
golden = [(int(lr.classes_[p.argmax()]), round(float(p.max()), 4)) for p in P]
n_phrases = len(set(d['h'] for d in data))
voices = sys.argv[3] if len(sys.argv) > 3 else "zh-CN-XiaoxiaoNeural"
app_acc = float(np.mean(APP_SCORES)) if APP_SCORES else 0.0
kt = f"""package com.dudlatitat.nextchapter.speech

// Generated by tools/tone_proto/phrase_tones.py from the app's native phrase recordings
// ({n_phrases} phrases, {len(y)} characters). Don't edit by hand: re-run the tool (CI does when 50+ phrases are new).
internal object PhraseToneModel {{
    /** How many phrases it was trained on (CI retrains when 50 or more have been added since). */
    const val PHRASES = {n_phrases}
    /** The voices it was trained on (CI retrains when the list changes). */
    const val VOICES = "{voices}"
    /** Held-out accuracy on the app's own voice: a retrained model only replaces this one if it does at least as well. */
    const val APP_ACCURACY = {app_acc:.2f}f
    val MU = floatArrayOf({f(mu)})
    val SD = floatArrayOf({f(sd)})
    val W = arrayOf(
{chr(10).join(f"        floatArrayOf({f(w)})," for w in lr.coef_)}
    )
    val B = floatArrayOf({f(lr.intercept_)})
    /** JudgeTest's worked example: the tone and probability Python gave for its three made-up characters. */
    val GOLDEN = listOf({", ".join(f"{t} to {p:.4f}f" for t, p in golden)})
}}
"""
# No-regression guard: keep the current model unless the new one does at least as well on the app's
# own voice (held out), within a point.
import os, re
old = 0.0
if os.path.exists(sys.argv[2]):
    m = re.search(r"APP_ACCURACY = ([0-9.]+)f", open(sys.argv[2]).read())
    old = float(m.group(1)) if m else 0.0
if app_acc + 0.01 < old:
    print(f"kept the old model: new {app_acc:.0%} < old {old:.0%} on the app voice")
    sys.exit(0)
open(sys.argv[2], "w").write(kt); print("wrote", sys.argv[2], f"(app voice {app_acc:.0%}, was {old:.0%})")
print("golden:", golden)
