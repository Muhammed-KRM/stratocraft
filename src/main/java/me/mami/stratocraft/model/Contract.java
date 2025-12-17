package me.mami.stratocraft.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

public class Contract {
    /**
     * @deprecated me.mami.stratocraft.enums.ContractType kullanın
     */
    @Deprecated
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
    @Deprecated
    private final ContractType type; // Sözleşme tipi (eski enum, geriye uyumluluk için)
    private me.mami.stratocraft.enums.ContractType contractType; // Yeni merkezi enum
    private me.mami.stratocraft.enums.PenaltyType penaltyType; // Ceza tipi
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
    
    // Çift taraflı kontrat için (YENİ)
    private UUID playerA = null;            // İlk oyuncu
    private UUID playerB = null;            // İkinci oyuncu
    private UUID contractRequestId = null;  // Orijinal istek
    private ContractTerms termsA = null;    // Oyuncu A'nın şartları
    private ContractTerms termsB = null;    // Oyuncu B'nin şartları
    private ContractStatus contractStatus = null; // ACTIVE, COMPLETED, BREACHED
    private long startedAt = 0;             // Aktif olma zamanı
    private long completedAt = 0;          // Tamamlanma zamanı
    private long breachedAt = 0;           // İhlal zamanı
    private UUID breacher = null;           // İhlal eden oyuncu
    
    /**
     * Kontrat durumu enum'u (çift taraflı kontrat için)
     */
    public enum ContractStatus {
        PENDING_TERMS_A,    // Oyuncu A şartlarını belirliyor
        PENDING_TERMS_B,    // Oyuncu B şartlarını belirliyor
        PENDING_APPROVAL,   // Her iki taraf da onay bekleniyor
        PENDING_FINAL_APPROVAL, // İlk gönderen oyuncunun son onayı bekleniyor
        ACTIVE,             // Aktif
        COMPLETED,          // Tamamlandı
        BREACHED,           // İhlal edildi
        CANCELLED           // İptal edildi
    }
    
    // Constructor (Çift taraflı kontrat için - YENİ)
    public Contract(UUID playerA, UUID playerB, UUID contractRequestId, 
                   ContractTerms termsA, ContractTerms termsB) {
        this.id = UUID.randomUUID();
        this.playerA = playerA;
        this.playerB = playerB;
        this.contractRequestId = contractRequestId;
        this.termsA = termsA;
        this.termsB = termsB;
        this.contractStatus = ContractStatus.ACTIVE;
        this.startedAt = System.currentTimeMillis();
        
        // Geriye uyumluluk için issuer/acceptor set et
        this.issuer = playerA;
        this.acceptor = playerB;
        this.scope = ContractScope.PLAYER_TO_PLAYER;
        
        // İlk şarttan tip bilgilerini al
        if (termsA != null) {
            this.contractType = termsA.getType();
            this.penaltyType = termsA.getPenaltyType();
            // Final field'ları initialize et (geriye uyumluluk için)
            this.type = convertToOldContractType(termsA.getType());
            this.reward = termsA.getReward(); // Varsayılan olarak termsA'nın ödülü
            this.penalty = termsA.getPenalty(); // Varsayılan olarak termsA'nın cezası
            this.deadline = termsA.getDeadline(); // Varsayılan olarak termsA'nın deadline'ı
        } else {
            // Fallback değerler
            this.type = ContractType.MATERIAL_DELIVERY;
            this.reward = 0;
            this.penalty = 0;
            this.deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 gün
        }
    }
    
    // Constructor (Yeni sistem - merkezi enum)
    public Contract(UUID issuer, me.mami.stratocraft.enums.ContractType contractType, ContractScope scope, 
                   double reward, me.mami.stratocraft.enums.PenaltyType penaltyType, long deadlineDays) {
        this.issuer = issuer;
        this.contractType = contractType;
        this.penaltyType = penaltyType;
        this.scope = scope;
        this.reward = reward;
        this.penalty = reward * 0.5; // Varsayılan ceza = ödülün yarısı
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
        // Geriye uyumluluk için eski enum'u map et
        this.type = convertToOldContractType(contractType);
    }
    
