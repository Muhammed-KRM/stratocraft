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
     * Mastery güç çarpanı (seviyeye göre)
     * Seviye -1 (Antrenman): 0.2x (1/5 güç)
     * Seviye 0 (Tamamlandı, mastery yok): 1.0x (normal güç)
     * Seviye 1 (20 kullanım): 1.2x (%20 güç artışı)
     * Seviye 2 (40 kullanım): 1.3x (%30 güç artışı)
     * Seviye 3 (50 kullanım): 1.4x (%40 güç artışı)
     */
    public double getMasteryMultiplier(UUID playerId, String ritualId) {
        int masteryLevel = getMasteryLevel(playerId, ritualId);
        
        switch (masteryLevel) {
            case -1: return 0.2; // Antrenman modu (1/5 güç)
            case 0: return 1.0; // Antrenman tamamlandı, normal güç
            case 1: return 1.2; // Seviye 1: %20 güç artışı
            case 2: return 1.3; // Seviye 2: %30 güç artışı
            case 3: return 1.4; // Seviye 3: %40 güç artışı
            default: return 1.0; // Varsayılan tam güç
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

