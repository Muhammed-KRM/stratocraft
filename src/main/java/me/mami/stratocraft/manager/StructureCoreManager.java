package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Structure;
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
     * Inaktif çekirdek ekle
     */
    public void addInactiveCore(Location loc, UUID owner) {
        if (loc == null || owner == null) return;
        
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
        return inactiveCores.containsKey(loc);
    }
    
    /**
     * Çekirdek sahibini al
     */
    public UUID getCoreOwner(Location loc) {
        if (loc == null) return null;
        return inactiveCores.get(loc);
    }
    
    /**
     * Çekirdeği aktif yapıya dönüştür
     */
    public void activateCore(Location coreLoc, Structure structure) {
        if (coreLoc == null || structure == null) return;
        
        // Inaktif listeden kaldır
        inactiveCores.remove(coreLoc);
        
        // Aktif listeye ekle
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
        return activeStructures.containsKey(loc);
    }
    
    /**
     * Aktif yapıyı al
     */
    public Structure getActiveStructure(Location loc) {
        if (loc == null) return null;
        return activeStructures.get(loc);
    }
    
    /**
     * Yapıyı kaldır (yıkıldığında)
     */
    public void removeStructure(Location loc) {
        if (loc == null) return;
        
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
        return block.hasMetadata(METADATA_KEY_CORE) || 
               block.hasMetadata("ActiveStructure") ||
               inactiveCores.containsKey(block.getLocation()) ||
               activeStructures.containsKey(block.getLocation());
    }
    
    /**
     * Tüm inaktif çekirdekleri al (backup için)
     */
    public Map<Location, UUID> getAllInactiveCores() {
        return new ConcurrentHashMap<>(inactiveCores);
    }
    
    /**
     * Tüm aktif yapıları al (backup için)
     */
    public Map<Location, Structure> getAllActiveStructures() {
        return new ConcurrentHashMap<>(activeStructures);
    }
    
    /**
     * Temizle (plugin kapanırken)
     */
    public void clear() {
        inactiveCores.clear();
        activeStructures.clear();
    }
}

