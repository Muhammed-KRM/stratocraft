package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Felaket Konfigürasyon Yöneticisi
 * Config dosyasından felaket ayarlarını yükler ve yönetir
 */
public class DisasterConfigManager {
    private final Map<Disaster.Type, DisasterConfig> typeConfigs = new HashMap<>();
    private final Map<Integer, DisasterConfig> levelConfigs = new HashMap<>();
    private DisasterConfig generalConfig;
    
    /**
     * Config dosyasından tüm felaket ayarlarını yükle
     */
    public void loadConfigs(FileConfiguration config) {
        // Genel ayarları yükle
        generalConfig = new DisasterConfig();
        loadGeneralConfig(config, generalConfig);
        
        // Seviye bazlı güçleri yükle
        loadLevelConfigs(config);
        
        // Her felaket tipi için özel ayarları yükle
        for (Disaster.Type type : Disaster.Type.values()) {
            DisasterConfig typeConfig = generalConfig.clone();
            loadTypeConfig(config, type, typeConfig);
            typeConfigs.put(type, typeConfig);
        }
    }
    
    /**
     * Genel ayarları yükle
     */
    private void loadGeneralConfig(FileConfiguration config, DisasterConfig generalConfig) {
        String path = "disaster.general.";
        generalConfig.setSpawnDistance(config.getDouble(path + "spawn-distance", 5000.0));
        generalConfig.setAttackInterval(config.getLong(path + "attack-interval", 120000L));
        generalConfig.setAttackRadius(config.getDouble(path + "attack-radius", 30.0));
        generalConfig.setCrystalProximity(config.getDouble(path + "crystal-proximity", 5.0));
        generalConfig.setCrystalCacheInterval(config.getLong(path + "crystal-cache-interval", 10000L));
        generalConfig.setChunkUnloadDelay(config.getInt(path + "chunk-unload-delay", 200));
        
        // Güç hesaplama çarpanları
        path = "disaster.power.";
        generalConfig.setPlayerMultiplier(config.getDouble(path + "player-multiplier", 0.1));
        generalConfig.setClanMultiplier(config.getDouble(path + "clan-multiplier", 0.15));
    }
    
    /**
     * Seviye bazlı güçleri yükle
     */
    private void loadLevelConfigs(FileConfiguration config) {
        for (int level = 1; level <= 4; level++) {
            DisasterConfig levelConfig = generalConfig.clone();
            String path = "disaster.levels.level" + level + ".";
            
            levelConfig.setBaseHealth(config.getDouble(path + "base-health", getDefaultBaseHealth(level)));
            levelConfig.setBaseDamage(config.getDouble(path + "base-damage", getDefaultBaseDamage(level)));
            levelConfig.setHealthMultiplier(config.getDouble(path + "health-multiplier", getDefaultHealthMultiplier(level)));
            levelConfig.setDamageMultiplier(config.getDouble(path + "damage-multiplier", getDefaultDamageMultiplier(level)));
            
            levelConfigs.put(level, levelConfig);
        }
    }
    
