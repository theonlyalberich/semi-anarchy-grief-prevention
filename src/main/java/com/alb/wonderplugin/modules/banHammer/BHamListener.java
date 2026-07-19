package com.alb.wonderplugin.modules.banHammer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class BHamListener implements Listener {
    private final FileConfiguration hammerConfig;

    public BHamListener() {
        File file = new File(Bukkit.getPluginManager().getPlugin("WonderPlugin").getDataFolder(), "hammer.yml");
        hammerConfig = YamlConfiguration.loadConfiguration(file);
    }

    @EventHandler
    public void onBanHammerUse(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player wielder = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        ItemStack item = wielder.getInventory().getItemInMainHand();

        if (!BHamCommand.isBanHammer(item)) {
            return;
        }

        // Run ban logic
        BHamLogic.handleBan(wielder, target, item);

        // Spawn 3 damaging lightning bolts that last ~3 seconds
        spawnPersistentDamagingLightning(target);

        // Skip broadcast in Creative mode; onSwing handler will broadcast instead
        if (wielder.getGameMode() != GameMode.CREATIVE) {
            broadcastBanMessage(wielder, target, item);
        }
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        Player wielder = event.getPlayer();
        ItemStack item = wielder.getInventory().getItemInMainHand();

        if (!BHamCommand.isBanHammer(item)) return;

        // If wielder is swinging in Creative, damage event won’t fire, so ray trace
        if (wielder.getGameMode() == GameMode.CREATIVE) {
            Entity targetEntity = wielder.getTargetEntity(5); // 5 block reach
            if (targetEntity instanceof Player) {
                Player target = (Player) targetEntity;

                BHamLogic.handleBan(wielder, target, item);
                spawnPersistentDamagingLightning(target);
                broadcastBanMessage(wielder, target, item);
            }
        }
    }

    private void spawnPersistentDamagingLightning(Player target) {
        for (int i = 0; i < 3; i++) {
            LightningStrike strike = target.getWorld().spawn(target.getLocation(), LightningStrike.class);
            // This is a real lightning strike: it deals damage and plays thunder

            // Remove after 3 seconds (60 ticks) so they don’t linger forever
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("WonderPlugin"),
                    strike::remove,
                    60
            );
        }
    }

    private void broadcastBanMessage(Player wielder, Player target, ItemStack item) {
        String template = hammerConfig.getString("ban_message");
        if (template == null) {
            Bukkit.getLogger().severe("hammer.yml missing ban_message key!");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        String itemName;
        if (meta != null && meta.hasDisplayName()) {
            itemName = meta.getDisplayName();
        } else {
            itemName = hammerConfig.getString("default_name", ChatColor.DARK_RED + "BanHammer");
        }

        String message = template
                .replace("%target%", target.getName())
                .replace("%player%", wielder.getName())
                .replace("%item_name%", itemName);

        Bukkit.broadcastMessage(ChatColor.RED + message);
    }
}
