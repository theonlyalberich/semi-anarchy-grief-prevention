package com.alb.wonderplugin.modules.pumpkening;

import com.alb.wonderplugin.WonderPlugin;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PumpMobCreation implements CommandExecutor {
    private final PumpBosses pumpBosses;

    public PumpMobCreation(WonderPlugin plugin, PumpBosses pumpBosses) {
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

        if (args.length < 2 || !args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(ChatColor.RED + "Usage: /boss <model_name> spawn");
            return true;
        }

        String modelName = args[0].toLowerCase();
        String baseMob = pumpBosses.getBaseMob(modelName);
        if (baseMob == null) {
            player.sendMessage(ChatColor.RED + "No base mob defined for " + modelName);
            return true;
        }

        // Spawn the base mob at player location
        Entity mob = player.getWorld().spawnEntity(
                player.getLocation(),
                EntityType.valueOf(baseMob.toUpperCase())
        );

        // Attach BetterModel to the mob entity
        BetterModel.model(modelName).ifPresentOrElse(renderer -> {
            try {
                renderer.create(BukkitAdapter.adapt(mob));
                PumpMobListener.registerBoss(mob, modelName);
                player.sendMessage(ChatColor.GREEN + "Spawned BetterModel boss " + modelName + " with base mob " + baseMob);
            } catch (Throwable ex) {
                player.sendMessage(ChatColor.RED + "Failed to attach BetterModel: " + ex.getMessage());
            }
        }, () -> player.sendMessage(ChatColor.RED + "Model not found: " + modelName));

        return true;
    }
}
