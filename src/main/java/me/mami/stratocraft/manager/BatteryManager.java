package me.mami.stratocraft.manager;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BatteryManager {

    // 1. ATEŞ TOPU (Geliştirilmiş)
    public void fireMagmaBattery(Player p, Material fuel, boolean boosted, boolean hasAmplifier) {
        int count;
        if (fuel == Material.DIAMOND) count = 5;
        else if (ItemManager.RED_DIAMOND != null && 
                 p.getInventory().getItemInMainHand().equals(ItemManager.RED_DIAMOND)) {
            count = 20;
        } else if (ItemManager.DARK_MATTER != null && 
                   p.getInventory().getItemInMainHand().equals(ItemManager.DARK_MATTER)) {
            count = 50;
        } else {
            count = 2;
        }
        
        if (boosted) count *= 2;
        
        float size = hasAmplifier ? 2.0f : 1.0f;
        
        for (int i = 0; i < count; i++) {
            Fireball fb = p.launchProjectile(Fireball.class);
            fb.setVelocity(p.getLocation().getDirection().multiply(1.5));
            if (hasAmplifier) {
                fb.setYield(fb.getYield() * size);
            }
        }
        p.sendMessage("§6Ateş topları fırlatıldı! (" + count + " adet)");
    }

    // 2. YILDIRIM
    public void fireLightningBattery(Player p) {
        Location target = p.getTargetBlock(null, 50).getLocation();
        p.getWorld().strikeLightning(target);
        p.sendMessage("§eYıldırım düştü!");
    }

    // 3. KARA DELİK
    public void fireBlackHole(Player p) {
        Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().createExplosion(target, 0F);
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, target, 1);
        for (Entity e : target.getWorld().getNearbyEntities(target, 15, 15, 15)) {
            if (e instanceof LivingEntity && e != p) {
                Vector dir = target.toVector().subtract(e.getLocation().toVector()).normalize().multiply(1.5);
                e.setVelocity(dir);
            }
        }
        p.sendMessage("§5Kara Delik aktif!");
    }

    // 4. ANLIK KÖPRÜ
    public void createInstantBridge(Player p) {
        Location start = p.getLocation().subtract(0, 1, 0);
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        for (int i = 1; i <= 15; i++) {
            Location point = start.clone().add(dir.clone().multiply(i));
            if (point.getBlock().getType() == Material.AIR) {
                point.getBlock().setType(Material.PACKED_ICE);
            }
        }
        p.sendMessage("§bBuz Köprüsü kuruldu!");
    }

    // 5. SIĞINAK KÜPÜ
    public void createInstantBunker(Player p) {
        Location center = p.getLocation();
        int r = 2;
        for (int x = -r; x <= r; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r || y == 3 || y == 0) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.AIR) b.setType(Material.COBBLESTONE);
                    }
                }
            }
        }
        p.teleport(center.add(0, 1, 0));
        p.sendMessage("§7Sığınak oluşturuldu!");
    }

    // 6. YERÇEKİMİ ÇAPASI (ANTI-AIR)
    public void fireGravityAnchor(Player p) {
        p.sendMessage("§5Yerçekimi Çapası Aktif!");
        for (Entity e : p.getNearbyEntities(50, 100, 50)) {
            if (e instanceof Player && ((Player) e).isGliding()) {
                e.setVelocity(new Vector(0, -3, 0));
                ((Player) e).setGliding(false);
                e.sendMessage("§c§lYERÇEKİMİ ÇAPASINA YAKALANDIN!");
            }
        }
    }

    // 7. TOPRAK SURU (Savunma)
    public void createEarthWall(Player p, Material material) {
        Location start = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        boolean isTitanium = ItemManager.TITANIUM_INGOT != null && 
                             ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "TITANIUM_INGOT");
        int height = isTitanium ? 5 : 3;
        Material wallMat = isTitanium ? Material.IRON_BLOCK : Material.COBBLESTONE;
        
        for (int y = 0; y < height; y++) {
            for (int x = -1; x <= 1; x++) {
                Location blockLoc = start.clone().add(x, y, 0);
                if (blockLoc.getBlock().getType() == Material.AIR) {
                    blockLoc.getBlock().setType(wallMat);
                }
            }
        }
        p.sendMessage("§7Toprak Suru oluşturuldu!");
    }

    // 8. MANYETİK BOZUCU (Utility)
    public void fireMagneticDisruptor(Player p) {
        p.sendMessage("§5Manyetik Bozucu Aktif!");
        for (Entity e : p.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                ItemStack mainHand = target.getInventory().getItemInMainHand();
                if (mainHand != null && mainHand.getType() != Material.AIR) {
                    target.getWorld().dropItemNaturally(target.getLocation(), mainHand.clone());
                    target.getInventory().setItemInMainHand(null);
                    target.sendMessage("§c§lSİLAHIN DÜŞTÜ!");
                }
            }
        }
    }

    // 9. SİSMİK ÇEKİÇ (Felaket Mücadele)
    public void fireSeismicHammer(Player p) {
        Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, target, 5);
        p.sendMessage("§6Sismik Çekiç Aktif! Yer altı titreşimleri gönderildi!");
        // Hiçlik Solucanı için titreşim sinyali
    }

    // 10. OZON KALKANI (Güneş Fırtınası Koruma)
    public void activateOzoneShield(Player p, Location center) {
        int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) {
                    Location loc = center.clone().add(x, 0, z);
                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setType(Material.BARRIER);
                        p.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 1);
                    }
                }
            }
        }
        p.sendMessage("§bOzon Kalkanı aktif! Güneş Fırtınası koruması sağlandı.");
    }

    // 11. ENERJİ DUVARI (Gelişmiş Savunma)
    public void createEnergyWall(Player p) {
        Location start = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        for (int y = 0; y < 5; y++) {
            for (int x = -2; x <= 2; x++) {
                Location loc = start.clone().add(x, y, 0);
                if (loc.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.BARRIER);
                    p.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, loc, 3);
                }
            }
        }
        p.sendMessage("§bEnerji Duvarı oluşturuldu!");
    }

    // 12. LAV HENDEKÇİSİ (Alan Savunması)
    public void createLavaTrench(Player p) {
        Location start = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(3));
        for (int i = 0; i < 10; i++) {
            Location loc = start.clone().add(i, -1, 0);
            if (loc.getBlock().getType() != Material.LAVA) {
                loc.getBlock().setType(Material.LAVA);
            }
        }
        p.sendMessage("§cLav Hendekçisi kuruldu!");
    }
}

