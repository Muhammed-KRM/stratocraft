package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Güneş Patlaması Handler
 */
public class SolarFlareHandler extends NaturalDisasterHandler {
    
    public SolarFlareHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Yüzeydeki oyuncuları yak, ahşap yapılar ve ormanlar tutuşur
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location playerLoc = p.getLocation();
            int highestY = p.getWorld().getHighestBlockYAt(playerLoc);
            
            // Klan bölgesinde mi kontrol et
            me.mami.stratocraft.model.Clan currentOwner = territoryManager.getTerritoryOwner(playerLoc);
            if (currentOwner != null) {
                continue; // Klan bölgesinde yakma
            }
            
            // Oyuncu yüzeydeyse (üstünde blok yoksa)
            if (playerLoc.getBlockY() >= highestY - 1) {
                p.setFireTicks(Math.max(p.getFireTicks(), config.getFireTickDuration()));
            }
            
            // Geniş alan tarama
            int scanRadius = config.getScanRadius();
            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    for (int y = -2; y <= 5; y++) {
                        Block targetBlock = playerLoc.clone().add(x, y, z).getBlock();
                        Material type = targetBlock.getType();
                        
                        // Klan bölgesi kontrolü
                        me.mami.stratocraft.model.Clan owner = territoryManager.getTerritoryOwner(targetBlock.getLocation());
                        if (owner != null) {
                            continue;
                        }
                        
                        // Gökyüzünü gören bloklar mı kontrol et
                        int blockHighestY = targetBlock.getWorld().getHighestBlockYAt(targetBlock.getLocation());
                        boolean canSeeSky = targetBlock.getY() >= blockHighestY - 1;
                        if (!canSeeSky) continue;
                        
                        // Yanıcı blokları yak
                        boolean isFlammable = 
                            type == Material.OAK_PLANKS || type == Material.BIRCH_PLANKS || 
                            type == Material.SPRUCE_PLANKS || type == Material.JUNGLE_PLANKS ||
                            type == Material.ACACIA_PLANKS || type == Material.DARK_OAK_PLANKS ||
                            type == Material.OAK_LOG || type == Material.BIRCH_LOG ||
                            type == Material.SPRUCE_LOG || type == Material.JUNGLE_LOG ||
                            type == Material.ACACIA_LOG || type == Material.DARK_OAK_LOG ||
                            type == Material.WHITE_WOOL || type == Material.BLACK_WOOL ||
                            type == Material.RED_WOOL || type == Material.BLUE_WOOL ||
                            type == Material.OAK_LEAVES || type == Material.BIRCH_LEAVES ||
                            type == Material.SPRUCE_LEAVES || type == Material.JUNGLE_LEAVES ||
                            type == Material.BOOKSHELF || type == Material.CHEST ||
                            type == Material.TRAPPED_CHEST || type == Material.LECTERN;
                        
                        if (isFlammable) {
                            double chance = (type.toString().contains("LOG")) ? config.getFlammableChanceLog() : config.getFlammableChanceOther();
                            if (Math.random() < chance) {
                                targetBlock.setType(Material.FIRE);
                            }
                        }
                        
                        // Lav oluştur
                        if (Math.random() < config.getLavaSpawnChance()) {
                            Block belowBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.DOWN);
                            if (belowBlock.getType() == Material.AIR || 
                                belowBlock.getType() == Material.GRASS_BLOCK ||
                                belowBlock.getType() == Material.DIRT) {
                                belowBlock.setType(Material.LAVA);
                            }
                        }
                    }
                }
            }
        }
    }
}
