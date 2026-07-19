package com.alb.wonderplugin.modules;

import com.alb.wonderplugin.modules.filebridge.SyncCommand;
import com.alb.wonderplugin.util.FileDownloader;
import com.alb.wonderplugin.WonderPlugin;
import com.alb.wonderplugin.api.Module;
import org.bukkit.plugin.PluginManager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBridgeModule implements Module {

    private WonderPlugin plugin;

    @Override
    public void onEnable(WonderPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        // Pass this module instance to SyncCommand
        plugin.getCommand("bridge").setExecutor(new SyncCommand(this));

        PluginManager pm = plugin.getServer().getPluginManager();
        // pm.registerEvents(new SomeListener(plugin), plugin);
    }

    @Override
    public void onDisable() {
        // Nothing special, but you could flush or cleanup if needed
    }

    @Override
    public String getName() {
        return "FileBridge";
    }

    public void startDownloadFromConfig() {
        String url = plugin.getConfig().getString("source");
        boolean overwrite = plugin.getConfig().getBoolean("overwrite", true);

        plugin.getLogger().info("Downloading from config: " + url);
        try {
            FileDownloader.downloadAndMirror(plugin, url, overwrite, plugin.getLogger());
        } catch (Exception e) {
            plugin.getLogger().severe("Download failed: " + e.getMessage());
        }
    }

    public void startDownloadFromLink(String url) {
        boolean overwrite = plugin.getConfig().getBoolean("overwrite", true);

        plugin.getLogger().info("Downloading from provided link: " + url);
        try {
            FileDownloader.downloadAndMirror(plugin, url, overwrite, plugin.getLogger());
        } catch (Exception e) {
            plugin.getLogger().severe("Download failed: " + e.getMessage());
        }
    }

    public void startDownloadRaw(String url) {
        boolean overwrite = plugin.getConfig().getBoolean("overwrite", true);

        plugin.getLogger().info("Downloading raw plugin file: " + url);
        try {
            FileDownloader.downloadRaw(plugin, url, overwrite, plugin.getLogger());
        } catch (Exception e) {
            plugin.getLogger().severe("Raw download failed: " + e.getMessage());
        }
    }

    public void startDownloadFromZips() {
        boolean overwrite = plugin.getConfig().getBoolean("overwrite", true);

        Path zipsFolder = Paths.get(plugin.getDataFolder().toString(), "zips");
        Path pluginsFolder = plugin.getDataFolder().getParentFile().toPath();

        try {
            Files.createDirectories(zipsFolder);
            DirectoryStream<Path> stream = Files.newDirectoryStream(zipsFolder, "*.zip");
            for (Path zip : stream) {
                plugin.getLogger().info("Importing local zip: " + zip.getFileName());
                FileDownloader.unzip(zip, pluginsFolder);

                Files.deleteIfExists(zip);
                plugin.getLogger().info("Deleted zip: " + zip.getFileName());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to import zips: " + e.getMessage());
        }
    }
}
