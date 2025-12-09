package me.mami.stratocraft.manager;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Oyun Denge Ayarları Konfigürasyonu
 * Tüm sistemlerdeki hardcoded değerleri config'den yönetir
 */
public class GameBalanceConfig {
    
    // ========== BATARYA SİSTEMİ ==========
    
    // Batarya hasar değerleri (seviye bazlı)
    private double batteryLevel1BaseDamage = 5.0;
    private double batteryLevel2BaseDamage = 10.0;
    private double batteryLevel3BaseDamage = 50.0;
    private double batteryLevel4BaseDamage = 100.0;
    private double batteryLevel5BaseDamage = 300.0;
    
    // Özel batarya hasarları
    private double batteryLevel3LightningStormDamage = 50.0;
    private double batteryLevel3IceAgeDamage = 70.0;
    private double batteryLevel4TeslaTowerDamage = 100.0;
    private double batteryLevel4DeathCloudDamage = 120.0;
    private double batteryLevel4ElectricShieldDamage = 70.0;
    private double batteryLevel5ApocalypseReactorDamage = 300.0;
    private double batteryLevel5BossKillerDamage = 300.0;
    private double batteryLevel5AreaDestroyerDamage = 300.0;
    private double batteryLevel5MountainDestroyerDamage = 300.0;
    
    // Yakıt çarpanları
    private double fuelDarkMatterMultiplier = 10.0;
    private double fuelRedDiamondMultiplier = 5.0;
    private double fuelDiamondMultiplier = 2.5;
    private double fuelIronMultiplier = 1.0;
    
    // Batarya RayTrace mesafeleri
    private int batteryRayTraceMaxDistance = 50;
    private int batteryRayTraceShortDistance = 30;
    
    // Batarya radius ve duration değerleri (seviye bazlı)
    private int batteryLevel1BaseRadius = 5;
    private int batteryLevel2BaseRadius = 5;
    private int batteryLevel3BaseRadius = 7;
    private int batteryLevel4BaseRadius = 10;
    private int batteryLevel5BaseRadius = 40;
    
    // Özel batarya radius/duration değerleri
    private int batteryLevel3LightningStormRadius = 7;
    private int batteryLevel3LightningStormDuration = 5;
    private int batteryLevel5BossKillerBossDamage = 300;
    private int batteryLevel5BossKillerNormalDamage = 100;
    private int batteryLevel5BossKillerRadius = 50;
    private int batteryLevel5AreaDestroyerRadius = 50;
    private double batteryLevel5AreaDestroyerExplosion = 8.0;
    private int batteryLevel5MountainDestroyerRadius = 50;
    private int batteryLevel5MountainDestroyerHeight = 15;
    private double batteryLevel5MountainDestroyerExplosion = 10.0;
    private double batteryLevel5ApocalypseReactorRadius = 40.0;
    private double batteryLevel5ApocalypseReactorExplosion = 10.0;
    
    // ========== RİTÜEL SİSTEMİ ==========
    
    // Ritüel cooldown
    private long ritualCooldown = 10000L; // 10 saniye (ms)
    
    // ========== TASK INTERVALS ==========
    
    // MobRideTask interval
    private long mobRideTaskInterval = 5L; // 5 tick (0.25 saniye)
    
    // ========== TRAINING SİSTEMİ ==========
    
    // Kullanım eşikleri
    private int trainingFullPowerUses = 5;        // Tam güce ulaşma (5 kullanım)
    private int trainingMasteryStartUses = 20;    // Ustalaşma başlangıcı (20 kullanım)
    private int trainingMaxPowerUses = 30;       // Maksimum güç (30 kullanım)
    
    // Seviye bazlı başlangıç güçleri
    private double trainingLevel1StartPower = 0.2;  // %20
    private double trainingLevel2StartPower = 0.4;  // %40
    private double trainingLevel3StartPower = 0.6;  // %60
    private double trainingLevel4StartPower = 0.7;  // %70
    private double trainingLevel5StartPower = 0.8;  // %80
    
    // Güç artış değerleri
    private double trainingPowerIncrement2 = 0.2;   // 2. kullanımda +%20
    private double trainingPowerIncrement3 = 0.4;   // 3. kullanımda +%40
    private double trainingPowerIncrement4 = 0.6;   // 4. kullanımda +%60
    private double trainingMaxPowerMultiplier = 1.5; // Maksimum güç çarpanı (%150)
    private double trainingMasteryPowerIncrement = 0.5; // Ustalaşma güç artışı (10 kullanımda +%50)
    