    /**
     * Felaket tipine özel ayarları yükle
     */
    private void loadTypeConfig(FileConfiguration config, Disaster.Type type, DisasterConfig typeConfig) {
        String path = "disaster.types." + type.name() + ".";
        
        if (!config.contains("disaster.types." + type.name())) {
            // Config'de yoksa varsayılan değerleri kullan
            return;
        }
        
        // Hareket ayarları
        typeConfig.setMoveSpeed(config.getDouble(path + "move-speed", typeConfig.getMoveSpeed()));
        typeConfig.setJumpHeight(config.getDouble(path + "jump-height", typeConfig.getJumpHeight()));
        typeConfig.setCanJump(config.getBoolean(path + "can-jump", typeConfig.canJump()));
        typeConfig.setCanTeleport(config.getBoolean(path + "can-teleport", typeConfig.canTeleport()));
        typeConfig.setTeleportDistance(config.getDouble(path + "teleport-distance", typeConfig.getTeleportDistance()));
        
        // Titan Golem
        typeConfig.setJumpIntervalMin(config.getInt(path + "jump-interval-min", typeConfig.getJumpIntervalMin()));
        typeConfig.setJumpIntervalMax(config.getInt(path + "jump-interval-max", typeConfig.getJumpIntervalMax()));
        typeConfig.setBlockThrowInterval(config.getInt(path + "block-throw-interval", typeConfig.getBlockThrowInterval()));
        typeConfig.setExplosionInterval(config.getInt(path + "explosion-interval", typeConfig.getExplosionInterval()));
        typeConfig.setExplosionPower(config.getDouble(path + "explosion-power", typeConfig.getExplosionPower()));
        typeConfig.setBlockBreakRadius(config.getInt(path + "block-break-radius", typeConfig.getBlockBreakRadius()));
        typeConfig.setPassiveExplosionPower(config.getDouble(path + "passive-explosion-power", typeConfig.getPassiveExplosionPower()));
        
        // Abyssal Worm
        typeConfig.setDigSpeed(config.getDouble(path + "dig-speed", typeConfig.getDigSpeed()));
        
        // Chaos Dragon
        typeConfig.setFireBreathChance(config.getInt(path + "fire-breath-chance", typeConfig.getFireBreathChance()));
        typeConfig.setFireBreathRange(config.getDouble(path + "fire-breath-range", typeConfig.getFireBreathRange()));
        typeConfig.setFireDamage(config.getDouble(path + "fire-damage", typeConfig.getFireDamage()));
        
        // Void Titan
        typeConfig.setVoidExplosionChance(config.getInt(path + "void-explosion-chance", typeConfig.getVoidExplosionChance()));
        typeConfig.setVoidExplosionPower(config.getDouble(path + "void-explosion-power", typeConfig.getVoidExplosionPower()));
        typeConfig.setVoidExplosionRadius(config.getDouble(path + "void-explosion-radius", typeConfig.getVoidExplosionRadius()));
        
        // Ice Leviathan
        typeConfig.setFreezeChance(config.getInt(path + "freeze-chance", typeConfig.getFreezeChance()));
        typeConfig.setFreezeRadius(config.getDouble(path + "freeze-radius", typeConfig.getFreezeRadius()));
        typeConfig.setFreezeDuration(config.getInt(path + "freeze-duration", typeConfig.getFreezeDuration()));
        typeConfig.setIceConversionChance(config.getInt(path + "ice-conversion-chance", typeConfig.getIceConversionChance()));
        typeConfig.setIceConversionRadius(config.getInt(path + "ice-conversion-radius", typeConfig.getIceConversionRadius()));
        
        // Grup Felaketler
        typeConfig.setGroupSize(config.getInt(path + "group-size", typeConfig.getGroupSize()));
        typeConfig.setGroupSizeMin(config.getInt(path + "group-size-min", typeConfig.getGroupSizeMin()));
        typeConfig.setGroupSizeMax(config.getInt(path + "group-size-max", typeConfig.getGroupSizeMax()));
        typeConfig.setSpawnRadius(config.getDouble(path + "spawn-radius", typeConfig.getSpawnRadius()));
        typeConfig.setHealthPercentage(config.getDouble(path + "health-percentage", typeConfig.getHealthPercentage()));
        
        // Solar Flare
        typeConfig.setFireTickDuration(config.getInt(path + "fire-tick-duration", typeConfig.getFireTickDuration()));
        typeConfig.setFlammableChanceLog(config.getDouble(path + "flammable-chance-log", typeConfig.getFlammableChanceLog()));
        typeConfig.setFlammableChanceOther(config.getDouble(path + "flammable-chance-other", typeConfig.getFlammableChanceOther()));
        typeConfig.setLavaSpawnChance(config.getDouble(path + "lava-spawn-chance", typeConfig.getLavaSpawnChance()));
        typeConfig.setScanRadius(config.getInt(path + "scan-radius", typeConfig.getScanRadius()));
        
        // Earthquake
        typeConfig.setExplosionChance(config.getInt(path + "explosion-chance", typeConfig.getExplosionChance()));
        typeConfig.setExplosionRadius(config.getDouble(path + "explosion-radius", typeConfig.getExplosionRadius()));
        typeConfig.setEarthquakeExplosionPower(config.getDouble(path + "explosion-power", typeConfig.getEarthquakeExplosionPower()));
        typeConfig.setDamageInterval(config.getInt(path + "damage-interval", typeConfig.getDamageInterval()));
        typeConfig.setDamageAmount(config.getDouble(path + "damage-amount", typeConfig.getDamageAmount()));
        typeConfig.setBlockFallRadius(config.getInt(path + "block-fall-radius", typeConfig.getBlockFallRadius()));
        typeConfig.setBlockFallHeight(config.getInt(path + "block-fall-height", typeConfig.getBlockFallHeight()));
        
        // Storm
        typeConfig.setLightningChanceNearby(config.getInt(path + "lightning-chance-nearby", typeConfig.getLightningChanceNearby()));
        typeConfig.setLightningChanceRandom(config.getInt(path + "lightning-chance-random", typeConfig.getLightningChanceRandom()));
        typeConfig.setLightningRadius(config.getDouble(path + "lightning-radius", typeConfig.getLightningRadius()));
        typeConfig.setLightningDamage(config.getDouble(path + "lightning-damage", typeConfig.getLightningDamage()));
        typeConfig.setLightningDamageRadius(config.getDouble(path + "lightning-damage-radius", typeConfig.getLightningDamageRadius()));
        
        // Meteor Shower
        typeConfig.setMeteorChance(config.getInt(path + "meteor-chance", typeConfig.getMeteorChance()));
        typeConfig.setMeteorSpawnHeight(config.getInt(path + "meteor-spawn-height", typeConfig.getMeteorSpawnHeight()));
        typeConfig.setMeteorSpawnRange(config.getDouble(path + "meteor-spawn-range", typeConfig.getMeteorSpawnRange()));
        typeConfig.setMeteorExplosionPower(config.getDouble(path + "meteor-explosion-power", typeConfig.getMeteorExplosionPower()));
        typeConfig.setMeteorDamageRadius(config.getInt(path + "meteor-damage-radius", typeConfig.getMeteorDamageRadius()));
        
        // Volcanic Eruption
        typeConfig.setVolcanicExplosionChance(config.getInt(path + "explosion-chance", typeConfig.getVolcanicExplosionChance()));
        typeConfig.setVolcanicExplosionRadius(config.getDouble(path + "explosion-radius", typeConfig.getVolcanicExplosionRadius()));
        typeConfig.setVolcanicExplosionPower(config.getDouble(path + "explosion-power", typeConfig.getVolcanicExplosionPower()));
        typeConfig.setVolcanicLavaSpawnChance(config.getInt(path + "lava-spawn-chance", typeConfig.getVolcanicLavaSpawnChance()));
        typeConfig.setVolcanicLavaSpawnRadius(config.getDouble(path + "lava-spawn-radius", typeConfig.getVolcanicLavaSpawnRadius()));
        typeConfig.setVolcanicFireDamageInterval(config.getInt(path + "fire-damage-interval", typeConfig.getVolcanicFireDamageInterval()));
        typeConfig.setVolcanicFireDamageAmount(config.getDouble(path + "fire-damage-amount", typeConfig.getVolcanicFireDamageAmount()));
    }
    
