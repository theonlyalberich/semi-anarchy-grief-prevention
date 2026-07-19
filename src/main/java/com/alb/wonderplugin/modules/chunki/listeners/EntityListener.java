package com.alb.wonderplugin.modules.chunki.listeners;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class EntityListener implements Listener {

    private final WonderPlugin plugin;

    public EntityListener(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    // Item frames (normal + glow)
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        EntityType type = event.getEntity().getType();
        if (type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) return;

        int configLimit = plugin.getConfig().getInt("entity-limits.item_frame", 5);

        Chunk chunk = event.getEntity().getLocation().getChunk();
        int count = 0;

        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
                count++;
            }
        }

        int newTotal = count + 1;
        if (newTotal > configLimit) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "item_frame limit reached (" + configLimit + ")");
        }
    }

    // Armor stands
    @EventHandler
    public void onArmorStandSpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) return;

        int configLimit = plugin.getConfig().getInt("entity-limits.armor_stand", 5);

        Chunk chunk = event.getEntity().getLocation().getChunk();
        int count = 0;

        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == EntityType.ARMOR_STAND) {
                count++;
            }
        }

        int newTotal = count + 1;
        if (newTotal > configLimit) {
            event.setCancelled(true);
            event.getEntity().remove();
            event.getEntity().getWorld().getPlayers().stream()
                    .filter(p -> p.getLocation().getChunk().equals(chunk))
                    .forEach(p -> p.sendMessage(ChatColor.RED + "armor_stand limit reached (" + configLimit + ")"));
        }
    }

    // Mob spawn enforcement
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!isCountedMob(event.getEntity())) return;

        if (!plugin.getConfig().isSet("entity-limits.mobs")) {
            return; // no mob limit defined
        }

        int configLimit = plugin.getConfig().getInt("entity-limits.mobs");

        Chunk chunk = event.getLocation().getChunk();
        int count = 0;
        for (Entity e : chunk.getEntities()) {
            if (isCountedMob(e)) count++;
        }

        // Include the one about to spawn
        int newTotal = count + 1;
        if (newTotal > configLimit) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    // Helpers (static so they can be used in ChunkiModule cleanup task)
    public static boolean isCountedMob(Entity e) {
        EntityType type = e.getType();
        if (!type.isAlive()) return false;
        if (type == EntityType.PLAYER) return false; // exclude players
        if (type == EntityType.ARMOR_STAND || type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) return false;
        if (type == EntityType.WOLF || type == EntityType.CAT) return false;
        if (type.name().toLowerCase().contains("minecart")) return false;
        if (type.name().toLowerCase().contains("copper_golem")) return false; // catches all copper golem variants
        if (type == EntityType.VILLAGER) return false; // exclude villagers
        // Exclude mobs with custom name tags
        if (e.getCustomName() != null && !e.getCustomName().isEmpty()) {
            return false;
        }

        return true;
    }

    public static Entity findDespawnCandidate(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            if (isCountedMob(e)) return e;
        }
        return null;
    }
}
