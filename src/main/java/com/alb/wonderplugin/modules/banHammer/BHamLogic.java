package com.alb.wonderplugin.modules.banHammer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BHamLogic {
    public static void handleBan(Player wielder, Player target, ItemStack item) {
        if (!BHamCommand.isBanHammer(item)) {
            return; // Not a ban hammer
        }

        // Preserve Bedrock names (including leading dot)
        String targetName = target.getName();

        // Run ban command as wielder
        String banCmd = "ban " + targetName + " ban hammer no reason needed";
        wielder.performCommand(banCmd);
    }
}
