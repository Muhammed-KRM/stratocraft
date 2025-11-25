package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final ClanManager clanManager;

    public CombatListener(ClanManager cm) {
        this.clanManager = cm;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker
                && event.getEntity() instanceof Player defender)) return;

        Clan clanA = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan clanD = clanManager.getClanByPlayer(defender.getUniqueId());

        if (clanA == null || clanD == null || clanA.equals(clanD)) return;

        int techA = clanA.getTechLevel();
        int techD = clanD.getTechLevel();

        if (techA >= techD + 2) {
            event.setCancelled(true);
            attacker.sendMessage("§4Kural İhlali! §cTeknoloji farkı çok yüksek olduğu için bu klana saldıramazsın. (Fark: " + (techA - techD) + ")");
        }
    }
}

