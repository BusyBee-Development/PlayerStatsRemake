package com.fernsehheft.playerstatsremake.core.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Compares dotted version strings (e.g. {@code 2026.6.0}).
 */
public final class VersionComparator {

    private VersionComparator() {
    }

    /**
     * @return negative if {@code left} is older than {@code right}, zero if equal, positive if newer
     */
    public static int compare(@NotNull String left, @NotNull String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int length = Math.max(leftParts.length, rightParts.length);

        for (int i = 0; i < length; i++) {
            int leftSegment = i < leftParts.length ? parseSegment(leftParts[i]) : 0;
            int rightSegment = i < rightParts.length ? parseSegment(rightParts[i]) : 0;
            if (leftSegment != rightSegment) {
                return Integer.compare(leftSegment, rightSegment);
            }
        }
        return 0;
    }

    public static boolean isNewer(@NotNull String candidate, @NotNull String current) {
        return compare(candidate, current) > 0;
    }

    private static int parseSegment(String segment) {
        String digits = segment.replaceAll("[^0-9].*", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
