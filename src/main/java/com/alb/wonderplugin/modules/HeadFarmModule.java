package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HeadFarmModule implements Module, Listener {
    private WonderPlugin plugin;
    private NamespacedKey creeperTagKey;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        this.creeperTagKey = new NamespacedKey(plugin, "charged_creeper_tagged");
        boolean enabled = plugin.getConfig().getBoolean("modules.headfarm.enabled", true);
        if (enabled) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void onDisable() {}

    @Override
    public String getName() {
        return "HeadFarm";
    }

    @EventHandler
    public void onChargedCreeperExplosion(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Creeper creeper && creeper.isPowered()) {
            if (event.getEntity() instanceof Player player) {
                // Tag player with timestamp
                PersistentDataContainer data = player.getPersistentDataContainer();
                data.set(creeperTagKey, PersistentDataType.LONG, System.currentTimeMillis());

                double finalHealth = player.getHealth() - event.getFinalDamage();

                // If this hit would kill the player, override it
                if (finalHealth <= 0.0) {
                    event.setCancelled(true); // cancel lethal damage

                    // Clamp to 0.1% of max health
                    double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    double minHealth = maxHealth * 0.001;
                    // Ensure at least 0.5 HP so they don't die instantly
                    if (minHealth < 0.5) minHealth = 0.5;

                    player.setHealth(minHealth);

                    player.sendMessage(ChatColor.RED + "A charged creeper left you barely alive!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PersistentDataContainer data = player.getPersistentDataContainer();
        Long taggedTime = data.get(creeperTagKey, PersistentDataType.LONG);

        if (taggedTime != null) {
            if (System.currentTimeMillis() - taggedTime <= 150000L) {
                // Spawn a temporary powered creeper that only exists as killer
                Creeper fake = player.getWorld().spawn(player.getLocation(), Creeper.class, c -> {
                    c.setPowered(true);
                    c.setInvisible(true);
                    c.setSilent(true);
                    c.setAI(false); // disables pathfinding/attacking
                });

                // Force death message to match creeper kill
                event.setDeathMessage(player.getName() + " was blown up by a charged creeper");

                // Drop the player’s head
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(player);
                head.setItemMeta(meta);
                player.getWorld().dropItemNaturally(player.getLocation(), head);

                player.sendMessage(ChatColor.GREEN + "Your head has been claimed by the charged creeper!");

                // Remove the fake creeper immediately so it never interacts with anything else
                Bukkit.getScheduler().runTaskLater(plugin, fake::remove, 1L);
            }
            // Clear tag after death
            data.remove(creeperTagKey);
        }
    }
}
