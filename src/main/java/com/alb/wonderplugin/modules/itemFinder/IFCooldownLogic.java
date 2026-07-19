package com.alb.wonderplugin.modules.itemFinder;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IFCooldownLogic {

    // Store cooldown expiry timestamps per player
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    // Duration: 25 seconds (20s particles + 5s buffer)
    private static final long COOLDOWN_MS = 25_000;

    /**
     * Check if a player is currently on cooldown.
     */
    public static boolean isOnCooldown(Player player) {
        Long expiry = cooldowns.get(player.getUniqueId());
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    /**
     * Set a cooldown for the player.
     */
    public static void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_MS);
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public static int getRemaining(Player player) {
        Long expiry = cooldowns.get(player.getUniqueId());
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
}
