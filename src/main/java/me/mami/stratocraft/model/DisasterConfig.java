package me.mami.stratocraft.model;

/**
 * Felaket Konfigürasyon Sınıfı
 * Her felaket tipi için ayarlanabilir tüm değerleri içerir
 */
public class DisasterConfig {
    // ========== GENEL AYARLAR ==========
    private double spawnDistance = 5000.0;          // Spawn mesafesi (blok)
    private long attackInterval = 120000L;          // Oyuncu saldırı aralığı (ms) - 2 dakika
    private double attackRadius = 30.0;             // Saldırı yarıçapı (blok)
    private double crystalProximity = 5.0;          // Kristal yakınlık (blok)
    private long crystalCacheInterval = 10000L;     // Cache güncelleme (ms) - 10 saniye
    private int chunkUnloadDelay = 200;             // Chunk unload gecikmesi (tick) - 10 saniye
    
    // ========== GÜÇ AYARLARI ==========
    private double baseHealth = 500.0;               // Temel can
    private double baseDamage = 1.0;                 // Temel hasar
    private double healthMultiplier = 1.0;           // Can çarpanı
    private double damageMultiplier = 1.0;           // Hasar çarpanı
    private double playerMultiplier = 0.1;           // Oyuncu başına çarpan
    private double clanMultiplier = 0.15;             // Klan seviyesi başına çarpan
    
    // ========== HAREKET AYARLARI ==========
    private double moveSpeed = 0.3;                  // Hareket hızı
    private double jumpHeight = 0.0;                 // Zıplama yüksekliği
    private boolean canJump = false;                 // Zıplama yapabilir mi?
    private boolean canTeleport = false;            // Işınlanabilir mi?
    private double teleportDistance = 5.0;          // Işınlanma mesafesi (blok)
    
    // ========== ÖZEL YETENEKLER ==========
    // Titan Golem
    private int jumpIntervalMin = 300;               // Zıplama aralığı min (tick)
    private int jumpIntervalMax = 400;               // Zıplama aralığı max (tick)
    private int blockThrowInterval = 200;            // Blok fırlatma aralığı (tick)
    private int explosionInterval = 200;            // Patlama aralığı (tick)
    private double explosionPower = 4.0;            // Patlama gücü
    private int blockBreakRadius = 3;                // Blok kırma yarıçapı
    private double passiveExplosionPower = 2.0;     // Pasif patlama gücü
    
    // Abyssal Worm
    private double digSpeed = 1.0;                  // Kazma hızı
    
    // Chaos Dragon
    private int fireBreathChance = 5;                // Ateş püskürtme şansı (%)
    private double fireBreathRange = 50.0;          // Ateş püskürtme menzili (blok)
    private double fireDamage = 5.0;                 // Ateş hasarı
    
    // Void Titan
    private int voidExplosionChance = 3;            // Boşluk patlaması şansı (%)
    private double voidExplosionPower = 4.0;        // Boşluk patlaması gücü
    private double voidExplosionRadius = 10.0;      // Boşluk patlaması yarıçapı (blok)
    
    // Ice Leviathan
    private int freezeChance = 5;                    // Donma şansı (%)
    private double freezeRadius = 30.0;              // Donma yarıçapı (blok)
    private int freezeDuration = 100;                // Donma süresi (tick)
    private int iceConversionChance = 30;            // Buz dönüşümü şansı (%)
    private int iceConversionRadius = 5;             // Buz dönüşümü yarıçapı (blok)
    
    // Grup Felaketler
    private int groupSize = 30;                      // Grup boyutu
    private int groupSizeMin = 100;                  // Mini dalga min boyut
    private int groupSizeMax = 500;                  // Mini dalga max boyut
    private double spawnRadius = 20.0;               // Spawn yarıçapı (blok)
    private double healthPercentage = 1.0;            // Can yüzdesi (mini dalga için %20 = 0.2)
    
