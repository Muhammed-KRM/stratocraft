package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
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
     * ✅ DÜZELTME: Vanilla AI'yı devre dışı bırak ve kristal hedefini set et
     */
    public static void attachAI(LivingEntity entity, Clan targetClan, Main plugin) {
        if (entity == null || targetClan == null || plugin == null) {
            plugin.getLogger().warning("[MobClanAttackAI] attachAI çağrıldı ama parametreler null: " + 
                "entity=" + (entity != null) + ", targetClan=" + (targetClan != null) + ", plugin=" + (plugin != null));
            return;
        }
        
        plugin.getLogger().info("[MobClanAttackAI] attachAI çağrıldı: " + entity.getType() + 
            " (" + entity.getCustomName() + ") -> Klan: " + targetClan.getName());
        
        // Zaten AI var mı?
        if (aiTasks.containsKey(entity)) {
            plugin.getLogger().info("[MobClanAttackAI] AI zaten mevcut, return");
            return;
        }
        
        Location crystalLoc = targetClan.getCrystalLocation();
        if (crystalLoc == null) {
            plugin.getLogger().warning("[MobClanAttackAI] Klan kristal lokasyonu null: " + targetClan.getName());
            return;
        }
        
        plugin.getLogger().info("[MobClanAttackAI] Klan kristal lokasyonu: " + crystalLoc.toString() + 
            ", Entity lokasyonu: " + entity.getLocation().toString() + 
            ", Mesafe: " + entity.getLocation().distance(crystalLoc));
        
        targetClans.put(entity, targetClan);
        
        // ✅ YENİ: Vanilla AI'yı devre dışı bırak (mob'ların kendi AI'sı ile çakışmasını önle)
        try {
            if (entity instanceof org.bukkit.entity.Mob) {
                org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
                boolean hadAI = mob.hasAI();
                mob.setAI(false); // Vanilla AI'yı devre dışı bırak
                plugin.getLogger().info("[MobClanAttackAI] Vanilla AI devre dışı bırakıldı: " + hadAI + " -> false");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[MobClanAttackAI] AI devre dışı bırakılamadı: " + e.getMessage());
            e.printStackTrace();
        }
        
        // ✅ YENİ: Kristal entity'sini hedef olarak set et (eğer varsa)
        org.bukkit.entity.EnderCrystal crystal = targetClan.getCrystalEntity();
        plugin.getLogger().info("[MobClanAttackAI] Crystal entity: " + (crystal != null ? crystal.getUniqueId() : "null") + 
            ", Dead: " + (crystal != null ? crystal.isDead() : "N/A"));
        
        if (crystal != null && !crystal.isDead() && entity instanceof org.bukkit.entity.Mob) {
            try {
                // EnderCrystal LivingEntity değil, bu yüzden manuel hedefleme yapılacak
                // Metadata ile hedefi işaretle
                entity.setMetadata("crystal_target_clan", new org.bukkit.metadata.FixedMetadataValue(
                    plugin, targetClan.getId().toString()));
                plugin.getLogger().info("[MobClanAttackAI] Metadata eklendi: crystal_target_clan = " + targetClan.getId());
            } catch (Exception e) {
                plugin.getLogger().warning("[MobClanAttackAI] Hedef set edilemedi: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
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
                    plugin.getLogger().warning("[MobClanAttackAI] Klan veya crystal location null, AI kaldırılıyor");
                    detachAI(entity);
                    cancel();
                    return;
                }
                
                tickCounter++;
                Location current = entity.getLocation();
                Location crystalLoc = clan.getCrystalLocation();
                
                if (crystalLoc == null) {
                    plugin.getLogger().warning("[MobClanAttackAI] Crystal location null, AI kaldırılıyor");
                    detachAI(entity);
                    cancel();
                    return;
                }
                
                double distance = current.distance(crystalLoc);
                
                // ✅ DEBUG: Her 100 tick'te bir (5 saniyede bir) log
                if (tickCounter % 100 == 0) {
                    plugin.getLogger().info("[MobClanAttackAI] AI çalışıyor: " + entity.getType() + 
                        " (" + entity.getCustomName() + ") -> Klan: " + clan.getName() + 
                        ", Mesafe: " + String.format("%.2f", distance) + 
                        ", Current: " + current.getBlockX() + "," + current.getBlockY() + "," + current.getBlockZ() + 
                        ", Target: " + crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
                }
                
                // ✅ OPTİMİZE: Her 40 tick'te bir (2 saniyede bir) hedef güncelle (performans için)
                if (tickCounter % 40 == 0) {
                    updateTarget(entity, clan, current, crystalLoc);
                }
                
                // Hareket
                moveTowardsTarget(entity, current, crystalLoc);
                
                // Saldırı kontrolü
                if (distance <= 5.0) {
                    plugin.getLogger().info("[MobClanAttackAI] Saldırı mesafesinde! Mesafe: " + String.format("%.2f", distance));
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
        
        // ✅ OPTİMİZE: Her tick çalıştır (daha responsive hareket için)
        aiTask.runTaskTimer(plugin, 0L, 1L); // Her tick (0.05 saniye)
        aiTasks.put(entity, aiTask);
        
        plugin.getLogger().info("[MobClanAttackAI] AI eklendi: " + entity.getType() + 
            " -> Klan: " + targetClan.getName() + " @ " + targetClan.getCrystalLocation());
    }
    
    /**
     * AI'yı entity'den kaldır
     * ✅ DÜZELTME: Vanilla AI'yı tekrar aktif et
     */
    public static void detachAI(LivingEntity entity) {
        if (entity == null) return;
        
        BukkitRunnable task = aiTasks.remove(entity);
        if (task != null) {
            task.cancel();
        }
        
        targetClans.remove(entity);
        
        // ✅ YENİ: Vanilla AI'yı tekrar aktif et
        try {
            if (entity instanceof org.bukkit.entity.Mob) {
                org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
                mob.setAI(true); // Vanilla AI'yı tekrar aktif et
            }
        } catch (Exception e) {
            // Hata durumunda devam et
        }
        
        // Metadata'yı temizle
        entity.removeMetadata("crystal_target_clan", Main.getInstance());
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
     * ✅ DÜZELTME: Yerdeki moblar için yerçekimi etkisini dikkate al
     */
    private static void moveTowardsTarget(LivingEntity entity, Location current, Location target) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            return;
        }
        
        // Yön hesapla
        Vector direction = target.toVector().subtract(current.toVector());
        double distance = direction.length();
        
        if (distance < 0.1) {
            // Çok yakınsa hareket etme
            return;
        }
        
        direction.normalize();
        
        // Hız ayarla (mesafeye göre ayarla - yakınsa yavaşla)
        double speed = 0.25; // Normal mob hızı
        if (distance < 3.0) {
            speed = 0.15; // Yakınsa yavaşla
        }
        
        Vector velocity = direction.multiply(speed);
        
        // Y ekseni kontrolü (uçan moblar için)
        if (entity instanceof org.bukkit.entity.Flying) {
            double yDiff = target.getY() - current.getY();
            velocity.setY(yDiff * 0.1);
        } else {
            // ✅ DÜZELTME: Yerdeki moblar için - mevcut Y hızını koru (yerçekimi için)
            // Sadece yerdeyse ve önünde engel varsa zıpla
            org.bukkit.block.Block belowBlock = current.clone().add(0, -1, 0).getBlock();
            org.bukkit.block.Block frontBlock = current.clone().add(direction).getBlock();
            
            boolean isOnGround = belowBlock.getType().isSolid();
            boolean hasObstacle = frontBlock.getType().isSolid();
            
            if (isOnGround && hasObstacle) {
                // Zıpla
                velocity.setY(0.4);
            } else if (!isOnGround) {
                // Havadaysa, mevcut Y hızını koru (yerçekimi etkisi)
                Vector currentVel = entity.getVelocity();
                velocity.setY(currentVel.getY());
            } else {
                // Yerde ve engel yok, Y eksenini sıfırla
                velocity.setY(0);
            }
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
            plugin.getLogger().warning("[MobClanAttackAI] attackCrystal parametreler null");
            return;
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            plugin.getLogger().warning("[MobClanAttackAI] Crystal entity null veya dead: " + 
                (crystal == null ? "null" : "dead=" + crystal.isDead()));
            return;
        }
        
        plugin.getLogger().info("[MobClanAttackAI] Kristale saldırı başlatılıyor: " + attacker.getType() + 
            " (" + attacker.getCustomName() + ") -> " + targetClan.getName());
        
        // Saldırı tipini belirle
        String mobType = getMobType(attacker);
        int bossLevel = getBossLevel(attacker);
        
        plugin.getLogger().info("[MobClanAttackAI] Mob type: " + mobType + ", Boss level: " + bossLevel);
        
        // Hasar uygula
        if (bossLevel > 0) {
            // Boss saldırısı
            plugin.getLogger().info("[MobClanAttackAI] Boss saldırısı yapılıyor");
            CrystalAttackHelper.attackCrystalByBoss(targetClan, crystalLoc, bossLevel, plugin);
        } else {
            // Özel mob saldırısı
            plugin.getLogger().info("[MobClanAttackAI] Özel mob saldırısı yapılıyor: " + mobType);
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

