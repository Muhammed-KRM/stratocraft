package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScavengerManager {
    private final Map<Location, UUID> wreckageLocations = new HashMap<>();

    public void markWreckage(Location loc, UUID disasterId) {
        wreckageLocations.put(loc, disasterId);
        // Enkaz bloğunu özel bir blokla işaretle
        loc.getBlock().setType(Material.ANCIENT_DEBRIS);
    }

    public boolean isWreckage(Location loc) {
        return wreckageLocations.containsKey(loc);
    }

    public void scavenge(Player p, Location loc) {
        if (!isWreckage(loc)) return;
        
        // Rastgele hurda eşyaları düşür
        p.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT, 3));
        wreckageLocations.remove(loc);
        loc.getBlock().setType(Material.AIR);
    }
}

