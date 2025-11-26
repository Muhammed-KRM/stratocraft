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
import java.util.Map;

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
                    
                    // CHUNK YÜKLEME KONTROLÜ - Sadece chunk yüklüyse çalış
                    if (!drillLoc.getChunk().isLoaded()) {
                        continue; // Chunk yüklü değilse atla
                    }
                    
                    // Eğer çıktı bloğu hava ise, rastgele maden düşür
                    if (outputBlock.getType() == Material.AIR || outputBlock.getType() == Material.CHEST) {
                        Material ore = getRandomOre(s.getLevel());
                        if (ore != null) {
                            if (outputBlock.getType() == Material.CHEST) {
                                // Sandığa ekle
                                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) outputBlock.getState();
                                ItemStack oreItem = new ItemStack(ore, random.nextInt(3) + 1);
                                
                                // Sandık dolu mu kontrol et
                                if (chest.getInventory().firstEmpty() == -1) {
                                    // Sandık dolu - üretimi durdur
                                    // Oyunculara uyarı göndermek için klan üyelerini bul
                                    for (org.bukkit.entity.Player member : Bukkit.getOnlinePlayers()) {
                                        if (clan.getMembers().containsKey(member.getUniqueId()) &&
                                            member.getLocation().distance(drillLoc) <= 50) {
                                            member.sendMessage("§c§l[MATKAP] Sandık dolu! Üretim durdu.");
                                        }
                                    }
                                    continue; // Bu matkap için üretimi durdur
                                }
                                
                                // Sandığa ekle
                                Map<Integer, ItemStack> remaining = chest.getInventory().addItem(oreItem);
                                if (!remaining.isEmpty()) {
                                    // Eğer eklenemeyen eşya varsa (nadir durum), üretimi durdur
                                    continue;
                                }
                            } else {
                                // Sadece sandık yoksa yere düşür (lag önleme: despawn süresini kısalt)
                                org.bukkit.entity.Item item = drillLoc.getWorld().dropItemNaturally(
                                    drillLoc.clone().add(0, -1, 0),
                                    new ItemStack(ore, random.nextInt(3) + 1)
                                );
                                // Despawn süresini 2 dakikaya düşür (normal 5 dakika)
                                item.setPickupDelay(20);
                                org.bukkit.Bukkit.getScheduler().runTaskLater(
                                    org.bukkit.Bukkit.getPluginManager().getPlugin("Stratocraft"),
                                    () -> { if (item.isValid() && !item.isDead()) item.remove(); },
                                    2400L // 2 dakika
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

