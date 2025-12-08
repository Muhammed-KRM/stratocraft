package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * Buzul Leviathan Handler
 * Donma ve buz dönüşümü yetenekleri
 */
public class IceLeviathanHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    
    public IceLeviathanHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof ElderGuardian)) return;
        
        ElderGuardian leviathan = (ElderGuardian) entity;
        Location current = leviathan.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Temel hareket
        super.handle(disaster, entity, config);
        
        // Donma yeteneği
        if (random.nextInt(100) < config.getFreezeChance()) {
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getFreezeRadius()) {
                    // Oyuncuyu dondur
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, config.getFreezeDuration(), 3, false, false));
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW_DIGGING, config.getFreezeDuration(), 2, false, false));
                    player.damage(3.0 * disaster.getDamageMultiplier(), leviathan);
                }
            }
            
            // Etrafındaki blokları buz yap
            for (int x = -config.getIceConversionRadius(); x <= config.getIceConversionRadius(); x++) {
                for (int z = -config.getIceConversionRadius(); z <= config.getIceConversionRadius(); z++) {
                    for (int y = -2; y <= 2; y++) {
                        Block block = current.clone().add(x, y, z).getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK && 
                            block.getType() != Material.ICE && block.getType() != Material.PACKED_ICE) {
                            if (random.nextInt(100) < config.getIceConversionChance()) {
                                block.setType(Material.ICE);
                            }
                        }
                    }
                }
            }
        }
    }
}
