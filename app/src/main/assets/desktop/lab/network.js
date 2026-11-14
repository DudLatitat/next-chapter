// IT Lab: networking labs (A+ Core 1 · Network+). Loaded by index.html like every /lab/*.js file.
// Plain script, nothing from the internet: every picture is inline SVG or CSS.
// Registers four labs, each its own card on the IT Lab hub:
//   router  – a pretend SOHO router admin page with real-world jobs to finish and grade
//   netmap  – place devices on a floor plan and cable them up; a checker explains mistakes
//   subnet  – generated subnetting questions with a binary helper
//   wifi    – 2.4 / 5 / 6 GHz channel picking and band choices
(function () {
  "use strict";
  if (!window.Lab) return;

  // ---------- shared bits ----------
  const CSS = `
  .nw-calm *, .nw-calm *::before, .nw-calm *::after { animation: none !important; transition: none !important; }
  .nw-head { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin: 0 0 12px; }
  .nw-head h2 { margin: 0; font-size: 22px; }
  .nw-head .nw-sp { margin-left: auto; }
  .nw-scns { display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 8px; margin-bottom: 14px; }
  .nw-scn { position: relative; text-align: left; padding: 9px 12px; border-radius: 14px; cursor: pointer; color: inherit; font: inherit;
            background: rgba(29, 40, 86, .55); border: 1px solid var(--line); transition: background .15s, border-color .2s, transform .15s; }
  .nw-scn:hover { background: rgba(29, 40, 86, .9); transform: translateY(-1px); }
  .nw-scn.on { border-color: rgba(255, 207, 122, .55); background: rgba(255, 207, 122, .08); }
  .nw-scn b { display: block; font-size: 14px; } .nw-scn span { font-size: 12px; color: var(--muted); }
  .nw-scn .nw-ok { position: absolute; top: 7px; right: 10px; color: var(--jade); font-weight: 700; }
  .nw-work { display: grid; grid-template-columns: minmax(0, 1fr) 350px; gap: 16px; align-items: start; }
  @media (max-width: 980px) { .nw-work { grid-template-columns: 1fr; } }
  .nw-side { padding: 16px 18px; }
  .nw-side h3 { margin: 0 0 6px; font-size: 16px; }
  .nw-side p { margin: 6px 0; font-size: 14px; }
  .nw-k { font-size: 12px; color: var(--muted); margin-top: 10px; }
  .nw-note { font-size: 12.5px; color: var(--muted); margin-top: 4px; line-height: 1.45; }
  .nw-warn { font-size: 12.5px; color: var(--coral); margin-top: 4px; }
  .nw-q { font-size: 15px; margin: 4px 0 8px; }
  .nw-q code, .nw-mono { font: 600 15px ui-monospace, Consolas, monospace; color: var(--sun); }
  .nw-wrongnote { font-size: 13px; color: #ffc2c2; margin: 2px 0 6px 4px; animation: rise .3s both; }
  /* checklist */
  .nw-list { list-style: none; margin: 10px 0 0; padding: 0; }
  .nw-list li { padding: 7px 0 7px 26px; position: relative; font-size: 13.5px; border-top: 1px solid var(--line); }
  .nw-list li::before { position: absolute; left: 2px; top: 6px; font-weight: 700; }
  .nw-list li.ok::before { content: "✓"; color: var(--jade); }
  .nw-list li.bad::before { content: "✗"; color: var(--coral); }
  .nw-list li.todo::before { content: "•"; color: var(--muted); }
  .nw-list li .nw-why { color: var(--muted); font-size: 12.5px; margin-top: 3px; }
  .nw-list li .nw-tip { color: var(--sun); font-size: 12px; margin-top: 3px; }
  .nw-list li.bad { animation: rise .35s both; }
  /* fake router admin page */
  .nw-browser { border-radius: 16px; overflow: hidden; border: 1px solid rgba(255,255,255,.1); background: #f4f6fb; color: #1d2433; box-shadow: 0 24px 60px -30px #000c; }
  .nw-bbar { display: flex; align-items: center; gap: 10px; padding: 8px 12px; background: #dfe3ee; font-size: 13px; color: #445; }
  .nw-bbar .nw-dots { letter-spacing: 3px; color: #99a; }
  .nw-url { flex: 1; background: #fff; border-radius: 999px; padding: 4px 12px; font: 12.5px ui-monospace, Consolas, monospace; color: #334; }
  .nw-url .nw-ns { color: #b3261e; font-family: "Segoe UI", system-ui, sans-serif; margin-right: 6px; }
  .nw-rhead { display: flex; align-items: center; gap: 12px; padding: 10px 16px; background: #22324f; color: #fff; }
  .nw-rhead b { letter-spacing: .5px; } .nw-rhead span { font-size: 12px; opacity: .75; }
  .nw-rbody { display: grid; grid-template-columns: 160px 1fr; min-height: 470px; }
  .nw-rnav { background: #e8ebf3; padding: 8px 0; }
  .nw-rnav button { display: block; width: 100%; text-align: left; border: 0; background: none; padding: 9px 16px; font: inherit; font-size: 14px; color: #2a3550; cursor: pointer; }
  .nw-rnav button:hover { background: #dce1ee; }
  .nw-rnav button.on { background: #fff; color: #0b5cad; font-weight: 600; box-shadow: inset 3px 0 0 #0b5cad; }
  .nw-rpane { padding: 14px 20px 20px; font-size: 14px; }
  .nw-rpane h4 { margin: 4px 0 10px; font-size: 16px; color: #22324f; }
  .nw-rpane h5 { margin: 16px 0 6px; font-size: 13px; color: #22324f; text-transform: uppercase; letter-spacing: .8px; }
  .nw-row { display: grid; grid-template-columns: 190px 1fr; gap: 10px; align-items: start; padding: 7px 0; border-bottom: 1px solid #e3e6ee; }
  .nw-row > label { color: #3b4560; padding-top: 5px; }
  .nw-rpane .nw-note { color: #5b6580; } .nw-rpane .nw-warn { color: #b3261e; }
  .nw-in { font: inherit; font-size: 14px; padding: 5px 8px; border: 1px solid #b9c0d3; border-radius: 6px; background: #fff; color: #1d2433; min-width: 0; }
  .nw-in:focus { outline: 2px solid #0b5cad55; border-color: #0b5cad; }
  .nw-in.sm { width: 76px; } .nw-in.md { width: 150px; } .nw-in.lg { width: 260px; max-width: 100%; }
  .nw-rbtn { font: inherit; font-size: 13.5px; padding: 6px 14px; border-radius: 6px; border: 1px solid #0b5cad; background: #0b5cad; color: #fff; cursor: pointer; }
  .nw-rbtn.alt { background: #fff; color: #0b5cad; } .nw-rbtn.del { background: #fff; color: #b3261e; border-color: #d9a3a0; padding: 3px 10px; }
  .nw-rbtn:disabled { opacity: .5; cursor: default; }
  .nw-tog { position: relative; display: inline-block; width: 42px; height: 23px; vertical-align: middle; }
  .nw-tog input { opacity: 0; width: 0; height: 0; position: absolute; }
  .nw-tog span { position: absolute; inset: 0; border-radius: 999px; background: #b9c0d3; cursor: pointer; transition: .2s; }
  .nw-tog span::after { content: ""; position: absolute; left: 3px; top: 3px; width: 17px; height: 17px; border-radius: 50%; background: #fff; transition: .2s; }
  .nw-tog input:checked + span { background: #0b8a5f; } .nw-tog input:checked + span::after { transform: translateX(19px); }
  .nw-tog input:focus-visible + span { outline: 2px solid #0b5cad; }
  .nw-tbl { width: 100%; border-collapse: collapse; font-size: 13px; margin: 4px 0 8px; }
  .nw-tbl th { text-align: left; color: #5b6580; font-weight: 600; border-bottom: 1px solid #cfd5e3; padding: 5px 6px; }
  .nw-tbl td { padding: 5px 6px; border-bottom: 1px solid #e3e6ee; }
  .nw-tbl code { font: 12.5px ui-monospace, Consolas, monospace; }
  .nw-form { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; margin-top: 6px; }
  .nw-bar { height: 10px; border-radius: 6px; background: #dfe3ee; overflow: hidden; margin: 8px 0; max-width: 360px; }
  .nw-bar i { display: block; height: 100%; background: #0b8a5f; transition: width .2s linear; }
  .nw-flash { color: #0b8a5f; font-size: 13px; margin-left: 8px; }
  .nw-rfoot { font-size: 12px; color: #6b7590; padding: 8px 20px; border-top: 1px solid #e3e6ee; }
  /* network map */
  .nw-map svg { width: 100%; height: auto; display: block; border-radius: 18px; background: rgba(5, 8, 22, .55); border: 1px solid var(--line); user-select: none; }
  .nw-room { fill: rgba(142, 148, 242, .05); stroke: rgba(255,255,255,.16); stroke-width: 2; }
  .nw-roomt { fill: var(--muted); font-size: 13px; }
  .nw-slot circle { fill: rgba(255,255,255,.03); stroke: rgba(255,255,255,.25); stroke-dasharray: 4 4; }
  .nw-slot text { fill: var(--muted); font-size: 11px; }
  .nw-slot.can circle { stroke: var(--sun); fill: rgba(255, 207, 122, .1); stroke-dasharray: none; cursor: pointer; }
  .nw-slot.can { animation: nwpulse 1.6s ease-in-out infinite; }
  @keyframes nwpulse { 50% { opacity: .55; } }
  .nw-dev { cursor: pointer; }
  .nw-dev circle { fill: #1d2856; stroke: rgba(255,255,255,.35); stroke-width: 2; transition: stroke .2s; }
  .nw-dev.sel circle { stroke: var(--sun); stroke-width: 3; }
  .nw-dev.bad circle { stroke: var(--coral); stroke-width: 3; }
  .nw-dev text.ic { font-size: 21px; } .nw-dev text.lb { fill: var(--ink); font-size: 11.5px; font-weight: 600; }
  .nw-link { stroke: var(--jade); stroke-width: 3; } .nw-link.poe { stroke: var(--sun); stroke-dasharray: 8 4; }
  .nw-link.bad { stroke: var(--coral); }
  .nw-linkhit { stroke: transparent; stroke-width: 14; cursor: pointer; }
  .nw-pal { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; margin: 8px 0; }
  .nw-pi { display: flex; gap: 8px; align-items: center; text-align: left; padding: 7px 9px; border-radius: 12px; cursor: pointer; color: inherit; font: inherit; font-size: 13px;
           background: rgba(255,255,255,.05); border: 1px solid var(--line); }
  .nw-pi.on { border-color: rgba(255, 207, 122, .7); background: rgba(255, 207, 122, .1); }
  .nw-pi:disabled { opacity: .35; cursor: default; }
  .nw-pi .nw-n { margin-left: auto; color: var(--muted); font-size: 12px; }
  .nw-issues { margin: 10px 0 0; padding: 0; list-style: none; }
  .nw-issues li { margin: 8px 0; padding: 9px 11px; border-radius: 12px; background: rgba(255, 107, 107, .08); border: 1px solid rgba(255, 107, 107, .3); font-size: 13.5px; animation: rise .35s both; }
  .nw-issues li .nw-tip { color: var(--sun); font-size: 12.5px; margin-top: 4px; }
  /* subnetting */
  .nw-sub { max-width: 860px; }
  .nw-card { padding: 20px 22px; }
  .nw-big { font: 600 26px ui-monospace, Consolas, monospace; color: var(--sun); letter-spacing: .5px; }
  .nw-stats { display: flex; gap: 8px; flex-wrap: wrap; }
  .nw-bin { margin-top: 12px; padding: 12px 14px; border-radius: 14px; background: rgba(5, 8, 22, .6); border: 1px solid var(--line); overflow-x: auto; animation: rise .3s both; }
  .nw-bin table { border-collapse: collapse; font: 13px ui-monospace, Consolas, monospace; }
  .nw-bin td { padding: 3px 8px 3px 0; white-space: nowrap; }
  .nw-bin .n { color: var(--jade); } .nw-bin .h { color: var(--sun); } .nw-bin .lbl { color: var(--muted); font-family: "Segoe UI", system-ui, sans-serif; }
  .nw-bin .dec { color: var(--ink); padding-left: 14px; }
  /* wifi */
  .nw-chart svg { width: 100%; height: auto; display: block; border-radius: 16px; background: rgba(5, 8, 22, .55); border: 1px solid var(--line); }
  .nw-chart .ax { stroke: rgba(255,255,255,.25); } .nw-chart .tk { fill: var(--muted); font-size: 12px; }
  .nw-chart .tk.off { opacity: .4; }
  .nw-hump { fill: rgba(142, 148, 242, .16); stroke: var(--violet); stroke-width: 2; }
  .nw-hump.mine { fill: rgba(255, 207, 122, .18); stroke: var(--sun); stroke-dasharray: 6 4; }
  .nw-hump.good { fill: rgba(79, 179, 169, .2); stroke: var(--jade); stroke-dasharray: none; }
  .nw-chart .hl { fill: var(--ink); font-size: 11.5px; }
  .nw-chart .dfs { fill: rgba(255, 107, 107, .07); }
  .nw-chart .dfst { fill: var(--coral); font-size: 11px; }
  `;
  function injectCss() {
    if (document.getElementById("nw-css")) return;
    const s = document.createElement("style"); s.id = "nw-css"; s.textContent = CSS; document.head.appendChild(s);
  }
  const shuffle = (a) => { for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; };
  const ri = (a, b) => a + Math.floor(Math.random() * (b - a + 1));
  const pick = (a) => a[Math.floor(Math.random() * a.length)];
  const live = (R) => R && R.ctx && R.ctx.el && R.ctx.el.isConnected;
  function setup(ctx) { injectCss(); ctx.el.classList.toggle("nw-calm", !!ctx.calm); }
  const whyBox = (text, tip, stamp) =>
    `<div class="why">${stamp ? `<span class="stamp">${stamp}</span>` : ""}<h5>Why</h5><div>${text}</div>${tip ? `<div class="tipx"><h5>Exam tip</h5>${tip}</div>` : ""}</div>`;

  // A 4-choice question. q = { prompt, choices: [html], a, why, tip, notes?: {index: html} }.
  // done(firstTry) runs once when the right answer is picked.
  function quiz(host, q, done, stamp) {
    const wrong = new Set(); let finished = false, lastWrong = -1;
    function draw() {
      host.innerHTML = `<div class="nw-q">${q.prompt}</div>` + q.choices.map((c, k) =>
        `<button class="ch ${wrong.has(k) ? "wrong" : ""} ${finished && k === q.a ? "right" : ""}" data-c="${k}" ${finished ? "disabled" : ""}><kbd>${k + 1}</kbd><span>${c}</span></button>` +
        (k === lastWrong && !finished && q.notes && q.notes[k] ? `<div class="nw-wrongnote">${q.notes[k]}</div>` : "")).join("") +
        (finished ? whyBox(q.why, q.tip, stamp) : "");
      host.querySelectorAll("[data-c]").forEach((b) => b.onclick = () => choose(+b.dataset.c));
    }
    function choose(k) {
      if (finished || !(k >= 0 && k < q.choices.length) || wrong.has(k)) return;
      if (k === q.a) { finished = true; draw(); done && done(wrong.size === 0); }
      else { wrong.add(k); lastWrong = k; draw(); }
    }
    draw();
    return { choose, get finished() { return finished; } };
  }

  // =====================================================================
  // 1. SOHO router setup
  // =====================================================================
  const LAN = "192.168.0.", LATEST = "2.4.1", OLDFW = "2.0.3";
  const SEC = [
    ["open", "Open (no password)"], ["wep", "WEP"], ["wpa", "WPA-Personal (TKIP)"], ["mixed", "WPA/WPA2 mixed (TKIP + AES)"],
    ["wpa2", "WPA2-Personal (AES)"], ["wpa23", "WPA2/WPA3-Personal (transition)"], ["wpa3", "WPA3-Personal (SAE)"],
  ];
  const SECNAME = Object.fromEntries(SEC);
  const WEAKSEC = ["open", "wep", "wpa", "mixed"];
  const TZS = ["UTC", "America/Los_Angeles", "America/Denver", "America/Chicago", "America/New_York", "Asia/Shanghai"];
  const WEAKPW = ["admin", "password", "1234", "12345", "123456", "12345678", "password1", "admin123"];
  const DEFAULT_SSID = "SOHO-AX1800-7F2A", DEFAULT_KEY = "7f2a9c41";
  const PORTNAME = { 21: "FTP", 22: "SSH", 23: "Telnet", 80: "web (HTTP)", 443: "web (HTTPS)", 445: "SMB file sharing", 515: "LPD printing",
    631: "IPP printing", 3389: "Remote Desktop (RDP)", 5900: "VNC remote control", 9100: "raw printing (port 9100)", 3074: "game traffic", 5000: "the NAS's web page", 5001: "the NAS's web page (HTTPS)" };
  const PORTWHY = {
    3389: "RDP open to the internet is one of the most attacked things there is: bots guess passwords all day, and RDP has had serious remote bugs (BlueKeep). Reach the PC through a VPN instead.",
    5900: "VNC on the open internet gets scanned and password-guessed constantly, and many VNC setups don't encrypt. Remote support should come in over a VPN or a vendor tool that connects outward.",
    5000: "This puts the NAS's login page on the internet for every scanner and ransomware gang to try. Reach it over the router's VPN instead.",
    5001: "HTTPS encrypts the traffic but the NAS is still on the internet for anyone to attack. Reach it over the router's VPN instead.",
    9100: "Printing happens inside the office network. Forwarding a printer port lets strangers on the internet print to it (or attack its old firmware).",
    631: "Printing happens inside the office network; nothing from the internet should reach the printer.",
    515: "Printing happens inside the office network; nothing from the internet should reach the printer.",
    445: "SMB must never face the internet: WannaCry spread exactly this way.",
    23: "Telnet sends everything, passwords included, in plain text.",
  };
  const ipOk = (ip) => { const m = /^192\.168\.0\.(\d{1,3})$/.exec(String(ip).trim()); return m && +m[1] >= 2 && +m[1] <= 254 ? +m[1] : 0; };

  const SCN = [
    { id: "dental", title: "Dental office", sub: "Secure it + front desk printer",
      brief: "A small dental office just got this router. <b>Secure it and get the front desk printer reachable</b> at an address that never changes. Patients want Wi-Fi in the waiting room, but they must never see the office network. Every office device supports WPA3.",
      tz: "America/Los_Angeles", legacy: false, guest: "need", fixed: "printer", pool: true, allowed: [],
      devices: [["frontpc", "FRONTDESK-PC", "3C:52:82:1A:07:E4", 2, true], ["printer", "Front desk printer", "00:1B:A9:4C:21:7D", 3, true],
        ["xray", "XRAY-PC", "3C:52:82:1A:11:9B", 4, true], ["op1", "OP1-LAPTOP", "A4:83:E7:52:90:0C", 5, false], ["phone", "Office phone (Wi-Fi)", "F0:99:B6:2E:41:D3", 6, false]],
      init: (s) => { s.fwd = [{ name: "Remote desktop (old IT)", proto: "TCP", ext: 3389, ip: LAN + 4, port: 3389 }]; },
      checks: ["fw", "adminpw", "remote", "wifisec", "wifikey", "wps", "ssid", "guest", "pool", "reserve", "fwd", "dmz", "upnp", "ntp"],
      q: { prompt: "The old IT person forwarded TCP 3389 so the dentist could work from home. What should the office use instead?",
        choices: ["Keep RDP but move it to port 3390", "A VPN into the office, then Remote Desktop over the VPN", "Put the X-ray PC in the DMZ", "Turn UPnP on so it opens only when needed"], a: 1,
        why: "With a VPN, only people holding the VPN key can reach the office at all, and RDP never faces the internet. Moving RDP to another port is security by obscurity: scanners check every port.",
        tip: "RDP = TCP 3389. Exposing it is a classic exam wrong answer; VPN first, then RDP." } },
    { id: "gamer", title: "Home office + console", sub: "Open NAT the safe way",
      brief: "A home office: a work laptop, phones, a smart TV, and a game console that says <b>NAT type: Strict</b>. The game's help page says to forward <b>UDP 3074</b> to the console. The console is a few years old and <b>only supports WPA2</b>. Do it without opening anything else.",
      tz: "America/Chicago", legacy: true, guest: "optional", fixed: "console", allowed: [{ proto: "UDP", port: 3074, dev: "console" }],
      devices: [["laptop", "WORK-LAPTOP", "8C:16:45:E0:3B:72", 2, false], ["console", "Game console", "7C:BB:8A:19:5E:06", 3, false],
        ["tv", "Living room TV", "B8:BC:5B:77:20:A1", 4, false], ["phone", "Phone", "DA:A1:19:6C:0E:55", 5, false]],
      init: () => {},
      checks: ["fw", "adminpw", "remote", "wifisec", "wifikey", "wps", "ssid", "guest", "reserve", "fwdneed", "fwd", "dmz", "upnp", "ntp"],
      q: { prompt: "Why give the console a DHCP reservation before forwarding the port to it?",
        choices: ["Port forwarding only works on static IPs set on the router itself", "A forward points at one IP address. If the console's lease changes, the forward points at nothing, or at another device", "Reservations make the console faster", "Consoles can't use DHCP"], a: 1,
        why: "The forward says “send UDP 3074 to 192.168.0.x”. DHCP can hand the console a different address after a reboot or a long time offline, and the forward would then miss it (or hit someone else). A reservation pins the address.",
        tip: "Port forwarding + DHCP reservation (or a static IP outside the pool) go together. DMZ host = every port to one device: avoid." } },
    { id: "cafe", title: "Café guest Wi-Fi", sub: "Customers online, register safe",
      brief: "A café wants Wi-Fi for customers, with a simple password on the menu board. Staff use the register (wired), a back-office PC and an order tablet (all WPA3). <b>Customers must not reach the register, the tablet, or each other's phones.</b> The point-of-sale vendor once opened a port for “support”.",
      tz: "America/New_York", legacy: false, guest: "need", fixed: null, allowed: [],
      devices: [["register", "REGISTER-1", "00:0E:C6:A1:44:10", 2, true], ["office", "BACKOFFICE-PC", "3C:52:82:30:7A:2B", 3, true],
        ["tablet", "Order tablet", "5C:E9:1E:0B:63:F4", 4, false]],
      init: (s) => { s.fwd = [{ name: "POS support (vendor)", proto: "TCP", ext: 5900, ip: LAN + 2, port: 5900 }]; s.guest = { on: true, ssid: "SOHO-Guest", sec: "open", pass: "", iso: false }; },
      checks: ["fw", "adminpw", "remote", "wifisec", "wifikey", "wps", "ssid", "guest", "fwd", "dmz", "upnp", "ntp"],
      q: { prompt: "A customer says they can see other people's phones and a shared laptop on the café Wi-Fi. Which setting fixes it?",
        choices: ["Hide the guest SSID", "Turn on client (AP) isolation for the guest network", "Switch the guest network to WEP", "Shorten the DHCP lease time"], a: 1,
        why: "Client isolation stops Wi-Fi devices on that network from talking to each other; the router still lets them out to the internet. Hiding the name doesn't change who can talk to whom.",
        tip: "Guest network + client isolation is the textbook answer for customer/visitor Wi-Fi." } },
    { id: "nas", title: "NAS off the internet", sub: "Remote access, the safe way",
      brief: "A family put their NAS (photos and backups) in the router's <b>DMZ</b> so they could open it from their phones anywhere, and the NAS also opened ports with UPnP. Someone turned the firewall off “to test something”. <b>Take the NAS off the internet</b>, keep remote access the safe way with the router's built-in VPN server, and harden the rest. One old laptop only does WPA2.",
      tz: "America/Los_Angeles", legacy: true, guest: "optional", fixed: "nas", allowed: [],
      devices: [["nas", "FAMILY-NAS", "00:11:32:8E:5A:C0", 2, true], ["pc", "FAMILY-PC", "3C:52:82:44:01:9E", 3, true],
        ["pi", "Raspberry Pi (Pi-hole)", "DC:A6:32:7B:12:E8", 4, true], ["laptop", "Old laptop", "00:21:6A:5D:88:31", 5, false], ["phone", "Phone", "62:1F:0C:93:AE:47", 6, false]],
      init: (s) => {
        s.dmz = { on: true, ip: LAN + 2 }; s.sec.spi = false;
        s.fwd = [{ name: "NAS web (UPnP)", proto: "TCP", ext: 5000, ip: LAN + 2, port: 5000 }, { name: "NAS web SSL (UPnP)", proto: "TCP", ext: 5001, ip: LAN + 2, port: 5001 }];
      },
      checks: ["fw", "adminpw", "remote", "spi", "dmz", "fwd", "upnp", "vpn", "reserve", "wifisec", "wifikey", "wps", "ssid", "ntp"],
      q: { prompt: "Why is the router's VPN better than forwarding the NAS's web page?",
        choices: ["The VPN is faster", "Only devices with your VPN key can get in, and the NAS isn't visible to internet scanners at all", "A VPN hides the NAS's MAC address", "Port forwarding only works on TCP"], a: 1,
        why: "A forwarded NAS page is open to the whole internet: anyone can try passwords or an unpatched bug. With the VPN, the only thing listening is the VPN server, and it ignores anyone without the right key.",
        tip: "Keep storage and admin pages internal; reach them over a VPN. WireGuard uses UDP (51820 by default); OpenVPN commonly UDP/TCP 1194." } },
  ];

  function freshRouter(sc) {
    const s = {
      admin: { user: "admin", pass: "admin", remote: true, rport: 8080 },
      time: { ntp: false, server: "", tz: "UTC" },
      fw: { ver: OLDFW, checked: false },
      wifi: { ssid: DEFAULT_SSID, hide: false, sec: "mixed", pass: DEFAULT_KEY, wps: true },
      guest: { on: false, ssid: "SOHO-Guest", sec: "open", pass: "", iso: false },
      lan: { dhcp: true, start: 2, end: 254, lease: 24, res: [] },
      sec: { spi: true, upnp: true, ping: false, vpn: false },
      fwd: [], dmz: { on: false, ip: "" },
    };
    sc.init(s);
    return s;
  }
  const devOf = (sc, id) => sc.devices.find((d) => d[0] === id);
  const resFor = (s, sc, id) => { const d = devOf(sc, id); return d && s.lan.res.find((r) => r.mac === d[2]); };
  const devIp = (s, sc, d) => { const r = s.lan.res.find((x) => x.mac === d[2]); return r ? r.ip : LAN + d[3]; };
  const fixedName = (sc) => (devOf(sc, sc.fixed) || [])[1] || "";

  // Each check: label, tab to fix it in, ok(s, sc), why(s, sc) when it fails, tip, hint.
  const CHK = {
    fw: { tab: "firmware", label: "Firmware up to date",
      ok: (s) => s.fw.ver === LATEST,
      why: () => `It's still on ${OLDFW}. Old firmware has known security holes that attackers scan for, and this update also adds WPA3.`,
      tip: "Firmware updates patch vulnerabilities. Get them only from the maker, and never cut power mid-update.",
      hint: "Firmware tab → Check for updates → Install." },
    adminpw: { tab: "admin", label: "Default admin password replaced",
      ok: (s) => !WEAKPW.includes(s.admin.pass.toLowerCase()) && s.admin.pass.length >= 12,
      why: (s) => s.admin.pass === "admin" ? "The login is still admin / admin. Default router logins are printed in manuals online; anyone on the network could take over." : "Use a long, unique passphrase (12+ characters). Short or common ones fall to guessing.",
      tip: "Changing default passwords is the first SOHO hardening step on A+.",
      hint: "Admin tab → change the router password (12+ characters)." },
    remote: { tab: "admin", label: "Remote management off",
      ok: (s) => !s.admin.remote,
      why: (s) => `Remote management puts this login page on the internet (WAN port ${s.admin.rport}). Bots try router logins all day. Manage it from inside the network, or over a VPN.`,
      tip: "Disable remote (WAN-side) administration unless there's a real need.",
      hint: "Admin tab → Remote management." },
    wifisec: { tab: "wireless", label: "Strong Wi-Fi encryption",
      ok: (s, sc) => sc.legacy ? ["wpa2", "wpa23"].includes(s.wifi.sec) : s.wifi.sec === "wpa3",
      why: (s, sc) => {
        const v = s.wifi.sec;
        if (v === "open") return "Open Wi-Fi: anyone nearby can join and see unencrypted traffic.";
        if (v === "wep") return "WEP can be cracked in minutes with free tools. Never use it.";
        if (v === "wpa" || v === "mixed") return "TKIP is the old, broken WPA cipher. Mixed mode keeps it alive for every device. WPA2 should use AES (CCMP).";
        if (sc.legacy && v === "wpa3") return "WPA3-only locks out the device that only speaks WPA2. Use WPA2-AES, or WPA2/WPA3 transition so newer devices still get WPA3.";
        if (!sc.legacy && v === "wpa23") return "Transition mode still lets devices join with WPA2. Every device here supports WPA3, so use WPA3-only.";
        return "Every device here supports WPA3, so use WPA3-Personal.";
      },
      tip: "Best to worst: WPA3 → WPA2-AES → (WPA/TKIP, WEP: never). WPA3-Personal uses SAE, which resists offline password guessing.",
      hint: "Wireless tab → Security (WPA3 needs the new firmware)." },
    wifikey: { tab: "wireless", label: "Wi-Fi passphrase changed and long",
      ok: (s) => s.wifi.pass !== DEFAULT_KEY && s.wifi.pass.length >= 12,
      why: (s) => s.wifi.pass === DEFAULT_KEY ? "It's still the key from the sticker on the router. Anyone who has seen the box (or a photo of it) knows it." : "Make it at least 12 characters. WPA2 keys can be guessed offline, so length matters.",
      tip: "WPA2/WPA3 Personal passphrases are 8–63 characters; longer is much stronger.",
      hint: "Wireless tab → Passphrase (12+ characters)." },
    wps: { tab: "wireless", label: "WPS off",
      ok: (s) => !s.wifi.wps,
      why: () => "WPS's 8-digit PIN is checked in two halves, so it can be brute-forced in hours, which gives away the Wi-Fi password.",
      tip: "Disable WPS on a SOHO router.",
      hint: "Wireless tab → WPS." },
    ssid: { tab: "wireless", label: "SSID renamed",
      ok: (s) => s.wifi.ssid.trim() && s.wifi.ssid.trim() !== DEFAULT_SSID,
      why: () => "The default name tells everyone the router model (and that nobody changed the defaults), which points attackers at known bugs and default logins.",
      tip: "Change the default SSID. Hiding it (“disable SSID broadcast”) is on the A+ list too, but it isn't real security: the name still shows up to any Wi-Fi scanner.",
      hint: "Wireless tab → Network name (SSID)." },
    guest: { tab: "wireless", label: (sc) => sc.guest === "need" ? "Guest network on, secured and isolated" : "Guest network off (or isolated)",
      ok: (s, sc) => {
        const g = s.guest, good = g.on && g.iso && !WEAKSEC.includes(g.sec) && g.pass.length >= 8;
        return sc.guest === "need" ? good : (!g.on || good);
      },
      why: (s, sc) => {
        const g = s.guest;
        if (!g.on) return "Visitors need Wi-Fi, so they need a guest network. Otherwise someone will hand out the main password.";
        if (!g.iso) return "Guest isolation is off, so guests can reach the office devices and each other. Turn isolation on.";
        if (WEAKSEC.includes(g.sec)) return "Guest Wi-Fi still needs real encryption (WPA2-AES or WPA3) so people nearby can't read each other's traffic.";
        return sc.guest === "need" ? "Set a guest passphrase of at least 8 characters." : "If you leave the guest network on, give it a passphrase and isolation; otherwise turn it off.";
      },
      tip: "Guest network = internet only, no access to the LAN. Disabling unused guest access is also an A+ hardening step.",
      hint: "Wireless tab → Guest network." },
    pool: { tab: "lan", label: "DHCP pool leaves room for fixed addresses",
      ok: (s) => s.lan.dhcp && s.lan.start >= 2 && s.lan.end <= 254 && s.lan.start <= s.lan.end && (s.lan.end - s.lan.start + 1) <= 200 && (s.lan.end - s.lan.start + 1) >= 20,
      why: (s) => !s.lan.dhcp ? "DHCP has to stay on, or every PC needs its address typed in by hand." : `The pool is .${s.lan.start}–.${s.lan.end}. Shrink it (for example .100–.199) so devices with fixed addresses can live outside it without clashing.`,
      tip: "Static IPs go outside the DHCP scope; reservations can sit inside it. Both stop IP conflicts.",
      hint: "LAN/DHCP tab → set start and end (for example 100 and 199)." },
    reserve: { tab: "lan", label: (sc) => `${fixedName(sc)} has a fixed address`,
      ok: (s, sc) => !!resFor(s, sc, sc.fixed),
      why: (s, sc) => `${fixedName(sc)} gets its address from DHCP, so it can change after a reboot and everything pointing at it breaks. Add a DHCP reservation for its MAC address.`,
      tip: "DHCP reservation = same IP every time, still handed out by DHCP. Printers, servers, NAS and anything you forward ports to should have one.",
      hint: "LAN/DHCP tab → Address reservation: pick the device, give it an address." },
    fwdneed: { tab: "fwd", label: "UDP 3074 forwarded to the console's fixed address",
      ok: (s, sc) => { const r = resFor(s, sc, "console"); return !!r && s.fwd.some((f) => (f.proto === "UDP" || f.proto === "Both") && f.ext === 3074 && f.port === 3074 && f.ip === r.ip); },
      why: (s, sc) => !resFor(s, sc, "console") ? "Give the console a reservation first, then forward UDP 3074 to that address." : "Add a rule: UDP, external 3074 → the console's reserved address, internal 3074.",
      tip: "Forward only the ports the game's help page lists, to one fixed address. UPnP or DMZ “fix” NAT by opening far more.",
      hint: "Port forwarding tab → add UDP 3074 to the console's reserved IP." },
    fwd: { tab: "fwd", label: (sc) => sc.allowed.length ? "Nothing else forwarded" : "No ports forwarded",
      ok: (s, sc) => s.fwd.every((f) => isAllowed(f, s, sc)),
      why: (s, sc) => s.fwd.filter((f) => !isAllowed(f, s, sc)).map((f) => `Delete “${esc(f.name)}” (${f.proto} ${f.ext}${PORTNAME[f.ext] ? ", " + PORTNAME[f.ext] : ""}). ` + (PORTWHY[f.ext] || "Every forwarded port is a door from the internet into this network; open only what something truly needs.")).join("<br>"),
      tip: "Inbound from the internet should be closed by default. Things inside (like printing) never need a port forward.",
      hint: "Port forwarding tab → delete rules nothing here needs." },
    dmz: { tab: "fwd", label: "DMZ host off",
      ok: (s) => !s.dmz.on,
      why: () => "A SOHO “DMZ host” sends every unrequested connection from the internet to that one device: it's like taking it out from behind the firewall.",
      tip: "A real screened subnet (DMZ) is a separate network for public servers. The SOHO DMZ-host setting is not that; avoid it.",
      hint: "Port forwarding tab → DMZ host off." },
    upnp: { tab: "security", label: "UPnP off",
      ok: (s) => !s.sec.upnp,
      why: () => "UPnP lets any program inside open ports on the router without asking, and malware can use it too. Forward what you need by hand.",
      tip: "Disable UPnP on a hardened SOHO router.",
      hint: "Security tab → UPnP." },
    spi: { tab: "security", label: "Firewall on",
      ok: (s) => s.sec.spi,
      why: () => "With the SPI firewall off, the router stops checking whether incoming traffic belongs to a connection someone inside started.",
      tip: "SPI = stateful packet inspection: replies to your own requests get in, unrequested traffic is dropped.",
      hint: "Security tab → SPI firewall." },
    vpn: { tab: "security", label: "VPN server on for remote access",
      ok: (s) => s.sec.vpn,
      why: () => "They still want their photos on the go. The router's VPN server gives that safely: the phone joins the home network through an encrypted tunnel.",
      tip: "VPN = encrypted tunnel into the network. The router runs it itself, so no port forward to the NAS is needed.",
      hint: "Security tab → VPN server." },
    ntp: { tab: "admin", label: "Time set by NTP, right time zone",
      ok: (s, sc) => s.time.ntp && s.time.server.trim() && s.time.tz === sc.tz,
      why: (s, sc) => !s.time.ntp || !s.time.server.trim() ? "The clock isn't synced. Wrong time breaks log timestamps, schedules and certificate checks (HTTPS certificates have valid-from/to dates)." : `Set the time zone to ${sc.tz} so logs match local time.`,
      tip: "NTP = UDP port 123.",
      hint: "Admin tab → System time: NTP on, a server, time zone." },
  };
  function isAllowed(f, s, sc) {
    return sc.allowed.some((a) => {
      const d = devOf(sc, a.dev), r = d && s.lan.res.find((x) => x.mac === d[2]);
      return (f.proto === a.proto || f.proto === "Both") && f.ext === a.port && f.port === a.port && (!r || f.ip === r.ip) && f.ip === devIp(s, sc, d);
    });
  }
  const label = (c, sc) => typeof c.label === "function" ? c.label(sc) : c.label;

  let RT = null; // the open router lab
  const RTABS = [["status", "Status"], ["wireless", "Wireless"], ["lan", "LAN / DHCP"], ["security", "Security"], ["fwd", "Port forwarding"], ["admin", "Admin"], ["firmware", "Firmware"]];

  function rtRender(ctx) {
    setup(ctx);
    const solved = ctx.store.get("solved", {});
    let sid = ctx.store.get("cur", "dental"); if (!SCN.some((x) => x.id === sid)) sid = "dental";
    RT = { ctx, solved, sid, tab: "status", graded: null, quiz: null, fwState: null, flash: "" };
    rtLoad();
    rtDraw();
  }
  function rtSc() { return SCN.find((x) => x.id === RT.sid); }
  function rtLoad() {
    const saved = RT.ctx.store.get("cfg:" + RT.sid, null);
    RT.s = saved && saved.admin ? saved : freshRouter(rtSc());
    RT.graded = null; RT.quiz = null; RT.tab = "status"; RT.fwState = null;
  }
  const rtSave = () => RT.ctx.store.set("cfg:" + RT.sid, RT.s);

  function rtDraw() {
    const { ctx } = RT, sc = rtSc(), done = Object.keys(RT.solved).length;
    ctx.el.innerHTML = `
      <div class="nw-head"><h2>🛜 SOHO router setup</h2><span class="muted">Secure a small router the way the exam (and real clients) expect.</span>
        <span class="pill sun nw-sp">${done}/${SCN.length} sites secured</span></div>
      <div class="nw-scns">${SCN.map((x) => `<button class="nw-scn ${x.id === RT.sid ? "on" : ""}" data-s="${x.id}"><b>${x.title}</b><span>${x.sub}</span>${RT.solved[x.id] ? `<span class="nw-ok">✓</span>` : ""}</button>`).join("")}</div>
      <div class="nw-work">
        <div class="nw-browser">
          <div class="nw-bbar"><span class="nw-dots">● ● ●</span><div class="nw-url"><span class="nw-ns">⚠ Not secure</span>http://192.168.0.1/${RT.tab}.html</div></div>
          <div class="nw-rhead"><b>SOHO AX1800</b><span>Wireless router · firmware ${RT.s.fw.ver}</span><span style="margin-left:auto">Logged in as ${esc(RT.s.admin.user)}</span></div>
          <div class="nw-rbody"><div class="nw-rnav">${RTABS.map(([k, v]) => `<button data-t="${k}" class="${RT.tab === k ? "on" : ""}">${v}</button>`).join("")}</div>
            <div class="nw-rpane" id="nwpane"></div></div>
          <div class="nw-rfoot">Changes apply right away. This is a practice router: nothing here touches a real network.</div>
        </div>
        <div class="glass nw-side" id="nwside"></div>
      </div>`;
    ctx.el.querySelectorAll("[data-s]").forEach((b) => b.onclick = () => rtSwitch(b.dataset.s));
    ctx.el.querySelectorAll("[data-t]").forEach((b) => b.onclick = () => rtTab(b.dataset.t));
    rtPane(); rtSide();
    ctx.report(`Router: ${sc.title}`);
  }
  function esc(s) { return String(s == null ? "" : s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]); }
  function rtSwitch(id) {
    if (!SCN.some((x) => x.id === id)) return;
    RT.sid = id; RT.ctx.store.set("cur", id); rtLoad(); rtDraw();
  }
  function rtTab(t) {
    RT.tab = t;
    RT.ctx.el.querySelectorAll("[data-t]").forEach((b) => b.classList.toggle("on", b.dataset.t === t));
    const u = RT.ctx.el.querySelector(".nw-url"); if (u) u.innerHTML = `<span class="nw-ns">⚠ Not secure</span>http://192.168.0.1/${t}.html`;
    rtPane();
  }
  const row = (lab, control, note) => `<div class="nw-row"><label>${lab}</label><div>${control}${note || ""}</div></div>`;
  const tog = (k, on) => `<label class="nw-tog"><input type="checkbox" data-k="${k}" ${on ? "checked" : ""}><span></span></label>`;
  const txt = (k, v, cls, extra) => `<input class="nw-in ${cls || "md"}" data-k="${k}" value="${esc(v)}" ${extra || ""} spellcheck="false" autocomplete="off">`;
  const sel = (k, v, opts) => `<select class="nw-in" data-k="${k}">${opts.map(([val, lab, dis]) => `<option value="${val}" ${val === v ? "selected" : ""} ${dis ? "disabled" : ""}>${lab}</option>`).join("")}</select>`;
  const note = (t) => `<div class="nw-note">${t}</div>`;
  const warn = (t) => `<div class="nw-warn">${t}</div>`;

  function rtPane() {
    const p = RT.ctx.el.querySelector("#nwpane"), s = RT.s, sc = rtSc(), newFw = s.fw.ver === LATEST;
    let h = "";
    if (RT.tab === "status") {
      h = `<h4>Status</h4>
        ${row("Internet (WAN)", "Connected · 203.0.113.45", note("A public address from the ISP (this one is from a range reserved for examples)."))}
        ${row("Router (LAN)", "192.168.0.1 · 255.255.255.0 (/24)")}
        ${row("Firmware", esc(s.fw.ver) + (newFw ? "" : ` <span style="color:#b3261e">· an update may be available</span>`))}
        ${row("Wi-Fi", `${esc(s.wifi.ssid)} · ${SECNAME[s.wifi.sec]}`)}
        <h5>Connected devices</h5>
        <table class="nw-tbl"><tr><th>Name</th><th>MAC address</th><th>IP address</th><th>Link</th></tr>
        ${sc.devices.map((d) => `<tr><td>${esc(d[1])}</td><td><code>${d[2]}</code></td><td><code>${devIp(s, sc, d)}</code>${s.lan.res.some((r) => r.mac === d[2]) ? " (reserved)" : ""}</td><td>${d[4] ? "Wired" : "Wi-Fi"}</td></tr>`).join("")}</table>`;
    } else if (RT.tab === "wireless") {
      const w = s.wifi, g = s.guest;
      const opts = SEC.map(([v, l]) => [v, l + ((v === "wpa3" || v === "wpa23") && !newFw ? " (needs firmware 2.4+)" : ""), (v === "wpa3" || v === "wpa23") && !newFw]);
      h = `<h4>Wireless</h4>
        ${row("Network name (SSID)", txt("wifi.ssid", w.ssid, "lg", 'maxlength="32"'))}
        ${row("Hide network name", tog("wifi.hide", w.hide), w.hide ? note("Hidden. Fine to know for the exam, but it isn't real security: Wi-Fi scanners still see the network, and devices call out the name while looking for it.") : "")}
        ${row("Security", sel("wifi.sec", w.sec, opts), WEAKSEC.includes(w.sec) ? warn(w.sec === "open" ? "No encryption." : "Weak: uses TKIP or WEP.") : "")}
        ${w.sec !== "open" ? row("Passphrase", txt("wifi.pass", w.pass, "lg", 'maxlength="63"'), w.pass.length < 8 ? warn("WPA needs at least 8 characters.") : "") : ""}
        ${row("WPS (push button / PIN)", tog("wifi.wps", w.wps))}
        <h5>Guest network</h5>
        ${row("Guest network", tog("guest.on", g.on))}
        ${g.on ? row("Guest SSID", txt("guest.ssid", g.ssid, "lg", 'maxlength="32"')) +
          row("Guest security", sel("guest.sec", g.sec, SEC.map(([v, l]) => [v, l, (v === "wpa3" || v === "wpa23") && !newFw]))) +
          (g.sec !== "open" ? row("Guest passphrase", txt("guest.pass", g.pass, "lg", 'maxlength="63"')) : "") +
          row("Isolate guests", tog("guest.iso", g.iso), note("Guests reach the internet only: not your network, and not each other.")) : ""}`;
    } else if (RT.tab === "lan") {
      const l = s.lan, used = new Set(sc.devices.map((d) => devIp(s, sc, d)));
      h = `<h4>LAN / DHCP</h4>
        ${row("Router address", "192.168.0.1 / 255.255.255.0")}
        ${row("DHCP server", tog("lan.dhcp", l.dhcp))}
        ${row("Address pool", `192.168.0.<input class="nw-in sm" data-k="lan.start" data-num value="${l.start}" inputmode="numeric"> to 192.168.0.<input class="nw-in sm" data-k="lan.end" data-num value="${l.end}" inputmode="numeric">`,
          (l.start < 2 || l.end > 254 || l.start > l.end) ? warn("Use numbers from 2 to 254, start ≤ end (.1 is the router).") : note(`${l.end - l.start + 1} addresses handed out automatically.`))}
        ${row("Lease time", `<input class="nw-in sm" data-k="lan.lease" data-num value="${l.lease}"> hours`)}
        <h5>Address reservation</h5>
        <table class="nw-tbl"><tr><th>Device</th><th>MAC</th><th>Reserved IP</th><th></th></tr>
        ${l.res.length ? l.res.map((r, i) => `<tr><td>${esc(r.name)}</td><td><code>${r.mac}</code></td><td><code>${r.ip}</code></td><td><button class="nw-rbtn del" data-delres="${i}">Delete</button></td></tr>`).join("") : `<tr><td colspan="4" style="color:#6b7590">No reservations yet.</td></tr>`}</table>
        <div class="nw-form"><select class="nw-in" id="nwresdev">${sc.devices.filter((d) => !l.res.some((r) => r.mac === d[2])).map((d) => `<option value="${d[0]}">${esc(d[1])} (${d[2]})</option>`).join("")}</select>
          192.168.0.<input class="nw-in sm" id="nwresip" inputmode="numeric" placeholder="50"><button class="nw-rbtn" id="nwresadd">Add</button><span id="nwresmsg" class="nw-warn"></span></div>
        ${note("A reservation ties an IP to a device's MAC address, so DHCP always hands it the same one. Addresses in use: " + [...used].map((x) => "." + x.split(".")[3]).join(", ") + ".")}`;
    } else if (RT.tab === "security") {
      h = `<h4>Security</h4>
        ${row("SPI firewall", tog("sec.spi", s.sec.spi), note("Drops traffic from the internet that nobody inside asked for."))}
        ${row("UPnP", tog("sec.upnp", s.sec.upnp), note("Lets devices inside open ports on the router by themselves."))}
        ${row("Answer pings from the internet", tog("sec.ping", s.sec.ping))}
        <h5>VPN server</h5>
        ${row("WireGuard VPN server", tog("sec.vpn", s.sec.vpn), s.sec.vpn ? note("On. The router listens on UDP 51820 itself; scan the QR code on each phone or laptop to add it. No port forward needed.") : note("Lets your own devices join this network securely from anywhere."))}`;
    } else if (RT.tab === "fwd") {
      const opts = sc.devices.map((d) => `<option value="${devIp(s, sc, d)}">${esc(d[1])} (${devIp(s, sc, d)})</option>`).join("");
      h = `<h4>Port forwarding</h4>
        <table class="nw-tbl"><tr><th>Name</th><th>Protocol</th><th>External port</th><th>To</th><th>Internal port</th><th></th></tr>
        ${s.fwd.length ? s.fwd.map((f, i) => `<tr><td>${esc(f.name)}</td><td>${f.proto}</td><td>${f.ext}</td><td><code>${f.ip}</code></td><td>${f.port}</td><td><button class="nw-rbtn del" data-delfwd="${i}">Delete</button></td></tr>`).join("") : `<tr><td colspan="6" style="color:#6b7590">No rules. Nothing from the internet gets in unless someone inside asked for it.</td></tr>`}</table>
        <div class="nw-form"><input class="nw-in md" id="nwfname" placeholder="Name"><select class="nw-in" id="nwfproto"><option>TCP</option><option>UDP</option><option>Both</option></select>
          <input class="nw-in sm" id="nwfext" placeholder="Ext." inputmode="numeric"> → <select class="nw-in" id="nwfip">${opts}</select> <input class="nw-in sm" id="nwfint" placeholder="Int." inputmode="numeric">
          <button class="nw-rbtn" id="nwfadd">Add rule</button><span id="nwfmsg" class="nw-warn"></span></div>
        <h5>DMZ host</h5>
        ${row("DMZ host", tog("dmz.on", s.dmz.on), s.dmz.on ? note(`Every unrequested connection goes to <code>${esc(s.dmz.ip)}</code>.`) : "")}`;
    } else if (RT.tab === "admin") {
      h = `<h4>Admin</h4>
        ${row("Username", txt("admin.user", s.admin.user))}
        ${row("Router password", `<div class="nw-form" style="margin:0"><input class="nw-in md" id="nwpw1" type="password" placeholder="New password" autocomplete="new-password"><input class="nw-in md" id="nwpw2" type="password" placeholder="Again" autocomplete="new-password"><button class="nw-rbtn" id="nwpwset">Change</button></div><span id="nwpwmsg"></span>`)}
        ${row("Remote management", tog("admin.remote", s.admin.remote), s.admin.remote ? note(`This page answers from the internet on port ${s.admin.rport}.`) : note("Only reachable from inside the network."))}
        <h5>System time</h5>
        ${row("Get time from NTP", tog("time.ntp", s.time.ntp))}
        ${s.time.ntp ? row("NTP server", txt("time.server", s.time.server, "lg", 'placeholder="pool.ntp.org"')) : ""}
        ${row("Time zone", sel("time.tz", s.time.tz, TZS.map((z) => [z, z])))}`;
    } else if (RT.tab === "firmware") {
      const st = RT.fwState;
      h = `<h4>Firmware</h4>
        ${row("Installed", esc(s.fw.ver))}
        ${newFw ? row("Status", "Up to date ✓") :
          st && st.p != null ? row("Updating", `<div class="nw-bar"><i style="width:${st.p}%"></i></div>${st.p < 100 ? "Writing firmware… don't turn the router off." : "Rebooting…"}`) :
          s.fw.checked ? row("Available", `<b>${LATEST}</b> · security fixes for remote management, adds WPA3.<div class="nw-form"><button class="nw-rbtn" id="nwfwgo">Install ${LATEST}</button></div>`) :
          row("Updates", `<button class="nw-rbtn alt" id="nwfwchk">Check for updates</button>`)}
        ${note("Tip: only install firmware from the maker. Losing power mid-update can leave the router unable to start.")}`;
    }
    p.innerHTML = h + (RT.flash ? `<div class="nw-flash">${RT.flash}</div>` : "");
    RT.flash = "";
    rtWire(p);
  }
  function setPath(o, path, v) { const k = path.split("."); let x = o; for (let i = 0; i < k.length - 1; i++) x = x[k[i]]; x[k[k.length - 1]] = v; }
  function rtWire(p) {
    const s = RT.s, sc = rtSc();
    const val = (el) => el.type === "checkbox" ? el.checked : el.hasAttribute("data-num") ? (parseInt(el.value, 10) || 0) : el.value;
    p.querySelectorAll("[data-k]").forEach((el) => {
      if (el.type === "checkbox" || el.tagName === "SELECT") el.onchange = () => {
        setPath(s, el.dataset.k, val(el));
        if (el.dataset.k === "dmz.on" && s.dmz.on && !s.dmz.ip) s.dmz.ip = devIp(s, sc, sc.devices[0]);
        rtChanged(true);
      };
      else { el.oninput = () => { setPath(s, el.dataset.k, val(el)); rtChanged(false); }; el.onchange = () => rtChanged(el.hasAttribute("data-num")); }
    });
    const on = (id, f) => { const b = p.querySelector(id); if (b) b.onclick = f; };
    on("#nwfwchk", () => { s.fw.checked = true; rtChanged(true); });
    on("#nwfwgo", () => rtFirmware());
    on("#nwpwset", () => {
      const a = p.querySelector("#nwpw1").value, b = p.querySelector("#nwpw2").value, m = p.querySelector("#nwpwmsg");
      if (!a) { m.className = "nw-warn"; m.textContent = "Type a new password."; return; }
      if (a !== b) { m.className = "nw-warn"; m.textContent = "The two passwords don't match."; return; }
      s.admin.pass = a; RT.flash = "Password changed."; rtChanged(true);
    });
    on("#nwresadd", () => {
      const id = p.querySelector("#nwresdev").value, d = devOf(sc, id), n = ipOk(LAN + p.querySelector("#nwresip").value.trim().replace(/^.*\./, "")), m = p.querySelector("#nwresmsg");
      if (!d) return;
      if (!n) { m.textContent = "Use a number from 2 to 254 (.1 is the router)."; return; }
      const ip = LAN + n, clash = sc.devices.find((x) => x !== d && devIp(s, sc, x) === ip);
      if (clash) { m.textContent = `${ip} is in use by ${clash[1]}. Pick a free one.`; return; }
      s.lan.res.push({ name: d[1], mac: d[2], ip });
      s.fwd.forEach((f) => { if (f.ip === LAN + d[3]) f.ip = ip; });   // existing rules follow the device, like a real router's device picker
      if (s.dmz.ip === LAN + d[3]) s.dmz.ip = ip;
      RT.flash = `Reserved ${ip} for ${esc(d[1])}.`; rtChanged(true);
    });
    p.querySelectorAll("[data-delres]").forEach((b) => b.onclick = () => { s.lan.res.splice(+b.dataset.delres, 1); rtChanged(true); });
    p.querySelectorAll("[data-delfwd]").forEach((b) => b.onclick = () => { const f = s.fwd.splice(+b.dataset.delfwd, 1)[0]; RT.flash = `Deleted “${esc(f.name)}”.`; rtChanged(true); });
    on("#nwfadd", () => {
      const m = p.querySelector("#nwfmsg"), ext = parseInt(p.querySelector("#nwfext").value, 10), intp = parseInt(p.querySelector("#nwfint").value || p.querySelector("#nwfext").value, 10);
      if (!(ext >= 1 && ext <= 65535) || !(intp >= 1 && intp <= 65535)) { m.textContent = "Ports are numbers from 1 to 65535."; return; }
      s.fwd.push({ name: p.querySelector("#nwfname").value.trim() || `Rule ${s.fwd.length + 1}`, proto: p.querySelector("#nwfproto").value, ext, ip: p.querySelector("#nwfip").value, port: intp });
      RT.flash = "Rule added."; rtChanged(true);
    });
  }
  function rtChanged(redraw) {
    rtSave();
    if (RT.graded) RT.graded = rtGrade();   // keep the checklist honest as you fix things
    if (redraw) {
      rtPane();
      const h = RT.ctx.el.querySelector(".nw-rhead span"); if (h) h.textContent = `Wireless router · firmware ${RT.s.fw.ver}`;
    }
    if (!RT.quiz) rtSide();
  }
  function rtFirmware() {
    const s = RT.s, step = RT.ctx.calm ? 50 : 4;
    RT.fwState = { p: 0 };
    const tick = () => {
      if (!live(RT) || !RT.fwState) return;
      RT.fwState.p = Math.min(100, RT.fwState.p + step);
      if (RT.tab === "firmware") rtPane();
      if (RT.fwState.p < 100) setTimeout(tick, 90);
      else setTimeout(() => { if (!RT.fwState) return; s.fw.ver = LATEST; RT.fwState = null; RT.flash = "Updated to " + LATEST + ". WPA3 is now available."; rtChanged(RT.tab === "firmware"); if (RT.tab !== "firmware") rtDraw(); else { const h = RT.ctx.el.querySelector(".nw-rhead span"); if (h) h.textContent = `Wireless router · firmware ${s.fw.ver}`; } }, RT.ctx.calm ? 100 : 700);
    };
    tick();
  }
  function rtGrade() {
    const sc = rtSc();
    return sc.checks.map((id) => { const c = CHK[id]; const ok = !!c.ok(RT.s, sc); return { id, ok, label: label(c, sc), why: ok ? "" : c.why(RT.s, sc), tip: c.tip }; });
  }
  function rtSide() {
    const side = RT.ctx.el.querySelector("#nwside"); if (!side) return;
    const sc = rtSc(), g = RT.graded, left = g ? g.filter((x) => !x.ok).length : null;
    side.innerHTML = `<h3>Work order · ${sc.title}</h3><p>${sc.brief}</p>
      ${g ? `<p><b>${left ? `${left} of ${g.length} still to fix` : "All " + g.length + " checks pass ✓"}</b></p>
        <ul class="nw-list">${g.map((x) => `<li class="${x.ok ? "ok" : "bad"}">${x.label}${x.ok ? "" : `<div class="nw-why">${x.why}</div><div class="nw-tip">${x.tip}</div>`}</li>`).join("")}</ul>`
        : `<p class="muted" style="font-size:13px">${sc.checks.length} things are checked. Click around the router, fix what you'd fix for a real client, then check your work.</p>`}
      <div class="drow" style="margin-top:12px"><button class="btn" id="nwcheck">Check my work <kbd>C</kbd></button><button class="btn ghost" id="nwhint">Hint <kbd>H</kbd></button><button class="btn ghost" id="nwreset" title="Factory reset this router">Reset</button></div>
      <div id="nwhintbox"></div><div id="nwq"></div>
      <div class="nw-k">Keys: <kbd>C</kbd> check · <kbd>H</kbd> hint · <kbd>[</kbd> <kbd>]</kbd> tabs · <kbd>1</kbd>–<kbd>4</kbd> answer</div>`;
    side.querySelector("#nwcheck").onclick = rtCheck;
    side.querySelector("#nwhint").onclick = rtHint;
    side.querySelector("#nwreset").onclick = () => { RT.s = freshRouter(sc); rtSave(); RT.graded = null; RT.quiz = null; RT.fwState = null; RT.ctx.toast("Router reset to factory defaults"); rtDraw(); };
    if (g && !left) rtQuiz();
  }
  function rtCheck() {
    RT.graded = rtGrade(); RT.quiz = null; rtSide();
    const left = RT.graded.filter((x) => !x.ok).length;
    RT.ctx.report(left ? `${left} left to fix` : "All checks pass");
    if (left) RT.ctx.toast(`${left} still to fix`, true);
  }
  function rtHint() {
    const g = rtGrade(), bad = g.find((x) => !x.ok), box = RT.ctx.el.querySelector("#nwhintbox");
    if (!box) return;
    box.innerHTML = `<div class="hintbox">${bad ? "💡 " + CHK[bad.id].hint : "Everything passes. Press Check my work."}</div>`;
    if (bad) { const t = CHK[bad.id].tab; if (t && RT.tab !== t) rtTab(t); }
  }
  function rtQuiz() {
    const sc = rtSc(), host = RT.ctx.el.querySelector("#nwq"); if (!host) return;
    const wasSolved = !!RT.solved[sc.id];
    host.innerHTML = `<div class="dq"><h4>One exam question</h4><div id="nwqq"></div></div>`;
    RT.quiz = quiz(host.querySelector("#nwqq"), sc.q, () => {
      RT.solved[sc.id] = true; RT.ctx.store.set("solved", RT.solved);
      if (!wasSolved) RT.ctx.toast(`${sc.title}: secured ✓`);
      const pill = RT.ctx.el.querySelector(".nw-head .pill"); if (pill) pill.textContent = `${Object.keys(RT.solved).length}/${SCN.length} sites secured`;
      const b = RT.ctx.el.querySelector(`.nw-scn[data-s="${sc.id}"]`); if (b && !b.querySelector(".nw-ok")) b.insertAdjacentHTML("beforeend", `<span class="nw-ok">✓</span>`);
    }, "SECURED");
    if (!RT.ctx.calm) host.scrollIntoView({ block: "nearest", behavior: "smooth" });
  }

  Lab.register({
    id: "router", title: "SOHO router setup", icon: "🛜", exams: ["a1", "net"],
    blurb: "A pretend router admin page. Lock down a dental office, a café, a gamer's home and a family NAS, then get graded.",
    render: rtRender,
    progress: (ctx) => { const n = Object.keys(ctx.store.get("solved", {})).length; return n ? `${n}/${SCN.length} secured` : ""; },
    key(e) {
      if (!live(RT)) return;
      const k = e.key.toLowerCase();
      if (RT.quiz && !RT.quiz.finished && "1234".includes(e.key)) { e.preventDefault(); RT.quiz.choose(+e.key - 1); return; }
      if (k === "c") { e.preventDefault(); rtCheck(); }
      else if (k === "h") { e.preventDefault(); rtHint(); }
      else if (k === "[" || k === "]") { const i = RTABS.findIndex((t) => t[0] === RT.tab); rtTab(RTABS[(i + (k === "]" ? 1 : RTABS.length - 1)) % RTABS.length][0]); }
    },
    remote(cmd, v) {
      if (!live(RT)) return;
      if (cmd === "show") rtHint();
      else if (cmd === "grade") { if (RT.quiz && !RT.quiz.finished) RT.quiz.choose(v - 1); else rtCheck(); }
    },
    _test: { SCN, CHK, freshRouter, isAllowed },
  });

  // =====================================================================
  // 2. Build the network map
  // =====================================================================
  const DEV = {
    ont: ["🌐", "ONT / modem"], router: ["🛡️", "Router/firewall"], switch: ["🔀", "Switch"], poe: ["⚡", "PoE switch"],
    ap: ["📡", "Access point"], cam: ["📹", "Camera"], printer: ["🖨️", "Printer"], pc: ["🖥️", "PC"], nas: ["🗄️", "NAS"],
    pi: ["🍓", "Raspberry Pi"], console: ["🎮", "Console"],
  };
  const ENDPOINT = new Set(["ap", "cam", "printer", "pc", "nas", "pi", "console"]);
  const INFRA = new Set(["router", "switch", "poe"]);
  const MAPS = [
    { id: "office", title: "Small office", sub: "Ceiling APs, door cameras",
      brief: "A small office. Fiber comes in at the closet. Two access points go on the ceiling and two cameras watch the doors: none of those spots has a power outlet. Place every device, then cable it so it all works and nothing is exposed.",
      rooms: [[10, 10, 210, 230, "Network closet"], [220, 10, 380, 300, "Lobby & front desk"], [600, 10, 290, 250, "Office A"], [600, 260, 290, 250, "Office B"], [10, 240, 210, 270, "Break room"], [220, 310, 380, 200, "Hallway"]],
      slots: [["demarc", 38, 120, "ISP line in", ["ont"], true], ["r1", 95, 60, "Rack", ["router", "switch", "poe", "nas"], true], ["r2", 170, 60, "Rack", ["router", "switch", "poe", "nas"], true],
        ["r3", 95, 165, "Rack", ["router", "switch", "poe", "nas"], true], ["r4", 170, 165, "Rack", ["router", "switch", "poe", "nas"], true],
        ["desk", 330, 215, "Front desk", ["pc", "printer"], true], ["deskp", 450, 215, "Front desk", ["pc", "printer"], true],
        ["ceil1", 410, 90, "Lobby ceiling", ["ap"], false], ["ceil2", 410, 420, "Hall ceiling", ["ap"], false],
        ["camf", 560, 280, "Front door", ["cam"], false], ["camb", 60, 470, "Back door", ["cam"], false],
        ["offa", 745, 130, "Desk", ["pc", "printer"], true], ["offb", 745, 390, "Desk", ["pc", "printer"], true]],
      need: { ont: 1, router: 1, switch: 1, poe: 1, nas: 1, ap: 2, cam: 2, printer: 1, pc: 3 },
      q: { prompt: "Which standard lets one Ethernet cable carry both data and power to the ceiling access points?",
        choices: ["802.11ax", "802.3af / 802.3at (PoE / PoE+)", "802.1X", "802.1Q"], a: 1,
        why: "PoE is 802.3af (about 15 W per port), PoE+ is 802.3at (about 30 W), and 802.3bt goes higher for bigger devices. The switch (or an injector) powers the device over the same cable.",
        tip: "802.11 = Wi-Fi, 802.1X = port-based login (authentication), 802.1Q = VLAN tagging." } },
    { id: "home", title: "Home lab", sub: "Pi-hole, NAS, console",
      brief: "A home lab: fiber in the den, a Raspberry Pi running Pi-hole (network-wide ad blocking), a NAS, a gaming console, a desk PC, one access point on a living-room shelf (with an outlet) and a camera outside the garage (no outlet). Place everything, then cable it.",
      rooms: [[10, 10, 300, 250, "Den"], [310, 10, 580, 330, "Living room"], [10, 260, 300, 250, "Garage"], [310, 340, 580, 170, "Kitchen"]],
      slots: [["demarc", 38, 130, "ISP line in", ["ont"], true], ["s1", 100, 60, "Shelf", ["router", "switch", "poe", "nas", "pi"], true], ["s2", 170, 60, "Shelf", ["router", "switch", "poe", "nas", "pi"], true],
        ["s3", 240, 60, "Shelf", ["router", "switch", "poe", "nas", "pi"], true], ["s4", 100, 160, "Shelf", ["router", "switch", "poe", "nas", "pi"], true],
        ["s5", 170, 160, "Shelf", ["router", "switch", "poe", "nas", "pi"], true], ["desk", 240, 210, "Desk", ["pc"], true],
        ["shelf", 600, 120, "Bookshelf (outlet)", ["ap"], true], ["tv", 780, 260, "TV stand", ["console"], true], ["garage", 160, 460, "Outside garage", ["cam"], false]],
      need: { ont: 1, router: 1, switch: 1, poe: 1, nas: 1, pi: 1, pc: 1, ap: 1, console: 1, cam: 1 },
      q: { prompt: "Pi-hole should filter every device in the house. What's the cleanest way to make that happen?",
        choices: ["Plug the Pi into the ONT so it sees all traffic", "Give the Pi a reserved IP, then set that IP as the DNS server the router's DHCP hands out", "Turn on UPnP", "Put the Pi in the DMZ"], a: 1,
        why: "Pi-hole is a DNS server. When DHCP tells every device to use the Pi for DNS, every lookup goes through it. It needs a fixed address so that setting never goes stale.",
        tip: "DHCP hands out more than an IP: also the subnet mask, default gateway and DNS servers. DNS = port 53." } },
  ];
  let NM = null;
  const nmMap = () => MAPS.find((m) => m.id === NM.mid);
  const slotOf = (id) => nmMap().slots.find((s) => s[0] === id);
  const nmName = (id) => { const t = NM.placed[id]; return `the ${DEV[t][1]}${slotOf(id)[3] && !INFRA.has(t) && t !== "ont" ? " at " + slotOf(id)[3] : ""}`; };
  const cap = (s) => s.charAt(0).toUpperCase() + s.slice(1);

  function nmRender(ctx) {
    setup(ctx);
    let mid = ctx.store.get("cur", "office"); if (!MAPS.some((m) => m.id === mid)) mid = "office";
    NM = { ctx, mid, solved: ctx.store.get("solved", {}), sel: null, pickType: null, issues: null, quiz: null, ok: false };
    nmLoad(); nmDraw();
  }
  function nmLoad() {
    const s = NM.ctx.store.get("map:" + NM.mid, null);
    NM.placed = (s && s.placed) || {}; NM.links = (s && s.links) || [];
    NM.issues = null; NM.quiz = null; NM.ok = false; NM.sel = null; NM.pickType = null;
  }
  const nmSave = () => NM.ctx.store.set("map:" + NM.mid, { placed: NM.placed, links: NM.links });
  function nmLeft() {
    const m = nmMap(), left = { ...m.need };
    Object.values(NM.placed).forEach((t) => { left[t] = (left[t] || 0) - 1; });
    return left;
  }

  // The checker. Returns a list of { text, tip, bad: [slot ids] }.
  function nmCheck(map, placed, links) {
    const issues = [], nb = {};
    Object.keys(placed).forEach((k) => nb[k] = []);
    links.forEach(([a, b]) => { nb[a].push(b); nb[b].push(a); });
    const name = (id) => { const t = placed[id], sl = map.slots.find((s) => s[0] === id); return `the ${DEV[t][1]}${!INFRA.has(t) && t !== "ont" ? " at " + sl[3] : ""}`; };
    const left = { ...map.need }; Object.values(placed).forEach((t) => left[t]--);
    const missing = Object.entries(left).filter(([, n]) => n > 0);
    if (missing.length) issues.push({ text: `Not everything is placed yet: ${missing.map(([t, n]) => `${n} × ${DEV[t][1]}`).join(", ")}.`, tip: "Pick a device on the right, then click a glowing spot.", bad: [] });
    const ont = Object.keys(placed).find((k) => placed[k] === "ont"), router = Object.keys(placed).find((k) => placed[k] === "router");
    if (ont) {
      const others = nb[ont].filter((x) => placed[x] !== "router");
      others.forEach((x) => {
        const t = placed[x];
        issues.push(INFRA.has(t)
          ? { text: `${cap(name(x))} is plugged straight into the ONT/modem, on the internet side of the router. Everything behind it skips the firewall.`, tip: "Order from the outside in: ONT/modem → router WAN port → switches → devices.", bad: [x, ont] }
          : { text: `${cap(name(x))} is plugged straight into the ONT/modem, so it sits on the internet with no firewall or NAT in front of it.`, tip: "Only the router's WAN port connects to the ISP's box. Servers and NAS stay behind the firewall.", bad: [x, ont] });
      });
      if (router && !nb[ont].includes(router)) issues.push({ text: "The router's WAN port needs a cable from the ONT/modem, or nothing reaches the internet.", tip: "The ONT (fiber) or modem (cable/DSL) is the ISP's hand-off point (the demarc).", bad: [ont, router] });
    }
    const pairs = new Set();
    Object.keys(placed).forEach((id) => {
      const t = placed[id];
      if (!ENDPOINT.has(t)) return;
      const n = nb[id];
      if (!n.length) { issues.push({ text: `${cap(name(id))} has no cable.`, tip: "Every wired device needs one cable to a switch (or a spare router LAN port).", bad: [id] }); return; }
      n.filter((x) => ENDPOINT.has(placed[x])).forEach((x) => {
        const key = [id, x].sort().join("|"); if (pairs.has(key)) return; pairs.add(key);
        issues.push({ text: `${cap(name(id))} is cabled to ${name(x)}. End devices don't pass traffic on; each needs its own cable to a switch.`, tip: "Switches connect devices together; endpoints have one network port.", bad: [id, x] });
      });
      if (n.length > 1 && !n.some((x) => ENDPOINT.has(placed[x]))) issues.push({ text: `${cap(name(id))} has ${n.length} cables. It has one network port: use one cable to a switch.`, tip: "Two paths to the same device also make a loop.", bad: [id] });
      if ((t === "ap" || t === "cam") && !map.slots.find((s) => s[0] === id)[5]) {
        const up = n.find((x) => INFRA.has(placed[x]));
        if (up && placed[up] !== "poe") issues.push({ text: `${cap(name(id))} has no power outlet there. A regular ${DEV[placed[up]][1].toLowerCase()} sends data but no power: connect it to the PoE switch.`, tip: "PoE (802.3af/at) carries power on the Ethernet cable, ideal for ceiling APs and cameras.", bad: [id] });
      }
    });
    // Loops (cycles) with union-find.
    const par = {}; const find = (x) => par[x] === undefined || par[x] === x ? (par[x] = x) : (par[x] = find(par[x]));
    for (const [a, b] of links) {
      const ra = find(a), rb = find(b);
      if (ra === rb) { issues.push({ text: `The cable between ${name(a)} and ${name(b)} closes a loop: there are now two paths between the same devices.`, tip: "Without Spanning Tree Protocol (STP, 802.1D) a switching loop causes a broadcast storm that can take the whole network down.", bad: [a, b] }); }
      else par[ra] = rb;
    }
    // Everything must reach the router without going through the ONT.
    if (router) {
      const seen = new Set([router]), q = [router];
      while (q.length) { const x = q.shift(); nb[x].forEach((y) => { if (!seen.has(y) && placed[y] !== "ont") { seen.add(y); q.push(y); } }); }
      Object.keys(placed).forEach((id) => {
        const t = placed[id];
        if (seen.has(id) || t === "ont") return;
        if (ENDPOINT.has(t) && !nb[id].length) return; // already reported as "no cable"
        if (nb[id].some((x) => placed[x] === "ont")) return; // already reported as on the ONT
        issues.push({ text: `${cap(name(id))} can't reach the router${INFRA.has(t) ? ": this switch has no uplink" : ""}.`, tip: "Uplink every switch to the router (or to a switch that is).", bad: [id] });
      });
    }
    return issues;
  }

  function nmDraw() {
    const { ctx } = NM, m = nmMap(), left = nmLeft();
    ctx.el.innerHTML = `
      <div class="nw-head"><h2>🗺️ Build the network map</h2><span class="muted">Place each device, then cable it. The checker explains what's wrong.</span>
        <span class="pill sun nw-sp">${Object.keys(NM.solved).length}/${MAPS.length} maps done</span></div>
      <div class="nw-scns">${MAPS.map((x) => `<button class="nw-scn ${x.id === NM.mid ? "on" : ""}" data-m="${x.id}"><b>${x.title}</b><span>${x.sub}</span>${NM.solved[x.id] ? `<span class="nw-ok">✓</span>` : ""}</button>`).join("")}</div>
      <div class="nw-work">
        <div class="nw-map" id="nwmap"></div>
        <div class="glass nw-side">
          <h3>${m.title}</h3><p>${m.brief}</p>
          <div class="nw-pal">${Object.keys(m.need).map((t, i) => `<button class="nw-pi ${NM.pickType === t ? "on" : ""}" data-p="${t}" ${left[t] > 0 ? "" : "disabled"}><kbd>${i + 1}</kbd>${DEV[t][0]} ${DEV[t][1]}<span class="nw-n">×${Math.max(0, left[t])}</span></button>`).join("")}</div>
          <div class="nw-note" id="nwmode"></div>
          <div class="drow" style="margin-top:10px"><button class="btn" id="nwmcheck">Check <kbd>C</kbd></button><button class="btn ghost" id="nwmhint">Hint <kbd>H</kbd></button>
            ${NM.sel ? `<button class="btn ghost" id="nwmdel">Remove device <kbd>Del</kbd></button>` : ""}<button class="btn ghost" id="nwmreset">Clear</button></div>
          <div id="nwmout"></div>
          <div class="nw-k">Click a cable to remove it. Keys: <kbd>1</kbd>–<kbd>9</kbd> pick · <kbd>Esc</kbd> cancel · <kbd>C</kbd> check · <kbd>H</kbd> hint</div>
        </div>
      </div>`;
    ctx.el.querySelectorAll("[data-m]").forEach((b) => b.onclick = () => { NM.mid = b.dataset.m; ctx.store.set("cur", NM.mid); nmLoad(); nmDraw(); });
    ctx.el.querySelectorAll("[data-p]").forEach((b) => b.onclick = () => nmPick(b.dataset.p));
    ctx.el.querySelector("#nwmcheck").onclick = nmRunCheck;
    ctx.el.querySelector("#nwmhint").onclick = nmHint;
    ctx.el.querySelector("#nwmreset").onclick = () => { NM.placed = {}; NM.links = []; nmSave(); NM.issues = null; NM.quiz = null; NM.ok = false; NM.sel = null; nmDraw(); };
    const del = ctx.el.querySelector("#nwmdel"); if (del) del.onclick = nmRemoveSel;
    const mode = ctx.el.querySelector("#nwmode");
    mode.textContent = NM.pickType ? `Placing: ${DEV[NM.pickType][1]}. Click a glowing spot.` : NM.sel ? `Cabling from ${nmName(NM.sel)}: click another device (click it again to cancel).` : "Click a placed device, then another, to run a cable between them.";
    nmSvg(); nmOut();
    ctx.report(`Network map: ${m.title}`);
  }
  function nmSvg() {
    const m = nmMap(), host = NM.ctx.el.querySelector("#nwmap"), bad = new Set((NM.issues || []).flatMap((i) => i.bad));
    const pos = Object.fromEntries(m.slots.map((s) => [s[0], s]));
    const badLink = (a, b) => (NM.issues || []).some((i) => i.bad.length === 2 && i.bad.includes(a) && i.bad.includes(b));
    let h = `<svg viewBox="0 0 900 520" role="img" aria-label="Floor plan">`;
    m.rooms.forEach(([x, y, w, hh, t]) => { h += `<rect class="nw-room" x="${x}" y="${y}" width="${w}" height="${hh}" rx="6"/><text class="nw-roomt" x="${x + 10}" y="${y + hh - 10}">${t}</text>`; });
    NM.links.forEach(([a, b], i) => {
      const A = pos[a], B = pos[b], poe = [NM.placed[a], NM.placed[b]].includes("poe") && [NM.placed[a], NM.placed[b]].some((t) => t === "ap" || t === "cam");
      h += `<line class="nw-link ${poe ? "poe" : ""} ${badLink(a, b) ? "bad" : ""}" x1="${A[1]}" y1="${A[2]}" x2="${B[1]}" y2="${B[2]}"/><line class="nw-linkhit" data-l="${i}" x1="${A[1]}" y1="${A[2]}" x2="${B[1]}" y2="${B[2]}"><title>Click to remove this cable</title></line>`;
    });
    m.slots.forEach(([id, x, y, lab, acc]) => {
      const t = NM.placed[id];
      if (!t) {
        const can = NM.pickType && acc.includes(NM.pickType);
        h += `<g class="nw-slot ${can ? "can" : ""}" data-slot="${id}"><circle cx="${x}" cy="${y}" r="22"/><text x="${x}" y="${y + 38}" text-anchor="middle">${lab}</text></g>`;
      } else {
        h += `<g class="nw-dev ${NM.sel === id ? "sel" : ""} ${bad.has(id) ? "bad" : ""}" data-dev="${id}"><circle cx="${x}" cy="${y}" r="24"/><text class="ic" x="${x}" y="${y + 7}" text-anchor="middle">${DEV[t][0]}</text><text class="lb" x="${x}" y="${y + 41}" text-anchor="middle">${DEV[t][1]}</text></g>`;
      }
    });
    host.innerHTML = h + `</svg>`;
    host.querySelectorAll("[data-slot]").forEach((g) => g.onclick = () => nmPlace(g.dataset.slot));
    host.querySelectorAll("[data-dev]").forEach((g) => g.onclick = () => nmClickDev(g.dataset.dev));
    host.querySelectorAll("[data-l]").forEach((l) => l.onclick = () => { NM.links.splice(+l.dataset.l, 1); nmChanged(); });
  }
  function nmOut() {
    const out = NM.ctx.el.querySelector("#nwmout"); if (!out) return;
    if (!NM.issues) { out.innerHTML = ""; return; }
    if (NM.issues.length) {
      out.innerHTML = `<ul class="nw-issues">${NM.issues.map((i) => `<li>${i.text}<div class="nw-tip">${i.tip}</div></li>`).join("")}</ul>`;
      return;
    }
    out.innerHTML = `<div class="hintbox" style="color:var(--jade)">✓ It all works: every device reaches the router, power goes where there's no outlet, no loops, nothing exposed.</div><div class="dq"><h4>One exam question</h4><div id="nwmq"></div></div>`;
    const m = nmMap(), was = !!NM.solved[m.id];
    NM.quiz = quiz(out.querySelector("#nwmq"), m.q, () => {
      NM.solved[m.id] = true; NM.ctx.store.set("solved", NM.solved);
      if (!was) NM.ctx.toast(`${m.title}: wired ✓`);
      const pill = NM.ctx.el.querySelector(".nw-head .pill"); if (pill) pill.textContent = `${Object.keys(NM.solved).length}/${MAPS.length} maps done`;
    }, "WIRED");
  }
  function nmPick(t) {
    if (!(nmLeft()[t] > 0)) return;
    NM.pickType = NM.pickType === t ? null : t; NM.sel = null; nmDraw();
  }
  function nmPlace(slot) {
    const sl = slotOf(slot);
    if (!NM.pickType) { NM.ctx.toast(`Pick a device first. ${sl[3]} takes: ${sl[4].map((t) => DEV[t][1]).join(", ")}.`); return; }
    if (!sl[4].includes(NM.pickType)) { NM.ctx.toast(`${DEV[NM.pickType][1]} doesn't go at ${sl[3]}. Try a glowing spot.`, true); return; }
    NM.placed[slot] = NM.pickType;
    if (!(nmLeft()[NM.pickType] > 0)) NM.pickType = null;
    nmChanged();
  }
  function nmClickDev(id) {
    if (NM.pickType) { NM.pickType = null; }
    if (!NM.sel) NM.sel = id;
    else if (NM.sel === id) NM.sel = null;
    else {
      const a = NM.sel, i = NM.links.findIndex(([x, y]) => (x === a && y === id) || (x === id && y === a));
      if (i >= 0) NM.links.splice(i, 1); else NM.links.push([a, id]);
      NM.sel = null;
    }
    nmChanged();
  }
  function nmRemoveSel() {
    if (!NM.sel) return;
    const id = NM.sel; delete NM.placed[id]; NM.links = NM.links.filter(([a, b]) => a !== id && b !== id); NM.sel = null; nmChanged();
  }
  function nmChanged() {
    nmSave();
    if (NM.issues) { const was = NM.issues.length; NM.issues = nmCheck(nmMap(), NM.placed, NM.links); if (!NM.issues.length && was) NM.issues = null; }
    nmDraw();
  }
  function nmRunCheck() {
    NM.issues = nmCheck(nmMap(), NM.placed, NM.links); NM.quiz = null;
    NM.ctx.report(NM.issues.length ? `${NM.issues.length} problem(s)` : "Network works");
    nmDraw();
  }
  function nmHint() {
    const out = NM.ctx.el.querySelector("#nwmout"), issues = nmCheck(nmMap(), NM.placed, NM.links), placedN = Object.keys(NM.placed).length;
    let t;
    if (!placedN) t = "Start where the internet comes in: put the ONT/modem at “ISP line in”, the router next to it, then the switches.";
    else if (issues.length && !issues[0].bad.length && !NM.links.length) t = issues[0].text + " " + issues[0].tip;
    else if (!NM.links.length) t = "Cable from the outside in: ONT → router, router → switch, router or switch → PoE switch, then each device to a switch.";
    else t = issues.length ? issues[0].tip : "Looks right. Press Check.";
    out.insertAdjacentHTML("afterbegin", `<div class="hintbox">💡 ${t}</div>`);
  }

  Lab.register({
    id: "netmap", title: "Build the network map", icon: "🗺️", exams: ["a1", "net"],
    blurb: "Place the modem, router, switches, APs and cameras on a floor plan and cable them. PoE, loops and exposed servers get caught.",
    render: nmRender,
    progress: (ctx) => { const n = Object.keys(ctx.store.get("solved", {})).length; return n ? `${n}/${MAPS.length} wired` : ""; },
    key(e) {
      if (!live(NM)) return;
      const k = e.key.toLowerCase();
      if (NM.quiz && !NM.quiz.finished && "1234".includes(e.key)) { e.preventDefault(); NM.quiz.choose(+e.key - 1); return; }
      if (/^[1-9]$/.test(e.key)) { const t = Object.keys(nmMap().need)[+e.key - 1]; if (t) { e.preventDefault(); nmPick(t); } }
      else if (e.key === "Escape") { NM.pickType = null; NM.sel = null; nmDraw(); }
      else if (e.key === "Delete" || e.key === "Backspace") { if (NM.sel) { e.preventDefault(); nmRemoveSel(); } }
      else if (k === "c") { e.preventDefault(); nmRunCheck(); }
      else if (k === "h") { e.preventDefault(); nmHint(); }
    },
    remote(cmd, v) {
      if (!live(NM)) return;
      if (cmd === "show") nmHint();
      else if (cmd === "grade") { if (NM.quiz && !NM.quiz.finished) NM.quiz.choose(v - 1); else nmRunCheck(); }
    },
    _test: { MAPS, nmCheck },
  });

  // =====================================================================
  // 3. Subnetting trainer
  // =====================================================================
  const ip2n = (s) => s.split(".").reduce((a, o) => a * 256 + (+o), 0);
  const n2ip = (n) => [n >>> 24, (n >>> 16) & 255, (n >>> 8) & 255, n & 255].join(".");
  const maskN = (p) => (p <= 0 ? 0 : (0xFFFFFFFF << (32 - p)) >>> 0);
  const netN = (n, p) => (n & maskN(p)) >>> 0;
  const bcN = (n, p) => (netN(n, p) | (~maskN(p) >>> 0)) >>> 0;
  const usable = (p) => (p >= 31 ? 0 : 2 ** (32 - p) - 2);
  const prefixFor = (hosts) => { for (let p = 30; p >= 1; p--) if (usable(p) >= hosts) return p; return 0; };
  const maskToPrefix = (m) => { const n = ip2n(m); let p = 0; while (p < 32 && (n & (0x80000000 >>> p))) p++; return maskN(p) === n ? p : -1; };
  function kindOf(s) {
    const [a, b] = s.split(".").map(Number);
    if (a === 127) return "loop";
    if (a === 169 && b === 254) return "apipa";
    if (a === 10 || (a === 172 && b >= 16 && b <= 31) || (a === 192 && b === 168)) return "priv";
    return "pub";
  }
  const KINDS = [["priv", "Private (RFC 1918)"], ["pub", "Public"], ["apipa", "APIPA (link-local)"], ["loop", "Loopback"]];
  const ORD = ["first", "second", "third", "fourth"];
  const MATH = { ip2n, n2ip, maskN, netN, bcN, usable, prefixFor, maskToPrefix, kindOf };

  function randPrivate() {
    const r = ri(0, 2);
    if (r === 0) return ip2n(`10.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`);
    if (r === 1) return ip2n(`172.${ri(16, 31)}.${ri(0, 255)}.${ri(1, 254)}`);
    return ip2n(`192.168.${ri(0, 255)}.${ri(1, 254)}`);
  }
  function explainAddr(n, p) {
    const m = maskN(p), net = netN(n, p), bc = bcN(n, p);
    let s;
    if (p % 8 === 0) s = `/${p} = ${n2ip(m)}: it ends on an octet boundary, so the first ${p / 8} octet${p > 8 ? "s are" : " is"} the network and the rest are hosts.`;
    else {
      const i = Math.floor(p / 8), sh = 24 - 8 * i, mo = (m >>> sh) & 255, blk = 256 - mo, oct = (n >>> sh) & 255, st = Math.floor(oct / blk) * blk;
      s = `/${p} = ${n2ip(m)}. The interesting octet is the ${ORD[i]} (${mo}), so the block size is 256 − ${mo} = <b>${blk}</b>. Subnets there go 0, ${blk}, ${blk * 2}…; ${oct} falls in the ${st}–${st + blk - 1} block.`;
    }
    return `${s} Network <b>${n2ip(net)}</b>, broadcast <b>${n2ip(bc)}</b>, hosts ${n2ip(net + 1)} – ${n2ip(bc - 1)} (${usable(p).toLocaleString("en-US")} usable = 2^${32 - p} − 2).`;
  }
  // Builds a question from a correct answer plus candidate wrong answers [value, note].
  function mc(prompt, right, wrongs, why, tip, filler) {
    const seen = new Set([right]), opts = [[right, null]];
    for (const [v, nt] of wrongs) if (v != null && !seen.has(v) && opts.length < 4) { seen.add(v); opts.push([v, nt]); }
    let guard = 0;
    while (opts.length < 4 && guard++ < 50) { const v = filler(); if (!seen.has(v)) { seen.add(v); opts.push([v, null]); } }
    shuffle(opts);
    const notes = {}; opts.forEach(([, nt], k) => { if (nt) notes[k] = nt; });
    return { prompt, choices: opts.map((o) => o[0]), a: opts.findIndex((o) => o[0] === right), why, tip, notes };
  }
  const SUBTYPES = { addr: ["net", "bc", "first", "last", "which"], size: ["hosts", "mask", "cidr", "fit", "need"], kind: ["kind"] };
  function genSub(mode) {
    const pool = mode === "mixed" ? [...SUBTYPES.addr, ...SUBTYPES.addr, ...SUBTYPES.size, "kind"] : SUBTYPES[mode];
    const type = pick(pool);
    const p = pick([20, 21, 22, 23, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30]);
    const n = randPrivate(), net = netN(n, p), bc = bcN(n, p), size = 2 ** (32 - p), ip = n2ip(n);
    const ex = explainAddr(n, p), bin = { n, p };
    const nearby = () => n2ip((net + size * ri(-3, 3) + (Math.random() < .5 ? 0 : size - 1)) >>> 0);
    const tipAddr = "Block size = 256 − the interesting mask octet. Network = the block's first address, broadcast = the next block minus one. Usable hosts = 2^(host bits) − 2.";
    if (type === "net") return { ...mc(`What is the <b>network address</b> of <span class="nw-big">${ip}/${p}</span>?`, n2ip(net),
      [[n2ip(bc), "That's the broadcast address (the last one in the block)."], [n2ip(net + size), "That's the next subnet."], [n2ip(netN(n, p + 1)), `That would be right for /${p + 1}.`], [n2ip(net + 1), "That's the first usable host, one past the network address."]], ex, tipAddr, nearby), type, bin };
    if (type === "bc") return { ...mc(`What is the <b>broadcast address</b> of <span class="nw-big">${ip}/${p}</span>?`, n2ip(bc),
      [[n2ip(net), "That's the network address (the first one)."], [n2ip(bc - 1), "That's the last usable host, one before the broadcast."], [n2ip(bcN(n, p + 1)), `That would be right for /${p + 1}.`], [n2ip(bcN(n, p - 1)), `That would be right for /${p - 1}.`]], ex, tipAddr, nearby), type, bin };
    if (type === "first") return { ...mc(`What is the <b>first usable host</b> in <span class="nw-big">${ip}/${p}</span>?`, n2ip(net + 1),
      [[n2ip(net), "That's the network address itself; hosts can't use it."], [n2ip(bc - 1), "That's the last usable host."], [n2ip(netN(n, p + 1) + 1), `That would be right for /${p + 1}.`], [n2ip(net + 2), null]], ex, tipAddr, nearby), type, bin };
    if (type === "last") return { ...mc(`What is the <b>last usable host</b> in <span class="nw-big">${ip}/${p}</span>?`, n2ip(bc - 1),
      [[n2ip(bc), "That's the broadcast address; hosts can't use it."], [n2ip(net + 1), "That's the first usable host."], [n2ip(bcN(n, p + 1) - 1), `That would be right for /${p + 1}.`], [n2ip(bc - 2), null]], ex, tipAddr, nearby), type, bin };
    if (type === "which") return { ...mc(`Host <span class="nw-big">${ip}</span> uses mask <b>${n2ip(maskN(p))}</b>. Which subnet is it in?`, `${n2ip(net)}/${p}`,
      [[`${n2ip((net - size) >>> 0)}/${p}`, "That block ends before this host."], [`${n2ip(net + size)}/${p}`, "That block starts after this host."], [`${n2ip(netN(n, p + 1) === net ? net + size / 2 : netN(n, p + 1) - size / 2)}/${p}`, "That isn't a /" + p + " boundary."]],
      ex, tipAddr, () => `${n2ip((net + size * ri(-4, 4)) >>> 0)}/${p}`), type, bin };
    if (type === "hosts") {
      const h = 32 - p;
      return { ...mc(`How many <b>usable hosts</b> fit in a <span class="nw-big">/${p}</span>?`, usable(p).toLocaleString("en-US"),
        [[(2 ** h).toLocaleString("en-US"), "That's every address; take away the network and broadcast addresses."], [(2 ** h - 1).toLocaleString("en-US"), "Take away two (network and broadcast), not one."], [usable(p + 1).toLocaleString("en-US"), `That's a /${p + 1}.`], [usable(p - 1).toLocaleString("en-US"), `That's a /${p - 1}.`]],
        `/${p} leaves ${h} host bits: 2^${h} = ${(2 ** h).toLocaleString("en-US")} addresses, minus the network and broadcast addresses = ${usable(p).toLocaleString("en-US")}.`, "Usable hosts = 2^(32 − prefix) − 2. Memorize /24 = 254, /25 = 126, /26 = 62, /27 = 30, /28 = 14, /29 = 6, /30 = 2.", () => String(ri(2, 9000))), type, bin: { n: net, p } };
    }
    if (type === "mask") return { ...mc(`What subnet mask is <span class="nw-big">/${p}</span>?`, n2ip(maskN(p)),
      [[n2ip(maskN(p + 1)), `That's /${p + 1}.`], [n2ip(maskN(p - 1)), `That's /${p - 1}.`], [n2ip(maskN(p + 2)), `That's /${p + 2}.`], [n2ip(maskN(p - 2)), `That's /${p - 2}.`]],
      `/${p} means ${p} ones from the left. ${p % 8 ? `The ${ORD[Math.floor(p / 8)]} octet gets ${p % 8} one-bit${p % 8 > 1 ? "s" : ""}: ${(maskN(p) >>> (24 - 8 * Math.floor(p / 8))) & 255}.` : ""} Mask: ${n2ip(maskN(p))}.`,
      "Mask octet values: 128, 192, 224, 240, 248, 252, 254, 255 (1 to 8 one-bits).", () => n2ip(maskN(ri(16, 30)))), type, bin: { n: net, p } };
    if (type === "cidr") return { ...mc(`<span class="nw-big">${n2ip(maskN(p))}</span> is which prefix?`, `/${p}`,
      [[`/${p + 1}`, null], [`/${p - 1}`, null], [`/${p + 2}`, null], [`/${p - 2}`, null]],
      `Count the one-bits: ${n2ip(maskN(p)).split(".").map((o) => (+o).toString(2).replace(/0/g, "").length).join(" + ")} = ${p}.`, "255 = 8 ones, 254 = 7, 252 = 6, 248 = 5, 240 = 4, 224 = 3, 192 = 2, 128 = 1.", () => `/${ri(16, 30)}`), type, bin: { n: net, p } };
    if (type === "fit") {
      const y = pick([16, 20, 22, 23, 24, 24, 25, 26]), x = ri(y + 1, Math.min(30, y + 6)), ans = 2 ** (x - y);
      return { ...mc(`How many <span class="nw-big">/${x}</span> subnets fit in one <span class="nw-big">/${y}</span>?`, String(ans),
        [[String(x - y), "That's the number of borrowed bits; raise 2 to that power."], [String(ans * 2), null], [String(ans / 2 >= 1 ? ans / 2 : ans + 1), null], [String(Math.max(0, ans - 2)), "No “minus two” for counting subnets; that's for hosts."]],
        `Going from /${y} to /${x} borrows ${x - y} bit${x - y > 1 ? "s" : ""}: 2^${x - y} = ${ans} subnets.`, "Subnets = 2^(borrowed bits). Hosts = 2^(host bits) − 2.", () => String(2 ** ri(1, 8))), type, bin: null };
    }
    if (type === "need") {
      const hosts = pick([5, 10, 12, 20, 25, 30, 31, 50, 60, 62, 63, 100, 120, 126, 127, 200, 250, 300, 500, 1000]), pp = prefixFor(hosts);
      return { ...mc(`A team needs <b>${hosts}</b> host addresses. What's the smallest subnet that fits?`, `/${pp}`,
        [[`/${pp + 1}`, `A /${pp + 1} has only ${usable(pp + 1)} usable hosts.`], [`/${pp - 1}`, `A /${pp - 1} fits (${usable(pp - 1)} hosts) but wastes addresses: a smaller one works.`], [`/${pp + 2}`, `A /${pp + 2} has only ${usable(pp + 2)} usable hosts.`]],
        `/${pp} gives ${usable(pp)} usable hosts${pp < 30 ? `; /${pp + 1} gives only ${usable(pp + 1)}` : ""}. ${hosts === usable(pp + 1) + 1 ? "Watch the edge: you need the −2 for network and broadcast." : ""}`, "Find the smallest 2^h − 2 that covers the hosts; prefix = 32 − h.", () => `/${ri(20, 30)}`), type, bin: null };
    }
    // kind
    const k = pick(["priv", "priv", "pub", "pub", "apipa", "loop"]);
    let s;
    if (k === "priv") s = pick([n2ip(randPrivate()), `172.${pick([16, 20, 31])}.${ri(0, 255)}.${ri(1, 254)}`]);
    else if (k === "pub") s = pick([`172.${pick([15, 32, 33])}.${ri(0, 255)}.${ri(1, 254)}`, `192.169.${ri(0, 255)}.${ri(1, 254)}`, `${pick([23, 34, 52, 64, 81, 104, 142, 151, 185])}.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`, `11.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`]);
    else if (k === "apipa") s = `169.254.${ri(1, 254)}.${ri(1, 254)}`;
    else s = `127.${ri(0, 255)}.${ri(0, 255)}.${ri(1, 254)}`;
    const real = kindOf(s), a = KINDS.findIndex((x) => x[0] === real);
    const whyK = { priv: "Private (RFC 1918) ranges: 10.0.0.0/8, 172.16.0.0/12 (172.16–172.31), 192.168.0.0/16. Not routed on the internet; NAT translates them.",
      pub: "It isn't in 10/8, 172.16–31, 192.168/16, 169.254/16 or 127/8, so it's a public address. Watch the traps: 172.15 and 172.32 are public, and so is 192.169.",
      apipa: "169.254.x.x is APIPA (link-local): Windows gives itself one when no DHCP server answers. On the exam it means a DHCP problem.",
      loop: "127.x.x.x is loopback: the computer talking to itself. ping 127.0.0.1 tests the TCP/IP stack." };
    return { prompt: `What kind of address is <span class="nw-big">${s}</span>?`, choices: KINDS.map((x) => x[1]), a, why: whyK[real], tip: "Private: 10/8, 172.16/12, 192.168/16 · APIPA: 169.254/16 · Loopback: 127/8.", notes: {}, type: "kind", bin: null };
  }
  function binHtml(n, p) {
    const bits = (v) => v.toString(2).padStart(32, "0");
    const rowb = (lbl, v) => `<tr><td class="lbl">${lbl}</td><td>${bits(v).split("").map((b, i) => `${i && i % 8 === 0 ? " . " : ""}<span class="${i < p ? "n" : "h"}">${b}</span>`).join("")}</td><td class="dec">${n2ip(v)}</td></tr>`;
    return `<div class="nw-bin"><table>${rowb("Address", n)}${rowb("Mask /" + p, maskN(p))}${rowb("Network", netN(n, p))}${rowb("Broadcast", bcN(n, p))}</table>
      <div class="nw-note"><span style="color:var(--jade)">Network bits</span> stay the same for the whole subnet. <span style="color:var(--sun)">Host bits</span> all 0 = network address, all 1 = broadcast.</div></div>`;
  }

  let SB = null;
  function sbRender(ctx) {
    setup(ctx);
    const st = ctx.store.get("stats", { right: 0, total: 0, streak: 0, best: 0 });
    SB = { ctx, st, mode: ctx.store.get("mode", "mixed"), q: null, quiz: null, bin: false, answered: false };
    sbNext();
  }
  function sbNext() {
    SB.q = genSub(SB.mode); SB.bin = false; SB.answered = false; sbDraw();
    SB.ctx.report(SB.q.prompt.replace(/<[^>]+>/g, ""));
  }
  function sbDraw() {
    const { ctx, st } = SB;
    ctx.el.innerHTML = `<div class="nw-sub">
      <div class="nw-head"><h2>🧮 Subnetting trainer</h2>
        <div class="seg">${[["mixed", "Mixed"], ["addr", "Addresses"], ["size", "Masks & sizes"], ["kind", "Private / APIPA"]].map(([k, v]) => `<button class="${SB.mode === k ? "on" : ""}" data-md="${k}">${v}</button>`).join("")}</div>
        <div class="nw-stats nw-sp"><span class="pill">${st.right}/${st.total} right</span><span class="pill sun">streak ${st.streak} · best ${st.best}</span></div></div>
      <div class="glass nw-card"><div id="nwsq"></div>
        <div class="drow" style="margin-top:12px">${SB.q.bin ? `<button class="btn ghost" id="nwbin">${SB.bin ? "Hide" : "Show"} binary <kbd>B</kbd></button>` : ""}
          ${SB.answered ? `<button class="btn" id="nwnext">Next <kbd>Enter</kbd></button>` : `<button class="btn ghost" id="nwskip">Skip</button>`}</div>
        <div id="nwbinbox">${SB.bin && SB.q.bin ? binHtml(SB.q.bin.n, SB.q.bin.p) : ""}</div>
        <div class="nw-k">Keys: <kbd>1</kbd>–<kbd>4</kbd> answer · <kbd>B</kbd> binary · <kbd>Enter</kbd> next</div></div></div>`;
    ctx.el.querySelectorAll("[data-md]").forEach((b) => b.onclick = () => { SB.mode = b.dataset.md; ctx.store.set("mode", SB.mode); sbNext(); });
    const on = (id, f) => { const b = ctx.el.querySelector(id); if (b) b.onclick = f; };
    on("#nwbin", sbBin); on("#nwnext", sbNext); on("#nwskip", sbNext);
    const was = SB.answered;
    SB.quiz = quiz(ctx.el.querySelector("#nwsq"), SB.q, (first) => {
      if (was) return;
      st.total++; if (first) { st.right++; st.streak++; st.best = Math.max(st.best, st.streak); } else st.streak = 0;
      ctx.store.set("stats", st); SB.answered = true;
      if (first && st.streak > 0 && st.streak % 10 === 0) ctx.toast(`${st.streak} in a row! 🔥`);
      sbDraw();
    });
    if (was) SB.quiz.choose(SB.q.a);
  }
  function sbBin() {
    if (!SB.q.bin) return;
    SB.bin = !SB.bin;
    const box = SB.ctx.el.querySelector("#nwbinbox"); if (box) box.innerHTML = SB.bin ? binHtml(SB.q.bin.n, SB.q.bin.p) : "";
    const b = SB.ctx.el.querySelector("#nwbin"); if (b) b.innerHTML = `${SB.bin ? "Hide" : "Show"} binary <kbd>B</kbd>`;
  }

  Lab.register({
    id: "subnet", title: "Subnetting trainer", icon: "🧮", exams: ["net"],
    blurb: "Endless generated questions: network, broadcast, host ranges, masks, “how many fit”, private vs APIPA. Binary helper on tap.",
    render: sbRender,
    progress: (ctx) => { const s = ctx.store.get("stats", null); return s && s.total ? `${s.right}/${s.total} · best streak ${s.best}` : ""; },
    key(e) {
      if (!live(SB)) return;
      const k = e.key.toLowerCase();
      if ("1234".includes(e.key) && e.key.length === 1) { e.preventDefault(); SB.quiz && SB.quiz.choose(+e.key - 1); }
      else if (k === "b") { e.preventDefault(); sbBin(); }
      else if ((e.key === "Enter" || e.code === "Space" || k === "n") && SB.answered) { e.preventDefault(); sbNext(); }
    },
    remote(cmd, v) {
      if (!live(SB)) return;
      if (cmd === "show") { if (SB.answered) sbNext(); else sbBin(); }
      else if (cmd === "grade") { if (SB.answered) sbNext(); else SB.quiz && SB.quiz.choose(v - 1); }
    },
    _math: MATH, _gen: genSub,
  });

  // =====================================================================
  // 4. Wi-Fi survey
  // =====================================================================
  // Channel rounds: neighbours [channel, dBm, name]; choices [label, [channels]]; a = right index.
  const WROUNDS = [
    { band: 24, title: "Your first AP", prompt: "Here's what a Wi-Fi analyzer sees at the new office (2.4 GHz). Which channel for your access point?",
      nets: [[1, -48, "Smith-Home"], [1, -70, "Bakery"], [6, -52, "Dental-Guest"], [6, -66, "Lobby"]],
      choices: [["Channel 11", [11]], ["Channel 9", [9]], ["Channel 6", [6]], ["Channel 3", [3]]], a: 0,
      why: "2.4 GHz channels are only 5 MHz apart but each signal is about 20 MHz wide, so neighbours overlap. In North America only 1, 6 and 11 don't overlap each other. 11 is empty here. Channel 9 or 3 “look free” but overlap 6, 11 or 1 and get interference from both sides.",
      tip: "2.4 GHz non-overlapping channels (US): 1, 6, 11. Europe and China also allow 12 and 13." },
    { band: 24, title: "No empty channel", prompt: "Every clean channel is taken. Where should your AP go?",
      nets: [[1, -50, "Apt-2B"], [1, -58, "Apt-3A"], [6, -78, "Far-Away"], [11, -52, "Apt-2C"], [11, -61, "Printer-Direct"]],
      choices: [["Channel 8 (looks empty)", [8]], ["Channel 6, sharing with the weak neighbour", [6]], ["Channel 3", [3]], ["Channel 1", [1]]], a: 1,
      why: "Sharing a channel is OK: Wi-Fi devices on the same channel take turns (CSMA/CA). Overlapping channels can't coordinate, so they just corrupt each other. Pick the 1/6/11 channel with the weakest neighbours: 6, where the only network is faint (−78 dBm).",
      tip: "Co-channel (same channel) = taking turns, slower. Adjacent overlap (e.g. 1 and 3) = interference, worse." },
    { band: 24, title: "Two APs, one office", prompt: "You're putting in two APs whose coverage overlaps in the middle of the office. A neighbour is strong on 6. Which channels?",
      nets: [[6, -55, "Upstairs"], [11, -82, "Street"]],
      choices: [["1 and 6", [1, 6]], ["6 and 6", [6, 6]], ["1 and 11", [1, 11]], ["3 and 9", [3, 9]]], a: 2,
      why: "Your own APs shouldn't overlap each other or the strong neighbour. 1 and 11 leave 6 to the neighbour; the network on 11 is very weak. 3 and 9 overlap everything.",
      tip: "Lay out APs like a 1-6-11 honeycomb so neighbours never share a channel." },
    { band: 5, title: "Moving to 5 GHz", prompt: "On 5 GHz, the lower channels are busy with neighbours. Which channel for a 20 MHz-wide AP?",
      nets: [[36, -50, "Office-5G"], [40, -55, "Clinic-5G"], [44, -60, "Upstairs-5G"], [48, -63, "Cafe-5G"]],
      choices: [["Channel 36", [36]], ["Channel 149", [149]], ["Channel 44", [44]], ["Go back to 2.4 GHz channel 6", []]], a: 1,
      why: "5 GHz channels sit side by side without overlapping (at 20 MHz), and there are many of them. 149 is free. 52–64 are DFS channels: allowed, but the AP must move off them if it detects radar.",
      tip: "5 GHz: more non-overlapping channels, faster, shorter range than 2.4 GHz. DFS = Dynamic Frequency Selection (radar avoidance)." },
  ];
  const CH5 = [36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161, 165];
  const WQS = [
    { prompt: "The break-room Wi-Fi drops every day around lunch, only on 2.4 GHz. Most likely cause?",
      choices: ["The microwave oven", "The DHCP lease expires at noon", "Too many 5 GHz devices", "The router's NTP server"], a: 0,
      why: "Microwave ovens run at about 2.45 GHz, right inside the 2.4 GHz Wi-Fi band, and leak enough to drown nearby signals. Move clients to 5 GHz or the AP away from the kitchen.",
      tip: "2.4 GHz interference: microwave ovens, Bluetooth, some baby monitors and wireless cameras." },
    { prompt: "A new smart plug only finds the network when the 2.4 GHz band is on. Why?",
      choices: ["It needs WPS", "Many small smart-home devices only have a 2.4 GHz radio", "5 GHz needs a static IP", "Smart plugs only work on hidden SSIDs"], a: 1,
      why: "Cheap IoT chips usually have only a 2.4 GHz radio: it's cheaper and reaches further. Keep 2.4 GHz on for them.",
      tip: "Can't see the network? Check the device supports that band (and that standard)." },
    { prompt: "A desk in the garage, through two walls from the AP, keeps dropping. Which band gives it the best chance?",
      choices: ["6 GHz", "5 GHz", "2.4 GHz", "All bands reach the same distance"], a: 2,
      why: "Lower frequencies travel further and get through walls better. 2.4 GHz is slower, but reaches the furthest; 6 GHz reaches the least.",
      tip: "Range: 2.4 > 5 > 6 GHz. Speed and free channels: 6 > 5 > 2.4 GHz." },
    { prompt: "A laptop with a Wi-Fi 6 (802.11ax) card can't see the new 6 GHz network, but a newer phone can. Why?",
      choices: ["6 GHz needs Wi-Fi 6E or Wi-Fi 7 hardware", "The SSID is hidden", "The laptop needs a firmware update for WPA2", "6 GHz only works with Ethernet"], a: 0,
      why: "Plain Wi-Fi 6 uses 2.4 and 5 GHz. The 6 GHz band arrived with Wi-Fi 6E (and Wi-Fi 7 also uses it). Older cards can't tune to it at all.",
      tip: "802.11a: 5 · b/g: 2.4 · n (Wi-Fi 4): 2.4/5 · ac (Wi-Fi 5): 5 only · ax (Wi-Fi 6): 2.4/5 · 6E: adds 6 GHz · be (Wi-Fi 7): 2.4/5/6." },
    { prompt: "You're setting up a 6 GHz network. Which security can it use?",
      choices: ["WPA2-AES, same as 5 GHz", "WPA3 only (or Enhanced Open for open networks)", "WEP for compatibility", "Any, it doesn't matter on 6 GHz"], a: 1,
      why: "The 6 GHz band requires WPA3 (or OWE “Enhanced Open” for networks without a password). WPA2 isn't allowed there.",
      tip: "6 GHz = WPA3 required." },
    { prompt: "The AP log says “radar detected, changing channel” and the 5 GHz network drops for a minute now and then. What's happening?",
      choices: ["Someone is jamming the network", "It's on a DFS channel and must move away when it hears radar", "The AP is overheating", "The 2.4 GHz band is full"], a: 1,
      why: "Some 5 GHz channels (in the US, 52–144) are shared with weather and airport radar. On these DFS channels the AP has to listen and leave when radar shows up. Pick a non-DFS channel (36–48 or 149–165) if it keeps happening.",
      tip: "DFS = Dynamic Frequency Selection." },
    { prompt: "In a busy apartment building, someone sets the 2.4 GHz channel width to 40 MHz for speed. What's the problem?",
      choices: ["40 MHz is illegal everywhere", "It covers most of the 2.4 GHz band, so it overlaps nearly every neighbour", "40 MHz turns off encryption", "Nothing, wider is always better"], a: 1,
      why: "The usable 2.4 GHz band only fits three 20 MHz channels. A 40 MHz channel eats two of those slots and overlaps almost everyone. Keep 2.4 GHz at 20 MHz; use wide channels on 5 or 6 GHz.",
      tip: "Wider channels = more speed but fewer non-overlapping channels." },
    { prompt: "Two readings from a Wi-Fi analyzer: −82 dBm and −55 dBm. Which is the stronger signal?",
      choices: ["−82 dBm", "−55 dBm", "They're the same", "dBm doesn't measure strength"], a: 1,
      why: "Signal strength in dBm is negative, and closer to zero is stronger. −55 is solid; −82 is close to unusable.",
      tip: "Around −67 dBm or better is a common target for calls and video." },
    { prompt: "A wireless headset keeps cutting out while the laptop is on 2.4 GHz Wi-Fi. Why could they clash?",
      choices: ["Bluetooth also uses the 2.4 GHz band", "Bluetooth uses 5 GHz", "The headset uses the Wi-Fi password", "Headsets need PoE"], a: 0,
      why: "Bluetooth hops around the 2.4 GHz band, the same band as 2.4 GHz Wi-Fi. Moving the laptop to 5 GHz usually helps both.",
      tip: "Bluetooth = 2.4 GHz, short range (PAN)." },
    { prompt: "Where should a home's single access point go?",
      choices: ["In the metal network cabinet in the garage", "Central and high, in the open, away from metal and big water (fish tanks)", "On the floor behind the TV", "Next to the microwave"], a: 1,
      why: "Signals spread out from the AP, and metal, concrete, mirrors and water absorb or reflect them. Central and high covers the most space.",
      tip: "Building materials matter: metal and concrete block the most; drywall the least." },
    { prompt: "A client wants proof the new Wi-Fi covers every room before they pay. What do you do?",
      choices: ["Run a site survey and show a heat map", "Ping the router from the closet", "Check the DHCP leases", "Count the antennas"], a: 0,
      why: "A site survey walks the space with a Wi-Fi analyzer and records signal strength at each spot. The result is a heat map that shows strong and weak areas.",
      tip: "Network+: site survey, heat map, Wi-Fi analyzer." },
    { prompt: "Which Wi-Fi standard only works on 5 GHz?",
      choices: ["802.11g", "802.11n (Wi-Fi 4)", "802.11ac (Wi-Fi 5)", "802.11ax (Wi-Fi 6)"], a: 2,
      why: "802.11ac (Wi-Fi 5) is 5 GHz only. 802.11n and 802.11ax work on 2.4 and 5 GHz; 802.11g is 2.4 GHz.",
      tip: "a = 5 GHz, b/g = 2.4, n = both, ac = 5, ax = both (6E adds 6)." },
  ];

  let WF = null;
  function wfRender(ctx) {
    setup(ctx);
    WF = { ctx, part: ctx.store.get("part", "ch"), i: 0, quiz: null, preview: null, done: false,
      chDone: ctx.store.get("chDone", {}), qDone: ctx.store.get("qDone", {}) };
    WF.i = WF.part === "ch" ? Math.max(0, WROUNDS.findIndex((_, k) => !WF.chDone[k])) : Math.max(0, WQS.findIndex((_, k) => !WF.qDone[k]));
    wfDraw();
  }
  function chart(r, mine, good) {
    const W = 860, H = 230, base = 190, L = 40, R = 20;
    const hFor = (dbm) => Math.max(14, Math.min(150, (dbm + 95) / 55 * 150));
    let h = `<svg viewBox="0 0 ${W} ${H}" role="img" aria-label="Wi-Fi channel chart">`;
    let xOf, half;
    if (r.band === 24) {
      const step = (W - L - R) / 14; xOf = (c) => L + c * step; half = 2 * step;
      h += `<line class="ax" x1="${L}" y1="${base}" x2="${W - R}" y2="${base}"/>`;
      for (let c = 1; c <= 13; c++) h += `<text class="tk ${c > 11 ? "off" : ""}" x="${xOf(c)}" y="${base + 18}" text-anchor="middle">${c}</text>`;
      h += `<text class="tk off" x="${xOf(12.5)}" y="${base + 34}" text-anchor="middle">not used in the US</text><text class="tk" x="${L}" y="${base + 34}">2.4 GHz channel</text>`;
    } else {
      const step = (W - L - R) / (CH5.length + 1); xOf = (c) => L + (CH5.indexOf(c) + 1) * step; half = step / 2;
      h += `<rect class="dfs" x="${xOf(52) - half}" y="20" width="${step * 4}" height="${base - 20}"/><text class="dfst" x="${xOf(52) - half + 4}" y="34">DFS (radar)</text>`;
      h += `<line class="ax" x1="${L}" y1="${base}" x2="${W - R}" y2="${base}"/>`;
      CH5.forEach((c) => { h += `<text class="tk" x="${xOf(c)}" y="${base + 18}" text-anchor="middle">${c}</text>`; });
      h += `<text class="tk" x="${L}" y="${base + 34}">5 GHz channel (20 MHz each; 100–144 left out)</text>`;
    }
    const hump = (c, hh, cls, lbl) => {
      const x0 = xOf(c) - half, x1 = xOf(c) + half, top = base - hh, w = x1 - x0;
      return `<path class="nw-hump ${cls}" d="M${x0} ${base} C${x0 + w * .3} ${top}, ${x1 - w * .3} ${top}, ${x1} ${base} Z"/>` + (lbl ? `<text class="hl" x="${xOf(c)}" y="${top + 12}" text-anchor="middle">${lbl}</text>` : "");
    };
    const stack = {};
    r.nets.forEach(([c, dbm, name]) => {
      const k = stack[c] = (stack[c] || 0) + 1;
      h += hump(c, hFor(dbm), "", "") + `<text class="hl" x="${xOf(c)}" y="${base - hFor(dbm) - 4 - (k - 1) * 13}" text-anchor="middle">${esc(name)} ${dbm}</text>`;
    });
    (mine || []).forEach((c) => { h += hump(c, 120, good ? "mine good" : "mine", ""); h += `<text class="hl" x="${xOf(c)}" y="${base - 126}" text-anchor="middle" style="fill:var(--sun)">you</text>`; });
    return h + `</svg>`;
  }
  function wfDraw() {
    const { ctx } = WF, chN = Object.keys(WF.chDone).length, qN = Object.keys(WF.qDone).length;
    const isCh = WF.part === "ch", list = isCh ? WROUNDS : WQS, item = list[WF.i];
    ctx.el.innerHTML = `
      <div class="nw-head"><h2>📶 Wi-Fi survey</h2>
        <div class="seg"><button class="${isCh ? "on" : ""}" data-pt="ch">Channel planner</button><button class="${!isCh ? "on" : ""}" data-pt="q">Bands & interference</button></div>
        <span class="pill sun nw-sp">channels ${chN}/${WROUNDS.length} · bands ${qN}/${WQS.length}</span></div>
      <div class="nw-scns">${list.map((x, k) => `<button class="nw-scn ${k === WF.i ? "on" : ""}" data-i="${k}"><b>${isCh ? x.title : "Question " + (k + 1)}</b><span>${isCh ? (x.band === 24 ? "2.4 GHz" : "5 GHz") : esc(x.prompt.slice(0, 44)) + "…"}</span>${(isCh ? WF.chDone : WF.qDone)[k] ? `<span class="nw-ok">✓</span>` : ""}</button>`).join("")}</div>
      <div class="glass nw-card">
        ${isCh ? `<div class="nw-chart" id="nwchart"></div><div class="nw-note" style="margin:6px 0 10px">Purple humps are neighbours (name and signal in dBm). Hover a choice to preview your AP.</div>` : ""}
        <div id="nwwq"></div>
        <div class="drow" style="margin-top:12px">${WF.done ? `<button class="btn" id="nwwnext">Next <kbd>Enter</kbd></button>` : `<button class="btn ghost" id="nwwhint">Hint <kbd>H</kbd></button>`}</div>
        <div id="nwwhintbox"></div>
        <div class="nw-k">Keys: <kbd>1</kbd>–<kbd>4</kbd> answer · <kbd>H</kbd> hint · <kbd>Enter</kbd> next</div>
      </div>`;
    ctx.el.querySelectorAll("[data-pt]").forEach((b) => b.onclick = () => { WF.part = b.dataset.pt; ctx.store.set("part", WF.part); WF.i = 0; WF.done = false; wfDraw(); });
    ctx.el.querySelectorAll("[data-i]").forEach((b) => b.onclick = () => { WF.i = +b.dataset.i; WF.done = false; wfDraw(); });
    const nx = ctx.el.querySelector("#nwwnext"); if (nx) nx.onclick = wfNext;
    const hb = ctx.el.querySelector("#nwwhint"); if (hb) hb.onclick = wfHint;
    const drawChart = (ch, good) => { const c = ctx.el.querySelector("#nwchart"); if (c) c.innerHTML = chart(item, ch, good); };
    const q = isCh ? { prompt: item.prompt, choices: item.choices.map((c) => c[0]), a: item.a, why: item.why, tip: item.tip } : item;
    const wasDone = WF.done;
    WF.quiz = quiz(ctx.el.querySelector("#nwwq"), q, (first) => {
      if (wasDone) return;
      (isCh ? WF.chDone : WF.qDone)[WF.i] = true;
      ctx.store.set(isCh ? "chDone" : "qDone", isCh ? WF.chDone : WF.qDone);
      WF.done = true;
      if (!first) ctx.toast("Got there. It'll stick next time.");
      wfDraw();
    });
    if (wasDone) WF.quiz.choose(q.a);
    if (isCh) {
      drawChart(wasDone ? item.choices[item.a][1] : [], wasDone);
      if (!wasDone) ctx.el.querySelectorAll("#nwwq .ch").forEach((b) => {
        b.onmouseenter = b.onfocus = () => drawChart(item.choices[+b.dataset.c][1], false);
        b.onmouseleave = () => drawChart([], false);
      });
    }
    ctx.report(isCh ? `Wi-Fi: ${item.title}` : `Wi-Fi question ${WF.i + 1}`);
  }
  function wfNext() {
    const list = WF.part === "ch" ? WROUNDS : WQS, done = WF.part === "ch" ? WF.chDone : WF.qDone;
    let n = list.findIndex((_, k) => k > WF.i && !done[k]);
    if (n < 0) n = list.findIndex((_, k) => !done[k]);
    if (n < 0) {
      if (WF.part === "ch") { WF.part = "q"; WF.ctx.store.set("part", "q"); n = Math.max(0, WQS.findIndex((_, k) => !WF.qDone[k])); WF.ctx.toast("Channels done ✓ On to bands."); }
      else { n = (WF.i + 1) % list.length; WF.ctx.toast("All done ✓ Going round again."); }
    }
    WF.i = n; WF.done = false; wfDraw();
  }
  function wfHint() {
    const box = WF.ctx.el.querySelector("#nwwhintbox"); if (!box) return;
    const t = WF.part === "ch" ? (WROUNDS[WF.i].band === 24 ? "On 2.4 GHz, stick to 1, 6 or 11, then pick the one with the fewest or weakest neighbours." : "5 GHz channels don't overlap at 20 MHz: look for an empty one that isn't in the DFS area.") : WQS[WF.i].tip;
    box.innerHTML = `<div class="hintbox">💡 ${t}</div>`;
  }

  Lab.register({
    id: "wifi", title: "Wi-Fi survey", icon: "📶", exams: ["a1", "net"],
    blurb: "Read a Wi-Fi analyzer and pick channels (1/6/11, 5 GHz, DFS), then choose bands for range vs speed and hunt down interference.",
    render: wfRender,
    progress: (ctx) => { const a = Object.keys(ctx.store.get("chDone", {})).length, b = Object.keys(ctx.store.get("qDone", {})).length; return a + b ? `${a + b}/${WROUNDS.length + WQS.length} done` : ""; },
    key(e) {
      if (!live(WF)) return;
      const k = e.key.toLowerCase();
      if ("1234".includes(e.key) && e.key.length === 1) { e.preventDefault(); WF.quiz && WF.quiz.choose(+e.key - 1); }
      else if (k === "h") { e.preventDefault(); wfHint(); }
      else if ((e.key === "Enter" || k === "n") && WF.done) { e.preventDefault(); wfNext(); }
    },
    remote(cmd, v) {
      if (!live(WF)) return;
      if (cmd === "show") { if (WF.done) wfNext(); else wfHint(); }
      else if (cmd === "grade") { if (WF.done) wfNext(); else WF.quiz && WF.quiz.choose(v - 1); }
    },
  });
})();
