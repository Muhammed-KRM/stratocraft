package me.mami.stratocraft.model.player;

import me.mami.stratocraft.model.base.BaseModel;
import me.mami.stratocraft.model.Clan;

import java.util.UUID;

/**
 * Oyuncu Veri Modeli
 * 
 * Oyuncunun tüm verilerini tutan merkezi model
 * 
 * Özellikler:
 * - Klan üyeliği
 * - Rütbe
 * - Aktivite
 * - Güç profili referansı
 */
public class PlayerData extends BaseModel {
    private UUID playerId; // Bukkit Player UUID
    private UUID clanId; // Klan ID (null = klansız)
    private Clan.Rank rank; // Klan içi rütbe (null = klansız)
    private boolean isInClan; // Klan durumu bool değişkeni
    private long lastActivity; // Son aktivite zamanı
    private UUID powerProfileId; // PlayerPowerProfile referansı (gelecekte)
    
    public PlayerData(UUID playerId) {
        super();
        this.playerId = playerId;
        this.clanId = null;
        this.rank = null;
        this.isInClan = false;
        this.lastActivity = System.currentTimeMillis();
    }
    
    public PlayerData(UUID id, UUID playerId) {
        super(id);
        this.playerId = playerId;
        this.clanId = null;
        this.rank = null;
        this.isInClan = false;
        this.lastActivity = System.currentTimeMillis();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
        updateTimestamp();
    }
    
    public UUID getClanId() {
        return clanId;
    }
    
    public void setClanId(UUID clanId) {
        this.clanId = clanId;
        this.isInClan = (clanId != null);
        updateTimestamp();
    }
    
    public Clan.Rank getRank() {
        return rank;
    }
    
    public void setRank(Clan.Rank rank) {
        this.rank = rank;
        updateTimestamp();
    }
    
    public boolean isInClan() {
        return isInClan;
    }
    
    public void setInClan(boolean inClan) {
        this.isInClan = inClan;
        if (!inClan) {
            this.clanId = null;
            this.rank = null;
        }
        updateTimestamp();
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
        updateTimestamp();
    }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
        updateTimestamp();
    }
    
    public UUID getPowerProfileId() {
        return powerProfileId;
    }
    
    public void setPowerProfileId(UUID powerProfileId) {
        this.powerProfileId = powerProfileId;
        updateTimestamp();
    }
    
    /**
     * Klan üyeliği ayarla
     */
    public void setClan(UUID clanId, Clan.Rank rank) {
        this.clanId = clanId;
        this.rank = rank;
        this.isInClan = (clanId != null);
        updateTimestamp();
    }
    
    /**
     * Klandan ayrıl
     */
    public void leaveClan() {
        this.clanId = null;
        this.rank = null;
        this.isInClan = false;
        updateTimestamp();
    }
}

