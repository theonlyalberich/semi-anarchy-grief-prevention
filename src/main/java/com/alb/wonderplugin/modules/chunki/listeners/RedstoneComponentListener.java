package com.alb.wonderplugin.modules.chunki.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.Set;

public class RedstoneComponentListener implements Listener {

    private final WonderPlugin plugin;

    public RedstoneComponentListener(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Chunk chunk = block.getChunk();

        // Families of components
        Map<String, Set<Material>> components = Map.ofEntries(
                Map.entry("redstone_wire", Set.of(Material.REDSTONE_WIRE)),
                Map.entry("redstone_torch", Set.of(Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH)),
                Map.entry("hopper", Set.of(Material.HOPPER)),

                // Chest family (oak, trapped, copper variants)
                Map.entry("chest", Set.of(
                        Material.CHEST,
                        Material.TRAPPED_CHEST,

                        // Copper chest variants (1.21)
                        Material.COPPER_CHEST,
                        Material.EXPOSED_COPPER_CHEST,
                        Material.WEATHERED_COPPER_CHEST,
                        Material.OXIDIZED_COPPER_CHEST,

                        // Waxed copper chest variants
                        Material.WAXED_COPPER_CHEST,
                        Material.WAXED_EXPOSED_COPPER_CHEST,
                        Material.WAXED_WEATHERED_COPPER_CHEST,
                        Material.WAXED_OXIDIZED_COPPER_CHEST
                )),

                // Copper bulb family (1.21 variants)
                Map.entry("copper_bulb", Set.of(
                        Material.COPPER_BULB,
                        Material.EXPOSED_COPPER_BULB,
                        Material.WEATHERED_COPPER_BULB,
                        Material.OXIDIZED_COPPER_BULB,

                        // Waxed copper bulb variants
                        Material.WAXED_COPPER_BULB,
                        Material.WAXED_EXPOSED_COPPER_BULB,
                        Material.WAXED_WEATHERED_COPPER_BULB,
                        Material.WAXED_OXIDIZED_COPPER_BULB
                )),

                Map.entry("repeater", Set.of(Material.REPEATER)),
                Map.entry("comparator", Set.of(Material.COMPARATOR)),
                Map.entry("note_block", Set.of(Material.NOTE_BLOCK)),
                Map.entry("tripwire_hook", Set.of(Material.TRIPWIRE_HOOK)),
                Map.entry("spawner", Set.of(Material.SPAWNER)),
                Map.entry("piston", Set.of(Material.PISTON)),
                Map.entry("sticky_piston", Set.of(Material.STICKY_PISTON))
        );

        // Check each family
        for (String key : components.keySet()) {
            if (components.get(key).contains(block.getType())) {
                int limit = plugin.getConfig().getInt("redstone-component-limits." + key, -1);
                if (limit == -1) return;

                int count = 0;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block b = chunk.getBlock(x, y, z);
                            if (components.get(key).contains(b.getType())) {
                                count++;
                            }
                        }
                    }
                }

                if (count > limit) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Chunk limit reached for " + key + " (" + limit + ")");
                }
                return;
            }
        }
    }
}
