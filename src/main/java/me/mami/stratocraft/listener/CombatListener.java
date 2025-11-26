package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {
    private final ClanManager clanManager;

    public CombatListener(ClanManager cm) {
        this.clanManager = cm;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        // TITAN GOLEM ZAYIF NOKTA KONTROLÜ
        Entity target = event.getEntity();
        if (target instanceof Giant && target.getCustomName() != null && 
            target.getCustomName().contains("TITAN GOLEM")) {
            
            Entity damager = event.getDamager();
            if (damager instanceof Player) {
                Player attacker = (Player) damager;
                
                // Admin bypass kontrolü
                if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(attacker)) {
                    return; // Admin bypass yetkisi varsa korumaları atla
                }
                
                // Golem'in arkasında mı kontrol et
                Vector golemDirection = target.getLocation().getDirection();
                Vector toAttacker = attacker.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                
                // Arka taraf kontrolü: dot product negatifse arkada
                double dot = golemDirection.dot(toAttacker);
                if (dot > -0.5) { // Arkada değilse (0.5 = yaklaşık 120 derece)
                    event.setCancelled(true);
                    attacker.sendMessage("§c§lTitan Golem sadece arkadan hasar alır! Önüne geç!");
                    return;
                }
            }
        }
        
        // OYUNCU VS OYUNCU KONTROLÜ
        if (!(event.getDamager() instanceof Player attacker
                && event.getEntity() instanceof Player defender)) return;

        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(attacker)) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }

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

