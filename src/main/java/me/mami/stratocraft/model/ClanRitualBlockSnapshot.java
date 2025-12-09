package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Klan Ritüel Blok Snapshot
 * 
 * Event-based Delta sistemi için:
 * - Blok koyulduğunda → Puan ekle
 * - Blok kırıldığında → Puan çıkar
 * - Aynı blok tekrar koyulursa → Puan ekleme (zaten sayılmış)
 * 
 * Performans: Full scan yerine event-based tracking
 */
public class ClanRitualBlockSnapshot {
    // Material -> Count (ritüel blok sayıları)
    private final Map<Material, Integer> blockCounts = new HashMap<>();
    
    // Takip edilen bloklar (Location String -> Material)
    // Location.equals() sorunlu olduğu için String key kullanıyoruz
    private final Map<String, Material> trackedBlocks = new HashMap<>();
    
    // Klan ID
    private final UUID clanId;
    
    public ClanRitualBlockSnapshot(UUID clanId) {
        this.clanId = clanId;
    }
    
    /**
     * Location'ı string key'e çevir (Location.equals() sorunlu)
     */
    private String locationToKey(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";" + 
               loc.getBlockX() + ";" + 
               loc.getBlockY() + ";" + 
               loc.getBlockZ();
    }
    
    /**
     * Blok koyulduğunda çağrılır
     */
    public void onBlockPlace(Location loc, Material material) {
        if (!isRitualBlock(material) || loc == null) return;
        
        String locKey = locationToKey(loc);
        if (locKey == null) return;
        
        // Aynı konumda zaten blok var mı? (duplicate kontrolü)
        if (trackedBlocks.containsKey(locKey)) {
            return; // Zaten sayılmış
        }
        
        // Yeni blok, sayıyı artır
        blockCounts.put(material, blockCounts.getOrDefault(material, 0) + 1);
        trackedBlocks.put(locKey, material);
    }
    
    /**
     * Blok kırıldığında çağrılır
     */
    public void onBlockBreak(Location loc, Material material) {
        if (!isRitualBlock(material) || loc == null) return;
        
        String locKey = locationToKey(loc);
        if (locKey == null) return;
        
        // Bu konumda takip edilen blok var mı?
        Material trackedMaterial = trackedBlocks.get(locKey);
        if (trackedMaterial == null || !trackedMaterial.equals(material)) {
            return; // Takip edilmeyen blok veya farklı material
        }
        
        // Blok sayısını azalt
        int count = blockCounts.getOrDefault(material, 0);
        if (count > 0) {
            blockCounts.put(material, count - 1);
        }
        
        // Takip listesinden çıkar
        trackedBlocks.remove(locKey);
    }
    
    /**
     * Ritüel blok mu? (config'den kontrol edilebilir)
     */
    private boolean isRitualBlock(Material material) {
        // Ritüel bloklar: Demir, Obsidyen, Elmas, Altın, Zümrüt, Netherite
        return material == Material.IRON_BLOCK ||
               material == Material.OBSIDIAN ||
               material == Material.DIAMOND_BLOCK ||
               material == Material.GOLD_BLOCK ||
               material == Material.EMERALD_BLOCK ||
               material == Material.NETHERITE_BLOCK;
    }
    
    /**
     * Blok sayılarını al
     */
    public Map<Material, Integer> getBlockCounts() {
        return new HashMap<>(blockCounts); // Defensive copy
    }
    
    /**
     * Toplam ritüel blok gücü hesapla (config'den güç değerleri alınır)
     */
    public double calculateTotalPower(me.mami.stratocraft.manager.ClanPowerConfig config) {
        double totalPower = 0.0;
        
        for (Map.Entry<Material, Integer> entry : blockCounts.entrySet()) {
            Material material = entry.getKey();
            int count = entry.getValue();
            
            double blockPower = config.getRitualBlockPower(material);
            totalPower += blockPower * count;
        }
        
        return totalPower;
    }
    
    /**
     * Snapshot'ı temizle (klan dağıldığında)
     */
    public void clear() {
        blockCounts.clear();
        trackedBlocks.clear();
    }
    
    public UUID getClanId() {
        return clanId;
    }
}

