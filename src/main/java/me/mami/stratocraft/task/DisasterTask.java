package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Random;

public class DisasterTask extends BukkitRunnable {
    private final DisasterManager disasterManager;
    private final TerritoryManager territoryManager;
    private final Random random = new Random();
    private int titanGolemTickCounter = 0; // Titan Golem için tick sayacı
    private int lastJumpTick = 0; // Son zıplama zamanı

    public DisasterTask(DisasterManager dm, TerritoryManager tm) { 
        this.disasterManager = dm; 
        this.territoryManager = tm;
    }

    @Override
    public void run() {
        Disaster disaster = disasterManager.getActiveDisaster();
        if (disaster == null || disaster.isDead()) {
            // Doğa olayları için entity yok
            if (disaster != null && disaster.getCategory() == Disaster.Category.NATURAL) {
                handleNaturalDisaster(disaster);
            }
            return;
        }

        // Süre doldu mu kontrol et
        if (disaster.isExpired()) {
            disaster.kill();
            disasterManager.setActiveDisaster(null);
            Bukkit.broadcastMessage("§a§lFelaket süresi doldu!");
            return;
        }

        Entity entity = disaster.getEntity();
        
        // Canlı felaketler için entity kontrolü
        if (disaster.getCategory() == Disaster.Category.CREATURE) {
            if (entity == null || entity.isDead()) {
                disaster.kill();
                disasterManager.setActiveDisaster(null);
                return;
            }
            handleCreatureDisaster(disaster, entity);
        } else {
            // Doğa olayları
            handleNaturalDisaster(disaster);
        }
    }
    
    /**
     * Canlı felaketleri işle
     */
    private void handleCreatureDisaster(Disaster disaster, Entity entity) {
        Location current = entity.getLocation();
        Location target = disaster.getTarget();
        double damageMultiplier = disaster.getDamageMultiplier();
        
        // Chunk yüklü mü kontrol et, değilse yükle (entity hareket edebilsin diye)
        if (current.getWorld() != null) {
            int chunkX = current.getBlockX() >> 4;
            int chunkZ = current.getBlockZ() >> 4;
            if (!current.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                current.getWorld().getChunkAt(chunkX, chunkZ).load(true);
            }
        }
        
        // TITAN GOLEM
        if (disaster.getType() == Disaster.Type.TITAN_GOLEM && entity instanceof Giant) {
            handleTitanGolem(disaster, (Giant) entity, current, target, damageMultiplier);
        }
        
        // HİÇLİK SOLUCANI
        else if (disaster.getType() == Disaster.Type.ABYSSAL_WORM && entity instanceof Silverfish) {
            handleAbyssalWorm(disaster, (Silverfish) entity, current, target, damageMultiplier);
        }
        
        // KHAOS EJDERİ
        else if (disaster.getType() == Disaster.Type.CHAOS_DRAGON && entity instanceof org.bukkit.entity.EnderDragon) {
            handleChaosDragon(disaster, (org.bukkit.entity.EnderDragon) entity, current, target, damageMultiplier);
        }
        
        // BOŞLUK TİTANI
        else if (disaster.getType() == Disaster.Type.VOID_TITAN && entity instanceof org.bukkit.entity.Wither) {
            handleVoidTitan(disaster, (org.bukkit.entity.Wither) entity, current, target, damageMultiplier);
        }
    }
    
