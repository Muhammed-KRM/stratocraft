package me.mami.stratocraft.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.TerritoryBoundaryManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.manager.config.TerritoryConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.territory.TerritoryData;

/**
 * Klan Alanı Sınır Partikül Görev Sistemi
 * 
 * Sürekli çalışan task - klan üyelerine sınır partikülleri gösterir
 */
public class TerritoryBoundaryParticleTask {
    private final Main plugin;
    private final TerritoryManager territoryManager;
    private final TerritoryBoundaryManager boundaryManager;
    private final TerritoryConfig config;
    
    private int taskId = -1;
    
    // ✅ YENİ: Oyuncu bazlı cooldown (performans optimizasyonu)
    private final Map<UUID, Long> playerCooldown = new HashMap<>();
    private static final long PARTICLE_COOLDOWN = 2000L; // 2 saniye (ms)
    
    // ✅ YENİ: Mesafe limitleri (config'den alınacak)
    // Not: MAX_PARTICLES_PER_PLAYER, MAX_TOTAL_DISTANCE, MAX_PARTICLE_DISTANCE artık config'den alınıyor
    
    public TerritoryBoundaryParticleTask(Main plugin, TerritoryManager territoryManager,
                                        TerritoryBoundaryManager boundaryManager,
                                        TerritoryConfig config) {
        this.plugin = plugin;
        this.territoryManager = territoryManager;
        this.boundaryManager = boundaryManager;
        this.config = config;
    }
    
