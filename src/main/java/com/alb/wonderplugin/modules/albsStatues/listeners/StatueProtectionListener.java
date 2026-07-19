package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StatueProtectionListener implements Listener {

    private final NamespacedKey ownerKey;

    public StatueProtectionListener(WonderPlugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand stand) {
            PersistentDataContainer data = stand.getPersistentDataContainer();
            if (data.has(ownerKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
            }
        }
    }
}
