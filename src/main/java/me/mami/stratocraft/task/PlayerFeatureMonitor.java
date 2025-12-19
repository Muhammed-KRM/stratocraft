package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.BuffManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oyuncu Özellik Kontrol Sistemi
 * 
 * Sürekli çalışan task - oyuncu klan üyeliği, buff'lar, partiküller vb. kontrol eder
 * 
 * Özellikler:
 * - Oyuncu klan üyeliği kontrolü
 * - Buff kontrolü ve güncelleme
 * - Cache sistemi (performans için)
 * - Event-based güncelleme
 */
public class PlayerFeatureMonitor {
    private final Main plugin;
    private final ClanManager clanManager;
    private final BuffManager buffManager;
    
    // Cache: Player UUID -> Clan ID
    private final Map<UUID, UUID> playerClanCache = new ConcurrentHashMap<>();
    
    private int taskId = -1;
    private static final long UPDATE_INTERVAL = 200L; // ✅ OPTİMİZE: 10 saniye (200 tick) - BuffTask zaten yapıyor, gereksiz tekrarı azalt
    
    public PlayerFeatureMonitor(Main plugin, ClanManager clanManager, BuffManager buffManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.buffManager = buffManager;
    }
    
    /**
     * Task'ı başlat
     */
    public void start() {
        if (taskId != -1) {
            stop(); // Zaten çalışıyorsa durdur
        }
        
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0L, UPDATE_INTERVAL).getTaskId();
    }
    
    /**
     * Task'ı durdur
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    /**
     * Task çalışma metodu
     * Her saniye çalışır ve oyuncu özelliklerini kontrol eder
     */
    private void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            
            UUID playerId = player.getUniqueId();
            Clan clan = clanManager.getClanByPlayer(playerId);
            
            // Cache güncelle
            UUID cachedClanId = playerClanCache.get(playerId);
            if (clan != null) {
                UUID currentClanId = clan.getId();
                if (!currentClanId.equals(cachedClanId)) {
                    // Klan değişti, cache güncelle
                    playerClanCache.put(playerId, currentClanId);
                    // Buff'ları güncelle
                    updatePlayerBuffs(player, clan);
                }
            } else {
                if (cachedClanId != null) {
                    // Klandan ayrıldı, cache temizle
                    playerClanCache.remove(playerId);
                    // Buff'ları temizle
                    clearPlayerBuffs(player);
                }
            }
            
            // Sürekli kontrol: Buff'lar, partiküller vb.
            if (clan != null) {
                checkPlayerBuffs(player, clan);
                // Diğer kontroller buraya eklenebilir...
            }
        }
    }
    
    /**
     * Oyuncu buff'larını güncelle (klan değiştiğinde)
     */
    private void updatePlayerBuffs(Player player, Clan clan) {
        if (buffManager != null) {
            buffManager.checkBuffsOnJoin(player, clan);
        }
    }
    
    /**
     * Oyuncu buff'larını temizle (klandan ayrıldığında)
     */
    private void clearPlayerBuffs(Player player) {
        // Buff'ları temizle (gerekirse)
        // Not: BuffManager'da otomatik temizleme olabilir
    }
    
    /**
     * Oyuncu buff'larını kontrol et (sürekli)
     */
    private void checkPlayerBuffs(Player player, Clan clan) {
        // Sürekli buff kontrolü
        // Not: BuffManager'da otomatik kontrol olabilir
        // Burada ek kontroller yapılabilir
        // Örnek: Buff süresi doldu mu kontrol et, gerekirse yeniden uygula
    }
    
    /**
     * Cache'den klan ID al (performans için)
     */
    public UUID getCachedClanId(UUID playerId) {
        return playerClanCache.get(playerId);
    }
    
    /**
     * Cache'i temizle (oyuncu çıkış yaptığında)
     */
    public void clearCache(UUID playerId) {
        playerClanCache.remove(playerId);
    }
}

