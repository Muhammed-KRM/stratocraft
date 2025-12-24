package me.mami.stratocraft.entity;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.util.CrystalAttackHelper;

/**
 * Vahşi Creeper
 * Klan sınırlarına saldıran özel creeper
 * - 3 kat daha güçlü patlama
 * - Zıplayabilir (hendeklerden geçebilir)
 * - Klan sınırına 3 blok yaklaştığında patlar
 * - Oyunculara da tepki verir
 */
public class WildCreeper {
    
    private static final double EXPLOSION_POWER_MULTIPLIER = 3.0; // 3 kat daha güçlü
    private static final double BOUNDARY_DETECTION_RADIUS = 3.0; // 3 blok yaklaşınca patla
    private static final double PLAYER_DETECTION_RADIUS = 10.0; // 10 blok yakında oyuncu varsa tepki ver
    
    /**
     * Vahşi Creeper spawn et ve AI ekle
     */
    public static void spawnWildCreeper(Location loc, Clan targetClan, Main plugin) {
        if (loc == null || loc.getWorld() == null || targetClan == null || plugin == null) {
            return;
        }
        
        // Creeper spawn et
        Creeper creeper = (Creeper) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.CREEPER);
        creeper.setCustomName("§c§lVahşi Creeper");
        creeper.setCustomNameVisible(true);
        
        // Özellikler
        creeper.setPowered(false); // Charged değil, ama patlaması güçlü
        creeper.setMaxFuseTicks(30); // Hızlı patlama (normal 30)
        
        // AI ekle
        attachAI(creeper, targetClan, plugin);
        
