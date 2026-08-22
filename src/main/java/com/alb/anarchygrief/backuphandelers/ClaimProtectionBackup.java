package com.alb.anarchygrief.backuphandelers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Backs up all claim permissions for a player.
 */
public class ClaimProtectionBackup
{
    private final DataStore dataStore;
    private final Map<UUID, Map<Long, Map<String, ClaimPermission>>> backups = new HashMap<>();

    public ClaimProtectionBackup()
    {
        Plugin gpPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if (gpPlugin instanceof GriefPrevention)
        {
            this.dataStore = ((GriefPrevention) gpPlugin).dataStore;
        }
        else
        {
            this.dataStore = null;
        }
    }

    /**
     * Backup all permissions for a player's claims.
     *
     * @param targetPlayerUUID UUID of player whose claims to backup
     */
    public void backupAllClaimPermissions(UUID targetPlayerUUID)
    {
        if (dataStore == null)
        {
            System.err.println("GriefPrevention plugin not found!");
            return;
        }

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

        Map<Long, Map<String, ClaimPermission>> playerBackup = new HashMap<>();

        for (Claim claim : playerClaims)
        {
            Map<String, ClaimPermission> claimPermissions = new HashMap<>();

            ArrayList<String> builders = new ArrayList<>();
            ArrayList<String> containers = new ArrayList<>();
            ArrayList<String> accessors = new ArrayList<>();
            ArrayList<String> managers = new ArrayList<>();

            claim.getPermissions(builders, containers, accessors, managers);

            for (String builder : builders)
                claimPermissions.put(builder, ClaimPermission.Build);
            for (String container : containers)
                claimPermissions.put(container, ClaimPermission.Container);  // Fixed: use enum constant
            for (String accessor : accessors)
                claimPermissions.put(accessor, ClaimPermission.Access);
            for (String manager : managers)
                claimPermissions.put(manager, ClaimPermission.Manage);

            playerBackup.put(claim.getID(), claimPermissions);
        }

        backups.put(targetPlayerUUID, playerBackup);
    }

    /**
     * Check if a backup exists for a player.
     *
     * @param targetPlayerUUID UUID of player
     * @return true if backup exists
     */
    public boolean hasBackup(UUID targetPlayerUUID)
    {
        return backups.containsKey(targetPlayerUUID);
    }

    /**
     * Get the backup for a player.
     *
     * @param targetPlayerUUID UUID of player
     * @return the backup map or null if not found
     */
    public Map<Long, Map<String, ClaimPermission>> getBackup(UUID targetPlayerUUID)
    {
        return backups.get(targetPlayerUUID);
    }
}