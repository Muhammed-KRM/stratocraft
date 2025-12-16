package me.mami.stratocraft.model.item;

import org.bukkit.Material;

/**
 * Maden Item Modeli
 * 
 * BaseItem'dan türeyen maden özellikleri:
 * - Smelt result (eritme sonucu)
 * - Rarity (nadirlik)
 * - Spawn chance (spawn şansı)
 */
public class OreItem extends BaseItem {
    private Material smeltResult; // Eritme sonucu (örn: TITANIUM_ORE -> TITANIUM_INGOT)
    private int rarity; // 1-10 arası nadirlik
    private double spawnChance; // 0.0-1.0 arası spawn şansı
    
    public OreItem(String itemId, Material material) {
        super(itemId, material);
        this.rarity = 1;
        this.spawnChance = 0.0;
    }
    
    public Material getSmeltResult() {
        return smeltResult;
    }
    
    public void setSmeltResult(Material smeltResult) {
        this.smeltResult = smeltResult;
        updateTimestamp();
    }
    
    public int getRarity() {
        return rarity;
    }
    
    public void setRarity(int rarity) {
        this.rarity = Math.max(1, Math.min(10, rarity)); // 1-10 arası
        updateTimestamp();
    }
    
    public double getSpawnChance() {
        return spawnChance;
    }
    
    public void setSpawnChance(double spawnChance) {
        this.spawnChance = Math.max(0.0, Math.min(1.0, spawnChance)); // 0.0-1.0 arası
        updateTimestamp();
    }
}

