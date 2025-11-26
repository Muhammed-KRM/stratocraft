package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ScavengerManager {
    private final Map<Location, UUID> wreckageLocations = new HashMap<>();
    private final Random random = new Random();

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
        
        // Hurda teknolojisi: Antik Dişli, Piston düşer
        // Antik Dişli düşür (%30 şans)
        if (random.nextDouble() < 0.3) {
            // Antik Dişli için özel item (IRON_NUGGET kullanıyoruz)
            ItemStack ancientGear = new ItemStack(Material.IRON_NUGGET);
            org.bukkit.inventory.meta.ItemMeta meta = ancientGear.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7Antik Dişli");
                ancientGear.setItemMeta(meta);
            }
            p.getWorld().dropItemNaturally(loc, ancientGear);
        }
        
        // Piston düşür (%40 şans)
        if (random.nextDouble() < 0.4) {
            p.getWorld().dropItemNaturally(loc, new ItemStack(Material.PISTON, 1));
        }
        
        // Normal hurda
        p.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT, random.nextInt(3) + 1));
        
        wreckageLocations.remove(loc);
        loc.getBlock().setType(Material.AIR);
        p.sendMessage("§aHurda toplandı! Antik parçalar bulundu.");
    }
}

