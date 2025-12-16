package me.mami.stratocraft.util;

import me.mami.stratocraft.enums.StructureCategory;
import me.mami.stratocraft.enums.StructureEffectType;
import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.structure.BaseStructure;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yapı Sistemi Yardımcı Sınıfı
 * 
 * Yapılar için yardımcı fonksiyonlar sağlar:
 * - Türkçe isimler
 * - Açıklamalar
 * - GUI ikonları
 * - Güç katkıları
 * - Yükseltme maliyetleri
 * 
 * Hem eski Structure.Type hem de yeni StructureType enum'unu destekler.
 */
public class StructureHelper {
    
    // ========== YAPI İSİMLERİ ==========
    
    /**
     * Yapı tipinin Türkçe ismini döndür (YENİ: StructureType)
     */
    public static String getStructureDisplayName(StructureType type) {
        if (type == null) return "Bilinmeyen Yapı";
        
        switch (type) {
            case CORE: return "Ana Kristal";
            case ALCHEMY_TOWER: return "Simya Kulesi";
            case POISON_REACTOR: return "Zehir Reaktörü";
            case TECTONIC_STABILIZER: return "Tektonik Sabitleyici";
            case SIEGE_FACTORY: return "Kuşatma Fabrikası";
            case WALL_GENERATOR: return "Sur Jeneratörü";
            case GRAVITY_WELL: return "Yerçekimi Kuyusu";
            case LAVA_TRENCHER: return "Lav Hendekçisi";
            case WATCHTOWER: return "Gözetleme Kulesi";
            case DRONE_STATION: return "Drone İstasyonu";
            case AUTO_TURRET: return "Otomatik Taret";
            case CATAPULT: return "Mancınık";
            case GLOBAL_MARKET_GATE: return "Global Pazar Kapısı";
            case AUTO_DRILL: return "Otomatik Madenci";
            case XP_BANK: return "Tecrübe Bankası";
            case MAG_RAIL: return "Manyetik Ray";
            case TELEPORTER: return "Işınlanma Platformu";
            case FOOD_SILO: return "Buzdolabı";
            case OIL_REFINERY: return "Petrol Rafinerisi";
            case HEALING_BEACON: return "Şifa Kulesi";
            case WEATHER_MACHINE: return "Hava Kontrolcüsü";
            case CROP_ACCELERATOR: return "Tarım Hızlandırıcı";
            case MOB_GRINDER: return "Mob Öğütücü";
            case INVISIBILITY_CLOAK: return "Görünmezlik Perdesi";
            case ARMORY: return "Cephanelik";
            case LIBRARY: return "Kütüphane";
            case WARNING_SIGN: return "Yasaklı Bölge Tabelası";
            case PERSONAL_MISSION_GUILD: return "Kişisel Görev Loncası";
            case CLAN_MANAGEMENT_CENTER: return "Klan Yönetim Merkezi";
            case CLAN_BANK: return "Klan Bankası";
            case CLAN_MISSION_GUILD: return "Klan Görev Loncası";
            case TRAINING_ARENA: return "Eğitim Alanı";
            case CARAVAN_STATION: return "Kervan İstasyonu";
            case CONTRACT_OFFICE: return "Kontrat Bürosu";
            case MARKET_PLACE: return "Market";
            case RECIPE_LIBRARY: return "Tarif Kütüphanesi";
            default: return type.name();
        }
    }
    
    /**
     * Yapı tipinin Türkçe ismini döndür (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public static String getStructureDisplayName(Structure.Type type) {
        if (type == null) return "Bilinmeyen Yapı";
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getStructureDisplayName(newType);
        } catch (IllegalArgumentException e) {
            return type.name();
        }
    }
    
    /**
     * Yapı tipinin Türkçe ismini döndür (BaseStructure için)
     */
    public static String getStructureDisplayName(BaseStructure structure) {
        if (structure == null) return "Bilinmeyen Yapı";
        return getStructureDisplayName(structure.getType());
    }
    
    /**
     * Yapı tipinin Türkçe ismini döndür (GERİYE UYUMLULUK: Structure için)
     * @deprecated BaseStructure kullanın
     */
    @Deprecated
    public static String getStructureDisplayName(Structure structure) {
        if (structure == null) return "Bilinmeyen Yapı";
        return getStructureDisplayName(structure.getType());
    }
    
    // ========== YAPI AÇIKLAMALARI ==========
    
