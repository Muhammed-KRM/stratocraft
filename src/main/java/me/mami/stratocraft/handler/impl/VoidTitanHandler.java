package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;

import java.util.List;
import java.util.Random;

/**
 * Boşluk Titanı Handler
 * Boşluk patlamaları oluşturur
 */
public class VoidTitanHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    
    public VoidTitanHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof Wither)) return;
        
        Wither wither = (Wither) entity;
        Location current = wither.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Temel hareket
        super.handle(disaster, entity, config);
        
        // Boşluk patlaması yeteneği
        if (random.nextInt(100) < config.getVoidExplosionChance()) {
            Location explosionLoc = current.clone().add(
                (random.nextDouble() - 0.5) * config.getVoidExplosionRadius(),
                0,
                (random.nextDouble() - 0.5) * config.getVoidExplosionRadius()
            );
            DisasterUtils.createExplosion(explosionLoc, config.getVoidExplosionPower() * disaster.getDamageMultiplier(), true);
        }
    }
}
