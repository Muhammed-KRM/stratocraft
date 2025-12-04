package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ContractManager;
import me.mami.stratocraft.model.Contract;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.List;

/**
 * Kontrat sistemi dinleyicisi:
 * - Bounty Hunting: Oyuncu öldürüldüğünde ödül ödenir
 * - Bölge Yasağı: Yasak bölgeye girildiğinde ihlal
 * - Saldırmama Anlaşması: Saldırıldığında ihlal
 */
public class ContractListener implements Listener {
    private final ContractManager contractManager;
    
    public ContractListener(ContractManager contractManager) {
        this.contractManager = contractManager;
    }
    
    /**
     * Oyuncu Öldürme Takibi - Bounty Kontratları
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // KRİTİK KONTROL: Eğer kurban bir oyuncu tarafından öldürülmediyse (doğal ölüm, intihar, mob öldürdü)
        if (killer == null) return;
        
        // KRİTİK KONTROL: Oyuncu kendine suikast düzenleyip para kasmasın
        if (killer.equals(victim)) return;
        
        // ContractManager'dan kurbanın başına ödül olup olmadığını sor
        Contract bounty = contractManager.getBountyContract(victim.getUniqueId());
        
        if (bounty != null && bounty.getAcceptor() != null) {
            // Kontratı kabul eden öldürdü mü?
            if (bounty.getAcceptor().equals(killer.getUniqueId())) {
                contractManager.completeBountyContract(bounty, killer.getUniqueId());
            }
        }
    }
    
    /**
     * Oyuncu Hareket Takibi - Bölge Yasağı Kontrolü
     * PERFORMANS OPTİMİZASYONU: Sadece blok değiştiyse çalış + Cache kullanımı
     */
    // PERFORMANS: Cache - Her oyuncu için son kontrol zamanı (spam önleme)
    private final java.util.Map<java.util.UUID, Long> lastTerritoryCheck = new java.util.HashMap<>();
    private static final long TERRITORY_CHECK_COOLDOWN = 1000L; // 1 saniye cooldown
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış (X, Y, Z kontrolü)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş, işlem yapma
        }
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        
        // PERFORMANS: Cooldown kontrolü (spam önleme)
        long currentTime = System.currentTimeMillis();
        Long lastCheck = lastTerritoryCheck.get(player.getUniqueId());
        if (lastCheck != null && (currentTime - lastCheck) < TERRITORY_CHECK_COOLDOWN) {
            return; // Çok sık kontrol etme
        }
        lastTerritoryCheck.put(player.getUniqueId(), currentTime);
        
        // Oyuncunun aktif kontratlarını al (cache'den)
        List<Contract> contracts = contractManager.getPlayerContracts(player.getUniqueId());
        
        for (Contract contract : contracts) {
            if (contract.getType() == Contract.ContractType.TERRITORY_RESTRICT) {
                // Yasak bölge kontrolü
                List<Location> restrictedAreas = contract.getRestrictedAreas();
                if (restrictedAreas != null) {
                    for (Location restrictedCenter : restrictedAreas) {
                        double distance = to.distance(restrictedCenter);
                        if (distance <= contract.getRestrictedRadius()) {
                            // İHLAL! Yasak bölgeye girdi
                            contractManager.breachContract(contract, player.getUniqueId(), 
                                "Yasak bölgeye girdi: " + restrictedCenter.getBlockX() + ", " + 
                                restrictedCenter.getBlockZ());
                            return;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Saldırma Takibi - Non-Aggression Anlaşmaları
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Saldırmama anlaşması var mı?
        Contract nonAggression = contractManager.getNonAggressionContract(
            attacker.getUniqueId(), victim.getUniqueId());
        
        if (nonAggression != null) {
            // İHLAL! Saldırmama anlaşması var ama saldırdı
            contractManager.breachContract(nonAggression, attacker.getUniqueId(),
                "Saldırmama anlaşmasını ihlal etti: " + victim.getName());
        }
    }
}

