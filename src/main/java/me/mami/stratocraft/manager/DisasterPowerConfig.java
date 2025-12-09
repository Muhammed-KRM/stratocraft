package me.mami.stratocraft.manager;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Felaket Güç Hesaplama Konfigürasyonu
 * Tüm güç hesaplama ayarlarını config'den yönetir
 */
public class DisasterPowerConfig {
    // Dinamik zorluk sistemi
    private boolean dynamicDifficultyEnabled = true;
    private double powerScalingFactor = 1.0;
    private double minPowerMultiplier = 0.5;
    private double maxPowerMultiplier = 5.0;
    
    // Ağırlık çarpanları
    private double structureWeight = 0.3;
    private double itemWeight = 0.4;
    private double buffWeight = 0.15;
    private double trainingWeight = 0.1;
    private double clanTechWeight = 0.05;
    
    // Yapı tipi çarpanları
    private double batteryMultiplier = 2.0;
    private double researchCenterMultiplier = 1.5;
    private double productionMultiplier = 1.2;
    private double defenseMultiplier = 1.8;
    private double defaultStructureMultiplier = 1.0;
    
    // Oyuncu sayısı çarpanları
    private double playerCount1to3Multiplier = 0.8;
    private double playerCount4to6Multiplier = 1.0;
    private double playerCount7to10Multiplier = 1.3;
    private double playerCount11to15Multiplier = 1.6;
    private double playerCount16PlusMultiplier = 2.0;
    
    // Eşya güç değerleri
    private double weaponBasePower = 5.0;
    private double armorBasePower = 3.0;
    private double armorSetBonus = 1.5;
    private double specialItemTier1Power = 10.0;
    private double specialItemTier2Power = 25.0;
    private double specialItemTier3Power = 50.0;
    private double specialItemTier4Power = 100.0;
    
    // Buff çarpanları
    private double damageBoostMultiplier = 2.0;
    private double defenseBoostMultiplier = 1.5;
    private double speedBoostMultiplier = 0.5;
    private double regenerationMultiplier = 1.0;
    private double otherBuffMultiplier = 0.8;
    private double buffBaseValue = 10.0;
    
    // Eğitim güç değerleri
    private double trainingLevelMultiplier = 5.0;
    
    // Klan tech güç değerleri
    private double clanTechLevelMultiplier = 10.0;
    
    // Eski sistem (geriye dönük uyumluluk)
    private double legacyPlayerMultiplier = 0.1;
    private double legacyClanMultiplier = 0.15;
    
