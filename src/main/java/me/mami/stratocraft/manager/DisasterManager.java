package me.mami.stratocraft.manager;

import java.util.Collection;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.CreatureDisasterType;
import me.mami.stratocraft.enums.DisasterCategory;
import me.mami.stratocraft.enums.DisasterType;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterPhase;

/**
 * Gelişmiş Felaket Yönetim Sistemi
 * 
 * Özellikler:
 * - Dinamik güç hesaplama (oyuncu sayısı + klan seviyesi)
 * - Kategoriler: Canlı felaketler, Doğa olayları
 * - Seviyeler: 1-3 (güç ve spawn sıklığı)
 * - Otomatik spawn sistemi
 * - Ekranda sayaç (BossBar)
 */
public class DisasterManager {
    private final Main plugin;
    private final ClanManager clanManager;
    private DifficultyManager difficultyManager; // Final değil, sonradan set edilecek
    private DisasterConfigManager configManager; // Config yöneticisi
    
    // Yeni dinamik zorluk sistemi (interface kullanarak gelecekte değiştirilebilir)
    private DisasterPowerConfig powerConfig;
    private IPowerCalculator playerPowerCalculator;
    private IServerPowerCalculator serverPowerCalculator;
    
    // Yeni Stratocraft Güç Sistemi (köprü fonksiyon için)
    private me.mami.stratocraft.manager.StratocraftPowerSystem stratocraftPowerSystem;
    
    // Arena transformasyon sistemi
    private DisasterArenaManager arenaManager;
    
    private Disaster activeDisaster = null;
    // ✅ lastDisasterTime başlangıçta şu anki zaman olmalı (sayaç donmasını önlemek için)
    // İlk felaket spawn olduğunda güncellenir
    private long lastDisasterTime = System.currentTimeMillis(); // Başlangıçta şu anki zaman
    
    // Mini felaket sistemi
    private long lastMiniDisasterTime = System.currentTimeMillis();
    private int miniDisasterCountToday = 0; // Bugün spawn olan mini felaket sayısı
    private long lastDayReset = System.currentTimeMillis();
    
    // Plan'a göre: 2 dakika önce uyarı sistemi
    private long lastWarningTime = 0;
    private static final long WARNING_INTERVAL = 120000L; // 2 dakika = 120000 ms
    
    // Zayıf nokta sistemi
    private final java.util.Map<java.util.UUID, Long> weakPointCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long WEAK_POINT_DURATION = 5000L; // 5 saniye
    private static final long WEAK_POINT_COOLDOWN = 15000L; // 15 saniye cooldown
    private org.bukkit.scheduler.BukkitTask weakPointTask;
    
    /**
     * Aktif felaket durumunu kaydet (DataManager için)
     * Not: Entity'ler kaydedilemez, sadece felaket durumu kaydedilir
     */
    public DisasterState getDisasterState() {
        if (activeDisaster == null || activeDisaster.isDead()) {
            return null;
        }
        return new DisasterState(
            activeDisaster.getType(),
            activeDisaster.getCategory(),
            activeDisaster.getLevel(),
            activeDisaster.getStartTime(),
            activeDisaster.getDuration(),
            activeDisaster.getTarget() != null ? activeDisaster.getTarget() : null
        );
    }
    
    /**
     * Felaket durumunu yükle (DataManager'dan çağrılır)
     * Not: Entity'ler kaydedilemediği için, sadece süre kontrolü yapılır
     * Entity olmadan felaket devam edemez, bu yüzden sadece log bırakıyoruz
     */
    public void loadDisasterState(DisasterState state) {
        if (state == null) return;
        
        // Süre kontrolü
        long elapsed = System.currentTimeMillis() - state.startTime;
        long remaining = state.duration - elapsed;
        
        if (remaining <= 0) {
            // Süre dolmuş, felaket bitti
            plugin.getLogger().info("Kaydedilmiş felaket süresi dolmuş: " + state.type.name());
            // lastDisasterTime'ı güncelle (süre dolmuş felaket için)
            lastDisasterTime = state.startTime + state.duration;
            return;
        }
        
        // Entity'ler kaydedilemediği için felaket devam edemez
        // Sadece bilgi log'u bırak, zorla iptal etme
        plugin.getLogger().info("Aktif felaket tespit edildi ancak entity'ler kaydedilemediği için devam edemiyor: " + 
            state.type.name() + " (Kalan süre: " + (remaining / 1000) + " saniye)");
        
        // lastDisasterTime'ı güncelle (felaket başladığı zaman)
        lastDisasterTime = state.startTime;
    }
    
    /**
     * Felaket durumu (kayıt için)
     */
    public static class DisasterState {
        public final Disaster.Type type;
        public final Disaster.Category category;
        public final int level;
        public final long startTime;
        public final long duration;
        public final Location target;
        
        public DisasterState(Disaster.Type type, Disaster.Category category, int level,
                           long startTime, long duration, Location target) {
            this.type = type;
            this.category = category;
            this.level = level;
            this.startTime = startTime;
            this.duration = duration;
            this.target = target;
        }
    }
    
    // Kategori Seviyesi Spawn Zamanları (ms)
    // Bu seviyeler felaketlerin otomatik spawn sıklığını belirler
    private static final long LEVEL_1_INTERVAL = 86400000L;  // Seviye 1: Her gün (1 gün)
    private static final long LEVEL_2_INTERVAL = 259200000L; // Seviye 2: 3 günde bir
    private static final long LEVEL_3_INTERVAL = 604800000L; // Seviye 3: 7 günde bir (haftada bir)
    
    // BossBar (ekranda sayaç)
    private BossBar disasterBossBar = null;
    private BukkitTask bossBarUpdateTask = null;
    
    // ✅ Countdown Scoreboard kaldırıldı - Artık HUDManager'daki bilgi panelinde gösteriliyor
    // Sadece uyarılar için task (scoreboard yok)
    private BukkitTask countdownUpdateTask = null;
    
    public DisasterManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        // difficultyManager henüz oluşturulmamış olabilir, sonradan set edilecek
        this.difficultyManager = plugin.getDifficultyManager();
        // ConfigManager'dan DisasterConfigManager al
        if (plugin.getConfigManager() != null) {
            this.configManager = plugin.getConfigManager().getDisasterConfigManager();
        }
        