    // Constructor (Geriye uyumluluk - eski enum)
    @Deprecated
    public Contract(UUID issuer, ContractType type, ContractScope scope, 
                   double reward, double penalty, long deadlineDays) {
        this.issuer = issuer;
        this.type = type;
        this.scope = scope;
        this.reward = reward;
        this.penalty = penalty;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
        // Yeni enum'u map et
        this.contractType = getContractType(); // Helper metod kullan
        this.penaltyType = me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY; // Varsayılan
    }
    
    /**
     * Eski ContractType'ı yeni ContractType'a dönüştür
     */
    private ContractType convertToOldContractType(me.mami.stratocraft.enums.ContractType type) {
        if (type == null) return null;
        try {
            return ContractType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            // Yeni enum değerlerini eski enum'a map et
            switch (type) {
                case RESOURCE_COLLECTION: return ContractType.MATERIAL_DELIVERY;
                case CONSTRUCTION: return ContractType.STRUCTURE_BUILD;
                case COMBAT: return ContractType.PLAYER_KILL;
                case TERRITORY: return ContractType.TERRITORY_RESTRICT;
                default: return ContractType.MATERIAL_DELIVERY;
            }
        }
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
    /**
     * @deprecated me.mami.stratocraft.enums.ContractType kullanın
     */
    @Deprecated
    public ContractType getType() { return type; }
    
    /**
     * Yeni merkezi enum'u döndür
     */
    public me.mami.stratocraft.enums.ContractType getContractType() {
        if (contractType != null) return contractType;
        if (type == null) return null;
        try {
            return me.mami.stratocraft.enums.ContractType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            // Eski enum değerlerini yeni enum'a map et
            switch (type) {
                case MATERIAL_DELIVERY: return me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION;
                case PLAYER_KILL: return me.mami.stratocraft.enums.ContractType.COMBAT;
                case TERRITORY_RESTRICT: return me.mami.stratocraft.enums.ContractType.TERRITORY;
                case NON_AGGRESSION: return me.mami.stratocraft.enums.ContractType.COMBAT; // Saldırmama = combat
                case BASE_PROTECTION: return me.mami.stratocraft.enums.ContractType.TERRITORY; // Base koruma = territory
                case STRUCTURE_BUILD: return me.mami.stratocraft.enums.ContractType.CONSTRUCTION;
                default: return me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION;
            }
        }
    }
    
    public me.mami.stratocraft.enums.PenaltyType getPenaltyType() {
        return penaltyType != null ? penaltyType : me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY;
    }
    
    public void setPenaltyType(me.mami.stratocraft.enums.PenaltyType penaltyType) {
        this.penaltyType = penaltyType;
    }
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
    
    // Çift taraflı kontrat için getter/setter (YENİ)
    public UUID getPlayerA() { return playerA; }
    public void setPlayerA(UUID playerA) { this.playerA = playerA; }
    public UUID getPlayerB() { return playerB; }
    public void setPlayerB(UUID playerB) { this.playerB = playerB; }
    public UUID getContractRequestId() { return contractRequestId; }
    public void setContractRequestId(UUID contractRequestId) { this.contractRequestId = contractRequestId; }
    public ContractTerms getTermsA() { return termsA; }
    public void setTermsA(ContractTerms termsA) { this.termsA = termsA; }
    public ContractTerms getTermsB() { return termsB; }
    public void setTermsB(ContractTerms termsB) { this.termsB = termsB; }
    public ContractStatus getContractStatus() { return contractStatus; }
    public void setContractStatus(ContractStatus status) { this.contractStatus = status; }
    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public long getBreachedAt() { return breachedAt; }
    public void setBreachedAt(long breachedAt) { this.breachedAt = breachedAt; }
    public UUID getBreacher() { return breacher; }
    public void setBreacher(UUID breacher) { this.breacher = breacher; }
    
    /**
     * Çift taraflı kontrat mı?
     */
    public boolean isBilateralContract() {
        return playerA != null && playerB != null && termsA != null && termsB != null;
    }
    
    /**
     * Çift taraflı kontrat tamamlandı mı?
     */
    public boolean isBilateralCompleted() {
        if (!isBilateralContract()) return false;
        return termsA.isCompleted() && termsB.isCompleted();
    }
    
    /**
     * Çift taraflı kontrat ihlal edildi mi?
     */
    public boolean isBilateralBreached() {
        if (!isBilateralContract()) return false;
        return termsA.isBreached() || termsB.isBreached();
    }
}

