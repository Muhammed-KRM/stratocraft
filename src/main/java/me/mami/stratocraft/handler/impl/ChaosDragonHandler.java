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
        if (!(entity instanceof EnderDragon)) {
            Main.getInstance().getLogger().warning("[ChaosDragonHandler] Entity is not EnderDragon: " + (entity != null ? entity.getType() : "null"));
            return;
        }
        
        EnderDragon dragon = (EnderDragon) entity;
        Location current = dragon.getLocation();
        Main plugin = Main.getInstance();
        
        // ✅ DEBUG: Handler çağrıldı
        plugin.getLogger().info("[ChaosDragonHandler] handle() called - State: " + disaster.getDisasterState() + 
            ", Location: " + current.getBlockX() + "," + current.getBlockY() + "," + current.getBlockZ() +
            ", hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ YENİ MANTIK: Durum bazlı davranış sistemi
        
        // 1. Durum bazlı davranış
        switch (disaster.getDisasterState()) {
            case GO_CENTER:
                plugin.getLogger().info("[ChaosDragonHandler] Executing GO_CENTER state");
                handleGoCenter(disaster, dragon, current, plugin, config);
                break;
            case ATTACK_CLAN:
                plugin.getLogger().info("[ChaosDragonHandler] Executing ATTACK_CLAN state");
                handleAttackClan(disaster, dragon, current, plugin, config);
                break;
            case ATTACK_PLAYER:
                plugin.getLogger().info("[ChaosDragonHandler] Executing ATTACK_PLAYER state");
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
        // ✅ DEBUG: GO_CENTER başladı
        plugin.getLogger().info("[ChaosDragonHandler] handleGoCenter() - hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ hasArrivedCenter kontrolü: Eğer merkeze ulaştıysa, bir daha merkeze gitme
        if (disaster.hasArrivedCenter()) {
            // Merkeze ulaştı, durumu değiştir
            plugin.getLogger().info("[ChaosDragonHandler] Already arrived at center, updating state");
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
            plugin.getLogger().info("[ChaosDragonHandler] Target set to center: " + 
                (target != null ? target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ() : "null"));
        }
        
        // Merkeze ulaşma kontrolü (50 blok yarıçap)
        if (target != null && current.getWorld().equals(target.getWorld())) {
            double distanceToCenter = current.distance(target);
            plugin.getLogger().fine("[ChaosDragonHandler] Distance to center: " + String.format("%.2f", distanceToCenter));
            
            if (distanceToCenter <= 50.0) {
                // ✅ Merkeze ulaştı!
                plugin.getLogger().info("[ChaosDragonHandler] REACHED CENTER! Distance: " + String.format("%.2f", distanceToCenter));
                disaster.setHasArrivedCenter(true);
                disaster.setDisasterState(DisasterState.ATTACK_CLAN); // Varsayılan olarak klan saldırısına geç
                updateStateAfterCenterReached(disaster, current, plugin);
                return;
            }
            
            // Merkeze doğru hareket et
            plugin.getLogger().fine("[ChaosDragonHandler] Moving towards center");
            moveToTarget(dragon, current, target, config);
        } else {
            plugin.getLogger().warning("[ChaosDragonHandler] Target is null or different world!");
        }
    }
    
    /**
     * ATTACK_CLAN durumu: Klan kristallerine saldırma mantığı
     */
    private void handleAttackClan(Disaster disaster, EnderDragon dragon, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[ChaosDragonHandler] handleAttackClan() called");
        
        Location targetCrystal = disaster.getTargetCrystal();
        plugin.getLogger().info("[ChaosDragonHandler] Target crystal: " + (targetCrystal != null ? 
            targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ() : "null"));
        
        // Hedef kristal yoksa veya kırıldıysa yeni hedef bul
        if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
            plugin.getLogger().info("[ChaosDragonHandler] Target crystal is null or destroyed, finding new target");
            // ✅ DÜZELTME: Merkeze ulaştıysa merkeze göre, değilse current'a göre kontrol
            Location searchLocation;
            double searchRadius;
            if (disaster.hasArrivedCenter()) {
                // Merkeze ulaştıysa merkeze göre kontrol (1500 blok)
                searchLocation = getCenterLocation(plugin, current);
                if (searchLocation == null) {
                    searchLocation = current; // Fallback
                }
                searchRadius = 1500.0;
            } else {
                // Merkeze ulaşmadıysa current'a göre kontrol (1000 blok)
                searchLocation = current;
                searchRadius = 1000.0;
            }
            
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
            plugin.getLogger().info("[ChaosDragonHandler] Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius + 
                " from " + (disaster.hasArrivedCenter() ? "center" : "current"));
            
            if (!nearbyCrystals.isEmpty()) {
                // En yakın klan kristalini hedef al
                targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                plugin.getLogger().info("[ChaosDragonHandler] New target crystal set: " + 
                    targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
            } else {
                // Yakında klan yok, oyuncuya saldır
                plugin.getLogger().info("[ChaosDragonHandler] No crystals found, switching to ATTACK_PLAYER");
                disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
                handleAttackPlayer(disaster, dragon, current, plugin, config);
                return;
            }
        }
        
        // Kristale doğru hareket et
        if (targetCrystal != null && current.getWorld().equals(targetCrystal.getWorld())) {
            double distanceToCrystal = current.distance(targetCrystal);
            plugin.getLogger().fine("[ChaosDragonHandler] Distance to crystal: " + String.format("%.2f", distanceToCrystal));
            
            // Kristale yakınsa (5 blok), vur
            if (distanceToCrystal <= 5.0) {
                plugin.getLogger().info("[ChaosDragonHandler] Attacking crystal at distance: " + String.format("%.2f", distanceToCrystal));
                boolean crystalDestroyed = attackCrystal(disaster, targetCrystal, plugin);
                
                // Kristal yok edildi, yeni hedef bul
                if (crystalDestroyed) {
                    plugin.getLogger().info("[ChaosDragonHandler] Crystal destroyed! Finding new target");
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(null);
                    
                    // Yeni hedef bulma mantığı:
                    // 1. 1000 blok yakında klan varsa → ATTACK_CLAN
                    // 2. Yoksa oyuncu varsa → ATTACK_PLAYER
                    // 3. Yoksa en yakın klana → ATTACK_CLAN
                    findNewTargetAfterCrystalDestroyed(disaster, current, plugin);
                } else {
                    plugin.getLogger().fine("[ChaosDragonHandler] Crystal still alive");
                }
            } else {
                // Kristale doğru hareket et
                plugin.getLogger().fine("[ChaosDragonHandler] Moving towards crystal");
                moveToTarget(dragon, current, targetCrystal, config);
            }
        } else {
            plugin.getLogger().warning("[ChaosDragonHandler] Target crystal is null or different world!");
        }
    }
    
    /**
     * ATTACK_PLAYER durumu: Oyuncuları kovalama mantığı
     */
    private void handleAttackPlayer(Disaster disaster, EnderDragon dragon, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[ChaosDragonHandler] handleAttackPlayer() called");
        
        Player targetPlayer = disaster.getTargetPlayer();
        plugin.getLogger().info("[ChaosDragonHandler] Target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        
        // Hedef oyuncu yoksa veya öldüyse yeni hedef bul
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            plugin.getLogger().info("[ChaosDragonHandler] Target player is null/offline/dead, finding new player");
            targetPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(targetPlayer);
            plugin.getLogger().info("[ChaosDragonHandler] New target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        }
        
        // 20 saniyede bir klan kontrolü yap
        long now = System.currentTimeMillis();
        if (now - disaster.getLastClanCheckTime() >= 20000) { // 20 saniye = 20000 ms
            disaster.setLastClanCheckTime(now);
            
            // ✅ DÜZELTME: Merkeze ulaştıysa merkeze göre, değilse current'a göre kontrol
            Location searchLocation;
            double searchRadius;
            if (disaster.hasArrivedCenter()) {
                // Merkeze ulaştıysa merkeze göre kontrol (1500 blok)
                searchLocation = getCenterLocation(plugin, current);
                if (searchLocation == null) {
                    searchLocation = current; // Fallback
                }
                searchRadius = 1500.0;
            } else {
                // Merkeze ulaşmadıysa current'a göre kontrol (1000 blok)
                searchLocation = current;
                searchRadius = 1000.0;
            }
            
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
            plugin.getLogger().info("[ChaosDragonHandler] Clan check: Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                // Klan bulundu, klan saldırısına geç
                plugin.getLogger().info("[ChaosDragonHandler] Clan found! Switching to ATTACK_CLAN");
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
            double distanceToPlayer = current.distance(playerLoc);
            plugin.getLogger().fine("[ChaosDragonHandler] Moving towards player: " + targetPlayer.getName() + 
                " (distance: " + String.format("%.2f", distanceToPlayer) + ")");
            moveToTarget(dragon, current, playerLoc, config);
        } else {
            // Oyuncu bulunamadı, en yakın klana yönel
            plugin.getLogger().info("[ChaosDragonHandler] Player not found or different world, finding nearest crystal");
            Location nearestCrystal = findNearestCrystal(plugin, current);
            if (nearestCrystal != null) {
                plugin.getLogger().info("[ChaosDragonHandler] Found nearest crystal, switching to ATTACK_CLAN");
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                disaster.setTargetPlayer(null);
            } else {
                plugin.getLogger().warning("[ChaosDragonHandler] No player and no crystal found!");
            }
        }
    }
    
    /**
     * Merkeze ulaştıktan sonra durumu güncelle
     */
    private void updateStateAfterCenterReached(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[ChaosDragonHandler] updateStateAfterCenterReached() called");
        
        // Merkez konumunu al (current değil, gerçek merkez)
        Location centerLocation = getCenterLocation(plugin, current);
        if (centerLocation == null) {
            centerLocation = current; // Fallback: current kullan
        }
        plugin.getLogger().info("[ChaosDragonHandler] Center location: " + 
            centerLocation.getBlockX() + "," + centerLocation.getBlockY() + "," + centerLocation.getBlockZ());
        
        // Merkeze 1500 blok yakında klan var mı? (merkeze göre kontrol)
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
        plugin.getLogger().info("[ChaosDragonHandler] Found " + nearbyCrystals.size() + " crystals within 1500 blocks of center");
        
        if (!nearbyCrystals.isEmpty()) {
            // Klan bulundu, klan saldırısına geç (merkeze en yakın klan)
            plugin.getLogger().info("[ChaosDragonHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            plugin.getLogger().info("[ChaosDragonHandler] Target crystal set: " + 
                targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
        } else {
            // Yakında klan yok, oyuncuya saldır
            plugin.getLogger().info("[ChaosDragonHandler] No crystals found, switching to ATTACK_PLAYER");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            Player nearestPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(nearestPlayer);
            if (nearestPlayer != null) {
                disaster.setTarget(nearestPlayer.getLocation());
                plugin.getLogger().info("[ChaosDragonHandler] Target player set: " + nearestPlayer.getName());
            } else {
                plugin.getLogger().warning("[ChaosDragonHandler] No player found!");
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private void moveToTarget(EnderDragon dragon, Location current, Location target, DisasterConfig config) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            Main.getInstance().getLogger().warning("[ChaosDragonHandler] moveToTarget() - Target is null or different world!");
            return;
        }
        
        double distance = current.distance(target);
        Main.getInstance().getLogger().fine("[ChaosDragonHandler] moveToTarget() - Distance: " + String.format("%.2f", distance));
        
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
        plugin.getLogger().info("[ChaosDragonHandler] attackCrystal() called at " + 
            crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        
        if (plugin == null || plugin.getTerritoryManager() == null) {
            plugin.getLogger().warning("[ChaosDragonHandler] attackCrystal() - Plugin or TerritoryManager is null!");
            return false;
        }
        
        // Klanı bul
        Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
        if (targetClan == null) {
            plugin.getLogger().warning("[ChaosDragonHandler] attackCrystal() - No clan found at crystal location!");
            return false;
        }
        
        plugin.getLogger().info("[ChaosDragonHandler] Attacking crystal of clan: " + targetClan.getName());
        
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
        
        // ✅ YENİ: CrystalAttackHelper kullan
        me.mami.stratocraft.util.CrystalAttackHelper.AttackResult result = 
            me.mami.stratocraft.util.CrystalAttackHelper.attackCrystalByDisaster(
                targetClan, crystalLoc, disaster.getDamageMultiplier(), plugin);
        
        if (result.isDestroyed()) {
            plugin.getLogger().info("[ChaosDragonHandler] Crystal destroyed! Clan: " + targetClan.getName());
            return true;
        }
        
        plugin.getLogger().fine("[ChaosDragonHandler] Crystal health: " + 
            String.format("%.2f", result.getCurrentHealth()) + "/" + 
            String.format("%.2f", result.getMaxHealth()));
        return false; // Kristal hala var
    }
    
    /**
     * Kristal yok edildikten sonra yeni hedef bul
     */
    private void findNewTargetAfterCrystalDestroyed(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[ChaosDragonHandler] findNewTargetAfterCrystalDestroyed() called");
        
        // ✅ DÜZELTME: Merkeze ulaştıysa merkeze göre, değilse current'a göre kontrol
        Location searchLocation;
        double searchRadius;
        if (disaster.hasArrivedCenter()) {
            // Merkeze ulaştıysa merkeze göre kontrol (1500 blok)
            searchLocation = getCenterLocation(plugin, current);
            if (searchLocation == null) {
                searchLocation = current; // Fallback
            }
            searchRadius = 1500.0;
            plugin.getLogger().info("[ChaosDragonHandler] Searching from center, radius: " + searchRadius);
        } else {
            // Merkeze ulaşmadıysa current'a göre kontrol (1000 blok)
            searchLocation = current;
            searchRadius = 1000.0;
            plugin.getLogger().info("[ChaosDragonHandler] Searching from current, radius: " + searchRadius);
        }
        
        // 1. Yarıçap içinde klan var mı?
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
        plugin.getLogger().info("[ChaosDragonHandler] Found " + nearbyCrystals.size() + " crystals in radius");
        
        if (!nearbyCrystals.isEmpty()) {
            // Klan bulundu, klan saldırısına devam et
            plugin.getLogger().info("[ChaosDragonHandler] Switching to ATTACK_CLAN (crystals found)");
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
            plugin.getLogger().info("[ChaosDragonHandler] Switching to ATTACK_PLAYER (player found: " + nearestPlayer.getName() + ")");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            disaster.setTargetPlayer(nearestPlayer);
            disaster.setTarget(nearestPlayer.getLocation());
            disaster.setTargetCrystal(null);
            return;
        }
        
        // 3. En yakın klana yönel (uzakta olsa bile)
        plugin.getLogger().info("[ChaosDragonHandler] No nearby crystals or players, finding nearest crystal");
        Location nearestCrystal = findNearestCrystal(plugin, current);
        if (nearestCrystal != null) {
            plugin.getLogger().info("[ChaosDragonHandler] Found nearest crystal, switching to ATTACK_CLAN");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            disaster.setTargetPlayer(null);
        } else {
            plugin.getLogger().warning("[ChaosDragonHandler] No crystals found at all!");
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
        if (from == null) {
            Main.getInstance().getLogger().warning("[ChaosDragonHandler] findNearestPlayer() - Location is null!");
            return null;
        }
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            Main.getInstance().getLogger().warning("[ChaosDragonHandler] findNearestPlayer() - World is null!");
            return null;
        }
        
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
        
        Main.getInstance().getLogger().info("[ChaosDragonHandler] findNearestPlayer() - Found: " + 
            (nearest != null ? nearest.getName() + " (distance: " + String.format("%.2f", minDistance) + ")" : "null"));
        
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
