package me.mami.stratocraft.model.block;

import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Tüm blokların temel modeli
 * 
 * Ortak özellikler:
 * - Location (konum)
 * - Material
 * - Owner UUID (sahip)
 * - Active durumu
 */
public abstract class BaseBlock extends BaseModel {
    protected Location location;
    protected Material material;
    protected UUID ownerId;
    protected boolean isActive;
    
    public BaseBlock(Location location, Material material) {
        super();
        this.location = location;
        this.material = material;
        this.isActive = false;
    }
    
    public BaseBlock(UUID id, Location location, Material material) {
        super(id);
        this.location = location;
        this.material = material;
        this.isActive = false;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
        updateTimestamp();
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
        updateTimestamp();
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        updateTimestamp();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        updateTimestamp();
    }
}