    // ========== POWER SYSTEM ==========
    
    // PowerSystemListener ayarları
    private long powerSystemSlotUpdateCooldown = 500L;  // Slot değişikliği cooldown (ms)
    private long powerSystemPlayerNameUpdateInterval = 600L; // Oyuncu adı güncelleme aralığı (tick)
    
    // ========== MAIN SYSTEM ==========
    
    // RayTrace ayarları
    private long mainRayTraceInterval = 20L;      // RayTrace güncelleme aralığı (tick)
    private int mainRayTraceMaxDistance = 50;     // RayTrace maksimum mesafe (blok)
    
    // ========== CONFIG YÜKLEME ==========
    
    public void loadFromConfig(FileConfiguration config) {
        String path;
        
        // Batarya hasar değerleri
        path = "game-balance.battery.damage.";
        batteryLevel1BaseDamage = config.getDouble(path + "level1-base", 5.0);
        batteryLevel2BaseDamage = config.getDouble(path + "level2-base", 10.0);
        batteryLevel3BaseDamage = config.getDouble(path + "level3-base", 50.0);
        batteryLevel4BaseDamage = config.getDouble(path + "level4-base", 100.0);
        batteryLevel5BaseDamage = config.getDouble(path + "level5-base", 300.0);
        
        path = "game-balance.battery.damage.special.";
        batteryLevel3LightningStormDamage = config.getDouble(path + "level3-lightning-storm", 50.0);
        batteryLevel3IceAgeDamage = config.getDouble(path + "level3-ice-age", 70.0);
        batteryLevel4TeslaTowerDamage = config.getDouble(path + "level4-tesla-tower", 100.0);
        batteryLevel4DeathCloudDamage = config.getDouble(path + "level4-death-cloud", 120.0);
        batteryLevel4ElectricShieldDamage = config.getDouble(path + "level4-electric-shield", 70.0);
        batteryLevel5ApocalypseReactorDamage = config.getDouble(path + "level5-apocalypse-reactor", 300.0);
        batteryLevel5BossKillerDamage = config.getDouble(path + "level5-boss-killer", 300.0);
        batteryLevel5AreaDestroyerDamage = config.getDouble(path + "level5-area-destroyer", 300.0);
        batteryLevel5MountainDestroyerDamage = config.getDouble(path + "level5-mountain-destroyer", 300.0);
        
        // Yakıt çarpanları
        path = "game-balance.battery.fuel-multipliers.";
        fuelDarkMatterMultiplier = config.getDouble(path + "dark-matter", 10.0);
        fuelRedDiamondMultiplier = config.getDouble(path + "red-diamond", 5.0);
        fuelDiamondMultiplier = config.getDouble(path + "diamond", 2.5);
        fuelIronMultiplier = config.getDouble(path + "iron", 1.0);
        
        // Batarya RayTrace mesafeleri
        path = "game-balance.battery.raytrace.";
        batteryRayTraceMaxDistance = config.getInt(path + "max-distance", 50);
        batteryRayTraceShortDistance = config.getInt(path + "short-distance", 30);
        
        // Batarya radius ve duration
        path = "game-balance.battery.radius.";
        batteryLevel1BaseRadius = config.getInt(path + "level1-base", 5);
        batteryLevel2BaseRadius = config.getInt(path + "level2-base", 5);
        batteryLevel3BaseRadius = config.getInt(path + "level3-base", 7);
        batteryLevel4BaseRadius = config.getInt(path + "level4-base", 10);
        batteryLevel5BaseRadius = config.getInt(path + "level5-base", 40);
        
        path = "game-balance.battery.special.";
        batteryLevel3LightningStormRadius = config.getInt(path + "level3-lightning-storm-radius", 7);
        batteryLevel3LightningStormDuration = config.getInt(path + "level3-lightning-storm-duration", 5);
        batteryLevel5BossKillerBossDamage = config.getInt(path + "level5-boss-killer-boss-damage", 300);
        batteryLevel5BossKillerNormalDamage = config.getInt(path + "level5-boss-killer-normal-damage", 100);
        batteryLevel5BossKillerRadius = config.getInt(path + "level5-boss-killer-radius", 50);
        batteryLevel5AreaDestroyerRadius = config.getInt(path + "level5-area-destroyer-radius", 50);
        batteryLevel5AreaDestroyerExplosion = config.getDouble(path + "level5-area-destroyer-explosion", 8.0);
        batteryLevel5MountainDestroyerRadius = config.getInt(path + "level5-mountain-destroyer-radius", 50);
        batteryLevel5MountainDestroyerHeight = config.getInt(path + "level5-mountain-destroyer-height", 15);
        batteryLevel5MountainDestroyerExplosion = config.getDouble(path + "level5-mountain-destroyer-explosion", 10.0);
        batteryLevel5ApocalypseReactorRadius = config.getDouble(path + "level5-apocalypse-reactor-radius", 40.0);
        batteryLevel5ApocalypseReactorExplosion = config.getDouble(path + "level5-apocalypse-reactor-explosion", 10.0);
        
        // Ritüel sistemi
        path = "game-balance.ritual.";
        ritualCooldown = config.getLong(path + "cooldown", 10000L);
        
        // Task intervals
        path = "game-balance.tasks.";
        mobRideTaskInterval = config.getLong(path + "mob-ride-interval", 5L);
        
        // Training sistemi
        path = "game-balance.training.thresholds.";
        trainingFullPowerUses = config.getInt(path + "full-power", 5);
        trainingMasteryStartUses = config.getInt(path + "mastery-start", 20);
        trainingMaxPowerUses = config.getInt(path + "max-power", 30);
        
        path = "game-balance.training.start-power.";
        trainingLevel1StartPower = config.getDouble(path + "level1", 0.2);
        trainingLevel2StartPower = config.getDouble(path + "level2", 0.4);
        trainingLevel3StartPower = config.getDouble(path + "level3", 0.6);
        trainingLevel4StartPower = config.getDouble(path + "level4", 0.7);
        trainingLevel5StartPower = config.getDouble(path + "level5", 0.8);
        
        path = "game-balance.training.power-increments.";
        trainingPowerIncrement2 = config.getDouble(path + "use2", 0.2);
        trainingPowerIncrement3 = config.getDouble(path + "use3", 0.4);
        trainingPowerIncrement4 = config.getDouble(path + "use4", 0.6);
        trainingMaxPowerMultiplier = config.getDouble(path + "max-multiplier", 1.5);
        trainingMasteryPowerIncrement = config.getDouble(path + "mastery-increment", 0.5);
        
        // Power System
        path = "game-balance.power-system.";
        powerSystemSlotUpdateCooldown = config.getLong(path + "slot-update-cooldown", 500L);
        powerSystemPlayerNameUpdateInterval = config.getLong(path + "player-name-update-interval", 600L);
        
        // Main System
        path = "game-balance.main.";
        mainRayTraceInterval = config.getLong(path + "raytrace-interval", 20L);
        mainRayTraceMaxDistance = config.getInt(path + "raytrace-max-distance", 50);
    }
    
