package com.alb.wonderplugin.modules.chunki.commands;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class ChunkLimitsCommand implements CommandExecutor {

    private final WonderPlugin plugin;

    public ChunkLimitsCommand(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        Chunk chunk = player.getLocation().getChunk();

        player.sendMessage(ChatColor.YELLOW + "=== Chunk Limits ===");

        // Families of components
        Map<String, Set<Material>> components = Map.ofEntries(
                Map.entry("redstone_wire", Set.of(Material.REDSTONE_WIRE)),
                Map.entry("redstone_torch", Set.of(Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH)),
                Map.entry("hopper", Set.of(Material.HOPPER)),

                // Chest family (oak, trapped, copper variants)
                Map.entry("chest", Set.of(
                        Material.CHEST,
                        Material.TRAPPED_CHEST,
                        Material.COPPER_CHEST,
                        Material.EXPOSED_COPPER_CHEST,
                        Material.WEATHERED_COPPER_CHEST,
                        Material.OXIDIZED_COPPER_CHEST,
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

        // Count blocks
        for (String key : components.keySet()) {
            int limit = plugin.getConfig().getInt("redstone-component-limits." + key, -1);
            if (limit == -1) continue;
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
            player.sendMessage(ChatColor.GRAY + key + ": " + count + "/" + limit);
        }

        // Optional items
        if (plugin.getConfig().isConfigurationSection("optional-component-limits")) {
            for (String key : plugin.getConfig().getConfigurationSection("optional-component-limits").getKeys(false)) {
                int limit = plugin.getConfig().getInt("optional-component-limits." + key, -1);
                if (limit == -1) continue;
                int count = 0;
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
                player.sendMessage(ChatColor.GRAY + key + ": " + count + "/" + limit);
            }
        }

        // Entities
        if (plugin.getConfig().isConfigurationSection("entity-limits")) {
            for (String key : plugin.getConfig().getConfigurationSection("entity-limits").getKeys(false)) {
                int limit = plugin.getConfig().getInt("entity-limits." + key, -1);
                if (limit == -1) continue;
                int count = 0;
                for (Entity e : chunk.getEntities()) {
                    if (e.getType().name().equalsIgnoreCase(key)) {
                        count++;
                    }
                }
                player.sendMessage(ChatColor.GRAY + key + ": " + count + "/" + limit);
            }
        }

        return true;
    }
}
