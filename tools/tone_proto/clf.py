import numpy as np, pickle
from proto import st
data=pickle.load(open("hz.pkl","rb"))
def rel(rows):
    base=np.median(np.concatenate([c for _,_,c in rows]))
    return [(h,t,st(c,base)) for h,t,c in rows]
def templates(rows): return {t:np.mean([c for _,tt,c in rows if tt==t],axis=0) for t in (1,2,3,4)}
def dist(c,tp):
    best=1e9
    for a in np.linspace(0.6,1.6,11):          # range: learners often use a narrower or wider span
        for b in np.linspace(-2,2,9):          # small level offset allowed, with a penalty
            m=a*tp+b
            d=np.mean((c-m)**2)+2*np.mean((np.diff(c)-np.diff(m))**2)+0.3*b*b+1.5*(a-1)**2
            best=min(best,d)
    return best
def classify(c,T):
    s={t:dist(c,T[t]) for t in T}; return min(s,key=s.get), s
def evaluate(train,test,loo=False):
    ok=0; conf={}
    for i,(h,t,c) in enumerate(test):
        tr=[r for j,r in enumerate(train) if not(loo and j==i)]
        got,_=classify(c,templates(tr)); ok+=got==t; conf[(t,got)]=conf.get((t,got),0)+1
    return ok,len(test),{k:v for k,v in conf.items() if k[0]!=k[1]}
A=rel(data[1.0]); B=rel(data[0.6])
print("LOO female      ",evaluate(A,A,True))
print("LOO male-shifted",evaluate(B,B,True))
print("female->male    ",evaluate(A,B))
T=templates(A)
np.set_printoptions(precision=2,suppress=True)
for t in T: print(t, np.round(T[t],2).tolist())
