package me.mami.stratocraft.util;

import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Felaket Entity AI Sistemi
 * Grup felaketler için pathfinding ve AI mantığı
 */
public class DisasterEntityAI {
    
    /**
     * Entity'yi hedefe yönlendir (AI ile)
     */
    public static void navigateToTarget(Entity entity, Location target, DisasterConfig config) {
        if (entity == null || target == null) return;
        if (!(entity instanceof LivingEntity)) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        // Basit pathfinding: Direkt hedefe git
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        
        Vector velocity = direction.multiply(speed);
        velocity.setY(0); // Y eksenini sıfırla
        
        // Önünde engel var mı kontrol et
        Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            // Engelleri aş
            avoidObstacles(entity, target, config);
            return;
        }
        
        // Hareket et
        entity.setVelocity(velocity);
        
        // Yüz yönlendirme
        DisasterBehavior.faceTarget(entity, target);
    }
    
    /**
     * Pathfinding (basit) - Engelleri aşarak hedefe git
     */
    public static Location findPath(Location from, Location to, int maxDistance) {
        if (from == null || to == null) return to;
        if (!from.getWorld().equals(to.getWorld())) return to;
        
        // Basit pathfinding: Direkt hedefe git
        // Daha gelişmiş pathfinding için A* algoritması kullanılabilir
        return to;
    }
    
    /**
     * Engelleri aş
     */
    public static void avoidObstacles(Entity entity, Location target, DisasterConfig config) {
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
        
        // Yan taraftan dene
        Vector direction = DisasterUtils.calculateDirection(current, target);
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Vector left = new Vector(direction.getZ(), 0, -direction.getX()).normalize();
        
        // Sağa git
        Location rightLoc = current.clone().add(right);
        Block rightBlock = rightLoc.getBlock();
        if (rightBlock.getType() == Material.AIR) {
            entity.setVelocity(right.multiply(config.getMoveSpeed()));
            return;
        }
        
        // Sola git
        Location leftLoc = current.clone().add(left);
        Block leftBlock = leftLoc.getBlock();
        if (leftBlock.getType() == Material.AIR) {
            entity.setVelocity(left.multiply(config.getMoveSpeed()));
            return;
        }
        
        // Yukarı zıpla
        entity.setVelocity(new Vector(0, 0.5, 0));
    }
    
    /**
     * Grup AI (tüm entity'ler birlikte hareket eder)
     */
    public static void updateGroupAI(List<Entity> entities, Location target, DisasterConfig config) {
        if (entities == null || entities.isEmpty() || target == null) return;
        
        // Her entity'yi hedefe yönlendir
        for (Entity entity : entities) {
            if (entity == null || entity.isDead() || !entity.isValid()) continue;
            
            Location current = entity.getLocation();
            if (!current.getWorld().equals(target.getWorld())) continue;
            
            // AI ile hedefe git
            navigateToTarget(entity, target, config);
            
            // Önünde engel varsa blok kır
            Vector direction = DisasterUtils.calculateDirection(current, target);
            if (DisasterUtils.hasObstacle(current, direction, 1.0)) {
                DisasterBehavior.breakBlocksInPath(entity, target, config);
            }
        }
    }
    
    /**
     * Entity'nin hedefe ulaşıp ulaşmadığını kontrol et
     */
    public static boolean hasReachedTarget(Entity entity, Location target, double proximity) {
        if (entity == null || target == null) return false;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return false;
        
        double distance = DisasterUtils.calculateDistance(current, target);
        return distance <= proximity;
    }
    
    /**
     * Entity'nin hedefe bakıp bakmadığını kontrol et
     */
    public static boolean isFacingTarget(Entity entity, Location target, double angleThreshold) {
        if (entity == null || target == null) return false;
        if (!(entity instanceof LivingEntity)) return false;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return false;
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        Vector entityDirection = current.getDirection();
        
        double angle = Math.toDegrees(Math.acos(direction.dot(entityDirection)));
        return angle <= angleThreshold;
    }
}
