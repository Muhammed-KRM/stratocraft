package me.mami.stratocraft.model;

import me.mami.stratocraft.enums.MissionType;
import me.mami.stratocraft.enums.MissionScope;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Mission {
    /**
     * @deprecated me.mami.stratocraft.enums.MissionType kullanın
     */
    @Deprecated
    public enum Type {
        KILL_MOB,              // Mob öldür
        GATHER_ITEM,           // Malzeme topla
        VISIT_LOCATION,        // Lokasyon ziyaret et
        BUILD_STRUCTURE,       // Yapı inşa et
        KILL_PLAYER,           // Oyuncu öldür
        CRAFT_ITEM,            // Item craft et
        MINE_BLOCK,            // Blok kaz
        TRAVEL_DISTANCE        // Mesafe kat et
    }
    
    // Görev Zorluk Seviyesi
    public enum Difficulty {
        EASY,      // Kolay (Seviye 1)
        MEDIUM,    // Orta (Seviye 2-3)
        HARD,      // Zor (Seviye 4-5)
        EXPERT     // Uzman (Seviye 5+)
    }
    
    private final UUID id = UUID.randomUUID();
    private final UUID playerId;
    @Deprecated
    private final Type type; // Eski enum, geriye uyumluluk için
    private MissionType missionType; // Yeni merkezi enum
    private MissionScope scope; // Kişisel mi klan mı?
    private final Difficulty difficulty;
    
    // Hedefler (tip'e göre)
    private EntityType targetEntity = null;      // KILL_MOB için
    private Material targetMaterial = null;      // GATHER_ITEM, CRAFT_ITEM, MINE_BLOCK için
    private Location targetLocation = null;      // VISIT_LOCATION için
    private String structureType = null;         // BUILD_STRUCTURE için
    private UUID targetPlayer = null;            // KILL_PLAYER için
    private int targetDistance = 0;              // TRAVEL_DISTANCE için (blok cinsinden)
    
    // İlerleme
    private int targetAmount;
    private int progress = 0;
    private double travelProgress = 0.0;         // TRAVEL_DISTANCE için (blok cinsinden)
    
    // Ödül
    private final ItemStack reward;
    private final double rewardMoney;
    
    // Süre
    private final long deadline; // Süre (milisaniye)
    
    // Constructor (Yeni sistem - merkezi enum)
    public Mission(UUID playerId, MissionType missionType, MissionScope scope, Difficulty difficulty, 
                   int targetAmount, ItemStack reward, double rewardMoney, long deadlineDays) {
        this.playerId = playerId;
        this.missionType = missionType;
        this.scope = scope;
        this.difficulty = difficulty;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.rewardMoney = rewardMoney;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
        // Geriye uyumluluk için eski enum'u map et
        this.type = convertToOldType(missionType);
    }
    
    // Constructor (Geriye uyumluluk - eski enum)
    @Deprecated
    public Mission(UUID playerId, Type type, Difficulty difficulty, 
                   int targetAmount, ItemStack reward, double rewardMoney, long deadlineDays) {
        this.playerId = playerId;
        this.type = type;
        this.difficulty = difficulty;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.rewardMoney = rewardMoney;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
        // Yeni enum'u map et
        this.missionType = getMissionType(); // Helper metod kullan
        this.scope = determineScopeFromType(type); // Tip'ten scope belirle
    }
    
    /**
     * Eski Type'ı yeni MissionType'a dönüştür
     */
    private Type convertToOldType(MissionType missionType) {
        if (missionType == null) return null;
        try {
            return Type.valueOf(missionType.name());
        } catch (IllegalArgumentException e) {
            // Yeni enum değerlerini eski enum'a map et
            switch (missionType) {
                case KILL_MOBS: return Type.KILL_MOB;
                case COLLECT_ITEMS: return Type.GATHER_ITEM;
                case EXPLORE_AREA: return Type.VISIT_LOCATION;
                case BUILD_STRUCTURE: return Type.BUILD_STRUCTURE;
                case DEFEND_CLAN: return Type.KILL_PLAYER;
                case CRAFT_ITEMS: return Type.CRAFT_ITEM;
                case CLAN_TERRITORY: return Type.BUILD_STRUCTURE;
                case CLAN_WAR: return Type.KILL_PLAYER;
                case CLAN_RESOURCE: return Type.GATHER_ITEM;
                default: return Type.KILL_MOB;
            }
        }
    }
    
    /**
     * Tip'ten scope belirle
     */
    private MissionScope determineScopeFromType(Type type) {
        if (type == null) return MissionScope.PERSONAL;
        switch (type) {
            case BUILD_STRUCTURE:
            case KILL_PLAYER: // Klan savaşı olabilir
                return MissionScope.CLAN;
            default:
                return MissionScope.PERSONAL;
        }
    }
    
    // Eski constructor'lar (geriye uyumluluk için)
    @Deprecated
    public Mission(UUID playerId, Type type, EntityType target, int amount, ItemStack reward) {
        this.playerId = playerId;
        this.type = type;
        this.difficulty = Difficulty.EASY; // Varsayılan
        this.targetEntity = target;
        this.targetMaterial = null;
        this.targetAmount = amount;
        this.reward = reward;
        this.rewardMoney = 0.0;
        this.deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 gün varsayılan
    }
    
    @Deprecated
    public Mission(UUID playerId, Type type, Material target, int amount, ItemStack reward) {
        this.playerId = playerId;
        this.type = type;
        this.difficulty = Difficulty.EASY; // Varsayılan
        this.targetEntity = null;
        this.targetMaterial = target;
        this.targetAmount = amount;
        this.reward = reward;
        this.rewardMoney = 0.0;
        this.deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 gün varsayılan
    }

    // Getter/Setter metodları
    public UUID getId() { return id; }
    public UUID getPlayerId() { return playerId; }
    /**
     * @deprecated me.mami.stratocraft.enums.MissionType kullanın
     */
    @Deprecated
    public Type getType() { return type; }
    
    /**
     * Yeni merkezi enum'u döndür
     */
    public MissionType getMissionType() {
        if (missionType != null) return missionType;
        if (type == null) return null;
        try {
            return MissionType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            // Eski enum değerlerini yeni enum'a map et
            switch (type) {
                case KILL_MOB: return MissionType.KILL_MOBS;
                case GATHER_ITEM: return MissionType.COLLECT_ITEMS;
                case VISIT_LOCATION: return MissionType.EXPLORE_AREA;
                case BUILD_STRUCTURE: return MissionType.BUILD_STRUCTURE;
                case KILL_PLAYER: return MissionType.DEFEND_CLAN; // Yaklaşık eşleşme
                case CRAFT_ITEM: return MissionType.CRAFT_ITEMS;
                case MINE_BLOCK: return MissionType.COLLECT_ITEMS; // Yaklaşık eşleşme
                case TRAVEL_DISTANCE: return MissionType.EXPLORE_AREA; // Yaklaşık eşleşme
                default: return MissionType.KILL_MOBS;
            }
        }
    }
    
    public MissionScope getScope() {
        return scope != null ? scope : MissionScope.PERSONAL;
    }
    
    public void setScope(MissionScope scope) {
        this.scope = scope;
    }
    public Difficulty getDifficulty() { return difficulty; }
    public EntityType getTargetEntity() { return targetEntity; }
    public void setTargetEntity(EntityType entity) { this.targetEntity = entity; }
    public Material getTargetMaterial() { return targetMaterial; }
    public void setTargetMaterial(Material material) { this.targetMaterial = material; }
    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location loc) { this.targetLocation = loc; }
    public String getStructureType() { return structureType; }
    public void setStructureType(String type) { this.structureType = type; }
    public UUID getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(UUID target) { this.targetPlayer = target; }
    public int getTargetDistance() { return targetDistance; }
    public void setTargetDistance(int distance) { this.targetDistance = distance; }
    public int getTargetAmount() { return targetAmount; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public double getTravelProgress() { return travelProgress; }
    public void setTravelProgress(double progress) { this.travelProgress = progress; }
    public void addProgress(int amount) { this.progress += amount; }
    public void addTravelProgress(double amount) { this.travelProgress += amount; }
    public boolean isCompleted() { 
        if (type == Type.TRAVEL_DISTANCE) {
            return travelProgress >= targetDistance;
        }
        return progress >= targetAmount; 
    }
    public ItemStack getReward() { return reward; }
    public double getRewardMoney() { return rewardMoney; }
    public long getDeadline() { return deadline; }
    public boolean isExpired() { return System.currentTimeMillis() > deadline; }
}

