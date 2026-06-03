package com.fernsehheft.playerstatsremake.core.statistic;

import com.fernsehheft.playerstatsremake.api.RequestGenerator;
import com.fernsehheft.playerstatsremake.api.StatRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public final class TopStatRequest extends StatRequest<LinkedHashMap<String, Integer>> implements RequestGenerator<LinkedHashMap<String, Integer>> {

    public TopStatRequest(int topListSize) {
        this(Bukkit.getConsoleSender(), topListSize, 1);
    }

    public TopStatRequest(int topListSize, int page) {
        this(Bukkit.getConsoleSender(), topListSize, page);
    }

    public TopStatRequest(CommandSender sender, int topListSize) {
        this(sender, topListSize, 1);
    }

    public TopStatRequest(CommandSender sender, int topListSize, int page) {
        super(sender);
        super.configureForTop(topListSize, page);
    }

    @Override
    public boolean isValid() {
        return super.hasMatchingSubStat();
    }

    @Override
    public StatRequest<LinkedHashMap<String, Integer>> untyped(@NotNull Statistic statistic) {
        super.configureUntyped(statistic);
        return this;
    }

    @Override
    public StatRequest<LinkedHashMap<String, Integer>> blockOrItemType(@NotNull Statistic statistic, @NotNull Material material) {
        super.configureBlockOrItemType(statistic, material);
        return this;
    }

    @Override
    public StatRequest<LinkedHashMap<String, Integer>> entityType(@NotNull Statistic statistic, @NotNull EntityType entityType) {
        super.configureEntityType(statistic, entityType);
        return this;
    }
}