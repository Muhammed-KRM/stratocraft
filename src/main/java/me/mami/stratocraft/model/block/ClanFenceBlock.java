package me.mami.stratocraft.model.block;

import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Klan Çiti Blok Modeli
 * 
 * Klan çiti bloklarını temsil eden model
 * 
 * Özellikler:
 * - Klan ID'si
 * - Sınır çiti kontrolü
 * - Metadata ile işaretleme
 */
public class ClanFenceBlock extends BaseBlock {
    private UUID ownerClanId; // Hangi klana ait
    private boolean isBoundaryFence; // Sınır çiti mi?
    private int fenceIndex; // Çit sırası (sınır hesaplama için)
    
    public ClanFenceBlock(Location location, UUID ownerClanId) {
        super(location, Material.OAK_FENCE);
        this.ownerClanId = ownerClanId;
        this.isBoundaryFence = false;
        this.fenceIndex = -1;
    }
    
    public ClanFenceBlock(UUID id, Location location, UUID ownerClanId) {
        super(id, location, Material.OAK_FENCE);
        this.ownerClanId = ownerClanId;
        this.isBoundaryFence = false;
        this.fenceIndex = -1;
    }
    
    public UUID getOwnerClanId() {
        return ownerClanId;
    }
    
    public void setOwnerClanId(UUID ownerClanId) {
        this.ownerClanId = ownerClanId;
        updateTimestamp();
    }
    
    public boolean isBoundaryFence() {
        return isBoundaryFence;
    }
    
    public void setBoundaryFence(boolean boundaryFence) {
        this.isBoundaryFence = boundaryFence;
        updateTimestamp();
    }
    
    public int getFenceIndex() {
        return fenceIndex;
    }
    
    public void setFenceIndex(int fenceIndex) {
        this.fenceIndex = fenceIndex;
        updateTimestamp();
    }
}

