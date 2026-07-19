package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class OwnerModule implements Module, CommandExecutor, Listener {
    private WonderPlugin plugin;
    private NamespacedKey ownerKey;
    private NamespacedKey historyKey;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "item_owner");
        this.historyKey = new NamespacedKey(plugin, "item_owner_history");
        plugin.getCommand("wp").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {}

    @Override
    public String getName() {
        return "Owner";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wp.owner")) {
            sender.sendMessage(ChatColor.RED + "You lack permission: wp.owner");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /wp owner | /wp history");
            return true;
        }

        if (args[0].equalsIgnoreCase("owner")) {
            if (sender instanceof Player player) {
                openOwnerGUI(player, 0);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can open the GUI.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("history")) {
            if (sender instanceof Player player) {
                showHistory(player);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can view item history directly.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /wp owner | /wp history");
        return true;
    }

    private void showHistory(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item to view ownership history.");
            return;
        }

        ItemMeta meta = held.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String history = data.get(historyKey, PersistentDataType.STRING);

        if (history == null || history.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "This item has no recorded owners.");
            return;
        }

        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : held.getType().name();
        player.sendMessage(ChatColor.GOLD + "Ownership history for [" + displayName + "]:");

        String[] uuids = history.split(",");
        for (String uuid : uuids) {
            Player online = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (online != null) {
                player.sendMessage(ChatColor.GREEN + "- " + online.getName() + " (" + uuid + ")");
            } else {
                player.sendMessage(ChatColor.GRAY + "- " + uuid + " (offline)");
            }
        }
    }

    private void openOwnerGUI(Player player, int page) {
        int size = 54; // double chest
        Inventory gui = Bukkit.createInventory(null, size,
                ChatColor.DARK_GREEN + "Choose Owner (Page " + (page + 1) + ")");

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        int start = page * 45; // 45 slots per page
        int end = Math.min(start + 45, online.size());
        int slot = 0;

        // Fill player heads
        for (int i = start; i < end; i++) {
            Player onlinePlayer = online.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName(ChatColor.YELLOW + onlinePlayer.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "UUID: " + onlinePlayer.getUniqueId().toString());
            meta.setLore(lore);
            head.setItemMeta(meta);
            gui.setItem(slot++, head);
        }

        // Navigation controls
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = size - 9; i < size; i++) gui.setItem(i, filler);

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Previous Page");
            prev.setItemMeta(meta);
            gui.setItem(size - 9, prev);
        }

        if (end < online.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Next Page");
            next.setItemMeta(meta);
            gui.setItem(size - 1, next);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(closeMeta);
        gui.setItem(size - 5, close);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Choose Owner")) return;

        event.setCancelled(true); // prevent item pickup
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) clicked.getItemMeta();
            if (skullMeta != null && skullMeta.getOwningPlayer() != null) {
                Player chosen = (Player) skullMeta.getOwningPlayer();

                // Write UUID to item in hand
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held != null && !held.getType().isAir()) {
                    ItemMeta meta = held.getItemMeta();
                    PersistentDataContainer data = meta.getPersistentDataContainer();

                    String newUUID = chosen.getUniqueId().toString();

                    // Check history first
                    String history = data.get(historyKey, PersistentDataType.STRING);
                    if (history != null && history.contains(newUUID)) {
                        player.sendMessage(ChatColor.RED + "That player is already listed as an owner.");
                        player.closeInventory();
                        return;
                    }

                    // Add current owner
                    data.set(ownerKey, PersistentDataType.STRING, newUUID);

                    // Append to history
                    if (history == null || history.isEmpty()) {
                        history = newUUID;
                    } else {
                        history = history + "," + newUUID;
                    }
                    data.set(historyKey, PersistentDataType.STRING, history);

                    held.setItemMeta(meta);

                    // Use display name if present
                    String displayName = meta.hasDisplayName() ? meta.getDisplayName() : held.getType().name();
                    player.sendMessage(ChatColor.GREEN + "Set owner of [" + displayName + "] to " +
                            chosen.getName() + " (" + newUUID + ")");
                } else {
                    player.sendMessage(ChatColor.RED + "You must hold an item to assign ownership.");
                }

                player.closeInventory();
            }
        } else if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            int currentPage = Integer.parseInt(title.replaceAll("[^0-9]", "")) - 1;
            if (name.equalsIgnoreCase("Next Page")) {
                openOwnerGUI(player, currentPage + 1);
            } else if (name.equalsIgnoreCase("Previous Page")) {
                openOwnerGUI(player, currentPage - 1);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held == null || held.getType().isAir()) return;

        ItemMeta meta = held.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String currentOwner = data.get(ownerKey, PersistentDataType.STRING);

        // Fortune 7+ special case
        if (meta.hasEnchant(Enchantment.FORTUNE) &&
                meta.getEnchantLevel(Enchantment.FORTUNE) >= 7) {

            if (currentOwner == null) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Please register to use.");
                return;
            }
        }

        // Normal ownership check
        if (currentOwner != null && !player.getUniqueId().toString().equals(currentOwner)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are not the owner.");
        }
    }


    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held == null || held.getType().isAir()) return;

        ItemMeta meta = held.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String currentOwner = data.get(ownerKey, PersistentDataType.STRING);

        if (currentOwner == null) return; // no owner set, allow normal use

        // Compare UUIDs
        if (!player.getUniqueId().toString().equals(currentOwner)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are not the owner.");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir()) return;

        ItemMeta meta = held.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        String currentOwner = data.get(ownerKey, PersistentDataType.STRING);

        if (currentOwner == null) return; // no owner set, allow normal use

        if (!player.getUniqueId().toString().equals(currentOwner)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are not the owner.");
        }
    }


}
