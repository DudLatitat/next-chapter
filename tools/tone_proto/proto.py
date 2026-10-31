import numpy as np, subprocess, hashlib, re, sys
VOICE="/tmp/apk/x/assets/voice/"
def load(text, shift=1.0, sr=16000):
    f=VOICE+hashlib.md5(text.encode()).hexdigest()+".mp3"
    af=f"asetrate=24000*{shift},aresample={sr}" if shift!=1.0 else f"aresample={sr}"
    raw=subprocess.run(["ffmpeg","-v","quiet","-i",f,"-af",af,"-ac","1","-f","s16le","-"],capture_output=True).stdout
    return np.frombuffer(raw,np.int16).astype(np.float32)/32768, sr

def yin(x, sr, fmin=65, fmax=500, frame=0.04, hop=0.01, th=0.15):
    N=int(frame*sr); H=int(hop*sr); tmin=int(sr/fmax); tmax=int(sr/fmin)
    out=[]; rms=[]
    for s in range(0, len(x)-N-tmax, H):
        w=x[s:s+N+tmax]
        r=np.sqrt(np.mean(x[s:s+N]**2)); rms.append(r)
        d=np.array([np.sum((w[:N]-w[t:t+N])**2) for t in range(tmax+1)])
        cm=np.ones_like(d); cs=np.cumsum(d[1:]); cm[1:]=d[1:]*np.arange(1,tmax+1)/np.maximum(cs,1e-12)
        tau=-1
        for t in range(tmin,tmax):
            if cm[t]<th:
                while t+1<tmax and cm[t+1]<cm[t]: t+=1
                tau=t; break
        if tau<0: out.append((0,1.0)); continue
        # parabolic
        a,b,c=cm[tau-1],cm[tau],cm[tau+1]; den=a-2*b+c
        tt=tau+(0.5*(a-c)/den if den!=0 else 0)
        out.append((sr/tt, cm[tau]))
    rms=np.array(rms); f=np.array([o[0] for o in out]); ap=np.array([o[1] for o in out])
    gate=max(0.02, 0.15*np.percentile(rms,95))
    f[(rms<gate)|(ap>th)]=0
    return f

def clean(f):
    f=f.copy(); v=f>0
    if v.sum()<3: return f
    med=np.median(f[v])
    # octave fix
    for i in np.where(v)[0]:
        while f[i]>med*1.8: f[i]/=2
        while f[i]<med/1.8: f[i]*=2
    # median filter 5 on voiced
    g=f.copy()
    for i in np.where(v)[0]:
        win=[f[j] for j in range(max(0,i-2),min(len(f),i+3)) if f[j]>0]
        g[i]=np.median(win)
    # drop isolated voiced runs < 4 frames
    i=0
    while i<len(g):
        if g[i]>0:
            j=i
            while j<len(g) and g[j]>0: j+=1
            if j-i<4: g[i:j]=0
            i=j
        else: i+=1
    return g

def st(f, ref): return 12*np.log2(f/ref)

TEMPL={1:[5,5],2:[3,5],3:[2,1,4],4:[5,1]}
def templ(t,n=20):
    pts=TEMPL[t]; xs=np.linspace(0,1,len(pts)); 
    return np.interp(np.linspace(0,1,n),xs,pts)
def classify(f, base=None):
    v=f[f>0]
    if len(v)<5: return None, {}
    # trim 10% edges (onset/offset glitches)
    k=max(1,len(v)//10); v=v[k:len(v)-k] if len(v)>12 else v
    ref = base if base else np.median(v)
    c=st(v, ref)
    c=np.interp(np.linspace(0,len(c)-1,20),np.arange(len(c)),c)
    scores={}
    for t in (1,2,3,4):
        tp=templ(t)
        # chao level -> semitone: 1 level ~ 2 st, centered
        tp_st=(tp-3)*2.0
        if base is None:  # shape only: remove means
            a=c-c.mean(); b=tp_st-tp_st.mean()
        else:
            a=c; b=tp_st
        d=np.mean((a-b)**2) + 3*np.mean((np.diff(a)-np.diff(b))**2)
        scores[t]=d
    return min(scores,key=scores.get), scores

if __name__=="__main__":
    src=open("/home/claude/next-chapter/app/src/main/java/com/dudlatitat/nextchapter/data/Pinyin.kt").read()
    items=re.findall(r'Triple\("(.)", "([^"]+)"',src)
    marks={c:i+1 for t in ["āēīōūǖ","áéíóúǘ","ǎěǐǒǔǚ","àèìòùǜ"] for i0,c in enumerate(t) for i in [ ["āēīōūǖ","áéíóúǘ","ǎěǐǒǔǚ","àèìòùǜ"].index(t) ]}
    for shift in (1.0, 0.62):
        ok=0; conf={}
        for h,p in items:
            tone=next((marks[c] for c in p if c in marks),5)
            x,sr=load(h,shift); f=clean(yin(x,sr))
            got,_=classify(f)
            ok+= got==tone; conf[(tone,got)]=conf.get((tone,got),0)+1
            if got!=tone and shift==1.0: print(" miss",h,p,tone,"->",got)
        print(f"shift {shift}: {ok}/{len(items)}", sorted(conf.items()))
