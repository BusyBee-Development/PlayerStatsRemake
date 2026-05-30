package com.fernsehheft.playerstatsremake.core.integration;

import com.fernsehheft.playerstatsremake.api.PlayerStatsRemake;
import com.fernsehheft.playerstatsremake.api.StatRequest;
import com.fernsehheft.playerstatsremake.api.StatResult;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStatsExpansion extends PlaceholderExpansion {

    private final PlayerStatsRemake api;

    public PlayerStatsExpansion(PlayerStatsRemake api) {
        this.api = api;
    }

    @Override
    public @NotNull String getIdentifier() {
        // Keeping the old identifier so existing scoreboards don't break
        return "playerstats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Fernsehheft";
    }

    @Override
    public @NotNull String getVersion() {
        return api.getVersion();
    }

    @Override
    public boolean persist() {
        return true; 
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        String[] split = params.toUpperCase().split("_");
        
        Statistic stat = null;
        String subStatName = null;

        for (int i = split.length; i > 0; i--) {
            StringBuilder statNameBuilder = new StringBuilder();
            for (int j = 0; j < i; j++) {
                if (j > 0) statNameBuilder.append("_");
                statNameBuilder.append(split[j]);
            }
            try {
                stat = Statistic.valueOf(statNameBuilder.toString());
                if (i < split.length) {
                    StringBuilder subStatBuilder = new StringBuilder();
                    for (int k = i; k < split.length; k++) {
                        if (k > i) subStatBuilder.append("_");
                        subStatBuilder.append(split[k]);
                    }
                    subStatName = subStatBuilder.toString();
                }
                break;
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (stat == null) {
            return null;
        }

        try {
            var generator = api.getStatManager().createPlayerStatRequest(player.getName());
            StatRequest<Integer> request = null;

            if (subStatName != null) {
                if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
                    Material mat = Material.matchMaterial(subStatName);
                    if (mat != null) {
                        request = generator.blockOrItemType(stat, mat);
                    } else {
                        return null;
                    }
                } else if (stat.getType() == Statistic.Type.ENTITY) {
                    try {
                        EntityType type = EntityType.valueOf(subStatName);
                        request = generator.entityType(stat, type);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            } else {
                request = generator.untyped(stat);
            }

            if (request == null) return null;

            StatResult<Integer> result = api.getStatManager().executePlayerStatRequest(request);
            
            // Just return the raw number so users can use it in math expansions if needed
            return String.valueOf(result.value());
        } catch (Exception e) {
            return null;
        }
    }
}
