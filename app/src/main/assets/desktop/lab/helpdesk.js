// IT Lab plug-in: "Help desk". Real tickets from coworkers, solved with CompTIA's troubleshooting method.
// Plain script (no modules): it registers itself with Lab.register and keeps everything inside this function.
//
// The method: CompTIA A+ lists SIX steps (the troubleshooting-methodology objective in the Core 1 exam;
// older A+ material and other courses sometimes counted 7). We use the current six:
//   1 Identify the problem (question the user, identify changes, back up data before making changes)
//   2 Establish a theory of probable cause (question the obvious; research if needed)
//   3 Test the theory (confirmed: next steps; not confirmed: new theory or escalate)
//   4 Establish a plan of action and implement the solution (vendor instructions, company policy)
//   5 Verify full system functionality and, if applicable, implement preventive measures
//   6 Document findings, actions and outcomes
// Network+ uses 7 steps (it splits "plan" from "implement or escalate"); that difference is an exam tip below.
// The malware ticket follows A+ Core 2's malware-removal steps instead (7 of them).
//
// Data shape: each ticket has turns; each turn has options. The FIRST option of a turn is the right one
// (they're shuffled on screen). Option = [label, kind, result]. kind "ok" → result is a list of lines
// ("@..." = what the user says, anything else = what you see). Other kinds ("order" = right idea, wrong
// moment; "risk" = could lose data or weaken security; "care" = poor customer service) → result is a
// short, gentle explanation and the option costs points.
(() => {
  "use strict";
  if (!window.Lab) return;

  const STEPS = [
    ["Identify the problem", "Question the user, find out what changed, back up before you change anything."],
    ["Establish a theory", "Question the obvious first. Research if you need to."],
    ["Test the theory", "Confirmed: plan the fix. Not confirmed: new theory, or escalate."],
    ["Plan and implement", "Follow vendor instructions and company policy."],
    ["Verify it all works", "Check everything, not just the fix. Add a preventive measure."],
    ["Document", "Findings, actions and outcomes, in the ticket."],
  ];
  const MALWARE = [
    ["Investigate and verify", "Confirm the symptoms really are malware."],
    ["Quarantine", "Cut the PC off from the network so it can't spread."],
    ["Disable System Restore", "Old restore points may hold the malware."],
    ["Remediate", "Update anti-malware, then scan and remove (Safe Mode / WinPE help)."],
    ["Schedule scans, run updates", "Keep it clean from now on."],
    ["Enable System Restore", "Turn it back on and create a clean restore point."],
    ["Educate the user", "How it got in, and how to avoid it next time."],
  ];
  const COST = { order: 10, risk: 15, care: 10 };
  const LABEL = { order: "Not yet", risk: "Risky", care: "Customer care" };
  const EXAM = { a1: "A+ Core 1", a2: "A+ Core 2" };

  const T = [
    { id: "charge", title: "Laptop won't charge", exam: "a1", topic: "Power · DC jack", who: "Dana", dept: "Accounting", col: "#ff8fb1",
      msg: "Hi! My laptop says “plugged in, not charging” and it's at 4%. I have month-end reports due Friday 😬",
      turns: [
        { s: 1, o: [
          ["Ask when it started and what changed (drops, spills, chargers)", "ok", ["@It started Monday. I did drop my laptop bag on Friday… the laptop was in it.", "Battery icon: 4%, plugged in, not charging."]],
          ["Order a replacement battery now: laptop batteries often stop charging after a few years", "order", "Too early. You don't know the cause yet, and a new battery won't help if power never reaches it."],
          ["Escalate to tier 2 straight away", "order", "Tier 2 would ask the same first questions. Escalate when a tested theory needs skills or access you don't have."],
          ["Tell her laptops just do that sometimes and it usually sorts itself out after a restart", "care", "Brushing it off leaves her stuck with a deadline. Take it seriously and ask questions."]] },
        { s: 2, o: [
          ["Check the obvious: outlet, adapter light, plug fully in", "ok", ["Her phone charges from the same outlet. The adapter's light is on.", "When you wiggle the plug at the laptop end, the charging light flickers on and off."]],
          ["Replace the motherboard", "order", "Expensive, slow, and nothing points there yet. Check the simple things first."],
          ["Update the BIOS first, since vendors often fix charging bugs in BIOS updates", "risk", "A BIOS update on a laptop at 4% that may lose power is a good way to brick it, and it isn't the obvious cause."],
          ["Search the “plugged in, not charging” message online and read forum threads for an hour", "order", "Research is fine, but question the obvious first: the plug and the adapter are right there."]] },
        { s: 3, o: [
          ["Try a known-good adapter with the same wattage, voltage and connector", "ok", ["Same thing: it only charges with the plug held at an angle.", "Theory confirmed: not the adapter. The laptop's DC jack is loose."]],
          ["Try any charger that fits, even a lower-wattage one", "risk", "The wrong wattage or voltage may not charge at all, or can overheat the adapter. Match the original's specs."],
          ["Open the laptop to look at the jack, battery still connected", "risk", "Never open it with power or the battery connected, and use ESD protection (a wrist strap)."],
          ["Close the ticket: it's the adapter", "order", "You haven't tested that yet, and the flicker on wiggle says otherwise."]] },
        { s: 4, o: [
          ["Back up her files, send it for warranty repair, lend her a loaner", "ok", ["@A loaner? Oh thank you. How long will mine be gone?", "You tell her: about 5 working days. Her files are copied to the loaner."]],
          ["Send it off without backing up her files", "risk", "Repair centers often reimage or swap the board. Back up before any device leaves your hands."],
          ["Open it up and solder a new DC jack yourself this afternoon to save her the wait", "risk", "The jack is often part of the motherboard, and opening it yourself can void the warranty. Follow the vendor's process."],
          ["Promise it'll be back tomorrow", "care", "Don't promise what you can't control. Set a real expectation she can plan around."]] },
        { s: 5, o: [
          ["When it's back: test charging and her files; suggest a padded sleeve", "ok", ["Charges steadily at any cable angle. Her files and apps are all there.", "@A padded sleeve, got it. Thank you!"]],
          ["Hand it back in its sealed box: the depot's report says it passed all their tests", "order", "Verify full functionality yourself before the user depends on it."],
          ["Skip the check, she'll call if it breaks", "care", "That makes her the tester. Check it before handing it back."],
          ["Reimage it to be safe", "risk", "Nothing calls for that, and it would wipe whatever isn't backed up."]] },
      ],
      notes: [
        ["Not charging after drop. Known-good adapter, same fault → loose DC jack. Backed up, warranty repair, loaner. Verified charging + data. Suggested sleeve."],
        ["Fixed laptop.", "Too thin: the next tech can't learn anything from it. Say the cause, the fix and how you checked."],
        ["Battery not charging. Swapped in a 45 W adapter from stock, then sent laptop to depot because user dropped it again (third time). Recommend billing her department. Closed.", "Blaming tone (the user may read it), a wrong adapter, and nothing verified. Stick to facts: cause, fix, how you checked."]],
      cs: "Setting expectations: a real repair time and a loaner kept Dana's deadline safe.",
      why: "Charging that comes and goes when you move the plug points at the DC jack, not the adapter. A known-good adapter with the right specs proves it. Since the jack is often soldered to the motherboard, a warranty repair beats a home fix.",
      tip: "Laptop won't charge: outlet → adapter LED → correct wattage and connector → DC jack → battery. Back up before a device goes out for repair, and disconnect power and battery (with ESD protection) before opening one." },

    { id: "display", title: "No display on a desktop", exam: "a1", topic: "Video · POST", who: "Marco", dept: "Sales", col: "#4fb3a9",
      msg: "My PC turns on, I can hear the fans, but the screen stays black. I have a client call at 11.",
      turns: [
        { s: 1, o: [
          ["Ask what he sees and hears (lights, beeps) and what changed", "ok", ["@Power light's on, no beeps. The monitor just says “No signal”. Oh, facilities moved my desk last night."]],
          ["Open the case and reseat the RAM; loose memory is a classic cause of a black screen", "order", "That's a later theory. No beeps and a desk move point at something simpler."],
          ["Order a new graphics card", "order", "Replacing parts before you know the cause wastes money and time."],
          ["Start unplugging and opening his PC right away, without explaining what you're checking", "care", "Tell him what you're going to check and how long it might take. He has a call at 11."]] },
        { s: 2, o: [
          ["Follow the video cable from the monitor to the PC", "ok", ["The cable goes into the video port on the motherboard (next to the USB ports).", "Lower down there's a graphics card, and its ports are empty."]],
          ["Look up the motherboard's beep code list on the maker's site and match the pattern", "order", "There were no beeps, so there's no code to look up. Check the cable first."],
          ["Swap in a brand-new monitor from the storeroom to rule out the screen", "order", "The monitor shows “No signal”, so it's on and working. Look at what feeds it."],
          ["Flash the latest BIOS, since the desk move may have reset its settings", "risk", "Nothing points at the BIOS, and a failed flash can stop the PC booting at all."]] },
        { s: 3, o: [
          ["Move the cable to a port on the graphics card", "ok", ["The Windows sign-in screen appears.", "Theory confirmed: with a graphics card installed, the motherboard's own video port is usually turned off."]],
          ["Reinstall Windows from the recovery partition in case the display driver broke", "risk", "Windows can't fix a cable in the wrong port, and a reinstall risks his data."],
          ["Reseat the RAM anyway, since a black screen after moving a PC often means loose memory", "order", "Test the theory you have first. If the cable move hadn't worked, RAM would be a fair next theory."],
          ["Tell him to use his laptop for now and you'll look into the desktop next week", "care", "That avoids the problem instead of fixing it."]] },
        { s: 4, o: [
          ["Seat the cable firmly in the graphics card's port, latch it, and tie it down neatly", "ok", ["@So it was just the wrong hole? Ha. Thanks, I'll make my call."]],
          ["Laugh and tell him it was a silly mistake", "care", "It wasn't his fault, and laughing at a user makes them stop asking for help."],
          ["Disable the graphics card in the BIOS so the motherboard port works", "risk", "That throws away the card he was given (and his second screen). Use the card's port."],
          ["Leave it loose, it works for now", "order", "A loose cable is the next ticket. Finish the fix properly."]] },
        { s: 5, o: [
          ["Check both screens; ask facilities to warn IT before desk moves", "ok", ["Correct resolution, both screens work.", "Facilities agree to send IT a note before desk moves."]],
          ["Close the ticket from your desk: he's back at work and the sign-in screen appeared", "order", "Verify full functionality: he has two screens, and you've only seen one."],
          ["Tell him to stop letting people move his desk", "care", "Fix it upstream with facilities instead of putting it on him."],
          ["Restart it five times to be sure", "order", "Restarting isn't verifying. Check what he actually uses: screens, resolution, apps."]] },
      ],
      notes: [
        ["No display after desk move: cable in motherboard port, GPU installed. Moved to GPU port, secured. Both screens verified. Facilities to warn IT before moves."],
        ["No signal on monitor. Reseated RAM, cleared CMOS, replaced HDMI cable and monitor; working after that. Old monitor sent to e-waste. Told user not to touch cables.", "That's not what happened. Wrong notes send the next tech down the wrong path."],
        ["User error.", "Unkind and useless. Write the cause, the fix and the prevention."]],
      cs: "Explaining clearly without blame: “the cable got moved to a port that's switched off” keeps Marco happy to call again.",
      why: "A black screen with “No signal” means the monitor is fine but gets nothing. When a PC has a graphics card, the motherboard's video port is usually disabled, so a cable moved there shows nothing. Checking the cable and port first fixed it in a minute.",
      tip: "No display: power, monitor input source, cable and port first. With a graphics card, plug into the card. Beeps at power-on are POST codes; their meaning depends on the BIOS maker, so look them up (memory and video are common causes)." },

    { id: "ghost", title: "Ghost images on prints", exam: "a1", topic: "Laser printers", who: "Priya", dept: "HR", col: "#8e94f2",
      msg: "Every page from the big printer has a faint copy of the top of the page repeated further down. It looks so unprofessional!",
      turns: [
        { s: 1, o: [
          ["Ask for a sample page and when it started", "ok", ["@It started this week. We've been printing a lot for open enrollment.", "Sample: a pale copy of the header repeats about 94 mm lower, all the way down the page."]],
          ["Reinstall the printer driver on every PC in HR, since a bad driver can garble pages", "order", "Big job, and you don't know it's a driver yet. Look at the problem first."],
          ["Shake the toner cartridge and close the ticket", "order", "That's a guess, not a diagnosis, and it skips testing and verifying."],
          ["Tell her to print less", "care", "That's not a fix, and HR needs those pages."]] },
        { s: 2, o: [
          ["Print the printer's own configuration page to rule out the PCs", "ok", ["The printer's own configuration page has the same ghost.", "So it's the printer itself, not a PC or a driver."]],
          ["Replace the fuser", "order", "The fuser is expensive. Find out where the fault is before replacing anything."],
          ["Reset the printer to factory settings", "risk", "That wipes its network settings, so everyone loses printing, and it can't fix a worn part."],
          ["Blame the paper and walk away", "care", "Without checking, that's a guess, and it leaves her with bad prints."]] },
        { s: 3, o: [
          ["Measure the repeat spacing and compare it with the vendor's repeating-defect chart", "ok", ["The chart: 94 mm = the imaging drum.", "The supplies page shows the drum at 2% life. Theory confirmed."]],
          ["Open the printer and feel the fuser by hand to see if it's heating properly", "risk", "The fuser runs very hot and can burn you. Power off and let it cool before touching anything inside."],
          ["Vacuum the spilled toner with the office vacuum", "risk", "Toner is so fine it passes through normal vacuums (and can be a hazard). Use a toner-rated vacuum."],
          ["Escalate to the printer vendor right now", "order", "Test your theory first; it's a quick check with the vendor's chart."]] },
        { s: 4, o: [
          ["Warn HR, power off, let it cool, swap the drum", "ok", ["New drum in. The old one goes back in its box for the vendor's recycling program.", "@10 minutes is fine, thanks for the heads-up!"]],
          ["Throw the old drum in the office bin, bagged up so the toner doesn't spill", "risk", "Consumables have disposal rules: follow the vendor's recycling program and local regulations (the SDS tells you how)."],
          ["Swap it in the middle of someone's print job without warning", "care", "Tell people before you take a shared device down."],
          ["Replace the fuser and transfer roller too while it's open, so you only do this once", "order", "The test pointed at the drum. Extra parts cost money and prove nothing."]] },
        { s: 5, o: [
          ["Print test pages, check the next HR jobs, set a low-supply alert", "ok", ["Clean pages, no ghost.", "The printer now emails IT when the drum drops below 10%."]],
          ["Assume it's fine and leave: the new drum came straight from the vendor", "order", "Verify with a test page; a second fault could still be there."],
          ["Turn off the printer's status emails; they fill the IT inbox and nobody reads them", "order", "Those alerts are the preventive measure. Keep them on and send them to IT."],
          ["Tell Priya to order drums from the vendor herself next time it happens", "care", "Supplies are IT's job; don't push the work onto the user."]] },
      ],
      notes: [
        ["Ghosting on all pages incl. test page; 94 mm repeat = drum (2% life). Drum replaced, old one recycled. Test pages clean. Low-supply alert to IT."],
        ["Printer fixed.", "Which part? Why? Future you will want the cause and the part."],
        ["Ghosting on HR printer. Replaced toner, fuser and drum to be safe, reinstalled drivers on HR PCs. Old parts in the bin. Printing OK per user, not tested.", "That's not what you did, and nothing was verified. Notes must match reality."]],
      cs: "Warning people before a shared device goes down for 10 minutes is small and makes a big difference.",
      why: "In a laser printer, a cleaning step wipes the drum before the next image. When the drum or its cleaning wears out, the old image prints again a little lower: a ghost. Defects that repeat at a fixed spacing match a roller's circumference, so the vendor's chart names the part.",
      tip: "Laser printing steps: processing, charging, exposing, developing, transferring, fusing, cleaning. Ghost images → drum or cleaning. Toner that smears or rubs off → fusing. Repeating marks → match the spacing to a roller." },

    { id: "slow", title: "Slow PC after an update", exam: "a2", topic: "Windows performance", who: "Jordan", dept: "Marketing", col: "#ffcf7a",
      msg: "Since last week's update my PC takes forever to start and everything crawls. Can you make it fast again?",
      turns: [
        { s: 1, o: [
          ["Ask when it started and what changed (updates, new apps)", "ok", ["@After the update, I think. I also installed a photo app that syncs my phone, and a couple of free tools."]],
          ["Reimage the PC tonight so it's back to a clean, fast state by the morning", "risk", "Reimaging wipes the disk. You don't know the cause, and nothing is backed up yet."],
          ["Turn off Windows Update with a policy so another update can't slow it down again", "risk", "Updates fix security holes. Turning them off trades a slow PC for an unsafe one."],
          ["Say the PC is just getting old and suggest asking their manager for a new one", "care", "Old PCs can still run well. Find out what's actually going on."]] },
        { s: 1, q: "Before you change anything on Jordan's PC…", o: [
          ["Check Jordan's files are backed up first", "ok", ["Desktop and Documents are synced. The photo app's folder isn't, so you copy it to the server first."]],
          ["Start deleting the biggest folders in their profile to free up space quickly", "risk", "Back up first. Those big folders might be someone's only copy."],
          ["Skip the backup: you're only changing settings and cleaning temp files, nothing personal", "order", "Back up before any change: “only tweaking” is how files get lost."],
          ["Ask Jordan to stay late while you work", "care", "Plan the work around the user, not the other way round."]] },
        { s: 2, o: [
          ["Check Task Manager and the free disk space", "ok", ["Disk: 100% busy. C: has 1.2 GB free of 256 GB.", "Startup tab: 14 apps, several marked “High” impact."]],
          ["Order more RAM: 8 GB isn't enough for Windows and all their apps anymore", "order", "You haven't checked anything yet. RAM might not be the bottleneck."],
          ["Run a free registry cleaner to remove leftover entries from the update", "risk", "Registry cleaners can break Windows and some carry malware. Not a standard fix."],
          ["Defragment the SSD overnight; files get fragmented after a big update", "order", "Don't defrag an SSD: it isn't needed and adds wear. Windows optimizes SSDs with TRIM instead."]] },
        { s: 3, o: [
          ["Open Settings > Storage to see what fills C:", "ok", ["Photo app cache: 60 GB. Temporary files (incl. Windows Update cleanup): 30 GB. Downloads: 20 GB.", "Theory confirmed: a nearly full disk plus heavy startup apps."]],
          ["Delete the biggest files in C:\\Windows\\WinSxS by hand to free space fast", "risk", "Deleting system files by hand can stop Windows from booting. Use Disk Cleanup or Storage Sense."],
          ["Escalate to the desktop team", "order", "You can test this yourself in a minute: look at what fills the disk."],
          ["Uninstall Jordan's apps without asking", "care", "Ask before removing things people use."]] },
        { s: 4, o: [
          ["With Jordan: Disk Cleanup, move the photo cache, trim startup apps", "ok", ["108 GB free now. Startup apps: 5.", "@Keep the chat app on startup, the rest can go."]],
          ["Disable every startup item in Task Manager, the antivirus included, for the fastest boot", "risk", "Turning off the antivirus at startup leaves the PC unprotected."],
          ["Empty the Downloads folder without looking; it's usually just old installers", "risk", "Downloads often has the only copy of something. Ask first."],
          ["Install a free “PC booster” app that cleans junk and speeds up startup automatically", "risk", "Most “boosters” are adware at best. Use the tools built into Windows."]] },
        { s: 5, o: [
          ["Restart and time the boot, confirm the update finished, then turn on Storage Sense", "ok", ["Starts in under a minute. Update history: installed successfully.", "Storage Sense will clean temp files automatically."]],
          ["Close the ticket now: Jordan says it already feels quicker since the cleanup", "order", "Verify: restart, time it, and make sure the stuck update actually finished."],
          ["Tell Jordan never to install anything", "care", "Explain what filled the disk and how to ask for software instead."],
          ["Turn off updates now that it works", "risk", "Updates are part of keeping it working. Leave them on."]] },
      ],
      notes: [
        ["Slow after update: C: 1.2 GB free (photo cache, temp), 14 startup apps. Backed up; Disk Cleanup, moved cache, trimmed startup with user. Boot <1 min, update OK, Storage Sense on."],
        ["Cleaned up PC.", "What did you remove? Next time someone will need to know."],
        ["PC slow. Ran registry cleaner and a PC booster, deleted Downloads and WinSxS files, disabled all startup items and Windows Update. Much faster. User filled the disk with junk.", "Judgy, and it records risky steps you didn't (and shouldn't) take. Say what used the space and what you did."]],
      cs: "Doing the startup cleanup together with Jordan (and keeping the chat app) means nothing they rely on vanishes.",
      why: "Windows needs free space for updates, temporary files and virtual memory. With 1.2 GB left, the update couldn't finish and the disk was busy all the time. Freeing space safely and trimming startup apps fixed both.",
      tip: "Slow PC: Task Manager (CPU, memory, disk), free space, startup apps (Task Manager > Startup), and whether updates finished. Use Disk Cleanup and Storage Sense. Don't defrag SSDs and don't turn off updates." },

    { id: "netprint", title: "Can't print to the network printer", exam: "a1", topic: "DHCP · printers", who: "Sam", dept: "Reception", col: "#4fb3a9",
      msg: "My print jobs just sit in the queue. The printer screen says Ready! Is it just me?",
      turns: [
        { s: 1, o: [
          ["Ask if it's only Sam and whether anything changed", "ok", ["@It's the whole floor. There was a power cut on Saturday, if that matters.", "Sam's queue: 6 jobs, “Error – Printing”. Printer status: Offline."]],
          ["Replace the printer's network card; a power cut can damage network hardware", "order", "Way too early: you haven't looked at the printer or the network yet."],
          ["Reinstall Windows on Sam's PC", "risk", "The whole floor has the problem, so it isn't Sam's PC, and a reinstall risks data."],
          ["Tell Sam to save to USB and walk documents to the copier from now on, and leave it there", "care", "A workaround is fine for now, but tell Sam you're fixing the real problem."]] },
        { s: 2, o: [
          ["Check power, paper and the network link light, then print its config page", "ok", ["On, ready, Ethernet link light on.", "Config page: IP address 192.168.1.87, assigned by DHCP."]],
          ["Factory-reset the printer", "risk", "It wipes its settings and won't tell you anything. Look at the config page first."],
          ["Restart the print spooler on every PC and close the ticket", "order", "That clears stuck jobs, but you don't know the cause yet. They'd get stuck again."],
          ["Research printer firmware bugs for an hour", "order", "Check the obvious first: the printer can tell you its own settings."]] },
        { s: 3, o: [
          ["Compare that with the port the PCs print to", "ok", ["The PCs send to 192.168.1.40. The printer is now .87.", "`ping 192.168.1.87` replies. Theory confirmed: DHCP gave it a new address after the power cut."]],
          ["Ping 192.168.1.40 from one PC and stop there", "order", "Half a test: you also need to know where the printer really is (.87) and that it answers."],
          ["Escalate to the network team now, since IP addresses are their responsibility", "order", "You can confirm this yourself first; then you'll know exactly what to ask them for."],
          ["Turn off the Windows firewall on Sam's PC to see if it's blocking print jobs", "risk", "Turning off a firewall to test weakens security, and the evidence points at the IP address."]] },
        { s: 4, o: [
          ["Reserve the old IP for the printer's MAC in DHCP; clear the queues", "ok", ["Reservation: printer MAC → 192.168.1.40 (back to the old address, so no PC changes needed).", "Printer restarted to pick it up. Spooler restarted; stuck jobs cleared."]],
          ["Just change the printer port on every PC to 192.168.1.87, the address it has now", "order", "It works today, but the address can change again after the next power cut. Fix the cause: keep its address fixed."],
          ["Set a static IP on the printer's own panel, picking a free address inside the DHCP pool", "risk", "DHCP could hand the same address to another device: an IP conflict. Use a reservation, or a static IP outside the pool."],
          ["Give Sam a USB printer and stop there", "order", "That helps one person, and the rest of the floor still can't print."]] },
        { s: 5, o: [
          ["Print from two PCs; restart the printer and check it keeps .40", "ok", ["Both PCs print. After a restart the printer still has .40.", "@It's printing! Thank you!"]],
          ["Assume it's fine: the light is green", "order", "The light was green when it was broken too. Print a real page."],
          ["Email the whole floor in capitals: PRINTER DOWN, DO NOT PRINT UNTIL FURTHER NOTICE", "care", "A short, calm note is enough: “Printing is fixed; resend anything stuck.”"],
          ["Turn off DHCP on the whole network", "risk", "Every other device needs DHCP. The reservation already fixed the printer."]] },
      ],
      notes: [
        ["No printing after Sat power cut: DHCP gave printer .87, PCs use .40. DHCP reservation (MAC → .40), queues cleared. Verified from 2 PCs and after a restart."],
        ["Printer offline, fixed.", "Why was it offline? Without the cause, the next tech starts from zero."],
        ["Printer offline after power cut. Changed the port to 192.168.1.87 on Sam's PC, restarted spooler; Sam can print. Other PCs probably need the same. IP may change again.", "That's not what you did, and it describes a half fix that will break again. Record the real cause and fix."]],
      cs: "Telling everyone it's fixed (and to resend stuck jobs) closes the loop for the whole floor.",
      why: "PCs print to a fixed address. After the power cut, DHCP gave the printer a different one, so jobs went to an address nobody had. A DHCP reservation ties an address to the printer's MAC address so it never changes.",
      tip: "Printers and servers need an address that doesn't change: a DHCP reservation (by MAC) or a static IP outside the DHCP pool. Stuck queue: restart the Print Spooler service. Common printer ports: TCP 9100 (raw) and 631 (IPP)." },

    { id: "email", title: "Phone stopped getting email", exam: "a1", topic: "Mobile email", who: "Alex", dept: "Legal", col: "#ff8fb1",
      msg: "My work email stopped arriving on my phone. The computer is fine. I'm out at a client tomorrow and really need it.",
      turns: [
        { s: 1, o: [
          ["Ask when it stopped and what changed (phone, update, password)", "ok", ["@Tuesday, I think. Oh, Windows made me change my password Tuesday morning."]],
          ["Back up the phone, then factory-reset it to clear out any corrupted mail settings", "risk", "Far too drastic, too early: a reset wipes the phone, and you have no idea of the cause yet."],
          ["Ask Alex to tell you their password so you can sign in on the phone and test it yourself", "risk", "Never ask for a user's password. Anyone asking for one should make you suspicious, including “IT”."],
          ["Tell Alex to just use the computer", "care", "Alex needs it on the road tomorrow. Help with what they actually need."]] },
        { s: 2, o: [
          ["Check the phone's internet and what the mail app says", "ok", ["Web pages load fine on Wi-Fi and mobile data.", "The mail app shows a red bar: “Cannot verify account”."]],
          ["Turn off MFA on Alex's account for a day to see if the prompts are blocking sync", "risk", "Multifactor authentication protects the account. Turning it off weakens security and isn't the cause."],
          ["Buy Alex a new phone", "order", "Nothing says the phone is broken. Look at the message it's showing."],
          ["Reboot the mail server tonight, since it may be holding a stuck session for the phone", "risk", "One user's phone doesn't justify taking email down for everyone."]] },
        { s: 3, o: [
          ["Compare the phone's server settings with IT's sheet, and check the sign-in log for failures", "ok", ["Server settings match.", "The sign-in log shows 30 failed attempts from Alex's phone since Tuesday: it's still using the old password. Theory confirmed."]],
          ["Delete the mail account from the phone to test", "order", "Test without breaking things first: the settings and sign-in log can confirm it."],
          ["Escalate to the email team with screenshots, since mail servers are their area", "order", "This is quick to confirm yourself; escalate only if it isn't the password."],
          ["Tell Alex it's probably Apple's / Google's fault", "care", "Guessing and blaming a vendor doesn't help. Check first."]] },
        { s: 4, o: [
          ["Have Alex enter the new password on the phone and approve MFA", "ok", ["Alex enters it and approves the prompt on their authenticator.", "The red bar disappears; 43 emails download."]],
          ["Ask Alex to read out the new password so you can type it in quickly for them", "risk", "Passwords are only for their owner. Let Alex type it."],
          ["Remove MFA so the phone signs in without prompts", "risk", "Fixing sync doesn't need weaker security."],
          ["Wipe and set up the phone from scratch", "risk", "Big data-loss risk for a one-field fix."]] },
        { s: 5, o: [
          ["Test mail both ways and calendar; explain why it broke", "ok", ["Test mail arrives on the phone; a reply arrives on the PC. Calendar is up to date.", "@Makes sense. I'll remember next time."]],
          ["Close it now: new mail arrived on the phone while you were standing there", "order", "Verify sending too, and calendar/contacts: “sync” is more than incoming mail."],
          ["Tell Alex not to change their password again until after the client visit", "risk", "Password changes are policy and good security. Explain the phone step instead."],
          ["Explain it precisely: “your ActiveSync EAS credential cache held a stale token”", "care", "Say it plainly: “your phone was still using your old password.”"]] },
      ],
      notes: [
        ["Phone mail stopped after Tue password change: phone had old password (30 failed sign-ins). User re-entered it + approved MFA. Send/receive and calendar verified."],
        ["Phone mail stopped after password change. Updated the phone's mail account with the user's new password (Spring2026!), approved MFA. Send/receive and calendar verified.", "Never write a password in a ticket: tickets are read by many people."],
        ["Phone issue.", "Too vague: what was wrong and what fixed it?"]],
      cs: "Plain words (“your phone still had your old password”) beat jargon every time.",
      why: "When a password changes, every device that saved the old one keeps failing. The phone kept retrying, which can even lock the account. Re-entering the new password (and approving MFA) fixed it.",
      tip: "Mobile email not syncing: check connectivity, then credentials and server settings (Exchange ActiveSync, or IMAP 993 / POP3 995 / SMTP 587 with TLS). Never ask for or write down a user's password." },

    { id: "wifi", title: "Wi-Fi drops in one room", exam: "a1", topic: "Wireless", who: "Lee", dept: "Operations", col: "#8e94f2",
      msg: "Video calls in meeting room 3 keep freezing and dropping. Everywhere else is fine.",
      turns: [
        { s: 1, o: [
          ["Ask which devices drop, and when", "ok", ["@Every laptop does it in that room. Mostly around lunchtime, actually."]],
          ["Replace every laptop's Wi-Fi card", "order", "All laptops fail in one room only, so it's the room, not the laptops."],
          ["Turn the access point's transmit power up to maximum so the signal reaches room 3", "order", "Guessing before identifying. And a louder AP doesn't make the laptops' replies louder."],
          ["Tell Lee to hold meetings somewhere else forever", "care", "A workaround, not a fix. Find the cause."]] },
        { s: 2, o: [
          ["Check where the nearest AP is and what's next to the room", "ok", ["The access point is at the far end of the hall.", "Room 3 shares a wall with the break room: two microwaves."]],
          ["Reset the access point to factory settings", "risk", "That drops everyone on it and loses its config, without telling you anything."],
          ["Push the latest Wi-Fi drivers to every laptop that uses the meeting room", "order", "Only one room has the problem, so look at the room first."],
          ["Blame the video call app", "order", "It's every laptop in one room, at lunch. The pattern points at the room."]] },
        { s: 3, o: [
          ["Use a Wi-Fi analyzer in the room while a microwave runs", "ok", ["2.4 GHz, channel 6: −74 dBm. When a microwave starts, noise jumps and the test laptop drops.", "5 GHz from the same AP: −70 dBm, steady. Theory confirmed: interference + distance."]],
          ["Ask Lee to keep a log of every drop for a week and send it to you", "care", "Don't hand your testing to the user. A few minutes with an analyzer does it."],
          ["Rename the SSID for room 3 so laptops reconnect with fresh settings", "order", "The name doesn't change the radio. Test what's happening on the air."],
          ["Switch the network from WPA3 to open (no password) for a day to see if drops stop", "risk", "Never weaken wireless security to troubleshoot signal."]] },
        { s: 4, o: [
          ["Steer laptops to 5 GHz now; request an access point near room 3 through change control", "ok", ["Band steering on: laptops prefer 5 GHz.", "Network team approves an extra access point in the hall by room 3 next week."]],
          ["Plug a spare home router into the room's wall port as an extra access point", "risk", "An unapproved access point is a rogue AP: a security hole. Go through the network team."],
          ["Ban the microwaves", "care", "People need lunch. Fix the network, not the people."],
          ["Move everyone to 2.4 GHz channel 3", "order", "Channel 3 overlaps 1 and 6 (only 1, 6 and 11 don't overlap), and the microwave still sits on 2.4 GHz."]] },
        { s: 5, o: [
          ["Run a video call in room 3 at lunchtime and watch it", "ok", ["30-minute call during lunch: no freezes, no drops.", "@Finally! The team says thanks."]],
          ["Run a quick test call in room 3 first thing in the morning, then close the ticket", "order", "The problem shows up at lunch. Verify under the same conditions."],
          ["Tell Lee it's fixed: the analyzer already showed a strong 5 GHz signal", "care", "Telling users it's fixed when it isn't costs their trust."],
          ["Restart the access point once more so the new settings are definitely applied", "order", "Restarting isn't verifying. Test the room the way people use it."]] },
      ],
      notes: [
        ["Room 3 drops at lunch: 2.4 GHz microwave interference + weak signal (−74 dBm). Band steering to 5 GHz; AP approved via change request. Lunchtime call verified."],
        ["Wi-Fi fixed.", "Which room, what cause, what change? Be specific."],
        ["Room 3 Wi-Fi drops. Found the microwaves cause it. Asked staff not to use them during meetings and turned AP power to max. Should be fine now, not tested yet.", "Not the fix you made, and untested. Record the cause, the real change, and how you verified it."]],
      cs: "Testing at lunchtime, when Lee's team actually has the problem, shows you took the ticket seriously.",
      why: "Microwave ovens leak a little energy around 2.4 GHz, the same band Wi-Fi uses, and the room was far from the access point. 5 GHz avoids the microwaves, and a closer access point fixes the distance.",
      tip: "2.4 GHz goes farther but is crowded: only channels 1, 6 and 11 don't overlap, and microwaves and Bluetooth share it. 5 GHz is faster with more channels but shorter range. Signal is in dBm: closer to 0 is stronger." },

    { id: "bsod", title: "Blue screen after new RAM", exam: "a1", topic: "Memory", who: "Robin", dept: "Finance", col: "#ff6b6b",
      msg: "My PC keeps crashing with a blue screen! I'm losing work every time.",
      turns: [
        { s: 1, o: [
          ["Ask what changed and when; note the stop code", "ok", ["@Only since Saturday, when someone from IT added a memory stick. It crashes a few times a day.", "Stop code: MEMORY_MANAGEMENT."]],
          ["Reinstall Windows", "risk", "A reinstall risks data and ignores the big clue: something changed on Saturday."],
          ["Update every driver on the PC, since blue screens usually come from bad drivers", "order", "You haven't even asked what changed yet."],
          ["Tell Robin a few crashes are normal after hardware changes while Windows settles in", "care", "They aren't, and Robin is losing work. Take it seriously."]] },
        { s: 2, o: [
          ["The crashes began with the new RAM: check its type and seating", "ok", ["Both sticks are DDR4 and latched in.", "Old stick: DDR4-3200, brand A. New stick: DDR4-2400, brand B."]],
          ["Replace the power supply, since the extra memory may draw more power than it can give", "order", "Nothing points at power. The timing points at the new memory."],
          ["Look for a virus", "order", "Possible in general, but question the obvious first: it started with the new RAM."],
          ["Undo the change without telling anyone", "care", "Keep Robin (and the tech who installed it) in the loop."]] },
        { s: 3, o: [
          ["Run Windows Memory Diagnostic, then run for a day with only the original stick", "ok", ["Memory Diagnostic: “Hardware problems were detected”.", "With only the original stick: a full day, no crash. Theory confirmed: the new module."]],
          ["Pull the RAM out with the PC running", "risk", "Never remove parts with power on. Shut down, unplug, and use an ESD strap."],
          ["Turn on XMP in the BIOS so both sticks run at the same rated speed", "risk", "Overclocking unstable memory makes crashes more likely, not less."],
          ["Escalate before testing", "order", "A memory test is quick and tells you exactly what to escalate, if needed."]] },
        { s: 4, o: [
          ["With ESD protection, remove the new stick; order a matched kit", "ok", ["The new stick goes back. A matched 2 × 8 GB kit is ordered.", "@So I'm on less memory for a few days? Fine, as long as it stops crashing!"]],
          ["Leave both sticks in and lower the RAM speed in the BIOS to see if that helps", "order", "The test says the new module is bad. Act on it."],
          ["Pull the new stick out quickly with bare hands; it's only a few seconds of contact", "risk", "Static you can't even feel can damage memory. Use an ESD strap or mat."],
          ["Tell Robin the colleague who installed it got it wrong and should fix it", "care", "Fix the problem, not the person. Keep it factual."]] },
        { s: 5, o: [
          ["Install the kit, check the full size shows, test, watch a week", "ok", ["System shows 16 GB. Memory Diagnostic: no problems found.", "A week later: no crashes."]],
          ["Close the ticket as soon as it boots and Windows shows the full 16 GB", "order", "Memory faults come and go. Test it, and watch it for a while."],
          ["Skip the memory test: the new kit is brand-new and covered by a lifetime warranty", "order", "New parts can be faulty too. That's exactly what happened."],
          ["Mark it fixed in the system; Robin will see the ticket status change", "care", "Let Robin know it's fixed and to save often until you've watched it a week."]] },
      ],
      notes: [
        ["BSOD MEMORY_MANAGEMENT since RAM added Sat. Mixed sticks; Memory Diagnostic errors; stable on original alone. Fitted matched 2×8 GB kit. 16 GB seen, test clean, no crashes in a week."],
        ["Blue screen fixed.", "Which stop code, what part, how verified? Write it down."],
        ["Blue screens. Updated all drivers and reinstalled Windows, still crashing, so pulled the new RAM. Colleague who installed it should be more careful. No crashes since this morning.", "That isn't what you did, it blames a colleague, and one quiet morning isn't verification."]],
      cs: "Robin was losing work: a quick, honest update (“it's the new memory; a few days on less RAM, then fixed”) lowers the stress.",
      why: "New RAM then crashes is a strong clue. Windows Memory Diagnostic found errors, and running with only the original stick was stable, which proves the new module. A matched kit that fits the motherboard's specs ends it.",
      tip: "After adding RAM: check it matches the motherboard (type, speed, capacity), reseat it, test with Windows Memory Diagnostic (mdsched.exe). Mixed speeds run at the slowest. Power off, unplug and use ESD protection." },

    { id: "locked", title: "Locked out, in a hurry", exam: "a2", topic: "Accounts · social engineering", who: "Morgan", dept: "Payroll", col: "#ffcf7a",
      msg: "I'm locked out!! Payroll has to go out by noon. Can you reset my password and send the new one here? I'm on Kim's PC.",
      turns: [
        { s: 1, o: [
          ["Verify identity per policy: check Morgan's badge at their desk", "ok", ["@Sure, desk 4B. Please hurry!", "You walk over: the badge photo matches. It really is Morgan."]],
          ["Reset it right away and paste a temporary password into this chat, since payroll is urgent", "risk", "You don't know who's typing on Kim's PC. Urgency + “send it here” is a classic social engineering pattern. Verify first."],
          ["Ask Kim, who's sitting right there, to confirm in chat that it's really Morgan", "risk", "Someone vouching in chat isn't verification. Follow the identity policy."],
          ["Tell Morgan to wait until tomorrow", "care", "Payroll is due at noon. Verify quickly instead of refusing."]] },
        { s: 2, o: [
          ["Check the account: locked or expired, and how many failures", "ok", ["Locked after 5 failed attempts, 8:00–8:02.", "@I changed my password Friday… and my Caps Lock was on this morning, I think."]],
          ["Turn off account lockout for Morgan's account so this can't block payroll again", "risk", "Lockout stops attackers from guessing passwords. Don't weaken it."],
          ["Assume a hacker and wipe the PC", "order", "Check the facts first. The sign-in log will tell you."],
          ["Remind Morgan right now, at length, why strong passwords and Caps Lock matter", "care", "Not now: Morgan is stressed and on a deadline. Help first."]] },
        { s: 3, o: [
          ["Check the sign-in log: which device and network the failed attempts came from", "ok", ["All 5 from Morgan's own PC, 8:00–8:02. No attempts from anywhere else.", "Theory confirmed: mistyped (Caps Lock), not an attack."]],
          ["Skip the log: Morgan already said Caps Lock was on, which explains it", "order", "Probably, but a lockout can also be an attack. The log takes 30 seconds."],
          ["Escalate to the security team immediately", "order", "Test first. If the attempts came from somewhere strange, then escalate."],
          ["Ask Morgan to tell you the old password", "risk", "Never ask for a password, old or new."]] },
        { s: 4, o: [
          ["Unlock it while Morgan is there (or set a temp password that must be changed)", "ok", ["Account unlocked. Morgan signs in with the Friday password, Caps Lock off.", "@Phew. Thank you!"]],
          ["Set the password to Payroll123 and write it on a sticky note", "risk", "Weak, guessable, and left on the desk. Use a temporary one that must be changed."],
          ["Turn off MFA so it's easier next time", "risk", "MFA is what stops a stolen password from being enough. Keep it."],
          ["Give Morgan your own login for today", "risk", "Never share accounts: everything would be logged as you."]] },
        { s: 5, o: [
          ["Have Morgan open payroll; mention the self-service reset portal", "ok", ["Payroll app opens, everything's there.", "@Self-service portal, noted. Thanks for coming over so fast."]],
          ["Walk away the moment it unlocks", "order", "Check Morgan can actually do the job (open payroll), not just sign in."],
          ["Tell Morgan they're a security risk", "care", "Caps Lock happens to everyone. Be kind and helpful."],
          ["Raise Morgan's lockout threshold to 50 failed tries so it doesn't happen again", "risk", "That makes password guessing much easier for attackers."]] },
      ],
      notes: [
        ["Morgan (Payroll) locked out: 5 failures from own PC (Caps Lock). Identity verified in person (badge). Unlocked; payroll access verified. Pointed to self-service reset."],
        ["Reset password to Payroll123.", "Never record a password in a ticket, and that one shouldn't exist at all."],
        ["Morgan locked out, urgent payroll. Reset to a temporary password and sent it by chat to Kim's PC so Morgan could get in fast. Disabled lockout to prevent repeats.", "Not what you did, and it records two security mistakes. Notes are an audit trail: who, why, how identity was verified."]],
      cs: "Staying calm and quick under pressure (noon deadline) while still following the verification policy.",
      why: "A lockout plus urgency plus “send it to this other account” is exactly what a social engineer would try. Verifying identity in person costs two minutes. The log showed the failures came from Morgan's own PC, so a simple unlock was safe.",
      tip: "Verify identity before any reset (per policy), never send passwords over chat or to another person, use temporary passwords that must be changed. Lockout policies stop brute-force attacks. Urgency and pressure are social engineering red flags." },

    { id: "malware", title: "Pop-ups everywhere", exam: "a2", topic: "Malware removal", who: "Taylor", dept: "Design", col: "#ff6b6b", track: "malware",
      msg: "Ads keep popping up, even with my browser closed, and my homepage changed to some weird search site?!",
      turns: [
        { s: 1, o: [
          ["Ask what they installed and check the symptoms yourself", "ok", ["@I installed a free font pack yesterday…", "Unknown “FontHelper” in startup, browser homepage hijacked, antivirus shows “turned off”. Symptoms verified."]],
          ["Run a full antivirus scan right away while it's still online, so it gets the newest definitions", "order", "Investigate first, then quarantine before remediating."],
          ["Format the drive", "risk", "That destroys the data and skips the method. Most infections can be removed."],
          ["Tell Taylor off for downloading free stuff, so they understand how serious this is", "care", "Educating comes at the end, kindly."]] },
        { s: 2, o: [
          ["Quarantine it: unplug the network cable and switch off its Wi-Fi adapter", "ok", ["The PC is off the network. The infection can't spread or phone home."]],
          ["Copy Taylor's files to the shared drive first", "risk", "That can spread the infection to the shared drive. Quarantine first."],
          ["Leave it online to download cleanup tools on it", "risk", "Infected and online can spread to other PCs. Bring tools on a clean USB stick."],
          ["Disable System Restore", "order", "Right idea, one step early: quarantine comes first."]] },
        { s: 3, o: [
          ["Disable System Restore in Windows, which deletes the old restore points", "ok", ["System Restore off; old restore points are deleted, so malware hiding in them can't come back."]],
          ["Roll back with System Restore and call it done", "risk", "Restore points can contain the malware, and a rollback isn't a scan."],
          ["Skip it: restore points are useful", "order", "Not when they may be infected. You'll make a clean one at the end."],
          ["Reconnect to the network", "risk", "Not until it's clean."]] },
        { s: 4, o: [
          ["Update anti-malware from a clean USB; scan and remove in Safe Mode", "ok", ["Definitions updated from a clean USB. Safe Mode full scan: 3 threats removed, including FontHelper.", "Browser reset to its default homepage."]],
          ["Find the files you don't recognise in C:\\Windows\\System32 and delete them by hand", "risk", "You can break Windows. Let the anti-malware tools do it."],
          ["Scan with old definitions in normal mode", "order", "Update first, and Safe Mode (or a preinstallation environment) stops malware from running during the scan."],
          ["Schedule weekly scans", "order", "Good, but that's the next step. Clean it first."]] },
        { s: 5, o: [
          ["Reconnect, then install Windows and app updates and schedule regular full scans", "ok", ["All updates installed. Full scan scheduled weekly; real-time protection on."]],
          ["Turn System Restore back on now that the Safe Mode scan came back clean", "order", "Close, but updates and scans come first, so the restore point you make is clean and current."],
          ["Leave updates for later", "risk", "Unpatched holes are how the next infection gets in."],
          ["Tell Taylor it's done", "order", "Not yet. Updates, scans, restore point and a chat still to go."]] },
        { s: 6, o: [
          ["Enable System Restore and create a restore point", "ok", ["System Restore on. New restore point: “Clean after malware removal”."]],
          ["Leave System Restore off for good, since restore points are where malware can hide", "order", "Turn it back on: future problems will need a clean restore point."],
          ["Create the restore point before the scan", "order", "Then the restore point would hold the malware. Now that it's clean is the right time."],
          ["Turn off the antivirus to speed things up", "risk", "Never. It just saved the day."]] },
        { s: 7, o: [
          ["Explain kindly how it got in and how to get fonts through IT", "ok", ["@Oops, I didn't know free stuff could do that. I'll ask you next time!"]],
          ["Walk Taylor through what happened at the team meeting so everyone learns from it", "care", "Shaming makes people hide mistakes. Keep it private and friendly."],
          ["Say nothing: it's fixed", "order", "Skipping education means it happens again next week."],
          ["Remove Taylor's admin rights and internet access for a month so it can't happen again", "care", "Too harsh and stops them working. Teach instead."]] },
      ],
      notes: [
        ["Adware from free font pack (FontHelper). Quarantined, System Restore off, defs via USB, Safe Mode scan removed 3. Updated, weekly scans, restore point, user educated."],
        ["Virus removed.", "Which one, how it got in, what was done: needed if it comes back or spreads."],
        ["Pop-ups from font pack. Scanned while online, deleted unknown files from System32, rolled back with System Restore. Seems OK. User downloaded junk again, should lose admin rights.", "Wrong steps, judgy, and nothing verified. Record what you actually did, in order."]],
      cs: "Educating without shaming means Taylor will ask before installing next time instead of hiding it.",
      why: "Malware removal has its own order: investigate and verify, quarantine, disable System Restore, remediate, schedule scans and update, re-enable System Restore with a clean restore point, then educate the user. Each step stops the malware spreading or coming back.",
      tip: "A+ Core 2 malware removal order: 1 investigate & verify symptoms, 2 quarantine, 3 disable System Restore, 4 remediate (update anti-malware, scan & remove; Safe Mode or a preinstallation environment), 5 schedule scans & run updates, 6 enable System Restore & create a restore point, 7 educate the end user." },

    { id: "hot", title: "Laptop running hot", exam: "a1", topic: "Cooling", who: "Chris", dept: "Facilities", col: "#4fb3a9",
      msg: "My laptop gets super hot, the fan screams, and it shut itself off twice today.",
      turns: [
        { s: 1, o: [
          ["Ask where and how it's used, and when it shuts off", "ok", ["@I use it in the workshop, it's pretty dusty. Often on my lap or on the sofa in the break room.", "@It shuts off when I have a lot of things open."]],
          ["Order a replacement CPU and heat sink, since the shutdowns point to a failing processor", "order", "CPUs rarely fail. Ask how it's used first."],
          ["Put it in the fridge to cool it", "risk", "Condensation means water inside electronics."],
          ["Tell Chris to use it less and shut it down whenever it starts getting warm", "care", "Chris needs the laptop for work. Find the cause."]] },
        { s: 1, q: "You may need to open it. First…", o: [
          ["Make sure Chris's files are backed up before you open anything", "ok", ["Files are synced to the server; you copy the Desktop folder too, which wasn't."]],
          ["Open it right away", "order", "Back up before making changes: a slip inside a laptop can cost the data."],
          ["Reimage it to rule out software", "risk", "Wipes everything, and the clues (dust, soft surfaces) point at airflow."],
          ["Take it without telling Chris for how long", "care", "Tell Chris what you're doing and when it'll be back."]] },
        { s: 2, o: [
          ["Check the vents, and Task Manager for runaway programs", "ok", ["Intake and exhaust vents are clogged with grey dust.", "Task Manager: CPU 8% at rest. No runaway program."]],
          ["Replace the fan; a screaming fan usually means its bearings are worn out", "order", "It might just be dusty. Look first."],
          ["Reinstall Windows to fix the heat", "risk", "Software can't unclog vents, and a reinstall risks data."],
          ["Undervolt the CPU with a free tool from a forum so it makes less heat", "risk", "Not a standard fix, and it can make the system unstable."]] },
        { s: 3, o: [
          ["Watch temperatures in the vendor's diagnostics under load", "ok", ["CPU climbs to 98 °C under light load, fan at full speed.", "Theory confirmed: airflow is blocked."]],
          ["Skip testing and start replacing the fan and heat sink, the usual suspects", "order", "Test the theory first. It's quick."],
          ["Blow hard into the vents yourself and see whether the temperature drops", "order", "Moisture from your breath and not much force. Use compressed air."],
          ["Escalate to the vendor's support line, since it's still under warranty", "order", "Cleaning is a normal help-desk task. Test and try it first."]] },
        { s: 4, o: [
          ["Power off, unplug, ESD strap; compressed air, fan held still", "ok", ["A lot of dust comes out. Still a bit hot under load, so per the vendor's guide you also replace the dried-out thermal paste."]],
          ["Vacuum the vents and fan with the office vacuum's crevice tool to pull the dust out", "risk", "Vacuums build up static that can damage parts. Compressed air (fan held still) is the usual method."],
          ["Let the fan spin freely while blasting air", "risk", "Spinning it too fast with air can damage the fan; hold it still."],
          ["Clean it while it's still on and charging", "risk", "Always power off and unplug first."]] },
        { s: 5, o: [
          ["Check temps under load; suggest a stand and regular cleaning", "ok", ["Under load: 72 °C, fan quiet. No shutdowns all afternoon.", "@A laptop stand for the workshop? Sounds good."]],
          ["Hand it back once it boots and the fan sounds quieter at the sign-in screen", "order", "Verify under load, where the problem showed up."],
          ["Tell Chris the workshop is too dirty for laptops and to leave it in the office", "care", "Offer a practical fix (stand, cleaning schedule) instead."],
          ["Tape mesh over the vents so workshop dust can't get inside again", "risk", "That's what caused the overheating."]] },
      ],
      notes: [
        ["Overheating + shutdowns: vents clogged (dusty workshop, soft surfaces). Backed up; compressed air, new thermal paste. Load temp 98 → 72 °C. Suggested stand, regular cleaning."],
        ["Cleaned laptop.", "Missing the temperatures before and after: that's your proof it worked."],
        ["Laptop overheating. Replaced fan and CPU, vacuumed vents, reinstalled Windows to clear bad processes. Runs cooler at sign-in screen. Told user the workshop is too dirty for laptops.", "That didn't happen, and it wasn't checked under load. Notes must match the work."]],
      cs: "A practical suggestion (a laptop stand, regular cleaning) helps Chris without telling them off.",
      why: "Dust blocks the vents and soft surfaces cover them, so heat can't leave. The laptop shuts itself off to protect the CPU. Cleaning with compressed air (fan held still) and fresh thermal paste brought the temperature down.",
      tip: "Overheating: check vents and fan for dust and blocked airflow first, then thermal paste between CPU and heat sink. Clean with compressed air, power off and unplugged, fan held still. Thermal shutdowns are the system protecting itself." },

    { id: "creds", title: "Shared drive gone after password change", exam: "a2", topic: "Credential Manager", who: "Pat", dept: "Marketing", col: "#8e94f2",
      msg: "Since I changed my password this morning, my P: drive has a red X and keeps asking for a password. Nothing I type works.",
      turns: [
        { s: 1, o: [
          ["Ask when it started, what changed, and whether anything else is broken", "ok", ["@Right after I changed my password at 9. Email and everything else work fine.", "P: → \\\\fs01\\marketing shows a red X: “Disconnected”."]],
          ["Rebuild Pat's Windows profile", "risk", "A profile rebuild is slow and risky, and nothing points there yet."],
          ["Reboot the file server", "risk", "That cuts everyone off for one user's problem."],
          ["Tell Pat to type the password more carefully", "care", "Pat says nothing works. Help find out why."]] },
        { s: 2, o: [
          ["Check if teammates can open the share", "ok", ["Teammates open \\\\fs01\\marketing fine. The server is up.", "So it's something about Pat's connection: likely the saved credentials."]],
          ["Give Pat Full Control on the whole server", "risk", "Least privilege: only give the access people need. Permissions aren't the problem anyway."],
          ["Remove and rejoin Pat's PC to the domain to refresh its trust with the file server", "order", "Email and sign-in work, so the domain is fine. Check the obvious."],
          ["Escalate to the server team, since the drive lives on their file server", "order", "Others can reach it, so the server is fine. Keep looking on Pat's side."]] },
        { s: 3, o: [
          ["Open Credential Manager > Windows Credentials and look for saved fs01 entries", "ok", ["A saved entry for “fs01” with Pat's username, saved last year: it still has the old password.", "Opening \\\\fs01\\marketing and typing the new password works. Theory confirmed."]],
          ["Map the drive with your own admin account", "risk", "Then everything Pat does is logged as you, with admin rights. Never share credentials."],
          ["Delete the Marketing share and recreate it", "risk", "Big risk to everyone's files for a one-user problem."],
          ["Ask Pat for the new password so you can test the share from your own PC", "risk", "Never ask for a password. Let Pat type it."]] },
        { s: 4, o: [
          ["Update the saved fs01 credential, then reconnect P:", "ok", ["Pat types the new password into the saved entry.", "P: reconnects. The red X is gone."]],
          ["Set Pat's password to never expire so the saved credential stays valid", "risk", "Password policy is there for security. Fix the saved credential instead."],
          ["Copy the Marketing folder to Pat's desktop so they can keep working offline for now", "order", "Then Pat works on copies that get out of sync. Fix the connection."],
          ["Turn off the account lockout policy", "risk", "Lockout protects against guessing. Old saved passwords retrying is a reason to fix the entry, not weaken policy."]] },
        { s: 5, o: [
          ["Sign out and in, check P: reconnects; explain saved passwords", "ok", ["After sign-in P: connects on its own and opens files.", "@Got it, Credential Manager. Thanks!"]],
          ["Close the ticket: P: is connected and Pat just opened a file from it", "order", "Check it reconnects after a sign-out too: that's when the old password would bite."],
          ["Tell Pat never to save passwords", "care", "Saved credentials are normal; explain how to update them."],
          ["Reset Pat's password again to be safe", "order", "That starts the same problem over."]] },
      ],
      notes: [
        ["P: (\\\\fs01\\marketing) failed after password change: old password saved in Credential Manager. Updated entry, reconnected. Verified after sign-out/in. Explained to user."],
        ["Drive fixed. New password: Marketing#9", "Never put a password in a ticket."],
        ["P: drive down after password change. Rejoined PC to domain, gave Pat Full Control on fs01, remapped with my admin account. Works now. Told user to stop saving passwords.", "Not what you did, and it records risky changes (Full Control, an admin account). The next tech needs “Credential Manager”."]],
      cs: "One sentence of explanation (“Windows saved your old password for that server”) means Pat can fix it alone next time.",
      why: "Windows had saved a separate password for the file server in Credential Manager. After the password change it kept sending the old one, so the drive failed even though Pat's sign-in worked. Updating that saved entry fixed it.",
      tip: "Saved network passwords live in Credential Manager (Control Panel > Credential Manager > Windows Credentials). After a password change, update or remove old entries, or they can keep failing and even lock the account." },
  ];

  // ---------------------------------------------------------------- styles
  const CSS = `
  .hd { max-width: 1180px; margin: 0 auto; }
  .hd-top { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin-bottom: 12px; }
  .hd-top h2 { margin: 0; }
  .hd-prog { margin-left: auto; display: flex; align-items: center; gap: 10px; font-size: 13px; color: var(--muted); }
  .hd-prog .bar { width: 140px; height: 6px; border-radius: 4px; background: rgba(255,255,255,.07); overflow: hidden; }
  .hd-prog .bar i { display: block; height: 100%; background: linear-gradient(90deg, var(--jade), #7ed6c9); transition: width .5s; }
  .hd-pick { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 8px; margin-bottom: 14px; }
  .hd-tk { position: relative; min-width: 0; text-align: left; padding: 8px 11px; border-radius: 14px; cursor: pointer; font: inherit; color: inherit;
           background: rgba(29, 40, 86, .55); border: 1px solid var(--line); transition: background .15s, border-color .2s, transform .15s; }
  .hd-tk:hover { background: rgba(29, 40, 86, .9); transform: translateY(-1px); }
  .hd-tk.on { border-color: rgba(255, 207, 122, .55); background: rgba(255, 207, 122, .08); }
  .hd-tk .nm { font-weight: 600; font-size: 13px; line-height: 1.25; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 30px; }
  .hd-tk .tp { font-size: 11.5px; color: var(--muted); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .hd-tk .hd-sc { position: absolute; top: 7px; right: 9px; font-size: 11px; font-weight: 700; color: var(--jade); }
  .hd-work { display: grid; grid-template-columns: minmax(0, 1fr) 330px; gap: 16px; align-items: start; }
  .hd-main { padding: 0; overflow: hidden; }
  .hd-head { display: flex; align-items: center; gap: 12px; padding: 14px 18px; border-bottom: 1px solid var(--line); background: rgba(255,255,255,.03); }
  .hd-av { flex: none; width: 38px; height: 38px; border-radius: 50%; display: grid; place-items: center; font-weight: 700; color: #141a3a; }
  .hd-head .who { font-weight: 600; } .hd-head .sub { font-size: 12.5px; color: var(--muted); }
  .hd-head .no { margin-left: auto; font: 600 12px ui-monospace, monospace; color: var(--muted); text-align: right; }
  .hd-chat { max-height: 330px; min-height: 160px; overflow: auto; padding: 14px 18px; display: flex; flex-direction: column; gap: 8px; }
  .hd-b { max-width: 82%; padding: 9px 13px; border-radius: 16px; font-size: 14px; line-height: 1.45; }
  .hd-b.u { align-self: flex-start; background: rgba(255,255,255,.07); border-bottom-left-radius: 5px; }
  .hd-b.u small { display: block; font-size: 11px; color: var(--muted); margin-bottom: 2px; }
  .hd-b.me { align-self: flex-end; background: rgba(255, 207, 122, .14); border: 1px solid rgba(255, 207, 122, .28); border-bottom-right-radius: 5px; }
  .hd-b.see { align-self: center; max-width: 92%; background: rgba(142, 148, 242, .1); border: 1px dashed rgba(142, 148, 242, .35); color: #cfd2ff; font-size: 13px; border-radius: 12px; }
  .hd-b.see::before { content: "🔍 "; }
  .hd-b.step { align-self: center; background: none; padding: 2px 0; font-size: 11.5px; letter-spacing: 1.1px; text-transform: uppercase; color: var(--jade); }
  .hd-anim .hd-b.new { animation: hdrise .35s cubic-bezier(.2,.8,.2,1) both; }
  @keyframes hdrise { from { opacity: 0; transform: translateY(8px); } }
  .hd-act { padding: 14px 18px 18px; border-top: 1px solid var(--line); }
  .hd-q { font-size: 14px; margin: 0 0 6px; } .hd-q b { color: var(--sun); }
  .hd-opt { display: grid; grid-template-columns: auto 1fr; gap: 9px; width: 100%; text-align: left; margin: 6px 0; padding: 9px 11px; border-radius: 12px; cursor: pointer; font: inherit;
            background: rgba(255,255,255,.05); border: 1px solid var(--line); color: var(--ink); font-size: 13.5px; line-height: 1.4; transition: background .15s, border-color .2s; }
  .hd-opt:hover { background: rgba(255,255,255,.09); }
  .hd-opt kbd { align-self: start; margin-top: 1px; }
  .hd-opt.wrong { border-color: rgba(255, 107, 107, .45); color: #ffc2c2; opacity: .7; cursor: default; }
  .hd-opt.hd-hinted { border-color: rgba(255, 207, 122, .6); box-shadow: 0 0 0 3px rgba(255, 207, 122, .12); }
  .hd-anim .hd-opt.wrong.just { animation: hdshake .35s; }
  @keyframes hdshake { 20%, 60% { transform: translateX(-5px); } 40%, 80% { transform: translateX(5px); } }
  .hd-coach { margin-top: 8px; padding: 10px 12px; border-radius: 12px; font-size: 13.5px; background: rgba(255, 107, 107, .08); border: 1px solid rgba(255, 107, 107, .28); }
  .hd-coach b { color: #ffb0b0; }
  .hd-coach.tipc { background: rgba(255, 207, 122, .07); border-color: rgba(255, 207, 122, .3); } .hd-coach.tipc b { color: var(--sun); }
  .hd-side { display: flex; flex-direction: column; gap: 12px; position: sticky; top: 84px; }
  .hd-track { padding: 14px 16px; }
  .hd-track h4, .hd-card h4 { margin: 0 0 8px; font-size: 11.5px; letter-spacing: 1.2px; text-transform: uppercase; color: var(--muted); font-weight: 600; }
  .hd-steps { list-style: none; margin: 0; padding: 0; }
  .hd-steps li { display: grid; grid-template-columns: 24px 1fr; gap: 8px; padding: 5px 0; color: var(--muted); font-size: 13.5px; }
  .hd-steps .n { width: 22px; height: 22px; border-radius: 50%; display: grid; place-items: center; font: 700 11px ui-monospace, monospace; border: 1px solid rgba(255,255,255,.18); }
  .hd-steps li.done { color: var(--ink); } .hd-steps li.done .n { background: var(--jade); border-color: transparent; color: #0b1129; }
  .hd-steps li.now { color: var(--ink); } .hd-steps li.now .n { border-color: var(--sun); color: var(--sun); box-shadow: 0 0 0 3px rgba(255, 207, 122, .14); }
  .hd-steps li .d { display: block; font-size: 12px; color: var(--muted); margin-top: 2px; }
  .hd-anim .hd-steps li.pop .n { animation: hdpop .45s cubic-bezier(.2,1.6,.4,1); }
  @keyframes hdpop { from { transform: scale(0); } }
  .hd-card { padding: 14px 16px; font-size: 13px; }
  .hd-card .big { font-size: 30px; font-weight: 700; color: var(--sun); line-height: 1.1; }
  .hd-card .row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-top: 8px; }
  .hd-card .btn { padding: 7px 13px; font-size: 13px; }
  .hd-rep { position: relative; }
  .hd-rep h3 { margin: 0 0 8px; }
  .hd-rc { display: grid; grid-template-columns: 22px 1fr; gap: 4px 8px; font-size: 13.5px; margin: 8px 0 4px; }
  .hd-rc .y { color: var(--jade); font-weight: 700; } .hd-rc .i { color: var(--sun); } .hd-rc .x { color: var(--coral); font-weight: 700; }
  .hd-why { position: relative; margin-top: 12px; padding: 12px 14px; border-radius: 14px; background: rgba(79, 179, 169, .08); border: 1px solid rgba(79, 179, 169, .25); font-size: 14px; }
  .hd-why h5 { margin: 0 0 4px; font-size: 11.5px; letter-spacing: 1.2px; text-transform: uppercase; color: var(--jade); }
  .hd-why .tipx { margin-top: 10px; padding-top: 9px; border-top: 1px dashed rgba(255,255,255,.12); }
  .hd-why .tipx h5 { color: var(--sun); }
  .hd-stamp { position: absolute; top: -4px; right: 0; padding: 3px 10px; border: 2px solid var(--jade); border-radius: 8px; color: var(--jade); font: 800 13px ui-monospace, monospace; letter-spacing: 2px; transform: rotate(-8deg); }
  .hd-anim .hd-stamp { animation: hdstamp .4s cubic-bezier(.2,1.4,.4,1) both; }
  @keyframes hdstamp { from { opacity: 0; transform: rotate(-8deg) scale(2); } }
  .hd-note { margin-top: 10px; padding: 9px 12px; border-radius: 12px; background: rgba(0,0,0,.22); font: 12.5px/1.5 ui-monospace, Consolas, monospace; color: #cdd3f3; }
  .hd-stars { color: var(--sun); letter-spacing: 2px; } .hd-stars .off { color: rgba(255,255,255,.18); }
  @media (max-width: 980px) { .hd-work { grid-template-columns: 1fr; } .hd-pick { grid-template-columns: repeat(3, minmax(0, 1fr)); } .hd-side { position: static; } }
  @media (max-width: 560px) { .hd-pick { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
  `;
  function addCss() {
    if (document.getElementById("hd-css")) return;
    const s = document.createElement("style"); s.id = "hd-css"; s.textContent = CSS; document.head.appendChild(s);
  }

  // ---------------------------------------------------------------- state
  let S = null;  // the open ticket: { ctx, t, ti, phase, opts, wrongs, last, hinted, log, lit, miss, hints, noteBad, score }
  const steps = (t) => (t.track === "malware" ? MALWARE : STEPS);
  const shuffle = (a) => { a = a.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; };
  const best = (ctx) => ctx.store.get("best", {}) || {};
  const stars = (sc) => (sc >= 90 ? 3 : sc >= 70 ? 2 : 1);
  const starHtml = (n) => `<span class="hd-stars">${"★".repeat(n)}<span class="off">${"★".repeat(3 - n)}</span></span>`;
  const initials = (n) => n.slice(0, 1).toUpperCase();

  function openTicket(ctx, id) {
    const t = T.find((x) => x.id === id) || T[0];
    ctx.store.set("cur", t.id);
    S = { ctx, t, ti: 0, phase: "turn", wrongs: new Set(), last: null, hinted: false, log: [], lit: new Set(), pop: -1,
          miss: { order: 0, risk: 0, care: 0 }, hints: 0, noteBad: 0, fresh: 0 };
    S.log.push({ k: "u", text: t.msg });
    enterTurn();
    draw();
  }
  function enterTurn() {
    S.wrongs = new Set(); S.last = null; S.hinted = false;
    if (S.ti < S.t.turns.length) {
      S.phase = "turn";
      const turn = S.t.turns[S.ti];
      S.opts = shuffle(turn.o.map((o, i) => ({ o, right: i === 0 })));
    } else {
      S.phase = "notes";
      S.opts = shuffle(S.t.notes.map((n, i) => ({ o: n, right: i === 0 })));
    }
  }
  const curStep = () => (S.phase === "turn" ? S.t.turns[S.ti].s : S.t.track === "malware" ? 0 : 6);

  function pick(i) {
    if (!S || S.phase === "done") return;
    const op = S.opts[i]; if (!op || S.wrongs.has(i)) return;
    if (!op.right) {
      S.wrongs.add(i);
      if (S.phase === "notes") { S.noteBad++; S.last = { i, kind: "doc", text: op.o[1] }; }
      else { const kind = op.o[1]; S.miss[kind] = (S.miss[kind] || 0) + 1; S.last = { i, kind, text: op.o[2] }; }
      S.shake = i;
      draw(); return;
    }
    const before = S.log.length;
    if (S.phase === "notes") {
      if (S.t.track !== "malware") { S.lit.add(6); S.pop = 6; }
      S.note = op.o[0];
      S.phase = "done";
      S.score = scoreNow();
      const b = best(S.ctx); const prev = b[S.t.id];
      S.newBest = prev == null || S.score > prev;
      if (S.newBest) { b[S.t.id] = S.score; S.ctx.store.set("best", b); }
      S.fresh = S.log.length - before;
      draw(); return;
    }
    const turn = S.t.turns[S.ti];
    S.log.push({ k: "me", text: op.o[0] });
    for (const line of op.o[2]) S.log.push(line.startsWith("@") ? { k: "u", text: line.slice(1) } : { k: "see", text: line });
    const nextS = S.t.turns[S.ti + 1]?.s;
    if (nextS !== turn.s) { S.lit.add(turn.s); S.pop = turn.s; } else S.pop = -1;
    S.ti++;
    S.fresh = S.log.length - before;
    enterTurn();
    draw();
  }
  function hint() {
    if (!S || S.phase === "done" || S.hinted) return;
    S.hinted = true; S.hints++; S.last = null;
    draw();
  }
  function scoreNow() {
    const m = S.miss;
    return Math.max(0, 100 - m.order * COST.order - m.risk * COST.risk - m.care * COST.care - S.hints * 5 - S.noteBad * 10);
  }
  function nextTicket() {
    if (!S) return;
    const b = best(S.ctx);
    const i = T.indexOf(S.t);
    const nxt = T.slice(i + 1).concat(T.slice(0, i + 1)).find((x) => b[x.id] == null) || T[(i + 1) % T.length];
    openTicket(S.ctx, nxt.id);
  }
  function reportRemote() {
    if (!S) return;
    const st = steps(S.t);
    const n = curStep();
    const where = S.phase === "done" ? `closed · ${S.score} pts` : S.phase === "notes" ? "write the ticket notes" : `step ${n}: ${st[n - 1][0]}`;
    S.ctx.report(`Help desk #${T.indexOf(S.t) + 1} ${S.t.title} · ${where}`);
  }

  // ---------------------------------------------------------------- drawing
  function draw() {
    const { ctx, t } = S; const esc = ctx.esc; const el = ctx.el;
    if (!el || !el.isConnected) return;
    const b = best(ctx);
    const closed = T.filter((x) => b[x.id] != null).length;
    const st = steps(t);
    const now = curStep();
    const chat = S.log.map((m, i) => {
      const nw = i >= S.log.length - S.fresh ? " new" : "";
      if (m.k === "u") return `<div class="hd-b u${nw}"><small>${esc(t.who)}</small>${esc(m.text)}</div>`;
      if (m.k === "me") return `<div class="hd-b me${nw}">${esc(m.text)}</div>`;
      return `<div class="hd-b see${nw}">${esc(m.text)}</div>`;
    }).join("");

    let act = "";
    if (S.phase === "turn" || S.phase === "notes") {
      const turn = S.phase === "turn" ? t.turns[S.ti] : null;
      const q = S.phase === "notes"
        ? `<b>Document</b> · Pick the ticket notes to save.`
        : `<b>${t.track === "malware" ? "Malware step" : "Step"} ${turn.s} · ${esc(st[turn.s - 1][0])}</b> · ${esc(turn.q || "What do you do?")}`;
      act = `<p class="hd-q">${q}</p>` + S.opts.map((op, i) => {
        const cls = (S.wrongs.has(i) ? " wrong" : "") + (S.shake === i ? " just" : "") + (S.hinted && op.right ? " hd-hinted" : "");
        return `<button class="hd-opt${cls}" data-i="${i}"><kbd>${i + 1}</kbd><span>${esc(op.o[0])}</span></button>`;
      }).join("");
      if (S.last) {
        const lab = S.last.kind === "doc" ? "Weak notes" : LABEL[S.last.kind];
        const cost = S.last.kind === "doc" ? 10 : COST[S.last.kind];
        act += `<div class="hd-coach"><b>${esc(lab)} (−${cost})</b> · ${esc(S.last.text)}</div>`;
      } else if (S.hinted) {
        const d = S.phase === "notes" ? "Good notes say the cause, what you did and how you checked it, so the next tech can follow along." : st[now - 1][1];
        act += `<div class="hd-coach tipc"><b>Hint (−5)</b> · ${esc(d)} The highlighted action fits this step.</div>`;
      }
    } else {
      const m = S.miss; const sc = S.score; const n = stars(sc);
      const row = (ok, txt) => `<span class="${ok ? "y" : "x"}">${ok ? "✓" : "✗"}</span><span>${txt}</span>`;
      act = `<div class="hd-rep">
        <div class="hd-stamp">CLOSED</div>
        <h3>Report card · ${starHtml(n)} <span style="color:var(--sun)">${sc}</span><span class="muted" style="font-size:13px;font-weight:400"> / 100${S.newBest ? " · new best" : ""}</span></h3>
        <div class="hd-rc">
          ${row(!m.order, m.order ? `Jumped ahead ${m.order}× (method steps out of order)` : "Followed the steps in order")}
          ${row(!m.risk, m.risk ? `${m.risk} risky move${m.risk > 1 ? "s" : ""} (data or security)` : "Kept data and security safe")}
          ${row(!m.care, m.care ? `Customer care slipped ${m.care}×` : `Good customer care: ${esc(t.cs)}`)}
          ${row(!S.noteBad, S.noteBad ? "Notes needed a second try" : "Clear ticket notes on the first try")}
          ${S.hints ? `<span class="i">💡</span><span>${S.hints} hint${S.hints > 1 ? "s" : ""} used (−${S.hints * 5})</span>` : ""}
        </div>
        ${m.care ? `<div class="muted" style="font-size:13px">Customer service: ${esc(t.cs)}</div>` : ""}
        <div class="hd-note">📝 ${esc(S.note)}</div>
        <div class="hd-why"><h5>Why</h5>${esc(t.why)}<div class="tipx"><h5>Exam tip · ${EXAM[t.exam]}</h5>${esc(t.tip)}</div></div>
        <div class="row" style="display:flex;gap:8px;margin-top:12px;flex-wrap:wrap">
          <button class="btn" id="hd-next">Next ticket <kbd>Enter</kbd></button>
          <button class="btn ghost" id="hd-again">Replay this one</button>
        </div></div>`;
    }

    const track = st.map((s, i) => {
      const k = i + 1;
      const cls = S.lit.has(k) ? "done" : k === now && S.phase !== "done" ? "now" : "";
      return `<li class="${cls}${S.pop === k ? " pop" : ""}"><span class="n">${S.lit.has(k) ? "✓" : k}</span><span>${esc(s[0])}${k === now && S.phase !== "done" ? `<span class="d">${esc(s[1])}</span>` : ""}</span></li>`;
    }).join("");
    const extra = t.track === "malware"
      ? `<div class="muted" style="font-size:12px;margin-top:6px">CompTIA's malware-removal order (A+ Core 2). Then document the ticket as always.</div>`
      : `<div class="muted" style="font-size:12px;margin-top:6px">CompTIA A+ uses these 6 steps (older material said 7). Network+ counts 7: it splits “plan” from “implement or escalate”.</div>`;

    el.innerHTML = `<div class="hd${ctx.calm ? "" : " hd-anim"}">
      <div class="hd-top"><span style="font-size:30px">🎧</span><div><h2>Help desk</h2>
        <div class="muted" style="font-size:13.5px">Coworkers message you with real problems. Work them the CompTIA way, one step at a time.</div></div>
        <div class="hd-prog">${closed}/${T.length} closed<span class="bar"><i style="width:${(closed / T.length) * 100}%"></i></span></div></div>
      <div class="hd-pick">${T.map((x, i) => `<button class="hd-tk${x === t ? " on" : ""}" data-t="${x.id}" title="${esc(x.title)}">
        ${b[x.id] != null ? `<span class="hd-sc">${"★".repeat(stars(b[x.id]))}</span>` : ""}
        <div class="nm">${i + 1}. ${esc(x.title)}</div><div class="tp">${EXAM[x.exam]} · ${esc(x.topic)}</div></button>`).join("")}</div>
      <div class="hd-work">
        <div class="glass hd-main">
          <div class="hd-head"><div class="hd-av" style="background:${t.col}">${esc(initials(t.who))}</div>
            <div><div class="who">${esc(t.who)} · ${esc(t.dept)}</div><div class="sub">${esc(t.title)} · ${EXAM[t.exam]}</div></div>
            <div class="no">TICKET #${String(4100 + T.indexOf(t) * 7).padStart(5, "0")}<br>${S.phase === "done" ? "closed" : "open"}</div></div>
          <div class="hd-chat">${chat}</div>
          <div class="hd-act">${act}</div>
        </div>
        <div class="hd-side">
          <div class="glass hd-track"><h4>${t.track === "malware" ? "Malware removal" : "Troubleshooting method"}</h4><ol class="hd-steps">${track}</ol>${extra}</div>
          <div class="glass hd-card"><h4>This ticket</h4>
            <div class="big">${S.phase === "done" ? S.score : scoreNow()}</div><div class="muted">points left of 100${b[t.id] != null ? ` · best ${b[t.id]}` : ""}</div>
            <div class="row"><button class="btn ghost" id="hd-hint" ${S.phase === "done" || S.hinted ? "disabled" : ""}>Hint (−5) <kbd>H</kbd></button></div>
            <div class="muted" style="margin-top:10px;font-size:12px">Keys: <kbd>1</kbd>–<kbd>9</kbd> pick · <kbd>H</kbd> hint · <kbd>Enter</kbd> next ticket. Phone remote: grade buttons pick 1–4, Show = hint.<br>Wrong for the moment: −10 · risky: −15 · bad notes: −10.</div>
          </div>
        </div>
      </div></div>`;

    el.querySelectorAll(".hd-opt").forEach((bt) => bt.onclick = () => pick(+bt.dataset.i));
    el.querySelectorAll(".hd-tk").forEach((bt) => bt.onclick = () => openTicket(ctx, bt.dataset.t));
    const q = (s) => el.querySelector(s);
    if (q("#hd-hint")) q("#hd-hint").onclick = hint;
    if (q("#hd-next")) q("#hd-next").onclick = nextTicket;
    if (q("#hd-again")) q("#hd-again").onclick = () => openTicket(ctx, t.id);
    const c = q(".hd-chat"); if (c) c.scrollTop = c.scrollHeight;
    if (S.phase === "done") q(".hd-rep")?.scrollIntoView({ block: "nearest", behavior: ctx.calm ? "auto" : "smooth" });
    S.fresh = 0; S.pop = -1; S.shake = -1; // new bubbles, the step pop and the shake only animate once
    reportRemote();
  }

  Lab.register({
    id: "helpdesk", title: "Help desk", icon: "🎧", exams: ["a1", "a2"],
    blurb: "Coworkers message you with real problems. Solve 12 tickets with CompTIA's troubleshooting steps: ask, test, fix, verify, document.",
    render(ctx) {
      addCss();
      const b = best(ctx);
      const cur = ctx.store.get("cur", null);
      const start = T.find((x) => x.id === cur) || T.find((x) => b[x.id] == null) || T[0];
      openTicket(ctx, start.id);
      // The page reports its own state right after render; send ours just after so the phone shows the ticket.
      setTimeout(reportRemote, 60);
    },
    progress(ctx) {
      const b = best(ctx);
      return `${T.filter((x) => b[x.id] != null).length}/${T.length} closed`;
    },
    key(e) {
      if (!S || !S.ctx.el || !S.ctx.el.isConnected || e.ctrlKey || e.metaKey || e.altKey) return;
      if (/^[1-9]$/.test(e.key)) { e.preventDefault(); if (S.phase === "done") return; pick(+e.key - 1); return; }
      const k = e.key.toLowerCase();
      if (k === "h") { e.preventDefault(); hint(); return; }
      if (e.key === "Enter" && S.phase === "done") { e.preventDefault(); nextTicket(); }
    },
    remote(cmd, value) {
      if (!S || !S.ctx.el || !S.ctx.el.isConnected) return;
      if (cmd === "show") hint();
      else if (cmd === "grade") { if (S.phase === "done") nextTicket(); else if (value >= 1 && value <= 4) pick(value - 1); }
    },
  });
})();
