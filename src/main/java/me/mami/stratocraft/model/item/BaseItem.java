package me.mami.stratocraft.model.item;

import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Tüm item'ların temel modeli
 * 
 * Ortak özellikler:
 * - Item ID (custom_id)
 * - Material
 * - Display Name
 * - Lore
 * - ItemStack referansı
 */
public abstract class BaseItem extends BaseModel {
    protected String itemId; // Custom item ID (örn: "TITANIUM_INGOT")
    protected Material material;
    protected String displayName;
    protected List<String> lore;
    protected ItemStack itemStack; // Bukkit ItemStack referansı
    
    public BaseItem(String itemId, Material material) {
        super();
        this.itemId = itemId;
        this.material = material;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
        updateTimestamp();
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
        updateTimestamp();
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateTimestamp();
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
        updateTimestamp();
    }
    
    public ItemStack getItemStack() {
        return itemStack;
    }
    
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        updateTimestamp();
    }
}

