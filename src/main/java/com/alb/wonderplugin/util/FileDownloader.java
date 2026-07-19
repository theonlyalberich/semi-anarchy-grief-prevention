package com.alb.wonderplugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.plugin.java.JavaPlugin;

public class FileDownloader {

    public static void downloadAndMirror(JavaPlugin plugin, String url, boolean overwrite, Logger logger) throws IOException {
        // Resolve the directory where the plugin JAR lives (plugins/)
        Path pluginJarDir = plugin.getDataFolder().getParentFile().toPath();

        // Always name the downloaded file Bridge.zip
        Path targetPath = pluginJarDir.resolve("Bridge.zip");

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Unzip directly into plugins folder
        unzip(targetPath, pluginJarDir);
        logger.info("Unzipped Bridge.zip into: " + pluginJarDir);

        // Delete Bridge.zip after successful extraction
        try {
            Files.deleteIfExists(targetPath);
            logger.info("Deleted Bridge.zip after extraction");
        } catch (IOException e) {
            logger.warning("Could not delete Bridge.zip: " + e.getMessage());
        }
    }

    // New method: download raw file as-is, no unzip, no deletion
    public static void downloadRaw(JavaPlugin plugin, String url, boolean overwrite, Logger logger) throws IOException {
        Path pluginJarDir = plugin.getDataFolder().getParentFile().toPath();

        // Preserve original filename from URL
        String fileName = Path.of(new URL(url).getPath()).getFileName().toString();
        Path targetPath = pluginJarDir.resolve(fileName);

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("Downloaded raw file retained at: " + targetPath);
    }

    public static void unzip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = destDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
