package me.mami.stratocraft.model.block;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

/**
 * Tuzak Çekirdeği Blok Modeli
 * 
 * BaseBlock'dan türeyen tuzak özellikleri:
 * - Trap Type (tuzak tipi)
 * - Fuel (yakıt)
 * - Frame Blocks (çerçeve blokları)
 * - Is Covered (üstü kapatılmış mı?)
 * - Owner Clan ID (sahip klan ID)
 */
public class TrapCoreBlock extends BaseBlock {
    private String trapType; // TrapManager.TrapType.name()
    private int fuel; // Kalan patlama hakkı
    private List<Location> frameBlocks; // Magma Block çerçevesi
    private boolean isCovered; // Üstü kapatılmış mı?
    private UUID ownerClanId; // Sahip klan ID (nullable)
    
    // Frame blocks için thread-safe liste
    public TrapCoreBlock(Location location) {
        super(location, Material.LODESTONE);
        this.trapType = null;
        this.fuel = 0;
        this.frameBlocks = new java.util.ArrayList<>();
        this.isCovered = false;
    }
    
    public TrapCoreBlock(UUID id, Location location) {
        super(id, location, Material.LODESTONE);
        this.trapType = null;
        this.fuel = 0;
        this.frameBlocks = new java.util.ArrayList<>();
        this.isCovered = false;
    }
    
    
    public String getTrapType() {
        return trapType;
    }
    
    public void setTrapType(String trapType) {
        this.trapType = trapType;
        updateTimestamp();
    }
    
    public int getFuel() {
        return fuel;
    }
    
    public void setFuel(int fuel) {
        this.fuel = Math.max(0, fuel);
        updateTimestamp();
    }
    
    public void addFuel(int amount) {
        this.fuel = Math.max(0, this.fuel + amount);
        updateTimestamp();
    }
    
    public void consumeFuel() {
        if (fuel > 0) {
            fuel--;
            updateTimestamp();
        }
    }
    
    public List<Location> getFrameBlocks() {
        return frameBlocks;
    }
    
    public void setFrameBlocks(List<Location> frameBlocks) {
        this.frameBlocks = frameBlocks;
        updateTimestamp();
    }
    
    public boolean isCovered() {
        return isCovered;
    }
    
    public void setCovered(boolean covered) {
        this.isCovered = covered;
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

