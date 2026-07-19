package com.alb.wonderplugin.modules.banHammer;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BHamCommand implements CommandExecutor {
    // Permanent marker key for BanHammer items
    private static final NamespacedKey BANHAMMER_KEY =
            new NamespacedKey("wonderplugin", "banhammer");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Only OPs may use /bham.");
            return true;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item to mark it as a Ban Hammer.");
            return true;
        }

        ItemMeta meta = inHand.getItemMeta();
        String originalName = (meta != null && meta.hasDisplayName())
                ? meta.getDisplayName()
                : inHand.getType().name();

        if (meta != null) {
            // Permanently tag the item as a BanHammer
            meta.getPersistentDataContainer().set(BANHAMMER_KEY, PersistentDataType.BYTE, (byte) 1);

            // If no display name exists, set default BanHammer
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(ChatColor.DARK_RED + "BanHammer");
            }

            inHand.setItemMeta(meta);
        }

        player.sendMessage(ChatColor.GREEN + "Marked " + originalName + " as a Ban Hammer!");
        return true;
    }

    public static boolean isBanHammer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(BANHAMMER_KEY, PersistentDataType.BYTE);
    }
}
