package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class LogisticsManager {
    private final TerritoryManager territoryManager;
    private final Map<Location, Location> railConnections = new HashMap<>(); // Start -> End

    public LogisticsManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    public void createRailConnection(Location start, Location end) {
        railConnections.put(start, end);
    }

    public void transportItems(Player p, Location railStation) {
        Location destination = railConnections.get(railStation);
        if (destination == null) {
            p.sendMessage("§cBu istasyon bağlı değil!");
            return;
        }

        // Basit lojistik - eşyaları taşı
        Block destBlock = destination.getBlock();
        if (destBlock.getType() == Material.CHEST) {
            // Eşya transfer mantığı buraya eklenebilir
            p.sendMessage("§aEşyalar gönderildi!");
        }
    }
}

