package com.alb.anarchygrief;

import com.alb.anarchygrief.backuphandelers.ClaimProtectionBackup;
import com.alb.anarchygrief.listeners.ConnectionHandler;
import com.alb.anarchygrief.listeners.PlayerConnectionListener;
import com.alb.anarchygrief.triggers.DisableProtection;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AnarchyGrief extends JavaPlugin implements ConnectionHandler {

    @Override
    public void onLogin(Player player) {
        // placeholder logic for login
        getLogger().info(player.getName() + " logged in (ConnectionHandler placeholder).");
    }

    @Override
    public void onLogout(Player player) {
        // placeholder logic for logout
        getLogger().info(player.getName() + " logged out (ConnectionHandler placeholder).");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Get GriefPrevention datastore
        GriefPrevention gp = (GriefPrevention) getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp == null) {
            getLogger().severe("GriefPrevention not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Managers
        ClaimProtectionBackup backup = new ClaimProtectionBackup();
        DisableProtection disabler = new DisableProtection(gp.dataStore);

        // Configurable delays
        int loginDelayMinutes = getConfig().getInt("login-delay-minutes", 5);
        int restoreDelayMinutes = getConfig().getInt("restoreDelayMinutes", 5);

        // Register listener with both delays
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(this, this, backup, disabler, loginDelayMinutes), this
        );

        getLogger().info("AnarchyGrief listener enabled. Login delay=" + loginDelayMinutes +
                "m, Restore delay=" + restoreDelayMinutes + "m");
    }

    @Override
    public void onDisable() {
        getLogger().info("AnarchyGrief disabled.");
    }
}
