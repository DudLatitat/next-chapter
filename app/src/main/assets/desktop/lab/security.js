// IT Lab plug-ins: security (Security+ and A+ Core 2).
// Three labs: "Phish or legit?" (spot the red flags in made-up emails, texts and sign-in pages),
// "Log detective" (read short logs, name the attack and the first response) and
// "Malware removal order" (A+'s seven steps, then a ticket that uses them).
// Every company, person, address and domain here is made up. Domains use the reserved .example and .test
// names, so nothing points at a real site. Nothing loads from the internet.
(() => {
  "use strict";
  if (!window.Lab) return;

  const CSS = `
  .sec-calm *, .sec-calm *::before, .sec-calm *::after { animation: none !important; transition: none !important; }
  .sec-top { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 14px; }
  .sec-top h2 { margin: 0; font-size: 22px; }
  .sec-prog { margin-left: auto; display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--muted); }
  .sec-bar { width: 140px; height: 6px; border-radius: 4px; background: rgba(255,255,255,.07); overflow: hidden; }
  .sec-bar i { display: block; height: 100%; background: linear-gradient(90deg, var(--rose), var(--sun)); transition: width .5s; }
  .sec-dots { display: flex; gap: 5px; flex-wrap: wrap; margin-bottom: 14px; }
  .sec-dot { min-width: 30px; height: 30px; border-radius: 10px; border: 1px solid var(--line); background: rgba(255,255,255,.04); color: var(--muted);
             font: 600 12px ui-monospace, monospace; cursor: pointer; }
  .sec-dot:hover { background: rgba(255,255,255,.1); }
  .sec-dot.on { border-color: rgba(255,207,122,.6); color: var(--ink); background: rgba(255,207,122,.1); }
  .sec-dot.ok { color: #0d2a27; background: var(--jade); border-color: transparent; }
  .sec-dot.bad { color: #3a0d0d; background: rgba(255,107,107,.7); border-color: transparent; }
  .sec-dot.ok.on, .sec-dot.bad.on { box-shadow: 0 0 0 2px var(--sun); }
  .sec-grid { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 18px; align-items: start; }
  @media (max-width: 980px) { .sec-grid { grid-template-columns: 1fr; } }
  .sec-side { padding: 16px 18px; }
  .sec-side h4 { margin: 0 0 8px; }
  .sec-world { font-size: 13px; color: var(--muted); padding: 10px 14px; border-radius: 14px; background: rgba(142,148,242,.1); margin-bottom: 12px; }
  .sec-world b { color: #c5c9ff; font-weight: 600; }
  .sec-ctx { font-size: 14px; padding: 8px 12px; border-radius: 12px; background: rgba(255,207,122,.1); color: var(--sun); margin-bottom: 10px; }
  @keyframes sec-rise { from { opacity: 0; transform: translateY(8px); } }
  @keyframes sec-shake { 20%, 60% { transform: translateX(-5px); } 40%, 80% { transform: translateX(5px); } }
  @keyframes sec-pulse { 50% { box-shadow: 0 0 0 4px rgba(255,207,122,.55); } }
  .sec-shake { animation: sec-shake .35s; }
  .sec-verdict { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin: 10px 0; }
  .sec-verdict .btn { padding: 14px 8px; font-size: 16px; }
  .sec-verdict .ph { background: var(--coral); } .sec-verdict .lg { background: var(--jade); }
  .sec-verdict .btn[disabled] { opacity: .35; cursor: default; }
  .sec-verdict .btn.pick { opacity: 1; box-shadow: 0 0 0 3px var(--sun); }
  .sec-res { margin-top: 12px; padding: 12px 14px; border-radius: 16px; animation: sec-rise .4s both; }
  .sec-res.good { background: rgba(79,179,169,.12); border: 1px solid rgba(79,179,169,.35); }
  .sec-res.bad { background: rgba(255,107,107,.1); border: 1px solid rgba(255,107,107,.35); }
  .sec-res h4 { margin: 0 0 6px; }
  .sec-res ul { margin: 6px 0 0; padding-left: 0; list-style: none; }
  .sec-res li { margin: 7px 0; font-size: 13.5px; line-height: 1.45; display: grid; grid-template-columns: 22px 1fr; }
  .sec-res li q { quotes: none; font-weight: 600; }
  .sec-next { display: flex; gap: 8px; align-items: center; margin-top: 12px; }

  /* Message mock-ups (light, like a real inbox) */
  .sec-mail { border-radius: 12px; overflow: hidden; background: #fbfbfd; color: #1c1e24; box-shadow: 0 30px 70px -30px #000d; animation: sec-rise .4s both;
              font: 14px/1.5 "Segoe UI", system-ui, sans-serif; }
  .sec-mail .hd { padding: 14px 18px 10px; border-bottom: 1px solid #e4e6ee; background: #f2f3f8; }
  .sec-mail .subj { font-size: 18px; font-weight: 600; margin-bottom: 6px; }
  .sec-mail .meta { font-size: 13px; color: #555b6e; }
  .sec-mail .meta b { color: #1c1e24; font-weight: 600; }
  .sec-mail .att { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
  .sec-mail .att > span { display: inline-block; padding: 5px 10px; border-radius: 8px; background: #fff; border: 1px solid #d5d8e3; font-size: 13px; }
  .sec-mail .bd { padding: 14px 18px 18px; min-height: 150px; }
  .sec-mail .bd p { margin: 0 0 10px; }
  .sec-mail .small { font-size: 12px; color: #7a8094; }
  .sec-status { padding: 5px 12px; font: 12px ui-monospace, monospace; background: #e9ebf3; color: #3a3f52; border-top: 1px solid #d9dce8; min-height: 26px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .sec-browser .url { margin: 10px 14px; padding: 7px 12px; border-radius: 999px; background: #fff; border: 1px solid #d5d8e3; font: 13px ui-monospace, monospace; }
  .sec-browser .page { padding: 26px; display: grid; place-items: center; background: linear-gradient(160deg, #eef3ff, #f9f0ff); min-height: 280px; }
  .sec-browser .card { width: min(360px, 100%); background: #fff; border-radius: 14px; padding: 22px; box-shadow: 0 10px 30px -15px #0004; }
  .sec-browser .logo { font-weight: 700; font-size: 20px; color: #3b5bdb; margin-bottom: 4px; }
  .sec-browser .fld { margin-top: 10px; padding: 9px 11px; border: 1px solid #cfd3e0; border-radius: 8px; color: #8a90a3; font-size: 13.5px; }
  .sec-browser .go { margin-top: 14px; padding: 9px; text-align: center; border-radius: 8px; background: #3b5bdb; color: #fff; font-weight: 600; }
  .sec-phone { width: min(380px, 100%); margin: 0 auto; border-radius: 34px; padding: 14px; background: #0c0f1c; border: 1px solid #2b3150; box-shadow: 0 30px 70px -30px #000d; animation: sec-rise .4s both; }
  .sec-phone .scr { border-radius: 24px; background: #f4f5f9; color: #1c1e24; min-height: 340px; overflow: hidden; font: 14px/1.45 system-ui, sans-serif; }
  .sec-phone .who { text-align: center; padding: 12px; font-size: 13px; color: #555b6e; background: #e9ebf3; }
  .sec-phone .who b { display: block; color: #1c1e24; font-size: 15px; }
  .sec-phone .bub { margin: 12px; padding: 10px 13px; border-radius: 18px 18px 18px 6px; background: #fff; box-shadow: 0 2px 6px -3px #0003; max-width: 88%; }
  .sec-phone .time { text-align: center; font-size: 11px; color: #8a90a3; margin-top: 10px; }
  .sec-spot { cursor: pointer; border-radius: 4px; padding: 0 2px; margin: 0 -2px; transition: background .15s, box-shadow .15s; }
  .sec-spot:hover { background: rgba(255,207,122,.35); }
  .sec-link { color: #2b59c3; text-decoration: underline; }
  .sec-spot.mk { background: rgba(255,107,107,.18); box-shadow: 0 0 0 2px #e03131; }
  .sec-spot.found { background: rgba(79,179,169,.22); box-shadow: 0 0 0 2px #2b8a7e; }
  .sec-spot.missed { box-shadow: 0 0 0 2px #e03131; background: rgba(255,107,107,.12); outline: 2px dashed #e03131; outline-offset: 2px; }
  .sec-spot.fa { box-shadow: 0 0 0 2px #7048e8; background: rgba(112,72,232,.12); }
  .sec-spot.fine { box-shadow: 0 0 0 1px #9aa1b8; }
  .sec-spot.pulse { animation: sec-pulse 1.2s ease-in-out 3; }
  .sec-spot.done { cursor: default; }

  /* Log detective */
  .sec-log { border-radius: 14px; overflow: hidden; background: rgba(5,8,22,.94); border: 1px solid rgba(255,255,255,.09); box-shadow: 0 24px 60px -30px #000c; }
  .sec-log .tb { padding: 7px 14px; font-size: 12px; color: var(--muted); background: rgba(255,255,255,.045); border-bottom: 1px solid var(--line); }
  .sec-log .body { overflow: auto; padding: 8px 0; font: 12.5px/1.55 "Cascadia Mono", Consolas, ui-monospace, monospace; color: #cdd3f3; max-height: 420px; }
  .sec-ln { white-space: pre; padding: 0 14px 0 0; display: flex; }
  .sec-ln .n { width: 34px; flex: none; text-align: right; padding-right: 10px; color: #5c6488; user-select: none; }
  .sec-ln.h0 { color: #8890b8; }
  .sec-ln.hot { background: rgba(255,107,107,.16); color: #ffd0d0; box-shadow: inset 3px 0 0 var(--coral); }
  .sec-q { margin-top: 14px; padding: 16px 18px; }
  .sec-qgrid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; align-items: start; }
  @media (max-width: 900px) { .sec-qgrid { grid-template-columns: 1fr; } }
  .sec-q .ask { font-size: 17px; font-weight: 600; margin-bottom: 10px; }
  .sec-opts { display: grid; gap: 8px; }
  .sec-opt { display: grid; grid-template-columns: auto 1fr; gap: 10px; align-items: center; text-align: left; padding: 10px 12px; border-radius: 14px; border: 1px solid var(--line);
             background: rgba(255,255,255,.04); color: var(--ink); cursor: pointer; font: inherit; font-size: 14.5px; transition: background .15s, border-color .2s; }
  .sec-opt:hover { background: rgba(255,255,255,.09); }
  .sec-opt.no { border-color: rgba(255,107,107,.5); background: rgba(255,107,107,.08); }
  .sec-opt.yes { border-color: rgba(79,179,169,.7); background: rgba(79,179,169,.15); }
  .sec-opt[disabled] { cursor: default; }
  .sec-why { margin-top: 10px; font-size: 14px; padding: 10px 12px; border-radius: 12px; background: rgba(255,255,255,.05); animation: sec-rise .35s both; }
  .sec-why .tip { color: var(--sun); margin-top: 6px; }
  .sec-hint { margin-top: 10px; padding: 9px 12px; border-radius: 12px; background: rgba(142,148,242,.12); color: #c5c9ff; font-size: 14px; animation: sec-rise .35s both; }

  /* Malware order */
  .sec-order { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; align-items: start; }
  @media (max-width: 900px) { .sec-order { grid-template-columns: 1fr; } }
  .sec-pool, .sec-built { display: grid; gap: 8px; }
  .sec-card { display: grid; grid-template-columns: auto 1fr; gap: 2px 10px; align-items: center; text-align: left; padding: 11px 13px; border-radius: 14px; cursor: pointer;
              border: 1px solid var(--line); background: rgba(29,40,86,.6); color: var(--ink); font: inherit; transition: transform .12s, background .15s; }
  .sec-card:hover { background: rgba(29,40,86,.95); transform: translateY(-1px); }
  .sec-card b { font-weight: 600; } .sec-card small { grid-column: 2; color: var(--muted); font-size: 12.5px; }
  .sec-card kbd { grid-row: span 2; }
  .sec-slot { display: grid; grid-template-columns: 30px 1fr; gap: 10px; align-items: center; padding: 10px 12px; border-radius: 14px; border: 1px dashed rgba(255,255,255,.14); min-height: 46px; color: var(--muted); }
  .sec-slot .no { font: 700 15px ui-monospace, monospace; color: var(--muted); }
  .sec-slot.fill { border-style: solid; border-color: rgba(79,179,169,.45); background: rgba(79,179,169,.1); color: var(--ink); animation: sec-rise .3s both; }
  .sec-slot.fill .no { color: var(--jade); }
  .sec-slot.fill small { display: block; color: var(--muted); font-size: 12.5px; }
  .sec-story { padding: 16px 18px; font-size: 15px; }
  .sec-steprib { display: flex; gap: 4px; margin: 10px 0 2px; }
  .sec-steprib i { flex: 1; height: 6px; border-radius: 3px; background: rgba(255,255,255,.08); }
  .sec-steprib i.on { background: var(--sun); } .sec-steprib i.ok { background: var(--jade); } .sec-steprib i.bad { background: var(--coral); }
  `;
  function addCss() {
    if (document.getElementById("sec-css")) return;
    const s = document.createElement("style"); s.id = "sec-css"; s.textContent = CSS; document.head.appendChild(s);
  }
  function shuffle(a) { a = a.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; }
  const live = (S) => S && S.ctx && S.ctx.el && S.ctx.el.isConnected;
  const plainKey = (e) => !(e.ctrlKey || e.metaKey || e.altKey);
  function shake(ctx, node) { if (!ctx.calm && node) { node.classList.remove("sec-shake"); void node.offsetWidth; node.classList.add("sec-shake"); } }
  const top = (ctx) => window.scrollTo({ top: 0, behavior: ctx.calm ? "auto" : "smooth" });

  // ---------------------------------------------------------------- Phish or legit?
  // Markup: [[id|text]] is a clickable spot; {{id|link text|real address}} is a link whose real address shows on hover.
  // spots: id → [is it a red flag?, short label, what to learn]
  const WORLD = `You are <b>M. Lee</b> at <b>Acme</b> (acme.example). You bank with <b>Lumen Bank</b> (lumenbank.example), shop at <b>Parcelo</b> (parcelo.example) and keep files on <b>Cloudnest</b> (cloudnest.example).`;
  const PH = [
    { id: "bank", kind: "email", phish: 1, ctx: "In your inbox this morning.",
      from: "[[from|Lumen Bank Security &lt;security@lumenbank-alerts.test&gt;]]", subj: "[[subj|⚠ Your account has been suspended]]",
      body: `<p>[[greet|Dear Customer,]]</p><p>We detected unusual activity and have temporarily suspended your online banking.
        [[urg|You must verify your identity within 24 hours or your account will be permanently closed.]]</p>
        <p>{{link|https://lumenbank.example/verify|http://lumenbank.example.account-verify.test/login.php}}</p><p>Lumen Bank Security Team</p>`,
      spots: { from: [1, "Sender", "Look-alike domain: lumenbank-alerts.test is not lumenbank.example."], subj: [1, "Scary subject", "Fear in the subject line, to make you act before you think."],
        greet: [1, "\"Dear Customer\"", "Generic greeting. Your real bank knows your name."], urg: [1, "24-hour threat", "Urgency plus a threat: classic pressure."],
        link: [1, "The link", "The text shows lumenbank.example, but it really goes to lumenbank.example.account-verify.test. Read the domain from the right: it's account-verify.test."] } },
    { id: "parcel", kind: "sms", phish: 1, ctx: "You aren't expecting any package.", who: "[[snd|+1 (555) 013-8842]]",
      msgs: ["Parcelo: [[held|your package is on hold]] due to an unpaid [[fee|$1.99 redelivery fee]]. [[urg|Pay today or it will be returned to sender]]: {{link|parcelo-redeliver.test/p/8Kx|https://parcelo-redeliver.test/p/8Kx}}"],
      spots: { snd: [0, "Phone number", "Real companies usually text from a short code, but the number alone proves little. The rest of the message is what gives it away."],
        held: [1, "\"On hold\"", "You didn't order anything. An unexpected delivery problem is the hook."], fee: [1, "Tiny fee", "A small fee seems harmless. The real goal is your card number."],
        urg: [1, "\"Pay today\"", "Urgency again."], link: [1, "The link", "parcelo-redeliver.test is not parcelo.example."] } },
    { id: "ceo", kind: "email", phish: 1, ctx: "Acme's CEO is Dana Whitfield.",
      from: "[[from|Dana Whitfield &lt;dana.whitfield.ceo@freemail.example&gt;]]", subj: "Quick favor",
      body: `<p>Hi,</p><p>Are you at your desk? [[urg|I'm stuck in meetings all day and need this done in the next hour.]]</p>
        <p>[[gift|Please buy 5 × $100 gift cards for a client thank-you and send me photos of the codes on the back.]] [[secret|Keep this between us for now, it's a surprise.]]</p><p>[[sig|Sent from my phone]]</p>`,
      spots: { from: [1, "Free-mail sender", "Your CEO would write from acme.example, not a free mail account with her name in it."], urg: [1, "\"In the next hour\"", "Rush, and \"stuck in meetings\" so you can't call to check."],
        gift: [1, "Gift card codes", "Gift cards are untraceable cash. A request for gift card codes is almost always fraud."], secret: [1, "\"Keep this between us\"", "Secrecy stops you from checking with anyone."],
        sig: [0, "\"Sent from my phone\"", "Common on real emails too. On its own it proves nothing."] } },
    { id: "invoice", kind: "email", phish: 1, ctx: "You've never heard of this vendor.",
      from: "[[from|Accounts Receivable &lt;billing@vendor-invoices.test&gt;]]", subj: "[[subj|OVERDUE: Invoice #4471, final notice]]", att: ["[[att|📦 Invoice_4471.zip]]"],
      body: `<p>Hello,</p><p>Please see the attached invoice. [[threat|Late fees of 15% will be added if not paid by end of day.]]</p><p>[[pw|The file password is 4471.]]</p><p>Regards,<br>Accounts</p>`,
      spots: { from: [1, "Unknown sender", "A vendor you don't work with, sending you a bill."], subj: [1, "\"Final notice\"", "Pressure in the subject."],
        att: [1, "Zip attachment", "An unexpected .zip: a common way to slip malware past email filters. Don't open it."], threat: [1, "Late-fee threat", "Urgency and money pressure."],
        pw: [1, "Password for the zip", "A password-protected zip stops email scanners from looking inside. Big red flag."] } },
    { id: "maint", kind: "email", phish: 0, ctx: "In your inbox on Thursday.",
      from: "[[from|Acme IT &lt;it@acme.example&gt;]]", subj: "Planned maintenance: Saturday 10 pm–2 am",
      body: `<p>Hi all,</p><p>Email and the file server will be down for planned maintenance this Saturday from 10 pm to 2 am. [[noact|You don't need to do anything.]]</p>
        <p>[[help|Questions? Call the help desk at ext. 4410 or open a ticket on the IT portal from your bookmarks.]]</p><p>Acme IT</p>`,
      spots: { from: [0, "Sender", "Matches Acme's real domain, acme.example."], noact: [0, "No action needed", "It asks nothing: no link, no login, no attachment."],
        help: [0, "Help desk", "Points you to channels you already know instead of a link to click."] } },
    { id: "fakelogin", kind: "web", phish: 1, ctx: "You clicked a \"shared file\" link in an email.",
      url: "[[lock|🔒]] [[url|https://cloudnest.example.login-session.test/signin]]",
      page: `<div class="logo">☁ Cloudnest</div><div>[[lure|Someone shared “Salary review 2026.pdf” with you.]] Sign in to view it.</div>
        <div class="fld">Email</div><div class="fld">Password</div><div class="fld">[[card|Card number (to confirm your identity)]]</div><div class="go">Sign in</div>`,
      spots: { lock: [0, "Padlock", "The padlock only means the connection is encrypted. Scam sites get certificates too."],
        url: [1, "Address bar", "Read the domain from the right: this site is login-session.test, not cloudnest.example."],
        lure: [1, "Juicy lure", "A salary file someone \"shared\": bait to make you hurry."], card: [1, "Card number", "A file service never needs your card to sign you in."] } },
    { id: "reallogin", kind: "web", phish: 0, ctx: "You typed cloudnest.example yourself and clicked Sign in.",
      url: "[[lock|🔒]] [[url|https://login.cloudnest.example/]]",
      page: `<div class="logo">☁ Cloudnest</div><div>Sign in to your account</div><div class="fld">Email</div><div class="fld">Password</div><div class="go">Next</div>
        <p style="font-size:12.5px;color:#666;margin:12px 0 0">[[mfa|Next, we'll ask for the code from your authenticator app.]]</p>`,
      spots: { lock: [0, "Padlock", "Encrypted, which is good, but it's the domain that tells you who it is."],
        url: [0, "Address bar", "login is a subdomain of cloudnest.example, the real domain. The right-most part decides."],
        mfa: [0, "MFA code", "Asking for your MFA code on the real site you went to yourself is normal."] } },
    { id: "otp", kind: "sms", phish: 0, ctx: "You just signed in to Lumen Bank on your laptop.", who: "[[snd|52734]]",
      msgs: ["Lumen Bank: [[code|Your sign-in code is 482913.]] [[warn|Don't share this code with anyone. We will never call or text to ask for it.]]"],
      spots: { snd: [0, "Short code", "Banks send codes from a short code like this."], code: [0, "The code", "You just signed in yourself, so a code is expected."],
        warn: [0, "\"Don't share\"", "Exactly what a real bank says."] } },
    { id: "codescam", kind: "sms", phish: 1, ctx: "You haven't signed in anywhere today.", who: "[[snd|+1 (555) 017-2291]]",
      msgs: ["Lumen Bank ALERT: [[amt|A $900.00 purchase at ELECTRO-MART is pending.]] [[ask|If this wasn't you, reply with the 6-digit code we just sent]] [[urg|to cancel it now.]]",
        "<span style=\"font-size:12px;color:#8a90a3\">From 52734:</span><br>Lumen Bank: Your sign-in code is 715204. Don't share this code with anyone."],
      spots: { snd: [1, "Normal phone number", "The \"alert\" came from an ordinary number, while the real code came from the bank's short code."],
        amt: [1, "Scary charge", "A big pending charge, to panic you."], ask: [1, "Reply with the code", "The attacker is signing in as you right now and needs that code. Banks never ask for it."],
        urg: [1, "\"Now\"", "Urgency, again."] } },
    { id: "bonus", kind: "email", phish: 1, ctx: "Bonus letters are usually posted on the HR portal.",
      from: "[[from|Acme HR &lt;hr@acme-example.test&gt;]]", subj: "[[subj|Your 2026 bonus letter]]", att: ["[[att|🌐 Bonus_Letter.htm]]"],
      body: `<p>Hi [[name|M. Lee]],</p><p>Your bonus letter is attached. [[login|Open it and sign in with your Acme email password to view it.]]</p><p>HR Team</p>`,
      spots: { from: [1, "Sender", "acme-example.test is a look-alike of acme.example (a dash instead of a dot, different ending)."],
        subj: [0, "Bonus subject", "A bonus email can be real. The sender and the attachment are what give this one away."],
        att: [1, ".htm attachment", "An .htm attachment opens a web page from your disk, usually a fake sign-in form."],
        name: [0, "Your name", "Using your name doesn't make it real. Names are easy to find."],
        login: [1, "Password request", "A letter doesn't need your password. This is credential harvesting."] } },
    { id: "receipt", kind: "email", phish: 0, ctx: "You ordered headphones from Parcelo yesterday.",
      from: "[[from|Parcelo &lt;no-reply@parcelo.example&gt;]]", subj: "Your order #1182 has shipped",
      body: `<p>Hi M. Lee,</p><p>Good news: your Studio Headphones are on the way and should arrive Thursday.</p><p>{{link|Track your package|https://parcelo.example/orders/1182}}</p>
        <p class="small">[[foot|You're getting this because you placed an order at parcelo.example.]]</p>`,
      spots: { from: [0, "Sender", "parcelo.example: the real domain."], link: [0, "Tracking link", "Hover: it goes to parcelo.example/orders, the real site, and matches your order."],
        foot: [0, "Footer", "Matches something you really did. No payment, no password, no attachment."] } },
    { id: "quota", kind: "email", phish: 1, ctx: "Acme's mail admins write from it@acme.example.",
      from: "[[from|Mail Administrator &lt;admin@acme.example.mailbox-quota.test&gt;]]", subj: "[[subj|Mailbox 99% full: messages will be deleted]]", att: ["[[att|💿 Mailbox_Restore.iso]]"],
      body: `<p>[[greet|Dear user,]]</p><p>Your mailbox has exceeded its storage limit. {{link|Click here to increase your quota|http://mailbox-quota.test/acme/owa/login}} or run the attached restore tool.</p>`,
      spots: { from: [1, "Sender", "It starts with acme.example, but the real domain is at the end: mailbox-quota.test."], subj: [1, "Deletion threat", "Threat of losing your mail: pressure."],
        att: [1, ".iso attachment", "A disk image you didn't ask for. Attackers hide malware inside .iso files."], greet: [1, "\"Dear user\"", "Generic greeting from your \"own\" IT team."],
        link: [1, "The link", "Hover: it goes to mailbox-quota.test, not acme.example."] } },
  ];
  function spotify(ctx, s, item, st) {
    const cls = (id) => {
      const flag = item.spots[id] && item.spots[id][0], mk = st.marked.has(id);
      if (!st.done) return (mk ? "mk" : "") + (st.pulse === id ? " pulse" : "");
      return "done " + (flag ? (mk ? "found" : "missed") : (mk ? "fa" : "fine"));
    };
    return s.replace(/\{\{(\w+)\|([^|}]*)\|([^}]*)\}\}/g, (_, id, text, href) => `<span class="sec-spot sec-link ${cls(id)}" tabindex="0" data-s="${id}" data-href="${ctx.esc(href)}">${text}</span>`)
      .replace(/\[\[(\w+)\|([^\]]*)\]\]/g, (_, id, text) => `<span class="sec-spot ${cls(id)}" tabindex="0" data-s="${id}">${text}</span>`);
  }
  let PS = null;
  function phRender(ctx) {
    addCss();
    PS = { ctx, i: Math.min(+ctx.store.get("cur", 0) || 0, PH.length - 1) };
    phStart(PS.i);
  }
  function phStart(i) {
    PS.i = i; PS.ctx.store.set("cur", i); PS.marked = new Set(); PS.done = false; PS.verdict = null; PS.pulse = null; PS.hint = false;
    phDraw();
    setTimeout(() => PS && PS.ctx.report(`Phish or legit? Message ${i + 1} of ${PH.length}`), 80);
  }
  function phMock(ctx, it) {
    const sp = (x) => spotify(ctx, x, it, PS);
    const status = `<div class="sec-status" id="sec-st">Hover a link to see where it really goes.</div>`;
    if (it.kind === "email") return `<div class="sec-mail"><div class="hd"><div class="subj">${sp(it.subj)}</div>
        <div class="meta"><b>From:</b> ${sp(it.from)}<br><b>To:</b> m.lee@acme.example</div>${it.att ? `<div class="att">${it.att.map((a) => `<span>${sp(a)}</span>`).join("")}</div>` : ""}</div>
        <div class="bd">${sp(it.body)}</div>${status}</div>`;
    if (it.kind === "web") return `<div class="sec-mail sec-browser"><div class="url">${sp(it.url)}</div><div class="page"><div class="card">${sp(it.page)}</div></div>${status}</div>`;
    return `<div class="sec-phone"><div class="scr"><div class="who"><b>${sp(it.who)}</b>Text message</div><div class="time">Today 8:41 AM</div>
      ${it.msgs.map((m) => `<div class="bub">${sp(m)}</div>`).join("")}${status.replace("sec-status", "sec-status\" style=\"background:#e9ebf3;margin-top:30px")}</div></div>`;
  }
  function phDraw() {
    const { ctx } = PS, it = PH[PS.i], res = ctx.store.get("res", {}), right = Object.values(res).filter((r) => r.ok).length;
    ctx.el.classList.toggle("sec-calm", !!ctx.calm);
    const flags = Object.entries(it.spots).filter(([, v]) => v[0]);
    let side;
    if (!PS.done) {
      side = `<h4>Your call</h4><p class="tag" style="margin:0 0 6px">Click anything suspicious in the message (click again to unmark), then decide.</p>
        <div class="sec-verdict"><button class="btn ph" data-v="1"><kbd>1</kbd> Phish</button><button class="btn lg" data-v="0"><kbd>2</kbd> Legit</button></div>
        <div class="tag">Marked: ${PS.marked.size}</div>
        ${PS.hint ? `<div class="sec-hint">💡 ${flags.length ? `There ${flags.length === 1 ? "is 1 red flag" : `are ${flags.length} red flags`} here. One is glowing.` : "Look closely: nothing here asks you to do anything risky."}</div>` : ""}`;
    } else {
      const ok = PS.verdict === it.phish;
      const found = flags.filter(([id]) => PS.marked.has(id)).length;
      const list = Object.entries(it.spots).map(([id, [flag, label, note]]) => {
        const mk = PS.marked.has(id);
        const icon = flag ? (mk ? "✅" : "⭕") : (mk ? "🟣" : "▫️");
        const tail = flag ? (mk ? "" : " <span class=\"tag\">(missed)</span>") : (mk ? " <span class=\"tag\">(false alarm)</span>" : "");
        return `<li><span>${icon}</span><span><q>${ctx.esc(label)}</q>${tail}: ${ctx.esc(note)}</span></li>`;
      }).join("");
      side = `<div class="sec-verdict"><button class="btn ph ${PS.verdict === 1 ? "pick" : ""}" disabled>Phish</button><button class="btn lg ${PS.verdict === 0 ? "pick" : ""}" disabled>Legit</button></div>
        <div class="sec-res ${ok ? "good" : "bad"}"><h4>${ok ? "✓ Right" : "✗ Not quite"}: it's ${it.phish ? "a phish" : "legit"}.</h4>
        <div class="tag">${it.phish ? `Red flags found: ${found}/${flags.length}` : "No red flags in this one."}</div><ul>${list}</ul>
        <div class="sec-next"><button class="btn" id="sec-nx">Next message →</button><span class="tag"><kbd>Enter</kbd></span></div></div>`;
    }
    ctx.el.innerHTML = `
      <div class="sec-top"><span style="font-size:30px">🎣</span><div><h2>Phish or legit?</h2><div class="tag">Find the red flags, then make the call. Some are real.</div></div>
        <div class="sec-prog">${right}/${PH.length} judged right<div class="sec-bar"><i style="width:${(right / PH.length) * 100}%"></i></div></div></div>
      <div class="sec-dots">${PH.map((x, k) => `<button class="sec-dot ${res[x.id] ? (res[x.id].ok ? "ok" : "bad") : ""} ${k === PS.i ? "on" : ""}" data-j="${k}">${k + 1}</button>`).join("")}</div>
      <div class="sec-world">${WORLD}</div>
      <div class="sec-grid"><div><div class="sec-ctx">📍 ${ctx.esc(it.ctx)}</div>${phMock(ctx, it)}</div><div class="glass sec-side">${side}</div></div>`;
    ctx.el.querySelectorAll(".sec-dot").forEach((b) => b.onclick = () => phStart(+b.dataset.j));
    ctx.el.querySelectorAll(".sec-verdict .btn:not([disabled])").forEach((b) => b.onclick = () => phVerdict(+b.dataset.v));
    const st = ctx.el.querySelector("#sec-st");
    ctx.el.querySelectorAll(".sec-spot").forEach((sp) => {
      if (sp.dataset.href) {
        const show = () => { st.textContent = "🔗 " + sp.dataset.href; };
        sp.onmouseenter = show; sp.onfocus = show;
        sp.onmouseleave = () => { st.textContent = "Hover a link to see where it really goes."; };
      }
      const tog = () => {
        if (PS.done) return;
        const id = sp.dataset.s;
        PS.marked.has(id) ? PS.marked.delete(id) : PS.marked.add(id);
        if (PS.pulse === id) PS.pulse = null;
        phDraw();
        if (sp.dataset.href) { const s2 = ctx.el.querySelector("#sec-st"); if (s2) s2.textContent = "🔗 " + sp.dataset.href; }
      };
      sp.onclick = tog;
      sp.onkeydown = (e) => { if (e.key === " ") { e.preventDefault(); e.stopPropagation(); tog(); } };
    });
    const nx = ctx.el.querySelector("#sec-nx"); if (nx) nx.onclick = phNext;
  }
  function phVerdict(v) {
    if (PS.done) return;
    const it = PH[PS.i];
    PS.done = true; PS.verdict = v; PS.pulse = null;
    const flags = Object.entries(it.spots).filter(([, x]) => x[0]);
    const res = PS.ctx.store.get("res", {});
    res[it.id] = { ok: v === it.phish, found: flags.filter(([id]) => PS.marked.has(id)).length, of: flags.length };
    PS.ctx.store.set("res", res);
    const all = PH.every((x) => res[x.id] && res[x.id].ok);
    PS.ctx.toast(v === it.phish ? (all ? "Every message judged right. Phishers fear you. 🛡️" : "✓ Good call") : "Not quite: read why →", v !== it.phish);
    phDraw();
  }
  function phNext() { phStart((PS.i + 1) % PH.length); top(PS.ctx); }
  function phHint() {
    if (PS.done) return;
    const it = PH[PS.i];
    const miss = Object.entries(it.spots).find(([id, v]) => v[0] && !PS.marked.has(id));
    PS.hint = true; PS.pulse = miss ? miss[0] : null; phDraw();
  }
  Lab.register({
    id: "sec-phish", title: "Phish or legit?", icon: "🎣", exams: ["sec", "a2"],
    blurb: "12 made-up emails, texts and sign-in pages. Click the red flags, check where links really go, then call it.",
    render: phRender,
    progress: (ctx) => { const r = ctx.store.get("res", {}); const n = Object.values(r).filter((x) => x.ok).length; return Object.keys(r).length ? `${n}/${PH.length} right` : `${PH.length} messages`; },
    key(e) {
      if (!live(PS) || !plainKey(e)) return;
      if (e.key === "1" || e.key === "2") { e.preventDefault(); phVerdict(e.key === "1" ? 1 : 0); }
      else if ((e.key === "Enter" || e.key === "ArrowRight") && PS.done) { e.preventDefault(); phNext(); }
      else if (e.key === "h" || e.key === "H") phHint();
    },
    remote(cmd, v) {
      if (!live(PS)) return;
      if (cmd === "show") phHint();
      else if (cmd === "grade") { if (PS.done) phNext(); else if (v === 1 || v === 2) phVerdict(v === 1 ? 1 : 0); }
    },
  });

  // ---------------------------------------------------------------- Log detective
  // Example IPs are from the ranges reserved for documentation (192.0.2.x, 198.51.100.x, 203.0.113.x).
  const LOGS = [
    { id: "brute", title: "Windows Security log · FS01", hint: "Count how many times the same account fails, from where, and what happens at the end.",
      lines: ["Time      ID    Task                                   Account        Source IP",
        "02:13:01  4625  An account failed to log on            administrator  203.0.113.45", "02:13:02  4625  An account failed to log on            administrator  203.0.113.45",
        "02:13:03  4625  An account failed to log on            administrator  203.0.113.45", "02:13:04  4625  An account failed to log on            administrator  203.0.113.45",
        "02:13:06  4625  An account failed to log on            administrator  203.0.113.45", "02:13:07  4625  An account failed to log on            administrator  203.0.113.45",
        "02:13:09  4625  An account failed to log on            administrator  203.0.113.45", "02:13:11  4624  An account was successfully logged on  administrator  203.0.113.45",
        "07:58:40  4624  An account was successfully logged on  j.chen         10.0.0.23"], hot: [1, 2, 3, 4, 5, 6, 7, 8],
      q1: ["Brute-force attack that succeeded", "A user who mistyped a password once", "A port scan", "A normal backup job"],
      q2: ["Disable or reset the administrator account, block 203.0.113.45, and check what it did after 02:13:11", "Nothing: it logged on successfully, so it's fine", "Delete the Security log to clear the alerts", "Restart the server"],
      why: "4625 = failed logon, 4624 = successful logon. Seven failures in ten seconds for one account from one outside IP, then a success from that same IP: someone guessed the password.",
      tip: "Account lockout and MFA stop this. Never delete logs: they're evidence." },
    { id: "spray", title: "Windows Security log · DC01", hint: "Look at the account names. How many tries does each one get?",
      lines: ["Time      ID    Task                         Account    Source IP", "01:00:12  4625  An account failed to log on  a.kim      198.51.100.61",
        "01:04:40  4625  An account failed to log on  b.ortiz    198.51.100.61", "01:09:03  4625  An account failed to log on  c.ng       198.51.100.61",
        "01:13:37  4625  An account failed to log on  d.patel    198.51.100.61", "01:18:02  4625  An account failed to log on  e.rossi    198.51.100.61",
        "01:22:29  4625  An account failed to log on  f.haddad   198.51.100.61", "01:26:51  4625  An account failed to log on  g.silva    198.51.100.61"], hot: [1, 2, 3, 4, 5, 6, 7],
      q1: ["Password spraying: one common password tried on many accounts", "Brute force on a single account", "Someone with caps lock stuck on", "A DDoS attack"],
      q2: ["Block the source, look for any 4624 success from it, and make sure MFA is on", "Lock every account in the company", "Ignore it: no account failed more than once", "Turn off auditing of failed logons"],
      why: "One failure per account, a few minutes apart, all from one IP. Spraying stays under the lockout limit on purpose, so lockout alone won't catch it.",
      tip: "Exam tip: brute force = many passwords against one account; spraying = one password against many accounts." },
    { id: "sqli", title: "Web server access log · shop.example", hint: "%20 is a space. What does the attacker put after id=7 ?",
      lines: [`203.0.113.9 - - [14/Mar/2026:10:02:11 +0000] "GET /products.php?id=7 HTTP/1.1" 200 5120`,
        `203.0.113.9 - - [14/Mar/2026:10:02:19 +0000] "GET /products.php?id=7' HTTP/1.1" 500 312`,
        `203.0.113.9 - - [14/Mar/2026:10:02:25 +0000] "GET /products.php?id=7'%20OR%201=1-- HTTP/1.1" 200 98304`,
        `203.0.113.9 - - [14/Mar/2026:10:02:41 +0000] "GET /products.php?id=7%20UNION%20SELECT%20username,password%20FROM%20users-- HTTP/1.1" 200 20480`,
        `198.51.100.4 - - [14/Mar/2026:10:03:02 +0000] "GET /index.html HTTP/1.1" 200 4096`], hot: [1, 2, 3],
      q1: ["SQL injection", "Cross-site scripting (XSS)", "Directory traversal", "Brute-force login"],
      q2: ["Block the IP, check whether customer data was exposed (those big 200 responses), and fix the page with parameterized queries", "Restart the web server", "Delete the access log", "Change the site's colors"],
      why: "A lone ' made the page crash (500): it builds SQL from what you type. ' OR 1=1-- makes the condition always true and comments out the rest; UNION SELECT pulls another table. The large 200 responses mean data came back.",
      tip: "Fix: input validation and parameterized queries (prepared statements). A web application firewall helps." },
    { id: "trav", title: "Web server access log · files.example", hint: "What does ../ mean in a path?",
      lines: [`198.51.100.30 - - [14/Mar/2026:11:20:05 +0000] "GET /download.php?file=report.pdf HTTP/1.1" 200 88211`,
        `198.51.100.30 - - [14/Mar/2026:11:20:31 +0000] "GET /download.php?file=../config.php HTTP/1.1" 404 210`,
        `198.51.100.30 - - [14/Mar/2026:11:20:44 +0000] "GET /download.php?file=../../../../etc/passwd HTTP/1.1" 200 1893`,
        `198.51.100.30 - - [14/Mar/2026:11:21:02 +0000] "GET /download.php?file=../../../../etc/hosts HTTP/1.1" 200 221`], hot: [1, 2, 3],
      q1: ["Directory traversal", "SQL injection", "Cross-site scripting (XSS)", "A port scan"],
      q2: ["Block the IP, check which files were read, and fix the page so it only serves allowed file names", "Turn off HTTPS", "Nothing: those are just text files", "Reboot the server"],
      why: "../ means \"go up one folder\". The attacker climbs out of the download folder to read system files. The 200 replies mean it worked.",
      tip: "Fix: never build file paths from user input. Allow-list file names and check the final path stays in the folder." },
    { id: "xss", title: "Web server access log · blog.example", hint: "%3C is < and %3E is >. Decode the q= part.",
      lines: [`203.0.113.77 - - [14/Mar/2026:15:40:10 +0000] "GET /search?q=tea HTTP/1.1" 200 6022`,
        `203.0.113.77 - - [14/Mar/2026:15:40:22 +0000] "GET /search?q=%3Cb%3Ehello%3C/b%3E HTTP/1.1" 200 6031`,
        `203.0.113.77 - - [14/Mar/2026:15:40:37 +0000] "GET /search?q=%3Cscript%3Ealert(1)%3C/script%3E HTTP/1.1" 200 6044`,
        `203.0.113.77 - - [14/Mar/2026:15:41:15 +0000] "POST /comments HTTP/1.1" 302 0`], hot: [1, 2, 3],
      q1: ["Cross-site scripting (XSS)", "SQL injection", "Directory traversal", "Password spraying"],
      q2: ["Fix the page to encode what it shows and validate input, and check whether a script was saved in the comments", "Block port 443", "Delete the search page's log", "Nothing: alert(1) is harmless"],
      why: "<script>alert(1)</script> is the classic test: does the site echo code back into the page, where it would run in other visitors' browsers? The POST to /comments right after may have stored it.",
      tip: "XSS attacks the site's visitors; SQL injection attacks its database." },
    { id: "scan", title: "Firewall log · edge", hint: "Same source each time. What changes on every line?",
      lines: ["Time      Action  Proto  Source               Destination       Flags", "01:22:03  DROP    TCP    198.51.100.23:40112  192.0.2.10:21     SYN",
        "01:22:03  DROP    TCP    198.51.100.23:40113  192.0.2.10:22     SYN", "01:22:03  DROP    TCP    198.51.100.23:40114  192.0.2.10:23     SYN",
        "01:22:03  DROP    TCP    198.51.100.23:40115  192.0.2.10:25     SYN", "01:22:04  ALLOW   TCP    198.51.100.23:40116  192.0.2.10:80     SYN",
        "01:22:04  DROP    TCP    198.51.100.23:40117  192.0.2.10:139    SYN", "01:22:04  ALLOW   TCP    198.51.100.23:40118  192.0.2.10:443    SYN",
        "01:22:04  DROP    TCP    198.51.100.23:40119  192.0.2.10:445    SYN", "01:22:05  DROP    TCP    198.51.100.23:40120  192.0.2.10:3389   SYN"], hot: [1, 2, 3, 4, 5, 6, 7, 8, 9],
      q1: ["A port scan (reconnaissance)", "A DDoS attack", "A brute-force login", "Normal web browsing"],
      q2: ["Block the source, confirm only the needed ports (80, 443) are open, and watch for a follow-up attack", "Open every port so nothing gets dropped", "Unplug the firewall", "Reset every user's password"],
      why: "One source touching many destination ports within two seconds, each with a single SYN: it's mapping what's open. Usually the step before an attack.",
      tip: "Exam tip: 21 FTP, 22 SSH, 23 Telnet, 25 SMTP, 80 HTTP, 139 NetBIOS, 443 HTTPS, 445 SMB, 3389 RDP." },
    { id: "travel", title: "Cloud sign-in log · j.chen", hint: "How far apart are the two places, and how many minutes?",
      lines: ["Time (UTC)  User    Result   Location         IP            App", "16:02       j.chen  Success  Los Angeles, US  192.0.2.44    Mail",
        "16:20       j.chen  Success  Frankfurt, DE    203.0.113.80  Mail", "16:21       j.chen  Success  Frankfurt, DE    203.0.113.80  Files (412 downloads)",
        "16:35       j.chen  Success  Los Angeles, US  192.0.2.44    Mail"], hot: [2, 3],
      q1: ["Impossible travel: someone else is using her account", "She took a very fast flight", "A port scan", "SQL injection"],
      q2: ["Revoke her sessions, reset her password, check her MFA, and confirm with her in person or by phone (not by email)", "Email her account to ask if it was her", "Wait a week to see if it happens again", "Delete her account"],
      why: "Los Angeles and Frankfurt 18 minutes apart is impossible. A VPN can cause false alarms, so check, but 412 downloads right after makes this serious.",
      tip: "Don't ask by email: if the account is stolen, the attacker reads the reply." },
    { id: "mfa", title: "Identity provider log · j.chen", hint: "The password part is already working for someone. What do they keep sending?",
      lines: ["Time      User    Event                 Result", "02:14:05  j.chen  MFA push sent          Denied", "02:14:41  j.chen  MFA push sent          Denied",
        "02:15:20  j.chen  MFA push sent          Denied", "02:16:02  j.chen  MFA push sent          No response", "02:17:30  j.chen  MFA push sent          Denied",
        "02:19:58  j.chen  MFA push sent          Approved", "02:20:10  j.chen  Sign-in               Success  203.0.113.80"], hot: [1, 2, 3, 4, 5, 6, 7],
      q1: ["MFA fatigue (push bombing): the attacker has the password and spams prompts until someone taps Approve", "The MFA app is broken", "Brute force on the password", "A port scan"],
      q2: ["Revoke the session, reset her password (the attacker knows it), and switch to number matching", "Turn MFA off since it's annoying her", "Tell her to approve faster next time", "Nothing: MFA approved it"],
      why: "Each push means someone typed the right password. At 2 am, after five refusals, a sleepy tap on Approve let them in.",
      tip: "Number matching (type the number shown on screen) stops blind approvals." },
    { id: "ssh", title: "/var/log/auth.log · web01 (Linux)", hint: "Read the user names after \"invalid user\".",
      lines: ["Mar 14 03:01:12 web01 sshd[2211]: Failed password for invalid user admin from 198.51.100.7 port 51122 ssh2",
        "Mar 14 03:01:14 web01 sshd[2213]: Failed password for invalid user test from 198.51.100.7 port 51130 ssh2",
        "Mar 14 03:01:15 web01 sshd[2215]: Failed password for invalid user oracle from 198.51.100.7 port 51138 ssh2",
        "Mar 14 03:01:17 web01 sshd[2217]: Failed password for invalid user pi from 198.51.100.7 port 51146 ssh2",
        "Mar 14 03:01:19 web01 sshd[2219]: Failed password for root from 198.51.100.7 port 51154 ssh2",
        "Mar 14 03:01:21 web01 sshd[2221]: Failed password for root from 198.51.100.7 port 51162 ssh2"], hot: [0, 1, 2, 3, 4, 5],
      q1: ["A dictionary / brute-force attack on SSH", "A port scan", "Phishing", "A user who changed their password"],
      q2: ["Block the IP (e.g. fail2ban), use SSH keys instead of passwords, and turn off root login over SSH", "Switch to Telnet on port 23", "Nothing: every attempt failed", "Reboot the server"],
      why: "Common default user names (admin, test, oracle, pi, root) tried every two seconds from one IP: a script working through a word list.",
      tip: "Servers facing the internet see this all day. Keys plus no root login make it harmless." },
    { id: "backdoor", title: "Windows Security log · FS01", hint: "4720, 4732 and 1102: what does each one do, and in what order?",
      lines: ["Time      ID    Task                                                   Details", "23:47:02  4624  An account was successfully logged on                  svc-backup from 10.0.0.57",
        "23:47:40  4720  A user account was created                             by svc-backup: helpdesk2", "23:47:41  4732  A member was added to a security-enabled local group   helpdesk2 → Administrators",
        "23:48:15  1102  The audit log was cleared                              by svc-backup"], hot: [2, 3, 4],
      q1: ["A compromised account made a hidden admin account, then cleared the log to cover its tracks", "Normal onboarding of a new help-desk tech", "A failed-logon storm", "A port scan"],
      q2: ["Disable helpdesk2, reset svc-backup's password, isolate the server, and keep any log copies as evidence", "Delete helpdesk2 and move on", "Give helpdesk2 a stronger password", "Ignore it: the log was cleared, so nothing's left"],
      why: "4720 = account created, 4732 = added to a local security group (here Administrators), 1102 = audit log cleared. Near midnight, by a service account, then the log wiped: a backdoor for later plus covering tracks.",
      tip: "Service accounts shouldn't create users. Forwarding logs to a separate server (a SIEM) keeps copies an attacker can't wipe." },
  ];
  let LG = null;
  function lgRender(ctx) {
    addCss();
    LG = { ctx, i: Math.min(+ctx.store.get("cur", 0) || 0, LOGS.length - 1) };
    lgStart(LG.i);
  }
  function lgStart(i) {
    const c = LOGS[i];
    LG.i = i; LG.ctx.store.set("cur", i); LG.stage = 1; LG.hint = false; LG.wrong = 0;
    LG.o1 = shuffle([0, 1, 2, 3]); LG.o2 = shuffle([0, 1, 2, 3]); LG.a1 = null; LG.a2 = null;
    lgDraw();
    setTimeout(() => LG && LG.ctx.report(`Log detective: ${c.title}`), 80);
  }
  function lgOpts(ctx, list, order, ans, done) {
    return `<div class="sec-opts">${order.map((o, k) => {
      const cls = done ? (o === 0 ? "yes" : o === ans ? "no" : "") : "";
      return `<button class="sec-opt ${cls}" data-k="${k}" ${done ? "disabled" : ""}><kbd>${k + 1}</kbd><span>${ctx.esc(list[o])}</span></button>`;
    }).join("")}</div>`;
  }
  function lgDraw() {
    const { ctx } = LG, c = LOGS[LG.i], res = ctx.store.get("res", {}), n = Object.values(res).filter(Boolean).length;
    ctx.el.classList.toggle("sec-calm", !!ctx.calm);
    const showHot = LG.stage > 1 || LG.hint;
    const head = /^Time/.test(c.lines[0]);
    const log = `<div class="sec-log"><div class="tb">📄 ${ctx.esc(c.title)}</div><div class="body">${c.lines.map((l, k) => {
      const isHead = head && k === 0;
      return `<div class="sec-ln ${showHot && c.hot.includes(k) ? "hot" : ""} ${isHead ? "h0" : ""}"><span class="n">${isHead ? "" : head ? k : k + 1}</span>${ctx.esc(l)}</div>`;
    }).join("")}</div></div>`;
    let qs = `<div class="glass sec-q"><div class="ask">1. What's going on?</div>${lgOpts(ctx, c.q1, LG.o1, LG.a1, LG.stage > 1)}
      ${LG.hint && LG.stage === 1 ? `<div class="sec-hint">💡 ${ctx.esc(c.hint)}</div>` : ""}
      ${LG.stage > 1 ? `<div class="sec-why">${LG.a1 === 0 ? "✓" : "✗"} It's <b>${ctx.esc(c.q1[0])}</b>. ${ctx.esc(c.why)}</div>` : ""}</div>`;
    if (LG.stage > 1) qs += `<div class="glass sec-q"><div class="ask">2. Best first response?</div>${lgOpts(ctx, c.q2, LG.o2, LG.a2, LG.stage > 2)}
      ${LG.stage > 2 ? `<div class="sec-why">${LG.a2 === 0 ? "✓" : "✗"} ${ctx.esc(c.q2[0])}.<div class="tip">${ctx.esc(c.tip)}</div></div>
        <div class="sec-next"><button class="btn" id="sec-lnx">Next log →</button><span class="tag"><kbd>Enter</kbd></span></div>` : ""}</div>`;
    ctx.el.innerHTML = `
      <div class="sec-top"><span style="font-size:30px">🕵️</span><div><h2>Log detective</h2><div class="tag">Read the log. Name the attack. Pick the first response. Keys 1–4.</div></div>
        <div class="sec-prog">${n}/${LOGS.length} cracked<div class="sec-bar"><i style="width:${(n / LOGS.length) * 100}%"></i></div></div></div>
      <div class="sec-dots">${LOGS.map((x, k) => `<button class="sec-dot ${x.id in res ? (res[x.id] ? "ok" : "bad") : ""} ${k === LG.i ? "on" : ""}" data-j="${k}">${k + 1}</button>`).join("")}</div>
      ${log}<div class="sec-qgrid">${qs}</div>`;
    ctx.el.querySelectorAll(".sec-dot").forEach((b) => b.onclick = () => lgStart(+b.dataset.j));
    ctx.el.querySelectorAll(".sec-opt:not([disabled])").forEach((b) => b.onclick = () => lgPick(+b.dataset.k));
    const nx = ctx.el.querySelector("#sec-lnx"); if (nx) nx.onclick = () => { lgStart((LG.i + 1) % LOGS.length); top(ctx); };
  }
  function lgPick(k) {
    if (k < 0 || k > 3) return;
    const c = LOGS[LG.i];
    if (LG.stage === 1) { LG.a1 = LG.o1[k]; LG.stage = 2; }
    else if (LG.stage === 2) {
      LG.a2 = LG.o2[k]; LG.stage = 3;
      const res = LG.ctx.store.get("res", {}); const ok = LG.a1 === 0 && LG.a2 === 0;
      if (!res[c.id]) { res[c.id] = ok; LG.ctx.store.set("res", res); }
      LG.ctx.toast(ok ? "Case cracked 🕵️" : "Read why, then try the next one", !ok);
    } else return;
    lgDraw();
  }
  Lab.register({
    id: "sec-logs", title: "Log detective", icon: "🕵️", exams: ["sec", "a2"],
    blurb: "Short real-looking logs: failed logons, SQL injection, a port scan, impossible travel… Name the attack and the first move.",
    render: lgRender,
    progress: (ctx) => { const r = ctx.store.get("res", {}); return Object.keys(r).length ? `${Object.values(r).filter(Boolean).length}/${LOGS.length} cracked` : `${LOGS.length} logs`; },
    key(e) {
      if (!live(LG) || !plainKey(e)) return;
      if (/^[1-4]$/.test(e.key)) { e.preventDefault(); lgPick(+e.key - 1); }
      else if ((e.key === "Enter" || e.key === "ArrowRight") && LG.stage === 3) { e.preventDefault(); lgStart((LG.i + 1) % LOGS.length); top(LG.ctx); }
      else if (e.key === "h" || e.key === "H") { LG.hint = true; lgDraw(); }
    },
    remote(cmd, v) {
      if (!live(LG)) return;
      if (cmd === "show") { LG.hint = true; lgDraw(); }
      else if (cmd === "grade") { if (LG.stage === 3) { lgStart((LG.i + 1) % LOGS.length); top(LG.ctx); } else lgPick(v - 1); }
    },
  });

  // ---------------------------------------------------------------- Malware removal order (A+ Core 2)
  const STEPS = [
    ["Investigate and verify malware symptoms", "Make sure it really is malware before you change anything."],
    ["Quarantine infected systems", "Cut it off from the network so it can't spread."],
    ["Disable System Restore in Windows", "Restore points may hold the malware. Turning it off clears them."],
    ["Remediate infected systems", "Update the anti-malware software, then scan and remove (safe mode, preinstallation environment)."],
    ["Schedule scans and run updates", "Keep it clean: regular scans, Windows and app updates."],
    ["Enable System Restore and create a restore point in Windows", "A fresh, clean point to come back to."],
    ["Educate the end user", "How it got in, and how to spot it next time."],
  ];
  const STORY = "Ticket: \"My browser's homepage changed by itself, pop-ups everywhere, and the PC is really slow.\" Her antivirus showed a warning yesterday that she closed.";
  const QS = [
    { ask: "Where do you start?", opts: ["Confirm the symptoms: browser extensions and homepage, Task Manager for unknown processes, the antivirus alerts", "Reinstall Windows right away", "Run Disk Cleanup", "Tell her it's probably nothing"],
      why: "Step 1: investigate and verify. Slow PCs and pop-ups have other causes too; confirm before acting." },
    { ask: "It's malware, and it's trying to connect to other PCs on the network. Next?", opts: ["Disconnect it from the network: unplug Ethernet, turn off Wi-Fi", "Keep it online so the antivirus can update", "Copy her files to the team share first", "Turn System Restore on"],
      why: "Step 2: quarantine. Stop the spread first. You can update the anti-malware later from a clean source." },
    { ask: "Before cleaning, you turn off System Restore. Why?", opts: ["Restore points can contain the malware, so restoring later could bring it back", "It makes scans faster", "Safe Mode needs it off", "It frees up RAM"],
      why: "Step 3: disable System Restore. Turning it off deletes the old (possibly infected) restore points." },
    { ask: "The malware restarts itself every time you scan in normal Windows. What now?", opts: ["Update the anti-malware, then scan from Safe Mode or a preinstallation environment (boot scan)", "Scan again in normal Windows, faster", "Delete files in System32 that look odd", "Skip ahead and turn System Restore back on"],
      why: "Step 4: remediate. Fresh definitions first; then scan where the malware can't run, like Safe Mode or a boot-time/PE scan." },
    { ask: "It's clean. Before handing it back:", opts: ["Schedule regular scans and run Windows and app updates", "Turn off automatic updates so nothing changes", "Uninstall the antivirus now that it's clean", "Give her admin rights so she can fix things herself"],
      why: "Step 5: schedule scans and run updates, so it stays clean and the hole it used gets patched." },
    { ask: "And in Windows, one more step:", opts: ["Turn System Restore back on and create a new, clean restore point", "Leave System Restore off for good", "Restore to the oldest restore point", "Delete the recovery partition"],
      why: "Step 6: enable System Restore and create a restore point, so there's a clean point to return to." },
    { ask: "Last step?", opts: ["Show her how it got in (a fake download button) and how to spot it next time", "Close the ticket without a word", "Quietly take away her internet access", "Reinstall her browser again"],
      why: "Step 7: educate the end user. The best scanner is a user who doesn't click the fake button." },
  ];
  let MW = null;
  function mwRender(ctx) {
    addCss();
    MW = { ctx };
    mwStart("order");
  }
  function mwStart(phase) {
    MW.phase = phase; MW.pool = shuffle(STEPS.map((_, k) => k)); MW.built = []; MW.miss = 0; MW.hint = false; MW.say = "";
    MW.q = 0; MW.right = 0; MW.ans = null; MW.qo = null; MW.marks = [];
    if (phase === "story") mwQ();
    mwDraw();
    setTimeout(() => MW && MW.ctx.report(phase === "order" ? "Malware removal: put the 7 steps in order" : "Malware removal: the pop-up ticket"), 80);
  }
  function mwQ() { MW.qo = shuffle([0, 1, 2, 3]); MW.ans = null; MW.hint = false; }
  function mwDraw() {
    const { ctx } = MW, best = ctx.store.get("best", null), storyBest = ctx.store.get("story", null);
    ctx.el.classList.toggle("sec-calm", !!ctx.calm);
    const head = `<div class="sec-top"><span style="font-size:30px">🧹</span><div><h2>Malware removal order</h2><div class="tag">CompTIA A+ Core 2's seven steps: memorize the order, then use it on a ticket.</div></div>
      <div class="sec-prog">${best != null ? `Best order: ${best === 0 ? "perfect" : `${best} slip${best === 1 ? "" : "s"}`}` : "Not done yet"}${storyBest != null ? ` · ticket ${storyBest}/7` : ""}</div></div>
      <div class="chips"><button class="chipf ${MW.phase === "order" ? "on" : ""}" id="sec-mo">1 · Put in order</button><button class="chipf ${MW.phase === "story" ? "on" : ""}" id="sec-ms">2 · Work the ticket</button></div>`;
    let body;
    if (MW.phase === "order") {
      const next = MW.built.length;
      const done = next === STEPS.length;
      body = `<div class="sec-order"><div><h4 style="margin:0 0 8px">${done ? "All placed" : `Which step is #${next + 1}? Click it (keys 1–${MW.pool.length || 1})`}</h4>
          <div class="sec-pool">${MW.pool.map((s, k) => `<button class="sec-card" data-k="${k}"><kbd>${k + 1}</kbd><b>${ctx.esc(STEPS[s][0])}</b>${MW.hint && s === next ? `<small>👈 this one</small>` : ""}</button>`).join("")}</div>
          ${MW.say ? `<div class="sec-hint" style="background:rgba(255,107,107,.12);color:#ffc9c9">${ctx.esc(MW.say)}</div>` : ""}
          ${done ? `<div class="sec-res good"><h4>${MW.miss === 0 ? "Perfect order! 🏆" : `Done with ${MW.miss} slip${MW.miss === 1 ? "" : "s"}`}</h4>
            <div class="tag">Exam tip: quarantine comes before disabling System Restore, and System Restore goes off before you clean and back on after.</div>
            <div class="sec-next"><button class="btn" id="sec-mgo">Work the ticket →</button><button class="btn ghost" id="sec-mre">Shuffle again</button><span class="tag"><kbd>Enter</kbd></span></div></div>` : ""}</div>
        <div class="sec-built">${STEPS.map((s, k) => k < next ? `<div class="sec-slot fill"><span class="no">${k + 1}</span><span><b>${ctx.esc(s[0])}</b><small>${ctx.esc(s[1])}</small></span></div>`
          : `<div class="sec-slot"><span class="no">${k + 1}</span><span>?</span></div>`).join("")}</div></div>`;
    } else {
      const fin = MW.q >= QS.length;
      const rib = `<div class="sec-steprib">${QS.map((_, k) => `<i class="${k < MW.q ? (MW.marks[k] ? "ok" : "bad") : k === MW.q ? "on" : ""}"></i>`).join("")}</div>`;
      if (fin) body = `<div class="glass sec-story">📞 ${ctx.esc(STORY)}${rib}<div class="sec-res ${MW.right >= 6 ? "good" : "bad"}"><h4>Ticket closed: ${MW.right}/7 steps right</h4>
          <ol style="margin:6px 0 0;padding-left:20px">${STEPS.map((s) => `<li>${ctx.esc(s[0])}</li>`).join("")}</ol>
          <div class="sec-next"><button class="btn" id="sec-mag">Try again</button><button class="btn ghost" id="sec-mo2">Practice the order</button></div></div></div>`;
      else {
        const q = QS[MW.q], done = MW.ans != null;
        body = `<div class="glass sec-story">📞 ${ctx.esc(STORY)}${rib}<div class="tag">Step ${MW.q + 1} of 7</div></div>
          <div class="glass sec-q"><div class="ask">${ctx.esc(q.ask)}</div><div class="sec-opts">${MW.qo.map((o, k) => {
            const cls = done ? (o === 0 ? "yes" : o === MW.ans ? "no" : "") : "";
            return `<button class="sec-opt ${cls}" data-k="${k}" ${done ? "disabled" : ""}><kbd>${k + 1}</kbd><span>${ctx.esc(q.opts[o])}</span></button>`;
          }).join("")}</div>
          ${MW.hint && !done ? `<div class="sec-hint">💡 This is step ${MW.q + 1}: ${ctx.esc(STEPS[MW.q][0])}.</div>` : ""}
          ${done ? `<div class="sec-why">${MW.ans === 0 ? "✓" : "✗"} ${ctx.esc(q.why)}</div><div class="sec-next"><button class="btn" id="sec-mq">Next →</button><span class="tag"><kbd>Enter</kbd></span></div>` : ""}</div>`;
      }
    }
    ctx.el.innerHTML = head + body;
    const on = (id, f) => { const b = ctx.el.querySelector(id); if (b) b.onclick = f; };
    on("#sec-mo", () => mwStart("order")); on("#sec-ms", () => mwStart("story")); on("#sec-mgo", () => mwStart("story")); on("#sec-mre", () => mwStart("order"));
    on("#sec-mag", () => mwStart("story")); on("#sec-mo2", () => mwStart("order")); on("#sec-mq", mwNextQ);
    ctx.el.querySelectorAll(".sec-card").forEach((b) => b.onclick = () => mwPlace(+b.dataset.k));
    ctx.el.querySelectorAll(".sec-opt:not([disabled])").forEach((b) => b.onclick = () => mwAnswer(+b.dataset.k));
  }
  function mwPlace(k) {
    if (MW.phase !== "order" || k < 0 || k >= MW.pool.length) return;
    const s = MW.pool[k], want = MW.built.length;
    if (s === want) {
      MW.built.push(s); MW.pool.splice(k, 1); MW.say = ""; MW.hint = false;
      if (MW.built.length === STEPS.length) {
        const best = MW.ctx.store.get("best", null);
        if (best == null || MW.miss < best) MW.ctx.store.set("best", MW.miss);
        MW.ctx.toast(MW.miss === 0 ? "Perfect order! 🏆" : "All seven in place ✓");
      }
      mwDraw();
    } else {
      MW.miss++;
      MW.say = s < want ? "Already placed… that shouldn't happen." : `Not yet: "${STEPS[s][0]}" comes later. What has to happen before it?`;
      mwDraw(); shake(MW.ctx, MW.ctx.el.querySelector(".sec-pool"));
    }
  }
  function mwAnswer(k) {
    if (MW.phase !== "story" || MW.q >= QS.length || MW.ans != null || k < 0 || k > 3) return;
    MW.ans = MW.qo[k]; MW.marks[MW.q] = MW.ans === 0; if (MW.ans === 0) MW.right++;
    mwDraw();
  }
  function mwNextQ() {
    if (MW.phase !== "story" || MW.ans == null) return;
    MW.q++;
    if (MW.q >= QS.length) {
      const b = MW.ctx.store.get("story", null); if (b == null || MW.right > b) MW.ctx.store.set("story", MW.right);
      MW.ctx.toast(MW.right === 7 ? "Textbook removal. 🧹✨" : `Ticket closed: ${MW.right}/7`);
    } else mwQ();
    mwDraw();
  }
  function mwHint() { MW.hint = true; mwDraw(); }
  Lab.register({
    id: "sec-malware", title: "Malware removal order", icon: "🧹", exams: ["a2", "sec"],
    blurb: "A+'s seven malware removal steps: click them into order, then work a real pop-up ticket step by step.",
    render: mwRender,
    progress: (ctx) => { const b = ctx.store.get("best", null), s = ctx.store.get("story", null); return b == null ? "" : `${b === 0 ? "order ✓" : `order: ${b} slip${b === 1 ? "" : "s"}`}${s != null ? ` · ticket ${s}/7` : ""}`; },
    key(e) {
      if (!live(MW) || !plainKey(e)) return;
      if (MW.phase === "order") {
        if (/^[1-7]$/.test(e.key)) { e.preventDefault(); mwPlace(+e.key - 1); }
        else if (e.key === "Enter" && MW.built.length === STEPS.length) { e.preventDefault(); mwStart("story"); }
      } else {
        if (/^[1-4]$/.test(e.key)) { e.preventDefault(); mwAnswer(+e.key - 1); }
        else if ((e.key === "Enter" || e.key === "ArrowRight") && MW.ans != null && MW.q < QS.length) { e.preventDefault(); mwNextQ(); }
      }
      if (e.key === "h" || e.key === "H") mwHint();
    },
    remote(cmd, v) {
      if (!live(MW)) return;
      if (cmd === "show") return mwHint();
      if (cmd !== "grade") return;
      if (MW.phase === "order") { if (MW.built.length === STEPS.length) mwStart("story"); else mwPlace(v - 1); }
      else if (MW.q >= QS.length) mwStart("story");
      else if (MW.ans != null) mwNextQ();
      else mwAnswer(v - 1);
    },
  });
})();
