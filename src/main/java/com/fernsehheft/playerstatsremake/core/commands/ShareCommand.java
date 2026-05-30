package com.fernsehheft.playerstatsremake.core.commands;

import com.fernsehheft.playerstatsremake.api.events.StatSharedEvent;
import com.fernsehheft.playerstatsremake.core.sharing.ShareManager;
import com.fernsehheft.playerstatsremake.core.enums.StandardMessage;
import com.fernsehheft.playerstatsremake.core.msg.OutputManager;
import com.fernsehheft.playerstatsremake.core.sharing.StoredResult;
import com.fernsehheft.playerstatsremake.core.utils.CommandCounter;
import com.fernsehheft.playerstatsremake.core.utils.MyLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ShareCommand implements CommandExecutor {

    private static OutputManager outputManager;
    private static ShareManager shareManager;
    private final CommandCounter commandCounter;

    public ShareCommand() {
        outputManager = OutputManager.getInstance();
        shareManager = ShareManager.getInstance();
        commandCounter = CommandCounter.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && shareManager.isEnabled()) {
            int shareCode;
            try {
                shareCode = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e) {
                MyLogger.logException(e, "ShareCommand", "/statshare is being called without a valid share-code!");
                return false;
            }
            if (shareManager.requestAlreadyShared(shareCode)) {
                outputManager.sendFeedbackMsg(sender, StandardMessage.RESULTS_ALREADY_SHARED);
            } else if (shareManager.isOnCoolDown(sender.getName())) {
                outputManager.sendFeedbackMsg(sender, StandardMessage.STILL_ON_SHARE_COOLDOWN);
            } else {
                StoredResult result = shareManager.getStatResult(sender.getName(), shareCode);
                if (result == null) {
                    // the only possible cause: request is older than 25 player-requests ago
                    outputManager.sendFeedbackMsg(sender, StandardMessage.STAT_RESULTS_TOO_OLD);
                } else {
                    commandCounter.upShareCommandCount();

                    // Fire the share event to expose the data for external integrations
                    // like DiscordSRV (PR #174). If cancelled, don't broadcast.
                    StatSharedEvent event = new StatSharedEvent(sender, result.formattedValue(), shareCode);
                    Bukkit.getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        outputManager.sendToAllPlayers(result.formattedValue());
                    }
                }
            }
        }
        return true;
    }
}
