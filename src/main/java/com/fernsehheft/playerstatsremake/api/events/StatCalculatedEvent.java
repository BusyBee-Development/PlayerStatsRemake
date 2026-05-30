package com.fernsehheft.playerstatsremake.api.events;

import com.fernsehheft.playerstatsremake.api.StatRequest;
import com.fernsehheft.playerstatsremake.api.StatResult;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired on the main thread after a statistic has been calculated,
 * but before the result is sent to the requesting player.
 *
 * <p>You can use this event to:
 * <ul>
 *   <li>Send stat results to external systems (like Discord via DiscordSRV)</li>
 *   <li>Log stat lookups</li>
 *   <li>Perform additional actions based on stat results</li>
 * </ul>
 *
 * <p>You can access the result via:
 * <ul>
 *   <li>{@link StatResult#getNumericalValue()} – raw number</li>
 *   <li>{@link StatResult#getFormattedTextComponent()} – Adventure TextComponent</li>
 *   <li>{@link StatResult#formattedString()} – plain String representation</li>
 * </ul>
 *
 * <p>Implemented based on PR #174 (DiscordSRV integration support).
 */
public class StatCalculatedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender requester;
    private final StatRequest<?> statRequest;
    private final StatResult<?> statResult;

    public StatCalculatedEvent(
            @NotNull CommandSender requester,
            @NotNull StatRequest<?> statRequest,
            @NotNull StatResult<?> statResult) {
        this.requester = requester;
        this.statRequest = statRequest;
        this.statResult = statResult;
    }

    /**
     * Gets the CommandSender who requested this statistic.
     *
     * @return the requesting CommandSender
     */
    @NotNull
    public CommandSender getRequester() {
        return requester;
    }

    /**
     * Gets the StatRequest that was executed.
     *
     * @return the StatRequest
     */
    @NotNull
    public StatRequest<?> getStatRequest() {
        return statRequest;
    }

    /**
     * Gets the StatResult that was calculated.
     * Use {@link StatResult#getNumericalValue()} for the raw number,
     * or {@link StatResult#formattedString()} for a plain text representation.
     *
     * @return the StatResult
     */
    @NotNull
    public StatResult<?> getStatResult() {
        return statResult;
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
