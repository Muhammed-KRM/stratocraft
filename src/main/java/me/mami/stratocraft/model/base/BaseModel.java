package me.mami.stratocraft.model.base;

import java.util.UUID;

/**
 * Tüm modellerin temel sınıfı
 * 
 * Ortak özellikler:
 * - ID (UUID)
 * - Oluşturulma zamanı
 * - Son güncelleme zamanı
 */
public abstract class BaseModel {
    protected UUID id;
    protected long createdAt;
    protected long lastUpdated;
    
    public BaseModel() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public BaseModel(UUID id) {
        this.id = id != null ? id : UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        if (id != null) {
            this.id = id;
            updateTimestamp();
        }
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Son güncelleme zamanını şimdiye ayarla
     */
    protected void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }
}

