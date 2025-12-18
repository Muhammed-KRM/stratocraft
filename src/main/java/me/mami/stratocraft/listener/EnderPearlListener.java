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
     * ✅ YENİ: Tüm teleport nedenlerini kontrol et
     * Ender Pearl, Chorus Fruit, Komut ve Plugin teleportlarını engelle
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnderPearlTeleport(PlayerTeleportEvent event) {
        // ✅ YENİ: Sadece ENDER_PEARL değil, tüm teleport nedenlerini kontrol et
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        
        // ✅ YENİ: Sadece oyuncu kaynaklı teleportları kontrol et
        // (ENDER_PEARL, CHORUS_FRUIT, COMMAND, PLUGIN)
        if (cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
            cause != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT &&
            cause != PlayerTeleportEvent.TeleportCause.COMMAND &&
            cause != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return; // Diğer teleport nedenleri (ENDER_PORTAL, NETHER_PORTAL vb.) kontrol edilmez
        }

        Player player = event.getPlayer();
        
        // ✅ YENİ: Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(player)) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        
        // Hedef konum
        Clan targetTerritory = territoryManager.getTerritoryOwner(event.getTo());
        
        // Eğer hedef başkasının klan bölgesiyse engelle
        if (targetTerritory != null && targetTerritory.hasCrystal()) {
            // Kendi klanına ışınlanabilir
            if (playerClan != null && playerClan.equals(targetTerritory)) {
                return; // İzin ver
            }
            
            // Misafir ise izin ver
            if (targetTerritory.isGuest(player.getUniqueId())) {
                return; // İzin ver
            }
            
            // ✅ YENİ: Savaş durumunda düşman klanına ışınlanabilir
            if (playerClan != null && targetTerritory.isAtWarWith(playerClan.getId())) {
                return; // Savaş durumunda ışınlanabilir
            }
            
            // Başkasının klanına ışınlanamaz
            event.setCancelled(true);
            
            // ✅ YENİ: Teleport nedenine göre mesaj
            String message;
            switch (cause) {
                case ENDER_PEARL:
                    message = "§cEnder Pearl ile başkasının klan bölgesine ışınlanamazsın!";
                    break;
                case CHORUS_FRUIT:
                    message = "§cChorus Fruit ile başkasının klan bölgesine ışınlanamazsın!";
                    break;
                case COMMAND:
                case PLUGIN:
                    message = "§cBu komut/plugin ile başkasının klan bölgesine ışınlanamazsın!";
                    break;
                default:
                    message = "§cBaşkasının klan bölgesine ışınlanamazsın!";
            }
            player.sendMessage(message);
        }
    }
}

