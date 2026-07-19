package com.alb.wonderplugin.modules.albsStatues.commands;

import com.alb.wonderplugin.modules.AlbsStatuesModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

public class StatueCommand implements CommandExecutor {

    private final AlbsStatuesModule module;

    public StatueCommand(AlbsStatuesModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || !item.hasItemMeta()) {
                player.sendMessage("You must hold an item.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();

            if (!meta.hasCustomModelData()) {
                player.sendMessage("This item does not have CustomModelData.");
                return true;
            }

            int cmd = meta.getCustomModelData();
            String key = item.getType().getKey().toString() + "_cmd:" + cmd;

            FileConfiguration storage = module.getStorage();

            if (storage.contains("statues." + key)) {
                player.sendMessage("Entry '" + key + "' is already registered.");
                return true;
            }

            storage.set("statues." + key, true);
            module.saveStorage();

            player.sendMessage("Entry '" + key + "' added to storage!");
            return true;
        }

        sender.sendMessage("Usage: /statue add");
        return true;
    }
}
