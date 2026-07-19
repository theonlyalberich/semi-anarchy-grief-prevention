package com.alb.wonderplugin.modules.itemFinder;

import com.alb.wonderplugin.WonderPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IFListener implements Listener {

    private final WonderPlugin plugin;
    private static final Pattern PAGE_PATTERN = Pattern.compile("page (\\d+)", Pattern.CASE_INSENSITIVE);

    public IFListener(WonderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Matcher matcher = PAGE_PATTERN.matcher(event.getMessage());
        if (matcher.matches()) {
            int page = Integer.parseInt(matcher.group(1));
            IFLogic.sendPage(event.getPlayer(), page);
            event.setCancelled(true);
        }
    }
}
