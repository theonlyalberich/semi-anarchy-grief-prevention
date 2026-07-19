package com.alb.wonderplugin.modules.albsStatues.commands;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.modules.albsStatues.mechanics.StatueMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DisplayCommand implements CommandExecutor {

    private final StatueMechanics mechanics;

    public DisplayCommand(WonderPlugin plugin) {
        this.mechanics = new StatueMechanics(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can use this command
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission("statue.display")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use /modeldisplay.");
            return true;
        }

        // Get item in hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            player.sendMessage(ChatColor.RED + "You must hold an item with CustomModelData to display.");
            return true;
        }

        // Build key from item type + CMD
        int cmd = item.getItemMeta().getCustomModelData();
        String key = item.getType().getKey().toString() + "_cmd:" + cmd;

        // Directly run placement logic (no storage check)
        mechanics.handlePlacement(player, key);
        player.sendMessage(ChatColor.GREEN + "Displayed model from item in hand.");

        return true;
    }
}