    /**
     * Titan Golem işle
     */
    private void handleTitanGolem(Disaster disaster, Giant golem, Location current, Location target, double damageMultiplier) {
            titanGolemTickCounter++;
            
            Vector direction = target.toVector().subtract(current.toVector()).normalize();
            
            // Zıplama-Patlama Yeteneği (Her 15-20 saniyede bir)
            if (titanGolemTickCounter - lastJumpTick >= (random.nextInt(100) + 300)) { // 15-20 saniye arası
                lastJumpTick = titanGolemTickCounter;
                
                // Yüksek zıplama: İleri ve yukarı doğru
                Vector jumpVector = direction.clone().multiply(1.5).setY(1.2);
                golem.setVelocity(jumpVector);
                
                // Zıplama sonrası patlama (0.8 saniye sonra)
                final Location jumpLocation = current.clone();
                final Giant finalGolem = golem;
                me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
                if (plugin != null) {
                    Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (finalGolem != null && !finalGolem.isDead() && finalGolem.isValid()) {
                                Location landLoc = finalGolem.getLocation();
                                // Düştüğü yerde patlama
                                finalGolem.getWorld().createExplosion(landLoc, 4.0f, false, true);
                                // Etrafındaki blokları yok et
                                for (int x = -3; x <= 3; x++) {
                                    for (int z = -3; z <= 3; z++) {
                                        for (int y = -1; y <= 2; y++) {
                                            Block block = landLoc.clone().add(x, y, z).getBlock();
                                            if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                                block.setType(Material.AIR);
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        16L // 0.8 saniye = 16 tick
                    );
                }
            }
            
            // Normal yürüme
            golem.setVelocity(direction.multiply(0.4));
            
            // Sıkışma kontrolü - önünde blok varsa zıpla
            Block frontBlock = current.clone().add(direction).getBlock();
            if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
                // Sıkışma önleme - zıplama
                if (titanGolemTickCounter % 20 == 0) { // Her saniye kontrol
                    Vector jumpVector = direction.clone().multiply(1.5).setY(1.5);
                    golem.setVelocity(jumpVector);
                }
            }
            
            // Blok Fırlatma Yeteneği (Her 10-15 saniyede bir)
            if (titanGolemTickCounter % (random.nextInt(100) + 200) == 0) { // 10-15 saniye arası
                // Etrafındaki oyunculara toprak blokları fırlat
                for (Player nearbyPlayer : golem.getWorld().getPlayers()) {
                    if (nearbyPlayer.getLocation().distance(current) <= 30) {
                        Location playerLoc = nearbyPlayer.getLocation();
                        Vector throwDirection = playerLoc.toVector().subtract(current.toVector()).normalize();
                        
                        // Toprak bloğu fırlat
                        FallingBlock fallingBlock = golem.getWorld().spawnFallingBlock(
                            current.clone().add(0, 3, 0),
                            Material.DIRT.createBlockData()
                        );
                        fallingBlock.setVelocity(throwDirection.multiply(1.2).setY(0.5));
                        fallingBlock.setHurtEntities(true);
                        fallingBlock.setDropItem(false);
                        
                        // Çarptığında hasar vermesi için kontrol (EntityChangeBlockEvent'te yakalanabilir)
                    }
                }
            }
            
            // Blok Yıkma - Tektonik Sabitleyici kontrolü
            Block frontBlockCheck = current.clone().add(direction).getBlock();
            if (frontBlockCheck.getType() != Material.AIR && frontBlockCheck.getType() != Material.BEDROCK) {
                // Bu bölgede Tektonik Sabitleyici var mı kontrol et
                Clan owner = territoryManager.getTerritoryOwner(frontBlockCheck.getLocation());
                if (owner != null) {
                    Structure stabilizer = owner.getStructures().stream()
                            .filter(s -> s.getType() == Structure.Type.TECTONIC_STABILIZER)
                            .findFirst().orElse(null);
                    
                    if (stabilizer != null && stabilizer.getLocation().distance(frontBlockCheck.getLocation()) <= 50) {
                        // Tektonik Sabitleyici aktif - blok kırma iptal, yakıt tüket
                        if (stabilizer.getLevel() > 0) {
                            stabilizer.consumeFuel();
                            EffectUtil.playDisasterEffect(frontBlockCheck.getLocation());
                            return; // Blok kırılmaz
                        }
                    }
                    
                    // Klan yok etme - yapıları yok et
                    destroyClanStructures(owner, current, damageMultiplier);
                }
                
                // Normal blok kırma
                frontBlockCheck.setType(Material.AIR);
                EffectUtil.playDisasterEffect(frontBlockCheck.getLocation());
            }
            
            // Pasif hasar - sürekli patlama
            if (titanGolemTickCounter % 200 == 0) { // Her 10 saniyede bir
                current.getWorld().createExplosion(current, (float)(2.0 * damageMultiplier), false, true);
            }
    }
    
    /**
     * Hiçlik Solucanı işle
     */
    private void handleAbyssalWorm(Disaster disaster, Silverfish worm, Location current, Location target, double damageMultiplier) {
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        worm.setVelocity(direction.multiply(0.3));
        
        // Temelleri (alt blokları) kaz
        Block belowBlock = current.clone().add(0, -1, 0).getBlock();
        if (belowBlock.getType() != Material.AIR && belowBlock.getType() != Material.BEDROCK) {
            belowBlock.setType(Material.AIR);
            EffectUtil.playDisasterEffect(belowBlock.getLocation());
        }
        
        // Önündeki bloğu da kır
        Block frontBlock = current.clone().add(direction).getBlock();
        if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
            frontBlock.setType(Material.AIR);
            EffectUtil.playDisasterEffect(frontBlock.getLocation());
        }
        
        // Sıkışma önleme - ışınlanma
        if (worm.getLocation().getBlock().getType() != Material.AIR) {
            Location teleportLoc = current.clone().add(direction.multiply(5));
            teleportLoc.setY(current.getWorld().getHighestBlockYAt(teleportLoc) + 1);
            worm.teleport(teleportLoc);
        }
    }
    
    /**
     * Khaos Ejderi işle
     */
    private void handleChaosDragon(Disaster disaster, org.bukkit.entity.EnderDragon dragon, Location current, Location target, double damageMultiplier) {
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        dragon.setVelocity(direction.multiply(0.5));
        
        // Ateş püskürtme
        if (random.nextInt(100) < 5) { // %5 şans
            for (Player player : current.getWorld().getPlayers()) {
                if (player.getLocation().distance(current) <= 50) {
                    Location playerLoc = player.getLocation();
                    playerLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, playerLoc, 20, 1, 1, 1, 0.1);
                    player.setFireTicks((int)(100 * damageMultiplier));
                    player.damage(5.0 * damageMultiplier, dragon);
                }
            }
        }
    }
    
