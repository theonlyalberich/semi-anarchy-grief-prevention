package com.alb.anarchygrief.listeners;

import com.alb.anarchygrief.triggers.DisableProtection;
import com.alb.anarchygrief.triggers.EnableProtection;
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
    private final EnableProtection enabler;
    private final int loginDelayMinutes;

    private final Map<UUID, ScheduledTask> restoreTasks = new HashMap<>();
    private final Set<UUID> activeChanges = new HashSet<>();

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

        // Folia-safe scheduling: disable protection after configured login delay
        plugin.getServer().getScheduler().runDelayed(plugin, task -> {
            disabler.disableProtectionAndEnableExplosions(uuid);
            activeChanges.add(uuid);
        }, loginDelayMinutes * 60L * 20L);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        handler.onLogout(player);

        int restoreDelayMinutes = plugin.getConfig().getInt("restoreDelayMinutes", 5);

        // Folia-safe scheduling: enable protection after configured restore delay
        ScheduledTask task = plugin.getServer().getScheduler().runDelayed(plugin, scheduledTask -> {
            enabler.enableProtectionAndDisableExplosions(uuid);
            restoreTasks.remove(uuid);
            activeChanges.remove(uuid);
        }, restoreDelayMinutes * 60L * 20L);

        restoreTasks.put(uuid, task);
    }
}
