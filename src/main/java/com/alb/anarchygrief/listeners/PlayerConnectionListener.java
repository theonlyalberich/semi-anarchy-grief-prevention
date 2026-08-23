package com.alb.anarchygrief.listeners;

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
    private final DisableProtection disabler;
    private final int delayMinutes;

    // Track pending restore tasks per player (still here for future restore logic)
    private final Map<UUID, ScheduledTask> restoreTasks = new HashMap<>();
    // Track players who already have a protection change scheduled
    private final Set<UUID> activeChanges = new HashSet<>();

    public PlayerConnectionListener(ConnectionHandler handler,
                                    JavaPlugin plugin,
                                    DisableProtection disabler,
                                    int delayMinutes) {
        this.handler = handler;
        this.plugin = plugin;
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
            activeChanges.remove(uuid);
            return;
        }

        if (activeChanges.contains(uuid)) {
            return;
        }

        handler.onLogin(player);

        // Folia-safe scheduling
        plugin.getServer().getScheduler().runDelayed(plugin, task -> {
            disabler.disableProtectionAndEnableExplosions(uuid);
            activeChanges.add(uuid);
        }, delayMinutes * 60L * 20L);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        handler.onLogout(player);

        // Restore logic still exists but untouched, per your request
        int restoreDelayMinutes = plugin.getConfig().getInt("restoreDelayMinutes", 5);

        ScheduledTask task = plugin.getServer().getScheduler().runDelayed(plugin, scheduledTask -> {
            // Placeholder: restore logic not removed yet
            restoreTasks.remove(uuid);
            activeChanges.remove(uuid);
        }, restoreDelayMinutes * 60L * 20L);

        restoreTasks.put(uuid, task);
    }
}
