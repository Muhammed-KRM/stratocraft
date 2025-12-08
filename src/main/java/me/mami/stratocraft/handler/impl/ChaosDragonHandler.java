package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * Khaos Ejderi Handler
 * Ateş püskürtme yeteneği
 */
public class ChaosDragonHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    
    public ChaosDragonHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof EnderDragon)) return;
        
        EnderDragon dragon = (EnderDragon) entity;
        Location current = dragon.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Temel hareket
        super.handle(disaster, entity, config);
        
        // Ateş püskürtme yeteneği
        if (random.nextInt(100) < config.getFireBreathChance()) {
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getFireBreathRange()) {
                    Location playerLoc = player.getLocation();
                    // Ateş partikülü
                    playerLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, playerLoc, 20, 1, 1, 1, 0.1);
                    // Hasar
                    player.setFireTicks((int)(100 * disaster.getDamageMultiplier()));
                    player.damage(config.getFireDamage() * disaster.getDamageMultiplier(), dragon);
                }
            }
        }
    }
}
