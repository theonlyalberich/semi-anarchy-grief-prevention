package com.alb.wonderplugin.modules.chunki.commands;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class ChunkiCommand implements CommandExecutor {

    private final WonderPlugin plugin;
    private final FileConfiguration chunkiConfig;
    private final File chunkiFile;

    public ChunkiCommand(WonderPlugin plugin, FileConfiguration chunkiConfig, File chunkiFile) {
        this.plugin = plugin;
        this.chunkiConfig = chunkiConfig;
        this.chunkiFile = chunkiFile;
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
            player.sendMessage(ChatColor.YELLOW + "Usage: /chunki set <limit>, /chunki remove, /chunki help");
            return true;
        }

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
            chunkiConfig.set("optional-component-limits." + key, limit);
            saveChunkiConfig();
            player.sendMessage(ChatColor.GREEN + "Set limit for " + key + " to " + limit);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            Material mat = player.getInventory().getItemInMainHand().getType();
            String key = mat.name().toLowerCase();
            if (chunkiConfig.contains("optional-component-limits." + key)) {
                chunkiConfig.set("optional-component-limits." + key, null);
                saveChunkiConfig();
                player.sendMessage(ChatColor.GREEN + "Removed " + key + " from optional limits.");
            } else {
                player.sendMessage(ChatColor.RED + key + " is not in optional limits.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.YELLOW + "=== Chunki Admin Help ===");
            player.sendMessage(ChatColor.GREEN + "/chunki set <limit> - Set limit for item in hand");
            player.sendMessage(ChatColor.GREEN + "/chunki remove - Remove item in hand from optional limits");
            player.sendMessage(ChatColor.GREEN + "/chunki help - Show this help menu");
            player.sendMessage(ChatColor.GREEN + "/chunk limits - Public command to view chunk usage");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /chunki set <limit>, /chunki remove, /chunki help");
        return true;
    }

    private void saveChunkiConfig() {
        try {
            chunkiConfig.save(chunkiFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save chunki.yml: " + e.getMessage());
        }
    }
}
