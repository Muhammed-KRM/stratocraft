package me.mami.stratocraft.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;

/**
 * Özel Boss AI Sistemi
 * 
 * Felaket boss'larının normal AI'sını devre dışı bırakıp,
 * belirli zamanlarda belirli hareketler, saldırılar ve bölge yok etme işlemleri yapar.
 * 
 * Özellikler:
 * - Belirli zamanlarda belirli yönlere gitme
 * - Belirli zamanlarda belirli saldırılar yapma
 * - Belirli bölgeleri yok etme
 * - Özel pathfinding kontrolü
 */
public class CustomBossAI {
    
    /**
     * Boss AI durumu
     */
    public enum AIState {
        MOVING_TO_TARGET,      // Hedefe gidiyor
        ATTACKING,             // Saldırıyor
        DESTROYING_AREA,       // Bölge yok ediyor
        SPECIAL_ABILITY,       // Özel yetenek kullanıyor
        IDLE                  // Bekliyor
    }
    
    /**
     * Saldırı tipi
     */
    public enum AttackType {
        MELEE,                // Yakın mesafe saldırı
        RANGED,               // Uzak mesafe saldırı
        AREA_DAMAGE,          // Alan hasarı
        FIRE_BREATH,          // Ateş püskürtme
        EXPLOSION,            // Patlama
        LIGHTNING,            // Yıldırım
        POISON_CLOUD          // Zehir bulutu
    }
    
    // Entity'ye özel AI durumu
    private static final Map<UUID, AIState> entityStates = new HashMap<>();
    
    // Entity'ye özel zaman takibi
    private static final Map<UUID, Long> lastActionTime = new HashMap<>();
    private static final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private static final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private static final Map<UUID, Long> lastSpecialAbilityTime = new HashMap<>();
    
    // Entity'ye özel hedef takibi
    private static final Map<UUID, Location> currentTarget = new HashMap<>();
    private static final Map<UUID, List<Location>> waypoints = new HashMap<>();
    private static final Map<UUID, Integer> currentWaypointIndex = new HashMap<>();
    
    // Entity'ye özel saldırı planı
    private static final Map<UUID, List<AttackPlan>> attackPlans = new HashMap<>();
    
    /**
     * Saldırı planı (belirli zamanlarda belirli saldırılar)
     */
    public static class AttackPlan {
        private final long triggerTime;      // Ne zaman tetiklenecek (ms)
        private final AttackType attackType; // Saldırı tipi
        private final double damage;         // Hasar miktarı
        private final double range;          // Menzil
        private boolean executed;            // Çalıştırıldı mı?
        
        public AttackPlan(long triggerTime, AttackType attackType, double damage, double range) {
            this.triggerTime = triggerTime;
            this.attackType = attackType;
            this.damage = damage;
            this.range = range;
            this.executed = false;
        }
        
        public long getTriggerTime() { return triggerTime; }
        public AttackType getAttackType() { return attackType; }
        public double getDamage() { return damage; }
        public double getRange() { return range; }
        public boolean isExecuted() { return executed; }
        public void setExecuted(boolean executed) { this.executed = executed; }
    }
    
    /**
     * Boss AI'sını başlat
     * Entity'nin normal AI'sını devre dışı bırak ve özel AI'yı aktif et
     */
    public static void initializeBossAI(Entity entity, Disaster disaster, DisasterConfig config) {
        if (entity == null || !(entity instanceof LivingEntity)) return;
        
        UUID entityId = entity.getUniqueId();
        LivingEntity living = (LivingEntity) entity;
        
        // Normal AI'yı devre dışı bırak
        living.setAI(false);
        
        // Başlangıç durumu
        entityStates.put(entityId, AIState.MOVING_TO_TARGET);
        lastActionTime.put(entityId, System.currentTimeMillis());
        lastAttackTime.put(entityId, 0L);
        lastMoveTime.put(entityId, 0L);
        lastSpecialAbilityTime.put(entityId, 0L);
        
        // Hedef belirle
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target != null) {
            currentTarget.put(entityId, target);
        }
        
