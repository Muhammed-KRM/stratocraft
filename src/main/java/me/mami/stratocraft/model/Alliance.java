package me.mami.stratocraft.model;

import java.util.UUID;

/**
 * İttifak Modeli
 * Klanlar arası kalıcı anlaşmalar için
 */
public class Alliance {
    public enum Type {
        DEFENSIVE,      // Savunma İttifakı: Birine saldırılırsa diğeri yardım eder
        OFFENSIVE,      // Saldırı İttifakı: Birlikte saldırı yapılır
        TRADE,          // Ticaret İttifakı: Ticaret bonusları
        FULL            // Tam İttifak: Her şey (en güçlü)
    }
    
    private UUID id = UUID.randomUUID();
    private UUID clan1Id;
    private UUID clan2Id;
    private Type type;
    private long createdAt;
    private long expiresAt; // 0 = süresiz
    private boolean active = true;
    private boolean broken = false; // İhlal edildi mi?
    private UUID breakerClanId = null; // İhlal eden klan
    
    public Alliance(UUID clan1Id, UUID clan2Id, Type type, long durationDays) {
        this.clan1Id = clan1Id;
        this.clan2Id = clan2Id;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = durationDays > 0 ? System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000) : 0;
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getClan1Id() { return clan1Id; }
    public UUID getClan2Id() { return clan2Id; }
    public Type getType() { return type; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active && !broken && !isExpired(); }
    public boolean isBroken() { return broken; }
    public UUID getBreakerClanId() { return breakerClanId; }
    
    public void setActive(boolean active) { this.active = active; }
    public void breakAlliance(UUID breakerClanId) {
        this.broken = true;
        this.active = false;
        this.breakerClanId = breakerClanId;
    }
    
    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }
    
    public boolean involvesClan(UUID clanId) {
        return clan1Id.equals(clanId) || clan2Id.equals(clanId);
    }
    
    public UUID getOtherClan(UUID clanId) {
        if (clan1Id.equals(clanId)) return clan2Id;
        if (clan2Id.equals(clanId)) return clan1Id;
        return null;
    }
}

