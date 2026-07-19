package com.alb.wonderplugin.modules.itemFinder;

import com.alb.wonderplugin.WonderPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IFCommands implements CommandExecutor {

    private final WonderPlugin plugin;

    public IFCommands(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        if ("findname".equalsIgnoreCase(label)) {
            return handleFindNameCommand(player, args);
        } else {
            return handleFindCommand(player, args);
        }
    }

    private boolean handleFindCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /find <item>");
            return true;
        }

        String query = args[0].toLowerCase();
        List<Material> matches = new ArrayList<>();

        // Exact match first
        Material exact = Material.matchMaterial(args[0].toUpperCase());
        if (exact != null) {
            matches.add(exact);
        } else if (query.length() >= 3) {
            // Partial matches (case-insensitive, min 3 letters)
            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().contains(query)) {
                    matches.add(mat);
                }
            }
        }

        if (matches.isEmpty()) {
            player.sendMessage(ChatColor.RED + query + " not found.");
            return true;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a claim.");
            return true;
        }

        String denial = claim.allowContainers(player);
        if (denial != null) {
            player.sendMessage(ChatColor.RED + denial);
            return true;
        }

        // Clear old results and start fresh search for all matches
        IFLogic.clearResults(player);
        IFLogic.startSearch(player, matches, claim, query);

        // Open GUI instead of chat reply
        IFGUI.openResultsGUI(player, query);

        return true;
    }

    private boolean handleFindNameCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /findname <display name>");
            return true;
        }

        String displayNameQuery = String.join(" ", args).toLowerCase();

        if (displayNameQuery.length() < 2) {
            player.sendMessage(ChatColor.RED + "Display name search must be at least 2 characters.");
            return true;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a claim.");
            return true;
        }

        String denial = claim.allowContainers(player);
        if (denial != null) {
            player.sendMessage(ChatColor.RED + denial);
            return true;
        }

        // Clear old results and start fresh search by display name
        IFLogic.clearResults(player);
        IFLogic.startDisplayNameSearch(player, claim, displayNameQuery);

        // Open GUI instead of chat reply
        IFGUI.openResultsGUI(player, displayNameQuery);

        return true;
    }
}
