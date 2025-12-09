package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Oyuncu Güç Puanı Hesaplama Sistemi
 * 
 * Modüler yapı: Her güç türü için ayrı metod
 * Config'den tüm değerler okunur
 * Performans: Cache kullanımı önerilir (her 5-10 saniyede bir hesapla)
 * 
 * Gelecekte değiştirilebilir: IPowerCalculator interface'i implement eder
 */
public class PlayerPowerCalculator implements IPowerCalculator {
    private final DisasterPowerConfig config;
    private final ClanManager clanManager;
    private final TrainingManager trainingManager;
    private final BuffManager buffManager;
    private final SpecialItemManager specialItemManager;
    
    public PlayerPowerCalculator(DisasterPowerConfig config, ClanManager clanManager,
                                 TrainingManager trainingManager, BuffManager buffManager,
                                 SpecialItemManager specialItemManager) {
        this.config = config;
        this.clanManager = clanManager;
        this.trainingManager = trainingManager;
        this.buffManager = buffManager;
        this.specialItemManager = specialItemManager;
    }
    
    /**
     * Oyuncunun toplam güç puanını hesapla
     * 
     * @param player Oyuncu
     * @return Toplam güç puanı
     */
    public double calculatePlayerPower(Player player) {
        if (player == null || !player.isOnline()) return 0.0;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        
        double structurePower = calculateStructurePower(clan);
        double itemPower = calculateItemPower(player);
        double buffPower = calculateBuffPower(player, clan);
        double trainingPower = calculateTrainingPower(player);
        double clanTechPower = calculateClanTechPower(clan);
        
        // Ağırlıklı toplam
        double totalPower = 
            (structurePower * config.getStructureWeight()) +
            (itemPower * config.getItemWeight()) +
            (buffPower * config.getBuffWeight()) +
            (trainingPower * config.getTrainingWeight()) +
            (clanTechPower * config.getClanTechWeight());
        
        return totalPower;
    }
    
    /**
     * Klan yapıları gücü
     * Her yapı için: Seviye × Yapı Tipi Çarpanı
     */
    public double calculateStructurePower(Clan clan) {
        if (clan == null) return 0.0;
        
        double totalPower = 0.0;
        for (Structure structure : clan.getStructures()) {
            double multiplier = getStructureTypeMultiplier(structure.getType());
            totalPower += structure.getLevel() * multiplier;
        }
        return totalPower;
    }
    
    /**
     * Yapı tipi çarpanı (config'den)
     */
    private double getStructureTypeMultiplier(Structure.Type type) {
        // Yapı tiplerini kategorilere ayır
        switch (type) {
            // Batarya kategorisi
            case ALCHEMY_TOWER:
                return config.getBatteryMultiplier();
            
            // Araştırma kategorisi
            case LIBRARY:
                return config.getResearchCenterMultiplier();
            
            // Üretim kategorisi
            case AUTO_DRILL:
            case OIL_REFINERY:
            case FOOD_SILO:
            case CROP_ACCELERATOR:
            case MOB_GRINDER:
                return config.getProductionMultiplier();
            
            // Savunma kategorisi
            case POISON_REACTOR:
            case TECTONIC_STABILIZER:
            case WALL_GENERATOR:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case WATCHTOWER:
            case DRONE_STATION:
            case AUTO_TURRET:
            case HEALING_BEACON:
            case INVISIBILITY_CLOAK:
                return config.getDefenseMultiplier();
            
            default:
                return config.getDefaultStructureMultiplier();
        }
    }
    
    /**
     * Eşya gücü: Silah + Zırh + Özel Eşya
     */
    public double calculateItemPower(Player player) {
        double weaponPower = calculateWeaponPower(player);
        double armorPower = calculateArmorPower(player);
        double specialItemPower = calculateSpecialItemPower(player);
        
        return weaponPower + armorPower + specialItemPower;
    }
    
    /**
     * Silah gücü
     * Formül: basePower × 2^(level-1)
     * Seviye 1: 5, Seviye 2: 10, Seviye 3: 20, Seviye 4: 40, Seviye 5: 80
     */
    public double calculateWeaponPower(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return 0.0;
        
        int level = ItemManager.getWeaponLevel(weapon);
        if (level == 0) return 0.0;
        
        // Formül: basePower × 2^(level-1)
        return config.getWeaponBasePower() * Math.pow(2, level - 1);
    }
    
