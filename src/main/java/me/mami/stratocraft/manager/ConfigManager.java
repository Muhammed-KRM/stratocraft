package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * ConfigManager - Tüm konfigürasyon değerlerini yönetir
 */
public class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;

    // ========== KLAN AYARLARI ==========
    private int maxClanMembers;
    private int maxClanGuests;
    
    // ========== KUŞATMA AYARLARI ==========
    private int siegeWarmupTime; // Saniye cinsinden
    private double siegeLootPercentage; // %50 = 0.5
    
    // ========== FELAKET AYARLARI ==========
    private long disasterCooldown; // Milisaniye cinsinden
    private int disasterWreckageSize; // Enkaz boyutu (5x5x3)
    
    // ========== MATKAP AYARLARI ==========
    private long drillInterval; // Tick cinsinden (20 tick = 1 saniye)
    private int drillMaxPerChunk; // Chunk başına maksimum matkap sayısı
    
    // ========== BUFF AYARLARI ==========
    private long conquerorBuffDuration; // Milisaniye
    private long heroBuffDuration; // Milisaniye
    
    // ========== WYVERN AYARLARI ==========
    private long wyvernFeedInterval; // Milisaniye
    private long wyvernCheckInterval; // Milisaniye
    
    // ========== RADAR AYARLARI ==========
    private int radarRange; // Blok cinsinden
    private long radarWarningCooldown; // Milisaniye
    
    // ========== YAPI AYARLARI ==========
    private int maxShieldFuel; // Maksimum kalkan yakıtı
    
    // ========== PERFORMANS AYARLARI ==========
    private boolean asyncSaving; // Async kayıt aktif mi?
    private int maxParticlesPerTick; // Tick başına maksimum partikül sayısı

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // Klan Ayarları
        maxClanMembers = config.getInt("clan.max-members", 10);
        maxClanGuests = config.getInt("clan.max-guests", 5);
        
        // Kuşatma Ayarları
        siegeWarmupTime = config.getInt("siege.warmup-time", 300); // 5 dakika
        siegeLootPercentage = config.getDouble("siege.loot-percentage", 0.5); // %50
        
        // Felaket Ayarları
        disasterCooldown = config.getLong("disaster.cooldown", 3600000L); // 1 saat
        disasterWreckageSize = config.getInt("disaster.wreckage-size", 5);
        
        // Matkap Ayarları
        drillInterval = config.getLong("drill.interval", 600L); // 30 saniye
        drillMaxPerChunk = config.getInt("drill.max-per-chunk", 3);
        
        // Buff Ayarları
        conquerorBuffDuration = config.getLong("buff.conqueror-duration", 86400000L); // 24 saat
        heroBuffDuration = config.getLong("buff.hero-duration", 172800000L); // 48 saat
        
        // Wyvern Ayarları
        wyvernFeedInterval = config.getLong("wyvern.feed-interval", 60000L); // 60 saniye
        wyvernCheckInterval = config.getLong("wyvern.check-interval", 1000L); // 1 saniye
        
        // Radar Ayarları
        radarRange = config.getInt("radar.range", 200);
        radarWarningCooldown = config.getLong("radar.warning-cooldown", 10000L); // 10 saniye
        
        // Yapı Ayarları
        maxShieldFuel = config.getInt("structure.max-shield-fuel", 43200); // 12 saat (saniye cinsinden)
        
        // Performans Ayarları
        asyncSaving = config.getBoolean("performance.async-saving", true);
        maxParticlesPerTick = config.getInt("performance.max-particles-per-tick", 50);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    // ========== GETTERS ==========
    
    public int getMaxClanMembers() { return maxClanMembers; }
    public int getMaxClanGuests() { return maxClanGuests; }
    public int getSiegeWarmupTime() { return siegeWarmupTime; }
    public double getSiegeLootPercentage() { return siegeLootPercentage; }
    public long getDisasterCooldown() { return disasterCooldown; }
    public int getDisasterWreckageSize() { return disasterWreckageSize; }
    public long getDrillInterval() { return drillInterval; }
    public int getDrillMaxPerChunk() { return drillMaxPerChunk; }
    public long getConquerorBuffDuration() { return conquerorBuffDuration; }
    public long getHeroBuffDuration() { return heroBuffDuration; }
    public long getWyvernFeedInterval() { return wyvernFeedInterval; }
    public long getWyvernCheckInterval() { return wyvernCheckInterval; }
    public int getRadarRange() { return radarRange; }
    public long getRadarWarningCooldown() { return radarWarningCooldown; }
    public int getMaxShieldFuel() { return maxShieldFuel; }
    public boolean isAsyncSaving() { return asyncSaving; }
    public int getMaxParticlesPerTick() { return maxParticlesPerTick; }
}

