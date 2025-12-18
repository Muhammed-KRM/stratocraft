package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.model.Clan;

public class SiegeManager {
    // ✅ YENİ: Çoklu savaş desteği - Her klan için savaşta olduğu klanlar listesi
    // Klan ID -> Savaşta olduğu klan ID'leri (Set)
    private final Map<UUID, Set<UUID>> activeWars = new HashMap<>();
    
    // Eski sistem (geriye dönük uyumluluk için tutuluyor, kaldırılabilir)
    @Deprecated
    private final Map<Clan, Clan> activeSieges = new HashMap<>();
    
    private BuffManager buffManager;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    private AllianceManager allianceManager;

    public void setBuffManager(BuffManager bm) {
        this.buffManager = bm;
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    public void setAllianceManager(AllianceManager am) {
        this.allianceManager = am;
    }
    
    private double getLootPercentage() {
        return balanceConfig != null ? balanceConfig.getSiegeLootPercentage() : 0.5;
    }
    
    private double getChestLootPercentage() {
        return balanceConfig != null ? balanceConfig.getSiegeChestLootPercentage() : 0.5;
    }

    /**
     * ✅ YENİ: İki taraflı savaş başlatma
     * Her iki klan da birbirine saldırabilir
     */
    public void startSiege(Clan attacker, Clan defender, Player attackerPlayer) {
        // Null check
        if (attacker == null || defender == null || attacker.getId().equals(defender.getId())) {
            return;
        }
        
        // Offline baskın önleme: Her iki klandan da en az 1 kişi online olmalı
        boolean isAttackerOnline = attacker.getMembers().keySet().stream()
            .anyMatch(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                return p != null && p.isOnline();
            });
        
        boolean isDefenderOnline = defender.getMembers().keySet().stream()
            .anyMatch(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                return p != null && p.isOnline();
            });
        
        if (!isAttackerOnline) {
            if (attackerPlayer != null) {
                attackerPlayer.sendMessage("§cKuşatma başlatmak için klandan en az 1 kişi online olmalı!");
            }
            return;
        }
        
        if (!isDefenderOnline) {
            if (attackerPlayer != null) {
                attackerPlayer.sendMessage("§cKuşatma başlatmak için karşı klandan en az 1 kişi online olmalı!");
            }
            return;
        }
        
        // ✅ YENİ: İki taraflı savaş kaydı
        UUID attackerId = attacker.getId();
        UUID defenderId = defender.getId();
        
        // Saldıran klanın savaş listesine ekle
        activeWars.computeIfAbsent(attackerId, k -> new HashSet<>()).add(defenderId);
        attacker.addWarringClan(defenderId);
        
        // Savunan klanın savaş listesine ekle
        activeWars.computeIfAbsent(defenderId, k -> new HashSet<>()).add(attackerId);
        defender.addWarringClan(attackerId);
        
        // Eski sistem (geriye dönük uyumluluk)
        activeSieges.put(defender, attacker);
        
