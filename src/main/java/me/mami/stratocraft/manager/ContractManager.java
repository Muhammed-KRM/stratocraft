package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.mami.stratocraft.enums.ContractType;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;

public class ContractManager {
    private final List<Contract> activeContracts = new ArrayList<>();
    private final ClanManager clanManager;
    private final me.mami.stratocraft.Main plugin;
    private Team traitorTeam;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    // Kalıcı can kaybı takibi (UUID -> Kayıp can sayısı)
    private final Map<UUID, Integer> permanentHealthLoss = new ConcurrentHashMap<>();

    public ContractManager(ClanManager cm) { 
        this.clanManager = cm;
        this.plugin = me.mami.stratocraft.Main.getInstance();
        initializeTraitorTeam();
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private double getHealthRestorePerHeart() {
        return balanceConfig != null ? balanceConfig.getContractHealthRestorePerHeart() : 2.0;
    }
    
    private int getDefaultDays() {
        return balanceConfig != null ? balanceConfig.getContractDefaultDays() : 7;
    }
    
    private double getRewardMultiplier() {
        return balanceConfig != null ? balanceConfig.getContractRewardMultiplier() : 0.5;
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
        if (contract == null) return;
        
        // Çift taraflı kontrat kontrolü - playerId gerekli, bu metod tek taraflı kontratlar için
        if (contract.isBilateralContract()) {
            // Çift taraflı kontratlar deliverBilateralContract(UUID contractId, UUID playerId, int amount) ile teslim edilmeli
            return;
        }
        
        // Eski sistem (tek taraflı kontrat)
        if (!contract.isCompleted()) {
            contract.addDelivered(amount);
            if (contract.isCompleted()) {
                // KRİTİK: Kontrat tamamlandığında kan imzası canını geri ver (1 kalp = 2 can)
                if (contract.getAcceptor() != null) {
                    restorePermanentHealth(contract.getAcceptor(), 1); // Kan imzası için 1 kalp geri ver
                }
                
                Player acceptor = Bukkit.getPlayer(contract.getAcceptor());
                if (acceptor != null && acceptor.isOnline()) {
                    Clan issuerClan = clanManager.getClanByPlayer(contract.getIssuer());
                    if (issuerClan != null) {
                        // Klan bankasından ödülü çek
                        if (issuerClan.getBalance() >= contract.getReward()) {
                            issuerClan.withdraw(contract.getReward());
                            
                            // Acceptor'a ödülü ver (EconomyManager)
                            if (plugin.getEconomyManager() != null) {
                                plugin.getEconomyManager().depositPlayer(acceptor, contract.getReward());
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
                        // İssuer'ın klanı yok, direkt ödül ver (EconomyManager)
                        if (plugin.getEconomyManager() != null) {
                            plugin.getEconomyManager().depositPlayer(acceptor, contract.getReward());
                            acceptor.sendMessage("§a§lSÖZLEŞME TAMAMLANDI!");
                            acceptor.sendMessage("§7Ödül: §e" + contract.getReward() + " altın");
                        }
                    }
                } else {
                    // Oyuncu offline - veriyi kaydet, giriş yaptığında ödülü ver
                    Bukkit.getLogger().info("Sözleşme tamamlandı ama oyuncu offline: " + contract.getAcceptor());
                }
                
                // Kontrat geçmişine ekle
                addToContractHistory(contract);
            }
        }
    }
    
    /**
     * Çift taraflı kontrat teslim etme (YENİ)
     * @deprecated Bu metod kullanılmıyor, deliverBilateralContract(UUID, UUID, int) kullan
     */
    @Deprecated
    private void deliverBilateralContract(Contract contract, int amount) {
        // Bu metod artık kullanılmıyor
    }
    
    /**
     * Çift taraflı kontrat teslim etme (Oyuncu ID ile)
     */
    public void deliverBilateralContract(UUID contractId, UUID playerId, int amount) {
        Contract contract = getContract(contractId);
        if (contract == null || !contract.isBilateralContract()) return;
        
        me.mami.stratocraft.model.ContractTerms termsA = contract.getTermsA();
        me.mami.stratocraft.model.ContractTerms termsB = contract.getTermsB();
        
        if (termsA == null || termsB == null) return;
        
        // Hangi oyuncu teslim ediyor?
        me.mami.stratocraft.model.ContractTerms playerTerms = null;
        me.mami.stratocraft.model.ContractTerms otherTerms = null;
        
        if (termsA.getPlayerId() != null && termsA.getPlayerId().equals(playerId)) {
            playerTerms = termsA;
            otherTerms = termsB;
        } else if (termsB.getPlayerId() != null && termsB.getPlayerId().equals(playerId)) {
            playerTerms = termsB;
            otherTerms = termsA;
        } else {
            // Bu oyuncu bu kontratın tarafı değil
            plugin.getLogger().warning("Oyuncu " + playerId + " kontrat " + contractId + " için teslim yapmaya çalıştı ama kontratın tarafı değil!");
            return;
        }
        
        if (playerTerms == null) {
            plugin.getLogger().warning("PlayerTerms null! Kontrat ID: " + contractId);
            return;
        }
        
        // RESOURCE_COLLECTION için malzeme teslimi
        if (playerTerms.getType() != null && playerTerms.getType() == me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION) {
            playerTerms.addDelivered(amount);
            
            // Şart tamamlandı mı?
            if (playerTerms.isCompleted()) {
                playerTerms.setCompleted(true);
                
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§a§lŞARTINIZ TAMAMLANDI!");
                    player.sendMessage("§7" + amount + "x " + 
                        (playerTerms.getMaterial() != null ? playerTerms.getMaterial().name() : "Malzeme") + 
                        " teslim edildi");
                }
            }
        }
        
        // Her iki şart da tamamlandı mı?
        if (termsA != null && termsB != null && termsA.isCompleted() && termsB.isCompleted()) {
            completeBilateralContract(contract);
        }
    }
    
    /**
     * Çift taraflı kontrat tamamlama
     */
    private void completeBilateralContract(Contract contract) {
        if (contract == null || !contract.isBilateralContract()) return;
        
        me.mami.stratocraft.model.ContractTerms termsA = contract.getTermsA();
        me.mami.stratocraft.model.ContractTerms termsB = contract.getTermsB();
        
        if (termsA == null || termsB == null) return;
        
        contract.setContractStatus(Contract.ContractStatus.COMPLETED);
        contract.setCompletedAt(System.currentTimeMillis());
        
        // Her iki oyuncuya da kan imzası geri ver
        UUID playerAId = contract.getPlayerA();
        UUID playerBId = contract.getPlayerB();
        
        if (playerAId == null || playerBId == null) {
            plugin.getLogger().warning("Çift taraflı kontrat tamamlandı ama oyuncu ID'leri null! Kontrat ID: " + contract.getId());
            return;
        }
        
        Player playerA = Bukkit.getPlayer(playerAId);
        Player playerB = Bukkit.getPlayer(playerBId);
        
        if (playerA != null && playerA.isOnline()) {
            restorePermanentHealth(playerAId, 1);
            playerA.sendMessage("§6═══════════════════════════════════");
            playerA.sendMessage("§a§lKONTRAT TAMAMLANDI!");
            playerA.sendMessage("§7Kalp geri verildi (+1 kalp)");
        }
        
        if (playerB != null && playerB.isOnline()) {
            restorePermanentHealth(playerBId, 1);
            playerB.sendMessage("§6═══════════════════════════════════");
            playerB.sendMessage("§a§lKONTRAT TAMAMLANDI!");
            playerB.sendMessage("§7Kalp geri verildi (+1 kalp)");
        }
        
        // Ödül ödemeleri
        // Oyuncu B'nin ödülü (termsA'dan) → Oyuncu A'nın klan bankasından
        if (clanManager != null) {
            Clan clanA = clanManager.getClanByPlayer(playerAId);
            if (clanA != null && clanA.getBalance() >= termsA.getReward()) {
                clanA.withdraw(termsA.getReward());
                if (plugin.getEconomyManager() != null && playerB != null && playerB.isOnline()) {
                    plugin.getEconomyManager().depositPlayer(playerB, termsA.getReward());
                    playerB.sendMessage("§7Ödül: §a" + termsA.getReward() + " altın");
                }
            }
            
            // Oyuncu A'nın ödülü (termsB'den) → Oyuncu B'nin klan bankasından
            Clan clanB = clanManager.getClanByPlayer(playerBId);
            if (clanB != null && clanB.getBalance() >= termsB.getReward()) {
                clanB.withdraw(termsB.getReward());
                if (plugin.getEconomyManager() != null && playerA != null && playerA.isOnline()) {
                    plugin.getEconomyManager().depositPlayer(playerA, termsB.getReward());
                    playerA.sendMessage("§7Ödül: §a" + termsB.getReward() + " altın");
                }
            }
        }
        
        // Kontrat geçmişine ekle
        addToContractHistory(contract);
    }

    public void checkExpiredContracts() {
        for (Contract contract : new ArrayList<>(activeContracts)) {
            // Çift taraflı kontrat kontrolü
            if (contract.isBilateralContract()) {
                me.mami.stratocraft.model.ContractTerms termsA = contract.getTermsA();
                me.mami.stratocraft.model.ContractTerms termsB = contract.getTermsB();
                
                if (termsA != null && termsB != null) {
                    // Her iki şartı da kontrol et
                    if (termsA.isBreached() || termsB.isBreached()) {
                        // İhlal eden oyuncuyu bul
                        UUID breacher = termsA.isBreached() && termsA.getPlayerId() != null ? 
                            termsA.getPlayerId() : 
                            (termsB.getPlayerId() != null ? termsB.getPlayerId() : null);
                        
                        if (breacher != null) {
                            breachBilateralContract(contract, breacher);
                            activeContracts.remove(contract);
                        } else {
                            plugin.getLogger().warning("Çift taraflı kontrat ihlal edildi ama breacher ID bulunamadı! Kontrat ID: " + contract.getId());
                        }
                    }
                } else {
                    plugin.getLogger().warning("Çift taraflı kontrat kontrol edilirken şartlar null! Kontrat ID: " + contract.getId());
                }
                continue;
            }
            
            // Eski sistem (tek taraflı kontrat)
            if (contract.isExpired() && contract.getAcceptor() != null) {
                // Kontrat geçmişine ekle
                addToContractHistory(contract);
                punishBreach(contract.getAcceptor());
                activeContracts.remove(contract);
            }
        }
    }
    
    /**
     * Çift taraflı kontrat ihlal etme
     */
    private void breachBilateralContract(Contract contract, UUID breacher) {
        if (contract == null || !contract.isBilateralContract() || breacher == null) return;
        
        contract.setContractStatus(Contract.ContractStatus.BREACHED);
        contract.setBreached(true);
        contract.setBreachedAt(System.currentTimeMillis());
        contract.setBreacher(breacher);
        
        // Sadece ihlal eden kişi ceza alır
        me.mami.stratocraft.model.ContractTerms termsA = contract.getTermsA();
        me.mami.stratocraft.model.ContractTerms termsB = contract.getTermsB();
        
        if (termsA == null || termsB == null) {
            plugin.getLogger().warning("Çift taraflı kontrat ihlal edildi ama şartlar null! Kontrat ID: " + contract.getId());
            return;
        }
        
        me.mami.stratocraft.model.ContractTerms breacherTerms = null;
        
        if (termsA.getPlayerId() != null && termsA.getPlayerId().equals(breacher)) {
            breacherTerms = termsA;
        } else if (termsB.getPlayerId() != null && termsB.getPlayerId().equals(breacher)) {
            breacherTerms = termsB;
        }
        
        if (breacherTerms != null) {
            // Ceza uygula
            if (clanManager != null) {
                Clan breacherClan = clanManager.getClanByPlayer(breacher);
                if (breacherClan != null && breacherClan.getBalance() >= breacherTerms.getPenalty()) {
                    breacherClan.withdraw(breacherTerms.getPenalty());
                }
            }
            
            Player breacherPlayer = Bukkit.getPlayer(breacher);
            if (breacherPlayer != null && breacherPlayer.isOnline()) {
                breacherPlayer.sendMessage("§6═══════════════════════════════════");
                breacherPlayer.sendMessage("§c§lKONTRAT İHLAL EDİLDİ!");
                breacherPlayer.sendMessage("§7Her iki kontrat da bitti");
                breacherPlayer.sendMessage("§7Ceza: §c" + breacherTerms.getPenalty() + " altın");
                breacherPlayer.sendMessage("§6═══════════════════════════════════");
            }
            
            // Diğer oyuncuya bildirim
            UUID playerA = contract.getPlayerA();
            UUID playerB = contract.getPlayerB();
            UUID otherPlayerId = null;
            
            if (playerA != null && playerA.equals(breacher)) {
                otherPlayerId = playerB;
            } else if (playerB != null && playerB.equals(breacher)) {
                otherPlayerId = playerA;
            }
            
            if (otherPlayerId != null) {
                Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
                if (otherPlayer != null && otherPlayer.isOnline()) {
                    otherPlayer.sendMessage("§6═══════════════════════════════════");
                    otherPlayer.sendMessage("§eKontrat İhlal Edildi");
                    otherPlayer.sendMessage("§7Karşı taraf kontratı ihlal etti");
                    otherPlayer.sendMessage("§7Kontrat sona erdi");
                    otherPlayer.sendMessage("§6═══════════════════════════════════");
                }
            }
        }
        
        // Kontrat geçmişine ekle
        addToContractHistory(contract);
    }

    @Deprecated
    public void punishBreach(UUID criminalId) {
        // Eski metod - breachContract kullanılmalı
        Contract dummyContract = new Contract(criminalId, Contract.ContractType.MATERIAL_DELIVERY, 
            Contract.ContractScope.PLAYER_TO_PLAYER, 0, 10000, 1);
        breachContract(dummyContract, criminalId, "Sözleşme süresi doldu");
    }
    
    /**
     * Sözleşme İhlali - Ciddi Ceza
     */
    public void breachContract(Contract contract, UUID violator, String reason) {
        if (contract.isBreached()) return; // Zaten ihlal edilmiş
        
        contract.setBreached(true);
        
        Player violatorPlayer = Bukkit.getPlayer(violator);
        if (violatorPlayer != null && violatorPlayer.isOnline()) {
            // 1. Kalıcı 2 Can Kaybı
            applyPermanentHealthLoss(violatorPlayer, 2);
            
            // 2. Hain Damgası
            applyTraitorTag(violatorPlayer);
            
            // 3. Para Cezası
            applyPenalty(violator, contract.getPenalty());
            
            // 4. Mesaj
            violatorPlayer.sendMessage("§4§l════════════════════════════");
            violatorPlayer.sendMessage("§4§lSÖZLEŞME İHLAL EDİLDİ!");
            violatorPlayer.sendMessage("§cSebep: §7" + reason);
            violatorPlayer.sendMessage("§cCeza: §7-2 Kalıcı Can, Hain Damgası");
            violatorPlayer.sendMessage("§4§l════════════════════════════");
        } else {
            // Oyuncu offline - veriyi kaydet
            permanentHealthLoss.put(violator, permanentHealthLoss.getOrDefault(violator, 0) + 2);
            // Giriş yaptığında cezayı uygula (PlayerJoinEvent'te)
        }
        
        // İssuer'a bildir
        Player issuer = Bukkit.getPlayer(contract.getIssuer());
        if (issuer != null && issuer.isOnline()) {
            issuer.sendMessage("§cSözleşmeniz ihlal edildi! " + 
                (violatorPlayer != null ? violatorPlayer.getName() : "Bilinmeyen"));
        }
    }
    
    /**
     * Kalıcı Can Kaybı Uygula
     */
    private void applyPermanentHealthLoss(Player player, int hearts) {
        // Mevcut kayıp can sayısını al
        int currentLoss = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
        int newLoss = currentLoss + hearts;
        permanentHealthLoss.put(player.getUniqueId(), newLoss);
        
        // Maksimum canı düşür
        org.bukkit.attribute.AttributeInstance maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            double currentMax = maxHealthAttr.getBaseValue();
            double healthPerHeart = getHealthRestorePerHeart();
            double newMax = Math.max(1.0, currentMax - (hearts * healthPerHeart)); // Config'den
            maxHealthAttr.setBaseValue(newMax);
            
            // Eğer mevcut can yeni maksimumdan fazlaysa, düşür
            if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
        }
        
        // DataManager'a kaydet (kalıcı)
        // DataManager.savePlayerHealthLoss(player.getUniqueId(), newLoss);
    }
    
    /**
     * Hain Damgası Uygula
     */
    private void applyTraitorTag(Player player) {
        // Scoreboard Team'e ekle
        if (traitorTeam != null) {
            traitorTeam.addEntry(player.getName());
        }
        
        // Display name'i değiştir
        player.setDisplayName("§4[HAİN] " + player.getName());
        
        // DataManager'a kaydet (kalıcı)
        // DataManager.saveTraitorTag(player.getUniqueId(), true);
    }
    
    /**
     * Para Cezası Uygula
     */
    private void applyPenalty(UUID playerId, double amount) {
        // EconomyManager kullan
        if (plugin != null && plugin.getEconomyManager() != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                plugin.getEconomyManager().withdrawPlayer(player, amount);
            }
        } else {
            // Klan bankasından çek
            Clan clan = clanManager.getClanByPlayer(playerId);
            if (clan != null) {
                clan.withdraw(amount);
            }
        }
    }
    
