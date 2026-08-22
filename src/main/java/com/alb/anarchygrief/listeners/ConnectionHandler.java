package com.alb.anarchygrief.listeners;

import org.bukkit.entity.Player;

public interface ConnectionHandler {
    void onLogin(Player player);
    void onLogout(Player player);
}
