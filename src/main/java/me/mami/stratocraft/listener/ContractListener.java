package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ContractManager;
import me.mami.stratocraft.model.Contract;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Kontrat sistemi dinleyicisi:
 * - Bounty Hunting: Oyuncu öldürüldüğünde ödül ödenir
 */
public class ContractListener implements Listener {
    private final ContractManager contractManager;
    
    public ContractListener(ContractManager contractManager) {
        this.contractManager = contractManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Eğer kurban bir oyuncu tarafından öldürüldüyse
        if (killer == null) return;
        
        // ContractManager'dan kurbanın başına ödül olup olmadığını sor
        Contract bounty = contractManager.getBountyContract(victim.getUniqueId());
        
        if (bounty != null) {
            // Ödülü öde ve kontratı tamamla
            contractManager.completeBountyContract(bounty, killer.getUniqueId());
        }
    }
}

