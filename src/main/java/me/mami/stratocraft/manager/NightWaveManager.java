package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;

/**
 * Gece Saldırı Dalgası Yöneticisi
 * Her gün gece yarısında boss ve özel mobların klanlara saldırmasını yönetir
 * 
 * Minecraft gün döngüsü:
 * - Bir gün = 24000 tick (20 dakika)
 * - Gece yarısı = 18000 tick (15 dakika sonra)
 * - Güneş doğuşu = 0 tick (20 dakika sonra)
 * - Gece süresi = 6000 tick (5 dakika)
 */
public class NightWaveManager {
    private final Main plugin;
    private final TerritoryManager territoryManager;
    private final MobManager mobManager;
    private final BossManager bossManager;
    
    // Aktif dalga takibi (world -> isActive)
    private final Map<World, Boolean> activeWaves = new HashMap<>();
    
    // Spawn edilen moblar (dalga sonunda temizlemek için)
    private final Map<World, List<LivingEntity>> waveEntities = new HashMap<>();
    
    // Spawn zamanlayıcıları
    private final Map<World, BukkitRunnable> spawnTasks = new HashMap<>();
    
    // Dalga kontrol zamanlayıcısı
    private BukkitRunnable waveCheckTask;
    
    // ✅ CONFIG: Ayarlar
    private boolean enabled = true;
    private long startTime = 18000L; // Gece yarısı
    private long endTime = 0L; // Güneş doğuşu
    private long checkInterval = 100L; // 5 saniye
    private long spawnIntervalInitial = 200L; // 10 saniye
    private long spawnIntervalFast = 100L; // 5 saniye
    private long speedIncreaseTime = 1200L; // 1 dakika
    private double spawnDistance = 50.0; // Blok
    private double bossSpawnChance = 0.2; // %20
    private double wildCreeperSpawnChance = 0.3; // %30
    private double specialMobSpawnChance = 0.5; // %50
    private int wildCreeperCountMin = 3;
    private int wildCreeperCountMax = 7;
    
    public NightWaveManager(Main plugin, TerritoryManager territoryManager, 
                          MobManager mobManager, BossManager bossManager) {
        this.plugin = plugin;
        this.territoryManager = territoryManager;
        this.mobManager = mobManager;
        this.bossManager = bossManager;
        
        // ✅ CONFIG: Ayarları yükle
        loadConfig();
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfig() {
        if (plugin == null || plugin.getConfigManager() == null || 
            plugin.getConfigManager().getConfig() == null) {
            plugin.getLogger().warning("[NightWaveManager] Config bulunamadı, varsayılan değerler kullanılıyor");
            return;
        }
        
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig();
        
        enabled = config.getBoolean("night-wave.enabled", true);
        startTime = config.getLong("night-wave.start-time", 18000L);
        endTime = config.getLong("night-wave.end-time", 0L);
        checkInterval = config.getLong("night-wave.check-interval", 100L);
        spawnIntervalInitial = config.getLong("night-wave.spawn-interval-initial", 200L);
        spawnIntervalFast = config.getLong("night-wave.spawn-interval-fast", 100L);
        speedIncreaseTime = config.getLong("night-wave.speed-increase-time", 1200L);
        spawnDistance = config.getDouble("night-wave.spawn-distance", 50.0);
        bossSpawnChance = config.getDouble("night-wave.boss-spawn-chance", 0.2);
        wildCreeperSpawnChance = config.getDouble("night-wave.wild-creeper-spawn-chance", 0.3);
        specialMobSpawnChance = config.getDouble("night-wave.special-mob-spawn-chance", 0.5);
        wildCreeperCountMin = config.getInt("night-wave.wild-creeper-count-min", 3);
        wildCreeperCountMax = config.getInt("night-wave.wild-creeper-count-max", 7);
        
        plugin.getLogger().info("[NightWaveManager] Config yüklendi: enabled=" + enabled + 
            ", startTime=" + startTime + ", endTime=" + endTime);
    }
    
    /**
     * Yöneticiyi başlat
     */
    public void start() {
        if (!enabled) {
            plugin.getLogger().info("[NightWaveManager] Devre dışı, başlatılmıyor");
            return;
        }
        
        plugin.getLogger().info("[NightWaveManager] Başlatılıyor...");
        
        // ✅ CONFIG: Config'den check interval kullan
        waveCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAndStartWaves();
            }
        };
        waveCheckTask.runTaskTimer(plugin, 0L, checkInterval);
        
