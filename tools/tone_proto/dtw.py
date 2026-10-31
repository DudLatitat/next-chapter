import numpy as np, re
from proto import load, st
from proto2 import yin
def voiced_contour(x,sr):
    f=yin(x,sr); v=f>0
    if v.sum()<5: return None
    med=np.median(f[v])
    for i in np.where(v)[0]:
        while f[i]>med*1.7: f[i]/=2
        while f[i]<med/1.7: f[i]*=2
    g=f[v]
    # median smooth
    g=np.array([np.median(g[max(0,i-2):i+3]) for i in range(len(g))])
    return g
def dtw(a,b):
    n,m=len(a),len(b); D=np.full((n+1,m+1),np.inf); D[0,0]=0
    for i in range(1,n+1):
        lo=max(1,int(i*m/n)-int(0.3*m)-2); hi=min(m,int(i*m/n)+int(0.3*m)+2)
        for j in range(lo,hi+1):
            c=abs(a[i-1]-b[j-1]); D[i,j]=c+min(D[i-1,j],D[i,j-1],D[i-1,j-1])
    # path length normalisation
    return D[n,m]/(n+m)
def score(user_st, ref_st):
    # both: semitones vs own baseline; slope features too
    d=dtw(user_st, ref_st)
    return 100*np.exp(-d/1.6), d
src=open("/home/claude/next-chapter/app/src/main/java/com/dudlatitat/nextchapter/data/Seed.kt").read()
phr=re.findall(r'c\("\w+", "([^"]+)"',src)[:30]
ref={};usr={}
for p in phr:
    r=voiced_contour(*load(p)); u=voiced_contour(*load(p,0.62))
    if r is None or u is None: continue
    ref[p]=st(r,248); usr[p]=st(u,248*0.62)
same=[score(usr[p],ref[p])[0] for p in ref]
rng=np.random.default_rng(0); keys=list(ref)
diff=[score(usr[keys[i]],ref[keys[(i+7)%len(keys)]])[0] for i in range(len(keys))]
print("same phrase, other voice: mean %.0f min %.0f"%(np.mean(same),np.min(same)))
print("different phrase:         mean %.0f max %.0f"%(np.mean(diff),np.max(diff)))

def dtw2(a,b,w=0.2):
    da=np.gradient(a)*4; db=np.gradient(b)*4
    n,m=len(a),len(b); D=np.full((n+1,m+1),np.inf); D[0,0]=0
    for i in range(1,n+1):
        lo=max(1,int(i*m/n)-int(w*m)-2); hi=min(m,int(i*m/n)+int(w*m)+2)
        for j in range(lo,hi+1):
            c=abs(a[i-1]-b[j-1])+abs(da[i-1]-db[j-1]); D[i,j]=c+min(D[i-1,j]+0.5,D[i,j-1]+0.5,D[i-1,j-1])
    return D[n,m]/(n+m)
def score2(u,r):
    d=dtw2(u,r); lr=abs(np.log(len(u)/len(r)))
    return 100*np.exp(-(d+1.5*max(0,lr-0.25))/1.8)
same=[score2(usr[p],ref[p]) for p in ref]
diff=[score2(usr[keys[i]],ref[keys[(i+k)%len(keys)]]) for k in (3,7,11) for i in range(len(keys))]
print("v2 same: mean %.0f min %.0f | diff: mean %.0f p90 %.0f max %.0f"%(np.mean(same),np.min(same),np.mean(diff),np.percentile(diff,90),np.max(diff)))
