package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Random;

/**
 * Mob İstilası Handler
 * Rastgele konumlarda güçlendirilmiş moblar spawn eder
 */
public class MobInvasionHandler extends MiniDisasterHandler {
    private final Random random = new Random();
    
    public MobInvasionHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Rastgele oyuncuların etrafında mob spawn et
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (random.nextInt(100) < 10) { // %10 şans
                Location spawnLoc = player.getLocation().clone().add(
                    (random.nextDouble() - 0.5) * 50,
                    0,
                    (random.nextDouble() - 0.5) * 50
                );
                spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
                
                // Rastgele mob tipi
                EntityType[] mobTypes = {
                    EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                    EntityType.CREEPER, EntityType.ENDERMAN
                };
                EntityType mobType = mobTypes[random.nextInt(mobTypes.length)];
                
                Entity mob = spawnLoc.getWorld().spawnEntity(spawnLoc, mobType);
                if (mob instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) mob;
                    // Güçlendirme
                    living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 6000, 1, false, false));
                    living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 6000, 1, false, false));
                }
            }
        }
    }
}
