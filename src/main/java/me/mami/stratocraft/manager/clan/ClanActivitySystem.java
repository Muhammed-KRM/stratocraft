package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klan Aktivite Takip Sistemi
 * 
 * Özellikler:
 * - Üye aktivite takibi (son aktiflik zamanı)
 * - Uzun süre offline üyeleri Recruit'e düşürme
 * - En aktif üyeleri listeleme
 */
public class ClanActivitySystem {
    private final Main plugin;
    private final ClanManager clanManager;
    
    // Üye -> Son aktiflik zamanı
    private final Map<UUID, Long> lastActivityTime = new ConcurrentHashMap<>();
    
    // Config
    private long inactiveThreshold = 2592000000L; // 30 gün (ms)
    private BukkitTask checkTask;
    
    public ClanActivitySystem(Main plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }
    
    /**
     * Config yükle
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration config) {
        inactiveThreshold = config.getLong("clan.activity-system.inactive-threshold", 2592000000L);
        
        // Eğer task çalışıyorsa iptal et
        if (checkTask != null) {
            checkTask.cancel();
        }
        
        // Yeni task başlat (her gün kontrol)
        checkTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkInactiveMembers, 
            0L, 1728000L); // 24 saat = 1728000 tick
    }
    
    /**
     * Aktivite güncelle (oyuncu online olduğunda)
     */
    public void updateActivity(UUID playerId) {
        lastActivityTime.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * Son aktivite zamanını al
     */
    public long getLastActivity(UUID playerId) {
        return lastActivityTime.getOrDefault(playerId, System.currentTimeMillis());
    }
    
    /**
     * Uzun süre offline üyeleri Recruit'e düşür (optimize edilmiş - batch processing)
     */
    private void checkInactiveMembers() {
        if (clanManager == null) return;
        
        java.util.Collection<Clan> allClans = clanManager.getAllClans();
        if (allClans == null || allClans.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        int processedClans = 0;
        int maxClansPerTick = 10; // Her tick'te maksimum 10 klan (lag önleme)
        
        // Offline player name cache (performans)
        java.util.Map<UUID, String> nameCache = new java.util.HashMap<>();
        
        for (Clan clan : allClans) {
            if (processedClans >= maxClansPerTick) break; // Rate limiting
            
            if (clan == null) continue;
            
            java.util.Map<UUID, Clan.Rank> members = clan.getMembers();
            if (members == null || members.isEmpty()) continue;
            
            int processedMembers = 0;
            int maxMembersPerClan = 20; // Her klan için maksimum 20 üye (lag önleme)
            
            // Thread-safe: Copy of keySet kullan
            java.util.Set<UUID> memberIds = new java.util.HashSet<>(members.keySet());
            
            for (UUID memberId : memberIds) {
                if (processedMembers >= maxMembersPerClan) break; // Rate limiting
                
                long lastActivity = getLastActivity(memberId);
                long inactiveTime = currentTime - lastActivity;
                
                // Geçersiz zaman kontrolü
                if (inactiveTime < 0) continue;
                
                if (inactiveTime > inactiveThreshold) {
                    Clan.Rank currentRank = clan.getRank(memberId);
                    if (currentRank != null && 
                        currentRank != Clan.Rank.RECRUIT && 
                        currentRank != Clan.Rank.LEADER) {
                        
                        // Thread-safe: Synchronized
                        synchronized (members) {
                            // Double-check (başka thread değiştirmiş olabilir)
                            if (clan.getRank(memberId) == currentRank) {
                                members.put(memberId, Clan.Rank.RECRUIT);
                            }
                        }
                        
                        // Lider'e bildir (cache kullan)
                        UUID leaderId = clan.getLeader();
                        if (leaderId != null) {
                            Player leader = Bukkit.getPlayer(leaderId);
                            if (leader != null && leader.isOnline()) {
                                // Name cache kullan
                                String playerName = nameCache.get(memberId);
                                if (playerName == null) {
                                    org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);
                                    playerName = offlinePlayer.getName();
                                    if (playerName != null) {
                                        nameCache.put(memberId, playerName);
                                    }
                                }
                                
                                // Config'den threshold'u gün cinsinden hesapla
                                long daysOffline = inactiveThreshold / (24L * 60L * 60L * 1000L);
                                leader.sendMessage("§c" + (playerName != null ? playerName : "Bilinmeyen") + 
                                    " " + daysOffline + " gün offline, Recruit'e düşürüldü!");
                            }
                        }
                    }
                }
                processedMembers++;
            }
            processedClans++;
        }
    }
    
    /**
     * En aktif üyeleri göster (optimize edilmiş)
     */
    public java.util.List<UUID> getMostActiveMembers(Clan clan, int limit) {
        if (clan == null) return new java.util.ArrayList<>();
        
        // Limit kontrolü (performans)
        int maxLimit = Math.min(limit, 100); // Maksimum 100 üye
        
        java.util.Map<UUID, Clan.Rank> members = clan.getMembers();
        if (members == null || members.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // Thread-safe: Copy of keySet kullan
        java.util.Set<UUID> memberIds = new java.util.HashSet<>(members.keySet());
        
        return memberIds.stream()
            .sorted((memberId1, memberId2) -> {
                long time1 = getLastActivity(memberId1);
                long time2 = getLastActivity(memberId2);
                return Long.compare(time2, time1); // En yeni önce
            })
            .limit(maxLimit)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Task'ı durdur
     */
    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
        }
    }
}

