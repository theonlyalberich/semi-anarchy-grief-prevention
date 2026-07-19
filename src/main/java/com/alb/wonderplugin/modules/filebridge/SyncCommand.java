package com.alb.wonderplugin.modules.filebridge;

import com.alb.wonderplugin.modules.FileBridgeModule;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SyncCommand implements CommandExecutor {

    private final FileBridgeModule module;

    public SyncCommand(FileBridgeModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Double lock: must be OP and have permission node
        if (!(sender.isOp() && sender.hasPermission("filebridge.use"))) {
            sender.sendMessage(ChatColor.RED + "You must be OP and have filebridge.use permission to run this command.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("import") && args[1].equalsIgnoreCase("config")) {
            sender.sendMessage(ChatColor.YELLOW + "Importing from config..." + ChatColor.RESET);
            module.startDownloadFromConfig();
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("import") && args[1].equalsIgnoreCase("zips")) {
            sender.sendMessage(ChatColor.YELLOW + "Importing from local zips folder..." + ChatColor.RESET);
            module.startDownloadFromZips();
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("import") && args[1].equalsIgnoreCase("link")) {
            String customLink = args[2];
            sender.sendMessage(ChatColor.YELLOW + "Importing from provided link..." + ChatColor.RESET);
            module.startDownloadFromLink(customLink);
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("import") && args[1].equalsIgnoreCase("plugin")) {
            String customLink = args[2];
            sender.sendMessage(ChatColor.YELLOW + "Downloading raw plugin file..." + ChatColor.RESET);
            module.startDownloadRaw(customLink);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/bridge import config|zips|link <url> OR /bridge import plugin <url>");
        return true;
    }
}