    /**
     * Boşluk Titanı işle
     */
    private void handleVoidTitan(Disaster disaster, org.bukkit.entity.Wither wither, Location current, Location target, double damageMultiplier) {
        Vector direction = target.toVector().subtract(current.toVector()).normalize();
        wither.setVelocity(direction.multiply(0.3));
        
        // Boşluk patlaması
        if (random.nextInt(100) < 3) { // %3 şans
            Location explosionLoc = current.clone().add(
                (random.nextDouble() - 0.5) * 10,
                0,
                (random.nextDouble() - 0.5) * 10
            );
            explosionLoc.getWorld().createExplosion(explosionLoc, (float)(4.0 * damageMultiplier), false, true);
        }
    }
    
    /**
     * Klan yapılarını yok et
     */
    private void destroyClanStructures(Clan clan, Location disasterLoc, double damageMultiplier) {
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(disasterLoc) <= 20) {
                // Yapıyı yok et
                structure.getLocation().getBlock().setType(Material.AIR);
                EffectUtil.playDisasterEffect(structure.getLocation());
            }
        }
    }
    
    /**
     * Doğa olaylarını işle
     */
    private void handleNaturalDisaster(Disaster disaster) {
        if (disaster == null) return;
        
        // GÜNEŞ FIRTINASI
        if (disaster.getType() == Disaster.Type.SOLAR_FLARE) {
            // Yüzeydeki oyuncuları yak, ahşap yapılar ve ormanlar tutuşur
            for (Player p : Bukkit.getOnlinePlayers()) {
                Location playerLoc = p.getLocation();
                int highestY = p.getWorld().getHighestBlockYAt(playerLoc);
                
                // Oyuncu yüzeydeyse (üstünde blok yoksa)
                if (playerLoc.getBlockY() >= highestY - 1) {
                    p.setFireTicks(Math.max(p.getFireTicks(), 100)); // 5 saniye yanma
                }
                
                // Geniş alan tarama: 10x10 alan (oyuncu merkezli)
                for (int x = -5; x <= 5; x++) {
                    for (int z = -5; z <= 5; z++) {
                        for (int y = -2; y <= 5; y++) { // Yükseklik aralığı
                            Block targetBlock = playerLoc.clone().add(x, y, z).getBlock();
                            Material type = targetBlock.getType();
                            
                            // Klan bölgesi kontrolü - korumalı bölgelerde yakma
                            Clan owner = territoryManager.getTerritoryOwner(targetBlock.getLocation());
                            if (owner != null) {
                                continue; // Klan bölgesinde yakma
                            }
                            
                            // Gökyüzünü gören bloklar mı kontrol et
                            int blockHighestY = targetBlock.getWorld().getHighestBlockYAt(targetBlock.getLocation());
                            boolean canSeeSky = targetBlock.getY() >= blockHighestY - 1;
                            
                            if (!canSeeSky) continue; // Çatı altındaysa yakma
                            
                            // Yanıcı blokları yak
                            boolean isFlammable = 
                                // Ahşap planks
                                type == Material.OAK_PLANKS || type == Material.BIRCH_PLANKS || 
                                type == Material.SPRUCE_PLANKS || type == Material.JUNGLE_PLANKS ||
                                type == Material.ACACIA_PLANKS || type == Material.DARK_OAK_PLANKS ||
                                // Ahşap loglar
                                type == Material.OAK_LOG || type == Material.BIRCH_LOG ||
                                type == Material.SPRUCE_LOG || type == Material.JUNGLE_LOG ||
                                type == Material.ACACIA_LOG || type == Material.DARK_OAK_LOG ||
                                // Yün bloklar
                                type == Material.WHITE_WOOL || type == Material.BLACK_WOOL ||
                                type == Material.RED_WOOL || type == Material.BLUE_WOOL ||
                                // Yapraklar
                                type == Material.OAK_LEAVES || type == Material.BIRCH_LEAVES ||
                                type == Material.SPRUCE_LEAVES || type == Material.JUNGLE_LEAVES ||
                                // Diğer yanıcılar
                                type == Material.BOOKSHELF || type == Material.CHEST ||
                                type == Material.TRAPPED_CHEST || type == Material.LECTERN;
                            
                            if (isFlammable) {
                                // Şansla yak (loglar daha dayanıklı)
                                double chance = (type.toString().contains("LOG")) ? 0.05 : 0.15; // Loglar %5, diğerleri %15
                                if (Math.random() < chance) {
                                    targetBlock.setType(Material.FIRE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