    /**
     * Felaket tipi için config al
     */
    public DisasterConfig getConfig(Disaster.Type type) {
        DisasterConfig config = typeConfigs.get(type);
        if (config == null) {
            // Config yoksa genel config'i döndür
            return generalConfig != null ? generalConfig.clone() : new DisasterConfig();
        }
        return config;
    }
    
    /**
     * Seviye için config al
     */
    public DisasterConfig getConfigForLevel(int level) {
        DisasterConfig config = levelConfigs.get(level);
        if (config == null) {
            // Config yoksa genel config'i döndür
            return generalConfig != null ? generalConfig.clone() : new DisasterConfig();
        }
        return config;
    }
    
    /**
     * Felaket tipi ve seviye için birleşik config al
     */
    public DisasterConfig getConfig(Disaster.Type type, int level) {
        DisasterConfig typeConfig = getConfig(type);
        DisasterConfig levelConfig = getConfigForLevel(level);
        
        // Seviye config'ini temel al, tip config'i ile birleştir
        DisasterConfig combined = levelConfig.clone();
        
        // Tip'e özel ayarları kopyala
        combined.setMoveSpeed(typeConfig.getMoveSpeed());
        combined.setJumpHeight(typeConfig.getJumpHeight());
        combined.setCanJump(typeConfig.canJump());
        combined.setCanTeleport(typeConfig.canTeleport());
        combined.setTeleportDistance(typeConfig.getTeleportDistance());
        
        // Özel yetenekler
        combined.setJumpIntervalMin(typeConfig.getJumpIntervalMin());
        combined.setJumpIntervalMax(typeConfig.getJumpIntervalMax());
        combined.setBlockThrowInterval(typeConfig.getBlockThrowInterval());
        combined.setExplosionInterval(typeConfig.getExplosionInterval());
        combined.setExplosionPower(typeConfig.getExplosionPower());
        combined.setBlockBreakRadius(typeConfig.getBlockBreakRadius());
        combined.setPassiveExplosionPower(typeConfig.getPassiveExplosionPower());
        combined.setDigSpeed(typeConfig.getDigSpeed());
        combined.setFireBreathChance(typeConfig.getFireBreathChance());
        combined.setFireBreathRange(typeConfig.getFireBreathRange());
        combined.setFireDamage(typeConfig.getFireDamage());
        combined.setVoidExplosionChance(typeConfig.getVoidExplosionChance());
        combined.setVoidExplosionPower(typeConfig.getVoidExplosionPower());
        combined.setVoidExplosionRadius(typeConfig.getVoidExplosionRadius());
        combined.setFreezeChance(typeConfig.getFreezeChance());
        combined.setFreezeRadius(typeConfig.getFreezeRadius());
        combined.setFreezeDuration(typeConfig.getFreezeDuration());
        combined.setIceConversionChance(typeConfig.getIceConversionChance());
        combined.setIceConversionRadius(typeConfig.getIceConversionRadius());
        combined.setGroupSize(typeConfig.getGroupSize());
        combined.setGroupSizeMin(typeConfig.getGroupSizeMin());
        combined.setGroupSizeMax(typeConfig.getGroupSizeMax());
        combined.setSpawnRadius(typeConfig.getSpawnRadius());
        combined.setHealthPercentage(typeConfig.getHealthPercentage());
        combined.setFireTickDuration(typeConfig.getFireTickDuration());
        combined.setFlammableChanceLog(typeConfig.getFlammableChanceLog());
        combined.setFlammableChanceOther(typeConfig.getFlammableChanceOther());
        combined.setLavaSpawnChance(typeConfig.getLavaSpawnChance());
        combined.setScanRadius(typeConfig.getScanRadius());
        combined.setExplosionChance(typeConfig.getExplosionChance());
        combined.setExplosionRadius(typeConfig.getExplosionRadius());
        combined.setEarthquakeExplosionPower(typeConfig.getEarthquakeExplosionPower());
        combined.setDamageInterval(typeConfig.getDamageInterval());
        combined.setDamageAmount(typeConfig.getDamageAmount());
        combined.setBlockFallRadius(typeConfig.getBlockFallRadius());
        combined.setBlockFallHeight(typeConfig.getBlockFallHeight());
        combined.setLightningChanceNearby(typeConfig.getLightningChanceNearby());
        combined.setLightningChanceRandom(typeConfig.getLightningChanceRandom());
        combined.setLightningRadius(typeConfig.getLightningRadius());
        combined.setLightningDamage(typeConfig.getLightningDamage());
        combined.setLightningDamageRadius(typeConfig.getLightningDamageRadius());
        
        return combined;
    }
    
    /**
     * Genel config al
     */
    public DisasterConfig getGeneralConfig() {
        return generalConfig != null ? generalConfig.clone() : new DisasterConfig();
    }
    
    // Varsayılan değerler
    private double getDefaultBaseHealth(int level) {
        switch (level) {
            case 1: return 500.0;
            case 2: return 1500.0;
            case 3: return 5000.0;
            case 4: return 10000.0;
            default: return 500.0;
        }
    }
    
    private double getDefaultBaseDamage(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 2.0;
            case 3: return 5.0;
            case 4: return 10.0;
            default: return 1.0;
        }
    }
    
    private double getDefaultHealthMultiplier(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 1.5;
            case 3: return 2.0;
            case 4: return 3.0;
            default: return 1.0;
        }
    }
    
    private double getDefaultDamageMultiplier(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 1.5;
            case 3: return 2.0;
            case 4: return 3.0;
            default: return 1.0;
        }
    }
}
