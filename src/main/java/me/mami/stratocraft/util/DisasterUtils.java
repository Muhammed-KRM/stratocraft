package me.mami.stratocraft.util;

import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Felaket Yardımcı Fonksiyonları
 * Ortak kullanılan tüm fonksiyonlar burada
 */
public class DisasterUtils {
    
    /**
     * Entity'yi güçlendir (can, hasar, efektler)
     */
    public static void strengthenEntity(Entity entity, DisasterConfig config, double healthMultiplier) {
        if (!(entity instanceof LivingEntity)) return;
        
        LivingEntity living = (LivingEntity) entity;
        
        // Can ayarla
        if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
            double maxHealth = config.getBaseHealth() * config.getHealthMultiplier() * healthMultiplier;
            living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            living.setHealth(maxHealth);
        }
        
        // Hasar artır
        if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            double attackDamage = config.getBaseDamage() * config.getDamageMultiplier();
            living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attackDamage);
        }
        
        // Savunma artır
        if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR) != null) {
            living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).setBaseValue(10.0);
        }
        
        // Hız artır
        if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            double speed = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
            living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed * 1.2);
        }
        
        // Güçlendirme efektleri
        living.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, false, false));
        living.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        living.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
    }
    
    /**
     * Blokları kır (yarıçap içinde)
     */
    public static void breakBlocks(Location center, int radius, DisasterConfig config, boolean respectTerritory) {
        if (center == null || center.getWorld() == null) return;
        
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();
                    
                    // Bedrock ve hava hariç
                    if (type == Material.BEDROCK || type == Material.AIR) continue;
                    
                    // Mesafe kontrolü
                    double distance = center.distance(block.getLocation());
                    if (distance > radius) continue;
                    
                    // Blok kır
                    block.setType(Material.AIR);
                    
                    // Partikül efekti
                    EffectUtil.playDisasterEffect(block.getLocation());
                }
            }
        }
    }
    
    /**
     * Patlama oluştur
     */
    public static void createExplosion(Location loc, double power, boolean breakBlocks) {
        if (loc == null || loc.getWorld() == null) return;
        loc.getWorld().createExplosion(loc, (float) power, false, breakBlocks);
    }
    
    /**
     * Partikül efekti oynat
     */
    public static void playEffect(Location loc, org.bukkit.Particle particle, int count) {
        if (loc == null || loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, 0.1);
    }
    
    /**
     * Mesafe hesapla
     */
    public static double calculateDistance(Location from, Location to) {
        if (from == null || to == null) return Double.MAX_VALUE;
        if (!from.getWorld().equals(to.getWorld())) return Double.MAX_VALUE;
        return from.distance(to);
    }
    
    /**
     * Yön hesapla (normalize edilmiş vektör)
     */
    public static Vector calculateDirection(Location from, Location to) {
        if (from == null || to == null) return new Vector(0, 0, 0);
        if (!from.getWorld().equals(to.getWorld())) return new Vector(0, 0, 0);
        
        Vector direction = to.toVector().subtract(from.toVector());
        if (direction.length() > 0) {
            direction.normalize();
        }
        return direction;
    }
    
    /**
     * Güvenli konum bul (havada kalmayacak)
     */
    public static Location findSafeLocation(Location center, int radius) {
        if (center == null || center.getWorld() == null) return center;
        
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        // Rastgele konum dene
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = centerX + (int) (Math.random() * radius * 2 - radius);
            int z = centerZ + (int) (Math.random() * radius * 2 - radius);
            int y = world.getHighestBlockYAt(x, z) + 1;
            
            Location testLoc = new Location(world, x, y, z);
            Block below = testLoc.clone().add(0, -1, 0).getBlock();
            
            // Altında katı blok varsa güvenli
            if (below.getType().isSolid()) {
                return testLoc;
            }
        }
        
        // Bulunamazsa merkezin yüksek bloğunu kullan
        int y = world.getHighestBlockYAt(centerX, centerZ) + 1;
        return new Location(world, centerX, y, centerZ);
    }
    
    /**
     * Chunk yükle (force load)
     */
    public static void loadChunk(Location loc, boolean force) {
        if (loc == null || loc.getWorld() == null) return;
        
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        org.bukkit.Chunk chunk = loc.getWorld().getChunkAt(chunkX, chunkZ);
        
        if (!chunk.isLoaded()) {
            chunk.load(force);
        }
        
        if (force) {
            chunk.setForceLoaded(true);
        }
    }
    
    /**
     * Entity'yi hedefe yönlendir (basit AI)
     */
    public static void setEntityTarget(Entity entity, Location target, DisasterConfig config) {
        if (entity == null || target == null || !(entity instanceof LivingEntity)) return;
        
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        Vector direction = calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        
        Vector velocity = direction.multiply(speed);
        velocity.setY(0); // Y eksenini sıfırla
        
        entity.setVelocity(velocity);
    }
    
    /**
     * Önünde engel var mı kontrol et
     */
    public static boolean hasObstacle(Location from, Vector direction, double distance) {
        if (from == null || from.getWorld() == null) return false;
        
        Location checkLoc = from.clone().add(direction.multiply(distance));
        Block block = checkLoc.getBlock();
        
        return block.getType() != Material.AIR && block.getType() != Material.BEDROCK;
    }
    
    /**
     * Yüzey yüksekliğini bul
     */
    public static int findSurfaceY(Location loc) {
        if (loc == null || loc.getWorld() == null) return loc != null ? loc.getBlockY() : 64;
        return loc.getWorld().getHighestBlockYAt(loc);
    }
}
