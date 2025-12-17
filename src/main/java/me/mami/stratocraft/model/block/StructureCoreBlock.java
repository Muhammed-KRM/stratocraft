package me.mami.stratocraft.model.block;

import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Yapı Çekirdeği Blok Modeli
 * 
 * BaseBlock'dan türeyen yapı çekirdeği özellikleri:
 * - Structure Type (yapı tipi)
 * - Structure Level (yapı seviyesi)
 * - Is Activated (aktifleştirilmiş mi?)
 * - Owner Clan ID (sahip klan ID - nullable, personal yapılar için null)
 */
public class StructureCoreBlock extends BaseBlock {
    private StructureType structureType; // YENİ: StructureType
    private int structureLevel;
    private boolean isActivated;
    private UUID ownerClanId; // Nullable - personal yapılar için null
    
    public StructureCoreBlock(Location location) {
        super(location, Material.OAK_LOG); // YENİ: LOG kullanıyoruz (tuzak çekirdeği gibi)
        this.structureType = null;
        this.structureLevel = 1;
        this.isActivated = false;
    }
    
    public StructureCoreBlock(UUID id, Location location) {
        super(id, location, Material.OAK_LOG); // YENİ: LOG kullanıyoruz (tuzak çekirdeği gibi)
        this.structureType = null;
        this.structureLevel = 1;
        this.isActivated = false;
    }
    
    public StructureType getStructureType() {
        return structureType;
    }
    
    public void setStructureType(StructureType structureType) {
        this.structureType = structureType;
        updateTimestamp();
    }
    
    /**
     * Yapı tipini al (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public Structure.Type getStructureTypeLegacy() {
        if (structureType == null) return null;
        try {
            return Structure.Type.valueOf(structureType.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Yapı tipini ayarla (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public void setStructureType(Structure.Type structureType) {
        if (structureType == null) {
            this.structureType = null;
            updateTimestamp();
            return;
        }
        try {
            this.structureType = StructureType.valueOf(structureType.name());
            updateTimestamp();
        } catch (IllegalArgumentException e) {
            // Eski enum'da yeni enum'da olmayan bir tip varsa
            this.structureType = null;
            updateTimestamp();
        }
    }
    
    public int getStructureLevel() {
        return structureLevel;
    }
    
    public void setStructureLevel(int structureLevel) {
        this.structureLevel = Math.max(1, structureLevel);
        updateTimestamp();
    }
    
    public boolean isActivated() {
        return isActivated;
    }
    
    public void setActivated(boolean activated) {
        this.isActivated = activated;
        updateTimestamp();
    }
    
    public UUID getOwnerClanId() {
        return ownerClanId;
    }
    
    public void setOwnerClanId(UUID ownerClanId) {
        this.ownerClanId = ownerClanId;
        updateTimestamp();
    }
}

