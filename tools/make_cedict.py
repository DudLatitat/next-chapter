#!/usr/bin/env python3
"""
Builds the offline English <-> Chinese dictionary the app searches (assets/cedict.tsv) from
CC-CEDICT (https://www.mdbg.net/chinese/dictionary?page=cc-cedict), licensed CC BY-SA 4.0:
the credit is kept in the file's first line and shown in the app.

    python3 tools/make_cedict.py cedict_1_0_ts_utf-8_mdbg.txt.gz app/src/main/assets/cedict.tsv

Keeps simplified headwords made only of Chinese characters, drops names (capitalised pinyin),
pure "variant of / see / surname" entries and measure-word notes, turns numbered pinyin into
tone marks (ni3 hao3 -> nǐ hǎo). Output lines: hanzi <TAB> pinyin <TAB> meaning; meaning; …
The output is generated (gitignored): change this script, not the file.
"""
import gzip, re, sys

VOWELS = "aeiouü"
MARKS = {"a": "āáǎà", "e": "ēéěè", "i": "īíǐì", "o": "ōóǒò", "u": "ūúǔù", "ü": "ǖǘǚǜ"}


def mark(syl: str) -> str:
    """'hao3' -> 'hǎo', 'lu:4' -> 'lǜ', 'ma5' -> 'ma'."""
    syl = syl.replace("u:", "ü").replace("v", "ü")
    m = re.fullmatch(r"([a-zü]+)([1-5])", syl)
    if not m:
        return syl.replace("5", "")
    body, tone = m.group(1), int(m.group(2))
    if tone == 5:
        return body
    # Where the mark goes: a or e if present; o in "ou"; otherwise the last vowel.
    if "a" in body:
        i = body.index("a")
    elif "e" in body:
        i = body.index("e")
    elif "ou" in body:
        i = body.index("o")
    else:
        i = max(body.rfind(v) for v in VOWELS)
        if i < 0:
            return body
    return body[:i] + MARKS[body[i]][tone - 1] + body[i + 1:]


def pinyin(numbered: str) -> str:
    return " ".join(mark(s) for s in numbered.lower().split())


HAN = re.compile(r"^[一-鿿]+$")
SKIP_DEF = re.compile(r"^(old |archaic |erroneous )?(variant of|see |surname |used in |abbr\. for |CL:|Taiwan pr\.|also pr\.)", re.I)


def clean(defs):
    out = []
    for d in defs:
        d = d.strip()
        if not d or SKIP_DEF.match(d):
            continue
        d = re.sub(r"\s*\[[^\]]*\]", "", d)          # drop pinyin cross-references like [ni3]
        d = re.sub(r"\|[一-鿿]+", "", d)      # "traditional|simplified" references: keep simplified
        out.append(d)
    return out[:5]


def main(src, dst):
    kept = 0
    with gzip.open(src, "rt", encoding="utf-8") as f, open(dst, "w", encoding="utf-8") as out:
        out.write("#\tCC-CEDICT (mdbg.net), CC BY-SA 4.0\n")
        seen = set()
        for line in f:
            if line.startswith("#"):
                continue
            m = re.match(r"^(\S+) (\S+) \[([^\]]*)\] /(.*)/\s*$", line)
            if not m:
                continue
            simp, py, defs = m.group(2), m.group(3), m.group(4).split("/")
            if not HAN.match(simp) or len(simp) > 6:
                continue
            if py[:1].isupper():                    # names of people and places
                continue
            meaning = clean(defs)
            if not meaning:
                continue
            key = (simp, py.lower())
            if key in seen:
                continue
            seen.add(key)
            out.write(f"{simp}\t{pinyin(py)}\t{'; '.join(meaning)}\n")
            kept += 1
    print(f"wrote {kept} entries to {dst}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
