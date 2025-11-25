package me.mami.stratocraft.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class EffectUtil {

    public static void playBatteryFire(Location loc) {
        loc.getWorld().spawnParticle(Particle.FLAME, loc.add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.5f);
    }

    public static void playDisasterEffect(Location loc) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.5f);
    }
}

