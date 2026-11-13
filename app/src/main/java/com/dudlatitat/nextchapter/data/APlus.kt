package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import kotlin.math.max

/** Where one A+ card is in its review schedule (the card's text lives in [APlus], not in saved data). */
@Serializable
data class Review(
    val id: String,
    val firstSeen: Long? = null,
    val due: Long = 0,
    val interval: Int = 0,
    val ease: Double = 2.5,
    val reps: Int = 0,
    val lapses: Int = 0,
    val last: Long? = null,
)

/**
 * CompTIA A+ study cards, starting with Core 1 (220-1201). Written for this app from CompTIA's
 * public exam objectives: short questions you can answer in your head on a phone. Reviews use
 * the same scheduler as the Chinese cards ([Srs]) but are saved separately, so they never mix
 * into the Chinese practice queue.
 */
object APlus {
    data class Q(val id: String, val domain: Int, val q: String, val a: String)

    /** Core 1 domains and their share of the exam. */
    val domains = linkedMapOf(
        1 to ("Mobile devices" to 13),
        2 to ("Networking" to 23),
        3 to ("Hardware" to 25),
        4 to ("Virtualization & cloud" to 11),
        5 to ("Troubleshooting" to 28),
    )

    private fun q(domain: Int, key: String, q: String, a: String) = Q("c1:$key", domain, q, a)

