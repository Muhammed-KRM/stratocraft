package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ClanManager {
    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<UUID, UUID> playerClanMap = new HashMap<>();

    public Clan createClan(String name, UUID leader) {
        if (getClanByPlayer(leader) != null) return null;
        Clan c = new Clan(name, leader);
        clans.put(c.getId(), c);
        playerClanMap.put(leader, c.getId());
        return c;
    }

    public Clan getClanByPlayer(UUID uuid) {
        UUID clanId = playerClanMap.get(uuid);
        return clanId != null ? clans.get(clanId) : null;
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    public void addMember(Clan clan, UUID memberId, Clan.Rank rank) {
        clan.addMember(memberId, rank);
        playerClanMap.put(memberId, clan.getId());
    }

    public boolean hasRecipe(Player p, String recipeId) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && ItemManager.isCustomItem(item, "RECIPE_" + recipeId.toUpperCase())) return true;
        }
        return false;
    }

    public void disbandClan(Clan clan) {
        clan.getMembers().keySet().forEach(playerClanMap::remove);
        clans.remove(clan.getId());
        Bukkit.broadcastMessage("§c" + clan.getName() + " klanı dağıtıldı.");
    }
    
    // DataManager için
    public void loadClan(Clan clan) {
        clans.put(clan.getId(), clan);
        for (UUID memberId : clan.getMembers().keySet()) {
            playerClanMap.put(memberId, clan.getId());
        }
    }
}

