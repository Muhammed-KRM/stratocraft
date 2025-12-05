package me.mami.stratocraft.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TrainingManager - Antrenman mekaniği
 * Oyuncular yeni ritüeller/bataryalar için antrenman yapmalı
 * Antrenman sırasında güç 1/5'inde olur
 */
public class TrainingManager {
    // Oyuncu UUID -> Ritüel ID -> Kullanım sayısı
    private final Map<UUID, Map<String, Integer>> playerTraining = new HashMap<>();
    
    // Ritüel ID -> Gerekli kullanım sayısı (seviyeye göre)
    private final Map<String, Integer> requiredUses = new HashMap<>();
    
    public TrainingManager() {
        initializeRequiredUses();
    }
    
    /**
     * Ritüel seviyelerine göre gerekli kullanım sayılarını belirle
     */
    private void initializeRequiredUses() {
        // Seviye 1 (Basit): 5 kullanım
        requiredUses.put("MAGMA_BATTERY", 5);
        requiredUses.put("LIGHTNING_BATTERY", 5);
        requiredUses.put("INSTANT_BRIDGE", 5);
        requiredUses.put("INSTANT_BUNKER", 5);
        
        // Seviye 2 (Orta): 10 kullanım
        requiredUses.put("BLACK_HOLE", 10);
        requiredUses.put("GRAVITY_ANCHOR", 10);
        requiredUses.put("EARTH_WALL", 10);
        
        // Seviye 3 (İleri): 15 kullanım
        requiredUses.put("MAGNETIC_DISRUPTOR", 15);
        requiredUses.put("OZONE_SHIELD", 15);
        requiredUses.put("ENERGY_WALL", 15);
        
        // Seviye 4 (Çok İleri): 20 kullanım
        requiredUses.put("SEISMIC_HAMMER", 20);
        requiredUses.put("LAVA_TRENCH", 20);
        
        // ========== YENİ 75 BATARYA SİSTEMİ ==========
        // Tüm bataryalar için seviyeye göre gerekli kullanım
        // Seviye 1: 5, Seviye 2: 10, Seviye 3: 15, Seviye 4: 20, Seviye 5: 25
        int defaultRequired = 5; // Varsayılan (bilinmeyen bataryalar için)
        requiredUses.put("DEFAULT", defaultRequired);
    }
    
    /**
     * Gerekli kullanım sayısını al (batarya ismine göre)
     */
    public int getRequiredUses(String trainingKey) {
        // Önce map'te var mı bak
        if (requiredUses.containsKey(trainingKey)) {
            return requiredUses.get(trainingKey);
        }
        
        // Yeni batarya sistemi için seviyeye göre otomatik hesapla
        // Seviye 1: 5, Seviye 2: 10, Seviye 3: 15, Seviye 4: 20, Seviye 5: 25
        if (trainingKey.contains("_L1") || trainingKey.endsWith(" L1")) {
            return 5;
        } else if (trainingKey.contains("_L2") || trainingKey.endsWith(" L2")) {
            return 10;
        } else if (trainingKey.contains("_L3") || trainingKey.endsWith(" L3")) {
            return 15;
        } else if (trainingKey.contains("_L4") || trainingKey.endsWith(" L4")) {
            return 20;
        } else if (trainingKey.contains("_L5") || trainingKey.endsWith(" L5")) {
            return 25;
        }
        
        // Varsayılan
        return requiredUses.getOrDefault("DEFAULT", 5);
    }
    
    /**
     * Oyuncu bir ritüel/batarya kullandığında çağrılır
     */
    public void recordUse(UUID playerId, String ritualId) {
        Map<String, Integer> playerRituals = playerTraining.computeIfAbsent(playerId, k -> new HashMap<>());
        int currentUses = playerRituals.getOrDefault(ritualId, 0);
        playerRituals.put(ritualId, currentUses + 1);
    }
    