        // Waypoint'leri oluştur (hedefe giden yol boyunca)
        if (target != null) {
            createWaypoints(entity, target, config);
        }
        
        // Saldırı planlarını oluştur
        createAttackPlans(entity, disaster, config);
    }
    
    // ✅ PERFORMANS: Tick sayacı (her 2 tick'te bir çalıştır)
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();
    
    /**
     * Boss AI'sını güncelle (her tick'te çağrılmalı)
     * ✅ PERFORMANS OPTİMİZASYONU: Her 2 tick'te bir çalıştır (saniyede 10 kez)
     */
    public static void updateBossAI(Entity entity, Disaster disaster, DisasterConfig config) {
        if (entity == null || !(entity instanceof LivingEntity)) return;
        if (entity.isDead() || !entity.isValid()) {
            cleanupBossAI(entity);
            return;
        }
        
        UUID entityId = entity.getUniqueId();
        
        // ✅ PERFORMANS: Her 2 tick'te bir çalıştır
        int tickCount = tickCounters.getOrDefault(entityId, 0);
        tickCounters.put(entityId, tickCount + 1);
        if (tickCount % 2 != 0) {
            return; // Bu tick'te çalıştırma
        }
        
        Location current = entity.getLocation();
        long now = System.currentTimeMillis();
        
        // AI durumunu kontrol et ve güncelle
        AIState currentState = entityStates.getOrDefault(entityId, AIState.MOVING_TO_TARGET);
        
        // Saldırı planlarını kontrol et (her zaman çalıştır - önemli)
        executeAttackPlans(entity, disaster, config, now);
        
        // Duruma göre hareket et
        switch (currentState) {
            case MOVING_TO_TARGET:
                moveToTarget(entity, disaster, config, current, now);
                break;
            case ATTACKING:
                performAttack(entity, disaster, config, current, now);
                break;
            case DESTROYING_AREA:
                destroyArea(entity, disaster, config, current, now);
                break;
            case SPECIAL_ABILITY:
                performSpecialAbility(entity, disaster, config, current, now);
                break;
            case IDLE:
                // Bekleme durumu - hedef belirle
                determineNextAction(entity, disaster, config, current, now);
                break;
        }
        
        // Durum geçişlerini kontrol et
        checkStateTransitions(entity, disaster, config, current, now);
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private static void moveToTarget(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        Location target = currentTarget.get(entityId);
        
        if (target == null) {
            // Hedef yoksa merkeze git
            Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null && plugin.getDifficultyManager() != null) {
                target = plugin.getDifficultyManager().getCenterLocation();
            }
            if (target == null) {
                target = current.getWorld().getSpawnLocation();
            }
            currentTarget.put(entityId, target);
        }
        
        // Waypoint kontrolü
        List<Location> waypointsList = waypoints.get(entityId);
        if (waypointsList != null && !waypointsList.isEmpty()) {
            int waypointIndex = currentWaypointIndex.getOrDefault(entityId, 0);
            if (waypointIndex < waypointsList.size()) {
                Location waypoint = waypointsList.get(waypointIndex);
                double distanceToWaypoint = current.distance(waypoint);
                
                if (distanceToWaypoint < 5.0) {
                    // Waypoint'e ulaşıldı, bir sonrakine geç
                    currentWaypointIndex.put(entityId, waypointIndex + 1);
                    if (waypointIndex + 1 >= waypointsList.size()) {
                        // Tüm waypoint'ler tamamlandı, hedefe git
                        target = currentTarget.get(entityId);
                    } else {
                        target = waypointsList.get(waypointIndex + 1);
                    }
                } else {
                    target = waypoint;
                }
            }
        }
        
        // Hedefe doğru hareket et
        if (target != null && current.getWorld().equals(target.getWorld())) {
            // ✅ PERFORMANS: Paper API pathfinding her 10 tick'te bir (saniyede 2 kez)
            long lastMove = lastMoveTime.getOrDefault(entityId, 0L);
            boolean shouldUsePathfinding = (now - lastMove) >= 500L; // 500ms = 10 tick
            
            if (shouldUsePathfinding && entity instanceof Mob) {
                Mob mob = (Mob) entity;
                try {
                    // Paper API: removeAllGoals ve moveTo (daha az sıklıkta)
                    org.bukkit.Bukkit.getMobGoals().removeAllGoals(mob);
                    mob.getPathfinder().moveTo(target, config.getMoveSpeed());
                } catch (Exception e) {
                    // Paper API yoksa velocity kullan
                    moveWithVelocity(entity, target, config);
                }
            } else {
                // Mob değilse veya pathfinding zamanı değilse velocity kullan
                moveWithVelocity(entity, target, config);
            }
            
            // Yüz yönlendirme (her zaman yap)
            DisasterBehavior.faceTarget(entity, target);
            
            // Önünde engel varsa blok kır (daha az sıklıkta kontrol et)
            if (shouldUsePathfinding) {
                Vector direction = DisasterUtils.calculateDirection(current, target);
                if (DisasterUtils.hasObstacle(current, direction, 1.0)) {
                    DisasterBehavior.breakBlocksInPath(entity, target, config);
                }
            }
        }
        
        lastMoveTime.put(entityId, now);
    }
    
    /**
     * Velocity ile hareket et (Paper API yoksa)
     */
    private static void moveWithVelocity(Entity entity, Location target, DisasterConfig config) {
        Location current = entity.getLocation();
        if (!current.getWorld().equals(target.getWorld())) return;
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        Vector velocity = direction.multiply(speed);
        
        // EnderDragon gibi uçan entity'ler için Y eksenini de kullan
        if (entity instanceof org.bukkit.entity.EnderDragon) {
            double yComponent = direction.getY() * speed * 0.5;
            velocity.setY(Math.max(0.1, Math.max(0, yComponent)));
        } else {
            velocity.setY(0);
        }
        
        entity.setVelocity(velocity);
    }
    
    /**
     * Saldırı yap
     */
    private static void performAttack(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        long lastAttack = lastAttackTime.getOrDefault(entityId, 0L);
        long attackInterval = (long) (config.getAttackInterval() * 1000); // ms'ye çevir
        
        if (now - lastAttack < attackInterval) {
            return; // Henüz saldırı zamanı değil
        }
        
        // En yakın oyuncuya saldır
        Player nearestPlayer = findNearestPlayer(current, config.getAttackRadius());
        if (nearestPlayer != null) {
            double damage = config.getBaseDamage() * config.getDamageMultiplier() * disaster.getDamageMultiplier();
            nearestPlayer.damage(damage, (LivingEntity) entity);
            
            // Partikül efekti
            DisasterUtils.playEffect(nearestPlayer.getLocation(), org.bukkit.Particle.DAMAGE_INDICATOR, 10);
        }
        
        lastAttackTime.put(entityId, now);
    }
    
    /**
     * Bölge yok et
     */
    private static void destroyArea(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        long lastAction = lastActionTime.getOrDefault(entityId, 0L);
        long destroyInterval = 2000L; // 2 saniyede bir blok yok et
        
        if (now - lastAction < destroyInterval) {
            return;
        }
        
        // Önündeki blokları yok et
        int radius = config.getBlockBreakRadius();
        Vector direction = current.getDirection();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = current.clone().add(direction.multiply(3)).add(x, y, z).getBlock();
                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                        double distance = current.distance(block.getLocation());
                        if (distance <= radius) {
                            block.setType(Material.AIR);
                            EffectUtil.playDisasterEffect(block.getLocation());
                        }
                    }
                }
            }
        }
        
        lastActionTime.put(entityId, now);
    }
    
    /**
     * Özel yetenek kullan
     */
    private static void performSpecialAbility(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        long lastAbility = lastSpecialAbilityTime.getOrDefault(entityId, 0L);
        long abilityCooldown = 10000L; // 10 saniye cooldown
        
        if (now - lastAbility < abilityCooldown) {
            return;
        }
        
        // Felaket tipine göre özel yetenek
        Disaster.Type type = disaster.getType();
        if (type == Disaster.Type.CATASTROPHIC_CHAOS_DRAGON) {
            // Kaos Ejderhası: Ateş püskürtme
            performFireBreath(entity, disaster, config, current);
        }
        
        lastSpecialAbilityTime.put(entityId, now);
    }
    
    /**
     * Ateş püskürtme yeteneği
     */
    private static void performFireBreath(Entity entity, Disaster disaster, DisasterConfig config, Location current) {
        double range = config.getFireBreathRange();
        double damage = config.getFireDamage() * disaster.getDamageMultiplier();
        
        for (Player player : current.getWorld().getPlayers()) {
            Location playerLoc = player.getLocation();
            if (current.distance(playerLoc) <= range) {
                // Ateş partikülü
                playerLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, playerLoc, 20, 1, 1, 1, 0.1);
                
                // Hasar
                player.setFireTicks((int)(100 * disaster.getDamageMultiplier()));
                if (entity instanceof LivingEntity) {
                    player.damage(damage, (LivingEntity) entity);
                }
            }
        }
    }
    
    /**
     * Durum geçişlerini kontrol et
     */
    private static void checkStateTransitions(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        AIState currentState = entityStates.getOrDefault(entityId, AIState.MOVING_TO_TARGET);
        Location target = currentTarget.get(entityId);
        
        // Hedefe ulaşıldı mı?
        if (target != null && current.distance(target) < 10.0) {
            // Hedefe ulaşıldı, saldırı moduna geç
            entityStates.put(entityId, AIState.ATTACKING);
            return;
        }
        
        // Yakında oyuncu var mı?
        Player nearestPlayer = findNearestPlayer(current, config.getAttackRadius());
        if (nearestPlayer != null && currentState == AIState.MOVING_TO_TARGET) {
            // Oyuncu yakında, saldırı moduna geç
            entityStates.put(entityId, AIState.ATTACKING);
            return;
        }
        
        // Özel yetenek zamanı mı?
        long lastAbility = lastSpecialAbilityTime.getOrDefault(entityId, 0L);
        if (now - lastAbility >= 10000L && currentState != AIState.SPECIAL_ABILITY) {
            // Özel yetenek kullan
            entityStates.put(entityId, AIState.SPECIAL_ABILITY);
            return;
        }
    }
    
    /**
     * Sonraki aksiyonu belirle
     */
    private static void determineNextAction(Entity entity, Disaster disaster, DisasterConfig config, Location current, long now) {
        UUID entityId = entity.getUniqueId();
        
        // Hedef belirle
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) {
            Main plugin = Main.getInstance();
            if (plugin != null && plugin.getDifficultyManager() != null) {
                target = plugin.getDifficultyManager().getCenterLocation();
            }
            if (target == null) {
                target = current.getWorld().getSpawnLocation();
            }
        }
        
        currentTarget.put(entityId, target);
        entityStates.put(entityId, AIState.MOVING_TO_TARGET);
    }
    
    /**
     * Saldırı planlarını çalıştır
     */
    private static void executeAttackPlans(Entity entity, Disaster disaster, DisasterConfig config, long now) {
        UUID entityId = entity.getUniqueId();
        List<AttackPlan> plans = attackPlans.get(entityId);
        if (plans == null) return;
        
        Location current = entity.getLocation();
        
        for (AttackPlan plan : plans) {
            if (plan.isExecuted()) continue;
            
            long disasterStartTime = disaster.getStartTime();
            long elapsedTime = now - disasterStartTime;
            
            if (elapsedTime >= plan.getTriggerTime()) {
                // Saldırı zamanı geldi
                executeAttackPlan(entity, disaster, config, plan, current);
            }
        }
    }
    
    /**
     * Saldırı planını çalıştır
     */
    private static void executeAttackPlan(Entity entity, Disaster disaster, DisasterConfig config, AttackPlan plan, Location current) {
        switch (plan.getAttackType()) {
            case FIRE_BREATH:
                performFireBreath(entity, disaster, config, current);
                break;
            case AREA_DAMAGE:
                performAreaDamage(entity, disaster, config, current, plan.getRange(), plan.getDamage());
                break;
            case EXPLOSION:
                performExplosion(entity, disaster, config, current, plan.getDamage());
                break;
            case LIGHTNING:
                performLightning(entity, disaster, config, current, plan.getRange());
                break;
            default:
                break;
        }
        plan.setExecuted(true);
    }
    
    /**
     * Alan hasarı yap
     */
    private static void performAreaDamage(Entity entity, Disaster disaster, DisasterConfig config, Location center, double range, double damage) {
        for (Player player : center.getWorld().getPlayers()) {
            Location playerLoc = player.getLocation();
            if (center.distance(playerLoc) <= range) {
                if (entity instanceof LivingEntity) {
                    player.damage(damage * disaster.getDamageMultiplier(), (LivingEntity) entity);
                }
                DisasterUtils.playEffect(playerLoc, org.bukkit.Particle.DAMAGE_INDICATOR, 10);
            }
        }
    }
    
    /**
     * Patlama yap
     */
    private static void performExplosion(Entity entity, Disaster disaster, DisasterConfig config, Location center, double damage) {
        center.getWorld().createExplosion(center, (float) (damage * 0.1f), false, false);
    }
    
    /**
     * Yıldırım çağır
     */
    private static void performLightning(Entity entity, Disaster disaster, DisasterConfig config, Location center, double range) {
        Player nearestPlayer = findNearestPlayer(center, range);
        if (nearestPlayer != null) {
            Location strikeLoc = nearestPlayer.getLocation();
            center.getWorld().strikeLightning(strikeLoc);
        }
    }
    
    /**
     * En yakın oyuncuyu bul
     */
    private static Player findNearestPlayer(Location center, double range) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : center.getWorld().getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            double distance = center.distance(player.getLocation());
            if (distance <= range && distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    /**
     * Waypoint'leri oluştur
     */
    private static void createWaypoints(Entity entity, Location target, DisasterConfig config) {
        UUID entityId = entity.getUniqueId();
        Location current = entity.getLocation();
        
        List<Location> waypointsList = new ArrayList<>();
        
        // Basit waypoint sistemi: Başlangıç ve hedef arasında 5 nokta
        int waypointCount = 5;
        for (int i = 1; i <= waypointCount; i++) {
            double t = (double) i / (waypointCount + 1);
            Location waypoint = current.clone().add(
                (target.getX() - current.getX()) * t,
                (target.getY() - current.getY()) * t,
                (target.getZ() - current.getZ()) * t
            );
            waypointsList.add(waypoint);
        }
        
        waypoints.put(entityId, waypointsList);
        currentWaypointIndex.put(entityId, 0);
    }
    
    /**
     * Saldırı planlarını oluştur
     */
    private static void createAttackPlans(Entity entity, Disaster disaster, DisasterConfig config) {
        UUID entityId = entity.getUniqueId();
        List<AttackPlan> plans = new ArrayList<>();
        
        // Örnek saldırı planları:
        // - 30 saniye sonra ateş püskürtme
        // - 60 saniye sonra alan hasarı
        // - 90 saniye sonra patlama
        
        plans.add(new AttackPlan(30000L, AttackType.FIRE_BREATH, config.getFireDamage(), config.getFireBreathRange()));
        plans.add(new AttackPlan(60000L, AttackType.AREA_DAMAGE, config.getBaseDamage() * 2, config.getAttackRadius()));
        plans.add(new AttackPlan(90000L, AttackType.EXPLOSION, config.getBaseDamage() * 3, 10.0));
        
        attackPlans.put(entityId, plans);
    }
    
    /**
     * Boss AI'sını temizle
     */
    public static void cleanupBossAI(Entity entity) {
        if (entity == null) return;
        UUID entityId = entity.getUniqueId();
        
        entityStates.remove(entityId);
        lastActionTime.remove(entityId);
        lastAttackTime.remove(entityId);
        lastMoveTime.remove(entityId);
        lastSpecialAbilityTime.remove(entityId);
        currentTarget.remove(entityId);
        waypoints.remove(entityId);
        currentWaypointIndex.remove(entityId);
        attackPlans.remove(entityId);
        tickCounters.remove(entityId); // ✅ PERFORMANS: Tick sayacını da temizle
    }
}
