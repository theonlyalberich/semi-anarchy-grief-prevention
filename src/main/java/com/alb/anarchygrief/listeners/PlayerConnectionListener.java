package com.alb.anarchygrief.listeners;

import com.alb.anarchygrief.triggers.DisableProtection;
import com.alb.anarchygrief.triggers.EnableProtection;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask; // Folia scheduler
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PlayerConnectionListener implements Listener {

    private final ConnectionHandler handler;
    private final JavaPlugin plugin;
    private final DisableProtection disabler;
    private final EnableProtection enabler;
    private final int loginDelayMinutes;

    private final Map<UUID, ScheduledTask> restoreTasks = new HashMap<>();
    private final Map<UUID, ScheduledTask> unprotectTasks = new HashMap<>();

    public PlayerConnectionListener(ConnectionHandler handler,
                                    JavaPlugin plugin,
                                    DisableProtection disabler,
                                    EnableProtection enabler,
                                    int loginDelayMinutes) {
        this.handler = handler;
        this.plugin = plugin;
        this.disabler = disabler;
        this.enabler = enabler;
        this.loginDelayMinutes = loginDelayMinutes;
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Cancel pending restore if player relogs before it executes
        if (restoreTasks.containsKey(uuid)) {
            restoreTasks.get(uuid).cancel();
            restoreTasks.remove(uuid);
        }

        // Prevent double unprotect schedules
        if (unprotectTasks.containsKey(uuid)) {
            return;
        }

        handler.onLogin(player);

        // Folia-safe scheduling: tied to player entity
        ScheduledTask task = player.getScheduler().runDelayed(
                plugin,
                scheduledTask -> {
                    disabler.disableProtectionAndEnableExplosions(uuid);
                    unprotectTasks.remove(uuid);
                },
                () -> unprotectTasks.remove(uuid),
                loginDelayMinutes * 60L * 20L
        );

        unprotectTasks.put(uuid, task);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        handler.onLogout(player);

        int restoreDelayMinutes = plugin.getConfig().getInt("restoreDelayMinutes", 5);

        // Prevent double schedules
        if (unprotectTasks.containsKey(uuid)) {
            return;
        }
        if (restoreTasks.containsKey(uuid)) {
            return;
        }

        // Folia-safe scheduling: use GlobalRegionScheduler (player entity is gone)
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().runDelayed(
                plugin,
                scheduledTask -> {
                    enabler.enableProtectionAndDisableExplosions(uuid);
                    restoreTasks.remove(uuid);
                },
                restoreDelayMinutes * 60L * 20L
        );

        restoreTasks.put(uuid, task);
    }
}
