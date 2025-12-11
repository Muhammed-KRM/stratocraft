package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.model.Disaster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Felaket hasar takibi için listener
 * Hangi oyuncunun ne kadar hasar verdiğini takip eder
 */
public class DisasterListener implements Listener {
    private final Main plugin;
    private final DisasterManager disasterManager;
    
    public DisasterListener(Main plugin) {
        this.plugin = plugin;
        this.disasterManager = plugin.getDisasterManager();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisasterDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        org.bukkit.entity.Entity target = event.getEntity();
        
        // Aktif felaket var mı?
        Disaster disaster = disasterManager.getActiveDisaster();
        if (disaster == null || disaster.isDead()) return;
        
        // Hedef felaket entity'si mi?
        if (disaster.getCategory() != Disaster.Category.CREATURE) return;
        
        // Tek boss felaketler için
        if (disaster.getEntity() != null && disaster.getEntity().equals(target)) {
            double damage = event.getFinalDamage();
            disaster.addPlayerDamage(player.getUniqueId(), damage);
            return;
        }
        
        // Grup felaketler için
        if (disaster.getGroupEntities() != null && !disaster.getGroupEntities().isEmpty()) {
            for (org.bukkit.entity.Entity groupEntity : disaster.getGroupEntities()) {
                if (groupEntity != null && groupEntity.equals(target)) {
                    double damage = event.getFinalDamage();
                    disaster.addPlayerDamage(player.getUniqueId(), damage);
                    return;
                }
            }
        }
    }
}

