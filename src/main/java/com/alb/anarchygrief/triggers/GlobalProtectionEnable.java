package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.Collection;

/**
 * Ensures all claims are protected on server startup.
 * Removes public trust and disables explosions globally on player claims only.
 */
public class GlobalProtectionEnable {

    private final DataStore dataStore;

    public GlobalProtectionEnable(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Iterate through all claims and enforce protection on player claims only.
     */
    public void enableProtectionOnAllClaims() {
        Collection<Claim> allClaims = dataStore.getClaims();

        for (Claim claim : allClaims) {
            // Skip admin claims
            if (claim.isAdminClaim()) {
                continue;
            }

            // Remove public build permission
            claim.dropPermission("public");

            // Disable explosions
            claim.areExplosivesAllowed = false;

            // Save changes
            dataStore.saveClaim(claim);
        }
    }
}
