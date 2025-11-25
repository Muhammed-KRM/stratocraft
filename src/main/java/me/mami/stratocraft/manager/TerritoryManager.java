package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.util.GeometryUtil;
import org.bukkit.Location;
import java.util.UUID;

public class TerritoryManager {
    private final ClanManager clanManager;

    public TerritoryManager(ClanManager cm) { this.clanManager = cm; }

    public ClanManager getClanManager() { return clanManager; }

    public Clan getTerritoryOwner(Location loc) {
        for (Clan clan : clanManager.getAllClans()) {
            Territory t = clan.getTerritory();
            if (t == null) continue;

            if (GeometryUtil.isInsideRadius(t.getCenter(), loc, t.getRadius())) {
                return clan;
            }
        }
        return null;
    }

    public boolean isSafeZone(UUID playerId, Location loc) {
        Clan owner = getTerritoryOwner(loc);
        Clan playerClan = clanManager.getClanByPlayer(playerId);
        return owner != null && playerClan != null && owner.equals(playerClan);
    }
}

