package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.block.StructureCoreBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yapı Çekirdeği Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - Inaktif çekirdekleri yönetir
 * - Aktif yapıları yönetir
 * - Çekirdek tespit ve kontrol
 * 
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class StructureCoreManager {
    
    private final Main plugin;
    
    // YENİ MODEL: StructureCoreBlock kullanımı
    private final Map<Location, StructureCoreBlock> inactiveCoreBlocks = new ConcurrentHashMap<>();
    private final Map<Location, StructureCoreBlock> activeCoreBlocks = new ConcurrentHashMap<>();
    
    // GERİYE UYUMLULUK: Eski sistem (deprecated, StructureCoreBlock kullanılmalı)
    // Inaktif çekirdekler: Location -> Owner UUID
    private final Map<Location, UUID> inactiveCores = new ConcurrentHashMap<>();
    
    // Aktif yapılar: Location (çekirdek) -> Structure
    private final Map<Location, Structure> activeStructures = new ConcurrentHashMap<>();
    
    // Metadata key
    private static final String METADATA_KEY_CORE = "StructureCore";
    private static final String METADATA_KEY_OWNER = "StructureCoreOwner";
    
    public StructureCoreManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inaktif çekirdek ekle (YENİ MODEL)
     */
    public void addInactiveCore(Location loc, UUID owner) {
        if (loc == null || owner == null) return;
        
        // YENİ MODEL: StructureCoreBlock oluştur
        StructureCoreBlock coreBlock = new StructureCoreBlock(loc);
        coreBlock.setOwnerId(owner);
        coreBlock.setActive(false);
        inactiveCoreBlocks.put(loc, coreBlock);
        
        // GERİYE UYUMLULUK: Eski sistem
        inactiveCores.put(loc, owner);
        
        // Metadata ekle
        Block block = loc.getBlock();
        if (block != null) {
            block.setMetadata(METADATA_KEY_CORE, new FixedMetadataValue(plugin, true));
            block.setMetadata(METADATA_KEY_OWNER, new FixedMetadataValue(plugin, owner.toString()));
        }
    }
    
    /**
     * Inaktif çekirdek kontrolü
     */
    public boolean isInactiveCore(Location loc) {
        if (loc == null) return false;
        // YENİ MODEL: Önce StructureCoreBlock kontrol et
        if (inactiveCoreBlocks.containsKey(loc)) return true;
        // GERİYE UYUMLULUK: Eski sistem
        return inactiveCores.containsKey(loc);
    }
    
    /**
     * Çekirdek sahibini al
     */
    public UUID getCoreOwner(Location loc) {
        if (loc == null) return null;
        // YENİ MODEL: Önce StructureCoreBlock kontrol et
        StructureCoreBlock coreBlock = inactiveCoreBlocks.get(loc);
        if (coreBlock != null) {
            return coreBlock.getOwnerId();
        }
        // GERİYE UYUMLULUK: Eski sistem
        return inactiveCores.get(loc);
    }
    
    /**
     * Inaktif çekirdek bloğunu al (YENİ MODEL)
     */
    public StructureCoreBlock getInactiveCoreBlock(Location loc) {
        if (loc == null) return null;
        return inactiveCoreBlocks.get(loc);
    }
    
    /**
     * Çekirdeği aktif yapıya dönüştür (YENİ MODEL)
     */
    public void activateCore(Location coreLoc, Structure structure) {
        if (coreLoc == null || structure == null) return;
        
        // YENİ MODEL: StructureCoreBlock güncelle
        StructureCoreBlock coreBlock = inactiveCoreBlocks.remove(coreLoc);
        if (coreBlock == null) {
            // Eğer yeni modelde yoksa, oluştur
            coreBlock = new StructureCoreBlock(coreLoc);
            UUID owner = inactiveCores.get(coreLoc);
            if (owner != null) {
                coreBlock.setOwnerId(owner);
            }
        }
        
        // Structure.Type'ı StructureType'a dönüştür
        try {
            me.mami.stratocraft.enums.StructureType structureType = 
                me.mami.stratocraft.enums.StructureType.valueOf(structure.getType().name());
            coreBlock.setStructureType(structureType);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Yapı tipi dönüştürülemedi: " + structure.getType());
        }
        coreBlock.setStructureLevel(structure.getLevel());
        coreBlock.setActivated(true);
        coreBlock.setActive(true);
        
        // Klan kontrolü (structure'dan al)
        // Not: Structure'da klan bilgisi yok, bu yüzden TerritoryManager'dan alınmalı
        // Şimdilik null bırakıyoruz, gerekirse eklenebilir
        
        activeCoreBlocks.put(coreLoc, coreBlock);
        
        // GERİYE UYUMLULUK: Eski sistem
        inactiveCores.remove(coreLoc);
        activeStructures.put(coreLoc, structure);
        
        // Metadata güncelle
        Block block = coreLoc.getBlock();
        if (block != null) {
            block.removeMetadata(METADATA_KEY_CORE, plugin);
            block.removeMetadata(METADATA_KEY_OWNER, plugin);
            block.setMetadata("ActiveStructure", new FixedMetadataValue(plugin, structure.getType().name()));
        }
    }
    
    /**
     * Aktif yapı kontrolü
     */
    public boolean isActiveStructure(Location loc) {
        if (loc == null) return false;
        // YENİ MODEL: Önce StructureCoreBlock kontrol et
        if (activeCoreBlocks.containsKey(loc)) return true;
        // GERİYE UYUMLULUK: Eski sistem
        return activeStructures.containsKey(loc);
    }
    
    /**
     * Aktif yapıyı al (GERİYE UYUMLULUK)
     */
    public Structure getActiveStructure(Location loc) {
        if (loc == null) return null;
        // GERİYE UYUMLULUK: Eski sistem
        return activeStructures.get(loc);
    }
    
    /**
     * Aktif yapı çekirdeğini al (YENİ MODEL)
     */
    public StructureCoreBlock getActiveCoreBlock(Location loc) {
        if (loc == null) return null;
        return activeCoreBlocks.get(loc);
    }
    
    /**
     * Yapıyı kaldır (yıkıldığında - YENİ MODEL)
     */
    public void removeStructure(Location loc) {
        if (loc == null) return;
        
        // YENİ MODEL: StructureCoreBlock temizle
        inactiveCoreBlocks.remove(loc);
        activeCoreBlocks.remove(loc);
        
        // GERİYE UYUMLULUK: Eski sistem
        inactiveCores.remove(loc);
        activeStructures.remove(loc);
        
        // Metadata temizle
        Block block = loc.getBlock();
        if (block != null) {
            block.removeMetadata(METADATA_KEY_CORE, plugin);
            block.removeMetadata(METADATA_KEY_OWNER, plugin);
            block.removeMetadata("ActiveStructure", plugin);
        }
    }
    
    /**
     * Blok çekirdek mi kontrol et (metadata ile)
     */
    public boolean isStructureCore(Block block) {
        if (block == null) return false;
        Location loc = block.getLocation();
        
        // YENİ MODEL: StructureCoreBlock kontrol et
        if (inactiveCoreBlocks.containsKey(loc) || activeCoreBlocks.containsKey(loc)) {
            return true;
        }
        
        // GERİYE UYUMLULUK: Eski sistem
        return block.hasMetadata(METADATA_KEY_CORE) || 
               block.hasMetadata("ActiveStructure") ||
               inactiveCores.containsKey(loc) ||
               activeStructures.containsKey(loc);
    }
    
    /**
     * Tüm inaktif çekirdekleri al (GERİYE UYUMLULUK - deprecated)
     */
    public Map<Location, UUID> getAllInactiveCores() {
        return new ConcurrentHashMap<>(inactiveCores);
    }
    
    /**
     * Tüm aktif yapıları al (GERİYE UYUMLULUK - deprecated)
     */
    public Map<Location, Structure> getAllActiveStructures() {
        return new ConcurrentHashMap<>(activeStructures);
    }
    
    /**
     * Tüm inaktif çekirdek bloklarını al (YENİ MODEL)
     */
    public Map<Location, StructureCoreBlock> getAllInactiveCoreBlocks() {
        return new ConcurrentHashMap<>(inactiveCoreBlocks);
    }
    
    /**
     * Tüm aktif çekirdek bloklarını al (YENİ MODEL)
     */
    public Map<Location, StructureCoreBlock> getAllActiveCoreBlocks() {
        return new ConcurrentHashMap<>(activeCoreBlocks);
    }
    
    /**
     * Temizle (plugin kapanırken)
     */
    public void clear() {
        // YENİ MODEL
        inactiveCoreBlocks.clear();
        activeCoreBlocks.clear();
        
        // GERİYE UYUMLULUK: Eski sistem
        inactiveCores.clear();
        activeStructures.clear();
    }
}

