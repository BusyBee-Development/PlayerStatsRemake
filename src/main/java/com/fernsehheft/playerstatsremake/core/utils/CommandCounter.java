package com.fernsehheft.playerstatsremake.core.utils;

import java.util.HashMap;
import java.util.Map;

public class CommandCounter {

    private static volatile CommandCounter instance;
    private int helpCommand;
    private int excludeCommand;
    private int shareCommand;
    private int playerStatCommand;
    private int serverStatCommand;
    private int topStatCommand;
    private int totalCommands;

    private CommandCounter() {
        resetCounts();
    }

    public static CommandCounter getInstance() {
        CommandCounter localVar = instance;
        if (localVar != null) {
            return localVar;
        }

        synchronized (CommandCounter.class) {
            if (instance == null) {
                instance = new CommandCounter();
            }
            return instance;
        }
    }

    public synchronized void upHelpCommandCount() {
        helpCommand++;
        totalCommands++;
    }

    public synchronized void upExcludeCommandCount() {
        excludeCommand++;
        totalCommands++;
    }

    public synchronized void upShareCommandCount() {
        shareCommand++;
        totalCommands++;
    }

    public synchronized void upPlayerStatCommandCount() {
        playerStatCommand++;
        totalCommands++;
    }

    public synchronized void upServerStatCommandCount() {
        serverStatCommand++;
        totalCommands++;
    }

    public synchronized void upTopStatCommandCount() {
        topStatCommand++;
        totalCommands++;
    }

    public Map<String, Integer> getCommandCounts() {
        Map<String, Integer> commandCounts = new HashMap<>();
        commandCounts.put("Help", helpCommand);
        commandCounts.put("Exclude", excludeCommand);
        commandCounts.put("Share", shareCommand);
        commandCounts.put("Player Stat", playerStatCommand);
        commandCounts.put("Server Stat", serverStatCommand);
        commandCounts.put("Top Stat", topStatCommand);
        resetCounts();
        return commandCounts;
    }

    public synchronized int getTotalCommands() {
        return totalCommands;
    }

    private synchronized void resetCounts() {
        helpCommand = 0;
        excludeCommand = 0;
        shareCommand = 0;
        playerStatCommand = 0;
        serverStatCommand = 0;
        topStatCommand = 0;
    }
}