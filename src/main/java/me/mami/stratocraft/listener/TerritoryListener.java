package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TerritoryListener implements Listener {
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;

    public TerritoryListener(TerritoryManager tm, SiegeManager sm) {
        this.territoryManager = tm;
        this.siegeManager = sm;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Clan owner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
        
        // Sahipsiz yerse kırılabilir
        if (owner == null) return;
        
        // Kendi yerinse kırılabilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        
        if (playerClan != null && playerClan.equals(owner)) {
            // Rütbe Kontrolü: Recruit (Acemi) yapı kıramaz
            if (playerClan.getRank(event.getPlayer().getUniqueId()) == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cAcemilerin yapı yıkma yetkisi yok!");
                return;
            }
            return; // Yetkisi varsa kırabilir
        }
        
        // Misafir İzni (Guest)
        if (owner.isGuest(event.getPlayer().getUniqueId())) {
             return; 
        }

        // --- ENERJİ KALKANI OFFLINE KORUMA ---
        // Eğer klan üyelerinden hiçbiri online değilse VE kalkan yakıtı > 0 ise hasarı iptal et
        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);
        
        if (core != null && core.isShieldActive()) {
            boolean anyOnline = owner.getMembers().keySet().stream()
                    .anyMatch(uuid -> org.bukkit.Bukkit.getPlayer(uuid) != null);
            
            if (!anyOnline) {
                // Offline koruma aktif
                event.setCancelled(true);
                event.getPlayer().sendMessage("§bEnerji Kalkanı aktif! Offline klan korunuyor. Kalkan Gücü: " + core.getShieldFuel());
                core.consumeFuel(); // Yakıt tüket
                return;
            }
        }

        // --- TAMAMLANMIŞ KUŞATMA KONTROLLERİ ---
        
        // Düşman bölgesi ise: SADECE KUŞATMA VARSA KIRILABİLİR
        if (siegeManager.isUnderSiege(owner)) {
            // Eğer Ana Kristali (Beacon) kırarsa oyunu bitir
            if (event.getBlock().getType() == Material.BEACON) {
                // Kalkan (Shield) Kontrolü
                if (core != null && core.isShieldActive()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§bKristal Enerji Kalkanı ile korunuyor! Kalkan Gücü: " + core.getShieldFuel());
                    return;
                }
                
                // Kalkan yoksa zafer!
                siegeManager.endSiege(playerClan, owner); // Kazanan: playerClan
                event.setDropItems(false);
                event.getBlock().setType(Material.AIR); // Kristali sil
                event.getPlayer().sendMessage("§6§lZAFER! Düşman kristalini parçaladın.");
            }
            return; // Kuşatma altındayken diğer blokları kırmaya izin ver (Stratejik yıkım)
        }

        // Koruma Aktif (Savaş yoksa dokunamazsın)
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cBu bölge " + owner.getName() + " klanına ait! Önce kuşatma başlatmalısın.");
    }

    @EventHandler
    public void onFuelAdd(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.COAL && event.getItem().getType() != Material.CHARCOAL) return;

        Clan owner = territoryManager.getTerritoryOwner(event.getClickedBlock().getLocation());
        if (owner == null) return;

        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);

        if (core == null) return;

        core.addFuel(10);
        event.getPlayer().sendMessage("§aKalkan Yakıtı Eklendi. Seviye: " + core.getShieldFuel());
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
}

