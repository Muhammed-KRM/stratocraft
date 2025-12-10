package me.mami.stratocraft.manager.clan.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Klan Koruma Sistemi Config
 * Tüm koruma değerleri config'den okunur
 */
public class ClanProtectionConfig {
    // Güç bazlı koruma
    private double powerThreshold = 0.40; // %40 eşik
    
    // Seviye bazlı koruma
    private int maxLevelDiff = 5; // Maksimum seviye farkı
    
    // Acemi koruması
    private double rookiePowerThreshold = 3000.0; // 3,000 güç
    private int rookieLevelThreshold = 5; // Seviye 5
    private double strongPlayerThreshold = 10000.0; // 10,000 güç (güçlü oyuncu)
    
    // Aktivite koruması
    private long inactiveThreshold = 604800000L; // 7 gün (ms)
    
    // Klan içi koruma
    private double clanThreshold = 0.50; // %50 eşik
    
    // Hasar azaltma
    private double damageReductionMin = 0.05; // Minimum hasar (%5)
    private double damageReductionMax = 0.50; // Maksimum hasar (%50)
    
    /**
     * Config'den yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        if (config == null) {
            // Varsayılan değerler zaten set edilmiş
            return;
        }
        
        String path = "clan.protection-system.";
        
        powerThreshold = config.getDouble(path + "power-threshold", 0.40);
        maxLevelDiff = config.getInt(path + "max-level-diff", 5);
        rookiePowerThreshold = config.getDouble(path + "rookie-power-threshold", 3000.0);
        rookieLevelThreshold = config.getInt(path + "rookie-level-threshold", 5);
        strongPlayerThreshold = config.getDouble(path + "strong-player-threshold", 10000.0);
        inactiveThreshold = config.getLong(path + "inactive-threshold", 604800000L);
        clanThreshold = config.getDouble(path + "clan-threshold", 0.50);
        damageReductionMin = config.getDouble(path + "damage-reduction-min", 0.05);
        damageReductionMax = config.getDouble(path + "damage-reduction-max", 0.50);
        
        // Geçersiz değer kontrolü
        if (powerThreshold < 0 || powerThreshold > 1) {
            powerThreshold = 0.40; // Varsayılan
        }
        if (maxLevelDiff < 0) {
            maxLevelDiff = 5; // Varsayılan
        }
        if (rookiePowerThreshold < 0) {
            rookiePowerThreshold = 3000.0; // Varsayılan
        }
        if (rookieLevelThreshold < 0) {
            rookieLevelThreshold = 5; // Varsayılan
        }
        if (strongPlayerThreshold < 0) {
            strongPlayerThreshold = 10000.0; // Varsayılan
        }
        if (inactiveThreshold < 0) {
            inactiveThreshold = 604800000L; // Varsayılan
        }
        if (clanThreshold < 0 || clanThreshold > 1) {
            clanThreshold = 0.50; // Varsayılan
        }
        if (damageReductionMin < 0 || damageReductionMin > 1) {
            damageReductionMin = 0.05; // Varsayılan
        }
        if (damageReductionMax < 0 || damageReductionMax > 1) {
            damageReductionMax = 0.50; // Varsayılan
        }
        if (damageReductionMin > damageReductionMax) {
            // Min > Max ise değiştir
            double temp = damageReductionMin;
            damageReductionMin = damageReductionMax;
            damageReductionMax = temp;
        }
    }
    
    // Getters
    public double getPowerThreshold() { return powerThreshold; }
    public int getMaxLevelDiff() { return maxLevelDiff; }
    public double getRookiePowerThreshold() { return rookiePowerThreshold; }
    public int getRookieLevelThreshold() { return rookieLevelThreshold; }
    public double getStrongPlayerThreshold() { return strongPlayerThreshold; }
    public long getInactiveThreshold() { return inactiveThreshold; }
    public double getClanThreshold() { return clanThreshold; }
    public double getDamageReductionMin() { return damageReductionMin; }
    public double getDamageReductionMax() { return damageReductionMax; }
}

