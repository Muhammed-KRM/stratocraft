package me.mami.stratocraft.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.model.Structure;

/**
 * Yapı Aktivasyon Item Yönetimi
 * 
 * Sorumluluklar:
 * - Yapı tipi → Aktivasyon item'ı mapping
 * - Item → Yapı tipi mapping
 * - Özel item kontrolü
 * 
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class StructureActivationItemManager {
    
    // Yapı tipi → Aktivasyon item'ı (YENİ: StructureType)
    private final Map<StructureType, Material> structureToItem = new ConcurrentHashMap<>();
    
    // Item → Yapı tipi (normal item'lar için) (YENİ: StructureType)
    private final Map<Material, StructureType> itemToStructure = new ConcurrentHashMap<>();
    
    // Özel item → Yapı tipi (custom item'lar için) (YENİ: StructureType)
    private final Map<String, StructureType> customItemToStructure = new ConcurrentHashMap<>();
    
    // GERİYE UYUMLULUK: Eski Structure.Type desteği
    @Deprecated
    private final Map<Structure.Type, Material> legacyStructureToItem = new ConcurrentHashMap<>();
    @Deprecated
    private final Map<Material, Structure.Type> legacyItemToStructure = new ConcurrentHashMap<>();
    @Deprecated
    private final Map<String, Structure.Type> legacyCustomItemToStructure = new ConcurrentHashMap<>();
    
    public StructureActivationItemManager() {
        registerActivationItems();
    }
    
    /**
     * Tüm aktivasyon item'larını kaydet
     */
    private void registerActivationItems() {
        // Basit yapılar - Normal item'lar (YENİ: StructureType)
        registerActivationItem(StructureType.PERSONAL_MISSION_GUILD, Material.IRON_INGOT);
        registerActivationItem(StructureType.CLAN_BANK, Material.GOLD_INGOT);
        registerActivationItem(StructureType.CONTRACT_OFFICE, Material.DIAMOND);
        registerActivationItem(StructureType.CLAN_MISSION_GUILD, Material.EMERALD);
        registerActivationItem(StructureType.MARKET_PLACE, Material.COAL);
        registerActivationItem(StructureType.RECIPE_LIBRARY, Material.BOOK);
        
        // Yeni yapılar için aktivasyon item'ları
        registerActivationItem(StructureType.TRAINING_ARENA, Material.IRON_SWORD);
        registerActivationItem(StructureType.CARAVAN_STATION, Material.CHEST);
        registerActivationItem(StructureType.CLAN_MANAGEMENT_CENTER, Material.NETHER_STAR);
        
        // Karmaşık yapılar - Özel item'lar (YENİ: StructureType)
        registerCustomActivationItem(StructureType.ALCHEMY_TOWER, "TITANIUM_INGOT");
        // Diğer karmaşık yapılar için boss item'ları eklenecek
    }
    
    /**
     * Normal item ile aktivasyon kaydet (YENİ: StructureType)
     */
    private void registerActivationItem(StructureType type, Material item) {
        if (type == null || item == null) return;
        structureToItem.put(type, item);
        itemToStructure.put(item, type);
    }
    
    /**
     * Özel item ile aktivasyon kaydet (YENİ: StructureType)
     */
    private void registerCustomActivationItem(StructureType type, String customItemId) {
        if (type == null || customItemId == null) return;
        customItemToStructure.put(customItemId, type);
    }
    
    /**
     * Item'dan yapı tipini al (YENİ: StructureType)
     */
    public StructureType getStructureTypeForItem(ItemStack item) {
        if (item == null) return null;
        
        // Önce özel item kontrolü
        String customItemId = getCustomItemId(item);
        if (customItemId != null) {
            StructureType type = customItemToStructure.get(customItemId);
            if (type != null) return type;
        }
        
        // Normal item kontrolü
        Material material = item.getType();
        return itemToStructure.get(material);
    }
    
    /**
     * Yapı tipi için aktivasyon item'ını al (YENİ: StructureType)
     */
    public Material getActivationItem(StructureType type) {
        if (type == null) return null;
        return structureToItem.get(type);
    }
    
    /**
     * Yapı tipi için özel item ID'sini al (YENİ: StructureType)
     */
    public String getCustomActivationItemId(StructureType type) {
        if (type == null) return null;
        
        for (Map.Entry<String, StructureType> entry : customItemToStructure.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * Normal item ile aktivasyon kaydet (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    private void registerActivationItem(Structure.Type type, Material item) {
        if (type == null || item == null) return;
        legacyStructureToItem.put(type, item);
        legacyItemToStructure.put(item, type);
        // Yeni enum'a da kaydet
        try {
            StructureType newType = StructureType.valueOf(type.name());
            registerActivationItem(newType, item);
        } catch (IllegalArgumentException e) {
            // Eski enum'da yeni enum'da olmayan bir tip varsa
        }
    }
    
    /**
     * Özel item ile aktivasyon kaydet (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    private void registerCustomActivationItem(Structure.Type type, String customItemId) {
        if (type == null || customItemId == null) return;
        legacyCustomItemToStructure.put(customItemId, type);
        // Yeni enum'a da kaydet
        try {
            StructureType newType = StructureType.valueOf(type.name());
            registerCustomActivationItem(newType, customItemId);
        } catch (IllegalArgumentException e) {
            // Eski enum'da yeni enum'da olmayan bir tip varsa
        }
    }
    
    /**
     * Item'dan yapı tipini al (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public Structure.Type getStructureTypeForItemLegacy(ItemStack item) {
        if (item == null) return null;
        
        String customItemId = getCustomItemId(item);
        if (customItemId != null) {
            Structure.Type type = legacyCustomItemToStructure.get(customItemId);
            if (type != null) return type;
        }
        
        Material material = item.getType();
        return legacyItemToStructure.get(material);
    }
    
    /**
     * Yapı tipi için aktivasyon item'ını al (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public Material getActivationItem(Structure.Type type) {
        if (type == null) return null;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getActivationItem(newType);
        } catch (IllegalArgumentException e) {
            return legacyStructureToItem.get(type);
        }
    }
    
    /**
     * Yapı tipi için özel item ID'sini al (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public String getCustomActivationItemId(Structure.Type type) {
        if (type == null) return null;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getCustomActivationItemId(newType);
        } catch (IllegalArgumentException e) {
            for (Map.Entry<String, Structure.Type> entry : legacyCustomItemToStructure.entrySet()) {
                if (entry.getValue() == type) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
    
    /**
     * Item'ın özel item olup olmadığını kontrol et ve ID'sini al
     */
    private String getCustomItemId(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        
        // ItemManager.isCustomItem kullan
        // Tüm özel item ID'lerini kontrol et
        for (String customId : customItemToStructure.keySet()) {
            if (me.mami.stratocraft.manager.ItemManager.isCustomItem(item, customId)) {
                return customId;
            }
        }
        
        return null;
    }
    
    /**
     * Yapı tipi için aktivasyon item bilgisi (mesaj için) (YENİ: StructureType)
     */
    public String getActivationItemInfo(StructureType type) {
        if (type == null) return "Bilinmeyen";
        
        Material material = getActivationItem(type);
        if (material != null) {
            return material.name();
        }
        
        String customId = getCustomActivationItemId(type);
        if (customId != null) {
            return customId;
        }
        
        return "Aktivasyon item'ı bulunamadı";
    }
    
    /**
     * Yapı tipi için aktivasyon item bilgisi (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public String getActivationItemInfo(Structure.Type type) {
        if (type == null) return "Bilinmeyen";
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getActivationItemInfo(newType);
        } catch (IllegalArgumentException e) {
            Material material = legacyStructureToItem.get(type);
            if (material != null) {
                return material.name();
            }
            String customId = getCustomActivationItemId(type);
            if (customId != null) {
                return customId;
            }
            return "Aktivasyon item'ı bulunamadı";
        }
    }
}

