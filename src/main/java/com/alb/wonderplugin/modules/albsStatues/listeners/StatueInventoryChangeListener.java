package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class StatueInventoryChangeListener implements Listener {

    private final WonderPlugin plugin;
    private final NamespacedKey ownerKey;

    // Track players already scheduled for refresh this tick
    private final Set<Player> scheduledRefresh = new HashSet<>();

    public StatueInventoryChangeListener(WonderPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            scheduleRefresh(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            scheduleRefresh(player);
        }
    }

    private void scheduleRefresh(Player player) {
        // Prevent multiple refreshes in the same tick
        if (scheduledRefresh.contains(player)) return;
        scheduledRefresh.add(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            refreshNearbyStatues(player);
            scheduledRefresh.remove(player);
        });
    }

    private void refreshNearbyStatues(Player player) {
        // Only check statues near the player (32 block radius)
        player.getNearbyEntities(32, 32, 32).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .forEach(stand -> {
                    PersistentDataContainer data = stand.getPersistentDataContainer();
                    if (data.has(ownerKey, PersistentDataType.STRING)) {
                        // Only reapply if needed
                        if (!stand.isInvisible()) stand.setInvisible(true);
                        if (!stand.isMarker()) stand.setMarker(true);
                        if (!stand.isInvulnerable()) stand.setInvulnerable(true);
                    }
                });
    }
}
