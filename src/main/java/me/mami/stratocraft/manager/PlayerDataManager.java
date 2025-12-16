package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.player.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oyuncu Veri Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - PlayerData modelini yönetir
 * - Klan üyeliği takibi
 * - Aktivite takibi
 * 
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class PlayerDataManager {
    
    private final Main plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    
    public PlayerDataManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Oyuncu verisini al (yoksa oluştur)
     */
    public PlayerData getPlayerData(UUID playerId) {
        if (playerId == null) return null;
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData(playerId));
    }
    
    /**
     * Oyuncu verisini al (oluşturma)
     */
    public PlayerData getPlayerDataOrNull(UUID playerId) {
        if (playerId == null) return null;
        return playerDataMap.get(playerId);
    }
    
    /**
     * Klan üyeliği ayarla
     */
    public void setClan(UUID playerId, UUID clanId, Clan.Rank rank) {
        if (playerId == null) return;
        
        PlayerData data = getPlayerData(playerId);
        data.setClan(clanId, rank);
    }
    
    /**
     * Klandan ayrıl
     */
    public void leaveClan(UUID playerId) {
        if (playerId == null) return;
        
        PlayerData data = getPlayerData(playerId);
        data.leaveClan();
    }
    
    /**
     * Oyuncu klan durumu kontrolü
     */
    public boolean isInClan(UUID playerId) {
        if (playerId == null) return false;
        
        PlayerData data = getPlayerDataOrNull(playerId);
        return data != null && data.isInClan();
    }
    
    /**
     * Oyuncunun klan ID'sini al
     */
    public UUID getClanId(UUID playerId) {
        if (playerId == null) return null;
        
        PlayerData data = getPlayerDataOrNull(playerId);
        return data != null ? data.getClanId() : null;
    }
    
    /**
     * Oyuncunun rütbesini al
     */
    public Clan.Rank getRank(UUID playerId) {
        if (playerId == null) return null;
        
        PlayerData data = getPlayerDataOrNull(playerId);
        return data != null ? data.getRank() : null;
    }
    
    /**
     * Aktivite güncelle
     */
    public void updateActivity(UUID playerId) {
        if (playerId == null) return;
        
        PlayerData data = getPlayerData(playerId);
        data.updateActivity();
    }
    
    /**
     * Oyuncu verisini kaydet (DataManager için)
     */
    public void savePlayerData(UUID playerId, PlayerData data) {
        if (playerId == null || data == null) return;
        playerDataMap.put(playerId, data);
    }
    
    /**
     * Oyuncu verisini yükle (DataManager için)
     */
    public void loadPlayerData(UUID playerId, PlayerData data) {
        if (playerId == null || data == null) return;
        playerDataMap.put(playerId, data);
    }
    
    /**
     * Tüm oyuncu verilerini al (backup için)
     */
    public Map<UUID, PlayerData> getAllPlayerData() {
        return new ConcurrentHashMap<>(playerDataMap);
    }
    
    /**
     * Oyuncu verisini temizle (oyuncu çıkışında)
     */
    public void removePlayerData(UUID playerId) {
        if (playerId == null) return;
        // Not: Veriyi silmeyiz, sadece memory'den temizleriz (DataManager'da kayıtlı)
        // playerDataMap.remove(playerId);
    }
    
    /**
     * Temizle (plugin kapanırken)
     */
    public void clear() {
        playerDataMap.clear();
    }
}