        plugin.getLogger().info("[WildCreeper] Vahşi Creeper spawn edildi: " + 
            loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + 
            " -> Klan: " + targetClan.getName());
    }
    
    /**
     * AI ekle
     */
    private static void attachAI(Creeper creeper, Clan targetClan, Main plugin) {
        BukkitRunnable aiTask = new BukkitRunnable() {
            private int tickCounter = 0;
            private Location lastLocation = null;
            private int stuckCounter = 0;
            private boolean isExploding = false;
            
            @Override
            public void run() {
                // Creeper hala var mı?
                if (creeper == null || !creeper.isValid() || creeper.isDead()) {
                    cancel();
                    return;
                }
                
                // Klan hala var mı?
                if (targetClan == null || targetClan.getCrystalLocation() == null) {
                    // En yakın klanı bul
                    Clan nearestClan = findNearestClan(creeper.getLocation(), plugin);
                    if (nearestClan == null) {
                        cancel();
                        return;
                    }
                    // ✅ DÜZELTME: Target değişti, yeni task başlat (eski task zaten cancel edilecek)
                    // Bu task'ı cancel et, yeni task başlat
                    cancel();
                    attachAI(creeper, nearestClan, plugin);
                    return;
                }
                
                tickCounter++;
                Location current = creeper.getLocation();
                Location crystalLoc = targetClan.getCrystalLocation();
                
                // Patlama kontrolü
                if (isExploding) {
                    return; // Zaten patlıyor
                }
                
                // 1. Klan sınırı kontrolü (3 blok yaklaşınca patla)
                if (isNearClanBoundary(current, targetClan, plugin)) {
                    explodeAtBoundary(creeper, targetClan, current, plugin);
                    isExploding = true;
                    cancel();
                    return;
                }
                
                // 2. Oyuncu kontrolü (10 blok yakında oyuncu varsa tepki ver)
                Player nearbyPlayer = findNearbyPlayer(current, PLAYER_DETECTION_RADIUS);
                if (nearbyPlayer != null) {
                    // Oyuncuya doğru git
                    moveTowardsPlayer(creeper, current, nearbyPlayer.getLocation());
                } else {
                    // Kristale doğru git
                    moveTowardsCrystal(creeper, current, crystalLoc);
                }
                
                // 3. Takılma kontrolü
                if (lastLocation != null && current.distance(lastLocation) < 0.5) {
                    stuckCounter++;
                    if (stuckCounter > 20) { // 1 saniye takılı kaldı
                        handleStuck(creeper, current, crystalLoc);
                        stuckCounter = 0;
                    }
                } else {
                    stuckCounter = 0;
                }
                
                lastLocation = current.clone();
            }
        };
        
        aiTask.runTaskTimer(plugin, 0L, 1L); // Her tick
    }
    
    /**
     * Klan sınırına yakın mı? (3 blok)
     */
    private static boolean isNearClanBoundary(Location loc, Clan clan, Main plugin) {
        if (clan == null || plugin == null) return false;
        
        TerritoryManager territoryManager = plugin.getTerritoryManager();
        if (territoryManager == null) return false;
        
        // Klan sınırını kontrol et
        me.mami.stratocraft.model.Territory territory = clan.getTerritory();
        if (territory == null) return false;
        
        Location center = territory.getCenter();
        double radius = territory.getRadius();
        double distance = center.distance(loc);
        
        // ✅ DÜZELTME: Sınırın 3 blok yakınında mı? (sınırın dışında veya sınırda)
        // Sınır: radius, yakınlık: 3 blok
        // distance >= (radius - 3) && distance <= (radius + 3)
        return distance >= (radius - BOUNDARY_DETECTION_RADIUS) && distance <= (radius + BOUNDARY_DETECTION_RADIUS);
    }
    
    /**
     * Sınırda patla
     */
    private static void explodeAtBoundary(Creeper creeper, Clan targetClan, Location loc, Main plugin) {
        if (creeper == null || creeper.isDead()) return;
        
        plugin.getLogger().info("[WildCreeper] Vahşi Creeper patladı: " + 
            loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + 
            " -> Klan: " + targetClan.getName());
        
        // Patlama efekti (3 kat güçlü)
        float explosionPower = (float) (3.0 * EXPLOSION_POWER_MULTIPLIER); // Normal creeper: 3.0
        
        // Patlama oluştur
        loc.getWorld().createExplosion(loc, explosionPower, true, true, creeper);
        
        // Kristale hasar ver (eğer yakınsa)
        Location crystalLoc = targetClan.getCrystalLocation();
        if (crystalLoc != null && loc.distance(crystalLoc) <= 10.0) {
            CrystalAttackHelper.attackCrystalByWildCreeper(targetClan, crystalLoc, plugin);
        }
        
        // Creeper'ı yok et
        creeper.remove();
    }
    
    /**
     * Kristale doğru hareket et
     * ✅ DÜZELTME: Zıplama mantığı düzeltildi - sürekli uçma sorunu çözüldü
     */
    private static void moveTowardsCrystal(Creeper creeper, Location current, Location target) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            return;
        }
        
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        double speed = 0.25;
        Vector velocity = direction.multiply(speed);
        
        // ✅ DÜZELTME: Zıplama kontrolü - sadece gerektiğinde zıpla
        Block frontBlock = current.clone().add(direction).getBlock();
        Block belowBlock = current.clone().add(0, -1, 0).getBlock();
        
        // Yerde mi kontrol et
        boolean isOnGround = belowBlock.getType().isSolid();
        
        // Önünde engel var mı?
        boolean hasObstacle = frontBlock.getType().isSolid();
        
        // ✅ DÜZELTME: Sadece yerdeyse ve önünde engel varsa zıpla
        // Y eksenini sıfırla (yerçekimi etkisini dikkate al)
        if (isOnGround && hasObstacle) {
            // Zıpla (sadece bir kez, yerçekimi düşürecek)
            velocity.setY(0.4); // 0.5 yerine 0.4 (daha kontrollü)
        } else if (!isOnGround) {
            // Havadaysa, yerçekimi etkisini koru (Y eksenini değiştirme)
            // Mevcut velocity'yi koru, sadece yatay hızı ayarla
            Vector currentVel = creeper.getVelocity();
            velocity.setY(currentVel.getY()); // Mevcut Y hızını koru
        } else {
            // Yerde ve engel yok, Y eksenini sıfırla
            velocity.setY(0);
        }
        
        creeper.setVelocity(velocity);
    }
    
    /**
     * Oyuncuya doğru hareket et
     * ✅ DÜZELTME: Zıplama mantığı düzeltildi - sürekli uçma sorunu çözüldü
     */
    private static void moveTowardsPlayer(Creeper creeper, Location current, Location target) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            return;
        }
        
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        double speed = 0.3; // Oyuncuya doğru biraz daha hızlı
        Vector velocity = direction.multiply(speed);
        
        // ✅ DÜZELTME: Zıplama kontrolü - sadece gerektiğinde zıpla
        Block frontBlock = current.clone().add(direction).getBlock();
        Block belowBlock = current.clone().add(0, -1, 0).getBlock();
        
        boolean isOnGround = belowBlock.getType().isSolid();
        boolean hasObstacle = frontBlock.getType().isSolid();
        
        if (isOnGround && hasObstacle) {
            // Zıpla (sadece bir kez)
            velocity.setY(0.4);
        } else if (!isOnGround) {
            // Havadaysa, mevcut Y hızını koru
            Vector currentVel = creeper.getVelocity();
            velocity.setY(currentVel.getY());
        } else {
            // Yerde ve engel yok
            velocity.setY(0);
        }
        
        creeper.setVelocity(velocity);
    }
    
    /**
     * Takılma durumunu çöz
     * ✅ DÜZELTME: Zıplama mantığı düzeltildi - tek seferlik zıplama
     */
    private static void handleStuck(Creeper creeper, Location current, Location target) {
        // ✅ DÜZELTME: Tek seferlik zıplama (yerçekimi düşürecek)
        Vector jumpVector = new Vector(0, 0.6, 0); // 0.8 yerine 0.6 (daha kontrollü)
        creeper.setVelocity(jumpVector);
        
        // Rastgele yön dene (yatay)
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        Vector randomDirection = new Vector(
            Math.cos(angle) * 0.3,
            0, // Y eksenini 0 yap (sadece yatay hareket)
            Math.sin(angle) * 0.3
        );
        // Zıplama ve yatay hareketi birleştir
        creeper.setVelocity(jumpVector.add(randomDirection));
    }
    
    /**
     * Yakındaki oyuncuyu bul
     */
    private static Player findNearbyPlayer(Location loc, double radius) {
        if (loc == null || loc.getWorld() == null) return null;
        
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            double distance = loc.distance(player.getLocation());
            if (distance <= radius && distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    /**
     * En yakın klanı bul
     */
    private static Clan findNearestClan(Location loc, Main plugin) {
        if (loc == null || plugin == null || plugin.getDisasterManager() == null) {
            return null;
        }
        
        List<Location> nearbyCrystals = plugin.getDisasterManager().findCrystalsInRadius(loc, 1000.0);
        if (nearbyCrystals.isEmpty()) {
            return null;
        }
        
        Location nearestCrystal = nearbyCrystals.get(0);
        TerritoryManager territoryManager = plugin.getTerritoryManager();
        if (territoryManager == null) {
            return null;
        }
        
        return territoryManager.getTerritoryOwner(nearestCrystal);
    }
}

