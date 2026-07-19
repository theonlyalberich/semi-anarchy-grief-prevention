package com.alb.wonderplugin.modules.albsStatues.commands;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.modules.albsStatues.listeners.StatueDestructionListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModelBreakCommand implements CommandExecutor {

    private final StatueDestructionListener destructionListener;

    public ModelBreakCommand(WonderPlugin plugin) {
        // Wire the listener with WonderPlugin context
        this.destructionListener = new StatueDestructionListener(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Delegate to listener logic
        return destructionListener.handleBreakCommand(player);
    }
}