    /**
     * Yapı tipinin açıklamasını döndür (YENİ: StructureType)
     */
    public static List<String> getStructureDescription(StructureType type) {
        List<String> lore = new ArrayList<>();
        if (type == null) {
            lore.add("§7Bilinmeyen yapı");
            return lore;
        }
        
        switch (type) {
            case CORE:
                lore.add("§7Klanın kalbi");
                lore.add("§7Yıkılırsa klan dağılır");
                break;
            case ALCHEMY_TOWER:
                lore.add("§7Batarya gücünü artırır");
                lore.add("§7Simya işlemleri için gerekli");
                break;
            case POISON_REACTOR:
                lore.add("§7Zehirli saldırılar üretir");
                lore.add("§7Savunma için kritik");
                break;
            case TECTONIC_STABILIZER:
                lore.add("§7Felaketlere karşı koruma");
                lore.add("§7Klan kristalini korur");
                break;
            case SIEGE_FACTORY:
                lore.add("§7Kuşatma silahları üretir");
                lore.add("§7Saldırı için gerekli");
                break;
            case WALL_GENERATOR:
                lore.add("§7Otomatik sur oluşturur");
                lore.add("§7Savunma için kritik");
                break;
            case GRAVITY_WELL:
                lore.add("§7Düşmanları yavaşlatır");
                lore.add("§7Alan kontrolü sağlar");
                break;
            case LAVA_TRENCHER:
                lore.add("§7Lav hendekleri oluşturur");
                lore.add("§7Savunma için gerekli");
                break;
            case WATCHTOWER:
                lore.add("§7Geniş görüş alanı");
                lore.add("§7Erken uyarı sistemi");
                break;
            case DRONE_STATION:
                lore.add("§7Otomatik dronlar");
                lore.add("§7Keşif ve saldırı");
                break;
            case AUTO_TURRET:
                lore.add("§7Otomatik savunma");
                lore.add("§7Düşmanları otomatik vurur");
                break;
            case CATAPULT:
                lore.add("§7Mancınık");
                lore.add("§7Kuşatma silahı");
                break;
            case GLOBAL_MARKET_GATE:
                lore.add("§7Global pazara erişim");
                lore.add("§7Uzun mesafe ticaret");
                break;
            case AUTO_DRILL:
                lore.add("§7Otomatik madencilik");
                lore.add("§7Kaynak üretimi");
                break;
            case XP_BANK:
                lore.add("§7Tecrübe depolama");
                lore.add("§7Klan üyeleri paylaşır");
                break;
            case MAG_RAIL:
                lore.add("§7Hızlı ulaşım");
                lore.add("§7Item taşıma");
                break;
            case TELEPORTER:
                lore.add("§7Anında ışınlanma");
                lore.add("§7Uzun mesafe seyahat");
                break;
            case FOOD_SILO:
                lore.add("§7Yiyecek depolama");
                lore.add("§7Açlık sorunu yok");
                break;
            case OIL_REFINERY:
                lore.add("§7Yakıt üretimi");
                lore.add("§7Enerji kaynağı");
                break;
            case HEALING_BEACON:
                lore.add("§7Otomatik şifa");
                lore.add("§7Yakındaki oyuncuları iyileştirir");
                break;
            case WEATHER_MACHINE:
                lore.add("§7Hava kontrolü");
                lore.add("§7Yağmur/kar kontrolü");
                break;
            case CROP_ACCELERATOR:
                lore.add("§7Tarım hızlandırma");
                lore.add("§7Ürün üretimi artar");
                break;
            case MOB_GRINDER:
                lore.add("§7Mob öğütme");
                lore.add("§7Otomatik farm");
                break;
            case INVISIBILITY_CLOAK:
                lore.add("§7Görünmezlik");
                lore.add("§7Klan bölgesini gizler");
                break;
            case ARMORY:
                lore.add("§7Silah/zırh depolama");
                lore.add("§7Hızlı erişim");
                break;
            case LIBRARY:
                lore.add("§7Tarif depolama");
                lore.add("§7Araştırma bonusu");
                break;
            case WARNING_SIGN:
                lore.add("§7Yasaklı bölge işareti");
                lore.add("§7Uyarı sistemi");
                break;
            case PERSONAL_MISSION_GUILD:
                lore.add("§7Kişisel görevler");
                lore.add("§7Her yere yapılabilir");
                break;
            case CLAN_MANAGEMENT_CENTER:
                lore.add("§7Klan yönetimi");
                lore.add("§7Klan menüleri");
                break;
            case CLAN_BANK:
                lore.add("§7Klan bankası");
                lore.add("§7Ortak depolama");
                break;
            case CLAN_MISSION_GUILD:
                lore.add("§7Klan görevleri");
                lore.add("§7Klan içine yapılır");
                break;
            case TRAINING_ARENA:
                lore.add("§7Eğitim alanı");
                lore.add("§7Canlı eğitimi");
                break;
            case CARAVAN_STATION:
                lore.add("§7Kervan istasyonu");
                lore.add("§7Şubeler arası transfer");
                break;
            case CONTRACT_OFFICE:
                lore.add("§7Kontrat bürosu");
                lore.add("§7Genel kullanım");
                break;
            case MARKET_PLACE:
                lore.add("§7Market");
                lore.add("§7Ticaret merkezi");
                break;
            case RECIPE_LIBRARY:
                lore.add("§7Tarif kütüphanesi");
                lore.add("§7Tarif depolama");
                break;
            default:
                lore.add("§7Yapı bilgisi yok");
        }
        return lore;
    }
    
