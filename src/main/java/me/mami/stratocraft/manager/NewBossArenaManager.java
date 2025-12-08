package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yeni Boss Arena Yönetim Sistemi
 * Boss'un etrafında sürekli değişen, dinamik arena
 */
public class NewBossArenaManager {
    private final Main plugin;
    private final Map<UUID, ArenaData> activeArenas;
    
    // Config'den yüklenecek ayarlar (varsayılan değerler)
    private int minArenasPerGroup;
    private int minArenasPerGroupFallback;
    private int baseMaxActiveArenas;
    private long taskInterval;
    private int blocksPerCycle;
    private int hazardCreateInterval;
    private double playerGroupDistance;
    private double playerGroupDistanceFallback;
    private double farDistance;
    private double farDistanceFallback;
    private double farDistanceMin;
    private double arenaExpansionLimit;
    private long groupCacheDuration;
    private double tpsThreshold;
    private int tpsSampleSize;
    
    // Dinamik ayarlar (performans sorunu varsa değişir)
    private int currentArenasPerGroup;
    private double currentPlayerGroupDistance;
    private double currentFarDistance;
    
    // Oyuncu grupları cache (performans optimizasyonu)
    private long lastGroupCalculation = 0;
    private List<List<org.bukkit.entity.Player>> cachedPlayerGroups = null;
    
    // Uzak arena tekrar başlatma sistemi
    private final Set<UUID> stoppedArenas = new HashSet<>(); // Durdurulmuş arenalar
    
    // Performans metrikleri
    private int totalArenasProcessed = 0;
    private int totalArenasStopped = 0;
    private double averageDistance = 0.0;
    private long lastMetricsReset = System.currentTimeMillis();
    
    private BukkitTask centralTask; // Merkezi task (tüm arenaları yönetir)
    
    public NewBossArenaManager(Main plugin) {
        this.plugin = plugin;
        this.activeArenas = new ConcurrentHashMap<>();
        loadConfig(); // Config'den ayarları yükle
        startCentralArenaTask(); // Merkezi task'ı başlat
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfig() {
        me.mami.stratocraft.manager.ConfigManager config = plugin.getConfigManager();
        
        minArenasPerGroup = config.getMinArenasPerGroup();
        minArenasPerGroupFallback = config.getMinArenasPerGroupFallback();
        baseMaxActiveArenas = config.getBaseMaxActiveArenas();
        taskInterval = config.getTaskInterval();
        blocksPerCycle = config.getBlocksPerCycle();
        hazardCreateInterval = config.getHazardCreateInterval();
        playerGroupDistance = config.getPlayerGroupDistance();
        playerGroupDistanceFallback = config.getPlayerGroupDistanceFallback();
        farDistance = config.getFarDistance();
        farDistanceFallback = config.getFarDistanceFallback();
        farDistanceMin = config.getFarDistanceMin();
        arenaExpansionLimit = config.getArenaExpansionLimit();
        groupCacheDuration = config.getGroupCacheDuration();
        tpsThreshold = config.getTpsThreshold();
        tpsSampleSize = config.getTpsSampleSize();
        
        // Dinamik ayarları başlangıç değerlerine ayarla
        currentArenasPerGroup = minArenasPerGroup;
        currentPlayerGroupDistance = playerGroupDistance;
        currentFarDistance = farDistance;
    }
    
    /**
     * Config'i yeniden yükle (reload komutu için)
     */
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Boss Arena ayarları yeniden yüklendi.");
    }
    
    /**
     * Arena transformasyonunu başlat
     * DİNAMİK ÖNCELİK SİSTEMİ: Oyuncu gruplarına göre dinamik limit
     */
    public void startArenaTransformation(Location center, BossManager.BossType bossType, int level, UUID bossId) {
        // Dinamik maksimum arena limitini hesapla
        int maxArenas = calculateMaxActiveArenas();
        
        // Maksimum arena limiti kontrolü
        if (activeArenas.size() >= maxArenas) {
            // Limit dolmuşsa, en uzaktaki bosslardan başlayarak durdur
            freeUpArenaSlot(center);
            
            // Tekrar kontrol et
            maxArenas = calculateMaxActiveArenas();
            if (activeArenas.size() >= maxArenas) {
                plugin.getLogger().warning("Maksimum arena sayısına ulaşıldı (" + maxArenas + "). Yeni arena oluşturulamıyor.");
                return;
            }
        }
        
        // Arena verisi oluştur
        ArenaData arena = new ArenaData(center, bossType, level, bossId);
        activeArenas.put(bossId, arena);
        
        // İlk kuleleri hemen oluştur
        int maxRadius = getArenaRadius(level);
        createBossTowers(arena, maxRadius);
        
        plugin.getLogger().info("Boss arena transformasyonu başlatıldı: " + bossId);
    }
    
    /**
     * Dinamik maksimum arena sayısını hesapla
     * Oyuncu gruplarına göre: grup sayısı * arenasPerGroup
     */
    private int calculateMaxActiveArenas() {
        List<List<org.bukkit.entity.Player>> playerGroups = getPlayerGroups();
        int groupCount = playerGroups.size();
        int maxArenas = Math.max(BASE_MAX_ACTIVE_ARENAS, groupCount * currentArenasPerGroup);
        return maxArenas;
    }
    
