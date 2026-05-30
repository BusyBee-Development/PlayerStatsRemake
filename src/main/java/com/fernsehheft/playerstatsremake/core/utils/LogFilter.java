package com.fernsehheft.playerstatsremake.core.utils;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Filters out the annoying "Invalid statistic in" spam that occurs
 * when the server reads offline player data with statistics from
 * older versions of Minecraft that no longer exist.
 * This fixes Issue #144.
 */
public class LogFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null || record.getMessage() == null) {
            return true;
        }
        
        // Filter out the spam warning from Spigot/Paper ServerStatisticManager
        if (record.getMessage().contains("Invalid statistic in")) {
            return false;
        }

        return true;
    }
}
