package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class SiegeManager {
    // Savunan Klan ID -> Saldıran Klan ID
    private final Map<Clan, Clan> activeSieges = new HashMap<>();
    private BuffManager buffManager;

    public void setBuffManager(BuffManager bm) {
        this.buffManager = bm;
    }

    public void startSiege(Clan attacker, Clan defender, Player attackerPlayer) {
        // Offline baskın önleme: Savunan klandan en az 1 kişi online olmalı
        boolean isDefenderOnline = defender.getMembers().keySet().stream()
            .anyMatch(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                return p != null && p.isOnline();
            });
        
        if (!isDefenderOnline) {
            if (attackerPlayer != null) {
                attackerPlayer.sendMessage("§cKuşatma başlatmak için karşı klandan en az 1 kişi online olmalı!");
            }
            return;
        }
        
        activeSieges.put(defender, attacker);
        Bukkit.broadcastMessage("§4§lSAVAŞ İLANI! §e" + attacker.getName() + " klani, " + defender.getName() + " klanına savaş açtı!");
        // SiegeTimer burada başlatılmalı
    }

    public void endSiege(Clan winner, Clan loser) {
        activeSieges.remove(loser);
        Bukkit.broadcastMessage("§2§lZAFER! §a" + winner.getName() + " klanı, " + loser.getName() + " klanını fethetti!");
        
        // Ödül mantığı
        double loot = loser.getBalance() * 0.5;
        winner.deposit(loot);
        loser.withdraw(loot);
        
        // FATİH BUFF'I UYGULA
        if (buffManager != null) {
            buffManager.applyConquerorBuff(winner);
        }
    }

    /**
     * Beyaz Bayrak - Pes Etme Sistemi
     * Klan savaşta pes eder, klan yok olmaz ama sandıkların yarısı gider
     */
    public void surrender(Clan surrenderingClan, ClanManager clanManager) {
        if (!isUnderSiege(surrenderingClan)) {
            return; // Savaşta değil
        }
        
        Clan attacker = activeSieges.get(surrenderingClan);
        if (attacker == null) return;
        
        // Savaşı bitir
        activeSieges.remove(surrenderingClan);
        
        // Mesaj
        Bukkit.broadcastMessage("§f§lBEYAZ BAYRAK! §e" + surrenderingClan.getName() + " klanı pes etti!");
        
        // Klan yok olmaz, sadece sandıkların yarısı gider
        // Sandık itemlerinin yarısını al
        takeHalfChestItems(surrenderingClan, attacker);
        
        // Kazanan klan ödülü (para)
        double loot = surrenderingClan.getBalance() * 0.5;
        attacker.deposit(loot);
        surrenderingClan.withdraw(loot);
        
        // Fatih buff'ı uygula
        if (buffManager != null) {
            buffManager.applyConquerorBuff(attacker);
        }
    }

    /**
     * Klan bölgesindeki tüm sandıkların itemlerinin yarısını al
     * ASYNC ve zamana yayılmış (lag önleme)
     */
    private void takeHalfChestItems(Clan surrenderingClan, Clan attacker) {
        if (surrenderingClan.getTerritory() == null) return;
        
        Location center = surrenderingClan.getTerritory().getCenter();
        if (center == null) return;
        
        int searchRadius = Math.min(surrenderingClan.getTerritory().getRadius(), 100);
        
        // Chunk bazlı tarama listesi oluştur (async thread'de)
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin == null) return;
        
        // Önce chunk listesini hazırla (sync thread'de)
        java.util.List<org.bukkit.Chunk> chunksToScan = new java.util.ArrayList<>();
        for (int x = -searchRadius; x <= searchRadius; x += 16) {
            for (int z = -searchRadius; z <= searchRadius; z += 16) {
                Location chunkLoc = center.clone().add(x, 0, z);
                org.bukkit.Chunk chunk = chunkLoc.getChunk();
                if (chunk != null && !chunksToScan.contains(chunk)) {
                    chunksToScan.add(chunk);
                }
            }
        }
        
        // Chunk'ları tara ve sandık lokasyonlarını topla (SYNC thread'de - Bukkit API gerekiyor)
        java.util.List<Location> chestLocations = new java.util.ArrayList<>();
        
        for (org.bukkit.Chunk chunk : chunksToScan) {
            if (!chunk.isLoaded()) continue;
            
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y <= 256; y += 4) { // Her 4 blokta bir kontrol
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                            Location blockLoc = block.getLocation();
                            if (me.mami.stratocraft.util.GeometryUtil.isInsideRadius(center, blockLoc, searchRadius)) {
                                chestLocations.add(blockLoc);
                            }
                        }
                    }
                }
            }
        }
        
        // Sandıkları zamana yayarak işle (lag önleme)
        if (!chestLocations.isEmpty()) {
            new ChestLootTask(chestLocations, attacker, 0).runTaskTimer(plugin, 1L, 1L);
        }
    }
    
    /**
     * Sandık itemlerini zamana yayarak işle (lag önleme)
     */
    private class ChestLootTask extends org.bukkit.scheduler.BukkitRunnable {
        private final java.util.List<Location> chestLocations;
        private final Clan attacker;
        private int currentIndex;
        private static final int CHESTS_PER_TICK = 2; // Her tick'te 2 sandık işle
        
        public ChestLootTask(java.util.List<Location> chestLocations, Clan attacker, int startIndex) {
            this.chestLocations = chestLocations;
            this.attacker = attacker;
            this.currentIndex = startIndex;
        }
        
        @Override
        public void run() {
            int processed = 0;
            
            while (currentIndex < chestLocations.size() && processed < CHESTS_PER_TICK) {
                Location chestLoc = chestLocations.get(currentIndex);
                Block block = chestLoc.getBlock();
                
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                    if (block.getState() instanceof Chest) {
                        Chest chest = (Chest) block.getState();
                        org.bukkit.inventory.Inventory inv = chest.getInventory();
                        
                        // Her item'ın yarısını al
                        for (int i = 0; i < inv.getSize(); i++) {
                            ItemStack item = inv.getItem(i);
                            if (item != null && item.getType() != Material.AIR) {
                                int halfAmount = item.getAmount() / 2;
                                if (halfAmount > 0) {
                                    ItemStack halfItem = item.clone();
                                    halfItem.setAmount(halfAmount);
                                    
                                    // Yere düşür
                                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1, 0.5), halfItem);
                                    
                                    // Sandıktan çıkar
                                    item.setAmount(item.getAmount() - halfAmount);
                                    inv.setItem(i, item);
                                }
                            }
                        }
                    }
                }
                
                currentIndex++;
                processed++;
            }
            
            // Daha fazla sandık varsa devam et
            if (currentIndex < chestLocations.size()) {
                new ChestLootTask(chestLocations, attacker, currentIndex).runTaskLater(
                    me.mami.stratocraft.Main.getInstance(), 1L);
            }
        }
    }

    public boolean isUnderSiege(Clan clan) {
        return activeSieges.containsKey(clan);
    }
    
    public Clan getAttacker(Clan defender) {
        return activeSieges.get(defender);
    }
}

