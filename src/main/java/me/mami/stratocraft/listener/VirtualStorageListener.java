package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualStorageListener implements Listener {
    private final TerritoryManager territoryManager;
    // Klan ID -> Sanal Envanter (Şubeler arası paylaşılan)
    private final Map<UUID, Inventory> virtualInventories = new HashMap<>();

    public VirtualStorageListener(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onVirtualStorageAccess(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        
        if (b.getType() != Material.ENDER_CHEST) return;

        Clan clan = territoryManager.getClanManager().getClanByPlayer(p.getUniqueId());
        if (clan == null) return;

        // Bu bölgede Sanal Bağlantı yapısı var mı?
        Structure virtualLink = clan.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.TELEPORTER && 
                            s.getLocation().distance(b.getLocation()) <= 10)
                .findFirst().orElse(null);

        if (virtualLink == null) return;

        // Sanal envanteri aç
        Inventory virtualInv = getVirtualInventory(clan.getId());
        p.openInventory(virtualInv);
        p.sendMessage("§bSanal Bağlantı envanteri açıldı! Şubeler arası paylaşılan depo.");
    }

    private Inventory getVirtualInventory(UUID clanId) {
        if (!virtualInventories.containsKey(clanId)) {
            // 54 slotluk bir envanter oluştur (çift sandık boyutu)
            Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, 
                "§5Sanal Bağlantı - Şubeler Arası Depo");
            virtualInventories.put(clanId, inv);
        }
        return virtualInventories.get(clanId);
    }
    
    // DataManager için
    public Map<UUID, Inventory> getVirtualInventories() {
        return virtualInventories;
    }
    
    public void setVirtualInventory(UUID clanId, Inventory inv) {
        virtualInventories.put(clanId, inv);
    }
}

