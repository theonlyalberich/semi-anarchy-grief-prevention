package com.alb.wonderplugin.modules.albsStatues.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Comparator;

public class StatueDestructionListener implements Listener {

    private final NamespacedKey ownerKey;

    public StatueDestructionListener(WonderPlugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    // Command handler method
    public boolean handleBreakCommand(Player player) {
        // Find closest armor stand within 2 blocks
        ArmorStand closest = player.getNearbyEntities(2, 2, 2).stream()
                .filter(e -> e instanceof ArmorStand)
                .map(e -> (ArmorStand) e)
                .min(Comparator.comparingDouble(e -> e.getLocation().distance(player.getLocation())))
                .orElse(null);

        if (closest == null) {
            player.sendMessage("No statue found within 2 blocks.");
            return true;
        }

        // Check ownership
        PersistentDataContainer data = closest.getPersistentDataContainer();
        String ownerUuid = data.get(ownerKey, PersistentDataType.STRING);

        boolean isOwner = ownerUuid != null && ownerUuid.equals(player.getUniqueId().toString());
        boolean hasPermission = player.hasPermission("statue");
        boolean isOp = player.isOp();

        if (!(isOwner || hasPermission || isOp)) {
            player.sendMessage("You do not have permission to break this statue.");
            return true;
        }

        // Drop items from armor stand slots
        ItemStack[] equipment = {
                closest.getEquipment().getHelmet(),
                closest.getEquipment().getChestplate(),
                closest.getEquipment().getLeggings(),
                closest.getEquipment().getBoots(),
                closest.getEquipment().getItemInMainHand(),
                closest.getEquipment().getItemInOffHand()
        };

        for (ItemStack item : equipment) {
            if (item != null) {
                closest.getWorld().dropItemNaturally(closest.getLocation(), item);
            }
        }

        // Remove the armor stand
        closest.remove();
        player.sendMessage("Statue removed.");

        return true;
    }
}
