package com.fernsehheft.playerstatsremake.core.database;

import com.fernsehheft.playerstatsremake.core.config.ConfigHandler;
import com.fernsehheft.playerstatsremake.core.utils.MyLogger;
import com.fernsehheft.playerstatsremake.core.utils.OfflinePlayerHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.fernsehheft.playerstatsremake.core.utils.Closable;
import java.util.concurrent.ForkJoinPool;

public class DatabaseManager implements Closable {

    private final JavaPlugin plugin;
    private final ConfigHandler config;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin, ConfigHandler config) {
        this.plugin = plugin;
        this.config = config;

        if (config.isDatabaseEnabled()) {
            connect();
            createTable();
            if (config.isDatabaseAutoSync()) {
                startAutoSync();
            }
        }
    }

    private void connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getDatabaseHost() + ":" + config.getDatabasePort() + "/" + config.getDatabaseName());
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(hikariConfig);
        MyLogger.logLowLevelMsg("Successfully connected to MySQL database!");
    }

    private void createTable() {
        if (dataSource == null) return;
        String tableName = config.getDatabaseTableName();
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "stat_name VARCHAR(64) NOT NULL, " +
                "sub_stat VARCHAR(64) NOT NULL, " +
                "stat_value BIGINT NOT NULL, " +
                "PRIMARY KEY (uuid, stat_name, sub_stat)" +
                ");";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            MyLogger.logException(e, "DatabaseManager", "Failed to create player_stats table!");
        }
    }

    public void startAutoSync() {
        int intervalMinutes = config.getDatabaseSyncInterval();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::exportAllStats, 20L * 60 * intervalMinutes, 20L * 60 * intervalMinutes);
        MyLogger.logLowLevelMsg("Started automatic MySQL sync task every " + intervalMinutes + " minutes.");
    }

    public void exportAllStats() {
        if (dataSource == null) return;

        MyLogger.logLowLevelMsg("Starting MySQL export for all players...");
        long startTime = System.currentTimeMillis();

        OfflinePlayerHandler playerHandler = OfflinePlayerHandler.getInstance();
        String tableName = config.getDatabaseTableName();
        String sql = "INSERT INTO " + tableName + " (uuid, player_name, stat_name, sub_stat, stat_value) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), player_name = VALUES(player_name);";

        ForkJoinPool.commonPool().execute(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);
                int batchCount = 0;

                for (String playerName : playerHandler.getIncludedOfflinePlayerNames()) {
                    OfflinePlayer player = playerHandler.getIncludedOfflinePlayer(playerName);
                    if (player.getName() == null) continue;

                    for (Statistic stat : Statistic.values()) {
                        try {
                            if (stat.getType() == Statistic.Type.UNTYPED) {
                                int val = player.getStatistic(stat);
                                if (val > 0) {
                                    addBatch(pstmt, player, stat.name(), "Total", val);
                                    batchCount++;
                                }
                            } else if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
                                for (Material mat : Material.values()) {
                                    if ((stat.getType() == Statistic.Type.BLOCK && mat.isBlock()) ||
                                            (stat.getType() == Statistic.Type.ITEM && mat.isItem())) {
                                        try {
                                            int val = player.getStatistic(stat, mat);
                                            if (val > 0) {
                                                addBatch(pstmt, player, stat.name(), mat.name(), val);
                                                batchCount++;
                                            }
                                        } catch (IllegalArgumentException ignored) {}
                                    }
                                }
                            } else if (stat.getType() == Statistic.Type.ENTITY) {
                                for (EntityType ent : EntityType.values()) {
                                    try {
                                        int val = player.getStatistic(stat, ent);
                                        if (val > 0) {
                                            addBatch(pstmt, player, stat.name(), ent.name(), val);
                                            batchCount++;
                                        }
                                    } catch (IllegalArgumentException ignored) {}
                                }
                            }

                            // Execute batch periodically to avoid memory exhaustion
                            if (batchCount > 5000) {
                                pstmt.executeBatch();
                                batchCount = 0;
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (batchCount > 0) {
                    pstmt.executeBatch();
                }
                conn.commit();
                
                long timeTaken = System.currentTimeMillis() - startTime;
                MyLogger.logMediumLevelTask("Finished MySQL export in " + timeTaken + "ms!", timeTaken);

            } catch (SQLException e) {
                MyLogger.logException(e, "DatabaseManager", "Failed to export stats to MySQL!");
            }
        });
    }

    private void addBatch(PreparedStatement pstmt, OfflinePlayer player, String statName, String subStat, int value) throws SQLException {
        pstmt.setString(1, player.getUniqueId().toString());
        pstmt.setString(2, player.getName());
        pstmt.setString(3, statName);
        pstmt.setString(4, subStat);
        pstmt.setLong(5, value);
        pstmt.addBatch();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
