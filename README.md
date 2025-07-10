# 🛡️ NetworkSlotsShield

**NetworkSlotsShield** is a lightweight and highly focused Spigot plugin that protects Minecraft clients from malicious or invalid inventory-related packets — particularly `SET_SLOT` and `WINDOW_ITEMS` — that can cause visual glitches or client crashes.

🔗 **Now available on Spigot:** [NetworkSlotsShield @ SpigotMC](https://www.spigotmc.org/resources/networkslotsshield.126797/)  

Built with [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/), this plugin acts as a **packet-level shield**, protecting players from harmful or spammy inventory actions — while remaining invisible to gameplay and compatible with most custom GUIs.

---

## 🚀 Features

- 🔒 **Filters invalid `SET_SLOT` packets** targeting non-existent inventory slots
- 🧼 **Grace period after teleport or inventory open** to avoid breaking custom interfaces
- 🧬 **Blocks abnormal `WINDOW_ITEMS` packets** (e.g. oversized item arrays)
- 🧃 **Throttles excessive packet frequency** (per-player cooldown)
- 📃 **Smart logging system** with repetition grouping (`(x3)` etc.)
- 🧪 **Debug mode** for live packet diagnostics
- 💬 **Toggle protection and debug via commands**
- 💻 **Colorized logs** with decorated console startup banner
- 🧠 **Minimal performance impact**, safe to run on any server

---

## 📦 Requirements

- Java **17+**
- Minecraft **1.20+**
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (**required**)

---

## 📥 Installation

1. Download the latest `NetworkSlotsShield.jar` from the [Releases page](https://github.com/YourS4nty/NetworkShield/releases)
2. Place it inside your server's `/plugins` folder
3. Install **ProtocolLib** if not already present
4. Restart or reload your server — done!

---

## ⚙️ Commands

| Command | Description |
|---------|-------------|
| `/networkshield true` | Enable protection |
| `/networkshield false` | Disable protection |
| `/networkshield debug true` | Enable debug logging |
| `/networkshield debug false` | Disable debug logging |

> 🛡️ **Permission required:** `networkshield.toggle`

---

## 🛠️ Compilation (Maven)

To build this plugin from source:

1. Clone the repository:
   
   - git clone https://github.com/YourS4nty/NetworkShield.git
   - cd NetworkShield


3. Build using Maven:

  
   - mvn clean package
   

4. The compiled JAR will be located in the `target/` folder.

> **Note:** Make sure you have ProtocolLib in your local Maven repository or as a dependency.

---

## 📄 Example Logs


- [NetworkShield] [DEBUG] Allowed SET_SLOT slot=38 (safe)
- [NetworkShield] [DEBUG] Blocked SET_SLOT: slot 50 > max 45
- [NetworkShield] [DEBUG] Blocked abnormal WINDOW_ITEMS for Player123 (size=145)
- [NetworkShield] [DEBUG] Slot packet flood blocked for PlayerXYZ (x6)


---

## 👨‍💻 Developers

This project is open-source and welcomes contributions!

* Clean and modern Java codebase
* Fully commented
* ProtocolLib integration with minimal overhead
* Easy to fork or extend

### Maven Dependency (optional)

If you publish to your own repo:


<dependency>
  <groupId>com.yours4nty</groupId>
  <artifactId>NetworkSlotsShield</artifactId>
  <version>1.0</version>
</dependency>

---

## 📚 License

This project is licensed under the **MIT License**.

---

## 👑 Credits

* Created with ❤️ by [@YourS4nty](https://github.com/YourS4nty)
* Powered by [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)

---

## 💬 Need Help?

Feel free to:

* Open an [issue](https://github.com/YourS4nty/NetworkShield/issues)
* Contact the author directly via GitHub

---
