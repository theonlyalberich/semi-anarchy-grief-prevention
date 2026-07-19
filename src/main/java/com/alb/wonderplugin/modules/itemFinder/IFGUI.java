package com.alb.wonderplugin.modules.itemFinder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IFGUI {

    public static void openResultsGUI(Player player, String query) {
        List<IFScanner.ResultEntry> results = IFLogic.getResults(player);

        if (results.isEmpty()) {
            player.sendMessage(query + " not found.");
            return;
        }

        int pageSize = 45; // 5 rows for results, last row for navigation
        int totalPages = (int) Math.ceil(results.size() / (double) pageSize);

        openPage(player, results, query, 1, totalPages);
    }

    public static void openPage(Player player, List<IFScanner.ResultEntry> results, String query, int page, int totalPages) {
        int pageSize = 45;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, results.size());

        Inventory gui = Bukkit.createInventory(null, 54, "Results: " + query + " (Page " + page + "/" + totalPages + ")");

        for (int i = start; i < end; i++) {
            IFScanner.ResultEntry entry = results.get(i);
            Material containerIcon = getContainerIcon(entry.type());
            ItemStack item = new ItemStack(containerIcon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(entry.type() + " containing " + entry.item().name());
            meta.setLore(List.of(
                    "Location: " + entry.loc().getBlockX() + "," + entry.loc().getBlockY() + "," + entry.loc().getBlockZ(),
                    "Amount: " + entry.amount()
            ));
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        // Navigation buttons
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName("Previous Page");
            prev.setItemMeta(meta);
            gui.setItem(45, prev);
        }
        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName("Next Page");
            next.setItemMeta(meta);
            gui.setItem(53, next);
        }

        player.openInventory(gui);
    }

    private static Material getContainerIcon(String containerType) {
        return switch (containerType) {
            case "CHEST" -> Material.CHEST;
            case "TRAPPED_CHEST" -> Material.TRAPPED_CHEST;
            case "BARREL" -> Material.BARREL;
            case "HOPPER" -> Material.HOPPER;
            case "DROPPER" -> Material.DROPPER;
            case "DISPENSER" -> Material.DISPENSER;
            case "SHULKER_BOX" -> Material.SHULKER_BOX;
            case "WHITE_SHULKER_BOX" -> Material.WHITE_SHULKER_BOX;
            case "ORANGE_SHULKER_BOX" -> Material.ORANGE_SHULKER_BOX;
            case "MAGENTA_SHULKER_BOX" -> Material.MAGENTA_SHULKER_BOX;
            case "LIGHT_BLUE_SHULKER_BOX" -> Material.LIGHT_BLUE_SHULKER_BOX;
            case "YELLOW_SHULKER_BOX" -> Material.YELLOW_SHULKER_BOX;
            case "LIME_SHULKER_BOX" -> Material.LIME_SHULKER_BOX;
            case "PINK_SHULKER_BOX" -> Material.PINK_SHULKER_BOX;
            case "GRAY_SHULKER_BOX" -> Material.GRAY_SHULKER_BOX;
            case "LIGHT_GRAY_SHULKER_BOX" -> Material.LIGHT_GRAY_SHULKER_BOX;
            case "CYAN_SHULKER_BOX" -> Material.CYAN_SHULKER_BOX;
            case "PURPLE_SHULKER_BOX" -> Material.PURPLE_SHULKER_BOX;
            case "BLUE_SHULKER_BOX" -> Material.BLUE_SHULKER_BOX;
            case "BROWN_SHULKER_BOX" -> Material.BROWN_SHULKER_BOX;
            case "GREEN_SHULKER_BOX" -> Material.GREEN_SHULKER_BOX;
            case "RED_SHULKER_BOX" -> Material.RED_SHULKER_BOX;
            case "BLACK_SHULKER_BOX" -> Material.BLACK_SHULKER_BOX;
            default -> Material.CHEST; // Default to chest if unknown
        };
    }
}
