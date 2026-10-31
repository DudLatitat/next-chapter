import numpy as np, pickle
from clf import rel, templates, classify, data
rng=np.random.default_rng(1)
A=rel(data[1.0]); B=rel(data[0.6]); T=templates(A)
for name,fn in [("base +2st",lambda c:c-2),("base -2st",lambda c:c+2),("range x0.6 (flat beginner)",lambda c:c*0.6),
                ("noise 0.7st",lambda c:c+rng.normal(0,0.7,len(c))),("range x0.6 + base +1.5 + noise",lambda c:c*0.6-1.5+rng.normal(0,0.5,len(c)))]:
    ok=sum(classify(fn(c),T)[0]==t for _,t,c in B); print(f"{name:34s} {ok}/40")
