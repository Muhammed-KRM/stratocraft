package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.config.TerritoryConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.territory.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Klan Alanı Sınır Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - TerritoryData yönetimi
 * - Çit lokasyonları yönetimi
 * - Sınır koordinatları hesaplama
 * - Y yüksekliği kontrolü
 */
public class TerritoryBoundaryManager {
    private final Main plugin;
    private final TerritoryManager territoryManager;
    private final TerritoryConfig config;
    
    // TerritoryData cache: Clan ID -> TerritoryData
    private final Map<UUID, TerritoryData> territoryDataMap = new ConcurrentHashMap<>();
    
    public TerritoryBoundaryManager(Main plugin, TerritoryManager territoryManager, TerritoryConfig config) {
        this.plugin = plugin;
        this.territoryManager = territoryManager;
        this.config = config;
    }
    
    /**
     * Klan için TerritoryData al (yoksa oluştur)
     * DÜZELTME: Null check ve Territory kontrolü
     */
    public TerritoryData getTerritoryData(Clan clan) {
        if (clan == null) return null;
        
        TerritoryData existing = territoryDataMap.get(clan.getId());
        if (existing != null) {
            return existing;
        }
        
        // Mevcut Territory'den TerritoryData oluştur
        me.mami.stratocraft.model.Territory territory = clan.getTerritory();
        if (territory == null || territory.getCenter() == null) {
            return null; // Territory yok, TerritoryData oluşturulamaz
        }
        
        TerritoryData data = new TerritoryData(clan.getId(), territory.getCenter());
        data.setRadius(territory.getRadius());
        if (config != null) {
            data.setSkyHeight(config.getSkyHeight());
            data.setGroundDepth(config.getGroundDepth());
        }
        
        territoryDataMap.put(clan.getId(), data);
        return data;
    }
    
    /**
     * TerritoryData'yı kaydet
     */
    public void setTerritoryData(Clan clan, TerritoryData data) {
        if (clan == null || data == null) return;
        territoryDataMap.put(clan.getId(), data);
    }
    
    /**
     * Çit lokasyonu ekle
     * DÜZELTME: Dünya kontrolü eklendi
     */
    public void addFenceLocation(Clan clan, Location fenceLoc) {
        if (clan == null || fenceLoc == null || fenceLoc.getWorld() == null) return;
        
        TerritoryData data = getTerritoryData(clan);
        if (data != null) {
            // Dünya kontrolü: Center ile aynı dünyada olmalı
            Location center = data.getCenter();
            if (center != null && !center.getWorld().equals(fenceLoc.getWorld())) {
                return; // Farklı dünya, ekleme
            }
            
            data.addFenceLocation(fenceLoc);
            
            // ✅ YENİ: Y ekseni sınırlarını güncelle
            data.updateYBounds();
            
            // Config'den kontrol: Çit yerleştirildiğinde yeniden hesapla
            if (config != null && config.isRecalculateOnFencePlace()) {
                if (config.isAsyncBoundaryCalculation()) {
                    calculateBoundariesAsync(clan, data, null);
                } else {
                    calculateBoundaries(clan, data);
                }
            }
        }
    }
    
    /**
     * Çit lokasyonu kaldır
     * DÜZELTME: Dünya kontrolü eklendi
     */
    public void removeFenceLocation(Clan clan, Location fenceLoc) {
        if (clan == null || fenceLoc == null || fenceLoc.getWorld() == null) return;
        
        TerritoryData data = getTerritoryData(clan);
        if (data != null) {
            // Dünya kontrolü: Center ile aynı dünyada olmalı
            Location center = data.getCenter();
            if (center != null && !center.getWorld().equals(fenceLoc.getWorld())) {
                return; // Farklı dünya, kaldırma
            }
            
            data.removeFenceLocation(fenceLoc);
            
            // ✅ YENİ: Y ekseni sınırlarını güncelle
            data.updateYBounds();
            
            // Config'den kontrol: Çit kırıldığında yeniden hesapla
            if (config != null && config.isRecalculateOnFenceBreak()) {
                if (config.isAsyncBoundaryCalculation()) {
                    calculateBoundariesAsync(clan, data, null);
                } else {
                    calculateBoundaries(clan, data);
                }
            }
        }
    }
    
    /**
     * Sınır koordinatlarını hesapla (sync)
     */
    public void calculateBoundaries(Clan clan, TerritoryData data) {
        if (clan == null || data == null) return;
        
        // Basit sınır hesaplama: Çitler arası çizgi
        // Gerçek hesaplama flood-fill ile yapılabilir
        data.calculateBoundaries();
        
        // Cache'i güncelle
        territoryManager.setCacheDirty();
    }
    
