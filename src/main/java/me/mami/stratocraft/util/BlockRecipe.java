package me.mami.stratocraft.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Kod İçi Yapı Tarifi
 * 
 * Builder Pattern kullanarak fluent API ile tarif oluşturma
 * 
 * Özellikler:
 * - Merkez bloğa göre relative pozisyonlar
 * - Thread-safe (immutable after build)
 * - Performanslı (lazy validation)
 */
public class BlockRecipe {
    
    private final Material coreMaterial;
    private final List<BlockRequirement> requirements;
    private final String recipeName; // Hata mesajları için
    
    /**
     * Private constructor - Builder pattern kullan
     */
    private BlockRecipe(Material coreMaterial, List<BlockRequirement> requirements, String recipeName) {
        this.coreMaterial = coreMaterial;
        this.requirements = new ArrayList<>(requirements); // Defensive copy
        this.recipeName = recipeName;
    }
    
    /**
     * Yeni tarif oluştur (Builder pattern)
     */
    public static Builder builder(String recipeName) {
        return new Builder(recipeName);
    }
    
    /**
     * Tarif doğrulama (main thread'de çalışmalı - World API thread-safe değil)
     * 
     * @param coreLocation Çekirdek bloğunun konumu
     * @return Tarif doğruysa true
     */
    public boolean validate(Location coreLocation) {
        if (coreLocation == null || coreLocation.getWorld() == null) {
            return false;
        }
        
        Block coreBlock = coreLocation.getBlock();
        
        // Çekirdek bloğu kontrolü
        if (coreBlock.getType() != coreMaterial) {
            return false;
        }
        
        // Tüm gereksinimleri kontrol et
        for (BlockRequirement req : requirements) {
            Location checkLoc = coreLocation.clone().add(req.relX, req.relY, req.relZ);
            
            if (!checkLoc.getWorld().equals(coreLocation.getWorld())) {
                return false;
            }
            
            Block checkBlock = checkLoc.getBlock();
            if (checkBlock.getType() != req.material) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Tarif adını döndür (hata mesajları için)
     */
    public String getRecipeName() {
        return recipeName;
    }
    
    /**
     * Gereksinim sayısını döndür
     */
    public int getRequirementCount() {
        return requirements.size();
    }
    
    /**
     * Çekirdek bloğu al
     */
    public Material getCoreMaterial() {
        return coreMaterial;
    }
    
    /**
     * Tariften yapıyı oluştur (blokları yerleştir)
     * 
     * @param coreLocation Çekirdek bloğunun konumu
     */
    public void build(Location coreLocation) {
        if (coreLocation == null || coreLocation.getWorld() == null) {
            return;
        }
        
        // Çekirdek bloğu yerleştir
        Block coreBlock = coreLocation.getBlock();
        coreBlock.setType(coreMaterial);
        
        // Tüm gereksinimleri yerleştir
        for (BlockRequirement req : requirements) {
            Location blockLoc = coreLocation.clone().add(req.relX, req.relY, req.relZ);
            
            if (!blockLoc.getWorld().equals(coreLocation.getWorld())) {
                continue;
            }
            
            Block block = blockLoc.getBlock();
            block.setType(req.material);
        }
    }
    
    /**
     * Block Requirement (immutable)
     */
    private static class BlockRequirement {
        final int relX, relY, relZ;
        final Material material;
        
        BlockRequirement(int relX, int relY, int relZ, Material material) {
            this.relX = relX;
            this.relY = relY;
            this.relZ = relZ;
            this.material = material;
        }
    }
    
    /**
     * Builder Pattern - Fluent API
     */
    public static class Builder {
        private Material coreMaterial;
        private final List<BlockRequirement> requirements = new ArrayList<>();
        private final String recipeName;
        
        public Builder(String recipeName) {
            this.recipeName = recipeName;
        }
        
        /**
         * Çekirdek bloğu ayarla
         */
        public Builder setCore(Material material) {
            this.coreMaterial = material;
            return this;
        }
        
        /**
         * Blok gereksinimi ekle (relative pozisyon)
         */
        public Builder addBlock(int relX, int relY, int relZ, Material material) {
            if (material == null) {
                throw new IllegalArgumentException("Material cannot be null");
            }
            requirements.add(new BlockRequirement(relX, relY, relZ, material));
            return this;
        }
        
        /**
         * Blok gereksinimi ekle (yukarı)
         */
        public Builder addBlockAbove(Material material) {
            return addBlock(0, 1, 0, material);
        }
        
        /**
         * Blok gereksinimi ekle (aşağı)
         */
        public Builder addBlockBelow(Material material) {
            return addBlock(0, -1, 0, material);
        }
        
        /**
         * Blok gereksinimi ekle (kuzey)
         */
        public Builder addBlockNorth(Material material) {
            return addBlock(0, 0, -1, material);
        }
        
        /**
         * Blok gereksinimi ekle (güney)
         */
        public Builder addBlockSouth(Material material) {
            return addBlock(0, 0, 1, material);
        }
        
        /**
         * Blok gereksinimi ekle (doğu)
         */
        public Builder addBlockEast(Material material) {
            return addBlock(1, 0, 0, material);
        }
        
        /**
         * Blok gereksinimi ekle (batı)
         */
        public Builder addBlockWest(Material material) {
            return addBlock(-1, 0, 0, material);
        }
        
        /**
         * Tarifi oluştur
         */
        public BlockRecipe build() {
            if (coreMaterial == null) {
                throw new IllegalStateException("Core material must be set");
            }
            return new BlockRecipe(coreMaterial, requirements, recipeName);
        }
    }
}

