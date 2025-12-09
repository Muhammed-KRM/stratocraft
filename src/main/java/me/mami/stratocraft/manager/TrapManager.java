package me.mami.stratocraft.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import me.mami.stratocraft.Main;

/**
 * Ritüel Tabanlı Tuzak Sistemi
 * 
 * Yapı: Magma Block çerçeve + LODESTONE çekirdek
 * Gizleme: Üstü kapatılmalı
 * Yakıt: Elmas (5), Zümrüt (10), Titanyum (20)
 * Tuzak Tipleri: Cehennem, Şok, Kara Delik, Mayın
 */
public class TrapManager {
    private final Main plugin;
    private final ClanManager clanManager;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    public TrapManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        loadTraps();
        startParticleTask();
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private int getTrapFuelDiamond() {
        return balanceConfig != null ? balanceConfig.getTrapFuelDiamond() : 5;
    }
    
    private int getTrapFuelEmerald() {
        return balanceConfig != null ? balanceConfig.getTrapFuelEmerald() : 10;
    }
    
    private int getTrapFuelTitanium() {
        return balanceConfig != null ? balanceConfig.getTrapFuelTitanium() : 20;
    }
    
    private double getTrapHellTrapDamage() {
        return balanceConfig != null ? balanceConfig.getTrapHellTrapDamage() : 3.0;
    }
    
    private double getTrapShockTrapDamage() {
        return balanceConfig != null ? balanceConfig.getTrapShockTrapDamage() : 2.0;
    }
    
    private double getTrapMineDamage() {
        return balanceConfig != null ? balanceConfig.getTrapMineDamage() : 5.0;
    }
    
    private double getTrapPoisonTrapDamage() {
        return balanceConfig != null ? balanceConfig.getTrapPoisonTrapDamage() : 0.5;
    }
    
    private double getTrapBlackHoleDamage() {
        return balanceConfig != null ? balanceConfig.getTrapBlackHoleDamage() : 10.0;
    }

