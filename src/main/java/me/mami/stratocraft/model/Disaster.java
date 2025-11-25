package me.mami.stratocraft.model;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

public class Disaster {
    public enum Type { TITAN_GOLEM, ABYSSAL_WORM, SOLAR_FLARE }
    
    private final Type type;
    private final Entity entity; // Bossun Minecraft Entity'si
    private Location target;
    private boolean isDead = false;

    public Disaster(Type type, Entity entity, Location target) {
        this.type = type;
        this.entity = entity;
        this.target = target;
    }

    public Type getType() { return type; }
    public Entity getEntity() { return entity; }
    public Location getTarget() { return target; }
    public void setTarget(Location target) { this.target = target; }
    public boolean isDead() { return isDead || entity.isDead(); }
    public void kill() { this.isDead = true; entity.remove(); }
}

