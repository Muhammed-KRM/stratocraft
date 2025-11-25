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
        }
    }

    public Disaster getActiveDisaster() { return activeDisaster; }
    public void setActiveDisaster(Disaster d) { this.activeDisaster = d; }
}

