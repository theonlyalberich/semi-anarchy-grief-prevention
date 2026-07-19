package com.alb.wonderplugin.modules.albsStatues.mechanics;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StatueMechanics {

    private final NamespacedKey ownerKey;

    // Constructor takes WonderPlugin to build the NamespacedKey
    public StatueMechanics(WonderPlugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "owner");
    }

    public void handlePlacement(Player player, String key) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("You must hold an item.");
            return;
        }

        Location loc = player.getLocation();

        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setGravity(false);
            armorStand.setInvisible(true);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setRotation(loc.getYaw(), loc.getPitch());

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                armorStand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                armorStand.addEquipmentLock(slot, ArmorStand.LockType.ADDING);
            }

            PersistentDataContainer data = armorStand.getPersistentDataContainer();
            data.set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

            armorStand.setCustomName(" ");
            armorStand.setCustomNameVisible(false);
        });

        // Always helmet slot
        stand.getEquipment().setHelmet(item);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage("Statue set, do [/modelbreak] to undo.");
    }
}
