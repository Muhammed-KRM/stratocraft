package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectAddEvent;
import org.bukkit.potion.PotionEffectRemoveEvent;

/**
 * Power System Event Listener
 * 
 * Güç sistemi için event'leri dinler:
 * - PlayerQuitEvent: Offline cache'e kaydetme
 * - PotionEffectAddEvent/RemoveEvent: Buff cache güncelleme
 * - BlockPlaceEvent/BlockBreakEvent: Delta sistemi (ritüel blok tracking)
 */
public class PowerSystemListener implements Listener {
    private final StratocraftPowerSystem powerSystem;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    public PowerSystemListener(StratocraftPowerSystem powerSystem, 
                              ClanManager clanManager,
                              TerritoryManager territoryManager) {
        this.powerSystem = powerSystem;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
    }
    
    /**
     * Oyuncu çıkışında offline cache'e kaydet
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            powerSystem.onPlayerQuit(player);
            
            // ✅ GÜÇ GEÇMİŞİ: Oyuncu çıkışında temizle
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null && plugin.getSimplePowerHistory() != null) {
                plugin.getSimplePowerHistory().onPlayerQuit(player.getUniqueId());
            }
        }
    }
    
    /**
     * PotionEffect eklendiğinde buff cache'i güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectAdd(PotionEffectAddEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            powerSystem.updateBuffPowerCache(player);
        }
    }
    
    /**
     * PotionEffect kaldırıldığında buff cache'i güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectRemove(PotionEffectRemoveEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            powerSystem.updateBuffPowerCache(player);
        }
    }
    
    /**
     * Blok koyulduğunda ritüel blok tracking (Delta sistemi)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material material = block.getType();
        
        // Ritüel blok mu?
        if (!powerSystem.isRitualBlock(material)) return;
        
        // Klan arazisi içinde mi?
        Clan clan = territoryManager != null ? 
            territoryManager.getTerritoryOwner(loc) : null;
        
        if (clan == null) {
            // Oyuncunun klanı var mı? (kendi arazisinde değilse)
            clan = clanManager != null ? 
                clanManager.getClanByPlayer(player.getUniqueId()) : null;
        }
        
        if (clan != null) {
            // Delta sistemi: Blok koyuldu
            powerSystem.onRitualBlockPlace(clan, loc, material);
        }
    }
    
    /**
     * Blok kırıldığında ritüel blok tracking (Delta sistemi)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material material = block.getType();
        
        // Ritüel blok mu?
        if (!powerSystem.isRitualBlock(material)) return;
        
        // Klan arazisi içinde mi?
        Clan clan = territoryManager != null ? 
            territoryManager.getTerritoryOwner(loc) : null;
        
        if (clan == null) {
            // Oyuncunun klanı var mı?
            clan = clanManager != null ? 
                clanManager.getClanByPlayer(player.getUniqueId()) : null;
        }
        
        if (clan != null) {
            // Delta sistemi: Blok kırıldı
            powerSystem.onRitualBlockBreak(clan, loc, material);
        }
    }
}

