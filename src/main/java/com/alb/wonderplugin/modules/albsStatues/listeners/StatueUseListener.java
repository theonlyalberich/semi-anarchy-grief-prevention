package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.modules.AlbsStatuesModule;
import com.alb.wonderplugin.modules.albsStatues.mechanics.StatueMechanics;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class StatueUseListener implements Listener {

    private final AlbsStatuesModule module;
    private final StatueMechanics mechanics;

    public StatueUseListener(AlbsStatuesModule module, WonderPlugin plugin) {
        this.module = module;
        this.mechanics = new StatueMechanics(plugin);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        // Only allow right‑click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return; // not a valid statue item
        }

        int cmd = item.getItemMeta().getCustomModelData();
        String key = item.getType().getKey().toString() + "_cmd:" + cmd;

        FileConfiguration storage = module.getStorage();
        if (!storage.contains("statues." + key)) {
            return; // not registered
        }

        // 🔒 GriefPrevention build permission check
        Location targetLocation = event.getClickedBlock() != null
                ? event.getClickedBlock().getLocation()
                : player.getLocation();

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(targetLocation, false, null);
        if (claim != null) {
            String denial = claim.allowBuild(player, item.getType());
            if (denial != null) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have "
                        + claim.getOwnerName() + "'s permission to place that here.");
                return;
            }
        }

        // ✅ Allowed: place statue
        mechanics.handlePlacement(player, key);
        event.setCancelled(true);
    }
}
