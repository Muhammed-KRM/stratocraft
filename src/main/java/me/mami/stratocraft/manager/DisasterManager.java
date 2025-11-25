package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Disaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;

public class DisasterManager {
    private Disaster activeDisaster = null;
    private long lastDisasterTime = System.currentTimeMillis();

    public void triggerDisaster(Disaster.Type type) {
        World world = Bukkit.getWorlds().get(0); // Ana dünya
        Location spawnLoc = world.getSpawnLocation().add(5000, 0, 5000);
        
        if (type == Disaster.Type.TITAN_GOLEM) {
            Giant golem = (Giant) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.GIANT);
            golem.setCustomName("§4§lTITAN GOLEM");
            golem.setHealth(500.0);
            activeDisaster = new Disaster(type, golem, world.getSpawnLocation());
            Bukkit.broadcastMessage("§c§lUYARI! §4Titan Golem haritanın ucunda doğdu ve merkeze yürüyor!");
        } else if (type == Disaster.Type.ABYSSAL_WORM) {
            org.bukkit.entity.Silverfish worm = (org.bukkit.entity.Silverfish) spawnLoc.getWorld().spawnEntity(spawnLoc.add(0, -20, 0), EntityType.SILVERFISH);
            worm.setCustomName("§5§lHİÇLİK SOLUCANI");
            worm.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
            worm.setHealth(300.0);
            activeDisaster = new Disaster(type, worm, world.getSpawnLocation());
            Bukkit.broadcastMessage("§5§lUYARI! §dHiçlik Solucanı yer altından merkeze doğru ilerliyor!");
        } else if (type == Disaster.Type.SOLAR_FLARE) {
            activeDisaster = new Disaster(type, null, world.getSpawnLocation());
            Bukkit.broadcastMessage("§c§lUYARI! §6Güneş Fırtınası başladı! 10 dakika boyunca yüzeyde yanacaksınız!");
            // Güneş Fırtınası için özel task başlatılacak
        }
    }

    public void dropRewards(Disaster disaster) {
        if (disaster == null || disaster.getEntity() == null) return;
        Location loc = disaster.getEntity().getLocation();
        
        if (Math.random() < 0.5) {
            if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
            }
        } else {
            if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
            }
        }
        
        Bukkit.broadcastMessage("§a§lFelaket yok edildi! Ödüller düştü!");
    }

    public Disaster getActiveDisaster() { return activeDisaster; }
    public void setActiveDisaster(Disaster d) { this.activeDisaster = d; }
}

