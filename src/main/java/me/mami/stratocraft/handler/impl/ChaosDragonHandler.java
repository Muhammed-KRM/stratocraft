package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Khaos Ejderi Handler
 * Ateş püskürtme yeteneği
 */
public class ChaosDragonHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    
    public ChaosDragonHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof EnderDragon)) return;
        
        EnderDragon dragon = (EnderDragon) entity;
        Location current = dragon.getLocation();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) {
            // Hedef yoksa, merkeze git
            Main plugin = Main.getInstance();
            if (plugin != null && plugin.getDifficultyManager() != null) {
                target = plugin.getDifficultyManager().getCenterLocation();
            }
            if (target == null) {
                target = current.getWorld().getSpawnLocation();
            }
            disaster.setTarget(target);
        }
        
        // ✅ HEDEF KONTROLÜ - Eğer hedef kristal varsa, ona yönel
        // Önce kristal kontrolü yap (klanlara saldırması için)
        if (disaster.getTargetCrystal() != null) {
            target = disaster.getTargetCrystal();
            disaster.setTarget(target);
        }
        
        // ✅ DÜZELTME: AI devre dışı olduğu için velocity ile hareket ettir
        // ✅ Her zaman hareket etsin (ilk spawn'da da çalışsın, can kontrolü yok)
        if (target != null && current.getWorld().equals(target.getWorld())) {
            // Velocity ile hareket ettir (EnderDragon için daha etkili)
            org.bukkit.util.Vector direction = me.mami.stratocraft.util.DisasterUtils.calculateDirection(current, target);
            double speed = config.getMoveSpeed();
            // ✅ DÜZELTME: Hızı artır (AI olmadığı için daha hızlı hareket etmeli)
            double adjustedSpeed = Math.max(speed, 0.3); // Minimum 0.3 hız
            org.bukkit.util.Vector velocity = direction.multiply(adjustedSpeed);
            // EnderDragon için Y eksenini de kullan (uçabilir)
            double yComponent = direction.getY() * adjustedSpeed * 0.5;
            velocity.setY(Math.max(0.1, Math.max(0, yComponent))); // Minimum 0.1 Y hızı (uçması için)
            
            // ✅ DÜZELTME: Velocity'yi her tick'te uygula (hareket etmesi için)
            dragon.setVelocity(velocity);
            
            // ✅ DÜZELTME: Yüz yönlendirmeyi her tick'te yap (hedef takibi için)
            me.mami.stratocraft.util.DisasterBehavior.faceTarget(entity, target);
        }
        
        // Temel hareket (BaseCreatureHandler'dan) - Blok kırma ve diğer özellikler
        // ✅ BaseCreatureHandler zaten disaster.getTargetCrystal() kullanıyor, bu yüzden kristal hedefi doğru çalışacak
        super.handle(disaster, entity, config);
        
        // ✅ PERFORMANS OPTİMİZASYONU: Ateş püskürtme yeteneği (partikül sayısı azaltıldı)
        if (random.nextInt(100) < config.getFireBreathChance()) {
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getFireBreathRange()) {
                    Location playerLoc = player.getLocation();
                    // Ateş partikülü (20 -> 10, performans için)
                    playerLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, playerLoc, 10, 1, 1, 1, 0.1);
                    // Hasar
                    player.setFireTicks((int)(100 * disaster.getDamageMultiplier()));
                    player.damage(config.getFireDamage() * disaster.getDamageMultiplier(), dragon);
                }
            }
        }
    }
}
