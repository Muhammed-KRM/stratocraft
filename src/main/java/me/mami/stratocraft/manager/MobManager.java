package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

public class MobManager {

    public void spawnHellDragon(Location loc, Player owner) {
        Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        dragon.setCustomName("§4Cehennem Ejderi");
        dragon.setSize(20);
        dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
    }

    public void spawnTerrorWorm(Location loc, Player owner) {
        Silverfish worm = (Silverfish) loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
        worm.setCustomName("§8Toprak Solucanı");
        worm.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
    }

    public void handleRiding(Player p) {
        Entity vehicle = p.getVehicle();
        if (vehicle == null) return;

        // TOPRAK SOLUCANI KONTROLÜ
        if (vehicle.getType() == EntityType.SILVERFISH && vehicle.getCustomName() != null && vehicle.getCustomName().contains("Solucan")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(0.5));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 5, false, false));
            
            ItemStack helmet = p.getInventory().getHelmet();
            if (!ItemManager.isCustomItem(helmet, "SONAR_GOGGLES")) {
                 p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                 p.sendMessage("§cÖnünü göremiyorsun! Sonar Gözlüğü lazım.");
            }
        }
        
        // EJDERHA KONTROLÜ
        if (vehicle.getType() == EntityType.PHANTOM && vehicle.getCustomName() != null && vehicle.getCustomName().contains("Ejder")) {
             vehicle.setVelocity(p.getLocation().getDirection().multiply(1.5));
        }
    }
}