    /**
     * Oyuncuları gruplara ayır (yakın oyuncular aynı grup)
     * 50 blok içindeki oyuncular aynı grup (performans sorunu varsa 25 blok)
     * Cache mekanizması ile optimize edilmiş
     */
    public List<List<org.bukkit.entity.Player>> getPlayerGroups() {
        // Cache kontrolü
        long now = System.currentTimeMillis();
        if (cachedPlayerGroups != null && (now - lastGroupCalculation) < groupCacheDuration) {
            return cachedPlayerGroups; // Cache'den dön
        }
        
        // Cache süresi dolmuş veya ilk hesaplama - yeniden hesapla
        List<org.bukkit.entity.Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (allPlayers.isEmpty()) {
            cachedPlayerGroups = new ArrayList<>();
            lastGroupCalculation = now;
            return cachedPlayerGroups;
        }
        
        List<List<org.bukkit.entity.Player>> groups = new ArrayList<>();
        Set<org.bukkit.entity.Player> assigned = new HashSet<>();
        
        for (org.bukkit.entity.Player player : allPlayers) {
            if (player == null || assigned.contains(player)) continue;
            
            // Yeni grup oluştur
            List<org.bukkit.entity.Player> group = new ArrayList<>();
            group.add(player);
            assigned.add(player);
            
            // Bu oyuncuya yakın olanları bul (Union-Find benzeri yaklaşım)
            boolean foundNew = true;
            while (foundNew) {
                foundNew = false;
                for (org.bukkit.entity.Player other : allPlayers) {
                    if (other == null || assigned.contains(other)) continue;
                    if (!player.getWorld().equals(other.getWorld())) continue;
                    
                    // Grup içindeki herhangi bir oyuncuya yakın mı kontrol et
                    for (org.bukkit.entity.Player groupPlayer : group) {
                        double distance = groupPlayer.getLocation().distance(other.getLocation());
                        if (distance <= currentPlayerGroupDistance) {
                            group.add(other);
                            assigned.add(other);
                            foundNew = true;
                            break; // Bu oyuncuyu ekledik, diğerlerine bak
                        }
                    }
                }
            }
            
            groups.add(group);
        }
        
        // Cache'le
        cachedPlayerGroups = groups;
        lastGroupCalculation = now;
        return groups;
    }
    
    /**
     * Arena slot'u boşalt - En uzaktaki bosslardan başlayarak durdur
     * Yeni boss için yer açmak için kullanılır
     */
    private void freeUpArenaSlot(Location newBossLocation) {
        if (activeArenas.isEmpty()) {
            return;
        }
        
        // Tüm oyuncuları al
        List<org.bukkit.entity.Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        // Her arena için yeni boss'a olan mesafeyi hesapla
        List<Map.Entry<UUID, ArenaData>> arenasWithDistance = new ArrayList<>();
        
        for (Map.Entry<UUID, ArenaData> entry : activeArenas.entrySet()) {
            ArenaData arena = entry.getValue();
            Location arenaCenter = arena.getCenter();
            
            // Aynı dünyada mı kontrol et
            if (!arenaCenter.getWorld().equals(newBossLocation.getWorld())) {
                continue;
            }
            
            // Yeni boss'a olan mesafeyi hesapla
            double distanceToNewBoss = arenaCenter.distance(newBossLocation);
            
            // En yakın oyuncu mesafesini de hesapla
            double minPlayerDistance = Double.MAX_VALUE;
            for (org.bukkit.entity.Player player : players) {
                if (!player.getWorld().equals(arenaCenter.getWorld())) continue;
                double dist = player.getLocation().distance(arenaCenter);
                if (dist < minPlayerDistance) {
                    minPlayerDistance = dist;
                }
            }
            
            arenasWithDistance.add(new AbstractMap.SimpleEntry<>(entry.getKey(), arena));
            arena.setNearestPlayerDistance(minPlayerDistance);
        }
        
        // Öncelik sıralaması:
        // 1. En uzaktaki bosslar (yeni boss'a göre)
        // 2. En uzaktaki oyunculara göre (100+ blok)
        arenasWithDistance.sort((e1, e2) -> {
            ArenaData a1 = e1.getValue();
            ArenaData a2 = e2.getValue();
            
            double dist1 = a1.getCenter().distance(newBossLocation);
            double dist2 = a2.getCenter().distance(newBossLocation);
            
            // Önce uzaktaki arenaları önceliklendir (dinamik uzaklık)
            boolean far1 = a1.getNearestPlayerDistance() > currentFarDistance;
            boolean far2 = a2.getNearestPlayerDistance() > currentFarDistance;
            
            if (far1 && !far2) return -1; // far1 önce
            if (!far1 && far2) return 1;  // far2 önce
            
            // İkisi de uzak veya yakın, mesafeye göre sırala (en uzak önce)
            return Double.compare(dist2, dist1);
        });
        
        // En uzaktaki arena'yı durdur
        if (!arenasWithDistance.isEmpty()) {
            UUID farthestBossId = arenasWithDistance.get(0).getKey();
            stopArenaTransformation(farthestBossId);
            plugin.getLogger().info("Uzaktaki boss arena'sı durduruldu (yeni boss için yer açıldı): " + farthestBossId);
        }
    }
    
    /**
     * Merkezi arena task'ı - Tüm arenaları mesafeye göre önceliklendirir
     */
    private void startCentralArenaTask() {
        if (centralTask != null && !centralTask.isCancelled()) {
            return; // Zaten çalışıyor
        }
        
        centralTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeArenas.isEmpty()) {
                    return; // Arena yoksa işlem yapma
                }
                
