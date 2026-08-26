package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Enable grief prevention and disable explosions on all claims owned by a player.
 * Opposite of DisableProtection: removes public trust and prevents explosions.
 */
public class EnableProtection {
    private final DataStore dataStore;

    public EnableProtection(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Enable grief prevention and disable explosions on all claims owned by the specified player.
     * Removes public build permission and disallows explosions and wither explosions.
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
            // Remove public build permission
            claim.dropPermission("public");

            // Disable explosions
            claim.areExplosivesAllowed = false;

            // Disable wither explosions
            claim.areWitherExplosionsAllowed = false;

            // Save changes
            dataStore.saveClaim(claim);
        }
    }
}
