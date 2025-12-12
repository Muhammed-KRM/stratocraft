package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import me.mami.stratocraft.Main;

import java.util.HashMap;
import java.util.List;
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
    
    // ========== OYUNCU BAZLI ÖZEL SANDIK SİSTEMİ ==========
    
    // Oyuncu ID -> Özel Sandık Envanteri
    private final Map<UUID, Inventory> privateChests = new HashMap<>();
    
    /**
     * Çit ile çevrili sandık yerleştirme - Özel sandık oluştur
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrivateChestPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        
        // Sadece sandık tipleri
        if (material != Material.CHEST && 
            material != Material.TRAPPED_CHEST && 
            material != Material.BARREL &&
            material != Material.SHULKER_BOX) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Klan üyesi mi?
        Clan clan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return; // Klan üyesi değil
        
        // Klan bölgesinde mi?
        Clan owner = territoryManager.getTerritoryOwner(block.getLocation());
        if (owner == null || !owner.equals(clan)) {
            return; // Kendi bölgesinde değil
        }
        
        // Shift + sağ tık ile özel sandık işaretleme (PlayerInteractEvent'te yapılacak)
        // Burada sadece metadata ile işaretleme yapıyoruz
        // Özel sandık olarak işaretlemek için oyuncunun shift+sağ tık yapması gerekiyor
    }
    
    /**
     * Shift + Sağ Tık ile özel sandık işaretleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrivateChestMark(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return; // Shift basılı olmalı
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        Material material = block.getType();
        if (material != Material.CHEST && 
            material != Material.TRAPPED_CHEST && 
            material != Material.BARREL &&
            material != Material.SHULKER_BOX) {
            return;
        }
        
        // Klan üyesi mi?
        Clan clan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cÖzel sandık oluşturmak için klan üyesi olmalısın!");
            return;
        }
        
        // Klan bölgesinde mi?
        Clan owner = territoryManager.getTerritoryOwner(block.getLocation());
        if (owner == null || !owner.equals(clan)) {
            player.sendMessage("§cÖzel sandık sadece kendi klan bölgende oluşturulabilir!");
            return;
        }
        
        // Zaten özel sandık mı?
        if (block.hasMetadata("PrivateChestOwner")) {
            UUID existingOwner = null;
            List<MetadataValue> metadata = block.getMetadata("PrivateChestOwner");
            if (!metadata.isEmpty()) {
                existingOwner = UUID.fromString(metadata.get(0).asString());
            }
            
            if (existingOwner != null && existingOwner.equals(player.getUniqueId())) {
                // Sahibi bu oyuncu, özel sandığı kaldır
                block.removeMetadata("PrivateChestOwner", Main.getInstance());
                player.sendMessage("§eÖzel sandık işareti kaldırıldı. Artık tüm klan üyeleri açabilir.");
                return;
            } else if (existingOwner != null) {
                player.sendMessage("§cBu sandık başka bir oyuncuya ait!");
                return;
            }
        }
        
        // Özel sandık olarak işaretle
        block.setMetadata("PrivateChestOwner", 
            new FixedMetadataValue(Main.getInstance(), player.getUniqueId().toString()));
        event.setCancelled(true); // Sandık açılmasını engelle, özel envanter açacağız
        
        // Özel envanter oluştur veya mevcut olanı al
        Inventory privateInv = getPrivateChest(player.getUniqueId());
        player.openInventory(privateInv);
        player.sendMessage("§aÖzel sandık oluşturuldu! Sadece sen açabilirsin.");
        player.sendMessage("§7(İşareti kaldırmak için tekrar Shift + Sağ Tık yap)");
    }
    
    /**
     * Özel sandık açma kontrolü
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrivateChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        // Sadece blok envanterleri
        if (event.getInventory().getType() != InventoryType.CHEST &&
            event.getInventory().getType() != InventoryType.BARREL &&
            event.getInventory().getType() != InventoryType.SHULKER_BOX) {
            return;
        }
        
        // Envanterin konumunu bul
        Location invLocation = null;
        if (event.getInventory().getHolder() instanceof org.bukkit.block.BlockState) {
            org.bukkit.block.BlockState state = (org.bukkit.block.BlockState) event.getInventory().getHolder();
            invLocation = state.getLocation();
        } else if (event.getView().getTopInventory().getLocation() != null) {
            invLocation = event.getView().getTopInventory().getLocation();
        }
        
        if (invLocation == null) return;
        
        Block block = invLocation.getBlock();
        
        // Özel sandık mu?
        if (!block.hasMetadata("PrivateChestOwner")) {
            return; // Özel sandık değil, normal koruma sistemi devreye girer
        }
        
        // Sahibi kim?
        UUID ownerId = null;
        List<MetadataValue> metadata = block.getMetadata("PrivateChestOwner");
        if (!metadata.isEmpty()) {
            try {
                ownerId = UUID.fromString(metadata.get(0).asString());
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        
        if (ownerId == null) return;
        
        // Sahibi bu oyuncu mu?
        if (!player.getUniqueId().equals(ownerId)) {
            event.setCancelled(true);
            Player owner = org.bukkit.Bukkit.getPlayer(ownerId);
            String ownerName = owner != null ? owner.getName() : "Bilinmeyen";
            player.sendMessage("§cBu sandık §e" + ownerName + " §coyuncusuna ait! Sadece sahibi açabilir.");
            return;
        }
        
        // Sahibi bu oyuncu, özel envanteri aç
        event.setCancelled(true);
        Inventory privateInv = getPrivateChest(ownerId);
        player.openInventory(privateInv);
    }
    
    /**
     * Oyuncuya özel sandık envanteri al
     */
    private Inventory getPrivateChest(UUID playerId) {
        if (!privateChests.containsKey(playerId)) {
            Inventory inv = org.bukkit.Bukkit.createInventory(null, 27, 
                "§6Özel Sandık - " + org.bukkit.Bukkit.getOfflinePlayer(playerId).getName());
            privateChests.put(playerId, inv);
        }
        return privateChests.get(playerId);
    }
    
    /**
     * DataManager için
     */
    public Map<UUID, Inventory> getPrivateChests() {
        return privateChests;
    }
    
    public void setPrivateChest(UUID playerId, Inventory inv) {
        privateChests.put(playerId, inv);
    }
}
