package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.manager.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Mob Klan Saldırı AI
 * Boss ve özel mobların klan kristallerine saldırmasını yönetir
 */
public class MobClanAttackAI {
    
    // Entity -> AI Task mapping
    private static final Map<LivingEntity, BukkitRunnable> aiTasks = new HashMap<>();
    
    // Entity -> Target Clan mapping
    private static final Map<LivingEntity, Clan> targetClans = new HashMap<>();
    
    /**
     * AI'yı entity'ye ekle
     */
    public static void attachAI(LivingEntity entity, Clan targetClan, Main plugin) {
        if (entity == null || targetClan == null || plugin == null) {
            return;
        }
        
        // Zaten AI var mı?
        if (aiTasks.containsKey(entity)) {
            return;
        }
        
        targetClans.put(entity, targetClan);
        
        // AI task'ı başlat
        BukkitRunnable aiTask = new BukkitRunnable() {
            private int tickCounter = 0;
            private Location lastTargetLocation = null;
            private int stuckCounter = 0;
            
            @Override
            public void run() {
                // Entity hala var mı?
                if (entity == null || !entity.isValid() || entity.isDead()) {
                    detachAI(entity);
                    cancel();
                    return;
                }
                
                // Klan hala var mı?
                Clan clan = targetClans.get(entity);
                if (clan == null || clan.getCrystalLocation() == null) {
                    detachAI(entity);
                    cancel();
                    return;
                }
                
                tickCounter++;
                Location current = entity.getLocation();
                Location crystalLoc = clan.getCrystalLocation();
                
                // ✅ OPTİMİZE: Her 40 tick'te bir (2 saniyede bir) hedef güncelle (performans için)
                if (tickCounter % 40 == 0) {
                    updateTarget(entity, clan, current, crystalLoc);
                }
                
                // Hareket
                moveTowardsTarget(entity, current, crystalLoc);
                
                // Saldırı kontrolü
                double distance = current.distance(crystalLoc);
                if (distance <= 5.0) {
                    attackCrystal(entity, clan, crystalLoc, plugin);
                }
                
                // Takılma kontrolü
                if (lastTargetLocation != null && current.distance(lastTargetLocation) < 1.0) {
                    stuckCounter++;
                    if (stuckCounter > 40) { // 2 saniye takılı kaldı
                        // Zıpla veya farklı yön dene
                        handleStuck(entity, current, crystalLoc);
                        stuckCounter = 0;
                    }
                } else {
                    stuckCounter = 0;
                }
                
                lastTargetLocation = current.clone();
            }
        };
        
        // ✅ OPTİMİZE: Her 2 tick'te bir çalıştır (performans için)
        aiTask.runTaskTimer(plugin, 0L, 2L); // Her 2 tick (0.1 saniye)
        aiTasks.put(entity, aiTask);
        
        plugin.getLogger().info("[MobClanAttackAI] AI eklendi: " + entity.getType() + 
            " -> Klan: " + targetClan.getName());
    }
    
    /**
     * AI'yı entity'den kaldır
     */
    public static void detachAI(LivingEntity entity) {
        if (entity == null) return;
        
        BukkitRunnable task = aiTasks.remove(entity);
        if (task != null) {
            task.cancel();
        }
        
        targetClans.remove(entity);
    }
    
