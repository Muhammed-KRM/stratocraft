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
    
    // ✅ OPTİMİZE: Crystal location → Clan cache (performans için)
    private final java.util.Map<String, Clan> crystalLocationCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Long> crystalLocationCacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CRYSTAL_LOCATION_CACHE_DURATION = 5000L; // 5 saniye
    
    // ✅ OPTİMİZE: findCrystalsInRadius() cache (performans için)
    private final java.util.Map<String, java.util.List<org.bukkit.Location>> crystalsInRadiusCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Long> crystalsInRadiusCacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CRYSTALS_IN_RADIUS_CACHE_DURATION = 2000L; // 2 saniye
    
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
            // ✅ PERFORMANS: Felaket bittiğinde handler temizliği
            cleanupDisasterHandlers(disaster);
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
            // ✅ PERFORMANS: Felaket bittiğinde handler temizliği
            cleanupDisasterHandlers(disaster);
            cleanupForceLoadedChunks();
            return;
        }

        Entity entity = disaster.getEntity();
        
        // Canlı felaketler için entity kontrolü
        if (disaster.getCategory() == Disaster.Category.CREATURE) {
            // Grup felaketler için kontrol
            if (disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MEDIUM_GROUP || 
                disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MINI_SWARM) {
                java.util.List<Entity> groupEntities = disaster.getGroupEntities();
                if (groupEntities == null || groupEntities.isEmpty()) {
                    // Tüm entity'ler öldü
                    disasterManager.dropRewards(disaster);
                    disaster.kill();
                    disasterManager.setActiveDisaster(null);
                    cleanupForceLoadedChunks();
                    return;
                }
                // Ölü entity'leri listeden çıkar
                groupEntities.removeIf(e -> e == null || e.isDead() || !e.isValid());
                if (groupEntities.isEmpty()) {
                    // Tüm entity'ler öldü
                    disasterManager.dropRewards(disaster);
                    disaster.kill();
                    disasterManager.setActiveDisaster(null);
                    cleanupForceLoadedChunks();
                    return;
                }
                // İlk entity'yi temsilci olarak kullan (hedef belirleme için)
                entity = groupEntities.get(0);
            }
            
            // Tek boss felaketler için kontrol
            // ✅ DÜZELTME: Entity valid kontrolü - EnderDragon kaybolma sorunu için
            if (entity == null || entity.isDead() || !entity.isValid()) {
                // Plan'a göre: Felaket yok edilince ödül
                disasterManager.dropRewards(disaster);
                disaster.kill();
                disasterManager.setActiveDisaster(null);
                cleanupForceLoadedChunks(); // Chunk'ları temizle
                return;
            }
            
            // ✅ DÜZELTME: Entity görünürlük kontrolü - EnderDragon kaybolma sorunu için
            if (entity instanceof org.bukkit.entity.EnderDragon) {
                org.bukkit.entity.EnderDragon dragon = (org.bukkit.entity.EnderDragon) entity;
                // Entity görünür değilse görünür yap
                if (!dragon.isVisibleByDefault()) {
                    dragon.setVisibleByDefault(true);
                }
                // Entity'nin chunk'ı yüklü değilse yükle
                org.bukkit.Chunk chunk = dragon.getLocation().getChunk();
                if (!chunk.isLoaded()) {
                    chunk.load(false);
                }
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
     * Canlı felaketleri işle
     */
    private void handleCreatureDisaster(Disaster disaster, Entity entity) {
        Location current = entity.getLocation();
        DisasterConfig config = getConfig(disaster);
        
        // ✅ ÖZEL AI: Tek boss felaketler için CustomBossAI kullan
        boolean useCustomAI = disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.SINGLE_BOSS;
        if (useCustomAI) {
            me.mami.stratocraft.util.CustomBossAI.updateBossAI(entity, disaster, config);
            // CustomBossAI zaten tüm hareket ve saldırıları yönetiyor, bu yüzden handler'ı atla
            // Ancak faz sistemi ve diğer kontrolleri yapmaya devam et
            // Handler çağrısını atla (aşağıda)
        }
        
        // FAZ SİSTEMİ: Faz kontrolü ve geçişi
        if (phaseManager != null) {
            phaseManager.checkAndUpdatePhase(disaster);
        }
        
        // Merkez lokasyonunu al
        Location centerLoc = null;
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getDifficultyManager() != null) {
            centerLoc = plugin.getDifficultyManager().getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = current.getWorld().getSpawnLocation();
        }
        
        // Merkeze ulaşma kontrolü
        boolean merkezeUlasildi = hasReachedCenter(disaster, current);
        if (merkezeUlasildi && disaster.getCenterReachedTime() == 0) {
            disaster.setCenterReachedTime(System.currentTimeMillis());
            Bukkit.broadcastMessage("§c§l⚠ FELAKET MERKEZE ULAŞTI! ⚠");
        }
        
        // 3 saat kuralı kontrolü (merkeze ulaştıktan sonra)
        if (disaster.getCenterReachedTime() > 0) {
            long timeSinceReached = System.currentTimeMillis() - disaster.getCenterReachedTime();
            long threeHours = 3 * 60 * 60 * 1000L;  // 3 saat
            
            if (timeSinceReached >= threeHours) {
                disaster.kill();
                disasterManager.setActiveDisaster(null);
                cleanupForceLoadedChunks();
                Bukkit.broadcastMessage("§c§l⚠ FELAKET 3 SAAT İÇİNDE ÖLDÜRÜLEMEDİ! ⚠");
                return;
            }
        }
        
        // Merkeze ulaştıysa özel mantık
        if (merkezeUlasildi) {
            // ✅ OPTİMİZE: Merkezde 1000 blok yarıçapında klan var mı? (cache'li)
            java.util.List<org.bukkit.Location> centerCrystals = 
                findCrystalsInRadiusCached(centerLoc, 1000.0);
            
            if (!centerCrystals.isEmpty()) {
                // Klan var, en yakın klana saldır
                Location nearestCrystal = centerCrystals.get(0);  // En yakın klan
                
                // Eğer hedef kristal değiştiyse veya yoksa güncelle
                if (disaster.getTargetCrystal() == null || 
                    !disaster.getTargetCrystal().equals(nearestCrystal)) {
                    disaster.setTargetCrystal(nearestCrystal);
                    disaster.setTarget(nearestCrystal);
                    crystalDestroyed = false; // Yeni hedef, reset
                }
                
                // Kristal kontrolü ve yok etme
                if (!crystalDestroyed) {
                    checkAndDestroyCrystal(disaster, entity, current, config);
                }
                
                // Kristal yok edildikten sonra oyuncularla savaş (1-2 dakikada bir)
                if (crystalDestroyed) {
                    // ÖNCE: Yeni klan kontrolü yap (öncelikli) - 1 dakika beklemeden
                    java.util.List<org.bukkit.Location> checkCrystals = 
                        disasterManager.findCrystalsInRadius(centerLoc, 1000.0);
                    
                    if (!checkCrystals.isEmpty()) {
                        // Yeni klan görüldü, ona yönel (1 dakika bekleme iptal)
                        Location checkCrystal = checkCrystals.get(0);
                        disaster.setTargetCrystal(checkCrystal);
                        disaster.setTarget(checkCrystal);
                        crystalDestroyed = false; // Reset
                        crystalDestroyedTime = 0;
                        // Devam et, yeni klana git
                    } else {
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
                            // ✅ OPTİMİZE: Cache'i temizle
                            clearCrystalCaches();
                        }
                    }
                } else {
                    // Normal durum: Config'den saldırı aralığı (1-2 dakikada bir)
                    long attackInterval = config.getAttackInterval();
                    if (phaseManager != null) {
                        attackInterval = phaseManager.getAttackInterval(disaster);
                    }
                    attackNearbyPlayersIfNeeded(disaster, entity, current, config, false, attackInterval);
                }
            } else {
                // Merkezde klan yok, oyunculara saldır
                // ✅ OPTİMİZE: Oyuncu saldırısı sırasında klan kontrolü (1000 blok yarıçap - cache'li)
                java.util.List<org.bukkit.Location> nearbyCrystals = 
                    findCrystalsInRadiusCached(current, 1000.0);
                
                if (!nearbyCrystals.isEmpty()) {
                    // Yeni klan görüldü, en yakın klana yönel
                    Location nearestCrystal = nearbyCrystals.get(0);
                    disaster.setTargetCrystal(nearestCrystal);
                    disaster.setTarget(nearestCrystal);
                    crystalDestroyed = false;
                } else {
                    // Klan yok, en yakın oyuncuya saldır VE HEDEF AYARLA
                    org.bukkit.entity.Player nearestPlayer = findNearestPlayer(current, config.getAttackRadius());
                    if (nearestPlayer != null) {
                        // Oyuncuya hedef ayarla (hareket etmesi için)
                        disaster.setTarget(nearestPlayer.getLocation());
                        long attackInterval = config.getAttackInterval();
                        if (phaseManager != null) {
                            attackInterval = phaseManager.getAttackInterval(disaster);
                        }
                        attackNearestPlayerIfNeeded(disaster, entity, current, config, attackInterval);
                    } else {
                        // Oyuncu yoksa merkezde kal
                        disaster.setTarget(centerLoc);
                    }
                }
            }
        } else {
            // ✅ OPTİMİZE: Merkeze ulaşmadı, normal mantık (cache'li)
            // 1000 blok yarıçapında klan var mı?
            java.util.List<org.bukkit.Location> nearbyCrystals = 
                findCrystalsInRadiusCached(current, 1000.0);
            
            if (!nearbyCrystals.isEmpty()) {
                // Klan var, en yakın klana saldır
                Location nearestCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                
                // Kristal kontrolü ve yok etme
                if (!crystalDestroyed) {
                    checkAndDestroyCrystal(disaster, entity, current, config);
                }
                
                // Kristal yok edildikten sonra oyuncularla savaş (1-2 dakikada bir)
                if (crystalDestroyed) {
                    // ÖNCE: Yeni klan kontrolü yap (öncelikli) - 1 dakika beklemeden
                    java.util.List<org.bukkit.Location> checkCrystals = 
                        disasterManager.findCrystalsInRadius(current, 1000.0);
                    
                    if (!checkCrystals.isEmpty()) {
                        // Yeni klan görüldü, ona yönel (1 dakika bekleme iptal)
                        Location checkCrystal = checkCrystals.get(0);
                        disaster.setTargetCrystal(checkCrystal);
                        disaster.setTarget(checkCrystal);
                        crystalDestroyed = false; // Reset
                        crystalDestroyedTime = 0;
                        // Devam et, yeni klana git
                    } else {
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
                            // ✅ OPTİMİZE: Cache'i temizle
                            clearCrystalCaches();
                        }
                    }
                } else {
                    // ✅ OPTİMİZE: Normal durum: Config'den saldırı aralığı (1-2 dakikada bir - cache'li)
                    // Oyunculara saldırırken klan kontrolü yap
                    java.util.List<org.bukkit.Location> checkCrystals = 
                        findCrystalsInRadiusCached(current, 1000.0);
                    
                    if (!checkCrystals.isEmpty()) {
                        // Yeni klan görüldü, ona yönel
                        Location checkCrystal = checkCrystals.get(0);
                        disaster.setTargetCrystal(checkCrystal);
                        disaster.setTarget(checkCrystal);
                        crystalDestroyed = false;
                    } else {
                        // Klan yok, oyunculara saldır
                        long attackInterval = config.getAttackInterval();
                        if (phaseManager != null && phaseManager.shouldAttackPlayers(disaster)) {
                            attackInterval = phaseManager.getAttackInterval(disaster);
                        }
                        attackNearbyPlayersIfNeeded(disaster, entity, current, config, false, attackInterval);
                    }
                }
                } else {
                    // Klan yok, merkeze ilerle
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(centerLoc);
                    
                    // ✅ OPTİMİZE: Merkeze ilerlerken de oyunculara saldır (1-2 dakikada bir - cache'li)
                    // Ayrıca oyunculara saldırırken klan kontrolü yap
                    java.util.List<org.bukkit.Location> checkCrystals2 = 
                        findCrystalsInRadiusCached(current, 1000.0);
                
                if (!checkCrystals2.isEmpty()) {
                    // Yeni klan görüldü, ona yönel
                    Location checkCrystal2 = checkCrystals2.get(0);
                    disaster.setTargetCrystal(checkCrystal2);
                    disaster.setTarget(checkCrystal2);
                    crystalDestroyed = false;
                } else {
                    // Klan yok, oyunculara saldır (merkeze ilerlerken)
                    long attackInterval = config.getAttackInterval();
                    if (phaseManager != null && phaseManager.shouldAttackPlayers(disaster)) {
                        attackInterval = phaseManager.getAttackInterval(disaster);
                    }
                    attackNearbyPlayersIfNeeded(disaster, entity, current, config, false, attackInterval);
                }
            }
        }
        
        // ✅ PERFORMANS OPTİMİZASYONU: Chunk yükleme işlemini optimize et
        // Force load yerine normal load kullan (sunucu donmasını önler)
        if (current.getWorld() != null) {
            int chunkX = current.getBlockX() >> 4;
            int chunkZ = current.getBlockZ() >> 4;
            String chunkKey = chunkX + ";" + chunkZ;
            
            // Mevcut chunk'ı kontrol et ve yükle (force load yerine normal load)
            org.bukkit.Chunk currentChunk = current.getWorld().getChunkAt(chunkX, chunkZ);
            if (!currentChunk.isLoaded()) {
                // ✅ Normal load kullan (force load sunucuyu dondurabilir)
                currentChunk.load(false); // false = force load değil, normal load
            }
            // ✅ Force load kullanma (memory leak ve sunucu donmasına neden olur)
            // currentChunk.setForceLoaded(true); // KALDIRILDI
            
            // Eski chunk referanslarını temizle (memory leak önleme)
            java.util.Iterator<java.util.Map.Entry<String, org.bukkit.Chunk>> iterator = 
                forceLoadedChunks.entrySet().iterator();
            while (iterator.hasNext()) {
                java.util.Map.Entry<String, org.bukkit.Chunk> entry = iterator.next();
                if (!entry.getKey().equals(chunkKey)) {
                    // Bu chunk artık kullanılmıyor, referansı kaldır
                    // ✅ Force load kaldırma işlemi yapılmıyor (zaten force load kullanmıyoruz)
                    iterator.remove();
                }
            }
            
            // Mevcut chunk'ı kaydet (sadece referans için)
            forceLoadedChunks.put(chunkKey, currentChunk);
        }
        
        // Handler sistemi kullan - hedef kristale hareket etmesi için
        DisasterHandler handler = handlerRegistry.getHandler(disaster.getType());
        
        // ✅ Hedef kristal ayarlandıysa, disaster'a bildir (HANDLER ÇAĞRISINDAN ÖNCE)
        Location targetCrystal = disaster.getTargetCrystal();
        if (targetCrystal != null) {
            disaster.setTarget(targetCrystal);
        } else if (merkezeUlasildi) {
            // ✅ OPTİMİZE: Merkeze ulaştıysa ve kristal yoksa, en yakın oyuncuya hedef ayarla (cache'li)
            // Önce klan kontrolü yap (1000 blok yarıçap)
            java.util.List<org.bukkit.Location> centerCrystals = 
                findCrystalsInRadiusCached(current, 1000.0);
            
            if (!centerCrystals.isEmpty()) {
                // Klan var, en yakın klana yönel
                Location nearestCrystal = centerCrystals.get(0);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
            } else {
                // Klan yok, en yakın oyuncuya hedef ayarla
                org.bukkit.entity.Player nearestPlayer = findNearestPlayer(current, config.getAttackRadius());
                if (nearestPlayer != null) {
                    disaster.setTarget(nearestPlayer.getLocation());
                    // ✅ Oyuncuya saldır (merkeze ulaştıktan sonra)
                    long attackInterval = config.getAttackInterval();
                    if (phaseManager != null) {
                        attackInterval = phaseManager.getAttackInterval(disaster);
                    }
                    attackNearestPlayerIfNeeded(disaster, entity, current, config, attackInterval);
                } else {
                    // Oyuncu yoksa merkezde kal
                    disaster.setTarget(centerLoc);
                }
            }
        } else {
            // Merkeze ulaşmadıysa ve kristal yoksa merkeze git
            disaster.setTarget(centerLoc);
        }
        
        // Grup felaketler için özel işleme
        if (disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MEDIUM_GROUP || 
            disaster.getCreatureDisasterType() == Disaster.CreatureDisasterType.MINI_SWARM) {
            java.util.List<Entity> groupEntities = disaster.getGroupEntities();
            if (groupEntities != null && !groupEntities.isEmpty() && handler != null) {
                // Grup felaketler için de kristal kontrolü yap (en yakın entity ile)
                if (!crystalDestroyed && disaster.getTargetCrystal() != null) {
                    // En yakın entity'yi bul
                    Location groupTargetCrystal = disaster.getTargetCrystal();
                    Entity nearestEntity = null;
                    double minDistance = Double.MAX_VALUE;
                    for (Entity e : groupEntities) {
                        if (e == null || e.isDead() || !e.isValid()) continue;
                        double dist = e.getLocation().distance(groupTargetCrystal);
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearestEntity = e;
                        }
                    }
                    if (nearestEntity != null) {
                        checkAndDestroyCrystal(disaster, nearestEntity, nearestEntity.getLocation(), config);
                    }
                }
                
                // ✅ PERFORMANS: Grup felaketler için oyuncu saldırısı (daha az entity)
                if (disaster.getTargetCrystal() == null || crystalDestroyed) {
                    // Klan yoksa veya kristal yok edildiyse oyunculara saldır
                    long attackInterval = config.getAttackInterval();
                    if (phaseManager != null && phaseManager.shouldAttackPlayers(disaster)) {
                        attackInterval = phaseManager.getAttackInterval(disaster);
                    }
                    // ✅ PERFORMANS: Daha az entity saldırır (10 -> 5)
                    int attackCount = Math.min(groupEntities.size(), 5); // Her tick maksimum 5 entity saldırır
                    for (int i = 0; i < attackCount; i++) {
                        Entity e = groupEntities.get((int)(System.currentTimeMillis() % groupEntities.size()));
                        if (e != null && !e.isDead() && e.isValid()) {
                            attackNearbyPlayersIfNeeded(disaster, e, e.getLocation(), config, false, attackInterval);
                        }
                    }
                }
                
                handler.handleGroup(disaster, groupEntities, config);
            }
            return;
        }
        
        // ✅ HEDEF KONTROLÜ - Eğer hedef yoksa veya null ise, yeniden belirle
        Location currentTarget = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (currentTarget == null) {
            // Hedef yoksa, yeniden belirle
            disasterManager.setDisasterTarget(disaster);
            currentTarget = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        }
        
        // ✅ ÖZEL AI: CustomBossAI kullanıldığında handler'ı atla
        if (!useCustomAI) {
            // Tek boss felaketler için handler kullan (CustomBossAI kullanılmıyorsa)
            if (handler != null) {
                handler.handle(disaster, entity, config);
            } else {
                // Handler yoksa, manuel hareket
                if (currentTarget != null) {
                    me.mami.stratocraft.util.DisasterBehavior.moveToTarget(entity, currentTarget, config);
                }
            }
            
            // Özel yetenekleri kullan (faz bazlı)
            me.mami.stratocraft.model.DisasterPhase currentPhase = disaster.getCurrentPhase();
            if (currentPhase != null && handler != null) {
                handler.useSpecialAbilities(disaster, entity, config, currentPhase);
            }
        }
        // CustomBossAI zaten tüm hareket, saldırı ve özel yetenekleri yönetiyor
        
        // Çevre değişimi (bazı felaketler için)
        if (handler != null) {
            handler.changeEnvironment(disaster, entity, config);
        }
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
     * ✅ OPTİMİZE: Cleanup - Force-loaded chunk'ları temizle ve cache'leri temizle
     */
    private void cleanupForceLoadedChunks() {
        for (org.bukkit.Chunk chunk : forceLoadedChunks.values()) {
            if (chunk != null && chunk.isLoaded()) {
                chunk.setForceLoaded(false);
            }
        }
        forceLoadedChunks.clear();
        // ✅ OPTİMİZE: Cache'leri de temizle (felaket bittiğinde)
        clearCrystalCaches();
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
     * ✅ OPTİMİZE: Kristal lokasyonuna göre klanı bul (cache ile)
     */
    private Clan findClanByCrystalLocation(Location crystalLoc) {
        if (territoryManager == null || crystalLoc == null) return null;
        
        // ✅ OPTİMİZE: Cache key oluştur (location bazlı)
        String cacheKey = crystalLoc.getBlockX() + ";" + crystalLoc.getBlockY() + ";" + crystalLoc.getBlockZ();
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        Long cacheTime = crystalLocationCacheTime.get(cacheKey);
        if (cacheTime != null && (now - cacheTime) < CRYSTAL_LOCATION_CACHE_DURATION) {
            Clan cached = crystalLocationCache.get(cacheKey);
            if (cached != null) {
                // ✅ OPTİMİZE: Cache'deki klanın kristal lokasyonunu kontrol et (hala geçerli mi?)
                if (cached.getCrystalLocation() != null && 
                    cached.getCrystalLocation().distanceSquared(crystalLoc) < 1.0) {
                    return cached; // Cache hit
                }
            }
        }
        
        // Cache miss - tüm klanları döngüye al
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan.getCrystalLocation() != null) {
                // ✅ OPTİMİZE: distanceSquared() kullan (Math.sqrt pahalı)
                double distanceSquared = clan.getCrystalLocation().distanceSquared(crystalLoc);
                if (distanceSquared < 1.0) {
                    // Cache'e kaydet
                    crystalLocationCache.put(cacheKey, clan);
                    crystalLocationCacheTime.put(cacheKey, now);
                    return clan;
                }
            }
        }
        
        // Klan bulunamadı - cache'e null kaydet (negatif cache)
        crystalLocationCache.put(cacheKey, null);
        crystalLocationCacheTime.put(cacheKey, now);
        return null;
    }
    
    /**
     * Felaket merkeze ulaştı mı?
     */
    private boolean hasReachedCenter(Disaster disaster, Location current) {
        if (current == null) return false;
        
        Location centerLoc = null;
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getDifficultyManager() != null) {
            centerLoc = plugin.getDifficultyManager().getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = current.getWorld().getSpawnLocation();
        }
        
        if (!centerLoc.getWorld().equals(current.getWorld())) return false;
        
        double distance = current.distance(centerLoc);
        return distance <= 100.0;  // 100 blok yakınsa merkeze ulaşmış sayılır
    }
    
    /**
     * Merkezde belirtilen yarıçap içinde klan var mı?
     */
    private boolean hasClansInCenterRadius(Location center, double radius) {
        if (center == null || disasterManager == null) return false;
        java.util.List<org.bukkit.Location> crystals = disasterManager.findCrystalsInRadius(center, radius);
        return !crystals.isEmpty();
    }
    
    /**
     * En yakın oyuncuyu bul
     */
    private org.bukkit.entity.Player findNearestPlayer(Location current, double radius) {
        if (current == null || current.getWorld() == null) return null;
        
        org.bukkit.entity.Player nearestPlayer = null;
        double minDistance = Double.MAX_VALUE;
        
        for (org.bukkit.entity.Player player : current.getWorld().getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            Location playerLoc = player.getLocation();
            if (!playerLoc.getWorld().equals(current.getWorld())) continue;
            
            double distance = current.distance(playerLoc);
            if (distance <= radius && distance < minDistance) {
                minDistance = distance;
                nearestPlayer = player;
            }
        }
        
        return nearestPlayer;
    }
    
    /**
     * En yakın oyuncuya saldır (merkeze ulaştıktan sonra klan yoksa)
     */
    private void attackNearestPlayerIfNeeded(Disaster disaster, Entity entity, Location current, 
                                             DisasterConfig config, long attackInterval) {
        UUID entityId = entity.getUniqueId();
        long now = System.currentTimeMillis();
        
        Long lastAttack = lastAttackTime.get(entityId);
        if (lastAttack != null && now - lastAttack < attackInterval) {
            return; // Henüz aralık geçmedi
        }
        
        // En yakın oyuncuya saldır
        DisasterBehavior.attackNearestPlayer(entity, current, config, disaster.getDamageMultiplier());
        
        lastAttackTime.put(entityId, now);
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
     * ✅ OPTİMİZE: findCrystalsInRadius() cache'li versiyonu
     */
    private java.util.List<org.bukkit.Location> findCrystalsInRadiusCached(Location from, double radius) {
        if (from == null || disasterManager == null) return new java.util.ArrayList<>();
        
        // ✅ OPTİMİZE: Cache key oluştur (location + radius bazlı)
        String cacheKey = from.getBlockX() + ";" + from.getBlockY() + ";" + from.getBlockZ() + ";" + (int)radius;
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        Long cacheTime = crystalsInRadiusCacheTime.get(cacheKey);
        if (cacheTime != null && (now - cacheTime) < CRYSTALS_IN_RADIUS_CACHE_DURATION) {
            java.util.List<org.bukkit.Location> cached = crystalsInRadiusCache.get(cacheKey);
            if (cached != null) {
                return new java.util.ArrayList<>(cached); // Defensive copy
            }
        }
        
        // Cache miss - DisasterManager'dan al
        java.util.List<org.bukkit.Location> result = disasterManager.findCrystalsInRadius(from, radius);
        
        // Cache'e kaydet
        crystalsInRadiusCache.put(cacheKey, new java.util.ArrayList<>(result));
        crystalsInRadiusCacheTime.put(cacheKey, now);
        
        return result;
    }
    
    /**
     * ✅ OPTİMİZE: Crystal cache'lerini temizle (kristal yok edildiğinde)
     */
    private void clearCrystalCaches() {
        crystalLocationCache.clear();
        crystalLocationCacheTime.clear();
        crystalsInRadiusCache.clear();
        crystalsInRadiusCacheTime.clear();
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
    
    /**
     * ✅ PERFORMANS: Felaket handler'larını temizle (cache ve tick counter'ları)
     */
    private void cleanupDisasterHandlers(Disaster disaster) {
        if (disaster == null) return;
        
        // MobInvasionHandler temizliği
        DisasterHandler handler = handlerRegistry.getHandler(disaster.getType());
        if (handler instanceof me.mami.stratocraft.handler.impl.MobInvasionHandler) {
            ((me.mami.stratocraft.handler.impl.MobInvasionHandler) handler).cleanup(disaster);
        }
    }
    
}

