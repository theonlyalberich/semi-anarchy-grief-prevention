package com.alb.wonderplugin.api;

import com.alb.wonderplugin.WonderPlugin;

public interface Module {
    void onEnable(WonderPlugin plugin);
    void onDisable();
    String getName();
}
