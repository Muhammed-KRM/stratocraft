package me.mami.stratocraft.manager;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Klan Güç Sistemi Konfigürasyonu
 * Tüm güç puanları ve seviye eşikleri config'den yönetilir
 */
public class ClanPowerConfig {
    // ========== ITEM GÜÇ PUANLARI ==========
    // Silah güç puanları (seviye bazlı)
    private double weaponLevel1Power = 60.0;
    private double weaponLevel2Power = 150.0;
    private double weaponLevel3Power = 400.0;
    private double weaponLevel4Power = 900.0;
    private double weaponLevel5Power = 1600.0; // Rapora göre 1600
    
    // Zırh güç puanları (seviye bazlı)
    private double armorLevel1Power = 40.0;
    private double armorLevel2Power = 100.0;
    private double armorLevel3Power = 250.0;
    private double armorLevel4Power = 600.0;
    private double armorLevel5Power = 1000.0;
    
    // Tam set bonusu (4 parça zırh)
    private double armorSetBonus = 1.1; // %10 bonus
    
    // ========== RİTÜEL BLOK GÜÇ PUANLARI ==========
    private double ritualBlockIron = 8.0;
    private double ritualBlockObsidian = 30.0;
    private double ritualBlockDiamond = 25.0;
    private double ritualBlockGold = 12.0;
    private double ritualBlockEmerald = 35.0;
    private double ritualBlockTitanyum = 150.0; // Titanyum/Netherite blok
    private double ritualBlockDefault = 5.0;
    
    // ========== RİTÜEL KAYNAK GÜÇ PUANLARI ==========
    private double ritualResourceIron = 5.0;
    private double ritualResourceDiamond = 10.0;
    private double ritualResourceRedDiamond = 18.0;
    private double ritualResourceDarkMatter = 50.0;
    private double ritualResourceTitanium = 15.0;
    private double ritualResourceDefault = 3.0;
    
    // ========== ANTRENMAN/USTALIK GÜÇ PUANLARI ==========
    // Her %100 üzerine çıkış için puan
    // Formül: basePower × (masteryPercent / 100) ^ exponent
    private double masteryBasePower = 150.0; // Rapora göre 150
    private double masteryExponent = 1.4; // Rapora göre 1.4
    
    // ========== KLAN YAPI GÜÇ PUANLARI ==========
    private double crystalBasePower = 500.0; // Klan kristali sabit bonus
    private double structureLevel1Power = 100.0;
    private double structureLevel2Power = 250.0;
    private double structureLevel3Power = 500.0;
    private double structureLevel4Power = 1200.0;
    private double structureLevel5Power = 2000.0;
    
    // ========== GÜÇ SEVİYESİ EŞİKLERİ (HİBRİT SİSTEM) ==========
    // Aşama 1: Hızlı ilerleme (Seviye 1-10) - Karekök
    // Aşama 2: Zor ilerleme (Seviye 11+) - Logaritmik
    private double playerLevelBasePower = 100.0; // Seviye 1 için temel güç
    private double playerLevelSwitchPower = 10000.0; // Karekök -> Logaritmik geçiş noktası
    private double playerLevelMultiplier = 3.0; // Logaritmik kısım için çarpan
    private int playerMaxLevel = 20; // Maksimum oyuncu seviyesi
    
    // Klan seviyesi için farklı parametreler (logaritmik)
    private double clanLevelBasePower = 500.0;
    private double clanLevelMultiplier = 2.0;
    private int maxClanLevel = 15;
    
    // ========== KORUMA SİSTEMİ ==========
    private double protectionThreshold = 0.5; // %50 - Hedef, saldıranın gücünün %50'sinden düşükse saldırı yapılamaz
    private double clanProtectionThreshold = 0.6; // %60 - Klan içi koruma (daha yüksek)
    private double rookieThreshold = 5000.0; // Acemi koruması eşiği
    private double strongPlayerThreshold = 10000.0; // Güçlü oyuncu eşiği (acemiye saldıramaz)
    
    // ========== GÜÇ AĞIRLIKLARI ==========
    private double combatPowerWeight = 0.6; // Combat Power ağırlığı
    private double progressionPowerWeight = 0.4; // Progression Power ağırlığı
    
    // ========== HİSTEREZİS SİSTEMİ ==========
    private long gearDecreaseDelay = 60000L; // Güç düşüşü gecikmesi (ms) - 60 saniye
    
