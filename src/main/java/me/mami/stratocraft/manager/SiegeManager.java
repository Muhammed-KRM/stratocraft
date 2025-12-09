package me.mami.stratocraft.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.model.Clan;

public class SiegeManager {
    // Savunan Klan ID -> Saldıran Klan ID
    private final Map<Clan, Clan> activeSieges = new HashMap<>();
    private BuffManager buffManager;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    public void setBuffManager(BuffManager bm) {
        this.buffManager = bm;
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private double getLootPercentage() {
        return balanceConfig != null ? balanceConfig.getSiegeLootPercentage() : 0.5;
    }
    
    private double getChestLootPercentage() {
        return balanceConfig != null ? balanceConfig.getSiegeChestLootPercentage() : 0.5;
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
        
        // Ödül mantığı (config'den)
        double lootPercentage = getLootPercentage();
        double loot = loser.getBalance() * lootPercentage;
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
        
        // Kazanan klan ödülü (para) (config'den)
        double lootPercentage = getLootPercentage();
        double loot = surrenderingClan.getBalance() * lootPercentage;
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
        
        int maxSearchRadius = balanceConfig != null ? balanceConfig.getSiegeMaxSearchRadius() : 100;
        int searchRadius = Math.min(surrenderingClan.getTerritory().getRadius(), maxSearchRadius);
        
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
        
        // Sandık bulma ve işleme işini zamana yay (lag önleme)
        new ChestScannerAndLootTask(chunksToScan, attacker, center, searchRadius).runTaskTimer(plugin, 1L, 1L);
    }
    
    /**
     * Sandık bulma ve işleme işini zamana yay (lag önleme)
     * getTileEntities() kullanarak performanslı tarama
     */
    private class ChestScannerAndLootTask extends org.bukkit.scheduler.BukkitRunnable {
        private final java.util.List<org.bukkit.Chunk> chunksToScan;
        private final Clan attacker;
        private final int searchRadius;
        private final Location center;
        private int currentChunkIndex = 0;
        private final java.util.List<Location> foundChests = new java.util.ArrayList<>();
        
        public ChestScannerAndLootTask(java.util.List<org.bukkit.Chunk> chunks, Clan attacker, Location center, int radius) {
            this.chunksToScan = chunks;
            this.attacker = attacker;
            this.center = center;
            this.searchRadius = radius;
        }
        
        @Override
        public void run() {
            int chunksProcessed = 0;
            
            // Her tick'te config'den belirlenen kadar chunk tara (sunucuyu yormaz)
            int chunksPerTick = balanceConfig != null ? balanceConfig.getSiegeChunksPerTick() : 5;
            while (currentChunkIndex < chunksToScan.size() && chunksProcessed < chunksPerTick) {
                org.bukkit.Chunk chunk = chunksToScan.get(currentChunkIndex);
                
                if (chunk.isLoaded()) {
                    // Chunk içindeki TileEntity'leri al (block.getType() döngüsünden 100 kat daha hızlı!)
                    for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                        if (state instanceof Chest) {
                            Location chestLoc = state.getLocation();
                            if (me.mami.stratocraft.util.GeometryUtil.isInsideRadius(center, chestLoc, searchRadius)) {
                                foundChests.add(chestLoc);
                            }
                        }
                    }
                }
                
                currentChunkIndex++;
                chunksProcessed++;
            }
            
            // Tarama bitti, şimdi loot task'i başlat
            if (currentChunkIndex >= chunksToScan.size()) {
                if (!foundChests.isEmpty()) {
                    new ChestLootTask(foundChests, attacker, 0).runTaskTimer(
                        me.mami.stratocraft.Main.getInstance(), 1L, 1L);
                }
                this.cancel();
            }
        }
    }
    
    /**
     * Sandık itemlerini zamana yayarak işle (lag önleme)
     */
    private class ChestLootTask extends org.bukkit.scheduler.BukkitRunnable {
        private final java.util.List<Location> chestLocations;
        private final Clan attacker;
        private int currentIndex;
        
        public ChestLootTask(java.util.List<Location> chestLocations, Clan attacker, int startIndex) {
            this.chestLocations = chestLocations;
            this.attacker = attacker;
            this.currentIndex = startIndex;
        }
        
        @Override
        public void run() {
            int processed = 0;
            
            int chestsPerTick = balanceConfig != null ? balanceConfig.getSiegeChestsPerTick() : 2;
            while (currentIndex < chestLocations.size() && processed < chestsPerTick) {
                Location chestLoc = chestLocations.get(currentIndex);
                Block block = chestLoc.getBlock();
                
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                    if (block.getState() instanceof Chest) {
                        Chest chest = (Chest) block.getState();
                        org.bukkit.inventory.Inventory inv = chest.getInventory();
                        
                        // Her item'ın yüzdesini al (config'den)
                        double chestLootPercentage = getChestLootPercentage();
                        for (int i = 0; i < inv.getSize(); i++) {
                            ItemStack item = inv.getItem(i);
                            if (item != null && item.getType() != Material.AIR) {
                                int halfAmount = (int) Math.ceil(item.getAmount() * chestLootPercentage);
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

