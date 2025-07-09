# 🛡️ NetworkSlotsShield

**NetworkSlotsShield** is a lightweight and highly focused Spigot plugin that protects Minecraft clients from invalid or malicious inventory-related packets (such as `SET_SLOT` and `WINDOW_ITEMS`) that may crash or destabilize the client.

🔗 **Now available on Spigot:** [NetworkSlotsShield @ SpigotMC](https://www.spigotmc.org/resources/networkslotsshield.126797/)

Built with `ProtocolLib`, this plugin acts as a shield against corrupted slot packets and spam, especially useful when dealing with Bedrock clients (via Geyser) or poorly written server-side code.

---

## 🚀 Features

- 🔒 Blocks `SET_SLOT` packets with invalid slot indexes (≥ 46)
- 🧹 Prevents spam by throttling excessive packet sending
- 🧬 Validates `WINDOW_ITEMS` contents (blocks abnormal item count)
- 🪵 Smart logging with repetition grouping (e.g. `(x3)`)
- 🧪 Debug mode with detailed packet insights
- 💬 Toggleable protection via command
- 💻 Console-friendly and developer-oriented logs
- 👨‍💻 Clean Java code with clear comments for extensibility

---

## 📦 Requirements

- Java **17+**
- Minecraft **1.20+**
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (**required**)

---

## 📥 Installation

1. Download the latest `NetworkSlotsShield.jar` from the [releases page](https://github.com/YourUsername/NetworkSlotsShield/releases)
2. Place the jar file in your server's `/plugins` folder
3. Ensure **ProtocolLib** is installed
4. Restart or reload your server
5. You're protected!

---

## ⚙️ Commands

| Command | Description |
|--------|-------------|
| `/networkshield true` | Enable protection |
| `/networkshield false` | Disable protection |
| `/networkshield debug true` | Enable debug mode |
| `/networkshield debug false` | Disable debug mode |

> 🛡️ Permission required: `networkshield.toggle`

---

## 📄 Example Logs

```text
[NetworkShield] [DEBUG] Valid SET_SLOT (0) sent to YourS4nty
[NetworkShield] [DEBUG] Blocked SET_SLOT packet with slot 47 for YourS4nty
[NetworkShield] [DEBUG] Blocked abnormal WINDOW_ITEMS for Player123 (size=145)
[NetworkShield] [DEBUG] Slot packet flood blocked for PlayerXYZ (x6)
````

---

## 👨‍💻 Developers

The plugin is fully open-source. Feel free to contribute, fork or submit PRs!

* Clean code written in modern Java
* Fully commented
* Maven-compatible (`pom.xml` included)

---

## 📚 License

This project is licensed under the MIT License.

---

## 💡 Credits

* Created with ❤️ by [@YourS4nty](https://github.com/YourS4nty)
* Powered by [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)

---

## 📞 Need help?

Feel free to open an issue or contact me directly for support.

---
