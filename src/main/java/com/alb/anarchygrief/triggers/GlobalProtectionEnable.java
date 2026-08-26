package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.Collection;

/**
 * Ensures all claims are protected on server startup.
 * Removes public trust and disables explosions globally on player claims only.
 * Leaves PvP enabled and drop protection to global config.
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

    /**
     * Recursively apply protection settings to all subdivisions of a claim.
     * Removes public access, disables explosions, but keeps PvP enabled.
     *
     * @param parentClaim The parent claim whose subdivisions should be protected
     */
    private void applyProtectionToSubclaims(Claim parentClaim) {
        for (Claim subclaim : parentClaim.getChildren()) {
            // Remove public access
            subclaim.dropPermission("public");

            // Disable explosions
            subclaim.areExplosivesAllowed = false;
            subclaim.areWitherExplosionsAllowed = false;

            // Keep PvP ENABLED
            subclaim.pvpEnabled = true;

            // Save subclaim
            dataStore.saveClaim(subclaim);

            // Recursively apply to nested subdivisions
            if (!subclaim.getChildren().isEmpty()) {
                applyProtectionToSubclaims(subclaim);
            }
        }
    }
}
