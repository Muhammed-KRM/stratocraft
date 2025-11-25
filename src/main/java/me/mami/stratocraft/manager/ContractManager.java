package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractManager {
    private final List<Contract> activeContracts = new ArrayList<>();
    private final ClanManager clanManager;

    public ContractManager(ClanManager cm) { this.clanManager = cm; }

    public void createContract(UUID issuer, Material mat, int amount, double reward) {
        activeContracts.add(new Contract(issuer, mat, amount, reward));
    }

    public List<Contract> getContracts() { return activeContracts; }

    public void punishBreach(UUID criminalId) {
        Player p = Bukkit.getPlayer(criminalId);
        if (p != null) {
            p.sendMessage("§4§lSÖZLEŞMEYİ İHLAL ETTİN! HAİN DAMGASI YEDİN.");
            p.setDisplayName("§4[HAİN] " + p.getName());
        }
        
        Clan clan = clanManager.getClanByPlayer(criminalId);
        if (clan != null) {
            double penalty = 10000;
            clan.withdraw(penalty);
        }
    }
}

