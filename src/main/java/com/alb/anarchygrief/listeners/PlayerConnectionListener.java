package com.alb.anarchygrief.listeners;

import com.alb.anarchygrief.backuphandelers.ClaimProtectionBackup;
import com.alb.anarchygrief.triggers.DisableProtection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private final ConnectionHandler handler;
    private final JavaPlugin plugin;
    private final ClaimProtectionBackup backup;
    private final DisableProtection disabler;
    private final int delayMinutes;

    // Constructor now wires in everything we need
    public PlayerConnectionListener(ConnectionHandler handler,
                                    JavaPlugin plugin,
                                    ClaimProtectionBackup backup,
                                    DisableProtection disabler,
                                    int delayMinutes) {
        this.handler = handler;
        this.plugin = plugin;
        this.backup = backup;
        this.disabler = disabler;
        this.delayMinutes = delayMinutes;
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Trigger handler logic (placeholder for other modules)
        handler.onLogin(player);

        // Schedule delayed GriefPrevention logic
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            backup.backupAllClaimPermissions(uuid);
            disabler.disableProtectionAndEnableExplosions(uuid);
            plugin.getLogger().info("Claims for " + player.getName() + " have been backed up and protection disabled.");
        }, delayMinutes * 60L * 20L); // minutes → ticks
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Trigger handler logic (placeholder for other modules)
        handler.onLogout(player);

        // Future: restore claims or other logout logic can go here
    }
}
