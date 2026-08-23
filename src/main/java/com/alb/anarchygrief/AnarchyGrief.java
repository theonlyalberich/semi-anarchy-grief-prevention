package com.alb.anarchygrief;

import com.alb.anarchygrief.listeners.ConnectionHandler;
import com.alb.anarchygrief.listeners.PlayerConnectionListener;
import com.alb.anarchygrief.triggers.DisableProtection;
import com.alb.anarchygrief.triggers.EnableProtection;
import com.alb.anarchygrief.triggers.GlobalProtectionEnable;
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
        DisableProtection disabler = new DisableProtection(gp.dataStore);
        EnableProtection enabler = new EnableProtection(gp.dataStore);
        GlobalProtectionEnable globalEnabler = new GlobalProtectionEnable(gp.dataStore);

        // Configurable delays
        int loginDelayMinutes = getConfig().getInt("login-delay-minutes", 5);
        int restoreDelayMinutes = getConfig().getInt("restoreDelayMinutes", 5);

        // Startup safeguard: enforce protection globally
        globalEnabler.enableProtectionOnAllClaims();
        getLogger().info("Global protection enforced on startup.");

        // Register listener with both managers
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(this, this, disabler, enabler, loginDelayMinutes), this
        );

        getLogger().info("AnarchyGrief listener enabled. Login delay=" + loginDelayMinutes +
                "m, Restore delay=" + restoreDelayMinutes + "m");
    }

    @Override
    public void onDisable() {
        getLogger().info("AnarchyGrief disabled.");
    }
}
