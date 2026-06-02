package com.fernsehheft.playerstatsremake.core.managers;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;
import com.fernsehheft.playerstatsremake.core.Main;
import com.fernsehheft.playerstatsremake.core.utils.CommandCounter;

public class FastStatsManager {

    private static final String FASTSTATS_TOKEN = "45250b5e15853434411fea9fbaf337d8";

    private final Main plugin;
    private final BukkitMetrics metrics;

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(java.lang.reflect.InvocationTargetException.class);

    public FastStatsManager(Main plugin) {
        this.plugin = plugin;

        this.metrics = BukkitMetrics.factory()
                .token(FASTSTATS_TOKEN)
                .errorTracker(ERROR_TRACKER)
                .addMetric(Metric.number("commands_total", () -> CommandCounter.getInstance().getTotalCommands()))
                .create(plugin);
    }

    public void onEnable() {
        metrics.ready();
    }

    public void onDisable() {
        metrics.shutdown();
    }
}
