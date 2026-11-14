/* Hardware bench: four IT Lab plug-ins for CompTIA A+ Core 1 (220-1201), hardware domain.
   Everything is drawn with inline SVG and CSS: nothing loads from the internet.
   Registers: hw-connectors (Connector ID), hw-build (Build to spec), hw-raid (RAID chooser), hw-order (Put it in order). */
(function () {
  "use strict";
  if (!window.Lab) return;

  // ---------- shared bits ----------
  const CSS = `
  .hw { --hwbg: #0d1430; }
  .hw-calm *, .hw-calm *::before, .hw-calm *::after { animation: none !important; transition: none !important; }
  .hw-top { display: flex; align-items: flex-start; gap: 14px; flex-wrap: wrap; margin-bottom: 14px; }
  .hw-top h2 { margin: 0 0 2px; font-size: 22px; }
  .hw-top p { margin: 0; }
  .hw-stats { margin-left: auto; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
  .hw-grid { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr); gap: 16px; align-items: start; }
  @media (max-width: 900px) { .hw-grid { grid-template-columns: 1fr; } }
  .hw-card { padding: 20px; }
  .hw-card h3 { margin: 2px 0 6px; font-size: 18px; }
  .hw-card h4 { margin: 0 0 8px; font-size: 15px; }
  .hw-stage { position: relative; display: grid; place-items: center; min-height: 300px; border-radius: 18px;
              background: radial-gradient(420px 260px at 50% 40%, #1d2a5c, var(--hwbg) 75%); border: 1px solid var(--line); overflow: hidden; }
  .hw-stage svg { width: 100%; max-width: 460px; height: auto; display: block; }
  .hw-stage.pop svg { animation: hwpop .45s cubic-bezier(.2, 1.2, .4, 1) both; }
  @keyframes hwpop { from { opacity: 0; transform: scale(.92) translateY(8px); } }
  .hw-view { position: absolute; left: 14px; bottom: 10px; font-size: 12px; color: var(--muted); }
  .hw-why { margin-top: 12px; }
  .hw-why b { color: var(--ink); }
  .hw-row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-top: 12px; }
  .hw-row .btn { padding: 8px 14px; font-size: 14px; }
  .hw-hint { margin-top: 10px; padding: 9px 12px; border-radius: 12px; background: rgba(142, 148, 242, .1); border: 1px dashed rgba(142, 148, 242, .4); font-size: 13.5px; color: #cfd2ff; animation: rise .35s both; }
  .hw-sheet { display: grid; grid-template-columns: repeat(auto-fill, minmax(190px, 1fr)); gap: 12px; }
  .hw-sheet figure { margin: 0; padding: 10px; border-radius: 16px; background: rgba(13, 20, 48, .7); border: 1px solid var(--line); }
  .hw-sheet figure svg { width: 100%; height: auto; display: block; }
  .hw-sheet figcaption { font-size: 13px; } .hw-sheet figcaption b { display: block; font-size: 14px; }
  /* build */
  .hw-seg { margin-bottom: 14px; flex-wrap: wrap; border-radius: 18px; }
  .hw-seg button .ok { color: var(--jade); margin-left: 4px; }
  .hw-job { padding: 12px 14px; border-radius: 14px; background: rgba(255, 207, 122, .07); border: 1px solid rgba(255, 207, 122, .22); margin-bottom: 12px; }
  .hw-job p { margin: 4px 0 6px; font-size: 14px; }
  .hw-job ul { margin: 0; padding-left: 18px; font-size: 13.5px; color: var(--muted); }
  .hw-slot { margin: 10px 0 0; }
  .hw-slabel { font-size: 12px; letter-spacing: 1px; text-transform: uppercase; color: var(--muted); margin-bottom: 5px; }
  .hw-opts { display: flex; gap: 6px; flex-wrap: wrap; }
  .hw-opt { text-align: left; padding: 6px 10px; border-radius: 11px; cursor: pointer; font-size: 13px; line-height: 1.3; color: var(--ink);
            background: rgba(255, 255, 255, .05); border: 1px solid var(--line); transition: background .15s, border-color .2s; }
  .hw-opt:hover { background: rgba(255, 255, 255, .09); }
  .hw-opt small { display: block; color: var(--muted); font-size: 11.5px; }
  .hw-opt.on { border-color: rgba(255, 207, 122, .65); background: rgba(255, 207, 122, .12); }
  .hw-opt.bad { border-color: rgba(255, 107, 107, .7); background: rgba(255, 107, 107, .1); }
  .hw-sticky { position: sticky; top: 84px; }
  @media (max-width: 900px) { .hw-sticky { position: static; } }
  .hw-board { border-radius: 16px; background: var(--hwbg); border: 1px solid var(--line); }
  .hw-board svg { width: 100%; height: auto; display: block; }
  .hw-board .hwb-bad rect, .hw-board .hwb-bad circle { stroke: #ff6b6b; stroke-width: 3px; }
  .hw-board .hwb-bad { animation: hwbad 1s ease-in-out infinite alternate; }
  @keyframes hwbad { from { opacity: 1; } to { opacity: .45; } }
  .hw-board .hwb-in { animation: hwin .4s cubic-bezier(.2, 1.3, .4, 1) both; transform-box: fill-box; transform-origin: center; }
  @keyframes hwin { from { opacity: 0; transform: scale(.8); } }
  .hw-screen { margin-top: 12px; min-height: 120px; padding: 12px 14px; border-radius: 14px; background: #04060f; border: 1px solid rgba(255, 255, 255, .1);
               font: 13px/1.55 "Cascadia Mono", Consolas, ui-monospace, monospace; color: #cdd3f3; }
  .hw-screen div { animation: rise .25s both; }
  .hw-screen .g { color: #7fe0b0; } .hw-screen .r { color: #ff9b9b; } .hw-screen .y { color: var(--sun); } .hw-screen .m { color: var(--muted); }
  .hw-screen .why2 { font-family: "Segoe UI", system-ui, sans-serif; font-size: 13.5px; color: var(--ink); margin: 4px 0 6px 18px; }
  /* raid */
  .hw-disks { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; padding: 16px 8px 8px; }
  .hw-disk { width: 86px; padding: 8px 6px; border-radius: 12px; background: #151c3d; border: 1px solid rgba(255, 255, 255, .14); cursor: pointer; color: var(--ink); font: inherit;
             transition: transform .2s, opacity .4s, border-color .3s; position: relative; }
  .hw-disk:hover { transform: translateY(-2px); }
  .hw-disk .dl { font-size: 11px; color: var(--muted); margin-bottom: 5px; }
  .hw-disk i { display: block; margin: 3px 0; padding: 4px 0; border-radius: 6px; font: 600 11.5px ui-monospace, monospace; font-style: normal; text-align: center; }
  .hw-disk i.d { background: rgba(142, 148, 242, .28); } .hw-disk i.p { background: rgba(255, 207, 122, .3); color: var(--sun); } .hw-disk i.c { background: rgba(79, 179, 169, .3); }
  .hw-disk.dead { border-color: var(--coral); opacity: .55; animation: shake .4s; }
  .hw-disk.dead i { filter: grayscale(1); opacity: .5; }
  .hw-disk.dead::after { content: "✗"; position: absolute; inset: 0; display: grid; place-items: center; font-size: 40px; color: var(--coral); }
  .hw-pair { display: flex; gap: 6px; padding: 6px; border-radius: 16px; border: 1px dashed rgba(79, 179, 169, .45); }
  .hw-verdict { text-align: center; font-weight: 600; margin: 6px 0 2px; padding: 8px; border-radius: 12px; }
  .hw-verdict.ok { color: #9ff0d0; background: rgba(79, 179, 169, .12); } .hw-verdict.lost { color: #ffb3b3; background: rgba(255, 107, 107, .1); }
  .hw-math { font: 14px ui-monospace, Consolas, monospace; color: var(--sun); text-align: center; margin: 6px 0; }
  .hw-legend { display: flex; gap: 14px; justify-content: center; font-size: 12px; color: var(--muted); flex-wrap: wrap; }
  .hw-legend span::before { content: ""; display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 5px; vertical-align: -1px; background: var(--c); }
  /* order */
  .hw-cols { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.15fr); gap: 16px; align-items: start; }
  @media (max-width: 900px) { .hw-cols { grid-template-columns: 1fr; } }
  .hw-pool, .hw-slots { display: flex; flex-direction: column; gap: 7px; min-height: 60px; }
  .hw-item { display: grid; grid-template-columns: auto 1fr; gap: 9px; align-items: center; text-align: left; width: 100%; padding: 9px 12px; border-radius: 12px; cursor: grab;
             background: rgba(255, 255, 255, .06); border: 1px solid var(--line); color: var(--ink); font: inherit; font-size: 14px; transition: background .15s, border-color .2s, transform .15s; }
  .hw-item:hover { background: rgba(255, 255, 255, .1); transform: translateX(2px); }
  .hw-item.drag { opacity: .4; }
  .hw-slotrow { display: grid; grid-template-columns: 30px 1fr; gap: 8px; align-items: center; }
  .hw-slotrow .n { font: 700 14px ui-monospace, monospace; color: var(--muted); text-align: center; }
  .hw-drop { min-height: 42px; border-radius: 12px; border: 1px dashed rgba(255, 255, 255, .16); display: flex; align-items: stretch; }
  .hw-drop.over { border-color: var(--sun); background: rgba(255, 207, 122, .08); }
  .hw-drop .hw-item { border-style: solid; }
  .hw-drop.lock .hw-item { border-color: rgba(79, 179, 169, .7); background: rgba(79, 179, 169, .14); cursor: default; }
  .hw-drop.lock .n, .hw-slotrow.lock .n { color: var(--jade); }
  .hw-drop.miss { animation: shake .35s; border-color: var(--coral); }
  .hw-steps { margin: 0; padding-left: 0; list-style: none; counter-reset: s; }
  .hw-steps li { counter-increment: s; display: grid; grid-template-columns: 26px 1fr; gap: 6px; padding: 5px 0; font-size: 14px; animation: rise .4s both; }
  .hw-steps li::before { content: counter(s); font: 700 13px ui-monospace, monospace; color: var(--jade); }
  .hw-steps li span { color: var(--muted); }
  `;
  function addCss() {
    if (document.getElementById("hw-css")) return;
    const s = document.createElement("style"); s.id = "hw-css"; s.textContent = CSS; document.head.appendChild(s);
  }
  const shuffle = (a) => { a = a.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; };
  const range = (n) => Array.from({ length: n }, (_, i) => i);
  const live = (ctx) => !!(ctx && ctx.el && ctx.el.isConnected);
  // Enter/Space on a focused button already clicks it: don't act twice.
  const onButton = (e) => e.target && e.target.tagName === "BUTTON" && (e.key === "Enter" || e.key === " ");
  const later = (ctx, ms, fn) => setTimeout(() => { if (live(ctx)) fn(); }, ctx.calm ? 0 : ms);
  const shell = (ctx, cls, html) => { ctx.el.innerHTML = `<div class="hw ${cls} ${ctx.calm ? "hw-calm" : ""}">${html}</div>`; return ctx.el.firstElementChild; };

  // ---------- 1. Connector ID ----------
  const K = { m: "#c9d0dd", md: "#7f889e", ho: "#060915", pl: "#262b40", pll: "#4b5277", wh: "#ebe6d8", go: "#e9b949", bl: "#3b6fd8" };
  const R = (x, y, w, h, f, more = "") => `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${f}" ${more}/>`;
  const P = (d, f, more = "") => `<path d="${d}" fill="${f}" ${more}/>`;
  const O = (cx, cy, r, f, more = "") => `<circle cx="${cx}" cy="${cy}" r="${r}" fill="${f}" ${more}/>`;
  const usbC = (dy) => R(55, 60 + dy, 130, 42, K.m, 'rx="21"') + R(61, 66 + dy, 118, 30, K.ho, 'rx="15"') + R(76, 76 + dy, 88, 10, K.pll, 'rx="4"')
    + range(12).map((i) => R(80 + i * 6.9, 76 + dy, 3, 2.6, K.go) + R(80 + i * 6.9, 83.4 + dy, 3, 2.6, K.go)).join("");
  const plate = () => R(8, 38, 224, 84, K.md, 'rx="8"') + O(26, 80, 10, "#5b6378") + O(26, 80, 4.5, K.ho) + O(214, 80, 10, "#5b6378") + O(214, 80, 4.5, K.ho);
  const cable = (col) => R(0, 74, 34, 12, col, 'rx="3"');
  const molexCells = (cols, x0, y0, w, h, gap) => range(2).map((row) => range(cols).map((i) => {
    const x = x0 + i * (w + gap), y = y0 + row * (h + 8), round = (i + row) % 2 === 0;
    return R(x, y, w, h, K.ho, `rx="${round ? 6 : 1.5}"`) + O(x + w / 2, y + h / 2, 3, K.md);
  }).join("")).join("");
  const latch = (x, w) => R(x, 26, w, 18, K.pl, 'rx="3"') + R(x + 5, 31, w - 10, 6, K.ho, 'rx="2"');

  const CONN = [
    { id: "rj45", name: "RJ45", g: "net", view: "Port (jack), front view",
      what: "Wired Ethernet on twisted-pair cable (Cat 5e/6). 8 pins, so it's also called 8P8C.",
      hint: "Count the gold contacts: 8. The little lights show link and activity.",
      svg: () => R(48, 18, 144, 124, K.m, 'rx="6"') + R(55, 25, 9, 6, "#5fd38a", 'rx="1.5"') + R(176, 25, 9, 6, "#ffb347", 'rx="1.5"')
        + P("M68 40H172V110H140V128H100V110H68Z", K.ho)
        + range(8).map((i) => `<line x1="${80 + i * 11.5}" y1="42" x2="${82 + i * 11.5}" y2="64" stroke="${K.go}" stroke-width="3" stroke-linecap="round"/>`).join("") },
    { id: "rj11", name: "RJ11", g: "net", view: "Port (jack), front view",
      what: "Telephone line and DSL. Narrower than RJ45: 6 positions, usually only 2 or 4 wired.",
      hint: "Same latch shape as Ethernet, but narrower, with fewer contacts.",
      svg: () => R(68, 28, 104, 104, K.wh, 'rx="6"') + P("M84 46H156V104H133V120H107V104H84Z", K.ho)
        + range(6).map((i) => `<line x1="${92 + i * 11.2}" y1="48" x2="${94 + i * 11.2}" y2="68" stroke="${i > 0 && i < 5 ? K.go : K.md}" stroke-width="3" stroke-linecap="round"/>`).join("") },
    { id: "usba", name: "USB-A", g: "usb", view: "Port, front view",
      what: "The classic flat USB port on PCs and chargers. Only fits one way up; a blue tongue usually means USB 3.",
      hint: "A flat rectangle with a plastic tongue filling half of it.",
      svg: () => R(46, 52, 148, 56, K.m, 'rx="3"') + R(52, 58, 136, 44, K.ho) + R(62, 58, 116, 18, K.bl)
        + range(4).map((i) => R(74 + i * 25, 73, 16, 4, K.go)).join("") + R(84, 52, 12, 4, K.md) + R(144, 52, 12, 4, K.md) },
    { id: "usbc", name: "USB-C", g: "usb", view: "Port, front view",
      what: "The reversible oval USB connector: data, video and charging (USB Power Delivery) in one plug.",
      hint: "Rounded ends, a thin tongue in the middle, contacts on both sides: it goes in either way up.",
      svg: () => usbC(0) },
    { id: "microusb", name: "Micro-USB", g: "usb", view: "Port, front view",
      what: "Older Android phones and small gadgets (micro-B). The trapezoid only fits one way; USB 2.0 speed.",
      hint: "Small, flat, and the bottom corners are cut at an angle.",
      svg: () => P("M52 60H188V84L174 100H66L52 84Z", K.m) + P("M58 66H182V82L171 94H69L58 82Z", K.ho) + R(72, 66, 96, 10, K.pll)
        + range(5).map((i) => R(80 + i * 17, 73, 9, 3, K.go)).join("") },
    { id: "lightning", name: "Lightning", g: "usb", view: "Plug, top view",
      what: "Apple's 8-pin reversible plug for older iPhones and iPads (iPhone 15 and later moved to USB-C).",
      hint: "A solid metal tab with 8 gold pads, no hollow shell around it.",
      svg: () => R(96, 64, 118, 32, K.m, 'rx="9"') + range(8).map((i) => R(116 + i * 11, 70, 7, 20, K.go, 'rx="2"')).join("")
        + R(0, 72, 22, 16, K.wh, 'rx="4"') + R(18, 56, 84, 48, K.wh, 'rx="12"') },
    { id: "hdmi", name: "HDMI", g: "video", view: "Port, front view",
      what: "Digital video and audio in one cable, to TVs and monitors. 19 pins.",
      hint: "Both bottom corners are cut at an angle (a symmetric trapezoid).",
      svg: () => P("M36 54H204V86L190 104H50L36 86Z", K.m) + P("M42 60H198V84L186 98H54L42 84Z", K.ho) + R(58, 72, 124, 11, K.pll, 'rx="2"')
        + range(10).map((i) => R(61 + i * 12, 69, 5, 3, K.go)).join("") + range(9).map((i) => R(67 + i * 12, 83, 5, 3, K.go)).join("") },
    { id: "dp", name: "DisplayPort", g: "video", view: "Port, front view",
      what: "Digital video and audio, mostly for PC monitors. 20 pins; carries high refresh rates and can daisy-chain monitors.",
      hint: "Only ONE corner is cut off.",
      svg: () => P("M40 52H200V88L184 106H40Z", K.m) + P("M46 58H194V86L181 100H46Z", K.ho) + R(56, 73, 128, 11, K.pll, 'rx="2"')
        + range(10).map((i) => R(59 + i * 12.3, 70, 5, 3, K.go) + R(59 + i * 12.3, 84, 5, 3, K.go)).join("") },
    { id: "dvid", name: "DVI-D", g: "video", view: "Port, front view",
      what: "Digital-only DVI video. The flat blade with NO four holes around it is the tell (DVI-I adds them for analog).",
      hint: "A grid of square holes plus a flat slot on one side. Look around the slot.",
      svg: () => plate() + P("M44 50H196L188 110H52Z", K.wh)
        + range(3).map((rw) => range(8).map((i) => R(58 + i * 12.5, 62 + rw * 14, 6, 6, K.ho, 'rx="1"')).join("")).join("") + R(160, 75, 22, 6, K.ho, 'rx="1"') },
    { id: "vga", name: "VGA (DE-15)", g: "video", view: "Port, front view",
      what: "Analog video on old monitors and projectors. DE-15: 15 pins in 3 rows, usually blue, with thumbscrews.",
      hint: "A blue D-shape with three rows of five round holes.",
      svg: () => plate() + P("M56 52H184L174 108H66Z", K.bl) + P("M62 57H178L169 103H71Z", "#2c56b0")
        + [[80, 20, 67], [84, 18, 80], [90, 15, 93]].map(([x0, step, y]) => range(5).map((i) => O(x0 + i * step, y, 3.6, K.ho)).join("")).join("") },
    { id: "ftype", name: "F-type (coax)", g: "net", view: "Port, front view (wall plate or modem)",
      what: "Coax for cable TV and cable modems. Screws on; used with RG-6 cable.",
      hint: "A threaded hex fitting with one tiny hole for the copper center wire.",
      svg: () => `<polygon points="${range(6).map((i) => { const a = Math.PI / 3 * i; return `${120 + 58 * Math.cos(a)},${80 + 58 * Math.sin(a)}`; }).join(" ")}" fill="${K.m}"/>`
        + O(120, 80, 38, K.md) + [36, 33, 30, 27].map((r) => O(120, 80, r, "none", `stroke="${K.m}" stroke-width="1.2" opacity=".7"`)).join("")
        + O(120, 80, 23, K.wh) + O(120, 80, 4.5, K.ho) },
    { id: "lc", name: "LC (fiber)", g: "fiber", view: "Plug, side view",
      what: "Small fiber connector with a 1.25 mm ferrule and a push-and-latch lever. Often in pairs (duplex) and in SFP modules.",
      hint: "Fiber, and small, with a latch lever on top like a little RJ45.",
      svg: () => cable("#3fc1c9") + P("M34 70L80 66V94L34 90Z", "#2a8f96") + R(80, 60, 86, 40, "#e6dcc4", 'rx="4"')
        + `<path d="M92 60L104 44H160" stroke="#e6dcc4" stroke-width="7" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`
        + R(166, 74, 28, 12, "#d9d9d9", 'rx="2"') + R(194, 76, 24, 8, K.wh, 'rx="3"') },
    { id: "sc", name: "SC (fiber)", g: "fiber", view: "Plug, side view",
      what: "Square push-pull fiber connector with a 2.5 mm ferrule. Clicks in and pulls straight out.",
      hint: "Fiber, with a big square body: push in, pull out, no twisting.",
      svg: () => cable("#f0c330") + P("M34 68L88 62V98L34 92Z", "#c9a12a") + R(88, 46, 92, 68, K.bl, 'rx="5"') + R(100, 56, 68, 48, "#2f5fc4", 'rx="3"')
        + R(180, 60, 14, 40, "#2c56b0", 'rx="2"') + R(194, 73, 30, 14, K.wh, 'rx="3"') },
    { id: "st", name: "ST (fiber)", g: "fiber", view: "Plug, side view",
      what: "Round bayonet fiber connector: push in and twist to lock (“stick and twist”). 2.5 mm ferrule.",
      hint: "Fiber, with a round metal ring and a slot that twists onto a pin.",
      svg: () => cable("#f0c330") + P("M34 70L90 64V96L34 90Z", K.pl) + R(90, 54, 62, 52, K.m, 'rx="7"')
        + range(9).map((i) => `<line x1="${96 + i * 6.5}" y1="58" x2="${96 + i * 6.5}" y2="102" stroke="${K.md}" stroke-width="1.5"/>`).join("")
        + `<path d="M118 54V74H138" stroke="${K.ho}" stroke-width="6" fill="none"/>` + R(152, 72, 58, 16, K.wh, 'rx="5"') },
    { id: "satadata", name: "SATA data", g: "drive", view: "Drive connector, front view",
      what: "Carries data between a drive and the motherboard. 7 pins, L-shaped so it only goes in one way.",
      hint: "A short L-shaped blade with 7 contacts.",
      svg: () => R(52, 60, 136, 40, K.pl, 'rx="3"') + R(60, 68, 120, 24, K.ho) + P("M66 72H174V79H76V88H66Z", K.pll)
        + range(7).map((i) => R(84 + i * 13, 73, 7, 4, K.go)).join("") },
    { id: "satapower", name: "SATA power", g: "drive", view: "Drive connector, front view",
      what: "Power for SATA drives from the PSU: 3.3 V, 5 V and 12 V. 15 pins, wider than SATA data, also L-shaped.",
      hint: "The long L-shaped blade, with 15 contacts.",
      svg: () => R(10, 60, 220, 40, K.pl, 'rx="3"') + R(16, 68, 208, 24, K.ho) + P("M22 72H218V88H208V79H22Z", K.pll)
        + range(15).map((i) => R(28 + i * 12, 73, 6, 4, K.go)).join("") },
    { id: "molex", name: "Molex (4-pin)", g: "drive", view: "Cable plug, front view (wire colours shown)",
      what: "Old 4-pin peripheral power: 12 V (yellow), 5 V (red) and two grounds (black). Older drives, fans, adapters.",
      hint: "White nylon, four big round sockets in a row, two corners cut.",
      svg: () => P("M36 52L50 38H190L204 52V108H36Z", K.wh) + range(4).map((i) => O(72 + i * 32, 74, 12, K.ho) + O(72 + i * 32, 74, 6, K.md)).join("")
        + ["#f2c94c", "#111", "#111", "#e2483d"].map((c, i) => R(62 + i * 32, 96, 20, 6, c, 'rx="2"')).join("") },
    { id: "atx24", name: "24-pin ATX", g: "power", view: "Cable plug, front view",
      what: "Main motherboard power from the PSU. 2 rows of 12 (older boards used 20-pin).",
      hint: "The biggest power plug in the case: two rows of twelve.",
      svg: () => latch(106, 28) + R(12, 42, 216, 78, K.pl, 'rx="4"') + molexCells(12, 19, 50, 14, 27, 3.3) },
    { id: "eps8", name: "8-pin EPS (CPU)", g: "power", view: "Cable plug, front view",
      what: "Extra 12 V power for the CPU, plugged in near the CPU socket (EPS12V, sometimes split 4+4).",
      hint: "One solid block of 2 × 4 near the top of the board, by the CPU.",
      svg: () => latch(106, 28) + R(68, 42, 104, 78, K.pl, 'rx="4"') + molexCells(4, 76, 50, 20, 27, 4.7) },
    { id: "pcie", name: "PCIe 6+2-pin", g: "power", view: "Cable plug, front view",
      what: "Extra power for graphics cards: 6-pin gives 75 W, 8-pin gives 150 W, on top of 75 W from the slot.",
      hint: "A 2 × 3 block with a separate 2 × 1 piece that clips on to make 8 pins.",
      svg: () => latch(72, 28) + R(36, 42, 100, 78, K.pl, 'rx="4"') + molexCells(3, 44, 50, 24, 27, 6) + R(146, 42, 42, 78, K.pl, 'rx="4"') + molexCells(1, 155, 50, 24, 27, 0) },
    { id: "audio35", name: "3.5 mm audio", g: "audio", view: "Plug, side view",
      what: "Analog audio: headphones, speakers, mics. On PCs: green = speakers/line out, pink = mic, light blue = line in.",
      hint: "A round metal pin with black bands between the tip and rings.",
      svg: () => cable("#1d2135") + R(14, 58, 80, 44, K.pl, 'rx="10"') + R(92, 66, 14, 28, K.md, 'rx="2"') + R(106, 72, 62, 16, K.m)
        + R(168, 72, 5, 16, K.ho) + R(173, 72, 20, 16, K.m) + R(193, 73, 5, 14, K.ho) + P("M198 72H208L216 77Q220 80 216 83L208 88H198Z", K.m) },
    { id: "thunderbolt", name: "Thunderbolt", g: "usb", view: "Port, front view (with its symbol)",
      what: "Thunderbolt 3 and 4 use the USB-C shape: look for the lightning-bolt symbol. Up to 40 Gbps, carries PCIe and DisplayPort. Thunderbolt 1 and 2 used the Mini DisplayPort shape.",
      hint: "The shape is one you know. What's the symbol next to it?",
      svg: () => P("M122 12L104 44H118L108 70L140 32H125L134 12Z", "#eef1ff") + usbC(28) },
  ];
  const conSvg = (c, label) => `<svg viewBox="0 0 240 160" role="img" aria-label="${label ? c.name : "A connector drawing"}">${c.svg()}</svg>`;

  const CS = { ctx: null, cur: null, choices: [], done: false, wrong: new Set(), hint: false, sheet: false, last: null };
  function conNew() {
    let c; do { c = CONN[Math.floor(Math.random() * CONN.length)]; } while (c === CS.last);
    CS.cur = c; CS.last = c; CS.done = false; CS.wrong = new Set(); CS.hint = false;
    const others = CONN.filter((x) => x !== c && !(c.id === "usbc" && x.id === "thunderbolt"));
    const same = shuffle(others.filter((x) => x.g === c.g)).slice(0, 2);
    const rest = shuffle(others.filter((x) => !same.includes(x))).slice(0, 3 - same.length);
    if (c.id === "thunderbolt" && !same.some((x) => x.id === "usbc")) same[0] = CONN.find((x) => x.id === "usbc");
    CS.choices = shuffle([c, ...same, ...rest]);
    CS.ctx.report("Connector ID: which connector is this?");
  }
  function conPick(k) {
    const ctx = CS.ctx, c = CS.choices[k];
    if (!c || CS.done || CS.sheet) return;
    const st = ctx.store;
    if (c === CS.cur) {
      CS.done = true;
      const streak = st.get("streak", 0) + 1; st.set("streak", streak);
      if (streak > st.get("best", 0)) st.set("best", streak);
      const known = st.get("known", {}); known[c.id] = 1; st.set("known", known);
      if (streak > 1 && streak % 5 === 0) ctx.toast(`${streak} in a row!`);
    } else {
      CS.wrong.add(k); st.set("streak", 0);
      if (CS.wrong.size >= 2) CS.done = true;   // two misses: show the answer
    }
    conRender(false);
  }
  function conRender(fresh) {
    const ctx = CS.ctx, st = ctx.store, c = CS.cur;
    const known = Object.keys(st.get("known", {})).length;
    const head = `<div class="hw-top"><div><h2>🔌 Connector ID</h2><p class="muted">Name the plug or port from the drawing. Two misses and it shows you.</p></div>
      <div class="hw-stats"><span class="pill sun">Streak ${st.get("streak", 0)}</span><span class="pill">Best ${st.get("best", 0)}</span><span class="pill">${known}/${CONN.length} named</span>
      <button class="btn ghost" id="hw-sheetb">${CS.sheet ? "Back to the game" : "Study sheet"}</button></div></div>`;
    if (CS.sheet) {
      const root = shell(ctx, "", `${head}<div class="hw-sheet">${CONN.map((x) => `<figure>${conSvg(x, true)}<figcaption><b>${ctx.esc(x.name)}</b><span class="muted">${ctx.esc(x.what)}</span></figcaption></figure>`).join("")}</div>`);
      root.querySelector("#hw-sheetb").onclick = () => { CS.sheet = false; conRender(true); };
      return;
    }
    const right = CS.done && !CS.wrong.size;
    const root = shell(ctx, "", `${head}
      <div class="hw-grid">
        <div class="glass hw-card"><div class="hw-stage ${fresh ? "pop" : ""}">${conSvg(c, false)}<span class="hw-view">${ctx.esc(c.view)}</span></div></div>
        <div class="glass hw-card">
          <h4>Which connector is this?</h4>
          ${CS.choices.map((x, k) => `<button class="ch ${CS.wrong.has(k) ? "wrong" : ""} ${CS.done && x === c ? "right" : ""}" data-k="${k}" ${CS.done ? "disabled" : ""}><kbd>${k + 1}</kbd><span>${ctx.esc(x.name)}</span></button>`).join("")}
          ${CS.hint && !CS.done ? `<div class="hw-hint">💡 ${ctx.esc(c.hint)}</div>` : ""}
          ${CS.done ? `<div class="why hw-why">${right ? `<span class="stamp">RIGHT</span>` : ""}<h5>${ctx.esc(c.name)}</h5>${ctx.esc(c.what)}</div>
            <div class="hw-row"><button class="btn" id="hw-next">Next connector <kbd>Enter</kbd></button></div>`
            : `<div class="hw-row"><button class="btn ghost" id="hw-hintb">Hint <kbd>H</kbd></button><span class="tag">Keys 1–4 · or your phone's grade buttons</span></div>`}
        </div>
      </div>`);
    root.querySelectorAll(".ch").forEach((b) => b.onclick = () => conPick(+b.dataset.k));
    root.querySelector("#hw-sheetb").onclick = () => { CS.sheet = true; conRender(false); };
    const nx = root.querySelector("#hw-next"); if (nx) { nx.onclick = () => { conNew(); conRender(true); }; nx.focus({ preventScroll: true }); }
    const hb = root.querySelector("#hw-hintb"); if (hb) hb.onclick = () => { CS.hint = true; conRender(false); };
  }

  Lab.register({
    id: "hw-connectors", title: "Connector ID", icon: "🔌", exams: ["a1"],
    blurb: "22 plugs and ports drawn from the front: RJ45 to SATA, HDMI to ST fiber. Name it, learn what it's for, keep the streak.",
    progress: (ctx) => { const b = ctx.store.get("best", 0), n = Object.keys(ctx.store.get("known", {})).length; return b || n ? `Best streak ${b} · ${n}/${CONN.length}` : ""; },
    render(ctx) { addCss(); CS.ctx = ctx; CS.sheet = false; conNew(); conRender(true); },
    key(e) {
      if (!live(CS.ctx) || onButton(e)) return;
      if ("1234".includes(e.key) && e.key) { e.preventDefault(); conPick(+e.key - 1); }
      else if (e.key === "Enter" && CS.done && !CS.sheet) { e.preventDefault(); conNew(); conRender(true); }
      else if (e.key.toLowerCase() === "h" && !CS.done) { CS.hint = true; conRender(false); }
    },
    remote(cmd, v) {
      if (!live(CS.ctx)) return;
      if (cmd === "grade") { if (CS.done) { conNew(); conRender(true); } else conPick(v - 1); }
      else if (cmd === "show") { CS.hint = true; conRender(false); }
    },
  });

  // ---------- shared RAID facts (Build to spec + RAID chooser) ----------
  const RAID = {
    0: { min: 2, kind: "striping", usable: (n, s) => n * s, math: (n, s) => `${n} × ${s} TB = ${n * s} TB`, survives: "no failures" },
    1: { min: 2, max: 2, kind: "mirroring", usable: (n, s) => s, math: (n, s) => `2 drives mirrored = one drive's worth = ${s} TB`, survives: "1 failure (the other copy)" },
    5: { min: 3, kind: "striping with parity", usable: (n, s) => (n - 1) * s, math: (n, s) => `(${n} − 1) × ${s} TB = ${(n - 1) * s} TB`, survives: "1 failure" },
    6: { min: 4, kind: "striping with double parity", usable: (n, s) => (n - 2) * s, math: (n, s) => `(${n} − 2) × ${s} TB = ${(n - 2) * s} TB`, survives: "any 2 failures" },
    10: { min: 4, even: true, kind: "mirror + stripe", usable: (n, s) => n / 2 * s, math: (n, s) => `${n} ÷ 2 × ${s} TB = ${n / 2 * s} TB`, survives: "1 failure per mirror pair" },
  };
  function raidFits(l, n) {
    const r = RAID[l];
    if (n < r.min) return `RAID ${l} needs at least ${r.min} drives.`;
    if (r.max && n > r.max) return `RAID 1 mirrors a pair of drives; with ${n} drives the mirror-and-stripe version is RAID 10.`;
    if (r.even && n % 2) return `RAID 10 needs an even number of drives (pairs of mirrors).`;
    return "";
  }
  function raidAlive(l, n, dead) {
    const f = [...dead];
    if (l === 0) return f.length === 0;
    if (l === 1) return f.length < n;
    if (l === 5) return f.length <= 1;
    if (l === 6) return f.length <= 2;
    return !range(n / 2).some((p) => dead.has(2 * p) && dead.has(2 * p + 1));
  }

  // ---------- 2. Build to spec ----------
  const PARTS = {
    board: [
      { id: "b-am5", name: "ATX board", d: "Socket AM5 · DDR5 · M.2 NVMe · 4 SATA", socket: "AM5", ram: "DDR5", ff: "ATX", sata: 4, ecc: false },
      { id: "b-am4", name: "Micro-ATX board", d: "Socket AM4 · DDR4 · M.2 NVMe · 4 SATA", socket: "AM4", ram: "DDR4", ff: "mATX", sata: 4, ecc: false },
      { id: "b-itx", name: "Mini-ITX board", d: "Socket LGA1700 · DDR4 · M.2 NVMe · 2 SATA", socket: "LGA1700", ram: "DDR4", ff: "ITX", sata: 2, ecc: false },
      { id: "b-srv", name: "Server board", d: "Socket LGA1700 · DDR5 + ECC · M.2 NVMe · 8 SATA", socket: "LGA1700", ram: "DDR5", ff: "ATX", sata: 8, ecc: true },
    ],
    cpu: [
      { id: "c-16", name: "16-core CPU", d: "Socket AM5 · 170 W · basic built-in graphics", socket: "AM5", w: 170, cores: 16, igpu: true },
      { id: "c-6a5", name: "6-core CPU", d: "Socket AM5 · 65 W · basic built-in graphics", socket: "AM5", w: 65, cores: 6, igpu: true },
      { id: "c-6a4", name: "6-core CPU", d: "Socket AM4 · 65 W · NO built-in graphics", socket: "AM4", w: 65, cores: 6, igpu: false },
      { id: "c-4", name: "4-core CPU", d: "Socket LGA1700 · 60 W · built-in graphics", socket: "LGA1700", w: 60, cores: 4, igpu: true },
      { id: "c-8", name: "8-core CPU", d: "Socket LGA1700 · 65 W · built-in graphics", socket: "LGA1700", w: 65, cores: 8, igpu: true },
    ],
    ram: [
      { id: "r-8d4", name: "8 GB DDR4", d: "1 × 8 GB DIMM", type: "DDR4", gb: 8, sticks: 1, ff: "DIMM" },
      { id: "r-16d4", name: "16 GB DDR4", d: "2 × 8 GB DIMM", type: "DDR4", gb: 16, sticks: 2, ff: "DIMM" },
      { id: "r-32d5", name: "32 GB DDR5", d: "2 × 16 GB DIMM", type: "DDR5", gb: 32, sticks: 2, ff: "DIMM" },
      { id: "r-64d5", name: "64 GB DDR5", d: "2 × 32 GB DIMM", type: "DDR5", gb: 64, sticks: 2, ff: "DIMM" },
      { id: "r-ecc", name: "32 GB DDR5 ECC", d: "2 × 16 GB, error-correcting", type: "DDR5", gb: 32, sticks: 2, ff: "DIMM", ecc: true },
      { id: "r-so", name: "16 GB DDR4 SO-DIMM", d: "2 × 8 GB, laptop size", type: "DDR4", gb: 16, sticks: 2, ff: "SO-DIMM" },
    ],
    boot: [
      { id: "s-nv2", name: "2 TB NVMe SSD", d: "M.2 stick, PCIe/NVMe", iface: "nvme", ssd: true, tb: 2 },
      { id: "s-nv256", name: "256 GB NVMe SSD", d: "M.2 stick, PCIe/NVMe", iface: "nvme", ssd: true, tb: 0.25 },
      { id: "s-sata", name: "500 GB SATA SSD", d: "2.5″ drive + SATA cable", iface: "sata", ssd: true, tb: 0.5 },
      { id: "s-hdd", name: "1 TB hard drive", d: "3.5″ 7200 rpm + SATA cable", iface: "sata", ssd: false, tb: 1 },
      { id: "s-none", name: "No boot drive", d: "skip it", none: true },
    ],
    data: [
      { id: "d-0", name: "None", d: "", n: 0 },
      { id: "d-2", name: "2 × 4 TB", d: "3.5″ hard drives", n: 2 },
      { id: "d-4", name: "4 × 4 TB", d: "3.5″ hard drives", n: 4 },
    ],
    raid: [{ id: "x-n", name: "No RAID", d: "separate drives", lvl: null }, ...[0, 1, 5, 6, 10].map((l) => ({ id: `x-${l}`, name: `RAID ${l}`, d: RAID[l].kind, lvl: l }))],
    gpu: [
      { id: "g-0", name: "No graphics card", d: "use the CPU's graphics", w: 0, tier: 0 },
      { id: "g-1", name: "Entry GPU", d: "75 W, powered by the slot alone", w: 75, tier: 1 },
      { id: "g-2", name: "Mid-range GPU", d: "200 W, one 8-pin PCIe cable", w: 200, tier: 2 },
      { id: "g-3", name: "High-end GPU", d: "320 W, two 8-pin PCIe cables", w: 320, tier: 3 },
    ],
    psu: [300, 550, 750, 1000].map((w) => ({ id: `p-${w}`, name: `${w} W PSU`, d: w === 300 ? "small and quiet" : w === 1000 ? "very big" : "", w })),
    lram: [
      { id: "l-keep", name: "Keep the 8 GB in it", d: "one DDR4 SO-DIMM", add: null },
      { id: "l-so4", name: "Add 8 GB DDR4 SO-DIMM", d: "laptop-size stick", add: "ok" },
      { id: "l-dimm", name: "Add 8 GB DDR4 DIMM", d: "desktop-size stick", add: "dimm" },
      { id: "l-so5", name: "Add 8 GB DDR5 SO-DIMM", d: "laptop size, newer type", add: "ddr5" },
    ],
    ldrive: [
      { id: "l-nvme", name: "1 TB M.2 NVMe SSD", d: "M.2 2280, PCIe/NVMe", fit: "m2", proto: "nvme" },
      { id: "l-m2s", name: "1 TB M.2 SATA SSD", d: "M.2 2280, SATA", fit: "m2", proto: "sata" },
      { id: "l-25", name: "1 TB 2.5″ SATA SSD", d: "swaps in for the old hard drive", fit: "bay", proto: "sata" },
      { id: "l-35", name: "2 TB 3.5″ hard drive", d: "desktop size", fit: "big", proto: "sata" },
    ],
  };
  const SLOTNAME = { board: "Motherboard", cpu: "CPU", ram: "Memory (RAM)", boot: "Boot drive", data: "Extra drives", raid: "RAID for the extra drives", gpu: "Graphics", psu: "Power supply", lram: "Memory", ldrive: "New drive" };
  const part = (slot, id) => PARTS[slot].find((p) => p.id === id) || null;
  const DESK = ["board", "cpu", "ram", "boot", "gpu", "psu"];

  const JOBS = [
    { id: "video", title: "Video editor's workstation", slots: DESK,
      text: "Mei edits 4K video all day. Renders take forever on her old PC.",
      needs: ["Rendering uses every CPU core it can get", "4K timelines want lots of memory (64 GB)", "A fast drive with room for projects (1 TB or more)", "Effects and encoding run on a real graphics card"],
      hint: "Start from the CPU with the most cores, then pick the board with the same socket, then RAM that matches that board.",
      spec: (b) => [
        [b.cpu.cores >= 12, "Plenty of cores for rendering", "Rendering wants many cores: pick the 16-core CPU."],
        [b.ram.gb >= 64, "64 GB of RAM for 4K", `Only ${b.ram.gb} GB of RAM: 4K editing wants 64 GB.`],
        [!b.boot.none && b.boot.iface === "nvme" && b.boot.tb >= 1, "Fast NVMe drive with room for projects", "Projects want a fast NVMe SSD of at least 1 TB."],
        [b.gpu.tier >= 2, "A real graphics card for effects", "Built-in or entry graphics are too slow for effects: add a mid-range or high-end GPU."],
      ] },
    { id: "server", title: "Small office file server", slots: ["board", "cpu", "ram", "boot", "data", "raid", "gpu", "psu"],
      text: "Eight people share files. It must keep running if a drive dies, and they need at least 8 TB of shared space.",
      needs: ["Memory that catches its own errors (ECC), on a board that supports it", "Separate data drives in a RAID that survives a dead drive", "At least 8 TB usable after RAID", "No gaming graphics needed"],
      hint: "ECC RAM only helps on a board that supports ECC. For the data: 4 drives, and a RAID level that has redundancy and leaves 8 TB usable.",
      spec: (b) => {
        const lvl = b.raid ? b.raid.lvl : null, n = b.data ? b.data.n : 0;
        const ok = lvl != null && lvl !== 0 && n && !raidFits(lvl, n);
        const use = ok ? RAID[lvl].usable(n, 4) : 0;
        return [
          [!!b.ram.ecc && b.board.ecc, "ECC memory on an ECC board", b.ram.ecc ? "ECC RAM needs a board that supports ECC: use the server board." : "A server should use ECC RAM: it detects and fixes single-bit memory errors."],
          [ok, `Redundant RAID (RAID ${lvl})`, lvl === 0 ? "RAID 0 has no redundancy: one dead drive loses everything." : "The data drives need a RAID level that survives a dead drive."],
          [use >= 8, `${use} TB usable`, `Only ${use} TB usable: they need at least 8 TB.`],
          [b.gpu.tier <= 1, "No gaming graphics", "A file server doesn't need a big graphics card: it's wasted power."],
        ];
      } },
    { id: "thin", title: "Thin client for reception", slots: DESK,
      text: "Reception only runs a browser and a remote desktop. It hides behind the monitor, so it must be tiny and quiet.",
      needs: ["Small enough to mount behind a monitor (Mini-ITX)", "No graphics card: the CPU's graphics are plenty", "An SSD so it starts fast", "Small, quiet power supply"],
      hint: "The smallest board is Mini-ITX. Its socket and RAM type decide the CPU and memory.",
      spec: (b) => [
        [b.board.ff === "ITX", "Mini-ITX: fits behind the monitor", "That board is too big to hide behind a monitor: use Mini-ITX."],
        [b.gpu.tier === 0, "Uses the CPU's built-in graphics", "A browser doesn't need a graphics card: leave it out."],
        [!b.boot.none && b.boot.ssd, "SSD for fast start-up", "Use an SSD: a hard drive is slow and noisy."],
        [b.psu.w <= 300, "Small, quiet PSU", "This needs about 150 W: a 300 W PSU is plenty, bigger is wasted money and noise."],
        [b.ram.gb >= 8, "8 GB or more of RAM", "Give it at least 8 GB."],
      ] },
    { id: "gaming", title: "Gaming PC on a budget", slots: DESK,
      text: "A first gaming PC for new games at 1080p. Every dollar counts.",
      needs: ["A graphics card that runs new games at 1080p, but not a top-end one", "A 6- or 8-core CPU (16 cores is overkill here)", "16 GB of RAM or more", "An SSD", "A PSU with headroom, but not a giant one"],
      hint: "Budget gaming = mid-range GPU. Then any matching CPU, board and RAM. Add up the watts.",
      spec: (b) => [
        [b.gpu.tier === 2, "Mid-range GPU: the sweet spot", b.gpu.tier === 3 ? "The high-end GPU blows the budget." : "New games need a real graphics card: mid-range is the budget pick."],
        [b.cpu.cores >= 6 && b.cpu.cores <= 8, "6–8 cores: right for games", b.cpu.cores > 8 ? "16 cores is overkill for gaming on a budget." : "4 cores struggles with new games: pick 6 or 8."],
        [b.ram.gb >= 16, "16 GB+ of RAM", `${b.ram.gb} GB is too little for new games: 16 GB or more.`],
        [!b.boot.none && b.boot.ssd, "SSD for fast loading", "Games load much faster from an SSD."],
        [b.psu.w <= 750, "Sensible PSU size", "1000 W is far more than this needs: wasted money."],
      ] },
    { id: "laptop", title: "Upgrade a 2018 laptop's storage", slots: ["lram", "ldrive"], laptop: true,
      text: "Her 2018 laptop takes ages to boot from its 500 GB hard drive. The manual says: one 2.5″ SATA bay (the old drive is in it), one M.2 2280 slot that supports SATA only (not NVMe), and DDR4 SO-DIMM memory with one empty slot.",
      needs: ["Faster: an SSD", "More space than 500 GB", "Must physically fit and be seen by the laptop", "More memory is a bonus, if you pick the right stick"],
      hint: "Laptops take laptop-size parts: SO-DIMM memory and 2.5″ or M.2 drives. And read the manual line about the M.2 slot again.",
      spec: () => [] },
  ];

  const BS = { ctx: null, job: 0, pick: {}, bad: null, lines: null, timers: [], hint: false };
  function buildOf(job) { const b = {}; job.slots.forEach((s) => { b[s] = part(s, BS.pick[s]); }); return b; }
  function watts(b) {
    const hdd = (b.data ? b.data.n : 0) + (b.boot && b.boot.iface === "sata" && !b.boot.ssd ? 1 : 0);
    const total = 60 + b.cpu.w + b.gpu.w + hdd * 8;
    return { total, need: Math.ceil(total * 1.25 / 10) * 10, hdd };
  }
  // POST: walk the build like a real PC would and stop at the first problem.
  function post(job) {
    const b = buildOf(job), L = [];
    const miss = job.slots.find((s) => !b[s]);
    if (miss) return { L: [["r", `Pick a part for: ${SLOTNAME[miss]}.`]], bad: miss, missing: true };
    const fail = (slot, title, why) => { L.push(["r", `✗ ${title}`], ["why2", why]); return { L, bad: slot }; };
    if (job.laptop) {
      L.push(["m", "Power button pressed…"]);
      if (b.lram.add === "dimm") return fail("lram", "The memory won't fit", "A DIMM is the long desktop stick. Laptops take the short SO-DIMM.");
      if (b.lram.add === "ddr5") return fail("lram", "The memory won't go in", "DDR5 and DDR4 have their notch in different places, so a DDR5 stick can't go in a DDR4 slot.");
      L.push(["g", `✓ Memory: ${b.lram.add ? "16 GB (8 + 8) DDR4" : "8 GB DDR4"} detected`]);
      if (b.ldrive.fit === "big") return fail("ldrive", "The drive doesn't fit", "3.5″ drives are desktop size. Laptops take 2.5″ drives or M.2 sticks.");
      if (b.ldrive.proto === "nvme") return fail("ldrive", "No bootable device", "The NVMe stick fits the M.2 slot, but this slot only speaks SATA, so the laptop never sees the drive.");
      L.push(["g", `✓ Drive detected: ${b.ldrive.name}`], ["y", "POST complete · beep"], ["m", "Next: clone the old drive onto it (or reinstall Windows)."]);
      return { L, ok: true, b };
    }
    L.push(["m", `Power button pressed… ${b.psu.name} on`]);
    if (b.cpu.socket !== b.board.socket) return fail("cpu", `The CPU doesn't fit the board`, `It's a socket ${b.cpu.socket} CPU on a socket ${b.board.socket} board. The pins and the shape are different, so it can't even be seated.`);
    L.push(["g", `✓ CPU seated in socket ${b.board.socket}`]);
    if (b.ram.ff !== "DIMM") return fail("ram", "The memory won't fit", "SO-DIMMs are short laptop sticks. A desktop board takes full-size DIMMs.");
    if (b.ram.type !== b.board.ram) return fail("ram", `${b.ram.type} memory in a ${b.board.ram} board`, `${b.ram.type} and ${b.board.ram} have their notch in different places, so the stick won't go into the slot. The board decides the RAM type.`);
    L.push(["g", `✓ Memory: ${b.ram.gb} GB ${b.ram.type} detected`]);
    const w = watts(b);
    if (w.need > b.psu.w) return fail("psu", "The power supply is too weak", `The parts draw about ${w.total} W (60 W board, RAM and fans + ${b.cpu.w} W CPU${b.gpu.w ? ` + ${b.gpu.w} W GPU` : ""}${w.hdd ? ` + ${w.hdd * 8} W drives` : ""}). With about 25% headroom that's ${w.need} W, but the PSU gives ${b.psu.w} W. It may start, then shut off under load.`);
    L.push(["g", `✓ Power: needs ~${w.need} W with headroom, PSU gives ${b.psu.w} W`]);
    if (b.gpu.tier === 0 && !b.cpu.igpu) return fail("gpu", "Fans spin, but no picture", "This CPU has no built-in graphics and there's no graphics card, so nothing drives the monitor.");
    L.push(["g", `✓ Video: ${b.gpu.tier ? b.gpu.name : "CPU's built-in graphics"}`]);
    const n = b.data ? b.data.n : 0, ports = n + (b.boot.iface === "sata" ? 1 : 0);
    if (ports > b.board.sata) return fail("data", "Not enough SATA ports", `${ports} SATA drives, but this board has only ${b.board.sata} SATA ports.`);
    if (b.raid && b.raid.lvl != null) {
      if (!n) return fail("raid", "RAID with no drives", "RAID needs extra drives to build the array from.");
      const why = raidFits(b.raid.lvl, n);
      if (why) return fail("raid", `RAID ${b.raid.lvl} can't be built`, why);
      L.push(["g", `✓ RAID ${b.raid.lvl}: ${n} × 4 TB = ${RAID[b.raid.lvl].usable(n, 4)} TB usable`]);
    } else if (n) L.push(["g", `✓ ${n} extra drives, no RAID`]);
    if (b.boot.none) return fail("boot", "No bootable device", "POST passed, but there's no drive with an operating system to start from.");
    L.push(["g", `✓ Boot drive: ${b.boot.name}`], ["y", "POST complete · one short beep (on most PCs)"]);
    return { L, ok: true, b };
  }
  function boardSvg(job) {
    const b = buildOf(job), bad = BS.bad, esc = BS.ctx.esc;
    const hl = (k) => (bad === k ? `class="hwb-bad"` : "");
    const t = (x, y, s, size = 11, col = "#a9b1d6", anchor = "start") => `<text x="${x}" y="${y}" font-size="${size}" fill="${col}" text-anchor="${anchor}" font-family="Segoe UI, system-ui, sans-serif">${esc(s)}</text>`;
    if (job.laptop) {
      const extra = b.lram && b.lram.add, dr = b.ldrive;
      return `<svg viewBox="0 0 620 400" role="img" aria-label="Laptop with its bottom panel off">
        ${R(20, 20, 580, 360, "#1b2347", 'rx="26" stroke="#3a4570" stroke-width="2"')}
        ${O(140, 120, 62, "#141a36", 'stroke="#3a4570" stroke-width="2"')}${range(7).map((i) => `<line x1="140" y1="120" x2="${140 + 55 * Math.cos(i * 0.9)}" y2="${120 + 55 * Math.sin(i * 0.9)}" stroke="#3a4570" stroke-width="5"/>`).join("")}${t(140, 200, "CPU fan", 11, "#a9b1d6", "middle")}
        ${R(330, 46, 230, 20, "#0a0f24", 'rx="3" stroke="#556" ')}${R(334, 49, 222, 14, "#4fb3a9", 'rx="2"')}${t(330, 40, "SO-DIMM slots (DDR4)")}
        <g ${hl("lram")}>${R(330, 74, 230, 20, "#0a0f24", 'rx="3" stroke="#556"')}${extra ? `<g class="hwb-in">${R(334, 77, extra === "dimm" ? 270 : 222, 14, extra === "ok" ? "#4fb3a9" : "#ff8fb1", 'rx="2"')}</g>` : ""}</g>
        <g ${hl("ldrive")}>${R(60, 228, 190, 18, "#0a0f24", 'rx="3" stroke="#556"')}${t(60, 222, "M.2 2280 slot · SATA only")}
          ${dr && dr.fit === "m2" ? `<g class="hwb-in">${R(64, 230, 170, 14, dr.proto === "nvme" ? "#8e94f2" : "#4fb3a9", 'rx="2"')}${t(150, 241, dr.proto === "nvme" ? "NVMe" : "SATA", 10, "#0b1129", "middle")}</g>` : ""}
          ${R(330, 120, 230, 140, "none", 'rx="8" stroke="#556" stroke-dasharray="6 5"')}${t(330, 114, "2.5″ SATA bay")}
          ${dr && dr.fit === "bay" ? `<g class="hwb-in">${R(340, 130, 210, 120, "#4fb3a9", 'rx="6" opacity=".85"')}${t(445, 196, "1 TB SSD", 14, "#0b1129", "middle")}</g>`
            : dr && dr.fit === "big" ? `<g class="hwb-in">${R(318, 110, 270, 170, "#ff8fb1", 'rx="6" opacity=".8"')}${t(453, 200, "3.5″ drive: too big", 14, "#0b1129", "middle")}</g>`
            : `${R(340, 130, 210, 120, "#5b6378", 'rx="6"')}${O(445, 190, 40, "#7f889e")}${t(445, 194, "old 500 GB HDD", 12, "#0b1129", "middle")}`}</g>
        ${R(60, 290, 500, 70, "#262b40", 'rx="10"')}${t(310, 330, "Battery", 13, "#a9b1d6", "middle")}
      </svg>`;
    }
    const bd = b.board, ff = bd ? bd.ff : "ATX";
    const bw = ff === "ITX" ? 230 : 310, bh = ff === "ITX" ? 240 : ff === "mATX" ? 320 : 370, bx = 20, by = 20;
    const dimms = ff === "ITX" ? 2 : 4, sx = bx + 60, sy = by + 50;
    const ramCol = b.ram ? (b.ram.ff !== "DIMM" ? "#ff8fb1" : b.ram.type === "DDR5" ? "#8e94f2" : "#4fb3a9") : "";
    const ramSlots = b.ram ? (b.ram.sticks === 1 ? [1] : dimms === 2 ? [0, 1] : [1, 3]) : [];
    const pcy = ff === "ITX" ? by + 200 : by + 230;
    let s = `<svg viewBox="0 0 620 410" role="img" aria-label="Motherboard with the chosen parts">`;
    s += `<g ${hl("board")}>${R(bx, by, bw, bh, bd ? "#14382f" : "none", `rx="8" stroke="${bd ? "#2f6b5a" : "#556"}" stroke-width="2" ${bd ? "" : 'stroke-dasharray="8 6"'}`)}</g>`;
    s += bd ? t(bx + 10, by + bh - 10, `${bd.name} · ${bd.socket} · ${bd.ram}`, 11, "#9fd6c4") : t(bx + bw / 2, by + bh / 2, "Pick a motherboard", 14, "#a9b1d6", "middle");
    s += R(bx + 18, by + 10, 34, 14, "#0b0f22", 'rx="2"') + t(bx + 56, by + 21, "CPU power", 9);
    s += R(bx + bw - 20, by + 40, 12, 74, "#0b0f22", 'rx="2"') + t(bx + bw - 24, by + 34, "24-pin", 9, "#a9b1d6", "end");
    s += `<g ${hl("cpu")}>${R(sx, sy, 84, 84, "#2a2f45", 'rx="4" stroke="#7f889e"')}${b.cpu ? `<g class="hwb-in">${R(sx + 8, sy + 8, 68, 68, "#c9d0dd", 'rx="4"')}${t(sx + 42, sy + 40, b.cpu.name.replace(" CPU", ""), 11, "#0b1129", "middle")}${t(sx + 42, sy + 55, b.cpu.socket, 10, "#0b1129", "middle")}</g>` : t(sx + 42, sy + 46, "socket", 10, "#7f889e", "middle")}</g>`;
    s += `<g ${hl("ram")}>${range(dimms).map((i) => R(sx + 104 + i * 16, sy - 20, 9, 140, "#0b0f22", 'rx="2"') + (ramSlots.includes(i) ? `<g class="hwb-in">${R(sx + 103 + i * 16, sy - 18, 11, 136, ramCol, 'rx="2"')}</g>` : "")).join("")}
      ${b.ram ? t(sx + 104, sy + 136, b.ram.name, 10) : ""}</g>`;
    s += `<g ${hl("boot")}>${R(bx + 40, sy + 120, 120, 12, "#0b0f22", 'rx="2"')}${t(bx + 40, sy + 114, "M.2", 9)}${b.boot && b.boot.iface === "nvme" ? `<g class="hwb-in">${R(bx + 42, sy + 121, 112, 10, "#8e94f2", 'rx="2"')}</g>` : ""}</g>`;
    s += `<g ${hl("gpu")}>${R(bx + 24, pcy, ff === "ITX" ? 180 : 230, 9, "#0b0f22", 'rx="2"')}${t(bx + 24, pcy - 4, "PCIe x16", 9)}
      ${b.gpu && b.gpu.tier ? `<g class="hwb-in">${R(bx + 14, pcy - 6, b.gpu.tier === 3 ? 300 : b.gpu.tier === 2 ? 260 : 170, 44, "#30365a", 'rx="6" stroke="#8e94f2"')}${range(b.gpu.tier === 1 ? 1 : 2).map((i) => O(bx + 60 + i * 90, pcy + 16, 17, "#1a1f38", 'stroke="#7f889e"')).join("")}${t(bx + 22 + (b.gpu.tier === 1 ? 70 : 190), pcy + 32, b.gpu.name, 10, "#c5c9ff")}</g>` : ""}</g>`;
    const nsata = bd ? bd.sata : 4;
    s += `<g ${hl("data")}>${range(nsata).map((i) => R(bx + bw - 30 - (i % 2) * 18, by + bh - 60 - Math.floor(i / 2) * 14, 14, 10, "#0b0f22", 'rx="1"')).join("")}</g>`;
    // right side: drives and PSU
    const dx = 380;
    s += t(dx, 40, "Drives", 12, "#eef1ff");
    const drives = [];
    if (b.boot && b.boot.iface === "sata") drives.push([b.boot.ssd ? "#4fb3a9" : "#7f889e", b.boot.ssd ? "SATA SSD (boot)" : "HDD (boot)"]);
    if (b.data) range(b.data.n).forEach((i) => drives.push(["#7f889e", `4 TB HDD ${i + 1}`]));
    s += `<g ${hl("boot")}>${b.boot && b.boot.none ? t(dx, 62, "no boot drive", 11, "#ff9b9b") : b.boot && b.boot.iface === "nvme" ? t(dx, 62, `${b.boot.name} on the M.2 slot`, 11, "#c5c9ff") : ""}</g>`;
    s += `<g ${bad === "data" || bad === "raid" ? hl(bad) : ""}>${drives.map(([c, l], i) => `<g class="hwb-in">${R(dx + (i % 2) * 112, 74 + Math.floor(i / 2) * 42, 104, 34, c, 'rx="5" opacity=".9"')}${t(dx + 8 + (i % 2) * 112, 95 + Math.floor(i / 2) * 42, l, 10, "#0b1129")}</g>`).join("")}
      ${b.raid && b.raid.lvl != null ? t(dx, 74 + Math.ceil(drives.length / 2) * 42 + 12, `Array: RAID ${b.raid.lvl}`, 12, "#ffcf7a") : ""}</g>`;
    s += `<g ${hl("psu")}>${R(dx, 300, 210, 90, b.psu ? "#262b40" : "none", 'rx="8" stroke="#7f889e" stroke-width="1.5"')}${b.psu ? `${O(dx + 50, 345, 30, "#1a1f38", 'stroke="#7f889e"')}${t(dx + 140, 342, b.psu.name.replace(" PSU", ""), 20, "#ffcf7a", "middle")}${t(dx + 140, 362, "power supply", 10, "#a9b1d6", "middle")}` : t(dx + 105, 350, "Pick a power supply", 12, "#a9b1d6", "middle")}</g>`;
    if (b.psu && bd) s += `<path d="M${dx} 330 C ${dx - 40} 330, ${bx + bw + 30} 80, ${bx + bw - 8} 80" stroke="#ffcf7a" stroke-width="2" fill="none" stroke-dasharray="5 5" opacity=".6"/>`;
    return s + `</svg>`;
  }
  function buildRender() {
    const ctx = BS.ctx, job = JOBS[BS.job], solved = ctx.store.get("solved", {}), esc = ctx.esc;
    const root = shell(ctx, "", `
      <div class="hw-top"><div><h2>🧩 Build to spec</h2><p class="muted">Read the job, pick one part per slot, then press Power on. The PC tells you what's wrong.</p></div>
        <div class="hw-stats"><span class="pill sun">${Object.keys(solved).length}/${JOBS.length} built</span></div></div>
      <div class="seg hw-seg">${JOBS.map((j, i) => `<button class="${i === BS.job ? "on" : ""}" data-j="${i}">${esc(j.title)}${solved[j.id] ? `<span class="ok">✓</span>` : ""}</button>`).join("")}</div>
      <div class="hw-grid">
        <div class="glass hw-card">
          <div class="hw-job"><span class="tag">THE JOB</span><h3>${esc(job.title)}</h3><p>${esc(job.text)}</p><ul>${job.needs.map((n) => `<li>${esc(n)}</li>`).join("")}</ul></div>
          ${job.slots.map((sl) => `<div class="hw-slot"><div class="hw-slabel">${SLOTNAME[sl]}</div><div class="hw-opts">${PARTS[sl].map((p) =>
            `<button class="hw-opt ${BS.pick[sl] === p.id ? (BS.bad === sl ? "on bad" : "on") : ""}" data-s="${sl}" data-v="${p.id}">${esc(p.name)}${p.d ? `<small>${esc(p.d)}</small>` : ""}</button>`).join("")}</div></div>`).join("")}
        </div>
        <div class="glass hw-card hw-sticky">
          <div class="hw-board">${boardSvg(job)}</div>
          <div class="hw-row"><button class="btn" id="hw-power">⏻ Power on <kbd>Enter</kbd></button><button class="btn ghost" id="hw-hintb">Hint <kbd>H</kbd></button><button class="btn ghost" id="hw-clear">Start over</button></div>
          ${BS.hint ? `<div class="hw-hint">💡 ${esc(job.hint)}</div>` : ""}
          <div class="hw-screen" id="hw-screen" aria-live="polite">${BS.lines ? "" : `<div class="m">Monitor off. Pick parts, then Power on.</div>`}</div>
        </div>
      </div>`);
    root.querySelectorAll(".hw-seg button").forEach((b) => b.onclick = () => buildJob(+b.dataset.j));
    root.querySelectorAll(".hw-opt").forEach((b) => b.onclick = () => { BS.pick[b.dataset.s] = b.dataset.v; BS.bad = null; BS.lines = null; buildRender(); });
    root.querySelector("#hw-power").onclick = buildPower;
    root.querySelector("#hw-hintb").onclick = () => { BS.hint = true; buildRender(); };
    root.querySelector("#hw-clear").onclick = () => { BS.pick = {}; BS.bad = null; BS.lines = null; buildRender(); };
    if (BS.lines) printLines(BS.lines, true);
  }
  function printLines(lines, instant) {
    const scr = BS.ctx.el.querySelector("#hw-screen"); if (!scr) return;
    BS.timers.forEach(clearTimeout); BS.timers = [];
    scr.innerHTML = "";
    const add = ([c, s]) => { const d = document.createElement("div"); d.className = c; d.textContent = s; scr.appendChild(d); };
    if (instant || BS.ctx.calm) { lines.forEach(add); return; }
    lines.forEach((l, i) => BS.timers.push(setTimeout(() => { if (live(BS.ctx)) add(l); }, i * 260)));
  }
  function buildPower() {
    const ctx = BS.ctx, job = JOBS[BS.job], r = post(job);
    BS.bad = r.bad || null;
    let lines = r.L;
    if (r.ok) {
      const spec = job.spec(r.b);
      const allOk = spec.every((x) => x[0]);
      lines = lines.concat(spec.length ? [["m", "— Does it fit the job? —"]] : [], spec.map(([ok, good, bad]) => [ok ? "g" : "r", ok ? `✓ ${good}` : `✗ ${bad}`]));
      lines.push(allOk ? ["y", "★ Built to spec. Job done."] : ["y", "It boots, but it doesn't fit the job yet."]);
      if (allOk) {
        const solved = ctx.store.get("solved", {});
        if (!solved[job.id]) { solved[job.id] = 1; ctx.store.set("solved", solved); }
        later(ctx, lines.length * 260, () => ctx.toast(`${job.title}: built to spec`));
      }
    }
    BS.lines = lines;
    buildRender();
    printLines(lines, false);
  }
  function buildJob(i) { BS.job = i; BS.pick = {}; BS.bad = null; BS.lines = null; BS.hint = false; buildRender(); BS.ctx.report(`Build to spec: ${JOBS[i].title}`); }

  Lab.register({
    id: "hw-build", title: "Build to spec", icon: "🧩", exams: ["a1"],
    blurb: "Five real jobs, from a video workstation to a laptop upgrade. Match socket, RAM type, drives and watts, then power it on.",
    progress: (ctx) => { const n = Object.keys(ctx.store.get("solved", {})).length; return n ? `${n}/${JOBS.length} built` : ""; },
    render(ctx) { addCss(); BS.ctx = ctx; const solved = ctx.store.get("solved", {}); const first = JOBS.findIndex((j) => !solved[j.id]); buildJob(first < 0 ? 0 : first); },
    key(e) {
      if (!live(BS.ctx) || onButton(e)) return;
      if (e.key === "Enter") { e.preventDefault(); buildPower(); }
      else if (e.key.toLowerCase() === "h") { BS.hint = true; buildRender(); }
    },
    remote(cmd) { if (!live(BS.ctx)) return; if (cmd === "show") { BS.hint = true; buildRender(); } else if (cmd === "grade") buildPower(); },
  });

  // ---------- 3. RAID chooser ----------
  const RSC = [
    { t: "Video scratch disk", d: "An editor wants the fastest possible scratch space from 2 × 1 TB drives. Everything on it is copied elsewhere, so losing it is fine.", n: 2, s: 1, opts: [0, 1, 5, 10], a: 0, fail: [0],
      why: "RAID 0 stripes the data across both drives: faster, and all 2 TB usable. There's no redundancy, so one dead drive loses everything, which is fine here." },
    { t: "The boss's PC", d: "The boss's desktop has 2 × 4 TB drives. It must keep working if one drive dies.", n: 2, s: 4, opts: [0, 1, 5, 6], a: 1, fail: [1],
      why: "RAID 1 mirrors: both drives hold the same data, so either one can die. Usable space is one drive's worth, 4 TB. RAID 5 and 6 need more drives." },
    { t: "Office file server", d: "A file server with 4 × 2 TB drives. It must survive one drive failing and keep as much space as possible.", n: 4, s: 2, opts: [0, 1, 5, 10], a: 5, fail: [2],
      why: "RAID 5 stripes data with parity spread over all drives. One drive's worth goes to parity, so (4 − 1) × 2 = 6 TB usable, and any one drive can fail. RAID 10 also survives but leaves only 4 TB." },
    { t: "Only three drives", d: "A small NAS has 3 × 4 TB drives and must survive a single drive failure.", n: 3, s: 4, opts: [0, 5, 6, 10], a: 5, fail: [0],
      why: "Only RAID 5 works: it needs at least 3 drives and gives (3 − 1) × 4 = 8 TB. RAID 6 and RAID 10 both need at least 4 drives." },
    { t: "Archive of big drives", d: "6 × 8 TB drives. Rebuilding a drive this big takes many hours, so it must survive two drives failing at once.", n: 6, s: 8, opts: [1, 5, 6, 10], a: 6, fail: [1, 4],
      why: "RAID 6 keeps two sets of parity, so any two drives can fail. Usable: (6 − 2) × 8 = 32 TB. RAID 5 survives only one; RAID 10 survives two only if they're in different mirror pairs." },
    { t: "Busy database", d: "A database server with 4 × 1 TB SSDs needs fast writes and must survive a drive failure.", n: 4, s: 1, opts: [0, 5, 6, 10], a: 10, fail: [3],
      why: "RAID 10 is a stripe of mirrors: fast like RAID 0, safe like RAID 1, with no parity to work out on every write. Usable: half, so 2 TB. It needs at least 4 drives." },
  ];
  const RS = { ctx: null, gen: 0, i: 0, picked: null, wrong: new Set(), view: null, dead: new Set(), hint: false };
  function blocks(l, n) {
    // 3 stripes (A, B, C). d = data, p = parity, c = mirror copy.
    return range(n).map((disk) => range(3).map((r) => {
      const L = "ABC"[r];
      if (l === 0) return ["d", `${L}${disk + 1}`];
      if (l === 1) return [disk ? "c" : "d", L];
      if (l === 10) return [disk % 2 ? "c" : "d", `${L}${Math.floor(disk / 2) + 1}`];
      const p = (n - 1 - r + n * 3) % n, q = (p + 1) % n;
      if (disk === p) return ["p", `${L}p`];
      if (l === 6 && disk === q) return ["p", `${L}q`];
      const dataDisks = range(n).filter((x) => x !== p && !(l === 6 && x === q));
      return ["d", `${L}${dataDisks.indexOf(disk) + 1}`];
    }));
  }
  function raidDiagram(l, sc) {
    const esc = RS.ctx.esc, bl = blocks(l, sc.n);
    const disk = (i) => `<button class="hw-disk ${RS.dead.has(i) ? "dead" : ""}" data-d="${i}" aria-label="Disk ${i + 1}${RS.dead.has(i) ? ", failed" : ""}"><div class="dl">Disk ${i + 1} · ${sc.s} TB</div>${bl[i].map(([c, s]) => `<i class="${c}">${s}</i>`).join("")}</button>`;
    const disks = l === 10 ? range(sc.n / 2).map((p) => `<div class="hw-pair">${disk(2 * p)}${disk(2 * p + 1)}</div>`).join("") : range(sc.n).map(disk).join("");
    const alive = raidAlive(l, sc.n, RS.dead), f = RS.dead.size;
    return `<div class="hw-disks">${disks}</div>
      <div class="hw-legend"><span style="--c:rgba(142,148,242,.6)">data</span>${l === 5 || l === 6 ? `<span style="--c:rgba(255,207,122,.6)">parity</span>` : ""}${l === 1 || l === 10 ? `<span style="--c:rgba(79,179,169,.6)">mirror copy</span>` : ""}<span>Click a disk to fail or replace it</span></div>
      <div class="hw-verdict ${alive ? "ok" : "lost"}">${!f ? "All drives healthy" : alive ? `✓ ${f} drive${f > 1 ? "s" : ""} down, data survives (degraded: replace and rebuild)` : `✗ ${f} drive${f > 1 ? "s" : ""} down: the array is lost, restore from backup`}</div>
      <div class="hw-math">RAID ${l}: ${esc(RAID[l].math(sc.n, sc.s))} usable · survives ${esc(RAID[l].survives)}</div>`;
  }
  function raidRender(animate) {
    const ctx = RS.ctx, sc = RSC[RS.i], esc = ctx.esc, done = RS.picked != null, right = ctx.store.get("right", {});
    const valid = [0, 1, 5, 6, 10].filter((l) => !raidFits(l, sc.n));
    const root = shell(ctx, "", `
      <div class="hw-top"><div><h2>💽 RAID chooser</h2><p class="muted">Pick the RAID level for the job, then watch a drive fail.</p></div>
        <div class="hw-stats"><span class="pill sun">${Object.keys(right).length}/${RSC.length} right first time</span><span class="pill">Scenario ${RS.i + 1} of ${RSC.length}</span></div></div>
      <div class="hw-grid">
        <div class="glass hw-card">
          <div class="hw-job"><span class="tag">THE JOB</span><h3>${esc(sc.t)}</h3><p>${esc(sc.d)}</p></div>
          ${sc.opts.map((l, k) => `<button class="ch ${RS.wrong.has(k) ? "wrong" : ""} ${done && l === sc.a ? "right" : ""}" data-k="${k}" ${done ? "disabled" : ""}><kbd>${k + 1}</kbd><span><b>RAID ${l}</b> <span class="muted">· ${RAID[l].kind}, min ${RAID[l].min} drives</span></span></button>`).join("")}
          ${RS.hint && !done ? `<div class="hw-hint">💡 Count the drives first: RAID 5 needs 3+, RAID 6 and 10 need 4+. Then: does it need to survive one failure, two, or none?</div>` : ""}
          ${done ? `<div class="why hw-why">${RS.wrong.size ? "" : `<span class="stamp">RIGHT</span>`}<h5>RAID ${sc.a}</h5>${esc(sc.why)}</div>
            <div class="hw-row"><button class="btn" id="hw-next">${RS.i + 1 < RSC.length ? "Next scenario" : "Start again"} <kbd>Enter</kbd></button></div>`
            : `<div class="hw-row"><button class="btn ghost" id="hw-hintb">Hint <kbd>H</kbd></button><span class="tag">Keys 1–4 · or your phone's grade buttons</span></div>`}
        </div>
        <div class="glass hw-card">
          ${done ? `<div class="seg" style="width:fit-content">${valid.map((l) => `<button class="${l === RS.view ? "on" : ""}" data-v="${l}">RAID ${l}</button>`).join("")}</div>
            ${raidDiagram(RS.view, sc)}`
            : `<div class="hw-disks">${range(sc.n).map((i) => `<div class="hw-disk" style="cursor:default"><div class="dl">Disk ${i + 1} · ${sc.s} TB</div><i class="d">?</i><i class="d">?</i><i class="d">?</i></div>`).join("")}</div>
               <p class="muted" style="text-align:center">${sc.n} × ${sc.s} TB drives. Choose a level to see how the data is laid out.</p>`}
        </div>
      </div>`);
    root.querySelectorAll(".ch").forEach((b) => b.onclick = () => raidPick(+b.dataset.k));
    root.querySelectorAll(".seg button[data-v]").forEach((b) => b.onclick = () => { RS.gen++; RS.view = +b.dataset.v; RS.dead = new Set(); raidRender(false); });
    root.querySelectorAll(".hw-disk[data-d]").forEach((b) => b.onclick = () => { RS.gen++; const d = +b.dataset.d; RS.dead.has(d) ? RS.dead.delete(d) : RS.dead.add(d); raidRender(false); });
    const nx = root.querySelector("#hw-next"); if (nx) { nx.onclick = raidNext; nx.focus({ preventScroll: true }); }
    const hb = root.querySelector("#hw-hintb"); if (hb) hb.onclick = () => { RS.hint = true; raidRender(false); };
    if (animate) {
      // Fail the scenario's drives one at a time so you can watch the verdict change.
      const gen = ++RS.gen;   // a newer scenario or a click cancels these
      sc.fail.forEach((d, i) => later(ctx, 700 + i * 900, () => { if (gen === RS.gen && RS.picked != null) { RS.dead.add(d); raidRender(false); } }));
    }
  }
  function raidPick(k) {
    const ctx = RS.ctx, sc = RSC[RS.i], l = sc.opts[k];
    if (l == null || RS.picked != null) return;
    if (l !== sc.a) {
      RS.wrong.add(k);
      const why = raidFits(l, sc.n);
      ctx.toast(why || `RAID ${l} doesn't fit this job: try again`, true);
      raidRender(false); return;
    }
    RS.picked = l; RS.view = l; RS.dead = new Set();
    if (!RS.wrong.size) { const r = ctx.store.get("right", {}); r[RS.i] = 1; ctx.store.set("right", r); }
    raidRender(true);
  }
  function raidNext() { RS.gen++; RS.i = (RS.i + 1) % RSC.length; RS.picked = null; RS.wrong = new Set(); RS.dead = new Set(); RS.hint = false; raidRender(false); RS.ctx.report(`RAID chooser: ${RSC[RS.i].t}`); }

  Lab.register({
    id: "hw-raid", title: "RAID chooser", icon: "💽", exams: ["a1"],
    blurb: "Six jobs, five RAID levels. Pick one, then fail drives and see if the data lives. Capacity math included.",
    progress: (ctx) => { const n = Object.keys(ctx.store.get("right", {})).length; return n ? `${n}/${RSC.length} right` : ""; },
    render(ctx) { addCss(); RS.ctx = ctx; RS.gen++; RS.picked = null; RS.wrong = new Set(); RS.dead = new Set(); RS.hint = false; raidRender(false); ctx.report(`RAID chooser: ${RSC[RS.i].t}`); },
    key(e) {
      if (!live(RS.ctx) || onButton(e)) return;
      if (e.key && "1234".includes(e.key)) { e.preventDefault(); raidPick(+e.key - 1); }
      else if (e.key === "Enter" && RS.picked != null) { e.preventDefault(); raidNext(); }
      else if (e.key.toLowerCase() === "h") { RS.hint = true; raidRender(false); }
    },
    remote(cmd, v) {
      if (!live(RS.ctx)) return;
      if (cmd === "grade") { if (RS.picked != null) raidNext(); else raidPick(v - 1); }
      else if (cmd === "show") { RS.hint = true; raidRender(false); }
    },
  });

  // ---------- 4. Put it in order ----------
  const SEQ = [
    { id: "laser", title: "Laser printer imaging process", icon: "🖨️", intro: "The seven steps a laser printer goes through for every page.",
      steps: [
        ["Processing", "The printer builds the whole page as an image in its memory (a raster image)."],
        ["Charging", "The primary charge roller (or corona wire) puts an even negative charge on the imaging drum."],
        ["Exposing", "The laser draws the page on the drum, removing the charge wherever toner should go."],
        ["Developing", "Charged toner jumps from the developer roller onto those discharged spots on the drum."],
        ["Transferring", "The transfer roller charges the paper so it pulls the toner off the drum; a static eliminator stops the page clinging."],
        ["Fusing", "Hot rollers melt and press the toner into the paper. That's why pages come out warm."],
        ["Cleaning", "A blade wipes leftover toner off the drum and its charge is erased, ready for the next page."],
      ] },
    { id: "laptop", title: "Swap a laptop's RAM or SSD safely", icon: "💻", intro: "The safe order for opening a laptop to replace memory or a drive.",
      steps: [
        ["Back up the data", "Before any storage work: if something goes wrong, nothing is lost (or clone the old drive)."],
        ["Shut it down fully", "A real shut down, not sleep: in sleep the RAM still has power."],
        ["Unplug the AC adapter", "No outside power while you work."],
        ["Remove the battery, if it's removable", "If it's built in, disconnect its cable as soon as the bottom panel is off."],
        ["Put on an ESD wrist strap", "Clip it to ground (or an ESD mat) so static from you can't damage the parts."],
        ["Remove the panel and document the screws", "Photograph or map them: laptop screws are often different lengths."],
        ["Swap the part", "RAM: open the clips, insert at an angle, press down until it clicks. M.2: insert at an angle and fit its screw."],
        ["Reassemble and check the BIOS/UEFI", "Make sure the new memory or drive shows up before you call it done."],
      ] },
  ];
  const OS = { ctx: null, s: 0, pool: [], slots: [], lock: [], tries: 0, done: false, dragFrom: null, miss: [] };
  function ordStart(i) {
    OS.s = i; const n = SEQ[i].steps.length;
    OS.pool = shuffle(range(n)); OS.slots = Array(n).fill(null); OS.lock = Array(n).fill(false); OS.tries = 0; OS.hints = 0; OS.done = false; OS.miss = [];
    ordRender(); OS.ctx.report(`Put it in order: ${SEQ[i].title}`);
  }
  function ordPlace(item, at) {
    // Take the item from wherever it is, then put it at slot `at` (or the first empty slot).
    const from = OS.slots.indexOf(item);
    if (from >= 0) OS.slots[from] = null; else OS.pool = OS.pool.filter((x) => x !== item);
    if (at == null) at = OS.slots.findIndex((x, k) => x == null && !OS.lock[k]);
    if (at < 0 || OS.lock[at]) { OS.pool.push(item); return; }
    const was = OS.slots[at];
    if (was != null) { if (from >= 0) OS.slots[from] = was; else OS.pool.push(was); }
    OS.slots[at] = item; OS.miss = [];
  }
  function ordUnplace(k) { if (OS.lock[k] || OS.slots[k] == null) return; OS.pool.push(OS.slots[k]); OS.slots[k] = null; OS.miss = []; }
  function ordCheck() {
    if (OS.done) return;
    if (OS.slots.some((x) => x == null)) { OS.ctx.toast("Fill every step first", true); return; }
    OS.tries++; OS.miss = [];
    OS.slots.forEach((x, k) => { if (x === k) OS.lock[k] = true; else { OS.miss.push(k); OS.pool.push(x); OS.slots[k] = null; } });
    if (!ordFinish()) OS.ctx.toast(`${OS.lock.filter(Boolean).length} of ${OS.lock.length} in the right place: the rest went back`);
    ordRender();
  }
  // Every slot locked: done. Each hint counts like a check, so "first try" means no help.
  function ordFinish() {
    if (!OS.lock.every(Boolean)) return false;
    OS.done = true; OS.tries = Math.max(1, OS.tries + OS.hints);
    const best = OS.ctx.store.get("best", {}), id = SEQ[OS.s].id;
    if (!best[id] || OS.tries < best[id]) { best[id] = OS.tries; OS.ctx.store.set("best", best); }
    OS.ctx.toast(OS.tries === 1 ? "Perfect order, first try!" : `In order after ${OS.tries} checks`);
    return true;
  }
  function ordHint() {
    if (OS.done) return;
    const k = OS.lock.findIndex((x) => !x); if (k < 0) return;
    ordPlace(k, k); OS.lock[k] = true; OS.hints++; ordFinish(); ordRender();
  }
  function drumSvg(steps) {
    // The drum with the stages around it, in the order they happen to one spot on the drum.
    const pts = steps.slice(1).map((st, i) => { const a = -Math.PI / 2 + i * (2 * Math.PI / 6); return [150 + 112 * Math.cos(a), 130 + 96 * Math.sin(a), st[0]]; });
    return `<svg viewBox="0 0 300 260" role="img" aria-label="Laser printer drum with the stages around it" style="max-width:340px;margin:8px auto 0;display:block">
      <circle cx="150" cy="130" r="56" fill="#1d2a5c" stroke="#8e94f2" stroke-width="2"/><text x="150" y="128" text-anchor="middle" fill="#c5c9ff" font-size="12" font-family="Segoe UI, sans-serif">imaging</text><text x="150" y="143" text-anchor="middle" fill="#c5c9ff" font-size="12" font-family="Segoe UI, sans-serif">drum</text>
      <path d="M150 64 A66 66 0 1 1 92 100" fill="none" stroke="#ffcf7a" stroke-width="1.5" stroke-dasharray="4 4" marker-end="url(#hw-arr)"/>
      <defs><marker id="hw-arr" viewBox="0 0 8 8" refX="4" refY="4" markerWidth="7" markerHeight="7" orient="auto"><path d="M0 0L8 4L0 8Z" fill="#ffcf7a"/></marker></defs>
      ${pts.map(([x, y, s], i) => `<g><circle cx="${x}" cy="${y}" r="13" fill="#ffcf7a"/><text x="${x}" y="${y + 4}" text-anchor="middle" font-size="11" font-weight="700" fill="#141a3a">${i + 2}</text>
        <text x="${x}" y="${y + (y < 130 ? -18 : 28)}" text-anchor="middle" font-size="11.5" fill="#eef1ff" font-family="Segoe UI, sans-serif">${s}</text></g>`).join("")}
    </svg><p class="muted" style="text-align:center;font-size:12.5px;margin:4px 0 0">Step 1, processing, happens in the printer's memory before the drum turns.</p>`;
  }
  function ordRender() {
    const ctx = OS.ctx, sq = SEQ[OS.s], esc = ctx.esc, best = ctx.store.get("best", {});
    const item = (x, where, k) => `<button class="hw-item" draggable="${OS.lock[k] && where === "slot" ? "false" : "true"}" data-i="${x}" data-w="${where}" ${where === "slot" ? `data-k="${k}"` : ""}>
      ${where === "pool" ? `<kbd>${OS.pool.indexOf(x) + 1}</kbd>` : `<span></span>`}<span>${esc(sq.steps[x][0])}</span></button>`;
    const root = shell(ctx, "", `
      <div class="hw-top"><div><h2>${sq.icon} Put it in order</h2><p class="muted">Drag steps into place, or click them in order (keys 1–9 pick from the list). Check when every slot is full.</p></div>
        <div class="hw-stats">${SEQ.map((x) => best[x.id] ? `<span class="pill">${esc(x.id === "laser" ? "Laser" : "Laptop")} ✓ ${best[x.id] === 1 ? "first try" : `${best[x.id]} checks`}</span>` : "").join("")}</div></div>
      <div class="seg hw-seg">${SEQ.map((x, i) => `<button class="${i === OS.s ? "on" : ""}" data-s="${i}">${x.icon} ${esc(x.title)}${best[x.id] ? `<span class="ok">✓</span>` : ""}</button>`).join("")}</div>
      ${OS.done ? `<div class="hw-grid"><div class="glass hw-card"><div class="why" style="margin-top:0"><span class="stamp">${OS.tries === 1 ? "PERFECT" : "IN ORDER"}</span><h5>${esc(sq.title)}</h5>
          <ol class="hw-steps">${sq.steps.map(([a, b], i) => `<li style="animation-delay:${ctx.calm ? 0 : i * 70}ms"><div><b>${esc(a)}</b><br><span>${esc(b)}</span></div></li>`).join("")}</ol></div>
          <div class="hw-row"><button class="btn" id="hw-again">Shuffle and go again <kbd>Enter</kbd></button></div></div>
          <div class="glass hw-card">${sq.id === "laser" ? `<h4>Around the drum</h4>${drumSvg(sq.steps)}` : `<h4>Why this order</h4><p class="muted" style="margin:0">Power off and unplug first so nothing is live. The battery goes before your hands go in. The ESD strap goes on before you touch any board, and documenting screws before you lose one. Check the BIOS/UEFI last, so you know the part really works.</p>`}</div></div>`
      : `<div class="glass hw-card"><p style="margin:0 0 12px">${esc(sq.intro)}</p><div class="hw-cols">
          <div><h4>Steps (shuffled)</h4><div class="hw-pool" id="hw-pool">${OS.pool.map((x) => item(x, "pool")).join("") || `<p class="muted">All placed. Press Check.</p>`}</div></div>
          <div><h4>Your order</h4><div class="hw-slots">${OS.slots.map((x, k) => `<div class="hw-slotrow ${OS.lock[k] ? "lock" : ""}"><span class="n">${k + 1}</span><div class="hw-drop ${OS.lock[k] ? "lock" : ""} ${OS.miss.includes(k) ? "miss" : ""}" data-k="${k}">${x != null ? item(x, "slot", k) : ""}</div></div>`).join("")}</div>
            <div class="hw-row"><button class="btn" id="hw-check">Check <kbd>Enter</kbd></button><button class="btn ghost" id="hw-hintb">Place one <kbd>H</kbd></button><button class="btn ghost" id="hw-undo">Undo <kbd>Backspace</kbd></button></div></div>
        </div></div>`}`);
    root.querySelectorAll(".hw-seg button").forEach((b) => b.onclick = () => ordStart(+b.dataset.s));
    const again = root.querySelector("#hw-again"); if (again) { again.onclick = () => ordStart(OS.s); again.focus({ preventScroll: true }); return; }
    root.querySelector("#hw-check").onclick = ordCheck;
    root.querySelector("#hw-hintb").onclick = ordHint;
    root.querySelector("#hw-undo").onclick = ordUndo;
    root.querySelectorAll(".hw-item").forEach((b) => {
      const x = +b.dataset.i;
      b.onclick = () => { if (b.dataset.w === "pool") ordPlace(x, null); else ordUnplace(+b.dataset.k); ordRender(); };
      b.ondragstart = (e) => { OS.dragFrom = x; b.classList.add("drag"); try { e.dataTransfer.setData("text/plain", String(x)); e.dataTransfer.effectAllowed = "move"; } catch {} };
      b.ondragend = () => b.classList.remove("drag");
    });
    root.querySelectorAll(".hw-drop").forEach((d) => {
      const k = +d.dataset.k; if (OS.lock[k]) return;
      d.ondragover = (e) => { e.preventDefault(); d.classList.add("over"); };
      d.ondragleave = () => d.classList.remove("over");
      d.ondrop = (e) => { e.preventDefault(); if (OS.dragFrom != null) { ordPlace(OS.dragFrom, k); OS.dragFrom = null; ordRender(); } };
    });
    const pool = root.querySelector("#hw-pool");
    if (pool) { pool.ondragover = (e) => e.preventDefault(); pool.ondrop = (e) => { e.preventDefault(); const k = OS.slots.indexOf(OS.dragFrom); if (k >= 0) { ordUnplace(k); ordRender(); } OS.dragFrom = null; }; }
  }
  function ordUndo() { for (let k = OS.slots.length - 1; k >= 0; k--) if (OS.slots[k] != null && !OS.lock[k]) { ordUnplace(k); break; } ordRender(); }

  Lab.register({
    id: "hw-order", title: "Put it in order", icon: "🖨️", exams: ["a1"],
    blurb: "The 7-step laser printer process and the safe way to swap a laptop's RAM or SSD. Drag or click the steps into order.",
    progress: (ctx) => { const n = Object.keys(ctx.store.get("best", {})).length; return n ? `${n}/${SEQ.length} in order` : ""; },
    render(ctx) { addCss(); OS.ctx = ctx; ordStart(OS.s || 0); },
    key(e) {
      if (!live(OS.ctx) || onButton(e)) return;
      if (OS.done) { if (e.key === "Enter") { e.preventDefault(); ordStart(OS.s); } return; }
      if (/^[1-9]$/.test(e.key)) { const x = OS.pool[+e.key - 1]; if (x != null) { e.preventDefault(); ordPlace(x, null); ordRender(); } }
      else if (e.key === "Enter") { e.preventDefault(); ordCheck(); }
      else if (e.key === "Backspace") { e.preventDefault(); ordUndo(); }
      else if (e.key.toLowerCase() === "h") ordHint();
    },
    remote(cmd, v) {
      if (!live(OS.ctx)) return;
      if (cmd === "show") ordHint();
      else if (cmd === "grade") {
        if (OS.done) ordStart(OS.s);
        else if (!OS.pool.length) ordCheck();
        else { const x = OS.pool[v - 1]; if (x != null) { ordPlace(x, null); ordRender(); } }
      }
    },
  });
})();