    /**
     * Oyuncu antrenmanı tamamladı mı?
     */
    public boolean isTrained(UUID playerId, String ritualId) {
        int required = requiredUses.getOrDefault(ritualId, 0);
        if (required == 0) return true; // Antrenman gerektirmeyen ritüeller
        
        Map<String, Integer> playerRituals = playerTraining.get(playerId);
        if (playerRituals == null) return false;
        
        int currentUses = playerRituals.getOrDefault(ritualId, 0);
        return currentUses >= required;
    }
    
    /**
     * Antrenman ilerlemesini al (0.0 - 1.0 arası)
     */
    public double getTrainingProgress(UUID playerId, String ritualId) {
        int required = requiredUses.getOrDefault(ritualId, 0);
        if (required == 0) return 1.0; // Antrenman gerektirmeyen ritüeller
        
        Map<String, Integer> playerRituals = playerTraining.get(playerId);
        if (playerRituals == null) return 0.0;
        
        int currentUses = playerRituals.getOrDefault(ritualId, 0);
        return Math.min(1.0, (double) currentUses / required);
    }
    
    /**
     * Kalan kullanım sayısını al
     */
    public int getRemainingUses(UUID playerId, String ritualId) {
        int required = requiredUses.getOrDefault(ritualId, 0);
        if (required == 0) return 0;
        
        Map<String, Integer> playerRituals = playerTraining.get(playerId);
        if (playerRituals == null) return required;
        
        int currentUses = playerRituals.getOrDefault(ritualId, 0);
        return Math.max(0, required - currentUses);
    }
    
    /**
     * Antrenman modunda mı? (Güç 1/5'inde olacak)
     */
    public boolean isInTraining(UUID playerId, String ritualId) {
        return !isTrained(playerId, ritualId);
    }
    
    /**
     * Antrenman güç çarpanı (1.0 = tam güç, 0.2 = 1/5 güç)
     * NOT: Artık mastery sistemi kullanılıyor, bu metod geriye dönük uyumluluk için
     */
    public double getTrainingPowerMultiplier(UUID playerId, String ritualId) {
        return getMasteryMultiplier(playerId, ritualId);
    }
    
    /**
     * Toplam kullanım sayısını al (antrenman + mastery dahil)
     */
    public int getTotalUses(UUID playerId, String ritualId) {
        Map<String, Integer> playerRituals = playerTraining.get(playerId);
        if (playerRituals == null) return 0;
        return playerRituals.getOrDefault(ritualId, 0);
    }
    
    /**
     * Mastery seviyesini al (-1 = Antrenman, 0 = Tamamlandı, 1-3 = Ustalık seviyeleri)
     * Seviye 1: 20 kullanım = %20 güç artışı
     * Seviye 2: 40 kullanım = %30 güç artışı
     * Seviye 3: 50 kullanım = %40 güç artışı
     */
    public int getMasteryLevel(UUID playerId, String ritualId) {
        int totalUses = getTotalUses(playerId, ritualId);
        
        // Antrenman modu (henüz tam güce ulaşmadı)
        if (!isTrained(playerId, ritualId)) {
            return -1; // Seviye -1 = Antrenman (özel durum)
        }
        
        // Mastery seviyeleri
        if (totalUses >= 50) return 3; // Seviye 3: %40 güç
        if (totalUses >= 40) return 2; // Seviye 2: %30 güç
        if (totalUses >= 20) return 1; // Seviye 1: %20 güç
        
        return 0; // Antrenman tamamlandı ama henüz mastery seviyesi yok (normal güç)
    }
    
    /**
     * Mastery güç çarpanı (seviyeye göre) - Geriye dönük uyumluluk için
     * Seviye bilgisi olmadan çağrıldığında varsayılan seviye 3 kullanılır
     */
    public double getMasteryMultiplier(UUID playerId, String ritualId) {
        // Varsayılan seviye 3 (orta seviye)
        return getMasteryMultiplier(playerId, ritualId, 3);
    }
    
