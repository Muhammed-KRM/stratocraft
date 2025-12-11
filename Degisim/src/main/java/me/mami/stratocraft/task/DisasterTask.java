package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.handler.DisasterHandlerRegistry;
import me.mami.stratocraft.manager.DisasterConfigManager;
import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.manager.DisasterPhaseConfig;
import me.mami.stratocraft.manager.DisasterPhaseManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.DisasterBehavior;
import me.mami.stratocraft.util.DisasterEntityAI;
import me.mami.stratocraft.util.DisasterUtils;
import me.mami.stratocraft.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class DisasterTask extends BukkitRunnable {
    private final DisasterManager disasterManager;
    private final TerritoryManager territoryManager;
    private final DisasterConfigManager configManager;
    private final DisasterHandlerRegistry handlerRegistry;
    private DisasterPhaseManager phaseManager;
    
    // Chunk yönetimi: Force-loaded chunk'ları takip et
    private final java.util.Map<String, org.bukkit.Chunk> forceLoadedChunks = new java.util.HashMap<>();
    
    // Oyuncu saldırısı için zaman takibi (her felaket için ayrı)
    private final java.util.Map<UUID, Long> lastAttackTime = new java.util.HashMap<>();
    
    // Klan kristali cache (performans için)
    private Location cachedNearestCrystal = null;
    private long lastCrystalCacheUpdate = 0;
    
    // Kristal yok edildikten sonra oyuncularla savaşma durumu
    private boolean crystalDestroyed = false;
    private long crystalDestroyedTime = 0;
    private static final long POST_CRYSTAL_FIGHT_DURATION = 60000L; // 1 dakika oyuncularla savaş

    public DisasterTask(DisasterManager dm, TerritoryManager tm) { 
        this.disasterManager = dm; 
        this.territoryManager = tm;
        // ConfigManager'dan DisasterConfigManager al
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getConfigManager() != null) {
            this.configManager = plugin.getConfigManager().getDisasterConfigManager();
        } else {
            this.configManager = null;
        }
        // Handler Registry oluştur
        this.handlerRegistry = new DisasterHandlerRegistry(territoryManager);
        
        // Faz Manager oluştur (plugin zaten yukarıda alındı)
        if (plugin != null && plugin.getConfigManager() != null) {
            DisasterPhaseConfig phaseConfig = plugin.getConfigManager().getDisasterPhaseConfig();
            if (phaseConfig != null) {
                this.phaseManager = new DisasterPhaseManager(phaseConfig, plugin);
            }
        }
    }
    
    /**
     * Config'den değer al (varsayılan değerle)
     */
    private DisasterConfig getConfig(Disaster disaster) {
        if (configManager != null && disaster != null) {
            return configManager.getConfig(disaster.getType(), disaster.getLevel());
        }
        return new DisasterConfig(); // Varsayılan config
    }

    @Override
    public void run() {
        Disaster disaster = disasterManager.getActiveDisaster();
        if (disaster == null) {
            cleanupForceLoadedChunks();
            return;
        }
        
        // Süre doldu mu kontrol et (tüm felaketler için, öncelikli)
        if (disaster.isExpired()) {
            disaster.kill();
            disasterManager.setActiveDisaster(null);
            cleanupForceLoadedChunks();
            String disasterName = disasterManager.getDisasterDisplayName(disaster.getType());
            Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GREEN + "" + org.bukkit.ChatColor.BOLD + 
                "Felaket süresi doldu: " + disasterName);
            return;
        }
        
        // Felaket ölü mü kontrol et (canlı felaketler için)
        if (disaster.isDead()) {
            cleanupForceLoadedChunks();
            return;
        }

        Entity entity = disaster.getEntity();
        
        // Canlı felaketler için entity kontrolü
        if (disaster.getCategory() == Disaster.Category.CREATURE) {
            if (entity == null || entity.isDead()) {
                // Plan'a göre: Felaket yok edilince ödül
                disasterManager.dropRewards(disaster);
                disaster.kill();
                disasterManager.setActiveDisaster(null);
                cleanupForceLoadedChunks(); // Chunk'ları temizle
                return;
            }
            handleCreatureDisaster(disaster, entity);
        } else if (disaster.getCategory() == Disaster.Category.NATURAL) {
            // Doğa olayları
            handleNaturalDisaster(disaster);
        } else if (disaster.getCategory() == Disaster.Category.MINI) {
            // Mini felaketler
            handleMiniDisaster(disaster);
        }
    }
    
    /**
     * Force-loaded chunk'ları temizle (Memory leak önleme)
     */
    private void cleanupForceLoadedChunks() {
        for (org.bukkit.Chunk chunk : forceLoadedChunks.values()) {
            if (chunk != null && chunk.isLoaded()) {
                chunk.setForceLoaded(false);
            }
        }
        forceLoadedChunks.clear();
    }
    
    /**
     * Canlı felaketleri işle
     */
    private void handleCreatureDisaster(Disaster disaster, Entity entity) {
        Location current = entity.getLocation();
        DisasterConfig config = getConfig(disaster);
        
        // FAZ SİSTEMİ: Faz kontrolü ve geçişi
        if (phaseManager != null) {
            phaseManager.checkAndUpdatePhase(disaster);
        }
        
        // Hedef kristali güncelle (config'den aralık)
        updateTargetCrystal(disaster, current, config);
        
        // Kristal kontrolü - eğer kristale yakınsa yok et (öncelikli)
        if (!crystalDestroyed) {
            checkAndDestroyCrystal(disaster, entity, current, config);
        }
        
        // Plan'a göre: Kristal yok edildikten sonra oyuncularla savaşır
        // Kristal yok edildikten sonra 1 dakika boyunca oyuncularla savaş
        if (crystalDestroyed) {
            long timeSinceCrystalDestroyed = System.currentTimeMillis() - crystalDestroyedTime;
            if (timeSinceCrystalDestroyed < POST_CRYSTAL_FIGHT_DURATION) {
                // Oyuncularla agresif savaş (daha sık saldırı)
                long attackInterval = config.getAttackInterval();
                if (phaseManager != null) {
                    attackInterval = phaseManager.getAttackInterval(disaster);
                }
                attackNearbyPlayersIfNeeded(disaster, entity, current, config, true, attackInterval);
            } else {
                // 1 dakika sonra yeni kristal bul
                crystalDestroyed = false;
                crystalDestroyedTime = 0;
                disaster.setTargetCrystal(null);
                cachedNearestCrystal = null;
                lastCrystalCacheUpdate = 0;
            }
        } else {
            // FAZ SİSTEMİ: Faz'a göre saldırı aralığı ve oyuncu saldırısı kontrolü
            if (phaseManager != null && phaseManager.shouldAttackPlayers(disaster)) {
                // Faz'a göre saldırı aralığı al
                long attackInterval = phaseManager.getAttackInterval(disaster);
                attackNearbyPlayersIfNeeded(disaster, entity, current, config, false, attackInterval);
            } else {
                // Normal durum: Config'den saldırı aralığı
                attackNearbyPlayersIfNeeded(disaster, entity, current, config, false, config.getAttackInterval());
            }
        }
        
        // Chunk yüklü mü kontrol et, değilse yükle (entity hareket edebilsin diye)
        if (current.getWorld() != null) {
            int chunkX = current.getBlockX() >> 4;
            int chunkZ = current.getBlockZ() >> 4;
            String chunkKey = chunkX + ";" + chunkZ;
            
            // Mevcut chunk'ı force load et
            org.bukkit.Chunk currentChunk = current.getWorld().getChunkAt(chunkX, chunkZ);
            if (!currentChunk.isLoaded()) {
                currentChunk.load(true);
            }
            currentChunk.setForceLoaded(true);
            forceLoadedChunks.put(chunkKey, currentChunk);
            
            // Eski chunk'ları unload et
            java.util.Iterator<java.util.Map.Entry<String, org.bukkit.Chunk>> iterator = 
                forceLoadedChunks.entrySet().iterator();
            while (iterator.hasNext()) {
                java.util.Map.Entry<String, org.bukkit.Chunk> entry = iterator.next();
                if (!entry.getKey().equals(chunkKey)) {
                    // Bu chunk artık kullanılmıyor, unload et
                    entry.getValue().setForceLoaded(false);
                    iterator.remove();
                }
            }
        }
        
        // Handler sistemi kullan - hedef kristale hareket etmesi için
        DisasterHandler handler = handlerRegistry.getHandler(disaster.getType());
        
        // Hedef kristal ayarlandıysa, disaster'a bildir
        Location targetCrystal = disaster.getTargetCrystal();
        if (targetCrystal != null) {
            disaster.setTarget(targetCrystal);
        }
        
        // Grup felaketler için özel işleme
        if (disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MEDIUM_GROUP || 
            disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MINI_SWARM) {
            java.util.List<Entity> groupEntities = disaster.getGroupEntities();
            if (groupEntities != null && !groupEntities.isEmpty() && handler != null) {
                handler.handleGroup(disaster, groupEntities, config);
            }
            return;
        }
        
        // Tek boss felaketler için handler kullan
        if (handler != null) {
            handler.handle(disaster, entity, config);
        } else {
            // Handler yoksa, manuel hareket
            Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
            if (target != null) {
                me.mami.stratocraft.util.DisasterBehavior.moveToTarget(entity, target, config);
            }
        }
        
        // Özel yetenekleri kullan (faz bazlı)
        me.mami.stratocraft.model.DisasterPhase currentPhase = disaster.getCurrentPhase();
        if (currentPhase != null) {
            handler.useSpecialAbilities(disaster, entity, config, currentPhase);
        }
        
        // Çevre değişimi (bazı felaketler için)
        handler.changeEnvironment(disaster, entity, config);
    }
    
    /**
     * Klan yapılarını yok et
     */
    private void destroyClanStructures(Clan clan, Location disasterLoc, double damageMultiplier) {
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(disasterLoc) <= 20) {
                // Yapıyı yok et
                structure.getLocation().getBlock().setType(Material.AIR);
                EffectUtil.playDisasterEffect(structure.getLocation());
            }
        }
    }
    
    /**
     * Hedef kristali güncelle (cache ile, config'den aralık)
     */
    private void updateTargetCrystal(Disaster disaster, Location current, DisasterConfig config) {
        long now = System.currentTimeMillis();
        long cacheInterval = config.getCrystalCacheInterval();
        
        // Cache güncelle (config'den aralık)
        if (now - lastCrystalCacheUpdate >= cacheInterval) {
            cachedNearestCrystal = disasterManager.findNearestCrystal(current);
            lastCrystalCacheUpdate = now;
        }
        
        // Eğer hedef kristal yoksa veya kırıldıysa yeni hedef bul
        Location oldCrystal = disaster.getTargetCrystal();
        if (disaster.getTargetCrystal() == null || 
            (cachedNearestCrystal != null && !cachedNearestCrystal.equals(disaster.getTargetCrystal()))) {
            disaster.setTargetCrystal(cachedNearestCrystal);
            disaster.setTarget(cachedNearestCrystal != null ? cachedNearestCrystal : disaster.getTarget());
            
            // Plan'a göre: Klan kristali hedef alındığında uyarı
            if (cachedNearestCrystal != null && (oldCrystal == null || !oldCrystal.equals(cachedNearestCrystal))) {
                Clan targetClan = findClanByCrystalLocation(cachedNearestCrystal);
                if (targetClan != null) {
                    Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                        "⚠ FELAKET " + targetClan.getName() + " klanının kristalini hedef aldı! ⚠");
                }
            }
        }
    }
    
    /**
     * Config'den aralık ile yakındaki oyunculara saldır
     * @param aggressiveMode Kristal yok edildikten sonra agresif mod (daha sık saldırı)
     * @param attackInterval Saldırı aralığı (ms) - faz sisteminden veya config'den
     */
    private void attackNearbyPlayersIfNeeded(Disaster disaster, Entity entity, Location current, 
                                             DisasterConfig config, boolean aggressiveMode, long attackInterval) {
        UUID entityId = entity.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Agresif modda daha sık saldır (normal aralığın yarısı)
        long finalAttackInterval = aggressiveMode ? attackInterval / 2 : attackInterval;
        
        Long lastAttack = lastAttackTime.get(entityId);
        if (lastAttack != null && now - lastAttack < finalAttackInterval) {
            return; // Henüz aralık geçmedi
        }
        
        // FAZ SİSTEMİ: Faz'a göre oyuncu saldırısı kontrolü
        if (phaseManager != null && !phaseManager.shouldAttackPlayers(disaster) && !aggressiveMode) {
            return; // Bu fazda oyunculara saldırmıyor
        }
        
        // Config'den yarıçap ile yakındaki oyuncuları bul ve saldır
        DisasterBehavior.attackPlayers(entity, current, config, disaster.getDamageMultiplier());
        
        lastAttackTime.put(entityId, now);
    }
    
    /**
     * Overload: Eski metod imzası (geriye dönük uyumluluk)
     */
    private void attackNearbyPlayersIfNeeded(Disaster disaster, Entity entity, Location current, 
                                             DisasterConfig config, boolean aggressiveMode) {
        long attackInterval = config.getAttackInterval();
        if (phaseManager != null) {
            attackInterval = phaseManager.getAttackInterval(disaster);
        }
        attackNearbyPlayersIfNeeded(disaster, entity, current, config, aggressiveMode, attackInterval);
    }
    
    /**
     * Kristal kontrolü ve yok etme (config'den yakınlık)
     */
    private void checkAndDestroyCrystal(Disaster disaster, Entity entity, Location current, DisasterConfig config) {
        Location targetCrystal = disaster.getTargetCrystal();
        if (targetCrystal == null) return;
        
        // Config'den yakınlık ile kristale yakın mı?
        double proximity = config.getCrystalProximity();
        if (current.distance(targetCrystal) <= proximity) {
            // Kristali bul
            Clan targetClan = findClanByCrystalLocation(targetCrystal);
            if (targetClan != null && targetClan.getCrystalEntity() != null) {
                // Kristali yok et - EntityDamageByEntityEvent ile (TerritoryListener yakalayacak)
                org.bukkit.entity.EnderCrystal crystal = targetClan.getCrystalEntity();
                if (crystal != null && !crystal.isDead()) {
                    // EnderCrystal'a hasar ver - EntityDamageByEntityEvent oluştur ve tetikle
                    // TerritoryListener bu event'i yakalayıp klanı dağıtacak
                    // Not: Constructor deprecated ama çalışıyor, alternatif yok
                    @SuppressWarnings("deprecation")
                    org.bukkit.event.entity.EntityDamageByEntityEvent damageEvent = 
                        new org.bukkit.event.entity.EntityDamageByEntityEvent(
                            crystal, entity, 
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK, 
                            1000.0);
                    Bukkit.getPluginManager().callEvent(damageEvent);
                    
                    // Event cancel edilmediyse kristali kır
                    if (!damageEvent.isCancelled() && damageEvent.getFinalDamage() >= 1.0) {
                        crystal.remove();
                    }
                }
                
                // Klanı dağıt (eğer TerritoryListener dağıtmadıysa)
                boolean crystalWasDestroyed = (targetClan.getCrystalEntity() == null || targetClan.getCrystalEntity().isDead());
                if (crystalWasDestroyed) {
                    destroyClan(targetClan, current, disaster.getDamageMultiplier());
                } else {
                    // Plan'a göre: Klan kristali korunursa bonus ödül (eğer felaket öldürülürse)
                    // Bu durumda kristal korundu, bonus ödül için flag set et
                    // (Felaket öldüğünde dropRewards'ta kontrol edilecek)
                }
                
                // Plan'a göre: Kristal yok edildikten sonra oyuncularla savaşır
                // Yeni kristal bulmadan önce 1 dakika oyuncularla savaş
                crystalDestroyed = true;
                crystalDestroyedTime = System.currentTimeMillis();
                disaster.setTargetCrystal(null);
                cachedNearestCrystal = null;
                lastCrystalCacheUpdate = 0;
                
                Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                    targetClan.getName() + " klanının kristali felaket tarafından yok edildi!");
                Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.YELLOW + 
                    "Felaket şimdi oyuncularla savaşıyor! 1 dakika sonra yeni kristal arayacak.");
            }
        }
    }
    
    /**
     * Kristal lokasyonuna göre klanı bul
     */
    private Clan findClanByCrystalLocation(Location crystalLoc) {
        if (territoryManager == null) return null;
        
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan.getCrystalLocation() != null && 
                clan.getCrystalLocation().distance(crystalLoc) < 1.0) {
                return clan;
            }
        }
        return null;
    }
    
    /**
     * Klanı yok et (yapılar ve kristal)
     */
    private void destroyClan(Clan clan, Location disasterLoc, double damageMultiplier) {
        // Yapıları yok et
        destroyClanStructures(clan, disasterLoc, damageMultiplier);
        
        // Klanı dağıt
        if (territoryManager != null && territoryManager.getClanManager() != null) {
            territoryManager.getClanManager().disbandClan(clan);
            territoryManager.setCacheDirty();
        }
        
        // Kahraman Buff'ı ver (eğer buffManager varsa)
        if (disasterManager != null) {
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null) {
                me.mami.stratocraft.manager.BuffManager buffManager = plugin.getBuffManager();
                if (buffManager != null) {
                    buffManager.applyHeroBuff(clan);
                }
            }
        }
    }
    
    
    /**
     * Doğa olaylarını işle (Handler sistemi ile)
     */
    private void handleNaturalDisaster(Disaster disaster) {
        if (disaster == null) return;
        
        // Süre doldu mu kontrol et (doğa olayları için önemli - öncelikli kontrol)
        // isExpired() kontrolü run() metodunda da var ama burada da kontrol ediyoruz
        if (disaster.isExpired() || disaster.getRemainingTime() <= 0) {
            // Felaketi durdur
            disaster.kill();
            disasterManager.setActiveDisaster(null);
            cleanupForceLoadedChunks();
            String disasterName = disasterManager.getDisasterDisplayName(disaster.getType());
            Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GREEN + "" + org.bukkit.ChatColor.BOLD + 
                "Doğa olayı süresi doldu: " + disasterName);
            // BossBar'ı temizle (DisasterManager'da otomatik temizleniyor)
            return;
        }
        
        DisasterConfig config = getConfig(disaster);
        DisasterHandler handler = handlerRegistry.getHandler(disaster.getType());
        handler.handle(disaster, null, config);
    }
    
    /**
     * Mini felaketleri işle (Handler sistemi ile)
     */
    private void handleMiniDisaster(Disaster disaster) {
        if (disaster == null) return;
        
        DisasterConfig config = getConfig(disaster);
        DisasterHandler handler = handlerRegistry.getHandler(disaster.getType());
        handler.handle(disaster, null, config);
    }
    
}

