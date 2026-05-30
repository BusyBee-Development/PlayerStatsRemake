package com.fernsehheft.playerstatsremake.core.config;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerColorManager {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;
    
    // Cache map: lower-case player name to custom TextColor
    private final Map<String, TextColor> customColorsByName;
    // Map: UUID to custom TextColor
    private final Map<UUID, TextColor> customColorsByUUID;

    public PlayerColorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customColorsByName = new HashMap<>();
        this.customColorsByUUID = new HashMap<>();
        loadColors();
    }

    private void loadColors() {
        file = new File(plugin.getDataFolder(), "player_colors.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player_colors.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        for (String uuidString : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String hex = config.getString(uuidString);
                if (hex != null) {
                    TextColor color = TextColor.fromHexString(hex);
                    if (color == null) {
                        // Might be a named color
                        color = net.kyori.adventure.text.format.NamedTextColor.NAMES.value(hex.toLowerCase());
                    }
                    if (color != null) {
                        customColorsByUUID.put(uuid, color);
                        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                        if (op.getName() != null) {
                            customColorsByName.put(op.getName().toLowerCase(), color);
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void setColor(Player player, TextColor color) {
        UUID uuid = player.getUniqueId();
        if (color == null) {
            customColorsByUUID.remove(uuid);
            customColorsByName.remove(player.getName().toLowerCase());
            config.set(uuid.toString(), null);
        } else {
            customColorsByUUID.put(uuid, color);
            customColorsByName.put(player.getName().toLowerCase(), color);
            config.set(uuid.toString(), color.asHexString());
        }
        save();
    }

    public TextColor getColorByPlayerName(String playerName) {
        if (playerName == null) return null;
        
        // Remove trailing colons or brackets if they got passed along
        playerName = playerName.replace(":", "").replace("[", "").replace("]", "");
        
        return customColorsByName.get(playerName.toLowerCase());
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_colors.yml!");
        }
    }
}
