package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaravanManager {
    private final Map<UUID, Entity> activeCaravans = new HashMap<>();

    public void createCaravan(Player owner, Location start, Location end) {
        Llama llama = start.getWorld().spawn(start, Llama.class);
        llama.setTamed(true);
        llama.setOwner(owner);
        activeCaravans.put(owner.getUniqueId(), llama);
        // Basit lojistik - daha gelişmiş özellikler eklenebilir
    }

    public Entity getCaravan(UUID playerId) {
        return activeCaravans.get(playerId);
    }
}

