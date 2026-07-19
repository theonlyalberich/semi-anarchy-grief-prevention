package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StatueInventoryClickListener implements Listener {

    private final WonderPlugin plugin;

    public StatueInventoryClickListener(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            refreshStatuesFor(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            refreshStatuesFor(player);
        }
    }

    private void refreshStatuesFor(Player player) {
        NamespacedKey key = new NamespacedKey(plugin, "owner");

        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
            PersistentDataContainer data = stand.getPersistentDataContainer();
            if (data.has(key, PersistentDataType.STRING)) {
                stand.setInvisible(true);
                stand.setMarker(true);
                stand.setInvulnerable(true);

                for (org.bukkit.inventory.EquipmentSlot slot : org.bukkit.inventory.EquipmentSlot.values()) {
                    stand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                    stand.addEquipmentLock(slot, ArmorStand.LockType.ADDING);
                }
            }
        }
    }
}
