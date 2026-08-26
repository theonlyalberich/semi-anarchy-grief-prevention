package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Convert a player's claims into complete anarchy when they are online.
 * Everyone can build, destroy, interact with anything - complete free-for-all.
 */
public class DisableProtection {
    private final DataStore dataStore;

    public DisableProtection(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Turn all claims owned by a player into complete anarchy.
     * Everyone gets full permissions (build, container, access) - it's a free-for-all.
     *
     * @param targetPlayerUUID UUID of player whose claims to convert to anarchy
     */
    public void enableCompleteAnarchy(UUID targetPlayerUUID) {
        // Get all claims and filter by owner
        Collection<Claim> allClaims = dataStore.getClaims();
        ArrayList<Claim> playerClaims = new ArrayList<>();

        for (Claim claim : allClaims) {
            if (targetPlayerUUID.equals(claim.ownerID)) {
                playerClaims.add(claim);
            }
        }

        // Convert each claim to complete anarchy
        for (Claim claim : playerClaims) {
            // Give PUBLIC full access to everything
            claim.setPermission("public", ClaimPermission.Build);      // Can place/break blocks
            claim.setPermission("public", ClaimPermission.Container);  // Can access chests/containers
            claim.setPermission("public", ClaimPermission.Access);     // Can use doors/interact

            // ===== ALLOW ALL EXPLOSIONS =====
            claim.areExplosivesAllowed = true;          // Allow creeper, TNT, other explosions
            claim.areWitherExplosionsAllowed = true;    // Allow wither explosions

            // ===== ALLOW PVP =====
            claim.pvpEnabled = true;                    // Allow PvP combat

            // Save the anarchic claim
            dataStore.saveClaim(claim);

            // Also apply to all subdivisions
            applyAnarchyToSubclaims(claim);
        }
    }

    /**
     * Recursively apply anarchy settings to all subdivisions of a claim.
     *
     * @param parentClaim The parent claim whose subdivisions should be anarchified
     */
    private void applyAnarchyToSubclaims(Claim parentClaim) {
        for (Claim subclaim : parentClaim.getChildren()) {
            // Give PUBLIC full access
            subclaim.setPermission("public", ClaimPermission.Build);
            subclaim.setPermission("public", ClaimPermission.Container);
            subclaim.setPermission("public", ClaimPermission.Access);

            // Allow explosions and PvP
            subclaim.areExplosivesAllowed = true;
            subclaim.areWitherExplosionsAllowed = true;
            subclaim.pvpEnabled = true;

            // Save subclaim
            dataStore.saveClaim(subclaim);

            // Recursively apply to nested subdivisions
            if (!subclaim.getChildren().isEmpty()) {
                applyAnarchyToSubclaims(subclaim);
            }
        }
    }

    /**
     * Apply complete anarchy to a single claim.
     * Useful if you need to apply it to specific claims on-demand.
     *
     * @param claim The claim to anarchify
     */
    public void applyAnarchyToClaim(Claim claim) {
        // PUBLIC gets full permissions
        claim.setPermission("public", ClaimPermission.Build);
        claim.setPermission("public", ClaimPermission.Container);
        claim.setPermission("public", ClaimPermission.Access);

        // All explosions allowed
        claim.areExplosivesAllowed = true;
        claim.areWitherExplosionsAllowed = true;

        // PvP enabled
        claim.pvpEnabled = true;

        // Save
        dataStore.saveClaim(claim);
    }
}
