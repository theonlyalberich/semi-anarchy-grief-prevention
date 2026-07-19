package com.alb.wonderplugin.modules.itemFinder;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;

public class IFProtectionListener implements Listener {

    private final NamespacedKey ownerKey;

    public IFProtectionListener(WonderPlugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Only protect chests and shulkers
        if (block.getState() instanceof Chest chest) {
            PersistentDataContainer data = chest.getPersistentDataContainer();
            if (data.has(ownerKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
            }
        } else if (block.getState() instanceof ShulkerBox shulker) {
            PersistentDataContainer data = shulker.getPersistentDataContainer();
            if (data.has(ownerKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
            }
        }
    }
}
