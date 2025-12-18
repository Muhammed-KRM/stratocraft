package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.TerritoryBoundaryManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.manager.config.TerritoryConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.territory.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            
            // ✅ OPTİMİZE: Oyuncu klan alanına yakın mı? (mesafe kontrolü)
            double distanceToCenter = player.getLocation().distance(center);
            double radius = territoryData.getRadius();
            
            // ✅ YENİ: Config'den mesafe limitini al
            int maxTotalDistance = config.getMaxTotalDistance();
            
            // ✅ YENİ: maxTotalDistance bloktan uzaktaysa hiç partikül gösterme (performans)
            // Oyuncunun sınır çizgisine olan minimum mesafesini hesapla
            double distanceToBoundary = Math.abs(distanceToCenter - radius);
            if (distanceToBoundary > maxTotalDistance) {
                continue; // Çok uzak, hiç partikül gösterme
            }
            
            // ✅ YENİ: Center'a olan mesafe kontrolü (eski mantık - geriye uyumluluk)
            // Not: distanceToBoundary kontrolü zaten yapıldı, bu ek bir güvenlik kontrolü
            double maxDistance = maxTotalDistance + radius;
            if (distanceToCenter > maxDistance) {
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
        if (player == null || territoryData == null) return;
        
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
        
        if (center == null || !center.getWorld().equals(playerLoc.getWorld())) {
            return;
        }
        
        // Mesafe kontrolü (performans)
        double distanceToCenter = playerLoc.distance(center);
        int visibleDistance = config.getBoundaryParticleVisibleDistance();
        
        if (distanceToCenter > visibleDistance + territoryData.getRadius()) {
            return; // Çok uzak
        }
        
        // Sınır çizgisini al
        List<Location> boundaryLine = territoryData.getBoundaryLine();
        if (boundaryLine.isEmpty()) {
            return; // Sınır koordinatları yok
        }
        
        // ✅ OPTİMİZE: Daha şeffaf, görüşü kapatmayan partikül tipi
        // END_ROD: Küçük, şeffaf, görüşü kapatmayan
        // ENCHANT: Büyü efekti, şeffaf
        Particle particleType = Particle.END_ROD; // Varsayılan: Şeffaf, küçük partikül
        org.bukkit.Color particleColor = config.getBoundaryParticleColor();
        
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
            
            // ✅ YENİ: 2D mesafe kontrolü (config'den gelen limit - performans optimizasyonu)
            double distance2D = Math.sqrt(
                Math.pow(playerLoc.getX() - boundaryLoc.getX(), 2) +
                Math.pow(playerLoc.getZ() - boundaryLoc.getZ(), 2)
            );
            
            // ✅ YENİ: maxParticleDistance bloktan uzaktaki sınırları gösterme (performans)
            if (distance2D > maxParticleDistance) {
                continue; // Çok uzak, bu sınır noktasını atla
            }
            
            // ✅ YENİ: visibleDistance kontrolü (config'den gelen değer, daha esnek)
            if (distance2D > visibleDistance) {
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
                
                // 3D mesafe kontrolü (performans)
                double distance3D = playerLoc.distance(particleLoc);
                if (distance3D > visibleDistance) {
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
            double distanceToBoundary = Math.abs(distanceToCenter - territoryData.getRadius());
            if (distanceToBoundary <= 5) {
                // Sınırın 5 blok yakınındaysa ActionBar'da bilgi göster
                try {
                    player.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent("§a§lKlan Sınırına Yakınsınız")
                    );
                } catch (Exception e) {
                    // Spigot API yoksa normal mesaj gönder (fallback)
                    player.sendMessage("§a§lKlan Sınırına Yakınsınız");
                }
            }
        }
    }
}

