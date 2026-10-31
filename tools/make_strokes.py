"""Collects stroke-order data for every character the app uses, into app/src/main/assets/strokes.json.

Data: hanzi-writer-data (npm), derived from Make Me a Hanzi; Arphic Public License.
Usage: python3 tools/make_strokes.py <path to the unpacked hanzi-writer-data package>
"""
import json, pathlib, re, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/main/java/com/dudlatitat/nextchapter/data"
OUT = ROOT / "app/src/main/assets/strokes.json"

def chars():
    found = set()
    for f in SRC.glob("*.kt"):
        found.update(c for c in f.read_text(encoding="utf-8") if "一" <= c <= "鿿")
    return sorted(found)

def main(pkg):
    pkg = pathlib.Path(pkg)
    data = {}
    for c in chars():
        f = pkg / f"{c}.json"
        if f.exists():
            d = json.loads(f.read_text(encoding="utf-8"))
            data[c] = {"s": d["strokes"], "m": d["medians"]}
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"{len(data)} characters, {OUT.stat().st_size // 1024} KB")

main(sys.argv[1])
