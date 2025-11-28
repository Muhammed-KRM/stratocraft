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
                    
                    // CHUNK YÜKLEME KONTROLÜ - Sadece chunk yüklüyse çalış
                    if (!drillLoc.getChunk().isLoaded()) {
                        continue; // Chunk yüklü değilse atla
                    }
                    
                    // YAKIT KONTROLÜ - Matkabın altında veya yanında kömür olmalı
                    boolean hasFuel = checkFuel(drillLoc);
                    if (!hasFuel) {
                        // Yakıt yok - üretimi durdur ve uyarı gönder
                        for (org.bukkit.entity.Player member : Bukkit.getOnlinePlayers()) {
                            if (clan.getMembers().containsKey(member.getUniqueId()) &&
                                member.getLocation().distance(drillLoc) <= 50) {
                                member.sendMessage("§c§l[MATKAP] Yakıt yok! Kömür ekleyin.");
                            }
                        }
                        continue; // Bu matkap için üretimi durdur
                    }
                    
                    // Yakıt tüket (her üretimde 1 kömür)
                    consumeFuel(drillLoc);
                    
                    // Madenci bloğunun altına maden düşür
                    Block outputBlock = drillLoc.clone().add(0, -1, 0).getBlock();
                    
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
    
    /**
     * Yakıt kontrolü - Matkabın altında veya yanında kömür var mı?
     */
    private boolean checkFuel(Location drillLoc) {
        // Matkabın altında kömür kontrolü
        Block belowBlock = drillLoc.clone().add(0, -1, 0).getBlock();
        if (belowBlock.getType() == Material.CHEST) {
            // Sandık varsa içinde kömür ara
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) belowBlock.getState();
            for (ItemStack item : chest.getInventory().getContents()) {
                if (item != null && (item.getType() == Material.COAL || 
                                     item.getType() == Material.CHARCOAL ||
                                     item.getType() == Material.COAL_BLOCK)) {
                    return true;
                }
            }
        }
        
        // Matkabın yanlarında kömür kontrolü (4 yön)
        Block[] sides = {
            drillLoc.clone().add(1, 0, 0).getBlock(),  // Doğu
            drillLoc.clone().add(-1, 0, 0).getBlock(), // Batı
            drillLoc.clone().add(0, 0, 1).getBlock(),  // Kuzey
            drillLoc.clone().add(0, 0, -1).getBlock()  // Güney
        };
        
        for (Block side : sides) {
            if (side.getType() == Material.COAL_BLOCK) {
                return true;
            }
            if (side.getType() == Material.CHEST) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) side.getState();
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item != null && (item.getType() == Material.COAL || 
                                         item.getType() == Material.CHARCOAL ||
                                         item.getType() == Material.COAL_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Yakıt tüket - 1 kömür veya 1/9 kömür bloğu
     */
    private void consumeFuel(Location drillLoc) {
        // Önce matkabın altındaki sandıktan tüket
        Block belowBlock = drillLoc.clone().add(0, -1, 0).getBlock();
        if (belowBlock.getType() == Material.CHEST) {
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) belowBlock.getState();
            for (int i = 0; i < chest.getInventory().getSize(); i++) {
                ItemStack item = chest.getInventory().getItem(i);
                if (item != null && (item.getType() == Material.COAL || 
                                     item.getType() == Material.CHARCOAL)) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        chest.getInventory().setItem(i, null);
                    }
                    return; // Yakıt tüketildi
                }
            }
        }
        
        // Sandıkta yoksa yanlardaki kömür bloklarından tüket
        Block[] sides = {
            drillLoc.clone().add(1, 0, 0).getBlock(),
            drillLoc.clone().add(-1, 0, 0).getBlock(),
            drillLoc.clone().add(0, 0, 1).getBlock(),
            drillLoc.clone().add(0, 0, -1).getBlock()
        };
        
        for (Block side : sides) {
            if (side.getType() == Material.COAL_BLOCK) {
                // Kömür bloğunu 8 kömüre dönüştür (1 tüketildi)
                side.setType(Material.AIR);
                side.getWorld().dropItemNaturally(
                    side.getLocation(),
                    new ItemStack(Material.COAL, 8)
                );
                return; // Yakıt tüketildi
            }
            if (side.getType() == Material.CHEST) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) side.getState();
                for (int i = 0; i < chest.getInventory().getSize(); i++) {
                    ItemStack item = chest.getInventory().getItem(i);
                    if (item != null && (item.getType() == Material.COAL || 
                                         item.getType() == Material.CHARCOAL)) {
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            chest.getInventory().setItem(i, null);
                        }
                        return; // Yakıt tüketildi
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

