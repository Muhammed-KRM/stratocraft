package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basit Güç Geçmişi Sistemi
 * 
 * Test için basit versiyon - sadece güç değişimlerini loglar
 */
public class SimplePowerHistory {
    private final Main plugin;
    
    // Oyuncu -> Son güç değeri
    private final Map<UUID, Double> lastPower = new ConcurrentHashMap<>();
    
    public SimplePowerHistory(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Güç değişimini logla
     */
    public void logPowerChange(Player player, double newPower) {
        UUID playerId = player.getUniqueId();
        Double oldPower = lastPower.get(playerId);
        
        if (oldPower != null) {
            double change = newPower - oldPower;
            
            // Önemli değişimler için log (100'den fazla değişim)
            if (Math.abs(change) > 100) {
                plugin.getLogger().info(String.format(
                    "Güç Değişimi: %s - Eski: %.2f, Yeni: %.2f, Değişim: %+.2f",
                    player.getName(),
                    oldPower,
                    newPower,
                    change
                ));
            }
        }
        
        lastPower.put(playerId, newPower);
    }
    
    /**
     * Oyuncu çıkışında temizle
     */
    public void onPlayerQuit(UUID playerId) {
        lastPower.remove(playerId);
    }
    
    /**
     * Tüm geçmişi temizle
     */
    public void clear() {
        lastPower.clear();
    }
}

