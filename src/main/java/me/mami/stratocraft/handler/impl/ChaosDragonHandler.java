package me.mami.stratocraft.handler.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.DisasterState;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;

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
        Main plugin = Main.getInstance();
        
        // ✅ YENİ MANTIK: Durum bazlı davranış sistemi
        
        // 1. Durum bazlı davranış
        switch (disaster.getDisasterState()) {
            case GO_CENTER:
                handleGoCenter(disaster, dragon, current, plugin, config);
                break;
            case ATTACK_CLAN:
                handleAttackClan(disaster, dragon, current, plugin, config);
                break;
            case ATTACK_PLAYER:
                handleAttackPlayer(disaster, dragon, current, plugin, config);
                break;
        }
        
        // Temel hareket (BaseCreatureHandler'dan) - Blok kırma ve diğer özellikler
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
    
    /**
     * GO_CENTER durumu: Merkeze gitme mantığı
     */
    private void handleGoCenter(Disaster disaster, EnderDragon dragon, Location current, Main plugin, DisasterConfig config) {
        // ✅ hasArrivedCenter kontrolü: Eğer merkeze ulaştıysa, bir daha merkeze gitme
        if (disaster.hasArrivedCenter()) {
            // Merkeze ulaştı, durumu değiştir
            updateStateAfterCenterReached(disaster, current, plugin);
            return;
        }
        
        // Merkeze gitme hedefi ayarla
        Location target = disaster.getTarget();
        if (target == null) {
            // Hedef yoksa, merkeze git
            if (plugin != null && plugin.getDifficultyManager() != null) {
                target = plugin.getDifficultyManager().getCenterLocation();
            }
            if (target == null) {
                target = current.getWorld().getSpawnLocation();
            }
            disaster.setTarget(target);
        }
        
        // Merkeze ulaşma kontrolü (50 blok yarıçap)
        if (target != null && current.getWorld().equals(target.getWorld())) {
            double distanceToCenter = current.distance(target);
            if (distanceToCenter <= 50.0) {
                // ✅ Merkeze ulaştı!
                disaster.setHasArrivedCenter(true);
                disaster.setDisasterState(DisasterState.ATTACK_CLAN); // Varsayılan olarak klan saldırısına geç
                updateStateAfterCenterReached(disaster, current, plugin);
                return;
            }
            
            // Merkeze doğru hareket et
            moveToTarget(dragon, current, target, config);
        }
    }
    
    /**
     * ATTACK_CLAN durumu: Klan kristallerine saldırma mantığı
     */
    private void handleAttackClan(Disaster disaster, EnderDragon dragon, Location current, Main plugin, DisasterConfig config) {
        Location targetCrystal = disaster.getTargetCrystal();
        
        // Hedef kristal yoksa veya kırıldıysa yeni hedef bul
        if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
            // Merkez konumunu al
            Location centerLocation = getCenterLocation(plugin, current);
            if (centerLocation == null) {
                centerLocation = current; // Fallback: current kullan
            }
            
            // Merkeze 1500 blok yakında klan var mı? (merkeze göre kontrol)
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
            if (!nearbyCrystals.isEmpty()) {
                // En yakın klan kristalini hedef al (merkeze en yakın)
                targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
            } else {
                // Yakında klan yok, oyuncuya saldır
                disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
                handleAttackPlayer(disaster, dragon, current, plugin, config);
                return;
            }
        }
        
        // Kristale doğru hareket et
        if (targetCrystal != null && current.getWorld().equals(targetCrystal.getWorld())) {
            double distanceToCrystal = current.distance(targetCrystal);
            
            // Kristale yakınsa (5 blok), vur
            if (distanceToCrystal <= 5.0) {
                boolean crystalDestroyed = attackCrystal(disaster, targetCrystal, plugin);
                
                // Kristal yok edildi, yeni hedef bul
                if (crystalDestroyed) {
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(null);
                    
                    // Yeni hedef bulma mantığı:
                    // 1. 1000 blok yakında klan varsa → ATTACK_CLAN
                    // 2. Yoksa oyuncu varsa → ATTACK_PLAYER
                    // 3. Yoksa en yakın klana → ATTACK_CLAN
                    findNewTargetAfterCrystalDestroyed(disaster, current, plugin);
                }
            } else {
                // Kristale doğru hareket et
                moveToTarget(dragon, current, targetCrystal, config);
            }
        }
    }
    
    /**
     * ATTACK_PLAYER durumu: Oyuncuları kovalama mantığı
     */
    private void handleAttackPlayer(Disaster disaster, EnderDragon dragon, Location current, Main plugin, DisasterConfig config) {
        Player targetPlayer = disaster.getTargetPlayer();
        
        // Hedef oyuncu yoksa veya öldüyse yeni hedef bul
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            targetPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(targetPlayer);
        }
        
        // 20 saniyede bir klan kontrolü yap
        long now = System.currentTimeMillis();
        if (now - disaster.getLastClanCheckTime() >= 20000) { // 20 saniye = 20000 ms
            disaster.setLastClanCheckTime(now);
            
            // 1000 blok yakında klan var mı?
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, current, 1000.0);
            if (!nearbyCrystals.isEmpty()) {
                // Klan bulundu, klan saldırısına geç
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                Location targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                disaster.setTargetPlayer(null);
                return;
            }
        }
        
        // Oyuncuya doğru hareket et
        if (targetPlayer != null && current.getWorld().equals(targetPlayer.getWorld())) {
            Location playerLoc = targetPlayer.getLocation();
            moveToTarget(dragon, current, playerLoc, config);
        } else {
            // Oyuncu bulunamadı, en yakın klana yönel
            Location nearestCrystal = findNearestCrystal(plugin, current);
            if (nearestCrystal != null) {
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                disaster.setTargetPlayer(null);
            }
        }
    }
    
    /**
     * Merkeze ulaştıktan sonra durumu güncelle
     */
    private void updateStateAfterCenterReached(Disaster disaster, Location current, Main plugin) {
        // Merkez konumunu al (current değil, gerçek merkez)
        Location centerLocation = getCenterLocation(plugin, current);
        if (centerLocation == null) {
            centerLocation = current; // Fallback: current kullan
        }
        
        // Merkeze 1500 blok yakında klan var mı? (merkeze göre kontrol)
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
        if (!nearbyCrystals.isEmpty()) {
            // Klan bulundu, klan saldırısına geç (merkeze en yakın klan)
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
        } else {
            // Yakında klan yok, oyuncuya saldır
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            Player nearestPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(nearestPlayer);
            if (nearestPlayer != null) {
                disaster.setTarget(nearestPlayer.getLocation());
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private void moveToTarget(EnderDragon dragon, Location current, Location target, DisasterConfig config) {
        if (target == null || !current.getWorld().equals(target.getWorld())) return;
        
        // Velocity ile hareket ettir (EnderDragon için daha etkili)
        org.bukkit.util.Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        double adjustedSpeed = Math.max(speed, 0.3); // Minimum 0.3 hız
        org.bukkit.util.Vector velocity = direction.multiply(adjustedSpeed);
        
        // EnderDragon için Y eksenini de kullan (uçabilir)
        double yComponent = direction.getY() * adjustedSpeed * 0.5;
        velocity.setY(Math.max(0.1, Math.max(0, yComponent))); // Minimum 0.1 Y hızı (uçması için)
        
        // Velocity'yi uygula
        dragon.setVelocity(velocity);
        
        // Yüz yönlendirmeyi yap
        me.mami.stratocraft.util.DisasterBehavior.faceTarget(dragon, target);
    }
    
    /**
     * Klan kristaline saldır
     * @return Kristal yok edildi mi?
     */
    private boolean attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
        if (plugin == null || plugin.getTerritoryManager() == null) return false;
        
        // Klanı bul
        Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
        if (targetClan == null) return false;
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) return false;
        
        // ✅ YENİ: Kalkan kontrolü
        me.mami.stratocraft.handler.structure.CrystalShieldHandler shieldHandler = 
            plugin.getCrystalShieldHandler();
        if (shieldHandler != null) {
            boolean blocked = shieldHandler.consumeShieldBlockOnDamage(targetClan);
            if (blocked) {
                // Saldırı engellendi
                crystalLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.BLOCK_CRACK,
                    crystalLoc,
                    20,
                    0.5, 0.5, 0.5, 0.1,
                    org.bukkit.Material.BARRIER.createBlockData()
                );
                return false; // Kristal hasar almadı
            }
        }
        
        // Felaket hasarı hesapla
        double baseDamage = disaster.getDamageMultiplier() * 10.0;
        
        // ✅ YENİ: Zırh kontrolü
        me.mami.stratocraft.handler.structure.CrystalArmorHandler armorHandler = 
            plugin.getCrystalArmorHandler();
        if (armorHandler != null) {
            // Zırh yakıt tüket
            armorHandler.consumeFuelOnDamage(targetClan, baseDamage);
        }
        
        // Hasar azaltma çarpanı
        double damageReduction = targetClan.getCrystalDamageReduction();
        double finalDamage = baseDamage * (1.0 - damageReduction);
        
        // ✅ YENİ: Kristale hasar ver (can sistemi ile)
        targetClan.damageCrystal(finalDamage);
        
        double currentHealth = targetClan.getCrystalCurrentHealth();
        double maxHealth = targetClan.getCrystalMaxHealth();
        double healthPercent = (currentHealth / maxHealth) * 100.0;
        
        // Partikül efekti (can yüzdesine göre)
        if (healthPercent > 50) {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, crystalLoc, 10);
        } else if (healthPercent > 25) {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, crystalLoc, 15);
        } else {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, crystalLoc, 20);
        }
        
        // Klan üyelerine uyarı
        for (UUID memberId : targetClan.getMembers().keySet()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c⚠ Kristal hasar aldı! Can: " + 
                    String.format("%.1f", currentHealth) + "/" + 
                    String.format("%.1f", maxHealth) + " (" + 
                    String.format("%.1f", healthPercent) + "%)");
            }
        }
        
        // Can bitti mi?
        if (currentHealth <= 0) {
            crystal.remove();
            // destroyCrystal() zaten damageCrystal() içinde çağrılıyor
            org.bukkit.Bukkit.broadcastMessage(
                org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                targetClan.getName() + " klanının kristali yok edildi!"
            );
            return true;
        }
        
        return false; // Kristal hala var
    }
    
    /**
     * Kristal yok edildikten sonra yeni hedef bul
     */
    private void findNewTargetAfterCrystalDestroyed(Disaster disaster, Location current, Main plugin) {
        // 1. 1000 blok yakında klan var mı?
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, current, 1000.0);
        if (!nearbyCrystals.isEmpty()) {
            // Klan bulundu, klan saldırısına devam et
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            disaster.setTargetPlayer(null);
            return;
        }
        
        // 2. Oyuncu var mı?
        Player nearestPlayer = findNearestPlayer(current);
        if (nearestPlayer != null) {
            // Oyuncu bulundu, oyuncuya saldır
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            disaster.setTargetPlayer(nearestPlayer);
            disaster.setTarget(nearestPlayer.getLocation());
            disaster.setTargetCrystal(null);
            return;
        }
        
        // 3. En yakın klana yönel (uzakta olsa bile)
        Location nearestCrystal = findNearestCrystal(plugin, current);
        if (nearestCrystal != null) {
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            disaster.setTargetPlayer(null);
        }
    }
    
    /**
     * Merkez konumunu al
     */
    private Location getCenterLocation(Main plugin, Location fallback) {
        if (plugin == null || plugin.getDifficultyManager() == null) {
            return fallback;
        }
        Location center = plugin.getDifficultyManager().getCenterLocation();
        return center != null ? center : fallback;
    }
    
    /**
     * Yarıçap içindeki klan kristallerini bul
     */
    private List<Location> findCrystalsInRadius(Main plugin, Location from, double radius) {
        if (plugin == null || plugin.getDisasterManager() == null) return new java.util.ArrayList<>();
        return plugin.getDisasterManager().findCrystalsInRadius(from, radius);
    }
    
    /**
     * En yakın klan kristalini bul
     */
    private Location findNearestCrystal(Main plugin, Location from) {
        if (plugin == null || plugin.getDisasterManager() == null) return null;
        return plugin.getDisasterManager().findNearestCrystal(from);
    }
    
    /**
     * En yakın oyuncuyu bul
     */
    private Player findNearestPlayer(Location from) {
        if (from == null) return null;
        org.bukkit.World world = from.getWorld();
        if (world == null) return null;
        
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : world.getPlayers()) {
            if (player.isDead() || !player.isOnline()) continue;
            
            double distance = from.distance(player.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    /**
     * Kristal yok edildi mi kontrol et
     */
    private boolean isCrystalDestroyed(Location crystalLoc) {
        if (crystalLoc == null || crystalLoc.getWorld() == null) return true;
        
        // EnderCrystal entity'si var mı kontrol et
        for (Entity entity : crystalLoc.getWorld().getNearbyEntities(crystalLoc, 2, 2, 2)) {
            if (entity instanceof EnderCrystal) {
                return false; // Kristal hala var
            }
        }
        
        return true; // Kristal yok
    }
}