    /**
     * Zırh gücü
     * Her parça: basePower × 2^(level-1)
     * Tam set (4 parça): +50% bonus
     */
    public double calculateArmorPower(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        double totalPower = 0.0;
        int equippedPieces = 0;
        
        for (ItemStack piece : armor) {
            if (piece != null) {
                int level = ItemManager.getArmorLevel(piece);
                if (level > 0) {
                    // Formül: basePower × 2^(level-1)
                    totalPower += config.getArmorBasePower() * Math.pow(2, level - 1);
                    equippedPieces++;
                }
            }
        }
        
        // Tam set bonusu (4 parça)
        if (equippedPieces == 4) {
            totalPower *= config.getArmorSetBonus();
        }
        
        return totalPower;
    }
    
    /**
     * Özel eşya gücü
     * Tüm envanteri kontrol et
     */
    public double calculateSpecialItemPower(Player player) {
        if (specialItemManager == null) return 0.0;
        
        double totalPower = 0.0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSpecialItem(item)) {
                int tier = getSpecialItemTier(item);
                if (tier > 0) {
                    totalPower += config.getSpecialItemPower(tier);
                }
            }
        }
        return totalPower;
    }
    
    /**
     * Özel eşya kontrolü
     */
    private boolean isSpecialItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        // SpecialItemManager'dan kontrol et
        if (specialItemManager != null) {
            // SpecialItemManager'da isSpecialItem metodu yoksa, persistent data'dan kontrol et
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(
                Main.getInstance(), "special_item_id");
            return item.getItemMeta().getPersistentDataContainer()
                .has(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        return false;
    }
    
    /**
     * Özel eşya tier'ı al
     */
    private int getSpecialItemTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        
        // weapon_level key'inden tier'ı al (SpecialItemManager bu şekilde kaydediyor)
        org.bukkit.NamespacedKey tierKey = new org.bukkit.NamespacedKey(
            Main.getInstance(), "weapon_level");
        Integer tier = item.getItemMeta().getPersistentDataContainer()
            .get(tierKey, org.bukkit.persistence.PersistentDataType.INTEGER);
        
        return tier != null ? tier : 0;
    }
    
    /**
     * Buff gücü: PotionEffect + BuffManager buffları
     */
    public double calculateBuffPower(Player player, Clan clan) {
        double totalPower = 0.0;
        
        // PotionEffect buffları
        for (PotionEffect effect : player.getActivePotionEffects()) {
            double multiplier = config.getBuffMultiplier(effect.getType());
            int amplifier = effect.getAmplifier() + 1; // 0-based to 1-based
            totalPower += amplifier * config.getBuffBaseValue() * multiplier;
        }
        
        // BuffManager buffları (Klan bazlı)
        if (buffManager != null && clan != null) {
            // Fatih Buff'ı: %20 hasar artışı
            if (buffManager.hasConquerorBuff(clan)) {
                totalPower += 20.0 * config.getDamageBoostMultiplier();
            }
            
            // Kahraman Buff'ı: %30 hasar artışı
            if (buffManager.hasHeroBuff(clan)) {
                totalPower += 30.0 * config.getDamageBoostMultiplier();
            }
        }
        
        return totalPower;
    }
    
    /**
     * Eğitim gücü
     * TrainingManager'dan ortalama mastery seviyesi al
     * Tüm bataryalar/ritüeller için ortalama mastery seviyesi hesaplanır
     */
    public double calculateTrainingPower(Player player) {
        if (trainingManager == null) return 0.0;
        
        // TrainingManager'dan tüm training verilerini al
        java.util.Map<java.util.UUID, java.util.Map<String, Integer>> allTrainingData = 
            trainingManager.getAllTrainingData();
        java.util.Map<String, Integer> playerTraining = allTrainingData
            .getOrDefault(player.getUniqueId(), new java.util.HashMap<>());
        
        if (playerTraining.isEmpty()) return 0.0;
        
        // Tüm bataryalar/ritüeller için ortalama mastery seviyesi hesapla
        double totalMasteryLevel = 0.0;
        int count = 0;
        
        for (String ritualId : playerTraining.keySet()) {
            int masteryLevel = trainingManager.getMasteryLevel(player.getUniqueId(), ritualId);
            // Mastery seviyesi -1 ise (antrenman modu), 0 olarak say
            if (masteryLevel >= 0) {
                totalMasteryLevel += masteryLevel;
                count++;
            }
        }
        
        if (count == 0) return 0.0;
        
        // Ortalama mastery seviyesi
        double averageMasteryLevel = totalMasteryLevel / count;
        
        // Eğitim gücü = Ortalama Mastery Seviyesi × Çarpan
        return averageMasteryLevel * config.getTrainingLevelMultiplier();
    }
    
    /**
     * Klan teknoloji gücü
     */
    public double calculateClanTechPower(Clan clan) {
        if (clan == null) return 0.0;
        return clan.getTechLevel() * config.getClanTechLevelMultiplier();
    }
}

