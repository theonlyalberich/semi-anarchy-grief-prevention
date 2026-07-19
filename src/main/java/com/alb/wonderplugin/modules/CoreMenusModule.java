package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.util.CoreMenus;
import net.coreprotect.CoreProtectAPI;

public class CoreMenusModule implements Module {

    private CoreProtectAPI api;

    @Override
    public void onEnable(WonderPlugin plugin) {
        if (plugin.getConfig().getBoolean("coremenus.enabled", false)) {
            api = CoreMenus.getCoreProtect(plugin);
            if (api != null) {
                plugin.getLogger().info("CoreMenusModule enabled. CoreProtect API v" + api.APIVersion() + " ready.");
            } else {
                plugin.getLogger().warning("CoreMenusModule enabled but CoreProtect API not available.");
            }
        } else {
            plugin.getLogger().info("CoreMenusModule disabled via config.");
        }
    }

    @Override
    public void onDisable() {
        api = null;
    }

    @Override
    public String getName() {
        return "CoreMenus";
    }
}
