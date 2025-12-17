package me.mami.stratocraft.model;

import java.util.UUID;

/**
 * Barış Anlaşması İsteği Modeli
 * 
 * İki klan arasında barış anlaşması isteği için
 */
public class PeaceRequest {
    private UUID id = UUID.randomUUID();
    private UUID senderClanId; // İstek gönderen klan
    private UUID targetClanId; // İstek alan klan
    private long createdAt; // İstek oluşturulma zamanı
    private long expiresAt; // İstek süresi (24 saat)
    private boolean accepted = false; // Onaylandı mı?
    private boolean rejected = false; // Reddedildi mi?
    
    public PeaceRequest(UUID senderClanId, UUID targetClanId) {
        this.senderClanId = senderClanId;
        this.targetClanId = targetClanId;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L); // 24 saat
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSenderClanId() { return senderClanId; }
    public UUID getTargetClanId() { return targetClanId; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isAccepted() { return accepted; }
    public boolean isRejected() { return rejected; }
    
    public void accept() {
        this.accepted = true;
    }
    
    public void reject() {
        this.rejected = true;
    }
    
    /**
     * İstek süresi dolmuş mu?
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    /**
     * İstek hala geçerli mi?
     */
    public boolean isValid() {
        return !accepted && !rejected && !isExpired();
    }
    
    /**
     * İstek belirli bir klanla ilgili mi?
     */
    public boolean involvesClan(UUID clanId) {
        return senderClanId.equals(clanId) || targetClanId.equals(clanId);
    }
    
    /**
     * Diğer klanı bul
     */
    public UUID getOtherClan(UUID clanId) {
        if (senderClanId.equals(clanId)) return targetClanId;
        if (targetClanId.equals(clanId)) return senderClanId;
        return null;
    }
}

