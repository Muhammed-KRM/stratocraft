package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.BuffManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.manager.clan.config.ClanLevelBonusConfig;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Klan Seviye Bonus Sistemi
 * 
 * Özellikler:
 * - Seviye bazlı güç bonusları
 * - Seviye bazlı özellik erişimi
 * - Klan üyelerine otomatik bonus uygulama
 */
public class ClanLevelBonusSystem {
    private final Main plugin;
    private final StratocraftPowerSystem powerSystem;
    private final BuffManager buffManager;
    private ClanLevelBonusConfig config;
    
    public ClanLevelBonusSystem(Main plugin, StratocraftPowerSystem powerSystem, 
                               BuffManager buffManager) {
        this.plugin = plugin;
        this.powerSystem = powerSystem;
        this.buffManager = buffManager;
        this.config = new ClanLevelBonusConfig();
    }
    
    /**
     * Config yükle
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration fileConfig) {
        config.loadFromConfig(fileConfig);
    }
    
    /**
     * Klan seviyesini hesapla (cache ile optimizasyon)
     */
    private final java.util.Map<java.util.UUID, Integer> levelCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<java.util.UUID, Long> levelCacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long LEVEL_CACHE_DURATION = 30000L; // 30 saniye cache
    
    public int calculateClanLevel(Clan clan) {
        if (clan == null) return 1;
        
        // Cache kontrolü
        UUID clanId = clan.getId();
        long now = System.currentTimeMillis();
        Integer cachedLevel = levelCache.get(clanId);
        Long cacheTime = levelCacheTime.get(clanId);
        
        if (cachedLevel != null && cacheTime != null && now - cacheTime < LEVEL_CACHE_DURATION) {
            return cachedLevel;
        }
        
        // Null check
        if (powerSystem == null) {
            plugin.getLogger().warning("PowerSystem null! Klan seviyesi hesaplanamıyor.");
            return 1;
        }
        
        // Klan gücünü al
        me.mami.stratocraft.model.ClanPowerProfile profile = powerSystem.calculateClanProfile(clan);
        if (profile == null) {
            return 1;
        }
        
        double clanPower = profile.getTotalClanPower();
        
        // Geçersiz değer kontrolü
        if (clanPower < 0) {
            return 1;
        }
        
        // Güç bazlı seviye hesaplama (logaritmik)
        // Seviye = log10(güç / 1000) + 1
        if (clanPower < 1000) {
            levelCache.put(clanId, 1);
            levelCacheTime.put(clanId, now);
            return 1;
        }
        
        int level = (int) (Math.log10(clanPower / 1000.0) + 1);
        level = Math.max(1, Math.min(level, 15)); // 1-15 arası
        
        // Cache'e kaydet
        levelCache.put(clanId, level);
        levelCacheTime.put(clanId, now);
        
        return level;
    }
    
    /**
     * Klan seviyesine göre güç bonusu
     */
    public double getClanPowerBonus(Clan clan) {
        if (clan == null) return 0.0;
        
        int level = calculateClanLevel(clan);
        
        // Config null check
        if (config == null) {
            plugin.getLogger().warning("ClanLevelBonusConfig null! Bonus hesaplanamıyor.");
            return 0.0;
        }
        
        if (level <= 3) return 0.0; // Bonus yok
        if (level <= 7) return config.getLevel4PowerBonus(); // %5
        if (level <= 12) return config.getLevel8PowerBonus(); // %10
        return config.getLevel13PowerBonus(); // %15
    }
    
    /**
     * Klan seviyesine göre özellik kontrolü
     */
    public boolean hasClanFeature(Clan clan, ClanFeature feature) {
        if (clan == null || feature == null) return false;
        
        int level = calculateClanLevel(clan);
        return feature.getRequiredLevel() <= level;
    }
    
    /**
     * Klan üyelerine bonus uygula (optimize edilmiş - batch processing)
     * 
     * Not: BuffManager'da applyClanPowerBonus metodu yok, bu yüzden
     * klan seviye bonusu şimdilik sadece güç hesaplamasında kullanılacak.
     * Gelecekte BuffManager'a metod eklenebilir.
     */
    public void applyClanBonuses(Clan clan) {
        if (clan == null) return;
        
        double powerBonus = getClanPowerBonus(clan);
        if (powerBonus <= 0) return;
        
        // Not: BuffManager'da applyClanPowerBonus metodu yok
        // Klan seviye bonusu şimdilik sadece güç hesaplamasında kullanılacak
        // (StratocraftPowerSystem içinde hesaplanırken bonus uygulanabilir)
        
        // Gelecekte BuffManager'a metod eklendiğinde burada çağrılabilir:
        // if (buffManager != null) {
        //     buffManager.applyClanPowerBonus(member, powerBonus);
        // }
        
        // Şimdilik sadece log (debug için)
        plugin.getLogger().fine("Klan seviye bonusu hesaplandı: " + clan.getName() + 
            " - Bonus: " + (powerBonus * 100) + "%");
    }
    
    /**
     * Klan seviye bonusunu al (güç hesaplaması için)
     * Bu metod StratocraftPowerSystem tarafından çağrılabilir
     */
    public double getClanPowerBonusForCalculation(Clan clan) {
        return getClanPowerBonus(clan);
    }
    
    /**
     * Klan özellikleri enum
     */
    public enum ClanFeature {
        BASIC_CLAN_CHAT(1),
        CLAN_BANK(1),
        BASIC_FEATURES(1),
        POWER_BONUS_5(4),
        CLAN_MARKET(4),
        ALLIANCE_SYSTEM(4),
        ADVANCED_FEATURES(4),
        POWER_BONUS_10(8),
        CLAN_WARS(8),
        SPECIAL_STRUCTURES(8),
        STRONG_FEATURES(8),
        POWER_BONUS_15(13),
        CLAN_CAPITAL(13),
        SPECIAL_EVENTS(13),
        LEGENDARY_FEATURES(13);
        
        private final int requiredLevel;
        
        ClanFeature(int requiredLevel) {
            this.requiredLevel = requiredLevel;
        }
        
        public int getRequiredLevel() {
            return requiredLevel;
        }
    }
}

