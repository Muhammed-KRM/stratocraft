package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EnderPearlListener implements Listener {
    private final TerritoryManager territoryManager;

    public EnderPearlListener(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    /**
     * Ender Pearl ile başkasının klanına ışınlanmayı engelle
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnderPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = event.getPlayer();
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        
        // Hedef konum
        Clan targetTerritory = territoryManager.getTerritoryOwner(event.getTo());
        
        // Eğer hedef başkasının klan bölgesiyse engelle
        if (targetTerritory != null) {
            // Kendi klanına ışınlanabilir
            if (playerClan != null && playerClan.equals(targetTerritory)) {
                return; // İzin ver
            }
            
            // Misafir ise izin ver
            if (targetTerritory.isGuest(player.getUniqueId())) {
                return; // İzin ver
            }
            
            // Başkasının klanına ışınlanamaz
            event.setCancelled(true);
            player.sendMessage("§cEnder Pearl ile başkasının klan bölgesine ışınlanamazsın!");
        }
    }
}

