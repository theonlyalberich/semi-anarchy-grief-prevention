package com.alb.wonderplugin.util;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class UpdateHelper implements Module {

    private WonderPlugin plugin;
    private File backupDir;
    private File backupConfig;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;

        // Ensure backup folder exists
        backupDir = new File(plugin.getDataFolder(), "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Path to backup config
        backupConfig = new File(backupDir, "config-backup.yml");

        // If backup exists, merge disabled keys into current config
        if (backupConfig.exists()) {
            mergeDisabledKeys(plugin.getConfig(), YamlConfiguration.loadConfiguration(backupConfig));
            try {
                plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save merged config.yml: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        // On shutdown, move current config.yml to backup folder
        File currentConfig = new File(plugin.getDataFolder(), "config.yml");
        if (currentConfig.exists()) {
            if (backupConfig.exists()) {
                backupConfig.delete();
            }
            boolean success = currentConfig.renameTo(backupConfig);
            if (success) {
                plugin.getLogger().info("Moved config.yml to backup folder.");
            } else {
                plugin.getLogger().warning("Failed to move config.yml to backup folder.");
            }
        }
    }

    @Override
    public String getName() {
        return "UpdateHelper";
    }

    /**
     * Merge disabled keys from old backup into new config.
     * Keeps new keys intact, but restores disabled states.
     */
    private void mergeDisabledKeys(FileConfiguration current, FileConfiguration backup) {
        ConfigurationSection curModules = current.getConfigurationSection("modules");
        ConfigurationSection oldModules = backup.getConfigurationSection("modules");

        if (curModules == null || oldModules == null) return;

        for (String key : oldModules.getKeys(false)) {
            if (curModules.contains(key)) {
                boolean oldEnabled = oldModules.getBoolean(key + ".enabled");
                boolean newEnabled = curModules.getBoolean(key + ".enabled");
                if (!oldEnabled && newEnabled) {
                    // Respect old disabled state
                    curModules.set(key + ".enabled", false);
                    plugin.getLogger().info("Restored disabled state for module: " + key);
                }
            }
        }
    }
}
