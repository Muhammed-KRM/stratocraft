package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.model.territory.TerritoryData;
import me.mami.stratocraft.util.GeometryUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryManager {
    private final ClanManager clanManager;
    
    // ✅ YENİ: TerritoryBoundaryManager referansı (Y ekseni kontrolü için)
    private TerritoryBoundaryManager boundaryManager;
    
    // Chunk-based cache: O(1) lookup için
    // Key: "chunkX;chunkZ", Value: Clan ID
    private final Map<String, UUID> chunkTerritoryCache = new HashMap<>();
    private boolean isCacheDirty = true; // Event-based cache güncelleme

    public TerritoryManager(ClanManager cm) { 
        this.clanManager = cm;
    }

    public ClanManager getClanManager() { return clanManager; }
    
    /**
     * ✅ YENİ: TerritoryBoundaryManager setter (Main.java'da çağrılacak)
     */
    public void setBoundaryManager(TerritoryBoundaryManager boundaryManager) {
        this.boundaryManager = boundaryManager;
    }

    /**
     * Chunk-based cache kullanarak bölge sahibini bul (O(1) lookup)
     * ✅ YENİ: Y ekseni kontrolü eklendi (TerritoryData.isInsideTerritory() kullanılıyor)
     */
    public Clan getTerritoryOwner(Location loc) {
        if (loc == null) return null;
        
        // ✅ YENİ: Önce TerritoryData.isInsideTerritory() ile kontrol et (Y ekseni dahil)
        // ✅ OPTİMİZE: Chunk cache'den önce kontrol et (cache'de varsa hemen return)
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        String chunkKey = chunkX + ";" + chunkZ;
        
        // ✅ OPTİMİZE: Önce chunk cache'den kontrol et
        UUID cachedClanId = chunkTerritoryCache.get(chunkKey);
        if (cachedClanId != null) {
            Clan cachedClan = clanManager.getAllClans().stream()
                .filter(c -> c.getId().equals(cachedClanId))
                .findFirst().orElse(null);
            
            if (cachedClan != null && boundaryManager != null) {
                TerritoryData data = boundaryManager.getTerritoryData(cachedClan);
                if (data != null && data.isInsideTerritory(loc)) {
                    return cachedClan; // ✅ Cache'den bulundu, hemen return
                }
            }
        }
        
        // Cache'de yoksa veya geçersizse tüm klanları kontrol et
        if (boundaryManager != null) {
            for (Clan clan : clanManager.getAllClans()) {
                TerritoryData data = boundaryManager.getTerritoryData(clan);
                if (data != null && data.isInsideTerritory(loc)) {
                    // Cache'e ekle (performans için)
                    chunkTerritoryCache.put(chunkKey, clan.getId());
                    return clan;
                }
            }
        }
        
        // Fallback: Eski yöntem (geriye uyumluluk için)
        // Sadece veri değiştiyse güncelle (event-based)
        if (isCacheDirty) {
            updateChunkCache();
            isCacheDirty = false;
        }
        
        // ✅ OPTİMİZE: Chunk key zaten yukarıda oluşturuldu, tekrar oluşturma
        // Cache'den kontrol et (2D kontrol - geriye uyumluluk)
        UUID clanId = chunkTerritoryCache.get(chunkKey);
        if (clanId != null) {
            Clan clan = clanManager.getAllClans().stream()
                .filter(c -> c.getId().equals(clanId))
                .findFirst().orElse(null);
            
            if (clan != null) {
                Territory t = clan.getTerritory();
                if (t != null && GeometryUtil.isInsideRadius(t.getCenter(), loc, t.getRadius())) {
                    // ✅ YENİ: Y ekseni kontrolü ekle (TerritoryData varsa)
                    if (boundaryManager != null) {
                        TerritoryData data = boundaryManager.getTerritoryData(clan);
                        if (data != null && !data.isInsideTerritory(loc)) {
                            return null; // Y ekseni dışında
                        }
                    }
                    return clan;
                }
            }
        }
        
        // Cache'de yoksa eski yöntemle ara (fallback)
        return getTerritoryOwnerLegacy(loc);
    }
    
    /**
     * Eski yöntem (fallback) - O(N) ama nadiren kullanılır
     * ✅ YENİ: Y ekseni kontrolü eklendi
     */
    private Clan getTerritoryOwnerLegacy(Location loc) {
        for (Clan clan : clanManager.getAllClans()) {
            Territory t = clan.getTerritory();
            if (t == null) continue;

            // ✅ YENİ: Önce TerritoryData.isInsideTerritory() ile kontrol et (Y ekseni dahil)
            if (boundaryManager != null) {
                TerritoryData data = boundaryManager.getTerritoryData(clan);
                if (data != null && data.isInsideTerritory(loc)) {
                    // Cache'e ekle
                    int chunkX = loc.getBlockX() >> 4;
                    int chunkZ = loc.getBlockZ() >> 4;
                    String chunkKey = chunkX + ";" + chunkZ;
                    chunkTerritoryCache.put(chunkKey, clan.getId());
                    return clan;
                }
            }
            
            // Fallback: Eski 2D kontrol (geriye uyumluluk)
            if (GeometryUtil.isInsideRadius(t.getCenter(), loc, t.getRadius())) {
                // ✅ YENİ: Y ekseni kontrolü ekle (TerritoryData varsa)
                if (boundaryManager != null) {
                    TerritoryData data = boundaryManager.getTerritoryData(clan);
                    if (data != null && !data.isInsideTerritory(loc)) {
                        continue; // Y ekseni dışında, sonraki klana bak
                    }
                }
                
                // Cache'e ekle
                int chunkX = loc.getBlockX() >> 4;
                int chunkZ = loc.getBlockZ() >> 4;
                String chunkKey = chunkX + ";" + chunkZ;
                chunkTerritoryCache.put(chunkKey, clan.getId());
                return clan;
            }
        }
        return null;
    }
    
    /**
     * Chunk cache'i güncelle
     */
    private void updateChunkCache() {
        chunkTerritoryCache.clear();
        
        for (Clan clan : clanManager.getAllClans()) {
            Territory t = clan.getTerritory();
            if (t == null || t.getCenter() == null) continue;
            
            int radius = t.getRadius();
            Location center = t.getCenter();
            
            // Bölgenin kapsadığı chunk'ları hesapla
            int minChunkX = (center.getBlockX() - radius) >> 4;
            int maxChunkX = (center.getBlockX() + radius) >> 4;
            int minChunkZ = (center.getBlockZ() - radius) >> 4;
            int maxChunkZ = (center.getBlockZ() + radius) >> 4;
            
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    String chunkKey = chunkX + ";" + chunkZ;
                    // Chunk'ın merkezi bölge içinde mi kontrol et
                    Location chunkCenter = new Location(center.getWorld(), 
                        (chunkX << 4) + 8, center.getY(), (chunkZ << 4) + 8);
                    if (GeometryUtil.isInsideRadius(center, chunkCenter, radius)) {
                        chunkTerritoryCache.put(chunkKey, clan.getId());
                    }
                }
            }
        }
    }
    
    /**
     * Bölge değiştiğinde cache'i işaretle (event-based güncelleme)
     */
    public void setCacheDirty() {
        this.isCacheDirty = true;
    }
    
    /**
     * Bölge değiştiğinde cache'i temizle (eski metod - geriye uyumluluk)
     */
    public void invalidateCache() {
        setCacheDirty();
    }

    public boolean isSafeZone(UUID playerId, Location loc) {
        Clan owner = getTerritoryOwner(loc);
        Clan playerClan = clanManager.getClanByPlayer(playerId);
        return owner != null && playerClan != null && owner.equals(playerClan);
    }
}

