package me.mami.stratocraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Sunucu Toplam Güç Puanı Hesaplama Sistemi
 * 
 * Tüm oyuncuların güç puanlarını toplar ve ortalama alır
 * Oyuncu sayısına göre çarpan uygular
 * 
 * Performans: Cache kullanımı önerilir (her 5-10 saniyede bir hesapla)
 * 
 * Gelecekte değiştirilebilir: IServerPowerCalculator interface'i implement eder
 */
public class ServerPowerCalculator implements IServerPowerCalculator {
    private final IPowerCalculator playerPowerCalculator;
    private final DisasterPowerConfig config;
    
    // Cache (performans için)
    private double cachedServerPower = 0.0;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5000L; // 5 saniye
    
    public ServerPowerCalculator(IPowerCalculator playerPowerCalculator,
                                 DisasterPowerConfig config) {
        this.playerPowerCalculator = playerPowerCalculator;
        this.config = config;
    }
    
    /**
     * Sunucu toplam güç puanını hesapla (cache ile)
     * 
     * @return Sunucu güç puanı
     */
    public double calculateServerPower() {
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (now - lastCacheUpdate < CACHE_DURATION) {
            return cachedServerPower;
        }
        
        // Cache'i güncelle
        cachedServerPower = calculateServerPowerInternal();
        lastCacheUpdate = now;
        
        return cachedServerPower;
    }
    
    /**
     * Sunucu güç puanını hesapla (cache olmadan)
     * 
     * ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Yeni StratocraftPowerSystem varsa onu kullan
     */
    private double calculateServerPowerInternal() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return 0.0;
        
        // Yeni Stratocraft Güç Sistemi var mı kontrol et
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
            // Yeni sistem kullan (köprü fonksiyon)
            return calculateServerPowerWithNewSystem(plugin.getStratocraftPowerSystem(), players);
        }
        
        // Eski sistem (geriye dönük uyumluluk)
        double totalPower = 0.0;
        int activePlayerCount = 0;
        
        // Tüm oyuncuların güç puanlarını topla
        for (Player player : players) {
            if (player.isOnline() && !player.isDead()) {
                double playerPower = playerPowerCalculator.calculatePlayerPower(player);
                totalPower += playerPower;
                activePlayerCount++;
            }
        }
        
        if (activePlayerCount == 0) return 0.0;
        
        // Ortalama güç
        double averagePower = totalPower / activePlayerCount;
        
        // Oyuncu sayısı çarpanı (config'den)
        // ✅ NULL KONTROLÜ: config null olabilir
        double playerCountMultiplier = 1.0;
        if (config != null) {
            playerCountMultiplier = config.getPlayerCountMultiplier(activePlayerCount);
        }
        
        // Sunucu güç puanı = Ortalama × Oyuncu Sayısı Çarpanı
        return averagePower * playerCountMultiplier;
    }
    
    /**
     * Yeni Stratocraft Güç Sistemi ile sunucu gücü hesapla (köprü fonksiyon)
     */
    private double calculateServerPowerWithNewSystem(
            me.mami.stratocraft.manager.StratocraftPowerSystem powerSystem,
            Collection<? extends Player> players) {
        
        double totalPower = 0.0;
        int activePlayerCount = 0;
        
        // Tüm oyuncuların güç puanlarını topla (yeni sistemden)
        for (Player player : players) {
            if (player.isOnline() && !player.isDead()) {
                me.mami.stratocraft.model.PlayerPowerProfile profile = 
                    powerSystem.calculatePlayerProfile(player);
                // Felaket için combat power önemli (veya total SGP)
                double playerPower = profile.getTotalCombatPower();
                totalPower += playerPower;
                activePlayerCount++;
            }
        }
        
        if (activePlayerCount == 0) return 0.0;
        
        // Ortalama güç
        double averagePower = totalPower / activePlayerCount;
        
        // Oyuncu sayısı çarpanı (config'den)
        // ✅ NULL KONTROLÜ: config null olabilir
        double playerCountMultiplier = 1.0;
        if (config != null) {
            playerCountMultiplier = config.getPlayerCountMultiplier(activePlayerCount);
        }
        
        // Sunucu güç puanı = Ortalama × Oyuncu Sayısı Çarpanı
        return averagePower * playerCountMultiplier;
    }
    
    /**
     * Cache'i temizle (oyuncu giriş/çıkışında çağrılabilir)
     */
    public void clearCache() {
        cachedServerPower = 0.0;
        lastCacheUpdate = 0;
    }
    
    /**
     * Cache süresini al
     */
    public long getCacheDuration() {
        return CACHE_DURATION;
    }
}