    /**
     * Yapı tipinin açıklamasını döndür (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public static List<String> getStructureDescription(Structure.Type type) {
        if (type == null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Bilinmeyen yapı");
            return lore;
        }
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getStructureDescription(newType);
        } catch (IllegalArgumentException e) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Yapı bilgisi yok");
            return lore;
        }
    }
    
    // ========== YAPI İKONLARI ==========
    
    /**
     * Yapı tipinin GUI ikonunu döndür (YENİ: StructureType)
     */
    public static Material getStructureIcon(StructureType type) {
        if (type == null) return Material.BARRIER;
        
        switch (type) {
            case CORE: return Material.END_CRYSTAL;
            case ALCHEMY_TOWER: return Material.BREWING_STAND;
            case POISON_REACTOR: return Material.BEACON;
            case TECTONIC_STABILIZER: return Material.END_ROD;
            case SIEGE_FACTORY: return Material.ANVIL;
            case WALL_GENERATOR: return Material.COBBLESTONE_WALL;
            case GRAVITY_WELL: return Material.END_PORTAL_FRAME;
            case LAVA_TRENCHER: return Material.LAVA_BUCKET;
            case WATCHTOWER: return Material.BEACON;
            case DRONE_STATION: return Material.DISPENSER;
            case AUTO_TURRET: return Material.DISPENSER;
            case CATAPULT: return Material.DISPENSER;
            case GLOBAL_MARKET_GATE: return Material.ENDER_CHEST;
            case AUTO_DRILL: return Material.IRON_PICKAXE;
            case XP_BANK: return Material.EXPERIENCE_BOTTLE;
            case MAG_RAIL: return Material.POWERED_RAIL;
            case TELEPORTER: return Material.END_PORTAL_FRAME;
            case FOOD_SILO: return Material.CHEST;
            case OIL_REFINERY: return Material.FURNACE;
            case HEALING_BEACON: return Material.BEACON;
            case WEATHER_MACHINE: return Material.CLOCK;
            case CROP_ACCELERATOR: return Material.WHEAT;
            case MOB_GRINDER: return Material.IRON_SWORD;
            case INVISIBILITY_CLOAK: return Material.GLASS;
            case ARMORY: return Material.IRON_CHESTPLATE;
            case LIBRARY: return Material.BOOKSHELF;
            case WARNING_SIGN: return Material.OAK_SIGN;
            case PERSONAL_MISSION_GUILD: return Material.LECTERN;
            case CLAN_MANAGEMENT_CENTER: return Material.BEACON;
            case CLAN_BANK: return Material.ENDER_CHEST;
            case CLAN_MISSION_GUILD: return Material.LECTERN;
            case TRAINING_ARENA: return Material.ANVIL;
            case CARAVAN_STATION: return Material.CHEST;
            case CONTRACT_OFFICE: return Material.CRAFTING_TABLE;
            case MARKET_PLACE: return Material.CHEST;
            case RECIPE_LIBRARY: return Material.LECTERN;
            default: return Material.STONE;
        }
    }
    
    /**
     * Yapı tipinin GUI ikonunu döndür (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public static Material getStructureIcon(Structure.Type type) {
        if (type == null) return Material.BARRIER;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getStructureIcon(newType);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }
    
    // ========== YAPI GÜÇ HESAPLAMA ==========
    
    /**
     * Yapının güç katkısını hesapla (YENİ: BaseStructure)
     */
    public static double getStructurePowerContribution(BaseStructure structure, 
                                                      StratocraftPowerSystem powerSystem) {
        if (structure == null || powerSystem == null) return 0.0;
        
        try {
            double basePower = 10.0; // Temel güç
            double levelMultiplier = 1.0 + (structure.getLevel() - 1) * 0.5; // Seviye çarpanı
            
            return basePower * levelMultiplier;
        } catch (Exception e) {
            return 10.0 * structure.getLevel();
        }
    }
    
