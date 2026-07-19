package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.modules.banHammer.BHamCommand;
import com.alb.wonderplugin.modules.banHammer.BHamListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BanHammerModule implements Module {
    @Override
    public void onEnable(WonderPlugin plugin) {
        // Ensure hammer.yml exists
        File hammerFile = new File(plugin.getDataFolder(), "hammer.yml");
        if (!hammerFile.exists()) {
            try {
                hammerFile.getParentFile().mkdirs();
                hammerFile.createNewFile();

                FileConfiguration hammerConfig = YamlConfiguration.loadConfiguration(hammerFile);
                hammerConfig.set("ban_message", "%target% has been struck down by %player% using %item_name%");
                hammerConfig.set("default_name", "BanHammer");
                hammerConfig.save(hammerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create hammer.yml: " + e.getMessage());
            }
        }

        // Register the /bham command executor
        if (plugin.getCommand("bham") != null) {
            plugin.getCommand("bham").setExecutor(new BHamCommand());
        }

        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(new BHamListener(), plugin);
    }

    @Override
    public void onDisable() {
        // Nothing to clean up here
    }

    @Override
    public String getName() {
        return "BanHammer";
    }
}
