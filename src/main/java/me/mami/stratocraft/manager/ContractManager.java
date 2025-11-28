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
    private final me.mami.stratocraft.Main plugin;
    private Team traitorTeam;

    public ContractManager(ClanManager cm) { 
        this.clanManager = cm;
        this.plugin = me.mami.stratocraft.Main.getInstance();
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
                if (acceptor != null && acceptor.isOnline()) {
                    Clan issuerClan = clanManager.getClanByPlayer(contract.getIssuer());
                    if (issuerClan != null) {
                        // Klan bankasından ödülü çek
                        if (issuerClan.getBalance() >= contract.getReward()) {
                            issuerClan.withdraw(contract.getReward());
                            
                            // Acceptor'a ödülü ver (Vault ekonomi sistemi)
                            if (plugin.getEconomy() != null) {
                                plugin.getEconomy().depositPlayer(acceptor, contract.getReward());
                                acceptor.sendMessage("§a§lSÖZLEŞME TAMAMLANDI!");
                                acceptor.sendMessage("§7Ödül: §e" + contract.getReward() + " altın");
                            } else {
                                // Vault yoksa mesaj gönder
                                acceptor.sendMessage("§a§lSÖZLEŞME TAMAMLANDI!");
                                acceptor.sendMessage("§7Ödül: §e" + contract.getReward() + " altın (Vault eksik, manuel ödeme gerekli)");
                            }
                            
                            // İssuer'a bildir
                            Player issuer = Bukkit.getPlayer(contract.getIssuer());
                            if (issuer != null && issuer.isOnline()) {
                                issuer.sendMessage("§7Sözleşmeniz tamamlandı! " + contract.getReward() + " altın ödendi.");
                            }
                        } else {
                            acceptor.sendMessage("§cSözleşme tamamlandı ama klan bankasında yeterli para yok!");
                        }
                    } else {
                        // İssuer'ın klanı yok, direkt ödül ver (Vault)
                        if (plugin.getEconomy() != null) {
                            plugin.getEconomy().depositPlayer(acceptor, contract.getReward());
                            acceptor.sendMessage("§a§lSÖZLEŞME TAMAMLANDI!");
                            acceptor.sendMessage("§7Ödül: §e" + contract.getReward() + " altın");
                        }
                    }
                } else {
                    // Oyuncu offline - veriyi kaydet, giriş yaptığında ödülü ver
                    Bukkit.getLogger().info("Sözleşme tamamlandı ama oyuncu offline: " + contract.getAcceptor());
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
        if (p != null && p.isOnline()) {
            p.sendMessage("§4§lSÖZLEŞMEYİ İHLAL ETTİN! HAİN DAMGASI YEDİN.");
            p.setDisplayName("§4[HAİN] " + p.getName());
            
            // Scoreboard Team'e ekle (kırmızı isim görünür)
            // UUID yerine isim kullanılıyor çünkü Scoreboard API isim gerektiriyor
            // Ancak veri kaydında UUID kullanılıyor (doğru)
            if (traitorTeam != null) {
                traitorTeam.addEntry(p.getName()); // Scoreboard için isim gerekli
            }
        } else {
            // Oyuncu offline - veriyi kaydet, giriş yaptığında cezayı ver
            // (Basit versiyon: Sadece log, gerçekte bir "pendingPunishments" listesi olmalı)
            Bukkit.getLogger().info("Hain damgası verilecek ama oyuncu offline: " + criminalId);
        }
        
        Clan clan = clanManager.getClanByPlayer(criminalId);
        if (clan != null) {
            double penalty = 10000;
            clan.withdraw(penalty);
        }
    }
    
    // DataManager için
    public void loadContract(Contract contract) {
        activeContracts.add(contract);
    }
    
    // ========== BOUNTY HUNTING (SUİKAST KONTRATLARI) ==========
    
    /**
     * Bir oyuncunun başına ödül koy (Bounty Contract)
     */
    public void createBountyContract(UUID issuer, UUID target, double reward) {
        // Bounty kontratı için özel bir Contract oluştur
        // Material = AIR (suikast kontratı için özel işaret), amount = 1 (tek hedef)
        Contract bounty = new Contract(issuer, org.bukkit.Material.AIR, 1, reward, 7); // 7 gün süre
        bounty.setTargetPlayer(target); // Hedef oyuncu UUID'si
        activeContracts.add(bounty);
    }
    
    /**
     * Hedef oyuncunun başındaki ödülü al
     */
    public Contract getBountyContract(UUID targetPlayer) {
        return activeContracts.stream()
            .filter(c -> c.getTargetPlayer() != null && c.getTargetPlayer().equals(targetPlayer))
            .filter(c -> !c.isCompleted())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Kontratı tamamla ve ödülü ver
     */
    public void completeBountyContract(Contract contract, UUID killer) {
        if (contract == null || contract.isCompleted()) return;
        
        contract.setCompleted(true);
        
        // Ödülü öde (EconomyManager kullan)
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getEconomyManager() != null) {
            plugin.getEconomyManager().depositPlayer(
                org.bukkit.Bukkit.getPlayer(killer), 
                contract.getReward()
            );
        }
        
        // Mesaj gönder
        Player killerPlayer = org.bukkit.Bukkit.getPlayer(killer);
        if (killerPlayer != null && killerPlayer.isOnline()) {
            Player targetPlayer = org.bukkit.Bukkit.getPlayer(contract.getTargetPlayer());
            String targetName = targetPlayer != null ? targetPlayer.getName() : "Bilinmeyen";
            
            killerPlayer.sendMessage("§a§l════════════════════════════");
            killerPlayer.sendMessage("§a§lKONTRAKT TAMAMLANDI!");
            killerPlayer.sendMessage("§eHedef: §c" + targetName);
            killerPlayer.sendMessage("§6Ödül: §a" + contract.getReward() + " Altın");
            killerPlayer.sendMessage("§a§l════════════════════════════");
        }
        
        // Kontratı listeden kaldır
        activeContracts.remove(contract);
    }
}

