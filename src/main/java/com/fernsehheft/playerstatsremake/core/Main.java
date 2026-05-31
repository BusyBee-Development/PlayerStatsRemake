package com.fernsehheft.playerstatsremake.core;

import com.fernsehheft.playerstatsremake.api.PlayerStatsRemake;
import com.fernsehheft.playerstatsremake.api.StatNumberFormatter;
import com.fernsehheft.playerstatsremake.api.StatTextFormatter;
import com.fernsehheft.playerstatsremake.api.StatManager;
import com.fernsehheft.playerstatsremake.core.commands.*;
import com.fernsehheft.playerstatsremake.core.msg.msgutils.NumberFormatter;
import com.fernsehheft.playerstatsremake.core.multithreading.ThreadManager;
import com.fernsehheft.playerstatsremake.core.msg.OutputManager;
import com.fernsehheft.playerstatsremake.core.config.ConfigHandler;
import com.fernsehheft.playerstatsremake.core.listeners.JoinListener;
import com.fernsehheft.playerstatsremake.core.msg.msgutils.LanguageKeyHandler;
import com.fernsehheft.playerstatsremake.core.sharing.ShareManager;
import com.fernsehheft.playerstatsremake.core.statistic.StatRequestManager;
import com.fernsehheft.playerstatsremake.core.utils.*;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PlayerStatsRemake's Main class
 */
public final class Main extends JavaPlugin implements PlayerStatsRemake {

    private static JavaPlugin pluginInstance;
    private static PlayerStatsRemake playerStatsAPI;
    private static ConfigHandler config;

    private static ThreadManager threadManager;
    private static StatRequestManager statManager;

    private static List<Reloadable> reloadables;
    private static List<Closable> closables;

    private static com.fernsehheft.playerstatsremake.core.database.DatabaseManager databaseManager;
    private static com.fernsehheft.playerstatsremake.core.config.PlayerColorManager playerColorManager;

    public static com.fernsehheft.playerstatsremake.core.database.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static com.fernsehheft.playerstatsremake.core.config.PlayerColorManager getPlayerColorManager() {
        return playerColorManager;
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        reloadables = new ArrayList<>();
        closables = new ArrayList<>();

        initializeMainClassesInOrder();
        registerCommands();
        setupMetrics();
        ModrinthUpdateChecker.getInstance(this).scheduleCheck();

        // register the listener
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);

