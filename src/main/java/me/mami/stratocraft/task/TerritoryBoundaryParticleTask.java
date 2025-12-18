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
        
        int playerCount = 0;
        // ✅ OPTİMİZE: Sadece aynı dünyadaki oyuncuları kontrol et (performans)
        // Tüm online oyuncuları kontrol et
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerCount++;
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
        
        // ✅ OPTİMİZE: Mesafe kontrolü (squared kullan - performans)
        double distanceSquared = playerLoc.distanceSquared(center);
        int visibleDistance = config.getBoundaryParticleVisibleDistance();
        double maxVisibleDistance = visibleDistance + territoryData.getRadius();
        double maxVisibleDistanceSquared = maxVisibleDistance * maxVisibleDistance;
        
        if (distanceSquared > maxVisibleDistanceSquared) {
            return; // Çok uzak
        }
        
        // Sınır çizgisini al
        List<Location> boundaryLine = territoryData.getBoundaryLine();
        if (boundaryLine.isEmpty()) {
            return; // Sınır koordinatları yok
        }
        
        // ✅ OPTİMİZE: Daha seyrek partiküller (performans)
        double spacing = Math.max(config.getBoundaryParticleSpacing(), 15.0); // Minimum 15 blok aralık
        
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
        
        // Sınır boyunca partikül göster
        int particleCount = 0;
        
        // ✅ OPTİMİZE: boundaryLine boyutunu kontrol et (çok büyükse sadece bir kısmını işle)
        int boundarySize = boundaryLine.size();
        int maxBoundaryPoints = config.getMaxParticlesPerPlayer() * 2; // Partikül limitinin 2 katı kadar nokta kontrol et
        
        // ✅ OPTİMİZE: Her X-Z koordinatında, sadece oyuncunun Y seviyesinde partikül göster
        // Sadece ilk maxBoundaryPoints kadar noktayı kontrol et (performans için)
        int pointsToCheck = Math.min(boundarySize, maxBoundaryPoints);
        for (int i = 0; i < pointsToCheck; i++) {
            Location boundaryLoc = boundaryLine.get(i);
            // ✅ YENİ: Config'den mesafe limitini al
            int maxParticleDistance = config.getMaxParticleDistance();
            
            // ✅ OPTİMİZE: 2D mesafe kontrolü (squared kullan - performans)
            double dx = playerLoc.getX() - boundaryLoc.getX();
            double dz = playerLoc.getZ() - boundaryLoc.getZ();
            double distance2DSquared = dx * dx + dz * dz;
            double maxParticleDistanceSquared = maxParticleDistance * maxParticleDistance;
            double visibleDistanceSquared = visibleDistance * visibleDistance;
            
            // ✅ YENİ: maxParticleDistance bloktan uzaktaki sınırları gösterme (performans - squared)
            if (distance2DSquared > maxParticleDistanceSquared) {
                continue; // Çok uzak, bu sınır noktasını atla
            }
            
            // ✅ YENİ: visibleDistance kontrolü (squared - performans)
            if (distance2DSquared > visibleDistanceSquared) {
                continue; // Config'den gelen mesafe limitinden uzak
            }
            
            // ✅ OPTİMİZE: Daha seyrek partiküller (her 15+ blokta bir)
            // Spacing kontrolü: Her N partikülden birini göster
            if (particleCount > 0 && particleCount % (int) spacing != 0) {
                particleCount++;
                continue;
            }
            
            // ✅ OPTİMİZE: Sadece oyuncunun Y seviyesinde ve yakınında partikül göster
            // Oyuncunun Y seviyesine en yakın Y koordinatını bul
            int targetY = playerY;
            if (targetY < minY) targetY = minY;
            if (targetY > maxY) targetY = maxY;
            
            // Sadece birkaç Y seviyesinde partikül göster (oyuncunun seviyesi ±2 blok)
            for (int yOffset = -2; yOffset <= 2; yOffset += 2) {
                int y = targetY + yOffset;
                if (y < minY || y > maxY) continue;
                
                Location particleLoc = boundaryLoc.clone();
                particleLoc.setY(y);
                
                // ✅ OPTİMİZE: 3D mesafe kontrolü (squared kullan - performans)
                // Not: visibleDistanceSquared zaten yukarıda tanımlı (satır 210)
                double distance3DSquared = playerLoc.distanceSquared(particleLoc);
                if (distance3DSquared > visibleDistanceSquared) {
                    continue; // Çok uzak (3D mesafe)
                }
                
                // ✅ OPTİMİZE: Şeffaf, küçük partikül göster (görüşü kapatmayan)
                // END_ROD: Küçük, şeffaf, görüşü kapatmayan
                player.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                
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

