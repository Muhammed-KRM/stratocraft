package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Volkanic Patlama Handler
 * Etrafta lavlar oluşur, patlamalar olur, oyuncular yanar
 */
public class VolcanicEruptionHandler extends NaturalDisasterHandler {
    
    public VolcanicEruptionHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Her oyuncu için volkanik etkiler
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            
            // Klan bölgesinde mi kontrol et
            me.mami.stratocraft.model.Clan owner = territoryManager.getTerritoryOwner(playerLoc);
            if (owner != null) {
                continue; // Klan bölgesinde volkanik etki yok
            }
            
            // Config'den volkanik patlama şansı
            if (random.nextInt(100) < config.getVolcanicExplosionChance()) {
                Location explosionLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getVolcanicExplosionRadius() * 2,
                    0,
                    (random.nextDouble() - 0.5) * config.getVolcanicExplosionRadius() * 2
                );
                explosionLoc.setY(playerLoc.getY());
                
                // Config'den patlama gücü
                DisasterUtils.createExplosion(explosionLoc, (float)config.getVolcanicExplosionPower(), true);
                
                // Patlama sonrası lav oluştur
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        Block block = explosionLoc.clone().add(x, -1, z).getBlock();
                        if (block.getType() == Material.AIR || 
                            block.getType() == Material.GRASS_BLOCK ||
                            block.getType() == Material.DIRT ||
                            block.getType() == Material.STONE) {
                            if (random.nextDouble() < 0.3) { // %30 şans
                                block.setType(Material.LAVA);
                            }
                        }
                    }
                }
            }
            
            // Config'den ateş hasar aralığı ve miktarı
            if (random.nextInt(config.getVolcanicFireDamageInterval()) < 1) {
                player.setFireTicks(100); // 5 saniye yanma
                player.damage(config.getVolcanicFireDamageAmount());
            }
            
            // Config'den lav oluşturma şansı ve yarıçapı
            if (random.nextInt(100) < config.getVolcanicLavaSpawnChance()) {
                Location lavaLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getVolcanicLavaSpawnRadius() * 2,
                    0,
                    (random.nextDouble() - 0.5) * config.getVolcanicLavaSpawnRadius() * 2
                );
                int highestY = playerLoc.getWorld().getHighestBlockYAt(lavaLoc);
                lavaLoc.setY(highestY);
                
                Block block = lavaLoc.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.LAVA);
                }
            }
        }
    }
}
