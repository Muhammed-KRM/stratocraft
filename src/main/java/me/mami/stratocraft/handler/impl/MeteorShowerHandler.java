package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Meteor Yağmuru Handler
 * Gökyüzünden meteorlar düşer, patlamalar oluşur
 */
public class MeteorShowerHandler extends NaturalDisasterHandler {
    
    public MeteorShowerHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Her oyuncu için meteor düşür
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            
            // Klan bölgesinde mi kontrol et
            me.mami.stratocraft.model.Clan owner = territoryManager.getTerritoryOwner(playerLoc);
            if (owner != null) {
                continue; // Klan bölgesinde meteor yok
            }
            
            // Config'den meteor düşürme şansı
            if (random.nextInt(100) < config.getMeteorChance()) {
                // Config'den spawn range ile rastgele konum
                Location meteorLoc = playerLoc.clone().add(
                    (random.nextDouble() - 0.5) * config.getMeteorSpawnRange() * 2,
                    config.getMeteorSpawnHeight() + random.nextDouble() * 30,
                    (random.nextDouble() - 0.5) * config.getMeteorSpawnRange() * 2
                );
                
                // Meteor bloğu (Obsidian veya Magma Block)
                Material meteorMaterial = random.nextBoolean() ? Material.OBSIDIAN : Material.MAGMA_BLOCK;
                
                // Falling block oluştur
                FallingBlock meteor = playerLoc.getWorld().spawnFallingBlock(meteorLoc, meteorMaterial.createBlockData());
                meteor.setHurtEntities(true);
                meteor.setDropItem(false);
                
                // Meteor düştüğünde patlama oluştur
                final Location finalMeteorLoc = meteorLoc;
                final double explosionPower = config.getMeteorExplosionPower();
                final int damageRadius = config.getMeteorDamageRadius();
                me.mami.stratocraft.Main.getInstance().getServer().getScheduler().runTaskLater(
                    me.mami.stratocraft.Main.getInstance(),
                    () -> {
                        Location impactLoc = finalMeteorLoc.clone();
                        impactLoc.setY(playerLoc.getWorld().getHighestBlockYAt(impactLoc));
                        
                        // Config'den patlama gücü
                        DisasterUtils.createExplosion(impactLoc, (float)explosionPower, true);
                        
                        // Config'den hasar yarıçapı ile blokları yok et
                        for (int x = -damageRadius; x <= damageRadius; x++) {
                            for (int z = -damageRadius; z <= damageRadius; z++) {
                                for (int y = -1; y <= 3; y++) {
                                    org.bukkit.block.Block block = impactLoc.clone().add(x, y, z).getBlock();
                                    if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                        block.setType(Material.AIR);
                                    }
                                }
                            }
                        }
                    },
                    60L // 3 saniye sonra (meteor düşme süresi)
                );
            }
        }
    }
}
