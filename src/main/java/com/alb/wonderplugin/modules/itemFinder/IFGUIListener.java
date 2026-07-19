package com.alb.wonderplugin.modules.itemFinder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IFGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith("Results:")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.equals("Previous Page")) {
            // handle previous page
        } else if (name.equals("Next Page")) {
            // handle next page
        } else {
            // clicked a container result
            player.closeInventory();
            IFPartickleLogic.startParticles(player, clicked);
        }
    }
}