    private void startParticleTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (TrapData trap : activeTraps.values()) {
                    if (trap.getLocation().getWorld() == null)
                        continue;
                    showTrapActivationParticles(trap);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private final Map<Location, TrapData> activeTraps = new HashMap<>();
    // Henüz aktifleştirilmemiş tuzak çekirdekleri (TrapCoreItem metadata'sı
    // olanlar)
    // Metadata kalıcı olmadığı için dosyaya kaydedilmeli
    private final Map<Location, UUID> inactiveTrapCores = new HashMap<>(); // Location -> Owner UUID
    private final Map<UUID, me.mami.stratocraft.model.Clan> clanCache = new HashMap<>(); // Performans optimizasyonu
    // Performans optimizasyonu: Üstteki blok -> Tuzak çekirdeği mapping (O(1) lookup)
    private final Map<Location, Location> coverBlockToTrapCore = new HashMap<>(); // Cover block location -> Trap core location
    private File trapsFile;
    private FileConfiguration trapsConfig;

    public enum TrapType {
        HELL_TRAP, // Cehennem Tuzağı (Magma Cream) - 3x3 lava
        SHOCK_TRAP, // Şok Tuzağı (Lightning Core) - Yıldırım
        BLACK_HOLE, // Kara Delik (Ender Pearl) - Körlük + Yavaşlık
        MINE, // Mayın (TNT) - Yüksek hasarlı patlama
        POISON_TRAP // Zehir Tuzağı (Spider Eye) - Zehir efekti
    }

    public static class TrapData {
        private final UUID ownerId;
        private final UUID ownerClanId;
        private final TrapType type;
        private int fuel; // Kalan patlama hakkı
        private final Location coreLocation;
        private final List<Location> frameBlocks; // Magma Block çerçevesi
        private boolean isCovered; // Üstü kapatılmış mı?

        public TrapData(UUID ownerId, UUID ownerClanId, TrapType type, int fuel, Location coreLocation) {
            this.ownerId = ownerId;
            this.ownerClanId = ownerClanId;
            this.type = type;
            this.fuel = fuel;
            this.coreLocation = coreLocation;
            this.frameBlocks = new ArrayList<>();
            this.isCovered = false;
        }

        public UUID getOwnerId() {
            return ownerId;
        }

        public UUID getOwnerClanId() {
            return ownerClanId;
        }

        public TrapType getType() {
            return type;
        }

        public int getFuel() {
            return fuel;
        }

        public void consumeFuel() {
            if (fuel > 0)
                fuel--;
        }

        public Location getLocation() {
            return coreLocation;
        }

        public boolean isCovered() {
            return isCovered;
        }

        public void setCovered(boolean covered) {
            this.isCovered = covered;
        }

        public void addFuel(int amount) {
            fuel += amount;
        }

        public Location getCoreLocation() {
            return coreLocation;
        }

        public List<Location> getFrameBlocks() {
            return frameBlocks;
        }
    }

    /**
     * Tuzak aktifleştirme partikülleri (sadece sahip ve klan üyelerine görünür)
     */
    private void showTrapActivationParticles(TrapData trap) {
        Location coreLoc = trap.getCoreLocation();

        // Tüm çerçeve bloklarının üstünde kırmızı partikül göster
        for (Location frameLoc : trap.getFrameBlocks()) {
            Location particleLoc = frameLoc.clone().add(0.5, 1.5, 0.5);

            // Sahibe göster
            Player owner = plugin.getServer().getPlayer(trap.getOwnerId());
            if (owner != null && owner.isOnline()) {
                owner.spawnParticle(org.bukkit.Particle.REDSTONE, particleLoc, 5,
                        0.3, 0.3, 0.3, 0,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            }

            // Klan üyelerine göster
            if (trap.getOwnerClanId() != null) {
                me.mami.stratocraft.model.Clan clan = getClanById(trap.getOwnerClanId());
                if (clan != null) {
                    for (UUID memberId : clan.getMembers().keySet()) {
                        Player member = plugin.getServer().getPlayer(memberId);
                        if (member != null && member.isOnline() &&
                                !member.getUniqueId().equals(trap.getOwnerId())) {
                            member.spawnParticle(org.bukkit.Particle.REDSTONE, particleLoc, 5,
                                    0.3, 0.3, 0.3, 0,
                                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                        }
                    }
                }
            }
        }

        // Çekirdek bloğunun üstünde de partikül göster
        Location coreParticleLoc = coreLoc.clone().add(0.5, 1.5, 0.5);

        Player owner = plugin.getServer().getPlayer(trap.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.spawnParticle(org.bukkit.Particle.REDSTONE, coreParticleLoc, 10,
                    0.5, 0.5, 0.5, 0,
                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        }

        if (trap.getOwnerClanId() != null) {
            me.mami.stratocraft.model.Clan clan = getClanById(trap.getOwnerClanId());
            if (clan != null) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline() &&
                            !member.getUniqueId().equals(trap.getOwnerId())) {
                        member.spawnParticle(org.bukkit.Particle.REDSTONE, coreParticleLoc, 10,
                                0.5, 0.5, 0.5, 0,
                                new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
                    }
                }
            }
        }
    }

    /**
     * Sürekli partikül efekti (Task tarafından çağrılır)
     */
    public void showTrapActivationParticles(Location loc, TrapType type) {
        org.bukkit.Particle particle = org.bukkit.Particle.SPELL_WITCH;

        switch (type) {
            case HELL_TRAP:
                particle = org.bukkit.Particle.FLAME;
                break;
            case SHOCK_TRAP:
                particle = org.bukkit.Particle.ELECTRIC_SPARK;
                break;
            case BLACK_HOLE:
                particle = org.bukkit.Particle.PORTAL;
                break;
            case MINE:
                particle = org.bukkit.Particle.SMOKE_LARGE;
                break;
            case POISON_TRAP:
                particle = org.bukkit.Particle.VILLAGER_HAPPY;
                break;
        }

        // Çerçeve boyunca partikül oluştur (3x3 varsayalım görsel için)
        if (loc.getWorld() != null) {
            loc.getWorld().spawnParticle(particle, loc.clone().add(0.5, 1, 0.5), 5, 0.5, 0.5, 0.5, 0.05);
        }
    }

    /**
     * Clan ID'ye göre Clan bul (Performans optimizasyonu - Cache kullan)
     */
    private me.mami.stratocraft.model.Clan getClanById(UUID clanId) {
        if (clanId == null)
            return null;

        // Cache'den kontrol et
        if (clanCache.containsKey(clanId)) {
            me.mami.stratocraft.model.Clan cached = clanCache.get(clanId);
            if (cached != null) {
                // Cache'deki klan hala geçerli mi kontrol et
                // Eğer klan üyelerinden biri hala bu klana aitse, cache geçerli
                if (!cached.getMembers().isEmpty()) {
                    UUID firstMember = cached.getMembers().keySet().iterator().next();
                    me.mami.stratocraft.model.Clan current = clanManager.getClanByPlayer(firstMember);
                    if (current != null && current.getId().equals(clanId)) {
                        return cached;
                    }
                }
                // Geçersiz cache, temizle
                clanCache.remove(clanId);
            }
        }

        // Cache'de yoksa, tüm klanlardan bul (sadece ilk sefer)
        for (me.mami.stratocraft.model.Clan clan : clanManager.getAllClans()) {
            if (clan.getId().equals(clanId)) {
                // Cache'e ekle
                clanCache.put(clanId, clan);
                return clan;
            }
        }
        return null;
    }

    /**
     * Tuzak üstünün kapatılıp kapatılmadığını kontrol et
     */
    public void checkTrapCoverage(Location coreLocation) {
        TrapData trap = activeTraps.get(coreLocation);
        if (trap == null)
            return;

        Block coreBlock = coreLocation.getBlock();
        if (coreBlock.getType() != Material.LODESTONE)
            return;

        // Üstteki blok kontrolü
        Block above = coreBlock.getRelative(0, 1, 0);
        boolean covered = above.getType() != Material.AIR &&
                above.getType() != Material.CAVE_AIR &&
                above.getType() != Material.VOID_AIR;

        // Çerçeve bloklarının üstünü de kontrol et (tüm çerçeve kapatılmalı)
        for (Location frameLoc : trap.getFrameBlocks()) {
            Block frameBlock = frameLoc.getBlock();
            if (frameBlock.getType() != Material.MAGMA_BLOCK)
                continue; // Çerçeve bozulmuş olabilir

            Block frameAbove = frameBlock.getRelative(0, 1, 0);
            if (frameAbove.getType() == Material.AIR ||
                    frameAbove.getType() == Material.CAVE_AIR ||
                    frameAbove.getType() == Material.VOID_AIR) {
                covered = false;
                break;
            }
        }

        trap.setCovered(covered);
        
        // Performans optimizasyonu: Cover block mapping'ini güncelle
        if (covered) {
            updateCoverBlockMapping(coreLocation, trap);
        } else {
            removeCoverBlockMapping(coreLocation, trap);
        }
    }

    /**
     * Tuzak tetikleme (PlayerMoveEvent'ten çağrılır)
     */
    public void triggerTrap(Location triggerLocation, Player victim) {
        // Null kontrolleri
        if (triggerLocation == null || victim == null) {
            return;
        }

        Block triggerBlock = triggerLocation.getBlock();
        TrapData trap = null;
        Location trapCore = null;

        // 1. Trigger bloğunun altında tuzak var mı? (1 blok altında - üstteki bloklara basıldığında)
        Block below = triggerBlock.getRelative(0, -1, 0);
        if (below.hasMetadata("TrapCore")) {
            trapCore = below.getLocation();
            trap = activeTraps.get(trapCore);
        }
        // 2. Trigger bloğunun altında tuzak var mı? (2 blok altında)
        else {
            Block below2 = below.getRelative(0, -1, 0);
            if (below2.hasMetadata("TrapCore")) {
                trapCore = below2.getLocation();
                trap = activeTraps.get(trapCore);
            }
        }

        // 3. Eğer trigger bloğu Magma Block ise, bu bir tuzak çerçevesi olabilir
        // Tüm aktif tuzakları kontrol et ve çerçeve bloklarını ara
        if (trap == null && triggerBlock.getType() == Material.MAGMA_BLOCK) {
            for (Map.Entry<Location, TrapData> entry : activeTraps.entrySet()) {
                TrapData checkTrap = entry.getValue();
                // Bu bloğun tuzak çerçevesinde olup olmadığını kontrol et
                for (Location frameLoc : checkTrap.getFrameBlocks()) {
                    if (frameLoc.getBlockX() == triggerBlock.getX() &&
                        frameLoc.getBlockY() == triggerBlock.getY() &&
                        frameLoc.getBlockZ() == triggerBlock.getZ()) {
                        trap = checkTrap;
                        trapCore = entry.getKey();
                        break;
                    }
                }
                if (trap != null) break;
            }
        }
        
        // 4. Eğer trigger bloğu tuzağın üstündeki kapak bloğu ise (üstteki bloklara basıldığında)
        // PERFORMANS OPTİMİZASYONU: O(1) lookup kullan (tüm tuzakları döngüye alma)
        if (trap == null) {
            Location trapCoreFromMapping = coverBlockToTrapCore.get(triggerLocation);
            if (trapCoreFromMapping != null) {
                trap = activeTraps.get(trapCoreFromMapping);
                if (trap != null) {
                    trapCore = trapCoreFromMapping;
                }
            }
        }

        // Tuzak bulunamadı
        if (trap == null || trapCore == null) {
            return;
        }

        // GİZLEME KONTROLÜ: Tuzağın üstü kapalı olmalı (gizlenmiş olmalı)
        Block trapBlock = trapCore.getBlock();
        Block coverBlock = trapBlock.getRelative(0, 1, 0); // Tuzağın üstündeki blok

        // Eğer tuzağın üstü açıksa (Hava ise) çalışma - tuzak gizlenmemiş
        if (coverBlock.getType() == Material.AIR ||
                coverBlock.getType() == Material.CAVE_AIR ||
                coverBlock.getType() == Material.VOID_AIR) {
            return; // Tuzak gizlenmemiş, çalışma
        }

        // Çerçeve bloklarının üstünü de kontrol et
        boolean allCovered = true;
        for (Location frameLoc : trap.getFrameBlocks()) {
            Block frameBlock = frameLoc.getBlock();
            Block frameAbove = frameBlock.getRelative(0, 1, 0);
            if (frameAbove.getType() == Material.AIR ||
                    frameAbove.getType() == Material.CAVE_AIR ||
                    frameAbove.getType() == Material.VOID_AIR) {
                allCovered = false;
                break;
            }
        }

        if (!allCovered) {
            return; // Tuzak tamamen gizlenmemiş, çalışma
        }

        // Owner kontrolü - Tuzak sahibi korunur
        if (victim != null && victim.getUniqueId().equals(trap.getOwnerId())) {
            return; // Tuzak sahibi, tuzak tetiklenmez
        }
        
        // Klan kontrolü - Dostlar korunur
        if (trap.getOwnerClanId() != null && victim != null) {
            if (clanManager.getClanByPlayer(victim.getUniqueId()) != null) {
                UUID victimClanId = clanManager.getClanByPlayer(victim.getUniqueId()).getId();
                if (victimClanId.equals(trap.getOwnerClanId())) {
                    return; // Dost, tuzak tetiklenmez
                }
            }
        }

        // Yakıt kontrolü
        if (trap.getFuel() <= 0) {
            removeTrap(trapCore);
            return;
        }

        // Tuzak tetiklenmeden önce CLICK sesi (0.5 saniye önce korku efekti)
        victim.playSound(triggerLocation, org.bukkit.Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);

        // Tuzak tipine göre etki
        executeTrap(trap, triggerLocation, victim);

        // Yakıt tüket
        trap.consumeFuel();
        if (trap.getFuel() <= 0) {
            removeTrap(trapCore);
        } else {
            saveTraps();
        }
    }

    /**
     * Tuzak etkisini uygula
     */
    private void executeTrap(TrapData trap, Location triggerLoc, Player victim) {
        switch (trap.getType()) {
            case HELL_TRAP:
                // 3x3 alanı lava yap
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = triggerLoc.clone().add(x, 0, z).getBlock();
                        if (block.getType() == Material.AIR || block.getType().isSolid()) {
                            block.setType(Material.LAVA);
                        }
                    }
                }
                triggerLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, triggerLoc.clone().add(0.5, 1, 0.5), 50,
                        1, 1, 1, 0.1);
                if (victim != null) {
                    victim.sendMessage("§c§lCEHENNEM TUZAĞINA YAKALANDIN!");
                }
                break;

            case SHOCK_TRAP:
                // Yıldırım çarptır
                triggerLoc.getWorld().strikeLightning(triggerLoc);
                if (victim != null) {
                    victim.sendMessage("§e§lŞOK TUZAĞINA YAKALANDIN!");
                }
                break;

            case BLACK_HOLE:
                // Körlük ve Yavaşlık ver
                triggerLoc.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, triggerLoc.clone().add(0.5, 1, 0.5),
                        100, 1, 1, 1, 0.5);
                triggerLoc.getWorld().spawnParticle(org.bukkit.Particle.SQUID_INK, triggerLoc.clone().add(0.5, 1, 0.5),
                        50, 1, 1, 1, 0.1);
                if (victim != null) {
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 2, false, false));
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 100, 3, false, false));
                    victim.sendMessage("§5§lKARA DELİK TUZAĞINA YAKALANDIN!");
                }
                break;

            case MINE:
                // Yüksek hasarlı patlama (blok kırmaz)
                triggerLoc.getWorld().createExplosion(triggerLoc, 5.0f, false, false);
                if (victim != null) {
                    victim.sendMessage("§c§lMAYINA BASDIN!");
                }
                break;

            case POISON_TRAP:
                // Zehir efekti (10 blok yarıçap)
                triggerLoc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, triggerLoc.clone().add(0.5, 1, 0.5),
                        100, 2, 2, 2, 0);
                if (victim != null) {
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.POISON, 100, 1, false, false));
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0, false, false));
                    victim.sendMessage("§2§lZEHİR TUZAĞINA YAKALANDIN!");

                    // Etrafındaki diğer oyunculara da zehir ver
                    for (org.bukkit.entity.Entity nearby : triggerLoc.getWorld()
                            .getNearbyEntities(triggerLoc, 10, 10, 10)) {
                        if (nearby instanceof Player && nearby != victim) {
                            Player nearbyPlayer = (Player) nearby;
                            // Klan kontrolü
                            if (trap.getOwnerClanId() != null) {
                                if (clanManager.getClanByPlayer(nearbyPlayer.getUniqueId()) != null) {
                                    UUID nearbyClanId = clanManager.getClanByPlayer(nearbyPlayer.getUniqueId()).getId();
                                    if (nearbyClanId.equals(trap.getOwnerClanId())) {
                                        continue; // Dost, zehir verilmez
                                    }
                                }
                            }
                            nearbyPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.POISON, 80, 0, false, false));
                        }
                    }
                }
                break;
        }
    }

    /**
     * Tuzak kaldır
     */
    public void removeTrap(Location coreLocation) {
        TrapData trap = activeTraps.remove(coreLocation);
        if (trap != null) {
            Block coreBlock = coreLocation.getBlock();
            coreBlock.removeMetadata("TrapCore", plugin);
            coreBlock.removeMetadata("TrapOwner", plugin);
            
            // Performans optimizasyonu: Cover block mapping'ini temizle
            removeCoverBlockMapping(coreLocation, trap);
            
            saveTraps();
        }
    }
    
    /**
     * Üstteki blok -> tuzak çekirdeği mapping'ini güncelle (performans optimizasyonu)
     */
    private void updateCoverBlockMapping(Location coreLoc, TrapData trap) {
        if (coreLoc == null || trap == null || coreLoc.getWorld() == null) {
            return; // Null kontrolü
        }
        
        // Tuzağın üstündeki blok
        Block coreBlock = coreLoc.getBlock();
        if (coreBlock == null) return;
        
        Block coreAbove = coreBlock.getRelative(0, 1, 0);
        if (coreAbove != null && coreAbove.getType() != Material.AIR && 
            coreAbove.getType() != Material.CAVE_AIR && 
            coreAbove.getType() != Material.VOID_AIR) {
            coverBlockToTrapCore.put(coreAbove.getLocation(), coreLoc);
        }
        
        // Çerçeve bloklarının üstündeki bloklar
        if (trap.getFrameBlocks() != null) {
            for (Location frameLoc : trap.getFrameBlocks()) {
                if (frameLoc == null || frameLoc.getWorld() == null) continue;
                
                Block frameBlock = frameLoc.getBlock();
                if (frameBlock == null) continue;
                
                Block frameAbove = frameBlock.getRelative(0, 1, 0);
                if (frameAbove != null && frameAbove.getType() != Material.AIR && 
                    frameAbove.getType() != Material.CAVE_AIR && 
                    frameAbove.getType() != Material.VOID_AIR) {
                    coverBlockToTrapCore.put(frameAbove.getLocation(), coreLoc);
                }
            }
        }
    }
    
    /**
     * Üstteki blok -> tuzak çekirdeği mapping'ini temizle
     */
    private void removeCoverBlockMapping(Location coreLoc, TrapData trap) {
        if (coreLoc == null || trap == null || coreLoc.getWorld() == null) {
            return; // Null kontrolü
        }
        
        // Tuzağın üstündeki blok
        Block coreBlock = coreLoc.getBlock();
        if (coreBlock != null) {
            Block coreAbove = coreBlock.getRelative(0, 1, 0);
            if (coreAbove != null) {
                coverBlockToTrapCore.remove(coreAbove.getLocation());
            }
        }
        
        // Çerçeve bloklarının üstündeki bloklar
        if (trap.getFrameBlocks() != null) {
            for (Location frameLoc : trap.getFrameBlocks()) {
                if (frameLoc == null || frameLoc.getWorld() == null) continue;
                
                Block frameBlock = frameLoc.getBlock();
                if (frameBlock != null) {
                    Block frameAbove = frameBlock.getRelative(0, 1, 0);
                    if (frameAbove != null) {
                        coverBlockToTrapCore.remove(frameAbove.getLocation());
                    }
                }
            }
        }
    }

    /**
     * Tuzakları kaydet (public - Main.java'dan çağrılabilir)
     */
    public void saveTraps() {
        if (trapsConfig == null)
            return;

        trapsConfig.set("traps", null);
        trapsConfig.set("inactive_cores", null);

        // Aktif tuzakları kaydet
        for (Map.Entry<Location, TrapData> entry : activeTraps.entrySet()) {
            Location loc = entry.getKey();
            TrapData trap = entry.getValue();

            String path = "traps." + loc.getWorld().getName() + "." +
                    loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

            trapsConfig.set(path + ".owner", trap.getOwnerId().toString());
            trapsConfig.set(path + ".clan", trap.getOwnerClanId() != null ? trap.getOwnerClanId().toString() : null);
            trapsConfig.set(path + ".type", trap.getType().name());
            trapsConfig.set(path + ".fuel", trap.getFuel());
        }

        // Henüz aktifleştirilmemiş tuzak çekirdeklerini kaydet
        for (Map.Entry<Location, UUID> entry : inactiveTrapCores.entrySet()) {
            Location loc = entry.getKey();
            UUID ownerId = entry.getValue();

            String path = "inactive_cores." + loc.getWorld().getName() + "." +
                    loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

            trapsConfig.set(path + ".owner", ownerId.toString());
        }

        try {
            trapsConfig.save(trapsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Tuzaklar kaydedilemedi: " + e.getMessage());
        }
    }

    /**
     * Tuzakları yükle
     */
    private void loadTraps() {
        trapsFile = new File(plugin.getDataFolder(), "traps.yml");
        if (!trapsFile.exists()) {
            try {
                trapsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("traps.yml oluşturulamadı: " + e.getMessage());
            }
        }

        trapsConfig = YamlConfiguration.loadConfiguration(trapsFile);

        if (trapsConfig.getConfigurationSection("traps") == null)
            return;

        for (String worldName : trapsConfig.getConfigurationSection("traps").getKeys(false)) {
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null)
                continue;

            for (String locStr : trapsConfig.getConfigurationSection("traps." + worldName).getKeys(false)) {
                String[] coords = locStr.split(",");
                if (coords.length != 3)
                    continue;

                try {
                    Location loc = new Location(world,
                            Integer.parseInt(coords[0]),
                            Integer.parseInt(coords[1]),
                            Integer.parseInt(coords[2]));

                    UUID ownerId = UUID
                            .fromString(trapsConfig.getString("traps." + worldName + "." + locStr + ".owner"));
                    String clanStr = trapsConfig.getString("traps." + worldName + "." + locStr + ".clan");
                    UUID clanId = clanStr != null ? UUID.fromString(clanStr) : null;
                    TrapType type = TrapType
                            .valueOf(trapsConfig.getString("traps." + worldName + "." + locStr + ".type"));
                    int fuel = trapsConfig.getInt("traps." + worldName + "." + locStr + ".fuel");

                    TrapData trap = new TrapData(ownerId, clanId, type, fuel, loc);
                    activeTraps.put(loc, trap);

                    // Metadata'yı geri yükle
                    Block block = loc.getBlock();
                    if (block.getType() == Material.LODESTONE) {
                        block.setMetadata("TrapCore", new FixedMetadataValue(plugin, true));
                        block.setMetadata("TrapOwner", new FixedMetadataValue(plugin, ownerId.toString()));
                    }
                    
                    // PERFORMANS OPTİMİZASYONU: Cover block mapping'ini yeniden oluştur (sunucu restart sonrası)
                    // Çerçeve bloklarını tespit et (dosyadan okunmuyor, yeniden tespit edilmeli)
                    List<Location> frameBlocks = new ArrayList<>();
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && z == 0) continue; // Çekirdek blok
                            Location frameLoc = loc.clone().add(x, 0, z);
                            Block frameBlock = frameLoc.getBlock();
                            if (frameBlock.getType() == Material.MAGMA_BLOCK) {
                                frameBlocks.add(frameLoc);
                            }
                        }
                    }
                    trap.getFrameBlocks().addAll(frameBlocks);
                    
                    // Mapping'i oluşturmak için checkTrapCoverage çağrılır
                    checkTrapCoverage(loc);
                } catch (Exception e) {
                    plugin.getLogger().warning("Tuzak yüklenemedi: " + locStr + " - " + e.getMessage());
                }
            }
        }

        // Henüz aktifleştirilmemiş tuzak çekirdeklerini yükle
        if (trapsConfig.getConfigurationSection("inactive_cores") != null) {
            for (String worldName : trapsConfig.getConfigurationSection("inactive_cores").getKeys(false)) {
                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world == null)
                    continue;

                for (String locStr : trapsConfig.getConfigurationSection("inactive_cores." + worldName)
                        .getKeys(false)) {
                    String[] coords = locStr.split(",");
                    if (coords.length != 3)
                        continue;

                    try {
                        Location loc = new Location(world,
                                Integer.parseInt(coords[0]),
                                Integer.parseInt(coords[1]),
                                Integer.parseInt(coords[2]));

                        UUID ownerId = UUID.fromString(
                                trapsConfig.getString("inactive_cores." + worldName + "." + locStr + ".owner"));

                        inactiveTrapCores.put(loc, ownerId);

                        // Metadata'yı geri yükle (sunucu restart sonrası)
                        Block block = loc.getBlock();
                        if (block.getType() == Material.LODESTONE) {
                            block.setMetadata("TrapCoreItem", new FixedMetadataValue(plugin, true));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning(
                                "Aktifleştirilmemiş tuzak çekirdeği yüklenemedi: " + locStr + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    public boolean isInactiveTrapCore(Location loc) {
        return inactiveTrapCores.containsKey(loc);
    }

    /**
     * Henüz aktifleştirilmemiş tuzak çekirdeğini kaydet
     */
    public void registerInactiveTrapCore(Location loc, UUID ownerId) {
        inactiveTrapCores.put(loc, ownerId);
        saveTraps();
    }

    /**
     * Tuzak yapısını kontrol et (LODESTONE çekirdeği etrafında MAGMA_BLOCK
     * çerçevesi)
     */
    public boolean isTrapStructure(Block core) {
        if (core.getType() != Material.LODESTONE)
            return false;

        Location coreLoc = core.getLocation();
        int magmaCount = 0;

        // 3x3 çerçeve kontrolü (ortadaki çekirdek hariç)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue; // Çekirdek blok

                Block frameBlock = coreLoc.clone().add(x, 0, z).getBlock();
                if (frameBlock.getType() == Material.MAGMA_BLOCK) {
                    magmaCount++;
                }
            }
        }

        // En az 6 magma bloğu olmalı (3x3'ün en az %75'i)
        return magmaCount >= 6;
    }

    /**
     * Tuzak oluştur (yakıt ve tip ile)
     */
    public boolean createTrap(Player player, Block coreBlock, TrapType type, Material fuelMaterial) {
        Location coreLoc = coreBlock.getLocation();

        // Çerçeve bloklarını tespit et
        List<Location> frameBlocks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;

                Location frameLoc = coreLoc.clone().add(x, 0, z);
                Block frameBlock = frameLoc.getBlock();
                if (frameBlock.getType() == Material.MAGMA_BLOCK) {
                    frameBlocks.add(frameLoc);
                }
            }
        }

        // Yakıt miktarını hesapla (config'den)
        int fuel = 0;
        if (fuelMaterial == Material.DIAMOND)
            fuel = getTrapFuelDiamond();
        else if (fuelMaterial == Material.EMERALD)
            fuel = getTrapFuelEmerald();
        // ItemManager.isCustomItem kontrolü listener'da yapılıyor, buraya geldiğinde
        // Titanyum ise config'den al
        else
            fuel = getTrapFuelTitanium(); // Titanyum veya diğer

        // Klan ID'sini al
        UUID clanId = null;
        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            clanId = clanManager.getClanByPlayer(player.getUniqueId()).getId();
        }

        // TrapData oluştur
        TrapData trap = new TrapData(player.getUniqueId(), clanId, type, fuel, coreLoc);
        trap.getFrameBlocks().addAll(frameBlocks);

        // Aktif tuzaklar listesine ekle
        activeTraps.put(coreLoc, trap);

        // Metadata ekle
        coreBlock.setMetadata("TrapCore", new FixedMetadataValue(plugin, true));
        coreBlock.setMetadata("TrapOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

        // Metadata'yı kaldır (artık aktif tuzak)
        coreBlock.removeMetadata("TrapCoreItem", plugin);
        inactiveTrapCores.remove(coreLoc);
        
        // Performans optimizasyonu: Üstteki blok -> tuzak çekirdeği mapping'i oluştur
        updateCoverBlockMapping(coreLoc, trap);

        // Kaydet
        saveTraps();

        player.sendMessage("§a§lTuzak başarıyla kuruldu!");
        player.sendMessage("§7Tip: " + type.name() + ", Yakıt: " + fuel);

        return true;
    }

    public Map<Location, TrapData> getActiveTraps() {
        return new HashMap<>(activeTraps);
    }
}
