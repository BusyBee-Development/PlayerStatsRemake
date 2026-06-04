package com.fernsehheft.playerstatsremake.core.multithreading;

import com.fernsehheft.playerstatsremake.core.utils.InclusionFilter;
import com.fernsehheft.playerstatsremake.core.utils.MyLogger;
import com.fernsehheft.playerstatsremake.core.utils.OfflinePlayerHandler;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

/**
 * The action that is executed when a reload-command is triggered.
 */
final class PlayerLoadAction extends RecursiveAction {

    private static int threshold;

    private final OfflinePlayer[] players;
    private final int start;
    private final int end;

    private final ConcurrentHashMap<String, UUID> offlinePlayerUUIDs;
    private final Set<UUID> bannedUUIDs;
    private final Set<UUID> whitelistedUUIDs;

    /**
     * Fills a ConcurrentHashMap with PlayerNames and UUIDs for all OfflinePlayers
     * that should be included in statistic calculations.
     *
     * @param players array of all OfflinePlayers to filter and load
     * @param offlinePlayerUUIDs the ConcurrentHashMap to put playerNames and UUIDs in
     * @see OfflinePlayerHandler
     */
    public PlayerLoadAction(OfflinePlayer[] players, ConcurrentHashMap<String, UUID> offlinePlayerUUIDs) {
       this(players, 0, players.length, offlinePlayerUUIDs, null, null);
    }

    public PlayerLoadAction(OfflinePlayer[] players, ConcurrentHashMap<String, UUID> offlinePlayerUUIDs, Set<UUID> bannedUUIDs, Set<UUID> whitelistedUUIDs) {
       this(players, 0, players.length, offlinePlayerUUIDs, bannedUUIDs, whitelistedUUIDs);
    }

    private PlayerLoadAction(OfflinePlayer[] players, int start, int end, ConcurrentHashMap<String, UUID> offlinePlayerUUIDs, Set<UUID> bannedUUIDs, Set<UUID> whitelistedUUIDs) {
        threshold = ThreadManager.getTaskThreshold();

        this.players = players;
        this.start = start;
        this.end = end;
        this.offlinePlayerUUIDs = offlinePlayerUUIDs;
        this.bannedUUIDs = bannedUUIDs;
        this.whitelistedUUIDs = whitelistedUUIDs;

        MyLogger.subActionCreated(Thread.currentThread().getName());
    }

    @Override
    protected void compute() {
        final int length = end - start;
        if (length < threshold) {
            process();
        }
        else {
            final int split = length / 2;
            final PlayerLoadAction subTask1 = new PlayerLoadAction(players, start, (start + split),
                    offlinePlayerUUIDs, bannedUUIDs, whitelistedUUIDs);
            final PlayerLoadAction subTask2 = new PlayerLoadAction(players, (start + split), end,
                    offlinePlayerUUIDs, bannedUUIDs, whitelistedUUIDs);

            //queue and compute all subtasks in the right order
            invokeAll(subTask1, subTask2);
        }
    }

    private void process() {
        OfflinePlayerHandler offlinePlayerHandler = OfflinePlayerHandler.getInstance();

        for (int i = start; i < end; i++) {
            OfflinePlayer player = players[i];
            String playerName = player.getName();
            MyLogger.actionRunning(Thread.currentThread().getName());
            if (playerName != null &&
                    !offlinePlayerHandler.isExcludedPlayer(player.getUniqueId()) &&
                    !InclusionFilter.isExcludedFromStatistics(player, bannedUUIDs, whitelistedUUIDs)) {
                offlinePlayerUUIDs.put(playerName, player.getUniqueId());
            }
        }
    }
}