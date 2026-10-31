import numpy as np, re
from proto import load, st
def yin(x, sr, fmin=65, fmax=500, frame=0.04, hop=0.01, th=0.15, vth=0.35):
    N=int(frame*sr); H=int(hop*sr); tmin=int(sr/fmax); tmax=int(sr/fmin)
    f=[]; ap=[]; rms=[]
    for s in range(0, len(x)-N-tmax, H):
        w=x[s:s+N+tmax]; rms.append(np.sqrt(np.mean(x[s:s+N]**2)))
        d=np.array([np.sum((w[:N]-w[t:t+N])**2) for t in range(tmax+1)])
        cm=np.ones_like(d); cs=np.cumsum(d[1:]); cm[1:]=d[1:]*np.arange(1,tmax+1)/np.maximum(cs,1e-12)
        tau=-1
        for t in range(tmin,tmax):
            if cm[t]<th:
                while t+1<tmax and cm[t+1]<cm[t]: t+=1
                tau=t; break
        if tau<0: tau=tmin+int(np.argmin(cm[tmin:tmax]))
        a,b,c=cm[tau-1],cm[tau],cm[min(tau+1,tmax)]; den=a-2*b+c
        tt=tau+(0.5*(a-c)/den if den!=0 else 0)
        f.append(sr/tt); ap.append(cm[tau])
    f=np.array(f); ap=np.array(ap); rms=np.array(rms)
    gate=0.1*np.percentile(rms,95)
    f[(rms<gate)|(ap>vth)]=0
    return f
def clean(f):
    f=f.copy(); v=f>0
    if v.sum()<3: return f
    med=np.median(f[v])
    for i in np.where(v)[0]:
        while f[i]>med*1.7: f[i]/=2
        while f[i]<med/1.7: f[i]*=2
    g=f.copy()
    for i in np.where(v)[0]:
        win=[f[j] for j in range(max(0,i-2),min(len(f),i+3)) if f[j]>0]; g[i]=np.median(win)
    # keep the longest voiced run (+ runs separated by gaps <= 5 frames), drop specks
    runs=[];i=0
    while i<len(g):
        if g[i]>0:
            j=i
            while j<len(g) and g[j]>0: j+=1
            runs.append([i,j]); i=j
        else: i+=1
    merged=[]
    for r in runs:
        if merged and r[0]-merged[-1][1]<=5: merged[-1][1]=r[1]
        else: merged.append(r)
    if not merged: return g
    a,b=max(merged,key=lambda r:r[1]-r[0])
    out=np.zeros_like(g); seg=g[a:b].copy()
    # interpolate gaps inside
    idx=np.arange(len(seg)); vv=seg>0
    seg=np.interp(idx, idx[vv], seg[vv]); out[a:b]=seg
    return out
def contour(f, n=20):
    v=f[f>0]
    if len(v)<6: return None
    k=max(1,len(v)//12); v=v[k:len(v)-k]
    c=st(v, np.median(v))
    return np.interp(np.linspace(0,len(c)-1,n),np.arange(len(c)),c)
def feats(c):
    n=len(c); h=n//2
    return np.array([c[0], c[-1], c.min(), np.argmin(c)/n, c.max(), np.argmax(c)/n,
        np.polyfit(range(h),c[:h],1)[0]*n, np.polyfit(range(h),c[h:],1)[0]*n, c.max()-c.min()])