    val core1: List<Q> = listOf(
        // ---- 2 Networking: ports ----
        q(2, "port-ftp", "Port for FTP?", "20 and 21 (TCP). 21 is control, 20 is data."),
        q(2, "port-ssh", "Port for SSH (and SFTP/SCP)?", "22 (TCP)"),
        q(2, "port-telnet", "Port for Telnet? Why avoid it?", "23 (TCP). It sends everything, passwords included, in clear text: use SSH."),
        q(2, "port-smtp", "Port for SMTP (sending email)?", "25 (TCP)"),
        q(2, "port-dns", "Port for DNS?", "53 (UDP for most lookups, TCP for zone transfers)"),
        q(2, "port-dhcp", "Ports for DHCP?", "67 (server) and 68 (client), UDP"),
        q(2, "port-http", "Port for HTTP?", "80 (TCP)"),
        q(2, "port-pop3", "Port for POP3 (download mail)?", "110 (TCP)"),
        q(2, "port-netbios", "Ports for NetBIOS?", "137–139"),
        q(2, "port-imap", "Port for IMAP (mail kept on the server)?", "143 (TCP)"),
        q(2, "port-snmp", "Ports for SNMP?", "161 (queries) and 162 (traps), UDP"),
        q(2, "port-ldap", "Port for LDAP (directory lookups)?", "389"),
        q(2, "port-https", "Port for HTTPS?", "443 (TCP)"),
        q(2, "port-smb", "Port for SMB/CIFS (Windows file sharing)?", "445 (TCP)"),
        q(2, "port-rdp", "Port for RDP (Remote Desktop)?", "3389 (TCP)"),
        q(2, "tcp-udp", "TCP vs UDP?", "TCP: connection-oriented, guaranteed delivery (handshake, retransmits). UDP: connectionless, faster, no guarantee (streaming, DNS, DHCP)."),
        // ---- 2 Networking: addressing ----
        q(2, "apipa", "A PC has 169.254.x.x. What does it mean?", "APIPA: it couldn't reach a DHCP server and gave itself a link-local address."),
        q(2, "private-ranges", "The three private IPv4 ranges?", "10.0.0.0/8, 172.16.0.0–172.31.255.255 (/12), 192.168.0.0/16"),
        q(2, "loopback", "Loopback address in IPv4 and IPv6?", "127.0.0.1 and ::1"),
        q(2, "ipv6-linklocal", "IPv6 link-local addresses start with…?", "fe80::"),
        q(2, "ip-bits", "Bits in an IPv4 address? IPv6?", "IPv4: 32 bits. IPv6: 128 bits."),
        q(2, "slash24", "/24 as a subnet mask?", "255.255.255.0"),
        q(2, "gateway", "What is the default gateway?", "The router address a device sends traffic to when the destination is outside its own subnet."),
        q(2, "dhcp-reservation", "DHCP reservation?", "The DHCP server always gives one device (by MAC address) the same IP."),
        q(2, "dhcp-scope", "DHCP scope and lease?", "Scope: the range of addresses it hands out. Lease: how long a device keeps one before renewing."),
        q(2, "dns-a", "DNS record types A and AAAA?", "A: name → IPv4. AAAA: name → IPv6."),
        q(2, "dns-mx", "DNS record type MX?", "Which mail server receives email for the domain."),
        q(2, "dns-cname", "DNS record type CNAME?", "An alias: one name pointing to another name."),
        q(2, "dns-txt", "DNS TXT records are used for…?", "Text data, notably email security: SPF, DKIM and DMARC."),
        q(2, "vlan-vpn", "VLAN vs VPN?", "VLAN: splits one switch into separate logical networks. VPN: an encrypted tunnel over an untrusted network."),
        // ---- 2 Networking: devices and wireless ----
        q(2, "hub-switch-router", "Hub vs switch vs router?", "Hub: repeats to every port. Switch: forwards by MAC address within a network. Router: forwards by IP address between networks."),
        q(2, "managed-switch", "Managed vs unmanaged switch?", "Managed can be configured (VLANs, port settings, monitoring). Unmanaged is plug and play."),
        q(2, "poe", "PoE vs PoE+ power?", "802.3af PoE: up to 15.4 W per port. 802.3at PoE+: up to 30 W."),
        q(2, "patch-panel", "What is a patch panel for?", "A row of ports where in-wall runs terminate, so connections can be moved with short patch cables."),
        q(2, "ont", "What does an ONT do?", "Optical network terminal: converts the fiber connection into Ethernet at the customer's site."),
        q(2, "80211-ac", "802.11ac is which Wi-Fi, on which band?", "Wi-Fi 5, 5 GHz"),
        q(2, "80211-ax", "802.11ax is which Wi-Fi? And Wi-Fi 6E?", "Wi-Fi 6 (2.4 and 5 GHz). Wi-Fi 6E adds the 6 GHz band."),
        q(2, "80211-n", "802.11n is which Wi-Fi, on which bands?", "Wi-Fi 4, 2.4 and 5 GHz"),
        q(2, "24-channels", "The non-overlapping 2.4 GHz channels?", "1, 6 and 11"),
        q(2, "24-vs-5", "2.4 GHz vs 5 GHz?", "2.4: longer range, goes through walls better, more interference. 5: faster, shorter range."),
        q(2, "nfc-bt", "Range: NFC vs Bluetooth?", "NFC: a few centimeters (tap to pay). Bluetooth: about 10 m for most devices."),
        q(2, "soho-secure", "First things to secure on a new SOHO router?", "Change the default admin password, update firmware, use WPA3 (or WPA2-AES), disable WPS."),
        q(2, "port-forward", "Port forwarding?", "The router sends incoming traffic on a port to one inside device (e.g. a game server)."),
        q(2, "satellite", "Biggest drawback of satellite internet?", "High latency (long delay)."),
        q(2, "net-types", "PAN, LAN, MAN, WAN, SAN?", "Personal (Bluetooth), local (a building), metropolitan (a city), wide (the internet), storage area network."),
        q(2, "toner", "What is a tone generator and probe for?", "Finding which cable is which: put the tone on one end, trace it with the probe."),
        q(2, "punchdown", "What does a punchdown tool do?", "Seats wires into a patch panel or 110 block."),
        q(2, "loopback-plug", "What is a loopback plug for?", "Tests a network port by sending its output straight back to its input."),
        q(2, "wifi-analyzer", "When do you use a Wi-Fi analyzer?", "To see nearby networks, channels and signal strength, e.g. to pick a less crowded channel."),
        // ---- 3 Hardware: cables and connectors ----
        q(3, "cat5e", "Cat 5e speed and max length?", "1 Gbps, 100 m"),
        q(3, "cat6", "Cat 6 vs Cat 6a for 10 Gbps?", "Cat 6: 10 Gbps up to about 55 m. Cat 6a: 10 Gbps up to 100 m."),
        q(3, "plenum", "Plenum-rated cable?", "Fire-resistant jacket with low smoke, required in air-handling spaces (above drop ceilings)."),
        q(3, "stp", "When to use shielded (STP) cable?", "Near electrical interference (motors, lights, power lines)."),
        q(3, "t568", "T568A vs T568B, and a crossover cable?", "Two wiring orders for RJ45. Same standard on both ends = straight-through; A on one end and B on the other = crossover."),
        q(3, "fiber-modes", "Single-mode vs multimode fiber?", "Single-mode: long distances, laser light. Multimode: shorter runs, cheaper."),
        q(3, "fiber-connectors", "Common fiber connectors?", "SC, LC and ST"),
        q(3, "coax", "Coax cable and connector for cable internet/TV?", "RG-6 with an F-type connector"),
        q(3, "rj11-rj45", "RJ11 vs RJ45?", "RJ11: phone/DSL, smaller. RJ45: Ethernet, 8 pins."),
        q(3, "usb-speeds", "USB 2.0 vs USB 3.0 (3.2 Gen 1) speed?", "480 Mbps vs 5 Gbps"),
        q(3, "thunderbolt", "Thunderbolt 3/4 speed and connector?", "40 Gbps over a USB-C connector"),
        q(3, "video-ports", "Which video connector is analog only?", "VGA. HDMI and DisplayPort are digital (and carry audio); DVI is digital (DVI-I also carries analog)."),
        q(3, "sata", "SATA III speed?", "6 Gbps"),
        q(3, "nvme", "NVMe vs SATA SSD?", "NVMe talks over PCIe lanes: several times faster than a SATA SSD."),
        q(3, "m2", "What is M.2?", "A small slot/form factor for SSDs (SATA or NVMe) and wireless cards."),
        // ---- 3 Hardware: inside the PC ----
        q(3, "raid0", "RAID 0?", "Striping: fast, no redundancy (one drive fails, all data is lost). Minimum 2 drives."),
        q(3, "raid1", "RAID 1?", "Mirroring: identical copies. Minimum 2 drives."),
        q(3, "raid5", "RAID 5?", "Striping with parity: survives one drive failure. Minimum 3 drives."),
        q(3, "raid10", "RAID 10?", "Mirrored pairs, striped together: fast and redundant. Minimum 4 drives."),
        q(3, "hdd-rpm", "Common HDD speeds?", "5,400 and 7,200 rpm for desktops/laptops; 10,000 and 15,000 rpm for servers"),
        q(3, "ram-dimm", "DIMM vs SODIMM?", "DIMM: desktop memory. SODIMM: smaller, for laptops."),
        q(3, "ecc", "ECC memory?", "Error-correcting memory that detects and fixes single-bit errors (servers)."),
        q(3, "channels", "Dual-channel memory?", "Matched sticks in paired slots so the CPU can use two memory channels at once (more bandwidth)."),
        q(3, "ddr-mix", "Can DDR4 go in a DDR5 slot?", "No. Each generation is keyed differently and isn't compatible."),
        q(3, "form-factors", "Motherboard form factors from biggest to smallest?", "ATX, microATX, mini-ITX"),
        q(3, "uefi", "UEFI vs legacy BIOS?", "UEFI: newer firmware with a graphical setup, GPT disks over 2 TB, and Secure Boot."),
        q(3, "secure-boot", "Secure Boot?", "UEFI only starts bootloaders signed by trusted keys, blocking boot-level malware."),
        q(3, "tpm", "What is a TPM?", "A chip that securely stores encryption keys (used by BitLocker and Windows Hello)."),
        q(3, "hsm", "What is an HSM?", "Hardware security module: a dedicated device for creating and storing crypto keys."),
        q(3, "vt", "A VM won't start: \"virtualization not supported\". Fix?", "Enable Intel VT-x or AMD-V in the BIOS/UEFI."),
        q(3, "arm", "ARM vs x64 CPUs?", "ARM: low power, phones/tablets and some laptops. x64: most desktops and laptops."),
        q(3, "psu-connectors", "Main power supply connectors?", "24-pin motherboard, 4/8-pin CPU (EPS), 6/8-pin PCIe for graphics cards, SATA power for drives"),
        q(3, "psu-voltage", "Input voltage: US vs China?", "US 110–120 V, China 220 V. Most PSUs and laptop chargers auto-switch; check the label."),
        q(3, "thermal-paste", "What is thermal paste for?", "Fills tiny gaps between the CPU and heat sink so heat transfers well."),
        // ---- 3 Hardware: printers ----
        q(3, "laser-steps", "The laser printing steps in order?", "Processing, charging, exposing, developing, transferring, fusing, cleaning"),
        q(3, "fuser", "What does the fuser do?", "Melts toner onto the paper with heat and pressure."),
        q(3, "thermal-printer", "Thermal printers?", "Heat on special paper. Receipts and labels; no ink or toner."),
        q(3, "impact-printer", "When is an impact (dot-matrix) printer used?", "Multipart forms (carbon copies)."),
        q(3, "duplex", "What does a duplexing assembly do?", "Prints on both sides of the paper."),
        // ---- 1 Mobile devices ----
        q(1, "laptop-parts", "Laptop parts you can usually replace?", "Battery, RAM (SODIMM), storage (M.2 or 2.5\" drive), keyboard, Wi-Fi card"),
        q(1, "wifi-antenna", "Where is a laptop's Wi-Fi antenna usually?", "Around the screen, in the display lid"),
        q(1, "digitizer", "What is the digitizer?", "The touch layer on a screen that turns touches into input."),
        q(1, "ips-tn", "IPS vs TN LCD?", "IPS: better colors and viewing angles. TN: fast and cheap, poor angles."),
        q(1, "oled", "OLED vs LCD?", "OLED pixels make their own light: no backlight, true blacks, can burn in."),
        q(1, "dock", "Docking station vs port replicator?", "Both add ports to a laptop; a docking station can also add extras like expansion slots or drive bays."),
        q(1, "tether", "Tethering vs mobile hotspot?", "Tethering: share the phone's data with one device (USB/Bluetooth). Hotspot: the phone acts as a Wi-Fi access point."),
        q(1, "bt-pair", "Bluetooth pairing steps?", "Turn it on, make the device discoverable, select it, enter/confirm the PIN, test the connection."),
        q(1, "mdm", "What is MDM?", "Mobile device management: a company manages phones centrally (policies, apps, remote wipe)."),
        q(1, "gps", "How does a phone find its location?", "GPS satellites, plus cell towers and Wi-Fi networks to speed it up."),
        q(1, "biometrics", "Mobile biometrics examples?", "Fingerprint and facial recognition"),
        q(1, "sync", "What does syncing a phone keep in step?", "Contacts, calendar, email, photos and files between devices (usually through a cloud account)."),
        // ---- 4 Virtualization & cloud ----
        q(4, "hyper-1", "Type 1 hypervisor?", "Bare metal: runs directly on the hardware (VMware ESXi, Hyper-V)."),
        q(4, "hyper-2", "Type 2 hypervisor?", "Hosted: runs as an app on a normal OS (VirtualBox, VMware Workstation)."),
        q(4, "vm-uses", "Common reasons to use a VM?", "Sandboxing (testing safely), running old software, test environments, cross-platform."),
        q(4, "iaas", "IaaS?", "Infrastructure as a service: rent servers, storage and networking; you manage the OS up (e.g. AWS EC2)."),
        q(4, "paas", "PaaS?", "Platform as a service: a ready platform to run your code; provider manages the OS."),
        q(4, "saas", "SaaS?", "Software as a service: just use the app online (Microsoft 365, Gmail)."),
        q(4, "cloud-deploy", "Public, private, hybrid, community cloud?", "Shared provider; one organization only; a mix of both; shared by organizations with common needs."),
        q(4, "elastic", "Rapid elasticity?", "Resources grow and shrink automatically with demand."),
        q(4, "metered", "Metered (measured) service?", "You pay for what you actually use."),
        q(4, "multitenancy", "Multitenancy?", "Many customers share the same cloud hardware, kept separate from each other."),
        q(4, "vdi", "What is VDI?", "Virtual desktop infrastructure: desktops run as VMs in a data center, used from any device."),
        q(4, "ha", "High availability?", "Systems built with redundancy so they keep running when a part fails."),
        // ---- 5 Troubleshooting ----
        q(5, "method", "CompTIA's troubleshooting steps?", "1 Identify the problem. 2 Establish a theory of probable cause. 3 Test the theory. 4 Plan of action and implement. 5 Verify full functionality and add preventive measures. 6 Document findings, actions and outcomes."),
        q(5, "method-1", "Step 1 includes…?", "Gather information from the user, identify changes, back up data before making changes."),
        q(5, "method-2", "If your theory is wrong in step 3?", "Establish a new theory or escalate."),
        q(5, "no-post", "PC powers on, no POST, beeps. First checks?", "RAM (reseat), CPU, and the beep code meaning for that motherboard."),
        q(5, "grinding", "Grinding or clicking from a PC?", "A failing hard drive: back up now and replace it."),
        q(5, "smart", "S.M.A.R.T. errors?", "The drive is predicting its own failure: back up and replace."),
        q(5, "overheat", "Random shutdowns under load?", "Overheating: check fans, dust, airflow and thermal paste."),
        q(5, "boot-device", "\"No boot device found\"?", "Check boot order, the drive's cables/detection in BIOS, and whether the drive failed."),
        q(5, "swollen", "A swollen battery?", "Stop using it, remove it carefully, recycle it properly. Fire risk."),
        q(5, "dim", "Laptop screen very dim but an image is there?", "Backlight failure (or brightness setting)."),
        q(5, "flicker", "Screen flickering?", "Loose/damaged display cable or failing backlight; check refresh rate too."),
        q(5, "burn-in", "Burn-in?", "A ghost of a static image stuck on the screen (OLED, plasma)."),
        q(5, "dead-pixel", "Dead pixel vs stuck pixel?", "Dead: always black. Stuck: stuck on one color, sometimes fixable."),
        q(5, "apipa-fix", "User has a 169.254 address. Next step?", "Check the cable/Wi-Fi link and the DHCP server, then renew (ipconfig /renew)."),
        q(5, "no-internet", "Valid IP, can't reach websites?", "Check the default gateway, then DNS (ping an IP vs a name)."),
        q(5, "slow-net", "Slow network on one PC only?", "Check the cable, NIC speed/duplex, Wi-Fi signal and interference."),
        q(5, "intermittent", "Wi-Fi drops in and out?", "Interference, distance, channel overlap, or a weak signal: run a Wi-Fi analyzer."),
        q(5, "ip-conflict", "\"IP address conflict\"?", "Two devices have the same IP: fix static assignments or DHCP reservations."),
        q(5, "jam", "Laser printer keeps jamming?", "Check pickup rollers, paper type and loading, and the paper path for debris."),
        q(5, "smudge", "Toner smudges or wipes off the page?", "Fuser problem: it isn't heating or pressing properly."),
        q(5, "faded", "Faded prints?", "Low toner (or toner save mode)."),
        q(5, "ghost", "Ghost images (faint repeats down the page)?", "Drum or cleaning problem."),
        q(5, "garbled", "Printer prints garbled characters?", "Wrong or corrupted driver: reinstall the correct one."),
        q(5, "streaks", "Vertical lines on laser prints?", "Scratched or dirty drum (imaging unit)."),
        q(5, "phone-drain", "Phone battery drains fast?", "Check battery usage by app, screen brightness, background apps, and an aging battery."),
        q(5, "phone-hot", "Phone overheating?", "Heavy apps, charging while gaming, a case trapping heat, or a failing battery."),
        q(5, "touch", "Touch screen not responding?", "Restart, remove the screen protector/case, clean it; then suspect the digitizer."),
        q(5, "charging", "Phone charges slowly or not at all?", "Try another cable/charger, clean the port, check for damage."),
        q(5, "raid-fail", "RAID 5 array says degraded?", "One drive failed: replace it soon so the array rebuilds before another fails."),
    )