        // register internal PlaceholderAPI Expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.fernsehheft.playerstatsremake.core.integration.PlayerStatsExpansion(this).register();
        }

        // register DiscordSRV Integration
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            Bukkit.getPluginManager().registerEvents(new com.fernsehheft.playerstatsremake.core.integration.DiscordSRVIntegration(), this);
        }

        // Apply LogFilter to silence Spigot's "Invalid statistic in" spam
        Bukkit.getLogger().setFilter(new LogFilter());

        // Pretty startup banner
        String version = this.getPluginMeta().getVersion();
        String vLine = "   PlayerStats  R E M A K E   v" + version;
        String paddedVLine = String.format("|%-45s|", vLine);
        getLogger().info("*---------------------------------------------*");
        getLogger().info(paddedVLine);
        getLogger().info("|   A fork of PlayerStats by Artemis_the_gr8    |");
        getLogger().info("|   https://github.com/itHotL/PlayerStats       |");
        getLogger().info("*---------------------------------------------*");
        getLogger().info("| + Folia support                               |");
        getLogger().info("| + All Issues Fixed                            |");
        getLogger().info("| + DiscordSRV + PlaceholderAPI                 |");
        getLogger().info("| + Fast startup                                |");
        getLogger().info("*---------------------------------------------*");
        getLogger().info("Started in " + (System.currentTimeMillis() - startTime) + "ms!");
        getLogger().info("Maintained by Fernsehheft & Djtmk1 " );
    }

    @Override
    public void onDisable() {
        closables.forEach(Closable::close);
        getLogger().info("");
        getLogger().info(" PlayerStatsRemake has been disabled. Goodbye!");
        getLogger().info(" Thank you for using PlayerStatsRemake.");
        getLogger().info("");
    }

    public void reloadPlugin() {
        // config is not registered as reloadable to ensure it can be reloaded before everything else
        config.reload();
        reloadables.forEach(Reloadable::reload);
    }

    public static void registerReloadable(Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    public static void registerClosable(Closable closable) {
        closables.add(closable);
    }

    /**
     * @return the JavaPlugin instance associated with PlayerStatsRemake
     * @throws IllegalStateException if PlayerStatsRemake is not enabled
     */
    public static @NotNull JavaPlugin getPluginInstance() throws IllegalStateException {
        if (pluginInstance == null) {
            throw new IllegalStateException("PlayerStatsRemake is not loaded!");
        }
        return pluginInstance;
    }

    public static @NotNull PlayerStatsRemake getPlayerStatsAPI() throws IllegalStateException {
        if (playerStatsAPI == null) {
            throw new IllegalStateException("PlayerStatsRemake does not seem to be loaded!");
        }
        return playerStatsAPI;
    }

    /**
     * Initialize all classes that need initializing,
     * and store references to classes that are
     * needed for the Command classes or the API.
     */
    private void initializeMainClassesInOrder() {
        pluginInstance = this;
        playerStatsAPI = this;
        config = ConfigHandler.getInstance();

        LanguageKeyHandler.getInstance();
        OfflinePlayerHandler.getInstance();
        OutputManager.getInstance();
        ShareManager.getInstance();

        statManager = new StatRequestManager();
        threadManager = new ThreadManager(this);

        databaseManager = new com.fernsehheft.playerstatsremake.core.database.DatabaseManager(this, config);
        registerClosable(databaseManager);
        playerColorManager = new com.fernsehheft.playerstatsremake.core.config.PlayerColorManager(this);
    }

    /**
     * Register all commands and assign the tabCompleter to the relevant commands.
     */
    private void registerCommands() {
        TabCompleter tabCompleter = new TabCompleter();

        PluginCommand statcmd = this.getCommand("statistic");
        if (statcmd != null) {
            statcmd.setExecutor(new StatCommand(threadManager));
            statcmd.setTabCompleter(tabCompleter);
        }
        PluginCommand excludecmd = this.getCommand("statisticexclude");
        if (excludecmd != null) {
            excludecmd.setExecutor(new ExcludeCommand());
            excludecmd.setTabCompleter(tabCompleter);
        }

        PluginCommand colorcmd = this.getCommand("statcolor");
        if (colorcmd != null) {
            com.fernsehheft.playerstatsremake.core.commands.ColorCommand colorExecutor = new com.fernsehheft.playerstatsremake.core.commands.ColorCommand();
            colorcmd.setExecutor(colorExecutor);
            colorcmd.setTabCompleter(colorExecutor);
        }

        PluginCommand reloadcmd = this.getCommand("statisticreload");
        if (reloadcmd != null) {
            reloadcmd.setExecutor(new ReloadCommand(threadManager));
        }
        PluginCommand sharecmd = this.getCommand("statisticshare");
        if (sharecmd != null) {
            sharecmd.setExecutor(new ShareCommand());
        }
    }

    /**
     * Setup bstats – uses Folia-compatible GlobalRegionScheduler (fix for #164 startup delay).
     */
    private void setupMetrics() {
        // Delay metrics setup to avoid startup lag (fix for issue #164)
        // Use GlobalRegionScheduler for Folia compatibility (PR #170)
        Bukkit.getGlobalRegionScheduler().runDelayed(pluginInstance, task -> {
            final Metrics metrics = new Metrics(pluginInstance, 15923);

            boolean placeholderExpansionActive;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                PlaceholderExpansion expansion = PlaceholderAPIPlugin
                        .getInstance()
                        .getLocalExpansionManager()
                        .getExpansion("playerstats");
                placeholderExpansionActive = (expansion != null);
            } else {
                placeholderExpansionActive = false;
            }
            metrics.addCustomChart(new Metrics.SimplePie(
                    "using_placeholder_expansion",
                    () -> placeholderExpansionActive ? "yes" : "no"));

            CommandCounter counter = CommandCounter.getInstance();
            metrics.addCustomChart(new Metrics.AdvancedPie(
                    "commands_used_the_last_30_minutes",
                    counter::getCommandCounts));
        }, 200L);
    }

    @Override
    public @NotNull String getVersion() {
        // Use getPluginMeta() instead of deprecated getDescription() (PR #170)
        return this.getPluginMeta().getVersion();
    }

    @Override
    public StatManager getStatManager() {
        return statManager;
    }

    @Override
    public StatTextFormatter getStatTextFormatter() {
        return OutputManager.getInstance().getMainMessageBuilder();
    }

    @Contract(" -> new")
    @Override
    public @NotNull StatNumberFormatter getStatNumberFormatter() {
        return new NumberFormatter();
    }
}