        // Dinamik zorluk sistemini başlat (config yüklendikten sonra Main.java'da çağrılacak)
        // initializeDynamicDifficulty() Main.java'da çağrılmalı
    }
    
    /**
     * DifficultyManager'ı set et (Main.java'da sonradan çağrılır)
     */
    public void setDifficultyManager(me.mami.stratocraft.manager.DifficultyManager dm) {
        this.difficultyManager = dm;
    }
    
    /**
     * ConfigManager'ı set et
     */
    public void setConfigManager(DisasterConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * ArenaManager'ı set et
     */
    public void setArenaManager(DisasterArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }
    
    /**
     * Güç hesaplama formülü (Config'den okur)
     * 
     * İki sistem desteklenir:
     * 1. Yeni Dinamik Zorluk Sistemi (önerilen)
     * 2. Eski Sistem (geriye dönük uyumluluk)
     * 
     * @param level Felaket seviyesi
     * @return Hesaplanmış güç
     */
    public DisasterPower calculateDisasterPower(int level) {
        // Config'den seviye config'i al
        me.mami.stratocraft.model.DisasterConfig levelConfig;
        if (configManager != null) {
            levelConfig = configManager.getConfigForLevel(level);
        } else {
            levelConfig = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        // Config'den temel güç
        double baseHealth = levelConfig.getBaseHealth() * levelConfig.getHealthMultiplier();
        double baseDamage = levelConfig.getBaseDamage() * levelConfig.getDamageMultiplier();
        
        // Yeni dinamik zorluk sistemi aktif mi?
        if (powerConfig != null && powerConfig.isDynamicDifficultyEnabled() && 
            serverPowerCalculator != null) {
            return calculateDisasterPowerDynamic(levelConfig, baseHealth, baseDamage);
        }
        
        // Eski sistem (geriye dönük uyumluluk)
        return calculateDisasterPowerLegacy(levelConfig, baseHealth, baseDamage);
    }
    
    /**
     * Yeni dinamik zorluk sistemi ile güç hesaplama
     */
    private DisasterPower calculateDisasterPowerDynamic(
            me.mami.stratocraft.model.DisasterConfig levelConfig,
            double baseHealth, double baseDamage) {
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Yeni sistem varsa onu kullan, yoksa eski sistemi kullan
        double serverPower;
        
        if (stratocraftPowerSystem != null) {
            // Yeni Stratocraft Güç Sistemi kullan
            serverPower = calculateServerPowerWithNewSystem();
        } else if (serverPowerCalculator != null) {
            // Eski sistem (geriye dönük uyumluluk)
            serverPower = serverPowerCalculator.calculateServerPower();
        } else {
            // Hiçbir sistem yok, varsayılan değer
            serverPower = 0.0;
        }
        
        // Güç çarpanı hesaplama
        double powerScalingFactor = powerConfig.getPowerScalingFactor();
        double powerMultiplier = 1.0 + (serverPower / 100.0) * powerScalingFactor;
        
        // Maksimum ve minimum sınırlar
        double minMultiplier = powerConfig.getMinPowerMultiplier();
        double maxMultiplier = powerConfig.getMaxPowerMultiplier();
        powerMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, powerMultiplier));
        
        // Hesaplanmış güç
        double calculatedHealth = baseHealth * powerMultiplier;
        double calculatedDamage = baseDamage * powerMultiplier;
        
        return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
    }
    
    // ✅ PERFORMANS: Sunucu güç cache (felaket spawn'larında gereksiz hesaplama önleme)
    private double cachedServerPowerNewSystem = 0.0;
    private long lastServerPowerUpdate = 0;
    private static final long SERVER_POWER_CACHE_DURATION = 10000L; // 10 saniye
    
    /**
     * Yeni Stratocraft Güç Sistemi ile sunucu gücü hesapla (köprü fonksiyon)
     * ✅ PERFORMANS: Cache kullanarak gereksiz hesaplamaları önler
     */
    private double calculateServerPowerWithNewSystem() {
        if (stratocraftPowerSystem == null) return 0.0;
        
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (now - lastServerPowerUpdate < SERVER_POWER_CACHE_DURATION) {
            return cachedServerPowerNewSystem;
        }
        
        java.util.Collection<? extends org.bukkit.entity.Player> players = org.bukkit.Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            cachedServerPowerNewSystem = 0.0;
            lastServerPowerUpdate = now;
            return 0.0;
        }
        
        double totalPower = 0.0;
        int activePlayerCount = 0;
        
        // Tüm oyuncuların güç puanlarını topla (yeni sistemden - cache kullanır)
        for (org.bukkit.entity.Player player : players) {
            if (player.isOnline() && !player.isDead()) {
                me.mami.stratocraft.model.PlayerPowerProfile profile = 
                    stratocraftPowerSystem.calculatePlayerProfile(player); // Cache kullanır
                // Felaket için combat power önemli (config'den ayarlanabilir)
                double playerPower = profile.getTotalCombatPower();
                totalPower += playerPower;
                activePlayerCount++;
            }
        }
        
        if (activePlayerCount == 0) {
            cachedServerPowerNewSystem = 0.0;
            lastServerPowerUpdate = now;
            return 0.0;
        }
        
        // Ortalama güç
        double averagePower = totalPower / activePlayerCount;
        
        // Oyuncu sayısı çarpanı (config'den)
        // ✅ NULL KONTROLÜ: powerConfig null olabilir
        double playerCountMultiplier = 1.0;
        if (powerConfig != null) {
            playerCountMultiplier = powerConfig.getPlayerCountMultiplier(activePlayerCount);
        }
        
        // Sunucu güç puanı = Ortalama × Oyuncu Sayısı Çarpanı
        cachedServerPowerNewSystem = averagePower * playerCountMultiplier;
        lastServerPowerUpdate = now;
        
        return cachedServerPowerNewSystem;
    }
    
    /**
     * Sunucu güç cache'ini temizle (oyuncu giriş/çıkışında çağrılabilir)
     */
    public void clearServerPowerCache() {
        cachedServerPowerNewSystem = 0.0;
        lastServerPowerUpdate = 0;
    }
    
    /**
     * Eski sistem ile güç hesaplama (geriye dönük uyumluluk)
     */
    private DisasterPower calculateDisasterPowerLegacy(
            me.mami.stratocraft.model.DisasterConfig levelConfig,
            double baseHealth, double baseDamage) {
        
        // Oyuncu sayısı
        int playerCount = org.bukkit.Bukkit.getOnlinePlayers().size();
        
        // Ortalama klan seviyesi
        java.util.Collection<Clan> clans = clanManager.getAllClans();
        double avgClanLevel = 0;
        if (!clans.isEmpty()) {
            int totalLevel = 0;
            for (Clan clan : clans) {
                totalLevel += clan.getTechLevel();
            }
            avgClanLevel = (double) totalLevel / clans.size();
        }
        
        // Eski çarpanlar
        double playerMultiplier = powerConfig != null ? 
            powerConfig.getLegacyPlayerMultiplier() : levelConfig.getPlayerMultiplier();
        double clanMultiplier = powerConfig != null ? 
            powerConfig.getLegacyClanMultiplier() : levelConfig.getClanMultiplier();
        
        // Güç çarpanı
        double powerMultiplier = 1.0 + (playerCount * playerMultiplier) + (avgClanLevel * clanMultiplier);
        
        // Hesaplanmış güç
        double calculatedHealth = baseHealth * powerMultiplier;
        double calculatedDamage = baseDamage * powerMultiplier;
        
        return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
    }
    
    /**
     * Dinamik zorluk sistemini başlat
     * Interface kullanarak gelecekte farklı implementasyonlar eklenebilir
     */
    public void initializeDynamicDifficulty(DisasterPowerConfig powerConfig,
                                           IPowerCalculator playerPowerCalculator,
                                           IServerPowerCalculator serverPowerCalculator) {
        this.powerConfig = powerConfig;
        this.playerPowerCalculator = playerPowerCalculator;
        this.serverPowerCalculator = serverPowerCalculator;
    }
    
    /**
     * Yeni Stratocraft Güç Sistemini set et (köprü fonksiyon için)
     */
    public void setStratocraftPowerSystem(me.mami.stratocraft.manager.StratocraftPowerSystem powerSystem) {
        this.stratocraftPowerSystem = powerSystem;
    }
    
    /**
     * Güç verisi sınıfı
     */
    public static class DisasterPower {
        public final double health;
        public final double damage;
        public final double multiplier;
        
        public DisasterPower(double health, double damage, double multiplier) {
            this.health = health;
            this.damage = damage;
            this.multiplier = multiplier;
        }
    }
    
    /**
     * Felaket başlat
     */
    /**
     * Felaket başlat (YENİ: DisasterType enum kullanır)
     */
    public void triggerDisaster(DisasterType type, int level) {
        int categoryLevel = Disaster.getDefaultLevel(convertToOldType(type));
        triggerDisaster(convertToOldType(type), categoryLevel, level);
    }
    
    /**
     * Felaket başlat (YENİ: DisasterType enum kullanır, konum belirtilmiş)
     */
    public void triggerDisaster(DisasterType type, int level, org.bukkit.Location spawnLoc) {
        int categoryLevel = Disaster.getDefaultLevel(convertToOldType(type));
        triggerDisaster(convertToOldType(type), categoryLevel, level, spawnLoc);
    }
    
    /**
     * Felaket başlat (YENİ: DisasterType enum kullanır, kategori ve iç seviye ayrı)
     */
    public void triggerDisaster(DisasterType type, int categoryLevel, int internalLevel) {
        World world = org.bukkit.Bukkit.getWorlds().get(0);
        org.bukkit.Location centerLoc = null;
        if (difficultyManager != null) {
            centerLoc = difficultyManager.getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = world.getSpawnLocation();
        }
        triggerDisaster(type, categoryLevel, internalLevel, centerLoc);
    }
    
    /**
     * Felaket başlat (YENİ: DisasterType enum kullanır, kategori ve iç seviye ayrı, konum belirtilmiş)
     */
    public void triggerDisaster(DisasterType type, int categoryLevel, int internalLevel, org.bukkit.Location spawnLoc) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            org.bukkit.Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        // Spawn lokasyonunun chunk'ını force load et
        World world = spawnLoc.getWorld();
        int chunkX = spawnLoc.getBlockX() >> 4;
        int chunkZ = spawnLoc.getBlockZ() >> 4;
        // ✅ OPTİMİZE: Force load kaldırıldı (sonsuz döngüye neden olabilir)
        // Chunk zaten yüklüyse işlem yapma
        org.bukkit.Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.isLoaded()) {
            chunk.load(false); // Normal load kullan (force load değil)
        }
        
        // Yeni enum'ları kullan
        DisasterCategory category = Disaster.getCategory(type);
        // İç seviye güç hesaplaması için kullanılır
        DisasterPower power = calculateDisasterPower(internalLevel);
        long duration = Disaster.getDefaultDuration(convertToOldType(type), categoryLevel);
        
        Entity entity = null;
        
        // Canlı felaketler için entity oluştur
        if (category == DisasterCategory.CREATURE) {
            me.mami.stratocraft.model.Disaster.CreatureDisasterType oldCreatureType = Disaster.getCreatureDisasterType(convertToOldType(type));
            CreatureDisasterType creatureType = oldCreatureType != null ? CreatureDisasterType.valueOf(oldCreatureType.name()) : null;
            
            if (creatureType == CreatureDisasterType.MEDIUM_GROUP) {
                // Grup felaket spawn (30 adet)
                org.bukkit.entity.EntityType entityType = getEntityTypeForDisaster(convertToOldType(type));
                if (entityType != null) {
                    spawnGroupDisaster(entityType, 30, spawnLoc, internalLevel);
                    return; // spawnGroupDisaster içinde activeDisaster set ediliyor
                }
            } else if (creatureType == CreatureDisasterType.MINI_SWARM) {
                // Mini dalga spawn (100-500 adet)
                org.bukkit.entity.EntityType entityType = getEntityTypeForDisaster(convertToOldType(type));
                if (entityType != null) {
                    int count = 100 + (int)(Math.random() * 400); // 100-500 arası rastgele
                    spawnSwarmDisaster(entityType, count, spawnLoc, internalLevel);
                    return; // spawnSwarmDisaster içinde activeDisaster set ediliyor
                }
            } else {
                // Tek boss felaket spawn
                entity = spawnCreatureDisaster(convertToOldType(type), spawnLoc, power);
                if (entity == null) {
                    org.bukkit.Bukkit.broadcastMessage("§c§l⚠ FELAKET SPAWN HATASI! ⚠");
                    org.bukkit.Bukkit.broadcastMessage("§7Felaket tipi için entity oluşturulamadı: §e" + type.name());
                    return;
                }
            }
        }
        
        // Felaket oluştur
        org.bukkit.Location targetLoc = null;
        if (difficultyManager != null) {
            targetLoc = difficultyManager.getCenterLocation();
        }
        if (targetLoc == null) {
            targetLoc = spawnLoc.getWorld().getSpawnLocation();
        }
        // Disaster oluştururken iç seviyeyi kullan (güç seviyesi)
        activeDisaster = new Disaster(convertToOldType(type), convertToOldCategory(category), internalLevel, entity,
                                      targetLoc, power.health, power.damage, duration);
        setDisasterTarget(activeDisaster);
        
        // ✅ ÖZEL AI: Tek boss felaketler için CustomBossAI'yı başlat
        if (entity != null && category == DisasterCategory.CREATURE) {
            Disaster.CreatureDisasterType creatureType = Disaster.getCreatureDisasterType(convertToOldType(type));
            if (creatureType == Disaster.CreatureDisasterType.SINGLE_BOSS) {
                me.mami.stratocraft.model.DisasterConfig disasterConfig = null;
                if (configManager != null) {
                    disasterConfig = configManager.getConfig(convertToOldType(type), internalLevel);
                }
                if (disasterConfig == null) {
                    disasterConfig = new me.mami.stratocraft.model.DisasterConfig();
                }
                me.mami.stratocraft.util.CustomBossAI.initializeBossAI(entity, activeDisaster, disasterConfig);
            }
        }
        
        // Arena transformasyon başlat
        if (arenaManager != null && category == DisasterCategory.CREATURE) {
            // getId() metodu yok, UUID oluştur
            java.util.UUID disasterId = java.util.UUID.randomUUID();
            arenaManager.startArenaTransformation(spawnLoc, convertToOldType(type), internalLevel, disasterId);
        }
        
        // BossBar oluştur
        createBossBar(activeDisaster);
        
        // Uyarı gönder
        org.bukkit.Bukkit.broadcastMessage("§c§l⚠ FELAKET BAŞLADI! ⚠");
        org.bukkit.Bukkit.broadcastMessage("§4§l" + getDisasterDisplayName(type) + " §7spawn oldu!");
        
        // Son felaket zamanını güncelle
        lastDisasterTime = System.currentTimeMillis();
    }
    
    /**
     * Felaket başlat (GERİYE UYUMLULUK: Disaster.Type enum kullanır)
     * @deprecated DisasterType kullanın
     */
    @Deprecated
    public void triggerDisaster(Disaster.Type type, int level) {
        int categoryLevel = Disaster.getDefaultLevel(type);
        triggerDisaster(type, categoryLevel, level);
    }
    
    /**
     * Felaket başlat (GERİYE UYUMLULUK: Disaster.Type enum kullanır)
     * @deprecated DisasterType kullanın
     */
    @Deprecated
    public void triggerDisaster(Disaster.Type type, int level, org.bukkit.Location spawnLoc) {
        int categoryLevel = Disaster.getDefaultLevel(type);
        triggerDisaster(type, categoryLevel, level, spawnLoc);
    }
    
    /**
     * Felaket başlat (GERİYE UYUMLULUK: Disaster.Type enum kullanır)
     * @deprecated DisasterType kullanın
     */
    @Deprecated
    public void triggerDisaster(Disaster.Type type, int categoryLevel, int internalLevel) {
        World world = org.bukkit.Bukkit.getWorlds().get(0);
        org.bukkit.Location centerLoc = null;
        if (difficultyManager != null) {
            centerLoc = difficultyManager.getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = world.getSpawnLocation();
        }
        
        // Config'den spawn mesafesini al (iç seviye kullanılır)
        double spawnDistance = 5000.0; // Varsayılan
        if (configManager != null) {
            me.mami.stratocraft.model.DisasterConfig config = configManager.getConfig(type, internalLevel);
            spawnDistance = config.getSpawnDistance();
        }
        
        // Merkezden en uzak noktayı bul (config'den okunan mesafe)
        // 360 derece rastgele açı kullan (4 yön yerine tüm yönler)
        int distance = (int) spawnDistance;
        double angle = random.nextDouble() * 2 * Math.PI; // 0-360 derece arası rastgele açı
        int x = centerLoc.getBlockX() + (int)(Math.cos(angle) * distance);
        int z = centerLoc.getBlockZ() + (int)(Math.sin(angle) * distance);
        
        // Chunk'ı force load et (felaket hareket edebilsin diye)
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        // ✅ OPTİMİZE: Force load kaldırıldı (sonsuz döngüye neden olabilir)
        // Chunk zaten yüklüyse işlem yapma
        org.bukkit.Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.isLoaded()) {
            chunk.load(false); // Normal load kullan (force load değil)
        }
        
        // Chunk yüklendikten sonra spawn yap
        int y = world.getHighestBlockYAt(x, z);
        org.bukkit.Location spawnLoc = new org.bukkit.Location(world, x, y + 1, z);
        
        triggerDisaster(type, categoryLevel, internalLevel, spawnLoc);
    }
    
    /**
     * Felaket başlat (yeni format - kategori seviyesi ve iç seviye ayrı, konum belirtilmiş)
     */
    public void triggerDisaster(Disaster.Type type, int categoryLevel, int internalLevel, org.bukkit.Location spawnLoc) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            org.bukkit.Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        // Spawn lokasyonunun chunk'ını force load et
        World world = spawnLoc.getWorld();
        int chunkX = spawnLoc.getBlockX() >> 4;
        int chunkZ = spawnLoc.getBlockZ() >> 4;
        // ✅ OPTİMİZE: Force load kaldırıldı (sonsuz döngüye neden olabilir)
        // Chunk zaten yüklüyse işlem yapma
        org.bukkit.Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.isLoaded()) {
            chunk.load(false); // Normal load kullan (force load değil)
        }
        
        // Canlı felaketler için merkeze giden yol boyunca chunk'ları da yükle (opsiyonel, performans için)
        // NOT: DisasterTask içinde chunk yönetimi yapılıyor, burada sadece spawn chunk'ını yükle
        
        Disaster.Category category = Disaster.getCategory(type);
        // İç seviye güç hesaplaması için kullanılır
        DisasterPower power = calculateDisasterPower(internalLevel);
        long duration = Disaster.getDefaultDuration(type, categoryLevel);
        
        Entity entity = null;
        
        // Canlı felaketler için entity oluştur
        if (category == Disaster.Category.CREATURE) {
            Disaster.CreatureDisasterType creatureType = Disaster.getCreatureDisasterType(type);
            
            if (creatureType == Disaster.CreatureDisasterType.MEDIUM_GROUP) {
                // Grup felaket spawn (30 adet)
                org.bukkit.entity.EntityType entityType = getEntityTypeForDisaster(type);
                if (entityType != null) {
                    spawnGroupDisaster(entityType, 30, spawnLoc, internalLevel);
                    return; // spawnGroupDisaster içinde activeDisaster set ediliyor
                }
            } else if (creatureType == Disaster.CreatureDisasterType.MINI_SWARM) {
                // Mini dalga spawn (100-500 adet)
                org.bukkit.entity.EntityType entityType = getEntityTypeForDisaster(type);
                if (entityType != null) {
                    int count = 100 + (int)(Math.random() * 400); // 100-500 arası rastgele
                    spawnSwarmDisaster(entityType, count, spawnLoc, internalLevel);
                    return; // spawnSwarmDisaster içinde activeDisaster set ediliyor
                }
            } else {
                // Tek boss felaket spawn
                entity = spawnCreatureDisaster(type, spawnLoc, power);
                if (entity == null) {
                    org.bukkit.Bukkit.broadcastMessage("§c§l⚠ FELAKET SPAWN HATASI! ⚠");
                    org.bukkit.Bukkit.broadcastMessage("§7Felaket tipi için entity oluşturulamadı: §e" + type.name());
                    return;
                }
            }
        }
        
        // Felaket oluştur
        org.bukkit.Location targetLoc = null;
        if (difficultyManager != null) {
            targetLoc = difficultyManager.getCenterLocation();
        }
        if (targetLoc == null) {
            targetLoc = spawnLoc.getWorld().getSpawnLocation();
        }
        // Disaster oluştururken iç seviyeyi kullan (güç seviyesi)
        activeDisaster = new Disaster(type, category, internalLevel, entity, 
                                     targetLoc, 
                                     power.health, power.damage, duration);
        
        // ✅ Hedef kristali belirle (İLK HEDEF BELİRLEME - ÖNEMLİ!)
        // Entity spawn olduktan SONRA hedef belirle (entity location'ı doğru olsun)
        setDisasterTarget(activeDisaster);
        
        // ✅ Entity'nin hemen hareket etmesi için hedefi kontrol et ve ayarla
        if (entity != null) {
            // Hedef yoksa veya null ise, yeniden belirle
            Location currentTarget = activeDisaster.getTargetCrystal() != null ? 
                activeDisaster.getTargetCrystal() : activeDisaster.getTarget();
            if (currentTarget == null) {
                // Entity location'ı ile yeniden hedef belirle
                setDisasterTarget(activeDisaster);
                currentTarget = activeDisaster.getTargetCrystal() != null ? 
                    activeDisaster.getTargetCrystal() : activeDisaster.getTarget();
            }
            
            // ✅ Hedefi kesinlikle ayarla (ilk spawn'da hareket etmesi için)
            if (currentTarget != null) {
                activeDisaster.setTarget(currentTarget);
            }
            
            // ✅ DÜZELTME: EnderDragon gibi özel entity'ler için spawn sonrası hemen hareket ettir
            if (entity instanceof org.bukkit.entity.EnderDragon) {
                org.bukkit.entity.EnderDragon dragon = (org.bukkit.entity.EnderDragon) entity;
                Location finalTarget = activeDisaster.getTargetCrystal() != null ? 
                    activeDisaster.getTargetCrystal() : activeDisaster.getTarget();
                if (finalTarget != null && entity.getLocation().getWorld().equals(finalTarget.getWorld())) {
                    // ✅ Dragon'un hedefini ayarla (ilk spawn'da hemen hareket etmesi için)
                    activeDisaster.setTarget(finalTarget);
                    
                    // ✅ DÜZELTME: Spawn sonrası hemen velocity ile hareket ettir (AI devre dışı olduğu için)
                    org.bukkit.util.Vector direction = me.mami.stratocraft.util.DisasterUtils.calculateDirection(
                        entity.getLocation(), finalTarget);
                    double initialSpeed = 0.4; // İlk hareket için hızlı başlangıç
                    org.bukkit.util.Vector initialVelocity = direction.multiply(initialSpeed);
                    initialVelocity.setY(Math.max(0.2, direction.getY() * initialSpeed * 0.5)); // Uçması için
                    dragon.setVelocity(initialVelocity);
                    
                    // Yüz yönlendirme
                    me.mami.stratocraft.util.DisasterBehavior.faceTarget(entity, finalTarget);
                }
            }
        }
        
        // ✅ Countdown Scoreboard kaldırıldı - Artık HUDManager'daki bilgi panelinde gösteriliyor
        // Sadece task'ı durdur (scoreboard yok artık)
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
            countdownUpdateTask = null;
        }
        
        // Broadcast (BossBar'dan önce, herkes görsün)
        String disasterName = getDisasterDisplayName(type);
        org.bukkit.Bukkit.broadcastMessage("§c§l⚠ FELAKET BAŞLADI! ⚠");
        org.bukkit.Bukkit.broadcastMessage("§4§l" + disasterName + " §7(Seviye " + internalLevel + ")");
        org.bukkit.Bukkit.broadcastMessage("§7Güç Çarpanı: §e" + String.format("%.2f", power.multiplier) + "x");
        if (entity != null) {
            org.bukkit.Bukkit.broadcastMessage("§7Konum: §e" + spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
        }
        
        // BossBar oluştur (canlı felaketler için)
        createBossBar(activeDisaster);
        
        // Arena transformasyonunu başlat (canlı felaketler için)
        if (category == Disaster.Category.CREATURE && entity != null) {
            arenaManager.startArenaTransformation(
                spawnLoc,
                type,
                internalLevel,
                entity.getUniqueId()
            );
        }
        
        lastDisasterTime = System.currentTimeMillis();
    }
    
    /**
     * Canlı felaket spawn et (Config kullanır)
     */
    private org.bukkit.entity.Entity spawnCreatureDisaster(Disaster.Type type, org.bukkit.Location loc, DisasterPower power) {
        World world = loc.getWorld();
        
        // Config'den ayarları al
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            int level = Disaster.getDefaultLevel(type);
            config = configManager.getConfig(type, level);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        org.bukkit.entity.Entity entity = null;
        
        switch (type) {
            case CATASTROPHIC_TITAN:
                // Felaket Titanı - 30 blok boyutunda dev golem
                entity = world.spawnEntity(loc, EntityType.IRON_GOLEM);
                entity.setCustomName("§4§lFELAKET TİTANI");
                // ✅ ÖZEL AI: Normal AI'yı devre dışı bırak
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                    living.setAI(false); // Özel AI kullanılacak
                    // Boyut: Normal IronGolem ~2.7 blok, 30 blok için ~11.1 kat
                    try {
                        org.bukkit.attribute.Attribute scaleAttr = org.bukkit.attribute.Attribute.valueOf("GENERIC_SCALE");
                        if (living.getAttribute(scaleAttr) != null) {
                            living.getAttribute(scaleAttr).setBaseValue(11.1);
                        }
                    } catch (IllegalArgumentException e) {
                        // GENERIC_SCALE bu versiyonda yok, atla
                    }
                    if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                        double maxHealth = config.getBaseHealth() * config.getHealthMultiplier() * power.health;
                        living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                    }
                    if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE) != null) {
                        living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(config.getBaseDamage() * power.multiplier);
                    }
                    double maxHealth = config.getBaseHealth() * config.getHealthMultiplier() * power.health;
                    living.setHealth(maxHealth);
                }
                break;
                
            case CATASTROPHIC_ABYSSAL_WORM:
                entity = world.spawnEntity(loc, EntityType.SILVERFISH);
                entity.setCustomName("§5§lFELAKET HİÇLİK SOLUCANI");
                // ✅ ÖZEL AI: Normal AI'yı devre dışı bırak
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                    living.setAI(false); // Özel AI kullanılacak
                    living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INVISIBILITY, 999999, 0, false, false));
                }
                break;
                
            case CATASTROPHIC_CHAOS_DRAGON:
                entity = world.spawnEntity(loc, EntityType.ENDER_DRAGON);
                entity.setCustomName("§5§lFELAKET KHAOS EJDERİ");
                // ✅ ÖZEL AI: EnderDragon AI'sını devre dışı bırak (sadece felaket hareketlerini yapsın)
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) entity).setAI(false);
                }
                break;
                
            case CATASTROPHIC_VOID_TITAN:
                entity = world.spawnEntity(loc, EntityType.WITHER);
                entity.setCustomName("§8§lFELAKET BOŞLUK TİTANI");
                // ✅ ÖZEL AI: Normal AI'yı devre dışı bırak
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) entity).setAI(false);
                }
                break;
                
            case CATASTROPHIC_ICE_LEVIATHAN:
                entity = world.spawnEntity(loc, EntityType.ELDER_GUARDIAN);
                entity.setCustomName("§b§lFELAKET BUZUL LEVİATHAN");
                // ✅ ÖZEL AI: Normal AI'yı devre dışı bırak
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                    living.setAI(false); // Özel AI kullanılacak
                    living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 999999, 0, false, false));
                }
                break;
                
            default:
                return null;
        }
        
        // DisasterUtils ile güçlendirme (tüm entity'ler için ortak)
        if (entity != null) {
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier);
        }
        
        return entity;
    }
    
    /**
     * BossBar oluştur ve güncelle
     */
    private void createBossBar(Disaster disaster) {
        // Eski BossBar'ı temizle
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
            disasterBossBar = null;
        }
        
        // Canlı felaketler için BossBar oluştur
        if (disaster != null && disaster.getCategory() == Disaster.Category.CREATURE) {
            // Entity kontrolü (null olabilir, ama BossBar yine de gösterilmeli)
            String disasterName = getDisasterDisplayName(disaster.getType());
            double health = disaster.getCurrentHealth();
            double maxHealth = disaster.getMaxHealth();
            double healthPercent = maxHealth > 0 ? Math.max(0.0, Math.min(1.0, health / maxHealth)) : 1.0;
            String timeLeft = formatTime(disaster.getRemainingTime());
            
            disasterBossBar = Bukkit.createBossBar(
                "§c§l" + disasterName + " §7| §c" + String.format("%.0f/%.0f", health, maxHealth) + " §7| §e⏰ " + timeLeft,
                BarColor.RED,
                BarStyle.SOLID
            );
            disasterBossBar.setProgress(healthPercent);
            
            // ✅ OPTİMİZE: Tüm oyunculara ekle (sadece bir kez, sonraki güncellemelerde ekleme yapılmaz)
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            for (Player player : onlinePlayers) {
                if (player != null && player.isOnline()) {
                disasterBossBar.addPlayer(player);
                }
            }
            
            plugin.getLogger().info("BossBar oluşturuldu: " + disasterName + " (Can: " + health + "/" + maxHealth + ")");
        } else if (disaster != null && disaster.getCategory() == Disaster.Category.NATURAL) {
            // Doğa olayları için ActionBar kullan (BossBar yok)
            // ActionBar güncellemesi bossBarUpdateTask içinde yapılıyor
            // Burada sadece ilk gösterimi yapıyoruz
        }
        
        // Güncelleme task'ı
        if (bossBarUpdateTask != null) {
            bossBarUpdateTask.cancel();
        }
        
        bossBarUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeDisaster == null || activeDisaster.isDead()) {
                if (disasterBossBar != null) {
                    disasterBossBar.removeAll();
                    disasterBossBar = null;
                }
                if (bossBarUpdateTask != null) {
                    bossBarUpdateTask.cancel();
                    bossBarUpdateTask = null;
                }
                // Felaket bittiğinde countdown'u tekrar göster
                updateCountdownBossBar();
                return;
            }
            
            // Can ve zaman bilgisi
            // Health sync: Entity'den can al (eğer entity varsa)
            double health = activeDisaster.getCurrentHealth();
            double maxHealth = activeDisaster.getMaxHealth();
            
            // Entity varsa ve canlı felaket ise, entity'den can al
            if (activeDisaster.getCategory() == Disaster.Category.CREATURE && activeDisaster.getEntity() != null) {
                Entity entity = activeDisaster.getEntity();
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                    health = living.getHealth();
                    // Max health'i de entity'den al (attribute modifier varsa)
                    if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                        maxHealth = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    }
                }
            }
            
            // Health değerlerini sınırla
            if (health < 0) health = 0;
            if (maxHealth <= 0) maxHealth = 1.0; // Sıfıra bölme hatası önleme
            if (health > maxHealth) health = maxHealth;
            
            double healthPercent = Math.max(0.0, Math.min(1.0, health / maxHealth));
            String timeLeft = formatTime(activeDisaster.getRemainingTime());
            String disasterName = getDisasterDisplayName(activeDisaster.getType());
            
            // Canlı felaketler için BossBar güncelle
            if (activeDisaster.getCategory() == Disaster.Category.CREATURE && disasterBossBar != null) {
                String bossBarTitle = "§c§l" + disasterName + " §7| §c" + 
                    String.format("%.0f/%.0f", health, maxHealth) + " §7| §e⏰ " + timeLeft;
                disasterBossBar.setTitle(bossBarTitle);
                disasterBossBar.setProgress(healthPercent);
                
                // Faz bazlı renk değiştir (öncelikli)
                DisasterPhase currentPhase = activeDisaster.getCurrentPhase();
                if (currentPhase != null) {
                    switch (currentPhase) {
                        case EXPLORATION:
                            disasterBossBar.setColor(BarColor.BLUE); // Mavi (keşif fazı)
                            break;
                        case ASSAULT:
                            disasterBossBar.setColor(BarColor.YELLOW); // Sarı (saldırı fazı)
                            break;
                        case RAGE:
                            disasterBossBar.setColor(BarColor.RED); // Kırmızı (öfke fazı)
                            break;
                        case DESPERATION:
                            disasterBossBar.setColor(BarColor.PURPLE); // Mor (son çare fazı)
                            break;
                        default:
                            // Can durumuna göre renk değiştir (fallback)
                            if (healthPercent > 0.6) {
                                disasterBossBar.setColor(BarColor.RED);
                            } else if (healthPercent > 0.3) {
                                disasterBossBar.setColor(BarColor.YELLOW);
                            } else {
                                disasterBossBar.setColor(BarColor.GREEN);
                            }
                            break;
                    }
                } else {
                    // Faz bilgisi yoksa can durumuna göre renk değiştir
                    if (healthPercent > 0.6) {
                        disasterBossBar.setColor(BarColor.RED);
                    } else if (healthPercent > 0.3) {
                        disasterBossBar.setColor(BarColor.YELLOW);
                    } else {
                        disasterBossBar.setColor(BarColor.GREEN);
                    }
                }
                
                // Yeni oyuncuları ekle (optimizasyon: sadece yeni oyuncular varsa)
                java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                for (Player player : onlinePlayers) {
                    if (!disasterBossBar.getPlayers().contains(player)) {
                        disasterBossBar.addPlayer(player);
                    }
                }
            } else if (activeDisaster.getCategory() == Disaster.Category.NATURAL) {
                // Doğa olayları için ActionBar kullan (her saniye güncelle)
                java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                String actionBarText = "§c§l" + disasterName + " §7| §e⏰ " + timeLeft;
                for (Player player : onlinePlayers) {
                    if (player != null && player.isOnline()) {
                        try {
                            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                        } catch (Exception e) {
                            // ActionBar gönderme hatası (oyuncu çıkış yapmış olabilir)
                            plugin.getLogger().fine("ActionBar gönderilemedi: " + e.getMessage());
                        }
                    }
                }
            }
        }, 0L, 20L); // Her saniye güncelle
    }
    
    /**
     * HUD için countdown bilgisini al (geriye uyumluluk için - sadece en yakın felaketi döndürür)
     */
    public String[] getCountdownInfo() {
        CountdownInfo[] allInfo = getAllCountdownInfo();
        if (allInfo == null || allInfo.length == 0) {
            return null;
        }
        
        // En yakın felaketi döndür (geriye uyumluluk için)
        CountdownInfo nearest = allInfo[0];
        for (CountdownInfo info : allInfo) {
            if (info.remainingTime < nearest.remainingTime) {
                nearest = info;
            }
        }
        
        return new String[]{"Seviye " + nearest.level, formatTime(nearest.remainingTime)};
    }
    
    /**
     * Tüm seviyeler için countdown bilgisini al (3 ayrı sayaç)
     */
    public static class CountdownInfo {
        public final int level;
        public final long remainingTime;
        public final String levelName;
        
        public CountdownInfo(int level, long remainingTime, String levelName) {
            this.level = level;
            this.remainingTime = remainingTime;
            this.levelName = levelName;
        }
    }
    
    /**
     * Tüm seviyeler için countdown bilgisini al (3 ayrı sayaç)
     */
    public CountdownInfo[] getAllCountdownInfo() {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            return null; // Aktif felaket varsa countdown gösterme
        }
        
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
        CountdownInfo[] infos = new CountdownInfo[3];
        
        for (int level = 1; level <= 3; level++) {
            long interval;
            String levelName;
            switch (level) {
                case 1: 
                    interval = LEVEL_1_INTERVAL;
                    levelName = "Günlük";
                    break;
                case 2: 
                    interval = LEVEL_2_INTERVAL;
                    levelName = "3 Günlük";
                    break;
                case 3: 
                    interval = LEVEL_3_INTERVAL;
                    levelName = "Haftalık";
                    break;
                default: 
                    continue;
            }
            
            long remaining = interval - elapsed;
            
            // Eğer süre geçmişse, mod işlemi yap (döngüsel sayaç)
            if (remaining <= 0) {
                remaining = interval - (elapsed % interval);
                // Eğer mod işlemi sonucu 0 ise, interval'i göster
                if (remaining == 0) {
                    remaining = interval;
                }
            }
            
            // Negatif değer kontrolü
            if (remaining < 0 || remaining > interval * 2) {
                remaining = interval;
            }
            
            infos[level - 1] = new CountdownInfo(level, remaining, levelName);
        }
        
        return infos;
    }
    
    /**
     * Zaman formatla (ms -> dd/hh/mm/ss)
     */
    public String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (days > 0) {
            return String.format("%02d/%02d/%02d/%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("00/%02d/%02d/%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("00/00/%02d/%02d", minutes, seconds);
        } else {
            return String.format("00/00/00/%02d", seconds);
        }
    }
    
    /**
     * Seviyeye göre felaket tipi ismi (uyarı için)
     */
    private String getDisasterTypeNameForLevel(int level) {
        switch (level) {
            case 1: return "Günlük";
            case 2: return "Orta";
            case 3: return "Büyük";
            default: return "Bilinmeyen";
        }
    }
    
    /**
     * Felaket ismi (YENİ: DisasterType enum kullanır)
     */
    public String getDisasterDisplayName(DisasterType type) {
        if (type == null) return "Bilinmeyen Felaket";
        switch (type) {
            case CATASTROPHIC_TITAN: return "Felaket Titanı";
            case CATASTROPHIC_ABYSSAL_WORM: return "Felaket Hiçlik Solucanı";
            case CATASTROPHIC_CHAOS_DRAGON: return "Felaket Khaos Ejderi";
            case CATASTROPHIC_VOID_TITAN: return "Felaket Boşluk Titanı";
            case CATASTROPHIC_ICE_LEVIATHAN: return "Felaket Buzul Leviathan";
            case ZOMBIE_HORDE: return "Zombi Ordusu";
            case SKELETON_LEGION: return "İskelet Lejyonu";
            case SPIDER_SWARM: return "Örümcek Sürüsü";
            case CREEPER_SWARM: return "Creeper Dalgası";
            case ZOMBIE_WAVE: return "Zombi Dalgası";
            case SOLAR_FLARE: return "Güneş Patlaması";
            case EARTHQUAKE: return "Deprem";
            case STORM: return "Fırtına";
            case METEOR_SHOWER: return "Meteor Yağmuru";
            case VOLCANIC_ERUPTION: return "Volkanik Patlama";
            case BOSS_BUFF_WAVE: return "Boss Güçlenme Dalgası";
            case MOB_INVASION: return "Mob İstilası";
            case PLAYER_BUFF_WAVE: return "Oyuncu Buff Dalgası";
            default: return "Bilinmeyen Felaket";
        }
    }
    
    /**
     * Felaket ismi (GERİYE UYUMLULUK: Disaster.Type enum kullanır)
     * @deprecated DisasterType kullanın
     */
    @Deprecated
    public String getDisasterDisplayName(Disaster.Type type) {
        if (type == null) return "Bilinmeyen Felaket";
        DisasterType newType = convertToNewType(type);
        if (newType != null) {
            return getDisasterDisplayName(newType);
        }
        return "Bilinmeyen Felaket";
    }
    
    /**
     * Seviyeye göre spawn zamanı kontrolü
     */
    public boolean shouldSpawnDisaster(int level) {
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
        long interval;
        
        switch (level) {
            case 1: interval = LEVEL_1_INTERVAL; break;
            case 2: interval = LEVEL_2_INTERVAL; break;
            case 3: interval = LEVEL_3_INTERVAL; break;
            default: return false;
        }
        
        return elapsed >= interval;
    }
    
    /**
     * Otomatik felaket spawn kontrolü
     */
    public void checkAutoSpawn() {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            // Aktif felaket varsa task'ı durdur (scoreboard yok artık)
            if (countdownUpdateTask != null) {
                countdownUpdateTask.cancel();
                countdownUpdateTask = null;
            }
            return; // Zaten aktif felaket var
        }
        
        // Countdown Scoreboard'ı güncelle
        updateCountdownBossBar();
        
        // Seviye 1 kontrolü (her gün) - Kategori seviyesi 1, iç seviye 2 (orta form)
        if (shouldSpawnDisaster(1)) {
            Disaster.Type[] level1Types = {Disaster.Type.SOLAR_FLARE};
            Disaster.Type randomType = level1Types[new java.util.Random().nextInt(level1Types.length)];
            int categoryLevel = 1;
            int internalLevel = 2; // Orta form (otomatik spawn için)
            triggerDisaster(randomType, categoryLevel, internalLevel);
            return;
        }
        
        // Seviye 2 kontrolü (3 günde bir) - Kategori seviyesi 2, iç seviye 2 (orta form)
        if (shouldSpawnDisaster(2)) {
            Disaster.Type[] level2Types = {Disaster.Type.CATASTROPHIC_ABYSSAL_WORM, Disaster.Type.EARTHQUAKE, Disaster.Type.METEOR_SHOWER};
            Disaster.Type randomType = level2Types[new java.util.Random().nextInt(level2Types.length)];
            int categoryLevel = 2;
            int internalLevel = 2; // Orta form (otomatik spawn için)
            triggerDisaster(randomType, categoryLevel, internalLevel);
            return;
        }
        
        // Seviye 3 kontrolü (7 günde bir) - Kategori seviyesi 3, iç seviye 2 (orta form)
        if (shouldSpawnDisaster(3)) {
            Disaster.Type[] level3Types = {Disaster.Type.CATASTROPHIC_TITAN, Disaster.Type.CATASTROPHIC_CHAOS_DRAGON, 
                                          Disaster.Type.CATASTROPHIC_VOID_TITAN, Disaster.Type.VOLCANIC_ERUPTION};
            Disaster.Type randomType = level3Types[new java.util.Random().nextInt(level3Types.length)];
            int categoryLevel = 3;
            int internalLevel = 2; // Orta form (otomatik spawn için)
            triggerDisaster(randomType, categoryLevel, internalLevel);
            return;
        }
        
        // Mini felaket kontrolü (günde 2-5 kez, rastgele zamanda)
        checkMiniDisasterSpawn();
    }
    
    /**
     * Mini felaket otomatik spawn kontrolü
     * Günde 2-5 kez rastgele zamanda spawn olur
     */
    private void checkMiniDisasterSpawn() {
        long now = System.currentTimeMillis();
        
        // Gün sıfırlama kontrolü (24 saat = 86400000 ms)
        if (now - lastDayReset >= 86400000L) {
            miniDisasterCountToday = 0;
            lastDayReset = now;
        }
        
        // Günde maksimum 5 kez
        if (miniDisasterCountToday >= 5) {
            return;
        }
        
        // Minimum 2 kez spawn olmalı (eğer gün bitiyorsa)
        long timeSinceLastMini = now - lastMiniDisasterTime;
        long timeUntilDayEnd = 86400000L - (now - lastDayReset);
        
        // Eğer gün bitiyorsa ve henüz 2 kez spawn olmadıysa zorla spawn et
        if (timeUntilDayEnd < 3600000L && miniDisasterCountToday < 2) { // Son 1 saat
            spawnRandomMiniDisaster();
            return;
        }
        
        // Rastgele spawn kontrolü (2-6 saat arası rastgele aralık)
        long minInterval = 7200000L;  // 2 saat
        long maxInterval = 21600000L; // 6 saat
        long randomInterval = minInterval + (long)(random.nextDouble() * (maxInterval - minInterval));
        
        if (timeSinceLastMini >= randomInterval) {
            spawnRandomMiniDisaster();
        }
    }
    
    /**
     * Rastgele mini felaket spawn et
     */
    private void spawnRandomMiniDisaster() {
        Disaster.Type[] miniTypes = {
            Disaster.Type.BOSS_BUFF_WAVE,
            Disaster.Type.MOB_INVASION,
            Disaster.Type.PLAYER_BUFF_WAVE
        };
        
        Disaster.Type randomType = miniTypes[random.nextInt(miniTypes.length)];
        int categoryLevel = Disaster.getDefaultLevel(randomType);
        int internalLevel = 2; // Orta form (otomatik spawn için)
        
        // Mini felaketler için özel spawn (entity yok, sadece efektler)
        triggerDisaster(randomType, categoryLevel, internalLevel);
        
        lastMiniDisasterTime = System.currentTimeMillis();
        miniDisasterCountToday++;
        
        org.bukkit.Bukkit.broadcastMessage("§6§l⚡ MİNİ FELAKET: " + getDisasterDisplayName(randomType) + " ⚡");
    }
    
    /**
     * Countdown BossBar'ı güncelle (spawn olacağı zamanı gösterir)
     */
    private void updateCountdownBossBar() {
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
        
        // En yakın spawn zamanını bul
        long nextSpawnTime = Long.MAX_VALUE;
        int nextLevel = 0;
        
        for (int level = 1; level <= 3; level++) {
            long interval;
            switch (level) {
                case 1: interval = LEVEL_1_INTERVAL; break;
                case 2: interval = LEVEL_2_INTERVAL; break;
                case 3: interval = LEVEL_3_INTERVAL; break;
                default: continue;
            }
            
            long remaining = interval - elapsed;
            if (remaining > 0 && remaining < nextSpawnTime) {
                nextSpawnTime = remaining;
                nextLevel = level;
            }
        }
        
        // Eğer hiç spawn zamanı yoksa (hepsi geçmişse), en kısa interval'i kullan
        if (nextSpawnTime == Long.MAX_VALUE || nextSpawnTime <= 0) {
            // En kısa interval'i bul
            long minInterval = Math.min(LEVEL_1_INTERVAL, Math.min(LEVEL_2_INTERVAL, LEVEL_3_INTERVAL));
            long timeSinceLast = elapsed % minInterval;
            nextSpawnTime = minInterval - timeSinceLast;
            nextLevel = 1; // En kısa interval seviye 1
        }
        
        // ✅ FELAKET SAYACI HUD'U KAPALI - Artık sadece bilgi panelinde gösteriliyor
        // Scoreboard oluşturma ve güncelleme kodu kaldırıldı
        // Felaket sayacı artık HUDManager'daki bilgi panelinde gösteriliyor
        
        // Görsel uyarı sistemi ve uyarı mesajları çalışmaya devam ediyor
        // Güncelleme task'ı sadece uyarılar için çalışıyor (scoreboard yok)
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
        }
        
        countdownUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeDisaster != null && !activeDisaster.isDead()) {
                // Aktif felaket varsa task'ı durdur
                if (countdownUpdateTask != null) {
                    countdownUpdateTask.cancel();
                    countdownUpdateTask = null;
                }
                return;
            }
            
            long currentElapsed = System.currentTimeMillis() - lastDisasterTime;
            
            // ✅ Sayaç bitince otomatik spawn kontrolü
            for (int level = 1; level <= 3; level++) {
                long interval;
                switch (level) {
                    case 1: interval = LEVEL_1_INTERVAL; break;
                    case 2: interval = LEVEL_2_INTERVAL; break;
                    case 3: interval = LEVEL_3_INTERVAL; break;
                    default: continue;
                }
                
                long remaining = interval - currentElapsed;
                
                // ✅ Sayaç bitti mi? (0 veya negatif ise)
                if (remaining <= 0) {
                    // Sayaç bitti, otomatik spawn yap
                    if (shouldSpawnDisaster(level)) {
                        checkAutoSpawn(); // Otomatik spawn kontrolü
                        return; // Spawn yapıldı, çık
                    }
                }
                
                // Görsel uyarı sistemi (2 dakika önce)
                if (remaining > 0 && remaining <= WARNING_INTERVAL) {
                    // Rastgele bir felaket tipi seç (gerçek felaket tipi bilinmiyor)
                    Disaster.Type[] allTypes = Disaster.Type.values();
                    if (allTypes.length > 0) {
                        Disaster.Type warningType = allTypes[new Random().nextInt(allTypes.length)];
                        showVisualWarning(warningType, remaining);
                    }
                }
            }
            
            // ✅ En yakın spawn zamanını bul (uyarı sistemi için)
            long currentNextSpawnTime = Long.MAX_VALUE;
            int currentNextLevel = 0;
            
            for (int level = 1; level <= 3; level++) {
                long interval;
                switch (level) {
                    case 1: interval = LEVEL_1_INTERVAL; break;
                    case 2: interval = LEVEL_2_INTERVAL; break;
                    case 3: interval = LEVEL_3_INTERVAL; break;
                    default: continue;
                }
                
                long remaining = interval - currentElapsed;
                if (remaining > 0 && remaining < currentNextSpawnTime) {
                    currentNextSpawnTime = remaining;
                    currentNextLevel = level;
                }
            }
            
            if (currentNextSpawnTime == Long.MAX_VALUE || currentNextSpawnTime <= 0) {
                // En kısa interval'i bul
                long minInterval = Math.min(LEVEL_1_INTERVAL, Math.min(LEVEL_2_INTERVAL, LEVEL_3_INTERVAL));
                long timeSinceLast = currentElapsed % minInterval;
                currentNextSpawnTime = minInterval - timeSinceLast;
                currentNextLevel = 1; // En kısa interval seviye 1
            }
            
            // Plan'a göre: Felaket spawn olmadan 2 dakika önce uyarı
            if (currentNextSpawnTime <= WARNING_INTERVAL && currentNextSpawnTime > (WARNING_INTERVAL - 2000)) {
                // 2 dakika kala uyarı (2 saniye tolerans)
                long now = System.currentTimeMillis();
                if (now - lastWarningTime >= WARNING_INTERVAL) {
                    String disasterTypeName = getDisasterTypeNameForLevel(currentNextLevel);
                    Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                        "⚠ UYARI: " + disasterTypeName + " felaketi 2 dakika içinde başlayacak! ⚠");
                    lastWarningTime = now;
                }
            }
        }, 20L, 20L); // Her saniye güncelle
    }
    
    // Getter/Setter
    public Disaster getActiveDisaster() { return activeDisaster; }
    public void setActiveDisaster(Disaster d) { 
        this.activeDisaster = d;
        if (d == null) {
            if (disasterBossBar != null) {
                disasterBossBar.removeAll();
                disasterBossBar = null;
            }
            if (bossBarUpdateTask != null) {
                bossBarUpdateTask.cancel();
                bossBarUpdateTask = null;
            }
            // Arena transformasyonunu durdur
            if (arenaManager != null && d != null && d.getEntity() != null) {
                arenaManager.stopArenaTransformation(d.getEntity().getUniqueId());
            }
            // Zayıf nokta cooldown'unu temizle
            if (d != null && d.getEntity() != null) {
                weakPointCooldowns.remove(d.getEntity().getUniqueId());
            }
            // Felaket bittiğinde countdown'u tekrar göster
            updateCountdownBossBar();
        }
    }
    
    /**
     * Tüm aktif felaketleri temizle
     */
    public void clearAllDisasters() {
        if (activeDisaster != null) {
            if (activeDisaster.getEntity() != null) {
                activeDisaster.kill();
            }
            activeDisaster = null;
        }
        
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
            disasterBossBar = null;
        }
        
        if (bossBarUpdateTask != null) {
            bossBarUpdateTask.cancel();
            bossBarUpdateTask = null;
        }
        
        // ✅ Countdown Scoreboard kaldırıldı - Artık HUDManager'daki bilgi panelinde gösteriliyor
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
            countdownUpdateTask = null;
        }
        
        Bukkit.broadcastMessage("§a§lTüm felaketler temizlendi!");
    }
    
    // Eski metodlar (geriye dönük uyumluluk)
    public void triggerDisaster(Disaster.Type type) {
        int level = Disaster.getDefaultLevel(type);
        triggerDisaster(type, (int) level);
    }
    
    public void triggerDisaster(Disaster.Type type, org.bukkit.Location spawnLoc) {
        int level = Disaster.getDefaultLevel(type);
        triggerDisaster(type, (int) level, spawnLoc);
    }
    
    /**
     * ✅ Plugin kapatılırken tüm task'ları durdur
     */
    public void shutdown() {
        // BossBar task'ını durdur
        if (bossBarUpdateTask != null) {
            bossBarUpdateTask.cancel();
            bossBarUpdateTask = null;
        }
        
        // Countdown task'ını durdur
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
            countdownUpdateTask = null;
        }
        
        // Weak point task'ını durdur
        if (weakPointTask != null) {
            weakPointTask.cancel();
            weakPointTask = null;
        }
        
        // BossBar'ı temizle
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
            disasterBossBar = null;
        }
        
        // Arena manager'ı durdur
        if (arenaManager != null && activeDisaster != null && activeDisaster.getEntity() != null) {
            arenaManager.stopArenaTransformation(activeDisaster.getEntity().getUniqueId());
        }
    }
    
    /**
     * Yeni oyuncu giriş yaptığında BossBar'a ekle ve cache'i temizle (güç hesaplama için)
     */
    public void onPlayerJoin(Player player) {
        // Cache'i temizle (yeni oyuncu gücü hesaplanacak)
        if (serverPowerCalculator != null) {
            serverPowerCalculator.clearCache();
        }
        if (disasterBossBar != null) {
            disasterBossBar.addPlayer(player);
        }
        // ✅ Countdown Scoreboard kaldırıldı - Artık HUDManager'daki bilgi panelinde gösteriliyor
    }
    
    /**
     * Oyuncu çıkış yaptığında cache'i temizle
     */
    public void onPlayerQuit(Player player) {
        // Cache'i temizle (oyuncu çıktı, güç hesaplaması değişecek)
        if (serverPowerCalculator != null) {
            serverPowerCalculator.clearCache();
        }
    }
    
    // Eski metodlar
    private me.mami.stratocraft.manager.BuffManager buffManager;
    private me.mami.stratocraft.manager.TerritoryManager territoryManager;
    
    public void setBuffManager(me.mami.stratocraft.manager.BuffManager bm) {
        this.buffManager = bm;
    }
    
    public void setTerritoryManager(me.mami.stratocraft.manager.TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    public void dropRewards(Disaster disaster) {
        if (disaster == null) return;
        
        // Entity lokasyonu (grup felaketler için ilk entity veya tek boss için entity)
        org.bukkit.Location loc = null;
        if (disaster.getEntity() != null) {
            loc = disaster.getEntity().getLocation();
        } else if (disaster.getGroupEntities() != null && !disaster.getGroupEntities().isEmpty()) {
            org.bukkit.entity.Entity firstEntity = disaster.getGroupEntities().get(0);
            if (firstEntity != null && !firstEntity.isDead()) {
                loc = firstEntity.getLocation();
            }
        }
        
        if (loc == null) return;
        
        // Enkaz yığını oluştur
        createWreckageStructure(loc);
        
        // 1. ÖLDÜĞÜ YERDE ÖZEL İTEMLER DÜŞÜR (her zaman)
        // Rastgele özel itemler düşür
        if (Math.random() < 0.5) {
            if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
            }
        } else {
            if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
            }
        }
        
        // 2. HASAR BAZLI ÖDÜL DAĞITIMI
        java.util.Map<java.util.UUID, Double> playerDamage = disaster.getPlayerDamage();
        double totalDamage = disaster.getTotalDamage();
        
        if (totalDamage > 0 && !playerDamage.isEmpty()) {
            // Toplam ödül miktarı (felaket seviyesine göre)
            int baseRewardCount = 5 + (disaster.getLevel() * 3); // Seviye 1: 8, Seviye 2: 11, Seviye 3: 14
            
            for (java.util.Map.Entry<java.util.UUID, Double> entry : playerDamage.entrySet()) {
                org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) continue;
                
                double damagePercent = entry.getValue() / totalDamage;
                int rewardCount = (int) Math.max(1, Math.round(baseRewardCount * damagePercent));
                
                // Oyuncuya ödül ver (inventory'sine)
                org.bukkit.Location playerLoc = player.getLocation();
                for (int i = 0; i < rewardCount; i++) {
                    if (Math.random() < 0.5) {
                        if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
                            } else {
                                playerLoc.getWorld().dropItemNaturally(playerLoc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
                            }
                        }
                    } else {
                        if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
                            } else {
                                playerLoc.getWorld().dropItemNaturally(playerLoc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
                            }
                        }
                    }
                }
                
                // Oyuncuya bilgi ver
                player.sendMessage("§a§lFELAKET ÖDÜLÜ!");
                player.sendMessage("§7Verdiğin hasar: §e" + String.format("%.1f", entry.getValue()));
                player.sendMessage("§7Hasar yüzdesi: §e" + String.format("%.1f", damagePercent * 100) + "%");
                player.sendMessage("§7Aldığın ödül: §e" + rewardCount + " item");
            }
        }
        
        // 3. KLAN KRISTALİ KORUNURSA BONUS ÖDÜL
        if (territoryManager != null) {
            Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null && affectedClan.getCrystalEntity() != null && !affectedClan.getCrystalEntity().isDead()) {
                // Kristal korundu - bonus ödül (öldüğü yerde)
                if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                    loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
                }
                if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                    loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
                }
                Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GOLD + "" + org.bukkit.ChatColor.BOLD + 
                    "⭐ BONUS ÖDÜL: " + affectedClan.getName() + " klanının kristali korundu! ⭐");
            }
        }
        
        // 4. KAHRAMAN BUFF'I
        if (territoryManager != null && buffManager != null) {
            Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null) {
                buffManager.applyHeroBuff(affectedClan);
            }
        }
        
        Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GREEN + "" + org.bukkit.ChatColor.BOLD + 
            "Felaket yok edildi! Ödüller dağıtıldı!");
    }
    
    private void createWreckageStructure(org.bukkit.Location center) {
        org.bukkit.Material wreckageMat = org.bukkit.Material.ANCIENT_DEBRIS;
        int surfaceY = center.getWorld().getHighestBlockYAt(center);
        org.bukkit.Location surfaceLoc = center.clone();
        surfaceLoc.setY(surfaceY);
        
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y < 3; y++) {
                    org.bukkit.Location blockLoc = surfaceLoc.clone().add(x, y, z);
                    if (blockLoc.getBlock().getType() == org.bukkit.Material.AIR ||
                            blockLoc.getBlock().getType() == org.bukkit.Material.GRASS_BLOCK ||
                            blockLoc.getBlock().getType() == org.bukkit.Material.TALL_GRASS) {
                        blockLoc.getBlock().setType(wreckageMat);
                    }
                }
            }
        }
        
        if (me.mami.stratocraft.Main.getInstance() != null) {
            me.mami.stratocraft.manager.ScavengerManager sm = 
                ((me.mami.stratocraft.Main) me.mami.stratocraft.Main.getInstance()).getScavengerManager();
            if (sm != null) {
                sm.markWreckage(surfaceLoc, java.util.UUID.randomUUID());
            }
        }
    }
    
    public void forceWormSurface(org.bukkit.Location seismicLocation) {
        Disaster disaster = getActiveDisaster();
        if (disaster == null || disaster.getType() != Disaster.Type.CATASTROPHIC_ABYSSAL_WORM) return;
        
        org.bukkit.entity.Entity worm = disaster.getEntity();
        if (worm == null) return;
        
        org.bukkit.Location surfaceLoc = seismicLocation.clone();
        surfaceLoc.setY(seismicLocation.getWorld().getHighestBlockYAt(seismicLocation) + 1);
        worm.teleport(surfaceLoc);
        org.bukkit.Bukkit.broadcastMessage("§6§lSİSMİK ÇEKİÇ! Hiçlik Solucanı yüzeye çıkmaya zorlandı!");
    }
    
    /**
     * En yakın klan kristalini bul
     */
    public org.bukkit.Location findNearestCrystal(org.bukkit.Location from) {
        if (from == null || clanManager == null) return null;
        
        org.bukkit.Location nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Clan clan : clanManager.getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            org.bukkit.Location crystalLoc = clan.getCrystalLocation();
            if (crystalLoc == null) continue;
            
            // Aynı dünyada mı kontrol et
            if (!crystalLoc.getWorld().equals(from.getWorld())) continue;
            
            double distance = from.distance(crystalLoc);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = crystalLoc;
            }
        }
        
        return nearest;
    }
    
    /**
     * Belirtilen yarıçap içindeki tüm klan kristallerini bul
     * @param from Merkez konum
     * @param radius Yarıçap (blok)
     * @return Yarıçap içindeki kristal lokasyonları listesi (en yakından en uzağa sıralı)
     */
    public java.util.List<org.bukkit.Location> findCrystalsInRadius(org.bukkit.Location from, double radius) {
        if (from == null || clanManager == null) return new java.util.ArrayList<>();
        
        java.util.List<org.bukkit.Location> crystals = new java.util.ArrayList<>();
        
        for (Clan clan : clanManager.getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            org.bukkit.Location crystalLoc = clan.getCrystalLocation();
            if (crystalLoc == null) continue;
            
            // Aynı dünyada mı kontrol et
            if (!crystalLoc.getWorld().equals(from.getWorld())) continue;
            
            double distance = from.distance(crystalLoc);
            if (distance <= radius) {
                crystals.add(crystalLoc);
            }
        }
        
        // Mesafeye göre sırala (en yakından en uzağa)
        crystals.sort((a, b) -> Double.compare(from.distance(a), from.distance(b)));
        
        return crystals;
    }
    
    /**
     * Felaket hedefini belirle (kristal veya merkez)
     */
    public void setDisasterTarget(Disaster disaster) {
        if (disaster == null || disaster.getCategory() != Disaster.Category.CREATURE) return;
        
        org.bukkit.entity.Entity entity = disaster.getEntity();
        if (entity == null && disaster.getGroupEntities().isEmpty()) return;
        
        org.bukkit.Location currentLoc = null;
        if (entity != null) {
            currentLoc = entity.getLocation();
        } else if (!disaster.getGroupEntities().isEmpty()) {
            currentLoc = disaster.getGroupEntities().get(0).getLocation();
        }
        
        if (currentLoc == null) return;
        
        // Önce en yakın kristali bul
        org.bukkit.Location nearestCrystal = findNearestCrystal(currentLoc);
        if (nearestCrystal != null) {
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            return;
        }
        
        // Kristal yoksa merkeze git
        org.bukkit.Location centerLoc = null;
        if (difficultyManager != null) {
            centerLoc = difficultyManager.getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = currentLoc.getWorld().getSpawnLocation();
        }
        disaster.setTarget(centerLoc);
    }
    
    private java.util.Random random = new java.util.Random();
    
    /**
     * Felaket tipine göre EntityType belirle
     */
    private org.bukkit.entity.EntityType getEntityTypeForDisaster(Disaster.Type type) {
        switch (type) {
            case ZOMBIE_HORDE:
            case ZOMBIE_WAVE:
                return org.bukkit.entity.EntityType.ZOMBIE;
            case SKELETON_LEGION:
                return org.bukkit.entity.EntityType.SKELETON;
            case SPIDER_SWARM:
                return org.bukkit.entity.EntityType.SPIDER;
            case CREEPER_SWARM:
                return org.bukkit.entity.EntityType.CREEPER;
            default:
                return null;
        }
    }
    
    /**
     * Grup felaket spawn (30 adet orta güçte) - Config kullanır
     * @param internalLevel Admin komutunda belirtilen iç seviye (1-3) - güç seviyesi
     */
    public void spawnGroupDisaster(org.bukkit.entity.EntityType entityType, int count, org.bukkit.Location spawnLoc, int internalLevel) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            org.bukkit.Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        World world = spawnLoc.getWorld();
        java.util.List<org.bukkit.entity.Entity> entities = new java.util.ArrayList<>();
        
        // EntityType'a göre felaket tipi belirle
        Disaster.Type disasterType = getDisasterTypeFromEntityType(entityType, true);
        // Admin komutundaki internalLevel kullan (güç seviyesi)
        DisasterPower power = calculateDisasterPower(internalLevel);
        
        // Config'den ayarları al (internalLevel kullan)
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            config = configManager.getConfig(disasterType, internalLevel);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        double spawnRadius = config.getSpawnRadius();
        
        // Spawn
        for (int i = 0; i < count; i++) {
            // Config'den spawn radius ile rastgele konum
            org.bukkit.Location entityLoc = spawnLoc.clone().add(
                (random.nextDouble() - 0.5) * spawnRadius * 2,
                0,
                (random.nextDouble() - 0.5) * spawnRadius * 2
            );
            entityLoc.setY(world.getHighestBlockYAt(entityLoc) + 1);
            
            org.bukkit.entity.Entity entity = world.spawnEntity(entityLoc, entityType);
            
            // DisasterUtils ile güçlendirme
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier);
            
            entities.add(entity);
        }
        
        // Disaster oluştur
        org.bukkit.Location targetLoc = findNearestCrystal(spawnLoc);
        if (targetLoc == null) {
            if (difficultyManager != null) {
                targetLoc = difficultyManager.getCenterLocation();
            }
            if (targetLoc == null) {
                targetLoc = world.getSpawnLocation();
            }
        }
        
        Disaster disaster = new Disaster(
            disasterType,
            Disaster.Category.CREATURE,
            internalLevel, // Admin komutundaki iç seviye
            entities.isEmpty() ? null : entities.get(0),
            targetLoc,
            power.health,
            power.damage,
            Disaster.getDefaultDuration(disasterType, Disaster.getDefaultLevel(disasterType))
        );
        
        // Grup entity'lerini ekle
        for (Entity e : entities) {
            disaster.addGroupEntity(e);
        }
        
        activeDisaster = disaster;
        setDisasterTarget(disaster);
        
        org.bukkit.Bukkit.broadcastMessage("§c§l⚠ GRUP FELAKET BAŞLADI! ⚠");
        org.bukkit.Bukkit.broadcastMessage("§4§l" + count + " adet güçlendirilmiş canavar spawn oldu!");
    }
    
    /**
     * EntityType'a göre felaket tipi belirle
     */
    private Disaster.Type getDisasterTypeFromEntityType(org.bukkit.entity.EntityType entityType, boolean isGroup) {
        if (isGroup) {
            // Grup felaketler
            switch (entityType) {
                case ZOMBIE: return Disaster.Type.ZOMBIE_HORDE;
                case SKELETON: return Disaster.Type.SKELETON_LEGION;
                case SPIDER: return Disaster.Type.SPIDER_SWARM;
                default: return Disaster.Type.ZOMBIE_HORDE;
            }
        } else {
            // Mini dalga felaketler
            switch (entityType) {
                case CREEPER: return Disaster.Type.CREEPER_SWARM;
                case ZOMBIE: return Disaster.Type.ZOMBIE_WAVE;
                default: return Disaster.Type.CREEPER_SWARM;
            }
        }
    }
    
    /**
     * Mini felaket dalgası spawn (100-500 adet) - Config kullanır
     * @param internalLevel Admin komutunda belirtilen iç seviye (1-3) - güç seviyesi
     */
    public void spawnSwarmDisaster(org.bukkit.entity.EntityType entityType, int count, org.bukkit.Location spawnLoc, int internalLevel) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            org.bukkit.Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        World world = spawnLoc.getWorld();
        java.util.List<org.bukkit.entity.Entity> entities = new java.util.ArrayList<>();
        
        // EntityType'a göre felaket tipi belirle
        Disaster.Type disasterType = getDisasterTypeFromEntityType(entityType, false);
        // Admin komutundaki internalLevel kullan (güç seviyesi)
        DisasterPower power = calculateDisasterPower(internalLevel);
        
        // Config'den ayarları al (internalLevel kullan)
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            config = configManager.getConfig(disasterType, internalLevel);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        double spawnRadius = config.getSpawnRadius();
        double healthPercentage = config.getHealthPercentage(); // Mini dalga için %20 = 0.2
        
        // Performans kontrolü - max 500
        int actualCount = Math.min(count, 500);
        
        // ✅ PERFORMANS: Spawn işlemini optimize et (batch spawn)
        // Spawn (config'den spawn radius ile)
        for (int i = 0; i < actualCount; i++) {
            // Config'den spawn radius ile rastgele konum
            org.bukkit.Location entityLoc = spawnLoc.clone().add(
                (random.nextDouble() - 0.5) * spawnRadius * 2,
                0,
                (random.nextDouble() - 0.5) * spawnRadius * 2
            );
            entityLoc.setY(world.getHighestBlockYAt(entityLoc) + 1);
            
            org.bukkit.entity.Entity entity = world.spawnEntity(entityLoc, entityType);
            
            // ✅ PERFORMANS: AI'yı devre dışı bırak (sadece velocity ile hareket)
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) entity).setAI(false);
            }
            
            // DisasterUtils ile güçlendirme (healthPercentage ile)
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier * healthPercentage);
            
            entities.add(entity);
        }
        
        // Disaster oluştur
        org.bukkit.Location targetLoc = findNearestCrystal(spawnLoc);
        if (targetLoc == null) {
            if (difficultyManager != null) {
                targetLoc = difficultyManager.getCenterLocation();
            }
            if (targetLoc == null) {
                targetLoc = world.getSpawnLocation();
            }
        }
        
        Disaster disaster = new Disaster(
            disasterType,
            Disaster.Category.CREATURE,
            internalLevel, // Admin komutundaki iç seviye
            entities.isEmpty() ? null : entities.get(0),
            targetLoc,
            power.health * healthPercentage,
            power.damage * healthPercentage,
            Disaster.getDefaultDuration(disasterType, Disaster.getDefaultLevel(disasterType))
        );
        
        // Grup entity'lerini ekle
        for (Entity e : entities) {
            disaster.addGroupEntity(e);
        }
        
        activeDisaster = disaster;
        setDisasterTarget(disaster);
        
        org.bukkit.Bukkit.broadcastMessage("§c§l⚠ MİNİ FELAKET DALGASI BAŞLADI! ⚠");
        org.bukkit.Bukkit.broadcastMessage("§4§l" + actualCount + " adet mini canavar spawn oldu!");
    }
    
    // ========== ZAYIF NOKTA SİSTEMİ ==========
    
    /**
     * Zayıf nokta task'ını başlat
     */
    private void startWeakPointTask() {
        weakPointTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (activeDisaster == null || activeDisaster.isDead()) return;
                
                Entity entity = activeDisaster.getEntity();
                if (entity == null || entity.isDead() || !(entity instanceof org.bukkit.entity.LivingEntity)) {
                    return;
                }
                
                UUID disasterId = entity.getUniqueId();
                long currentTime = System.currentTimeMillis();
                
                // Zayıf nokta cooldown kontrolü
                Long weakPointTime = weakPointCooldowns.get(disasterId);
                if (weakPointTime == null || currentTime > weakPointTime + WEAK_POINT_COOLDOWN) {
                    // Yeni zayıf nokta aktivasyonu
                    activateWeakPoint(activeDisaster, (org.bukkit.entity.LivingEntity) entity);
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // Her 5 saniyede bir kontrol
    }
    
    /**
     * Zayıf noktayı aktif et
     */
    private void activateWeakPoint(Disaster disaster, org.bukkit.entity.LivingEntity entity) {
        UUID disasterId = entity.getUniqueId();
        weakPointCooldowns.put(disasterId, System.currentTimeMillis() + WEAK_POINT_DURATION);
        
        // Görsel gösterge - başın etrafında parlak partiküller
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!weakPointCooldowns.containsKey(disasterId) || 
                    System.currentTimeMillis() > weakPointCooldowns.get(disasterId) ||
                    entity.isDead()) {
                    cancel();
                    return;
                }
                
                org.bukkit.Location headLoc = entity.getLocation().add(0, 2, 0);
                entity.getWorld().spawnParticle(
                    org.bukkit.Particle.END_ROD, 
                    headLoc, 20, 0.3, 0.3, 0.3, 0.1
                );
                entity.getWorld().spawnParticle(
                    org.bukkit.Particle.CRIT_MAGIC, 
                    headLoc, 15, 0.5, 0.5, 0.5, 0.1
                );
                
                // Oyunculara uyarı
                for (Player player : entity.getWorld().getPlayers()) {
                    if (player.getLocation().distance(entity.getLocation()) <= 30) {
                        player.sendActionBar("§e§l⚡ ZAYIF NOKTA AÇIK! BAŞA SALDIR!");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // ✅ OPTİMİZE: Her saniye (20 tick) - performans için
    }
    
    /**
     * Zayıf nokta aktif mi?
     */
    public boolean isWeakPointActive(UUID disasterId) {
        Long until = weakPointCooldowns.get(disasterId);
        return until != null && System.currentTimeMillis() < until;
    }
    
    /**
     * Zayıf nokta çarpanını al
     */
    public double getWeakPointMultiplier(UUID disasterId) {
        if (isWeakPointActive(disasterId)) {
            return 3.0; // 3x hasar
        }
        return 1.0;
    }
    
    // ========== GÖRSEL UYARI SİSTEMİ ==========
    
    /**
     * Felaket öncesi görsel uyarı sistemi (2 dakika önce)
     */
    public void showVisualWarning(Disaster.Type type, long timeUntilDisaster) {
        if (timeUntilDisaster > WARNING_INTERVAL) {
            return; // 2 dakikadan fazla varsa uyarı gösterme
        }
        
        long now = System.currentTimeMillis();
        if (now - lastWarningTime < WARNING_INTERVAL) {
            return; // Son uyarıdan 2 dakika geçmediyse tekrar gösterme
        }
        
        lastWarningTime = now;
        
        // Tüm oyunculara görsel efektler
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            // Partikül efektleri
            org.bukkit.Location loc = player.getLocation();
            player.getWorld().spawnParticle(
                org.bukkit.Particle.VILLAGER_ANGRY,
                loc.add(0, 2, 0),
                50,
                2.0, 2.0, 2.0,
                0.1
            );
            player.getWorld().spawnParticle(
                org.bukkit.Particle.LAVA,
                loc,
                30,
                1.0, 0.5, 1.0,
                0.05
            );
            
            // Ses efektleri
            player.playSound(loc, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.8f);
            player.playSound(loc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 1.2f);
            
            // Ekran titremesi (Title)
            String disasterName = getDisasterDisplayName(type);
            player.sendTitle(
                "§c§l⚠ UYARI ⚠",
                "§e" + disasterName + " §7yaklaşıyor!",
                10, 60, 20
            );
            
            // ActionBar mesajı
            long seconds = timeUntilDisaster / 1000;
            player.sendActionBar("§c§l⚠ FELAKET YAKLAŞIYOR! §e" + seconds + " §7saniye kaldı!");
        }
        
        // Broadcast mesajı
        String disasterName = getDisasterDisplayName(type);
        org.bukkit.Bukkit.broadcastMessage("§c§l⚠ FELAKET UYARISI ⚠");
        org.bukkit.Bukkit.broadcastMessage("§e" + disasterName + " §7yaklaşıyor! Hazırlanın!");
    }
    
    // ========== HELPER METODLAR: Yeni Enum Dönüşümleri ==========
    
    /**
     * DisasterType'ı eski Disaster.Type'a dönüştür (geriye uyumluluk için)
     */
    private Disaster.Type convertToOldType(DisasterType type) {
        if (type == null) return null;
        try {
            return Disaster.Type.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * DisasterCategory'yi eski Disaster.Category'ye dönüştür (geriye uyumluluk için)
     */
    private Disaster.Category convertToOldCategory(DisasterCategory category) {
        if (category == null) return null;
        try {
            return Disaster.Category.valueOf(category.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Eski Disaster.Type'ı yeni DisasterType'a dönüştür
     */
    private DisasterType convertToNewType(Disaster.Type type) {
        if (type == null) return null;
        try {
            return DisasterType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Eski Disaster.Category'yi yeni DisasterCategory'ye dönüştür
     */
    private DisasterCategory convertToNewCategory(Disaster.Category category) {
        if (category == null) return null;
        try {
            return DisasterCategory.valueOf(category.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
