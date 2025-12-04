package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Contract {
    // Sözleşme Tipi Enum
    public enum ContractType {
        MATERIAL_DELIVERY,    // Malzeme temini
        PLAYER_KILL,          // Oyuncu öldürme (bounty)
        TERRITORY_RESTRICT,   // Bölge yasağı
        NON_AGGRESSION,       // Saldırmama
        BASE_PROTECTION,      // Base koruma
        STRUCTURE_BUILD       // Yapı inşa
    }
    
    // Sözleşme Kapsamı Enum
    public enum ContractScope {
        PLAYER_TO_PLAYER,     // Oyuncu → Oyuncu
        CLAN_TO_CLAN,         // Klan → Klan
        PLAYER_TO_CLAN,       // Oyuncu → Klan
        CLAN_TO_PLAYER        // Klan → Oyuncu
    }
    
    // Temel Bilgiler
    private UUID id = UUID.randomUUID();
    private final UUID issuer; // Sözleşmeyi veren
    private UUID acceptor = null; // Sözleşmeyi kabul eden
    private final ContractType type; // Sözleşme tipi
    private final ContractScope scope; // Oyuncu mu, klan mı?
    
    // Ödül ve Ceza
    private final double reward; // Ödül (altın)
    private final double penalty; // İhlal cezası (altın)
    private final long deadline; // Süre (milisaniye)
    
    // İhlal Durumu
    private boolean breached = false; // İhlal edildi mi?
    
    // Tip'e göre hedefler
    // MATERIAL_DELIVERY için
    private Material material = null;
    private int amount = 0;
    private int delivered = 0;
    
    // PLAYER_KILL için
    private UUID targetPlayer = null;
    
    // TERRITORY_RESTRICT için
    private List<Location> restrictedAreas = new ArrayList<>(); // Yasak bölgeler (merkez noktalar)
    private int restrictedRadius = 50; // Yasak bölge yarıçapı (blok)
    
    // NON_AGGRESSION için
    private UUID nonAggressionTarget = null; // Saldırmama anlaşması hedefi
    
    // STRUCTURE_BUILD için
    private String structureType = null; // İnşa edilecek yapı tipi
    
    // Constructor (Yeni sistem)
    public Contract(UUID issuer, ContractType type, ContractScope scope, 
                   double reward, double penalty, long deadlineDays) {
        this.issuer = issuer;
        this.type = type;
        this.scope = scope;
        this.reward = reward;
        this.penalty = penalty;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
    }
    
    // Eski constructor (geriye uyumluluk)
    @Deprecated
    public Contract(UUID issuer, Material material, int amount, double reward, long deadlineDays) {
        this.issuer = issuer;
        this.type = ContractType.MATERIAL_DELIVERY;
        this.scope = ContractScope.PLAYER_TO_PLAYER;
        this.material = material;
        this.amount = amount;
        this.reward = reward;
        this.penalty = reward * 0.5; // Varsayılan ceza = ödülün yarısı
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
    }
    
    // Getter/Setter metodları
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; } // DataManager için
    public UUID getIssuer() { return issuer; }
    public UUID getAcceptor() { return acceptor; }
    public void setAcceptor(UUID acceptor) { this.acceptor = acceptor; }
    public ContractType getType() { return type; }
    public ContractScope getScope() { return scope; }
    public double getReward() { return reward; }
    public double getPenalty() { return penalty; }
    public long getDeadline() { return deadline; }
    public boolean isBreached() { return breached; }
    public void setBreached(boolean breached) { this.breached = breached; }
    public boolean isExpired() { return System.currentTimeMillis() > deadline && !isCompleted(); }
    
    // MATERIAL_DELIVERY için
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getDelivered() { return delivered; }
    public void setDelivered(int delivered) { this.delivered = delivered; }
    public void addDelivered(int amount) { this.delivered += amount; }
    private boolean completed = false; // Tamamlandı mı?
    
    public boolean isCompleted() { 
        if (completed) return true;
        if (type == ContractType.MATERIAL_DELIVERY) {
            return delivered >= amount;
        }
        // Diğer tipler için özel kontrol gerekebilir
        return false;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    // PLAYER_KILL için
    public UUID getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(UUID targetPlayer) { this.targetPlayer = targetPlayer; }
    
    // TERRITORY_RESTRICT için
    public List<Location> getRestrictedAreas() { return restrictedAreas; }
    public void setRestrictedAreas(List<Location> areas) { this.restrictedAreas = areas; }
    public void addRestrictedArea(Location center) { this.restrictedAreas.add(center); }
    public int getRestrictedRadius() { return restrictedRadius; }
    public void setRestrictedRadius(int radius) { this.restrictedRadius = radius; }
    
    // NON_AGGRESSION için
    public UUID getNonAggressionTarget() { return nonAggressionTarget; }
    public void setNonAggressionTarget(UUID target) { this.nonAggressionTarget = target; }
    
    // STRUCTURE_BUILD için
    public String getStructureType() { return structureType; }
    public void setStructureType(String type) { this.structureType = type; }
}

