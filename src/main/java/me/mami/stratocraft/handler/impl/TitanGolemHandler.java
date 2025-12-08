package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterBehavior;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

/**
 * Titan Golem Handler
 * BaseCreatureHandler'ı extend eder, özel yetenekler ekler
 */
public class TitanGolemHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    private int tickCounter = 0;
    private int lastJumpTick = 0;
    
    public TitanGolemHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof Giant)) return;
        
        Giant golem = (Giant) entity;
        Location current = golem.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        tickCounter++;
        Vector direction = DisasterUtils.calculateDirection(current, target);
        
        // Zıplama-Patlama Yeteneği (Titan Golem özel)
        int jumpInterval = random.nextInt(config.getJumpIntervalMax() - config.getJumpIntervalMin() + 1) + config.getJumpIntervalMin();
        if (tickCounter - lastJumpTick >= jumpInterval) {
            lastJumpTick = tickCounter;
            
            // Yüksek zıplama
            Vector jumpVector = direction.clone().multiply(1.5).setY(config.getJumpHeight());
            golem.setVelocity(jumpVector);
            
            // Zıplama sonrası patlama (0.8 saniye sonra)
            final Giant finalGolem = golem;
            Bukkit.getScheduler().runTaskLater(
                me.mami.stratocraft.Main.getInstance(),
                () -> {
                    if (finalGolem != null && !finalGolem.isDead() && finalGolem.isValid()) {
                        Location landLoc = finalGolem.getLocation();
                        DisasterUtils.createExplosion(landLoc, config.getExplosionPower(), true);
                        DisasterUtils.breakBlocks(landLoc, config.getBlockBreakRadius(), config, false);
                    }
                },
                16L // 0.8 saniye
            );
        }
        
        // Temel hareket (base handler'dan)
        super.handle(disaster, entity, config);
        
        // Sıkışma kontrolü (Titan Golem özel)
        org.bukkit.block.Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            if (tickCounter % 20 == 0) {
                Vector jumpVector = direction.clone().multiply(1.5).setY(config.getJumpHeight());
                golem.setVelocity(jumpVector);
            }
        }
        
        // Blok Fırlatma Yeteneği (Titan Golem özel)
        if (tickCounter % config.getBlockThrowInterval() == 0) {
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getAttackRadius()) {
                    Location playerLoc = player.getLocation();
                    Vector throwDirection = DisasterUtils.calculateDirection(current, playerLoc);
                    
                    FallingBlock fallingBlock = current.getWorld().spawnFallingBlock(
                        current.clone().add(0, 3, 0),
                        Material.DIRT.createBlockData()
                    );
                    fallingBlock.setVelocity(throwDirection.multiply(1.2).setY(0.5));
                    fallingBlock.setHurtEntities(true);
                    fallingBlock.setDropItem(false);
                }
            }
        }
        
        // Pasif patlama (Titan Golem özel)
        if (tickCounter % config.getExplosionInterval() == 0) {
            DisasterUtils.createExplosion(current, config.getPassiveExplosionPower() * disaster.getDamageMultiplier(), false);
        }
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        // Titan Golem tek boss, grup yok
    }
}
