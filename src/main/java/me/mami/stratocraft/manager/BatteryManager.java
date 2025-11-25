package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class BatteryManager {

    // 1. ATEŞ TOPU
    public void fireMagmaBattery(Player p, Material fuel, boolean boosted) {
        int count = fuel == Material.DIAMOND ? 5 : 2;
        if (boosted) count *= 2;
        for (int i = 0; i < count; i++) {
            Fireball fb = p.launchProjectile(Fireball.class);
            fb.setVelocity(p.getLocation().getDirection().multiply(1.5));
        }
        p.sendMessage("§6Ateş topları fırlatıldı!");
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
}

