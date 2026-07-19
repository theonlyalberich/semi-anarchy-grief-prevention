package com.alb.wonderplugin;

import com.alb.wonderplugin.api.Module;
import com.alb.wonderplugin.modules.*;
import com.alb.wonderplugin.util.UpdateHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WonderPlugin extends JavaPlugin {
    private final List<Module> modules = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Always add UpdateHelper first (cannot be disabled)
        modules.add(new UpdateHelper());

        // Test CoreMenus/CoreProtect integration only if enabled in config
        if (getConfig().getBoolean("coremenus.enabled", false)) {
            com.alb.wonderplugin.util.CoreMenus.test(this);
        }

        // Load modules based on config
        if (getConfig().getBoolean("modules.owner.enabled")) {
            modules.add(new OwnerModule());
        }
        if (getConfig().getBoolean("modules.spawner.enabled")) {
            modules.add(new SpawnerModule());
        }
        if (getConfig().getBoolean("modules.creepercontrol.enabled")) {
            modules.add(new CreeperControlModule());
        }
        if (getConfig().getBoolean("modules.headfarm.enabled")) {
            modules.add(new HeadFarmModule());
        }
        if (getConfig().getBoolean("modules.cnkboarder.enabled")) {
            modules.add(new CnkBoarderModule());
        }
        if (getConfig().getBoolean("modules.statues.enabled")) {
            modules.add(new AlbsStatuesModule());
        }
        if (getConfig().getBoolean("modules.banhammer.enabled")) {
            modules.add(new BanHammerModule());
        }

        /*if (getConfig().getBoolean("modules.filebridge.enabled")) {
            modules.add(new FileBridgeModule());
        }*/
        if (getConfig().getBoolean("modules.anvilwatch.enabled")) {
            modules.add(new AnvilWatchModule());
        }
        /*if (getConfig().getBoolean("modules.chunki.enabled")) {
            modules.add(new ChunkiModule());
        }*/

        // NEW: ItemFinderModule (Option 2 style)
        if (getConfig().getBoolean("modules.itemfinder.enabled")) {
            modules.add(new ItemFinderModule());
        }

        if (getConfig().getBoolean("modules.follower.enabled")) {
            modules.add(new FollowerModule());
        }

        if (getConfig().getBoolean("modules.pumpkening.enabled")) {
            modules.add(new PumpkeningModule());
        }


        // Enable all modules
        for (Module m : modules) {
            m.onEnable(this);
            getLogger().info("Enabled module: " + m.getName());
        }
    }

    @Override
    public void onDisable() {
        for (Module m : modules) {
            m.onDisable();
        }
    }
}
