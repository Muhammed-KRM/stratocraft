package me.mami.stratocraft.model.structure;

import me.mami.stratocraft.enums.StructureCategory;
import me.mami.stratocraft.enums.StructureEffectType;
import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Temel Yapı Modeli
 * 
 * Tüm yapılar için temel sınıf.
 * BaseModel'den türer, yapı durumu, güç, efekt bilgisi gibi alanları içerir.
 */
public class BaseStructure extends BaseModel {
    protected StructureType type;
    protected Location location;
    protected int level;
    protected boolean isActive;
    protected double power;
    protected StructureCategory category;
    protected StructureEffectType effectType;
    protected int shieldFuel;
    protected UUID ownerId; // Yapı sahibi (oyuncu veya klan lideri)
    
    // MAX_FUEL artık ConfigManager'dan gelecek, burada sadece varsayılan değer
    public static int getDefaultMaxFuel() {
        return 12 * 60; // 12 Saat (saniye cinsinden)
    }
    
    public BaseStructure(StructureType type, Location location, int level) {
        super();
        this.type = type;
        this.location = location;
        this.level = level;
        this.isActive = true; // Varsayılan olarak aktif
        this.power = 0.0;
        this.category = determineCategory(type);
        this.effectType = determineEffectType(type);
        this.shieldFuel = 0;
        this.ownerId = null;
    }
    
    public BaseStructure(UUID id, StructureType type, Location location, int level) {
        super(id);
        this.type = type;
        this.location = location;
        this.level = level;
        this.isActive = true;
        this.power = 0.0;
        this.category = determineCategory(type);
        this.effectType = determineEffectType(type);
        this.shieldFuel = 0;
        this.ownerId = null;
    }
    
    // Getters
    public StructureType getType() { return type; }
    public Location getLocation() { return location; }
    public int getLevel() { return level; }
    public boolean isActive() { return isActive; }
    public double getPower() { return power; }
    public StructureCategory getCategory() { return category; }
    public StructureEffectType getEffectType() { return effectType; }
    public int getShieldFuel() { return shieldFuel; }
    public UUID getOwnerId() { return ownerId; }
    
    // Setters
    public void setType(StructureType type) { 
        this.type = type; 
        this.category = determineCategory(type);
        this.effectType = determineEffectType(type);
        updateTimestamp(); 
    }
    
    public void setLocation(Location location) { 
        this.location = location; 
        updateTimestamp(); 
    }
    
    public void setLevel(int level) { 
        this.level = level; 
        updateTimestamp(); 
    }
    
    public void setActive(boolean active) { 
        this.isActive = active; 
        updateTimestamp(); 
    }
    
    public void setPower(double power) { 
        this.power = power; 
        updateTimestamp(); 
    }
    
    public void setCategory(StructureCategory category) { 
        this.category = category; 
        updateTimestamp(); 
    }
    
    public void setEffectType(StructureEffectType effectType) { 
        this.effectType = effectType; 
        updateTimestamp(); 
    }
    
    public void setOwnerId(UUID ownerId) { 
        this.ownerId = ownerId; 
        updateTimestamp(); 
    }
    
    // Shield Fuel Methods
    public void addFuel(int amount, int maxFuel) { 
        this.shieldFuel = Math.min(this.shieldFuel + amount, maxFuel); 
        updateTimestamp();
    }
    
    public void addFuel(int amount) { 
        this.shieldFuel = Math.min(this.shieldFuel + amount, getDefaultMaxFuel()); 
        updateTimestamp();
    }
    
    public void consumeFuel() { 
        if (this.shieldFuel > 0) {
            this.shieldFuel--; 
            updateTimestamp();
        }
    }
    
    public boolean isShieldActive() { 
        return this.shieldFuel > 0; 
    }
    
    /**
     * Yapı tipine göre kategori belirle
     */
    private StructureCategory determineCategory(StructureType type) {
        if (type == null) return StructureCategory.BASIC;
        
        switch (type) {
            case CORE:
                return StructureCategory.BASIC;
            case ALCHEMY_TOWER:
            case POISON_REACTOR:
            case TECTONIC_STABILIZER:
            case SIEGE_FACTORY:
            case WALL_GENERATOR:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case WATCHTOWER:
            case DRONE_STATION:
            case AUTO_TURRET:
            case CATAPULT:
                return StructureCategory.DEFENSE;
            case GLOBAL_MARKET_GATE:
            case AUTO_DRILL:
            case XP_BANK:
            case MAG_RAIL:
            case TELEPORTER:
            case FOOD_SILO:
            case OIL_REFINERY:
                return StructureCategory.ECONOMY;
            case HEALING_BEACON:
            case WEATHER_MACHINE:
            case CROP_ACCELERATOR:
            case MOB_GRINDER:
            case INVISIBILITY_CLOAK:
            case ARMORY:
            case LIBRARY:
            case WARNING_SIGN:
                return StructureCategory.SUPPORT;
            case PERSONAL_MISSION_GUILD:
            case CLAN_MANAGEMENT_CENTER:
            case CLAN_BANK:
            case CLAN_MISSION_GUILD:
            case TRAINING_ARENA:
            case CARAVAN_STATION:
            case CONTRACT_OFFICE:
            case MARKET_PLACE:
            case RECIPE_LIBRARY:
                return StructureCategory.MANAGEMENT;
            default:
                return StructureCategory.BASIC;
        }
    }
    
    /**
     * Yapı tipine göre efekt tipi belirle
     */
    private StructureEffectType determineEffectType(StructureType type) {
        if (type == null) return StructureEffectType.NONE;
        
        switch (type) {
            case ALCHEMY_TOWER:
            case HEALING_BEACON:
            case INVISIBILITY_CLOAK:
            case CROP_ACCELERATOR:
                return StructureEffectType.BUFF;
            case POISON_REACTOR:
            case GRAVITY_WELL:
                return StructureEffectType.DEBUFF;
            case CLAN_BANK:
            case CLAN_MISSION_GUILD:
            case CLAN_MANAGEMENT_CENTER:
            case PERSONAL_MISSION_GUILD:
            case CONTRACT_OFFICE:
            case MARKET_PLACE:
            case RECIPE_LIBRARY:
            case TRAINING_ARENA:
            case CARAVAN_STATION:
            case TELEPORTER:
                return StructureEffectType.UTILITY;
            case WATCHTOWER:
            case AUTO_TURRET:
            case TECTONIC_STABILIZER:
            case WEATHER_MACHINE:
            case AUTO_DRILL:
            case XP_BANK:
                return StructureEffectType.PASSIVE;
            default:
                return StructureEffectType.NONE;
        }
    }
    
    /**
     * Geriye uyumluluk: Eski Structure.Type'dan dönüştür
     */
    @Deprecated
    public static StructureType fromOldType(Structure.Type oldType) {
        if (oldType == null) return null;
        try {
            return StructureType.valueOf(oldType.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Geriye uyumluluk: Eski Structure'a dönüştür
     */
    @Deprecated
    public Structure toLegacyStructure() {
        Structure.Type legacyType = Structure.Type.valueOf(this.type.name());
        Structure legacy = new Structure(legacyType, this.location, this.level);
        legacy.setLevel(this.level);
        legacy.addFuel(this.shieldFuel);
        return legacy;
    }
}