    /**
     * Yapının güç katkısını hesapla (GERİYE UYUMLULUK: Structure)
     * @deprecated BaseStructure kullanın
     */
    @Deprecated
    public static double getStructurePowerContribution(Structure structure, 
                                                      StratocraftPowerSystem powerSystem) {
        if (structure == null || powerSystem == null) return 0.0;
        
        try {
            double basePower = 10.0;
            double levelMultiplier = 1.0 + (structure.getLevel() - 1) * 0.5;
            return basePower * levelMultiplier;
        } catch (Exception e) {
            return 10.0 * structure.getLevel();
        }
    }
    
    // ========== YÜKSELTME MALİYETLERİ ==========
    
    /**
     * Yükseltme maliyetini hesapla (YENİ: BaseStructure)
     */
    public static Map<Material, Integer> getUpgradeCost(BaseStructure structure, int targetLevel) {
        Map<Material, Integer> cost = new HashMap<>();
        if (structure == null || targetLevel <= structure.getLevel()) {
            return cost;
        }
        
        int levelDiff = targetLevel - structure.getLevel();
        StructureType type = structure.getType();
        
        // Temel malzemeler
        cost.put(Material.IRON_INGOT, levelDiff * 32);
        cost.put(Material.GOLD_INGOT, levelDiff * 16);
        cost.put(Material.DIAMOND, levelDiff * 4);
        
        // Yapı tipine özel malzemeler
        switch (type) {
            case ALCHEMY_TOWER:
                cost.put(Material.BREWING_STAND, levelDiff * 2);
                cost.put(Material.BLAZE_POWDER, levelDiff * 8);
                break;
            case POISON_REACTOR:
                cost.put(Material.PRISMARINE, levelDiff * 16);
                cost.put(Material.SPIDER_EYE, levelDiff * 8);
                break;
            case TECTONIC_STABILIZER:
                cost.put(Material.OBSIDIAN, levelDiff * 32);
                cost.put(Material.END_ROD, levelDiff * 4);
                break;
            case WATCHTOWER:
                cost.put(Material.STONE_BRICKS, levelDiff * 64);
                cost.put(Material.IRON_BLOCK, levelDiff * 8);
                break;
            case AUTO_TURRET:
                cost.put(Material.IRON_BLOCK, levelDiff * 16);
                cost.put(Material.REDSTONE, levelDiff * 32);
                break;
            default:
                cost.put(Material.STONE, levelDiff * 64);
        }
        return cost;
    }
    
    /**
     * Yükseltme maliyetini hesapla (GERİYE UYUMLULUK: Structure)
     * @deprecated BaseStructure kullanın
     */
    @Deprecated
    public static Map<Material, Integer> getUpgradeCost(Structure structure, int targetLevel) {
        Map<Material, Integer> cost = new HashMap<>();
        if (structure == null || targetLevel <= structure.getLevel()) {
            return cost;
        }
        
        int levelDiff = targetLevel - structure.getLevel();
        Structure.Type type = structure.getType();
        
        cost.put(Material.IRON_INGOT, levelDiff * 32);
        cost.put(Material.GOLD_INGOT, levelDiff * 16);
        cost.put(Material.DIAMOND, levelDiff * 4);
        
        switch (type) {
            case ALCHEMY_TOWER:
                cost.put(Material.BREWING_STAND, levelDiff * 2);
                cost.put(Material.BLAZE_POWDER, levelDiff * 8);
                break;
            case POISON_REACTOR:
                cost.put(Material.PRISMARINE, levelDiff * 16);
                cost.put(Material.SPIDER_EYE, levelDiff * 8);
                break;
            case TECTONIC_STABILIZER:
                cost.put(Material.OBSIDIAN, levelDiff * 32);
                cost.put(Material.END_ROD, levelDiff * 4);
                break;
            case WATCHTOWER:
                cost.put(Material.STONE_BRICKS, levelDiff * 64);
                cost.put(Material.IRON_BLOCK, levelDiff * 8);
                break;
            case AUTO_TURRET:
                cost.put(Material.IRON_BLOCK, levelDiff * 16);
                cost.put(Material.REDSTONE, levelDiff * 32);
                break;
            default:
                cost.put(Material.STONE, levelDiff * 64);
        }
        return cost;
    }
    
    // ========== YÜKSELTME KONTROLÜ ==========
    
