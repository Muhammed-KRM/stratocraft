package me.mami.stratocraft.handler.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
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
 * Titan Golem Handler
 * Zıplama-Patlama, Blok Fırlatma yetenekleri
 * State-based AI (Kaos Ejderi mantığı)
 */
public class TitanGolemHandler extends BaseCreatureHandler {
    private final Random random = new Random();
    private int tickCounter = 0;
    private int lastJumpTick = 0;
    
    public TitanGolemHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof IronGolem)) {
            Main.getInstance().getLogger().warning("[TitanGolemHandler] Entity is not IronGolem: " + (entity != null ? entity.getType() : "null"));
            return;
        }
        
        IronGolem golem = (IronGolem) entity;
        Location current = golem.getLocation();
        Main plugin = Main.getInstance();
        
        tickCounter++;
        
        // ✅ DEBUG: Handler çağrıldı
        plugin.getLogger().info("[TitanGolemHandler] handle() called - State: " + disaster.getDisasterState() + 
            ", Location: " + current.getBlockX() + "," + current.getBlockY() + "," + current.getBlockZ() +
            ", hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ YENİ MANTIK: Durum bazlı davranış sistemi
        
        // 1. Durum bazlı davranış
        switch (disaster.getDisasterState()) {
            case GO_CENTER:
                plugin.getLogger().info("[TitanGolemHandler] Executing GO_CENTER state");
                handleGoCenter(disaster, golem, current, plugin, config);
                break;
            case ATTACK_CLAN:
                plugin.getLogger().info("[TitanGolemHandler] Executing ATTACK_CLAN state");
                handleAttackClan(disaster, golem, current, plugin, config);
                break;
            case ATTACK_PLAYER:
                plugin.getLogger().info("[TitanGolemHandler] Executing ATTACK_PLAYER state");
                handleAttackPlayer(disaster, golem, current, plugin, config);
                break;
        }
        
        // Temel hareket (BaseCreatureHandler'dan) - Blok kırma ve diğer özellikler
        super.handle(disaster, entity, config);
        
        // ✅ ÖZEL YETENEKLER: Zıplama-Patlama, Blok Fırlatma (Titan Golem özel)
        useTitanAbilities(disaster, golem, current, config);
    }
    
    /**
     * GO_CENTER durumu: Merkeze gitme mantığı
     */
    private void handleGoCenter(Disaster disaster, IronGolem golem, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[TitanGolemHandler] handleGoCenter() - hasArrivedCenter: " + disaster.hasArrivedCenter());
        
        // ✅ hasArrivedCenter kontrolü: Eğer merkeze ulaştıysa, bir daha merkeze gitme
        if (disaster.hasArrivedCenter()) {
            plugin.getLogger().info("[TitanGolemHandler] Already arrived at center, updating state");
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
            plugin.getLogger().info("[TitanGolemHandler] Target set to center: " + 
                (target != null ? target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ() : "null"));
        }
        
        // Merkeze ulaşma kontrolü (50 blok yarıçap)
        if (target != null && current.getWorld().equals(target.getWorld())) {
            double distanceToCenter = current.distance(target);
            plugin.getLogger().fine("[TitanGolemHandler] Distance to center: " + String.format("%.2f", distanceToCenter));
            
            if (distanceToCenter <= 50.0) {
                // ✅ Merkeze ulaştı!
                plugin.getLogger().info("[TitanGolemHandler] REACHED CENTER! Distance: " + String.format("%.2f", distanceToCenter));
                disaster.setHasArrivedCenter(true);
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                updateStateAfterCenterReached(disaster, current, plugin);
                return;
            }
            
            // Merkeze doğru hareket et
            plugin.getLogger().fine("[TitanGolemHandler] Moving towards center");
            moveToTarget(golem, current, target, config);
        } else {
            plugin.getLogger().warning("[TitanGolemHandler] Target is null or different world!");
        }
    }
    
    /**
     * ATTACK_CLAN durumu: Klan kristallerine saldırma mantığı
     */
    private void handleAttackClan(Disaster disaster, IronGolem golem, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[TitanGolemHandler] handleAttackClan() called");
        
        Location targetCrystal = disaster.getTargetCrystal();
        plugin.getLogger().info("[TitanGolemHandler] Target crystal: " + (targetCrystal != null ? 
            targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ() : "null"));
        
        // Hedef kristal yoksa veya kırıldıysa yeni hedef bul
        if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
            plugin.getLogger().info("[TitanGolemHandler] Target crystal is null or destroyed, finding new target");
            
            // ✅ DÜZELTME: Merkeze ulaştıysa merkeze göre, değilse current'a göre kontrol
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
            plugin.getLogger().info("[TitanGolemHandler] Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                targetCrystal = nearbyCrystals.get(0);
                disaster.setTargetCrystal(targetCrystal);
                disaster.setTarget(targetCrystal);
                plugin.getLogger().info("[TitanGolemHandler] New target crystal set: " + 
                    targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
            } else {
                plugin.getLogger().info("[TitanGolemHandler] No crystals found, switching to ATTACK_PLAYER");
                disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
                handleAttackPlayer(disaster, golem, current, plugin, config);
                return;
            }
        }
        
        // Kristale doğru hareket et
        if (targetCrystal != null && current.getWorld().equals(targetCrystal.getWorld())) {
            double distanceToCrystal = current.distance(targetCrystal);
            plugin.getLogger().fine("[TitanGolemHandler] Distance to crystal: " + String.format("%.2f", distanceToCrystal));
            
            // Kristale yakınsa (5 blok), vur
            if (distanceToCrystal <= 5.0) {
                plugin.getLogger().info("[TitanGolemHandler] Attacking crystal at distance: " + String.format("%.2f", distanceToCrystal));
                boolean crystalDestroyed = attackCrystal(disaster, targetCrystal, plugin);
                
                if (crystalDestroyed) {
                    plugin.getLogger().info("[TitanGolemHandler] Crystal destroyed! Finding new target");
                    disaster.setTargetCrystal(null);
                    disaster.setTarget(null);
                    findNewTargetAfterCrystalDestroyed(disaster, current, plugin);
                } else {
                    plugin.getLogger().fine("[TitanGolemHandler] Crystal still alive");
                }
            } else {
                plugin.getLogger().fine("[TitanGolemHandler] Moving towards crystal");
                moveToTarget(golem, current, targetCrystal, config);
            }
        } else {
            plugin.getLogger().warning("[TitanGolemHandler] Target crystal is null or different world!");
        }
    }
    
    /**
     * ATTACK_PLAYER durumu: Oyuncuları kovalama mantığı
     */
    private void handleAttackPlayer(Disaster disaster, IronGolem golem, Location current, Main plugin, DisasterConfig config) {
        plugin.getLogger().info("[TitanGolemHandler] handleAttackPlayer() called");
        
        Player targetPlayer = disaster.getTargetPlayer();
        plugin.getLogger().info("[TitanGolemHandler] Target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        
        // Hedef oyuncu yoksa veya öldüyse yeni hedef bul
        if (targetPlayer == null || !targetPlayer.isOnline() || targetPlayer.isDead()) {
            plugin.getLogger().info("[TitanGolemHandler] Target player is null/offline/dead, finding new player");
            targetPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(targetPlayer);
            plugin.getLogger().info("[TitanGolemHandler] New target player: " + (targetPlayer != null ? targetPlayer.getName() : "null"));
        }
        
        // 20 saniyede bir klan kontrolü yap
        long now = System.currentTimeMillis();
        if (now - disaster.getLastClanCheckTime() >= 20000) {
            disaster.setLastClanCheckTime(now);
            
            // ✅ DÜZELTME: Merkeze ulaştıysa merkeze göre, değilse current'a göre kontrol
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
            plugin.getLogger().info("[TitanGolemHandler] Clan check: Found " + nearbyCrystals.size() + " crystals in radius " + searchRadius);
            
            if (!nearbyCrystals.isEmpty()) {
                plugin.getLogger().info("[TitanGolemHandler] Clan found! Switching to ATTACK_CLAN");
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
            plugin.getLogger().fine("[TitanGolemHandler] Moving towards player: " + targetPlayer.getName() + 
                " (distance: " + String.format("%.2f", distanceToPlayer) + ")");
            moveToTarget(golem, current, playerLoc, config);
        } else {
            plugin.getLogger().info("[TitanGolemHandler] Player not found or different world, finding nearest crystal");
            Location nearestCrystal = findNearestCrystal(plugin, current);
            if (nearestCrystal != null) {
                plugin.getLogger().info("[TitanGolemHandler] Found nearest crystal, switching to ATTACK_CLAN");
                disaster.setDisasterState(DisasterState.ATTACK_CLAN);
                disaster.setTargetCrystal(nearestCrystal);
                disaster.setTarget(nearestCrystal);
                disaster.setTargetPlayer(null);
            } else {
                plugin.getLogger().warning("[TitanGolemHandler] No player and no crystal found!");
            }
        }
    }
    
    /**
     * Merkeze ulaştıktan sonra durumu güncelle
     */
    private void updateStateAfterCenterReached(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[TitanGolemHandler] updateStateAfterCenterReached() called");
        
        Location centerLocation = getCenterLocation(plugin, current);
        if (centerLocation == null) {
            centerLocation = current;
        }
        plugin.getLogger().info("[TitanGolemHandler] Center location: " + 
            centerLocation.getBlockX() + "," + centerLocation.getBlockY() + "," + centerLocation.getBlockZ());
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
        plugin.getLogger().info("[TitanGolemHandler] Found " + nearbyCrystals.size() + " crystals within 1500 blocks of center");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[TitanGolemHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            plugin.getLogger().info("[TitanGolemHandler] Target crystal set: " + 
                targetCrystal.getBlockX() + "," + targetCrystal.getBlockY() + "," + targetCrystal.getBlockZ());
        } else {
            plugin.getLogger().info("[TitanGolemHandler] No crystals found, switching to ATTACK_PLAYER");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            Player nearestPlayer = findNearestPlayer(current);
            disaster.setTargetPlayer(nearestPlayer);
            if (nearestPlayer != null) {
                disaster.setTarget(nearestPlayer.getLocation());
                plugin.getLogger().info("[TitanGolemHandler] Target player set: " + nearestPlayer.getName());
            } else {
                plugin.getLogger().warning("[TitanGolemHandler] No player found!");
            }
        }
    }
    
    /**
     * Hedefe doğru hareket et
     */
    private void moveToTarget(IronGolem golem, Location current, Location target, DisasterConfig config) {
        if (target == null || !current.getWorld().equals(target.getWorld())) {
            Main.getInstance().getLogger().warning("[TitanGolemHandler] moveToTarget() - Target is null or different world!");
            return;
        }
        
        double distance = current.distance(target);
        Main.getInstance().getLogger().fine("[TitanGolemHandler] moveToTarget() - Distance: " + String.format("%.2f", distance));
        
        Vector direction = DisasterUtils.calculateDirection(current, target);
        double speed = config.getMoveSpeed();
        double adjustedSpeed = Math.max(speed, 0.3);
        Vector velocity = direction.multiply(adjustedSpeed);
        
        golem.setVelocity(velocity);
        DisasterBehavior.faceTarget(golem, target);
    }
    
    /**
     * Klan kristaline saldır
     */
    private boolean attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
        plugin.getLogger().info("[TitanGolemHandler] attackCrystal() called at " + 
            crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        
        if (plugin == null || plugin.getTerritoryManager() == null) {
            plugin.getLogger().warning("[TitanGolemHandler] attackCrystal() - Plugin or TerritoryManager is null!");
            return false;
        }
        
        Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
        if (targetClan == null) {
            plugin.getLogger().warning("[TitanGolemHandler] attackCrystal() - No clan found at crystal location!");
            return false;
        }
        
        plugin.getLogger().info("[TitanGolemHandler] Attacking crystal of clan: " + targetClan.getName());
        
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
            plugin.getLogger().info("[TitanGolemHandler] Crystal destroyed! Clan: " + targetClan.getName());
            return true;
        }
        
        plugin.getLogger().fine("[TitanGolemHandler] Crystal health: " + 
            String.format("%.2f", result.getCurrentHealth()) + "/" + 
            String.format("%.2f", result.getMaxHealth()));
        return false;
    }
    
    /**
     * Kristal yok edildikten sonra yeni hedef bul
     */
    private void findNewTargetAfterCrystalDestroyed(Disaster disaster, Location current, Main plugin) {
        plugin.getLogger().info("[TitanGolemHandler] findNewTargetAfterCrystalDestroyed() called");
        
        Location searchLocation;
        double searchRadius;
        if (disaster.hasArrivedCenter()) {
            searchLocation = getCenterLocation(plugin, current);
            if (searchLocation == null) {
                searchLocation = current;
            }
            searchRadius = 1500.0;
            plugin.getLogger().info("[TitanGolemHandler] Searching from center, radius: " + searchRadius);
        } else {
            searchLocation = current;
            searchRadius = 1000.0;
            plugin.getLogger().info("[TitanGolemHandler] Searching from current, radius: " + searchRadius);
        }
        
        List<Location> nearbyCrystals = findCrystalsInRadius(plugin, searchLocation, searchRadius);
        plugin.getLogger().info("[TitanGolemHandler] Found " + nearbyCrystals.size() + " crystals in radius");
        
        if (!nearbyCrystals.isEmpty()) {
            plugin.getLogger().info("[TitanGolemHandler] Switching to ATTACK_CLAN (crystals found)");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            Location targetCrystal = nearbyCrystals.get(0);
            disaster.setTargetCrystal(targetCrystal);
            disaster.setTarget(targetCrystal);
            disaster.setTargetPlayer(null);
            return;
        }
        
        Player nearestPlayer = findNearestPlayer(current);
        if (nearestPlayer != null) {
            plugin.getLogger().info("[TitanGolemHandler] Switching to ATTACK_PLAYER (player found: " + nearestPlayer.getName() + ")");
            disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
            disaster.setTargetPlayer(nearestPlayer);
            disaster.setTarget(nearestPlayer.getLocation());
            disaster.setTargetCrystal(null);
            return;
        }
        
        plugin.getLogger().info("[TitanGolemHandler] No nearby crystals or players, finding nearest crystal");
        Location nearestCrystal = findNearestCrystal(plugin, current);
        if (nearestCrystal != null) {
            plugin.getLogger().info("[TitanGolemHandler] Found nearest crystal, switching to ATTACK_CLAN");
            disaster.setDisasterState(DisasterState.ATTACK_CLAN);
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            disaster.setTargetPlayer(null);
        } else {
            plugin.getLogger().warning("[TitanGolemHandler] No crystals found at all!");
        }
    }
    
    /**
     * Titan Golem özel yetenekleri
     */
    private void useTitanAbilities(Disaster disaster, IronGolem golem, Location current, DisasterConfig config) {
        Main plugin = Main.getInstance();
        Vector direction = null;
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target != null && current.getWorld().equals(target.getWorld())) {
            direction = DisasterUtils.calculateDirection(current, target);
        }
        
        // Zıplama-Patlama Yeteneği
        int jumpInterval = random.nextInt(config.getJumpIntervalMax() - config.getJumpIntervalMin() + 1) + config.getJumpIntervalMin();
        if (tickCounter - lastJumpTick >= jumpInterval) {
            lastJumpTick = tickCounter;
            plugin.getLogger().info("[TitanGolemHandler] Using jump ability");
            
            if (direction != null) {
                Vector jumpVector = direction.clone().multiply(1.5).setY(config.getJumpHeight());
                golem.setVelocity(jumpVector);
                
                // Zıplama sonrası patlama (0.8 saniye sonra)
                final IronGolem finalGolem = golem;
                Bukkit.getScheduler().runTaskLater(
                    Main.getInstance(),
                    () -> {
                        if (finalGolem != null && !finalGolem.isDead() && finalGolem.isValid()) {
                            Location landLoc = finalGolem.getLocation();
                            DisasterUtils.createExplosion(landLoc, config.getExplosionPower(), true);
                            DisasterUtils.breakBlocks(landLoc, config.getBlockBreakRadius(), config, false);
                        }
                    },
                    16L
                );
            }
        }
        
        // Sıkışma kontrolü
        if (direction != null) {
            org.bukkit.block.Block frontBlock = current.clone().add(direction).getBlock();
            if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
                if (tickCounter % 20 == 0) {
                    Vector jumpVector = direction.clone().multiply(1.5).setY(config.getJumpHeight());
                    golem.setVelocity(jumpVector);
                }
            }
        }
        
        // Blok Fırlatma Yeteneği
        if (tickCounter % config.getBlockThrowInterval() == 0) {
            for (Player player : current.getWorld().getPlayers()) {
                if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getAttackRadius()) {
                    Location playerLoc = player.getLocation();
                    Vector throwDirection = DisasterUtils.calculateDirection(current, playerLoc);
                    
                    FallingBlock fallingBlock = current.getWorld().spawnFallingBlock(
                        current.clone().add(0, 3, 0),
                        Material.DIRT.createBlockData()
                    );
                    fallingBlock.setVelocity(throwDirection.multiply(1.2).setY(0.5));
                    fallingBlock.setHurtEntities(true);
                    fallingBlock.setDropItem(false);
                }
            }
        }
        
        // Pasif patlama
        if (tickCounter % config.getExplosionInterval() == 0) {
            DisasterUtils.createExplosion(current, config.getPassiveExplosionPower() * disaster.getDamageMultiplier(), false);
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
            Main.getInstance().getLogger().warning("[TitanGolemHandler] findNearestPlayer() - Location is null!");
            return null;
        }
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            Main.getInstance().getLogger().warning("[TitanGolemHandler] findNearestPlayer() - World is null!");
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
        
        Main.getInstance().getLogger().info("[TitanGolemHandler] findNearestPlayer() - Found: " + 
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
    
    @Override
    public void useSpecialAbilities(Disaster disaster, Entity entity, DisasterConfig config, me.mami.stratocraft.model.DisasterPhase phase) {
        if (!(entity instanceof IronGolem)) return;
        
        IronGolem golem = (IronGolem) entity;
        Location current = golem.getLocation();
        
        Main.getInstance().getLogger().info("[TitanGolemHandler] useSpecialAbilities() - Phase: " + phase);
        
        switch (phase) {
            case RAGE:
                if (tickCounter % (config.getBlockThrowInterval() / 2) == 0) {
                    throwBlocksAtPlayers(golem, current, config);
                }
                break;
            case DESPERATION:
                if (tickCounter % 40 == 0) {
                    DisasterUtils.createExplosion(current, config.getPassiveExplosionPower() * 1.5, true);
                }
                break;
            default:
                break;
        }
    }
    
    @Override
    public void changeEnvironment(Disaster disaster, Entity entity, DisasterConfig config) {
        if (!(entity instanceof IronGolem)) return;
        
        IronGolem golem = (IronGolem) entity;
        Location current = golem.getLocation();
        
        Main.getInstance().getLogger().fine("[TitanGolemHandler] changeEnvironment() called");
        
        // Çevre değişimi: Etrafındaki blokları obsidyene çevir
        if (tickCounter % 200 == 0) {
            int radius = 5;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        org.bukkit.block.Block block = current.clone().add(x, -1, z).getBlock();
                        if (block.getType() != Material.OBSIDIAN && 
                            block.getType() != Material.BEDROCK &&
                            block.getType() != Material.AIR) {
                            block.setType(Material.OBSIDIAN);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Oyunculara blok fırlat
     */
    private void throwBlocksAtPlayers(IronGolem golem, Location current, DisasterConfig config) {
        for (Player player : current.getWorld().getPlayers()) {
            if (DisasterUtils.calculateDistance(current, player.getLocation()) <= config.getAttackRadius()) {
                Location playerLoc = player.getLocation();
                Vector throwDirection = DisasterUtils.calculateDirection(current, playerLoc);
                
                FallingBlock fallingBlock = current.getWorld().spawnFallingBlock(
                    current.clone().add(0, 3, 0),
                    Material.DIRT.createBlockData()
                );
                fallingBlock.setVelocity(throwDirection.multiply(1.2).setY(0.5));
                fallingBlock.setHurtEntities(true);
                fallingBlock.setDropItem(false);
            }
        }
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        // Titan Golem tek boss, grup yok
    }
}
