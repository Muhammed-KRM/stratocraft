package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.DisasterPhase;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Felaket Faz Sistemi Konfigürasyonu
 * Tüm faz ayarlarını config'den yönetir
 */
public class DisasterPhaseConfig {
    // Faz geçiş eşikleri (can yüzdesi)
    private double explorationMaxHealth = 1.0;
    private double explorationMinHealth = 0.75;
    private double assaultMaxHealth = 0.75;
    private double assaultMinHealth = 0.50;
    private double rageMaxHealth = 0.50;
    private double rageMinHealth = 0.25;
    private double desperationMaxHealth = 0.25;
    private double desperationMinHealth = 0.0;
    
    // Faz saldırı aralıkları (ms)
    private long explorationAttackInterval = 120000L;  // 2 dakika
    private long assaultAttackInterval = 90000L;       // 1.5 dakika
    private long rageAttackInterval = 60000L;          // 1 dakika
    private long desperationAttackInterval = 30000L;   // 30 saniye
    
    // Faz aktif yetenek sayıları
    private int explorationAbilityCount = 0;
    private int assaultAbilityCount = 2;
    private int rageAbilityCount = 5;
    private int desperationAbilityCount = 10;
    
    // Faz hareket hızı çarpanları
    private double explorationSpeedMultiplier = 1.0;
    private double assaultSpeedMultiplier = 1.2;
    private double rageSpeedMultiplier = 1.5;
    private double desperationSpeedMultiplier = 2.0;
    
    // Faz oyuncu saldırısı ayarları
    private boolean explorationAttackPlayers = false;
    private boolean assaultAttackPlayers = false;
    private boolean rageAttackPlayers = true;
    private boolean desperationAttackPlayers = true;
    
    // Faz geçiş bildirimleri
    private boolean phaseTransitionMessages = true;
    private boolean phaseTransitionEffects = true;
    
    /**
     * Config'den ayarları yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        String path = "disaster.phase-system.";
        
        // Faz geçiş eşikleri
        path = "disaster.phase-system.health-thresholds.";
        explorationMaxHealth = config.getDouble(path + "exploration.max", 1.0);
        explorationMinHealth = config.getDouble(path + "exploration.min", 0.75);
        assaultMaxHealth = config.getDouble(path + "assault.max", 0.75);
        assaultMinHealth = config.getDouble(path + "assault.min", 0.50);
        rageMaxHealth = config.getDouble(path + "rage.max", 0.50);
        rageMinHealth = config.getDouble(path + "rage.min", 0.25);
        desperationMaxHealth = config.getDouble(path + "desperation.max", 0.25);
        desperationMinHealth = config.getDouble(path + "desperation.min", 0.0);
        
        // Faz saldırı aralıkları
        path = "disaster.phase-system.attack-intervals.";
        explorationAttackInterval = config.getLong(path + "exploration", 120000L);
        assaultAttackInterval = config.getLong(path + "assault", 90000L);
        rageAttackInterval = config.getLong(path + "rage", 60000L);
        desperationAttackInterval = config.getLong(path + "desperation", 30000L);
        
        // Faz aktif yetenek sayıları
        path = "disaster.phase-system.ability-counts.";
        explorationAbilityCount = config.getInt(path + "exploration", 0);
        assaultAbilityCount = config.getInt(path + "assault", 2);
        rageAbilityCount = config.getInt(path + "rage", 5);
        desperationAbilityCount = config.getInt(path + "desperation", 10);
        
        // Faz hareket hızı çarpanları
        path = "disaster.phase-system.speed-multipliers.";
        explorationSpeedMultiplier = config.getDouble(path + "exploration", 1.0);
        assaultSpeedMultiplier = config.getDouble(path + "assault", 1.2);
        rageSpeedMultiplier = config.getDouble(path + "rage", 1.5);
        desperationSpeedMultiplier = config.getDouble(path + "desperation", 2.0);
        
        // Faz oyuncu saldırısı ayarları
        path = "disaster.phase-system.attack-players.";
        explorationAttackPlayers = config.getBoolean(path + "exploration", false);
        assaultAttackPlayers = config.getBoolean(path + "assault", false);
        rageAttackPlayers = config.getBoolean(path + "rage", true);
        desperationAttackPlayers = config.getBoolean(path + "desperation", true);
        
        // Faz geçiş bildirimleri
        path = "disaster.phase-system.";
        phaseTransitionMessages = config.getBoolean(path + "transition-messages", true);
        phaseTransitionEffects = config.getBoolean(path + "transition-effects", true);
    }
    
    /**
     * Faz için saldırı aralığını al
     */
    public long getAttackInterval(DisasterPhase phase) {
        switch (phase) {
            case EXPLORATION:
                return explorationAttackInterval;
            case ASSAULT:
                return assaultAttackInterval;
            case RAGE:
                return rageAttackInterval;
            case DESPERATION:
                return desperationAttackInterval;
            default:
                return 120000L; // Varsayılan
        }
    }
    
    /**
     * Faz için aktif yetenek sayısını al
     */
    public int getAbilityCount(DisasterPhase phase) {
        switch (phase) {
            case EXPLORATION:
                return explorationAbilityCount;
            case ASSAULT:
                return assaultAbilityCount;
            case RAGE:
                return rageAbilityCount;
            case DESPERATION:
                return desperationAbilityCount;
            default:
                return 0;
        }
    }
    
    /**
     * Faz için hareket hızı çarpanını al
     */
    public double getSpeedMultiplier(DisasterPhase phase) {
        switch (phase) {
            case EXPLORATION:
                return explorationSpeedMultiplier;
            case ASSAULT:
                return assaultSpeedMultiplier;
            case RAGE:
                return rageSpeedMultiplier;
            case DESPERATION:
                return desperationSpeedMultiplier;
            default:
                return 1.0;
        }
    }
    
    /**
     * Faz için oyuncu saldırısı yapıyor mu?
     */
    public boolean shouldAttackPlayers(DisasterPhase phase) {
        switch (phase) {
            case EXPLORATION:
                return explorationAttackPlayers;
            case ASSAULT:
                return assaultAttackPlayers;
            case RAGE:
                return rageAttackPlayers;
            case DESPERATION:
                return desperationAttackPlayers;
            default:
                return false;
        }
    }
    
    // Getters
    public boolean isPhaseTransitionMessages() { return phaseTransitionMessages; }
    public boolean isPhaseTransitionEffects() { return phaseTransitionEffects; }
}

