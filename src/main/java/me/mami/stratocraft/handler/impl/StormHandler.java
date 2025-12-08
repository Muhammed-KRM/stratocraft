package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Fırtına Handler
 */
public class StormHandler extends NaturalDisasterHandler {
    
    public StormHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            
            // Klan bölgesinde mi kontrol et
            me.mami.stratocraft.model.Clan owner = territoryManager.getTerritoryOwner(playerLoc);
            if (owner != null) {
                continue; // Klan bölgesinde yıldırım yok
            }
            
            // Oyuncu yaklaştıkça yıldırım düşer
            if (random.nextInt(100) < config.getLightningChanceNearby()) {
                Location lightningLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getLightningRadius(),
                    0,
                    (random.nextDouble() - 0.5) * config.getLightningRadius()
                );
                lightningLoc.setY(playerLoc.getWorld().getHighestBlockYAt(lightningLoc));
                
                playerLoc.getWorld().strikeLightning(lightningLoc);
                
                // Yakındaki oyunculara hasar
                for (Player nearby : playerLoc.getWorld().getPlayers()) {
                    if (nearby.getLocation().distance(lightningLoc) <= config.getLightningDamageRadius()) {
                        nearby.damage(config.getLightningDamage());
                    }
                }
            }
            
            // Rastgele konumlarda yıldırım
            if (random.nextInt(100) < config.getLightningChanceRandom()) {
                Location randomLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * 100,
                    0,
                    (random.nextDouble() - 0.5) * 100
                );
                randomLoc.setY(playerLoc.getWorld().getHighestBlockYAt(randomLoc));
                
                // Klan bölgesinde değilse yıldırım düşür
                me.mami.stratocraft.model.Clan randomOwner = territoryManager.getTerritoryOwner(randomLoc);
                if (randomOwner == null) {
                    playerLoc.getWorld().strikeLightning(randomLoc);
                }
            }
        }
    }
}
