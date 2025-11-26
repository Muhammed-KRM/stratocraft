package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DrillTask extends BukkitRunnable {
    private final TerritoryManager territoryManager;
    private final Random random = new Random();

    public DrillTask(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    @Override
    public void run() {
        // Her klan için Otomatik Madenci kontrolü
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            for (Structure s : clan.getStructures()) {
                if (s.getType() == Structure.Type.AUTO_DRILL) {
                    // Otomatik maden üretimi (her 30 saniyede bir)
                    Location drillLoc = s.getLocation();
                    
                    // Madenci bloğunun altına maden düşür
                    Block outputBlock = drillLoc.clone().add(0, -1, 0).getBlock();
                    
                    // Eğer çıktı bloğu hava ise, rastgele maden düşür
                    if (outputBlock.getType() == Material.AIR || outputBlock.getType() == Material.CHEST) {
                        Material ore = getRandomOre(s.getLevel());
                        if (ore != null) {
                            if (outputBlock.getType() == Material.CHEST) {
                                // Sandığa ekle
                                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) outputBlock.getState();
                                chest.getInventory().addItem(new ItemStack(ore, random.nextInt(3) + 1));
                            } else {
                                // Yere düşür
                                drillLoc.getWorld().dropItemNaturally(
                                    drillLoc.clone().add(0, -1, 0),
                                    new ItemStack(ore, random.nextInt(3) + 1)
                                );
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Material getRandomOre(int level) {
        // Seviye arttıkça daha değerli madenler
        double rand = random.nextDouble();
        if (level >= 5) {
            if (rand < 0.1) return Material.DIAMOND;
            if (rand < 0.3) return Material.EMERALD;
            if (rand < 0.6) return Material.GOLD_INGOT;
            return Material.IRON_INGOT;
        } else if (level >= 3) {
            if (rand < 0.2) return Material.GOLD_INGOT;
            if (rand < 0.5) return Material.IRON_INGOT;
            return Material.COAL;
        } else {
            if (rand < 0.3) return Material.IRON_INGOT;
            return Material.COAL;
        }
    }
}