    /**
     * Mastery güç çarpanı (seviye bazlı dinamik sistem)
     * 
     * BAŞLANGIÇ GÜCÜ (Seviye Bazlı):
     * - Seviye 1: %20
     * - Seviye 2: %40
     * - Seviye 3: %60
     * - Seviye 4: %70
     * - Seviye 5: %80
     * 
     * KADEMELİ ARTIŞLAR:
     * - 1. kullanım: Başlangıç gücü
     * - 2. kullanım: Başlangıç + %20
     * - 3. kullanım: Başlangıç + %40
     * - 4. kullanım: Başlangıç + %60
     * - 5. kullanım: Başlangıç + %80 (genellikle %100'e ulaşır)
     * - 6-20. kullanım: %100 (tam güç)
     * - 21-30. kullanım: %100 → %150 (kademeli artış)
     */
    public double getMasteryMultiplier(UUID playerId, String ritualId, int batteryLevel) {
        int totalUses = getTotalUses(playerId, ritualId);
        
        // Başlangıç gücünü seviyeye göre belirle
        double startPower = getStartingPower(batteryLevel);
        
        // Kullanım sayısına göre güç hesapla
        if (totalUses == 0) {
            // Hiç kullanılmamış
            return startPower;
        } else if (totalUses == 1) {
            // 1. kullanım: Başlangıç gücü
            return startPower;
        } else if (totalUses == 2) {
            // 2. kullanım: +%20
            return Math.min(1.0, startPower + 0.2);
        } else if (totalUses == 3) {
            // 3. kullanım: +%40
            return Math.min(1.0, startPower + 0.4);
        } else if (totalUses == 4) {
            // 4. kullanım: +%60
            return Math.min(1.0, startPower + 0.6);
        } else if (totalUses >= 5 && totalUses <= 20) {
            // 5-20. kullanım: %100 (tam güç)
            return 1.0;
        } else if (totalUses > 20 && totalUses <= 30) {
            // 21-30. kullanım: %100 → %150 (kademeli)
            int extraUses = totalUses - 20;
            double extraPower = (extraUses / 10.0) * 0.5; // Her 10 kullanımda %50 artış
            return 1.0 + extraPower;
        } else {
            // 30+ kullanım: %150 (maksimum güç)
            return 1.5;
        }
    }
    
    /**
     * Seviyeye göre başlangıç gücü
     * - Seviye 1: %20
     * - Seviye 2: %40
     * - Seviye 3: %60
     * - Seviye 4: %70
     * - Seviye 5: %80
     */
    private double getStartingPower(int batteryLevel) {
        switch (batteryLevel) {
            case 1: return 0.2;  // %20
            case 2: return 0.4;  // %40
            case 3: return 0.6;  // %60
            case 4: return 0.7;  // %70
            case 5: return 0.8;  // %80
            default: return 0.5; // Varsayılan %50
        }
    }
    
    /**
     * Bir önceki seviyeyi al (seviye atlama kontrolü için)
     */
    public int getPreviousMasteryLevel(UUID playerId, String ritualId) {
        int totalUses = getTotalUses(playerId, ritualId);
        
        // Eğer henüz antrenman tamamlanmadıysa
        if (!isTrained(playerId, ritualId)) {
            return -1; // Antrenman modu
        }
        
        // Bir önceki kullanım sayısına göre seviye hesapla
        int previousUses = totalUses - 1;
        
        if (previousUses >= 50) return 3;
        if (previousUses >= 40) return 2;
        if (previousUses >= 20) return 1;
        
        return 0; // Antrenman tamamlandı ama henüz mastery seviyesi yok
    }
    
    /**
     * DataManager için - antrenman verilerini al
     */
    public Map<UUID, Map<String, Integer>> getAllTrainingData() {
        return playerTraining;
    }
    
    /**
     * DataManager için - antrenman verilerini yükle
     */
    public void loadTrainingData(Map<UUID, Map<String, Integer>> data) {
        if (data != null) {
            playerTraining.putAll(data);
        }
    }
}

