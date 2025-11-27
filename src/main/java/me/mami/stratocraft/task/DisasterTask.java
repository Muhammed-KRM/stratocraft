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
        if (disaster == null || disaster.isDead()) return;

        Entity entity = disaster.getEntity();
        if (entity == null) return;

        Location current = entity.getLocation();
        Location target = disaster.getTarget();
        
        // TITAN GOLEM
        if (disaster.getType() == Disaster.Type.TITAN_GOLEM && entity instanceof Giant) {
            Giant golem = (Giant) entity;
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
            entity.setVelocity(direction.multiply(0.4));
            
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
            Block frontBlock = current.clone().add(direction).getBlock();
            if (frontBlock.getType() != Material.AIR && frontBlock.getType() != Material.BEDROCK) {
                // Bu bölgede Tektonik Sabitleyici var mı kontrol et
                Clan owner = territoryManager.getTerritoryOwner(frontBlock.getLocation());
                if (owner != null) {
                    Structure stabilizer = owner.getStructures().stream()
                            .filter(s -> s.getType() == Structure.Type.TECTONIC_STABILIZER)
                            .findFirst().orElse(null);
                    
                    if (stabilizer != null && stabilizer.getLocation().distance(frontBlock.getLocation()) <= 50) {
                        // Tektonik Sabitleyici aktif - blok kırma iptal, yakıt tüket
                        if (stabilizer.getLevel() > 0) {
                            stabilizer.consumeFuel();
                            // Hasarı %90 azalt (sadece görsel efekt)
                            EffectUtil.playDisasterEffect(frontBlock.getLocation());
                            return; // Blok kırılmaz
                        }
                    }
                }
                
                // Normal blok kırma
                frontBlock.setType(Material.AIR);
                EffectUtil.playDisasterEffect(frontBlock.getLocation());
            }
        }
        
        // HİÇLİK SOLUCANI
        else if (disaster.getType() == Disaster.Type.ABYSSAL_WORM && entity instanceof Silverfish) {
            Vector direction = target.toVector().subtract(current.toVector()).normalize();
            entity.setVelocity(direction.multiply(0.3));
            
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
        }
        
        // GÜNEŞ FIRTINASI
        else if (disaster.getType() == Disaster.Type.SOLAR_FLARE) {
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

