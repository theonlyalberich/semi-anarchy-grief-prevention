package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.modules.follower.FollowerCommands;
import com.alb.wonderplugin.modules.follower.FollowerListener;

public class FollowerModule implements Module {

    private WonderPlugin plugin;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;

        FollowerCommands commands = new FollowerCommands(plugin);
        plugin.getCommand("follow").setExecutor(commands);
        plugin.getCommand("clearfollow").setExecutor(commands);
        plugin.getCommand("addfollower").setExecutor(commands);
        FollowerListener.startTask(plugin);

        plugin.getLogger().info("[WonderPlugin] FollowerModule enabled.");
    }

    @Override
    public void onDisable() {
        plugin.getLogger().info("[WonderPlugin] FollowerModule disabled.");
    }

    @Override
    public String getName() {
        return "FollowerModule";
    }
}
