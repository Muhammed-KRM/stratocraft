package me.mami.stratocraft.util;

import me.mami.stratocraft.model.Alliance;
import me.mami.stratocraft.model.Clan;

import java.util.ArrayList;
import java.util.List;

/**
 * İttifak Sistemi Yardımcı Sınıfı
 * 
 * İttifaklar için yardımcı fonksiyonlar sağlar:
 * - Türkçe isimler
 * - Açıklamalar
 * - Bonus bilgileri
 * - Süre hesaplamaları
 */
public class AllianceHelper {
    
    /**
     * İttifak tipinin Türkçe ismini döndür
     */
    public static String getAllianceTypeDisplayName(Alliance.Type type) {
        if (type == null) return "Bilinmeyen İttifak";
        
        switch (type) {
            case DEFENSIVE:
                return "Savunma İttifakı";
            case OFFENSIVE:
                return "Saldırı İttifakı";
            case TRADE:
                return "Ticaret İttifakı";
            case FULL:
                return "Tam İttifak";
            default:
                return type.name();
        }
    }
    
    /**
     * İttifak tipinin açıklamasını döndür
     */
    public static List<String> getAllianceTypeDescription(Alliance.Type type) {
        List<String> lore = new ArrayList<>();
        
        if (type == null) {
            lore.add("§7Bilinmeyen ittifak tipi");
            return lore;
        }
        
        switch (type) {
            case DEFENSIVE:
                lore.add("§7Birine saldırılırsa");
                lore.add("§7diğeri yardım eder");
                lore.add("§7Savunma bonusu: §e+10%");
                break;
            case OFFENSIVE:
                lore.add("§7Birlikte saldırı yapılır");
                lore.add("§7Saldırı bonusu: §e+15%");
                break;
            case TRADE:
                lore.add("§7Ticaret bonusları");
                lore.add("§7Ticaret kazancı: §e+20%");
                break;
            case FULL:
                lore.add("§7En güçlü ittifak");
                lore.add("§7Tüm bonuslar aktif");
                lore.add("§7Savunma: §e+10%");
                lore.add("§7Saldırı: §e+15%");
                lore.add("§7Ticaret: §e+20%");
                break;
            default:
                lore.add("§7İttifak bilgisi yok");
        }
        
        return lore;
    }
    
    /**
     * İttifak bonuslarını döndür
     */
    public static List<String> getAllianceBonuses(Alliance alliance) {
        List<String> bonuses = new ArrayList<>();
        
        if (alliance == null || !alliance.isActive()) {
            bonuses.add("§7Aktif ittifak yok");
            return bonuses;
        }
        
        Alliance.Type type = alliance.getType();
        
        bonuses.add("§7═══════════════════════");
        bonuses.add("§7İttifak Bonusları:");
        bonuses.add("§7═══════════════════════");
        
        switch (type) {
            case DEFENSIVE:
                bonuses.add("§7• Savunma Gücü: §e+10%");
                bonuses.add("§7• Karşılıklı Yardım");
                break;
            case OFFENSIVE:
                bonuses.add("§7• Saldırı Gücü: §e+15%");
                bonuses.add("§7• Birlikte Saldırı");
                break;
            case TRADE:
                bonuses.add("§7• Ticaret Kazancı: §e+20%");
                bonuses.add("§7• Düşük Komisyon");
                break;
            case FULL:
                bonuses.add("§7• Savunma Gücü: §e+10%");
                bonuses.add("§7• Saldırı Gücü: §e+15%");
                bonuses.add("§7• Ticaret Kazancı: §e+20%");
                bonuses.add("§7• Tüm Özellikler");
                break;
        }
        
        bonuses.add("§7═══════════════════════");
        
        return bonuses;
    }
    
    /**
     * Kalan süreyi döndür (formatlanmış)
     */
    public static String getRemainingTime(Alliance alliance) {
        if (alliance == null) return "Bilinmeyen";
        
        if (alliance.getExpiresAt() == 0) {
            return "§aSüresiz";
        }
        
        long remaining = alliance.getExpiresAt() - System.currentTimeMillis();
        
        if (remaining <= 0) {
            return "§cSüresi Doldu";
        }
        
        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        
        if (days > 0) {
            return "§e" + days + " gün " + hours + " saat";
        } else if (hours > 0) {
            return "§e" + hours + " saat " + minutes + " dakika";
        } else {
            return "§e" + minutes + " dakika";
        }
    }
    
    /**
     * İttifak kurulabilir mi kontrol et
     */
    public static boolean canCreateAlliance(Clan clan1, Clan clan2, 
                                          org.bukkit.entity.Player player,
                                          me.mami.stratocraft.manager.AllianceManager allianceManager) {
        if (clan1 == null || clan2 == null || player == null || allianceManager == null) {
            return false;
        }
        
        // Aynı klan kontrolü
        if (clan1.getId().equals(clan2.getId())) {
            return false;
        }
        
        // Oyuncu klan lideri mi?
        if (clan1.getRank(player.getUniqueId()) != Clan.Rank.LEADER) {
            return false;
        }
        
        // Zaten ittifak var mı?
        if (allianceManager.hasAlliance(clan1.getId(), clan2.getId())) {
            return false;
        }
        
        // Cooldown kontrolü
        if (allianceManager.isOnCooldown(clan1.getId())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * İttifak iptal edilebilir mi kontrol et
     */
    public static boolean canDissolveAlliance(Alliance alliance, Clan clan, 
                                             org.bukkit.entity.Player player) {
        if (alliance == null || clan == null || player == null) {
            return false;
        }
        
        // İttifak aktif mi?
        if (!alliance.isActive()) {
            return false;
        }
        
        // Klan ittifakta mı?
        if (!alliance.involvesClan(clan.getId())) {
            return false;
        }
        
        // Oyuncu klan lideri mi?
        if (clan.getRank(player.getUniqueId()) != Clan.Rank.LEADER) {
            return false;
        }
        
        return true;
    }
    
    /**
     * İttifak tipinin ikonunu döndür
     */
    public static org.bukkit.Material getAllianceTypeIcon(Alliance.Type type) {
        if (type == null) return org.bukkit.Material.BARRIER;
        
        switch (type) {
            case DEFENSIVE:
                return org.bukkit.Material.SHIELD;
            case OFFENSIVE:
                return org.bukkit.Material.IRON_SWORD;
            case TRADE:
                return org.bukkit.Material.EMERALD;
            case FULL:
                return org.bukkit.Material.BEACON;
            default:
                return org.bukkit.Material.PAPER;
        }
    }
}