    /** A card's saved review, or a fresh one. */
    fun review(state: AppState, id: String): Review = state.aplus.firstOrNull { it.id == id } ?: Review(id)

    /** Due reviews first, then today's share of new cards, taking turns through the domains. */
    fun queue(state: AppState, day: Long = today()): List<Q> {
        val reviews = state.aplus.associateBy { it.id }
        val due = core1.filter { reviews[it.id]?.let { r -> r.firstSeen != null && r.due <= day } == true }
            .sortedBy { reviews.getValue(it.id).due }
        val introducedToday = state.aplus.count { it.firstSeen == day }
        val room = max(0, state.aplusPerDay - introducedToday)
        val fresh = core1.filter { reviews[it.id]?.firstSeen == null }
        val byDomain = fresh.groupBy { it.domain }.values.map { it.toMutableList() }
        val mixed = buildList {
            while (byDomain.any { it.isNotEmpty() }) byDomain.forEach { l -> if (l.isNotEmpty()) add(l.removeAt(0)) }
        }
        return due + mixed.take(room)
    }

    /** Grades one card with the same scheduler as the Chinese cards. */
    fun grade(r: Review, grade: Grade, day: Long = today()): Review {
        val asCard = Card(
            id = r.id, hanzi = "", pinyin = "", english = "", deck = "aplus",
            firstSeen = r.firstSeen, due = r.due, interval = r.interval, ease = r.ease, reps = r.reps, lapses = r.lapses, last = r.last,
        )
        val c = Srs.grade(asCard, grade, day)
        return Review(r.id, c.firstSeen, c.due, c.interval, c.ease, c.reps, c.lapses, c.last)
    }

    fun withReview(list: List<Review>, r: Review): List<Review> = list.filterNot { it.id == r.id } + r

    /** The whole state after grading one card (shared by the phone and the computer page). */
    fun graded(s: AppState, id: String, g: Grade, day: Long = today()): AppState =
        s.copy(aplus = withReview(s.aplus, grade(review(s, id), g, day)))

    /** Share of each domain's cards you know (remembered a week or more), 0–1. */
    fun strength(state: AppState): Map<Int, Float> {
        val known = state.aplus.filter { it.interval >= 7 }.map { it.id }.toSet()
        return domains.keys.associateWith { d ->
            val cards = core1.filter { it.domain == d }
            if (cards.isEmpty()) 0f else cards.count { it.id in known } / cards.size.toFloat()
        }
    }

    /** The domain that most needs work, weighted by its share of the exam; null before you start. */
    fun weakest(state: AppState): Int? {
        if (state.aplus.none { it.firstSeen != null }) return null
        val s = strength(state)
        return domains.keys.maxByOrNull { d -> (1f - (s[d] ?: 0f)) * domains.getValue(d).second }
    }
}
