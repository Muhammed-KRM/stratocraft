package me.mami.stratocraft.manager.config;

import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Klan Alanı Config Yöneticisi
 * 
 * config.yml'den klan alanı ayarlarını yükler
 */
public class TerritoryConfig {
    // Yükseklik Ayarları
    private int skyHeight = 150;
    private int groundDepth = 50;
    
    // Sınır Görselleştirme
    private boolean boundaryParticleEnabled = true;
    private Particle boundaryParticleType = Particle.REDSTONE;
    private Color boundaryParticleColor = Color.GREEN;
    private double boundaryParticleDensity = 0.5;
    private int boundaryParticleUpdateInterval = 20;
    private int boundaryParticleVisibleDistance = 100;
    private double boundaryParticleSpacing = 2.0;
    
    // Alan Genişletme/Küçültme
    private int minArea = 9;
    private int maxArea = 10000;
    private int expansionCooldown = 60;
    private boolean requireFenceConnection = true;
    private boolean checkOverlap = true;
    private int overlapBuffer = 5;
    private int maxExpansionPerAction = 50;
    
    // Çit Ayarları
    private String fenceMaterial = "OAK_FENCE";
    private String fenceMetadataKey = "ClanFence";
    private boolean requireClanFenceItem = true;
    private int minFenceCount = 4;
    private int fenceConnectionDistance = 2;
    private int fenceHeightTolerance = 5; // ✅ YENİ: Çitler arası maksimum yükseklik farkı
    private boolean fenceConnectionRequired = true; // ✅ YENİ: Çitlerin bağlantılı olması gerekli mi?
    
    // Kristal Ayarları
    private String crystalMetadataKey = "ClanCrystal";
    private boolean requireClanCrystalItem = true;
    private int minDistanceFromOtherCrystal = 100;
    
    // Sınır Hesaplama
    private boolean asyncBoundaryCalculation = true;
    private long boundaryCacheDuration = 300000;
    private boolean recalculateOnFenceBreak = true;
    private boolean recalculateOnFencePlace = true;
    
    public void loadFromConfig(FileConfiguration config) {
        // Yükseklik Ayarları
        skyHeight = config.getInt("clan.territory.sky-height", 150);
        groundDepth = config.getInt("clan.territory.ground-depth", 50);
        
        // Sınır Görselleştirme
        boundaryParticleEnabled = config.getBoolean("clan.territory.boundary-particle.enabled", true);
        String particleTypeStr = config.getString("clan.territory.boundary-particle.type", "REDSTONE");
        try {
            boundaryParticleType = Particle.valueOf(particleTypeStr);
        } catch (IllegalArgumentException e) {
            boundaryParticleType = Particle.REDSTONE;
        }
        
        String colorStr = config.getString("clan.territory.boundary-particle.color", "GREEN");
        boundaryParticleColor = parseColor(colorStr);
        boundaryParticleDensity = config.getDouble("clan.territory.boundary-particle.density", 0.5);
        boundaryParticleUpdateInterval = config.getInt("clan.territory.boundary-particle.update-interval", 20);
        boundaryParticleVisibleDistance = config.getInt("clan.territory.boundary-particle.visible-distance", 100);
        boundaryParticleSpacing = config.getDouble("clan.territory.boundary-particle.particle-spacing", 2.0);
        
        // Alan Genişletme/Küçültme
        minArea = config.getInt("clan.territory.expansion.min-area", 9);
        maxArea = config.getInt("clan.territory.expansion.max-area", 10000);
        expansionCooldown = config.getInt("clan.territory.expansion.cooldown", 60);
        requireFenceConnection = config.getBoolean("clan.territory.expansion.require-fence-connection", true);
        checkOverlap = config.getBoolean("clan.territory.expansion.check-overlap", true);
        overlapBuffer = config.getInt("clan.territory.expansion.overlap-buffer", 5);
        maxExpansionPerAction = config.getInt("clan.territory.expansion.max-expansion-per-action", 50);
        
        // Çit Ayarları
        fenceMaterial = config.getString("clan.territory.fence.material", "OAK_FENCE");
        fenceMetadataKey = config.getString("clan.territory.fence.metadata-key", "ClanFence");
        requireClanFenceItem = config.getBoolean("clan.territory.fence.require-clan-fence-item", true);
        minFenceCount = config.getInt("clan.territory.fence.min-fence-count", 4);
        fenceConnectionDistance = config.getInt("clan.territory.fence.fence-connection-distance", 2);
        fenceHeightTolerance = config.getInt("clan.territory.fence.height-tolerance", 5); // ✅ YENİ
        fenceConnectionRequired = config.getBoolean("clan.territory.fence.connection-required", true); // ✅ YENİ
        
        // Kristal Ayarları
        crystalMetadataKey = config.getString("clan.territory.crystal.metadata-key", "ClanCrystal");
        requireClanCrystalItem = config.getBoolean("clan.territory.crystal.require-clan-crystal-item", true);
        minDistanceFromOtherCrystal = config.getInt("clan.territory.crystal.min-distance-from-other", 100);
        
        // Sınır Hesaplama
        asyncBoundaryCalculation = config.getBoolean("clan.territory.boundary-calculation.async", true);
        boundaryCacheDuration = config.getLong("clan.territory.boundary-calculation.cache-duration", 300000);
        recalculateOnFenceBreak = config.getBoolean("clan.territory.boundary-calculation.recalculate-on-fence-break", true);
        recalculateOnFencePlace = config.getBoolean("clan.territory.boundary-calculation.recalculate-on-fence-place", true);
    }
    
