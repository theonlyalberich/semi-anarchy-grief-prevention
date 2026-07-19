package com.alb.wonderplugin.modules.chunki.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class OptionalComponentListener implements Listener {

    private final WonderPlugin plugin;

    public OptionalComponentListener(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Material mat = block.getType();

        // Look up optional limit by material name
        String key = mat.name().toLowerCase();
        int configLimit = plugin.getConfig().getInt("optional-component-limits." + key, -1);

        if (configLimit == -1) return; // not tracked, allow placement

        int effectiveLimit = configLimit + 1; // total listening logic

        Chunk chunk = block.getChunk();
        int count = 0;

        // Scan blocks in chunk
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block b = chunk.getBlock(x, y, z);
                    if (b.getType().name().equalsIgnoreCase(key)) {
                        count++;
                    }
                }
            }
        }

        int newTotal = count + 1;

        if (newTotal > effectiveLimit) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + key + " limit reached (" + configLimit + ")");
        }
    }
}
