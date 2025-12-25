package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Eğitme Başarı İhtimali Hesaplama Sistemi
 * 
 * Canavarın gücüne göre eğitme başarı ihtimalini hesaplar:
 * - Normal mob (seviye 1, güç 1-2): %70
 * - Boss (seviye 1, güç 3-4): %10
 * - Güç arttıkça ihtimal azalır
 * - ✅ YENİ: Config'den okuma desteği
 */
public class TrainingSuccessCalculator {
    
    // ✅ YENİ: Config'den okunan değerler (cache)
    private static Map<Integer, Double> normalMobChances = new HashMap<>();
    private static Map<Integer, Double> bossChances = new HashMap<>();
    private static double arenaLevelPenaltyPerLevel = 0.5; // Her seviye farkı için yarıya iner
    private static double arenaLevelBonusPerLevel = 0.1; // Her seviye farkı için %10 bonus
    private static boolean configLoaded = false;
    
    /**
     * ✅ YENİ: Config'den değerleri yükle
     */
    public static void loadConfig(FileConfiguration config) {
        if (config == null) {
            // Varsayılan değerleri kullan
            loadDefaultValues();
            return;
        }
        
        normalMobChances.clear();
        bossChances.clear();
        
        String basePath = "training-system.";
        
        // Normal mob başarı ihtimalleri
        String normalMobPath = basePath + "normal-mob-success-chance.";
        for (int power = 1; power <= 6; power++) {
            double chance = config.getDouble(normalMobPath + "power-" + power, getDefaultNormalMobChance(power));
            normalMobChances.put(power, Math.max(0.0, Math.min(1.0, chance)));
        }
        
        // Boss başarı ihtimalleri
        String bossPath = basePath + "boss-success-chance.";
        for (int power = 3; power <= 10; power++) {
            double chance = config.getDouble(bossPath + "power-" + power, getDefaultBossChance(power));
            bossChances.put(power, Math.max(0.0, Math.min(1.0, chance)));
        }
        
        // Yapı seviyesi etkisi
        String arenaPath = basePath + "arena-level-effect.";
        arenaLevelPenaltyPerLevel = config.getDouble(arenaPath + "penalty-per-level", 0.5);
        arenaLevelBonusPerLevel = config.getDouble(arenaPath + "bonus-per-level", 0.1);
        
        configLoaded = true;
    }
    
    /**
     * Varsayılan değerleri yükle
     */
    private static void loadDefaultValues() {
        normalMobChances.clear();
        bossChances.clear();
        
        for (int power = 1; power <= 6; power++) {
            normalMobChances.put(power, getDefaultNormalMobChance(power));
        }
        
        for (int power = 3; power <= 10; power++) {
            bossChances.put(power, getDefaultBossChance(power));
        }
        
        configLoaded = true;
    }
    
    /**
     * Varsayılan normal mob ihtimali
     */
    private static double getDefaultNormalMobChance(int power) {
        switch (power) {
            case 1: return 0.70;
            case 2: return 0.50;
            case 3: return 0.30;
            case 4: return 0.20;
            case 5: return 0.10;
            case 6: return 0.05;
            default: return 0.50;
        }
    }
    
    /**
     * Varsayılan boss ihtimali
     */
    private static double getDefaultBossChance(int power) {
        switch (power) {
            case 3: return 0.10;
            case 4: return 0.08;
            case 5: return 0.05;
            case 6: return 0.03;
            case 7: return 0.02;
            case 8: return 0.01;
            case 9:
            case 10: return 0.005;
            default: return 0.05;
        }
    }
    
    /**
     * Eğitme başarı ihtimalini hesapla
     * 
     * @param entity Canavar entity'si
     * @param location Canavarın konumu
     * @param arenaLevel Eğitim Alanı yapı seviyesi (1-5, null ise sadece canavar gücüne göre)
     * @return Başarı ihtimali (0.0 - 1.0 arası, örn: 0.7 = %70)
     */
    public static double calculateSuccessChance(LivingEntity entity, org.bukkit.Location location, Integer arenaLevel) {
        if (entity == null || location == null) {
            return 0.5; // Varsayılan %50
        }
        
        int mobPower = MobPowerCalculator.calculateMobPower(entity, location);
        int mobLevel = MobPowerCalculator.getMobLevel(entity, location);
        boolean isBoss = MobPowerCalculator.isBoss(entity);
        
        // ✅ DÜZELTME: Config yüklenmemişse yükle
        if (!configLoaded) {
            Main plugin = Main.getInstance();
            if (plugin != null && plugin.getConfigManager() != null) {
                loadConfig(plugin.getConfigManager().getConfig());
            } else {
                loadDefaultValues();
            }
        }
        
        // Temel başarı ihtimali (config'den veya varsayılan)
        double baseChance;
        if (isBoss) {
            baseChance = calculateBossSuccessChance(mobPower);
        } else {
            baseChance = calculateNormalMobSuccessChance(mobPower);
        }
        
        // ✅ DÜZELTME: Yapı seviyesi kontrolü - Config'den okunan değerlerle
        if (arenaLevel != null) {
            if (arenaLevel < mobLevel) {
                // Yapı seviyesi canavar seviyesinden düşükse, ihtimal azalır
                int levelDiff = mobLevel - arenaLevel;
                double penalty = Math.pow(arenaLevelPenaltyPerLevel, levelDiff); // Config'den okunan penalty
                baseChance *= penalty;
            } else if (arenaLevel > mobLevel) {
                // Yapı seviyesi canavar seviyesinden yüksekse, bonus ver
                int levelDiff = arenaLevel - mobLevel;
                double bonus = 1.0 + (levelDiff * arenaLevelBonusPerLevel); // Config'den okunan bonus
                baseChance = Math.min(1.0, baseChance * bonus); // Maksimum %100
            }
        }
        
        return Math.max(0.0, Math.min(1.0, baseChance)); // 0-1 arası sınırla
    }
    
    /**
     * Eğitme başarı ihtimalini hesapla (yapı seviyesi olmadan)
     */
    public static double calculateSuccessChance(LivingEntity entity, org.bukkit.Location location) {
        return calculateSuccessChance(entity, location, null);
    }
    
    /**
     * Boss eğitme başarı ihtimali (Config'den veya varsayılan)
     */
    private static double calculateBossSuccessChance(int power) {
        if (bossChances.containsKey(power)) {
            return bossChances.get(power);
        }
        return getDefaultBossChance(power);
    }
    
    /**
     * Normal mob eğitme başarı ihtimali (Config'den veya varsayılan)
     */
    private static double calculateNormalMobSuccessChance(int power) {
        if (normalMobChances.containsKey(power)) {
            return normalMobChances.get(power);
        }
        return getDefaultNormalMobChance(power);
    }
    
    /**
     * Eğitme başarılı mı? (Rastgele kontrol)
     */
    public static boolean isTrainingSuccessful(LivingEntity entity, org.bukkit.Location location) {
        double chance = calculateSuccessChance(entity, location);
        return Math.random() < chance;
    }
    
    /**
     * Başarı ihtimalini yüzde olarak string'e çevir
     */
    public static String getSuccessChanceAsString(LivingEntity entity, org.bukkit.Location location) {
        double chance = calculateSuccessChance(entity, location);
        return String.format("%.1f", chance * 100) + "%";
    }
}

