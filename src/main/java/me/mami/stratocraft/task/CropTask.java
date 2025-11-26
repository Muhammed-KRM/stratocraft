package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class CropTask extends BukkitRunnable {
    private final TerritoryManager territoryManager;

    public CropTask(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    @Override
    public void run() {
        // Her klan için Tarım Hızlandırıcı kontrolü
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            for (Structure s : clan.getStructures()) {
                if (s.getType() == Structure.Type.CROP_ACCELERATOR) {
                    Location center = s.getLocation();
                    int radius = 15; // 15 blok yarıçap
                    int level = s.getLevel();
                    
                    // Etraftaki ekinleri büyüt
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block block = center.clone().add(x, 0, z).getBlock();
                            Material type = block.getType();
                            
                            // Ekin kontrolü
                            if (isCrop(type)) {
                                // Modern API kullan (1.13+)
                                if (type == Material.BEETROOTS || type == Material.CARROTS ||
                                        type == Material.POTATOES || type == Material.WHEAT) {
                                    // Modern API için
                                    if (Math.random() < (0.1 + level * 0.05)) {
                                        org.bukkit.block.data.Ageable ageable =
                                                (org.bukkit.block.data.Ageable) block.getBlockData();
                                        if (ageable.getAge() < ageable.getMaximumAge()) {
                                            ageable.setAge(ageable.getAge() + 1);
                                            block.setBlockData(ageable);

                                            block.getWorld().spawnParticle(
                                                    org.bukkit.Particle.VILLAGER_HAPPY,
                                                    block.getLocation().add(0.5, 0.5, 0.5),
                                                    3
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean isCrop(Material type) {
        return type == Material.WHEAT || type == Material.CARROTS || 
               type == Material.POTATOES || type == Material.BEETROOTS ||
               type == Material.NETHER_WART || type == Material.COCOA;
    }
}

