// IT Lab plug-ins: Windows tools (A+ Core 2).
// Two labs: "Right tool for the job" (pick the Windows tool for a real request, then do the one action
// in a small pretend window drawn with CSS) and "NTFS vs share permissions" (work out effective access).
// Nothing here loads from the internet and nothing touches a real computer.
(() => {
  "use strict";
  if (!window.Lab) return;

  const CSS = `
  .win-calm *, .win-calm *::before, .win-calm *::after { animation: none !important; transition: none !important; }
  .win-top { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 14px; }
  .win-top h2 { margin: 0; font-size: 22px; }
  .win-top .win-prog { margin-left: auto; display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--muted); }
  .win-bar { width: 140px; height: 6px; border-radius: 4px; background: rgba(255,255,255,.07); overflow: hidden; }
  .win-bar i { display: block; height: 100%; background: linear-gradient(90deg, var(--jade), #7ed6c9); transition: width .5s; }
  .win-dots { display: flex; gap: 5px; flex-wrap: wrap; margin-bottom: 14px; }
  .win-dot { width: 30px; height: 30px; border-radius: 10px; border: 1px solid var(--line); background: rgba(255,255,255,.04); color: var(--muted);
             font: 600 12px ui-monospace, monospace; cursor: pointer; transition: background .15s, border-color .2s; }
  .win-dot:hover { background: rgba(255,255,255,.1); }
  .win-dot.on { border-color: rgba(255,207,122,.6); color: var(--ink); background: rgba(255,207,122,.1); }
  .win-dot.ok { color: #0d2a27; background: var(--jade); border-color: transparent; }
  .win-dot.ok.on { box-shadow: 0 0 0 2px var(--sun); }
  .win-grid { display: grid; grid-template-columns: minmax(0, 420px) minmax(0, 1fr); gap: 18px; align-items: start; }
  @media (max-width: 980px) { .win-grid { grid-template-columns: 1fr; } }
  .win-ticket { padding: 18px 20px; }
  .win-ticket .win-tno { font: 600 12px ui-monospace, monospace; color: var(--sun); letter-spacing: 1px; text-transform: uppercase; }
  .win-ticket .win-ask { font-size: 19px; font-weight: 600; line-height: 1.4; margin: 6px 0 4px; }
  .win-choices { display: grid; gap: 8px; margin-top: 14px; }
  .win-ch { display: grid; grid-template-columns: auto 1fr; gap: 2px 10px; align-items: center; text-align: left; padding: 10px 12px; border-radius: 14px;
            border: 1px solid var(--line); background: rgba(255,255,255,.04); color: var(--ink); cursor: pointer; transition: background .15s, border-color .2s, transform .12s; }
  .win-ch:hover { background: rgba(255,255,255,.09); transform: translateY(-1px); }
  .win-ch kbd { grid-row: span 2; }
  .win-ch b { font-weight: 600; } .win-ch code { font: 12px ui-monospace, monospace; color: var(--muted); }
  .win-ch .win-does { grid-column: 2; font-size: 13px; color: var(--coral); margin-top: 4px; }
  .win-ch.no { border-color: rgba(255,107,107,.45); background: rgba(255,107,107,.07); cursor: default; }
  .win-ch.yes { border-color: rgba(79,179,169,.7); background: rgba(79,179,169,.14); }
  .win-ch.dim { opacity: .4; }
  .win-ch[disabled] { cursor: default; transform: none; }
  .win-hint { margin-top: 12px; padding: 10px 12px; border-radius: 12px; background: rgba(142,148,242,.12); color: #c5c9ff; font-size: 14px; animation: win-rise .35s both; }
  .win-why { margin-top: 14px; padding: 14px 16px; border-radius: 16px; background: rgba(79,179,169,.1); border: 1px solid rgba(79,179,169,.3); animation: win-rise .4s both; }
  .win-why h4 { margin: 0 0 6px; color: var(--jade); font-size: 15px; }
  .win-why p { margin: 6px 0; font-size: 14.5px; }
  .win-why .win-tip { color: var(--sun); }
  .win-next { display: flex; gap: 8px; margin-top: 12px; align-items: center; }
  @keyframes win-rise { from { opacity: 0; transform: translateY(8px); } }
  @keyframes win-shake { 20%, 60% { transform: translateX(-5px); } 40%, 80% { transform: translateX(5px); } }
  .win-shake { animation: win-shake .35s; }

  /* The pretend Windows window */
  .win-w { border-radius: 10px; overflow: hidden; background: #1f1f1f; color: #e6e6e6; border: 1px solid #3a3a3a; box-shadow: 0 30px 70px -30px #000d;
           font: 13px/1.45 "Segoe UI Variable Text", "Segoe UI", system-ui, sans-serif; animation: win-rise .4s both; }
  .win-w.wait { opacity: .35; filter: grayscale(.6); }
  .win-tb { display: flex; align-items: center; gap: 8px; padding: 7px 0 7px 12px; background: #2b2b2b; font-size: 12.5px; }
  .win-tb .win-ctl { margin-left: auto; display: flex; }
  .win-tb .win-ctl span { width: 40px; text-align: center; color: #bbb; }
  .win-menu { display: flex; gap: 16px; padding: 4px 12px; background: #262626; border-bottom: 1px solid #333; font-size: 12.5px; color: #cfcfcf; }
  .win-body { display: grid; grid-template-columns: 1fr; min-height: 190px; }
  .win-body.two { grid-template-columns: 210px 1fr; }
  .win-tree { background: #1a1a1a; border-right: 1px solid #333; padding: 6px 0; font-size: 12.5px; }
  .win-tree div { padding: 2px 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .win-tree div.on { background: #0f4a7a; color: #fff; }
  .win-main { overflow: auto; }
  .win-t { width: 100%; border-collapse: collapse; font-size: 12.5px; }
  .win-t th { text-align: left; font-weight: 400; color: #bdbdbd; padding: 5px 10px; border-bottom: 1px solid #383838; border-right: 1px solid #2e2e2e; background: #232323; }
  .win-t td { padding: 4px 10px; border-bottom: 1px solid #262626; white-space: nowrap; }
  .win-t tr.on td { background: #0f4a7a; color: #fff; }
  .win-t tr.new td { animation: win-flash 1.4s; }
  @keyframes win-flash { from { background: rgba(79,179,169,.45); } }
  .win-warn { color: #f5c542; font-weight: 700; } .win-crit { color: #ff6b6b; font-weight: 700; } .win-okc { color: #6fd08c; }
  .win-act { border-top: 1px solid #333; background: #232323; padding: 10px 12px 12px; }
  .win-act .win-q { font-size: 13.5px; color: #fff; margin-bottom: 8px; }
  .win-act .win-q b { color: var(--sun); font-weight: 600; }
  .win-ctx { display: inline-flex; flex-direction: column; min-width: 280px; max-width: 100%; padding: 4px; border-radius: 8px; background: #2c2c2c; border: 1px solid #454545; box-shadow: 0 10px 30px -10px #000c; }
  .win-ctx button { display: flex; gap: 10px; align-items: center; text-align: left; padding: 6px 10px; border: 0; border-radius: 5px; background: none; color: #eee; cursor: pointer; font: inherit; font-size: 13px; }
  .win-ctx button:hover { background: #3d3d3d; }
  .win-ctx button kbd { color: #ddd; }
  .win-ctx button.x { color: #ff8f8f; text-decoration: line-through; cursor: default; }
  .win-ctx button.glow { box-shadow: inset 3px 0 0 var(--sun); }
  .win-say { margin-top: 8px; font-size: 13px; color: #ffb3b3; animation: win-rise .3s both; }
  .win-status { padding: 5px 12px; background: #1b1b1b; border-top: 1px solid #333; font-size: 12px; color: #a8a8a8; min-height: 26px; }
  .win-status.good { color: #8fe0ad; }
  .win-ph { padding: 40px 20px; text-align: center; color: var(--muted); border: 1px dashed rgba(255,255,255,.12); border-radius: 18px; }
  .win-ph .big { font-size: 40px; opacity: .7; }

  /* Permissions lab */
  .win-pgrid { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 18px; align-items: start; }
  @media (max-width: 980px) { .win-pgrid { grid-template-columns: 1fr; } }
  .win-who { display: flex; gap: 14px; align-items: center; padding: 16px 18px; flex-wrap: wrap; }
  .win-ava { width: 46px; height: 46px; border-radius: 50%; display: grid; place-items: center; font-weight: 700; font-size: 18px; color: #1a1f3d; background: linear-gradient(135deg, var(--sun), var(--rose)); }
  .win-groups { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 4px; }
  .win-g { font-size: 12px; padding: 2px 9px; border-radius: 999px; background: rgba(142,148,242,.16); color: #c5c9ff; }
  .win-where { margin-left: auto; padding: 8px 12px; border-radius: 12px; font-size: 13.5px; font-weight: 600; }
  .win-where.net { background: rgba(255,207,122,.14); color: var(--sun); }
  .win-where.loc { background: rgba(79,179,169,.14); color: var(--jade); }
  .win-acls { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 12px 0; }
  @media (max-width: 700px) { .win-acls { grid-template-columns: 1fr; } }
  .win-acl.off { opacity: .45; }
  .win-acl .win-t td:last-child { white-space: normal; }
  .win-deny { color: #ff8f8f; font-weight: 600; } .win-inh { color: #9a9a9a; font-size: 11.5px; }
  .win-mine td:first-child { color: #ffd98f; }
  .win-ans { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
  @media (max-width: 700px) { .win-ans { grid-template-columns: 1fr 1fr; } }
  .win-ans .btn { padding: 12px 8px; }
  .win-ans .btn.no { background: rgba(255,107,107,.25); color: var(--ink); }
  .win-ans .btn.yes { background: var(--jade); }
  .win-rules { padding: 16px 18px; font-size: 14px; }
  .win-rules h4 { margin: 0 0 8px; }
  .win-rules ol { margin: 0; padding-left: 20px; } .win-rules li { margin: 6px 0; padding: 2px 6px; border-radius: 8px; transition: background .3s; }
  .win-rules li.lit { background: rgba(255,207,122,.16); }
  .win-q2 { font-size: 18px; font-weight: 600; margin: 4px 0 12px; line-height: 1.4; }
  .win-steps { margin: 8px 0 0; padding-left: 18px; } .win-steps li { margin: 3px 0; }
  `;
  function addCss() {
    if (document.getElementById("win-css")) return;
    const s = document.createElement("style"); s.id = "win-css"; s.textContent = CSS; document.head.appendChild(s);
  }
  function shuffle(a) { a = a.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; }
  const live = (S) => S && S.ctx && S.ctx.el && S.ctx.el.isConnected;
  function plainKey(e) { return !(e.ctrlKey || e.metaKey || e.altKey); }
  function shake(S, node) { if (!S.ctx.calm && node) { node.classList.remove("win-shake"); void node.offsetWidth; node.classList.add("win-shake"); } }

  // ---------------------------------------------------------------- Right tool for the job
  // [name, how to open it, what it's for]
  const TOOLS = {
    devmgmt: ["Device Manager", "devmgmt.msc", "Every piece of hardware and its driver: update, roll back, disable or uninstall drivers."],
    eventvwr: ["Event Viewer", "eventvwr.msc", "Windows' diary: System, Application and Security logs of what happened and when."],
    diskmgmt: ["Disk Management", "diskmgmt.msc", "Initialize disks, create, extend or shrink volumes, format and change drive letters."],
    taskmgr: ["Task Manager", "taskmgr · Ctrl+Shift+Esc", "Running apps and processes, CPU/RAM/disk use, startup apps, ending a frozen app."],
    services: ["Services", "services.msc", "Background services: start, stop, startup type, and what to do when one fails."],
    taskschd: ["Task Scheduler", "taskschd.msc", "Runs a program or script automatically on a schedule or when something happens."],
    msconfig: ["System Configuration", "msconfig", "Boot options (Safe boot, boot log) and services. Its Startup tab now only points to Task Manager."],
    resmon: ["Resource Monitor", "resmon", "Deep live detail per process: files and handles it holds, network connections, disk activity."],
    perfmon: ["Performance Monitor", "perfmon", "Graphs and records performance counters over time (Data Collector Sets)."],
    regedit: ["Registry Editor", "regedit", "Edits Windows' settings database directly. Powerful and risky: export a backup first."],
    compmgmt: ["Computer Management", "compmgmt.msc", "One window with Event Viewer, Shared Folders, Local Users and Groups, Device Manager, Disk Management and Services. Can connect to another PC."],
    lusrmgr: ["Local Users and Groups", "lusrmgr.msc", "Local accounts: create, reset passwords, put them in groups like Administrators. Not in Home editions."],
    gpedit: ["Local Group Policy Editor", "gpedit.msc", "Policies for this PC: password rules, lockout, what users may change. Not in Home editions."],
    certmgr: ["Certificate Manager", "certmgr.msc", "The current user's certificates: Personal, Trusted Root Certification Authorities and more."],
    cleanmgr: ["Disk Cleanup", "cleanmgr", "Safely deletes temp files, the Recycle Bin and old Windows Update files."],
    dfrgui: ["Defragment and Optimize Drives", "dfrgui", "Defragments hard disk drives and sends TRIM to SSDs, now or on a schedule."],
    msinfo32: ["System Information", "msinfo32", "A read-only report: BIOS version, installed RAM, CPU, Secure Boot state, drivers."],
    ntfs: ["Folder Properties → Security tab", "NTFS permissions", "Who may read, change or fully control a file or folder, at the PC and over the network."],
  };

  // Each request: the tool it needs, four tools to pick from, a hint, and a small window where you do the one action.
  // mock.steps: ask, opts, ok (index of the right one), bad (why the others are wrong), after (what the window shows next).
  const SC = [
    { id: "scanner", ask: "A new USB scanner doesn't work. Windows shows a yellow ! next to it.", tool: "devmgmt",
      pick: ["devmgmt", "diskmgmt", "services", "msinfo32"], hint: "A yellow ! is a hardware-and-driver problem. Which tool lists every device?",
      mock: { title: "Device Manager", icon: "🖥️", menu: 1,
        rows: [["▸ 🔊 Audio inputs and outputs"], ["▸ 💽 Disk drives"], ["▸ 🖵 Display adapters"], ["▾ 📷 Imaging devices"], [`&nbsp;&nbsp;&nbsp;&nbsp;<span class="win-warn">⚠</span> USB Scanner`], ["▸ ⌨️ Keyboards"], ["▸ 🌐 Network adapters"]], sel: 4,
        steps: [{ ask: "Right-click <b>USB Scanner</b>. Which do you choose?", opts: ["Update driver", "Disable device", "Uninstall device", "Scan for hardware changes"], ok: 0,
          bad: ["", "Disabling just turns it off (it gets a down-arrow icon). It still won't scan.", "That's the plan B: if updating doesn't help, uninstall it, then scan for hardware changes to reinstall.", "The scanner is already found. It's missing a working driver, not being detected."],
          after: { rows: [["▸ 🔊 Audio inputs and outputs"], ["▸ 💽 Disk drives"], ["▸ 🖵 Display adapters"], ["▾ 📷 Imaging devices"], [`&nbsp;&nbsp;&nbsp;&nbsp;<span class="win-okc">✓</span> USB Scanner`], ["▸ ⌨️ Keyboards"], ["▸ 🌐 Network adapters"]], sel: 4, note: "Driver installed. The yellow ! is gone." } }] },
      why: "A yellow ! in Device Manager means Windows found the device but its driver is missing or not working. Updating the driver is the first fix.",
      tip: "Exam tip: a down arrow means the device is disabled. After a bad driver update, use Properties → Driver → Roll Back Driver." },

    { id: "restart", ask: "The PC randomly restarts at night. Find out why.", tool: "eventvwr",
      pick: ["eventvwr", "taskmgr", "msinfo32", "perfmon"], hint: "You need to look back at what happened at 3 am, not at what's happening now.",
      mock: { title: "Event Viewer", icon: "📔", menu: 1,
        tree: [[0, "Event Viewer (Local)"], [1, "Custom Views"], [1, "Windows Logs"], [2, "Application"], [2, "Security"], [2, "Setup"], [2, "System"], [2, "Forwarded Events"]],
        cols: ["Level", "Date and Time", "Source", "Event ID"], rows: [],
        steps: [
          { ask: "Which log records shutdowns, crashes and driver problems?", opts: ["Application", "Security", "System", "Setup"], ok: 2,
            bad: ["Application holds app errors (a program crashing), not power or kernel events.", "Security holds logons and audit events, like 4625 failed logons.", "", "Setup holds Windows installs and updates."],
            after: { tree: [[0, "Event Viewer (Local)"], [1, "Custom Views"], [1, "Windows Logs"], [2, "Application"], [2, "Security"], [2, "Setup"], [2, "System", 1], [2, "Forwarded Events"]],
              rows: [["ℹ Information", "3/14 7:02:10 AM", "Service Control Manager", "7036"], [`<span class="win-crit">⛔ Critical</span>`, "3/14 3:11:52 AM", "Kernel-Power", "41"], ["❗ Error", "3/14 3:11:58 AM", "EventLog", "6008"], ["ℹ Information", "3/13 9:40:03 PM", "Service Control Manager", "7036"], [`<span class="win-crit">⛔ Critical</span>`, "3/13 2:47:19 AM", "Kernel-Power", "41"], ["ℹ Information", "3/12 6:15:44 PM", "User32", "1074"]],
              note: "System log: 1,284 events. Too many to read: filter it." } },
          { ask: "<b>Filter Current Log…</b> Which Event ID means it restarted without shutting down cleanly?", opts: ["41 (Kernel-Power)", "1074 (User32)", "7036 (Service Control Manager)", "4625"], ok: 0,
            bad: ["", "1074 is a planned restart something asked for, like Windows Update. Worth checking, but it isn't a crash or power loss.", "7036 just says a service started or stopped. Normal noise.", "4625 is a failed logon, and it lives in the Security log."],
            after: { rows: [[`<span class="win-crit">⛔ Critical</span>`, "3/14 3:11:52 AM", "Kernel-Power", "41"], [`<span class="win-crit">⛔ Critical</span>`, "3/13 2:47:19 AM", "Kernel-Power", "41"], [`<span class="win-crit">⛔ Critical</span>`, "3/11 3:05:02 AM", "Kernel-Power", "41"]], fresh: 1,
              note: "Filtered: Event ID 41. Three nights, around 3 am." } }] },
      why: "Event Viewer keeps history. Kernel-Power 41 in the System log means Windows came back up without a clean shutdown: lost power, a crash, or a hang.",
      tip: "Next steps: check the power supply or UPS, sleep settings, and for blue-screen (BugCheck) events at the same times. Event 6008 also says the last shutdown was unexpected." },

    { id: "drive", ask: "Add a second drive to this PC and give it a letter.", tool: "diskmgmt",
      pick: ["diskmgmt", "devmgmt", "dfrgui", "cleanmgr"], hint: "Disks, partitions, formatting and letters all live in one tool.",
      mock: { title: "Disk Management", icon: "💽", menu: 1, cols: ["Disk", "Type", "Size", "Status", "Volume"],
        rows: [["Disk 0", "Basic", "476 GB", "Online", "(C:) NTFS · Healthy (Boot, Page File)"], ["Disk 1", "Unknown", "931 GB", `<span class="win-warn">Not Initialized</span>`, "931 GB Unallocated"]], sel: 1,
        steps: [
          { ask: "Disk 1 says <b>Not Initialized</b>. First step?", opts: ["Initialize Disk (GPT)", "New Simple Volume…", "Format…", "Change Drive Letter and Paths…"], ok: 0,
            bad: ["", "Windows can't make a volume on a disk that isn't initialized yet.", "There's no volume to format yet.", "There's no volume to give a letter to yet."],
            after: { rows: [["Disk 0", "Basic", "476 GB", "Online", "(C:) NTFS · Healthy (Boot, Page File)"], ["Disk 1", "Basic", "931 GB", "Online", "931 GB Unallocated"]], sel: 1, fresh: 1, note: "Disk 1 initialized as GPT." } },
          { ask: "Right-click the <b>Unallocated</b> space:", opts: ["New Simple Volume…", "Extend Volume…", "Delete Volume…", "Mark Partition as Active"], ok: 0,
            bad: ["", "Extend grows an existing volume into free space. There's no volume here yet.", "There's nothing to delete.", "That's for old MBR boot partitions, not a data drive."], after: { note: "New Simple Volume Wizard: size 953,867 MB." } },
          { ask: "File system for an internal Windows data drive?", opts: ["NTFS", "FAT32", "exFAT", "ext4"], ok: 0,
            bad: ["", "FAT32 can't hold a single file over 4 GB and has no permissions.", "exFAT is great for USB drives shared with Macs, but has no NTFS permissions or journaling.", "ext4 is a Linux file system. Disk Management can't format it."], after: { note: "Format: NTFS, label \"Data\", quick format." } },
          { ask: "Assign a drive letter:", opts: ["D:", "C:", "Don't assign a drive letter"], ok: 0,
            bad: ["", "C: is already Windows.", "It would work, but the drive wouldn't show up in File Explorer like the user asked."],
            after: { rows: [["Disk 0", "Basic", "476 GB", "Online", "(C:) NTFS · Healthy (Boot, Page File)"], ["Disk 1", "Basic", "931 GB", "Online", `<span class="win-okc">Data (D:) NTFS · Healthy (Basic Data Partition)</span>`]], sel: 1, fresh: 1, note: "D: is ready in File Explorer." } }] },
      why: "Disk Management does the whole job: initialize the new disk, create a volume, format it and give it a letter.",
      tip: "Exam tip: GPT is for UEFI and disks over 2 TB. MBR tops out at 2 TB and 4 primary partitions." },

    { id: "startup", ask: "The laptop takes forever to boot. Turn off the ChatLink app at startup (she still wants the app).", tool: "taskmgr",
      pick: ["taskmgr", "msconfig", "services", "regedit"], hint: "In Windows 10 and 11 the startup list moved. Where does msconfig's Startup tab send you now?",
      alt: { msconfig: "Close: in Windows 10/11 msconfig's Startup tab only says \"Open Task Manager\". Go straight there." },
      mock: { title: "Task Manager", icon: "📊",
        tree: [[0, "☰ Processes"], [0, "📈 Performance"], [0, "🕘 App history"], [0, "🚀 Startup apps"], [0, "👤 Users"], [0, "📋 Details"], [0, "⚙ Services"]],
        cols: ["Name", "Status", "Startup impact"], rows: [],
        steps: [
          { ask: "Which page lists what starts with Windows?", opts: ["Processes", "Performance", "Startup apps", "Services"], ok: 2,
            bad: ["Processes shows what's running now. Ending it here only lasts until the next boot.", "Performance shows graphs, not startup items.", "", "Services are background services, not apps that open at sign-in."],
            after: { tree: [[0, "☰ Processes"], [0, "📈 Performance"], [0, "🕘 App history"], [0, "🚀 Startup apps", 1], [0, "👤 Users"], [0, "📋 Details"], [0, "⚙ Services"]],
              rows: [["ChatLink", "Enabled", `<span class="win-crit">High</span>`], ["CloudSync", "Enabled", "Medium"], ["Audio Console", "Enabled", "Low"], ["Security notification icon", "Enabled", "Low"]], sel: 0, note: "4 startup apps · last BIOS time: 9.8 seconds" } },
          { ask: "<b>ChatLink</b> is selected. Click:", opts: ["Disable", "End task", "Open file location", "Properties"], ok: 0,
            bad: ["", "That closes it now, but it starts again next boot.", "That just shows where the program lives.", "Properties only shows file details."],
            after: { rows: [["ChatLink", `<span class="win-okc">Disabled</span>`, "None"], ["CloudSync", "Enabled", "Medium"], ["Audio Console", "Enabled", "Low"], ["Security notification icon", "Enabled", "Low"]], sel: 0, fresh: 1, note: "ChatLink won't start with Windows. The app still works when she opens it." } }] },
      why: "Task Manager's Startup apps page turns startup programs on or off without uninstalling them. \"Startup impact\" shows which ones slow the boot most.",
      tip: "Exam tip: in Windows 10/11, msconfig's Startup tab only links to Task Manager. Settings → Apps → Startup works too." },

    { id: "service", ask: "The Print Spooler service keeps stopping, so printing fails every few hours.", tool: "services",
      pick: ["services", "taskschd", "devmgmt", "lusrmgr"], hint: "It's a background service. Which tool manages those, including what happens when one fails?",
      mock: { title: "Services", icon: "⚙️", menu: 1, cols: ["Name", "Description", "Status", "Startup Type"],
        rows: [["Plug and Play", "Enables a computer to recognize…", "Running", "Manual"], ["Print Spooler", "This service spools print jobs…", "", "Automatic"], ["Remote Registry", "Enables remote users to modify…", "", "Disabled"], ["Windows Update", "Enables the detection, download…", "Running", "Manual (Trigger Start)"]], sel: 1,
        steps: [
          { ask: "Open <b>Print Spooler → Properties</b>. Which tab makes Windows restart it when it fails?", opts: ["General", "Log On", "Recovery", "Dependencies"], ok: 2,
            bad: ["General sets the startup type and Start/Stop, but not what happens after a crash.", "Log On sets which account the service runs as.", "", "Dependencies shows what it needs. Useful, but it doesn't keep it running."], after: { note: "Recovery tab: First failure / Second failure / Subsequent failures." } },
          { ask: "First failure:", opts: ["Restart the Service", "Take No Action", "Run a Program", "Restart the Computer"], ok: 0,
            bad: ["", "That's what's happening now: it stays stopped.", "Possible for special cases, but restarting the service is the simple fix.", "Rebooting the whole PC for a print service is far too disruptive."],
            after: { rows: [["Plug and Play", "Enables a computer to recognize…", "Running", "Manual"], ["Print Spooler", "This service spools print jobs…", `<span class="win-okc">Running</span>`, "Automatic"], ["Remote Registry", "Enables remote users to modify…", "", "Disabled"], ["Windows Update", "Enables the detection, download…", "Running", "Manual (Trigger Start)"]], sel: 1, fresh: 1,
              note: "Recovery set and service started. Now find out why it stops." } }] },
      why: "services.msc controls background services. The Recovery tab restarts a failed service so the user can keep printing while you find the cause.",
      tip: "Then check Event Viewer's System log for why it stops (often a bad printer driver or stuck print job). Startup types: Automatic, Automatic (Delayed Start), Manual, Disabled." },

    { id: "ram", ask: "The PC is slow. See which app is eating the RAM.", tool: "taskmgr",
      pick: ["taskmgr", "diskmgmt", "eventvwr", "msinfo32"], hint: "The quickest live view of what's running and how much memory each thing uses.",
      alt: { resmon: "Resource Monitor's Memory tab works too, but Task Manager is the quickest." },
      mock: { title: "Task Manager", icon: "📊",
        tree: [[0, "☰ Processes", 1], [0, "📈 Performance"], [0, "🕘 App history"], [0, "🚀 Startup apps"], [0, "👤 Users"], [0, "📋 Details"], [0, "⚙ Services"]],
        cols: ["Name", "CPU", "Memory", "Disk"],
        rows: [["📝 Notes", "0%", "48 MB", "0 MB/s"], ["🗂 Windows Explorer", "0.4%", "112 MB", "0 MB/s"], ["🌐 Web Browser (38)", "6%", `<span class="win-crit">5,912 MB</span>`, "0.1 MB/s"], ["🎵 Music", "1%", "210 MB", "0 MB/s"], ["🛡 Antimalware Service Executable", "0.2%", "164 MB", "0 MB/s"]],
        steps: [
          { ask: "Click a column header to sort by it:", opts: ["CPU", "Memory", "Disk", "Network"], ok: 1,
            bad: ["CPU is the processor, not RAM.", "", "Disk is storage activity, not RAM.", "Network is traffic, not RAM."],
            after: { rows: [["🌐 Web Browser (38)", "6%", `<span class="win-crit">5,912 MB</span>`, "0.1 MB/s"], ["🎵 Music", "1%", "210 MB", "0 MB/s"], ["🛡 Antimalware Service Executable", "0.2%", "164 MB", "0 MB/s"], ["🗂 Windows Explorer", "0.4%", "112 MB", "0 MB/s"], ["📝 Notes", "0%", "48 MB", "0 MB/s"]], sel: 0, fresh: 1, note: "Memory: 91% in use." } },
          { ask: "The browser has 38 tabs open the user doesn't need. Best move?", opts: ["Close tabs (or End task on the browser)", "End task on Windows Explorer", "End task on Antimalware Service Executable", "Turn off the page file"], ok: 0,
            bad: ["", "That restarts the taskbar and desktop. It isn't the problem.", "That's the antivirus. Never kill it to save memory.", "That makes low memory worse, not better."],
            after: { rows: [["🎵 Music", "1%", "210 MB", "0 MB/s"], ["🛡 Antimalware Service Executable", "0.2%", "164 MB", "0 MB/s"], ["🗂 Windows Explorer", "0.4%", "112 MB", "0 MB/s"], ["🌐 Web Browser (3)", "1%", "640 MB", "0 MB/s"], ["📝 Notes", "0%", "48 MB", "0 MB/s"]], fresh: 1, note: "Memory: 38% in use." } }] },
      why: "Task Manager's Processes page shows live CPU, memory and disk use per app. Sort by Memory and the culprit floats to the top.",
      tip: "If memory is always near 100% with normal use, the fix is more RAM. Resource Monitor gives deeper per-process detail." },

    { id: "hr", ask: "Only HR staff should open the D:\\HR folder. Right now every user can.", tool: "ntfs",
      pick: ["ntfs", "lusrmgr", "gpedit", "certmgr"], hint: "Who can open a folder is set on the folder itself.",
      mock: { title: "HR Properties", icon: "📁", tree: [[0, "General"], [0, "Sharing"], [0, "Security", 1], [0, "Previous Versions"], [0, "Customize"]],
        cols: ["Group or user names", "Permissions"],
        rows: [["👥 SYSTEM", "Full control"], ["👥 Administrators", "Full control"], [`👥 Users <span class="win-inh">(inherited from D:\\)</span>`, "Read & execute"]], sel: 2,
        steps: [
          { ask: "<b>Users</b> is inherited from D:\\ (greyed out). How do you take it off?", opts: ["Advanced → Disable inheritance → convert to explicit, then remove Users", "Tick Deny Full control for Users", "Make the folder Hidden", "Rename the folder"], ok: 0,
            bad: ["", "HR staff are in Users too, and Deny wins: you'd lock HR out as well.", "Hidden isn't security. Anyone can show hidden files.", "A new name changes nothing about who can open it."],
            after: { rows: [["👥 SYSTEM", "Full control"], ["👥 Administrators", "Full control"]], note: "Inheritance off. Users removed." } },
          { ask: "<b>Add… HR-Staff</b>. Which permission lets them open, edit and save, but not change permissions?", opts: ["Modify", "Full control", "Read", "Write"], ok: 0,
            bad: ["", "Full control also lets them change permissions and take ownership. More than they need.", "Read can't save changes.", "Write alone doesn't include reading the files."],
            after: { rows: [["👥 SYSTEM", "Full control"], ["👥 Administrators", "Full control"], ["👥 HR-Staff", `<span class="win-okc">Modify</span>`]], sel: 2, fresh: 1, note: "Only HR (plus admins and SYSTEM) can open D:\\HR." } }] },
      why: "NTFS permissions on the folder's Security tab decide who can open it. Give the HR group just enough (Modify) and remove the broad Users entry.",
      tip: "Exam tip: assign permissions to groups, not single people (least privilege). NTFS applies at the PC and over the network; share permissions only over the network." },

    { id: "space", ask: "C: is almost full. Free up space safely.", tool: "cleanmgr",
      pick: ["cleanmgr", "dfrgui", "diskmgmt", "regedit"], hint: "Which tool deletes junk files and knows what's safe to delete?",
      mock: { title: "Disk Cleanup for (C:)", icon: "🧹", cols: ["Files to delete", "Size"],
        rows: [["☑ Temporary Internet Files", "212 MB"], ["☑ Recycle Bin", "1.2 GB"], ["☑ Temporary files", "640 MB"], ["☑ Thumbnails", "84 MB"], ["☐ Downloads", "6.4 GB"]],
        steps: [
          { ask: "Which one should you <b>not</b> tick without asking the user?", opts: ["Downloads", "Temporary files", "Thumbnails", "Temporary Internet Files"], ok: 0,
            bad: ["", "Temp files are safe to delete.", "Thumbnails are rebuilt automatically.", "Browser cache is safe to delete."], after: { note: "Leave Downloads alone: those are the user's own files." } },
          { ask: "You also want old Windows Update leftovers gone. Click:", opts: ["Clean up system files", "Delete C:\\Windows\\WinSxS by hand", "Run chkdsk /f", "Format C:"], ok: 0,
            bad: ["", "Never delete WinSxS by hand. It can break Windows. Let the cleanup tools do it.", "chkdsk fixes file system errors. It doesn't free space.", "That erases everything."],
            after: { rows: [["☑ Windows Update Cleanup", "3.1 GB"], ["☑ Temporary Internet Files", "212 MB"], ["☑ Recycle Bin", "1.2 GB"], ["☑ Temporary files", "640 MB"], ["☑ Thumbnails", "84 MB"], ["☐ Downloads", "6.4 GB"]], fresh: 1, note: "Total space you gain: 5.2 GB." } }] },
      why: "Disk Cleanup (cleanmgr) knows which files Windows can safely throw away, including old update files after \"Clean up system files\".",
      tip: "Storage Sense (Settings → System → Storage) can do the same automatically." },

    { id: "bios", ask: "Support asks for this PC's BIOS version and installed RAM. Don't open the case.", tool: "msinfo32",
      pick: ["msinfo32", "devmgmt", "regedit", "perfmon"], hint: "One read-only report shows BIOS, RAM, CPU and more on its first page.",
      mock: { title: "System Information", icon: "ℹ️", menu: 1, tree: [[0, "System Summary"], [1, "Hardware Resources"], [1, "Components"], [1, "Software Environment"]],
        cols: ["Item", "Value"], rows: [],
        steps: [{ ask: "Which page shows them?", opts: ["System Summary", "Hardware Resources", "Components", "Software Environment"], ok: 0,
          bad: ["", "That's IRQs, memory addresses and conflicts.", "That's per-device detail (display, storage, network…).", "That's drivers, services and startup programs."],
          after: { tree: [[0, "System Summary", 1], [1, "Hardware Resources"], [1, "Components"], [1, "Software Environment"]],
            rows: [["OS Name", "Microsoft Windows 11 Pro"], ["System Manufacturer", "ExampleCo"], [`<b>BIOS Version/Date</b>`, `<span class="win-okc">ExampleCo 1.14.0, 3/8/2025</span>`], ["BIOS Mode", "UEFI"], ["Secure Boot State", "On"], [`<b>Installed Physical Memory (RAM)</b>`, `<span class="win-okc">16.0 GB</span>`]], fresh: 1, note: "Found both. File → Export saves the report as text." } }] },
      why: "msinfo32 shows the BIOS version, BIOS mode, Secure Boot state and installed RAM on its System Summary page, without opening the case.",
      tip: "Exam tip: it's read-only. Good for \"is Secure Boot on?\" and \"is this UEFI?\" questions too." },

    { id: "schedule", ask: "Run a backup script every Friday at 6 pm, even when nobody is signed in.", tool: "taskschd",
      pick: ["taskschd", "services", "msconfig", "perfmon"], hint: "Which tool runs things automatically on a timetable?",
      mock: { title: "Create Basic Task Wizard", icon: "🗓️", tree: [[0, "Create a Basic Task"], [0, "Trigger", 1], [0, "Action"], [0, "Finish"]], cols: ["Setting", "Value"], rows: [["Name", "Friday backup"]],
        steps: [
          { ask: "When do you want the task to start?", opts: ["Daily", "Weekly", "Monthly", "When the computer starts"], ok: 1,
            bad: ["Daily would run it every day.", "", "Monthly can't do \"every Friday\" as simply.", "That runs at boot, not Friday evenings."], after: { rows: [["Name", "Friday backup"], ["Trigger", "Weekly · Friday · 6:00 PM"]], fresh: 1 } },
          { ask: "Action:", opts: ["Start a program", "Send an e-mail (deprecated)", "Display a message (deprecated)"], ok: 0,
            bad: ["", "Deprecated and doesn't run the script.", "Deprecated and doesn't run the script."], after: { rows: [["Name", "Friday backup"], ["Trigger", "Weekly · Friday · 6:00 PM"], ["Action", "Start a program: backup.ps1"]], fresh: 1 } },
          { ask: "Task <b>Properties → General</b>: so it runs with nobody signed in, choose:", opts: ["Run whether user is logged on or not", "Run only when user is logged on", "Hidden", "Configure for: Windows 10"], ok: 0,
            bad: ["", "Then it skips Fridays when nobody's signed in.", "Hidden only hides it in the list.", "That's compatibility, not when it runs."],
            after: { rows: [["Name", "Friday backup"], ["Trigger", "Weekly · Friday · 6:00 PM"], ["Action", "Start a program: backup.ps1"], ["Security options", `<span class="win-okc">Run whether user is logged on or not</span>`]], fresh: 1, note: "Task ready." } }] },
      why: "Task Scheduler runs programs or scripts on a timetable or on events (at logon, at startup), with or without anyone signed in.",
      tip: "Exam tip: triggers say when, actions say what." },

    { id: "admin", ask: "Make the new technician's local account an administrator on this PC (no domain).", tool: "lusrmgr",
      pick: ["lusrmgr", "gpedit", "ntfs", "certmgr"], hint: "Local accounts and the groups they belong to.",
      mock: { title: "lusrmgr - [Local Users and Groups (Local)\\Groups]", icon: "👥", menu: 1, tree: [[0, "Local Users and Groups (Local)"], [1, "Users"], [1, "Groups", 1]],
        cols: ["Name", "Description"], rows: [["👥 Administrators", "Administrators have complete and unrestricted access…"], ["👥 Guests", "Guests have the same access as members of the Users group by default…"], ["👥 Remote Desktop Users", "Members in this group are granted the right to logon remotely"], ["👥 Users", "Users are prevented from making accidental or intentional system-wide changes…"]], sel: 0,
        steps: [{ ask: "Open <b>Administrators</b>. Then:", opts: ["Add… and type the technician's account name", "Rename the group", "Delete the group", "Remove the built-in Administrator"], ok: 0,
          bad: ["", "Renaming doesn't put anyone in it.", "Never delete built-in groups.", "That doesn't add the technician."],
          after: { note: "Members: Administrator, new-tech. They'll have admin rights at next sign-in." } }] },
      why: "lusrmgr.msc manages local accounts and groups. Membership in the local Administrators group is what makes an account an admin on that PC.",
      tip: "Exam tip: Home editions don't have lusrmgr.msc. Use Settings → Accounts, or: net localgroup Administrators <name> /add" },

    { id: "pwlen", ask: "Require passwords of at least 12 characters on this standalone PC.", tool: "gpedit",
      pick: ["gpedit", "lusrmgr", "regedit", "msconfig"], hint: "A rule for the whole PC, not one account: that's a policy.",
      mock: { title: "Local Group Policy Editor", icon: "📜", menu: 1, tree: [[0, "Local Computer Policy"], [1, "Computer Configuration"], [1, "User Configuration"]],
        cols: ["Policy", "Security Setting"], rows: [],
        steps: [
          { ask: "Where is the password policy?", opts: ["Computer Configuration → Windows Settings → Security Settings → Account Policies → Password Policy", "User Configuration → Administrative Templates → Control Panel", "Computer Configuration → Administrative Templates → System → Logon", "User Configuration → Windows Settings → Scripts"], ok: 0,
            bad: ["", "Control Panel policies hide or limit settings pages.", "Logon policies change sign-in behavior, not password rules.", "Scripts run at logon or logoff."],
            after: { tree: [[0, "Local Computer Policy"], [1, "Computer Configuration"], [2, "Windows Settings"], [3, "Security Settings"], [4, "Account Policies"], [5, "Password Policy", 1], [5, "Account Lockout Policy"]],
              rows: [["Enforce password history", "0 passwords remembered"], ["Maximum password age", "42 days"], ["Minimum password age", "0 days"], ["Minimum password length", "0 characters"], ["Password must meet complexity requirements", "Disabled"], ["Store passwords using reversible encryption", "Disabled"]], sel: 3 } },
          { ask: "Open <b>Minimum password length</b> and set:", opts: ["12 characters", "12 days", "Enable \"Store passwords using reversible encryption\"", "0 characters"], ok: 0,
            bad: ["", "Days belong to the password age settings.", "That weakens security: passwords become recoverable.", "0 means no minimum at all."],
            after: { rows: [["Enforce password history", "0 passwords remembered"], ["Maximum password age", "42 days"], ["Minimum password age", "0 days"], ["Minimum password length", `<span class="win-okc">12 characters</span>`], ["Password must meet complexity requirements", "Disabled"], ["Store passwords using reversible encryption", "Disabled"]], sel: 3, fresh: 1, note: "Applies at the next password change." } }] },
      why: "Password rules for a whole PC are a security policy. On a standalone Pro PC that's the Local Group Policy Editor.",
      tip: "Exam tip: Home editions don't have gpedit.msc. In a domain the same setting comes from Group Policy on the server; gpupdate /force applies it right away." },

    { id: "cert", ask: "A user's email signing certificate seems to be missing. Check her certificates.", tool: "certmgr",
      pick: ["certmgr", "lusrmgr", "msinfo32", "gpedit"], hint: "Certificates have their own console.",
      mock: { title: "certmgr - [Certificates - Current User]", icon: "📜", menu: 1, tree: [[0, "Certificates - Current User"], [1, "Personal"], [1, "Trusted Root Certification Authorities"], [1, "Intermediate Certification Authorities"], [1, "Untrusted Certificates"]],
        cols: ["Issued To", "Issued By", "Expiration Date", "Intended Purposes"], rows: [],
        steps: [{ ask: "Which folder holds her own certificates (the ones with her private key)?", opts: ["Personal", "Trusted Root Certification Authorities", "Untrusted Certificates", "Intermediate Certification Authorities"], ok: 0,
          bad: ["", "Those are the CAs her PC trusts, not her own certificate.", "Those are explicitly distrusted ones.", "Those are the in-between CAs in a chain."],
          after: { tree: [[0, "Certificates - Current User"], [1, "Personal", 1], [2, "Certificates"], [1, "Trusted Root Certification Authorities"], [1, "Intermediate Certification Authorities"], [1, "Untrusted Certificates"]],
            rows: [["m.lee@acme.example", "Acme Issuing CA", `<span class="win-crit">2/1/2026 (expired)</span>`, "Secure Email"]], fresh: 1, note: "Found it: expired. She needs a new one from IT." } }] },
      why: "certmgr.msc shows the current user's certificates. Her own certificates (with private keys) are in Personal → Certificates.",
      tip: "Exam tip: certmgr.msc is for the current user; certlm.msc is for the local computer." },

    { id: "defrag", ask: "An old office PC with a spinning hard drive feels slow opening files.", tool: "dfrgui",
      pick: ["dfrgui", "cleanmgr", "diskmgmt", "resmon"], hint: "Fragmented files slow down spinning disks. Which tool fixes that?",
      mock: { title: "Optimize Drives", icon: "🧩", cols: ["Drive", "Media type", "Last run", "Current status"],
        rows: [["(C:)", "Hard disk drive", "Never run", `<span class="win-warn">OK (18% fragmented)</span>`], ["Data (D:)", "Solid state drive", "3/9/2026", "OK (12 days since last retrim)"]],
        steps: [{ ask: "Which drive should be defragmented?", opts: ["C: (hard disk drive)", "D: (solid state drive)", "Both", "Neither"], ok: 0,
          bad: ["", "SSDs don't benefit from defragmenting. For them, Optimize sends TRIM instead.", "Only the hard disk drive needs defragmenting.", "C: is 18% fragmented: that's worth fixing."],
          after: { rows: [["(C:)", "Hard disk drive", "Today", `<span class="win-okc">OK (0% fragmented)</span>`], ["Data (D:)", "Solid state drive", "3/9/2026", "OK (12 days since last retrim)"]], fresh: 1, note: "Optimization complete." } }] },
      why: "Defragment and Optimize Drives puts a hard drive's file pieces back together so the read head moves less.",
      tip: "Exam tip: don't defragment SSDs. Windows knows the media type and sends TRIM to SSDs instead." },

    { id: "perf", ask: "Record CPU and disk use all day to prove the PC is overloaded.", tool: "perfmon",
      pick: ["perfmon", "taskmgr", "eventvwr", "dfrgui"], hint: "You need a recording over hours, not a live look.",
      alt: { taskmgr: "Task Manager shows now, not a full day's record. You need something that logs over time." },
      mock: { title: "Performance Monitor", icon: "📈", menu: 1, tree: [[0, "Performance"], [1, "Monitoring Tools"], [2, "Performance Monitor"], [1, "Data Collector Sets"], [2, "User Defined", 1], [2, "System"], [1, "Reports"]],
        cols: ["Name", "Status"], rows: [],
        steps: [{ ask: "Right-click <b>User Defined → New</b>:", opts: ["Data Collector Set", "Event Viewer filter", "Restore point", "Scheduled defrag"], ok: 0,
          bad: ["", "That's in Event Viewer and filters logs, not counters.", "Restore points are a System Protection thing.", "That's Optimize Drives."],
          after: { rows: [["All-day load", `<span class="win-okc">Running</span>`]], fresh: 1, note: "Logging \\Processor(_Total)\\% Processor Time and \\PhysicalDisk(_Total)\\% Disk Time every 15 s. Reports appear under Reports." } }] },
      why: "Performance Monitor records counters over time (Data Collector Sets) and builds reports, so you can prove what happened all day.",
      tip: "Task Manager and Resource Monitor show now. Performance Monitor records history." },

    { id: "reg", ask: "Vendor support says to change one registry value to fix a bug.", tool: "regedit",
      pick: ["regedit", "gpedit", "msconfig", "services"], hint: "The registry has its own editor. What do you do before touching it?",
      mock: { title: "Registry Editor", icon: "🧬", menu: 1, tree: [[0, "Computer"], [1, "HKEY_CLASSES_ROOT"], [1, "HKEY_CURRENT_USER"], [1, "HKEY_LOCAL_MACHINE"], [2, "SOFTWARE"], [3, "ExampleVendor", 1], [1, "HKEY_USERS"], [1, "HKEY_CURRENT_CONFIG"]],
        cols: ["Name", "Type", "Data"], rows: [["(Default)", "REG_SZ", "(value not set)"], ["CacheMode", "REG_DWORD", "0x00000001 (1)"]], sel: 1,
        steps: [{ ask: "Before you edit <b>CacheMode</b>:", opts: ["File → Export the key (a backup)", "Delete the whole key", "Edit it and restart right away", "Empty the Recycle Bin"], ok: 0,
          bad: ["", "Deleting can break the app, and there's no undo.", "If it goes wrong, there's no way back without a backup.", "Unrelated."],
          after: { rows: [["(Default)", "REG_SZ", "(value not set)"], ["CacheMode", "REG_DWORD", `<span class="win-okc">0x00000000 (0)</span>`]], sel: 1, fresh: 1, note: "Backup saved as ExampleVendor.reg, then the value was changed." } }] },
      why: "regedit edits the registry directly. There is no undo, so export the key (or make a restore point) first.",
      tip: "Exam tip: wrong registry edits can stop Windows from booting. Back up first, change only what you were told." },

    { id: "safemode", ask: "Boot into Safe Mode at the next restart to remove a stubborn program.", tool: "msconfig",
      pick: ["msconfig", "taskmgr", "services", "dfrgui"], hint: "One tool has a Boot tab.",
      mock: { title: "System Configuration", icon: "🛠️", tree: [[0, "General"], [0, "Boot"], [0, "Services"], [0, "Startup"], [0, "Tools"]], cols: ["Option", ""], rows: [],
        steps: [
          { ask: "Which tab?", opts: ["General", "Boot", "Services", "Startup"], ok: 1,
            bad: ["General picks normal or selective startup.", "", "Services lets you turn services off for testing.", "In Windows 10/11 that tab only links to Task Manager."],
            after: { tree: [[0, "General"], [0, "Boot", 1], [0, "Services"], [0, "Startup"], [0, "Tools"]], rows: [["☐ Safe boot", "Minimal · Alternate shell · Active Directory repair · Network"], ["☐ No GUI boot", ""], ["☐ Boot log", ""], ["☐ Base video", ""]] } },
          { ask: "Tick:", opts: ["Safe boot (Minimal)", "No GUI boot", "Boot log", "Base video"], ok: 0,
            bad: ["", "That only hides the boot animation.", "That writes a boot log file (ntbtlog.txt).", "That uses a basic display driver, not Safe Mode."],
            after: { rows: [[`<span class="win-okc">☑ Safe boot</span>`, "◉ Minimal"], ["☐ No GUI boot", ""], ["☐ Boot log", ""], ["☐ Base video", ""]], fresh: 1, note: "Restart to boot into Safe Mode." } }] },
      why: "msconfig's Boot tab can force Safe Mode on the next restart. Safe Mode loads only basic drivers, so stubborn programs can't start and fight back.",
      tip: "Untick Safe boot afterwards, or the PC keeps booting into Safe Mode. Another way: Shift + Restart → Troubleshoot → Advanced options → Startup Settings." },

    { id: "remote", ask: "From your desk, check a coworker's PC: its event logs, services and shared folders, all in one window.", tool: "compmgmt",
      pick: ["compmgmt", "eventvwr", "msinfo32", "certmgr"], hint: "One console bundles many of the others, and can point at another computer.",
      mock: { title: "Computer Management", icon: "🖥️", menu: 1, tree: [[0, "Computer Management (Local)", 1], [1, "System Tools"], [2, "Task Scheduler"], [2, "Event Viewer"], [2, "Shared Folders"], [2, "Local Users and Groups"], [2, "Performance"], [2, "Device Manager"], [1, "Storage"], [2, "Disk Management"], [1, "Services and Applications"]],
        cols: ["Name"], rows: [["System Tools"], ["Storage"], ["Services and Applications"]],
        steps: [{ ask: "Right-click <b>Computer Management (Local)</b>:", opts: ["Connect to another computer…", "Export List…", "Refresh", "Help"], ok: 0,
          bad: ["", "That saves the list to a file.", "That just reloads this PC.", "That opens help."],
          after: { tree: [[0, "Computer Management (PC-0214)", 1], [1, "System Tools"], [2, "Task Scheduler"], [2, "Event Viewer"], [2, "Shared Folders"], [2, "Local Users and Groups"], [2, "Performance"], [2, "Device Manager"], [1, "Storage"], [2, "Disk Management"], [1, "Services and Applications"]], note: "Connected to PC-0214." } }] },
      why: "Computer Management bundles Event Viewer, Shared Folders, Local Users and Groups, Device Manager, Disk Management and Services, and can connect to another computer.",
      tip: "You need admin rights on that PC, and its firewall must allow remote management." },

    { id: "locked", ask: "A file won't delete: \"it's open in another program\". Find which program.", tool: "resmon",
      pick: ["resmon", "perfmon", "msinfo32", "cleanmgr"], hint: "You need to see which process holds a handle to that file.",
      alt: { taskmgr: "Task Manager shows processes, but not which files they hold open. Resource Monitor does." },
      mock: { title: "Resource Monitor", icon: "🔎", menu: 1, tree: [[0, "Overview"], [0, "CPU", 1], [0, "Memory"], [0, "Disk"], [0, "Network"]],
        cols: ["Image", "PID", "Handle Type", "Handle Name"], rows: [],
        steps: [{ ask: "CPU tab → <b>Associated Handles</b>. Then:", opts: ["Type the file name in its search box", "Sort Processes by CPU", "Open the Network tab", "Monitor → Stop Monitoring"], ok: 0,
          bad: ["", "CPU use won't tell you who has the file open.", "Network shows connections, not open files.", "That freezes the view."],
          after: { rows: [["EXCEL.EXE", "8124", "File", "C:\\Users\\m.lee\\Documents\\report.xlsx"]], fresh: 1, note: "Excel has it open (in a window hidden behind others). Save and close it, then delete." } }] },
      why: "Resource Monitor's Associated Handles search shows exactly which process has a file open.",
      tip: "Resource Monitor has more detail than Task Manager: per-process files, handles and network connections." },
  ];

  let W = null; // the open "Right tool" session

  function wtSolved(store) { return store.get("solved", {}); }
  function wtRender(ctx) {
    addCss();
    const store = ctx.store;
    let i = Math.min(Math.max(+store.get("cur", 0) || 0, 0), SC.length - 1);
    W = { ctx, i };
    wtStart(i);
  }
  function wtStart(i) {
    const s = SC[i];
    W.i = i; W.ctx.store.set("cur", i);
    W.order = shuffle(s.pick); W.phase = "pick"; W.wrong = new Set(); W.hinted = false; W.step = 0; W.bad = null;
    W.view = { rows: s.mock.rows || [], tree: s.mock.tree || null, sel: s.mock.sel, note: "", fresh: 0 };
    wtDraw();
    setTimeout(() => W && W.ctx.report(`Right tool: ${s.ask}`), 80);
  }
  function wtDraw() {
    const { ctx } = W, s = SC[W.i], solved = wtSolved(ctx.store), n = Object.keys(solved).length;
    ctx.el.classList.toggle("win-calm", !!ctx.calm);
    const ch = W.order.map((t, k) => {
      const [nm, cmd, does] = TOOLS[t];
      const right = t === s.tool, no = W.wrong.has(t), done = W.phase !== "pick";
      const cls = done ? (right ? "yes" : "dim") : (no ? "no" : "");
      const extra = no ? `<div class="win-does">${ctx.esc(s.alt && s.alt[t] || does)}</div>` : "";
      return `<button class="win-ch ${cls}" data-k="${k}" ${done || no ? "disabled" : ""}><kbd>${k + 1}</kbd><b>${ctx.esc(nm)}</b><code>${ctx.esc(cmd)}</code>${extra}</button>`;
    }).join("");
    const why = W.phase === "done" ? `<div class="win-why"><h4>✓ Why ${ctx.esc(TOOLS[s.tool][0])}</h4><p>${ctx.esc(s.why)}</p><p class="win-tip">${ctx.esc(s.tip)}</p>
      <div class="win-next"><button class="btn" id="win-nx">Next request →</button><span class="tag"><kbd>Enter</kbd></span></div></div>` : "";
    const hint = W.hinted && W.phase === "pick" ? `<div class="win-hint">💡 ${ctx.esc(s.hint)}</div>` : "";
    ctx.el.innerHTML = `
      <div class="win-top"><span style="font-size:30px">🧰</span><div><h2>Right tool for the job</h2><div class="tag">Read the request, pick the Windows tool, then do the one thing in it.</div></div>
        <div class="win-prog">${n}/${SC.length} solved<div class="win-bar"><i style="width:${(n / SC.length) * 100}%"></i></div></div></div>
      <div class="win-dots">${SC.map((x, k) => `<button class="win-dot ${solved[x.id] ? "ok" : ""} ${k === W.i ? "on" : ""}" data-j="${k}" title="${ctx.esc(x.ask)}">${k + 1}</button>`).join("")}</div>
      <div class="win-grid">
        <div>
          <div class="glass win-ticket"><div class="win-tno">Ticket #${4100 + W.i * 7}</div><div class="win-ask">${ctx.esc(s.ask)}</div>
            <div class="tag">${W.phase === "pick" ? "Which tool do you open? Keys 1–4." : W.phase === "do" ? "Good. Now do it in the window →" : "Done."}</div>
            <div class="win-choices">${ch}</div>${hint}</div>
          ${why}
        </div>
        <div id="win-mock">${W.phase === "pick" ? `<div class="win-ph"><div class="big">🪟</div>The tool's window opens here once you pick it.</div>` : wtMock(s)}</div>
      </div>`;
    ctx.el.querySelectorAll(".win-ch").forEach((b) => b.onclick = () => wtPick(+b.dataset.k));
    ctx.el.querySelectorAll(".win-dot").forEach((b) => b.onclick = () => wtStart(+b.dataset.j));
    ctx.el.querySelectorAll(".win-ctx button").forEach((b) => b.onclick = () => wtDo(+b.dataset.o));
    const nx = ctx.el.querySelector("#win-nx"); if (nx) nx.onclick = wtNext;
  }
  function wtMock(s) {
    const m = s.mock, v = W.view, st = m.steps[W.step];
    const tree = v.tree ? `<div class="win-tree">${v.tree.map(([d, l, on]) => `<div class="${on ? "on" : ""}" style="padding-left:${8 + d * 14}px">${l}</div>`).join("")}</div>` : "";
    const cols = m.cols;
    const table = v.rows.length ? `<table class="win-t">${cols ? `<tr>${cols.map((c) => `<th>${c}</th>`).join("")}</tr>` : ""}${v.rows.map((r, k) => `<tr class="${k === v.sel ? "on" : ""} ${v.fresh ? "new" : ""}">${r.map((c) => `<td>${c}</td>`).join("")}</tr>`).join("")}</table>`
      : `<div style="padding:30px;color:#888;text-align:center">${cols ? "Pick something below." : ""}</div>`;
    let act = "";
    if (W.phase === "do" && st) {
      act = `<div class="win-act"><div class="win-q">${st.ask}</div><div class="win-ctx">${st.opts.map((o, k) => {
        const x = W.tried && W.tried.has(k), glow = W.glow && k === st.ok;
        return `<button data-o="${k}" class="${x ? "x" : ""} ${glow ? "glow" : ""}" ${x ? "disabled" : ""}><kbd>${k + 1}</kbd>${W.ctx.esc(o)}</button>`;
      }).join("")}</div>${W.bad ? `<div class="win-say">✗ ${W.ctx.esc(W.bad)}</div>` : ""}</div>`;
    }
    const menu = m.menu ? `<div class="win-menu"><span>File</span><span>Action</span><span>View</span><span>Help</span></div>` : "";
    return `<div class="win-w"><div class="win-tb"><span>${m.icon}</span>${W.ctx.esc(m.title)}<span class="win-ctl"><span>─</span><span>▢</span><span>✕</span></span></div>${menu}
      <div class="win-body ${tree ? "two" : ""}">${tree}<div class="win-main">${table}</div></div>${act}
      <div class="win-status ${W.phase === "done" ? "good" : ""}">${W.ctx.esc(v.note || " ")}</div></div>`;
  }
  function wtPick(k) {
    if (W.phase !== "pick") return;
    const s = SC[W.i], t = W.order[k];
    if (!t || W.wrong.has(t)) return;
    if (t === s.tool) {
      W.phase = "do"; W.step = 0; W.tried = new Set(); W.bad = null;
      W.ctx.toast(`✓ ${TOOLS[t][0]}`);
      wtDraw();
    } else {
      W.wrong.add(t); wtDraw();
      shake(W, W.ctx.el.querySelector(`.win-ch[data-k="${k}"]`));
    }
  }
  function wtDo(k) {
    if (W.phase !== "do") return;
    const s = SC[W.i], st = s.mock.steps[W.step];
    if (!st || k >= st.opts.length || W.tried.has(k)) return;
    if (k !== st.ok) {
      W.tried.add(k); W.bad = (st.bad && st.bad[k]) || "Not that one."; W.missed = true;
      wtDraw(); shake(W, W.ctx.el.querySelector(".win-ctx")); return;
    }
    const a = st.after || {};
    if (a.rows) W.view.rows = a.rows;
    if (a.tree) W.view.tree = a.tree;
    W.view.sel = "sel" in a ? a.sel : (a.rows ? -1 : W.view.sel);
    W.view.fresh = a.fresh || 0; W.view.note = a.note || W.view.note;
    W.step++; W.tried = new Set(); W.bad = null; W.glow = false;
    if (W.step >= s.mock.steps.length) {
      W.phase = "done";
      const solved = wtSolved(W.ctx.store);
      if (!solved[s.id]) { solved[s.id] = 1; W.ctx.store.set("solved", solved); }
      W.ctx.toast(Object.keys(solved).length === SC.length ? "All requests solved. You know your Windows tools. 🏆" : "Solved ✓");
    }
    wtDraw();
  }
  function wtNext() {
    const solved = wtSolved(W.ctx.store);
    let j = W.i;
    for (let n = 1; n <= SC.length; n++) { const c = (W.i + n) % SC.length; if (!solved[SC[c].id]) { j = c; break; } if (n === SC.length) j = (W.i + 1) % SC.length; }
    wtStart(j);
    window.scrollTo({ top: 0, behavior: W.ctx.calm ? "auto" : "smooth" });
  }
  function wtHint() {
    if (W.phase === "pick") { W.hinted = true; wtDraw(); }
    else if (W.phase === "do") { W.glow = true; wtDraw(); }
  }

  Lab.register({
    id: "win-tools", title: "Right tool for the job", icon: "🧰", exams: ["a2"],
    blurb: "Real requests: pick the right Windows tool (Device Manager, Event Viewer, Disk Management…), then do the fix in it.",
    render: wtRender,
    progress: (ctx) => { const n = Object.keys(ctx.store.get("solved", {})).length; return n ? `${n}/${SC.length} solved` : `${SC.length} requests`; },
    key(e) {
      if (!live(W) || !plainKey(e)) return;
      if (/^[1-4]$/.test(e.key)) { e.preventDefault(); W.phase === "pick" ? wtPick(+e.key - 1) : wtDo(+e.key - 1); }
      else if ((e.key === "Enter" || e.key === "ArrowRight") && W.phase === "done") { e.preventDefault(); wtNext(); }
      else if (e.key === "h" || e.key === "H") wtHint();
    },
    remote(cmd, v) {
      if (!live(W)) return;
      if (cmd === "show") wtHint();
      else if (cmd === "grade") { if (W.phase === "done") wtNext(); else if (W.phase === "pick") wtPick(v - 1); else wtDo(v - 1); }
    },
  });

  // ---------------------------------------------------------------- NTFS vs share permissions
  // Rights as bits: R = read, W = write/change, F = the extras Full control adds (change permissions, take ownership).
  const R = 1, Wr = 2, F = 4;
  const BITS = { "Read": R, "Read & execute": R, "Write": Wr, "Modify": R | Wr, "Change": R | Wr, "Full control": R | Wr | F };
  const LEVELS = ["Full control", "Modify", "Read", "No access"];
  function level(b) { return (b & 7) === 7 ? 0 : (b & 3) === 3 ? 1 : (b & R) ? 2 : 3; }
  // Windows checks entries in this order, and for each right the first entry that mentions it decides:
  // explicit Deny, explicit Allow, inherited Deny, inherited Allow. Nothing mentioned = not allowed.
  function evalAcl(list, groups) {
    const mine = list.filter((x) => groups.includes(x[0]));
    const rank = (x) => (/inh/.test(x[2] || "") ? 2 : 0) + (/deny/.test(x[2] || "") ? 0 : 1);
    let out = 0;
    for (const bit of [R, Wr, F]) {
      const hit = mine.filter((x) => BITS[x[1]] & bit).sort((a, b) => rank(a) - rank(b))[0];
      if (hit && !/deny/.test(hit[2] || "")) out |= bit;
    }
    return out;
  }
  // [user, groups, where ("net" | "loc"), path, share entries, NTFS entries, rule numbers to light, note]
  // Entry: [group, permission, flags] with flags "deny" and/or "inh" (inherited from the parent folder).
  const PS = [
    { u: "Mei", g: ["Sales", "Users", "Everyone"], w: "loc", p: "D:\\Sales", share: [["Everyone", "Read"]], ntfs: [["Sales", "Modify"], ["Users", "Read", "inh"]], rules: [1],
      note: "She's signed in at the PC where the folder lives, so share permissions don't apply at all. NTFS gives Sales Modify." },
    { u: "Mei", g: ["Sales", "Users", "Everyone"], w: "net", p: "\\\\FS01\\Sales", share: [["Everyone", "Read"]], ntfs: [["Sales", "Modify"], ["Users", "Read", "inh"]], rules: [2],
      note: "Same folder, but now over the network: the share only allows Read, and the stricter of the two wins." },
    { u: "Omar", g: ["HR", "Users", "Everyone"], w: "net", p: "\\\\FS01\\HR", share: [["Everyone", "Full control"]], ntfs: [["Users", "Read", "inh"], ["HR", "Modify"]], rules: [2, 3],
      note: "NTFS allows add up across his groups: Read + Modify = Modify. The share allows everything, so NTFS decides." },
    { u: "Lena", g: ["HR", "Interns", "Users", "Everyone"], w: "net", p: "\\\\FS01\\HR", share: [["Everyone", "Change"]], ntfs: [["HR", "Modify"], ["Interns", "Full control", "deny"]], rules: [4],
      note: "She's in HR, but also in Interns, which has an explicit Deny. Deny beats Allow." },
    { u: "Sam", g: ["Accounting", "Users", "Everyone"], w: "loc", p: "D:\\Data\\Budget", share: [["Everyone", "Read"]], ntfs: [["Users", "Read", "inh"], ["Accounting", "Modify"]], rules: [1, 3, 5],
      note: "Local, so only NTFS counts. Users Read comes down from D:\\Data (inherited); Accounting Modify is set on the folder. Allows add up: Modify." },
    { u: "Chris", g: ["IT-Admins", "Users", "Everyone"], w: "net", p: "\\\\FS01\\Tools", share: [["Everyone", "Read"], ["IT-Admins", "Full control"]], ntfs: [["IT-Admins", "Full control"], ["Users", "Read"]], rules: [2, 3],
      note: "Share allows add up too: Everyone Read + IT-Admins Full control = Full control. NTFS also gives Full control." },
    { u: "Jo", g: ["Users", "Temp-Staff", "Everyone"], w: "net", p: "\\\\FS01\\Projects", share: [["Everyone", "Full control"]], ntfs: [["Users", "Modify", "inh"], ["Temp-Staff", "Write", "deny"]], rules: [3, 4],
      note: "Users gives Modify (read + write), but Temp-Staff has Deny Write. Deny removes the writing and leaves reading: Read." },
    { u: "Alex", g: ["Users", "Everyone"], w: "net", p: "\\\\FS01\\Mgmt", share: [["Everyone", "Read"]], ntfs: [["Managers", "Full control"], ["Administrators", "Full control"]], rules: [2],
      note: "The share would let him read, but NTFS lists nothing for any of his groups. No entry = no access." },
    { u: "Kai", g: ["Marketing", "Users", "Everyone"], w: "net", p: "\\\\FS01\\Mkt", share: [["Everyone", "Read"], ["Marketing", "Change"]], ntfs: [["Marketing", "Full control"]], rules: [2, 3],
      note: "NTFS gives Full control, but the share tops out at Change (read, write, delete: like Modify). Over the network the stricter wins: Modify." },
    { u: "Priya", g: ["Project-X", "Contractors", "Users", "Everyone"], w: "loc", p: "D:\\Work\\Project-X", share: [["Everyone", "Read"]], ntfs: [["Contractors", "Write", "deny inh"], ["Project-X", "Modify"]], rules: [5], bonus: 1,
      note: "Bonus rule: an Allow set right on the folder beats a Deny inherited from the parent. Explicit Deny still beats everything." },
    { q: "You copy report.docx from D:\\HR (HR only) into D:\\Public (Everyone: Read). What permissions does the copy have?", rules: [6],
      opts: ["The same as before: HR only", "It inherits D:\\Public's: Everyone can read it", "No one can open it until you fix it", "Everyone gets Full control"], ok: 1,
      note: "A copy is a new file, so it takes the permissions of the folder it lands in." },
    { q: "You move report.docx from D:\\HR (HR only) to D:\\Public on the same D: drive. What permissions does it have?", rules: [6],
      opts: ["It keeps its permissions: HR only", "It inherits D:\\Public's: Everyone can read it", "It loses all permissions", "Only the person who moved it can open it"], ok: 0,
      note: "Moving within the same NTFS volume just updates where the file is listed, so it keeps its permissions." },
    { q: "You move report.docx from D:\\HR (HR only) to E:\\Public, a different drive. What permissions does it have?", rules: [6],
      opts: ["It keeps its permissions: HR only", "It inherits E:\\Public's permissions", "It loses all permissions", "It becomes read-only for everyone"], ok: 1,
      note: "A move to another volume is really a copy then a delete, so it acts like a copy: it inherits the new folder's permissions." },
  ];
  const RULES = [
    "At the PC itself, only NTFS permissions count.",
    "Over the network, work out share and NTFS separately, then the more restrictive one wins.",
    "Inside share (or inside NTFS), Allows from all your groups add up.",
    "Deny beats Allow.",
    "Permissions flow down from the parent folder (inheritance). An Allow set on the folder itself beats an inherited Deny.",
    "Copy, or move to another drive: takes the new folder's permissions. Move on the same drive: keeps its own.",
  ];
  let P = null;
  function pAnswer(s) {
    if (s.q) return s.ok;
    const n = evalAcl(s.ntfs, s.g);
    return level(s.w === "loc" ? n : n & evalAcl(s.share, s.g));
  }
  function pRender(ctx) {
    addCss();
    P = { ctx, i: Math.min(+ctx.store.get("cur", 0) || 0, PS.length - 1) };
    pStart(P.i);
  }
  function pStart(i) {
    P.i = i; P.ctx.store.set("cur", i); P.picked = null; P.hint = false;
    pDraw();
    setTimeout(() => P && P.ctx.report(`NTFS vs share: scenario ${i + 1}`), 80);
  }
  function aclTable(ctx, title, list, s, off) {
    return `<div class="win-acl ${off ? "off" : ""}"><div class="win-w" style="animation:none"><div class="win-tb"><span>${title === "Share" ? "🔗" : "🔒"}</span>${title === "Share" ? "Share permissions" : "Security (NTFS)"}${off ? " · not used here" : ""}<span class="win-ctl"><span>✕</span></span></div>
      <table class="win-t"><tr><th>Group</th><th>Permission</th></tr>${list.map((x) => {
        const mine = s.g.includes(x[0]);
        const f = x[2] || "";
        return `<tr class="${mine ? "win-mine" : ""}"><td>${mine ? "★ " : ""}${ctx.esc(x[0])}</td><td>${/deny/.test(f) ? `<span class="win-deny">Deny</span>` : "Allow"} ${ctx.esc(x[1])}${/inh/.test(f) ? ` <span class="win-inh">(inherited)</span>` : ""}</td></tr>`;
      }).join("")}</table></div></div>`;
  }
  function pDraw() {
    const { ctx } = P, s = PS[P.i], ans = pAnswer(s), got = ctx.store.get("right", {}), n = Object.keys(got).length;
    ctx.el.classList.toggle("win-calm", !!ctx.calm);
    const opts = s.q ? s.opts : LEVELS;
    let body;
    if (s.q) body = `<div class="glass" style="padding:18px 20px"><div class="win-tno tag">Move or copy?</div><div class="win-q2">${ctx.esc(s.q)}</div></div>`;
    else body = `<div class="glass win-who"><div class="win-ava">${ctx.esc(s.u[0])}</div><div><b>${ctx.esc(s.u)}</b> opens <code>${ctx.esc(s.p)}</code><div class="win-groups">${s.g.map((g) => `<span class="win-g">${ctx.esc(g)}</span>`).join("")}</div></div>
        <div class="win-where ${s.w}">${s.w === "net" ? "🌐 Over the network" : "🖥️ At the PC itself"}</div></div>
      <div class="win-acls">${aclTable(ctx, "Share", s.share, s, s.w === "loc")}${aclTable(ctx, "NTFS", s.ntfs, s, false)}</div>
      <div class="win-q2" style="font-size:16px">What can ${ctx.esc(s.u)} actually do? <span class="tag">(★ = a group ${ctx.esc(s.u)} is in)</span></div>`;
    const done = P.picked != null;
    const btns = `<div class="win-ans" style="grid-template-columns:repeat(${s.q ? 2 : 4},1fr)">${opts.map((o, k) => {
      const cls = done ? (k === ans ? "yes" : k === P.picked ? "no" : "ghost") : "ghost";
      return `<button class="btn ${cls}" data-k="${k}" ${done ? "disabled" : ""}><kbd>${k + 1}</kbd> ${ctx.esc(o)}</button>`;
    }).join("")}</div>`;
    let why = "";
    if (done) {
      let steps = "";
      if (!s.q) {
        const nb = evalAcl(s.ntfs, s.g), sb = evalAcl(s.share, s.g);
        steps = `<ol class="win-steps">${s.w === "net" ? `<li>Share gives: <b>${LEVELS[level(sb)]}</b></li>` : `<li>Share: not used (at the PC itself)</li>`}<li>NTFS gives: <b>${LEVELS[level(nb)]}</b></li>
          ${s.w === "net" ? `<li>Stricter of the two: <b>${LEVELS[ans]}</b></li>` : ""}</ol>`;
      }
      why = `<div class="win-why" style="${P.picked === ans ? "" : "background:rgba(255,107,107,.08);border-color:rgba(255,107,107,.35)"}"><h4 style="${P.picked === ans ? "" : "color:var(--coral)"}">${P.picked === ans ? "✓ Right" : "✗ It's " + ctx.esc(opts[ans])}${s.bonus ? " · bonus rule" : ""}</h4>
        <p>${ctx.esc(s.note)}</p>${steps}<div class="win-next"><button class="btn" id="win-pn">Next →</button><span class="tag"><kbd>Enter</kbd></span></div></div>`;
    }
    const lit = done || P.hint ? s.rules : [];
    ctx.el.innerHTML = `
      <div class="win-top"><span style="font-size:30px">🔐</span><div><h2>NTFS vs share permissions</h2><div class="tag">Work out what someone can really do with a folder.</div></div>
        <div class="win-prog">${n}/${PS.length} right<div class="win-bar"><i style="width:${(n / PS.length) * 100}%"></i></div></div></div>
      <div class="win-dots">${PS.map((x, k) => `<button class="win-dot ${got[k] ? "ok" : ""} ${k === P.i ? "on" : ""}" data-j="${k}">${k + 1}</button>`).join("")}</div>
      <div class="win-pgrid"><div>${body}${btns}${why}</div>
        <div class="glass win-rules"><h4>The rules</h4><ol>${RULES.map((r, k) => `<li class="${lit.includes(k + 1) ? "lit" : ""}">${ctx.esc(r)}</li>`).join("")}</ol>
        <p class="tag" style="margin:10px 0 0">Share levels: Read, Change, Full control. Change ≈ Modify.<br>Phone Show button or <kbd>H</kbd> lights the rule you need.</p></div></div>`;
    ctx.el.querySelectorAll(".win-ans .btn").forEach((b) => b.onclick = () => pPick(+b.dataset.k));
    ctx.el.querySelectorAll(".win-dot").forEach((b) => b.onclick = () => pStart(+b.dataset.j));
    const nx = ctx.el.querySelector("#win-pn"); if (nx) nx.onclick = pNext;
  }
  function pPick(k) {
    const s = PS[P.i], max = s.q ? s.opts.length : 4;
    if (P.picked != null || k < 0 || k >= max) return;
    P.picked = k;
    if (k === pAnswer(s)) { const got = P.ctx.store.get("right", {}); got[P.i] = 1; P.ctx.store.set("right", got); }
    pDraw();
  }
  function pNext() { pStart((P.i + 1) % PS.length); window.scrollTo({ top: 0, behavior: P.ctx.calm ? "auto" : "smooth" }); }

  Lab.register({
    id: "win-perms", title: "NTFS vs share permissions", icon: "🔐", exams: ["a2"],
    blurb: "Who can open what? Combine share and NTFS permissions, Deny, inheritance, and move vs copy.",
    render: pRender,
    progress: (ctx) => { const n = Object.keys(ctx.store.get("right", {})).length; return n ? `${n}/${PS.length} right` : `${PS.length} cases`; },
    key(e) {
      if (!live(P) || !plainKey(e)) return;
      if (/^[1-4]$/.test(e.key)) { e.preventDefault(); pPick(+e.key - 1); }
      else if ((e.key === "Enter" || e.key === "ArrowRight") && P.picked != null) { e.preventDefault(); pNext(); }
      else if (e.key === "h" || e.key === "H") { P.hint = true; pDraw(); }
    },
    remote(cmd, v) {
      if (!live(P)) return;
      if (cmd === "show") { P.hint = true; pDraw(); }
      else if (cmd === "grade") { if (P.picked != null) pNext(); else pPick(v - 1); }
    },
  });
  // For tests: lets a checker confirm every computed answer.
  window.__winPermsAnswers = () => PS.map((s) => (s.q ? s.opts : LEVELS)[pAnswer(s)]);
})();
