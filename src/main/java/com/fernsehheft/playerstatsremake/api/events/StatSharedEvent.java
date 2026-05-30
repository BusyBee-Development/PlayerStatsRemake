package com.fernsehheft.playerstatsremake.api.events;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a player shares their stat result with all players.
 * It is cancellable – if cancelled, the result will NOT be broadcast to all players.
 *
 * <p>You can use this event to:
 * <ul>
 *   <li>Send shared stats to Discord via DiscordSRV</li>
 *   <li>Log stat shares</li>
 *   <li>Prevent certain stats from being shared</li>
 *   <li>Send the stat to custom channels</li>
 * </ul>
 *
 * <p>Implemented based on PR #174 (DiscordSRV integration support).
 */
public class StatSharedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender sharer;
    private final TextComponent statResult;
    private final int shareCode;
    private boolean cancelled;

    public StatSharedEvent(
            @NotNull CommandSender sharer,
            @NotNull TextComponent statResult,
            int shareCode) {
        this.sharer = sharer;
        this.statResult = statResult;
        this.shareCode = shareCode;
        this.cancelled = false;
    }

    /**
     * Gets the CommandSender who is sharing the result.
     *
     * @return the sharing CommandSender
     */
    @NotNull
    public CommandSender getSharer() {
        return sharer;
    }

    /**
     * Gets the formatted stat result as a TextComponent.
     * For a plain String representation, you can use
     * Adventure's {@code PlainTextComponentSerializer}.
     *
     * @return the formatted stat result
     */
    @NotNull
    public TextComponent getStatResult() {
        return statResult;
    }

    /**
     * Gets the share code that was used to retrieve this statistic.
     * This is the unique identifier for this particular stat share.
     *
     * @return the share code
     */
    public int getShareCode() {
        return shareCode;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
