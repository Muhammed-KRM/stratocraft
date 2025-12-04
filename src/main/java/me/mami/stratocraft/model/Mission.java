package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Mission {
    // Görev Tipi Enum
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
    private final Type type;
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
    
    // Constructor (Yeni sistem)
    public Mission(UUID playerId, Type type, Difficulty difficulty, 
                   int targetAmount, ItemStack reward, double rewardMoney, long deadlineDays) {
        this.playerId = playerId;
        this.type = type;
        this.difficulty = difficulty;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.rewardMoney = rewardMoney;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
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
    public Type getType() { return type; }
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

