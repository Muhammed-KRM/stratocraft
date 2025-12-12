package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // Yapı tipi → Aktivasyon item'ı
    private final Map<Structure.Type, Material> structureToItem = new ConcurrentHashMap<>();
    
    // Item → Yapı tipi (normal item'lar için)
    private final Map<Material, Structure.Type> itemToStructure = new ConcurrentHashMap<>();
    
    // Özel item → Yapı tipi (custom item'lar için)
    private final Map<String, Structure.Type> customItemToStructure = new ConcurrentHashMap<>();
    
    public StructureActivationItemManager() {
        registerActivationItems();
    }
    
    /**
     * Tüm aktivasyon item'larını kaydet
     */
    private void registerActivationItems() {
        // Basit yapılar - Normal item'lar
        registerActivationItem(Structure.Type.PERSONAL_MISSION_GUILD, Material.IRON_INGOT);
        registerActivationItem(Structure.Type.CLAN_BANK, Material.GOLD_INGOT);
        registerActivationItem(Structure.Type.CONTRACT_OFFICE, Material.DIAMOND);
        registerActivationItem(Structure.Type.CLAN_MISSION_GUILD, Material.EMERALD);
        registerActivationItem(Structure.Type.MARKET_PLACE, Material.COAL);
        registerActivationItem(Structure.Type.RECIPE_LIBRARY, Material.BOOK);
        
        // Karmaşık yapılar - Özel item'lar
        registerCustomActivationItem(Structure.Type.ALCHEMY_TOWER, "TITANIUM_INGOT");
        // Diğer karmaşık yapılar için boss item'ları eklenecek
    }
    
    /**
     * Normal item ile aktivasyon kaydet
     */
    private void registerActivationItem(Structure.Type type, Material item) {
        if (type == null || item == null) return;
        structureToItem.put(type, item);
        itemToStructure.put(item, type);
    }
    
    /**
     * Özel item ile aktivasyon kaydet
     */
    private void registerCustomActivationItem(Structure.Type type, String customItemId) {
        if (type == null || customItemId == null) return;
        // ConcurrentHashMap null value kabul etmez, bu yüzden structureToItem'a eklemiyoruz
        // Sadece customItemToStructure'a ekliyoruz
        customItemToStructure.put(customItemId, type);
    }
    
    /**
     * Item'dan yapı tipini al
     */
    public Structure.Type getStructureTypeForItem(ItemStack item) {
        if (item == null) return null;
        
        // Önce özel item kontrolü
        String customItemId = getCustomItemId(item);
        if (customItemId != null) {
            Structure.Type type = customItemToStructure.get(customItemId);
            if (type != null) return type;
        }
        
        // Normal item kontrolü
        Material material = item.getType();
        return itemToStructure.get(material);
    }
    
    /**
     * Yapı tipi için aktivasyon item'ını al
     */
    public Material getActivationItem(Structure.Type type) {
        if (type == null) return null;
        return structureToItem.get(type);
    }
    
    /**
     * Yapı tipi için özel item ID'sini al
     */
    public String getCustomActivationItemId(Structure.Type type) {
        if (type == null) return null;
        
        // Özel item mapping'de ara
        for (Map.Entry<String, Structure.Type> entry : customItemToStructure.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        
        return null;
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
     * Yapı tipi için aktivasyon item bilgisi (mesaj için)
     */
    public String getActivationItemInfo(Structure.Type type) {
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
}

