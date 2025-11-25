package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.task.SiegeTimer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SiegeListener implements Listener {
    private final SiegeManager siegeManager;
    private final TerritoryManager territoryManager;

    public SiegeListener(SiegeManager sm, TerritoryManager tm) {
        this.siegeManager = sm;
        this.territoryManager = tm;
    }

    @EventHandler
    public void onSiegeAnitPlace(BlockPlaceEvent event) {
        // Kuşatma Anıtı, DOKUMANDAN alınan örneğe göre End Crystal olarak varsayalım.
        if (event.getBlock().getType() != Material.END_CRYSTAL) return;

        Clan attacker = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        if (attacker == null) return;

        // Düşman bölgesine yakın mı? (Basitlik için 10 blok uzağı kontrol edelim)
        Clan defender = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
        
        if (defender != null && !defender.equals(attacker)) {
            if (!siegeManager.isUnderSiege(defender)) {
                
                siegeManager.startSiege(attacker, defender);
                new SiegeTimer(defender).runTaskTimer(me.mami.stratocraft.Main.getInstance(), 20L, 20L); // 1 saniye = 20 tick
                event.getPlayer().sendMessage("§6Kuşatma İlan Edildi! Hazırlık süresi başladı.");
            } else {
                event.getPlayer().sendMessage("§eBu klan zaten kuşatma altında.");
            }
        } else {
            event.getPlayer().sendMessage("§cKuşatma Anıtı düşman bölgesinin yakınında olmalı!");
            event.setCancelled(true);
        }
    }
}

