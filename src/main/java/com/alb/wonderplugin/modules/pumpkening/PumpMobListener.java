package com.alb.wonderplugin.modules.pumpkening;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for BetterModel boss mob deaths and triggers drop mechanics.
 */
public class PumpMobListener implements Listener {
    private final PumpBosses pumpBosses;
    private static final Map<Entity, String> bossMap = new HashMap<>();

    public PumpMobListener(PumpBosses pumpBosses) {
        this.pumpBosses = pumpBosses;
    }

    public static void registerBoss(Entity entity, String modelName) {
        bossMap.put(entity, modelName);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        String modelName = bossMap.remove(entity);

        if (modelName != null && pumpBosses.isBossModel(modelName)) {
            pumpBosses.debugDrop(modelName, entity.getLocation(), false);
        }
    }
}
