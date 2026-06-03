package com.fernsehheft.playerstatsremake.core.msg;

import com.fernsehheft.playerstatsremake.api.StatTextFormatter;
import com.fernsehheft.playerstatsremake.core.Main;
import com.fernsehheft.playerstatsremake.core.config.ConfigHandler;
import com.fernsehheft.playerstatsremake.core.enums.StandardMessage;
import com.fernsehheft.playerstatsremake.core.utils.PlayerNameAnalysis;
import com.fernsehheft.playerstatsremake.core.msg.components.*;
import com.fernsehheft.playerstatsremake.core.msg.msgutils.FormattingFunction;
import com.fernsehheft.playerstatsremake.api.StatRequest;
import com.fernsehheft.playerstatsremake.core.utils.Closable;
import com.fernsehheft.playerstatsremake.core.utils.Reloadable;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.function.Function;

import static com.fernsehheft.playerstatsremake.core.enums.StandardMessage.*;

/**
 * This class manages all PlayerStats output. It is the only
 * place where messages are sent. It gets its messages from a
 * {@link MessageBuilder} configured for either a Console or
 * for Players (mainly to deal with the lack of hover-text,
 * and for Bukkit consoles to make up for the lack of hex-colors).
 */
public final class OutputManager implements Reloadable, Closable {

    private static volatile OutputManager instance;
    private static BukkitAudiences adventure;
    private static EnumMap<StandardMessage, Function<MessageBuilder, TextComponent>> standardMessages;

    private final ConfigHandler config;
    private MessageBuilder messageBuilder;
    private MessageBuilder consoleMessageBuilder;

    private OutputManager() {
        adventure = BukkitAudiences.create(Main.getPluginInstance());
        config = ConfigHandler.getInstance();

        getMessageBuilders();
        prepareFunctions();

        Main.registerReloadable(this);
        Main.registerClosable(this);
    }

    public static OutputManager getInstance() {
        OutputManager localVar = instance;
        if (localVar != null) {
            return localVar;
        }

        synchronized (OutputManager.class) {
            if (instance == null) {
                instance = new OutputManager();
            }
            return instance;
        }
    }

    @Override
    public void reload() {
        getMessageBuilders();
    }

    @Override
    public void close() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public StatTextFormatter getMainMessageBuilder() {
        return messageBuilder;
    }

    public @NotNull String textComponentToString(TextComponent component) {
        return messageBuilder.textComponentToString(component);
    }

    /**
     * @return a TextComponent with the following parts:
     * <br>[player-name]: [number] [stat-name] {sub-stat-name}
     */
    public @NotNull FormattingFunction formatPlayerStat(@NotNull StatRequest.Settings requestSettings, int playerStat) {
        return getMessageBuilder(requestSettings.getCommandSender())
                .formattedPlayerStatFunction(playerStat, requestSettings);
    }

    /**
     * @return a TextComponent with the following parts:
     * <br>[Total on] [server-name]: [number] [stat-name] [sub-stat-name]
     */
    public @NotNull FormattingFunction formatServerStat(@NotNull StatRequest.Settings requestSettings, long serverStat) {
        return getMessageBuilder(requestSettings.getCommandSender())
                .formattedServerStatFunction(serverStat, requestSettings);
    }

    /**
     * @return a TextComponent with the following parts:
     * <br>[PlayerStats] [Top 10] [stat-name] [sub-stat-name]
     * <br> [1.] [player-name] [number]
     * <br> [2.] [player-name] [number]
     * <br> [3.] etc...
     */
    public @NotNull FormattingFunction formatTopStats(@NotNull StatRequest.Settings requestSettings, @NotNull LinkedHashMap<String, Integer> topStats) {
        return getMessageBuilder(requestSettings.getCommandSender())
                .formattedTopStatFunction(topStats, requestSettings);
    }

    public void sendFeedbackMsg(@NotNull CommandSender sender, StandardMessage message) {
        if (message != null) {
            adventure.sender(sender).sendMessage(standardMessages.get(message)
                    .apply(getMessageBuilder(sender)));
        }
    }

