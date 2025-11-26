package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractManager {
    private final List<Contract> activeContracts = new ArrayList<>();
    private final ClanManager clanManager;
    private Team traitorTeam;

    public ContractManager(ClanManager cm) { 
        this.clanManager = cm;
        initializeTraitorTeam();
    }
    
    private void initializeTraitorTeam() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            Scoreboard mainBoard = manager.getMainScoreboard();
            traitorTeam = mainBoard.getTeam("TRAITOR");
            if (traitorTeam == null) {
                traitorTeam = mainBoard.registerNewTeam("TRAITOR");
                traitorTeam.setColor(ChatColor.RED);
                traitorTeam.setPrefix("§4[HAİN] ");
            }
        }
    }

    public void createContract(UUID issuer, Material mat, int amount, double reward, long days) {
        activeContracts.add(new Contract(issuer, mat, amount, reward, days));
    }

    public List<Contract> getContracts() { return activeContracts; }

    public Contract getContract(UUID id) {
        return activeContracts.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public void acceptContract(UUID contractId, UUID acceptor) {
        Contract contract = getContract(contractId);
        if (contract != null && contract.getAcceptor() == null) {
            contract.setAcceptor(acceptor);
        }
    }

    public void deliverContract(UUID contractId, int amount) {
        Contract contract = getContract(contractId);
        if (contract != null && !contract.isCompleted()) {
            contract.addDelivered(amount);
            if (contract.isCompleted()) {
                Player acceptor = Bukkit.getPlayer(contract.getAcceptor());
                if (acceptor != null) {
                    Clan clan = clanManager.getClanByPlayer(contract.getIssuer());
                    if (clan != null) {
                        clan.withdraw(contract.getReward());
                        // Ödülü acceptor'a ver (basit versiyon)
                        acceptor.sendMessage("§aSözleşme tamamlandı! " + contract.getReward() + " altın kazandın!");
                    }
                }
            }
        }
    }

    public void checkExpiredContracts() {
        for (Contract contract : new ArrayList<>(activeContracts)) {
            if (contract.isExpired() && contract.getAcceptor() != null) {
                punishBreach(contract.getAcceptor());
                activeContracts.remove(contract);
            }
        }
    }

    public void punishBreach(UUID criminalId) {
        Player p = Bukkit.getPlayer(criminalId);
        if (p != null) {
            p.sendMessage("§4§lSÖZLEŞMEYİ İHLAL ETTİN! HAİN DAMGASI YEDİN.");
            p.setDisplayName("§4[HAİN] " + p.getName());
            
            // Scoreboard Team'e ekle (kırmızı isim görünür)
            if (traitorTeam != null) {
                traitorTeam.addEntry(p.getName());
            }
        }
        
        Clan clan = clanManager.getClanByPlayer(criminalId);
        if (clan != null) {
            double penalty = 10000;
            clan.withdraw(penalty);
        }
    }
}

