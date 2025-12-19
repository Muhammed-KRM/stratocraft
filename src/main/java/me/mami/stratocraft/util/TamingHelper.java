package me.mami.stratocraft.util;

import me.mami.stratocraft.manager.TamingManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Eğitme Sistemi Yardımcı Sınıfı
 * 
 * Eğitilmiş canlılar için yardımcı fonksiyonlar sağlar:
 * - Oyuncunun eğitilmiş canlılarını getirme
 * - Canlı bilgileri
 * - Üreme kontrolü
 */
public class TamingHelper {
    
    /**
     * Oyuncunun eğitilmiş canlılarını getir
     */
    public static List<LivingEntity> getTamedCreatures(Player player, TamingManager tamingManager) {
        List<LivingEntity> creatures = new ArrayList<>();
        
        if (player == null || tamingManager == null) {
            return creatures;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Tüm dünyalardaki canlıları kontrol et
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            if (world == null) continue;
            
            for (org.bukkit.entity.LivingEntity entity : world.getLivingEntities()) {
                if (entity == null || entity.isDead()) continue;
                
                // Eğitilmiş mi?
                if (!tamingManager.isTamed(entity)) continue;
                
                // Sahibi bu oyuncu mu?
                UUID ownerId = tamingManager.getOwner(entity);
                if (ownerId != null && ownerId.equals(playerId)) {
                    creatures.add(entity);
                }
            }
        }
        
        return creatures;
    }
    
    /**
     * Klanın eğitilmiş canlılarını getir
     */
    public static List<LivingEntity> getClanTamedCreatures(me.mami.stratocraft.model.Clan clan, 
                                                           TamingManager tamingManager) {
        List<LivingEntity> creatures = new ArrayList<>();
        
        if (clan == null || tamingManager == null) {
            return creatures;
        }
        
        // Klan üyelerinin canlılarını topla
        for (UUID memberId : clan.getMembers().keySet()) {
            org.bukkit.OfflinePlayer member = org.bukkit.Bukkit.getOfflinePlayer(memberId);
            if (member != null && member.isOnline() && member.getPlayer() != null) {
                creatures.addAll(getTamedCreatures(member.getPlayer(), tamingManager));
            }
        }
        
        return creatures;
    }
    
