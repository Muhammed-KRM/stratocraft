package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.SupplyDropManager;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Hava Drop Listener
 * Düşen sandığın yere düştüğünde sandığı oluşturur
 */
public class SupplyDropListener implements Listener {
    private final SupplyDropManager supplyDropManager;

    public SupplyDropListener(SupplyDropManager supplyDropManager) {
        this.supplyDropManager = supplyDropManager;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
    public void onSupplyDropLand(EntityChangeBlockEvent event) {
        if (event == null || event.getEntity() == null)
            return;
        if (!(event.getEntity() instanceof FallingBlock))
            return;
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();

        if (!fallingBlock.hasMetadata("SupplyDrop"))
            return;

        // SupplyDropManager'a bildir
        supplyDropManager.onSupplyDropLand(event);

        // Event'i iptal ETME, doğal olarak blok oluşsun
        // event.setCancelled(true);
    }
}
