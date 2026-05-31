package com.fernsehheft.playerstatsremake.core.utils;

import com.fernsehheft.playerstatsremake.core.enums.PlayerLookupResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Outcome of {@link OfflinePlayerHandler#analyzePlayerName(String)}.
 */
public record PlayerNameAnalysis(
    @NotNull PlayerLookupResult result,
    @Nullable String suggestedCorrectName) {

  public boolean isIncluded() {
    return result == PlayerLookupResult.INCLUDED;
  }
}
