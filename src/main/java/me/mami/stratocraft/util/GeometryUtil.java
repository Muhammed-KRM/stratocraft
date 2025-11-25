package me.mami.stratocraft.util;

import org.bukkit.Location;

public class GeometryUtil {

    public static boolean isInsideRadius(Location center, Location point, int radius) {
        if (!center.getWorld().equals(point.getWorld())) return false;
        return center.distanceSquared(point) <= (radius * radius);
    }
}

