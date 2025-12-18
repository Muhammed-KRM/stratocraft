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

import java.util.List;
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
        
        // OPTİMİZE: Sadece klan alanına yakın oyuncular için partikül göster
        int visibleDistance = config.getBoundaryParticleVisibleDistance();
        
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
            
            // OPTİMİZE: Oyuncu klan alanına yakın mı? (mesafe kontrolü)
            double distanceToCenter = player.getLocation().distance(center);
            double maxDistance = visibleDistance + territoryData.getRadius();
            
            if (distanceToCenter > maxDistance) {
                continue; // Çok uzak, partikül gösterme
            }
            
            // Sınır partikülleri göster
            showBoundaryParticles(player, territoryData);
        }
    }
    
    /**
     * Sınır partikülleri göster
     */
    private void showBoundaryParticles(Player player, TerritoryData territoryData) {
        if (player == null || territoryData == null) return;
        
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
        
        // Partikül tipi ve rengi
        Particle particleType = config.getBoundaryParticleType();
        org.bukkit.Color particleColor = config.getBoundaryParticleColor();
        double density = config.getBoundaryParticleDensity();
        double spacing = config.getBoundaryParticleSpacing();
        
        // ✅ YENİ: Y ekseni sınırlarını al
        int minY = territoryData.getMinY() - territoryData.getGroundDepth();
        int maxY = territoryData.getMaxY() + territoryData.getSkyHeight();
        int playerY = playerLoc.getBlockY();
        
        // Oyuncunun Y seviyesini sınırlar içinde tut
        int effectiveY = Math.max(minY, Math.min(maxY, playerY));
        
        // Sınır boyunca partikül göster
        int particleCount = 0;
        int maxParticles = (int) (boundaryLine.size() * density);
        
        for (Location boundaryLoc : boundaryLine) {
            // Mesafe kontrolü
            if (playerLoc.distance(boundaryLoc) > visibleDistance) {
                continue;
            }
            
            // Partikül arası mesafe kontrolü
            if (particleCount % (int) spacing != 0) {
                particleCount++;
                continue;
            }
            
            // ✅ YENİ: Y koordinatını sınırlar içinde ayarla
            // Oyuncunun Y seviyesine göre partikül göster, ama sınırlar içinde kal
            double y = Math.max(minY, Math.min(maxY, effectiveY + (Math.random() * 4 - 2)));
            Location particleLoc = boundaryLoc.clone();
            particleLoc.setY(y);
            
            // Partikül göster
            if (particleType == Particle.REDSTONE) {
                player.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(particleColor, 1.0f));
            } else {
                player.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
            }
            
            particleCount++;
            
            // Maksimum partikül limiti (performans)
            if (particleCount >= maxParticles) {
                break;
            }
        }
    }
}

