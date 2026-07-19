package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class StatueInventoryListener implements Listener {

    private final WonderPlugin plugin;
    private final NamespacedKey ownerKey;
    private final Set<Player> scheduledRefresh = new HashSet<>();

    public StatueInventoryListener(WonderPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            scheduleRefresh(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            scheduleRefresh(player);
        }
    }

    private void scheduleRefresh(Player player) {
        if (scheduledRefresh.contains(player)) return;
        scheduledRefresh.add(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            refreshNearbyStatues(player);
            scheduledRefresh.remove(player);
        });
    }

    private void refreshNearbyStatues(Player player) {
        player.getNearbyEntities(32, 32, 32).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .forEach(stand -> {
                    PersistentDataContainer data = stand.getPersistentDataContainer();
                    if (data.has(ownerKey, PersistentDataType.STRING)) {
                        if (!stand.isInvisible()) stand.setInvisible(true);
                        if (!stand.isMarker()) stand.setMarker(true);
                        if (!stand.isInvulnerable()) stand.setInvulnerable(true);
                    }
                });
    }
}
