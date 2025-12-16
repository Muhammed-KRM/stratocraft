package me.mami.stratocraft.model.battery;

import me.mami.stratocraft.enums.BatteryCategory;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Batarya Veri Modeli
 * 
 * Bataryaların tüm verilerini tutar.
 */
public class BatteryData extends BaseModel {
    private String batteryName; // Batarya adı
    private BatteryCategory category;
    private UUID ownerId; // Batarya sahibi
    private UUID clanId; // Klan bataryası ise
    private Location location; // Batarya konumu (yapılmışsa)
    private Material fuel; // Yakıt tipi
    private int alchemyLevel; // Simya seviyesi
    private boolean hasAmplifier; // Amplifikatör var mı?
    private double trainingMultiplier; // Eğitim çarpanı
    private boolean isRedDiamond; // Kırmızı elmas var mı?
    private boolean isDarkMatter; // Karanlık madde var mı?
    private int batteryLevel; // Batarya seviyesi (1-5)
    private boolean isActive; // Aktif mi?
    
    public BatteryData(String batteryName, BatteryCategory category, 
                      UUID ownerId, Material fuel, int alchemyLevel, boolean hasAmplifier,
                      double trainingMultiplier, boolean isRedDiamond, boolean isDarkMatter, int batteryLevel) {
        super();
        this.batteryName = batteryName;
        this.category = category;
        this.ownerId = ownerId;
        this.clanId = null;
        this.location = null;
        this.fuel = fuel;
        this.alchemyLevel = alchemyLevel;
        this.hasAmplifier = hasAmplifier;
        this.trainingMultiplier = trainingMultiplier;
        this.isRedDiamond = isRedDiamond;
        this.isDarkMatter = isDarkMatter;
        this.batteryLevel = batteryLevel;
        this.isActive = true;
    }
    
    public BatteryData(UUID id, String batteryName, BatteryCategory category,
                      UUID ownerId, Material fuel, int alchemyLevel, boolean hasAmplifier,
                      double trainingMultiplier, boolean isRedDiamond, boolean isDarkMatter, int batteryLevel) {
        super(id);
        this.batteryName = batteryName;
        this.category = category;
        this.ownerId = ownerId;
        this.clanId = null;
        this.location = null;
        this.fuel = fuel;
        this.alchemyLevel = alchemyLevel;
        this.hasAmplifier = hasAmplifier;
        this.trainingMultiplier = trainingMultiplier;
        this.isRedDiamond = isRedDiamond;
        this.isDarkMatter = isDarkMatter;
        this.batteryLevel = batteryLevel;
        this.isActive = true;
    }
    
    // Getters
    public String getBatteryName() { return batteryName; }
    public BatteryCategory getCategory() { return category; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getClanId() { return clanId; }
    public Location getLocation() { return location; }
    public Material getFuel() { return fuel; }
    public int getAlchemyLevel() { return alchemyLevel; }
    public boolean hasAmplifier() { return hasAmplifier; }
    public double getTrainingMultiplier() { return trainingMultiplier; }
    public boolean isRedDiamond() { return isRedDiamond; }
    public boolean isDarkMatter() { return isDarkMatter; }
    public int getBatteryLevel() { return batteryLevel; }
    public boolean isActive() { return isActive; }
    
    // Setters
    public void setBatteryName(String batteryName) { this.batteryName = batteryName; updateTimestamp(); }
    public void setCategory(BatteryCategory category) { this.category = category; updateTimestamp(); }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; updateTimestamp(); }
    public void setClanId(UUID clanId) { this.clanId = clanId; updateTimestamp(); }
    public void setLocation(Location location) { this.location = location; updateTimestamp(); }
    public void setFuel(Material fuel) { this.fuel = fuel; updateTimestamp(); }
    public void setAlchemyLevel(int alchemyLevel) { this.alchemyLevel = alchemyLevel; updateTimestamp(); }
    public void setHasAmplifier(boolean hasAmplifier) { this.hasAmplifier = hasAmplifier; updateTimestamp(); }
    public void setTrainingMultiplier(double trainingMultiplier) { this.trainingMultiplier = trainingMultiplier; updateTimestamp(); }
    public void setRedDiamond(boolean redDiamond) { this.isRedDiamond = redDiamond; updateTimestamp(); }
    public void setDarkMatter(boolean darkMatter) { this.isDarkMatter = darkMatter; updateTimestamp(); }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; updateTimestamp(); }
    public void setActive(boolean active) { this.isActive = active; updateTimestamp(); }
}

