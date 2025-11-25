package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ScavengerManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;

public class ScavengerListener implements Listener {
    private final ScavengerManager scavengerManager;

    public ScavengerListener(ScavengerManager sm) { this.scavengerManager = sm; }

    @EventHandler
    public void onWreckageBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            if (scavengerManager.isWreckage(event.getBlock().getLocation())) {
                if (event.getPlayer() instanceof Player) {
                    scavengerManager.scavenge((Player) event.getPlayer(), event.getBlock().getLocation());
                }
            }
        }
    }
}

