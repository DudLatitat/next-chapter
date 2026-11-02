# Line-for-line port of app/.../speech/Pitch.kt (track, syllable, classify).
import numpy as np
T = {
1:[3.50,3.63,3.71,3.76,3.80,3.84,3.87,3.89,3.92,3.95,3.99,4.02,4.06,4.10,4.12,4.15,4.19,4.21,4.21,4.23],
2:[-2.68,-2.80,-2.91,-2.99,-3.02,-3.03,-3.01,-2.94,-2.83,-2.63,-2.36,-2.03,-1.63,-1.03,-0.32,0.38,1.03,1.66,2.14,2.37],
3:[-2.18,-2.31,-2.48,-2.65,-2.81,-2.96,-3.08,-3.16,-3.20,-3.20,-3.17,-3.10,-2.97,-2.80,-2.58,-2.29,-2.02,-1.82,-1.61,-1.51],
4:[4.56,4.61,4.58,4.53,4.45,4.33,4.15,3.91,3.63,3.26,2.79,2.27,1.67,0.94,0.03,-0.92,-1.58,-2.08,-2.33,-2.26]}
T = {k: np.array(v) for k, v in T.items()}
def track(x, sr, fmin=65, fmax=500):
    n=int(0.04*sr); hop=int(0.01*sr); tmin=sr//fmax; tmax=sr//fmin
    frames=max(0,(len(x)-n-tmax)//hop); f=np.zeros(frames); ap=np.zeros(frames); rms=np.zeros(frames)
    for k in range(frames):
        s=k*hop; w=x[s:s+n]; rms[k]=np.sqrt(np.mean(w*w))
        d=np.array([np.sum((w-x[s+t:s+t+n])**2) for t in range(tmax+1)])
        cs=np.cumsum(d[1:]); cm=np.ones(tmax+1); t=np.arange(1,tmax+1)
        cm[1:]=np.where(cs>1e-12, d[1:]*t/np.maximum(cs,1e-30), 1.0)
        tau=-1; t=tmin
        while t<tmax:
            if cm[t]<0.15:
                while t+1<tmax and cm[t+1]<cm[t]: t+=1
                tau=t; break
            t+=1
        if tau<0: tau=tmin+int(np.argmin(cm[tmin:tmax]))
        a=cm[max(tau-1,0)]; b=cm[tau]; c=cm[min(tau+1,tmax)]; den=a-2*b+c
        tt=tau+(0.5*(a-c)/den if den!=0 else 0); f[k]=sr/tt; ap[k]=b
    if frames:
        gate=0.1*np.sort(rms)[int((frames-1)*0.95)]
        f[(rms<gate)|(ap>0.35)]=0
    return f
def median(v): return float(np.median(v)) if len(v) else 0.0
def resample(v,n):
    v=np.asarray(v); 
    if len(v)==1: return np.full(n,v[0])
    p=np.arange(n)*(len(v)-1)/(n-1); lo=np.minimum(p.astype(int),len(v)-2); return v[lo]+(v[lo+1]-v[lo])*(p-lo)
def syllable(f, points=20):
    g=f.copy(); vi=[i for i in range(len(g)) if g[i]>0]
    if len(vi)<6: return None
    med=median(g[vi])
    for i in vi:
        while g[i]>med*1.7: g[i]/=2
        while g[i]<med/1.7: g[i]*=2
    sm=g.copy()
    for i in vi:
        w=[g[j] for j in range(max(0,i-2),min(len(g),i+3)) if g[j]>0]; sm[i]=median(w)
    runs=[]; i=0
    while i<len(sm):
        if sm[i]>0:
            j=i
            while j<len(sm) and sm[j]>0: j+=1
            if runs and i-runs[-1][1]<=5: runs[-1][1]=j
            else: runs.append([i,j])
            i=j
        else: i+=1
    if not runs: return None
    b0,b1=max(runs,key=lambda r:r[1]-r[0]); seg=sm[b0:b1].copy()
    known=[k for k in range(len(seg)) if seg[k]>0]
    for k in range(len(seg)):
        if seg[k]<=0:
            lo=max([q for q in known if q<k],default=known[0]); hi=min([q for q in known if q>k],default=known[-1])
            seg[k]=seg[lo] if hi==lo else seg[lo]+(seg[hi]-seg[lo])*(k-lo)/(hi-lo)
    if len(seg)<6: return None
    a=max(1,len(seg)//7); b=len(seg)-max(1,len(seg)//14)
    return resample(seg[a:b],points)
def tdist(c,tp):
    best=1e9
    for ai in range(11):
        a=0.6+ai*0.1
        for bi in range(9):
            b=-2+bi*0.5; m=a*tp+b
            d=np.mean((c-m)**2)+2*np.sum((np.diff(c)-np.diff(m))**2)/(len(c)-1)+0.3*b*b+1.5*(a-1)**2
            best=min(best,d)
    return best
def classify(hz, base, scale=1.0, shift_level=0.0):
    c=syllable(hz)
    if c is None: return None
    st=12*np.log2(c/base)/scale - shift_level
    d=sorted((tdist(st,T[t]),t) for t in T); margin=d[1][0]-d[0][0]
    return d[0][1], margin/(margin+d[0][0]+0.5), st