                // Tüm oyuncuları al
                List<org.bukkit.entity.Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                
                // Her arena için en yakın oyuncu mesafesini hesapla ve önceliklendir
                List<Map.Entry<UUID, ArenaData>> sortedArenas = new ArrayList<>(activeArenas.entrySet());
                
                // Önce mesafeleri hesapla ve sırala
                for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
                    ArenaData arena = entry.getValue();
                    Location center = arena.getCenter();
                    World world = center.getWorld();
                    
                    if (world == null) continue;
                    
                    // En yakın oyuncu mesafesini bul
                    double minDistance = Double.MAX_VALUE;
                    for (org.bukkit.entity.Player player : players) {
                        if (!player.getWorld().equals(world)) continue;
                        double dist = player.getLocation().distance(center);
                        if (dist < minDistance) {
                            minDistance = dist;
                        }
                    }
                    
                    // Arena'ya mesafeyi kaydet
                    arena.setNearestPlayerDistance(minDistance);
                }
                
                // Performans kontrolü ve dinamik ayarlama
                adjustPerformanceSettings();
                
                // Uzaktaki bossların transformasyonunu durdur (dinamik uzaklık)
                // Mesafe zaten hesaplanmış, tekrar hesaplamaya gerek yok
                List<UUID> farArenasToStop = new ArrayList<>();
                for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
                    ArenaData arena = entry.getValue();
                    if (arena.getNearestPlayerDistance() > currentFarDistance) {
                        farArenasToStop.add(entry.getKey());
                    }
                }
                
                // Uzak arenaları durdur
                for (UUID farBossId : farArenasToStop) {
                    stopArenaTransformation(farBossId);
                    // Log spam önleme: Sadece önemli durumlar loglanır
                    if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                        plugin.getLogger().fine(currentFarDistance + "+ blok uzaktaki boss arena'sı durduruldu: " + farBossId);
                    }
                }
                
                // UZAK ARENA TEKRAR BAŞLATMA: Oyuncu yaklaştığında durdurulmuş arenaları tekrar başlat
                checkAndRestartStoppedArenas(players);
                
                // Aktif arenaları tekrar al (uzak olanlar durduruldu)
                // Mesafe zaten hesaplanmış, tekrar hesaplamaya gerek yok
                sortedArenas = new ArrayList<>(activeArenas.entrySet());
                
                // Mesafeye göre sırala (en yakın önce) - Mesafe zaten hesaplanmış
                sortedArenas.sort(Comparator.comparingDouble(e -> e.getValue().getNearestPlayerDistance()));
                
                // Performans metriklerini güncelle
                updateMetrics(sortedArenas);
                
                // Oyuncu gruplarına göre öncelikli arenaları işle
                List<List<org.bukkit.entity.Player>> playerGroups = getPlayerGroups();
                int maxProcessPerCycle = playerGroups.size() * currentArenasPerGroup;
                maxProcessPerCycle = Math.max(maxProcessPerCycle, baseMaxActiveArenas);
                
                int processed = 0;
                for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
                    if (processed >= maxProcessPerCycle) break;
                    
                    UUID bossId = entry.getKey();
                    ArenaData arena = entry.getValue();
                    
                    // Boss kontrolü
                    BossManager.BossData bossData = plugin.getBossManager().getBossData(bossId);
                    if (bossData == null || bossData.getEntity() == null || bossData.getEntity().isDead()) {
                        stopArenaTransformation(bossId);
                        continue;
                    }
                    
                    // Boss'un güncel konumunu al
                    Location bossLoc = bossData.getEntity().getLocation();
                    arena.setCenter(bossLoc);
                    
                    // Arena transformasyonunu gerçekleştir (mesafeye göre önceliklendirilmiş)
                    transformArenaBlocks(arena);
                    
                    processed++;
                    totalArenasProcessed++;
                }
            }
        }.runTaskTimer(plugin, 0L, taskInterval);
    }
    
    /**
     * Durdurulmuş arenaları kontrol et ve oyuncu yaklaştığında tekrar başlat
     */
    private void checkAndRestartStoppedArenas(List<org.bukkit.entity.Player> players) {
        if (stoppedArenas.isEmpty()) {
            return;
        }
        
        List<UUID> toRestart = new ArrayList<>();
        
        for (UUID bossId : stoppedArenas) {
            BossManager.BossData bossData = plugin.getBossManager().getBossData(bossId);
            if (bossData == null || bossData.getEntity() == null || bossData.getEntity().isDead()) {
                // Boss ölmüş, listeden çıkar
                stoppedArenas.remove(bossId);
                continue;
            }
            
            Location bossLoc = bossData.getEntity().getLocation();
            World world = bossLoc.getWorld();
            if (world == null) continue;
            
            // En yakın oyuncu mesafesini bul
            double minDistance = Double.MAX_VALUE;
            for (org.bukkit.entity.Player player : players) {
                if (!player.getWorld().equals(world)) continue;
                double dist = player.getLocation().distance(bossLoc);
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
            
            // Oyuncu yaklaştıysa (currentFarDistance içindeyse) tekrar başlat
            if (minDistance <= currentFarDistance) {
                toRestart.add(bossId);
            }
        }
        
        // Tekrar başlatılacak arenaları işle
        for (UUID bossId : toRestart) {
            BossManager.BossData bossData = plugin.getBossManager().getBossData(bossId);
            if (bossData == null) continue;
            
            Location bossLoc = bossData.getEntity().getLocation();
            startArenaTransformation(bossLoc, bossData.getType(), bossData.getLevel(), bossId);
            stoppedArenas.remove(bossId);
            plugin.getLogger().info("Durdurulmuş boss arena'sı tekrar başlatıldı (oyuncu yaklaştı): " + bossId);
        }
    }
    
    /**
     * Performans metriklerini güncelle
     */
    private void updateMetrics(List<Map.Entry<UUID, ArenaData>> sortedArenas) {
        if (sortedArenas.isEmpty()) {
            averageDistance = 0.0;
            return;
        }
        
        double totalDistance = 0.0;
        for (Map.Entry<UUID, ArenaData> entry : sortedArenas) {
            totalDistance += entry.getValue().getNearestPlayerDistance();
        }
        averageDistance = totalDistance / sortedArenas.size();
    }
    
    /**
     * Performans metriklerini al (admin komutları için)
     */
    public ArenaMetrics getMetrics() {
        return new ArenaMetrics(
            activeArenas.size(),
            stoppedArenas.size(),
            totalArenasProcessed,
            totalArenasStopped,
            averageDistance,
            getCurrentTPS(),
            getPlayerGroups().size(),
            currentArenasPerGroup,
            currentPlayerGroupDistance,
            currentFarDistance,
            System.currentTimeMillis() - lastMetricsReset
        );
    }
    
    /**
     * Performans metriklerini sıfırla
     */
    public void resetMetrics() {
        totalArenasProcessed = 0;
        totalArenasStopped = 0;
        averageDistance = 0.0;
        lastMetricsReset = System.currentTimeMillis();
    }
    
    /**
     * Performans metrikleri veri sınıfı
     */
    public static class ArenaMetrics {
        public final int activeArenas;
        public final int stoppedArenas;
        public final int totalProcessed;
        public final int totalStopped;
        public final double averageDistance;
        public final double currentTPS;
        public final int playerGroups;
        public final int arenasPerGroup;
        public final double playerGroupDistance;
        public final double farDistance;
        public final long metricsUptime;
        
        public ArenaMetrics(int activeArenas, int stoppedArenas, int totalProcessed, int totalStopped,
                          double averageDistance, double currentTPS, int playerGroups, int arenasPerGroup,
                          double playerGroupDistance, double farDistance, long metricsUptime) {
            this.activeArenas = activeArenas;
            this.stoppedArenas = stoppedArenas;
            this.totalProcessed = totalProcessed;
            this.totalStopped = totalStopped;
            this.averageDistance = averageDistance;
            this.currentTPS = currentTPS;
            this.playerGroups = playerGroups;
            this.arenasPerGroup = arenasPerGroup;
            this.playerGroupDistance = playerGroupDistance;
            this.farDistance = farDistance;
            this.metricsUptime = metricsUptime;
        }
    }
    
    /**
     * Arena transformasyonunu durdur
     */
    public void stopArenaTransformation(UUID bossId) {
        activeArenas.remove(bossId);
        stoppedArenas.add(bossId); // Durdurulmuş arenaları takip et
        totalArenasStopped++;
        plugin.getLogger().info("Boss arena transformasyonu durduruldu: " + bossId);
    }
    
    /**
     * Performans ayarlarını dinamik olarak ayarla
     * Performans sorunu varsa ayarları düşür
     */
    private void adjustPerformanceSettings() {
        // TPS kontrolü (config'den eşik değeri)
        double currentTPS = getCurrentTPS();
        boolean performanceIssue = currentTPS < tpsThreshold;
        
        if (performanceIssue) {
            // Performans sorunu var - ayarları düşür
            if (currentArenasPerGroup > minArenasPerGroupFallback) {
                currentArenasPerGroup = minArenasPerGroupFallback;
                plugin.getLogger().warning("Performans sorunu tespit edildi! Arena sayısı oyuncu başına " + 
                    minArenasPerGroupFallback + "'e düşürüldü.");
            }
            
            if (currentPlayerGroupDistance > playerGroupDistanceFallback) {
                currentPlayerGroupDistance = playerGroupDistanceFallback;
                plugin.getLogger().warning("Performans sorunu tespit edildi! Oyuncu grup mesafesi " + 
                    playerGroupDistanceFallback + " bloğa düşürüldü.");
            }
            
            if (currentFarDistance > farDistanceMin) {
                currentFarDistance = farDistanceMin;
                plugin.getLogger().warning("Performans sorunu tespit edildi! Uzaklık limiti " + 
                    farDistanceMin + " bloğa düşürüldü.");
            }
        } else {
            // Performans iyi - normal ayarlara dön
            if (currentArenasPerGroup < minArenasPerGroup) {
                currentArenasPerGroup = minArenasPerGroup;
            }
            
            if (currentPlayerGroupDistance < playerGroupDistance) {
                currentPlayerGroupDistance = playerGroupDistance;
            }
            
            // Uzaklık limiti: yavaşça artır
            if (currentFarDistance < farDistance) {
                // Performans iyi ama hala düşükse, yavaşça artır
                if (currentFarDistance == farDistanceMin && currentTPS > 19.5) {
                    currentFarDistance = farDistanceFallback;
                } else if (currentFarDistance == farDistanceFallback && currentTPS > 19.8) {
                    currentFarDistance = farDistance;
                }
            }
        }
    }
    
    // TPS ölçümü için tick zamanları
    private final List<Long> tickTimes = new ArrayList<>();
    
    /**
     * Mevcut TPS'i hesapla (tick zamanı ölçümü ile)
     */
    private double getCurrentTPS() {
        try {
            long currentTime = System.currentTimeMillis();
            tickTimes.add(currentTime);
            
            // Son N tick'i tut (config'den)
            if (tickTimes.size() > tpsSampleSize) {
                tickTimes.remove(0);
            }
            
            // Yeterli örnek yoksa varsayılan değer döndür
            if (tickTimes.size() < 20) {
                return 20.0;
            }
            
            // İlk ve son tick arasındaki süreyi hesapla
            long timeDiff = tickTimes.get(tickTimes.size() - 1) - tickTimes.get(0);
            if (timeDiff <= 0) {
                return 20.0;
            }
            
            // TPS = (tick sayısı / saniye cinsinden süre)
            double seconds = timeDiff / 1000.0;
            double tps = (tickTimes.size() - 1) / seconds;
            
            // TPS'i 0-20 aralığına sınırla
            return Math.max(0.0, Math.min(20.0, tps));
        } catch (Exception e) {
            return 20.0; // Hata durumunda varsayılan değer
        }
    }
    
    /**
     * Arena bloklarını dönüştür (Sürekli - YAYILMA MEKANİZMASI)
     * Boss'tan başlayarak dışa doğru yayılır
     * PERFORMANS OPTİMİZE EDİLDİ: Oyuncu kontrolü esnetildi
     */
    private void transformArenaBlocks(ArenaData arena) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        // En yakın oyuncu mesafesini al (merkezi task tarafından hesaplanmış)
        double nearestPlayerDistance = arena.getNearestPlayerDistance();
        boolean isNearArena = nearestPlayerDistance <= currentFarDistance; // Dinamik uzaklık içindeyse aktif
        boolean isWithin50Blocks = nearestPlayerDistance <= 50.0; // 50 blok kontrolü (bir kez hesapla)
        
        // UZAK ARENALAR (dinamik uzaklık dışı) İÇİN HİÇBİR ŞEY YAPILMAZ - ERKEN ÇIKIŞ
        if (!isNearArena) {
            return; // Uzak arenalar için hiçbir transformasyon yapılmaz
        }
        
        int maxRadius = getArenaRadius(arena.getLevel());

        // Kuleler sürekli oluşturulur (yayılma gibi) - Her arena için ayrı sayaç
        // Sadece expansion limit içindeki arenalarda oluşturulur
        arena.incrementCycleCount();
        boolean isWithinExpansionLimit = nearestPlayerDistance <= arenaExpansionLimit;
        if (isWithinExpansionLimit) {
            // Task interval = 40 tick (2 saniye), dakikada 1 = 60 saniye = 30 döngü
            if (arena.getCycleCount() % 30 == 0) {
                createBossTowers(arena, maxRadius);
            }
            
            // Rastgele örümcek ağları, lavlar ve sular oluştur
            if (arena.getCycleCount() % HAZARD_CREATE_INTERVAL == 0) {
                createRandomHazards(arena, maxRadius);
            }
        }
        
        // Mevcut genişleme yarıçapını artır
        // Sadece arenaExpansionLimit içindeki arenalarda genişler
        double currentRadius = arena.getCurrentRadius();
        boolean isWithinExpansionLimit = nearestPlayerDistance <= arenaExpansionLimit;
        if (isWithinExpansionLimit && currentRadius < maxRadius) {
            arena.setCurrentRadius(currentRadius + 1.2); // Her 2 saniyede 1.2 blok genişle
        }
        
        // Şu anki radius'ta blokları dönüştür
        // Sadece arenaExpansionLimit içindeki arenalarda blok transformasyonu yapılır
        if (!isWithinExpansionLimit) {
            return; // Expansion limit dışındaki arenalar için hiçbir transformasyon yapılmaz
        }
        
        int transformed = 0;
        int maxAttempts = BLOCKS_PER_CYCLE * 3; // Maksimum deneme sayısı (chunk yüklü değilse atla) - optimize edildi
        
        for (int attempt = 0; attempt < maxAttempts && transformed < BLOCKS_PER_CYCLE; attempt++) {
            // Rastgele açı seç (çember oluşturmak için)
            double angle = Math.random() * 2 * Math.PI;
            
            // Mevcut radius civarında blok seç (yayılma efekti)
            double distance = currentRadius + (Math.random() * 3 - 1.5); // ±1.5 blok tolerans
            if (distance < 3) distance = 3; // Boss'a çok yakın olma
            if (distance > maxRadius) distance = maxRadius; // Max radius'u geçme
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
                continue; // Chunk yüklü değilse atla
            }
            
            // Y koordinatını bul (ZEMİN bloğunun kendisi) - optimize edilmiş
            int y = findGroundLevelOptimized(world, x, center.getBlockY(), z);
            
            if (y == -1) continue; // Zemin bulunamadı
            
            // ZEMİN BLOĞUNU DEĞİŞTİR (hava bloğu değil!)
            Location blockLoc = new Location(world, x, y, z);
            
            // Boss'a çok yakınsa atlama
            if (blockLoc.distance(center) < 3) continue;
            
            // Bloğu dönüştür
            transformSingleBlock(blockLoc, arena);
            transformed++;
        }
    }

    /**
     * Boss'un 50 blok etrafında rastgele örümcek ağları, lavlar ve sular oluşturur.
     * Oyuncunun hareketini zorlaştıran çevresel tehlikeler.
     * PERFORMANS OPTİMİZE EDİLDİ: Daha az tehlike, chunk kontrolü
     * NOT: Oyuncu kontrolü çağıran metod tarafından yapılıyor
     */
    private void createRandomHazards(ArenaData arena, int maxRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        Random random = new Random();
        
        // Her döngüde rastgele sayıda tehlike oluştur (ÇOK ARTTIRILDI)
        int hazardCount = 12 + random.nextInt(8); // 12-19 tehlike (2 KAT ARTTIRILDI - 6-9'dan)
        
        for (int i = 0; i < hazardCount; i++) {
            // Rastgele açı ve mesafe
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 8 + random.nextDouble() * (maxRadius - 8); // Boss'tan en az 8 blok uzakta
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            // Zemin seviyesini bul (optimize edilmiş)
            int groundY = findGroundLevelOptimized(world, x, center.getBlockY(), z);
            if (groundY == -1) continue;
            
            // Boss'a çok yakınsa atlama
            Location hazardLoc = new Location(world, x, groundY, z);
            if (hazardLoc.distance(center) < 5) continue;
            
            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) continue;
            
            // Rastgele tehlike tipi seç - DAHA ÇOK LAV VE ÖRÜMCEK AĞI (ARTTIRILDI)
            double hazardType = random.nextDouble();
            
            if (hazardType < 0.45) {
                // %45 şans: Örümcek ağı (zemin üzerinde veya havada) - ARTTIRILDI
                int webY = groundY + 1 + random.nextInt(5); // Zemin + 1-5 blok yukarıda (artırıldı)
                if (webY < world.getMaxHeight()) {
                    Block webBlock = world.getBlockAt(x, webY, z);
                    if (webBlock.getType() == Material.AIR) {
                        webBlock.setType(Material.COBWEB);
                    }
                }
            } else if (hazardType < 0.85) {
                // %40 şans: Lav (zemin seviyesinde) - ARTTIRILDI
                Block lavaBlock = world.getBlockAt(x, groundY + 1, z);
                if (lavaBlock.getType() == Material.AIR) {
                    // Lava kaynağı oluştur (sadece kaynak, akan lav değil)
                    lavaBlock.setType(Material.LAVA);
                }
            } else {
                // %15 şans: Su (zemin seviyesinde) - ARTTIRILDI
                Block waterBlock = world.getBlockAt(x, groundY + 1, z);
                if (waterBlock.getType() == Material.AIR) {
                    // Su kaynağı oluştur (sadece kaynak, akan su değil)
                    waterBlock.setType(Material.WATER);
                }
            }
        }
    }
    
    /**
     * Boss'un etrafına kuleler oluşturur.
     * - 50 blok yarıçap içinde
     * - Yükseklik: 3, 5, 8, 10
     * - Kalınlık (kare taban): 1, 2, 4, 6
     * - Malzeme boss tipine göre seçilir (ör: Titan Golem demir, Chaos God obsidyen)
     * PERFORMANS OPTİMİZE EDİLDİ: Daha az kule, chunk kontrolü önceden
     */
    private void createBossTowers(ArenaData arena, int arenaRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;

        BossManager.BossType type = arena.getBossType();
        int level = arena.getLevel();

        int maxRadius = Math.min((int)arenaExpansionLimit, arenaRadius);
        if (maxRadius < 8) maxRadius = 8; // minimum mantıklı alan

        Random random = new Random();
        // Sürekli oluşturma için daha az kule (tek seferde yeterli, ama sürekli oluşmaya devam etsin)
        int towerCount = 5 + random.nextInt(5); // 3-5 kule (dakikada 1 kere oluşturulur)

        // ÇEŞİTLENDİRİLMİŞ KULE BOYUTLARI - Daha fazla çeşitlilik
        int[] heights = {2, 3, 4, 5, 6, 7, 8, 10, 12, 15}; // Daha kısa ve uzun kuleler
        int[] widths = {1, 1, 2, 2, 3, 4, 5, 6}; // Tek blok, 2 blok, daha geniş kuleler

        Material towerMaterial = getTowerMaterial(type, level);

        for (int i = 0; i < towerCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 6 + random.nextDouble() * (maxRadius - 10); // boss'tan en az 6 blok uzakta

            int baseX = (int) (center.getX() + distance * Math.cos(angle));
            int baseZ = (int) (center.getZ() + distance * Math.sin(angle));

            // KRİTİK: Chunk yüklü mü kontrol et (performans için - önce chunk kontrolü)
            Chunk chunk = world.getChunkAt(baseX >> 4, baseZ >> 4);
            if (!chunk.isLoaded()) continue; // Chunk yüklü değilse bu kuleyi atla
            
            int baseY = findGroundLevelOptimized(world, baseX, center.getBlockY(), baseZ);
            if (baseY == -1) continue;

            int height = heights[random.nextInt(heights.length)];
            int width = widths[random.nextInt(widths.length)];
            int half = width / 2;

            // Kule bloklarını oluştur
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    for (int dy = 0; dy < height; dy++) {
                        int blockX = baseX + dx;
                        int blockZ = baseZ + dz;
                        int blockY = baseY + dy;
                        
                        // Aynı chunk içinde mi kontrol et (performans)
                        if ((blockX >> 4) != (baseX >> 4) || (blockZ >> 4) != (baseZ >> 4)) {
                            continue; // Farklı chunk'a geçme
                        }
                        
                        Block block = world.getBlockAt(blockX, blockY, blockZ);

                        // Sadece hava veya geçilebilir blokların yerine kule bloğu koy
                        if (!block.getType().isSolid() || block.getType() == Material.AIR) {
                            block.setType(towerMaterial);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Zemin seviyesini bul (ZEMİN BLOĞUNUN KENDİSİNİN Y'sini döndürür, hava bloğunu değil!)
     * OPTİMİZE EDİLDİ: Daha az blok kontrol eder
     */
    private int findGroundLevel(World world, int x, int startY, int z) {
        return findGroundLevelOptimized(world, x, startY, z);
    }
    
    /**
     * Optimize edilmiş zemin bulma (daha az blok kontrol eder)
     */
    private int findGroundLevelOptimized(World world, int x, int startY, int z) {
        // Chunk yüklü mü kontrol et (performans)
        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
        if (!chunk.isLoaded()) {
            return -1; // Chunk yüklü değilse zemin bulunamaz
        }
        
        // Aşağıya doğru tara - ZEMİN BLOĞUNU BUL (daha az aralık)
        int searchRange = 5; // 10'dan 5'e azaltıldı (performans)
        for (int y = startY; y > startY - searchRange && y > world.getMinHeight(); y--) {
            Block currentBlock = world.getBlockAt(x, y, z);
            
            // Bu blok katı mı? (zemin bloğu)
            if (currentBlock.getType().isSolid() && !currentBlock.getType().isAir()) {
                return y; // ZEMİN BLOĞUNUN Y'sini döndür
            }
        }
        
        // Yukarıya doğru tara (daha az aralık)
        for (int y = startY; y < startY + searchRange && y < world.getMaxHeight(); y++) {
            Block currentBlock = world.getBlockAt(x, y, z);
            
            if (currentBlock.getType().isSolid() && !currentBlock.getType().isAir()) {
                return y; // ZEMİN BLOĞUNUN Y'sini döndür
            }
        }
        
        return -1; // Zemin bulunamadı
    }
    
    /**
     * Tek bir bloğu dönüştür (ZEMİN BLOĞUNU DEĞİŞTİRİR - ASLA HAVA BLOĞU YAPMA)
     */
    private void transformSingleBlock(Location location, ArenaData arena) {
        Block block = location.getBlock();
        
        // Chunk yüklü değilse atlama
        if (!block.getChunk().isLoaded()) return;
        
        // Hava bloğu ise atlama (zemin bloğu olmalı)
        if (block.getType() == Material.AIR) return;
        
        // Boss'un çok yakınındaki bloklara hiç dokunma (altını doldurma/kazma tamamen kapalı)
        Location bossLoc = arena.getCenter();
        double horizontalDist = Math.sqrt(
            Math.pow(location.getX() - bossLoc.getX(), 2) + 
            Math.pow(location.getZ() - bossLoc.getZ(), 2)
        );
        if (horizontalDist < 5) {
            return;
        }
        
        // ZEMIN BLOĞUNU DEĞİŞTİR (ASLA HAVA BLOĞU YAPMA!)
        Material oldMaterial = block.getType();
        Material newMaterial = getArenaMaterial(arena.getBossType(), arena.getLevel(), false);
        
        // KRİTİK: Yeni malzeme AIR olmamalı! (çukur oluşmasını engeller)
        if (newMaterial == Material.AIR || !newMaterial.isSolid()) {
            return; // Hava bloğu yapmayı engelle!
        }
        
        // Aynı malzeme ise değiştirme (performans)
        if (oldMaterial == newMaterial) return;
        
        block.setType(newMaterial);
        
        // Efekt ekle (değişim anı)
        if (Math.random() < 0.15) { // %15 şansla
            location.getWorld().spawnParticle(
                Particle.BLOCK_CRACK, 
                location.clone().add(0.5, 1.0, 0.5), 
                15, 
                0.4, 0.4, 0.4, 
                0.1, 
                oldMaterial.createBlockData()
            );
            
            // Ses efekti
            location.getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 0.3f, 0.8f);
        }
    }
    
    /**
     * Arena materyalini al (Boss tipine göre)
     */
    private Material getArenaMaterial(BossManager.BossType bossType, int level, boolean filling) {
        // Doldurma için güvenli malzeme
        if (filling) {
            return Material.STONE;
        }
        
        // Boss tipine göre malzemeler
        List<Material> materials = new ArrayList<>();
        
                switch (bossType) {
            case GOBLIN_KING:
            case ORC_CHIEF:
                // Seviye 1-2: Basit malzemeler
                materials.add(Material.DIRT);
                materials.add(Material.COARSE_DIRT);
                materials.add(Material.GRAVEL);
                materials.add(Material.COBBLESTONE);
                materials.add(Material.STONE);
                break;
                
            case TROLL_KING:
                // Seviye 2: Orman ve taş
                materials.add(Material.GRASS_BLOCK);
                materials.add(Material.PODZOL);
                materials.add(Material.MOSSY_COBBLESTONE);
                materials.add(Material.STONE);
                materials.add(Material.ANDESITE);
                break;
                
            case DRAGON:
                // Seviye 3: Yanık ve kayalık
                materials.add(Material.NETHERRACK);
                materials.add(Material.BLACKSTONE);
                materials.add(Material.BASALT);
                materials.add(Material.MAGMA_BLOCK);
                // KRİTİK: LAVA KALDIRMA (çukur oluşturur!)
                // if (Math.random() < 0.1) materials.add(Material.LAVA); // KALDIRILDI
                break;
                
            case TITAN_GOLEM:
                // Seviye 4: Taş ve maden
                materials.add(Material.STONE);
                materials.add(Material.DIORITE);
                materials.add(Material.GRANITE);
                materials.add(Material.DEEPSLATE);
                materials.add(Material.IRON_BLOCK);
                if (Math.random() < 0.05) materials.add(Material.OBSIDIAN);
                // KRİTİK: LAVA VE MAGMA BLOĞU KALDIRMA (çukur oluşturabilir)
                break;
                
            case VOID_DRAGON:
                // Seviye 5: End malzemeleri
                materials.add(Material.END_STONE);
                materials.add(Material.END_STONE_BRICKS);
                materials.add(Material.PURPUR_BLOCK);
                materials.add(Material.OBSIDIAN);
                if (Math.random() < 0.1) materials.add(Material.CRYING_OBSIDIAN);
                break;
                
            case CHAOS_GOD:
            case CHAOS_TITAN:
                // Seviye 5: Kaos malzemeleri - Obsidyen, örümcek ağı, netherrack
                materials.add(Material.OBSIDIAN);
                materials.add(Material.CRYING_OBSIDIAN);
                materials.add(Material.NETHERRACK);
                materials.add(Material.BLACKSTONE);
                materials.add(Material.BASALT);
                materials.add(Material.COBWEB); // Örümcek ağı
                if (Math.random() < 0.2) materials.add(Material.MAGMA_BLOCK);
                break;
                
            default:
                materials.add(Material.STONE);
        }
        
        // Seviyeye göre ek malzemeler
        if (level >= 3 && Math.random() < 0.15) {
            materials.add(Material.COBWEB); // Örümcek ağı
        }
        
        if (level >= 4 && Math.random() < 0.1) {
            materials.add(Material.MAGMA_BLOCK); // Magma bloğu
        }
        
        if (level >= 5 && Math.random() < 0.05) {
            materials.add(Material.NETHERITE_BLOCK); // Netherite bloğu
        }
        
        return materials.get(new Random().nextInt(materials.size()));
    }

    /**
     * Kule malzemesini boss tipine göre seç.
     */
    private Material getTowerMaterial(BossManager.BossType bossType, int level) {
        switch (bossType) {
            case TITAN_GOLEM:
                return Material.IRON_BLOCK;
            case CHAOS_GOD:
            case CHAOS_TITAN:
                return Material.OBSIDIAN;
            case VOID_DRAGON:
                return Material.END_STONE_BRICKS;
            case DRAGON:
            case HELL_DRAGON:
                return Material.NETHERRACK;
            case HYDRA:
                return Material.PRISMARINE_BRICKS;
            case PHOENIX:
                return Material.MAGMA_BLOCK;
            case GOBLIN_KING:
            case ORC_CHIEF:
            case TROLL_KING:
                return Material.COBBLESTONE;
            default:
                return Material.STONE_BRICKS;
        }
    }
    
    /**
     * Boss'un altını doldurma sistemi.
     *
     * NOT: Daha önce burada boss'un altındaki boşlukları STONE ile dolduruyorduk,
     * bu da boss zıpladıkça yukarı doğru taş kuleler oluşmasına sebep oluyordu.
     * Oyuncu isteği üzerine bu mantık TAMAMEN DEVRE DIŞI bırakıldı.
     */
    private void fillBelowBoss(Location bossLoc) {
        // Bilinçli olarak boş bırakıldı.
    }
    
    /**
     * Arena yarıçapını al
     */
    private int getArenaRadius(int level) {
        switch (level) {
            case 1: return 15;
            case 2: return 20;
            case 3: return 25;
            case 4: return 30;
            case 5: return 35;
            default: return 20;
        }
    }
    
    /**
     * Arena verisi (YAYILMA RADIUS'U EKLENDİ)
     */
    private static class ArenaData {
        private Location center;
        private final BossManager.BossType bossType;
        private final int level;
        private final UUID bossId;
        private double currentRadius; // Mevcut yayılma yarıçapı
        private boolean towersCreated; // Kuleler bir kez oluşturuldu mu?
        private double nearestPlayerDistance = Double.MAX_VALUE; // En yakın oyuncu mesafesi
        private int cycleCount = 0; // Her arena için ayrı döngü sayacı (kule oluşturma için)
        
        public ArenaData(Location center, BossManager.BossType bossType, int level, UUID bossId) {
            this.center = center;
            this.bossType = bossType;
            this.level = level;
            this.bossId = bossId;
            this.currentRadius = 3.0; // Boss'un 3 blok çevresinden başla
            this.towersCreated = false;
            this.cycleCount = 0; // Her arena için ayrı sayaç
        }
        
        public Location getCenter() { return center; }
        public void setCenter(Location newCenter) { 
            // Boss hareket edince radius'u sıfırla (yeni yerden başlat)
            // 5 BLOK hareket = yeni alan oluştur
            if (center.distance(newCenter) > 5) {
                this.currentRadius = 3.0;
            }
            this.center = newCenter; 
        }
        public BossManager.BossType getBossType() { return bossType; }
        public int getLevel() { return level; }
        public UUID getBossId() { return bossId; }
        public double getCurrentRadius() { return currentRadius; }
        public void setCurrentRadius(double radius) { this.currentRadius = radius; }
        public boolean isTowersCreated() { return towersCreated; }
        public void setTowersCreated(boolean towersCreated) { this.towersCreated = towersCreated; }
        public double getNearestPlayerDistance() { return nearestPlayerDistance; }
        public void setNearestPlayerDistance(double distance) { this.nearestPlayerDistance = distance; }
        public int getCycleCount() { return cycleCount; }
        public void incrementCycleCount() { this.cycleCount++; }
    }
}

