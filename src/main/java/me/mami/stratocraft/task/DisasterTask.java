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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DisasterTask extends BukkitRunnable {
    private final DisasterManager disasterManager;
    private final TerritoryManager territoryManager;

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
            Vector direction = target.toVector().subtract(current.toVector()).normalize();
            entity.setVelocity(direction.multiply(0.4));
            
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
            // Yüzeydeki oyuncuları yak, ahşap yapılar tutuşur
            for (Player p : Bukkit.getOnlinePlayers()) {
                Location playerLoc = p.getLocation();
                int highestY = p.getWorld().getHighestBlockYAt(playerLoc);
                
                // Oyuncu yüzeydeyse (üstünde blok yoksa)
                if (playerLoc.getBlockY() >= highestY - 1) {
                    p.setFireTicks(Math.max(p.getFireTicks(), 100)); // 5 saniye yanma
                }
                
                // Ahşap blokları yak
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        Block woodBlock = playerLoc.clone().add(x, 0, z).getBlock();
                        Material type = woodBlock.getType();
                        if (type == Material.OAK_PLANKS || type == Material.BIRCH_PLANKS || 
                            type == Material.SPRUCE_PLANKS || type == Material.JUNGLE_PLANKS ||
                            type == Material.ACACIA_PLANKS || type == Material.DARK_OAK_PLANKS ||
                            type == Material.OAK_LOG || type == Material.BIRCH_LOG) {
                            if (Math.random() < 0.1) { // %10 şans
                                woodBlock.setType(Material.FIRE);
                            }
                        }
                    }
                }
            }
        }
    }
}

