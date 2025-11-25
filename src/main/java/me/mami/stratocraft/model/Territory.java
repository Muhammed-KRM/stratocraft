package me.mami.stratocraft.model;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Territory {
    private final UUID clanId;
    private final Location center;
    private int radius = 50;
    private final List<Location> outposts = new ArrayList<>();

    public Territory(UUID clanId, Location center) {
        this.clanId = clanId;
        this.center = center;
    }

    public Location getCenter() { return center; }
    public int getRadius() { return radius; }
    public void expand(int amount) { this.radius += amount; }
    public List<Location> getOutposts() { return outposts; }
    public void addOutpost(Location loc) { outposts.add(loc); }
    public UUID getClanId() { return clanId; }
}

