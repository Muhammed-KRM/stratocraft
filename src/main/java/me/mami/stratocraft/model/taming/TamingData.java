package me.mami.stratocraft.model.taming;

import me.mami.stratocraft.enums.Gender;
import me.mami.stratocraft.enums.RideableType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Evcil Canlı Veri Modeli
 * 
 * Evcil canlıların tüm verilerini tutar.
 */
public class TamingData extends BaseModel {
    private UUID entityId; // Entity UUID (entity kaydedilemez, sadece UUID)
    private UUID ownerId;
    private UUID clanId; // Klan için evcil canlı ise
    private RideableType rideableType;
    private Gender gender;
    private Location tamingLocation;
    private int level;
    private double maxHealth;
    private double currentHealth;
    private UUID followingTarget; // Takip edilecek oyuncu UUID
    private boolean isTamed;
    private long tamedTime;
    
    public TamingData(UUID entityId, UUID ownerId, RideableType rideableType, 
                    Gender gender, Location tamingLocation, int level) {
        super();
        this.entityId = entityId;
        this.ownerId = ownerId;
        this.clanId = null;
        this.rideableType = rideableType;
        this.gender = gender;
        this.tamingLocation = tamingLocation;
        this.level = level;
        this.isTamed = true;
        this.tamedTime = System.currentTimeMillis();
    }
    
    public TamingData(UUID id, UUID entityId, UUID ownerId, RideableType rideableType,
                    Gender gender, Location tamingLocation, int level) {
        super(id);
        this.entityId = entityId;
        this.ownerId = ownerId;
        this.clanId = null;
        this.rideableType = rideableType;
        this.gender = gender;
        this.tamingLocation = tamingLocation;
        this.level = level;
        this.isTamed = true;
        this.tamedTime = System.currentTimeMillis();
    }
    
    // Getters
    public UUID getEntityId() { return entityId; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getClanId() { return clanId; }
    public RideableType getRideableType() { return rideableType; }
    public Gender getGender() { return gender; }
    public Location getTamingLocation() { return tamingLocation; }
    public int getLevel() { return level; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public UUID getFollowingTarget() { return followingTarget; }
    public boolean isTamed() { return isTamed; }
    public long getTamedTime() { return tamedTime; }
    
    // Setters
    public void setEntityId(UUID entityId) { this.entityId = entityId; updateTimestamp(); }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; updateTimestamp(); }
    public void setClanId(UUID clanId) { this.clanId = clanId; updateTimestamp(); }
    public void setRideableType(RideableType rideableType) { this.rideableType = rideableType; updateTimestamp(); }
    public void setGender(Gender gender) { this.gender = gender; updateTimestamp(); }
    public void setTamingLocation(Location tamingLocation) { this.tamingLocation = tamingLocation; updateTimestamp(); }
    public void setLevel(int level) { this.level = level; updateTimestamp(); }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; updateTimestamp(); }
    public void setCurrentHealth(double currentHealth) { this.currentHealth = currentHealth; updateTimestamp(); }
    public void setFollowingTarget(UUID followingTarget) { this.followingTarget = followingTarget; updateTimestamp(); }
    public void setTamed(boolean tamed) { this.isTamed = tamed; updateTimestamp(); }
    public void setTamedTime(long tamedTime) { this.tamedTime = tamedTime; updateTimestamp(); }
}

