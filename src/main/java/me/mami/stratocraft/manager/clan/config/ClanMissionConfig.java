package me.mami.stratocraft.manager.clan.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Klan Görevleri Config
 * Tüm görev ayarları config'den okunur
 */
public class ClanMissionConfig {
    // Görev süresi
    private long missionDuration = 604800000L; // 7 gün (ms)
    
    // Ödüller
    private int rewardDiamond = 5;      // Elmas ödülü
    private int rewardGold = 10;        // Altın ödülü
    private int rewardXP = 100;         // XP ödülü
    
    /**
     * Config'den yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        if (config == null) {
            loadDefaults();
            return;
        }
        
        String path = "clan.mission-system.";
        
        // Görev süresi
        missionDuration = config.getLong(path + "mission-duration", 604800000L);
        
        // Ödüller
        rewardDiamond = config.getInt(path + "rewards.diamond", 5);
        rewardGold = config.getInt(path + "rewards.gold", 10);
        rewardXP = config.getInt(path + "rewards.xp", 100);
        
        // Geçersiz değer kontrolleri
        if (missionDuration < 0) {
            missionDuration = 604800000L; // Varsayılan
        }
        if (rewardDiamond < 0) {
            rewardDiamond = 5; // Varsayılan
        }
        if (rewardGold < 0) {
            rewardGold = 10; // Varsayılan
        }
        if (rewardXP < 0) {
            rewardXP = 100; // Varsayılan
        }
    }
    
    /**
     * Varsayılan değerleri yükle
     */
    private void loadDefaults() {
        missionDuration = 604800000L;
        rewardDiamond = 5;
        rewardGold = 10;
        rewardXP = 100;
    }
    
    // Getters
    public long getMissionDuration() { return missionDuration; }
    public int getRewardDiamond() { return rewardDiamond; }
    public int getRewardGold() { return rewardGold; }
    public int getRewardXP() { return rewardXP; }
}

