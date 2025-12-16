package me.mami.stratocraft.model.item;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.List;
import java.util.Map;

/**
 * Silah Item Modeli
 * 
 * BaseItem'dan türeyen silah özellikleri:
 * - Hasar değeri
 * - Dayanıklılık
 * - Özel attribute'lar
 * - Özel yetenekler
 */
public class WeaponItem extends BaseItem {
    private double damage;
    private int durability;
    private Map<Attribute, AttributeModifier> attributes;
    private List<String> specialAbilities;
    
    public WeaponItem(String itemId, Material material) {
        super(itemId, material);
        this.damage = 0.0;
        this.durability = 0;
    }
    
    public double getDamage() {
        return damage;
    }
    
    public void setDamage(double damage) {
        this.damage = damage;
        updateTimestamp();
    }
    
    public int getDurability() {
        return durability;
    }
    
    public void setDurability(int durability) {
        this.durability = durability;
        updateTimestamp();
    }
    
    public Map<Attribute, AttributeModifier> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<Attribute, AttributeModifier> attributes) {
        this.attributes = attributes;
        updateTimestamp();
    }
    
    public List<String> getSpecialAbilities() {
        return specialAbilities;
    }
    
    public void setSpecialAbilities(List<String> specialAbilities) {
        this.specialAbilities = specialAbilities;
        updateTimestamp();
    }
}

