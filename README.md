# GoldenVelocityUtils ⚡

GoldenVelocityUtils is a comprehensive plugin for **Velocity** proxy servers, designed to empower the administration of your Minecraft server network. It offers moderation tools, persistent chat, maintenance mode, automatic announcements, and seamless synchronization across your network.

## 🚀 Key Features

*   **🛡️ Advanced Administrative Chat System:** Independent channels for Staff (`/sc`) and Admins (`/ac`) or continuous use via "Toggle Mode".
*   **🔌 Persistent State (Seamless Switch):** When switching between servers (e.g., from Lobby to Survival), your StaffChat or AdminChat state remains automatically active.
*   **📡 GoldenChatBridge Integration:** Fully prepared to bypass the strict "Signed Chat Validation" systems introduced in Minecraft 1.19.1+ by using asynchronous plugin messaging channels.
*   **🚧 Global Maintenance Mode:** Enable whitelists and restrict access to your entire network with real-time commands.
*   **📣 Dynamic Announcements:** Set up multi-line automatic messages natively supporting RGB/HEX formatting and hover tooltips, broadcasted across the network at scheduled intervals.
*   **🔍 Moderation Tools:** HelpOp system (live user reports), `/goto`, `/find`, and tracking of connection hours/statistics for every Staff member.
*   **🌐 100% Flexible Customization:** YAML configuration system, allowing built-in prefixes via LuckPerms integration and custom HEX colors.

---

## 🔗 GoldenChatBridge (Important for 1.19+ Networks!)

For the successful use of **StaffChat** and **AdminChat** on a Minecraft 1.19.1+ server, **you cannot intercept standard chats via the proxy** without Minecraft kicking the user out due to "Signed Chat Validation" errors.

To solve this, GoldenVelocityUtils works synchronously with our companion plugin: **GoldenChatBridge**.

**GoldenChatBridge** is installed on your backend servers (your gamemodes like Lobby, Survival, BedWars, etc.). It stealthily receives proxy messages and handles the player's chat directly from Spigot/Paper to forward them back to your Velocity proxy without breaking the player's encrypted signature security.

👉 **[Link to GoldenChatBridge Repository or Download]((PUT_REPO_LINK_HERE))** 👈

*Installation Note: Make sure to install GoldenChatBridge on your backend servers so the proxy messaging can flow freely without causing disconnects.*

---

## ⚙️ Installation

1. Download the compiled `.jar` file of **GoldenVelocityUtils**.
2. Drop it into the `/plugins/` folder of your **Velocity** server.
3. *Optional but Recommended:* Install [LuckPerms](https://luckperms.net/) on the proxy to properly leverage rank dependencies.
4. Restart your proxy to generate the configuration folder.
5. *(Remember to install the aforementioned Bridge plugin on your Bukkit/Paper backend servers)*.

## 💻 Useful Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/sc [message]` | `gbu.staff.chat` | Toggles staff chat or sends a quick message. |
| `/ac [message]` | `gbu.admin.chat` | Toggles admin chat or sends a quick message. |
| `/helpop <msg>` | `gbu.user.helpop` | Sends a help request to all online staff. |
| `/alert <msg>` | `gbu.staff.alert` | Broadcasts an on-screen alert across all servers. |
| `/goto <player>` | `gbu.staff.goto` | Teleport to the server where a player is currently located. |
| `/find <player>` | `gbu.staff.find` | Check the current server/location of an active player. |
| `/gbu maintenance`| `gbu.admin.cmd` | Toggles the proxy network's maintenance mode. |
| `/gbu reload` | `gbu.admin.cmd` | Live-reloads all YAML plugin configurations. |

---

*Developed with ❤️ for modern Velocity networks.*