    /**
     * Config'den ayarları yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        String path = "disaster.power.dynamic-difficulty.";
        
        // Dinamik zorluk sistemi
        dynamicDifficultyEnabled = config.getBoolean(path + "enabled", true);
        powerScalingFactor = config.getDouble(path + "power-scaling-factor", 1.0);
        minPowerMultiplier = config.getDouble(path + "min-power-multiplier", 0.5);
        maxPowerMultiplier = config.getDouble(path + "max-power-multiplier", 5.0);
        
        // Ağırlık çarpanları
        path = "disaster.power.dynamic-difficulty.weights.";
        structureWeight = config.getDouble(path + "structure", 0.3);
        itemWeight = config.getDouble(path + "item", 0.4);
        buffWeight = config.getDouble(path + "buff", 0.15);
        trainingWeight = config.getDouble(path + "training", 0.1);
        clanTechWeight = config.getDouble(path + "clan-tech", 0.05);
        
        // Yapı tipi çarpanları
        path = "disaster.power.dynamic-difficulty.structure-multipliers.";
        batteryMultiplier = config.getDouble(path + "battery", 2.0);
        researchCenterMultiplier = config.getDouble(path + "research-center", 1.5);
        productionMultiplier = config.getDouble(path + "production", 1.2);
        defenseMultiplier = config.getDouble(path + "defense", 1.8);
        defaultStructureMultiplier = config.getDouble(path + "default", 1.0);
        
        // Oyuncu sayısı çarpanları
        path = "disaster.power.dynamic-difficulty.player-count-multipliers.";
        playerCount1to3Multiplier = config.getDouble(path + "1-3", 0.8);
        playerCount4to6Multiplier = config.getDouble(path + "4-6", 1.0);
        playerCount7to10Multiplier = config.getDouble(path + "7-10", 1.3);
        playerCount11to15Multiplier = config.getDouble(path + "11-15", 1.6);
        playerCount16PlusMultiplier = config.getDouble(path + "16+", 2.0);
        
        // Eşya güç değerleri
        path = "disaster.power.dynamic-difficulty.item-power.";
        weaponBasePower = config.getDouble(path + "weapon-base", 5.0);
        armorBasePower = config.getDouble(path + "armor-base", 3.0);
        armorSetBonus = config.getDouble(path + "armor-set-bonus", 1.5);
        specialItemTier1Power = config.getDouble(path + "special-item-tier-1", 10.0);
        specialItemTier2Power = config.getDouble(path + "special-item-tier-2", 25.0);
        specialItemTier3Power = config.getDouble(path + "special-item-tier-3", 50.0);
        specialItemTier4Power = config.getDouble(path + "special-item-tier-4", 100.0);
        
        // Buff çarpanları
        path = "disaster.power.dynamic-difficulty.buff-multipliers.";
        damageBoostMultiplier = config.getDouble(path + "damage-boost", 2.0);
        defenseBoostMultiplier = config.getDouble(path + "defense-boost", 1.5);
        speedBoostMultiplier = config.getDouble(path + "speed-boost", 0.5);
        regenerationMultiplier = config.getDouble(path + "regeneration", 1.0);
        otherBuffMultiplier = config.getDouble(path + "other", 0.8);
        buffBaseValue = config.getDouble(path + "base-value", 10.0);
        
        // Eğitim güç değerleri
        path = "disaster.power.dynamic-difficulty.training.";
        trainingLevelMultiplier = config.getDouble(path + "level-multiplier", 5.0);
        
        // Klan tech güç değerleri
        path = "disaster.power.dynamic-difficulty.clan-tech.";
        clanTechLevelMultiplier = config.getDouble(path + "level-multiplier", 10.0);
        
        // Eski sistem
        path = "disaster.power.";
        legacyPlayerMultiplier = config.getDouble(path + "player-multiplier", 0.1);
        legacyClanMultiplier = config.getDouble(path + "clan-multiplier", 0.15);
    }
    
    // Getters
    public boolean isDynamicDifficultyEnabled() { return dynamicDifficultyEnabled; }
    public double getPowerScalingFactor() { return powerScalingFactor; }
    public double getMinPowerMultiplier() { return minPowerMultiplier; }
    public double getMaxPowerMultiplier() { return maxPowerMultiplier; }
    
    public double getStructureWeight() { return structureWeight; }
    public double getItemWeight() { return itemWeight; }
    public double getBuffWeight() { return buffWeight; }
    public double getTrainingWeight() { return trainingWeight; }
    public double getClanTechWeight() { return clanTechWeight; }
    
    public double getBatteryMultiplier() { return batteryMultiplier; }
    public double getResearchCenterMultiplier() { return researchCenterMultiplier; }
    public double getProductionMultiplier() { return productionMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
    public double getDefaultStructureMultiplier() { return defaultStructureMultiplier; }
    
    public double getPlayerCountMultiplier(int playerCount) {
        if (playerCount <= 3) return playerCount1to3Multiplier;
        if (playerCount <= 6) return playerCount4to6Multiplier;
        if (playerCount <= 10) return playerCount7to10Multiplier;
        if (playerCount <= 15) return playerCount11to15Multiplier;
        return playerCount16PlusMultiplier;
    }
    
    public double getWeaponBasePower() { return weaponBasePower; }
    public double getArmorBasePower() { return armorBasePower; }
    public double getArmorSetBonus() { return armorSetBonus; }
    public double getSpecialItemPower(int tier) {
        switch (tier) {
            case 1: return specialItemTier1Power;
            case 2: return specialItemTier2Power;
            case 3: return specialItemTier3Power;
            case 4: return specialItemTier4Power;
            default: return 0.0;
        }
    }
    
    public double getBuffMultiplier(org.bukkit.potion.PotionEffectType type) {
        if (type == org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE) return damageBoostMultiplier;
        if (type == org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE) return defenseBoostMultiplier;
        if (type == org.bukkit.potion.PotionEffectType.SPEED) return speedBoostMultiplier;
        if (type == org.bukkit.potion.PotionEffectType.REGENERATION) return regenerationMultiplier;
        return otherBuffMultiplier;
    }
    public double getBuffBaseValue() { return buffBaseValue; }
    
    public double getTrainingLevelMultiplier() { return trainingLevelMultiplier; }
    public double getClanTechLevelMultiplier() { return clanTechLevelMultiplier; }
    
    public double getLegacyPlayerMultiplier() { return legacyPlayerMultiplier; }
    public double getLegacyClanMultiplier() { return legacyClanMultiplier; }
    
    // Buff çarpanı getter (PlayerPowerCalculator için)
    public double getDamageBoostMultiplier() { return damageBoostMultiplier; }
}

