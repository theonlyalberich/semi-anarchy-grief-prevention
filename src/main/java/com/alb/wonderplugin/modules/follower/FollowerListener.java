package com.alb.wonderplugin.modules.follower;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class FollowerListener implements Listener {
    private static BukkitTask task;

    public static void startTask(WonderPlugin plugin) {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Placeholder: future follower tick logic can go here.
            // Keeping a lightweight repeating task so module can be extended later.
        }, 0L, 10L);
    }

    public static void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
