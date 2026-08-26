package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Enable grief prevention on all claims owned by a player.
 * Removes public trust and prevents explosions, but leaves PvP and drop protection disabled.
 * Opposite of DisableProtection, but allows PvP and item pickup.
 */
public class EnableProtection {
    private final DataStore dataStore;

    public EnableProtection(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Enable grief prevention on all claims owned by the specified player.
     * Removes public access and disallows explosions, but leaves PvP enabled and drop protection off.
     *
     * @param targetPlayerUUID UUID of player whose claims to modify
     */
    public void enableProtectionAndDisableExplosions(UUID targetPlayerUUID) {
        // Get all claims and filter by owner
        Collection<Claim> allClaims = dataStore.getClaims();
        ArrayList<Claim> playerClaims = new ArrayList<>();

        for (Claim claim : allClaims) {
            if (targetPlayerUUID.equals(claim.ownerID)) {
                playerClaims.add(claim);
            }
        }

        // Modify each claim
        for (Claim claim : playerClaims) {
            // Remove public access - nobody can build/access without trust
            claim.dropPermission("public");

            // Disable explosions
            claim.areExplosivesAllowed = false;

            // Disable wither explosions
            claim.areWitherExplosionsAllowed = false;

            // Keep PvP ENABLED - players can still fight
            claim.pvpEnabled = true;

            // Note: Drop protection is handled by global config (ProtectItemsDroppedOnDeath)
            // We don't touch that here - it's server-wide setting, not per-claim

            // Save changes
            dataStore.saveClaim(claim);

            // Also apply to all subdivisions
            applyProtectionToSubclaims(claim);
        }
    }
