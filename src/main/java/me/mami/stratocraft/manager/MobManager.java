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

    public void spawnWarBear(Location loc, Player owner) {
        org.bukkit.entity.PolarBear bear = (org.bukkit.entity.PolarBear) loc.getWorld().spawnEntity(loc, EntityType.POLAR_BEAR);
        bear.setCustomName("§7Savaş Ayısı");
        bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
        bear.setHealth(150.0);
        bear.setTamed(true);
        bear.setOwner(owner);
    }

    public void spawnShadowPanther(Location loc, Player owner) {
        org.bukkit.entity.Cat panther = (org.bukkit.entity.Cat) loc.getWorld().spawnEntity(loc, EntityType.CAT);
        panther.setCustomName("§8Gölge Panteri");
        panther.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(80.0);
        panther.setHealth(80.0);
        panther.setTamed(true);
        panther.setOwner(owner);
    }

    public void spawnWyvern(Location loc, Player owner) {
        Phantom wyvern = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
        wyvern.setCustomName("§bWyvern");
        wyvern.setSize(15);
        wyvern.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(250.0);
        wyvern.setHealth(250.0);
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

        // SAVAŞ AYISI KONTROLÜ
        if (vehicle.getType() == EntityType.POLAR_BEAR && vehicle.getCustomName() != null && vehicle.getCustomName().contains("Ayısı")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(0.3));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 2, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
        }

        // GÖLGE PANTERİ KONTROLÜ
        if (vehicle.getType() == EntityType.CAT && vehicle.getCustomName() != null && vehicle.getCustomName().contains("Panter")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(1.2));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
            // Gece görünmezlik
            if (p.getWorld().getTime() > 13000 || p.getWorld().getTime() < 23000) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
            }
        }

        // WYVERN KONTROLÜ (Kızıl Elmas besleme gerekli)
        if (vehicle.getType() == EntityType.PHANTOM && vehicle.getCustomName() != null && vehicle.getCustomName().contains("Wyvern")) {
            vehicle.setVelocity(p.getLocation().getDirection().multiply(2.0));
            // Her 30 saniyede bir Kızıl Elmas kontrolü yapılmalı (MobRideTask'ta)
        }
    }
}

