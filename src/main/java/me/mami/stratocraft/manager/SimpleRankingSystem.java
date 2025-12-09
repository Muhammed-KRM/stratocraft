package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basit Güç Sıralaması Sistemi
 * 
 * Test için basit versiyon - sadece online oyuncuları sıralar
 * ✅ PERFORMANS: Cache kullanarak gereksiz hesaplamaları önler
 */
public class SimpleRankingSystem {
    private final Main plugin;
    
    // ✅ PERFORMANS: Sıralama cache (5 saniye)
    private List<PlayerRanking> cachedTopPlayers = new ArrayList<>();
    private long lastTopPlayersUpdate = 0;
    private static final long RANKING_CACHE_DURATION = 5000L; // 5 saniye
    
    public SimpleRankingSystem(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Top oyuncuları al
     * ✅ PERFORMANS: Cache kullanarak gereksiz hesaplamaları önler
     */
    public List<PlayerRanking> getTopPlayers(int limit) {
        StratocraftPowerSystem powerSystem = plugin.getStratocraftPowerSystem();
        if (powerSystem == null) return new ArrayList<>();
        
        long now = System.currentTimeMillis();
        
        // Cache kontrolü (limit aynıysa)
        if (now - lastTopPlayersUpdate < RANKING_CACHE_DURATION && 
            !cachedTopPlayers.isEmpty() && 
            cachedTopPlayers.size() >= limit) {
            return cachedTopPlayers.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }
        
        List<PlayerRanking> rankings = new ArrayList<>();
        
        // Tüm oyuncular için güç hesapla (cache kullanır)
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(player); // Cache kullanır
            rankings.add(new PlayerRanking(
                player.getName(),
                profile.getTotalSGP(),
                profile.getPlayerLevel()
            ));
        }
        
        // Sırala ve cache'e kaydet
        cachedTopPlayers = rankings.stream()
            .sorted(Comparator.comparing(PlayerRanking::getPower).reversed())
            .collect(Collectors.toList());
        lastTopPlayersUpdate = now;
        
        // Limit uygula
        return cachedTopPlayers.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // ✅ PERFORMANS: Klan sıralama cache
    private List<ClanRanking> cachedTopClans = new ArrayList<>();
    private long lastTopClansUpdate = 0;
    
    /**
     * Top klanları al
     * ✅ PERFORMANS: Cache kullanarak gereksiz hesaplamaları önler
     */
    public List<ClanRanking> getTopClans(int limit) {
        StratocraftPowerSystem powerSystem = plugin.getStratocraftPowerSystem();
        if (powerSystem == null) return new ArrayList<>();
        
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (now - lastTopClansUpdate < RANKING_CACHE_DURATION && 
            !cachedTopClans.isEmpty() && 
            cachedTopClans.size() >= limit) {
            return cachedTopClans.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }
        
        List<ClanRanking> rankings = new ArrayList<>();
        
        // Tüm klanlar için güç hesapla (cache kullanır)
        for (me.mami.stratocraft.model.Clan clan : plugin.getClanManager().getAllClans()) {
            me.mami.stratocraft.model.ClanPowerProfile profile = powerSystem.calculateClanProfile(clan); // Cache kullanır
            rankings.add(new ClanRanking(
                clan.getName(),
                profile.getTotalClanPower(),
                profile.getClanLevel()
            ));
        }
        
        // Sırala ve cache'e kaydet
        cachedTopClans = rankings.stream()
            .sorted(Comparator.comparing(ClanRanking::getPower).reversed())
            .collect(Collectors.toList());
        lastTopClansUpdate = now;
        
        // Limit uygula
        return cachedTopClans.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Oyuncu sıralama verisi
     */
    public static class PlayerRanking {
        private final String playerName;
        private final double power;
        private final int level;
        
        public PlayerRanking(String playerName, double power, int level) {
            this.playerName = playerName;
            this.power = power;
            this.level = level;
        }
        
        public String getPlayerName() { return playerName; }
        public double getPower() { return power; }
        public int getLevel() { return level; }
    }
    
    /**
     * Klan sıralama verisi
     */
    public static class ClanRanking {
        private final String clanName;
        private final double power;
        private final int level;
        
        public ClanRanking(String clanName, double power, int level) {
            this.clanName = clanName;
            this.power = power;
            this.level = level;
        }
        
        public String getClanName() { return clanName; }
        public double getPower() { return power; }
        public int getLevel() { return level; }
    }
}

