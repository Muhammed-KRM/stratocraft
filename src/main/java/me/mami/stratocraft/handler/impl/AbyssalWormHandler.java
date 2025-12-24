package me.mami.stratocraft.handler.impl;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.DisasterState;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterBehavior;
import me.mami.stratocraft.util.DisasterUtils;

/**
 * Hiçlik Solucanı Handler
 * Yer altından kazarak ilerler, ışınlanabilir
 * State-based AI (Kaos Ejderi mantığı)
 */
public class AbyssalWormHandler extends BaseCreatureHandler {
    
    public AbyssalWormHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof Silverfish)) {
            Main.getInstance().getLogger().warning("[AbyssalWormHandler] Entity is not Silverfish: " + (entity != null ? entity.getType() : "null"));
            return;
        }
        
        Silverfish worm = (Silverfish) entity;
        Location current = worm.getLocation();
        Main plugin = Main.getInstance();
        
        // ✅ DEBUG: Handler çağrıldı
        plugin.getLogger().info("[AbyssalWormHandler] handle() called - State: " + disaster.getDisasterState() + 
            ", Location: " + current.getBlockX() + "," + current.getBlockY() + "," + current.getBlockZ() +
            ", hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ YENİ MANTIK: Durum bazlı davranış sistemi
        
        // 1. Durum bazlı davranış
        switch (disaster.getDisasterState()) {
            case GO_CENTER:
                plugin.getLogger().info("[AbyssalWormHandler] Executing GO_CENTER state");
                handleGoCenter(disaster, worm, current, plugin, config);
                break;
            case ATTACK_CLAN:
                plugin.getLogger().info("[AbyssalWormHandler] Executing ATTACK_CLAN state");
                handleAttackClan(disaster, worm, current, plugin, config);
                break;
            case ATTACK_PLAYER:
                plugin.getLogger().info("[AbyssalWormHandler] Executing ATTACK_PLAYER state");
                handleAttackPlayer(disaster, worm, current, plugin, config);
                break;
        }
        
        // Temel hareket (BaseCreatureHandler'dan)
        super.handle(disaster, entity, config);
        
        // ✅ ÖZEL YETENEKLER: Yer altından kazma, ışınlanma (Abyssal Worm özel)
        useWormAbilities(disaster, worm, current, config);
    }
    
    /**
     * GO_CENTER durumu: Merkeze gitme mantığı
     */
    private void handleGoCenter(Disaster disaster, Silverfish worm, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[AbyssalWormHandler] handleGoCenter() - hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        if (disaster.hasArrivedCenter()) {
            plugin.getLogger().info("[AbyssalWormHandler] Already arrived at center, updating state");
            updateStateAfterCenterReached(disaster, current, plugin);
            return;
        }
        
        Location target = disaster.getTarget();
        if (target == null) {
            if (plugin != null && plugin.getDifficultyManager() != null) {
                target = plugin.getDifficultyManager().getCenterLocation();
            }
            if (target == null) {
                target = current.getWorld().getSpawnLocation();
            }
            disaster.setTarget(target);
            plugin.getLogger().info("[AbyssalWormHandler] Target set to center: " + 
                (target != null ? target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ() : "null"));
        }
        
        if (target != null && current.getWorld().equals(target.getWorld())) {
            double distanceToCenter = current.distance(target);
            plugin.getLogger().fine("[AbyssalWormHandler] Distance to center: " + String.format("%.2f", distanceToCenter));
            
            if (distanceToCenter <= 50.0) {
                plugin.getLogger().info("[AbyssalWormHandler] REACHED CENTER! Distance: " + String.format("%.2f", distanceToCenter));
                disaster.setHasArrivedCenter(true);
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                updateStateAfterCenterReached(disaster, current, plugin);
                return;
            }
            
            plugin.getLogger().fine("[AbyssalWormHandler] Moving towards center");
            moveToTarget(worm, current, target, config);
        } else {
            plugin.getLogger().warning("[AbyssalWormHandler] Target is null or different world!");
        }
    }
    
    /**
     * ATTACK_CLAN durumu: Klan kristallerine saldırma mantığı
     */
    private void handleAttackClan(Disaster disaster, Silverfish worm, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[AbyssalWormHandler] handleAttackClan() called");
        
        Location targetCrystal = disaster.getTargetCrystal();
        plugin.getLogger().info("[AbyssalWormHandler] Target crystal: " + (targetCrystal != null ? 
            targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ() : "null"));
        
        if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
            plugin.getLogger().info("[AbyssalWormHandler] Target crystal is null or destroyed, finding new target");
            
            Location searchLocation;
            double searchRadius;
            if (disaster.hasArrivedCenter()) {
                searchLocation = getCenterLocation(plugin, current);
                if (searchLocation == null) {
                    searchLocation = current;
                }
                searchRadius = 1500.0;
            } else {
                searchLocation = current;
                searchRadius = 1000.0;
            }
            
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
            plugin.getLogger().info("[AbyssalWormHandler] Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                plugin.getLogger().info("[AbyssalWormHandler] New target crystal set: " + 
                    targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
            } else {
                plugin.getLogger().info("[AbyssalWormHandler] No crystals found, switching to ATTACK_PLAYER");
                disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
                handleAttackPlayer(disaster, worm, current, plugin, config);
                return;
            }
        }
        
        if (targetCrystal != null && current.getWorld().equals(targetCrystal.getWorld())) {
            double distanceToCrystal = current.distance(targetCrystal);
            plugin.getLogger().fine("[AbyssalWormHandler] Distance to crystal: " + String.format("%.2f", distanceToCrystal));
            
            if (distanceToCrystal <= 5.0) {
                plugin.getLogger().info("[AbyssalWormHandler] Attacking crystal at distance: " + String.format("%.2f", distanceToCrystal));
                boolean crystalDestroyed = attackCrystal(disaster, targetCrystal, plugin);
                
                if (crystalDestroyed) {
                    plugin.getLogger().info("[AbyssalWormHandler] Crystal destroyed! Finding new target");
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(null);
                    findNewTargetAfterCrystalDestroyed(disaster, current, plugin);
                } else {
                    plugin.getLogger().fine("[AbyssalWormHandler] Crystal still alive");
                }
            } else {
                plugin.getLogger().fine("[AbyssalWormHandler] Moving towards crystal");
                moveToTarget(worm, current, targetCrystal, config);
            }
        } else {
            plugin.getLogger().warning("[AbyssalWormHandler] Target crystal is null or different world!");
        }
    }
    
    /**
     * ATTACK_PLAYER durumu: Oyuncuları kovalama mantığı
     */
    private void handleAttackPlayer(Disaster disaster, Silverfish worm, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[AbyssalWormHandler] handleAttackPlayer() called");
        
        Player targetPlayer = disaster.getTargetPlayer();
        plugin.getLogger().info("[AbyssalWormHandler] Target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            plugin.getLogger().info("[AbyssalWormHandler] Target player is null/offline/dead, finding new player");
            targetPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(targetPlayer);
            plugin.getLogger().info("[AbyssalWormHandler] New target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        }
        
        long now = System.currentTimeMillis();
        if (now - disaster.getLastClanCheckTime() >= 20000) {
            disaster.setLastClanCheckTime(now);
            
            Location searchLocation;
            double searchRadius;
            if (disaster.hasArrivedCenter()) {
                searchLocation = getCenterLocation(plugin, current);
                if (searchLocation == null) {
                    searchLocation = current;
                }
                searchRadius = 1500.0;
            } else {
                searchLocation = current;
                searchRadius = 1000.0;
            }
            
            List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
            plugin.getLogger().info("[AbyssalWormHandler] Clan check: Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                plugin.getLogger().info("[AbyssalWormHandler] Clan found! Switching to ATTACK_CLAN");
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                Location targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                disaster.setTargetPlayer(null);
                return;
            }
        }
        
        if (targetPlayer != null && current.getWorld().equals(targetPlayer.getWorld())) {
            Location playerLoc = targetPlayer.getLocation();
            double distanceToPlayer = current.distance(playerLoc);
            plugin.getLogger().fine("[AbyssalWormHandler] Moving towards player: " + targetPlayer.getName() + 
                " (distance: " + String.format("%.2f", distanceToPlayer) + ")");
            moveToTarget(worm, current, playerLoc, config);
        } else {
            plugin.getLogger().info("[AbyssalWormHandler] Player not found or different world, finding nearest crystal");
            Location nearestCrystal = findNearestCrystal(plugin, current);
            if (nearestCrystal != null) {
                plugin.getLogger().info("[AbyssalWormHandler] Found nearest crystal, switching to ATTACK_CLAN");
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                disaster.setTargetPlayer(null);
            } else {
                plugin.getLogger().warning("[AbyssalWormHandler] No player and no crystal found!");
            }
        }
    }
    
    /**
     * Merkeze ulaştıktan sonra durumu güncelle
     */
    private void updateStateAfterCenterReached(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[AbyssalWormHandler] updateStateAfterCenterReached() called");
        
        Location centerLocation = getCenterLocation(plugin, current);
        if (centerLocation == null) {
            centerLocation = current;
        }
        plugin.getLogger().info("[AbyssalWormHandler] Center location: " + 
            centerLocation.getBlockX() + "," + centerLocation.getBlockY() + "," + centerLocation.getBlockZ());
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
        plugin.getLogger().info("[AbyssalWormHandler] Found " + nearbyCrystals.size() + " crystals within 1500 blocks of center");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[AbyssalWormHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            plugin.getLogger().info("[AbyssalWormHandler] Target crystal set: " + 
                targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
        } else {
            plugin.getLogger().info("[AbyssalWormHandler] No crystals found, switching to ATTACK_PLAYER");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            Player nearestPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(nearestPlayer);
            if (nearestPlayer != null) {
                disaster.setTarget(nearestPlayer.getLocation());
                plugin.getLogger().info("[AbyssalWormHandler] Target player set: " + nearestPlayer.getName());
            } else {
                plugin.getLogger().warning("[AbyssalWormHandler] No player found!");
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private void moveToTarget(Silverfish worm, Location current, Location target, DisasterConfig config) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            Main.getInstance().getLogger().warning("[AbyssalWormHandler] moveToTarget() - Target is null or different world!");
            return;
        }
        
        double distance = current.distance(target);
        Main.getInstance().getLogger().fine("[AbyssalWormHandler] moveToTarget() - Distance: " + String.format("%.2f", distance));
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        double adjustedSpeed = Math.max(speed, 0.3);
        Vector velocity = direction.multiply(adjustedSpeed);
        
        worm.setVelocity(velocity);
        DisasterBehavior.faceTarget(worm, target);
    }
    
    /**
     * Klan kristaline saldır
     */
    private boolean attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
        plugin.getLogger().info("[AbyssalWormHandler] attackCrystal() called at " + 
            crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        
        if (plugin == null || plugin.getTerritoryManager() == null) {
            plugin.getLogger().warning("[AbyssalWormHandler] attackCrystal() - Plugin or TerritoryManager is null!");
            return false;
        }
        
        Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
        if (targetClan == null) {
            plugin.getLogger().warning("[AbyssalWormHandler] attackCrystal() - No clan found at crystal location!");
            return false;
        }
        
        plugin.getLogger().info("[AbyssalWormHandler] Attacking crystal of clan: " + targetClan.getName());
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) return false;
        
        // Kalkan kontrolü
        me.mami.stratocraft.handler.structure.CrystalShieldHandler shieldHandler = 
            plugin.getCrystalShieldHandler();
        if (shieldHandler != null) {
            boolean blocked = shieldHandler.consumeShieldBlockOnDamage(targetClan);
            if (blocked) {
                crystalLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.BLOCK_CRACK,
                    crystalLoc,
                    20,
                    0.5, 0.5, 0.5, 0.1,
                    Material.BARRIER.createBlockData()
                );
                return false;
            }
        }
        
        // ✅ YENİ: CrystalAttackHelper kullan
        me.mami.stratocraft.util.CrystalAttackHelper.AttackResult result = 
            me.mami.stratocraft.util.CrystalAttackHelper.attackCrystalByDisaster(
                targetClan, crystalLoc, disaster.getDamageMultiplier(), plugin);
        
        if (result.isDestroyed()) {
            plugin.getLogger().info("[AbyssalWormHandler] Crystal destroyed! Clan: " + targetClan.getName());
            return true;
        }
        
        plugin.getLogger().fine("[AbyssalWormHandler] Crystal health: " + 
            String.format("%.2f", result.getCurrentHealth()) + "/" + 
            String.format("%.2f", result.getMaxHealth()));
        return false;
    }
    
    /**
     * Kristal yok edildikten sonra yeni hedef bul
     */
    private void findNewTargetAfterCrystalDestroyed(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[AbyssalWormHandler] findNewTargetAfterCrystalDestroyed() called");
        
        Location searchLocation;
        double searchRadius;
        if (disaster.hasArrivedCenter()) {
            searchLocation = getCenterLocation(plugin, current);
            if (searchLocation == null) {
                searchLocation = current;
            }
            searchRadius = 1500.0;
            plugin.getLogger().info("[AbyssalWormHandler] Searching from center, radius: " + searchRadius);
        } else {
            searchLocation = current;
            searchRadius = 1000.0;
            plugin.getLogger().info("[AbyssalWormHandler] Searching from current, radius: " + searchRadius);
        }
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
        plugin.getLogger().info("[AbyssalWormHandler] Found " + nearbyCrystals.size() + " crystals in radius");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[AbyssalWormHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            disaster.setTargetPlayer(null);
            return;
        }
        
        Player nearestPlayer = findNearestPlayer(current);
        if (nearestPlayer != null) {
            plugin.getLogger().info("[AbyssalWormHandler] Switching to ATTACK_PLAYER (player found: " + nearestPlayer.getName() + ")");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            disaster.setTargetPlayer(nearestPlayer);
            disaster.setTarget(nearestPlayer.getLocation());
            disaster.setTargetCrystal(null);
            return;
        }
        
        plugin.getLogger().info("[AbyssalWormHandler] No nearby crystals or players, finding nearest crystal");
        Location nearestCrystal = findNearestCrystal(plugin, current);
        if (nearestCrystal != null) {
            plugin.getLogger().info("[AbyssalWormHandler] Found nearest crystal, switching to ATTACK_CLAN");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            disaster.setTargetPlayer(null);
        } else {
            plugin.getLogger().warning("[AbyssalWormHandler] No crystals found at all!");
        }
    }
    
    /**
     * Abyssal Worm özel yetenekleri: Yer altından kazma, ışınlanma
     */
    private void useWormAbilities(Disaster disaster, Silverfish worm, Location current, DisasterConfig config) {
        Main plugin = Main.getInstance();
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        plugin.getLogger().fine("[AbyssalWormHandler] useWormAbilities() called");
        
        // Yer altından kazma (alt blokları kır)
        Block belowBlock = current.clone().add(0, -1, 0).getBlock();
        if (belowBlock.getType() != Material.AIR && belowBlock.getType() != Material.BEDROCK) {
            belowBlock.setType(Material.AIR);
            me.mami.stratocraft.util.EffectUtil.playDisasterEffect(belowBlock.getLocation());
        }
        
        // Önündeki bloğu da kır
        Vector direction = DisasterUtils.calculateDirection(current, target);
        Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            frontBlock.setType(Material.AIR);
            me.mami.stratocraft.util.EffectUtil.playDisasterEffect(frontBlock.getLocation());
        }
        
        // Sıkışma önleme - ışınlanma
        if (worm.getLocation().getBlock().getType() != Material.AIR) {
            Location teleportLoc = DisasterUtils.findSafeLocation(
                current.clone().add(direction.multiply(config.getTeleportDistance())),
                (int) config.getTeleportDistance()
            );
            if (teleportLoc != null) {
                plugin.getLogger().info("[AbyssalWormHandler] Teleporting to avoid stuck");
                worm.teleport(teleportLoc);
            }
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
            Main.getInstance().getLogger().warning("[AbyssalWormHandler] findNearestPlayer() - Location is null!");
            return null;
        }
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            Main.getInstance().getLogger().warning("[AbyssalWormHandler] findNearestPlayer() - World is null!");
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
        
        Main.getInstance().getLogger().info("[AbyssalWormHandler] findNearestPlayer() - Found: " + 
            (nearest != null ? nearest.getName() + " (distance: " + String.format("%.2f", minDistance) + ")" : "null"));
        
        return nearest;
    }
    
    /**
     * Kristal yok edildi mi kontrol et
     */
    private boolean isCrystalDestroyed(Location crystalLoc) {
        if (crystalLoc == null || crystalLoc.getWorld() == null) return true;
        
        for (Entity entity : crystalLoc.getWorld().getNearbyEntities(crystalLoc, 2, 2, 2)) {
            if (entity instanceof EnderCrystal) {
                return false;
            }
        }
        
        return true;
    }
}
