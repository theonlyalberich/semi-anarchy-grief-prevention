package com.alb.wonderplugin.util;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoreMenus {

    private CoreMenus() {
        // Utility class, no instantiation
    }

    /**
     * Safely get the CoreProtect API instance.
     *
     * @param plugin your main plugin (JavaPlugin)
     * @return CoreProtectAPI if available and compatible, otherwise null
     */
    public static CoreProtectAPI getCoreProtect(JavaPlugin plugin) {
        Plugin core = plugin.getServer().getPluginManager().getPlugin("CoreProtect");

        if (!(core instanceof CoreProtect)) {
            return null;
        }

        CoreProtectAPI api = ((CoreProtect) core).getAPI();

        if (!api.isEnabled()) {
            return null;
        }

        if (api.APIVersion() < 12) {
            return null;
        }

        return api;
    }

    /**
     * Quick test to confirm CoreProtect API is working.
     */
    public static void test(JavaPlugin plugin) {
        CoreProtectAPI api = getCoreProtect(plugin);
        if (api != null) {
            api.testAPI(); // prints "[CoreProtect] API test successful."
            plugin.getLogger().info("CoreProtect API v" + api.APIVersion() + " detected and ready.");
        } else {
            plugin.getLogger().warning("CoreProtect API not available or incompatible.");
        }
    }
}