    private Color parseColor(String colorStr) {
        if (colorStr == null) return Color.GREEN;
        
        switch (colorStr.toUpperCase()) {
            case "RED": return Color.RED;
            case "GREEN": return Color.GREEN;
            case "BLUE": return Color.BLUE;
            case "YELLOW": return Color.YELLOW;
            case "ORANGE": return Color.ORANGE;
            case "PURPLE": return Color.PURPLE;
            case "WHITE": return Color.WHITE;
            case "BLACK": return Color.BLACK;
            default: return Color.GREEN;
        }
    }
    
    // Getters
    public int getSkyHeight() { return skyHeight; }
    public int getGroundDepth() { return groundDepth; }
    public boolean isBoundaryParticleEnabled() { return boundaryParticleEnabled; }
    public Particle getBoundaryParticleType() { return boundaryParticleType; }
    public Color getBoundaryParticleColor() { return boundaryParticleColor; }
    public double getBoundaryParticleDensity() { return boundaryParticleDensity; }
    public int getBoundaryParticleUpdateInterval() { return boundaryParticleUpdateInterval; }
    public int getBoundaryParticleVisibleDistance() { return boundaryParticleVisibleDistance; }
    public double getBoundaryParticleSpacing() { return boundaryParticleSpacing; }
    public int getMinArea() { return minArea; }
    public int getMaxArea() { return maxArea; }
    public int getExpansionCooldown() { return expansionCooldown; }
    public boolean isRequireFenceConnection() { return requireFenceConnection; }
    public boolean isCheckOverlap() { return checkOverlap; }
    public int getOverlapBuffer() { return overlapBuffer; }
    public int getMaxExpansionPerAction() { return maxExpansionPerAction; }
    public String getFenceMaterial() { return fenceMaterial; }
    public String getFenceMetadataKey() { return fenceMetadataKey; }
    public boolean isRequireClanFenceItem() { return requireClanFenceItem; }
    public int getMinFenceCount() { return minFenceCount; }
    public int getFenceConnectionDistance() { return fenceConnectionDistance; }
    public int getFenceHeightTolerance() { return fenceHeightTolerance; } // ✅ YENİ
    public boolean isFenceConnectionRequired() { return fenceConnectionRequired; } // ✅ YENİ
    public String getCrystalMetadataKey() { return crystalMetadataKey; }
    public boolean isRequireClanCrystalItem() { return requireClanCrystalItem; }
    public int getMinDistanceFromOtherCrystal() { return minDistanceFromOtherCrystal; }
    public boolean isAsyncBoundaryCalculation() { return asyncBoundaryCalculation; }
    public long getBoundaryCacheDuration() { return boundaryCacheDuration; }
    public boolean isRecalculateOnFenceBreak() { return recalculateOnFenceBreak; }
    public boolean isRecalculateOnFencePlace() { return recalculateOnFencePlace; }
}

