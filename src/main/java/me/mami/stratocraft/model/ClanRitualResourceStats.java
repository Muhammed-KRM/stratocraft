package me.mami.stratocraft.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Klan Ritüel Kaynak İstatistikleri
 * 
 * Ritüel kaynak tüketimi için tracking:
 * - Sadece başarılı ritüeller için puan verilir
 * - Başarısız ritüeller puan vermez
 */
public class ClanRitualResourceStats {
    // Ritüel tipi -> Kullanılan kaynaklar (Kaynak tipi -> Miktar)
    private final Map<String, Map<String, Integer>> ritualResources = new HashMap<>();
    
    // Klan ID
    private final UUID clanId;
    
    public ClanRitualResourceStats(UUID clanId) {
        this.clanId = clanId;
    }
    
    /**
     * Ritüel başarıyla tamamlandığında çağrılır
     */
    public void onRitualSuccess(String ritualType, Map<String, Integer> usedResources) {
        if (ritualType == null || usedResources == null || usedResources.isEmpty()) {
            return;
        }
        
        Map<String, Integer> resources = ritualResources.computeIfAbsent(
            ritualType, 
            k -> new HashMap<>()
        );
        
        // Kullanılan kaynakları ekle
        for (Map.Entry<String, Integer> entry : usedResources.entrySet()) {
            String resourceType = entry.getKey();
            int amount = entry.getValue();
            
            int current = resources.getOrDefault(resourceType, 0);
            resources.put(resourceType, current + amount);
        }
    }
    
    /**
     * Ritüel başarısız olduğunda çağrılır (puan verme)
     */
    public void onRitualFailure(String ritualType) {
        // Başarısız ritüeller puan vermez, sadece log
        // (Bu metod şimdilik boş, gelecekte log eklenebilir)
    }
    
    /**
     * Toplam ritüel kaynak gücü hesapla (config'den güç değerleri alınır)
     */
    public double calculateTotalPower(me.mami.stratocraft.manager.ClanPowerConfig config) {
        double totalPower = 0.0;
        
        for (Map<String, Integer> resources : ritualResources.values()) {
            for (Map.Entry<String, Integer> entry : resources.entrySet()) {
                String resourceType = entry.getKey();
                int amount = entry.getValue();
                
                double resourcePower = config.getRitualResourcePower(resourceType);
                totalPower += resourcePower * amount;
            }
        }
        
        return totalPower;
    }
    
    /**
     * İstatistikleri temizle (klan dağıldığında)
     */
    public void clear() {
        ritualResources.clear();
    }
    
    public UUID getClanId() {
        return clanId;
    }
    
    /**
     * Ritüel kaynaklarını al (debug için)
     */
    public Map<String, Map<String, Integer>> getRitualResources() {
        return new HashMap<>(ritualResources); // Defensive copy
    }
}