    // Doğa Olayları - Solar Flare
    private int fireTickDuration = 100;              // Ateş tick süresi
    private double flammableChanceLog = 0.05;        // Log yakma şansı
    private double flammableChanceOther = 0.15;      // Diğer yanıcı blok yakma şansı
    private double lavaSpawnChance = 0.02;         // Lav oluşturma şansı
    private int scanRadius = 5;                      // Tarama yarıçapı (blok)
    
    // Doğa Olayları - Earthquake
    private int explosionChance = 5;                 // Patlama şansı (%)
    private double explosionRadius = 20.0;           // Patlama yarıçapı (blok)
    private double earthquakeExplosionPower = 3.0;   // Deprem patlama gücü
    private int damageInterval = 40;                  // Hasar aralığı (tick)
    private double damageAmount = 2.0;               // Hasar miktarı
    private int blockFallRadius = 2;                 // Blok düşme yarıçapı
    private int blockFallHeight = 5;                 // Blok düşme yüksekliği
    
    // Doğa Olayları - Storm
    private int lightningChanceNearby = 3;            // Yakın yıldırım şansı (%)
    private int lightningChanceRandom = 1;           // Rastgele yıldırım şansı (%)
    private double lightningRadius = 10.0;          // Yıldırım yarıçapı (blok)
    private double lightningDamage = 10.0;          // Yıldırım hasarı
    private double lightningDamageRadius = 5.0;      // Yıldırım hasar yarıçapı (blok)
    
    // Doğa Olayları - Meteor Shower
    private int meteorChance = 5;                    // Meteor düşürme şansı (%)
    private int meteorSpawnHeight = 50;              // Meteor spawn yüksekliği (blok)
    private double meteorSpawnRange = 50.0;          // Meteor spawn yarıçapı (blok)
    private double meteorExplosionPower = 4.0;        // Meteor patlama gücü
    private int meteorDamageRadius = 3;              // Meteor hasar yarıçapı (blok)
    
    // Doğa Olayları - Volcanic Eruption
    private int volcanicExplosionChance = 3;         // Volkanik patlama şansı (%)
    private double volcanicExplosionRadius = 30.0;    // Volkanik patlama yarıçapı (blok)
    private double volcanicExplosionPower = 5.0;      // Volkanik patlama gücü
    private int volcanicLavaSpawnChance = 1;         // Lav oluşturma şansı (%)
    private double volcanicLavaSpawnRadius = 20.0;    // Lav oluşturma yarıçapı (blok)
    private int volcanicFireDamageInterval = 40;     // Ateş hasar aralığı (tick)
    private double volcanicFireDamageAmount = 2.0;    // Ateş hasar miktarı
    
    // ========== CONSTRUCTOR ==========
    public DisasterConfig() {
        // Varsayılan değerler yukarıda tanımlı
    }
    
