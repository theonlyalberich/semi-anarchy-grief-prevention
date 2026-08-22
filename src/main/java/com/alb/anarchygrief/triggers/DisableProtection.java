package com.alb.anarchygrief.triggers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Disable grief prevention and enable explosions on all claims owned by a player.
 * Combines /trust all public and /claimexplosions into a single operation.
 */
public class DisableProtection
{
    private final DataStore dataStore;

    public DisableProtection(DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    /**
     * Disable grief prevention and enable explosions on all claims owned by the specified player.
     * Opens all claims to public access (everyone can build) and allows explosions.
     *
     * @param targetPlayerUUID UUID of player whose claims to modify
     */
    public void disableProtectionAndEnableExplosions(UUID targetPlayerUUID)
    {
        // Get all claims and filter by owner
        Collection<Claim> allClaims = dataStore.getClaims();
        ArrayList<Claim> playerClaims = new ArrayList<>();

        for (Claim claim : allClaims)
        {
            if (targetPlayerUUID.equals(claim.ownerID))
            {
                playerClaims.add(claim);
            }
        }

        // Modify each claim
        for (Claim claim : playerClaims)
        {
            // Disable grief prevention - allow public to build
            claim.setPermission("public", ClaimPermission.Build);

            // Enable explosions
            claim.areExplosivesAllowed = true;

            // Save changes
            dataStore.saveClaim(claim);
        }
    }
}