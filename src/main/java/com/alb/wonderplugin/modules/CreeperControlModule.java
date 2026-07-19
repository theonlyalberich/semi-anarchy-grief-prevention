//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import java.util.HashMap;
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
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CreeperControlModule implements Module, CommandExecutor, Listener {
    private WonderPlugin plugin;
    private NamespacedKey creeperPercentKey;
    private Map<UUID, Block> context = new HashMap();

    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;
        boolean enabled = plugin.getConfig().getBoolean("modules.creepercontrol.enabled", true);
        if (enabled) {
            this.creeperPercentKey = new NamespacedKey(plugin, "creeper_percent");
            plugin.getCommand("creep").setExecutor(this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public void onDisable() {
    }

    public String getName() {
        return "CreeperControl";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("wp.creep.admin")) {
                player.sendMessage(String.valueOf(ChatColor.RED) + "You lack permission: wp.creep.admin");
                return true;
            } else {
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() == Material.SPAWNER) {
                    BlockState state = target.getState();
                    if (state instanceof CreatureSpawner) {
                        CreatureSpawner spawner = (CreatureSpawner)state;
                        if (spawner.getSpawnedType() != EntityType.CREEPER) {
                            player.sendMessage(String.valueOf(ChatColor.RED) + "This is not a creeper spawner.");
                            return true;
                        }

                        this.context.put(player.getUniqueId(), target);
                        this.openGui(player);
                    }

                    return true;
                } else {
                    player.sendMessage(String.valueOf(ChatColor.RED) + "You are not looking at a spawner.");
                    return true;
                }
            }
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Only players can use this command.");
            return true;
        }
    }

    private void openGui(Player player) {
        Inventory gui = Bukkit.createInventory((InventoryHolder)null, 27, String.valueOf(ChatColor.DARK_GREEN) + "Creeper Charge %");

        for(int i = 0; i <= 20; ++i) {
            int percent = i * 5;
            ItemStack item = new ItemStack(Material.CREEPER_SPAWN_EGG);
            ItemMeta meta = item.getItemMeta();
            String var10001 = ChatColor.YELLOW.toString();
            meta.setDisplayName(var10001 + percent + "%");
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        if (event.getView().getTitle().equals(String.valueOf(ChatColor.DARK_GREEN) + "Creeper Charge %")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (name.endsWith("%")) {
                int percent = Integer.parseInt(name.replace("%", ""));
                Block target = (Block)this.context.get(player.getUniqueId());
                if (target == null) {
                    return;
                }

                BlockState state = target.getState();
                if (state instanceof CreatureSpawner) {
                    CreatureSpawner spawner = (CreatureSpawner)state;
                    PersistentDataContainer data = spawner.getPersistentDataContainer();
                    data.set(this.creeperPercentKey, PersistentDataType.INTEGER, percent);
                    spawner.update(true);
                    String var10001 = String.valueOf(ChatColor.GREEN);
                    player.sendMessage(var10001 + "Creeper spawner set to " + percent + "% charged creepers.");
                    player.closeInventory();
                }
            }
        }

    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        BlockState state = event.getSpawner().getBlock().getState();
        if (state instanceof CreatureSpawner spawner) {
            if (spawner.getSpawnedType() == EntityType.CREEPER) {
                PersistentDataContainer data = spawner.getPersistentDataContainer();
                Integer percent = (Integer)data.get(this.creeperPercentKey, PersistentDataType.INTEGER);
                if (percent != null && percent > 0) {
                    Entity var7 = event.getEntity();
                    if (var7 instanceof Creeper) {
                        Creeper creeper = (Creeper)var7;
                        int roll = (int)(Math.random() * (double)100.0F);
                        if (roll < percent) {
                            creeper.setPowered(true);
                        }
                    }
                }
            }
        }

    }
}
