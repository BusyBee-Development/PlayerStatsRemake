package com.fernsehheft.playerstatsremake.core.enums;

/**
 * Result of resolving a player name for /statistic player lookups.
 */
public enum PlayerLookupResult {
    /** Name is in the included-player list (exact match). */
    INCLUDED,
    /** Name is on the manual exclude list. */
    EXCLUDED_MANUAL,
    /** Same player, wrong capitalization. */
    WRONG_CASE,
    /** No record that this player has ever joined. */
    UNKNOWN,
    /** Excluded because exclude-banned-players is enabled. */
    FILTERED_BANNED,
    /** Excluded because include-whitelist-only is enabled. */
    FILTERED_WHITELIST,
    /** Excluded because number-of-days-since-last-joined is exceeded. */
    FILTERED_INACTIVE
}
