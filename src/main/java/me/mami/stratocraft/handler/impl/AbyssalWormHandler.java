package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Silverfish;

import java.util.List;

/**
 * Hiçlik Solucanı Handler
 * Yer altından kazarak ilerler, ışınlanabilir
 */
public class AbyssalWormHandler extends BaseCreatureHandler {
    
    public AbyssalWormHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof Silverfish)) return;
        
        Silverfish worm = (Silverfish) entity;
        Location current = worm.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Temel hareket
        super.handle(disaster, entity, config);
        
        // Yer altından kazma (alt blokları kır)
        Block belowBlock = current.clone().add(0, -1, 0).getBlock();
        if (belowBlock.getType() != Material.AIR && belowBlock.getType() != Material.BEDROCK) {
            belowBlock.setType(Material.AIR);
            me.mami.stratocraft.util.EffectUtil.playDisasterEffect(belowBlock.getLocation());
        }
        
        // Önündeki bloğu da kır
        org.bukkit.util.Vector direction = DisasterUtils.calculateDirection(current, target);
        Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            frontBlock.setType(Material.AIR);
            me.mami.stratocraft.util.EffectUtil.playDisasterEffect(frontBlock.getLocation());
        }
        
        // Sıkışma önleme - ışınlanma
        if (worm.getLocation().getBlock().getType() != Material.AIR) {
            Location teleportLoc = DisasterUtils.findSafeLocation(
                current.clone().add(direction.multiply(config.getTeleportDistance())),
                (int) config.getTeleportDistance()
            );
            worm.teleport(teleportLoc);
        }
    }
}
