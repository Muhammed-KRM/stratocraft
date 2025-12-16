package me.mami.stratocraft.model.mine;

import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;

import java.util.UUID;

// Not: MineType NewMineManager.MineType olarak kullanılıyor (tuzak mayınları)
// Bu model gelecekte kullanılabilir

/**
 * Mayın Veri Modeli
 * 
 * Mayınların tüm verilerini tutar.
 */
public class MineData extends BaseModel {
    private UUID ownerId; // Mayın sahibi
    private UUID ownerClanId; // Klan mayını ise
    private String type; // Mayın tipi (NewMineManager.MineType enum değeri)
    private Location location;
    private int level; // Mayın seviyesi
    private double damage; // Hasar miktarı
    private boolean isHidden; // Gizli mi?
    private boolean isActive; // Aktif mi?
    private long placedTime; // Yerleştirilme zamanı
    
    public MineData(UUID ownerId, UUID ownerClanId, String type, Location location, int level) {
        super();
        this.ownerId = ownerId;
        this.ownerClanId = ownerClanId;
        this.type = type;
        this.location = location;
        this.level = level;
        this.isHidden = false;
        this.isActive = true;
        this.placedTime = System.currentTimeMillis();
    }
    
    public MineData(UUID id, UUID ownerId, UUID ownerClanId, String type, Location location, int level) {
        super(id);
        this.ownerId = ownerId;
        this.ownerClanId = ownerClanId;
        this.type = type;
        this.location = location;
        this.level = level;
        this.isHidden = false;
        this.isActive = true;
        this.placedTime = System.currentTimeMillis();
    }
    
    // Getters
    public UUID getOwnerId() { return ownerId; }
    public UUID getOwnerClanId() { return ownerClanId; }
    public String getType() { return type; }
    public Location getLocation() { return location; }
    public int getLevel() { return level; }
    public double getDamage() { return damage; }
    public boolean isHidden() { return isHidden; }
    public boolean isActive() { return isActive; }
    public long getPlacedTime() { return placedTime; }
    
    // Setters
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; updateTimestamp(); }
    public void setOwnerClanId(UUID ownerClanId) { this.ownerClanId = ownerClanId; updateTimestamp(); }
    public void setType(String type) { this.type = type; updateTimestamp(); }
    public void setLocation(Location location) { this.location = location; updateTimestamp(); }
    public void setLevel(int level) { this.level = level; updateTimestamp(); }
    public void setDamage(double damage) { this.damage = damage; updateTimestamp(); }
    public void setHidden(boolean hidden) { this.isHidden = hidden; updateTimestamp(); }
    public void setActive(boolean active) { this.isActive = active; updateTimestamp(); }
    public void setPlacedTime(long placedTime) { this.placedTime = placedTime; updateTimestamp(); }
}

