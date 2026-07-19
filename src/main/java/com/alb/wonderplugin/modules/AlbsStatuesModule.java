package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;

public class AlbsStatuesModule implements Module {

    private WonderPlugin plugin;
    private File storageFile;
    private FileConfiguration storage;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;

        // Register commands
        plugin.getCommand("statue").setExecutor(
                new com.alb.wonderplugin.modules.albsStatues.commands.StatueCommand(this)
        );
        plugin.getCommand("modelbreak").setExecutor(
                new com.alb.wonderplugin.modules.albsStatues.commands.ModelBreakCommand(plugin)
        );

        // NEW: Register /modeldisplay
        if (plugin.getCommand("modeldisplay") != null) {
            plugin.getCommand("modeldisplay").setExecutor(
                    new com.alb.wonderplugin.modules.albsStatues.commands.DisplayCommand(plugin)
            );
        }

        // Setup storage.yml
        storageFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!storageFile.exists()) {
            plugin.saveResource("storage.yml", false);
        }
        storage = YamlConfiguration.loadConfiguration(storageFile);

        // Register listeners
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueProtectionListener(plugin), plugin);
        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueVisibilityListener(plugin), plugin);

        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueInventoryListener(plugin), plugin);
        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueInventoryChangeListener(plugin), plugin);
        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueInventoryClickListener(plugin), plugin);

        pm.registerEvents(new com.alb.wonderplugin.modules.albsStatues.listeners.StatueUseListener(this, plugin), plugin);
    }

    @Override
    public void onDisable() {
        saveStorage();
    }

    @Override
    public String getName() {
        return "AlbsStatues";
    }

    public FileConfiguration getStorage() {
        return storage;
    }

    public void saveStorage() {
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
