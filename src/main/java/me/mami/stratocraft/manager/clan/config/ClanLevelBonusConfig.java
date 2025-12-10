package me.mami.stratocraft.manager.clan.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Klan Seviye Bonus Config
 */
public class ClanLevelBonusConfig {
    // Güç bonusları
    private double level4PowerBonus = 0.05; // %5 (Seviye 4-7)
    private double level8PowerBonus = 0.10; // %10 (Seviye 8-12)
    private double level13PowerBonus = 0.15; // %15 (Seviye 13-15)
    
    /**
     * Config'den yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        if (config == null) {
            // Varsayılan değerler zaten set edilmiş
            return;
        }
        
        String path = "clan.level-bonuses.";
        
        level4PowerBonus = config.getDouble(path + "level-4-power-bonus", 0.05);
        level8PowerBonus = config.getDouble(path + "level-8-power-bonus", 0.10);
        level13PowerBonus = config.getDouble(path + "level-13-power-bonus", 0.15);
        
        // Geçersiz değer kontrolü
        if (level4PowerBonus < 0 || level4PowerBonus > 1) {
            level4PowerBonus = 0.05; // Varsayılan
        }
        if (level8PowerBonus < 0 || level8PowerBonus > 1) {
            level8PowerBonus = 0.10; // Varsayılan
        }
        if (level13PowerBonus < 0 || level13PowerBonus > 1) {
            level13PowerBonus = 0.15; // Varsayılan
        }
        
        // Mantık kontrolü: Seviye arttıkça bonus artmalı
        if (level4PowerBonus > level8PowerBonus || level8PowerBonus > level13PowerBonus) {
            // Sıralama yanlış, düzelt
            if (level4PowerBonus > level8PowerBonus) {
                level8PowerBonus = level4PowerBonus + 0.05; // En az %5 daha fazla
            }
            if (level8PowerBonus > level13PowerBonus) {
                level13PowerBonus = level8PowerBonus + 0.05; // En az %5 daha fazla
            }
        }
    }
    
    // Getters
    public double getLevel4PowerBonus() { return level4PowerBonus; }
    public double getLevel8PowerBonus() { return level8PowerBonus; }
    public double getLevel13PowerBonus() { return level13PowerBonus; }
}

