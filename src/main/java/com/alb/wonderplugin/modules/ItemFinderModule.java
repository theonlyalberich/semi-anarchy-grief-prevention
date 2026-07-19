package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.modules.itemFinder.IFCommands;
import com.alb.wonderplugin.modules.itemFinder.IFLogic;
import com.alb.wonderplugin.modules.itemFinder.IFScanner;
import com.alb.wonderplugin.modules.itemFinder.IFProtectionListener;
import com.alb.wonderplugin.modules.itemFinder.IFGUIListener;
import org.bukkit.plugin.PluginManager;

public class ItemFinderModule implements Module {

    private WonderPlugin plugin;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        PluginManager pm = plugin.getServer().getPluginManager();

        // Register command executors
        plugin.getCommand("find").setExecutor(new IFCommands(plugin));
        plugin.getCommand("findname").setExecutor(new IFCommands(plugin));

        // Register GUI listener (handles inventory clicks, page navigation, particle trigger)
        pm.registerEvents(new IFGUIListener(), plugin);

        // Register protection listener (chests/shulkers)
        pm.registerEvents(new IFProtectionListener(plugin), plugin);

        // Initialize logic and scanner singletons
        IFLogic.init(plugin);
        IFScanner.init(plugin);

        // No need to register IFPartickleLogic or IFCooldownLogic as listeners,
        // they are utility classes invoked by IFGUIListener.
    }

    @Override
    public void onDisable() {
        // Nothing to clean up yet
    }

    @Override
    public String getName() {
        return "ItemFinderModule";
    }
}
