package me.mami.stratocraft.model;

import java.util.UUID;

import org.bukkit.Material;

import me.mami.stratocraft.enums.ContractType;
import me.mami.stratocraft.enums.PenaltyType;

/**
 * Kontrat Şartları - Her oyuncunun kendi şartları
 * Çift taraflı kontrat sisteminde her oyuncu kendi şartlarını belirler
 */
public class ContractTerms {
    private UUID id;
    private UUID contractRequestId;   // Hangi isteğe ait
    private UUID playerId;           // Şartları koyan oyuncu
    private ContractType type;       // RESOURCE_COLLECTION, vb.
    
    // RESOURCE_COLLECTION için
    private Material material;
    private int amount;
    private int delivered = 0;
    
    // COMBAT için
    private UUID targetPlayer;
    
    // TERRITORY için
    private java.util.List<org.bukkit.Location> restrictedAreas;
    private int restrictedRadius;
    
    // CONSTRUCTION için
    private String structureType;
    
    // Genel
    private long deadline;           // Süre (milisaniye)
    private double reward;           // Ödül (altın)
    private PenaltyType penaltyType;
    private double penalty;          // Ceza (altın)
    
    private boolean approved = false; // Oyuncu onayladı mı?
    private boolean completed = false;
    private boolean breached = false;
    
    public ContractTerms(UUID contractRequestId, UUID playerId, ContractType type) {
        this.id = UUID.randomUUID();
        this.contractRequestId = contractRequestId;
        this.playerId = playerId;
        this.type = type;
        this.restrictedAreas = new java.util.ArrayList<>();
    }
    
    // Constructor (veritabanından yükleme için)
    public ContractTerms(UUID id, UUID contractRequestId, UUID playerId, ContractType type,
                         Material material, int amount, int delivered, UUID targetPlayer,
                         long deadline, double reward, PenaltyType penaltyType, double penalty,
                         boolean approved, boolean completed, boolean breached) {
        this.id = id;
        this.contractRequestId = contractRequestId;
        this.playerId = playerId;
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.delivered = delivered;
        this.targetPlayer = targetPlayer;
        this.deadline = deadline;
        this.reward = reward;
        this.penaltyType = penaltyType;
        this.penalty = penalty;
        this.approved = approved;
        this.completed = completed;
        this.breached = breached;
        this.restrictedAreas = new java.util.ArrayList<>();
    }
    
    // Getter/Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getContractRequestId() { return contractRequestId; }
    public UUID getPlayerId() { return playerId; }
    public ContractType getType() { return type; }
    public void setType(ContractType type) { this.type = type; }
    
    // RESOURCE_COLLECTION
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getDelivered() { return delivered; }
    public void setDelivered(int delivered) { this.delivered = delivered; }
    public void addDelivered(int amount) { this.delivered += amount; }
    
    // COMBAT
    public UUID getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(UUID targetPlayer) { this.targetPlayer = targetPlayer; }
    
    // TERRITORY
    public java.util.List<org.bukkit.Location> getRestrictedAreas() { return restrictedAreas; }
    public void setRestrictedAreas(java.util.List<org.bukkit.Location> areas) { 
        this.restrictedAreas = areas != null ? areas : new java.util.ArrayList<>();
    }
    public int getRestrictedRadius() { return restrictedRadius; }
    public void setRestrictedRadius(int radius) { this.restrictedRadius = radius; }
    
    // CONSTRUCTION
    public String getStructureType() { return structureType; }
    public void setStructureType(String type) { this.structureType = type; }
    
    // Genel
    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }
    public double getReward() { return reward; }
    public void setReward(double reward) { this.reward = reward; }
    public PenaltyType getPenaltyType() { return penaltyType; }
    public void setPenaltyType(PenaltyType penaltyType) { this.penaltyType = penaltyType; }
    public double getPenalty() { return penalty; }
    public void setPenalty(double penalty) { this.penalty = penalty; }
    
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public boolean isCompleted() { 
        if (completed) return true;
        if (type == ContractType.RESOURCE_COLLECTION) {
            return delivered >= amount && amount > 0;
        }
        return false;
    }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isBreached() { 
        if (breached) return true;
        // Süre doldu mu?
        if (deadline > 0 && System.currentTimeMillis() > deadline && !isCompleted()) {
            return true;
        }
        return false;
    }
    public void setBreached(boolean breached) { this.breached = breached; }
    
    public boolean isExpired() {
        return deadline > 0 && System.currentTimeMillis() > deadline;
    }
}