    // ========== GETTERS ==========
    
    // Batarya hasar değerleri
    public double getBatteryLevel1BaseDamage() { return batteryLevel1BaseDamage; }
    public double getBatteryLevel2BaseDamage() { return batteryLevel2BaseDamage; }
    public double getBatteryLevel3BaseDamage() { return batteryLevel3BaseDamage; }
    public double getBatteryLevel4BaseDamage() { return batteryLevel4BaseDamage; }
    public double getBatteryLevel5BaseDamage() { return batteryLevel5BaseDamage; }
    
    public double getBatteryLevel3LightningStormDamage() { return batteryLevel3LightningStormDamage; }
    public double getBatteryLevel3IceAgeDamage() { return batteryLevel3IceAgeDamage; }
    public double getBatteryLevel4TeslaTowerDamage() { return batteryLevel4TeslaTowerDamage; }
    public double getBatteryLevel4DeathCloudDamage() { return batteryLevel4DeathCloudDamage; }
    public double getBatteryLevel4ElectricShieldDamage() { return batteryLevel4ElectricShieldDamage; }
    public double getBatteryLevel5ApocalypseReactorDamage() { return batteryLevel5ApocalypseReactorDamage; }
    public double getBatteryLevel5BossKillerDamage() { return batteryLevel5BossKillerDamage; }
    public double getBatteryLevel5AreaDestroyerDamage() { return batteryLevel5AreaDestroyerDamage; }
    public double getBatteryLevel5MountainDestroyerDamage() { return batteryLevel5MountainDestroyerDamage; }
    
