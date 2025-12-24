package me.mami.stratocraft.handler.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.DisasterState;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterBehavior;
import me.mami.stratocraft.util.DisasterUtils;

/**
 * Buzul Leviathan Handler
 * Donma ve buz dönüşümü yetenekleri
 * State-based AI (Kaos Ejderi mantığı)
 */
public class IceLeviathanHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    
    public IceLeviathanHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof ElderGuardian)) {
            Main.getInstance().getLogger().warning("[IceLeviathanHandler] Entity is not ElderGuardian: " + (entity != null ? entity.getType() : "null"));
            return;
        }
        
        ElderGuardian leviathan = (ElderGuardian) entity;
        Location current = leviathan.getLocation();
        Main plugin = Main.getInstance();
        
        // ✅ DEBUG: Handler çağrıldı
        plugin.getLogger().info("[IceLeviathanHandler] handle() called - State: " + disaster.getDisasterState() + 
            ", Location: " + current.getBlockX() + "," + current.getBlockY() + "," + current.getBlockZ() +
            ", hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ YENİ MANTIK: Durum bazlı davranış sistemi
        
        // 1. Durum bazlı davranış
        switch (disaster.getDisasterState()) {
            case GO_CENTER:
                plugin.getLogger().info("[IceLeviathanHandler] Executing GO_CENTER state");
                handleGoCenter(disaster, leviathan, current, plugin, config);
                break;
            case ATTACK_CLAN:
                plugin.getLogger().info("[IceLeviathanHandler] Executing ATTACK_CLAN state");
                handleAttackClan(disaster, leviathan, current, plugin, config);
                break;
            case ATTACK_PLAYER:
                plugin.getLogger().info("[IceLeviathanHandler] Executing ATTACK_PLAYER state");
                handleAttackPlayer(disaster, leviathan, current, plugin, config);
                break;
        }
        
        // Temel hareket (BaseCreatureHandler'dan)
        super.handle(disaster, entity, config);
        
        // ✅ ÖZEL YETENEKLER: Donma, buz dönüşümü (Ice Leviathan özel)
        useIceAbilities(disaster, leviathan, current, config);
    }
    
    /**
     * GO_CENTER durumu: Merkeze gitme mantığı
     */
    private void handleGoCenter(Disaster disaster, ElderGuardian leviathan, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[IceLeviathanHandler] handleGoCenter() - hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        if (disaster.hasArrivedCenter()) {
            plugin.getLogger().info("[IceLeviathanHandler] Already arrived at center, updating state");
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
            plugin.getLogger().info("[IceLeviathanHandler] Target set to center: " + 
                (target != null ? target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ() : "null"));
        }
        
        if (target != null && current.getWorld().equals(target.getWorld())) {
            double distanceToCenter = current.distance(target);
            plugin.getLogger().fine("[IceLeviathanHandler] Distance to center: " + String.format("%.2f", distanceToCenter));
            
            if (distanceToCenter <= 50.0) {
                plugin.getLogger().info("[IceLeviathanHandler] REACHED CENTER! Distance: " + String.format("%.2f", distanceToCenter));
                disaster.setHasArrivedCenter(true);
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                updateStateAfterCenterReached(disaster, current, plugin);
                return;
            }
            
            plugin.getLogger().fine("[IceLeviathanHandler] Moving towards center");
            moveToTarget(leviathan, current, target, config);
        } else {
            plugin.getLogger().warning("[IceLeviathanHandler] Target is null or different world!");
        }
    }
    
    /**
     * ATTACK_CLAN durumu: Klan kristallerine saldırma mantığı
     */
    private void handleAttackClan(Disaster disaster, ElderGuardian leviathan, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[IceLeviathanHandler] handleAttackClan() called");
        
        Location targetCrystal = disaster.getTargetCrystal();
        plugin.getLogger().info("[IceLeviathanHandler] Target crystal: " + (targetCrystal != null ? 
            targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ() : "null"));
        
        if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
            plugin.getLogger().info("[IceLeviathanHandler] Target crystal is null or destroyed, finding new target");
            
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
            plugin.getLogger().info("[IceLeviathanHandler] Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                plugin.getLogger().info("[IceLeviathanHandler] New target crystal set: " + 
                    targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
            } else {
                plugin.getLogger().info("[IceLeviathanHandler] No crystals found, switching to ATTACK_PLAYER");
                disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
                handleAttackPlayer(disaster, leviathan, current, plugin, config);
                return;
            }
        }
        
        if (targetCrystal != null && current.getWorld().equals(targetCrystal.getWorld())) {
            double distanceToCrystal = current.distance(targetCrystal);
            plugin.getLogger().fine("[IceLeviathanHandler] Distance to crystal: " + String.format("%.2f", distanceToCrystal));
            
            if (distanceToCrystal <= 5.0) {
                plugin.getLogger().info("[IceLeviathanHandler] Attacking crystal at distance: " + String.format("%.2f", distanceToCrystal));
                boolean crystalDestroyed = attackCrystal(disaster, targetCrystal, plugin);
                
                if (crystalDestroyed) {
                    plugin.getLogger().info("[IceLeviathanHandler] Crystal destroyed! Finding new target");
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(null);
                    findNewTargetAfterCrystalDestroyed(disaster, current, plugin);
                } else {
                    plugin.getLogger().fine("[IceLeviathanHandler] Crystal still alive");
                }
            } else {
                plugin.getLogger().fine("[IceLeviathanHandler] Moving towards crystal");
                moveToTarget(leviathan, current, targetCrystal, config);
            }
        } else {
            plugin.getLogger().warning("[IceLeviathanHandler] Target crystal is null or different world!");
        }
    }
    
    /**
     * ATTACK_PLAYER durumu: Oyuncuları kovalama mantığı
     */
    private void handleAttackPlayer(Disaster disaster, ElderGuardian leviathan, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[IceLeviathanHandler] handleAttackPlayer() called");
        
        Player targetPlayer = disaster.getTargetPlayer();
        plugin.getLogger().info("[IceLeviathanHandler] Target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            plugin.getLogger().info("[IceLeviathanHandler] Target player is null/offline/dead, finding new player");
            targetPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(targetPlayer);
            plugin.getLogger().info("[IceLeviathanHandler] New target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
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
            plugin.getLogger().info("[IceLeviathanHandler] Clan check: Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                plugin.getLogger().info("[IceLeviathanHandler] Clan found! Switching to ATTACK_CLAN");
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
            plugin.getLogger().fine("[IceLeviathanHandler] Moving towards player: " + targetPlayer.getName() + 
                " (distance: " + String.format("%.2f", distanceToPlayer) + ")");
            moveToTarget(leviathan, current, playerLoc, config);
        } else {
            plugin.getLogger().info("[IceLeviathanHandler] Player not found or different world, finding nearest crystal");
            Location nearestCrystal = findNearestCrystal(plugin, current);
            if (nearestCrystal != null) {
                plugin.getLogger().info("[IceLeviathanHandler] Found nearest crystal, switching to ATTACK_CLAN");
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                disaster.setTargetPlayer(null);
            } else {
                plugin.getLogger().warning("[IceLeviathanHandler] No player and no crystal found!");
            }
        }
    }
    
    /**
     * Merkeze ulaştıktan sonra durumu güncelle
     */
    private void updateStateAfterCenterReached(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[IceLeviathanHandler] updateStateAfterCenterReached() called");
        
        Location centerLocation = getCenterLocation(plugin, current);
        if (centerLocation == null) {
            centerLocation = current;
        }
        plugin.getLogger().info("[IceLeviathanHandler] Center location: " + 
            centerLocation.getBlockX() + "," + centerLocation.getBlockY() + "," + centerLocation.getBlockZ());
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
        plugin.getLogger().info("[IceLeviathanHandler] Found " + nearbyCrystals.size() + " crystals within 1500 blocks of center");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[IceLeviathanHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            plugin.getLogger().info("[IceLeviathanHandler] Target crystal set: " + 
                targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
        } else {
            plugin.getLogger().info("[IceLeviathanHandler] No crystals found, switching to ATTACK_PLAYER");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            Player nearestPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(nearestPlayer);
            if (nearestPlayer != null) {
                disaster.setTarget(nearestPlayer.getLocation());
                plugin.getLogger().info("[IceLeviathanHandler] Target player set: " + nearestPlayer.getName());
            } else {
                plugin.getLogger().warning("[IceLeviathanHandler] No player found!");
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private void moveToTarget(ElderGuardian leviathan, Location current, Location target, DisasterConfig config) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            Main.getInstance().getLogger().warning("[IceLeviathanHandler] moveToTarget() - Target is null or different world!");
            return;
        }
        
        double distance = current.distance(target);
        Main.getInstance().getLogger().fine("[IceLeviathanHandler] moveToTarget() - Distance: " + String.format("%.2f", distance));
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        double adjustedSpeed = Math.max(speed, 0.3);
        Vector velocity = direction.multiply(adjustedSpeed);
        
        leviathan.setVelocity(velocity);
        DisasterBehavior.faceTarget(leviathan, target);
    }
    
    /**
     * Klan kristaline saldır
     */
    private boolean attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
        plugin.getLogger().info("[IceLeviathanHandler] attackCrystal() called at " + 
            crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        
        if (plugin == null || plugin.getTerritoryManager() == null) {
            plugin.getLogger().warning("[IceLeviathanHandler] attackCrystal() - Plugin or TerritoryManager is null!");
            return false;
        }
        
        Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
        if (targetClan == null) {
            plugin.getLogger().warning("[IceLeviathanHandler] attackCrystal() - No clan found at crystal location!");
            return false;
        }
        
        plugin.getLogger().info("[IceLeviathanHandler] Attacking crystal of clan: " + targetClan.getName());
        
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
            plugin.getLogger().info("[IceLeviathanHandler] Crystal destroyed! Clan: " + targetClan.getName());
            return true;
        }
        
        plugin.getLogger().fine("[IceLeviathanHandler] Crystal health: " + 
            String.format("%.2f", result.getCurrentHealth()) + "/" + 
            String.format("%.2f", result.getMaxHealth()));
        return false;
    }
    
    /**
     * Kristal yok edildikten sonra yeni hedef bul
     */
    private void findNewTargetAfterCrystalDestroyed(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[IceLeviathanHandler] findNewTargetAfterCrystalDestroyed() called");
        
        Location searchLocation;
        double searchRadius;
        if (disaster.hasArrivedCenter()) {
            searchLocation = getCenterLocation(plugin, current);
            if (searchLocation == null) {
                searchLocation = current;
            }
            searchRadius = 1500.0;
            plugin.getLogger().info("[IceLeviathanHandler] Searching from center, radius: " + searchRadius);
        } else {
            searchLocation = current;
            searchRadius = 1000.0;
            plugin.getLogger().info("[IceLeviathanHandler] Searching from current, radius: " + searchRadius);
        }
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
        plugin.getLogger().info("[IceLeviathanHandler] Found " + nearbyCrystals.size() + " crystals in radius");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[IceLeviathanHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            disaster.setTargetPlayer(null);
            return;
        }
        
        Player nearestPlayer = findNearestPlayer(current);
        if (nearestPlayer != null) {
            plugin.getLogger().info("[IceLeviathanHandler] Switching to ATTACK_PLAYER (player found: " + nearestPlayer.getName() + ")");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            disaster.setTargetPlayer(nearestPlayer);
            disaster.setTarget(nearestPlayer.getLocation());
            disaster.setTargetCrystal(null);
            return;
        }
        
        plugin.getLogger().info("[IceLeviathanHandler] No nearby crystals or players, finding nearest crystal");
        Location nearestCrystal = findNearestCrystal(plugin, current);
        if (nearestCrystal != null) {
            plugin.getLogger().info("[IceLeviathanHandler] Found nearest crystal, switching to ATTACK_CLAN");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            disaster.setTargetPlayer(null);
        } else {
            plugin.getLogger().warning("[IceLeviathanHandler] No crystals found at all!");
        }
    }
    
    /**
     * Ice Leviathan özel yetenekleri: Donma, buz dönüşümü
     */
    private void useIceAbilities(Disaster disaster, ElderGuardian leviathan, Location current, DisasterConfig config) {
        Main plugin = Main.getInstance();
        plugin.getLogger().fine("[IceLeviathanHandler] useIceAbilities() called");
        
        // Donma yeteneği
        if (random.nextInt(100) < config.getFreezeChance()) {
            plugin.getLogger().info("[IceLeviathanHandler] Using freeze ability");
            
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getFreezeRadius()) {
                    // Oyuncuyu dondur
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, config.getFreezeDuration(), 3, false, false));
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW_DIGGING, config.getFreezeDuration(), 2, false, false));
                    player.damage(3.0 * disaster.getDamageMultiplier(), leviathan);
                }
            }
            
            // Etrafındaki blokları buz yap
            for (int x = -config.getIceConversionRadius(); x <= config.getIceConversionRadius(); x++) {
                for (int z = -config.getIceConversionRadius(); z <= config.getIceConversionRadius(); z++) {
                    for (int y = -2; y <= 2; y++) {
                        Block block = current.clone().add(x, y, z).getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK && 
                            block.getType() != Material.ICE && block.getType() != Material.PACKED_ICE) {
                            if (random.nextInt(100) < config.getIceConversionChance()) {
                                block.setType(Material.ICE);
                            }
                        }
                    }
                }
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
            Main.getInstance().getLogger().warning("[IceLeviathanHandler] findNearestPlayer() - Location is null!");
            return null;
        }
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            Main.getInstance().getLogger().warning("[IceLeviathanHandler] findNearestPlayer() - World is null!");
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
        
        Main.getInstance().getLogger().info("[IceLeviathanHandler] findNearestPlayer() - Found: " + 
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