    // ========== CLONE METHOD ==========
    public DisasterConfig clone() {
        DisasterConfig clone = new DisasterConfig();
        clone.spawnDistance = this.spawnDistance;
        clone.attackInterval = this.attackInterval;
        clone.attackRadius = this.attackRadius;
        clone.crystalProximity = this.crystalProximity;
        clone.crystalCacheInterval = this.crystalCacheInterval;
        clone.chunkUnloadDelay = this.chunkUnloadDelay;
        clone.baseHealth = this.baseHealth;
        clone.baseDamage = this.baseDamage;
        clone.healthMultiplier = this.healthMultiplier;
        clone.damageMultiplier = this.damageMultiplier;
        clone.playerMultiplier = this.playerMultiplier;
        clone.clanMultiplier = this.clanMultiplier;
        clone.moveSpeed = this.moveSpeed;
        clone.jumpHeight = this.jumpHeight;
        clone.canJump = this.canJump;
        clone.canTeleport = this.canTeleport;
        clone.teleportDistance = this.teleportDistance;
        clone.jumpIntervalMin = this.jumpIntervalMin;
        clone.jumpIntervalMax = this.jumpIntervalMax;
        clone.blockThrowInterval = this.blockThrowInterval;
        clone.explosionInterval = this.explosionInterval;
        clone.explosionPower = this.explosionPower;
        clone.blockBreakRadius = this.blockBreakRadius;
        clone.passiveExplosionPower = this.passiveExplosionPower;
        clone.digSpeed = this.digSpeed;
        clone.fireBreathChance = this.fireBreathChance;
        clone.fireBreathRange = this.fireBreathRange;
        clone.fireDamage = this.fireDamage;
        clone.voidExplosionChance = this.voidExplosionChance;
        clone.voidExplosionPower = this.voidExplosionPower;
        clone.voidExplosionRadius = this.voidExplosionRadius;
        clone.freezeChance = this.freezeChance;
        clone.freezeRadius = this.freezeRadius;
        clone.freezeDuration = this.freezeDuration;
        clone.iceConversionChance = this.iceConversionChance;
        clone.iceConversionRadius = this.iceConversionRadius;
        clone.groupSize = this.groupSize;
        clone.groupSizeMin = this.groupSizeMin;
        clone.groupSizeMax = this.groupSizeMax;
        clone.spawnRadius = this.spawnRadius;
        clone.healthPercentage = this.healthPercentage;
        clone.fireTickDuration = this.fireTickDuration;
        clone.flammableChanceLog = this.flammableChanceLog;
        clone.flammableChanceOther = this.flammableChanceOther;
        clone.lavaSpawnChance = this.lavaSpawnChance;
        clone.scanRadius = this.scanRadius;
        clone.explosionChance = this.explosionChance;
        clone.explosionRadius = this.explosionRadius;
        clone.earthquakeExplosionPower = this.earthquakeExplosionPower;
        clone.damageInterval = this.damageInterval;
        clone.damageAmount = this.damageAmount;
        clone.blockFallRadius = this.blockFallRadius;
        clone.blockFallHeight = this.blockFallHeight;
        clone.lightningChanceNearby = this.lightningChanceNearby;
        clone.lightningChanceRandom = this.lightningChanceRandom;
        clone.lightningRadius = this.lightningRadius;
        clone.lightningDamage = this.lightningDamage;
        clone.lightningDamageRadius = this.lightningDamageRadius;
        clone.meteorChance = this.meteorChance;
        clone.meteorSpawnHeight = this.meteorSpawnHeight;
        clone.meteorSpawnRange = this.meteorSpawnRange;
        clone.meteorExplosionPower = this.meteorExplosionPower;
        clone.meteorDamageRadius = this.meteorDamageRadius;
        clone.volcanicExplosionChance = this.volcanicExplosionChance;
        clone.volcanicExplosionRadius = this.volcanicExplosionRadius;
        clone.volcanicExplosionPower = this.volcanicExplosionPower;
        clone.volcanicLavaSpawnChance = this.volcanicLavaSpawnChance;
        clone.volcanicLavaSpawnRadius = this.volcanicLavaSpawnRadius;
        clone.volcanicFireDamageInterval = this.volcanicFireDamageInterval;
        clone.volcanicFireDamageAmount = this.volcanicFireDamageAmount;
        return clone;
    }
    
    // ========== GETTERS & SETTERS ==========
    // Genel Ayarlar
    public double getSpawnDistance() { return spawnDistance; }
    public void setSpawnDistance(double spawnDistance) { this.spawnDistance = spawnDistance; }
    
    public long getAttackInterval() { return attackInterval; }
    public void setAttackInterval(long attackInterval) { this.attackInterval = attackInterval; }
    
    public double getAttackRadius() { return attackRadius; }
    public void setAttackRadius(double attackRadius) { this.attackRadius = attackRadius; }
    
    public double getCrystalProximity() { return crystalProximity; }
    public void setCrystalProximity(double crystalProximity) { this.crystalProximity = crystalProximity; }
    
    public long getCrystalCacheInterval() { return crystalCacheInterval; }
    public void setCrystalCacheInterval(long crystalCacheInterval) { this.crystalCacheInterval = crystalCacheInterval; }
    
