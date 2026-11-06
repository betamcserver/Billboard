package org.garsooon.billboard.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.garsooon.billboard.billboard;

public class PlayerJoinListener implements Listener {

    private final billboard plugin;

    public PlayerJoinListener(billboard plugin) {
        this.plugin = plugin;
    }

    // For when offline and listed
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (player.hasPermission("billboard.moderate")) {
                int pendingCount = plugin.getAdManager().getPendingAds().size();
                if (pendingCount > 0) {
                    player.sendMessage(ChatColor.YELLOW + "New ad pending review! Use /reviewads");
                }
            }
        }, 1L);
    }
}
