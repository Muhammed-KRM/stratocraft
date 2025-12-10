package me.mami.stratocraft.util;

import me.mami.stratocraft.manager.BossManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss Faz Sistemi Yardımcı Sınıfı
 * 
 * Boss faz geçişleri için yardımcı fonksiyonlar sağlar:
 * - Sağlık eşikleri
 * - Faz yetenekleri
 * - Zayıf nokta konumları
 */
public class BossPhaseHelper {
    
    /**
     * Faz için sağlık eşiğini döndür (0.0 - 1.0 arası)
     */
    public static double getPhaseHealthThreshold(int phase, int maxPhase) {
        if (maxPhase <= 0) return 1.0;
        if (phase <= 0) return 1.0;
        if (phase > maxPhase) return 0.0;
        
        // Faz 1: %100 - %66
        // Faz 2: %66 - %33
        // Faz 3: %33 - %0
        return 1.0 - ((double) phase / (double) maxPhase);
    }
    
    /**
     * Faz için yetenekleri döndür
     */
    public static List<BossManager.BossAbility> getPhaseAbilities(int phase, BossManager.BossType type) {
        List<BossManager.BossAbility> abilities = new ArrayList<>();
        
        if (phase <= 0) return abilities;
        
        // Faz 1: Temel yetenekler
        if (phase == 1) {
            abilities.add(BossManager.BossAbility.FIRE_BREATH);
            abilities.add(BossManager.BossAbility.EXPLOSION);
            abilities.add(BossManager.BossAbility.CHARGE);
        }
        
        // Faz 2: Orta seviye yetenekler
        if (phase == 2) {
            abilities.add(BossManager.BossAbility.LIGHTNING_STRIKE);
            abilities.add(BossManager.BossAbility.POISON_CLOUD);
            abilities.add(BossManager.BossAbility.TELEPORT);
            abilities.add(BossManager.BossAbility.SUMMON_MINIONS);
        }
        
        // Faz 3: İleri seviye yetenekler
        if (phase >= 3) {
            abilities.add(BossManager.BossAbility.HEAL);
            abilities.add(BossManager.BossAbility.SHOCKWAVE);
            abilities.add(BossManager.BossAbility.BLOCK_THROW);
        }
        
        // Boss tipine özel yetenekler
        switch (type) {
            case DRAGON:
            case HELL_DRAGON:
            case VOID_DRAGON:
                if (phase >= 2) {
                    abilities.add(BossManager.BossAbility.FIRE_BREATH);
                }
                break;
            case HYDRA:
                if (phase >= 2) {
                    abilities.add(BossManager.BossAbility.POISON_CLOUD);
                    abilities.add(BossManager.BossAbility.SUMMON_MINIONS);
                }
                break;
            case TITAN_GOLEM:
            case CHAOS_TITAN:
                if (phase >= 2) {
                    abilities.add(BossManager.BossAbility.SHOCKWAVE);
                    abilities.add(BossManager.BossAbility.BLOCK_THROW);
                }
                break;
            case GOBLIN_KING:
            case ORC_CHIEF:
            case TROLL_KING:
            case TREX:
            case CYCLOPS:
            case PHOENIX:
            case CHAOS_GOD:
                // Bu bosslar için özel yetenek yok, temel yetenekler kullanılır
                break;
        }
        
        return abilities;
    }
    
    /**
     * Faz için zayıf nokta konumlarını döndür (göreceli konumlar)
     */
    public static List<Location> getWeakPointLocations(LivingEntity boss, int phase) {
        List<Location> locations = new ArrayList<>();
        
        if (boss == null || !boss.isValid()) return locations;
        
        Location bossLoc = boss.getLocation();
        
        // Faz 1: Baş (yukarı)
        if (phase == 1) {
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.8, 0));
        }
        
        // Faz 2: Baş + Göğüs
        if (phase >= 2) {
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.8, 0)); // Baş
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.5, 0)); // Göğüs
        }
        
        // Faz 3: Baş + Göğüs + Sırt
        if (phase >= 3) {
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.8, 0)); // Baş
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.5, 0)); // Göğüs
            locations.add(bossLoc.clone().add(0, boss.getHeight() * 0.3, -0.5)); // Sırt
        }
        
        return locations;
    }
    
    /**
     * Faz geçişi için gerekli sağlık yüzdesini kontrol et
     */
    public static boolean shouldTransitionPhase(double currentHealthPercent, int currentPhase, int maxPhase) {
        if (maxPhase <= 0 || currentPhase >= maxPhase) return false;
        
        double threshold = getPhaseHealthThreshold(currentPhase + 1, maxPhase);
        return currentHealthPercent <= threshold;
    }
    
    /**
     * Faz geçişi mesajı
     */
    public static String getPhaseTransitionMessage(int oldPhase, int newPhase, BossManager.BossType type) {
        String bossName = getBossDisplayName(type);
        return "§c§l═══════════════════════════\n" +
               "§c§l" + bossName + " FAZ " + newPhase + "!\n" +
               "§7Faz " + oldPhase + " → Faz " + newPhase + "\n" +
               "§c§l═══════════════════════════";
    }
    
    /**
     * Boss tipinin Türkçe ismini döndür
     */
    private static String getBossDisplayName(BossManager.BossType type) {
        if (type == null) return "Bilinmeyen Boss";
        
        switch (type) {
            case GOBLIN_KING:
                return "Goblin Kralı";
            case ORC_CHIEF:
                return "Ork Şefi";
            case TROLL_KING:
                return "Troll Kralı";
            case DRAGON:
                return "Ejderha";
            case TREX:
                return "T-Rex";
            case CYCLOPS:
                return "Kiklop";
            case TITAN_GOLEM:
                return "Titan Golem";
            case HELL_DRAGON:
                return "Cehennem Ejderhası";
            case HYDRA:
                return "Hidra";
            case PHOENIX:
                return "Anka Kuşu";
            case VOID_DRAGON:
                return "Boşluk Ejderhası";
            case CHAOS_TITAN:
                return "Kaos Titanı";
            case CHAOS_GOD:
                return "Kaos Tanrısı";
            default:
                return type.name();
        }
    }
}

