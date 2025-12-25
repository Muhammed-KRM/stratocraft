package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.BossManager;
import me.mami.stratocraft.manager.DifficultyManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Canavar Gücü Hesaplama Sistemi
 * 
 * Canavarın gücünü hesaplar:
 * - Normal moblar: Seviye 1-5 (DifficultyManager'dan)
 * - Bosslar: BossType'a göre seviye
 * - Her canavar için bir "power" değeri
 */
public class MobPowerCalculator {
    
    /**
     * Canavarın gücünü hesapla
     * 
     * @param entity Canavar entity'si
     * @param location Canavarın konumu (zorluk seviyesi için)
     * @return Canavar gücü (1-10 arası, 1 = en zayıf, 10 = en güçlü)
     */
    public static int calculateMobPower(LivingEntity entity, Location location) {
        if (entity == null || location == null) {
            return 1; // Varsayılan güç
        }
        
        Main plugin = Main.getInstance();
        if (plugin == null) {
            return 1;
        }
        
        // Boss kontrolü
        BossManager bossManager = plugin.getBossManager();
        if (bossManager != null) {
            BossManager.BossData bossData = bossManager.getBossData(entity.getUniqueId());
            if (bossData != null) {
                // Boss gücü: BossType'a göre
                return calculateBossPower(bossData.getType());
            }
        }
        
        // Normal mob gücü: Zorluk seviyesine göre
        DifficultyManager difficultyManager = plugin.getDifficultyManager();
        if (difficultyManager != null) {
            int difficultyLevel = difficultyManager.getDifficultyLevel(location);
            return calculateNormalMobPower(entity, difficultyLevel);
        }
        
        return 1; // Varsayılan
    }
    
    /**
     * Boss gücü hesapla
     */
    private static int calculateBossPower(BossManager.BossType bossType) {
        if (bossType == null) {
            return 5; // Varsayılan boss gücü
        }
        
        switch (bossType) {
            // Seviye 1 Bosslar (Güç: 3-4)
            case GOBLIN_KING:
                return 3;
            case ORC_CHIEF:
                return 4;
            case TROLL_KING:
                return 4;
                
            // Seviye 2 Bosslar (Güç: 5-6)
            case DRAGON:
                return 6;
            case TREX:
                return 5;
            case CYCLOPS:
                return 5;
                
            // Seviye 3 Bosslar (Güç: 7-8)
            case TITAN_GOLEM:
                return 8;
            case HELL_DRAGON:
                return 7;
            case HYDRA:
                return 8;
                
            // Seviye 4 Bosslar (Güç: 9-10)
            case CHAOS_GOD:
                return 10;
                
            default:
                return 5; // Varsayılan boss gücü
        }
    }
    
    /**
     * Normal mob gücü hesapla
     */
    private static int calculateNormalMobPower(LivingEntity entity, int difficultyLevel) {
        String customName = entity.getCustomName();
        if (customName == null) {
            // İsimsiz mob: Zorluk seviyesine göre
            return Math.max(1, Math.min(5, difficultyLevel));
        }
        
        // Mob tipine göre güç belirle
        String mobType = extractMobType(customName);
        
        // Seviye 1 moblar (Güç: 1-2)
        if (mobType.contains("goblin")) {
            return 1;
        }
        if (mobType.contains("wild_boar") || mobType.contains("yaban_domuzu")) {
            return 1;
        }
        if (mobType.contains("wolf") || mobType.contains("kurt")) {
            return 1;
        }
        
        // Seviye 2 moblar (Güç: 2-3)
        if (mobType.contains("ork") || mobType.contains("orc")) {
            return 2;
        }
        if (mobType.contains("skeleton") || mobType.contains("iskelet")) {
            return 2;
        }
        if (mobType.contains("troll")) {
            return 3;
        }
        if (mobType.contains("werewolf") || mobType.contains("kurt_adam")) {
            return 2;
        }
        if (mobType.contains("mage") || mobType.contains("buyucu")) {
            return 2;
        }
        if (mobType.contains("spider") || mobType.contains("orumcek")) {
            return 2;
        }
        
        // Seviye 3 moblar (Güç: 3-4)
        if (mobType.contains("minotaur")) {
            return 3;
        }
        if (mobType.contains("harpy")) {
            return 3;
        }
        if (mobType.contains("basilisk")) {
            return 4;
        }
        if (mobType.contains("griffin")) {
            return 4;
        }
        if (mobType.contains("wraith")) {
            return 3;
        }
        if (mobType.contains("lich")) {
            return 4;
        }
        
        // Seviye 4 moblar (Güç: 4-5)
        if (mobType.contains("dragon") || mobType.contains("ejderha")) {
            return 5;
        }
        if (mobType.contains("wyvern")) {
            return 5;
        }
        if (mobType.contains("hell_dragon") || mobType.contains("cehennem")) {
            return 5;
        }
        if (mobType.contains("war_bear") || mobType.contains("savas_ayisi")) {
            return 4;
        }
        if (mobType.contains("phoenix")) {
            return 5;
        }
        
        // Seviye 5 moblar (Güç: 5-6)
        if (mobType.contains("titan") || mobType.contains("golem")) {
            return 6;
        }
        if (mobType.contains("hydra")) {
            return 6;
        }
        if (mobType.contains("void") || mobType.contains("hiclik")) {
            return 6;
        }
        if (mobType.contains("kraken")) {
            return 5;
        }
        if (mobType.contains("behemoth")) {
            return 6;
        }
        
        // Varsayılan: Zorluk seviyesine göre
        return Math.max(1, Math.min(5, difficultyLevel));
    }
    
    /**
     * Mob tipini çıkar (isimden)
     */
    private static String extractMobType(String name) {
        if (name == null) {
            return "";
        }
        
        // Renk kodlarını ve özel karakterleri temizle
        name = name.replaceAll("§[0-9a-fk-or]", "").toLowerCase();
        name = name.replace("♂", "").replace("♀", "").replace("[eğitilmiş]", "").trim();
        
        return name;
    }
    
    /**
     * Canavar boss mu?
     */
    public static boolean isBoss(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        Main plugin = Main.getInstance();
        if (plugin == null) {
            return false;
        }
        
        BossManager bossManager = plugin.getBossManager();
        if (bossManager != null) {
            BossManager.BossData bossData = bossManager.getBossData(entity.getUniqueId());
            return bossData != null;
        }
        
        return false;
    }
    
    /**
     * Canavarın seviyesini al (1-5)
     */
    public static int getMobLevel(LivingEntity entity, Location location) {
        int power = calculateMobPower(entity, location);
        // Güç 1-2 = Seviye 1, Güç 3-4 = Seviye 2, vb.
        return Math.max(1, Math.min(5, (power + 1) / 2));
    }
}

