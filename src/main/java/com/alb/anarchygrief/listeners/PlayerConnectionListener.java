package com.alb.anarchygrief.listeners;

import com.alb.anarchygrief.backuphandelers.ClaimProtectionBackup;
import com.alb.anarchygrief.backuphandelers.ClaimProtectionRestore;
import com.alb.anarchygrief.triggers.DisableProtection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.ScheduledTask;

import java.util.*;

public class PlayerConnectionListener implements Listener {

    private final ConnectionHandler handler;
    private final JavaPlugin plugin;
    private final ClaimProtectionBackup backup;
    private final DisableProtection disabler;
    private final int delayMinutes;

    // Track pending restore tasks per player
    private final Map<UUID, ScheduledTask> restoreTasks = new HashMap<>();
    // Track players who already have a backup scheduled
    private final Set<UUID> activeBackups = new HashSet<>();

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

        // Cancel pending restore if player relogs before it runs
        if (restoreTasks.containsKey(uuid)) {
            restoreTasks.get(uuid).cancel();
            restoreTasks.remove(uuid);
            activeBackups.remove(uuid); // reset cycle
            plugin.getLogger().info("Cancelled pending restore for " + player.getName() + " due to relog.");
            return;
        }

        if (activeBackups.contains(uuid)) {
            plugin.getLogger().info("Skipping backup schedule for " + player.getName() + " (already active).");
            return;
        }

        handler.onLogin(player);

        // Folia-safe scheduling
        plugin.getServer().getScheduler().runDelayed(plugin, task -> {
            backup.backupAllClaimPermissions(uuid);
            disabler.disableProtectionAndEnableExplosions(uuid);
            activeBackups.add(uuid);
            plugin.getLogger().info("Claims for " + player.getName() + " have been backed up and protection disabled.");
        }, delayMinutes * 60L * 20L);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        handler.onLogout(player);

        int restoreDelayMinutes = plugin.getConfig().getInt("restoreDelayMinutes", 5);
        ClaimProtectionRestore restore = new ClaimProtectionRestore(backup);

        ScheduledTask task = plugin.getServer().getScheduler().runDelayed(plugin, scheduledTask -> {
            boolean success = restore.restoreAllClaimPermissions(uuid);
            if (success) {
                plugin.getLogger().info("Claims for " + player.getName() + " have been restored from backup.");
            } else {
                plugin.getLogger().warning("Failed to restore claims for " + player.getName() + ".");
            }
            restoreTasks.remove(uuid);
            activeBackups.remove(uuid);
        }, restoreDelayMinutes * 60L * 20L);

        restoreTasks.put(uuid, task);
    }
}
