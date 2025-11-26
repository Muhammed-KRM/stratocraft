package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.LogisticsManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class LogisticsListener implements Listener {
    private final LogisticsManager logisticsManager;

    public LogisticsListener(LogisticsManager lm) { this.logisticsManager = lm; }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
    public void onRailInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.POWERED_RAIL) {
                logisticsManager.transportItems(event.getPlayer(), event.getClickedBlock().getLocation());
            }
        }
    }
}

