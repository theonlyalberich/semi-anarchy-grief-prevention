package com.alb.wonderplugin.modules.chunki.commands;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkiSetCommand implements CommandExecutor {

    private final WonderPlugin plugin;

    public ChunkiSetCommand(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("c.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /chunki set <limit> or /chunki remove");
            return true;
        }

        // --- /chunki set <limit> ---
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /chunki set <limit>");
                return true;
            }

            int limit;
            try {
                limit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Limit must be a number.");
                return true;
            }

            Material mat = player.getInventory().getItemInMainHand().getType();
            String key = mat.name().toLowerCase();

            plugin.getConfig().set("optional-component-limits." + key, limit);
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Set limit for " + key + " to " + limit);
            return true;
        }

        // --- /chunki remove ---
        if (args[0].equalsIgnoreCase("remove")) {
            Material mat = player.getInventory().getItemInMainHand().getType();
            String key = mat.name().toLowerCase();

            if (plugin.getConfig().contains("optional-component-limits." + key)) {
                plugin.getConfig().set("optional-component-limits." + key, null);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Removed " + key + " from optional limits.");
            } else {
                player.sendMessage(ChatColor.RED + key + " is not in optional limits.");
            }
            return true;
        }

        // Unknown subcommand
        player.sendMessage(ChatColor.RED + "Usage: /chunki set <limit> or /chunki remove");
        return true;
    }
}
