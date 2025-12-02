package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.util.GeometryUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryManager {
    private final ClanManager clanManager;
    
    // Chunk-based cache: O(1) lookup için
    // Key: "chunkX;chunkZ", Value: Clan ID
    private final Map<String, UUID> chunkTerritoryCache = new HashMap<>();
    private boolean isCacheDirty = true; // Event-based cache güncelleme

    public TerritoryManager(ClanManager cm) { 
        this.clanManager = cm;
    }

    public ClanManager getClanManager() { return clanManager; }

    /**
     * Chunk-based cache kullanarak bölge sahibini bul (O(1) lookup)
     */
    public Clan getTerritoryOwner(Location loc) {
        // Sadece veri değiştiyse güncelle (event-based)
        if (isCacheDirty) {
            updateChunkCache();
            isCacheDirty = false;
        }
        
        // Chunk key oluştur
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        String chunkKey = chunkX + ";" + chunkZ;
        
        // Cache'den kontrol et
        UUID clanId = chunkTerritoryCache.get(chunkKey);
        if (clanId != null) {
            Clan clan = clanManager.getAllClans().stream()
                .filter(c -> c.getId().equals(clanId))
                .findFirst().orElse(null);
            
            if (clan != null) {
                Territory t = clan.getTerritory();
                if (t != null && GeometryUtil.isInsideRadius(t.getCenter(), loc, t.getRadius())) {
                    return clan;
                }
            }
        }
        
        // Cache'de yoksa eski yöntemle ara (fallback)
        return getTerritoryOwnerLegacy(loc);
    }
    
    /**
     * Eski yöntem (fallback) - O(N) ama nadiren kullanılır
     */
    private Clan getTerritoryOwnerLegacy(Location loc) {
        for (Clan clan : clanManager.getAllClans()) {
            Territory t = clan.getTerritory();
            if (t == null) continue;

            if (GeometryUtil.isInsideRadius(t.getCenter(), loc, t.getRadius())) {
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