    /**
     * Canlı bilgilerini döndür
     */
    public static List<String> getCreatureInfo(LivingEntity creature, TamingManager tamingManager) {
        List<String> info = new ArrayList<>();
        
        if (creature == null || tamingManager == null) {
            info.add("§cCanlı bulunamadı");
            return info;
        }
        
        String name = creature.getCustomName();
        if (name == null) {
            name = creature.getType().name();
        }
        
        TamingManager.Gender gender = tamingManager.getGender(creature);
        String genderStr = gender == TamingManager.Gender.MALE ? "§bErkek" : "§dDişi";
        
        UUID ownerId = tamingManager.getOwner(creature);
        String ownerName = "Bilinmeyen";
        if (ownerId != null) {
            org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(ownerId);
            if (owner != null && owner.getName() != null) {
                ownerName = owner.getName();
            }
        }
        
        boolean isRideable = tamingManager.isRideable(creature);
        
        info.add("§7═══════════════════════");
        info.add("§7İsim: §e" + name);
        info.add("§7Cinsiyet: " + genderStr);
        info.add("§7Sahip: §e" + ownerName);
        info.add("§7Binilebilir: " + (isRideable ? "§aEvet" : "§cHayır"));
        info.add("§7Sağlık: §e" + String.format("%.1f", creature.getHealth()) + 
                 "§7/§e" + String.format("%.1f", creature.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
        info.add("§7═══════════════════════");
        
        return info;
    }
    
    /**
     * Üreme yapılabilir mi kontrol et
     */
    public static boolean canBreed(LivingEntity creature1, LivingEntity creature2, 
                                  TamingManager tamingManager) {
        if (creature1 == null || creature2 == null || tamingManager == null) {
            return false;
        }
        
        // Her ikisi de eğitilmiş mi?
        if (!tamingManager.isTamed(creature1) || !tamingManager.isTamed(creature2)) {
            return false;
        }
        
        // Aynı sahip mi?
        UUID owner1 = tamingManager.getOwner(creature1);
        UUID owner2 = tamingManager.getOwner(creature2);
        if (owner1 == null || owner2 == null || !owner1.equals(owner2)) {
            return false;
        }
        
        // Farklı cinsiyet mi?
        TamingManager.Gender gender1 = tamingManager.getGender(creature1);
        TamingManager.Gender gender2 = tamingManager.getGender(creature2);
        
        if (gender1 == gender2) {
            return false; // Aynı cinsiyet
        }
        
        // Aynı tür mü? (basit kontrol - isim bazlı)
        String name1 = creature1.getCustomName();
        String name2 = creature2.getCustomName();
        
        if (name1 == null || name2 == null) {
            return false; // İsimsiz canlılar üreyemez
        }
        
        // Aynı tür kontrolü (basit - isim benzerliği)
        String type1 = extractCreatureType(name1);
        String type2 = extractCreatureType(name2);
        
        return type1.equals(type2);
    }
    
    /**
     * Canlı tipini çıkar (isimden)
     */
    private static String extractCreatureType(String name) {
        if (name == null) return "";
        
        // Cinsiyet işaretlerini ve eğitilmiş işaretini temizle
        name = name.replace("§b♂", "").replace("§d♀", "").replace("§7[Eğitilmiş]", "").trim();
        
        // Türkçe isimlerden tip çıkar
        if (name.contains("Ejderha") || name.contains("DRAGON")) return "DRAGON";
        if (name.contains("T-Rex") || name.contains("TREX")) return "TREX";
        if (name.contains("Griffin") || name.contains("GRIFFIN")) return "GRIFFIN";
        if (name.contains("Savaş Ayısı") || name.contains("WAR_BEAR")) return "WAR_BEAR";
        if (name.contains("Phoenix") || name.contains("PHOENIX")) return "PHOENIX";
        if (name.contains("Wyvern") || name.contains("WYVERN")) return "WYVERN";
        if (name.contains("Cehennem Ejderi") || name.contains("HELL_DRAGON")) return "HELL_DRAGON";
        if (name.contains("Hydra") || name.contains("HYDRA")) return "HYDRA";
        if (name.contains("Khaos Tanrısı") || name.contains("CHAOS_GOD")) return "CHAOS_GOD";
        if (name.contains("Ork") || name.contains("ORK")) return "ORK";
        if (name.contains("Troll") || name.contains("TROLL")) return "TROLL";
        if (name.contains("Goblin") || name.contains("GOBLIN")) return "GOBLIN";
        
        return name; // Varsayılan olarak tam isim
    }
    
    /**
     * Canlı ikonunu döndür
     */
    public static org.bukkit.Material getCreatureIcon(LivingEntity creature) {
        if (creature == null) return org.bukkit.Material.BARRIER;
        
        String name = creature.getCustomName();
        if (name == null) {
            name = creature.getType().name();
        }
        
        String type = extractCreatureType(name);
        
        switch (type) {
            case "DRAGON":
            case "HELL_DRAGON":
                return org.bukkit.Material.DRAGON_HEAD;
            case "TREX":
                return org.bukkit.Material.BONE;
            case "GRIFFIN":
                return org.bukkit.Material.FEATHER;
            case "WAR_BEAR":
                return org.bukkit.Material.HONEYCOMB;
            case "PHOENIX":
                return org.bukkit.Material.FIRE_CHARGE;
            case "WYVERN":
                return org.bukkit.Material.ELYTRA;
            case "HYDRA":
                return org.bukkit.Material.POISONOUS_POTATO;
            case "CHAOS_GOD":
                return org.bukkit.Material.NETHER_STAR;
            case "ORK":
            case "TROLL":
            case "GOBLIN":
                return org.bukkit.Material.IRON_SWORD;
            default:
                return org.bukkit.Material.SPAWNER;
        }
    }
}


















