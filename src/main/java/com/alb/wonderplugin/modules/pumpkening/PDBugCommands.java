package com.alb.wonderplugin.modules.pumpkening;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PDBugCommands implements CommandExecutor {
    private final PumpBosses pumpBosses;

    public PDBugCommands(WonderPlugin plugin, PumpBosses pumpBosses) {
        this.pumpBosses = pumpBosses;
        plugin.getCommand("boss").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Only ops can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /boss <model_name> <drop|dropall>");
            return true;
        }

        String modelName = args[0].toLowerCase();
        String action = args[1].toLowerCase();
        Location loc = player.getLocation();

        if (action.equals("drop")) {
            pumpBosses.debugDrop(modelName, loc, false);
            player.sendMessage(ChatColor.GREEN + "Simulated drop for boss '" + modelName + "' (respecting chance).");
            return true;
        }

        if (action.equals("dropall")) {
            pumpBosses.debugDrop(modelName, loc, true);
            player.sendMessage(ChatColor.GREEN + "Simulated drop for boss '" + modelName + "' (all items).");
            return true;
        }

        player.sendMessage(ChatColor.RED + "Unknown action. Use drop or dropall.");
        return true;
    }
}