    /**
     * Hedef güncelle (en yakın klan kristali)
     */
    private static void updateTarget(LivingEntity entity, Clan currentClan, Location current, Location crystalLoc) {
        // Eğer kristal yok edildiyse, en yakın klanı bul
        if (crystalLoc == null || currentClan.getCrystalEntity() == null || 
            currentClan.getCrystalEntity().isDead()) {
            
            // En yakın klanı bul (1000 blok yarıçap)
            Main plugin = Main.getInstance();
            if (plugin == null || plugin.getDisasterManager() == null) {
                return;
            }
            
            List<Location> nearbyCrystals = plugin.getDisasterManager().findCrystalsInRadius(current, 1000.0);
            if (!nearbyCrystals.isEmpty()) {
                Location nearestCrystal = nearbyCrystals.get(0);
                Clan nearestClan = plugin.getTerritoryManager().getTerritoryOwner(nearestCrystal);
                if (nearestClan != null) {
                    targetClans.put(entity, nearestClan);
                }
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private static void moveTowardsTarget(LivingEntity entity, Location current, Location target) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            return;
        }
        
        // Yön hesapla
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        
        // Hız ayarla
        double speed = 0.25; // Normal mob hızı
        Vector velocity = direction.multiply(speed);
        
        // Y ekseni kontrolü (uçan moblar için)
        if (entity instanceof org.bukkit.entity.Flying) {
            double yDiff = target.getY() - current.getY();
            velocity.setY(yDiff * 0.1);
        } else {
            // Yerdeki moblar için Y eksenini sıfırla
            velocity.setY(0);
        }
        
        entity.setVelocity(velocity);
        
        // Yüz yönlendirme
        DisasterBehavior.faceTarget(entity, target);
    }
    
    /**
     * Kristale saldır
     */
    private static void attackCrystal(LivingEntity attacker, Clan targetClan, Location crystalLoc, Main plugin) {
        if (attacker == null || targetClan == null || crystalLoc == null || plugin == null) {
            return;
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            return;
        }
        
        // Saldırı tipini belirle
        String mobType = getMobType(attacker);
        int bossLevel = getBossLevel(attacker);
        
        // Hasar uygula
        if (bossLevel > 0) {
            // Boss saldırısı
            CrystalAttackHelper.attackCrystalByBoss(targetClan, crystalLoc, bossLevel, plugin);
        } else {
            // Özel mob saldırısı
            CrystalAttackHelper.attackCrystalBySpecialMob(targetClan, crystalLoc, mobType, plugin);
        }
        
        // Saldırı efekti
        crystalLoc.getWorld().spawnParticle(
            org.bukkit.Particle.DAMAGE_INDICATOR,
            crystalLoc,
            10,
            0.5, 0.5, 0.5, 0.1
        );
    }
    
    /**
     * Takılma durumunu çöz
     */
    private static void handleStuck(LivingEntity entity, Location current, Location target) {
        // Zıpla
        Vector jumpVector = new Vector(0, 0.5, 0);
        entity.setVelocity(jumpVector);
        
        // Rastgele yön dene
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        Vector randomDirection = new Vector(
            Math.cos(angle) * 0.3,
            0,
            Math.sin(angle) * 0.3
        );
        entity.setVelocity(entity.getVelocity().add(randomDirection));
    }
    
    /**
     * Mob tipini al
     */
    private static String getMobType(LivingEntity entity) {
        String customName = entity.getCustomName();
        if (customName == null) {
            return entity.getType().name().toLowerCase();
        }
        
        if (customName.contains("Ork") || customName.contains("Orc")) {
            return "ork";
        } else if (customName.contains("İskelet") || customName.contains("Knight")) {
            return "skeleton_knight";
        } else if (customName.contains("Troll")) {
            return "troll";
        } else if (customName.contains("Goblin")) {
            return "goblin";
        } else if (customName.contains("Kurt") || customName.contains("Werewolf")) {
            return "werewolf";
        }
        
        return entity.getType().name().toLowerCase();
    }
    
    /**
     * Boss seviyesini al
     */
    private static int getBossLevel(LivingEntity entity) {
        String customName = entity.getCustomName();
        if (customName == null) {
            return 0; // Boss değil
        }
        
        // Boss isimlerini kontrol et
        if (customName.contains("Şef") || customName.contains("Chief")) {
            return 2;
        } else if (customName.contains("Kral") || customName.contains("King")) {
            return 3;
        } else if (customName.contains("Goblin King")) {
            return 1;
        }
        
        return 0; // Boss değil
    }
}