    public void sendFeedbackMsgPlayerExcluded(@NotNull CommandSender sender, String playerName) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .excludeSuccess(playerName));
    }

    public void sendFeedbackMsgPlayerIncluded(@NotNull CommandSender sender, String playerName) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .includeSuccess(playerName));
    }

    public void sendFeedbackMsgMissingSubStat(@NotNull CommandSender sender, String statType) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .missingSubStatName(statType));
    }

    public void sendFeedbackMsgWrongSubStat(@NotNull CommandSender sender, String statType, @Nullable String subStatName) {
        if (subStatName == null) {
            sendFeedbackMsgMissingSubStat(sender, statType);
        } else {
            adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                    .wrongSubStatType(statType, subStatName));
        }
    }

    public @NotNull TextComponent updateAvailableMessage(
            @NotNull String currentVersion,
            @NotNull String latestVersion,
            @NotNull String downloadUrl) {
        return messageBuilder.updateAvailableMessage(currentVersion, latestVersion, downloadUrl);
    }

    public void sendPlayerLookupFailure(
            @NotNull CommandSender sender,
            @NotNull PlayerNameAnalysis analysis,
            @NotNull String playerName) {
        MessageBuilder builder = getMessageBuilder(sender);
        TextComponent message = switch (analysis.result()) {
            case EXCLUDED_MANUAL -> builder.playerIsExcluded();
            case WRONG_CASE -> builder.playerNameWrongCase(
                    playerName,
                    analysis.suggestedCorrectName() != null ? analysis.suggestedCorrectName() : playerName);
            case UNKNOWN -> builder.unknownPlayerName(playerName);
            case FILTERED_BANNED -> builder.playerFilteredBanned(playerName);
            case FILTERED_WHITELIST -> builder.playerFilteredWhitelist(playerName);
            case FILTERED_INACTIVE -> builder.playerFilteredInactive(
                    playerName, config.getLastPlayedLimit());
            case INCLUDED -> builder.unknownPlayerName(playerName);
        };
        adventure.sender(sender).sendMessage(message);
    }

    public void sendExamples(@NotNull CommandSender sender) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .usageExamples());
    }

    public void sendHelp(@NotNull CommandSender sender) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .helpMsg());
    }

    public void sendExcludeInfo(@NotNull CommandSender sender) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .excludeInfoMsg());
    }

    public void sendExcludedList(@NotNull CommandSender sender, ArrayList<String> excludedPlayerNames) {
        adventure.sender(sender).sendMessage(getMessageBuilder(sender)
                .excludedList(excludedPlayerNames));
    }

    public void sendToAllPlayers(@NotNull net.kyori.adventure.text.Component component) {
        adventure.players().sendMessage(component);
    }

    public void sendToCommandSender(@NotNull CommandSender sender, @NotNull net.kyori.adventure.text.Component component) {
        adventure.sender(sender).sendMessage(component);
    }

    private MessageBuilder getMessageBuilder(CommandSender sender) {
        return sender instanceof ConsoleCommandSender ? consoleMessageBuilder : messageBuilder;
    }

    private void getMessageBuilders() {
        messageBuilder = getClientMessageBuilder();
        consoleMessageBuilder = getConsoleMessageBuilder();
    }

    private MessageBuilder getClientMessageBuilder() {
        ComponentFactory festiveFactory = getFestiveFactory();
        if (festiveFactory == null) {
            return MessageBuilder.defaultBuilder();
        }
        return MessageBuilder.fromComponentFactory(festiveFactory);
    }

    private @NotNull MessageBuilder getConsoleMessageBuilder() {
        MessageBuilder consoleBuilder;
        if (isBukkit()) {
            consoleBuilder = MessageBuilder.fromComponentFactory(new BukkitConsoleComponentFactory());
        } else {
            consoleBuilder = MessageBuilder.fromComponentFactory(new ConsoleComponentFactory());
        }
        return consoleBuilder;
    }

    private @Nullable ComponentFactory getFestiveFactory() {
        if (config.useRainbowMode()) {
            return new PrideComponentFactory();
        }
        else if (config.useFestiveFormatting()) {
            return switch (LocalDate.now().getMonth()) {
                case JUNE -> new PrideComponentFactory();
                case OCTOBER -> new HalloweenComponentFactory();
                case SEPTEMBER -> {
                    if (LocalDate.now().getDayOfMonth() == 12) {
                        yield new BirthdayComponentFactory();
                    }
                    yield null;
                }
                case DECEMBER -> new WinterComponentFactory();
                default -> null;
            };
        }
        return null;
    }

    private boolean isBukkit() {
        return Bukkit.getName().equalsIgnoreCase("CraftBukkit");
    }

    private void prepareFunctions() {
        standardMessages = new EnumMap<>(StandardMessage.class);

        standardMessages.put(RELOADED_CONFIG, MessageBuilder::reloadedConfig);
        standardMessages.put(STILL_RELOADING, MessageBuilder::stillReloading);
        standardMessages.put(EXCLUDE_FAILED, MessageBuilder::excludeFailed);
        standardMessages.put(INCLUDE_FAILED, MessageBuilder::includeFailed);
        standardMessages.put(MISSING_STAT_NAME, MessageBuilder::missingStatName);
        standardMessages.put(MISSING_PLAYER_NAME, MessageBuilder::missingPlayerName);
        standardMessages.put(PLAYER_IS_EXCLUDED, MessageBuilder::playerIsExcluded);
        standardMessages.put(WAIT_A_MOMENT, MessageBuilder::waitAMoment);
        standardMessages.put(WAIT_A_MINUTE, MessageBuilder::waitAMinute);
        standardMessages.put(REQUEST_ALREADY_RUNNING, MessageBuilder::requestAlreadyRunning);
        standardMessages.put(STILL_ON_SHARE_COOLDOWN, MessageBuilder::stillOnShareCoolDown);
        standardMessages.put(RESULTS_ALREADY_SHARED, MessageBuilder::resultsAlreadyShared);
        standardMessages.put(STAT_RESULTS_TOO_OLD, MessageBuilder::statResultsTooOld);
        standardMessages.put(UNKNOWN_ERROR, MessageBuilder::unknownError);
    }
}