    // Yakıt çarpanları
    public double getFuelDarkMatterMultiplier() { return fuelDarkMatterMultiplier; }
    public double getFuelRedDiamondMultiplier() { return fuelRedDiamondMultiplier; }
    public double getFuelDiamondMultiplier() { return fuelDiamondMultiplier; }
    public double getFuelIronMultiplier() { return fuelIronMultiplier; }
    
    // Batarya RayTrace mesafeleri
    public int getBatteryRayTraceMaxDistance() { return batteryRayTraceMaxDistance; }
    public int getBatteryRayTraceShortDistance() { return batteryRayTraceShortDistance; }
    
    // Batarya radius ve duration
    public int getBatteryLevel1BaseRadius() { return batteryLevel1BaseRadius; }
    public int getBatteryLevel2BaseRadius() { return batteryLevel2BaseRadius; }
    public int getBatteryLevel3BaseRadius() { return batteryLevel3BaseRadius; }
    public int getBatteryLevel4BaseRadius() { return batteryLevel4BaseRadius; }
    public int getBatteryLevel5BaseRadius() { return batteryLevel5BaseRadius; }
    
    public int getBatteryLevel3LightningStormRadius() { return batteryLevel3LightningStormRadius; }
    public int getBatteryLevel3LightningStormDuration() { return batteryLevel3LightningStormDuration; }
    public int getBatteryLevel5BossKillerBossDamage() { return batteryLevel5BossKillerBossDamage; }
    public int getBatteryLevel5BossKillerNormalDamage() { return batteryLevel5BossKillerNormalDamage; }
    public int getBatteryLevel5BossKillerRadius() { return batteryLevel5BossKillerRadius; }
    public int getBatteryLevel5AreaDestroyerRadius() { return batteryLevel5AreaDestroyerRadius; }
    public double getBatteryLevel5AreaDestroyerExplosion() { return batteryLevel5AreaDestroyerExplosion; }
    public int getBatteryLevel5MountainDestroyerRadius() { return batteryLevel5MountainDestroyerRadius; }
    public int getBatteryLevel5MountainDestroyerHeight() { return batteryLevel5MountainDestroyerHeight; }
    public double getBatteryLevel5MountainDestroyerExplosion() { return batteryLevel5MountainDestroyerExplosion; }
    public double getBatteryLevel5ApocalypseReactorRadius() { return batteryLevel5ApocalypseReactorRadius; }
    public double getBatteryLevel5ApocalypseReactorExplosion() { return batteryLevel5ApocalypseReactorExplosion; }
    
    // Ritüel sistemi
    public long getRitualCooldown() { return ritualCooldown; }
    
    // Task intervals
    public long getMobRideTaskInterval() { return mobRideTaskInterval; }
    
    // Training sistemi
    public int getTrainingFullPowerUses() { return trainingFullPowerUses; }
    public int getTrainingMasteryStartUses() { return trainingMasteryStartUses; }
    public int getTrainingMaxPowerUses() { return trainingMaxPowerUses; }
    
    public double getTrainingLevel1StartPower() { return trainingLevel1StartPower; }
    public double getTrainingLevel2StartPower() { return trainingLevel2StartPower; }
    public double getTrainingLevel3StartPower() { return trainingLevel3StartPower; }
    public double getTrainingLevel4StartPower() { return trainingLevel4StartPower; }
    public double getTrainingLevel5StartPower() { return trainingLevel5StartPower; }
    
    public double getTrainingPowerIncrement2() { return trainingPowerIncrement2; }
    public double getTrainingPowerIncrement3() { return trainingPowerIncrement3; }
    public double getTrainingPowerIncrement4() { return trainingPowerIncrement4; }
    public double getTrainingMaxPowerMultiplier() { return trainingMaxPowerMultiplier; }
    public double getTrainingMasteryPowerIncrement() { return trainingMasteryPowerIncrement; }
    
    // Power System
    public long getPowerSystemSlotUpdateCooldown() { return powerSystemSlotUpdateCooldown; }
    public long getPowerSystemPlayerNameUpdateInterval() { return powerSystemPlayerNameUpdateInterval; }
    
    // Main System
    public long getMainRayTraceInterval() { return mainRayTraceInterval; }
    public int getMainRayTraceMaxDistance() { return mainRayTraceMaxDistance; }
}

