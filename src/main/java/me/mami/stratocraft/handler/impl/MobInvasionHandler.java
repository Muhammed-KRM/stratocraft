package me.mami.stratocraft.handler.impl;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;

/**
 * Mob İstilası Handler
 * Rastgele konumlarda güçlendirilmiş moblar spawn eder
 * ✅ PERFORMANS OPTİMİZASYONU: Spawn sıklığı azaltıldı, AI devre dışı, entity sayısı sınırlandı
 */
public class MobInvasionHandler extends MiniDisasterHandler {
    private final Random random = new Random();
    
    // ✅ PERFORMANS: Spawn zamanı takibi (her oyuncu için ayrı)
    private final Map<UUID, Long> lastSpawnTime = new ConcurrentHashMap<>();
    private static final long SPAWN_COOLDOWN = 5000L; // 5 saniye cooldown
    
    // ✅ PERFORMANS: Toplam spawn edilen entity sayısı (felaket başına)
    private final Map<UUID, Integer> spawnedEntityCount = new ConcurrentHashMap<>();
    private static final int MAX_ENTITIES_PER_DISASTER = 100; // Maksimum 100 entity
    
    public MobInvasionHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // ✅ PERFORMANS: Felaket UUID'si al (entity yoksa disaster'dan)
        UUID disasterId = entity != null ? entity.getUniqueId() : UUID.randomUUID();
        
        // ✅ PERFORMANS: Maksimum entity sayısı kontrolü
        int currentCount = spawnedEntityCount.getOrDefault(disasterId, 0);
        if (currentCount >= MAX_ENTITIES_PER_DISASTER) {
            return; // Maksimum sayıya ulaşıldı
        }
        
        // ✅ PERFORMANS: Rastgele oyuncuların etrafında mob spawn et (daha az sıklıkta)
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            long lastSpawn = lastSpawnTime.getOrDefault(playerId, 0L);
            long now = System.currentTimeMillis();
            
            // ✅ PERFORMANS: Cooldown kontrolü ve daha düşük spawn şansı (%10 -> %2)
            if (now - lastSpawn < SPAWN_COOLDOWN) continue;
            if (random.nextInt(100) >= 2) continue; // %2 şans (önceden %10)
            
            Location spawnLoc = player.getLocation().clone().add(
                (random.nextDouble() - 0.5) * 50,
                0,
                (random.nextDouble() - 0.5) * 50
            );
            spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
            
            // Rastgele mob tipi
            EntityType[] mobTypes = {
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.CREEPER, EntityType.ENDERMAN
            };
            EntityType mobType = mobTypes[random.nextInt(mobTypes.length)];
            
            Entity mob = spawnLoc.getWorld().spawnEntity(spawnLoc, mobType);
            if (mob instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) mob;
                
                // ✅ PERFORMANS: AI'yı devre dışı bırak (sadece velocity ile hareket)
                living.setAI(false);
                
                // Güçlendirme
                living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 6000, 1, false, false));
                living.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 6000, 1, false, false));
                
                // ✅ PERFORMANS: Spawn sayısını artır
                spawnedEntityCount.put(disasterId, currentCount + 1);
            }
            
            lastSpawnTime.put(playerId, now);
        }
    }
    
    /**
     * ✅ PERFORMANS: Felaket bittiğinde temizlik
     */
    public void cleanup(Disaster disaster) {
        UUID disasterId = disaster.getEntity() != null ? disaster.getEntity().getUniqueId() : null;
        if (disasterId != null) {
            spawnedEntityCount.remove(disasterId);
        }
    }
}
