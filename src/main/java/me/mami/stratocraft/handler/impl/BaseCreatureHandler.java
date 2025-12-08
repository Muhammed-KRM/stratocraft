package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.DisasterBehavior;
import me.mami.stratocraft.util.DisasterEntityAI;
import me.mami.stratocraft.util.DisasterUtils;
import me.mami.stratocraft.util.EffectUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Temel Canavar Felaket Handler
 * Tüm tek boss felaketler için ortak mantık
 */
public class BaseCreatureHandler implements DisasterHandler {
    protected final TerritoryManager territoryManager;
    
    public BaseCreatureHandler(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof LivingEntity)) return;
        
        Location current = entity.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Temel hareket
        DisasterBehavior.moveToTarget(entity, target, config);
        
        // Blok kırma
        DisasterBehavior.breakBlocksInPath(entity, target, config);
        
        // Tektonik Sabitleyici kontrolü
        checkTectonicStabilizer(disaster, entity, current, config);
    }
    
    /**
     * Tektonik Sabitleyici kontrolü (tüm boss felaketler için ortak)
     */
    protected void checkTectonicStabilizer(Disaster disaster, Entity entity, Location current, DisasterConfig config) {
        if (current == null || current.getWorld() == null) return;
        
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        org.bukkit.util.Vector direction = DisasterUtils.calculateDirection(current, target);
        Block frontBlock = current.clone().add(direction).getBlock();
        
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            Clan owner = territoryManager.getTerritoryOwner(frontBlock.getLocation());
            if (owner != null) {
                Structure stabilizer = owner.getStructures().stream()
                    .filter(s -> s.getType() == Structure.Type.TECTONIC_STABILIZER)
                    .findFirst().orElse(null);
                
                if (stabilizer != null && stabilizer.getLocation().distance(frontBlock.getLocation()) <= 50) {
                    if (stabilizer.getLevel() > 0) {
                        stabilizer.consumeFuel();
                        EffectUtil.playDisasterEffect(frontBlock.getLocation());
                        return; // Blok kırılmaz
                    }
                }
            }
            
            // Normal blok kırma
            frontBlock.setType(Material.AIR);
            EffectUtil.playDisasterEffect(frontBlock.getLocation());
        }
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        // Tek boss felaketler için grup yok
    }
}
