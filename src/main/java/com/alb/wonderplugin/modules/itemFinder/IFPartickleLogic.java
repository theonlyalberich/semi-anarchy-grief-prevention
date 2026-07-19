package com.alb.wonderplugin.modules.itemFinder;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class IFPartickleLogic {

    public static void startParticles(Player player, ItemStack clicked) {
        if (IFCooldownLogic.isOnCooldown(player)) {
            player.sendMessage("You must wait " + IFCooldownLogic.getRemaining(player) + "s before using this again.");
            return;
        }

        List<String> lore = clicked.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;

        String locLine = lore.get(0).replace("Location: ", "");
        String[] parts = locLine.split(",");
        Location target = new Location(player.getWorld(),
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]));

        IFCooldownLogic.setCooldown(player);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 400) { // 20 seconds total
                    cancel();
                    return;
                }

                Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0F);

                // Start and end points
                Location start = player.getLocation().add(0, 1, 0);
                Location end = target.clone().add(0.5, 1, 0.5);

                // Vector between them
                double distance = start.distance(end);
                Vector step = end.toVector().subtract(start.toVector()).normalize().multiply(0.25); // spacing

                Location point = start.clone();
                for (double d = 0; d < distance; d += 0.25) {
                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            point,
                            1, 0, 0, 0,
                            dust
                    );
                    point.add(step);
                }

                ticks += 60; // advance 3 seconds (60 ticks)
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("WonderPlugin"), 0, 60); // run every 3 seconds
    }
}