        Bukkit.broadcastMessage("§4§lSAVAŞ İLANI! §e" + attacker.getName() + " ve " + defender.getName() + " klanları savaşa girdi!");
    }

    /**
     * ✅ YENİ: İki taraflı savaş bitirme
     * Sadece bu iki klan arasındaki savaşı bitirir
     */
    public void endWar(Clan clan1, Clan clan2) {
        if (clan1 == null || clan2 == null) return;
        
        UUID clan1Id = clan1.getId();
        UUID clan2Id = clan2.getId();
        
        // Her iki klanın savaş listesinden kaldır
        Set<UUID> clan1Wars = activeWars.get(clan1Id);
        if (clan1Wars != null) {
            clan1Wars.remove(clan2Id);
            if (clan1Wars.isEmpty()) {
                activeWars.remove(clan1Id);
            }
        }
        clan1.removeWarringClan(clan2Id);
        
        Set<UUID> clan2Wars = activeWars.get(clan2Id);
        if (clan2Wars != null) {
            clan2Wars.remove(clan1Id);
            if (clan2Wars.isEmpty()) {
                activeWars.remove(clan2Id);
            }
        }
        clan2.removeWarringClan(clan1Id);
        
        // Eski sistem (geriye dönük uyumluluk)
        activeSieges.remove(clan2);
        activeSieges.remove(clan1);
    }
    
    /**
     * ✅ YENİ: Savaş kazanma (kristal kırma durumunda)
     * Ganimet paylaşımı: İttifak varsa paylaşılır
     */
    public void endSiege(Clan winner, Clan loser) {
        if (winner == null || loser == null) return;
        
        // Savaşı bitir
        endWar(winner, loser);
        
        Bukkit.broadcastMessage("§2§lZAFER! §a" + winner.getName() + " klanı, " + loser.getName() + " klanını fethetti!");
        
        // ✅ YENİ: Ganimet paylaşımı - İttifak kontrolü
        double lootPercentage = getLootPercentage();
        double totalLoot = loser.getBalance() * lootPercentage;
        
        // İttifak var mı kontrol et
        if (allianceManager != null) {
            List<me.mami.stratocraft.model.Alliance> winnerAlliances = allianceManager.getAlliances(winner.getId());
            
            // Savaş ittifakı veya tam ittifak var mı?
            List<Clan> alliedClans = new ArrayList<>();
            for (me.mami.stratocraft.model.Alliance alliance : winnerAlliances) {
                if (alliance.getType() == me.mami.stratocraft.model.Alliance.Type.OFFENSIVE || 
                    alliance.getType() == me.mami.stratocraft.model.Alliance.Type.FULL) {
                    UUID otherClanId = alliance.getOtherClan(winner.getId());
                    Clan otherClan = getClanById(otherClanId);
                    if (otherClan != null && otherClan.isAtWarWith(loser.getId())) {
                        // Bu ittifak klanı da aynı klanla savaşta
                        alliedClans.add(otherClan);
                    }
                }
            }
            
            // İttifak varsa ganimet paylaş
            if (!alliedClans.isEmpty()) {
                int totalRecipients = 1 + alliedClans.size(); // Kazanan + ittifak klanları
                double lootPerClan = totalLoot / totalRecipients;
                
                // Kazanan klana payını ver
                winner.deposit(lootPerClan);
                
                // İttifak klanlarına paylarını ver
                for (Clan alliedClan : alliedClans) {
                    alliedClan.deposit(lootPerClan);
                    Bukkit.broadcastMessage("§e" + alliedClan.getName() + " klanı ittifak payı aldı: " + lootPerClan + " altın");
                }
                
                Bukkit.broadcastMessage("§6Ganimet " + totalRecipients + " klan arasında paylaşıldı!");
            } else {
                // İttifak yoksa tüm ganimet kazanan klana
                winner.deposit(totalLoot);
            }
        } else {
            // AllianceManager yoksa normal ödül
            winner.deposit(totalLoot);
        }
        
        loser.withdraw(totalLoot);
        
        // FATİH BUFF'I UYGULA
        if (buffManager != null) {
            buffManager.applyConquerorBuff(winner);
        }
    }
    
    /**
     * Yardımcı metod: UUID ile klan bul
     */
    private Clan getClanById(UUID clanId) {
        // ClanManager'dan al (Main.getInstance() üzerinden)
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getClanManager() != null) {
            return plugin.getClanManager().getClanById(clanId);
        }
        return null;
    }

    /**
     * ✅ YENİ: Beyaz Bayrak - Pes Etme Sistemi (Çoklu savaş desteği)
     * Belirli bir klana karşı pes etme
     */
    public void surrender(Clan surrenderingClan, UUID targetClanId, ClanManager clanManager) {
        if (surrenderingClan == null || targetClanId == null) return;
        
        // Savaşta mılar?
        if (!surrenderingClan.isAtWarWith(targetClanId)) {
            return; // Bu klanla savaşta değil
        }
        
        Clan attacker = getClanById(targetClanId);
        if (attacker == null) return;
        
        // Sadece bu klanla savaşı bitir
        endWar(surrenderingClan, attacker);
        
        // Mesaj
        Bukkit.broadcastMessage("§f§lBEYAZ BAYRAK! §e" + surrenderingClan.getName() + 
            " klanı " + attacker.getName() + " klanına karşı pes etti!");
        
        // Klan yok olmaz, sadece sandıkların yarısı gider
        // Sandık itemlerinin yarısını al
        takeHalfChestItems(surrenderingClan, attacker);
        
        // Kazanan klan ödülü (para) (config'den)
        double lootPercentage = getLootPercentage();
        double loot = surrenderingClan.getBalance() * lootPercentage;
        
        // ✅ YENİ: İttifak kontrolü - Ganimet paylaşımı
        if (allianceManager != null) {
            List<me.mami.stratocraft.model.Alliance> attackerAlliances = allianceManager.getAlliances(attacker.getId());
            
            List<Clan> alliedClans = new ArrayList<>();
            for (me.mami.stratocraft.model.Alliance alliance : attackerAlliances) {
                if (alliance.getType() == me.mami.stratocraft.model.Alliance.Type.OFFENSIVE || 
                    alliance.getType() == me.mami.stratocraft.model.Alliance.Type.FULL) {
                    UUID otherClanId = alliance.getOtherClan(attacker.getId());
                    Clan otherClan = getClanById(otherClanId);
                    if (otherClan != null && otherClan.isAtWarWith(surrenderingClan.getId())) {
                        alliedClans.add(otherClan);
                    }
                }
            }
            
            if (!alliedClans.isEmpty()) {
                int totalRecipients = 1 + alliedClans.size();
                double lootPerClan = loot / totalRecipients;
                
                attacker.deposit(lootPerClan);
                for (Clan alliedClan : alliedClans) {
                    alliedClan.deposit(lootPerClan);
                }
            } else {
                attacker.deposit(loot);
            }
        } else {
            attacker.deposit(loot);
        }
        
        surrenderingClan.withdraw(loot);
        
        // Fatih buff'ı uygula
        if (buffManager != null) {
            buffManager.applyConquerorBuff(attacker);
        }
    }
    
    /**
     * Eski metod (geriye dönük uyumluluk) - İlk savaşta olunan klana pes etme
     */
    @Deprecated
    public void surrender(Clan surrenderingClan, ClanManager clanManager) {
        if (surrenderingClan == null) return;
        
        // İlk savaşta olunan klanı bul
        Set<UUID> wars = activeWars.get(surrenderingClan.getId());
        if (wars == null || wars.isEmpty()) {
            return;
        }
        
        UUID firstWar = wars.iterator().next();
        surrender(surrenderingClan, firstWar, clanManager);
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
        new ChestScannerAndLootTask(chunksToScan, attacker, center, searchRadius).runTaskTimer(plugin, 1L, 5L); // ✅ OPTİMİZE: Her 5 tick'te bir (0.25 saniye) - performans için (1L -> 5L)
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
                        me.mami.stratocraft.Main.getInstance(), 1L, 5L); // ✅ OPTİMİZE: Her 5 tick'te bir (0.25 saniye) - performans için (1L -> 5L)
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

    /**
     * ✅ YENİ: Klan savaşta mı? (herhangi bir klanla)
     */
    public boolean isUnderSiege(Clan clan) {
        if (clan == null) return false;
        Set<UUID> wars = activeWars.get(clan.getId());
        return wars != null && !wars.isEmpty();
    }
    
    /**
     * ✅ YENİ: İki klan birbirleriyle savaşta mı?
     */
    public boolean areAtWar(Clan clan1, Clan clan2) {
        if (clan1 == null || clan2 == null) return false;
        return clan1.isAtWarWith(clan2.getId()) && clan2.isAtWarWith(clan1.getId());
    }
    
    /**
     * ✅ YENİ: Klanın savaşta olduğu tüm klanlar
     */
    public Set<UUID> getWarringClans(UUID clanId) {
        Set<UUID> wars = activeWars.get(clanId);
        return wars != null ? new HashSet<>(wars) : new HashSet<>();
    }
    
    /**
     * Eski metod (geriye dönük uyumluluk)
     */
    @Deprecated
    public Clan getAttacker(Clan defender) {
        if (defender == null) return null;
        Set<UUID> wars = activeWars.get(defender.getId());
        if (wars == null || wars.isEmpty()) {
            return null;
        }
        // İlk savaşta olunan klanı döndür
        UUID firstWar = wars.iterator().next();
        return getClanById(firstWar);
    }
}

