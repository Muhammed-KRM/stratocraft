package me.mami.stratocraft.model.boss;

import me.mami.stratocraft.enums.BossType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;

/**
 * Boss Veri Modeli
 * 
 * Boss'ların tüm verilerini tutar.
 */
public class BossData extends BaseModel {
    private BossType type;
    private UUID entityId; // Entity UUID (entity kaydedilemez, sadece UUID)
    private UUID ownerId;
    private Location spawnLocation;
    private int maxPhase;
    private int currentPhase;
    private List<BossWeakness> weaknesses;
    private long lastAbilityTime;
    private long abilityCooldownMs;
    private double maxHealth;
    private double currentHealth;
    private int level;
    
    /**
     * Boss zayıflıkları
     */
    public enum BossWeakness {
        FIRE,
        WATER,
        POISON,
        LIGHTNING
    }
    
    public BossData(BossType type, UUID entityId, UUID ownerId, Location spawnLocation, 
                   int maxPhase, List<BossWeakness> weaknesses, int level) {
        super();
        this.type = type;
        this.entityId = entityId;
        this.ownerId = ownerId;
        this.spawnLocation = spawnLocation;
        this.maxPhase = maxPhase;
        this.currentPhase = 1;
        this.weaknesses = weaknesses;
        this.lastAbilityTime = 0L;
        this.abilityCooldownMs = 6000L; // 6 saniye varsayılan
        this.level = level;
    }
    
    public BossData(UUID id, BossType type, UUID entityId, UUID ownerId, Location spawnLocation,
                   int maxPhase, List<BossWeakness> weaknesses, int level) {
        super(id);
        this.type = type;
        this.entityId = entityId;
        this.ownerId = ownerId;
        this.spawnLocation = spawnLocation;
        this.maxPhase = maxPhase;
        this.currentPhase = 1;
        this.weaknesses = weaknesses;
        this.lastAbilityTime = 0L;
        this.abilityCooldownMs = 6000L;
        this.level = level;
    }
    
    // Getters
    public BossType getType() { return type; }
    public UUID getEntityId() { return entityId; }
    public UUID getOwnerId() { return ownerId; }
    public Location getSpawnLocation() { return spawnLocation; }
    public int getMaxPhase() { return maxPhase; }
    public int getCurrentPhase() { return currentPhase; }
    public List<BossWeakness> getWeaknesses() { return weaknesses; }
    public long getLastAbilityTime() { return lastAbilityTime; }
    public long getAbilityCooldownMs() { return abilityCooldownMs; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public int getLevel() { return level; }
    
    // Setters
    public void setType(BossType type) { this.type = type; updateTimestamp(); }
    public void setEntityId(UUID entityId) { this.entityId = entityId; updateTimestamp(); }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; updateTimestamp(); }
    public void setSpawnLocation(Location spawnLocation) { this.spawnLocation = spawnLocation; updateTimestamp(); }
    public void setMaxPhase(int maxPhase) { this.maxPhase = maxPhase; updateTimestamp(); }
    public void setCurrentPhase(int currentPhase) { this.currentPhase = currentPhase; updateTimestamp(); }
    public void setWeaknesses(List<BossWeakness> weaknesses) { this.weaknesses = weaknesses; updateTimestamp(); }
    public void setLastAbilityTime(long lastAbilityTime) { this.lastAbilityTime = lastAbilityTime; updateTimestamp(); }
    public void setAbilityCooldownMs(long abilityCooldownMs) { this.abilityCooldownMs = abilityCooldownMs; updateTimestamp(); }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; updateTimestamp(); }
    public void setCurrentHealth(double currentHealth) { this.currentHealth = currentHealth; updateTimestamp(); }
    public void setLevel(int level) { this.level = level; updateTimestamp(); }
}

