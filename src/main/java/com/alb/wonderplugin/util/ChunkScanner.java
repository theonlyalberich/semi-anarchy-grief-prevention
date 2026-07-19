package com.alb.wonderplugin.util;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public class ChunkScanner {

    public static int countComponents(Chunk chunk, Set<Material> materials) {
        int count = 0;
        int minY = chunk.getWorld().getMinHeight();
        int maxY = chunk.getWorld().getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (materials.contains(block.getType())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