    /**
     * Sınır koordinatlarını hesapla (async)
     */
    public void calculateBoundariesAsync(Clan clan, TerritoryData data, Consumer<TerritoryData> callback) {
        if (clan == null || data == null) return;
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Async hesaplama
            data.calculateBoundaries();
            
            // Main thread'e geri dön
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (callback != null) {
                    callback.accept(data);
                }
                territoryManager.setCacheDirty();
            });
        });
    }
    
    /**
     * Flood-fill ile alan hesapla (çitler arası)
     */
    public int calculateAreaFromFences(Location center, List<Location> fenceLocations) {
        if (center == null || fenceLocations == null || fenceLocations.isEmpty()) {
            return 0;
        }
        
        // Basit alan hesaplama: Çitler arası mesafe
        // Gerçek hesaplama flood-fill ile yapılabilir
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        
        Block centerBlock = center.getBlock();
        queue.add(centerBlock);
        visited.add(centerBlock);
        
        int area = 0;
        int maxIterations = 10000; // Anti-infinite loop
        
        while (!queue.isEmpty() && area < maxIterations) {
            Block current = queue.poll();
            area++;
            
            // ✅ YENİ: 3D flood-fill (6 yöne bak)
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST),
                current.getRelative(BlockFace.UP),    // ✅ Y ekseni eklendi
                current.getRelative(BlockFace.DOWN)   // ✅ Y ekseni eklendi
            };
            
            for (Block neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                
                // ✅ YENİ: Yükseklik toleransı kontrolü
                int heightDiff = Math.abs(neighbor.getY() - centerY);
                if (heightDiff > heightTolerance) {
                    visited.add(neighbor); // Ziyaret edildi olarak işaretle
                    continue; // Tolerans dışında, atla
                }
                
                // Çit kontrolü
                boolean isFence = false;
                for (Location fenceLoc : fenceLocations) {
                    if (neighbor.getLocation().equals(fenceLoc)) {
                        isFence = true;
                        break;
                    }
                }
                
                if (isFence) continue; // Sınır
                
                // Hava veya geçilebilir blok
                if (neighbor.getType().isAir() || neighbor.getType().isSolid()) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return area;
    }
    
    /**
     * Çitler bağlantılı mı kontrol et
     */
    public boolean areFencesConnected(List<Location> fenceLocations, int maxDistance) {
        if (fenceLocations == null || fenceLocations.size() < 2) {
            return fenceLocations != null && fenceLocations.size() >= config.getMinFenceCount();
        }
        
        // Basit bağlantı kontrolü: Her çit en az bir çite yakın mı?
        for (Location fence1 : fenceLocations) {
            boolean connected = false;
            for (Location fence2 : fenceLocations) {
                if (fence1.equals(fence2)) continue;
                
                double distance = fence1.distance(fence2);
                if (distance <= maxDistance) {
                    connected = true;
                    break;
                }
            }
            
            if (!connected) {
                return false; // En az bir çit bağlantısız
            }
        }
        
        return true;
    }
    
    /**
     * Diğer klan alanlarıyla çakışma kontrolü
     */
    public boolean checkOverlap(Clan clan, TerritoryData newData, int buffer) {
        if (clan == null || newData == null) return false;
        
        Location newCenter = newData.getCenter();
        if (newCenter == null) return false;
        
        for (Clan otherClan : territoryManager.getClanManager().getAllClans()) {
            if (otherClan.equals(clan)) continue; // Kendi klanı
            
            me.mami.stratocraft.model.Territory otherTerritory = otherClan.getTerritory();
            if (otherTerritory == null || otherTerritory.getCenter() == null) continue;
            
            double distance = newCenter.distance(otherTerritory.getCenter());
            int combinedRadius = newData.getRadius() + otherTerritory.getRadius() + buffer;
            
            if (distance < combinedRadius) {
                return true; // Çakışma var
            }
        }
        
        return false; // Çakışma yok
    }
    
    /**
     * TerritoryData'yı temizle
     */
    public void removeTerritoryData(Clan clan) {
        if (clan == null) return;
        territoryDataMap.remove(clan.getId());
    }
    
    /**
     * Tüm TerritoryData'ları al
     */
    public Map<UUID, TerritoryData> getAllTerritoryData() {
        return new ConcurrentHashMap<>(territoryDataMap);
    }
}

