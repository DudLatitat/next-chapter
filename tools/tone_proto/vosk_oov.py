"""
Lists the app's Chinese characters that Vosk's model doesn't know on their own (it ignores them
in a grammar, so they'd never be "heard"). Add a word containing each to Judge.modelWords.

  python3 vosk_oov.py <vosk-cn model dir> app/src/main/java/com/dudlatitat/nextchapter/data
"""
import sys, re, glob, json
from vosk import Model, KaldiRecognizer
chars = set()
for f in glob.glob(sys.argv[2] + "/*.kt"): chars |= set(re.findall(r'[一-鿿]', open(f).read()))
print(len(chars), "characters; Vosk lists the unknown ones as 'Ignoring word missing in vocabulary':", flush=True)
KaldiRecognizer(Model(sys.argv[1]), 16000, json.dumps(sorted(chars) + ["[unk]"], ensure_ascii=False))
