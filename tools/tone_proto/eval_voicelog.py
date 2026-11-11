"""
Scores tone models on Rudy's own voice, from the voice log exported in Settings → Speaking check.

  python3 eval_voicelog.py voice-log.json model_a.kt [model_b.kt …]

A model file is a PhraseToneModel.kt (e.g. `git show <commit>:app/…/PhraseToneModel.kt > a.kt`).
There's no "true answer" for a learner (you may really have said the wrong tone), so it reports,
per model: how often it's sure enough to judge, how often it says "wrong tone" when it's sure,
how often models disagree, and the disagreements with their pitch shapes so they can be checked
by eye (a clear fall judged as a rise is a model mistake, not yours).
"""
import sys, json, re, numpy as np
import os
# The exact features the model uses (Judge.phraseFeatures), taken from the trainer without running it.
_src = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "phrase_tones.py")).read()
exec(_src[_src.index("def features"):_src.index("def rows")])

SURE = 0.7

def load(path):
    s = open(path).read()
    arr = lambda name: np.array([float(x) for x in re.search(name + r" = floatArrayOf\(([^)]*)\)", s).group(1).replace("f", "").split(",")])
    W = np.array([[float(x) for x in row.replace("f", "").split(",")] for row in re.findall(r"floatArrayOf\(([^)]*)\),", s.split("val W")[1].split(")\n    val B")[0])])
    return arr("MU"), arr("SD"), W, arr("B")

def judge(model, cs):
    mu, sd, W, B = model
    out = []
    for i in range(len(cs)):
        if cs[i] is None: out.append(None); continue
        z = W @ ((np.array(features(cs, i)) - mu) / sd) + B
        p = np.exp(z - z.max()); p /= p.sum()
        out.append((int(p.argmax()) + 1, float(p.max())))
    return out

log = json.load(open(sys.argv[1]))
models = {m: load(m) for m in sys.argv[2:]}
print(f"{len(log)} attempts, {sum(len(a['hanzi']) for a in log)} characters; calibrated: {sum(a['calibrated'] for a in log)}")
rows = []
for a in log:
    if len(a["hanzi"]) < 2: continue  # single characters use the syllable classifier, not this model
    cs = [None if c is None else np.array(c) for c in a["contours"]]
    res = {m: judge(mod, cs) for m, mod in models.items()}
    for i, ok in enumerate(a["accepted"]):
        if not ok or cs[i] is None: continue
        rows.append((a["hanzi"], i, ok, cs[i], {m: r[i] for m, r in res.items()}))
print(f"{len(rows)} judgeable characters in phrases\n")
for m in models:
    sure = [r for r in rows if r[4][m][1] >= SURE]
    wrong = [r for r in sure if r[4][m][0] not in r[2]]
    print(f"{m}: sure on {len(sure)/max(1,len(rows)):.0%}; of those, says 'wrong tone' on {len(wrong)/max(1,len(sure)):.0%}")
if len(models) == 2:
    a, b = models
    dis = [r for r in rows if r[4][a][0] != r[4][b][0]]
    print(f"\ndisagree on {len(dis)/max(1,len(rows)):.0%} of characters. Examples (expected → {a.split('/')[-1]} vs {b.split('/')[-1]}; shape start/min/end semitones):")
    for h, i, ok, c, g in dis[:25]:
        print(f"  {h}[{i}] {h[i]} expect {ok}: {g[a][0]} ({g[a][1]:.2f}) vs {g[b][0]} ({g[b][1]:.2f}) · {c[0]:+.1f} / {c.min():+.1f} / {c[-1]:+.1f}")