    /**
     * Yükseltme yapılabilir mi kontrol et (YENİ: BaseStructure)
     */
    public static boolean canUpgrade(BaseStructure structure, Clan clan, 
                                    org.bukkit.entity.Player player) {
        if (structure == null || clan == null || player == null) {
            return false;
        }
        
        int maxLevel = getMaxLevel(structure.getType());
        if (structure.getLevel() >= maxLevel) {
            return false;
        }
        
        if (!clan.getMembers().containsKey(player.getUniqueId())) {
            return false;
        }
        
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank == null || (!rank.equals(Clan.Rank.LEADER) && !rank.equals(Clan.Rank.GENERAL))) {
            return false;
        }
        
        // Yapı klana ait mi kontrolü (ClanStructure ise)
        if (structure instanceof me.mami.stratocraft.model.structure.ClanStructure) {
            me.mami.stratocraft.model.structure.ClanStructure clanStructure = 
                (me.mami.stratocraft.model.structure.ClanStructure) structure;
            if (!clanStructure.getClanId().equals(clan.getId())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Yükseltme yapılabilir mi kontrol et (GERİYE UYUMLULUK: Structure)
     * @deprecated BaseStructure kullanın
     */
    @Deprecated
    public static boolean canUpgrade(Structure structure, Clan clan, 
                                    org.bukkit.entity.Player player) {
        if (structure == null || clan == null || player == null) {
            return false;
        }
        
        int maxLevel = getMaxLevel(structure.getType());
        if (structure.getLevel() >= maxLevel) {
            return false;
        }
        
        if (!clan.getMembers().containsKey(player.getUniqueId())) {
            return false;
        }
        
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank == null || (!rank.equals(Clan.Rank.LEADER) && !rank.equals(Clan.Rank.GENERAL))) {
            return false;
        }
        
        if (!clan.getStructures().contains(structure)) {
            return false;
        }
        
        return true;
    }
    
    // ========== MAKSİMUM SEVİYE ==========
    
    /**
     * Maksimum seviyeyi döndür (YENİ: StructureType)
     */
    public static int getMaxLevel(StructureType type) {
        if (type == null) return 1;
        return 5; // Tüm yapılar için maksimum seviye 5
    }
    
    /**
     * Maksimum seviyeyi döndür (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public static int getMaxLevel(Structure.Type type) {
        if (type == null) return 1;
        return 5;
    }
    
    // ========== YAPI KATEGORİSİ ==========
    
    /**
     * Yapı kategorisini döndür (YENİ: StructureType -> StructureCategory)
     */
    public static StructureCategory getStructureCategory(StructureType type) {
        if (type == null) return StructureCategory.BASIC;
        
        switch (type) {
            case CORE:
                return StructureCategory.BASIC;
            case ALCHEMY_TOWER:
            case POISON_REACTOR:
            case TECTONIC_STABILIZER:
            case SIEGE_FACTORY:
            case WALL_GENERATOR:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case WATCHTOWER:
            case DRONE_STATION:
            case AUTO_TURRET:
            case CATAPULT:
                return StructureCategory.DEFENSE;
            case GLOBAL_MARKET_GATE:
            case AUTO_DRILL:
            case XP_BANK:
            case MAG_RAIL:
            case TELEPORTER:
            case FOOD_SILO:
            case OIL_REFINERY:
                return StructureCategory.ECONOMY;
            case HEALING_BEACON:
            case WEATHER_MACHINE:
            case CROP_ACCELERATOR:
            case MOB_GRINDER:
            case INVISIBILITY_CLOAK:
            case ARMORY:
            case LIBRARY:
            case WARNING_SIGN:
                return StructureCategory.SUPPORT;
            case PERSONAL_MISSION_GUILD:
            case CLAN_MANAGEMENT_CENTER:
            case CLAN_BANK:
            case CLAN_MISSION_GUILD:
            case TRAINING_ARENA:
            case CARAVAN_STATION:
            case CONTRACT_OFFICE:
            case MARKET_PLACE:
            case RECIPE_LIBRARY:
                return StructureCategory.MANAGEMENT;
            default:
                return StructureCategory.BASIC;
        }
    }
    
    /**
     * Yapı kategorisini döndür (GERİYE UYUMLULUK: Structure.Type -> String)
     * @deprecated StructureType ve StructureCategory kullanın
     */
    @Deprecated
    public static String getStructureCategory(Structure.Type type) {
        if (type == null) return "Diğer";
        
        try {
            StructureType newType = StructureType.valueOf(type.name());
            StructureCategory category = getStructureCategory(newType);
            return category.name();
        } catch (IllegalArgumentException e) {
            return "Diğer";
        }
    }
}