    /**
     * Task'ı başlat
     */
    public void start() {
        if (taskId != -1) {
            stop(); // Zaten çalışıyorsa durdur
        }
        
        if (!config.isBoundaryParticleEnabled()) {
            return; // Partikül kapalı
        }
        
        int interval = config.getBoundaryParticleUpdateInterval();
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::run, 0L, interval);
    }
    
    /**
     * Task'ı durdur
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    /**
     * Task çalışma metodu
     * OPTİMİZE: Sadece klan alanına yakın oyuncular için partikül göster
     */
    private void run() {
        if (!config.isBoundaryParticleEnabled()) {
            return;
        }
        
        // ✅ OPTİMİZE: Sadece aynı dünyadaki oyuncuları kontrol et (performans)
        // Tüm online oyuncuları kontrol et
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            
            // Oyuncunun klanı var mı?
            Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
            if (playerClan == null) continue;
            
            // TerritoryData al
            TerritoryData territoryData = boundaryManager.getTerritoryData(playerClan);
            if (territoryData == null) continue;
            
            Location center = territoryData.getCenter();
            if (center == null || !center.getWorld().equals(player.getWorld())) {
                continue; // Farklı dünya veya center yok
            }
            
            // ✅ OPTİMİZE: Oyuncu klan alanına yakın mı? (mesafe kontrolü - distanceSquared kullan)
            Location playerLoc = player.getLocation();
            if (playerLoc == null || playerLoc.getWorld() == null) continue; // Null kontrolü
            
            double distanceSquared = playerLoc.distanceSquared(center);
            double radius = territoryData.getRadius();
            
            // ✅ YENİ: Config'den mesafe limitini al
            int maxTotalDistance = config.getMaxTotalDistance();
            
            // ✅ YENİ: maxTotalDistance bloktan uzaktaysa hiç partikül gösterme (performans)
            // Oyuncunun sınır çizgisine olan minimum mesafesini hesapla (squared kullan)
            double distanceToCenter = Math.sqrt(distanceSquared);
            double distanceToBoundary = Math.abs(distanceToCenter - radius);
            if (distanceToBoundary > maxTotalDistance) {
                continue; // Çok uzak, hiç partikül gösterme
            }
            
            // ✅ YENİ: Center'a olan mesafe kontrolü (squared - performans)
            double maxDistance = maxTotalDistance + radius;
            double maxDistanceSquared = maxDistance * maxDistance;
            if (distanceSquared > maxDistanceSquared) {
                continue; // Çok uzak, partikül gösterme
            }
            
            // Sınır partikülleri göster
            showBoundaryParticles(player, territoryData);
        }
        
    }
    
    /**
     * Sınır partikülleri göster
     * ✅ OPTİMİZE: Görüşü kapatmayan, performans dostu partiküller
     */
    private void showBoundaryParticles(Player player, TerritoryData territoryData) {
        if (player == null || territoryData == null) {
            return;
        }
        
        // ✅ YENİ: Cooldown kontrolü (performans)
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastTime = playerCooldown.get(playerId);
        if (lastTime != null && (now - lastTime) < PARTICLE_COOLDOWN) {
            return; // Cooldown'da
        }
        playerCooldown.put(playerId, now);
        
        Location playerLoc = player.getLocation();
        Location center = territoryData.getCenter();
        
        // ✅ Null kontrolü
        if (playerLoc == null || center == null || center.getWorld() == null || 
            playerLoc.getWorld() == null || !center.getWorld().equals(playerLoc.getWorld())) {
            return;
        }
        
        // ✅ DÜZELTME: Mesafe kontrolü (squared kullan - performans)
        // Sınıra yaklaşınca partikül gözükmemesi sorununu çözmek için
        // visibleDistance kontrolünü kaldırdık, sadece maxParticleDistance kontrolü yeterli
        double distanceSquared = playerLoc.distanceSquared(center);
        int maxParticleDistance = config.getMaxParticleDistance();
        double radius = territoryData.getRadius();
        double maxVisibleDistance = maxParticleDistance + radius;
        double maxVisibleDistanceSquared = maxVisibleDistance * maxVisibleDistance;
        
        if (distanceSquared > maxVisibleDistanceSquared) {
            return; // Çok uzak
        }
        
        // Sınır çizgisini al
        List<Location> boundaryLine = territoryData.getBoundaryLine();
        
        // ✅ YENİ: BoundaryLine boşsa ama radius varsa, dinamik olarak hesapla
        // Çit olmasa bile sınırlar gösterilmeli
        if (boundaryLine.isEmpty()) {
            // ✅ DÜZELTME: radius zaten yukarıda tanımlı, tekrar tanımlama
            if (radius > 0 && center != null && center.getWorld() != null) {
                // Radius varsa, dinamik olarak sınır çizgisi oluştur
                int particleCount = (int) (radius * 2 * Math.PI / 2.0); // Her 2 blokta bir partikül
                if (particleCount < 8) {
                    particleCount = 8; // En az 8 nokta
                }
                boundaryLine = new java.util.ArrayList<>();
                for (int i = 0; i < particleCount; i++) {
                    double angle = (2 * Math.PI * i) / particleCount;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location boundaryLoc = new Location(center.getWorld(), x, center.getY(), z);
                    boundaryLine.add(boundaryLoc);
                }
            } else {
                return; // Sınır koordinatları yok ve radius da yok
            }
        }
        
        // ✅ OPTİMİZE: Sadece oyuncunun Y seviyesinde ve yakınında partikül göster
        int playerY = playerLoc.getBlockY();
        int yRange = 10; // Oyuncunun Y seviyesinden ±10 blok
        int minY = Math.max(
            territoryData.getMinY() - territoryData.getGroundDepth(),
            playerY - yRange
        );
        int maxY = Math.min(
            territoryData.getMaxY() + territoryData.getSkyHeight(),
            playerY + yRange
        );
        
        // ✅ YENİ: Mesafeye göre yoğunluk ayarlı partikül sistemi
        // Yakınlaştıkça en yakın olduğun yerdeki partiküller atsın, uzak yerdekiler azalsın
        int particleCount = 0;
        
        // ✅ OPTİMİZE: boundaryLine boyutunu kontrol et (çok büyükse sadece bir kısmını işle)
        int boundarySize = boundaryLine.size();
        int maxBoundaryPoints = config.getMaxParticlesPerPlayer() * 3; // Daha fazla nokta kontrol et (mesafeye göre filtreleme için)
        
        // ✅ DÜZELTME: maxParticleDistance zaten yukarıda tanımlı, tekrar tanımlama
        double maxParticleDistanceSquared = maxParticleDistance * maxParticleDistance;
        
        // ✅ YENİ: Mesafeye göre yoğunluk faktörü hesaplama için minimum ve maksimum mesafeler
        double minDensityDistance = 10.0; // 10 blok içinde maksimum yoğunluk
        double maxDensityDistance = maxParticleDistance; // maxParticleDistance'te minimum yoğunluk
        double densityRange = maxDensityDistance - minDensityDistance;
        
        // ✅ DÜZELTME: Oyuncuya en yakın sınır noktalarını önce işle
        // boundaryLine içindeki noktaları oyuncuya göre mesafeye göre sırala
        // Böylece oyuncuya yakın noktalar önce işlenir ve daha fazla partikül gösterilir
        java.util.List<Location> sortedBoundaryLine = new java.util.ArrayList<>(boundaryLine);
        sortedBoundaryLine.sort((loc1, loc2) -> {
            double dist1Squared = playerLoc.distanceSquared(loc1);
            double dist2Squared = playerLoc.distanceSquared(loc2);
            return Double.compare(dist1Squared, dist2Squared);
        });
        
        // ✅ OPTİMİZE: Her X-Z koordinatında, sadece oyuncunun Y seviyesinde partikül göster
        // Sadece ilk maxBoundaryPoints kadar noktayı kontrol et (performans için)
        // Artık oyuncuya en yakın noktalar önce işlenecek
        int pointsToCheck = Math.min(sortedBoundaryLine.size(), maxBoundaryPoints);
        for (int i = 0; i < pointsToCheck; i++) {
            Location boundaryLoc = sortedBoundaryLine.get(i);
            
            // ✅ OPTİMİZE: 2D mesafe kontrolü (squared kullan - performans)
            double dx = playerLoc.getX() - boundaryLoc.getX();
            double dz = playerLoc.getZ() - boundaryLoc.getZ();
            double distance2DSquared = dx * dx + dz * dz;
            
            // ✅ DÜZELTME: visibleDistance kontrolü (squared - performans)
            // Sınıra yaklaşınca partikül gözükmemesi sorununu çözmek için
            // visibleDistance kontrolünü kaldırdık, sadece maxParticleDistance kontrolü yeterli
            if (distance2DSquared > maxParticleDistanceSquared) {
                continue; // Çok uzak, bu sınır noktasını atla
            }
            
            // ✅ DÜZELTME: Mesafeye göre yoğunluk faktörü hesapla (0.0 - 1.0 arası)
            // Yakın = 1.0 (her zaman göster), uzak = 0.1 (nadiren göster)
            double distance2D = Math.sqrt(distance2DSquared);
            double densityFactor;
            if (distance2D <= minDensityDistance) {
                densityFactor = 1.0; // Çok yakın, maksimum yoğunluk
            } else if (distance2D >= maxDensityDistance) {
                densityFactor = 0.1; // Çok uzak, minimum yoğunluk
            } else {
                // Lineer interpolasyon: yakın -> uzak (1.0 -> 0.1)
                double normalizedDistance = (distance2D - minDensityDistance) / densityRange;
                densityFactor = 1.0 - (normalizedDistance * 0.9); // 1.0'dan 0.1'e düş
            }
            
            // ✅ DÜZELTME: Yoğunluk faktörüne göre partikül göster (rastgele kontrol)
            // densityFactor = 1.0 ise %100 göster, 0.1 ise %10 göster
            // Yakın = yüksek densityFactor = daha fazla göster
            // Uzak = düşük densityFactor = daha az göster
            // DÜZELTME: Math.random() < densityFactor kullan (doğru mantık)
            // Math.random() 0.0-1.0 arası (1.0 dahil değil), densityFactor 0.1-1.0 arası
            // densityFactor = 1.0 -> Math.random() < 1.0 her zaman true -> her zaman göster ✓
            // densityFactor = 0.1 -> Math.random() < 0.1 sadece %10 ihtimalle true -> nadiren göster ✓
            // ✅ DÜZELTME: Mantık hatası düzeltildi
            // Math.random() < densityFactor -> göster
            // Math.random() >= densityFactor -> gösterme (continue)
            // densityFactor = 1.0 -> Math.random() < 1.0 her zaman true -> her zaman göster ✓
            // densityFactor = 0.1 -> Math.random() < 0.1 sadece %10 ihtimalle true -> nadiren göster ✓
            // NOT: if (Math.random() >= densityFactor) continue; YANLIŞ çünkü:
            // - densityFactor = 1.0 -> Math.random() >= 1.0 asla true olmaz -> her zaman göster ✓ (doğru)
            // - densityFactor = 0.1 -> Math.random() >= 0.1 çoğu zaman true -> çoğu zaman gösterilmez ✗ (yanlış, tersine çalışıyor)
            // DOĞRUSU: if (Math.random() < densityFactor) göster, else continue
            // Yani: if (Math.random() >= densityFactor) continue; YANLIŞ
            // ÇÖZÜM: if (Math.random() < densityFactor) { /* göster */ } else { continue; }
            // Ama daha basit: if (Math.random() >= densityFactor) continue; yerine
            // if (Math.random() < densityFactor) { /* göster */ } else { continue; }
            // Veya: if (Math.random() >= (1.0 - densityFactor + 0.1)) continue; (karmaşık)
            // EN BASİT ÇÖZÜM: if (Math.random() < densityFactor) kullan
            if (Math.random() < densityFactor) {
                // Göster (yoğunluk faktörüne göre)
            } else {
                continue; // Bu sefer gösterme (yoğunluk faktörüne göre)
            }
            
            // ✅ OPTİMİZE: Sadece oyuncunun Y seviyesinde ve yakınında partikül göster
            // Oyuncunun Y seviyesine en yakın Y koordinatını bul
            int targetY = playerY;
            if (targetY < minY) targetY = minY;
            if (targetY > maxY) targetY = maxY;
            
            // ✅ YENİ: Yoğunluk faktörüne göre Y seviyesi sayısını ayarla
            // Yakın = daha fazla Y seviyesi, uzak = daha az Y seviyesi
            int yLevels = (int) Math.max(1, Math.ceil(densityFactor * 3)); // 1-3 Y seviyesi
            int yStep = yLevels > 1 ? 2 : 1; // 2 blok aralık
            
            // Sadece birkaç Y seviyesinde partikül göster (oyuncunun seviyesi ±yLevels blok)
            for (int yOffset = -yLevels; yOffset <= yLevels; yOffset += yStep) {
                int y = targetY + yOffset;
                if (y < minY || y > maxY) continue;
                
                Location particleLoc = boundaryLoc.clone();
                particleLoc.setY(y);
                
                // ✅ DÜZELTME: 3D mesafe kontrolü (squared kullan - performans)
                // Sınıra yaklaşınca partikül gözükmemesi sorununu çözmek için
                // visibleDistance kontrolünü kaldırdık, sadece maxParticleDistance kontrolü yeterli
                double distance3DSquared = playerLoc.distanceSquared(particleLoc);
                double maxParticleDistance3DSquared = maxParticleDistance * maxParticleDistance;
                if (distance3DSquared > maxParticleDistance3DSquared) {
                    continue; // Çok uzak (3D mesafe)
                }
                
                // ✅ YENİ: Yoğunluk faktörüne göre partikül sayısını ayarla
                // Yakın = daha fazla partikül, uzak = daha az partikül
                int particleAmount = (int) Math.max(1, Math.ceil(densityFactor * 2)); // 1-2 partikül
                
                // ✅ OPTİMİZE: Şeffaf, küçük partikül göster (görüşü kapatmayan)
                // Config'den partikül tipini al
                Particle particleType = config.getBoundaryParticleType();
                org.bukkit.Color particleColor = config.getBoundaryParticleColor();
                
                if (particleType == Particle.REDSTONE && particleColor != null) {
                    // REDSTONE partikülü için renk ayarla
                    org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(
                        particleColor, 1.0f);
                    player.spawnParticle(particleType, particleLoc, particleAmount, 0, 0, 0, 0, dustOptions);
                } else {
                    // Diğer partikül tipleri için normal spawn
                    player.spawnParticle(particleType, particleLoc, particleAmount, 0, 0, 0, 0);
                }
                
                particleCount++;
                
                // ✅ OPTİMİZE: Config'den maksimum partikül limitini al
                int maxParticlesPerPlayer = config.getMaxParticlesPerPlayer();
                if (particleCount >= maxParticlesPerPlayer) {
                    return; // Limit aşıldı
                }
            }
        }
        
        // ✅ YENİ: ActionBar ile sınır bilgisi (görüşü kapatmayan alternatif)
        if (particleCount > 0) {
            double distanceToCenter = Math.sqrt(playerLoc.distanceSquared(center));
            double distanceToBoundary = Math.abs(distanceToCenter - territoryData.getRadius());
            if (distanceToBoundary <= 5) {
                // Sınırın 5 blok yakınındaysa ActionBar'da bilgi göster
                // ✅ OPTİMİZE: Adventure API kullan (Paper 1.16+ - deprecated değil)
                try {
                    // Modern Adventure API (deprecated değil)
                    net.kyori.adventure.text.Component component = 
                        net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                            .legacySection().deserialize("§a§lKlan Sınırına Yakınsınız");
                    player.sendActionBar(component);
                } catch (NoClassDefFoundError | NoSuchMethodError | Exception e) {
                    // Eski versiyonlarda Adventure API yoksa normal mesaj gönder (fallback)
                    // Not: sendActionBar(String) deprecated ama çalışır, bu yüzden kullanmıyoruz
                    player.sendMessage("§a§lKlan Sınırına Yakınsınız");
                }
            }
        }
        
    }
}

