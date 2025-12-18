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
    private long powerSystemPlayerNameUpdateInterval = 1200L; // ✅ OPTİMİZE: Oyuncu adı güncelleme aralığı (tick) - 600L -> 1200L (30 saniye) - performans için
    
    // ========== MAIN SYSTEM ==========
    
    // RayTrace ayarları
    private long mainRayTraceInterval = 20L;      // RayTrace güncelleme aralığı (tick)
    private int mainRayTraceMaxDistance = 50;     // RayTrace maksimum mesafe (blok)
    
    // ========== SIEGE SİSTEMİ ==========
    
    // Kuşatma yağma yüzdesi
    private double siegeLootPercentage = 0.5; // %50
    
    // Kuşatma sandık item yüzdesi (pes etme durumunda)
    private double siegeChestLootPercentage = 0.5; // %50
    private int siegeChunksPerTick = 5; // Her tick'te taranacak chunk sayısı
    private int siegeChestsPerTick = 2; // Her tick'te işlenecek sandık sayısı
    private int siegeMaxSearchRadius = 100; // Maksimum arama yarıçapı (blok)
    
    // ========== BUFF SİSTEMİ ==========
    
    // Fatih Buff çarpanları
    private double conquerorBuffDamageMultiplier = 0.2; // %20 hasar artışı
    private double conquerorBuffProductionMultiplier = 0.3; // %30 üretim hızı artışı
    
    // Kahraman Buff çarpanları
    private double heroBuffHealthMultiplier = 0.15; // %15 can artışı
    private double heroBuffDefenseMultiplier = 0.25; // %25 savunma artışı
    
    // ========== CONTRACT SİSTEMİ ==========
    
    // Kontrat ödül çarpanları
    private double contractRewardMultiplier = 0.5; // Ödül çarpanı
    private int contractDefaultDays = 7; // Varsayılan süre (gün)
    private double contractHealthRestorePerHeart = 2.0; // Her kalp = 2 can
    
    // ========== SHOP SİSTEMİ ==========
    
    // Market vergi oranı
    private double shopTaxPercentage = 0.05; // %5 vergi
    
    // ========== MISSION SİSTEMİ ==========
    
    // Görev ödülleri (seviye bazlı)
    private int missionTier1KillMobRewardAmount = 5; // Tier 1 öldürme görevi ödülü (Iron Ingot)
    private int missionTier1GatherRewardAmount = 3; // Tier 1 toplama görevi ödülü (Gold Ingot)
    private int missionTier2KillMobRewardAmount = 5; // Tier 2 öldürme görevi ödülü (Diamond)
    private int missionTier1KillMobTarget = 10; // Tier 1 öldürme hedefi
    private int missionTier1GatherTarget = 64; // Tier 1 toplama hedefi
    private int missionTier2KillMobTarget = 20; // Tier 2 öldürme hedefi
    private int missionTier2GatherTarget = 10; // Tier 2 toplama hedefi
    
    // ========== BOSS SİSTEMİ ==========
    
    // BossBar ayarları
    private int bossMaxBossBarDistance = 100; // BossBar gösterim mesafesi (blok)
    private int bossMaxBossBarsPerPlayer = 3; // Oyuncu başına maksimum BossBar sayısı
    
    // Boss cooldown'ları
    private long bossRitualCooldown = 60000L; // Ritüel cooldown (ms) - 60 saniye
    private long bossWeakPointDuration = 5000L; // Zayıf nokta süresi (ms) - 5 saniye
    private long bossShieldDuration = 3000L; // Kalkan süresi (ms) - 3 saniye
    
    // ========== TRAP SİSTEMİ ==========
    
    // Tuzak yakıt değerleri
    private int trapFuelDiamond = 5; // Elmas yakıt değeri
    private int trapFuelEmerald = 10; // Zümrüt yakıt değeri
    private int trapFuelTitanium = 20; // Titanyum yakıt değeri
    
    // Tuzak hasar değerleri
    private double trapHellTrapDamage = 3.0; // Cehennem tuzağı hasarı
    private double trapShockTrapDamage = 2.0; // Şok tuzağı hasarı
    private double trapMineDamage = 5.0; // Mayın hasarı
    private double trapPoisonTrapDamage = 0.5; // Zehir tuzağı hasarı
    private double trapBlackHoleDamage = 10.0; // Kara delik tuzağı hasarı
    
    // ========== MINE SİSTEMİ ==========
    
    // Mayın hasar değerleri (seviye bazlı)
    private double mineLevel1BaseDamage = 3.0; // Seviye 1 mayın temel hasarı
    private double mineLevel2BaseDamage = 5.0; // Seviye 2 mayın temel hasarı
    private double mineLevel3BaseDamage = 8.0; // Seviye 3 mayın temel hasarı
    private double mineLevel4BaseDamage = 12.0; // Seviye 4 mayın temel hasarı
    private double mineLevel5BaseDamage = 20.0; // Seviye 5 mayın temel hasarı
    
    // ========== MOB SİSTEMİ ==========
    
    // Özel mob can değerleri
    private double mobHellDragonHealth = 200.0; // Cehennem Ejderi canı
    private double mobTerrorWormHealth = 100.0; // Toprak Solucanı canı
    private double mobWarBearHealth = 150.0; // Savaş Ayısı canı
    private double mobShadowPantherHealth = 80.0; // Gölge Panteri canı
    private double mobWyvernHealth = 250.0; // Wyvern canı
    private double mobFireAmphiptereHealth = 120.0; // Ateş Amfibiterü canı
    private double mobFireAmphiptereDamage = 10.0; // Ateş Amfibiterü hasarı
    
    // Özel mob boyutları
    private int mobHellDragonSize = 20; // Cehennem Ejderi boyutu
    private int mobWyvernSize = 15; // Wyvern boyutu
    
    // Normal mob değerleri (sık gelen canavarlar)
    private double mobGoblinHealth = 30.0; // Goblin canı
    private double mobGoblinSpeed = 0.35; // Goblin hızı
    private double mobOrkHealth = 80.0; // Ork canı
    private double mobOrkDamage = 8.0; // Ork hasarı
    private double mobTrollHealth = 120.0; // Troll canı
    private double mobTrollSpeed = 0.2; // Troll hızı
    private double mobTrollDamage = 10.0; // Troll hasarı
    private double mobKnightHealth = 60.0; // İskelet Şövalye canı
    private double mobMageHealth = 50.0; // Karanlık Büyücü canı
    private double mobWerewolfHealth = 70.0; // Kurt Adam canı
    private double mobWerewolfDamage = 6.0; // Kurt Adam hasarı
    private double mobSpiderHealth = 60.0; // Dev Örümcek canı
    private double mobSpiderSpeed = 0.4; // Dev Örümcek hızı
    
    // ========== SUPPLY DROP SİSTEMİ ==========
    
    // Supply Drop ayarları
    private long supplyDropInterval = 3 * 60 * 60 * 20L; // 3 saat (tick cinsinden)
    private int supplyDropHeight = 50; // Drop yüksekliği (blok)
    private int supplyDropMinDiamond = 3; // Minimum elmas
    private int supplyDropMaxDiamond = 8; // Maksimum elmas
    private int supplyDropMinGold = 10; // Minimum altın
    private int supplyDropMaxGold = 30; // Maksimum altın
    private int supplyDropMinEmerald = 5; // Minimum zümrüt
    private int supplyDropMaxEmerald = 15; // Maksimum zümrüt
    private double supplyDropLightningCoreChance = 0.3; // Lightning Core şansı (0.0-1.0)
    private int supplyDropSpawnRange = 2000; // Spawn'dan uzaklık (blok)
    private int supplyDropSmokeEffectDuration = 100; // Duman efekti süresi (tick)
    
    // ========== CARAVAN SİSTEMİ ==========
    
    // Caravan ayarları
    private int caravanMinDistance = 1000; // Minimum mesafe (blok)
    private int caravanMinStacks = 20; // Minimum stack sayısı
    private double caravanMinValue = 5000.0; // Minimum değer (altın)
    private int caravanArrivalRadius = 5; // Varış yarıçapı (blok)
    private double caravanRewardMultiplier = 1.5; // Ödül çarpanı (x1.5)
    
    // ========== TAMING SİSTEMİ ==========
    
    // Taming ayarları
    private long tamingRitualCooldown = 30000L; // Ritüel cooldown (ms)
    
    // ========== BREEDING SİSTEMİ ==========
    
    // Breeding ayarları
    private long breedingLevel1Duration = 86400000L; // 1 gün (ms)
    private long breedingLevel2Duration = 172800000L; // 2 gün (ms)
    private long breedingLevel3Duration = 259200000L; // 3 gün (ms)
    private long breedingLevel4Duration = 345600000L; // 4 gün (ms)
    private long breedingLevel5Duration = 432000000L; // 5 gün (ms)
    private long breedingNaturalDuration = 60000L; // Doğal çiftleştirme süresi (1 dakika, ms)
    private int breedingMinFoodBlocks = 3; // Minimum yiyecek bloğu sayısı
    
    // ========== SPECIAL WEAPON SİSTEMİ ==========
    
    // Special Weapon ayarları
    private int weaponLevel3Range = 20; // Seviye 3 silah menzili (blok)
    private double weaponLevel3ExplosionPower = 3.0; // Seviye 3 patlama gücü
    private double weaponLevel3Damage = 10.0; // Seviye 3 hasar
    private int weaponLevel3Radius = 5; // Seviye 3 etki yarıçapı (blok)
    private long weaponLevel4LaserCooldown = 500L; // Seviye 4 lazer cooldown (ms)
    
    // ========== SPECIAL ARMOR SİSTEMİ ==========
    
    // Special Armor ayarları
    private double armorLevel2ThornDamageMultiplier = 0.3; // Seviye 2 diken hasar çarpanı (%30)
    private int armorLevel3SpeedDuration = 100; // Seviye 3 hız süresi (tick)
    private int armorLevel3SpeedLevel = 1; // Seviye 3 hız seviyesi
    private int armorLevel3JumpDuration = 100; // Seviye 3 zıplama süresi (tick)
    private int armorLevel3JumpLevel = 2; // Seviye 3 zıplama seviyesi
    private long armorLevel4RegenInterval = 1000L; // Seviye 4 can yenileme aralığı (ms)
    private double armorLevel4RegenAmount = 1.0; // Seviye 4 can yenileme miktarı
    private long armorLevel5DoubleJumpCooldown1 = 1000L; // Seviye 5 çift zıplama cooldown 1 (ms)
    private long armorLevel5DoubleJumpCooldown2 = 500L; // Seviye 5 çift zıplama cooldown 2 (ms)
    private long armorLevel5FlightDuration = 100L; // Seviye 5 uçma süresi (tick) - 5 saniye
    
    // ========== SPECIAL ITEM SİSTEMİ ==========
    
    // Special Item ayarları (Kanca sistemi)
    private double hookRustyMaxVertical = 3.0; // Paslı kanca maksimum dikey mesafe (blok)
    private double hookRustyPullStrength = 0.4; // Paslı kanca çekme gücü
    private double hookGoldenRange = 20.0; // Altın kanca menzili (blok)
    private double hookGoldenPullStrength = 1.2; // Altın kanca çekme gücü
    private double hookTitanRange = 40.0; // Titan kancası menzili (blok)
    private double hookTitanPullStrength = 2.5; // Titan kancası çekme gücü
    private long hookCooldown = 1000L; // Kanca cooldown (ms)
    private long hookFallDamageProtectionDuration = 3000L; // Düşme hasarı koruma süresi (ms)
    private long spyDuration = 3000L; // Casusluk dürbünü süresi (ms)
    
    // ========== RESEARCH SİSTEMİ ==========
    
    // Research ayarları
    private int researchTableDistance = 10; // Araştırma masası mesafesi (blok)
    
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
        mobRideTaskInterval = Math.max(40L, config.getLong(path + "mob-ride-interval", 40L)); // ✅ OPTİMİZE: En az 2 saniye (40 tick)
        
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
        powerSystemPlayerNameUpdateInterval = config.getLong(path + "player-name-update-interval", 1200L); // ✅ OPTİMİZE: Default 1200L (30 saniye)
        
        // Main System
        path = "game-balance.main.";
        mainRayTraceInterval = config.getLong(path + "raytrace-interval", 20L);
        mainRayTraceMaxDistance = config.getInt(path + "raytrace-max-distance", 50);
        
        // Siege sistemi
        path = "game-balance.siege.";
        siegeLootPercentage = config.getDouble(path + "loot-percentage", 0.5);
        siegeChestLootPercentage = config.getDouble(path + "chest-loot-percentage", 0.5);
        siegeChunksPerTick = config.getInt(path + "chunks-per-tick", 5);
        siegeChestsPerTick = config.getInt(path + "chests-per-tick", 2);
        siegeMaxSearchRadius = config.getInt(path + "max-search-radius", 100);
        
        // Buff sistemi
        path = "game-balance.buff.conqueror.";
        conquerorBuffDamageMultiplier = config.getDouble(path + "damage-multiplier", 0.2);
        conquerorBuffProductionMultiplier = config.getDouble(path + "production-multiplier", 0.3);
        
        path = "game-balance.buff.hero.";
        heroBuffHealthMultiplier = config.getDouble(path + "health-multiplier", 0.15);
        heroBuffDefenseMultiplier = config.getDouble(path + "defense-multiplier", 0.25);
        
        // Contract sistemi
        path = "game-balance.contract.";
        contractRewardMultiplier = config.getDouble(path + "reward-multiplier", 0.5);
        contractDefaultDays = config.getInt(path + "default-days", 7);
        contractHealthRestorePerHeart = config.getDouble(path + "health-restore-per-heart", 2.0);
        
        // Shop sistemi
        path = "game-balance.shop.";
        shopTaxPercentage = config.getDouble(path + "tax-percentage", 0.05);
        
        // Mission sistemi
        path = "game-balance.mission.tier1.";
        missionTier1KillMobRewardAmount = config.getInt(path + "kill-mob-reward-amount", 5);
        missionTier1GatherRewardAmount = config.getInt(path + "gather-reward-amount", 3);
        missionTier1KillMobTarget = config.getInt(path + "kill-mob-target", 10);
        missionTier1GatherTarget = config.getInt(path + "gather-target", 64);
        
        path = "game-balance.mission.tier2.";
        missionTier2KillMobRewardAmount = config.getInt(path + "kill-mob-reward-amount", 5);
        missionTier2KillMobTarget = config.getInt(path + "kill-mob-target", 20);
        missionTier2GatherTarget = config.getInt(path + "gather-target", 10);
        
        // Boss sistemi
        path = "game-balance.boss.";
        bossMaxBossBarDistance = config.getInt(path + "max-bossbar-distance", 100);
        bossMaxBossBarsPerPlayer = config.getInt(path + "max-bossbars-per-player", 3);
        bossRitualCooldown = config.getLong(path + "ritual-cooldown", 60000L);
        bossWeakPointDuration = config.getLong(path + "weak-point-duration", 5000L);
        bossShieldDuration = config.getLong(path + "shield-duration", 3000L);
        
        // Trap sistemi
        path = "game-balance.trap.fuel.";
        trapFuelDiamond = config.getInt(path + "diamond", 5);
        trapFuelEmerald = config.getInt(path + "emerald", 10);
        trapFuelTitanium = config.getInt(path + "titanium", 20);
        
        path = "game-balance.trap.damage.";
        trapHellTrapDamage = config.getDouble(path + "hell-trap", 3.0);
        trapShockTrapDamage = config.getDouble(path + "shock-trap", 2.0);
        trapMineDamage = config.getDouble(path + "mine", 5.0);
        trapPoisonTrapDamage = config.getDouble(path + "poison-trap", 0.5);
        trapBlackHoleDamage = config.getDouble(path + "black-hole", 10.0);
        
        // Mine sistemi
        path = "game-balance.mine.damage.";
        mineLevel1BaseDamage = config.getDouble(path + "level1-base", 3.0);
        mineLevel2BaseDamage = config.getDouble(path + "level2-base", 5.0);
        mineLevel3BaseDamage = config.getDouble(path + "level3-base", 8.0);
        mineLevel4BaseDamage = config.getDouble(path + "level4-base", 12.0);
        mineLevel5BaseDamage = config.getDouble(path + "level5-base", 20.0);
        
        // Mob sistemi
        path = "game-balance.mob.";
        mobHellDragonHealth = config.getDouble(path + "hell-dragon-health", 200.0);
        mobTerrorWormHealth = config.getDouble(path + "terror-worm-health", 100.0);
        mobWarBearHealth = config.getDouble(path + "war-bear-health", 150.0);
        mobShadowPantherHealth = config.getDouble(path + "shadow-panther-health", 80.0);
        mobWyvernHealth = config.getDouble(path + "wyvern-health", 250.0);
        mobFireAmphiptereHealth = config.getDouble(path + "fire-amphiptere-health", 120.0);
        mobFireAmphiptereDamage = config.getDouble(path + "fire-amphiptere-damage", 10.0);
        mobHellDragonSize = config.getInt(path + "hell-dragon-size", 20);
        mobWyvernSize = config.getInt(path + "wyvern-size", 15);
        
        // Normal mob değerleri
        path = "game-balance.mob.normal.";
        mobGoblinHealth = config.getDouble(path + "goblin-health", 30.0);
        mobGoblinSpeed = config.getDouble(path + "goblin-speed", 0.35);
        mobOrkHealth = config.getDouble(path + "ork-health", 80.0);
        mobOrkDamage = config.getDouble(path + "ork-damage", 8.0);
        mobTrollHealth = config.getDouble(path + "troll-health", 120.0);
        mobTrollSpeed = config.getDouble(path + "troll-speed", 0.2);
        mobTrollDamage = config.getDouble(path + "troll-damage", 10.0);
        mobKnightHealth = config.getDouble(path + "knight-health", 60.0);
        mobMageHealth = config.getDouble(path + "mage-health", 50.0);
        mobWerewolfHealth = config.getDouble(path + "werewolf-health", 70.0);
        mobWerewolfDamage = config.getDouble(path + "werewolf-damage", 6.0);
        mobSpiderHealth = config.getDouble(path + "spider-health", 60.0);
        mobSpiderSpeed = config.getDouble(path + "spider-speed", 0.4);
        
        // Supply Drop sistemi
        path = "game-balance.supply-drop.";
        supplyDropInterval = config.getLong(path + "interval", 3 * 60 * 60 * 20L);
        supplyDropHeight = config.getInt(path + "height", 50);
        supplyDropMinDiamond = config.getInt(path + "min-diamond", 3);
        supplyDropMaxDiamond = config.getInt(path + "max-diamond", 8);
        supplyDropMinGold = config.getInt(path + "min-gold", 10);
        supplyDropMaxGold = config.getInt(path + "max-gold", 30);
        supplyDropMinEmerald = config.getInt(path + "min-emerald", 5);
        supplyDropMaxEmerald = config.getInt(path + "max-emerald", 15);
        supplyDropLightningCoreChance = config.getDouble(path + "lightning-core-chance", 0.3);
        supplyDropSpawnRange = config.getInt(path + "spawn-range", 2000);
        supplyDropSmokeEffectDuration = config.getInt(path + "smoke-effect-duration", 100);
        
        // Caravan sistemi
        path = "game-balance.caravan.";
        caravanMinDistance = config.getInt(path + "min-distance", 1000);
        caravanMinStacks = config.getInt(path + "min-stacks", 20);
        caravanMinValue = config.getDouble(path + "min-value", 5000.0);
        caravanArrivalRadius = config.getInt(path + "arrival-radius", 5);
        caravanRewardMultiplier = config.getDouble(path + "reward-multiplier", 1.5);
        
        // Taming sistemi
        path = "game-balance.taming.";
        tamingRitualCooldown = config.getLong(path + "ritual-cooldown", 30000L);
        
        // Breeding sistemi
        path = "game-balance.breeding.";
        breedingLevel1Duration = config.getLong(path + "level1-duration", 86400000L);
        breedingLevel2Duration = config.getLong(path + "level2-duration", 172800000L);
        breedingLevel3Duration = config.getLong(path + "level3-duration", 259200000L);
        breedingLevel4Duration = config.getLong(path + "level4-duration", 345600000L);
        breedingLevel5Duration = config.getLong(path + "level5-duration", 432000000L);
        breedingNaturalDuration = config.getLong(path + "natural-duration", 60000L);
        breedingMinFoodBlocks = config.getInt(path + "min-food-blocks", 3);
        
        // Special Weapon sistemi
        path = "game-balance.special-weapon.";
        weaponLevel3Range = config.getInt(path + "level3-range", 20);
        weaponLevel3ExplosionPower = config.getDouble(path + "level3-explosion-power", 3.0);
        weaponLevel3Damage = config.getDouble(path + "level3-damage", 10.0);
        weaponLevel3Radius = config.getInt(path + "level3-radius", 5);
        weaponLevel4LaserCooldown = config.getLong(path + "level4-laser-cooldown", 500L);
        
        // Special Armor sistemi
        path = "game-balance.special-armor.";
        armorLevel2ThornDamageMultiplier = config.getDouble(path + "level2-thorn-damage-multiplier", 0.3);
        armorLevel3SpeedDuration = config.getInt(path + "level3-speed-duration", 100);
        armorLevel3SpeedLevel = config.getInt(path + "level3-speed-level", 1);
        armorLevel3JumpDuration = config.getInt(path + "level3-jump-duration", 100);
        armorLevel3JumpLevel = config.getInt(path + "level3-jump-level", 2);
        armorLevel4RegenInterval = config.getLong(path + "level4-regen-interval", 1000L);
        armorLevel4RegenAmount = config.getDouble(path + "level4-regen-amount", 1.0);
        armorLevel5DoubleJumpCooldown1 = config.getLong(path + "level5-double-jump-cooldown1", 1000L);
        armorLevel5DoubleJumpCooldown2 = config.getLong(path + "level5-double-jump-cooldown2", 500L);
        armorLevel5FlightDuration = config.getLong(path + "level5-flight-duration", 100L);
        
        // Special Item sistemi
        path = "game-balance.special-item.";
        hookRustyMaxVertical = config.getDouble(path + "hook-rusty-max-vertical", 3.0);
        hookRustyPullStrength = config.getDouble(path + "hook-rusty-pull-strength", 0.4);
        hookGoldenRange = config.getDouble(path + "hook-golden-range", 20.0);
        hookGoldenPullStrength = config.getDouble(path + "hook-golden-pull-strength", 1.2);
        hookTitanRange = config.getDouble(path + "hook-titan-range", 40.0);
        hookTitanPullStrength = config.getDouble(path + "hook-titan-pull-strength", 2.5);
        hookCooldown = config.getLong(path + "hook-cooldown", 1000L);
        hookFallDamageProtectionDuration = config.getLong(path + "hook-fall-damage-protection-duration", 3000L);
        spyDuration = config.getLong(path + "spy-duration", 3000L);
        
        // Research sistemi
        path = "game-balance.research.";
        researchTableDistance = config.getInt(path + "table-distance", 10);
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
    
    // Siege sistemi
    public double getSiegeLootPercentage() { return siegeLootPercentage; }
    public double getSiegeChestLootPercentage() { return siegeChestLootPercentage; }
    public int getSiegeChunksPerTick() { return siegeChunksPerTick; }
    public int getSiegeChestsPerTick() { return siegeChestsPerTick; }
    public int getSiegeMaxSearchRadius() { return siegeMaxSearchRadius; }
    
    // Buff sistemi
    public double getConquerorBuffDamageMultiplier() { return conquerorBuffDamageMultiplier; }
    public double getConquerorBuffProductionMultiplier() { return conquerorBuffProductionMultiplier; }
    public double getHeroBuffHealthMultiplier() { return heroBuffHealthMultiplier; }
    public double getHeroBuffDefenseMultiplier() { return heroBuffDefenseMultiplier; }
    
    // Contract sistemi
    public double getContractRewardMultiplier() { return contractRewardMultiplier; }
    public int getContractDefaultDays() { return contractDefaultDays; }
    public double getContractHealthRestorePerHeart() { return contractHealthRestorePerHeart; }
    
    // Shop sistemi
    public double getShopTaxPercentage() { return shopTaxPercentage; }
    
    // Mission sistemi
    public int getMissionTier1KillMobRewardAmount() { return missionTier1KillMobRewardAmount; }
    public int getMissionTier1GatherRewardAmount() { return missionTier1GatherRewardAmount; }
    public int getMissionTier2KillMobRewardAmount() { return missionTier2KillMobRewardAmount; }
    public int getMissionTier1KillMobTarget() { return missionTier1KillMobTarget; }
    public int getMissionTier1GatherTarget() { return missionTier1GatherTarget; }
    public int getMissionTier2KillMobTarget() { return missionTier2KillMobTarget; }
    public int getMissionTier2GatherTarget() { return missionTier2GatherTarget; }
    
    // Boss sistemi
    public int getBossMaxBossBarDistance() { return bossMaxBossBarDistance; }
    public int getBossMaxBossBarsPerPlayer() { return bossMaxBossBarsPerPlayer; }
    public long getBossRitualCooldown() { return bossRitualCooldown; }
    public long getBossWeakPointDuration() { return bossWeakPointDuration; }
    public long getBossShieldDuration() { return bossShieldDuration; }
    
    // Trap sistemi
    public int getTrapFuelDiamond() { return trapFuelDiamond; }
    public int getTrapFuelEmerald() { return trapFuelEmerald; }
    public int getTrapFuelTitanium() { return trapFuelTitanium; }
    public double getTrapHellTrapDamage() { return trapHellTrapDamage; }
    public double getTrapShockTrapDamage() { return trapShockTrapDamage; }
    public double getTrapMineDamage() { return trapMineDamage; }
    public double getTrapPoisonTrapDamage() { return trapPoisonTrapDamage; }
    public double getTrapBlackHoleDamage() { return trapBlackHoleDamage; }
    
    // Mine sistemi
    public double getMineLevel1BaseDamage() { return mineLevel1BaseDamage; }
    public double getMineLevel2BaseDamage() { return mineLevel2BaseDamage; }
    public double getMineLevel3BaseDamage() { return mineLevel3BaseDamage; }
    public double getMineLevel4BaseDamage() { return mineLevel4BaseDamage; }
    public double getMineLevel5BaseDamage() { return mineLevel5BaseDamage; }
    
    // Mob sistemi
    public double getMobHellDragonHealth() { return mobHellDragonHealth; }
    public double getMobTerrorWormHealth() { return mobTerrorWormHealth; }
    public double getMobWarBearHealth() { return mobWarBearHealth; }
    public double getMobShadowPantherHealth() { return mobShadowPantherHealth; }
    public double getMobWyvernHealth() { return mobWyvernHealth; }
    public double getMobFireAmphiptereHealth() { return mobFireAmphiptereHealth; }
    public double getMobFireAmphiptereDamage() { return mobFireAmphiptereDamage; }
    public int getMobHellDragonSize() { return mobHellDragonSize; }
    public int getMobWyvernSize() { return mobWyvernSize; }
    
    // Normal mob değerleri
    public double getMobGoblinHealth() { return mobGoblinHealth; }
    public double getMobGoblinSpeed() { return mobGoblinSpeed; }
    public double getMobOrkHealth() { return mobOrkHealth; }
    public double getMobOrkDamage() { return mobOrkDamage; }
    public double getMobTrollHealth() { return mobTrollHealth; }
    public double getMobTrollSpeed() { return mobTrollSpeed; }
    public double getMobTrollDamage() { return mobTrollDamage; }
    public double getMobKnightHealth() { return mobKnightHealth; }
    public double getMobMageHealth() { return mobMageHealth; }
    public double getMobWerewolfHealth() { return mobWerewolfHealth; }
    public double getMobWerewolfDamage() { return mobWerewolfDamage; }
    public double getMobSpiderHealth() { return mobSpiderHealth; }
    public double getMobSpiderSpeed() { return mobSpiderSpeed; }
    
    // Supply Drop sistemi
    public long getSupplyDropInterval() { return supplyDropInterval; }
    public int getSupplyDropHeight() { return supplyDropHeight; }
    public int getSupplyDropMinDiamond() { return supplyDropMinDiamond; }
    public int getSupplyDropMaxDiamond() { return supplyDropMaxDiamond; }
    public int getSupplyDropMinGold() { return supplyDropMinGold; }
    public int getSupplyDropMaxGold() { return supplyDropMaxGold; }
    public int getSupplyDropMinEmerald() { return supplyDropMinEmerald; }
    public int getSupplyDropMaxEmerald() { return supplyDropMaxEmerald; }
    public double getSupplyDropLightningCoreChance() { return supplyDropLightningCoreChance; }
    public int getSupplyDropSpawnRange() { return supplyDropSpawnRange; }
    public int getSupplyDropSmokeEffectDuration() { return supplyDropSmokeEffectDuration; }
    
    // Caravan sistemi
    public int getCaravanMinDistance() { return caravanMinDistance; }
    public int getCaravanMinStacks() { return caravanMinStacks; }
    public double getCaravanMinValue() { return caravanMinValue; }
    public int getCaravanArrivalRadius() { return caravanArrivalRadius; }
    public double getCaravanRewardMultiplier() { return caravanRewardMultiplier; }
    
    // Taming sistemi
    public long getTamingRitualCooldown() { return tamingRitualCooldown; }
    
    // Breeding sistemi
    public long getBreedingLevel1Duration() { return breedingLevel1Duration; }
    public long getBreedingLevel2Duration() { return breedingLevel2Duration; }
    public long getBreedingLevel3Duration() { return breedingLevel3Duration; }
    public long getBreedingLevel4Duration() { return breedingLevel4Duration; }
    public long getBreedingLevel5Duration() { return breedingLevel5Duration; }
    public long getBreedingNaturalDuration() { return breedingNaturalDuration; }
    public int getBreedingMinFoodBlocks() { return breedingMinFoodBlocks; }
    
    // Breeding seviye bazlı süre getter
    public long getBreedingDurationForLevel(int level) {
        switch (level) {
            case 1: return breedingLevel1Duration;
            case 2: return breedingLevel2Duration;
            case 3: return breedingLevel3Duration;
            case 4: return breedingLevel4Duration;
            case 5: return breedingLevel5Duration;
            default: return breedingLevel1Duration;
        }
    }
    
    // Special Weapon sistemi
    public int getWeaponLevel3Range() { return weaponLevel3Range; }
    public double getWeaponLevel3ExplosionPower() { return weaponLevel3ExplosionPower; }
    public double getWeaponLevel3Damage() { return weaponLevel3Damage; }
    public int getWeaponLevel3Radius() { return weaponLevel3Radius; }
    public long getWeaponLevel4LaserCooldown() { return weaponLevel4LaserCooldown; }
    
    // Special Armor sistemi
    public double getArmorLevel2ThornDamageMultiplier() { return armorLevel2ThornDamageMultiplier; }
    public int getArmorLevel3SpeedDuration() { return armorLevel3SpeedDuration; }
    public int getArmorLevel3SpeedLevel() { return armorLevel3SpeedLevel; }
    public int getArmorLevel3JumpDuration() { return armorLevel3JumpDuration; }
    public int getArmorLevel3JumpLevel() { return armorLevel3JumpLevel; }
    public long getArmorLevel4RegenInterval() { return armorLevel4RegenInterval; }
    public double getArmorLevel4RegenAmount() { return armorLevel4RegenAmount; }
    public long getArmorLevel5DoubleJumpCooldown1() { return armorLevel5DoubleJumpCooldown1; }
    public long getArmorLevel5DoubleJumpCooldown2() { return armorLevel5DoubleJumpCooldown2; }
    public long getArmorLevel5FlightDuration() { return armorLevel5FlightDuration; }
    
    // Special Item sistemi
    public double getHookRustyMaxVertical() { return hookRustyMaxVertical; }
    public double getHookRustyPullStrength() { return hookRustyPullStrength; }
    public double getHookGoldenRange() { return hookGoldenRange; }
    public double getHookGoldenPullStrength() { return hookGoldenPullStrength; }
    public double getHookTitanRange() { return hookTitanRange; }
    public double getHookTitanPullStrength() { return hookTitanPullStrength; }
    public long getHookCooldown() { return hookCooldown; }
    public long getHookFallDamageProtectionDuration() { return hookFallDamageProtectionDuration; }
    public long getSpyDuration() { return spyDuration; }
    
    // Research sistemi
    public int getResearchTableDistance() { return researchTableDistance; }
}

