package com.alb.anarchygrief.mechanics;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.entity.Player;

/**
 * Blocker mechanic for AnarchyGrief plugin.
 * Disables golden shovel claim resizing for non-op players.
 * Disables claim boundary visualization for players who don't own the claim.
 */
public class Blocker {

    /**
     * Checks if a player can resize a claim using the golden shovel.
     * Only op players (or those with griefprevention admin perms) can resize claims.
     *
     * @param player the player attempting to resize
     * @param claim the claim being resized
     * @param playerData the player's data
     * @return true if the resize is allowed, false otherwise
     */
    public static boolean canResizeClaim(Player player, Claim claim, PlayerData playerData) {
        // Only allow ops to resize claims
        if (!player.isOp()) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a player should see the visual boundary of a claim.
     * Only the claim owner (or ops) can see claim boundaries.
     *
     * @param player the player viewing the claim
     * @param claim the claim being viewed
     * @return true if the visualization should be shown, false otherwise
     */
    public static boolean canSeeClaim(Player player, Claim claim) {
        // Only allow ops and claim owners to see the boundary visualization
        if (player.isOp()) {
            return true;
        }
        
        // Check if player is the claim owner
        if (claim.isAdminClaim()) {
            return false; // Non-ops can't see admin claims
        }
        
        if (claim.getOwnerID() != null && claim.getOwnerID().equals(player.getUniqueId())) {
            return true; // Owner can see their own claim
        }
        
        return false; // Non-owners can't see the claim
    }
}
