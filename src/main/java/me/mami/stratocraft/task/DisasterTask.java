package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.util.EffectUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DisasterTask extends BukkitRunnable {
    private final DisasterManager disasterManager;
    private final TerritoryManager territoryManager;

    public DisasterTask(DisasterManager dm, TerritoryManager tm) { 
        this.disasterManager = dm; 
        this.territoryManager = tm;
    }

    @Override
    public void run() {
        Disaster disaster = disasterManager.getActiveDisaster();
        if (disaster == null || disaster.isDead()) return;

        Location current = disaster.getEntity().getLocation();
        
        // Hedefe Yönelme Mantığı (Titan Golem)
        Location target = disaster.getTarget();
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        disaster.getEntity().setVelocity(direction.multiply(0.4));
        
        // Blok Yıkma
        Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            frontBlock.setType(Material.AIR);
            EffectUtil.playDisasterEffect(frontBlock.getLocation());
        }
    }
}

