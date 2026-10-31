import numpy as np, re, pickle
from proto import load, st
from proto2 import yin, clean
src=open("/home/claude/next-chapter/app/src/main/java/com/dudlatitat/nextchapter/data/Pinyin.kt").read()
items=re.findall(r'Triple\("(.)", "([^"]+)"',src)
T=["āēīōūǖ","áéíóúǘ","ǎěǐǒǔǚ","àèìòùǜ"]
tone=lambda p: next(i+1 for i,t in enumerate(T) if any(c in t for c in p))
def hzcontour(f,n=20):
    v=f[f>0]; k=max(1,len(v)//7); v=v[k:len(v)-max(1,len(v)//14)]
    return np.interp(np.linspace(0,len(v)-1,n),np.arange(len(v)),v)
data={}
for shift in (1.0,0.6):
    data[shift]=[(h,tone(p),hzcontour(clean(yin(*load(h,shift))))) for h,p in items]
pickle.dump(data,open("hz.pkl","wb"))
for shift,rows in data.items():
    base=np.median(np.concatenate([c for _,_,c in rows]))
    print("shift",shift,"base",round(base))
    for t in (1,2,3,4):
        cs=[st(c,base) for _,tt,c in rows if tt==t]
        lv=[c.mean() for c in cs]; lo=[c.min() for c in cs]; hi=[c.max() for c in cs]
        print(f" T{t} mean {np.mean(lv):5.1f} [{min(lv):.1f},{max(lv):.1f}]  min {np.mean(lo):5.1f}  max {np.mean(hi):5.1f}")
