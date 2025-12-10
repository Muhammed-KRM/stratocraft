package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.AllianceManager;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {
    private final ClanManager clanManager;
    private AllianceManager allianceManager;

    public CombatListener(ClanManager cm) {
        this.clanManager = cm;
    }
    
    public void setAllianceManager(AllianceManager am) {
        this.allianceManager = am;
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

        // ========== YENİ KLAN KORUMA SİSTEMİ (Öncelikli) ==========
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getClanProtectionSystem() != null) {
            me.mami.stratocraft.manager.clan.ClanProtectionSystem protectionSystem = 
                plugin.getClanProtectionSystem();
            
            // Yeni koruma sistemi kontrolü (güç + seviye + aktivite)
            if (!protectionSystem.canAttackPlayer(attacker, defender)) {
                event.setCancelled(true);
                // Mesaj zaten canAttackPlayer içinde gönderildi
                return;
            }
            
            // Hasar azaltma hesapla (sadece saldırı yapılabilir durumda)
            // Not: canAttackPlayer true döndüyse, güç farkına göre hasar azaltma yapılabilir
            double damageReduction = protectionSystem.calculateDamageReduction(attacker, defender);
            if (damageReduction < 1.0 && damageReduction > 0) {
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * damageReduction;
                // Minimum hasar kontrolü (0.5'ten az olamaz)
                event.setDamage(Math.max(0.5, reducedDamage));
            }
        } else {
            // Fallback: Eski güç sistemi (geriye dönük uyumluluk)
            if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
                me.mami.stratocraft.manager.StratocraftPowerSystem powerSystem = 
                    plugin.getStratocraftPowerSystem();
                
                // Güç sistemi koruma kontrolü (histerezis ile exploit önleme)
                if (!powerSystem.canAttackPlayer(attacker, defender)) {
                    event.setCancelled(true);
                    // Mesaj zaten canAttackPlayer içinde gönderildi
                    return;
                }
            }
        }

        Clan clanA = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan clanD = clanManager.getClanByPlayer(defender.getUniqueId());

        if (clanA == null || clanD == null || clanA.equals(clanD)) return;

        // İttifak kontrolü: İttifaklı klanlar birbirine vuramaz (ama öldürmek ittifakı bozar)
        if (allianceManager != null && allianceManager.hasAlliance(clanA.getId(), clanD.getId())) {
            event.setCancelled(true);
            attacker.sendMessage("§cİttifak üyesine saldıramazsın!");
            return;
        }

        int techA = clanA.getTechLevel();
        int techD = clanD.getTechLevel();

        if (techA >= techD + 2) {
            event.setCancelled(true);
            attacker.sendMessage("§4Kural İhlali! §cTeknoloji farkı çok yüksek olduğu için bu klana saldıramazsın. (Fark: " + (techA - techD) + ")");
        }
    }
    
    /**
     * Oyuncu öldüğünde ittifak kontrolü: Öldürmek ittifakı bozar
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (allianceManager == null) return;
        
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) return;
        
        Clan killerClan = clanManager.getClanByPlayer(killer.getUniqueId());
        Clan victimClan = clanManager.getClanByPlayer(victim.getUniqueId());
        
        if (killerClan == null || victimClan == null || killerClan.equals(victimClan)) return;
        
        // İttifak var mı kontrol et
        if (allianceManager.hasAlliance(killerClan.getId(), victimClan.getId())) {
            // İttifakı boz (ceza uygulanır)
            java.util.List<me.mami.stratocraft.model.Alliance> alliances = allianceManager.getAlliances(killerClan.getId());
            for (me.mami.stratocraft.model.Alliance alliance : alliances) {
                if (alliance.involvesClan(victimClan.getId()) && alliance.isActive()) {
                    allianceManager.breakAlliance(alliance.getId(), killerClan.getId());
                    break;
                }
            }
        }
    }
}

