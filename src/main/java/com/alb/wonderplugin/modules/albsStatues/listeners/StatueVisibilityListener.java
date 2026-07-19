package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StatueVisibilityListener implements Listener {

    private final NamespacedKey ownerKey;

    public StatueVisibilityListener(WonderPlugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand stand) {
                PersistentDataContainer data = stand.getPersistentDataContainer();
                if (data.has(ownerKey, PersistentDataType.STRING)) {
                    if (!stand.isInvisible()) stand.setInvisible(true);
                    if (!stand.isMarker()) stand.setMarker(true);
                    if (!stand.isInvulnerable()) stand.setInvulnerable(true);
                }
            }
        }
    }
}
