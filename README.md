<p align="center">
   <img src="src/main/resources/images/logo_gray_rounded.png">
</p>

<h1 align="center">PlayerStatsRemake</h1>

<p align="center">
  <strong>A modern, actively maintained fork of <a href="https://github.com/itHotL/PlayerStats">PlayerStats</a> by Artemis_the_gr8</strong>
  All open issues fixed · All pull requests merged · Folia support · Java 21 · Paper API
</p>

<p align="center">
  <a href="https://github.com/itHotL/PlayerStats"><img src="https://img.shields.io/badge/Based%20on-PlayerStats%20by%20Artemis-blue" alt="Based on PlayerStats"></a>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="MIT License">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/API-Paper%201.20%2B-blueviolet" alt="Paper API">
  <img src="https://img.shields.io/badge/Folia-supported-brightgreen" alt="Folia Supported">
</p>

&nbsp;

## 🔱 About this Fork

**PlayerStatsRemake** is a fork of the popular [PlayerStats](https://github.com/itHotL/PlayerStats) plugin originally created by [Artemis_the_gr8](https://github.com/Artemis-the-gr8). Since the original project is no longer actively maintained, this fork was created to:

- ✅ Fixed most GitHub issues from the original repository
- ✅ Merged all pending pull requests
- ✅ Added Folia server support
- ✅ Modernized the codebase (Java 21, Paper API)
- ✅ Kept the plugin up-to-date with new Minecraft versions

> **Credits:** All original work, design, and concept belongs to **Artemis_the_gr8**. This fork is released under the same [MIT License](LICENSE).  
> Original repository: https://github.com/itHotL/PlayerStats

&nbsp;

## About

PlayerStatsRemake is a Minecraft server plugin that adds commands to view player statistics in top-x format, individually, or as a server-wide total. Tested on **Paper 1.20+**, **Purpur**, and **Folia**.

**Supported platforms:**
- Paper ✅ (primary)
- Spigot ✅
- Purpur ✅
- Folia ✅ (new in this fork)
- Bukkit ⚠️ (may work, not officially tested)

&nbsp;

## What's New in PlayerStatsRemake

### Bug Fixes (from original issues)
| Issue | Description | Status |
|-------|-------------|--------|
| [#175](https://github.com/itHotL/PlayerStats/issues/175) | `NullPointerException` when looking up stats for new entities like `happy_ghast` | ✅ Fixed |
| [#164](https://github.com/itHotL/PlayerStats/issues/164) | 42-second startup delay on Purpur 1.21.5 | ✅ Fixed |

### Merged Pull Requests
| PR | Description | Status |
|----|-------------|--------|
| [#170](https://github.com/itHotL/PlayerStats/pull/170) | Folia support + Paper API migration | ✅ Merged |
| [#174](https://github.com/itHotL/PlayerStats/pull/174) | Bukkit events for DiscordSRV integration | ✅ Merged |

### New Features
- **`StatCalculatedEvent`** – fired after every stat lookup (useful for DiscordSRV, logging, etc.)
- **`StatSharedEvent`** – cancellable event when a player shares results in chat

&nbsp;

## Features

* **Easy to use**
  - One central command that can:
    - Explain **how to use** the plugin with `/statistic`
    - Show you the **top 10** on your server for all possible statistics with `/statistic ... top`
    - See those same statistics for any **individual player** with `/statistic ... player`
    - Look up the **combined total** of everyone on your server
    - Guide you through the available options while you type with an extensive **tab-complete** feature
    - See the output in a **readable format** with hover-text for more info
    - **Share statistics** in chat with a click

* **No set-up required**
   - Works regardless of how long your server has existed
   - Data is retrieved directly from existing playerfiles — no database needed

* **PlaceholderAPI support**
   - Placeholders are added in a separate expansion, found on [GitHub](https://github.com/Artemis-the-gr8/PlayerStatsExpansion) or via `/papi ecloud download PlayerStats`

* **Safe & Fast**
   - Uses **multi-threading** to ensure server performance doesn't suffer
   - Players cannot crash the server by spamming commands

* **Customizable**
    - Choose which **range of units** to display time-, damage- and distance-based statistics in
    - **Automatically translate** statistics to the language of the client
    - Festive formatting, rainbow mode
    - Only show statistics for **whitelisted** players / exclude **banned** players
    - Exclude specific players with `/statexclude`
    - Limit who can **share statistics** and how often
    - Limit statistics based on when a player **last joined**
    - Custom **colors** including **hex colors**
    - Additional **style** options (italics, etc.)

&nbsp;

## Permissions

> ⚠️ **Note:** Permissions have changed from the original plugin!

| Permission | Description | Default |
|---|---|---|
| `playerstatsremake.stat` | Use `/statistic` | false |
| `playerstatsremake.share` | Share statistics in chat | false |
| `playerstatsremake.reload` | Reload the config with `/statreload` | OP only |
| `playerstatsremake.exclude` | Exclude players with `/statexclude` | OP only |

&nbsp;

## API Usage

> **Note:** This fork uses a different package path than the original. If you were using the original PlayerStats API, update your imports.

```xml
<!-- In your pom.xml -->
<dependency>
    <groupId>com.fernsehheft</groupId>
    <artifactId>PlayerStatsRemake</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

To get an instance of the API:

```java
// Import from the new package
import com.fernsehheft.playerstatsremake.api.PlayerStatsRemake;

PlayerStatsRemake api = PlayerStatsRemake.getAPI();
```

### Listening to Events (new in this fork)

```java
// Listen to stat calculation results (e.g. send to Discord)
@EventHandler
public void onStatCalculated(StatCalculatedEvent event) {
    String result = event.getStatResult().formattedString();
    CommandSender requester = event.getRequester();
    // send to Discord, log it, etc.
}

// Cancel stat sharing
@EventHandler
public void onStatShared(StatSharedEvent event) {
    event.setCancelled(true); // prevent broadcast to all players
    // send to a specific channel instead
}
```

&nbsp;

## Migrating from PlayerStats

If you were using the original PlayerStats plugin, note these changes:

| What | Original | PlayerStatsRemake |
|---|---|---|
| Plugin name | `PlayerStats` | `PlayerStatsRemake` |
| Package | `com.artemis.the.gr8.playerstats` | `com.fernsehheft.playerstatsremake` |
| API class | `PlayerStats.getAPI()` | `PlayerStatsRemake.getAPI()` |
| Permissions | `playerstats.*` | `playerstatsremake.*` |
| Java version | 16 | 21 |
| Server API | Spigot | Paper (Spigot compatible) |

&nbsp;

## Credits & License

- **Original Plugin:** [PlayerStats](https://github.com/itHotL/PlayerStats) by [Artemis_the_gr8](https://github.com/Artemis-the-gr8) — all original design, concept and implementation
- **This Fork:** Maintained by [Fernsehheft](https://github.com/Fernsehheft)
- **License:** MIT — see [LICENSE](LICENSE) for details
