package com.alb.wonderplugin.modules.follower;

import com.alb.wonderplugin.WonderPlugin;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.tracker.EntityHideOption;
import kr.toxicity.model.api.tracker.TrackerModifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FollowerCommands implements CommandExecutor, TabCompleter, Listener {

    private final WonderPlugin plugin;
    private final Set<String> allowedModels = new HashSet<>();
    private static final Map<UUID, String> activeFollowers = new HashMap<>();
    private File configFile;

    public FollowerCommands(WonderPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("follower.yml", false);
        configFile = new File(plugin.getDataFolder(), "follower.yml");
        loadAllowedModels();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadAllowedModels() {
        allowedModels.clear();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
        List<String> list = cfg.getStringList("allowed-models");
        for (String s : list) {
            allowedModels.add(s.toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeFollowers.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Handle /addfollower
        if (label.equalsIgnoreCase("addfollower")) {
            return handleAddFollower(player, args);
        }

        // Handle /clearfollow
        if (label.equalsIgnoreCase("clearfollow") || label.equalsIgnoreCase("rmfollow") || label.equalsIgnoreCase("stopfollow")) {
            removeFollower(player, uuid);
            return true;
        }

        // Handle /follow <model|stop>
        if (args.length == 0) {
            player.sendMessage("Usage: /follow <model|stop>");
            return true;
        }

        String model = args[0].toLowerCase(Locale.ROOT);

        if (model.equals("stop") || model.equals("remove") || model.equals("off")) {
            removeFollower(player, uuid);
            return true;
        }

        // Check dynamic permission wp.follow.<model_name>
        String perm = "wp.follow." + model;
        if (!player.hasPermission(perm)) {
            player.sendMessage("You do not have permission to use this model.");
            return true;
        }

        if (!allowedModels.contains("all") && !allowedModels.contains(model)) {
            player.sendMessage("This model is not allowed to be used as a non-hide follower.");
            return true;
        }

        // Close existing follower if any
        try {
            var existingReg = BetterModel.registryOrNull(uuid);
            if (existingReg != null) {
                existingReg.close();
                player.sendMessage("Previous follower removed.");
            }
        } catch (Throwable ignored) {
        }

        BetterModel.model(model).ifPresentOrElse(renderer -> {
            try {
                renderer.getOrCreate(BukkitAdapter.adapt(player), TrackerModifier.DEFAULT, t -> {
                    t.hideOption(EntityHideOption.FALSE); // keep player visible
                });
                activeFollowers.put(uuid, model);
                player.sendMessage("Summoned follower: " + model);
            } catch (Throwable ex) {
                player.sendMessage("Failed to create follower: " + ex.getMessage());
            }
        }, () -> player.sendMessage("Model not found: " + model));

        return true;
    }

    private boolean handleAddFollower(Player player, String[] args) {
        if (!player.hasPermission("wp.follower.admin")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /addfollower <model_name>");
            return true;
        }

        String model = args[0].toLowerCase(Locale.ROOT);

        if (allowedModels.contains(model)) {
            player.sendMessage("Model '" + model + "' is already in the allowed list.");
            return true;
        }

        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
            List<String> list = cfg.getStringList("allowed-models");
            if (!list.contains(model)) {
                list.add(model);
                cfg.set("allowed-models", list);
                cfg.save(configFile);
                loadAllowedModels();
                player.sendMessage("Model '" + model + "' added to allowed followers.");
            }
        } catch (Exception ex) {
            player.sendMessage("Failed to save model: " + ex.getMessage());
        }

        return true;
    }

    private void removeFollower(Player player, UUID uuid) {
        try {
            var reg = BetterModel.registryOrNull(uuid);
            if (reg != null) {
                reg.close();
                player.sendMessage("Follower removed.");
            } else {
                player.sendMessage("You don't have an active follower.");
            }
        } catch (Throwable ignored) {
            player.sendMessage("Failed to remove follower.");
        }
        activeFollowers.remove(uuid);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String m : allowedModels) {
                if (m.startsWith(prefix)) out.add(m);
            }
        }
        return out;
    }
}
