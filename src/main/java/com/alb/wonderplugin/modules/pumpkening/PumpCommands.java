package com.alb.wonderplugin.modules.pumpkening;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PumpCommands implements CommandExecutor {
    private final PumpBosses pumpBosses;

    public PumpCommands(WonderPlugin plugin, PumpBosses pumpBosses) {
        this.pumpBosses = pumpBosses;
        plugin.getCommand("addpump").setExecutor(this);
        plugin.getCommand("droppump").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Only ops can use this command.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("addpump")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /addpump <model_name> <base_mob>");
                return true;
            }
            pumpBosses.addBoss(args[0], args[1]);
            player.sendMessage(ChatColor.GREEN + "Pump boss '" + args[0] + "' registered with base mob '" + args[1] + "'.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("droppump")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /droppump <model_name> <chance>");
                return true;
            }
            String modelName = args[0].toLowerCase();
            int chance;
            try {
                chance = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Chance must be a number.");
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType().isAir()) {
                player.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
                return true;
            }

            pumpBosses.addDrop(modelName, inHand, chance);
            player.sendMessage(ChatColor.GREEN + "Added drop to '" + modelName + "' with chance " + chance + "%.");
            return true;
        }

        return false;
    }
}
