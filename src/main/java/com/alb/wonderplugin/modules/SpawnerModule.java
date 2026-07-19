//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerModule implements Module, CommandExecutor, Listener {
    private WonderPlugin plugin;
    private NamespacedKey spawnerOwnerKey;
    private NamespacedKey spawnerToggleKey;
    private NamespacedKey spawnerMobKey;
    private Map<UUID, Block> spawnerContext = new HashMap();

    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        boolean enabled = plugin.getConfig().getBoolean("modules.spawner.enabled", true);
        if (enabled) {
            this.spawnerOwnerKey = new NamespacedKey(plugin, "spawner_owner");
            this.spawnerToggleKey = new NamespacedKey(plugin, "spawner_toggle");
            this.spawnerMobKey = new NamespacedKey(plugin, "spawner_mob");
            plugin.getCommand("spnr").setExecutor(this);
            plugin.getCommand("spwn").setExecutor(this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

    }

    public void onDisable() {
    }

    public String getName() {
        return "Spawner";
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() == Material.SPAWNER) {
            Player player = event.getPlayer();
            BlockState state = block.getState();
            if (state instanceof CreatureSpawner spawner) {
                PersistentDataContainer data = spawner.getPersistentDataContainer();
                ItemStack item = event.getItemInHand();

                // Only set owner, ignore mob type
                if (item.hasItemMeta()) {
                    PersistentDataContainer itemData = item.getItemMeta().getPersistentDataContainer();
                    String owner = itemData.get(this.spawnerOwnerKey, PersistentDataType.STRING);
                    if (owner != null) {
                        data.set(this.spawnerOwnerKey, PersistentDataType.STRING, owner);
                    } else {
                        data.set(this.spawnerOwnerKey, PersistentDataType.STRING, player.getName());
                    }
                } else {
                    data.set(this.spawnerOwnerKey, PersistentDataType.STRING, player.getName());
                }

                // Default toggle state
                data.set(this.spawnerToggleKey, PersistentDataType.STRING, "on");

                spawner.update(true);
            }
        }
    }


    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        BlockState state = event.getSpawner().getBlock().getState();
        if (state instanceof CreatureSpawner spawner) {
            PersistentDataContainer data = spawner.getPersistentDataContainer();
            String toggle = (String)data.get(this.spawnerToggleKey, PersistentDataType.STRING);
            if (toggle != null && toggle.equalsIgnoreCase("off")) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.SPAWNER) {
                Player player = event.getPlayer();
                if (!player.hasPermission("wp.spnr") && !player.hasPermission("wp.spnr.admin")) {
                    return;
                }

                ItemStack item = event.getItem();
                if (item == null || item.getType() == Material.AIR) {
                    this.spawnerContext.put(player.getUniqueId(), block);
                    this.openSpawnerGui(player);
                    event.setCancelled(true);
                }
            }
        }

    }

    private void openSpawnerGui(Player player) {
        Block target = (Block)this.spawnerContext.get(player.getUniqueId());
        if (target != null && target.getType() == Material.SPAWNER) {
            BlockState state = target.getState();
            if (state instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)state;
                PersistentDataContainer data = spawner.getPersistentDataContainer();
                String owner = (String)data.get(this.spawnerOwnerKey, PersistentDataType.STRING);
                boolean isOwner = owner != null && owner.equalsIgnoreCase(player.getName());
                boolean isAdmin = player.hasPermission("wp.spnr.admin");
                Inventory gui = Bukkit.createInventory((InventoryHolder)null, 9, String.valueOf(ChatColor.DARK_GREEN) + "Spawner Commands");
                if (player.hasPermission("wp.spnr") || isAdmin) {
                    gui.setItem(0, this.makeMenuItem(Material.PAPER, String.valueOf(ChatColor.YELLOW) + "/spnr check"));
                    gui.setItem(1, this.makeMenuItem(Material.NAME_TAG, String.valueOf(ChatColor.YELLOW) + "/spnr set"));
                    if (isAdmin) {
                        gui.setItem(2, this.makeMenuItem(Material.BOOK, String.valueOf(ChatColor.YELLOW) + "/spnr change"));
                    }

                    gui.setItem(3, this.makeMenuItem(Material.IRON_PICKAXE, String.valueOf(ChatColor.YELLOW) + "/spnr break"));
                    if (isOwner || isAdmin) {
                        gui.setItem(4, this.makeMenuItem(Material.REDSTONE_TORCH, String.valueOf(ChatColor.YELLOW) + "/spwn on/off"));
                    }
                }

                gui.setItem(8, this.makeMenuItem(Material.BARRIER, String.valueOf(ChatColor.RED) + "Close"));
                player.openInventory(gui);
            }
        }

    }

    private ItemStack makeMenuItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.hasItemMeta()) {
            if (event.getView().getTitle().equals(String.valueOf(ChatColor.DARK_GREEN) + "Spawner Commands")) {
                event.setCancelled(true);
                String name = clicked.getItemMeta().getDisplayName();
                if (name.contains("/spnr check")) {
                    player.performCommand("spnr check");
                    player.closeInventory();
                } else if (name.contains("/spnr set")) {
                    this.openPlayerSelectGui(player, 0, "set");
                } else if (name.contains("/spnr change")) {
                    this.openPlayerSelectGui(player, 0, "change");
                } else if (name.contains("/spnr break")) {
                    player.performCommand("spnr break");
                    player.closeInventory();
                } else if (name.contains("/spwn on/off")) {
                    Inventory toggleGui = Bukkit.createInventory((InventoryHolder)null, 9, String.valueOf(ChatColor.GOLD) + "Toggle Spawner");
                    toggleGui.setItem(3, this.makeMenuItem(Material.LIME_WOOL, String.valueOf(ChatColor.GREEN) + "ON"));
                    toggleGui.setItem(5, this.makeMenuItem(Material.RED_WOOL, String.valueOf(ChatColor.RED) + "OFF"));
                    toggleGui.setItem(8, this.makeMenuItem(Material.BARRIER, String.valueOf(ChatColor.RED) + "Back"));
                    player.openInventory(toggleGui);
                } else if (name.contains("Close")) {
                    player.closeInventory();
                }
            } else if (event.getView().getTitle().startsWith(String.valueOf(ChatColor.BLUE) + "Select Player")) {
                event.setCancelled(true);
                String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (clicked.getType() == Material.PLAYER_HEAD) {
                    if (event.getView().getTitle().contains("set")) {
                        player.performCommand("spnr set " + name);
                    } else {
                        player.performCommand("spnr change " + name);
                    }

                    player.closeInventory();
                } else if (clicked.getType() == Material.ARROW) {
                    if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                        this.openPlayerSelectGui(player, 1, event.getView().getTitle().contains("set") ? "set" : "change");
                    } else {
                        this.openPlayerSelectGui(player, 0, event.getView().getTitle().contains("set") ? "set" : "change");
                    }
                } else if (clicked.getType() == Material.BARRIER) {
                    this.openSpawnerGui(player);
                }
            } else if (event.getView().getTitle().equals(String.valueOf(ChatColor.GOLD) + "Toggle Spawner")) {
                event.setCancelled(true);
                Block target = (Block)this.spawnerContext.get(player.getUniqueId());
                if (target == null) {
                    return;
                }

                BlockState state = target.getState();
                if (!(state instanceof CreatureSpawner)) {
                    return;
                }

                CreatureSpawner spawner = (CreatureSpawner)state;
                PersistentDataContainer data = spawner.getPersistentDataContainer();
                String name = clicked.getItemMeta().getDisplayName();
                if (name.contains("ON")) {
                    data.set(this.spawnerToggleKey, PersistentDataType.STRING, "on");
                    spawner.update(true);
                    player.sendMessage(String.valueOf(ChatColor.GREEN) + "Spawner toggled ON");
                    player.closeInventory();
                } else if (name.contains("OFF")) {
                    data.set(this.spawnerToggleKey, PersistentDataType.STRING, "off");
                    spawner.update(true);
                    player.sendMessage(String.valueOf(ChatColor.GREEN) + "Spawner toggled OFF");
                    player.closeInventory();
                } else if (name.contains("Back")) {
                    this.openSpawnerGui(player);
                }
            }
        }

    }

    private void openPlayerSelectGui(Player player, int page, String mode) {
        int size = 54;
        Inventory gui = Bukkit.createInventory((InventoryHolder)null, size, String.valueOf(ChatColor.BLUE) + "Select Player (" + mode + ") Page " + (page + 1));
        List<Player> online = new ArrayList(Bukkit.getOnlinePlayers());
        int start = page * 45;
        int end = Math.min(start + 45, online.size());

        for(int i = start; i < end; ++i) {
            Player target = (Player)online.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            String var10001 = String.valueOf(ChatColor.YELLOW);
            meta.setDisplayName(var10001 + target.getName());
            head.setItemMeta(meta);
            gui.setItem(i - start, head);
        }

        if (page > 0) {
            gui.setItem(45, this.makeMenuItem(Material.ARROW, String.valueOf(ChatColor.GREEN) + "Previous Page"));
        }

        if (end < online.size()) {
            gui.setItem(53, this.makeMenuItem(Material.ARROW, String.valueOf(ChatColor.GREEN) + "Next Page"));
        }

        gui.setItem(49, this.makeMenuItem(Material.BARRIER, String.valueOf(ChatColor.RED) + "Back"));
        player.openInventory(gui);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        boolean isAdmin = player.hasPermission("wp.spnr.admin");
        boolean hasNode = player.hasPermission("wp.spnr") || isAdmin;

        if (label.equalsIgnoreCase("spnr")) {
            if (args.length >= 1 && args[0].equalsIgnoreCase("set")) {
                if (!hasNode) {
                    player.sendMessage(ChatColor.RED + "You lack permission: wp.spnr");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /spnr set <player>");
                    return true;
                }
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner spawner) {
                        PersistentDataContainer data = spawner.getPersistentDataContainer();
                        data.set(this.spawnerOwnerKey, PersistentDataType.STRING, args[1]);
                        spawner.update(true);
                        player.sendMessage(ChatColor.GREEN + "Spawner owner set to " + args[1]);
                    }
                }
                return true;

            } else if (args.length >= 1 && args[0].equalsIgnoreCase("change")) {
                if (!isAdmin) {
                    player.sendMessage(ChatColor.RED + "You lack permission: wp.spnr.admin");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /spnr change <player>");
                    return true;
                }
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner spawner) {
                        PersistentDataContainer data = spawner.getPersistentDataContainer();
                        data.set(this.spawnerOwnerKey, PersistentDataType.STRING, args[1]);
                        spawner.update(true);
                        player.sendMessage(ChatColor.GREEN + "Spawner owner changed to " + args[1]);
                    }
                }
                return true;

            } else if (args.length >= 1 && args[0].equalsIgnoreCase("check")) {
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner spawner) {
                        PersistentDataContainer data = spawner.getPersistentDataContainer();
                        String owner = data.get(this.spawnerOwnerKey, PersistentDataType.STRING);
                        String mobType = data.get(this.spawnerMobKey, PersistentDataType.STRING);
                        if (mobType == null) mobType = spawner.getSpawnedType().name();
                        player.sendMessage(ChatColor.YELLOW + "Spawner type: " + mobType);
                        player.sendMessage(ChatColor.YELLOW + "Owner: " + (owner != null ? owner : "None"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not looking at a spawner.");
                }
                return true;

            } else if (args.length >= 1 && args[0].equalsIgnoreCase("break")) {
                if (!hasNode) {
                    player.sendMessage(ChatColor.RED + "You lack permission: wp.spnr");
                    return true;
                }
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner spawner) {
                        PersistentDataContainer data = spawner.getPersistentDataContainer();
                        String mobType = data.get(this.spawnerMobKey, PersistentDataType.STRING);
                        if (mobType == null) mobType = spawner.getSpawnedType().name();
                        String owner = data.get(this.spawnerOwnerKey, PersistentDataType.STRING);

                        // Remove spawner block
                        target.setType(Material.AIR);

                        // Drop spawner item
                        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);
                        ItemMeta meta = spawnerItem.getItemMeta();
                        meta.setDisplayName(ChatColor.GOLD + mobType + " Spawner");
                        PersistentDataContainer itemData = meta.getPersistentDataContainer();
                        itemData.set(this.spawnerMobKey, PersistentDataType.STRING, mobType);
                        if (owner != null) {
                            itemData.set(this.spawnerOwnerKey, PersistentDataType.STRING, owner);
                        }
                        spawnerItem.setItemMeta(meta);
                        target.getWorld().dropItemNaturally(target.getLocation(), spawnerItem);

                        // Drop corresponding spawn egg
                        try {
                            EntityType type = EntityType.valueOf(mobType.toUpperCase());
                            Material eggMat = Material.valueOf(type.name() + "_SPAWN_EGG");
                            ItemStack egg = new ItemStack(eggMat, 1);
                            target.getWorld().dropItemNaturally(target.getLocation(), egg);
                        } catch (IllegalArgumentException ex) {
                            // If no spawn egg exists for this mob type, skip
                        }

                        player.sendMessage(ChatColor.GREEN + "Spawner broken and dropped as " + mobType + " Spawner"
                                + (owner != null ? " (owner: " + owner + ")." : " (natural, no owner)."));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not looking at a spawner.");
                }
                return true;
            }


        } else if (label.equalsIgnoreCase("spwn")) {
            if (args.length >= 1) {
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner spawner) {
                        PersistentDataContainer data = spawner.getPersistentDataContainer();
                        String owner = data.get(this.spawnerOwnerKey, PersistentDataType.STRING);

                        boolean isOwner = owner != null && owner.equalsIgnoreCase(player.getName());

                        if (!isOwner && !hasNode) {
                            player.sendMessage(ChatColor.RED + "You are not the owner of this spawner.");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("on")) {
                            data.set(this.spawnerToggleKey, PersistentDataType.STRING, "on");
                            spawner.update(true);
                            player.sendMessage(ChatColor.GREEN + "Spawner toggled ON");
                        } else if (args[0].equalsIgnoreCase("off")) {
                            data.set(this.spawnerToggleKey, PersistentDataType.STRING, "off");
                            spawner.update(true);
                            player.sendMessage(ChatColor.RED + "Spawner toggled OFF");
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "Usage: /spwn on|off");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not looking at a spawner.");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Usage: /spwn on|off");
            }
            return true;
        }

        return false;
    }
}
