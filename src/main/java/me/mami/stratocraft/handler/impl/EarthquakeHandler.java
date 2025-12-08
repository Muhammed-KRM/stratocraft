package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Deprem Handler
 */
public class EarthquakeHandler extends NaturalDisasterHandler {
    
    public EarthquakeHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Rastgele konumlarda patlamalar
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            
            // Klan bölgesinde mi kontrol et
            me.mami.stratocraft.model.Clan owner = territoryManager.getTerritoryOwner(playerLoc);
            if (owner != null) {
                continue; // Klan bölgesinde patlama yok
            }
            
            // Rastgele patlama şansı
            if (random.nextInt(100) < config.getExplosionChance()) {
                // Oyuncu etrafında rastgele konum
                Location explosionLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getExplosionRadius(),
                    0,
                    (random.nextDouble() - 0.5) * config.getExplosionRadius()
                );
                explosionLoc.setY(playerLoc.getY());
                
                // Patlama
                DisasterUtils.createExplosion(explosionLoc, (float)config.getEarthquakeExplosionPower(), true);
                
                // Blokları düşür
                for (int x = -config.getBlockFallRadius(); x <= config.getBlockFallRadius(); x++) {
                    for (int z = -config.getBlockFallRadius(); z <= config.getBlockFallRadius(); z++) {
                        Block block = explosionLoc.clone().add(x, config.getBlockFallHeight(), z).getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            FallingBlock falling = playerLoc.getWorld().spawnFallingBlock(
                                block.getLocation(), block.getBlockData());
                            falling.setDropItem(true);
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
            
            // Sürekli hasar
            if (random.nextInt(config.getDamageInterval()) < 1) {
                player.damage(config.getDamageAmount());
                player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, 
                    player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1, Material.STONE.createBlockData());
            }
        }
    }
}
