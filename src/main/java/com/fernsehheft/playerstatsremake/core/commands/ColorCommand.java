package com.fernsehheft.playerstatsremake.core.commands;

import com.fernsehheft.playerstatsremake.core.Main;
import com.fernsehheft.playerstatsremake.core.msg.OutputManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ColorCommand implements CommandExecutor, TabCompleter {

    private final OutputManager outputManager = OutputManager.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("playerstats.color")) {
            outputManager.sendToCommandSender(player, Component.text("You do not have permission to use this command!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            outputManager.sendToCommandSender(player, Component.text("Usage: /statcolor <color|hex|reset>").color(NamedTextColor.RED));
            return true;
        }

        String colorInput = args[0];

        if (colorInput.equalsIgnoreCase("reset") || colorInput.equalsIgnoreCase("clear")) {
            Main.getPlayerColorManager().setColor(player, null);
            outputManager.sendToCommandSender(player, Component.text("Your stat color has been reset!").color(NamedTextColor.GREEN));
            return true;
        }

        TextColor color = null;
        if (colorInput.startsWith("#")) {
            color = TextColor.fromHexString(colorInput);
        } else {
            color = NamedTextColor.NAMES.value(colorInput.toLowerCase());
        }

        if (color == null) {
            outputManager.sendToCommandSender(player, Component.text("Invalid color! Use a hex code (e.g., #FF0000) or a standard Minecraft color (e.g., red, gold).").color(NamedTextColor.RED));
            return true;
        }

        Main.getPlayerColorManager().setColor(player, color);
        outputManager.sendToCommandSender(player, Component.text("Your stat color has been set to this!").color(color));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("reset".startsWith(partial)) completions.add("reset");
            if ("clear".startsWith(partial)) completions.add("clear");
            
            for (String colorName : NamedTextColor.NAMES.keys()) {
                if (colorName.startsWith(partial)) {
                    completions.add(colorName);
                }
            }
        }
        return completions;
    }
}