    /**
     * Config'den ayarları yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        String path = "clan-power-system.";
        
        // Item güç puanları
        path = "clan-power-system.item-power.";
        weaponLevel1Power = config.getDouble(path + "weapon.level-1", 60.0);
        weaponLevel2Power = config.getDouble(path + "weapon.level-2", 150.0);
        weaponLevel3Power = config.getDouble(path + "weapon.level-3", 400.0);
        weaponLevel4Power = config.getDouble(path + "weapon.level-4", 900.0);
        weaponLevel5Power = config.getDouble(path + "weapon.level-5", 1600.0);
        
        // Tam set bonusu
        armorSetBonus = config.getDouble(path + "armor-set-bonus", 1.1);
        
        armorLevel1Power = config.getDouble(path + "armor.level-1", 40.0);
        armorLevel2Power = config.getDouble(path + "armor.level-2", 100.0);
        armorLevel3Power = config.getDouble(path + "armor.level-3", 250.0);
        armorLevel4Power = config.getDouble(path + "armor.level-4", 600.0);
        armorLevel5Power = config.getDouble(path + "armor.level-5", 1000.0);
        
        // Ritüel blok güç puanları
        path = "clan-power-system.ritual-blocks.";
        ritualBlockIron = config.getDouble(path + "iron", 8.0);
        ritualBlockObsidian = config.getDouble(path + "obsidian", 30.0);
        ritualBlockDiamond = config.getDouble(path + "diamond", 25.0);
        ritualBlockGold = config.getDouble(path + "gold", 12.0);
        ritualBlockEmerald = config.getDouble(path + "emerald", 35.0);
        ritualBlockTitanyum = config.getDouble(path + "titanyum", 150.0);
        ritualBlockDefault = config.getDouble(path + "default", 5.0);
        
        // Ritüel kaynak güç puanları
        path = "clan-power-system.ritual-resources.";
        ritualResourceIron = config.getDouble(path + "iron", 5.0);
        ritualResourceDiamond = config.getDouble(path + "diamond", 10.0);
        ritualResourceRedDiamond = config.getDouble(path + "red-diamond", 18.0);
        ritualResourceDarkMatter = config.getDouble(path + "dark-matter", 50.0);
        ritualResourceTitanium = config.getDouble(path + "titanium", 15.0);
        ritualResourceDefault = config.getDouble(path + "default", 3.0);
        
        // Antrenman/Ustalık güç puanları
        path = "clan-power-system.mastery.";
        masteryBasePower = config.getDouble(path + "base-power", 150.0);
        masteryExponent = config.getDouble(path + "exponent", 1.4);
        
        // Klan yapı güç puanları
        path = "clan-power-system.structure-power.";
        crystalBasePower = config.getDouble(path + "crystal-base", 500.0);
        structureLevel1Power = config.getDouble(path + "level-1", 100.0);
        structureLevel2Power = config.getDouble(path + "level-2", 250.0);
        structureLevel3Power = config.getDouble(path + "level-3", 500.0);
        structureLevel4Power = config.getDouble(path + "level-4", 1200.0);
        structureLevel5Power = config.getDouble(path + "level-5", 2000.0);
        
        // Güç seviyesi eşikleri (hibrit sistem)
        path = "clan-power-system.level-system.";
        playerLevelBasePower = config.getDouble(path + "player-base-power", 100.0);
        playerLevelSwitchPower = config.getDouble(path + "player-switch-power", 10000.0);
        playerLevelMultiplier = config.getDouble(path + "player-log-multiplier", 3.0);
        playerMaxLevel = config.getInt(path + "player-max-level", 20);
        
        clanLevelBasePower = config.getDouble(path + "clan-base-power", 500.0);
        clanLevelMultiplier = config.getDouble(path + "clan-multiplier", 2.0);
        maxClanLevel = config.getInt(path + "max-clan-level", 15);
        
        // Koruma sistemi
        path = "clan-power-system.protection.";
        protectionThreshold = config.getDouble(path + "threshold", 0.5);
        clanProtectionThreshold = config.getDouble(path + "clan-threshold", 0.6);
        rookieThreshold = config.getDouble(path + "rookie-threshold", 5000.0);
        strongPlayerThreshold = config.getDouble(path + "strong-player-threshold", 10000.0);
        
        // Güç ağırlıkları
        path = "clan-power-system.power-weights.";
        combatPowerWeight = config.getDouble(path + "combat", 0.6);
        progressionPowerWeight = config.getDouble(path + "progression", 0.4);
        
        // Histerezis sistemi
        path = "clan-power-system.protection.";
        gearDecreaseDelay = config.getLong(path + "gear-decrease-delay", 60000L);
    }
    
    /**
     * Silah gücü al (seviye bazlı)
     */
    public double getWeaponPower(int level) {
        switch (level) {
            case 1: return weaponLevel1Power;
            case 2: return weaponLevel2Power;
            case 3: return weaponLevel3Power;
            case 4: return weaponLevel4Power;
            case 5: return weaponLevel5Power;
            default: return 0.0;
        }
    }
    
