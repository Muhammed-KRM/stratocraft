package me.mami.stratocraft.util;

import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Felaket Davranış Mantığı
 * Hareket, saldırı, blok kırma gibi davranışları yönetir
 */
public class DisasterBehavior {
    
    /**
     * Entity'yi hedefe hareket ettir
     */
    public static void moveToTarget(Entity entity, Location target, DisasterConfig config) {
        if (entity == null || target == null) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        
        Vector velocity = direction.multiply(speed);
        velocity.setY(0); // Y eksenini sıfırla
        
        // Önünde engel var mı kontrol et
        if (DisasterUtils.hasObstacle(current, direction, 1.0)) {
            // Sıkışma önleme
            preventStuck(entity, target, config);
            return;
        }
        
        entity.setVelocity(velocity);
    }
    
    /**
     * Yakındaki oyunculara saldır (tüm yakındaki oyunculara)
     */
    public static void attackPlayers(Entity entity, Location center, DisasterConfig config, double damageMultiplier) {
        if (entity == null || center == null || center.getWorld() == null) return;
        if (!(entity instanceof LivingEntity)) return;
        
        LivingEntity attacker = (LivingEntity) entity;
        double attackRadius = config.getAttackRadius();
        double damage = config.getBaseDamage() * config.getDamageMultiplier() * damageMultiplier;
        
        for (Player player : center.getWorld().getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            Location playerLoc = player.getLocation();
            if (!playerLoc.getWorld().equals(center.getWorld())) continue;
            
            double distance = DisasterUtils.calculateDistance(center, playerLoc);
            if (distance <= attackRadius) {
                // Oyuncuya hasar ver
                player.damage(damage, attacker);
                
                // Partikül efekti
                DisasterUtils.playEffect(playerLoc, org.bukkit.Particle.DAMAGE_INDICATOR, 10);
            }
        }
    }
    
    /**
     * En yakındaki oyuncuya saldır (sadece en yakın oyuncuya)
     */
    public static void attackNearestPlayer(Entity entity, Location center, DisasterConfig config, double damageMultiplier) {
        if (entity == null || center == null || center.getWorld() == null) return;
        if (!(entity instanceof LivingEntity)) return;
        
        LivingEntity attacker = (LivingEntity) entity;
        double attackRadius = config.getAttackRadius();
        
        // En yakın oyuncuyu bul
        Player nearestPlayer = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : center.getWorld().getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            Location playerLoc = player.getLocation();
            if (!playerLoc.getWorld().equals(center.getWorld())) continue;
            
            double distance = DisasterUtils.calculateDistance(center, playerLoc);
            if (distance <= attackRadius && distance < minDistance) {
                minDistance = distance;
                nearestPlayer = player;
            }
        }
        
        // Sadece en yakın oyuncuya saldır
        if (nearestPlayer != null) {
            double damage = config.getBaseDamage() * config.getDamageMultiplier() * damageMultiplier;
            nearestPlayer.damage(damage, attacker);
            
            // Partikül efekti
            DisasterUtils.playEffect(nearestPlayer.getLocation(), org.bukkit.Particle.DAMAGE_INDICATOR, 10);
        }
    }
    
    /**
     * Yol üzerindeki blokları kır
     */
    public static void breakBlocksInPath(Entity entity, Location target, DisasterConfig config) {
        if (entity == null || target == null) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        int radius = config.getBlockBreakRadius();
        
        // Önündeki blokları kır
        Location breakLoc = current.clone().add(direction);
        Block frontBlock = breakLoc.getBlock();
        
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            // Blok kır
            frontBlock.setType(Material.AIR);
            EffectUtil.playDisasterEffect(breakLoc);
            
            // Etrafındaki blokları da kır (yarıçap içinde)
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = breakLoc.clone().add(x, y, z).getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            double distance = breakLoc.distance(block.getLocation());
                            if (distance <= radius) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sıkışma önleme (zıplama veya ışınlanma)
     */
    public static void preventStuck(Entity entity, Location target, DisasterConfig config) {
        if (entity == null || target == null) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        // Zıplama yapabilirse zıpla
        if (config.canJump()) {
            Vector direction = DisasterUtils.calculateDirection(current, target);
            Vector jumpVector = direction.clone().multiply(1.5).setY(config.getJumpHeight());
            entity.setVelocity(jumpVector);
            return;
        }
        
        // Işınlanabilirse ışınlan
        if (config.canTeleport()) {
            Location safeLoc = DisasterUtils.findSafeLocation(
                current.clone().add(DisasterUtils.calculateDirection(current, target).multiply(config.getTeleportDistance())),
                (int) config.getTeleportDistance()
            );
            entity.teleport(safeLoc);
            return;
        }
        
        // Hiçbiri yoksa yukarı zıpla
        entity.setVelocity(new Vector(0, 0.5, 0));
    }
    
    /**
     * Grup hareketi (tüm entity'ler birlikte hareket eder)
     */
    public static void moveGroupToTarget(List<Entity> entities, Location target, DisasterConfig config) {
        if (entities == null || entities.isEmpty() || target == null) return;
        
        // Her entity'yi hedefe yönlendir
        for (Entity entity : entities) {
            if (entity == null || entity.isDead() || !entity.isValid()) continue;
            
            Location current = entity.getLocation();
            if (!current.getWorld().equals(target.getWorld())) continue;
            
            // Entity'yi hedefe hareket ettir
            moveToTarget(entity, target, config);
            
            // Önünde engel varsa blok kır
            Vector direction = DisasterUtils.calculateDirection(current, target);
            if (DisasterUtils.hasObstacle(current, direction, 1.0)) {
                breakBlocksInPath(entity, target, config);
            }
        }
    }
    
    /**
     * Entity'yi hedefe doğru döndür (yüz yönlendirme)
     */
    public static void faceTarget(Entity entity, Location target) {
        if (entity == null || target == null) return;
        if (!(entity instanceof LivingEntity)) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        
        // Yaw ve Pitch hesapla
        double dx = direction.getX();
        double dz = direction.getZ();
        double dy = direction.getY();
        
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = Math.toDegrees(Math.asin(dy));
        
        Location newLoc = current.clone();
        newLoc.setYaw((float) yaw);
        newLoc.setPitch((float) pitch);
        
        entity.teleport(newLoc);
    }
}
