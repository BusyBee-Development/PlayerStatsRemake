package com.fernsehheft.playerstatsremake.core.integration;

import com.fernsehheft.playerstatsremake.api.events.StatSharedEvent;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DiscordSRVIntegration implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStatShared(StatSharedEvent event) {
        // Convert the colored TextComponent into plain text for Discord
        String plainText = PlainTextComponentSerializer.plainText().serialize(event.getStatResult());

        // Format the message with a little emoji
        String discordMessage = "🎮 **" + event.getSharer().getName() + "** shared a statistic:\n" + plainText;

        // Send to DiscordSRV's main text channel
        TextChannel mainChannel = DiscordSRV.getPlugin().getMainTextChannel();
        if (mainChannel != null) {
            DiscordUtil.sendMessage(mainChannel, discordMessage);
        }
    }
}
