package com.fernsehheft.playerstatsremake.api;

import com.fernsehheft.playerstatsremake.core.Main;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The outgoing API that represents the core functionality of PlayerStatsRemake!
 *
 * <p>To work with it, call PlayerStatsRemake.{@link #getAPI()}
 * and get an instance of PlayerStatsRemake.
 *
 * @see StatManager
 * @see StatTextFormatter
 * @see StatNumberFormatter
 */
public interface PlayerStatsRemake {

    /**
     * Gets an instance of the PlayerStatsRemake API.
     *
     * @return the PlayerStatsRemake API
     * @throws IllegalStateException if PlayerStatsRemake is not loaded on the server
     */
    @Contract(pure = true)
    static @NotNull PlayerStatsRemake getAPI() throws IllegalStateException {
        return Main.getPlayerStatsAPI();
    }

    /**
     * Gets the version number of the PlayerStatsRemake API.
     *
     * @return the API version number
     */
    String getVersion();

    StatManager getStatManager();

    StatTextFormatter getStatTextFormatter();

    StatNumberFormatter getStatNumberFormatter();
}
