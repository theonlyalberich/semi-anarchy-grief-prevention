package com.alb.wonderplugin.modules.pumpkening;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Handles boss death logic: executes system command and triggers drops.
 */
public class PumpDeath {

    /**
     * @param modelName  The BetterModel name of the mob (used for drops).
     * @param entity     The Bukkit entity that died.
     * @param deathCommand The system command to run on death (from config).
     */
    public static void handleDeath(String modelName, Entity entity, String deathCommand) {
        // Run system command if provided
        if (deathCommand != null && !deathCommand.isEmpty()) {
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    deathCommand.replace("{mob}", entity.getName())
            );
        }

        // Trigger drops
        Location loc = entity.getLocation();
        PumpDrops.dropItems(modelName, loc);
    }
}
