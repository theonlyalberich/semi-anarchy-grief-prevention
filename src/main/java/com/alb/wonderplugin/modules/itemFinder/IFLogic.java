package com.alb.wonderplugin.modules.itemFinder;

import com.alb.wonderplugin.WonderPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class IFLogic {

    private static WonderPlugin plugin;

    // Store results per player
    private static final Map<UUID, List<IFScanner.ResultEntry>> results = new HashMap<>();

    public static void init(WonderPlugin pl) {
        plugin = pl;
    }

    public static void startSearch(Player player, List<Material> materials, Claim claim, String query) {
        List<IFScanner.ResultEntry> allFound = new ArrayList<>();

        for (Material material : materials) {
            List<IFScanner.ResultEntry> found = IFScanner.scanClaim(claim, material);
            allFound.addAll(found);
        }

        results.put(player.getUniqueId(), allFound);
    }

    public static void startDisplayNameSearch(Player player, Claim claim, String displayNameQuery) {
        List<IFScanner.ResultEntry> found = IFScanner.scanClaimByDisplayName(claim, displayNameQuery);
        results.put(player.getUniqueId(), found);
    }

    public static List<IFScanner.ResultEntry> getResults(Player player) {
        return results.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }

    public static void clearResults(Player player) {
        results.remove(player.getUniqueId());
    }

    public static void sendPage(Player player, int page) {
        List<IFScanner.ResultEntry> playerResults = getResults(player);
        if (playerResults.isEmpty()) {
            player.sendMessage("No results found.");
            return;
        }

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) playerResults.size() / itemsPerPage);

        if (page < 1 || page > totalPages) {
            player.sendMessage("Invalid page. Available pages: 1-" + totalPages);
            return;
        }

        player.sendMessage("Results page " + page + "/" + totalPages);
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, playerResults.size());

        for (int i = startIndex; i < endIndex; i++) {
            IFScanner.ResultEntry entry = playerResults.get(i);
            player.sendMessage("  " + entry.toString());
        }
    }
}