        plugin.getLogger().info("[NightWaveManager] Başlatıldı! (checkInterval: " + checkInterval + " tick)");
    }
    
    /**
     * Yöneticiyi durdur
     */
    public void stop() {
        plugin.getLogger().info("[NightWaveManager] Durduruluyor...");
        
        if (waveCheckTask != null) {
            waveCheckTask.cancel();
        }
        
        // Tüm aktif dalgaları durdur
        for (World world : new ArrayList<>(activeWaves.keySet())) {
            stopWave(world);
        }
        
        plugin.getLogger().info("[NightWaveManager] Durduruldu!");
    }
    
    /**
     * Gece kontrolü yap ve dalga başlat
     */
    private void checkAndStartWaves() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != org.bukkit.World.Environment.NORMAL) {
                continue; // Sadece normal dünya
            }
            
            long time = world.getTime();
            // ✅ CONFIG: Config'den zamanları kullan
            boolean isNight = time >= startTime || time < endTime;
            
            // Gece yarısı kontrolü (startTime ± 100 tick tolerans)
            if (isNight && !activeWaves.getOrDefault(world, false)) {
                // Gece yarısına yakın mı? (startTime ± 100)
                if (time >= (startTime - 100) && time <= (startTime + 100)) {
                    startWave(world);
                }
            }
            
            // Güneş doğuşu kontrolü (endTime ± 100 tick tolerans)
            if (!isNight && activeWaves.getOrDefault(world, false)) {
                // Güneş doğdu mu? (endTime ± 100)
                if (time >= (endTime - 100) && time <= (endTime + 100)) {
                    stopWave(world);
                }
            }
        }
    }
    
    /**
     * Saldırı dalgasını başlat
     */
    private void startWave(World world) {
        if (activeWaves.getOrDefault(world, false)) {
            return; // Zaten aktif
        }
        
        plugin.getLogger().info("[NightWaveManager] Gece saldırı dalgası başlatılıyor: " + world.getName());
        activeWaves.put(world, true);
        waveEntities.put(world, new ArrayList<>());
        
        // Tüm klanlara bildirim
        Bukkit.broadcastMessage("§c§l⚠ GECE SALDIRI DALGASI BAŞLADI! ⚠");
        Bukkit.broadcastMessage("§7Bosslar ve özel moblar klanlara saldırıyor!");
        
        // Her klan için spawn başlat
        startSpawnForAllClans(world);
    }
    
    /**
     * Saldırı dalgasını durdur
     */
    private void stopWave(World world) {
        if (!activeWaves.getOrDefault(world, false)) {
            return; // Zaten durdurulmuş
        }
        
        plugin.getLogger().info("[NightWaveManager] Gece saldırı dalgası durduruluyor: " + world.getName());
        activeWaves.put(world, false);
        
        // Spawn görevini durdur
        BukkitRunnable spawnTask = spawnTasks.remove(world);
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        
        // Tüm bildirim
        Bukkit.broadcastMessage("§a§l✓ GECE SALDIRI DALGASI BİTTİ!");
        Bukkit.broadcastMessage("§7Güneş doğdu, moblar geri çekiliyor...");
        
        // Spawn edilen mobları temizleme (opsiyonel - moblar ölebilir)
        // waveEntities.get(world) listesi sadece takip için, temizleme gerekmez
    }
    
    /**
     * Tüm klanlar için spawn başlat
     */
    private void startSpawnForAllClans(World world) {
        // ✅ OPTİMİZE: Thread-safe için final list oluştur
        final List<Clan> allClans = new ArrayList<>(territoryManager.getClanManager().getAllClans());
        
        for (Clan clan : allClans) {
            if (clan.getCrystalLocation() == null || 
                !clan.getCrystalLocation().getWorld().equals(world)) {
                continue; // Bu dünyada değil
            }
            
            startSpawnForClan(clan);
        }
        
        // ✅ CONFIG: Config'den spawn interval'ları kullan
        BukkitRunnable spawnTask = new BukkitRunnable() {
            private long waveTick = 0;
            
            @Override
            public void run() {
                // Dalga aktif mi?
                if (!activeWaves.getOrDefault(world, false)) {
                    cancel();
                    return;
                }
                
                waveTick += spawnIntervalInitial; // İlk interval
                
                // ✅ OPTİMİZE: Her seferinde güncel klan listesini al (thread-safe)
                List<Clan> currentClans = new ArrayList<>(territoryManager.getClanManager().getAllClans());
                
                // Her spawn interval'da yeni spawn
                for (Clan clan : currentClans) {
                    if (clan.getCrystalLocation() == null || 
                        !clan.getCrystalLocation().getWorld().equals(world)) {
                        continue;
                    }
                    
                    // ✅ CONFIG: Spawn hızı artışı: İlk speedIncreaseTime normal, sonra hızlanır
                    long spawnInterval = waveTick < speedIncreaseTime ? spawnIntervalInitial : spawnIntervalFast;
                    if (waveTick % spawnInterval == 0) {
                        spawnMobsForClan(clan);
                    }
                }
            }
        };
        
        spawnTask.runTaskTimer(plugin, 0L, spawnIntervalInitial);
        spawnTasks.put(world, spawnTask);
    }
    
    /**
     * Bir klan için spawn başlat
     */
    private void startSpawnForClan(Clan clan) {
        Location crystalLoc = clan.getCrystalLocation();
        if (crystalLoc == null) return;
        
        // İlk spawn (hemen)
        spawnMobsForClan(clan);
    }
    
    /**
     * Bir klan için mob spawn et
     * ✅ DÜZELTME: Her dalgada en az bir boss garantisi ve klan yaşına göre güç
     */
    private void spawnMobsForClan(Clan clan) {
        Location crystalLoc = clan.getCrystalLocation();
        if (crystalLoc == null) return;
        
        World world = crystalLoc.getWorld();
        if (world == null) return;
        
        // Klan sınırından 50 blok ötede spawn noktası bul
        Location spawnLoc = findSpawnLocation(clan, crystalLoc);
        if (spawnLoc == null) {
            plugin.getLogger().warning("[NightWaveManager] Spawn konumu bulunamadı: " + clan.getName());
            return;
        }
        
        // ✅ YENİ: Klan yaşına göre spawn sayısı artır
        long clanAge = System.currentTimeMillis() - clan.getCreatedAt();
        long daysSinceCreation = clanAge / (24 * 60 * 60 * 1000L);
        
        // Her 7 günde bir ekstra spawn (max 3 ekstra)
        int extraSpawns = Math.min(3, (int)(daysSinceCreation / 7));
        int totalSpawns = 1 + extraSpawns; // En az 1 spawn
        
        Random random = new Random();
        
        // ✅ YENİ: İlk spawn her zaman boss (garanti)
        boolean bossSpawned = false;
        if (bossManager != null) {
            spawnBossForClan(clan, spawnLoc);
            bossSpawned = true;
        }
        
        // ✅ YENİ: Ekstra spawnlar (klan yaşına göre)
        for (int i = 0; i < extraSpawns; i++) {
            double chance = random.nextDouble();
            
            if (chance < bossSpawnChance && bossManager != null) {
                // Boss spawn
                spawnBossForClan(clan, spawnLoc);
            } else if (chance < (bossSpawnChance + wildCreeperSpawnChance)) {
                // Vahşi creeper spawn
                spawnWildCreeperForClan(clan, spawnLoc);
            } else {
                // Özel mob spawn
                spawnSpecialMobForClan(clan, spawnLoc);
            }
        }
        
        // ✅ YENİ: Eğer boss spawn edilmediyse ve bossManager varsa, bir tane daha dene
        if (!bossSpawned && bossManager != null && random.nextDouble() < 0.5) {
            spawnBossForClan(clan, spawnLoc);
        }
    }
    
    /**
     * Spawn konumu bul (klan sınırından 50 blok ötede)
     */
    private Location findSpawnLocation(Clan clan, Location crystalLoc) {
        // Klan sınırını al
        me.mami.stratocraft.model.Territory territory = clan.getTerritory();
        if (territory == null) return null;
        
        double radius = territory.getRadius();
        Location center = territory.getCenter();
        
        // Rastgele açı seç (0-360 derece)
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        
        // ✅ CONFIG: Config'den spawn distance kullan
        double distance = radius + spawnDistance;
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        
        // Y yüksekliğini bul
        int y = crystalLoc.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
        
        Location spawnLoc = new Location(crystalLoc.getWorld(), x, y, z);
        
        // Güvenli konum kontrolü
        if (spawnLoc.getBlock().getType() != org.bukkit.Material.AIR ||
            spawnLoc.clone().add(0, -1, 0).getBlock().getType() == org.bukkit.Material.AIR) {
            // Güvenli değilse, biraz daha uzakta dene
            distance += 20.0;
            x = center.getX() + Math.cos(angle) * distance;
            z = center.getZ() + Math.sin(angle) * distance;
            y = crystalLoc.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
            spawnLoc = new Location(crystalLoc.getWorld(), x, y, z);
        }
        
        return spawnLoc;
    }
    
    /**
     * Boss spawn et
     * ✅ DÜZELTME: bossManager null kontrolü ve klan yaşına göre güç sistemi
     */
    private void spawnBossForClan(Clan clan, Location spawnLoc) {
        // ✅ DÜZELTME: bossManager null kontrolü
        if (bossManager == null) {
            plugin.getLogger().warning("[NightWaveManager] BossManager null! Boss spawn edilemedi: " + clan.getName());
            return;
        }
        
        Random random = new Random();
        
        // ✅ YENİ: Klan yaşına göre boss seviyesi hesapla
        long clanAge = System.currentTimeMillis() - clan.getCreatedAt();
        long daysSinceCreation = clanAge / (24 * 60 * 60 * 1000L); // Gün cinsinden
        
        // Klan yaşına göre boss seviyesi: Her 7 günde bir seviye artar (max 5)
        int baseBossLevel = Math.min(5, 1 + (int)(daysSinceCreation / 7));
        
        // Rastgele boss seviyesi (baseLevel ± 1, min 1, max 5)
        int bossLevel = Math.max(1, Math.min(5, baseBossLevel + random.nextInt(3) - 1));
        
        // ✅ YENİ: Klan yaşına göre boss tipi seçimi
        me.mami.stratocraft.manager.BossManager.BossType[] bossTypes;
        if (daysSinceCreation < 7) {
            // İlk hafta: Sadece basit bosslar
            bossTypes = new me.mami.stratocraft.manager.BossManager.BossType[]{
                me.mami.stratocraft.manager.BossManager.BossType.GOBLIN_KING,
                me.mami.stratocraft.manager.BossManager.BossType.ORC_CHIEF
            };
        } else if (daysSinceCreation < 14) {
            // İkinci hafta: Orta seviye bosslar
            bossTypes = new me.mami.stratocraft.manager.BossManager.BossType[]{
                me.mami.stratocraft.manager.BossManager.BossType.ORC_CHIEF,
                me.mami.stratocraft.manager.BossManager.BossType.TROLL_KING
            };
        } else {
            // 2 haftadan sonra: Tüm bosslar
            bossTypes = new me.mami.stratocraft.manager.BossManager.BossType[]{
                me.mami.stratocraft.manager.BossManager.BossType.ORC_CHIEF,
                me.mami.stratocraft.manager.BossManager.BossType.TROLL_KING,
                me.mami.stratocraft.manager.BossManager.BossType.GOBLIN_KING
            };
        }
        
        me.mami.stratocraft.manager.BossManager.BossType bossType = 
            bossTypes[random.nextInt(bossTypes.length)];
        
        // Boss spawn et
        boolean spawned = bossManager.spawnBossFromRitual(spawnLoc, bossType, null);
        if (spawned) {
            // Spawn edilen boss entity'sini bul (spawn location'ına yakın)
            org.bukkit.entity.LivingEntity boss = findSpawnedBoss(spawnLoc, bossType);
            if (boss != null) {
                // AI'yı klan saldırısına ayarla
                me.mami.stratocraft.util.MobClanAttackAI.attachAI(boss, clan, plugin);
                
                // Listeye ekle
                World world = spawnLoc.getWorld();
                if (world != null) {
                    waveEntities.get(world).add(boss);
                }
                
                plugin.getLogger().info("[NightWaveManager] Boss spawn edildi: " + bossType.name() + 
                    " (Level: " + bossLevel + ", Klan Yaşı: " + daysSinceCreation + " gün) - Klan: " + clan.getName());
            }
        }
    }
    
    /**
     * Vahşi Creeper spawn et
     * ✅ DÜZELTME: Spawn mesafesi artırıldı - birbirlerini patlatmayı önlemek için
     */
    private void spawnWildCreeperForClan(Clan clan, Location spawnLoc) {
        // ✅ CONFIG: Config'den count değerlerini kullan
        Random random = new Random();
        int count = wildCreeperCountMin + random.nextInt(wildCreeperCountMax - wildCreeperCountMin + 1);
        
        // ✅ DÜZELTME: Spawn mesafesi artırıldı (10 -> 20 blok) - birbirlerini patlatmayı önlemek için
        double spawnRadius = 20.0; // 20 blok yarıçap
        
        for (int i = 0; i < count; i++) {
            // Rastgele açı ve mesafe (daha dağınık spawn)
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 5.0 + random.nextDouble() * spawnRadius; // 5-25 blok arası
            
            Location creeperLoc = spawnLoc.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );
            creeperLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(
                creeperLoc.getBlockX(), creeperLoc.getBlockZ()) + 1);
            
            // Vahşi Creeper spawn et
            me.mami.stratocraft.entity.WildCreeper.spawnWildCreeper(creeperLoc, clan, plugin);
        }
        
        plugin.getLogger().info("[NightWaveManager] " + count + " Vahşi Creeper spawn edildi - Klan: " + clan.getName());
    }
    
    /**
     * Özel mob spawn et
     */
    private void spawnSpecialMobForClan(Clan clan, Location spawnLoc) {
        Random random = new Random();
        
        // Rastgele özel mob tipi
        String[] mobTypes = {"ork", "skeleton_knight", "troll", "goblin", "werewolf"};
        String mobType = mobTypes[random.nextInt(mobTypes.length)];
        
        org.bukkit.entity.LivingEntity mob = null;
        String expectedName = null;
        
        // Spawn öncesi entity sayısını al (yeni spawn edileni bulmak için)
        int entityCountBefore = spawnLoc.getWorld().getEntities().size();
        
        switch (mobType) {
            case "ork":
                expectedName = "Ork";
                mobManager.spawnOrk(spawnLoc);
                break;
            case "skeleton_knight":
                expectedName = "İskelet";
                mobManager.spawnSkeletonKnight(spawnLoc);
                break;
            case "troll":
                expectedName = "Troll";
                mobManager.spawnTroll(spawnLoc);
                break;
            case "goblin":
                expectedName = "Goblin";
                mobManager.spawnGoblin(spawnLoc);
                break;
            case "werewolf":
                expectedName = "Kurt";
                mobManager.spawnWerewolf(spawnLoc);
                break;
        }
        
        // ✅ OPTİMİZE: Spawn edilen entity'yi bul (spawnLoc etrafında, custom name'e göre)
        // SpawnLoc'a en yakın, beklenen isme sahip entity'yi bul
        double minDistance = Double.MAX_VALUE;
        for (org.bukkit.entity.Entity nearby : spawnLoc.getWorld().getNearbyEntities(spawnLoc, 5, 5, 5)) {
            if (nearby instanceof org.bukkit.entity.LivingEntity) {
                String customName = nearby.getCustomName();
                if (customName != null && expectedName != null && customName.contains(expectedName)) {
                    double distance = spawnLoc.distance(nearby.getLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        mob = (org.bukkit.entity.LivingEntity) nearby;
                    }
                }
            }
        }
        
        if (mob != null) {
            // AI'yı klan saldırısına ayarla
            me.mami.stratocraft.util.MobClanAttackAI.attachAI(mob, clan, plugin);
            
            // Listeye ekle
            World world = spawnLoc.getWorld();
            if (world != null) {
                waveEntities.get(world).add(mob);
            }
            
            plugin.getLogger().info("[NightWaveManager] Özel mob spawn edildi: " + mobType + 
                " - Klan: " + clan.getName());
        } else {
            plugin.getLogger().warning("[NightWaveManager] Spawn edilen mob bulunamadı: " + mobType);
        }
    }
    
    /**
     * Spawn edilen boss entity'sini bul
     */
    private org.bukkit.entity.LivingEntity findSpawnedBoss(Location spawnLoc, 
                                                           me.mami.stratocraft.manager.BossManager.BossType bossType) {
        if (spawnLoc == null || spawnLoc.getWorld() == null) {
            return null;
        }
        
        // Boss tipine göre beklenen entity type ve name
        org.bukkit.entity.EntityType expectedType = getBossEntityType(bossType);
        String expectedName = getBossExpectedName(bossType);
        
        if (expectedType == null || expectedName == null) {
            return null;
        }
        
        // Spawn location'ına yakın entity'leri kontrol et (10 blok yarıçap - spawn gecikmesi için)
        org.bukkit.entity.LivingEntity foundBoss = null;
        double minDistance = Double.MAX_VALUE;
        
        for (org.bukkit.entity.Entity nearby : spawnLoc.getWorld().getNearbyEntities(spawnLoc, 10, 10, 10)) {
            if (nearby instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) nearby;
                
                // Entity type kontrolü
                if (living.getType() != expectedType) {
                    continue;
                }
                
                // Custom name kontrolü (boss name'ini içermeli)
                String customName = living.getCustomName();
                if (customName != null && customName.contains(expectedName)) {
                    // En yakın boss'u bul
                    double distance = spawnLoc.distance(living.getLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        foundBoss = living;
                    }
                }
            }
        }
        
        return foundBoss;
    }
    
    /**
     * Boss tipine göre entity type'ı döndür
     */
    private org.bukkit.entity.EntityType getBossEntityType(me.mami.stratocraft.manager.BossManager.BossType bossType) {
        switch (bossType) {
            case ORC_CHIEF:
                return org.bukkit.entity.EntityType.ZOMBIE;
            case TROLL_KING:
                return org.bukkit.entity.EntityType.ZOMBIE;
            case GOBLIN_KING:
                return org.bukkit.entity.EntityType.ZOMBIE;
            default:
                return null;
        }
    }
    
    /**
     * Boss tipine göre beklenen custom name'i döndür (renk kodları olmadan)
     */
    private String getBossExpectedName(me.mami.stratocraft.manager.BossManager.BossType bossType) {
        switch (bossType) {
            case GOBLIN_KING:
                return "GOBLIN KRALI";
            case ORC_CHIEF:
                return "ORK ŞEFİ";
            case TROLL_KING:
                return "TROLL KRALI";
            default:
                return null;
        }
    }
    
    /**
     * Dalga aktif mi?
     */
    public boolean isWaveActive(World world) {
        return activeWaves.getOrDefault(world, false);
    }
}