    /**
     * Zırh gücü al (seviye bazlı)
     */
    public double getArmorPower(int level) {
        switch (level) {
            case 1: return armorLevel1Power;
            case 2: return armorLevel2Power;
            case 3: return armorLevel3Power;
            case 4: return armorLevel4Power;
            case 5: return armorLevel5Power;
            default: return 0.0;
        }
    }
    
    /**
     * Ritüel blok gücü al
     */
    public double getRitualBlockPower(org.bukkit.Material material) {
        switch (material) {
            case IRON_BLOCK: return ritualBlockIron;
            case OBSIDIAN: return ritualBlockObsidian;
            case DIAMOND_BLOCK: return ritualBlockDiamond;
            case GOLD_BLOCK: return ritualBlockGold;
            case EMERALD_BLOCK: return ritualBlockEmerald;
            case NETHERITE_BLOCK: return ritualBlockTitanyum; // Netherite = Titanyum
            default: return ritualBlockDefault;
        }
    }
    
    /**
     * Ritüel kaynak gücü al
     */
    public double getRitualResourcePower(String resourceType) {
        switch (resourceType.toUpperCase()) {
            case "IRON": return ritualResourceIron;
            case "DIAMOND": return ritualResourceDiamond;
            case "RED_DIAMOND":
            case "REDDIAMOND": return ritualResourceRedDiamond;
            case "DARK_MATTER":
            case "DARKMATTER": return ritualResourceDarkMatter;
            case "TITANIUM": return ritualResourceTitanium;
            default: return ritualResourceDefault;
        }
    }
    
    /**
     * Ustalık gücü hesapla
     * Formül: basePower × (masteryPercent / 100) ^ exponent
     */
    public double getMasteryPower(double masteryPercent) {
        if (masteryPercent <= 100) return 0.0; // %100 ve altı için bonus yok
        
        double multiplier = Math.pow(masteryPercent / 100.0, masteryExponent);
        return masteryBasePower * multiplier;
    }
    
    /**
     * Yapı gücü al (seviye bazlı)
     */
    public double getStructurePower(int level) {
        switch (level) {
            case 1: return structureLevel1Power;
            case 2: return structureLevel2Power;
            case 3: return structureLevel3Power;
            case 4: return structureLevel4Power;
            case 5: return structureLevel5Power;
            default: return 0.0;
        }
    }
    
    /**
     * Güç puanına göre oyuncu seviyesi hesapla (hibrit sistem)
     * Aşama 1 (1-10): Karekök (hızlı ilerleme)
     * Aşama 2 (11+): Logaritmik (zor ilerleme)
     */
    public int calculatePlayerLevel(double power) {
        if (power < 0) return 1;
        
        if (power < playerLevelSwitchPower) {
            // Aşama 1: Karekök (hızlı ilerleme)
            double level = Math.sqrt(power / playerLevelBasePower);
            return Math.max(1, (int) Math.floor(level));
        } else {
            // Aşama 2: Logaritmik (zor ilerleme)
            double level = 10 + Math.floor(Math.log10(power / playerLevelSwitchPower) * playerLevelMultiplier);
            return Math.min((int) level, playerMaxLevel);
        }
    }
    
    /**
     * Eski metod (geriye uyumluluk için)
     * @deprecated calculatePlayerLevel kullan
     */
    @Deprecated
    public int calculateLevel(double power) {
        return calculatePlayerLevel(power);
    }
    
    /**
     * Klan güç puanına göre seviye hesapla
     */
    public int calculateClanLevel(double power) {
        if (power < clanLevelBasePower) return 1;
        
        double level = Math.log10(power / clanLevelBasePower) * clanLevelMultiplier + 1;
        int calculatedLevel = (int) Math.floor(level);
        
        return Math.min(calculatedLevel, maxClanLevel);
    }
    
    // ========== GETTERS ==========
    
    public double getArmorSetBonus() { return armorSetBonus; }
    public double getCrystalBasePower() { return crystalBasePower; }
    
    public double getProtectionThreshold() { return protectionThreshold; }
    public double getClanProtectionThreshold() { return clanProtectionThreshold; }
    public double getRookieThreshold() { return rookieThreshold; }
    public double getStrongPlayerThreshold() { return strongPlayerThreshold; }
    
    public double getCombatPowerWeight() { return combatPowerWeight; }
    public double getProgressionPowerWeight() { return progressionPowerWeight; }
    
    public long getGearDecreaseDelay() { return gearDecreaseDelay; }
}

