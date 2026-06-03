# PlayerStatsRemake - Issue Tracker

Here is the complete list of all issues we inherited from the original plugin.

## 🟢 Already Fixed (Done)
- [x] **#141** - Formatted number (e.g., `1.5M` instead of `1500000`) for placeholders (Fixed by adding `_short` suffix and `formatAbbreviatedNumber`)
- [x] **#144** - "Invalid statistic in" console spam (Fixed by adding a global LogFilter for Spigot logs)
- [x] **#152** - Console error message (Bug) (Fixed by integrating PAPI directly)
- [x] **#155** - Recommendation: Remove default permissions (Removed from plugin.yml)
- [x] **#176 & #175** - Bug: `happy_ghast` / NPE for new entities (Fixed via null-checks)
- [x] **#173** - Feature: Support Folia (Fixed using GlobalRegionScheduler)
- [x] **#166 & #165 & #97** - Feature: API Events for DiscordSRV Alert Usage (Fixed via `StatCalculatedEvent` & `StatSharedEvent`)
- [x] **#164** - Bug: Startup delay on Purpur/Paper 1.21+ (Fixed by delaying metrics setup)
- [x] **#151** - Ability to modify time display (Added time.day.short etc. to language.yml)
- [x] **#136** - Total amount of all mined blocks (without specifying a block)
- [x] **#160 & #85** - Option to pull data from files and send it to MySQL database
- [x] **#39** - Allow custom colors for players
- [x] **#53** - Improve feedback for wrong playerNames
- [x] **#51** - Look into notification-for-update feature
- [x] **00** - Added support for FastStats metrics
- [x] **#154** - Add pagination / more than top 10 

## 🟡 Open (Bugfixes & Quality of Life)
- [ ] **#134** - Make a language file for plugin messages
- [ ] **#133** - Add hovertext to explain the secret birthday theme
- [ ] **#132** - Make config setting to set specific themes

## 🔴 Open (Major New Features)
- [ ] **#147** - The ability to run a command(s) when players achieves stat numbers (Achievements)
- [ ] **#146** - Custom top static
- [ ] **#29** - Display rank number for individual statistics
- [ ] **#27** - Future idea: competitive leaderboards
