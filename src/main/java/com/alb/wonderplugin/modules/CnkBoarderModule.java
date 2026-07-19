package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.WonderPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CnkBoarderModule implements Module, Listener, CommandExecutor {
    private WonderPlugin plugin;

    // Cooldowns per player (expiry timestamps)
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 4 * 60 * 1000; // 4 minutes

    // Precomputed chunk corner offsets
    private static final int[][] CORNERS = {
            {0,0},    // NW
            {15,0},   // NE
            {0,15},   // SW
            {15,15}   // SE
    };

    private static final BlockFace[] FACES = {
            BlockFace.WEST,  // NW faces west
            BlockFace.NORTH, // NE faces north
            BlockFace.SOUTH, // SW faces south
            BlockFace.EAST   // SE faces east
    };

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("cb").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        cooldowns.clear();
    }

    @Override
    public String getName() {
        return "CnkBoarder";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (command.getName().equalsIgnoreCase("cb")) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (cooldowns.containsKey(uuid)) {
                long expiry = cooldowns.get(uuid);
                if (now < expiry) {
                    long remaining = (expiry - now) / 1000;
                    player.sendMessage(Component.text("You must wait " + remaining + " seconds before using /cb again.", NamedTextColor.RED));
                    return true;
                }
            }

            cooldowns.put(uuid, now + COOLDOWN_MILLIS);
            startBorderTask(player);
        }
        return true;
    }

    private void startBorderTask(Player player) {
        player.sendMessage(Component.text("Chunk boarders are now visible for 3 minutes.", NamedTextColor.GREEN));

        List<ArmorStand> currentStands = new ArrayList<>();
        Chunk originChunk = player.getLocation().getChunk();

        // Repeating refresh task
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            boolean sameChunk = player.getLocation().getChunk().equals(originChunk);
            boolean withinRange = false;

            if (sameChunk && !currentStands.isEmpty()) {
                int playerY = player.getLocation().getBlockY();
                for (ArmorStand s : currentStands) {
                    if (Math.abs(playerY - s.getLocation().getBlockY()) <= 16) {
                        withinRange = true;
                        break;
                    }
                }
            }

            if (!(sameChunk && withinRange)) {
                // Remove old stands
                for (ArmorStand s : currentStands) {
                    s.remove();
                }
                currentStands.clear();

                // Summon new stands
                currentStands.addAll(spawnChunkBorder(player));
            }
        }, 0L, 40L); // every 2 seconds

        // Cleanup after 3 minutes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (ArmorStand s : currentStands) {
                s.remove();
            }
            Bukkit.getScheduler().cancelTask(taskId);
        }, 20L * 60 * 3);
    }

    private List<ArmorStand> spawnChunkBorder(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        World world = player.getWorld();
        List<ArmorStand> stands = new ArrayList<>();

        int baseY = player.getLocation().getBlockY();
        int[] levels = {baseY - 20, baseY, baseY + 20};

        for (int i = 0; i < CORNERS.length; i++) {
            int[] corner = CORNERS[i];
            BlockFace facing = FACES[i];

            for (int y : levels) {
                int bx = chunk.getX() * 16 + corner[0];
                int bz = chunk.getZ() * 16 + corner[1];
                Location loc = new Location(world, bx + 0.5, y, bz + 0.5);

                ArmorStand stand = world.spawn(loc, ArmorStand.class, s -> {
                    s.setGravity(false);
                    s.setMarker(true);          // no hitbox, no interaction
                    s.setInvisible(false);      // armor stand visible
                    s.customName(Component.text("cb"));
                    s.setCustomNameVisible(false); // name hidden
                    s.setInvulnerable(true);
                    s.setPersistent(false);     // don’t save to disk
                    s.setRotation(getYawForFacing(facing), 0f);
                });

                stands.add(stand);
            }
        }

        return stands;
    }

    private float getYawForFacing(BlockFace face) {
        switch (face) {
            case NORTH: return 180f;
            case EAST:  return -90f;
            case SOUTH: return 0f;
            case WEST:  return 90f;
            default:    return 0f;
        }
    }
}
