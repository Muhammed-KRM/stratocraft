package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;

import java.util.*;

public class ClanManager {
    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<UUID, UUID> playerClanMap = new HashMap<>();
    private TerritoryManager territoryManager; // Cache güncellemesi için

    public void setTerritoryManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    public Clan createClan(String name, UUID leader) {
        if (getClanByPlayer(leader) != null) return null;
        Clan c = new Clan(name, leader);
        clans.put(c.getId(), c);
        playerClanMap.put(leader, c.getId());
        // Cache'i güncelle
        if (territoryManager != null) {
            territoryManager.setCacheDirty();
        }
        return c;
    }

    public Clan getClanByPlayer(UUID uuid) {
        UUID clanId = playerClanMap.get(uuid);
        return clanId != null ? clans.get(clanId) : null;
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }
    
    /**
     * UUID ile klan getir
     */
    public Clan getClan(UUID clanId) {
        return clans.get(clanId);
    }
    
    /**
     * İsim ile klan getir
     */
    public Clan getClanByName(String name) {
        return clans.values().stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public void addMember(Clan clan, UUID memberId, Clan.Rank rank) {
        clan.addMember(memberId, rank);
        playerClanMap.put(memberId, clan.getId());
    }
    
    public void removeMember(Clan clan, UUID memberId) {
        clan.getMembers().remove(memberId);
        playerClanMap.remove(memberId);
    }

    // Not: hasRecipe metodu kaldırıldı - ResearchManager.hasRecipeBook kullanılmalı
    // Bu metod daha gelişmiş (Lectern kontrolü de var)

    public void disbandClan(Clan clan) {
        clan.getMembers().keySet().forEach(playerClanMap::remove);
        clans.remove(clan.getId());
        Bukkit.broadcastMessage("§c" + clan.getName() + " klanı dağıtıldı.");
        // Cache'i güncelle
        if (territoryManager != null) {
            territoryManager.setCacheDirty();
        }
    }
    
    // DataManager için
    public void loadClan(Clan clan) {
        clans.put(clan.getId(), clan);
        for (UUID memberId : clan.getMembers().keySet()) {
            playerClanMap.put(memberId, clan.getId());
        }
    }
}

