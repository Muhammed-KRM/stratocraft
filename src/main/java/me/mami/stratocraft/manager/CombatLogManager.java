package me.mami.stratocraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CombatLogManager - Savaştan kaçmayı engeller
 */
public class CombatLogManager implements Listener {
    // Oyuncu UUID -> Savaş bitiş zamanı (milisaniye)
    private final Map<UUID, Long> combatPlayers = new HashMap<>();
    private static final long COMBAT_DURATION = 15000L; // 15 saniye

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        // Oyuncu hasar aldığında savaşta işaretle
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            combatPlayers.put(player.getUniqueId(), System.currentTimeMillis() + COMBAT_DURATION);
        }
        
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            combatPlayers.put(attacker.getUniqueId(), System.currentTimeMillis() + COMBAT_DURATION);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Öldüğünde savaştan çıkar
        combatPlayers.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Savaşta mı kontrol et
        if (isInCombat(playerId)) {
            // Savaştan kaçmaya çalışıyor - öldür
            player.setHealth(0);
            Bukkit.broadcastMessage("§4§l" + player.getName() + " savaştan kaçmaya çalıştı ve öldü!");
            combatPlayers.remove(playerId);
        }
    }

    public boolean isInCombat(UUID playerId) {
        Long endTime = combatPlayers.get(playerId);
        if (endTime == null) return false;
        
        if (System.currentTimeMillis() > endTime) {
            // Savaş süresi doldu
            combatPlayers.remove(playerId);
            return false;
        }
        
        return true;
    }

    /**
     * Periyodik olarak süresi dolan savaşları temizle
     */
    public void startCleanupTask(org.bukkit.plugin.Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                combatPlayers.entrySet().removeIf(entry -> currentTime > entry.getValue());
            }
        }.runTaskTimer(plugin, 20L, 20L); // Her saniye temizle
    }
}