    public int getChunkUnloadDelay() { return chunkUnloadDelay; }
    public void setChunkUnloadDelay(int chunkUnloadDelay) { this.chunkUnloadDelay = chunkUnloadDelay; }
    
    // Güç Ayarları
    public double getBaseHealth() { return baseHealth; }
    public void setBaseHealth(double baseHealth) { this.baseHealth = baseHealth; }
    
    public double getBaseDamage() { return baseDamage; }
    public void setBaseDamage(double baseDamage) { this.baseDamage = baseDamage; }
    
    public double getHealthMultiplier() { return healthMultiplier; }
    public void setHealthMultiplier(double healthMultiplier) { this.healthMultiplier = healthMultiplier; }
    
    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }
    
    public double getPlayerMultiplier() { return playerMultiplier; }
    public void setPlayerMultiplier(double playerMultiplier) { this.playerMultiplier = playerMultiplier; }
    
    public double getClanMultiplier() { return clanMultiplier; }
    public void setClanMultiplier(double clanMultiplier) { this.clanMultiplier = clanMultiplier; }
    
    // Hareket Ayarları
    public double getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(double moveSpeed) { this.moveSpeed = moveSpeed; }
    
    public double getJumpHeight() { return jumpHeight; }
    public void setJumpHeight(double jumpHeight) { this.jumpHeight = jumpHeight; }
    
    public boolean canJump() { return canJump; }
    public void setCanJump(boolean canJump) { this.canJump = canJump; }
    
    public boolean canTeleport() { return canTeleport; }
    public void setCanTeleport(boolean canTeleport) { this.canTeleport = canTeleport; }
    
    public double getTeleportDistance() { return teleportDistance; }
    public void setTeleportDistance(double teleportDistance) { this.teleportDistance = teleportDistance; }
    
    // Titan Golem
    public int getJumpIntervalMin() { return jumpIntervalMin; }
    public void setJumpIntervalMin(int jumpIntervalMin) { this.jumpIntervalMin = jumpIntervalMin; }
    
    public int getJumpIntervalMax() { return jumpIntervalMax; }
    public void setJumpIntervalMax(int jumpIntervalMax) { this.jumpIntervalMax = jumpIntervalMax; }
    
    public int getBlockThrowInterval() { return blockThrowInterval; }
    public void setBlockThrowInterval(int blockThrowInterval) { this.blockThrowInterval = blockThrowInterval; }
    
    public int getExplosionInterval() { return explosionInterval; }
    public void setExplosionInterval(int explosionInterval) { this.explosionInterval = explosionInterval; }
    
    public double getExplosionPower() { return explosionPower; }
    public void setExplosionPower(double explosionPower) { this.explosionPower = explosionPower; }
    
    public int getBlockBreakRadius() { return blockBreakRadius; }
    public void setBlockBreakRadius(int blockBreakRadius) { this.blockBreakRadius = blockBreakRadius; }
    
    public double getPassiveExplosionPower() { return passiveExplosionPower; }
    public void setPassiveExplosionPower(double passiveExplosionPower) { this.passiveExplosionPower = passiveExplosionPower; }
    
    // Abyssal Worm
    public double getDigSpeed() { return digSpeed; }
    public void setDigSpeed(double digSpeed) { this.digSpeed = digSpeed; }
    
    // Chaos Dragon
    public int getFireBreathChance() { return fireBreathChance; }
    public void setFireBreathChance(int fireBreathChance) { this.fireBreathChance = fireBreathChance; }
    
    public double getFireBreathRange() { return fireBreathRange; }
    public void setFireBreathRange(double fireBreathRange) { this.fireBreathRange = fireBreathRange; }
    
    public double getFireDamage() { return fireDamage; }
    public void setFireDamage(double fireDamage) { this.fireDamage = fireDamage; }
    
    // Void Titan
    public int getVoidExplosionChance() { return voidExplosionChance; }
    public void setVoidExplosionChance(int voidExplosionChance) { this.voidExplosionChance = voidExplosionChance; }
    
    public double getVoidExplosionPower() { return voidExplosionPower; }
    public void setVoidExplosionPower(double voidExplosionPower) { this.voidExplosionPower = voidExplosionPower; }
    
    public double getVoidExplosionRadius() { return voidExplosionRadius; }
    public void setVoidExplosionRadius(double voidExplosionRadius) { this.voidExplosionRadius = voidExplosionRadius; }
    
    // Ice Leviathan
    public int getFreezeChance() { return freezeChance; }
    public void setFreezeChance(int freezeChance) { this.freezeChance = freezeChance; }
    
    public double getFreezeRadius() { return freezeRadius; }
    public void setFreezeRadius(double freezeRadius) { this.freezeRadius = freezeRadius; }
    
    public int getFreezeDuration() { return freezeDuration; }
    public void setFreezeDuration(int freezeDuration) { this.freezeDuration = freezeDuration; }
    
    public int getIceConversionChance() { return iceConversionChance; }
    public void setIceConversionChance(int iceConversionChance) { this.iceConversionChance = iceConversionChance; }
    
    public int getIceConversionRadius() { return iceConversionRadius; }
    public void setIceConversionRadius(int iceConversionRadius) { this.iceConversionRadius = iceConversionRadius; }
    
    // Grup Felaketler
    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }
    
    public int getGroupSizeMin() { return groupSizeMin; }
    public void setGroupSizeMin(int groupSizeMin) { this.groupSizeMin = groupSizeMin; }
    
    public int getGroupSizeMax() { return groupSizeMax; }
    public void setGroupSizeMax(int groupSizeMax) { this.groupSizeMax = groupSizeMax; }
    
    public double getSpawnRadius() { return spawnRadius; }
    public void setSpawnRadius(double spawnRadius) { this.spawnRadius = spawnRadius; }
    
    public double getHealthPercentage() { return healthPercentage; }
    public void setHealthPercentage(double healthPercentage) { this.healthPercentage = healthPercentage; }
    
    // Solar Flare
    public int getFireTickDuration() { return fireTickDuration; }
    public void setFireTickDuration(int fireTickDuration) { this.fireTickDuration = fireTickDuration; }
    
    public double getFlammableChanceLog() { return flammableChanceLog; }
    public void setFlammableChanceLog(double flammableChanceLog) { this.flammableChanceLog = flammableChanceLog; }
    
    public double getFlammableChanceOther() { return flammableChanceOther; }
    public void setFlammableChanceOther(double flammableChanceOther) { this.flammableChanceOther = flammableChanceOther; }
    
    public double getLavaSpawnChance() { return lavaSpawnChance; }
    public void setLavaSpawnChance(double lavaSpawnChance) { this.lavaSpawnChance = lavaSpawnChance; }
    
    public int getScanRadius() { return scanRadius; }
    public void setScanRadius(int scanRadius) { this.scanRadius = scanRadius; }
    
    // Earthquake
    public int getExplosionChance() { return explosionChance; }
    public void setExplosionChance(int explosionChance) { this.explosionChance = explosionChance; }
    
    public double getExplosionRadius() { return explosionRadius; }
    public void setExplosionRadius(double explosionRadius) { this.explosionRadius = explosionRadius; }
    
    public double getEarthquakeExplosionPower() { return earthquakeExplosionPower; }
    public void setEarthquakeExplosionPower(double earthquakeExplosionPower) { this.earthquakeExplosionPower = earthquakeExplosionPower; }
    
    public int getDamageInterval() { return damageInterval; }
    public void setDamageInterval(int damageInterval) { this.damageInterval = damageInterval; }
    
    public double getDamageAmount() { return damageAmount; }
    public void setDamageAmount(double damageAmount) { this.damageAmount = damageAmount; }
    
    public int getBlockFallRadius() { return blockFallRadius; }
    public void setBlockFallRadius(int blockFallRadius) { this.blockFallRadius = blockFallRadius; }
    
    public int getBlockFallHeight() { return blockFallHeight; }
    public void setBlockFallHeight(int blockFallHeight) { this.blockFallHeight = blockFallHeight; }
    
    // Storm
    public int getLightningChanceNearby() { return lightningChanceNearby; }
    public void setLightningChanceNearby(int lightningChanceNearby) { this.lightningChanceNearby = lightningChanceNearby; }
    
    public int getLightningChanceRandom() { return lightningChanceRandom; }
    public void setLightningChanceRandom(int lightningChanceRandom) { this.lightningChanceRandom = lightningChanceRandom; }
    
    public double getLightningRadius() { return lightningRadius; }
    public void setLightningRadius(double lightningRadius) { this.lightningRadius = lightningRadius; }
    
    public double getLightningDamage() { return lightningDamage; }
    public void setLightningDamage(double lightningDamage) { this.lightningDamage = lightningDamage; }
    
    public double getLightningDamageRadius() { return lightningDamageRadius; }
    public void setLightningDamageRadius(double lightningDamageRadius) { this.lightningDamageRadius = lightningDamageRadius; }
    
    // Meteor Shower
    public int getMeteorChance() { return meteorChance; }
    public void setMeteorChance(int meteorChance) { this.meteorChance = meteorChance; }
    
    public int getMeteorSpawnHeight() { return meteorSpawnHeight; }
    public void setMeteorSpawnHeight(int meteorSpawnHeight) { this.meteorSpawnHeight = meteorSpawnHeight; }
    
    public double getMeteorSpawnRange() { return meteorSpawnRange; }
    public void setMeteorSpawnRange(double meteorSpawnRange) { this.meteorSpawnRange = meteorSpawnRange; }
    
    public double getMeteorExplosionPower() { return meteorExplosionPower; }
    public void setMeteorExplosionPower(double meteorExplosionPower) { this.meteorExplosionPower = meteorExplosionPower; }
    
    public int getMeteorDamageRadius() { return meteorDamageRadius; }
    public void setMeteorDamageRadius(int meteorDamageRadius) { this.meteorDamageRadius = meteorDamageRadius; }
    
    // Volcanic Eruption
    public int getVolcanicExplosionChance() { return volcanicExplosionChance; }
    public void setVolcanicExplosionChance(int volcanicExplosionChance) { this.volcanicExplosionChance = volcanicExplosionChance; }
    
    public double getVolcanicExplosionRadius() { return volcanicExplosionRadius; }
    public void setVolcanicExplosionRadius(double volcanicExplosionRadius) { this.volcanicExplosionRadius = volcanicExplosionRadius; }
    
    public double getVolcanicExplosionPower() { return volcanicExplosionPower; }
    public void setVolcanicExplosionPower(double volcanicExplosionPower) { this.volcanicExplosionPower = volcanicExplosionPower; }
    
    public int getVolcanicLavaSpawnChance() { return volcanicLavaSpawnChance; }
    public void setVolcanicLavaSpawnChance(int volcanicLavaSpawnChance) { this.volcanicLavaSpawnChance = volcanicLavaSpawnChance; }
    
    public double getVolcanicLavaSpawnRadius() { return volcanicLavaSpawnRadius; }
    public void setVolcanicLavaSpawnRadius(double volcanicLavaSpawnRadius) { this.volcanicLavaSpawnRadius = volcanicLavaSpawnRadius; }
    
    public int getVolcanicFireDamageInterval() { return volcanicFireDamageInterval; }
    public void setVolcanicFireDamageInterval(int volcanicFireDamageInterval) { this.volcanicFireDamageInterval = volcanicFireDamageInterval; }
    
    public double getVolcanicFireDamageAmount() { return volcanicFireDamageAmount; }
    public void setVolcanicFireDamageAmount(double volcanicFireDamageAmount) { this.volcanicFireDamageAmount = volcanicFireDamageAmount; }
}
