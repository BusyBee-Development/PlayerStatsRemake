package com.fernsehheft.playerstatsremake.core.utils;

import com.fernsehheft.playerstatsremake.core.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Shared rules for which offline players are included in statistic calculations.
 */
public final class InclusionFilter {

    private InclusionFilter() {
    }

    /**
     * @return true if this player should not be included in top/server stat calculations
     */
    public static boolean isExcludedFromStatistics(@NotNull OfflinePlayer player) {
        return isExcludedFromStatistics(player, null, null);
    }

    /**
     * @return true if this player should not be included in top/server stat calculations
     */
    public static boolean isExcludedFromStatistics(@NotNull OfflinePlayer player, @Nullable Set<UUID> bannedUUIDs, @Nullable Set<UUID> whitelistedUUIDs) {
        ConfigHandler config = ConfigHandler.getInstance();
        String playerName = player.getName();
        if (playerName == null) {
            return true;
        }

        if (config.whitelistOnly()) {
            if (whitelistedUUIDs != null) {
                if (!whitelistedUUIDs.contains(player.getUniqueId())) {
                    return true;
                }
            } else if (!isWhitelisted(player)) {
                return true;
            }
        }
        if (config.excludeBanned()) {
            if (bannedUUIDs != null) {
                if (bannedUUIDs.contains(player.getUniqueId())) {
                    return true;
                }
            } else if (isBanned(player)) {
                return true;
            }
        }
        return !UnixTimeHandler.hasPlayedSince(config.getLastPlayedLimit(), player.getLastPlayed());
    }

  public static boolean isWhitelisted(@NotNull OfflinePlayer player) {
    return Bukkit.getWhitelistedPlayers().contains(player);
  }

  public static boolean isBanned(@NotNull OfflinePlayer player) {
    if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
      return player.isBanned();
    }
    Set<OfflinePlayer> banList = Bukkit.getBannedPlayers();
    return banList.contains(player);
  }

  /**
   * Whether a player passes config filters (whitelist, ban, last-played), ignoring manual exclude file.
   */
  public static boolean passesConfigFilters(@NotNull OfflinePlayer player) {
    return !isExcludedFromStatistics(player);
  }

  /**
   * Primary filter reason when a known player is not in the included list.
   */
  public static @NotNull com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult getConfigFilterReason(
      @NotNull OfflinePlayer player) {
    ConfigHandler config = ConfigHandler.getInstance();
    if (config.excludeBanned() && isBanned(player)) {
      return com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult.FILTERED_BANNED;
    }
    if (config.whitelistOnly() && !isWhitelisted(player)) {
      return com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult.FILTERED_WHITELIST;
    }
    if (!UnixTimeHandler.hasPlayedSince(config.getLastPlayedLimit(), player.getLastPlayed())) {
      return com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult.FILTERED_INACTIVE;
    }
    return com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult.UNKNOWN;
  }
}
