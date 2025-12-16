package me.mami.stratocraft.model;

import java.util.UUID;

/**
 * Kontrat İsteği - Çift taraflı kontrat sistemi için
 * Bir oyuncu diğerine kontrat isteği gönderir
 */
public class ContractRequest {
    private UUID id;
    private UUID sender;              // İstek gönderen
    private UUID target;              // İstek alan
    private Contract.ContractScope scope; // PLAYER_TO_PLAYER, vb.
    private ContractRequestStatus status; // PENDING, ACCEPTED, REJECTED, CANCELLED
    private long createdAt;
    private long respondedAt;         // Kabul/red zamanı
    
    public ContractRequest(UUID sender, UUID target, Contract.ContractScope scope) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.target = target;
        this.scope = scope;
        this.status = ContractRequestStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Constructor (veritabanından yükleme için)
    public ContractRequest(UUID id, UUID sender, UUID target, Contract.ContractScope scope, 
                          ContractRequestStatus status, long createdAt, Long respondedAt) {
        this.id = id;
        this.sender = sender;
        this.target = target;
        this.scope = scope;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt != null ? respondedAt : 0;
    }
    
    // Getter/Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSender() { return sender; }
    public UUID getTarget() { return target; }
    public Contract.ContractScope getScope() { return scope; }
    public ContractRequestStatus getStatus() { return status; }
    public void setStatus(ContractRequestStatus status) { 
        this.status = status;
        if (status != ContractRequestStatus.PENDING) {
            this.respondedAt = System.currentTimeMillis();
        }
    }
    public long getCreatedAt() { return createdAt; }
    public long getRespondedAt() { return respondedAt; }
    public void setRespondedAt(long respondedAt) { this.respondedAt = respondedAt; }
    
    /**
     * İstek durumu enum'u
     */
    public enum ContractRequestStatus {
        PENDING,      // Beklemede
        ACCEPTED,     // Kabul edildi
        REJECTED,     // Reddedildi
        CANCELLED     // İptal edildi
    }
}
