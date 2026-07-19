package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.modules.pumpkening.*;
import org.bukkit.plugin.PluginManager;

/**
 * Bootstrap class for the Pumpkening system.
 * Handles boss registration, drop configuration, debug commands, and mob spawning.
 */
public class PumpkeningModule implements Module {
    private PumpBosses pumpBosses;

    @Override
    public String getName() {
        return "Pumpkening";
    }

    @Override
    public void onEnable(WonderPlugin plugin) {
        // Initialize boss manager
        pumpBosses = new PumpBosses(plugin);

        // Register admin commands (/addpump, /droppump)
        new PumpCommands(plugin, pumpBosses);

        // Register debug commands (/boss <model_name> drop|dropall)
        new PDBugCommands(plugin, pumpBosses);

        // Register spawn command (/boss <model_name> spawn)
        new PumpMobCreation(plugin, pumpBosses);

        // Register mob death listener
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new PumpMobListener(pumpBosses), plugin);

        // Initialize drops system
        PumpDrops.init(pumpBosses);

        plugin.getLogger().info("Pumpkening module enabled.");
    }

    @Override
    public void onDisable() {
        // Nothing special yet, bosses are persisted immediately
        pumpBosses = null;
    }
}