    /**
     * KRİTİK: Kalıcı can kaybını geri ver (kontrat tamamlandığında veya iptal edildiğinde)
     */
    public void restorePermanentHealth(UUID playerId, int hearts) {
        int currentLoss = permanentHealthLoss.getOrDefault(playerId, 0);
        if (currentLoss <= 0) return; // Zaten can kaybı yok
        
        int newLoss = Math.max(0, currentLoss - hearts);
        permanentHealthLoss.put(playerId, newLoss);
        
        // Oyuncu online ise canı geri ver
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            org.bukkit.attribute.AttributeInstance maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                double baseMax = 20.0; // Varsayılan maksimum can
                double newMax = Math.max(1.0, baseMax - (newLoss * 2.0)); // Her kalp = 2 can
                maxHealthAttr.setBaseValue(newMax);
                
                // Canı yeni maksimuma ayarla (eğer düşükse)
                if (player.getHealth() < newMax) {
                    player.setHealth(Math.min(player.getHealth() + (hearts * 2.0), newMax));
                }
            }
        }
    }
    
    /**
     * Oyuncu giriş yaptığında kalıcı cezaları uygula
     */
    public void onPlayerJoin(Player player) {
        // Kalıcı can kaybı
        int healthLoss = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
        if (healthLoss > 0) {
            // Mevcut kaybı uygula (hearts parametresi 0 değil, mevcut kayıp kadar)
            int currentLoss = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
            if (currentLoss > 0) {
                // Maksimum canı düşür
                org.bukkit.attribute.AttributeInstance maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double baseMax = 20.0; // Varsayılan maksimum can
                    double newMax = Math.max(1.0, baseMax - (currentLoss * 2.0)); // Her kalp = 2 can
                    maxHealthAttr.setBaseValue(newMax);
                    
                    // Eğer mevcut can yeni maksimumdan fazlaysa, düşür
                    if (player.getHealth() > newMax) {
                        player.setHealth(newMax);
                    }
                }
            }
        }
        
        // Hain damgası
        if (isTraitor(player.getUniqueId())) {
            applyTraitorTag(player);
        }
    }
    
    /**
     * Oyuncu hain mi?
     */
    public boolean isTraitor(UUID playerId) {
        // Scoreboard'dan kontrol et
        if (traitorTeam != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                return traitorTeam.hasEntry(player.getName());
            }
        }
        return false;
    }
    
    // DataManager için
    public void loadContract(Contract contract) {
        activeContracts.add(contract);
    }
    
    // ========== BOUNTY HUNTING (SUİKAST KONTRATLARI) ==========
    
    /**
     * Bir oyuncunun başına ödül koy (Bounty Contract) (YENİ: ContractType enum kullanır)
     */
    public void createBountyContract(UUID issuer, UUID target, double reward) {
        double rewardMultiplier = getRewardMultiplier();
        int defaultDays = getDefaultDays();
        me.mami.stratocraft.enums.PenaltyType penaltyType = me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY; // Varsayılan
        Contract bounty = new Contract(issuer, me.mami.stratocraft.enums.ContractType.COMBAT, 
            Contract.ContractScope.PLAYER_TO_PLAYER, reward, penaltyType, defaultDays);
        bounty.setTargetPlayer(target); // Hedef oyuncu UUID'si
        activeContracts.add(bounty);
    }
    
    /**
     * Yeni kontrat oluştur (YENİ: ContractType ve PenaltyType enum kullanır)
     */
    public void createContract(UUID issuer, me.mami.stratocraft.enums.ContractType type, Contract.ContractScope scope,
                              double reward, me.mami.stratocraft.enums.PenaltyType penaltyType, long deadlineDays) {
        Contract contract = new Contract(issuer, type, scope, reward, penaltyType, deadlineDays);
        activeContracts.add(contract);
    }
    
    /**
     * Yeni kontrat oluştur (GERİYE UYUMLULUK: eski Contract.ContractType enum kullanır)
     * @deprecated ContractType ve PenaltyType kullanın
     */
    @Deprecated
    public void createContract(UUID issuer, Contract.ContractType type, Contract.ContractScope scope,
                              double reward, double penalty, long deadlineDays) {
        Contract contract = new Contract(issuer, type, scope, reward, penalty, deadlineDays);
        activeContracts.add(contract);
    }
    
    /**
     * ContractType'ı eski Contract.ContractType'a dönüştür (geriye uyumluluk için)
     */
    private Contract.ContractType convertToOldContractType(ContractType type) {
        if (type == null) return null;
        try {
            return Contract.ContractType.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            // Yeni enum değerlerini eski enum'a map et
            switch (type) {
                case RESOURCE_COLLECTION: return Contract.ContractType.MATERIAL_DELIVERY;
                case COMBAT: return Contract.ContractType.PLAYER_KILL;
                case TERRITORY: return Contract.ContractType.BASE_PROTECTION;
                case CONSTRUCTION: return Contract.ContractType.STRUCTURE_BUILD;
                default: return Contract.ContractType.MATERIAL_DELIVERY;
            }
        }
    }
    
    /**
     * Oyuncunun aktif kontratlarını al
     */
    public List<Contract> getPlayerContracts(UUID playerId) {
        return activeContracts.stream()
            .filter(c -> c.getAcceptor() != null && c.getAcceptor().equals(playerId))
            .filter(c -> !c.isCompleted() && !c.isBreached())
            .collect(Collectors.toList());
    }
    
    /**
     * Saldırmama anlaşması var mı? (YENİ: ContractType enum kullanır)
     */
    public Contract getNonAggressionContract(UUID player1, UUID player2) {
        return activeContracts.stream()
            .filter(c -> {
                me.mami.stratocraft.enums.ContractType contractType = c.getContractType();
                // Saldırmama anlaşması COMBAT tipinde olabilir (saldırmama = combat kontratı)
                return contractType != null && contractType == me.mami.stratocraft.enums.ContractType.COMBAT;
            })
            .filter(c -> c.getAcceptor() != null && c.getAcceptor().equals(player1))
            .filter(c -> c.getNonAggressionTarget() != null && c.getNonAggressionTarget().equals(player2))
            .filter(c -> !c.isBreached() && !c.isExpired())
            .findFirst()
            .orElse(null);
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
        
        // KRİTİK: Kontrat tamamlandığında kan imzası canını geri ver (1 kalp = 2 can)
        if (contract.getAcceptor() != null) {
            restorePermanentHealth(contract.getAcceptor(), 1); // Kan imzası için 1 kalp geri ver
        }
        
        // Ödülü öde (EconomyManager kullan)
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
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
        
        // Kontrat geçmişine ekle
        addToContractHistory(contract);
        
        // Kontratı listeden kaldır
        activeContracts.remove(contract);
    }
    
    /**
     * Kontrat geçmişine ekle (ContractMenu için)
     */
    public void addToContractHistory(Contract contract) {
        if (contract == null) return;
        
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getContractMenu() != null) {
            plugin.getContractMenu().addContractToHistory(contract);
        }
    }
}

