package com.alb.wonderplugin.modules.pumpkening;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages Pump bosses and their drop configurations.
 */
public class PumpBosses {
    private final WonderPlugin plugin;
    private final File pumpFolder;
    private final File configFile;
    private final YamlConfiguration config;

    public PumpBosses(WonderPlugin plugin) {
        this.plugin = plugin;

        this.pumpFolder = new File(plugin.getDataFolder(), "pump");
        if (!pumpFolder.exists()) {
            pumpFolder.mkdirs();
        }

        this.configFile = new File(pumpFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void addBoss(String modelName, String baseMob) {
        config.set("bosses." + modelName + ".base_mob", baseMob);
        saveConfig();

        File bossFile = new File(pumpFolder, modelName + ".yml");
        if (!bossFile.exists()) {
            try {
                bossFile.createNewFile();
                YamlConfiguration bossCfg = YamlConfiguration.loadConfiguration(bossFile);
                bossCfg.set("base_mob", baseMob);
                bossCfg.save(bossFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addDrop(String modelName, ItemStack item, int chance) {
        File bossFile = new File(pumpFolder, modelName + ".yml");
        if (!bossFile.exists()) {
            plugin.getLogger().warning("Boss file for " + modelName + " not found.");
            return;
        }

        YamlConfiguration bossCfg = YamlConfiguration.loadConfiguration(bossFile);
        int safeChance = Math.max(0, Math.min(100, chance));

        List<Map<String, Object>> drops = (List<Map<String, Object>>) bossCfg.getList("drops", new ArrayList<>());
        Map<String, Object> dropEntry = new HashMap<>();
        dropEntry.put("chance", safeChance);
        dropEntry.put("item", item.serialize());

        drops.add(dropEntry);
        bossCfg.set("drops", drops);

        try {
            bossCfg.save(bossFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debugDrop(String modelName, Location loc, boolean dropAll) {
        File bossFile = new File(pumpFolder, modelName + ".yml");
        if (!bossFile.exists()) {
            plugin.getLogger().warning("Boss file for " + modelName + " not found.");
            return;
        }

        YamlConfiguration bossCfg = YamlConfiguration.loadConfiguration(bossFile);
        List<Map<?, ?>> drops = (List<Map<?, ?>>) bossCfg.getList("drops", new ArrayList<>());

        for (Map<?, ?> entry : drops) {
            Object chanceObj = entry.get("chance");
            int chance = (chanceObj instanceof Number) ? ((Number) chanceObj).intValue() : 100;

            Object itemData = entry.get("item");
            if (!(itemData instanceof Map)) continue;

            ItemStack stack = ItemStack.deserialize((Map<String, Object>) itemData);

            boolean shouldDrop = dropAll || (ThreadLocalRandom.current().nextInt(100) < chance);
            if (shouldDrop) {
                loc.getWorld().dropItemNaturally(loc, stack);
            }
        }
    }

    public List<ItemStack> getDrops(String modelName) {
        File bossFile = new File(pumpFolder, modelName + ".yml");
        if (!bossFile.exists()) return Collections.emptyList();

        YamlConfiguration bossCfg = YamlConfiguration.loadConfiguration(bossFile);
        List<Map<?, ?>> drops = (List<Map<?, ?>>) bossCfg.getList("drops", new ArrayList<>());

        List<ItemStack> items = new ArrayList<>();
        for (Map<?, ?> entry : drops) {
            Object itemData = entry.get("item");
            if (itemData instanceof Map) {
                ItemStack stack = ItemStack.deserialize((Map<String, Object>) itemData);
                items.add(stack);
            }
        }
        return items;
    }

    public String getBaseMob(String modelName) {
        File bossFile = new File(pumpFolder, modelName + ".yml");
        if (!bossFile.exists()) return null;
        YamlConfiguration bossCfg = YamlConfiguration.loadConfiguration(bossFile);
        return bossCfg.getString("base_mob");
    }

    public boolean isBossModel(String modelName) {
        File bossFile = new File(pumpFolder, modelName + ".yml");
        return bossFile.exists();
    }
}
