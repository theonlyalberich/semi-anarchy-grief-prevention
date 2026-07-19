package com.alb.wonderplugin.modules.pumpkening;

import org.bukkit.Location;

public class PumpDrops {
    private static PumpBosses bossManager;

    public static void init(PumpBosses pumpBosses) {
        bossManager = pumpBosses;
    }

    public static void dropItems(String modelName, Location loc) {
        if (bossManager != null) {
            bossManager.debugDrop(modelName, loc, false);
        }
    }
}
