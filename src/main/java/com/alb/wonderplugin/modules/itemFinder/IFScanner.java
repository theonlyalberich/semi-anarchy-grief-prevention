package com.alb.wonderplugin.modules.itemFinder;

import com.alb.wonderplugin.WonderPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IFScanner {

    private static WonderPlugin plugin;

    public static void init(WonderPlugin pl) {
        plugin = pl;
    }

    public record ResultEntry(Location loc, Material item, int amount, String type) {
        @Override
        public String toString() {
            return type + " (" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ") "
                    + item.name() + " [" + amount + "]";
        }
    }

    public static List<ResultEntry> scanClaim(Claim claim, Material material) {
        List<ResultEntry> results = new ArrayList<>();

        // Iterate all loaded chunks in the world
        for (Chunk chunk : claim.getLesserBoundaryCorner().getWorld().getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                // Only consider containers inside the claim boundaries
                if (claim.contains(state.getLocation(), false, false) && state instanceof Container container) {
                    int amount = countItems(container.getInventory(), material);
                    if (amount > 0) {
                        results.add(new ResultEntry(
                                state.getLocation(),
                                material,
                                amount,
                                state.getType().name() // e.g. CHEST, BARREL, HOPPER, DROPPER, DISPENSER
                        ));
                    }
                }
            }
        }
        return results;
    }

    private static int countItems(Inventory inv, Material material) {
        int total = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public static List<ResultEntry> scanClaimByDisplayName(Claim claim, String displayNameQuery) {
        List<ResultEntry> results = new ArrayList<>();
        String query = displayNameQuery.toLowerCase();

        // Iterate all loaded chunks in the world
        for (Chunk chunk : claim.getLesserBoundaryCorner().getWorld().getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                // Only consider containers inside the claim boundaries
                if (claim.contains(state.getLocation(), false, false) && state instanceof Container container) {
                    List<ItemStack> foundItems = findItemsByDisplayName(container.getInventory(), query);
                    for (ItemStack stack : foundItems) {
                        results.add(new ResultEntry(
                                state.getLocation(),
                                stack.getType(),
                                stack.getAmount(),
                                state.getType().name()
                        ));
                    }
                }
            }
        }
        return results;
    }

    private static List<ItemStack> findItemsByDisplayName(Inventory inv, String displayNameQuery) {
        List<ItemStack> found = new ArrayList<>();
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && !stack.isEmpty()) {
                String displayName = null;
                if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                    displayName = stack.getItemMeta().getDisplayName().toLowerCase();
                }
                if (displayName != null && displayName.contains(displayNameQuery)) {
                    found.add(stack);
                }
            }
        }
        return found;
    }
}
